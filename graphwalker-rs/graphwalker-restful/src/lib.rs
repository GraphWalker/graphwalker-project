pub mod actor;
pub mod rest;
pub mod session;
pub mod websocket;

use std::net::SocketAddr;

use axum::extract::{State, WebSocketUpgrade};
use axum::response::IntoResponse;
use axum::routing::{get, post, put};
use axum::Router;
use tower_http::cors::CorsLayer;

use crate::actor::spawn_machine_thread;
use crate::session::SessionManager;

pub async fn start_rest_server(
    port: u16,
    seed: Option<u64>,
) -> Result<(), Box<dyn std::error::Error>> {
    let tx = spawn_machine_thread();
    let app = build_rest_router(rest::RestState {
        machine_tx: tx,
        default_seed: seed,
    });
    let addr = SocketAddr::from(([0, 0, 0, 0], port));
    eprintln!("GraphWalker REST server listening on http://{}", addr);

    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;
    Ok(())
}

pub async fn start_websocket_server(port: u16) -> Result<(), Box<dyn std::error::Error>> {
    let session_mgr = SessionManager::new();
    let app = build_websocket_router(session_mgr);
    let addr = SocketAddr::from(([0, 0, 0, 0], port));
    eprintln!("GraphWalker WebSocket server listening on ws://{}", addr);

    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;
    Ok(())
}

fn build_rest_router(state: rest::RestState) -> Router {
    Router::new()
        .route("/graphwalker/load", post(rest::load))
        .route("/graphwalker/hasNext", get(rest::has_next))
        .route("/graphwalker/getNext", get(rest::get_next))
        .route("/graphwalker/getData", get(rest::get_data))
        .route("/graphwalker/setData/{script}", put(rest::set_data))
        .route("/graphwalker/restart", put(rest::restart))
        .route("/graphwalker/getStatistics", get(rest::get_statistics))
        .layer(CorsLayer::permissive())
        .with_state(state)
}

fn build_websocket_router(session_mgr: SessionManager) -> Router {
    Router::new()
        .route("/", get(ws_upgrade_handler))
        .route("/graphwalker", get(ws_upgrade_handler))
        .layer(CorsLayer::permissive())
        .with_state(session_mgr)
}

async fn ws_upgrade_handler(
    ws: WebSocketUpgrade,
    State(session_mgr): State<SessionManager>,
) -> impl IntoResponse {
    ws.on_upgrade(move |socket| websocket::handle_socket(socket, session_mgr))
}
