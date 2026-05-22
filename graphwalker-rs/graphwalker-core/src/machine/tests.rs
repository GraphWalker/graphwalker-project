use std::cell::RefCell;
use std::rc::Rc;

use crate::condition::StopCondition;
use crate::generator::PathGenerator;
use crate::machine::{
    EventType, ExecutionContext, Machine, MachineError, MachineObserver, ScriptError,
};
use crate::model::*;

fn branching_model() -> RuntimeModel {
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
            .id("e_ac")
            .name("e_AC")
            .source_vertex(v_a.clone())
            .target_vertex(v_c.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .name("e_BA")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
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

fn simple_model() -> RuntimeModel {
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

#[test]
fn new_context_has_no_current_element() {
    let ctx = ExecutionContext::new(simple_model());
    assert!(ctx.current_element().is_none());
    assert!(ctx.last_element().is_none());
    assert_eq!(ctx.total_visit_count(), 0);
    assert_eq!(ctx.execution_status(), ExecutionStatus::NotExecuted);
}

#[test]
fn set_current_element_records_visit() {
    let mut ctx = ExecutionContext::new(simple_model());
    let va = ElementIndex::Vertex(VertexIndex(0));

    ctx.set_current_element(va);

    assert_eq!(ctx.current_element(), Some(va));
    assert!(ctx.is_visited(va));
    assert_eq!(ctx.visit_count(va), 1);
    assert_eq!(ctx.total_visit_count(), 1);
    assert!(ctx.is_at_vertex());
}

#[test]
fn set_current_element_updates_last() {
    let mut ctx = ExecutionContext::new(simple_model());
    let va = ElementIndex::Vertex(VertexIndex(0));
    let e0 = ElementIndex::Edge(EdgeIndex(0));

    ctx.set_current_element(va);
    ctx.set_current_element(e0);

    assert_eq!(ctx.current_element(), Some(e0));
    assert_eq!(ctx.last_element(), Some(va));
    assert!(!ctx.is_at_vertex());
}

#[test]
fn visit_count_increments() {
    let mut ctx = ExecutionContext::new(simple_model());
    let va = ElementIndex::Vertex(VertexIndex(0));

    ctx.set_current_element(va);
    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(0)));
    ctx.set_current_element(ElementIndex::Vertex(VertexIndex(1)));
    ctx.set_current_element(ElementIndex::Edge(EdgeIndex(1)));
    ctx.set_current_element(va);

    assert_eq!(ctx.visit_count(va), 2);
    assert_eq!(ctx.total_visit_count(), 5);
}

#[test]
fn unvisited_elements_return_zero() {
    let ctx = ExecutionContext::new(simple_model());
    let va = ElementIndex::Vertex(VertexIndex(0));

    assert!(!ctx.is_visited(va));
    assert_eq!(ctx.visit_count(va), 0);
}

#[test]
fn requirement_tracking() {
    let mut ctx = ExecutionContext::new(simple_model());
    let req1 = Requirement::new("REQ-001");
    let req2 = Requirement::new("REQ-002");

    ctx.set_requirement_status(req1, RequirementStatus::Passed);
    ctx.set_requirement_status(req2, RequirementStatus::Failed);

    assert_eq!(ctx.requirement_count(), 2);
    assert_eq!(ctx.requirements_with_status(RequirementStatus::Passed), 1);
    assert_eq!(ctx.requirements_with_status(RequirementStatus::Failed), 1);
    assert_eq!(
        ctx.requirements_with_status(RequirementStatus::NotCovered),
        0
    );
}

#[test]
fn predefined_path_index() {
    let mut ctx = ExecutionContext::new(simple_model());
    assert_eq!(ctx.predefined_path_current_edge_index(), 0);

    ctx.set_predefined_path_current_edge_index(3);
    assert_eq!(ctx.predefined_path_current_edge_index(), 3);
}

#[test]
fn filter_elements_passes_through() {
    let ctx = ExecutionContext::new(simple_model());
    let elements = vec![
        ElementIndex::Vertex(VertexIndex(0)),
        ElementIndex::Edge(EdgeIndex(0)),
    ];
    let filtered = ctx.filter_elements(&elements);
    assert_eq!(filtered, elements);
}

#[test]
fn floyd_warshall_cache_is_lazy() {
    let ctx = ExecutionContext::new(simple_model());
    let fw = ctx.floyd_warshall();
    let va = ElementIndex::Vertex(VertexIndex(0));
    let vb = ElementIndex::Vertex(VertexIndex(1));

    assert_eq!(fw.shortest_distance(va, va), 0);
    assert!(fw.shortest_distance(va, vb) > 0);
}

#[test]
fn seeded_context_is_deterministic() {
    let mut ctx1 = ExecutionContext::new_with_seed(simple_model(), 42);
    let mut ctx2 = ExecutionContext::new_with_seed(simple_model(), 42);

    let a = ctx1.gen_usize(100);
    let b = ctx2.gen_usize(100);
    assert_eq!(a, b);
}

#[test]
fn different_seeds_produce_different_sequences() {
    let mut ctx1 = ExecutionContext::new_with_seed(simple_model(), 42);
    let mut ctx2 = ExecutionContext::new_with_seed(simple_model(), 99);

    let seq1: Vec<usize> = (0..20).map(|_| ctx1.gen_usize(1000)).collect();
    let seq2: Vec<usize> = (0..20).map(|_| ctx2.gen_usize(1000)).collect();
    assert_ne!(seq1, seq2);
}

#[test]
fn same_seed_produces_identical_execution_path() {
    let run = |seed: u64| -> Vec<(usize, ElementIndex)> {
        let mut ctx = ExecutionContext::new_with_seed(branching_model(), seed);
        ctx.set_next_element(Some(ElementIndex::Vertex(VertexIndex(0))));
        let gen = PathGenerator::random(StopCondition::Length(20));
        let mut machine = Machine::new(vec![(ctx, gen)]).unwrap();
        while machine.has_next_step() {
            machine.get_next_step().unwrap();
        }
        machine.execution_path().to_vec()
    };

    assert_eq!(run(42), run(42));
    assert_eq!(run(123), run(123));
}

#[test]
fn different_seeds_produce_different_execution_paths() {
    let run = |seed: u64| -> Vec<(usize, ElementIndex)> {
        let mut ctx = ExecutionContext::new_with_seed(branching_model(), seed);
        ctx.set_next_element(Some(ElementIndex::Vertex(VertexIndex(0))));
        let gen = PathGenerator::random(StopCondition::Length(20));
        let mut machine = Machine::new(vec![(ctx, gen)]).unwrap();
        while machine.has_next_step() {
            machine.get_next_step().unwrap();
        }
        machine.execution_path().to_vec()
    };

    assert_ne!(run(42), run(99));
}

// ---------------------------------------------------------------------------
// Script engine — actions
// ---------------------------------------------------------------------------

#[test]
fn execute_action_sets_variable() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.execute_action(&Action::new("let x = 42")).unwrap();

    let val = ctx.get_attribute("x").unwrap();
    assert_eq!(val.as_int().unwrap(), 42);
}

#[test]
fn execute_action_modifies_variable() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.execute_action(&Action::new("let x = 10")).unwrap();
    ctx.execute_action(&Action::new("x = x + 5")).unwrap();

    let val = ctx.get_attribute("x").unwrap();
    assert_eq!(val.as_int().unwrap(), 15);
}

#[test]
fn execute_action_empty_script_is_noop() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.execute_action(&Action::new("")).unwrap();
}

#[test]
fn execute_action_invalid_script_returns_error() {
    let ctx = ExecutionContext::new(simple_model());
    let result = ctx.execute_action(&Action::new("let @@@invalid"));
    assert!(result.is_err());
}

#[test]
fn execute_multiple_actions() {
    let ctx = ExecutionContext::new(simple_model());
    let actions = vec![
        Action::new("let a = 1"),
        Action::new("let b = 2"),
        Action::new("let c = a + b"),
    ];
    ctx.execute_actions(&actions).unwrap();

    let val = ctx.get_attribute("c").unwrap();
    assert_eq!(val.as_int().unwrap(), 3);
}

// ---------------------------------------------------------------------------
// Script engine — global scope
// ---------------------------------------------------------------------------

#[test]
fn global_action_uses_global_scope() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.execute_action(&Action::new("let global.count = 100"))
        .unwrap();

    let val = ctx.get_attribute("global.count").unwrap();
    assert_eq!(val.as_int().unwrap(), 100);

    assert!(ctx.get_attribute("count").is_none());
}

#[test]
fn local_and_global_scopes_are_independent() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.execute_action(&Action::new("let x = 10")).unwrap();
    ctx.execute_action(&Action::new("let global.x = 20"))
        .unwrap();

    let local = ctx.get_attribute("x").unwrap();
    let global = ctx.get_attribute("global.x").unwrap();
    assert_eq!(local.as_int().unwrap(), 10);
    assert_eq!(global.as_int().unwrap(), 20);
}

// ---------------------------------------------------------------------------
// Script engine — guards
// ---------------------------------------------------------------------------

#[test]
fn evaluate_guard_true() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.execute_action(&Action::new("let x = 10")).unwrap();

    assert!(ctx.evaluate_guard("x > 5").unwrap());
}

#[test]
fn evaluate_guard_false() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.execute_action(&Action::new("let x = 3")).unwrap();

    assert!(!ctx.evaluate_guard("x > 5").unwrap());
}

#[test]
fn evaluate_guard_empty_returns_true() {
    let ctx = ExecutionContext::new(simple_model());
    assert!(ctx.evaluate_guard("").unwrap());
}

#[test]
fn evaluate_guard_non_boolean_returns_error() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.execute_action(&Action::new("let x = 42")).unwrap();

    let result = ctx.evaluate_guard("x");
    assert!(result.is_err());
}

#[test]
fn evaluate_guard_global_scope() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.execute_action(&Action::new("let global.ready = true"))
        .unwrap();

    assert!(ctx.evaluate_guard("global.ready").unwrap());
}

// ---------------------------------------------------------------------------
// Script engine — edge availability (guards)
// ---------------------------------------------------------------------------

fn guarded_model() -> RuntimeModel {
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
            .guard(Guard::new("x > 5")),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ac")
            .name("e_AC")
            .source_vertex(v_a.clone())
            .target_vertex(v_c.clone()),
    );
    mb.build()
}

#[test]
fn is_edge_available_no_guard() {
    let ctx = ExecutionContext::new(guarded_model());
    assert!(ctx.is_edge_available(EdgeIndex(1)));
}

#[test]
fn is_edge_available_guard_true() {
    let ctx = ExecutionContext::new(guarded_model());
    ctx.execute_action(&Action::new("let x = 10")).unwrap();

    assert!(ctx.is_edge_available(EdgeIndex(0)));
}

#[test]
fn is_edge_available_guard_false() {
    let ctx = ExecutionContext::new(guarded_model());
    ctx.execute_action(&Action::new("let x = 3")).unwrap();

    assert!(!ctx.is_edge_available(EdgeIndex(0)));
}

#[test]
fn is_edge_available_guard_error_returns_false() {
    let ctx = ExecutionContext::new(guarded_model());
    assert!(!ctx.is_edge_available(EdgeIndex(0)));
}

// ---------------------------------------------------------------------------
// Script engine — filter_elements with guards
// ---------------------------------------------------------------------------

#[test]
fn filter_elements_blocks_guarded_edge() {
    let ctx = ExecutionContext::new(guarded_model());
    ctx.execute_action(&Action::new("let x = 3")).unwrap();

    let elements = vec![
        ElementIndex::Edge(EdgeIndex(0)),
        ElementIndex::Edge(EdgeIndex(1)),
    ];
    let filtered = ctx.filter_elements(&elements);
    assert_eq!(filtered, vec![ElementIndex::Edge(EdgeIndex(1))]);
}

#[test]
fn filter_elements_allows_satisfied_guard() {
    let ctx = ExecutionContext::new(guarded_model());
    ctx.execute_action(&Action::new("let x = 10")).unwrap();

    let elements = vec![
        ElementIndex::Edge(EdgeIndex(0)),
        ElementIndex::Edge(EdgeIndex(1)),
    ];
    let filtered = ctx.filter_elements(&elements);
    assert_eq!(filtered, elements);
}

#[test]
fn filter_elements_always_passes_vertices() {
    let ctx = ExecutionContext::new(guarded_model());

    let elements = vec![
        ElementIndex::Vertex(VertexIndex(0)),
        ElementIndex::Vertex(VertexIndex(1)),
    ];
    let filtered = ctx.filter_elements(&elements);
    assert_eq!(filtered, elements);
}

// ---------------------------------------------------------------------------
// Script engine — element actions
// ---------------------------------------------------------------------------

fn model_with_actions() -> RuntimeModel {
    let v_a = VertexBuilder::new()
        .id("va")
        .name("A")
        .add_action(Action::new("let vertex_visited = true"));
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone())
            .add_action(Action::new("let edge_traversed = true")),
    );
    mb.build()
}

#[test]
fn execute_element_actions_vertex() {
    let ctx = ExecutionContext::new(model_with_actions());
    ctx.execute_element_actions(ElementIndex::Vertex(VertexIndex(0)))
        .unwrap();

    let val = ctx.get_attribute("vertex_visited").unwrap();
    assert_eq!(val.as_bool().unwrap(), true);
}

#[test]
fn execute_element_actions_edge() {
    let ctx = ExecutionContext::new(model_with_actions());
    ctx.execute_element_actions(ElementIndex::Edge(EdgeIndex(0)))
        .unwrap();

    let val = ctx.get_attribute("edge_traversed").unwrap();
    assert_eq!(val.as_bool().unwrap(), true);
}

// ---------------------------------------------------------------------------
// Script engine — model-level actions
// ---------------------------------------------------------------------------

#[test]
fn execute_model_actions() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a)
            .target_vertex(v_b),
    );
    mb.add_action(Action::new("let initialized = true"));
    let model = mb.build();

    let ctx = ExecutionContext::new(model);
    ctx.execute_model_actions().unwrap();

    let val = ctx.get_attribute("initialized").unwrap();
    assert_eq!(val.as_bool().unwrap(), true);
}

// ---------------------------------------------------------------------------
// Script engine — set/get attribute
// ---------------------------------------------------------------------------

#[test]
fn set_and_get_attribute() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.set_attribute("name", "GraphWalker");

    let val = ctx.get_attribute("name").unwrap();
    assert_eq!(val.into_string().unwrap(), "GraphWalker");
}

#[test]
fn set_attribute_overwrites() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.set_attribute("x", 1 as i64);
    ctx.set_attribute("x", 2 as i64);

    let val = ctx.get_attribute("x").unwrap();
    assert_eq!(val.as_int().unwrap(), 2);
}

#[test]
fn get_attribute_nonexistent() {
    let ctx = ExecutionContext::new(simple_model());
    assert!(ctx.get_attribute("nonexistent").is_none());
}

#[test]
fn set_global_attribute() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.set_global_attribute("counter", 0 as i64);

    let val = ctx.get_attribute("global.counter").unwrap();
    assert_eq!(val.as_int().unwrap(), 0);
}

// ---------------------------------------------------------------------------
// Script engine — data dump
// ---------------------------------------------------------------------------

#[test]
fn data_returns_variable_dump() {
    let ctx = ExecutionContext::new(simple_model());
    ctx.execute_action(&Action::new("let a = 1")).unwrap();
    ctx.execute_action(&Action::new("let global.b = 2"))
        .unwrap();

    let data = ctx.data();
    assert!(data.contains("a=1"));
    assert!(data.contains("global.b=2"));
}

// ---------------------------------------------------------------------------
// Script engine — ScriptError
// ---------------------------------------------------------------------------

#[test]
fn script_error_display() {
    let err = ScriptError::new("something went wrong");
    assert_eq!(err.to_string(), "Script error: something went wrong");
}

// ===========================================================================
// Machine tests
// ===========================================================================

fn make_context(
    model: RuntimeModel,
    start: ElementIndex,
    gen: PathGenerator,
) -> (ExecutionContext, PathGenerator) {
    let mut ctx = ExecutionContext::new_with_seed(model, 42);
    ctx.set_next_element(Some(start));
    (ctx, gen)
}

// ---------------------------------------------------------------------------
// Machine — construction
// ---------------------------------------------------------------------------

#[test]
fn machine_no_contexts_error() {
    let result = Machine::new(vec![]);
    assert!(matches!(result, Err(MachineError::NoContexts)));
}

#[test]
fn machine_no_start_context_error() {
    let ctx = ExecutionContext::new(simple_model());
    let gen = PathGenerator::random(StopCondition::EdgeCoverage(100));
    let result = Machine::new(vec![(ctx, gen)]);
    assert!(matches!(result, Err(MachineError::NoStartContext)));
}

// ---------------------------------------------------------------------------
// Machine — single model execution
// ---------------------------------------------------------------------------

#[test]
fn machine_single_model_edge_coverage() {
    let entry = make_context(
        simple_model(),
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::random(StopCondition::EdgeCoverage(100)),
    );
    let mut machine = Machine::new_with_seed(vec![entry], 42).unwrap();

    let mut steps = 0;
    while machine.has_next_step() && steps < 200 {
        machine.get_next_step().unwrap();
        steps += 1;
    }

    assert!(!machine.has_next_step());
    assert!(steps > 0);

    let ctx = machine.current_context();
    assert!(ctx.is_visited(ElementIndex::Edge(EdgeIndex(0))));
    assert!(ctx.is_visited(ElementIndex::Edge(EdgeIndex(1))));
    assert_eq!(ctx.execution_status(), ExecutionStatus::Completed);
}

#[test]
fn machine_execution_path_recorded() {
    let entry = make_context(
        simple_model(),
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::random(StopCondition::Length(4)),
    );
    let mut machine = Machine::new_with_seed(vec![entry], 42).unwrap();

    while machine.has_next_step() {
        machine.get_next_step().unwrap();
    }

    let path = machine.execution_path();
    assert!(!path.is_empty());
    for &(ctx_idx, _elem) in path {
        assert_eq!(ctx_idx, 0);
    }
}

#[test]
fn machine_status_transitions() {
    let entry = make_context(
        simple_model(),
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::random(StopCondition::Length(3)),
    );
    let mut machine = Machine::new_with_seed(vec![entry], 42).unwrap();

    assert_eq!(
        machine.current_context().execution_status(),
        ExecutionStatus::NotExecuted
    );

    machine.get_next_step().unwrap();
    assert_eq!(
        machine.current_context().execution_status(),
        ExecutionStatus::Executing
    );

    while machine.has_next_step() {
        machine.get_next_step().unwrap();
    }
    assert_eq!(
        machine.current_context().execution_status(),
        ExecutionStatus::Completed
    );
}

// ---------------------------------------------------------------------------
// Machine — actions and guards
// ---------------------------------------------------------------------------

#[test]
fn machine_executes_element_actions() {
    let v_a = VertexBuilder::new()
        .id("va")
        .name("A")
        .add_action(Action::new("let visits = 0"));
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone())
            .add_action(Action::new("visits = visits + 1")),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    let model = mb.build();

    let entry = make_context(
        model,
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::random(StopCondition::Length(10)),
    );
    let mut machine = Machine::new_with_seed(vec![entry], 42).unwrap();

    while machine.has_next_step() {
        machine.get_next_step().unwrap();
    }

    let visits = machine.current_context().get_attribute("visits").unwrap();
    assert!(visits.as_int().unwrap() > 0);
}

#[test]
fn machine_model_init_actions() {
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
    mb.add_action(Action::new("let initialized = true"));
    let model = mb.build();

    let entry = make_context(
        model,
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::random(StopCondition::Length(1)),
    );
    let machine = Machine::new_with_seed(vec![entry], 42).unwrap();

    let val = machine
        .current_context()
        .get_attribute("initialized")
        .unwrap();
    assert_eq!(val.as_bool().unwrap(), true);
}

#[test]
fn machine_guards_filter_edges() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");
    let v_c = VertexBuilder::new().id("vc").name("C");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone())
            .guard(Guard::new("x > 100")),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ac")
            .source_vertex(v_a.clone())
            .target_vertex(v_c.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ca")
            .source_vertex(v_c.clone())
            .target_vertex(v_a.clone()),
    );
    mb.add_action(Action::new("let x = 0"));
    let model = mb.build();

    let entry = make_context(
        model,
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::random(StopCondition::Length(20)),
    );
    let mut machine = Machine::new_with_seed(vec![entry], 42).unwrap();

    while machine.has_next_step() {
        machine.get_next_step().unwrap();
    }

    assert!(!machine
        .current_context()
        .is_visited(ElementIndex::Edge(EdgeIndex(0))));
    assert!(machine
        .current_context()
        .is_visited(ElementIndex::Edge(EdgeIndex(1))));
}

// ---------------------------------------------------------------------------
// Machine — requirements
// ---------------------------------------------------------------------------

#[test]
fn machine_tracks_element_requirements() {
    let v_a = VertexBuilder::new()
        .id("va")
        .name("A")
        .add_requirement(Requirement::new("REQ-001"));
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
    let model = mb.build();

    let entry = make_context(
        model,
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::random(StopCondition::Length(1)),
    );
    let mut machine = Machine::new_with_seed(vec![entry], 42).unwrap();

    machine.get_next_step().unwrap();

    assert_eq!(
        machine
            .current_context()
            .requirements_with_status(RequirementStatus::Passed),
        1
    );
}

// ---------------------------------------------------------------------------
// Machine — observer
// ---------------------------------------------------------------------------

struct EventLog {
    events: Rc<RefCell<Vec<(usize, ElementIndex, EventType)>>>,
}

impl MachineObserver for EventLog {
    fn update(&mut self, context_index: usize, element: ElementIndex, event: EventType) {
        self.events
            .borrow_mut()
            .push((context_index, element, event));
    }
}

#[test]
fn machine_notifies_observers() {
    let entry = make_context(
        simple_model(),
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::random(StopCondition::Length(3)),
    );
    let mut machine = Machine::new_with_seed(vec![entry], 42).unwrap();

    let events = Rc::new(RefCell::new(Vec::new()));
    machine.add_observer(Box::new(EventLog {
        events: Rc::clone(&events),
    }));

    while machine.has_next_step() {
        machine.get_next_step().unwrap();
    }

    let log = events.borrow();
    assert!(!log.is_empty());
    for (i, entry) in log.iter().enumerate() {
        if i % 2 == 0 {
            assert_eq!(entry.2, EventType::BeforeElement);
        } else {
            assert_eq!(entry.2, EventType::AfterElement);
        }
    }
}

// ---------------------------------------------------------------------------
// Machine — InternalState with actions
// ---------------------------------------------------------------------------

#[test]
fn machine_internal_state_stop_condition() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone())
            .add_action(Action::new("count = count + 1")),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    mb.add_action(Action::new("let count = 0"));
    let model = mb.build();

    let entry = make_context(
        model,
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::random(StopCondition::InternalState("count >= 3".to_string())),
    );
    let mut machine = Machine::new_with_seed(vec![entry], 42).unwrap();

    let mut steps = 0;
    while machine.has_next_step() && steps < 200 {
        machine.get_next_step().unwrap();
        steps += 1;
    }

    assert!(!machine.has_next_step());
    let count = machine.current_context().get_attribute("count").unwrap();
    assert!(count.as_int().unwrap() >= 3);
}

// ---------------------------------------------------------------------------
// Machine — shared global scope
// ---------------------------------------------------------------------------

#[test]
fn machine_shares_global_scope_across_contexts() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb1 = ModelBuilder::new();
    mb1.add_edge(
        EdgeBuilder::new()
            .id("e1_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb1.add_edge(
        EdgeBuilder::new()
            .id("e1_ba")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    mb1.add_action(Action::new("let global.shared_var = 42"));
    let model1 = mb1.build();

    let v_c = VertexBuilder::new().id("vc").name("C");
    let v_d = VertexBuilder::new().id("vd").name("D");

    let mut mb2 = ModelBuilder::new();
    mb2.add_edge(
        EdgeBuilder::new()
            .id("e2_cd")
            .source_vertex(v_c.clone())
            .target_vertex(v_d.clone()),
    );
    mb2.add_edge(
        EdgeBuilder::new()
            .id("e2_dc")
            .source_vertex(v_d.clone())
            .target_vertex(v_c.clone()),
    );
    let model2 = mb2.build();

    let entry1 = make_context(
        model1,
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::random(StopCondition::Length(4)),
    );
    let entry2 = make_context(
        model2,
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::random(StopCondition::Length(4)),
    );
    let machine = Machine::new_with_seed(vec![entry1, entry2], 42).unwrap();

    let val_ctx0 = machine
        .context(0)
        .get_attribute("global.shared_var")
        .unwrap();
    let val_ctx1 = machine
        .context(1)
        .get_attribute("global.shared_var")
        .unwrap();
    assert_eq!(val_ctx0.as_int().unwrap(), 42);
    assert_eq!(val_ctx1.as_int().unwrap(), 42);
}

// ---------------------------------------------------------------------------
// Machine — multi-model with shared state
// ---------------------------------------------------------------------------

fn two_model_shared_state() -> (RuntimeModel, RuntimeModel) {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new()
        .id("vb")
        .name("B")
        .shared_state("SHARED");

    let mut mb1 = ModelBuilder::new();
    mb1.add_edge(
        EdgeBuilder::new()
            .id("e1_ab")
            .name("e1_AB")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb1.add_edge(
        EdgeBuilder::new()
            .id("e1_ba")
            .name("e1_BA")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    let model1 = mb1.build();

    let v_c = VertexBuilder::new()
        .id("vc")
        .name("C")
        .shared_state("SHARED");
    let v_d = VertexBuilder::new().id("vd").name("D");

    let mut mb2 = ModelBuilder::new();
    mb2.add_edge(
        EdgeBuilder::new()
            .id("e2_cd")
            .name("e2_CD")
            .source_vertex(v_c.clone())
            .target_vertex(v_d.clone()),
    );
    mb2.add_edge(
        EdgeBuilder::new()
            .id("e2_dc")
            .name("e2_DC")
            .source_vertex(v_d.clone())
            .target_vertex(v_c.clone()),
    );
    let model2 = mb2.build();

    (model1, model2)
}

#[test]
fn machine_shared_state_switches_context() {
    let (model1, model2) = two_model_shared_state();

    let mut ctx1 = ExecutionContext::new_with_seed(model1, 42);
    ctx1.set_next_element(Some(ElementIndex::Vertex(VertexIndex(0))));
    let gen1 = PathGenerator::random(StopCondition::EdgeCoverage(100));

    let mut ctx2 = ExecutionContext::new_with_seed(model2, 42);
    ctx2.set_next_element(Some(ElementIndex::Vertex(VertexIndex(0))));
    let gen2 = PathGenerator::random(StopCondition::EdgeCoverage(100));

    let mut machine = Machine::new_with_seed(vec![(ctx1, gen1), (ctx2, gen2)], 99).unwrap();

    let mut visited_contexts = std::collections::HashSet::new();
    let mut steps = 0;

    while machine.has_next_step() && steps < 500 {
        machine.get_next_step().unwrap();
        visited_contexts.insert(machine.current_context_index());
        steps += 1;
    }

    assert!(
        visited_contexts.len() >= 2,
        "Expected both contexts to be visited, got {:?}",
        visited_contexts
    );
}

#[test]
fn machine_shared_state_covers_both_models() {
    let (model1, model2) = two_model_shared_state();

    let mut ctx1 = ExecutionContext::new_with_seed(model1, 42);
    ctx1.set_next_element(Some(ElementIndex::Vertex(VertexIndex(0))));
    let gen1 = PathGenerator::random(StopCondition::EdgeCoverage(100));

    let mut ctx2 = ExecutionContext::new_with_seed(model2, 42);
    ctx2.set_next_element(Some(ElementIndex::Vertex(VertexIndex(0))));
    let gen2 = PathGenerator::random(StopCondition::EdgeCoverage(100));

    let mut machine = Machine::new_with_seed(vec![(ctx1, gen1), (ctx2, gen2)], 99).unwrap();

    let mut steps = 0;
    while machine.has_next_step() && steps < 500 {
        machine.get_next_step().unwrap();
        steps += 1;
    }

    assert!(
        machine
            .context(0)
            .is_visited(ElementIndex::Edge(EdgeIndex(0))),
        "Model 1 edge e1_AB should be visited"
    );
    assert!(
        machine
            .context(1)
            .is_visited(ElementIndex::Edge(EdgeIndex(0))),
        "Model 2 edge e2_CD should be visited"
    );
}

// ---------------------------------------------------------------------------
// Machine — NewYorkStreetSweeper skips actions and ignores guards
// ---------------------------------------------------------------------------

#[test]
fn machine_nyss_skips_actions() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone())
            .add_action(Action::new("counter = counter + 1")),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    mb.add_action(Action::new("let counter = 0"));
    let model = mb.build();

    let entry = make_context(
        model,
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::new_york_street_sweeper(),
    );
    let mut machine = Machine::new_with_seed(vec![entry], 42).unwrap();

    while machine.has_next_step() {
        machine.get_next_step().unwrap();
    }

    let counter = machine
        .current_context()
        .get_attribute("counter")
        .unwrap();
    assert_eq!(
        counter.as_int().unwrap(),
        0,
        "Actions should be skipped for NewYorkStreetSweeper"
    );
}

#[test]
fn machine_nyss_ignores_guards() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone())
            .guard(Guard::new("false")),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    let model = mb.build();

    let entry = make_context(
        model,
        ElementIndex::Vertex(VertexIndex(0)),
        PathGenerator::new_york_street_sweeper(),
    );
    let mut machine = Machine::new_with_seed(vec![entry], 42).unwrap();

    while machine.has_next_step() {
        machine.get_next_step().unwrap();
    }

    assert!(
        machine
            .current_context()
            .is_visited(ElementIndex::Edge(EdgeIndex(0))),
        "Guarded edge should be visited — guards are ignored by NYSS"
    );
}

// ---------------------------------------------------------------------------
// Machine — MachineError Display
// ---------------------------------------------------------------------------

#[test]
fn machine_error_display() {
    assert_eq!(MachineError::NoContexts.to_string(), "No contexts provided");
    assert_eq!(
        MachineError::NoStartContext.to_string(),
        "No start context found"
    );
    assert_eq!(
        MachineError::NoCurrentElement.to_string(),
        "No current element"
    );
}

// ---------------------------------------------------------------------------
// prepare_script — JavaScript-to-Rhai bridging
// ---------------------------------------------------------------------------

#[test]
fn prepare_script_auto_let_for_new_variable() {
    let scope = rhai::Scope::new();
    let result = ExecutionContext::prepare_script("x=42", &scope);
    assert_eq!(result, "let x=42");
}

#[test]
fn prepare_script_no_let_for_existing_variable() {
    let mut scope = rhai::Scope::new();
    scope.push("x", 0_i64);
    let result = ExecutionContext::prepare_script("x=42", &scope);
    assert_eq!(result, "x=42");
}

#[test]
fn prepare_script_multiple_statements() {
    let scope = rhai::Scope::new();
    let result = ExecutionContext::prepare_script("a=1;b=true;", &scope);
    assert_eq!(result, "let a=1;let b=true");
}

#[test]
fn prepare_script_preserves_comparison_operators() {
    let scope = rhai::Scope::new();
    assert_eq!(ExecutionContext::prepare_script("x==1", &scope), "x==1");
    assert_eq!(ExecutionContext::prepare_script("x!=1", &scope), "x!=1");
    assert_eq!(ExecutionContext::prepare_script("x<=1", &scope), "x<=1");
    assert_eq!(ExecutionContext::prepare_script("x>=1", &scope), "x>=1");
}

#[test]
fn prepare_script_preserves_compound_assignment() {
    let mut scope = rhai::Scope::new();
    scope.push("x", 0_i64);
    assert_eq!(ExecutionContext::prepare_script("x+=1", &scope), "x+=1");
    assert_eq!(ExecutionContext::prepare_script("x-=1", &scope), "x-=1");
}

#[test]
fn prepare_script_increment() {
    let scope = rhai::Scope::new();
    assert_eq!(
        ExecutionContext::prepare_script("numOfPets++", &scope),
        "numOfPets += 1"
    );
}

#[test]
fn prepare_script_decrement() {
    let scope = rhai::Scope::new();
    assert_eq!(
        ExecutionContext::prepare_script("count--", &scope),
        "count -= 1"
    );
}

#[test]
fn prepare_script_increment_in_multi_statement() {
    let scope = rhai::Scope::new();
    let result = ExecutionContext::prepare_script("x=0;x++;y=true;", &scope);
    assert_eq!(result, "let x=0;x += 1;let y=true");
}

#[test]
fn prepare_script_skips_empty_statements() {
    let scope = rhai::Scope::new();
    assert_eq!(
        ExecutionContext::prepare_script(";;x=1;;", &scope),
        "let x=1"
    );
}

#[test]
fn prepare_script_existing_let_not_doubled() {
    let scope = rhai::Scope::new();
    assert_eq!(
        ExecutionContext::prepare_script("let x=1", &scope),
        "let x=1"
    );
}

#[test]
fn prepare_script_increment_roundtrip() {
    let model = simple_model();
    let ctx = ExecutionContext::new(model);
    ctx.execute_action(&Action::new("let count = 0")).unwrap();
    ctx.execute_action(&Action::new("count++")).unwrap();
    ctx.execute_action(&Action::new("count++")).unwrap();
    let val = ctx.get_attribute("count").unwrap();
    assert_eq!(val.as_int().unwrap(), 2);
}

#[test]
fn prepare_script_decrement_roundtrip() {
    let model = simple_model();
    let ctx = ExecutionContext::new(model);
    ctx.execute_action(&Action::new("let count = 5")).unwrap();
    ctx.execute_action(&Action::new("count--")).unwrap();
    let val = ctx.get_attribute("count").unwrap();
    assert_eq!(val.as_int().unwrap(), 4);
}

#[test]
fn prepare_script_auto_let_roundtrip() {
    let model = simple_model();
    let ctx = ExecutionContext::new(model);
    ctx.execute_action(&Action::new("validLogin=false;rememberMe=false;"))
        .unwrap();
    let val = ctx.get_attribute("validLogin").unwrap();
    assert_eq!(val.as_bool().unwrap(), false);
    ctx.execute_action(&Action::new("validLogin=true;"))
        .unwrap();
    let val = ctx.get_attribute("validLogin").unwrap();
    assert_eq!(val.as_bool().unwrap(), true);
}
