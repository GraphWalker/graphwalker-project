use std::collections::{HashMap, HashSet};
use std::sync::atomic::{AtomicU64, Ordering};
use std::sync::{Arc, Mutex, RwLock};

use serde_json::{json, Value};
use tokio::sync::{broadcast, Notify};

use crate::actor::Command;

pub struct ExecutionControl {
    inner: Mutex<ControlState>,
    notify: Notify,
}

struct ControlState {
    paused: bool,
    step_once: bool,
    delay_ms: u64,
    breakpoints: HashSet<String>,
}

impl ExecutionControl {
    pub fn new() -> Self {
        Self {
            inner: Mutex::new(ControlState {
                paused: false,
                step_once: false,
                delay_ms: 0,
                breakpoints: HashSet::new(),
            }),
            notify: Notify::new(),
        }
    }

    pub async fn gate(&self) {
        loop {
            {
                let mut state = self.inner.lock().unwrap();
                if !state.paused {
                    return;
                }
                if state.step_once {
                    state.step_once = false;
                    return;
                }
            }
            self.notify.notified().await;
        }
    }

    pub fn pause(&self) {
        self.inner.lock().unwrap().paused = true;
    }

    pub fn resume(&self) {
        self.inner.lock().unwrap().paused = false;
        self.notify.notify_waiters();
    }

    pub fn step(&self) {
        self.inner.lock().unwrap().step_once = true;
        self.notify.notify_waiters();
    }

    pub fn set_delay(&self, ms: u64) {
        self.inner.lock().unwrap().delay_ms = ms;
    }

    pub fn delay_ms(&self) -> u64 {
        self.inner.lock().unwrap().delay_ms
    }

    pub fn set_breakpoints(&self, bps: HashSet<String>) {
        self.inner.lock().unwrap().breakpoints = bps;
    }

    pub fn check_and_pause_if_breakpoint(&self, model_id: &str, element_id: &str) -> bool {
        let key = format!("{},{}", model_id, element_id);
        let mut state = self.inner.lock().unwrap();
        if state.breakpoints.contains(&key) {
            state.paused = true;
            true
        } else {
            false
        }
    }

    pub fn is_paused(&self) -> bool {
        self.inner.lock().unwrap().paused
    }

    pub fn reset(&self) {
        let mut state = self.inner.lock().unwrap();
        state.paused = false;
        state.step_once = false;
        state.delay_ms = 0;
        state.breakpoints.clear();
        drop(state);
        self.notify.notify_waiters();
    }
}

#[derive(Clone)]
pub struct SessionHandle {
    pub id: String,
    pub name: String,
    pub model_json: String,
    pub seed: Option<u64>,
    pub machine_tx: std::sync::mpsc::Sender<Command>,
    pub broadcast_tx: broadcast::Sender<Value>,
    pub control: Arc<ExecutionControl>,
}

#[derive(Clone)]
pub struct SessionManager {
    sessions: Arc<RwLock<HashMap<String, SessionHandle>>>,
    counter: Arc<AtomicU64>,
    change_tx: broadcast::Sender<Value>,
}

impl SessionManager {
    pub fn new() -> Self {
        let (change_tx, _) = broadcast::channel(64);
        Self {
            sessions: Arc::new(RwLock::new(HashMap::new())),
            counter: Arc::new(AtomicU64::new(1)),
            change_tx,
        }
    }

    pub fn create_session(
        &self,
        name: String,
        model_json: String,
        seed: Option<u64>,
        machine_tx: std::sync::mpsc::Sender<Command>,
    ) -> SessionHandle {
        let id = format!("session-{}", self.counter.fetch_add(1, Ordering::Relaxed));
        let (broadcast_tx, _) = broadcast::channel(256);
        let handle = SessionHandle {
            id: id.clone(),
            name: name.clone(),
            model_json,
            seed,
            machine_tx,
            broadcast_tx,
            control: Arc::new(ExecutionControl::new()),
        };
        self.sessions
            .write()
            .unwrap()
            .insert(id.clone(), handle.clone());
        let _ = self.change_tx.send(json!({
            "command": "sessionCreated",
            "sessionId": id,
            "name": name,
        }));
        handle
    }

    pub fn remove_session(&self, id: &str) {
        if let Some(session) = self.sessions.write().unwrap().remove(id) {
            session.control.reset();
            let _ = self.change_tx.send(json!({
                "command": "sessionEnded",
                "sessionId": id,
            }));
        }
    }

    pub fn list_sessions(&self) -> Vec<SessionHandle> {
        self.sessions.read().unwrap().values().cloned().collect()
    }

    pub fn get_session(&self, id: &str) -> Option<SessionHandle> {
        self.sessions.read().unwrap().get(id).cloned()
    }

    pub fn subscribe_changes(&self) -> broadcast::Receiver<Value> {
        self.change_tx.subscribe()
    }
}
