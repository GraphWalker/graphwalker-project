use std::path::PathBuf;

use crate::json::*;

fn fixture(name: &str) -> PathBuf {
    PathBuf::from(env!("CARGO_MANIFEST_DIR"))
        .join("tests/fixtures/json")
        .join(name)
}

// ---------------------------------------------------------------------------
// Reading JSON files
// ---------------------------------------------------------------------------

#[test]
fn read_small_model() {
    let contexts = read_json_file(&fixture("SmallModel.json")).unwrap();
    assert_eq!(contexts.len(), 1);

    let ctx = &contexts[0];
    assert_eq!(ctx.generator.as_deref(), Some("random(edge_coverage(100))"));
    assert_eq!(ctx.start_element_id.as_deref(), Some("e0"));

    let model = &ctx.model;
    assert_eq!(model.vertices().len(), 2);
    assert_eq!(model.edges().len(), 4);

    assert_eq!(model.vertices()[0].name(), Some("v_VerifySomeAction"));
    assert_eq!(model.vertices()[0].id(), "n0");
    assert_eq!(model.vertices()[1].name(), Some("v_VerifySomeOtherAction"));
}

#[test]
fn read_example_with_actions() {
    let contexts = read_json_file(&fixture("example.json")).unwrap();
    let model = &contexts[0].model;

    let edge0 = &model.edges()[0];
    assert_eq!(edge0.name(), Some("e_FirstAction"));
    assert_eq!(edge0.actions().len(), 2);
    assert_eq!(edge0.actions()[0].script(), "x=0;");
    assert_eq!(edge0.actions()[1].script(), "y=0;");
}

#[test]
fn read_shared_state() {
    let contexts = read_json_file(&fixture("SharedState.json")).unwrap();
    let model = &contexts[0].model;

    assert!(model.vertices()[0].has_shared_state());
    assert_eq!(
        model.vertices()[0].shared_state(),
        Some("PORTAL_TO_ANOTHER_GALAXY")
    );
    assert!(!model.vertices()[1].has_shared_state());
}

#[test]
fn read_login_with_guards() {
    let contexts = read_json_file(&fixture("Login.json")).unwrap();
    assert_eq!(contexts.len(), 1);

    let model = &contexts[0].model;
    assert_eq!(model.name(), Some("Login"));

    let e1 = model.edges().iter().find(|e| e.id() == "e1").unwrap();
    assert!(e1.has_guard());
    assert_eq!(e1.guard().unwrap().script(), "!rememberMe||!validLogin");

    let e0 = model.edges().iter().find(|e| e.id() == "e0").unwrap();
    assert_eq!(e0.actions().len(), 1);
    assert_eq!(
        e0.actions()[0].script(),
        "validLogin=false;rememberMe=false;"
    );

    let n1 = model.vertices().iter().find(|v| v.id() == "n1").unwrap();
    assert_eq!(n1.shared_state(), Some("CLIENT_NOT_RUNNNG"));
    assert!(n1.has_property("x"));
}

#[test]
fn read_predefined_path() {
    let contexts = read_json_file(&fixture("ModelWithPredefinedPath.json")).unwrap();
    let model = &contexts[0].model;

    assert!(model.has_predefined_path());
    let path = model.predefined_path();
    assert_eq!(path.len(), 5);

    assert_eq!(model.edge(path[0]).id(), "e0");
    assert_eq!(model.edge(path[1]).id(), "e3");
    assert_eq!(model.edge(path[2]).id(), "e1");
    assert_eq!(model.edge(path[3]).id(), "e3");
    assert_eq!(model.edge(path[4]).id(), "e2");
}

#[test]
fn read_dependency_model() {
    let contexts = read_json_file(&fixture("DependencyModel.json")).unwrap();
    let model = &contexts[0].model;

    let e0 = model.edges().iter().find(|e| e.id() == "e0").unwrap();
    assert_eq!(e0.dependency(), 100);

    let e2 = model.edges().iter().find(|e| e.id() == "e2").unwrap();
    assert_eq!(e2.dependency(), 85);
}

// ---------------------------------------------------------------------------
// Round-trip: read → write → read
// ---------------------------------------------------------------------------

#[test]
fn json_round_trip() {
    let contexts = read_json_file(&fixture("SmallModel.json")).unwrap();
    let json_out = write_json_string(&contexts).unwrap();
    let contexts2 = read_json_string(&json_out).unwrap();

    assert_eq!(contexts2.len(), 1);
    assert_eq!(contexts2[0].model.vertices().len(), 2);
    assert_eq!(contexts2[0].model.edges().len(), 4);
    assert_eq!(
        contexts2[0].generator.as_deref(),
        Some("random(edge_coverage(100))")
    );
}

#[test]
fn json_round_trip_preserves_shared_state() {
    let contexts = read_json_file(&fixture("SharedState.json")).unwrap();
    let json_out = write_json_string(&contexts).unwrap();
    let contexts2 = read_json_string(&json_out).unwrap();

    assert_eq!(
        contexts2[0].model.vertices()[0].shared_state(),
        Some("PORTAL_TO_ANOTHER_GALAXY")
    );
}

// ---------------------------------------------------------------------------
// Deserialization edge cases
// ---------------------------------------------------------------------------

#[test]
fn read_json_string_directly() {
    let json = r#"{
        "models": [{
            "name": "Test",
            "generator": "random(never)",
            "vertices": [{"id": "v1", "name": "A"}],
            "edges": [{"id": "e1", "targetVertexId": "v1"}]
        }]
    }"#;

    let contexts = read_json_string(json).unwrap();
    assert_eq!(contexts.len(), 1);
    assert_eq!(contexts[0].model.vertices().len(), 1);
}

#[test]
fn properties_as_empty_array_is_ok() {
    let json = r#"{
        "models": [{
            "name": "Test",
            "vertices": [{"id": "v1", "properties": []}],
            "edges": [{"id": "e1", "targetVertexId": "v1", "properties": []}]
        }]
    }"#;

    let contexts = read_json_string(json).unwrap();
    assert!(!contexts[0].model.vertices()[0].has_properties());
}

#[test]
fn dependency_as_string() {
    let json = r#"{
        "models": [{
            "name": "Test",
            "vertices": [{"id": "v1"}],
            "edges": [{"id": "e1", "targetVertexId": "v1", "dependency": "75"}]
        }]
    }"#;

    let contexts = read_json_string(json).unwrap();
    let edge = &contexts[0].model.edges()[0];
    assert_eq!(edge.dependency(), 75);
}

#[test]
fn weight_as_string() {
    let json = r#"{
        "models": [{
            "name": "Test",
            "vertices": [{"id": "v1"}],
            "edges": [{"id": "e1", "targetVertexId": "v1", "weight": "0.5"}]
        }]
    }"#;

    let contexts = read_json_string(json).unwrap();
    let edge = &contexts[0].model.edges()[0];
    assert_eq!(edge.weight(), 0.5);
}
