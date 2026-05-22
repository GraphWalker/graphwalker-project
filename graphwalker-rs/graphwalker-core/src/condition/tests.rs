use std::time::Duration;

use crate::condition::*;
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

fn walk_cycle(ctx: &mut ExecutionContext, steps: usize) {
    for i in 0..steps {
        let elem = if i % 4 == 0 {
            ElementIndex::Vertex(VertexIndex(0))
        } else if i % 4 == 1 {
            ElementIndex::Edge(EdgeIndex(0))
        } else if i % 4 == 2 {
            ElementIndex::Vertex(VertexIndex(1))
        } else {
            ElementIndex::Edge(EdgeIndex(1))
        };
        ctx.set_current_element(elem);
    }
}

// ---------------------------------------------------------------------------
// Never
// ---------------------------------------------------------------------------

#[test]
fn never_is_never_fulfilled() {
    let ctx = ExecutionContext::new(two_vertex_cycle());
    let cond = StopCondition::Never;

    assert!(!cond.is_fulfilled(&ctx));
    assert_eq!(cond.get_fulfilment(&ctx), 0.0);
}

// ---------------------------------------------------------------------------
// EdgeCoverage
// ---------------------------------------------------------------------------

#[test]
fn edge_coverage_zero_at_start() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let cond = StopCondition::EdgeCoverage(100);

    assert_eq!(cond.get_fulfilment(&ctx), 0.0);
    assert!(!cond.is_fulfilled(&ctx));
}

#[test]
fn edge_coverage_50_percent() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(0)));
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(1)));
    let cond = StopCondition::EdgeCoverage(100);

    let f = cond.get_fulfilment(&ctx);
    assert!((f - 0.5).abs() < 0.001);
}

#[test]
fn edge_coverage_100_percent_fulfilled() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    walk_cycle(&mut ctx, 5);
    let cond = StopCondition::EdgeCoverage(100);

    assert!(cond.get_fulfilment(&ctx) >= FULFILLMENT_LEVEL);
    assert!(cond.is_fulfilled(&ctx));
}

#[test]
fn edge_coverage_not_fulfilled_on_edge() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    walk_cycle(&mut ctx, 4);
    let cond = StopCondition::EdgeCoverage(100);

    assert!(cond.get_fulfilment(&ctx) >= FULFILLMENT_LEVEL);
    assert!(!cond.is_fulfilled(&ctx));
}

#[test]
fn edge_coverage_50_percent_target() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(0)));
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(1)));
    let cond = StopCondition::EdgeCoverage(50);

    assert!(cond.get_fulfilment(&ctx) >= FULFILLMENT_LEVEL);
    assert!(cond.is_fulfilled(&ctx));
}

// ---------------------------------------------------------------------------
// VertexCoverage
// ---------------------------------------------------------------------------

#[test]
fn vertex_coverage_increases_with_visits() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let cond = StopCondition::VertexCoverage(100);

    let f1 = cond.get_fulfilment(&ctx);
    assert!((f1 - 0.5).abs() < 0.001);

    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(0)));
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(1)));

    let f2 = cond.get_fulfilment(&ctx);
    assert!(f2 >= FULFILLMENT_LEVEL);
    assert!(cond.is_fulfilled(&ctx));
}

// ---------------------------------------------------------------------------
// Length
// ---------------------------------------------------------------------------

#[test]
fn length_counts_total_visits() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    let cond = StopCondition::Length(4);

    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    assert_eq!(cond.get_fulfilment(&ctx), 0.25);

    walk_cycle(&mut ctx, 3);
    assert!(cond.get_fulfilment(&ctx) >= FULFILLMENT_LEVEL);
    assert!(cond.is_fulfilled(&ctx));
}

#[test]
fn length_not_fulfilled_on_edge() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    let cond = StopCondition::Length(2);

    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(0)));

    assert!(cond.get_fulfilment(&ctx) >= FULFILLMENT_LEVEL);
    assert!(!cond.is_fulfilled(&ctx));
}

// ---------------------------------------------------------------------------
// ReachedVertex
// ---------------------------------------------------------------------------

#[test]
fn reached_vertex_not_fulfilled_initially() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let cond = StopCondition::reached_vertex("B");

    assert!(!cond.is_fulfilled(&ctx));
    assert!(cond.get_fulfilment(&ctx) < FULFILLMENT_LEVEL);
}

#[test]
fn reached_vertex_fulfilled_when_reached() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(0)));
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(1)));
    let cond = StopCondition::reached_vertex("B");

    assert!(cond.is_fulfilled(&ctx));
}

#[test]
fn reached_vertex_sticky_fulfilled() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(0)));
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(1)));
    let cond = StopCondition::reached_vertex("B");

    assert!(cond.is_fulfilled(&ctx));

    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(1)));
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    assert!(cond.is_fulfilled(&ctx));
}

// ---------------------------------------------------------------------------
// ReachedEdge
// ---------------------------------------------------------------------------

#[test]
fn reached_edge_fulfilled_when_reached() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(0)));
    let cond = StopCondition::reached_edge("e_AB");

    assert!(cond.is_fulfilled(&ctx));
}

// ---------------------------------------------------------------------------
// TimeDuration
// ---------------------------------------------------------------------------

#[test]
fn time_duration_starts_unfulfilled() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let cond = StopCondition::time_duration(Duration::from_secs(60));

    assert!(!cond.is_fulfilled(&ctx));
    assert!(cond.get_fulfilment(&ctx) < FULFILLMENT_LEVEL);
}

#[test]
fn time_duration_zero_is_immediately_fulfilled() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let cond = StopCondition::TimeDuration {
        duration: Duration::from_nanos(1),
        start: std::time::Instant::now() - Duration::from_secs(1),
    };

    assert!(cond.is_fulfilled(&ctx));
}

// ---------------------------------------------------------------------------
// RequirementCoverage
// ---------------------------------------------------------------------------

#[test]
fn requirement_coverage_empty_is_fulfilled() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let cond = StopCondition::RequirementCoverage(100);

    assert_eq!(cond.get_fulfilment(&ctx), 1.0);
    assert!(cond.is_fulfilled(&ctx));
}

#[test]
fn requirement_coverage_tracks_passed_and_failed() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    ctx.set_requirement_status(Requirement::new("R1"), RequirementStatus::Passed);
    ctx.set_requirement_status(Requirement::new("R2"), RequirementStatus::NotCovered);

    let cond = StopCondition::RequirementCoverage(100);
    assert!((cond.get_fulfilment(&ctx) - 0.5).abs() < 0.001);
}

// ---------------------------------------------------------------------------
// DependencyEdgeCoverage
// ---------------------------------------------------------------------------

#[test]
fn dependency_edge_coverage() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone())
            .dependency(80),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone())
            .dependency(20),
    );
    let model = mb.build();

    let mut ctx = ExecutionContext::new(model);
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let cond = StopCondition::DependencyEdgeCoverage(50);

    assert_eq!(cond.get_fulfilment(&ctx), 0.0);

    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(0)));
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(1)));

    assert!(cond.get_fulfilment(&ctx) >= FULFILLMENT_LEVEL);
    assert!(cond.is_fulfilled(&ctx));
}

// ---------------------------------------------------------------------------
// Alternative (OR)
// ---------------------------------------------------------------------------

#[test]
fn alternative_any_child_fulfilled() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let cond = StopCondition::Alternative(vec![StopCondition::Never, StopCondition::Length(1)]);

    assert!(cond.is_fulfilled(&ctx));
}

#[test]
fn alternative_max_fulfilment() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let cond = StopCondition::Alternative(vec![StopCondition::Never, StopCondition::Length(2)]);

    assert_eq!(cond.get_fulfilment(&ctx), 0.5);
}

// ---------------------------------------------------------------------------
// Combined (AND)
// ---------------------------------------------------------------------------

#[test]
fn combined_all_must_be_fulfilled() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let cond = StopCondition::Combined(vec![StopCondition::Length(1), StopCondition::Never]);

    assert!(!cond.is_fulfilled(&ctx));
}

#[test]
fn combined_average_fulfilment() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let cond = StopCondition::Combined(vec![StopCondition::Length(1), StopCondition::Never]);

    assert_eq!(cond.get_fulfilment(&ctx), 0.5);
}

// ---------------------------------------------------------------------------
// PredefinedPath
// ---------------------------------------------------------------------------

#[test]
fn predefined_path_fulfilment() {
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
    let cond = StopCondition::PredefinedPath;

    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    let f1 = cond.get_fulfilment(&ctx);
    assert!((f1 - 0.2).abs() < 0.001);

    ctx.set_predefined_path_current_edge_index(1);
    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(0)));
    let f2 = cond.get_fulfilment(&ctx);
    assert!((f2 - 0.4).abs() < 0.001);
}

// ---------------------------------------------------------------------------
// InternalState
// ---------------------------------------------------------------------------

#[test]
fn internal_state_not_fulfilled_when_false() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    ctx.execute_action(&crate::model::Action::new("let x = 3"))
        .unwrap();

    let cond = StopCondition::InternalState("x > 5".to_string());
    assert!(!cond.is_fulfilled(&ctx));
    assert_eq!(cond.get_fulfilment(&ctx), 0.0);
}

#[test]
fn internal_state_fulfilled_when_true() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    ctx.execute_action(&crate::model::Action::new("let x = 10"))
        .unwrap();

    let cond = StopCondition::InternalState("x > 5".to_string());
    assert!(cond.is_fulfilled(&ctx));
    assert_eq!(cond.get_fulfilment(&ctx), 1.0);
}

#[test]
fn internal_state_requires_vertex() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));
    ctx.execute_action(&crate::model::Action::new("let x = 10"))
        .unwrap();
    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(0)));

    let cond = StopCondition::InternalState("x > 5".to_string());
    assert!(!cond.is_fulfilled(&ctx));
    assert_eq!(cond.get_fulfilment(&ctx), 1.0);
}

#[test]
fn internal_state_script_error_returns_false() {
    let mut ctx = ExecutionContext::new(two_vertex_cycle());
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(0)));

    let cond = StopCondition::InternalState("undefined_var > 5".to_string());
    assert!(!cond.is_fulfilled(&ctx));
}

// ---------------------------------------------------------------------------
// Display
// ---------------------------------------------------------------------------

#[test]
fn display_formatting() {
    assert_eq!(
        StopCondition::EdgeCoverage(100).to_string(),
        "EdgeCoverage(100)"
    );
    assert_eq!(StopCondition::Never.to_string(), "Never");
    assert_eq!(
        StopCondition::reached_vertex("B").to_string(),
        "ReachedVertex(B)"
    );
    assert_eq!(
        StopCondition::Alternative(vec![StopCondition::Never, StopCondition::Length(5),])
            .to_string(),
        "(Never OR Length(5))"
    );
    assert_eq!(
        StopCondition::InternalState("x > 5".to_string()).to_string(),
        "InternalState(x > 5)"
    );
}

// ---------------------------------------------------------------------------
// target_elements
// ---------------------------------------------------------------------------

#[test]
fn target_elements_for_reached_vertex() {
    let ctx = ExecutionContext::new(two_vertex_cycle());
    let cond = StopCondition::reached_vertex("A");
    let targets = cond.target_elements(&ctx);

    assert_eq!(targets.len(), 1);
    assert_eq!(targets[0], ElementIndex::Vertex(VertexIndex(0)));
}

#[test]
fn target_elements_for_reached_edge() {
    let ctx = ExecutionContext::new(two_vertex_cycle());
    let cond = StopCondition::reached_edge("e_AB");
    let targets = cond.target_elements(&ctx);

    assert_eq!(targets.len(), 1);
    assert_eq!(targets[0], ElementIndex::Edge(EdgeIndex(0)));
}

#[test]
fn target_elements_for_non_reached_is_empty() {
    let ctx = ExecutionContext::new(two_vertex_cycle());
    let cond = StopCondition::EdgeCoverage(100);
    assert!(cond.target_elements(&ctx).is_empty());
}
