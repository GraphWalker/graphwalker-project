use std::process::{Child, Command};
use std::time::Duration;

use futures_util::{SinkExt, StreamExt};
use serde_json::{json, Value};
use tokio::time::{sleep, timeout};
use tokio_tungstenite::tungstenite::Message;

fn fixture(path: &str) -> String {
    format!("tests/fixtures/{}", path)
}

fn graphwalker_bin() -> String {
    assert_cmd::cargo::cargo_bin("graphwalker")
        .to_str()
        .unwrap()
        .to_string()
}

fn start_server(service: &str, port: u16) -> Child {
    start_server_with_args(service, port, &[])
}

fn start_server_with_args(service: &str, port: u16, extra_args: &[&str]) -> Child {
    let mut cmd = Command::new(graphwalker_bin());
    cmd.args(["online", "-s", service, "-p", &port.to_string()]);
    cmd.args(extra_args);
    cmd.stdout(std::process::Stdio::piped())
        .stderr(std::process::Stdio::piped())
        .spawn()
        .expect("Failed to start server")
}

async fn wait_for_port(port: u16) {
    for _ in 0..50 {
        if tokio::net::TcpStream::connect(format!("127.0.0.1:{}", port))
            .await
            .is_ok()
        {
            return;
        }
        sleep(Duration::from_millis(100)).await;
    }
    panic!("Server on port {} did not start within 5 seconds", port);
}

struct ServerGuard {
    child: Child,
}

impl ServerGuard {
    fn new(service: &str, port: u16) -> Self {
        Self {
            child: start_server(service, port),
        }
    }

    fn with_args(service: &str, port: u16, extra_args: &[&str]) -> Self {
        Self {
            child: start_server_with_args(service, port, extra_args),
        }
    }
}

impl Drop for ServerGuard {
    fn drop(&mut self) {
        let _ = self.child.kill();
        let _ = self.child.wait();
    }
}

fn small_model_json() -> String {
    std::fs::read_to_string(fixture("json/SmallModel.json")).unwrap()
}

// ---------------------------------------------------------------------------
// WebSocket helpers
// ---------------------------------------------------------------------------

async fn ws_connect(
    port: u16,
) -> tokio_tungstenite::WebSocketStream<
    tokio_tungstenite::MaybeTlsStream<tokio::net::TcpStream>,
> {
    let url = format!("ws://127.0.0.1:{}", port);
    let (ws, _) = tokio_tungstenite::connect_async(&url)
        .await
        .expect("Failed to connect WebSocket");
    ws
}

async fn ws_send(
    ws: &mut tokio_tungstenite::WebSocketStream<
        tokio_tungstenite::MaybeTlsStream<tokio::net::TcpStream>,
    >,
    msg: &Value,
) -> Value {
    ws.send(Message::Text(msg.to_string().into()))
        .await
        .expect("Failed to send message");

    let resp = timeout(Duration::from_secs(5), ws.next())
        .await
        .expect("Timeout waiting for response")
        .expect("Stream ended")
        .expect("Read error");

    serde_json::from_str::<Value>(&resp.into_text().unwrap()).expect("Invalid JSON response")
}

// ---------------------------------------------------------------------------
// REST tests
// ---------------------------------------------------------------------------

#[tokio::test]
async fn rest_load_has_next_get_next() {
    let port = 19100;
    let _server = ServerGuard::new("RESTFUL", port);
    wait_for_port(port).await;

    let client = reqwest::Client::new();
    let base = format!("http://127.0.0.1:{}/graphwalker", port);

    let resp: Value = client
        .post(format!("{}/load", base))
        .body(small_model_json())
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "ok");

    let resp: Value = client
        .get(format!("{}/hasNext", base))
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "ok");
    assert_eq!(resp["hasNext"], "true");

    let resp: Value = client
        .get(format!("{}/getNext", base))
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "ok");
    assert!(resp["currentElementName"].as_str().is_some());
}

#[tokio::test]
async fn rest_get_next_without_load_fails() {
    let port = 19101;
    let _server = ServerGuard::new("RESTFUL", port);
    wait_for_port(port).await;

    let client = reqwest::Client::new();
    let base = format!("http://127.0.0.1:{}/graphwalker", port);

    let resp: Value = client
        .get(format!("{}/getNext", base))
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "nok");
}

#[tokio::test]
async fn rest_full_traversal() {
    let port = 19102;
    let _server = ServerGuard::new("RESTFUL", port);
    wait_for_port(port).await;

    let client = reqwest::Client::new();
    let base = format!("http://127.0.0.1:{}/graphwalker", port);

    let resp: Value = client
        .post(format!("{}/load", base))
        .body(small_model_json())
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "ok");

    let mut visited = Vec::new();
    loop {
        let resp: Value = client
            .get(format!("{}/hasNext", base))
            .send()
            .await
            .unwrap()
            .json()
            .await
            .unwrap();
        if resp["hasNext"] != "true" {
            break;
        }

        let resp: Value = client
            .get(format!("{}/getNext", base))
            .send()
            .await
            .unwrap()
            .json()
            .await
            .unwrap();
        let name = resp["currentElementName"].as_str().unwrap().to_string();
        visited.push(name);

        if visited.len() > 500 {
            panic!("Traversal exceeded 500 steps, aborting");
        }
    }

    assert!(!visited.is_empty(), "Should have visited at least one element");
    assert!(visited.contains(&"e_FirstAction".to_string()));
    assert!(visited.contains(&"v_VerifySomeAction".to_string()));
    assert!(visited.contains(&"v_VerifySomeOtherAction".to_string()));
    assert!(visited.contains(&"e_AnotherAction".to_string()));
    assert!(visited.contains(&"e_SomeOtherAction".to_string()));
}

#[tokio::test]
async fn rest_get_data() {
    let port = 19103;
    let _server = ServerGuard::new("RESTFUL", port);
    wait_for_port(port).await;

    let client = reqwest::Client::new();
    let base = format!("http://127.0.0.1:{}/graphwalker", port);

    client
        .post(format!("{}/load", base))
        .body(small_model_json())
        .send()
        .await
        .unwrap();

    let resp: Value = client
        .get(format!("{}/getData", base))
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "ok");
    assert!(resp.get("data").is_some());
}

#[tokio::test]
async fn rest_set_data() {
    let port = 19104;
    let _server = ServerGuard::new("RESTFUL", port);
    wait_for_port(port).await;

    let client = reqwest::Client::new();
    let base = format!("http://127.0.0.1:{}/graphwalker", port);

    client
        .post(format!("{}/load", base))
        .body(small_model_json())
        .send()
        .await
        .unwrap();

    let resp: Value = client
        .put(format!("{}/setData/let x = 42;", base))
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "ok");

    let resp: Value = client
        .get(format!("{}/getData", base))
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    let data = resp["data"].as_str().unwrap();
    assert!(data.contains("x"), "Data should contain the variable we set");
}

#[tokio::test]
async fn rest_restart() {
    let port = 19105;
    let _server = ServerGuard::new("RESTFUL", port);
    wait_for_port(port).await;

    let client = reqwest::Client::new();
    let base = format!("http://127.0.0.1:{}/graphwalker", port);

    client
        .post(format!("{}/load", base))
        .body(small_model_json())
        .send()
        .await
        .unwrap();

    // Take a few steps
    for _ in 0..3 {
        client
            .get(format!("{}/getNext", base))
            .send()
            .await
            .unwrap();
    }

    let resp: Value = client
        .put(format!("{}/restart", base))
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "ok");

    // After restart, hasNext should be true again
    let resp: Value = client
        .get(format!("{}/hasNext", base))
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["hasNext"], "true");
}

#[tokio::test]
async fn rest_get_statistics() {
    let port = 19106;
    let _server = ServerGuard::new("RESTFUL", port);
    wait_for_port(port).await;

    let client = reqwest::Client::new();
    let base = format!("http://127.0.0.1:{}/graphwalker", port);

    client
        .post(format!("{}/load", base))
        .body(small_model_json())
        .send()
        .await
        .unwrap();

    client
        .get(format!("{}/getNext", base))
        .send()
        .await
        .unwrap();

    let resp: Value = client
        .get(format!("{}/getStatistics", base))
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "ok");
    assert!(resp.get("totalNumberOfEdges").is_some());
    assert!(resp.get("totalNumberOfVisitedEdges").is_some());
}

// ---------------------------------------------------------------------------
// WebSocket tests
// ---------------------------------------------------------------------------

#[tokio::test]
async fn ws_start_has_next_get_next() {
    let port = 19110;
    let _server = ServerGuard::new("WEBSOCKET", port);
    wait_for_port(port).await;

    let mut ws = ws_connect(port).await;

    let resp = ws_send(
        &mut ws,
        &json!({
            "command": "start",
            "gw": serde_json::from_str::<Value>(&small_model_json()).unwrap(),
        }),
    )
    .await;
    assert_eq!(resp["success"], true);
    assert!(resp.get("seed").is_some());

    let resp = ws_send(&mut ws, &json!({"command": "hasNext"})).await;
    assert_eq!(resp["success"], true);
    assert_eq!(resp["hasNext"], true);

    let resp = ws_send(&mut ws, &json!({"command": "getNext"})).await;
    assert_eq!(resp["success"], true);
    let name = resp["name"]
        .as_str()
        .or_else(|| resp["currentElementName"].as_str());
    assert!(name.is_some(), "Response should contain element name: {resp}");
}

#[tokio::test]
async fn ws_full_traversal() {
    let port = 19111;
    let _server = ServerGuard::new("WEBSOCKET", port);
    wait_for_port(port).await;

    let mut ws = ws_connect(port).await;

    let resp = ws_send(
        &mut ws,
        &json!({
            "command": "start",
            "gw": serde_json::from_str::<Value>(&small_model_json()).unwrap(),
        }),
    )
    .await;
    assert_eq!(resp["success"], true);

    let mut visited = Vec::new();
    loop {
        let resp = ws_send(&mut ws, &json!({"command": "hasNext"})).await;
        if resp["hasNext"] != true {
            break;
        }

        let resp = ws_send(&mut ws, &json!({"command": "getNext"})).await;
        let name = resp["name"]
            .as_str()
            .or_else(|| resp["currentElementName"].as_str())
            .unwrap()
            .to_string();
        visited.push(name);

        if visited.len() > 500 {
            panic!("Traversal exceeded 500 steps");
        }
    }

    assert!(!visited.is_empty());
    assert!(visited.contains(&"e_FirstAction".to_string()));
    assert!(visited.contains(&"v_VerifySomeAction".to_string()));
}

async fn ws_run_with_seed(port: u16, seed: u64) -> Vec<String> {
    let mut ws = ws_connect(port).await;

    let resp = ws_send(
        &mut ws,
        &json!({
            "command": "start",
            "seed": seed,
            "gw": serde_json::from_str::<Value>(&small_model_json()).unwrap(),
        }),
    )
    .await;
    assert_eq!(resp["success"], true);

    let mut names = Vec::new();
    for _ in 0..10 {
        let resp = ws_send(&mut ws, &json!({"command": "hasNext"})).await;
        if resp["hasNext"] != true {
            break;
        }
        let resp = ws_send(&mut ws, &json!({"command": "getNext"})).await;
        let name = resp["name"]
            .as_str()
            .or_else(|| resp["currentElementName"].as_str())
            .unwrap()
            .to_string();
        names.push(name);
    }
    names
}

#[tokio::test]
async fn ws_start_with_seed_is_deterministic() {
    let port = 19112;
    let _server = ServerGuard::new("WEBSOCKET", port);
    wait_for_port(port).await;

    let run1 = ws_run_with_seed(port, 42).await;
    let run2 = ws_run_with_seed(port, 42).await;

    assert_eq!(run1, run2, "Same seed should produce identical sequence");
}

#[tokio::test]
async fn ws_get_next_without_start_fails() {
    let port = 19113;
    let _server = ServerGuard::new("WEBSOCKET", port);
    wait_for_port(port).await;

    let mut ws = ws_connect(port).await;

    let resp = ws_send(&mut ws, &json!({"command": "getNext"})).await;
    assert_eq!(resp["success"], false);
}

#[tokio::test]
async fn ws_get_data() {
    let port = 19114;
    let _server = ServerGuard::new("WEBSOCKET", port);
    wait_for_port(port).await;

    let mut ws = ws_connect(port).await;

    ws_send(
        &mut ws,
        &json!({
            "command": "start",
            "gw": serde_json::from_str::<Value>(&small_model_json()).unwrap(),
        }),
    )
    .await;

    let resp = ws_send(&mut ws, &json!({"command": "getData"})).await;
    assert_eq!(resp["success"], true);
    assert!(resp.get("data").is_some());
}

#[tokio::test]
async fn ws_set_data() {
    let port = 19115;
    let _server = ServerGuard::new("WEBSOCKET", port);
    wait_for_port(port).await;

    let mut ws = ws_connect(port).await;

    ws_send(
        &mut ws,
        &json!({
            "command": "start",
            "gw": serde_json::from_str::<Value>(&small_model_json()).unwrap(),
        }),
    )
    .await;

    let resp = ws_send(
        &mut ws,
        &json!({"command": "setData", "action": "let myVar = 99;"}),
    )
    .await;
    assert_eq!(resp["success"], true);

    let resp = ws_send(&mut ws, &json!({"command": "getData"})).await;
    let data = resp["data"].as_str().unwrap();
    assert!(
        data.contains("myVar"),
        "getData should reflect the variable we set: {data}"
    );
}

#[tokio::test]
async fn ws_list_sessions_initially_empty() {
    let port = 19116;
    let _server = ServerGuard::new("WEBSOCKET", port);
    wait_for_port(port).await;

    let mut ws = ws_connect(port).await;

    let resp = ws_send(&mut ws, &json!({"command": "listSessions"})).await;
    assert_eq!(resp["success"], true);
    let sessions = resp["sessions"].as_array().unwrap();
    assert!(sessions.is_empty(), "No sessions should exist initially");
}

#[tokio::test]
async fn ws_start_creates_session() {
    let port = 19117;
    let _server = ServerGuard::new("WEBSOCKET", port);
    wait_for_port(port).await;

    // Start a session
    let mut runner = ws_connect(port).await;
    let resp = ws_send(
        &mut runner,
        &json!({
            "command": "start",
            "gw": serde_json::from_str::<Value>(&small_model_json()).unwrap(),
        }),
    )
    .await;
    assert_eq!(resp["success"], true);
    let session_id = resp["sessionId"].as_str().unwrap().to_string();
    assert!(!session_id.is_empty());

    // A fresh connection should see the session via listSessions
    let mut observer = ws_connect(port).await;
    let resp = ws_send(&mut observer, &json!({"command": "listSessions"})).await;
    assert_eq!(resp["success"], true);
    let sessions = resp["sessions"].as_array().unwrap();
    assert_eq!(sessions.len(), 1);
    assert_eq!(sessions[0]["id"].as_str().unwrap(), session_id);
}

#[tokio::test]
async fn ws_subscribe_session() {
    let port = 19118;
    let _server = ServerGuard::new("WEBSOCKET", port);
    wait_for_port(port).await;

    // Start a session
    let mut runner = ws_connect(port).await;
    let resp = ws_send(
        &mut runner,
        &json!({
            "command": "start",
            "gw": serde_json::from_str::<Value>(&small_model_json()).unwrap(),
        }),
    )
    .await;
    let session_id = resp["sessionId"].as_str().unwrap().to_string();

    // Take a step so there's visited state
    ws_send(&mut runner, &json!({"command": "hasNext"})).await;
    ws_send(&mut runner, &json!({"command": "getNext"})).await;

    // Subscribe from observer
    let mut observer = ws_connect(port).await;
    let resp = ws_send(
        &mut observer,
        &json!({"command": "subscribeSession", "sessionId": session_id}),
    )
    .await;
    assert_eq!(resp["success"], true);
    assert!(resp.get("models").is_some(), "Subscribe should return models");
    assert!(resp.get("elements").is_some(), "Subscribe should return elements snapshot");
    assert!(resp.get("seed").is_some(), "Subscribe should return seed");
}

#[tokio::test]
async fn ws_unknown_command() {
    let port = 19119;
    let _server = ServerGuard::new("WEBSOCKET", port);
    wait_for_port(port).await;

    let mut ws = ws_connect(port).await;

    let resp = ws_send(&mut ws, &json!({"command": "nonExistentCommand"})).await;
    assert_eq!(resp["success"], false);
}

#[tokio::test]
async fn ws_login_model_with_guards() {
    let port = 19120;
    let _server = ServerGuard::new("WEBSOCKET", port);
    wait_for_port(port).await;

    let mut ws = ws_connect(port).await;

    let login_json: Value =
        serde_json::from_str(&std::fs::read_to_string(fixture("json/Login.json")).unwrap())
            .unwrap();

    let resp = ws_send(
        &mut ws,
        &json!({
            "command": "start",
            "gw": login_json,
        }),
    )
    .await;
    assert_eq!(resp["success"], true);

    let mut visited = Vec::new();
    loop {
        let resp = ws_send(&mut ws, &json!({"command": "hasNext"})).await;
        if resp["hasNext"] != true {
            break;
        }

        let resp = ws_send(&mut ws, &json!({"command": "getNext"})).await;
        assert_eq!(resp["success"], true);
        let name = resp["name"]
            .as_str()
            .or_else(|| resp["currentElementName"].as_str())
            .unwrap()
            .to_string();
        visited.push(name);

        if visited.len() > 500 {
            panic!("Traversal exceeded 500 steps");
        }
    }

    assert!(visited.contains(&"e_Init".to_string()));
    assert!(visited.contains(&"v_ClientNotRunning".to_string()));
    assert!(visited.contains(&"v_Browse".to_string()));
}

// ---------------------------------------------------------------------------
// REST seed tests
// ---------------------------------------------------------------------------

async fn rest_run_traversal(port: u16) -> Vec<String> {
    let client = reqwest::Client::new();
    let base = format!("http://127.0.0.1:{}/graphwalker", port);

    let resp: Value = client
        .post(format!("{}/load", base))
        .body(small_model_json())
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "ok");

    let mut names = Vec::new();
    loop {
        let resp: Value = client
            .get(format!("{}/hasNext", base))
            .send()
            .await
            .unwrap()
            .json()
            .await
            .unwrap();
        if resp["hasNext"] != "true" {
            break;
        }
        let resp: Value = client
            .get(format!("{}/getNext", base))
            .send()
            .await
            .unwrap()
            .json()
            .await
            .unwrap();
        names.push(resp["currentElementName"].as_str().unwrap().to_string());
        if names.len() > 500 {
            panic!("Traversal exceeded 500 steps");
        }
    }
    names
}

#[tokio::test]
async fn rest_seed_deterministic() {
    let port_a = 19130;
    let port_b = 19131;
    let _server_a = ServerGuard::with_args("RESTFUL", port_a, &["--seed", "42"]);
    let _server_b = ServerGuard::with_args("RESTFUL", port_b, &["--seed", "42"]);
    wait_for_port(port_a).await;
    wait_for_port(port_b).await;

    let run_a = rest_run_traversal(port_a).await;
    let run_b = rest_run_traversal(port_b).await;

    assert_eq!(run_a, run_b, "Same seed should produce identical traversal");
}

#[tokio::test]
async fn rest_seed_different_seeds_diverge() {
    let port_a = 19132;
    let port_b = 19133;
    let _server_a = ServerGuard::with_args("RESTFUL", port_a, &["--seed", "42"]);
    let _server_b = ServerGuard::with_args("RESTFUL", port_b, &["--seed", "99"]);
    wait_for_port(port_a).await;
    wait_for_port(port_b).await;

    let run_a = rest_run_traversal(port_a).await;
    let run_b = rest_run_traversal(port_b).await;

    assert_ne!(run_a, run_b, "Different seeds should produce different traversals");
}

#[tokio::test]
async fn rest_seed_load_returns_seed() {
    let port = 19134;
    let _server = ServerGuard::with_args("RESTFUL", port, &["--seed", "12345"]);
    wait_for_port(port).await;

    let client = reqwest::Client::new();
    let base = format!("http://127.0.0.1:{}/graphwalker", port);

    let resp: Value = client
        .post(format!("{}/load", base))
        .body(small_model_json())
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "ok");
    assert_eq!(resp["seed"], 12345, "Load response should echo back the seed");
}

#[tokio::test]
async fn rest_no_seed_returns_generated_seed() {
    let port = 19135;
    let _server = ServerGuard::new("RESTFUL", port);
    wait_for_port(port).await;

    let client = reqwest::Client::new();
    let base = format!("http://127.0.0.1:{}/graphwalker", port);

    let resp: Value = client
        .post(format!("{}/load", base))
        .body(small_model_json())
        .send()
        .await
        .unwrap()
        .json()
        .await
        .unwrap();
    assert_eq!(resp["result"], "ok");
    assert!(
        resp.get("seed").and_then(|v| v.as_u64()).is_some(),
        "Load response should contain a generated seed"
    );
}
