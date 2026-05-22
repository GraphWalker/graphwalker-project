use std::sync::mpsc;

use axum::extract::{Path, State};
use axum::http::StatusCode;
use axum::response::Json;
use serde_json::{json, Value};
use tokio::sync::oneshot;

use crate::actor::Command;

#[derive(Clone)]
pub struct RestState {
    pub machine_tx: mpsc::Sender<Command>,
    pub default_seed: Option<u64>,
}

async fn send_command(
    tx: &mpsc::Sender<Command>,
    build: impl FnOnce(oneshot::Sender<Result<Value, String>>) -> Command,
) -> (StatusCode, Json<Value>) {
    let (reply_tx, reply_rx) = oneshot::channel();
    if tx.send(build(reply_tx)).is_err() {
        return (
            StatusCode::INTERNAL_SERVER_ERROR,
            Json(json!({"result": "nok", "error": "Machine thread unavailable"})),
        );
    }
    match reply_rx.await {
        Ok(Ok(val)) => (StatusCode::OK, Json(val)),
        Ok(Err(msg)) => (StatusCode::OK, Json(json!({"result": "nok", "error": msg}))),
        Err(_) => (
            StatusCode::INTERNAL_SERVER_ERROR,
            Json(json!({"result": "nok", "error": "Machine thread dropped"})),
        ),
    }
}

pub async fn load(State(state): State<RestState>, body: String) -> (StatusCode, Json<Value>) {
    send_command(&state.machine_tx, |reply| Command::Load {
        json_body: body,
        seed: state.default_seed,
        global_data: None,
        reply,
    })
    .await
}

pub async fn has_next(State(state): State<RestState>) -> (StatusCode, Json<Value>) {
    send_command(&state.machine_tx, |reply| Command::HasNext { reply }).await
}

pub async fn get_next(State(state): State<RestState>) -> (StatusCode, Json<Value>) {
    send_command(&state.machine_tx, |reply| Command::GetNext {
        verbose: false,
        reply,
    })
    .await
}

pub async fn get_data(State(state): State<RestState>) -> (StatusCode, Json<Value>) {
    send_command(&state.machine_tx, |reply| Command::GetData { reply }).await
}

pub async fn set_data(
    State(state): State<RestState>,
    Path(script): Path<String>,
) -> (StatusCode, Json<Value>) {
    send_command(&state.machine_tx, |reply| Command::SetData { script, reply }).await
}

pub async fn restart(State(state): State<RestState>) -> (StatusCode, Json<Value>) {
    send_command(&state.machine_tx, |reply| Command::Restart { reply }).await
}

pub async fn get_statistics(State(state): State<RestState>) -> (StatusCode, Json<Value>) {
    send_command(&state.machine_tx, |reply| Command::GetStatistics { reply }).await
}
