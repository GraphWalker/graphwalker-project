use std::sync::mpsc;

use rand::prelude::*;

use graphwalker_core::machine::{ExecutionContext, Machine};
use graphwalker_core::model::{Action, EdgeIndex, ElementIndex, VertexIndex};
use graphwalker_dsl::generator::parse_generator;
use graphwalker_io::graphml::read_graphml_string;
use graphwalker_io::json::{read_json_string, write_json_string};
use graphwalker_io::ModelContext;
use serde_json::{json, Value};
use tokio::sync::oneshot;
use tracing::debug;

pub enum Command {
    Load {
        json_body: String,
        seed: Option<u64>,
        global_data: Option<String>,
        reply: oneshot::Sender<Result<Value, String>>,
    },
    Check {
        json_body: String,
        reply: oneshot::Sender<Result<Value, String>>,
    },
    HasNext {
        reply: oneshot::Sender<Result<Value, String>>,
    },
    GetNext {
        verbose: bool,
        reply: oneshot::Sender<Result<Value, String>>,
    },
    GetData {
        reply: oneshot::Sender<Result<Value, String>>,
    },
    SetData {
        script: String,
        reply: oneshot::Sender<Result<Value, String>>,
    },
    Restart {
        reply: oneshot::Sender<Result<Value, String>>,
    },
    GetStatistics {
        reply: oneshot::Sender<Result<Value, String>>,
    },
    GetModel {
        reply: oneshot::Sender<Result<Value, String>>,
    },
    UpdateAllElements {
        reply: oneshot::Sender<Result<Value, String>>,
    },
    ConvertGraphml {
        graphml: String,
        reply: oneshot::Sender<Result<Value, String>>,
    },
}

struct MachineState {
    machine: Option<Machine>,
    contexts_snapshot: Vec<ModelContext>,
}

pub fn spawn_machine_thread() -> mpsc::Sender<Command> {
    let (tx, rx) = mpsc::channel::<Command>();

    std::thread::spawn(move || {
        let mut state = MachineState {
            machine: None,
            contexts_snapshot: Vec::new(),
        };

        while let Ok(cmd) = rx.recv() {
            match cmd {
                Command::Load {
                    json_body,
                    seed,
                    global_data,
                    reply,
                } => {
                    let _ = reply.send(handle_load(&mut state, &json_body, seed, global_data.as_deref()));
                }
                Command::Check { json_body, reply } => {
                    let _ = reply.send(handle_check(&json_body));
                }
                Command::HasNext { reply } => {
                    let _ = reply.send(handle_has_next(&mut state));
                }
                Command::GetNext { verbose, reply } => {
                    let _ = reply.send(handle_get_next(&mut state, verbose));
                }
                Command::GetData { reply } => {
                    let _ = reply.send(handle_get_data(&mut state));
                }
                Command::SetData { script, reply } => {
                    let _ = reply.send(handle_set_data(&mut state, &script));
                }
                Command::Restart { reply } => {
                    let _ = reply.send(handle_restart(&mut state));
                }
                Command::GetStatistics { reply } => {
                    let _ = reply.send(handle_get_statistics(&mut state));
                }
                Command::GetModel { reply } => {
                    let _ = reply.send(handle_get_model(&state));
                }
                Command::UpdateAllElements { reply } => {
                    let _ = reply.send(handle_update_all_elements(&state));
                }
                Command::ConvertGraphml { graphml, reply } => {
                    let _ = reply.send(handle_convert_graphml(&graphml));
                }
            }
        }
    });

    tx
}

pub fn handle_check(json_body: &str) -> Result<Value, String> {
    let contexts = read_json_string(json_body).map_err(|e| e.to_string())?;
    let issues = graphwalker_model_checker::check_contexts(&contexts);
    let messages: Vec<&str> = issues.iter().map(|i| i.message.as_str()).collect();
    Ok(json!({"result": "ok", "issues": messages}))
}

fn handle_load(
    state: &mut MachineState,
    json_body: &str,
    seed: Option<u64>,
    global_data: Option<&str>,
) -> Result<Value, String> {
    let contexts = read_json_string(json_body).map_err(|e| e.to_string())?;

    let actual_seed = seed.unwrap_or_else(|| rand::thread_rng().gen());

    let mut entries = Vec::new();
    for ctx in &contexts {
        let gen_str = ctx
            .generator
            .as_deref()
            .ok_or("Model has no generator specified")?;
        let generator = parse_generator(gen_str).map_err(|e| e.to_string())?;
        let mut exec_ctx = ExecutionContext::new_with_seed(ctx.model.clone(), actual_seed);
        if let Some(ref start_id) = ctx.start_element_id {
            if let Some(element) = exec_ctx.model().element_by_id(start_id) {
                exec_ctx.set_next_element(Some(element));
            }
        }
        entries.push((exec_ctx, generator));
    }

    let machine = Machine::new_with_seed(entries, actual_seed).map_err(|e| e.to_string())?;

    if let Some(data) = global_data {
        for stmt in data.split(';') {
            let trimmed = stmt.trim();
            if !trimmed.is_empty() {
                let action = Action::new(&format!("global.{}", trimmed));
                machine
                    .current_context()
                    .execute_action(&action)
                    .map_err(|e| e.to_string())?;
            }
        }
    }

    state.machine = Some(machine);
    state.contexts_snapshot = contexts;

    Ok(json!({"result": "ok", "seed": actual_seed}))
}

fn handle_has_next(state: &mut MachineState) -> Result<Value, String> {
    let machine = state.machine.as_mut().ok_or("No model(s) are loaded.")?;
    let has = machine.has_next_step();
    Ok(json!({"result": "ok", "hasNext": has.to_string()}))
}

fn handle_get_next(state: &mut MachineState, verbose: bool) -> Result<Value, String> {
    let machine = state.machine.as_mut().ok_or("No model(s) are loaded.")?;

    machine.get_next_step().map_err(|e| e.to_string())?;

    let ctx_idx = machine.current_context_index();
    let ctx = machine.context(ctx_idx);
    let element = ctx.current_element().ok_or("No current element")?;

    let name = element_name_from_ctx(ctx, element);
    let id = element_id_from_ctx(ctx, element);
    let model_id = ctx.model().id().to_string();

    let model_name = ctx.model().name().unwrap_or("?");
    let data = ctx.data();
    debug!(
        model = model_name,
        element = name,
        data = data,
        "getNext"
    );

    let mut resp = json!({
        "result": "ok",
        "currentElementName": name,
        "currentElementID": id,
        "modelId": model_id,
    });

    if verbose {
        let data = ctx.data();
        let visit_count = ctx.visit_count(element);
        let total_count = ctx.total_visit_count();
        let fulfilment = machine.get_fulfilment(ctx_idx);

        resp["data"] = json!(data);
        resp["visitedCount"] = json!(visit_count);
        resp["totalCount"] = json!(total_count);
        resp["stopConditionFulfillment"] = json!(fulfilment);
    }

    Ok(resp)
}

fn handle_get_data(state: &mut MachineState) -> Result<Value, String> {
    let machine = state.machine.as_mut().ok_or("No model(s) are loaded.")?;

    let ctx = machine.current_context();
    let data = ctx.data();

    Ok(json!({"result": "ok", "data": data}))
}

fn handle_set_data(state: &mut MachineState, script: &str) -> Result<Value, String> {
    let machine = state.machine.as_mut().ok_or("No model(s) are loaded.")?;

    let action = Action::new(script);
    let ctx_idx = machine.current_context_index();
    machine
        .context_mut(ctx_idx)
        .execute_action(&action)
        .map_err(|e| e.to_string())?;

    Ok(json!({"result": "ok"}))
}

fn handle_restart(state: &mut MachineState) -> Result<Value, String> {
    if state.contexts_snapshot.is_empty() {
        return Err("No model(s) are loaded.".to_string());
    }

    let mut entries = Vec::new();
    for ctx in &state.contexts_snapshot {
        let gen_str = ctx
            .generator
            .as_deref()
            .ok_or("Model has no generator specified")?;
        let generator = parse_generator(gen_str).map_err(|e| e.to_string())?;
        let mut exec_ctx = ExecutionContext::new(ctx.model.clone());
        if let Some(ref start_id) = ctx.start_element_id {
            if let Some(element) = exec_ctx.model().element_by_id(start_id) {
                exec_ctx.set_next_element(Some(element));
            }
        }
        entries.push((exec_ctx, generator));
    }

    let machine = Machine::new(entries).map_err(|e| e.to_string())?;
    state.machine = Some(machine);

    Ok(json!({"result": "ok"}))
}

fn handle_get_statistics(state: &mut MachineState) -> Result<Value, String> {
    let machine = state.machine.as_ref().ok_or("No model(s) are loaded.")?;

    let mut total_vertices = 0usize;
    let mut total_edges = 0usize;
    let mut visited_vertices = 0usize;
    let mut visited_edges = 0usize;

    for i in 0..machine.context_count() {
        let ctx = machine.context(i);
        let model = ctx.model();

        let nv = model.vertices().len();
        let ne = model.edges().len();
        total_vertices += nv;
        total_edges += ne;

        for vi in 0..nv {
            if ctx.is_visited(ElementIndex::Vertex(VertexIndex(vi))) {
                visited_vertices += 1;
            }
        }
        for ei in 0..ne {
            if ctx.is_visited(ElementIndex::Edge(EdgeIndex(ei))) {
                visited_edges += 1;
            }
        }
    }

    let vertex_coverage = if total_vertices > 0 {
        (visited_vertices as f64 / total_vertices as f64) * 100.0
    } else {
        0.0
    };
    let edge_coverage = if total_edges > 0 {
        (visited_edges as f64 / total_edges as f64) * 100.0
    } else {
        0.0
    };

    Ok(json!({
        "result": "ok",
        "totalNumberOfVertices": total_vertices,
        "totalNumberOfEdges": total_edges,
        "totalNumberOfVisitedVertices": visited_vertices,
        "totalNumberOfVisitedEdges": visited_edges,
        "totalNumberOfUnvisitedVertices": total_vertices - visited_vertices,
        "totalNumberOfUnvisitedEdges": total_edges - visited_edges,
        "vertexCoverage": vertex_coverage as u32,
        "edgeCoverage": edge_coverage as u32,
    }))
}

fn handle_get_model(state: &MachineState) -> Result<Value, String> {
    if state.contexts_snapshot.is_empty() {
        return Err("No model(s) are loaded.".to_string());
    }

    let json_str = write_json_string(&state.contexts_snapshot).map_err(|e| e.to_string())?;
    Ok(json!({"result": "ok", "models": json_str}))
}

fn handle_update_all_elements(state: &MachineState) -> Result<Value, String> {
    let machine = state.machine.as_ref().ok_or("No model(s) are loaded.")?;

    let mut elements = Vec::new();

    for i in 0..machine.context_count() {
        let ctx = machine.context(i);
        let model = ctx.model();
        let model_id = model.id().to_string();

        for vi in 0..model.vertices().len() {
            let vertex = model.vertex(VertexIndex(vi));
            let elem = ElementIndex::Vertex(VertexIndex(vi));
            elements.push(json!({
                "modelId": model_id,
                "elementId": vertex.id(),
                "visitedCount": ctx.visit_count(elem),
            }));
        }

        for ei in 0..model.edges().len() {
            let edge = model.edge(EdgeIndex(ei));
            let elem = ElementIndex::Edge(EdgeIndex(ei));
            elements.push(json!({
                "modelId": model_id,
                "elementId": edge.id(),
                "visitedCount": ctx.visit_count(elem),
            }));
        }
    }

    Ok(json!({"result": "ok", "elements": elements}))
}

pub fn handle_convert_graphml(graphml: &str) -> Result<Value, String> {
    let contexts = read_graphml_string(graphml).map_err(|e| e.to_string())?;
    let json_str = write_json_string(&contexts).map_err(|e| e.to_string())?;
    Ok(json!({"result": "ok", "models": json_str}))
}

fn element_name_from_ctx(ctx: &ExecutionContext, element: ElementIndex) -> String {
    match element {
        ElementIndex::Vertex(vi) => ctx.model().vertex(vi).name().unwrap_or("").to_string(),
        ElementIndex::Edge(ei) => ctx.model().edge(ei).name().unwrap_or("").to_string(),
    }
}

fn element_id_from_ctx(ctx: &ExecutionContext, element: ElementIndex) -> String {
    match element {
        ElementIndex::Vertex(vi) => ctx.model().vertex(vi).id().to_string(),
        ElementIndex::Edge(ei) => ctx.model().edge(ei).id().to_string(),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn run_to_completion(state: &mut MachineState) -> Vec<String> {
        let mut path = Vec::new();
        while handle_has_next(state)
            .ok()
            .and_then(|v| v.get("hasNext").and_then(|h| h.as_str()).map(|s| s == "true"))
            .unwrap_or(false)
        {
            let resp = handle_get_next(state, false).unwrap();
            let id = resp
                .get("currentElementID")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            path.push(id);
        }
        path
    }

    const SMALL_MODEL_JSON: &str = r#"{
        "models": [{
            "name": "Small",
            "generator": "random(edge_coverage(100))",
            "startElementId": "e0",
            "vertices": [
                {"name": "v_A", "id": "n0"},
                {"name": "v_B", "id": "n1"}
            ],
            "edges": [
                {"name": "e_Start", "id": "e0", "targetVertexId": "n0"},
                {"name": "e_AB", "id": "e1", "sourceVertexId": "n0", "targetVertexId": "n1"},
                {"name": "e_Loop", "id": "e2", "sourceVertexId": "n1", "targetVertexId": "n1"},
                {"name": "e_BA", "id": "e3", "sourceVertexId": "n1", "targetVertexId": "n0"}
            ]
        }]
    }"#;

    fn fresh_state() -> MachineState {
        MachineState {
            machine: None,
            contexts_snapshot: Vec::new(),
        }
    }

    #[test]
    fn load_with_seed_is_deterministic() {
        let run = |seed: u64| -> Vec<String> {
            let mut state = fresh_state();
            handle_load(&mut state, SMALL_MODEL_JSON, Some(seed), None).unwrap();
            run_to_completion(&mut state)
        };

        assert_eq!(run(42), run(42));
        assert_eq!(run(123), run(123));
    }

    #[test]
    fn load_with_different_seeds_diverges() {
        let run = |seed: u64| -> Vec<String> {
            let mut state = fresh_state();
            handle_load(&mut state, SMALL_MODEL_JSON, Some(seed), None).unwrap();
            run_to_completion(&mut state)
        };

        assert_ne!(run(42), run(99));
    }

    #[test]
    fn load_without_seed_returns_generated_seed() {
        let mut state = fresh_state();
        let result = handle_load(&mut state, SMALL_MODEL_JSON, None, None).unwrap();
        let seed = result.get("seed").and_then(|v| v.as_u64());
        assert!(seed.is_some(), "response must contain generated seed");

        let path = run_to_completion(&mut state);
        assert!(!path.is_empty());
    }

    #[test]
    fn auto_generated_seed_replays_deterministically() {
        let mut state1 = fresh_state();
        let resp = handle_load(&mut state1, SMALL_MODEL_JSON, None, None).unwrap();
        let seed = resp.get("seed").and_then(|v| v.as_u64()).unwrap();
        let path1 = run_to_completion(&mut state1);

        let mut state2 = fresh_state();
        handle_load(&mut state2, SMALL_MODEL_JSON, Some(seed), None).unwrap();
        let path2 = run_to_completion(&mut state2);

        assert_eq!(path1, path2);
    }

    #[test]
    fn load_with_global_data_sets_variables() {
        let mut state = fresh_state();
        handle_load(&mut state, SMALL_MODEL_JSON, Some(42), Some("x=10;y=20")).unwrap();

        let resp = handle_get_data(&mut state).unwrap();
        let data = resp.get("data").and_then(|d| d.as_str()).unwrap_or("");
        assert!(data.contains("x=10"));
        assert!(data.contains("y=20"));
    }
}
