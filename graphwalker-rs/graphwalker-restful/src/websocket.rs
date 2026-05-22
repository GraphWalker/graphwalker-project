use std::collections::HashSet;
use std::time::Duration;

use axum::extract::ws::{Message, WebSocket};
use futures::{SinkExt, StreamExt};
use serde_json::{json, Value};
use tokio::sync::{broadcast, oneshot};
use tracing::{debug, warn};

use crate::actor::{self, spawn_machine_thread, Command};
use crate::session::{SessionHandle, SessionManager};

pub async fn handle_socket(socket: WebSocket, session_mgr: SessionManager) {
    let (mut ws_write, mut ws_read) = socket.split();

    let mut own_session: Option<SessionHandle> = None;
    let mut subscribed_rx: Option<broadcast::Receiver<Value>> = None;
    let mut subscribed_session_id: Option<String> = None;
    let mut change_rx: Option<broadcast::Receiver<Value>> = None;

    debug!("new websocket connection");

    loop {
        tokio::select! {
            msg = ws_read.next() => {
                let text = match msg {
                    Some(Ok(Message::Text(t))) => t.to_string(),
                    Some(Ok(Message::Close(_))) | None => {
                        debug!("websocket closed by client");
                        break;
                    }
                    _ => continue,
                };

                let response = match serde_json::from_str::<Value>(&text) {
                    Ok(request) => {
                        let cmd = request.get("command").and_then(|c| c.as_str()).unwrap_or("?");
                        debug!(command = cmd, "recv command");
                        process_command(
                            &session_mgr,
                            &mut own_session,
                            &mut subscribed_rx,
                            &mut subscribed_session_id,
                            &mut change_rx,
                            &request,
                        )
                        .await
                    }
                    Err(e) => json!({
                        "command": "unknown",
                        "success": false,
                        "message": e.to_string(),
                    }),
                };

                let resp_cmd = response.get("command").and_then(|c| c.as_str()).unwrap_or("?");
                let resp_ok = response.get("success").and_then(|v| v.as_bool()).unwrap_or(false);
                debug!(command = resp_cmd, success = resp_ok, "send response");

                if ws_write
                    .send(Message::Text(response.to_string().into()))
                    .await
                    .is_err()
                {
                    warn!("ws_write failed sending response");
                    break;
                }
            }

            event = async {
                match &mut subscribed_rx {
                    Some(rx) => rx.recv().await,
                    None => std::future::pending().await,
                }
            } => {
                match event {
                    Ok(value) => {
                        let cmd = value.get("command").and_then(|c| c.as_str()).unwrap_or("?");
                        debug!(command = cmd, "forward broadcast to subscriber");
                        if ws_write
                            .send(Message::Text(value.to_string().into()))
                            .await
                            .is_err()
                        {
                            warn!("ws_write failed sending broadcast");
                            break;
                        }
                    }
                    Err(broadcast::error::RecvError::Lagged(n)) => {
                        warn!(skipped = n, "broadcast receiver lagged");
                    }
                    Err(broadcast::error::RecvError::Closed) => {
                        debug!("broadcast channel closed, clearing subscription");
                        subscribed_rx = None;
                    }
                }
            }

            event = async {
                match &mut change_rx {
                    Some(rx) => rx.recv().await,
                    None => std::future::pending().await,
                }
            } => {
                match event {
                    Ok(value) => {
                        let cmd = value.get("command").and_then(|c| c.as_str()).unwrap_or("?");
                        debug!(command = cmd, "forward change event");
                        if ws_write
                            .send(Message::Text(value.to_string().into()))
                            .await
                            .is_err()
                        {
                            warn!("ws_write failed sending change event");
                            break;
                        }
                    }
                    Err(_) => {}
                }
            }
        }
    }

    debug!("websocket handler cleaning up");

    if let Some(sid) = subscribed_session_id {
        debug!(session_id = %sid, "resetting control for watched session");
        if let Some(session) = session_mgr.get_session(&sid) {
            session.control.reset();
        }
    }

    if let Some(session) = own_session {
        debug!(session_id = %session.id, "removing owned session");
        let _ = session.broadcast_tx.send(json!({
            "command": "sessionEnded",
            "sessionId": session.id,
        }));
        session_mgr.remove_session(&session.id);
    }
}

async fn send_command(
    tx: &std::sync::mpsc::Sender<Command>,
    build: impl FnOnce(oneshot::Sender<Result<Value, String>>) -> Command,
) -> Result<Value, String> {
    let (reply_tx, reply_rx) = oneshot::channel();
    tx.send(build(reply_tx))
        .map_err(|_| "Machine thread unavailable".to_string())?;
    match reply_rx.await {
        Ok(result) => result,
        Err(_) => Err("Machine thread dropped".to_string()),
    }
}

fn get_target_session(
    session_mgr: &SessionManager,
    request: &Value,
) -> Result<SessionHandle, String> {
    let session_id = request
        .get("sessionId")
        .and_then(|v| v.as_str())
        .ok_or("Missing 'sessionId' field")?;
    session_mgr
        .get_session(session_id)
        .ok_or_else(|| format!("Session '{}' not found", session_id))
}

async fn process_command(
    session_mgr: &SessionManager,
    own_session: &mut Option<SessionHandle>,
    subscribed_rx: &mut Option<broadcast::Receiver<Value>>,
    subscribed_session_id: &mut Option<String>,
    change_rx: &mut Option<broadcast::Receiver<Value>>,
    request: &Value,
) -> Value {
    let command = request
        .get("command")
        .and_then(|c| c.as_str())
        .unwrap_or("")
        .to_uppercase();

    let result = match command.as_str() {
        "MODE" => Ok(json!({
            "command": "mode",
            "mode": "EDITOR",
            "success": true,
        })),
        "CHECK" => cmd_check(request),
        "CONVERTGRAPHML" => cmd_convert_graphml(request),
        "START" => cmd_start(session_mgr, own_session, request).await,
        "GETNEXT" => cmd_get_next(own_session).await,
        "HASNEXT" => cmd_has_next(own_session).await,
        "GETDATA" => cmd_get_data(own_session).await,
        "SETDATA" => cmd_set_data(own_session, request).await,
        "GETMODEL" => cmd_get_model(own_session).await,
        "UPDATEALLELEMENTS" => cmd_update_all_elements(own_session).await,
        "LISTSESSIONS" => cmd_list_sessions(session_mgr, change_rx),
        "SUBSCRIBESESSION" => {
            cmd_subscribe_session(session_mgr, subscribed_rx, subscribed_session_id, request).await
        }
        "UNSUBSCRIBESESSION" => {
            cmd_unsubscribe_session(session_mgr, subscribed_rx, subscribed_session_id)
        }
        "PAUSESESSION" => cmd_pause_session(session_mgr, request),
        "RESUMESESSION" => cmd_resume_session(session_mgr, request),
        "STEPSESSION" => cmd_step_session(session_mgr, request),
        "SETDELAY" => cmd_set_delay(session_mgr, request),
        "SETBREAKPOINTS" => cmd_set_breakpoints(session_mgr, request),
        _ => Ok(json!({
            "command": command.to_lowercase(),
            "success": false,
            "message": format!("Unknown command: {}", command),
        })),
    };

    match result {
        Ok(val) => val,
        Err(msg) => {
            warn!(command = %command, error = %msg, "command failed");
            json!({
                "command": command.to_lowercase(),
                "success": false,
                "message": msg,
            })
        }
    }
}

fn cmd_check(request: &Value) -> Result<Value, String> {
    let gw = request.get("gw").ok_or("Missing 'gw' field")?;
    let val = actor::handle_check(&gw.to_string())?;
    Ok(json!({
        "command": "check",
        "issues": val.get("issues").unwrap_or(&json!([])),
        "success": true,
    }))
}

fn cmd_convert_graphml(request: &Value) -> Result<Value, String> {
    let graphml = request
        .get("graphml")
        .and_then(|g| g.as_str())
        .ok_or("Missing 'graphml' field")?;
    let val = actor::handle_convert_graphml(graphml)?;
    Ok(json!({
        "command": "convertGraphml",
        "models": val.get("models").unwrap_or(&json!("")),
        "success": true,
    }))
}

async fn cmd_start(
    session_mgr: &SessionManager,
    own_session: &mut Option<SessionHandle>,
    request: &Value,
) -> Result<Value, String> {
    if let Some(prev) = own_session.take() {
        debug!(session_id = %prev.id, "removing previous session");
        session_mgr.remove_session(&prev.id);
    }

    let gw = request.get("gw").ok_or("Missing 'gw' field")?;
    let json_body = gw.to_string();
    let seed = request.get("seed").and_then(|v| v.as_u64());
    let global_data = request
        .get("globalData")
        .and_then(|v| v.as_str())
        .map(|s| s.to_string());
    let session_name = request
        .get("name")
        .and_then(|v| v.as_str())
        .map(|s| s.to_string())
        .unwrap_or_else(|| {
            gw.get("models")
                .and_then(|m| m.as_array())
                .and_then(|arr| arr.first())
                .and_then(|m| m.get("name"))
                .and_then(|n| n.as_str())
                .unwrap_or("Unnamed")
                .to_string()
        });

    let machine_tx = spawn_machine_thread();

    let val = send_command(&machine_tx, |reply| Command::Load {
        json_body: json_body.clone(),
        seed,
        global_data,
        reply,
    })
    .await?;

    let actual_seed = val.get("seed").and_then(|v| v.as_u64());
    let handle =
        session_mgr.create_session(session_name.clone(), json_body, actual_seed, machine_tx);
    let session_id = handle.id.clone();
    *own_session = Some(handle);

    debug!(session_id = %session_id, name = %session_name, seed = ?actual_seed, "session started");

    Ok(json!({
        "command": "start",
        "success": true,
        "seed": val.get("seed").unwrap_or(&json!(0)),
        "sessionId": session_id,
    }))
}

async fn cmd_get_next(own_session: &Option<SessionHandle>) -> Result<Value, String> {
    let session = own_session.as_ref().ok_or("No active session")?;

    debug!(session_id = %session.id, paused = session.control.is_paused(), "getNext: entering gate");
    session.control.gate().await;
    debug!(session_id = %session.id, "getNext: gate passed");

    let val = send_command(&session.machine_tx, |reply| Command::GetNext {
        verbose: true,
        reply,
    })
    .await?;

    let model_id = val
        .get("modelId")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    let element_id = val
        .get("currentElementID")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    let name = val
        .get("currentElementName")
        .and_then(|v| v.as_str())
        .unwrap_or("");

    debug!(session_id = %session.id, element = %name, "getNext: got element");

    let response = json!({
        "command": "visitedElement",
        "modelId": val.get("modelId").unwrap_or(&json!("")),
        "elementId": val.get("currentElementID").unwrap_or(&json!("")),
        "name": val.get("currentElementName").unwrap_or(&json!("")),
        "visitedCount": val.get("visitedCount").unwrap_or(&json!(0)),
        "totalCount": val.get("totalCount").unwrap_or(&json!(0)),
        "stopConditionFulfillment": val.get("stopConditionFulfillment").unwrap_or(&json!(0.0)),
        "data": val.get("data").unwrap_or(&json!("")),
        "success": true,
    });

    let subscribers = session.broadcast_tx.send(response.clone()).unwrap_or(0);
    debug!(session_id = %session.id, subscribers, "getNext: broadcast sent");

    if session
        .control
        .check_and_pause_if_breakpoint(model_id, element_id)
    {
        debug!(session_id = %session.id, element = %name, "breakpoint hit, pausing");
        let _ = session.broadcast_tx.send(json!({
            "command": "sessionPaused",
            "reason": "breakpoint",
            "modelId": model_id,
            "elementId": element_id,
        }));
    }

    let delay_ms = session.control.delay_ms();
    if delay_ms > 0 {
        debug!(session_id = %session.id, delay_ms, "getNext: sleeping");
        tokio::time::sleep(Duration::from_millis(delay_ms)).await;
    }

    Ok(response)
}

async fn cmd_has_next(own_session: &Option<SessionHandle>) -> Result<Value, String> {
    let session = own_session.as_ref().ok_or("No active session")?;
    let val = send_command(&session.machine_tx, |reply| Command::HasNext { reply }).await?;
    let has_next = val
        .get("hasNext")
        .and_then(|v| v.as_str())
        .unwrap_or("false");

    Ok(json!({
        "command": "hasNext",
        "hasNext": has_next == "true",
        "success": true,
    }))
}

async fn cmd_get_data(own_session: &Option<SessionHandle>) -> Result<Value, String> {
    let session = own_session.as_ref().ok_or("No active session")?;
    let val = send_command(&session.machine_tx, |reply| Command::GetData { reply }).await?;
    Ok(json!({
        "command": "getData",
        "data": val.get("data").unwrap_or(&json!("")),
        "success": true,
    }))
}

async fn cmd_set_data(
    own_session: &Option<SessionHandle>,
    request: &Value,
) -> Result<Value, String> {
    let session = own_session.as_ref().ok_or("No active session")?;
    let script = request
        .get("action")
        .and_then(|a| a.as_str())
        .ok_or("Missing 'action' field")?
        .to_string();
    send_command(&session.machine_tx, |reply| Command::SetData { script, reply }).await?;
    Ok(json!({"command": "setData", "success": true}))
}

async fn cmd_get_model(own_session: &Option<SessionHandle>) -> Result<Value, String> {
    let session = own_session.as_ref().ok_or("No active session")?;
    let val = send_command(&session.machine_tx, |reply| Command::GetModel { reply }).await?;
    Ok(json!({
        "command": "getModel",
        "models": val.get("models").unwrap_or(&json!("")),
        "success": true,
    }))
}

async fn cmd_update_all_elements(own_session: &Option<SessionHandle>) -> Result<Value, String> {
    let session = own_session.as_ref().ok_or("No active session")?;
    let val =
        send_command(&session.machine_tx, |reply| Command::UpdateAllElements { reply }).await?;
    Ok(json!({
        "command": "updateAllElements",
        "elements": val.get("elements").unwrap_or(&json!([])),
        "success": true,
    }))
}

fn cmd_list_sessions(
    session_mgr: &SessionManager,
    change_rx: &mut Option<broadcast::Receiver<Value>>,
) -> Result<Value, String> {
    if change_rx.is_none() {
        *change_rx = Some(session_mgr.subscribe_changes());
    }

    let sessions = session_mgr.list_sessions();
    let list: Vec<Value> = sessions
        .iter()
        .map(|s| {
            json!({
                "id": s.id,
                "name": s.name,
            })
        })
        .collect();

    debug!(count = sessions.len(), "listSessions");

    Ok(json!({
        "command": "sessions",
        "sessions": list,
        "success": true,
    }))
}

async fn cmd_subscribe_session(
    session_mgr: &SessionManager,
    subscribed_rx: &mut Option<broadcast::Receiver<Value>>,
    subscribed_session_id: &mut Option<String>,
    request: &Value,
) -> Result<Value, String> {
    if let Some(prev_id) = subscribed_session_id.take() {
        debug!(prev_session = %prev_id, "resetting previous subscription");
        if let Some(prev_session) = session_mgr.get_session(&prev_id) {
            prev_session.control.reset();
        }
    }

    let session_id = request
        .get("sessionId")
        .and_then(|v| v.as_str())
        .ok_or("Missing 'sessionId' field")?;

    let session = session_mgr
        .get_session(session_id)
        .ok_or_else(|| format!("Session '{}' not found", session_id))?;

    debug!(session_id = %session_id, "fetching element snapshot");

    let elements = send_command(&session.machine_tx, |reply| Command::UpdateAllElements {
        reply,
    })
    .await
    .ok()
    .and_then(|v| v.get("elements").cloned())
    .unwrap_or(json!([]));

    let models_value = send_command(&session.machine_tx, |reply| Command::GetModel { reply })
        .await
        .ok()
        .and_then(|v| v.get("models").and_then(|m| m.as_str()).map(String::from))
        .and_then(|s| serde_json::from_str::<Value>(&s).ok())
        .unwrap_or_else(|| serde_json::from_str::<Value>(&session.model_json).unwrap_or(json!(null)));

    let elem_count = elements.as_array().map(|a| a.len()).unwrap_or(0);
    debug!(session_id = %session_id, elements = elem_count, "subscribing to broadcast");

    *subscribed_rx = Some(session.broadcast_tx.subscribe());
    *subscribed_session_id = Some(session_id.to_string());

    Ok(json!({
        "command": "subscribeSession",
        "sessionId": session.id,
        "name": session.name,
        "models": models_value,
        "elements": elements,
        "seed": session.seed,
        "paused": session.control.is_paused(),
        "success": true,
    }))
}

fn cmd_unsubscribe_session(
    session_mgr: &SessionManager,
    subscribed_rx: &mut Option<broadcast::Receiver<Value>>,
    subscribed_session_id: &mut Option<String>,
) -> Result<Value, String> {
    if let Some(sid) = subscribed_session_id.take() {
        debug!(session_id = %sid, "unsubscribing, resetting control");
        if let Some(session) = session_mgr.get_session(&sid) {
            session.control.reset();
        }
    }
    *subscribed_rx = None;
    Ok(json!({
        "command": "unsubscribeSession",
        "success": true,
    }))
}

fn cmd_pause_session(session_mgr: &SessionManager, request: &Value) -> Result<Value, String> {
    let session = get_target_session(session_mgr, request)?;
    debug!(session_id = %session.id, "pauseSession");
    session.control.pause();
    let _ = session.broadcast_tx.send(json!({
        "command": "sessionPaused",
        "sessionId": session.id,
    }));
    Ok(json!({"command": "pauseSession", "success": true}))
}

fn cmd_resume_session(session_mgr: &SessionManager, request: &Value) -> Result<Value, String> {
    let session = get_target_session(session_mgr, request)?;
    debug!(session_id = %session.id, "resumeSession");
    session.control.resume();
    let _ = session.broadcast_tx.send(json!({
        "command": "sessionResumed",
        "sessionId": session.id,
    }));
    Ok(json!({"command": "resumeSession", "success": true}))
}

fn cmd_step_session(session_mgr: &SessionManager, request: &Value) -> Result<Value, String> {
    let session = get_target_session(session_mgr, request)?;
    debug!(session_id = %session.id, "stepSession (pause + step)");
    session.control.pause();
    session.control.step();
    Ok(json!({"command": "stepSession", "success": true}))
}

fn cmd_set_delay(session_mgr: &SessionManager, request: &Value) -> Result<Value, String> {
    let session = get_target_session(session_mgr, request)?;
    let ms = request
        .get("value")
        .and_then(|v| v.as_u64())
        .unwrap_or(0);
    debug!(session_id = %session.id, delay_ms = ms, "setDelay");
    session.control.set_delay(ms);
    Ok(json!({"command": "setDelay", "success": true}))
}

fn cmd_set_breakpoints(session_mgr: &SessionManager, request: &Value) -> Result<Value, String> {
    let session = get_target_session(session_mgr, request)?;
    let bps: HashSet<String> = request
        .get("breakpoints")
        .and_then(|v| v.as_array())
        .map(|arr| {
            arr.iter()
                .filter_map(|v| v.as_str().map(|s| s.to_string()))
                .collect()
        })
        .unwrap_or_default();
    debug!(session_id = %session.id, count = bps.len(), "setBreakpoints");
    session.control.set_breakpoints(bps);
    Ok(json!({"command": "setBreakpoints", "success": true}))
}
