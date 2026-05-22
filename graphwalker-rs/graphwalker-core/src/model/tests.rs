use super::*;
use serde_json::json;

// =========================================================================
// Value types
// =========================================================================

#[test]
fn action_stores_script() {
    let a = Action::new("var i = 1;");
    assert_eq!(a.script(), "var i = 1;");
    assert_eq!(a.to_string(), "var i = 1;");
}

#[test]
fn guard_stores_script() {
    let g = Guard::new("i < 10");
    assert_eq!(g.script(), "i < 10");
    assert!(g.has_script());
}

#[test]
fn guard_empty_script() {
    let g = Guard::new("");
    assert!(!g.has_script());
}

#[test]
fn requirement_stores_key() {
    let r = Requirement::new("REQ_001");
    assert_eq!(r.key(), "REQ_001");
    assert_eq!(r.to_string(), "REQ_001");
}

// =========================================================================
// VertexBuilder
// =========================================================================

#[test]
fn vertex_builder_defaults() {
    let v = VertexBuilder::new();
    assert!(!v.get_id().is_empty());
    assert!(v.get_name().is_none());
    assert!(v.get_shared_state().is_none());
    assert!(v.get_actions().is_empty());
    assert!(v.get_requirements().is_empty());
    assert!(v.get_properties().is_empty());
}

#[test]
fn vertex_builder_fluent_api() {
    let v = VertexBuilder::new()
        .id("n1")
        .name("v_Start")
        .shared_state("SHARED")
        .add_action(Action::new("init()"))
        .add_requirement(Requirement::new("REQ-001"))
        .property("color", json!("red"));

    assert_eq!(v.get_id(), "n1");
    assert_eq!(v.get_name(), Some("v_Start"));
    assert_eq!(v.get_shared_state(), Some("SHARED"));
    assert_eq!(v.get_actions().len(), 1);
    assert!(v.get_requirements().contains(&Requirement::new("REQ-001")));
    assert_eq!(v.get_properties().get("color"), Some(&json!("red")));
}

#[test]
fn vertex_builder_clone_preserves_id() {
    let v1 = VertexBuilder::new().name("shared");
    let v2 = v1.clone();
    assert_eq!(v1.get_id(), v2.get_id());
}

// =========================================================================
// EdgeBuilder
// =========================================================================

#[test]
fn edge_builder_defaults() {
    let e = EdgeBuilder::new();
    assert!(!e.get_id().is_empty());
    assert!(e.get_name().is_none());
    assert!(e.get_guard().is_none());
    assert_eq!(e.get_weight(), 0.0);
    assert_eq!(e.get_dependency(), 0);
}

#[test]
fn edge_builder_fluent_api() {
    let v1 = VertexBuilder::new().name("v1");
    let v2 = VertexBuilder::new().name("v2");
    let e = EdgeBuilder::new()
        .id("e1")
        .name("e_action")
        .source_vertex(v1)
        .target_vertex(v2)
        .guard(Guard::new("x > 0"))
        .add_action(Action::new("x++"))
        .weight(0.75)
        .dependency(50);

    assert_eq!(e.get_id(), "e1");
    assert_eq!(e.get_name(), Some("e_action"));
    assert_eq!(e.get_guard().unwrap().script(), "x > 0");
    assert_eq!(e.get_weight(), 0.75);
    assert_eq!(e.get_dependency(), 50);
}

// =========================================================================
// ModelBuilder — basic construction
// =========================================================================

#[test]
fn empty_model() {
    let model = ModelBuilder::new().build();
    assert!(model.vertices().is_empty());
    assert!(model.edges().is_empty());
    assert!(!model.has_shared_states());
    assert!(!model.has_predefined_path());
}

#[test]
fn model_with_single_vertex() {
    let mut mb = ModelBuilder::new();
    mb.add_vertex(VertexBuilder::new().name("test"));
    let model = mb.build();

    assert_eq!(model.vertices().len(), 1);
    assert_eq!(model.edges().len(), 0);
    assert_eq!(model.vertex(VertexIndex(0)).name(), Some("test"));
}

#[test]
fn model_add_edge_auto_adds_vertices() {
    let v1 = VertexBuilder::new().id("v1").name("v1");
    let v2 = VertexBuilder::new().id("v2").name("v2");
    let e = EdgeBuilder::new()
        .id("e1")
        .source_vertex(v1)
        .target_vertex(v2);

    let mut mb = ModelBuilder::new();
    mb.add_edge(e);
    let model = mb.build();

    assert_eq!(model.vertices().len(), 2);
    assert_eq!(model.edges().len(), 1);
}

#[test]
fn model_shared_vertex_via_clone_dedup() {
    let v1 = VertexBuilder::new().id("v1").name("shared");
    let v2 = VertexBuilder::new().id("v2");
    let v3 = VertexBuilder::new().id("v3");

    let e1 = EdgeBuilder::new()
        .source_vertex(v1.clone())
        .target_vertex(v2);
    let e2 = EdgeBuilder::new()
        .source_vertex(v1.clone())
        .target_vertex(v3);

    let mut mb = ModelBuilder::new();
    mb.add_edge(e1);
    mb.add_edge(e2);
    let model = mb.build();

    // v1 is shared, so 3 unique vertices
    assert_eq!(model.vertices().len(), 3);
    assert_eq!(model.edges().len(), 2);

    // Both edges should reference the same source vertex index
    let src0 = model.edge(EdgeIndex(0)).source_vertex().unwrap();
    let src1 = model.edge(EdgeIndex(1)).source_vertex().unwrap();
    assert_eq!(src0, src1);
}

#[test]
fn model_explicit_vertex_deduped_with_edge_vertex() {
    let v1 = VertexBuilder::new().id("v1").name("v1");
    let v2 = VertexBuilder::new().id("v2").name("v2");

    let e = EdgeBuilder::new()
        .source_vertex(v1.clone())
        .target_vertex(v2);

    let mut mb = ModelBuilder::new();
    mb.add_vertex(v1); // explicitly added
    mb.add_edge(e); // edge's source v1 should dedup
    let model = mb.build();

    assert_eq!(model.vertices().len(), 2);
}

// =========================================================================
// ModelBuilder — delete operations
// =========================================================================

#[test]
fn delete_edge_preserves_vertices() {
    let v1 = VertexBuilder::new().id("v1");
    let v2 = VertexBuilder::new().id("v2");
    let e1 = EdgeBuilder::new()
        .id("e1")
        .source_vertex(v1.clone())
        .target_vertex(v2.clone());

    let mut mb = ModelBuilder::new();
    mb.add_vertex(v1);
    mb.add_vertex(v2);
    mb.add_edge(e1);

    let e_ref = EdgeBuilder::new().id("e1");
    mb.delete_edge(&e_ref);
    let model = mb.build();

    assert_eq!(model.edges().len(), 0);
    assert_eq!(model.vertices().len(), 2);
}

#[test]
fn delete_vertex_removes_connected_edges() {
    let v1 = VertexBuilder::new().id("v1");
    let v2 = VertexBuilder::new().id("v2");
    let e1 = EdgeBuilder::new()
        .id("e1")
        .source_vertex(v1.clone())
        .target_vertex(v2.clone());

    let mut mb = ModelBuilder::new();
    mb.add_vertex(v1.clone());
    mb.add_vertex(v2);
    mb.add_edge(e1);

    mb.delete_vertex(&v1);
    let model = mb.build();

    assert_eq!(model.vertices().len(), 1);
    assert_eq!(model.edges().len(), 0);
}

// =========================================================================
// RuntimeVertex
// =========================================================================

#[test]
fn runtime_vertex_has_methods() {
    let v = VertexBuilder::new()
        .id("n1")
        .name("v_Start")
        .shared_state("SHARED")
        .add_action(Action::new("init()"))
        .add_requirement(Requirement::new("REQ-001"))
        .property("x", json!(42));

    let mut mb = ModelBuilder::new();
    mb.add_vertex(v);
    let model = mb.build();
    let rv = model.vertex(VertexIndex(0));

    assert_eq!(rv.id(), "n1");
    assert!(rv.has_name());
    assert_eq!(rv.name(), Some("v_Start"));
    assert!(rv.has_shared_state());
    assert_eq!(rv.shared_state(), Some("SHARED"));
    assert!(rv.has_actions());
    assert_eq!(rv.actions().len(), 1);
    assert!(rv.has_requirements());
    assert!(rv.has_property("x"));
    assert_eq!(rv.property("x"), Some(&json!(42)));
    assert!(!rv.has_property("nonexistent"));
    assert!(rv.has_properties());
}

#[test]
fn runtime_vertex_empty_has_methods() {
    let v = VertexBuilder::new().id("n1");
    let mut mb = ModelBuilder::new();
    mb.add_vertex(v);
    let model = mb.build();
    let rv = model.vertex(VertexIndex(0));

    assert!(!rv.has_name());
    assert!(!rv.has_shared_state());
    assert!(!rv.has_actions());
    assert!(!rv.has_requirements());
    assert!(!rv.has_properties());
}

#[test]
fn runtime_vertex_equality_by_id() {
    let v1 = VertexBuilder::new().id("same").name("name_a");
    let v2 = VertexBuilder::new().id("same").name("name_b");

    let mut mb1 = ModelBuilder::new();
    mb1.add_vertex(v1);
    let m1 = mb1.build();

    let mut mb2 = ModelBuilder::new();
    mb2.add_vertex(v2);
    let m2 = mb2.build();

    assert_eq!(m1.vertex(VertexIndex(0)), m2.vertex(VertexIndex(0)));
}

#[test]
fn runtime_vertex_inequality_different_id() {
    let v1 = VertexBuilder::new().id("id1");
    let v2 = VertexBuilder::new().id("id2");

    let mut mb = ModelBuilder::new();
    mb.add_vertex(v1);
    mb.add_vertex(v2);
    let model = mb.build();

    assert_ne!(model.vertex(VertexIndex(0)), model.vertex(VertexIndex(1)));
}

// =========================================================================
// RuntimeEdge
// =========================================================================

#[test]
fn runtime_edge_resolves_vertex_indices() {
    let v1 = VertexBuilder::new().id("v1").name("start");
    let v2 = VertexBuilder::new().id("v2").name("end");
    let e = EdgeBuilder::new()
        .id("e1")
        .name("transition")
        .source_vertex(v1)
        .target_vertex(v2)
        .guard(Guard::new("x > 0"))
        .weight(0.5)
        .dependency(75);

    let mut mb = ModelBuilder::new();
    mb.add_edge(e);
    let model = mb.build();

    let re = model.edge(EdgeIndex(0));
    assert_eq!(re.id(), "e1");
    assert!(re.has_name());
    assert_eq!(re.name(), Some("transition"));
    assert!(re.has_guard());
    assert_eq!(re.guard().unwrap().script(), "x > 0");
    assert_eq!(re.weight(), 0.5);
    assert_eq!(re.dependency(), 75);
    assert_eq!(re.dependency_as_f64(), 0.75);

    // Verify source/target resolve to valid vertices
    let src = re.source_vertex().unwrap();
    let tgt = re.target_vertex().unwrap();
    assert_eq!(model.vertex(src).name(), Some("start"));
    assert_eq!(model.vertex(tgt).name(), Some("end"));
}

#[test]
fn runtime_edge_no_guard() {
    let e = EdgeBuilder::new().id("e1");
    let mut mb = ModelBuilder::new();
    mb.add_edge(e);
    let model = mb.build();

    assert!(!model.edge(EdgeIndex(0)).has_guard());
}

#[test]
fn runtime_edge_empty_guard() {
    let e = EdgeBuilder::new().id("e1").guard(Guard::new(""));
    let mut mb = ModelBuilder::new();
    mb.add_edge(e);
    let model = mb.build();

    assert!(!model.edge(EdgeIndex(0)).has_guard());
}

#[test]
fn runtime_edge_no_source_vertex() {
    let v = VertexBuilder::new().id("v1");
    let e = EdgeBuilder::new().id("e1").target_vertex(v);
    let mut mb = ModelBuilder::new();
    mb.add_edge(e);
    let model = mb.build();

    assert!(model.edge(EdgeIndex(0)).source_vertex().is_none());
    assert!(model.edge(EdgeIndex(0)).target_vertex().is_some());
}

// =========================================================================
// RuntimeModel — caches and lookups
// =========================================================================

fn build_triangle_model() -> RuntimeModel {
    // v1 --e1--> v2 --e2--> v3 --e3--> v1
    let v1 = VertexBuilder::new().id("v1").name("vertex1");
    let v2 = VertexBuilder::new().id("v2").name("vertex2");
    let v3 = VertexBuilder::new().id("v3").name("vertex3");

    let e1 = EdgeBuilder::new()
        .id("e1")
        .name("edge1")
        .source_vertex(v1.clone())
        .target_vertex(v2.clone());
    let e2 = EdgeBuilder::new()
        .id("e2")
        .name("edge2")
        .source_vertex(v2.clone())
        .target_vertex(v3.clone());
    let e3 = EdgeBuilder::new()
        .id("e3")
        .name("edge3")
        .source_vertex(v3.clone())
        .target_vertex(v1.clone());

    let mut mb = ModelBuilder::new();
    mb.id("model1");
    mb.name("Triangle");
    mb.add_edge(e1);
    mb.add_edge(e2);
    mb.add_edge(e3);
    mb.build()
}

#[test]
fn model_out_edges() {
    let model = build_triangle_model();
    let v1_idx = VertexIndex(0); // v1

    let out = model.out_edges(v1_idx);
    assert_eq!(out.len(), 1);
    assert_eq!(model.edge(out[0]).name(), Some("edge1"));
}

#[test]
fn model_in_edges() {
    let model = build_triangle_model();
    let v1_idx = VertexIndex(0); // v1

    let in_e = model.in_edges(v1_idx);
    assert_eq!(in_e.len(), 1);
    assert_eq!(model.edge(in_e[0]).name(), Some("edge3"));
}

#[test]
fn model_out_edges_empty_for_isolated_vertex() {
    let mut mb = ModelBuilder::new();
    mb.add_vertex(VertexBuilder::new().id("isolated"));
    let model = mb.build();

    assert!(model.out_edges(VertexIndex(0)).is_empty());
    assert!(model.in_edges(VertexIndex(0)).is_empty());
}

#[test]
fn model_find_vertices_by_name() {
    let model = build_triangle_model();
    let found = model.find_vertices("vertex2");
    assert_eq!(found.len(), 1);
    assert_eq!(model.vertex(found[0]).id(), "v2");
}

#[test]
fn model_find_vertices_not_found() {
    let model = build_triangle_model();
    assert!(model.find_vertices("nonexistent").is_empty());
}

#[test]
fn model_find_edges_by_name() {
    let model = build_triangle_model();
    let found = model.find_edges("edge2");
    assert_eq!(found.len(), 1);
    assert_eq!(model.edge(found[0]).id(), "e2");
}

#[test]
fn model_find_elements_by_name() {
    // Create model where a vertex and edge share the same name
    let v = VertexBuilder::new().id("v1").name("shared_name");
    let e = EdgeBuilder::new()
        .id("e1")
        .name("shared_name")
        .source_vertex(v.clone())
        .target_vertex(VertexBuilder::new().id("v2"));

    let mut mb = ModelBuilder::new();
    mb.add_edge(e);
    let model = mb.build();

    let found = model.find_elements("shared_name");
    assert_eq!(found.len(), 2); // one vertex + one edge
}

#[test]
fn model_element_by_id() {
    let model = build_triangle_model();

    let v_elem = model.element_by_id("v2");
    assert!(v_elem.is_some());
    assert!(matches!(v_elem.unwrap(), ElementIndex::Vertex(_)));

    let e_elem = model.element_by_id("e1");
    assert!(e_elem.is_some());
    assert!(matches!(e_elem.unwrap(), ElementIndex::Edge(_)));

    assert!(model.element_by_id("nonexistent").is_none());
}

#[test]
fn model_all_elements() {
    let model = build_triangle_model();
    let all = model.all_elements();
    assert_eq!(all.len(), 6); // 3 vertices + 3 edges
}

// =========================================================================
// Shared states
// =========================================================================

#[test]
fn model_shared_states() {
    let v1 = VertexBuilder::new()
        .id("v1")
        .name("v1")
        .shared_state("MyState");
    let v2 = VertexBuilder::new()
        .id("v2")
        .name("v2")
        .shared_state("MyState");
    let v3 = VertexBuilder::new().id("v3").name("v3");

    let mut mb = ModelBuilder::new();
    mb.add_vertex(v1);
    mb.add_vertex(v2);
    mb.add_vertex(v3);
    let model = mb.build();

    assert!(model.has_shared_states());
    assert!(model.has_shared_state("MyState"));
    assert!(!model.has_shared_state("Other"));

    let shared = model.shared_state_vertices("MyState");
    assert_eq!(shared.len(), 2);

    let names: Vec<&str> = model.shared_state_names().collect();
    assert_eq!(names.len(), 1);
    assert!(names.contains(&"MyState"));
}

#[test]
fn model_no_shared_states() {
    let mut mb = ModelBuilder::new();
    mb.add_vertex(VertexBuilder::new().id("v1"));
    let model = mb.build();

    assert!(!model.has_shared_states());
    assert!(model.shared_state_vertices("anything").is_empty());
}

// =========================================================================
// Next elements (graph traversal helper)
// =========================================================================

#[test]
fn next_elements_from_vertex() {
    let model = build_triangle_model();
    let next = model.next_elements(ElementIndex::Vertex(VertexIndex(0)));
    assert_eq!(next.len(), 1);
    assert!(matches!(next[0], ElementIndex::Edge(EdgeIndex(0))));
}

#[test]
fn next_elements_from_edge() {
    let model = build_triangle_model();
    let next = model.next_elements(ElementIndex::Edge(EdgeIndex(0)));
    assert_eq!(next.len(), 1);
    // edge0 (e1) goes from v1 to v2, so next is v2 (index 1)
    assert!(matches!(next[0], ElementIndex::Vertex(VertexIndex(1))));
}

// =========================================================================
// Predefined path
// =========================================================================

#[test]
fn predefined_path() {
    let v1 = VertexBuilder::new().id("v1");
    let v2 = VertexBuilder::new().id("v2");
    let e1 = EdgeBuilder::new()
        .id("e1")
        .source_vertex(v1.clone())
        .target_vertex(v2.clone());
    let e2 = EdgeBuilder::new()
        .id("e2")
        .source_vertex(v2)
        .target_vertex(v1);

    let mut mb = ModelBuilder::new();
    mb.add_edge(e1);
    mb.add_edge(e2);
    mb.set_predefined_path_edge_ids(vec!["e1".to_string(), "e2".to_string(), "e1".to_string()]);
    let model = mb.build();

    assert!(model.has_predefined_path());
    let path = model.predefined_path();
    assert_eq!(path.len(), 3);
    assert_eq!(path[0], EdgeIndex(0));
    assert_eq!(path[1], EdgeIndex(1));
    assert_eq!(path[2], EdgeIndex(0));
}

// =========================================================================
// Model-level actions, requirements, properties
// =========================================================================

#[test]
fn model_actions() {
    let mut mb = ModelBuilder::new();
    mb.add_action(Action::new("init()"));
    mb.add_action(Action::new("setup()"));
    let model = mb.build();

    assert!(model.has_actions());
    assert_eq!(model.actions().len(), 2);
    assert_eq!(model.actions()[0].script(), "init()");
}

#[test]
fn model_requirements() {
    let mut mb = ModelBuilder::new();
    mb.add_requirement(Requirement::new("REQ-001"));
    let model = mb.build();

    assert!(model.has_requirements());
    assert!(model.requirements().contains(&Requirement::new("REQ-001")));
}

#[test]
fn model_properties() {
    let mut mb = ModelBuilder::new();
    mb.property("color", json!("grey"));
    let model = mb.build();

    assert!(model.has_property("color"));
    assert_eq!(model.property("color"), Some(&json!("grey")));
    assert!(!model.has_property("other"));
}

// =========================================================================
// RuntimeModel equality
// =========================================================================

#[test]
fn model_equality_by_id() {
    let mut mb1 = ModelBuilder::new();
    mb1.id("same_id");
    mb1.name("model_a");
    let m1 = mb1.build();

    let mut mb2 = ModelBuilder::new();
    mb2.id("same_id");
    mb2.name("model_b");
    let m2 = mb2.build();

    assert_eq!(m1, m2);
}

#[test]
fn model_inequality_different_id() {
    let mut mb1 = ModelBuilder::new();
    mb1.id("id1");
    let m1 = mb1.build();

    let mut mb2 = ModelBuilder::new();
    mb2.id("id2");
    let m2 = mb2.build();

    assert_ne!(m1, m2);
}

// =========================================================================
// Self-loop edge
// =========================================================================

#[test]
fn self_loop_edge() {
    let v = VertexBuilder::new().id("v1").name("loop_vertex");
    let e = EdgeBuilder::new()
        .id("e1")
        .source_vertex(v.clone())
        .target_vertex(v);

    let mut mb = ModelBuilder::new();
    mb.add_edge(e);
    let model = mb.build();

    assert_eq!(model.vertices().len(), 1);
    assert_eq!(model.edges().len(), 1);

    let re = model.edge(EdgeIndex(0));
    assert_eq!(re.source_vertex(), re.target_vertex());

    // Both in and out edges for the vertex
    assert_eq!(model.out_edges(VertexIndex(0)).len(), 1);
    assert_eq!(model.in_edges(VertexIndex(0)).len(), 1);
}

// =========================================================================
// Multiple edges with same name
// =========================================================================

#[test]
fn multiple_edges_same_name() {
    let v1 = VertexBuilder::new().id("v1");
    let v2 = VertexBuilder::new().id("v2");
    let v3 = VertexBuilder::new().id("v3");

    let e1 = EdgeBuilder::new()
        .id("e1")
        .name("action")
        .source_vertex(v1.clone())
        .target_vertex(v2);
    let e2 = EdgeBuilder::new()
        .id("e2")
        .name("action")
        .source_vertex(v1)
        .target_vertex(v3);

    let mut mb = ModelBuilder::new();
    mb.add_edge(e1);
    mb.add_edge(e2);
    let model = mb.build();

    assert_eq!(model.find_edges("action").len(), 2);
}

// =========================================================================
// Large model
// =========================================================================

#[test]
fn large_model() {
    let mut mb = ModelBuilder::new();

    let vertices: Vec<VertexBuilder> = (0..1000)
        .map(|i| {
            VertexBuilder::new()
                .id(format!("v{i}"))
                .name(format!("vertex_{i}"))
        })
        .collect();

    for v in &vertices {
        mb.add_vertex(v.clone());
    }

    for i in 0..999 {
        mb.add_edge(
            EdgeBuilder::new()
                .id(format!("e{i}"))
                .name(format!("edge_{i}"))
                .source_vertex(vertices[i].clone())
                .target_vertex(vertices[i + 1].clone()),
        );
    }

    let model = mb.build();
    assert_eq!(model.vertices().len(), 1000);
    assert_eq!(model.edges().len(), 999);
    assert_eq!(model.all_elements().len(), 1999);

    // Spot-check lookups
    assert_eq!(model.find_vertices("vertex_500").len(), 1);
    assert_eq!(model.find_edges("edge_500").len(), 1);
    assert_eq!(model.out_edges(VertexIndex(500)).len(), 1);
    assert_eq!(model.in_edges(VertexIndex(500)).len(), 1);

    // First and last vertices have different in/out edge counts
    assert_eq!(model.out_edges(VertexIndex(0)).len(), 1);
    assert_eq!(model.in_edges(VertexIndex(0)).len(), 0);
    assert_eq!(model.out_edges(VertexIndex(999)).len(), 0);
    assert_eq!(model.in_edges(VertexIndex(999)).len(), 1);
}

// =========================================================================
// Edge with actions and requirements
// =========================================================================

#[test]
fn edge_actions_and_requirements() {
    let e = EdgeBuilder::new()
        .id("e1")
        .add_action(Action::new("x = x + 1"))
        .add_action(Action::new("y = 0"))
        .add_requirement(Requirement::new("REQ-A"))
        .add_requirement(Requirement::new("REQ-B"))
        .property("color", json!("blue"));

    let mut mb = ModelBuilder::new();
    mb.add_edge(e);
    let model = mb.build();
    let re = model.edge(EdgeIndex(0));

    assert!(re.has_actions());
    assert_eq!(re.actions().len(), 2);
    assert!(re.has_requirements());
    assert_eq!(re.requirements().len(), 2);
    assert!(re.has_property("color"));
}

// =========================================================================
// Vertex with multiple actions (set replaces)
// =========================================================================

#[test]
fn vertex_set_actions_replaces() {
    let v = VertexBuilder::new()
        .id("v1")
        .add_action(Action::new("a"))
        .add_action(Action::new("b"))
        .actions(vec![Action::new("c")]); // replaces

    assert_eq!(v.get_actions().len(), 1);
    assert_eq!(v.get_actions()[0].script(), "c");
}
