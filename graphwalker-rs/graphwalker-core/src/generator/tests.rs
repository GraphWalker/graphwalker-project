use crate::condition::StopCondition;
use crate::generator::*;
use crate::machine::ExecutionContext;
use crate::model::*;

fn two_vertex_cycle() -> RuntimeModel {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .name("e_AB")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .name("e_BA")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    mb.build()
}

fn triangle_model() -> RuntimeModel {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");
    let v_c = VertexBuilder::new().id("vc").name("C");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .name("e_AB")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_bc")
            .name("e_BC")
            .source_vertex(v_b.clone())
            .target_vertex(v_c.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ca")
            .name("e_CA")
            .source_vertex(v_c.clone())
            .target_vertex(v_a.clone()),
    );
    mb.build()
}

// ---------------------------------------------------------------------------
// RandomPath
// ---------------------------------------------------------------------------

#[test]
fn random_path_walks_cycle() {
    let mut ctx = ExecutionContext::new_with_seed(two_vertex_cycle(), 42);
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::random(StopCondition::EdgeCoverage(100));

    let mut steps = 0;
    while gen.has_next_step(&ctx) && steps < 100 {
        gen.get_next_step(&mut ctx).unwrap();
        steps += 1;
    }

    assert!(steps > 0);
    assert!(!gen.has_next_step(&ctx));
}

#[test]
fn random_path_errors_on_no_current() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    let mut gen = PathGenerator::random(StopCondition::Never);

    let result = gen.get_next_step(&mut ctx);
    assert!(matches!(result, Err(GeneratorError::NoCurrentElement)));
}

#[test]
fn random_path_alternates_vertex_edge() {
    let mut ctx = ExecutionContext::new_with_seed(two_vertex_cycle(), 42);
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::random(StopCondition::Length(6));

    let mut elements = vec![ctx.current_element().unwrap()];
    while gen.has_next_step(&ctx) {
        gen.get_next_step(&mut ctx).unwrap();
        elements.push(ctx.current_element().unwrap());
    }

    for (i, elem) in elements.iter().enumerate() {
        if i % 2 == 0 {
            assert!(
                matches!(elem, ElementIndex::Vertex(_)),
                "expected vertex at index {}, got {:?}",
                i,
                elem
            );
        } else {
            assert!(
                matches!(elem, ElementIndex::Edge(_)),
                "expected edge at index {}, got {:?}",
                i,
                elem
            );
        }
    }
}

// ---------------------------------------------------------------------------
// QuickRandomPath
// ---------------------------------------------------------------------------

#[test]
fn quick_random_path_covers_all_edges() {
    let mut ctx = ExecutionContext::new_with_seed(triangle_model(), 42);
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::quick_random(StopCondition::EdgeCoverage(100));

    let mut steps = 0;
    while gen.has_next_step(&ctx) && steps < 200 {
        gen.get_next_step(&mut ctx).unwrap();
        steps += 1;
    }

    assert!(!gen.has_next_step(&ctx));
    for i in 0..3 {
        assert!(ctx.is_visited(ElementIndex::Edge(EdgeIndex(i))));
    }
}

// ---------------------------------------------------------------------------
// WeightedRandomPath
// ---------------------------------------------------------------------------

#[test]
fn weighted_random_respects_weights() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");
    let v_c = VertexBuilder::new().id("vc").name("C");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone())
            .weight(0.9),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ac")
            .source_vertex(v_a.clone())
            .target_vertex(v_c.clone())
            .weight(0.1),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ca")
            .source_vertex(v_c.clone())
            .target_vertex(v_a.clone()),
    );
    let model = mb.build();

    let mut ctx = ExecutionContext::new_with_seed(model, 42);
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::weighted_random(StopCondition::Length(200));

    let mut ab_count = 0u64;
    let mut ac_count = 0u64;

    while gen.has_next_step(&ctx) {
        gen.get_next_step(&mut ctx).unwrap();
        if ctx.current_element() == Some(ElementIndex::Edge(EdgeIndex(0))) {
            ab_count += 1;
        } else if ctx.current_element() == Some(ElementIndex::Edge(EdgeIndex(1))) {
            ac_count += 1;
        }
    }

    assert!(
        ab_count > ac_count,
        "Expected e_AB (weight 0.9) to be chosen more than e_AC (weight 0.1), got ab={} ac={}",
        ab_count,
        ac_count
    );
}

// ---------------------------------------------------------------------------
// AStarPath
// ---------------------------------------------------------------------------

#[test]
fn astar_path_reaches_target() {
    let mut ctx = ExecutionContext::new_with_seed(triangle_model(), 42);
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::a_star(StopCondition::reached_vertex("C"));

    let mut steps = 0;
    while gen.has_next_step(&ctx) && steps < 50 {
        gen.get_next_step(&mut ctx).unwrap();
        steps += 1;
    }

    assert!(!gen.has_next_step(&ctx));
    assert!(ctx.is_visited(ElementIndex::Vertex(VertexIndex(2))));
}

// ---------------------------------------------------------------------------
// ShortestAllPaths
// ---------------------------------------------------------------------------

#[test]
fn shortest_all_paths_covers_all_edges() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::shortest_all_paths(StopCondition::EdgeCoverage(100));

    let mut steps = 0;
    while gen.has_next_step(&ctx) && steps < 50 {
        gen.get_next_step(&mut ctx).unwrap();
        steps += 1;
    }

    assert!(!gen.has_next_step(&ctx));
    for i in 0..2 {
        assert!(ctx.is_visited(ElementIndex::Edge(EdgeIndex(i))));
    }
}

// ---------------------------------------------------------------------------
// PredefinedPath
// ---------------------------------------------------------------------------

#[test]
fn predefined_path_follows_defined_order() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    mb.set_predefined_path_edge_ids(vec!["e_ab".to_string(), "e_ba".to_string()]);
    let model = mb.build();

    let mut ctx = ExecutionContext::new(model);
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::predefined(StopCondition::PredefinedPath);

    let mut visited = Vec::new();
    while gen.has_next_step(&ctx) {
        gen.get_next_step(&mut ctx).unwrap();
        visited.push(ctx.current_element().unwrap());
    }

    assert_eq!(
        visited,
        vec![
            ElementIndex::Edge(EdgeIndex(0)),
            ElementIndex::Vertex(VertexIndex(1)),
            ElementIndex::Edge(EdgeIndex(1)),
            ElementIndex::Vertex(VertexIndex(0)),
        ]
    );
}

// ---------------------------------------------------------------------------
// CombinedPath
// ---------------------------------------------------------------------------

#[test]
fn combined_path_chains_generators() {
    let mut ctx = ExecutionContext::new_with_seed(two_vertex_cycle(), 42);
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let gen1 = PathGenerator::random(StopCondition::Length(3));
    let gen2 = PathGenerator::random(StopCondition::Length(6));
    let mut combined = PathGenerator::combined(vec![gen1, gen2]);

    let mut steps = 0;
    while combined.has_next_step(&ctx) && steps < 50 {
        combined.get_next_step(&mut ctx).unwrap();
        steps += 1;
    }

    assert!(!combined.has_next_step(&ctx));
    assert_eq!(ctx.total_visit_count(), 7);
}

// ---------------------------------------------------------------------------
// Guard filtering
// ---------------------------------------------------------------------------

#[test]
fn random_path_respects_guards() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");
    let v_c = VertexBuilder::new().id("vc").name("C");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .name("e_AB")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone())
            .guard(crate::model::Guard::new("x > 100")),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ac")
            .name("e_AC")
            .source_vertex(v_a.clone())
            .target_vertex(v_c.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ca")
            .name("e_CA")
            .source_vertex(v_c.clone())
            .target_vertex(v_a.clone()),
    );
    let model = mb.build();

    let mut ctx = ExecutionContext::new_with_seed(model, 42);
    ctx.execute_action(&crate::model::Action::new("let x = 0"))
        .unwrap();
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::random(StopCondition::Length(20));

    while gen.has_next_step(&ctx) {
        gen.get_next_step(&mut ctx).unwrap();
    }

    assert!(
        !ctx.is_visited(ElementIndex::Edge(EdgeIndex(0))),
        "Guarded edge e_AB should never be visited when x=0"
    );
    assert!(ctx.is_visited(ElementIndex::Edge(EdgeIndex(1))));
}

#[test]
fn random_path_with_internal_state_stop() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone())
            .add_action(crate::model::Action::new("count = count + 1")),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    let model = mb.build();

    let mut ctx = ExecutionContext::new_with_seed(model, 42);
    ctx.execute_action(&crate::model::Action::new("let count = 0"))
        .unwrap();
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::random(StopCondition::InternalState("count >= 3".to_string()));

    let mut steps = 0;
    while gen.has_next_step(&ctx) && steps < 200 {
        gen.get_next_step(&mut ctx).unwrap();
        if let Some(elem) = ctx.current_element() {
            ctx.execute_element_actions(elem).unwrap();
        }
        steps += 1;
    }

    assert!(!gen.has_next_step(&ctx));
    let count = ctx.get_attribute("count").unwrap();
    assert!(count.as_int().unwrap() >= 3);
}

// ---------------------------------------------------------------------------
// Display
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// NewYorkStreetSweeper
// ---------------------------------------------------------------------------

#[test]
fn new_york_street_sweeper_eulerian_graph() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::new_york_street_sweeper();

    let mut steps = 0;
    while gen.has_next_step(&ctx) && steps < 100 {
        gen.get_next_step(&mut ctx).unwrap();
        steps += 1;
    }

    assert!(!gen.has_next_step(&ctx));
    for i in 0..2 {
        assert!(
            ctx.is_visited(ElementIndex::Edge(EdgeIndex(i))),
            "Edge {} should be visited",
            i
        );
    }
}

#[test]
fn new_york_street_sweeper_triangle() {
    let mut ctx = ExecutionContext::new(triangle_model());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::new_york_street_sweeper();

    let mut elements = Vec::new();
    while gen.has_next_step(&ctx) {
        gen.get_next_step(&mut ctx).unwrap();
        elements.push(ctx.current_element().unwrap());
    }

    assert_eq!(elements.len(), 6);

    for i in 0..3 {
        assert!(ctx.is_visited(ElementIndex::Edge(EdgeIndex(i))));
    }
}

#[test]
fn new_york_street_sweeper_non_eulerian() {
    // A→B, B→C, C→A, A→B (not Eulerian, but strongly connected)
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");
    let v_c = VertexBuilder::new().id("vc").name("C");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e1")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e2")
            .source_vertex(v_b.clone())
            .target_vertex(v_c.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e3")
            .source_vertex(v_c.clone())
            .target_vertex(v_a.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e4")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    let model = mb.build();

    let mut ctx = ExecutionContext::new(model);
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::new_york_street_sweeper();

    let mut steps = 0;
    while gen.has_next_step(&ctx) && steps < 200 {
        gen.get_next_step(&mut ctx).unwrap();
        steps += 1;
    }

    assert!(!gen.has_next_step(&ctx));
    for i in 0..4 {
        assert!(
            ctx.is_visited(ElementIndex::Edge(EdgeIndex(i))),
            "Edge {} should be visited",
            i
        );
    }
}

#[test]
fn new_york_street_sweeper_not_strongly_connected_errors() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e1")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    let model = mb.build();

    let mut ctx = ExecutionContext::new(model);
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let mut gen = PathGenerator::new_york_street_sweeper();
    let result = gen.get_next_step(&mut ctx);
    assert!(result.is_err());
}

// ---------------------------------------------------------------------------
// Display
// ---------------------------------------------------------------------------

#[test]
fn display_formatting() {
    let gen = PathGenerator::random(StopCondition::EdgeCoverage(100));
    assert_eq!(gen.to_string(), "RandomPath(EdgeCoverage(100))");

    let gen = PathGenerator::new_york_street_sweeper();
    assert_eq!(gen.to_string(), "NewYorkStreetSweeper()");

    let gen = PathGenerator::combined(vec![
        PathGenerator::random(StopCondition::Length(5)),
        PathGenerator::random(StopCondition::Never),
    ]);
    assert_eq!(
        gen.to_string(),
        "CombinedPath(RandomPath(Length(5)), RandomPath(Never))"
    );
}
