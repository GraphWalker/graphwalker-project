use std::path::PathBuf;

use graphwalker_core::model::{Action, EdgeBuilder, ModelBuilder, Requirement, VertexBuilder};
use graphwalker_io::ModelContext;

use crate::*;

fn fixture(name: &str) -> PathBuf {
    PathBuf::from(env!("CARGO_MANIFEST_DIR"))
        .join("tests/fixtures/json")
        .join(name)
}

fn build_context(mb: ModelBuilder, generator: Option<&str>, start: Option<&str>) -> ModelContext {
    ModelContext {
        model: mb.build(),
        generator: generator.map(|s| s.to_string()),
        start_element_id: start.map(|s| s.to_string()),
    }
}

// ---------------------------------------------------------------------------
// Element-level checks
// ---------------------------------------------------------------------------

#[test]
fn element_default_vertex_no_issues() {
    let mut mb = ModelBuilder::new();
    let vb = VertexBuilder::new().id("n0").name("v_Vertex");
    mb.add_vertex(vb);
    let model = mb.build();
    let issues = check_vertex(&model.vertices()[0]);
    assert!(issues.is_empty());
}

#[test]
fn element_empty_requirement() {
    let mut mb = ModelBuilder::new();
    let vb = VertexBuilder::new()
        .id("n0")
        .name("v_Vertex")
        .add_requirement(Requirement::new(""));
    mb.add_vertex(vb);
    let model = mb.build();
    let issues = check_vertex(&model.vertices()[0]);
    assert_eq!(issues.len(), 1);
    assert_eq!(issues[0].message, "Requirement cannot be an empty string");
}

#[test]
fn element_empty_action() {
    let mut mb = ModelBuilder::new();
    let eb = EdgeBuilder::new()
        .id("e0")
        .add_action(Action::new(""))
        .target_vertex(VertexBuilder::new().id("n0").name("v_Target"));
    mb.add_edge(eb);
    let model = mb.build();
    let issues = check_edge(&model.edges()[0], &model);
    assert!(
        issues
            .iter()
            .any(|i| i.message == "Script statement cannot be an empty string"),
        "expected empty action issue, got: {:?}",
        issues
    );
}

// ---------------------------------------------------------------------------
// Vertex checks
// ---------------------------------------------------------------------------

#[test]
fn vertex_no_name() {
    let mut mb = ModelBuilder::new();
    let vb = VertexBuilder::new().id("n0");
    mb.add_vertex(vb);
    let model = mb.build();
    let issues = check_vertex(&model.vertices()[0]);
    assert_eq!(issues.len(), 1);
    assert_eq!(issues[0].message, "Name of vertex cannot be null");
}

#[test]
fn vertex_with_name_no_issues() {
    let mut mb = ModelBuilder::new();
    let vb = VertexBuilder::new().id("n0").name("v_Valid");
    mb.add_vertex(vb);
    let model = mb.build();
    let issues = check_vertex(&model.vertices()[0]);
    assert!(issues.is_empty());
}

#[test]
fn vertex_empty_name() {
    let mut mb = ModelBuilder::new();
    let vb = VertexBuilder::new().id("n0").name("");
    mb.add_vertex(vb);
    let model = mb.build();
    let issues = check_vertex(&model.vertices()[0]);
    assert_eq!(issues.len(), 1);
    assert_eq!(
        issues[0].message,
        "Name of vertex cannot be an empty string"
    );
}

#[test]
fn vertex_name_with_spaces() {
    let mut mb = ModelBuilder::new();
    let vb = VertexBuilder::new().id("n0").name("has spaces");
    mb.add_vertex(vb);
    let model = mb.build();
    let issues = check_vertex(&model.vertices()[0]);
    assert_eq!(issues.len(), 1);
    assert_eq!(
        issues[0].message,
        "Name of vertex cannot have any white spaces."
    );
}

// ---------------------------------------------------------------------------
// Edge checks
// ---------------------------------------------------------------------------

#[test]
fn edge_no_target() {
    let mut mb = ModelBuilder::new();
    let eb = EdgeBuilder::new().id("e0");
    mb.add_edge(eb);
    let model = mb.build();
    let issues = check_edge(&model.edges()[0], &model);
    assert!(issues
        .iter()
        .any(|i| i.message == "Edge must have a target vertex."));
}

#[test]
fn edge_with_target_no_issues() {
    let mut mb = ModelBuilder::new();
    let eb = EdgeBuilder::new()
        .id("e0")
        .name("e_Valid")
        .target_vertex(VertexBuilder::new().id("n0").name("v_Target"));
    mb.add_edge(eb);
    let model = mb.build();
    let issues = check_edge(&model.edges()[0], &model);
    assert!(issues.is_empty(), "unexpected issues: {:?}", issues);
}

#[test]
fn edge_name_with_spaces() {
    let mut mb = ModelBuilder::new();
    let eb = EdgeBuilder::new()
        .id("e0")
        .name("has spaces")
        .target_vertex(VertexBuilder::new().id("n0").name("v_Target"));
    mb.add_edge(eb);
    let model = mb.build();
    let issues = check_edge(&model.edges()[0], &model);
    assert!(issues
        .iter()
        .any(|i| i.message == "Name of edge cannot have any white spaces."));
}

#[test]
fn edge_empty_name_is_ok() {
    let mut mb = ModelBuilder::new();
    let eb = EdgeBuilder::new()
        .id("e0")
        .target_vertex(VertexBuilder::new().id("n0").name("v_Target"));
    mb.add_edge(eb);
    let model = mb.build();
    let issues = check_edge(&model.edges()[0], &model);
    assert!(issues.is_empty(), "unexpected issues: {:?}", issues);
}

#[test]
fn edge_weight_negative() {
    let mut mb = ModelBuilder::new();
    let eb = EdgeBuilder::new()
        .id("e0")
        .name("e_Bad")
        .weight(-1.0)
        .target_vertex(VertexBuilder::new().id("n0").name("v_Target"));
    mb.add_edge(eb);
    let model = mb.build();
    let issues = check_edge(&model.edges()[0], &model);
    assert!(issues
        .iter()
        .any(|i| i.message == "The weight must be a value between 0 and 1."));
}

#[test]
fn edge_weight_valid() {
    let mut mb = ModelBuilder::new();
    let eb = EdgeBuilder::new()
        .id("e0")
        .name("e_Good")
        .weight(0.5)
        .target_vertex(VertexBuilder::new().id("n0").name("v_Target"));
    mb.add_edge(eb);
    let model = mb.build();
    let issues = check_edge(&model.edges()[0], &model);
    assert!(issues.is_empty(), "unexpected issues: {:?}", issues);
}

// ---------------------------------------------------------------------------
// Model checks
// ---------------------------------------------------------------------------

#[test]
fn model_empty_no_issues() {
    let mb = ModelBuilder::new();
    let model = mb.build();
    let issues = check_model(&model);
    assert!(issues.is_empty());
}

#[test]
fn model_invalid_vertex() {
    let mut mb = ModelBuilder::new();
    mb.add_vertex(VertexBuilder::new().id("n0"));
    let model = mb.build();
    let issues = check_model(&model);
    assert!(issues
        .iter()
        .any(|i| i.message == "Name of vertex cannot be null"));
}

#[test]
fn model_duplicate_element_ids() {
    let mut mb = ModelBuilder::new();
    mb.add_vertex(VertexBuilder::new().id("n0").name("v_A"));
    mb.add_vertex(VertexBuilder::new().id("n1").name("v_B"));
    let eb = EdgeBuilder::new()
        .id("n0")
        .name("e_Dup")
        .source_vertex(VertexBuilder::new().id("n0"))
        .target_vertex(VertexBuilder::new().id("n1"));
    mb.add_edge(eb);
    let model = mb.build();
    let issues = check_model(&model);
    assert!(
        issues
            .iter()
            .any(|i| i.message == "Id of the edge is not unique: n0"),
        "expected duplicate ID issue, got: {:?}",
        issues
    );
}

#[test]
fn model_unnamed_self_loop() {
    let mut mb = ModelBuilder::new();
    let vb = VertexBuilder::new().id("n0").name("v_Loop");
    let eb = EdgeBuilder::new()
        .id("e0")
        .source_vertex(VertexBuilder::new().id("n0"))
        .target_vertex(VertexBuilder::new().id("n0"));
    mb.add_vertex(vb);
    mb.add_edge(eb);
    let model = mb.build();
    let issues = check_model(&model);
    assert!(
        issues
            .iter()
            .any(|i| i.message.contains("have a unnamed self loop edge")),
        "expected self-loop issue, got: {:?}",
        issues
    );
}

// ---------------------------------------------------------------------------
// Context checks
// ---------------------------------------------------------------------------

#[test]
fn context_no_start_no_shared_state() {
    let mut mb = ModelBuilder::new();
    mb.add_vertex(VertexBuilder::new().id("n0").name("v_A"));
    let ctx = build_context(mb, None, None);
    let issues = check_context(&ctx);
    assert!(issues
        .iter()
        .any(|i| i.message == "The model has neither a start element or a defined shared state."));
}

#[test]
fn context_with_start_element_no_issues() {
    let mut mb = ModelBuilder::new();
    let v0 = VertexBuilder::new().id("n0").name("v_A");
    let v1 = VertexBuilder::new().id("n1").name("v_B");
    let e0 = EdgeBuilder::new()
        .id("e0")
        .name("e_Go")
        .source_vertex(VertexBuilder::new().id("n0"))
        .target_vertex(VertexBuilder::new().id("n1"));
    let e1 = EdgeBuilder::new()
        .id("e1")
        .name("e_Back")
        .source_vertex(VertexBuilder::new().id("n1"))
        .target_vertex(VertexBuilder::new().id("n0"));
    mb.add_vertex(v0);
    mb.add_vertex(v1);
    mb.add_edge(e0);
    mb.add_edge(e1);
    let ctx = build_context(mb, None, Some("e0"));
    let issues = check_context(&ctx);
    assert!(issues.is_empty(), "unexpected issues: {:?}", issues);
}

#[test]
fn context_with_shared_state_no_issues() {
    let mut mb = ModelBuilder::new();
    let v0 = VertexBuilder::new()
        .id("n0")
        .name("v_A")
        .shared_state("PORTAL");
    mb.add_vertex(v0);
    let ctx = build_context(mb, None, None);
    let issues = check_context(&ctx);
    assert!(issues.is_empty(), "unexpected issues: {:?}", issues);
}

#[test]
fn context_multiple_cul_de_sacs_with_random_full_edge_coverage() {
    let mut mb = ModelBuilder::new();
    let v0 = VertexBuilder::new().id("n0").name("v_Start");
    let v1 = VertexBuilder::new().id("n1").name("v_DeadEnd1");
    let v2 = VertexBuilder::new().id("n2").name("v_DeadEnd2");
    let e0 = EdgeBuilder::new()
        .id("e0")
        .name("e_ToEnd1")
        .source_vertex(VertexBuilder::new().id("n0"))
        .target_vertex(VertexBuilder::new().id("n1"));
    let e1 = EdgeBuilder::new()
        .id("e1")
        .name("e_ToEnd2")
        .source_vertex(VertexBuilder::new().id("n0"))
        .target_vertex(VertexBuilder::new().id("n2"));
    mb.add_vertex(v0);
    mb.add_vertex(v1);
    mb.add_vertex(v2);
    mb.add_edge(e0);
    mb.add_edge(e1);

    let ctx = build_context(mb, Some("random(edge_coverage(100))"), Some("e0"));
    let issues = check_context(&ctx);
    assert!(
        issues
            .iter()
            .any(|i| i.message.contains("multiple cul-de-sacs")),
        "expected multiple cul-de-sacs issue, got: {:?}",
        issues
    );
}

#[test]
fn context_single_cul_de_sac_with_random_full_edge_coverage() {
    let mut mb = ModelBuilder::new();
    let v0 = VertexBuilder::new().id("n0").name("v_Start");
    let v1 = VertexBuilder::new().id("n1").name("v_DeadEnd");
    let e0 = EdgeBuilder::new()
        .id("e0")
        .name("e_ToEnd")
        .source_vertex(VertexBuilder::new().id("n0"))
        .target_vertex(VertexBuilder::new().id("n1"));
    let e1 = EdgeBuilder::new()
        .id("e1")
        .name("e_Loop")
        .source_vertex(VertexBuilder::new().id("n0"))
        .target_vertex(VertexBuilder::new().id("n0"));
    mb.add_vertex(v0);
    mb.add_vertex(v1);
    mb.add_edge(e0);
    mb.add_edge(e1);

    let ctx = build_context(mb, Some("random(edge_coverage(100))"), Some("e0"));
    let issues = check_context(&ctx);
    assert!(
        issues.iter().any(|i| i.message.contains("one cul-de-sacs")),
        "expected one cul-de-sac issue, got: {:?}",
        issues
    );
}

// ---------------------------------------------------------------------------
// Multi-context checks
// ---------------------------------------------------------------------------

#[test]
fn contexts_duplicate_model_ids() {
    let mut mb1 = ModelBuilder::new();
    mb1.id("same-id".to_string());
    mb1.add_vertex(VertexBuilder::new().id("n0").name("v_A").shared_state("S"));

    let mut mb2 = ModelBuilder::new();
    mb2.id("same-id".to_string());
    mb2.add_vertex(VertexBuilder::new().id("n1").name("v_B").shared_state("S"));

    let contexts = vec![
        build_context(mb1, None, None),
        build_context(mb2, None, None),
    ];
    let issues = check_contexts(&contexts);
    assert!(
        issues
            .iter()
            .any(|i| i.message == "Id of the model is not unique: same-id"),
        "expected duplicate model ID issue, got: {:?}",
        issues
    );
}

#[test]
fn contexts_pet_clinic_no_issues() {
    let contexts = graphwalker_io::json::read_json_file(&fixture("petClinic.json")).unwrap();
    let issues = check_contexts(&contexts);
    assert!(issues.is_empty(), "unexpected issues: {:?}", issues);
}
