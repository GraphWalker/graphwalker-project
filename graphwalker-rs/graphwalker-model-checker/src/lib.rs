use std::collections::HashSet;

use graphwalker_core::condition::StopCondition;
use graphwalker_core::generator::GeneratorKind;
use graphwalker_core::model::{RuntimeEdge, RuntimeModel, RuntimeVertex, VertexIndex};
use graphwalker_io::ModelContext;

#[derive(Clone, Debug, PartialEq, Eq)]
pub struct Issue {
    pub message: String,
}

impl Issue {
    fn new(message: impl Into<String>) -> Self {
        Self {
            message: message.into(),
        }
    }
}

impl std::fmt::Display for Issue {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str(&self.message)
    }
}

// ---------------------------------------------------------------------------
// Element-level checks (shared by vertex and edge)
// ---------------------------------------------------------------------------

fn check_element_common(
    id: &str,
    requirements: &HashSet<graphwalker_core::model::Requirement>,
    actions: &[graphwalker_core::model::Action],
    issues: &mut Vec<Issue>,
) {
    if id.is_empty() {
        issues.push(Issue::new("Id cannot be null"));
    }

    for req in requirements {
        if req.key().is_empty() {
            issues.push(Issue::new("Requirement cannot be an empty string"));
        }
    }

    for action in actions {
        if action.script().is_empty() {
            issues.push(Issue::new("Script statement cannot be an empty string"));
        }
    }
}

// ---------------------------------------------------------------------------
// Vertex checks
// ---------------------------------------------------------------------------

pub fn check_vertex(vertex: &RuntimeVertex) -> Vec<Issue> {
    let mut issues = Vec::new();

    check_element_common(
        vertex.id(),
        vertex.requirements(),
        vertex.actions(),
        &mut issues,
    );

    match vertex.name() {
        None => {
            issues.push(Issue::new("Name of vertex cannot be null"));
        }
        Some("") => {
            issues.push(Issue::new("Name of vertex cannot be an empty string"));
        }
        Some(name) if name.contains(char::is_whitespace) => {
            issues.push(Issue::new("Name of vertex cannot have any white spaces."));
        }
        _ => {}
    }

    issues
}

// ---------------------------------------------------------------------------
// Edge checks
// ---------------------------------------------------------------------------

pub fn check_edge(edge: &RuntimeEdge, model: &RuntimeModel) -> Vec<Issue> {
    let mut issues = Vec::new();

    check_element_common(edge.id(), edge.requirements(), edge.actions(), &mut issues);

    if edge.target_vertex().is_none() {
        issues.push(Issue::new("Edge must have a target vertex."));
    }

    if let Some(name) = edge.name() {
        if name.contains(char::is_whitespace) {
            issues.push(Issue::new("Name of edge cannot have any white spaces."));
        }
    }

    let w = edge.weight();
    if !(0.0..=1.0).contains(&w) {
        issues.push(Issue::new("The weight must be a value between 0 and 1."));
    }

    // Unnamed self-loop
    if !edge.has_name() {
        if let (Some(src), Some(tgt)) = (edge.source_vertex(), edge.target_vertex()) {
            if src == tgt {
                let vertex_name = model.vertex(src).name().unwrap_or(model.vertex(src).id());
                issues.push(Issue::new(format!(
                    "Vertex: {}, have a unnamed self loop edge.",
                    vertex_name
                )));
            }
        }
    }

    issues
}

// ---------------------------------------------------------------------------
// Model checks
// ---------------------------------------------------------------------------

pub fn check_model(model: &RuntimeModel) -> Vec<Issue> {
    let mut issues = Vec::new();
    let mut seen_ids: HashSet<&str> = HashSet::new();

    for vertex in model.vertices() {
        issues.extend(check_vertex(vertex));
        if !seen_ids.insert(vertex.id()) {
            issues.push(Issue::new(format!(
                "Id of the vertex is not unique: {}",
                vertex.id()
            )));
        }
    }

    for edge in model.edges() {
        issues.extend(check_edge(edge, model));
        if !seen_ids.insert(edge.id()) {
            issues.push(Issue::new(format!(
                "Id of the edge is not unique: {}",
                edge.id()
            )));
        }
    }

    issues
}

// ---------------------------------------------------------------------------
// Context checks
// ---------------------------------------------------------------------------

pub fn check_context(context: &ModelContext) -> Vec<Issue> {
    let mut issues = Vec::new();

    issues.extend(check_model(&context.model));

    if context.start_element_id.is_none() && !context.model.has_shared_states() {
        issues.push(Issue::new(
            "The model has neither a start element or a defined shared state.",
        ));
    }

    if let Some(ref gen_str) = context.generator {
        check_cul_de_sacs(gen_str, &context.model, &mut issues);
    }

    issues
}

fn check_cul_de_sacs(generator_str: &str, model: &RuntimeModel, issues: &mut Vec<Issue>) {
    let parsed = match graphwalker_dsl::generator::parse_generator(generator_str) {
        Ok(pg) => pg,
        Err(_) => return,
    };

    let is_random = matches!(parsed.kind, GeneratorKind::Random);
    let is_full_edge_coverage = matches!(parsed.stop_condition, StopCondition::EdgeCoverage(100));

    if !is_random || !is_full_edge_coverage {
        return;
    }

    let cul_de_sac_count = count_cul_de_sacs(model);

    if cul_de_sac_count > 1 {
        issues.push(Issue::new(
            "The model has multiple cul-de-sacs, and is requested to run using a random path generator and 100% edge coverage. That will not work."
        ));
    } else if cul_de_sac_count == 1 {
        issues.push(Issue::new(
            "The model has one cul-de-sacs, and is requested to run using a random path generator and 100% edge coverage. That might not work."
        ));
    }
}

fn count_cul_de_sacs(model: &RuntimeModel) -> usize {
    let mut count = 0;
    for (i, vertex) in model.vertices().iter().enumerate() {
        if vertex.has_shared_state() {
            continue;
        }
        let vi = VertexIndex(i);
        if model.out_edges(vi).is_empty() {
            count += 1;
        }
    }
    count
}

// ---------------------------------------------------------------------------
// Multi-context checks
// ---------------------------------------------------------------------------

pub fn check_contexts(contexts: &[ModelContext]) -> Vec<Issue> {
    let mut issues = Vec::new();
    let mut seen_model_ids: HashSet<&str> = HashSet::new();

    for context in contexts {
        issues.extend(check_context(context));

        let model_id = context.model.id();
        if !seen_model_ids.insert(model_id) {
            issues.push(Issue::new(format!(
                "Id of the model is not unique: {}",
                model_id
            )));
        }
    }

    issues
}

#[cfg(test)]
mod tests;
