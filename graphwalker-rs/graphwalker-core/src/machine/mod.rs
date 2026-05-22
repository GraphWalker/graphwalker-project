use std::cell::{OnceCell, RefCell};
use std::collections::{HashMap, HashSet};
use std::rc::Rc;

use rand::rngs::StdRng;
use rand::seq::SliceRandom;
use rand::{Rng, SeedableRng};
use rhai::{Dynamic, Engine, Scope};

use crate::algorithm::FloydWarshall;
use crate::generator::{GeneratorError, PathGenerator};
use crate::model::{
    Action, EdgeIndex, ElementIndex, ExecutionStatus, Requirement, RequirementStatus, RuntimeModel,
    VertexIndex,
};

// ---------------------------------------------------------------------------
// ScriptError
// ---------------------------------------------------------------------------

#[derive(Clone, Debug, PartialEq, Eq)]
pub struct ScriptError {
    pub message: String,
}

impl ScriptError {
    pub fn new(message: impl Into<String>) -> Self {
        Self {
            message: message.into(),
        }
    }
}

impl std::fmt::Display for ScriptError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "Script error: {}", self.message)
    }
}

impl std::error::Error for ScriptError {}

impl From<rhai::EvalAltResult> for ScriptError {
    fn from(e: rhai::EvalAltResult) -> Self {
        Self::new(e.to_string())
    }
}

impl From<Box<rhai::EvalAltResult>> for ScriptError {
    fn from(e: Box<rhai::EvalAltResult>) -> Self {
        Self::new(e.to_string())
    }
}

// ---------------------------------------------------------------------------
// ExecutionContext
// ---------------------------------------------------------------------------

pub struct ExecutionContext {
    model: RuntimeModel,
    current_element: Option<ElementIndex>,
    last_element: Option<ElementIndex>,
    next_element: Option<ElementIndex>,
    execution_status: ExecutionStatus,

    visited: HashSet<ElementIndex>,
    visit_counts: HashMap<ElementIndex, u64>,
    total_visit_count: u64,
    visited_vertex_count: usize,
    visited_edge_count: usize,

    requirement_status: HashMap<Requirement, RequirementStatus>,

    predefined_path_current_edge_index: usize,

    floyd_warshall: OnceCell<FloydWarshall>,

    rng: StdRng,

    engine: Engine,
    local_scope: RefCell<Scope<'static>>,
    global_scope: Rc<RefCell<Scope<'static>>>,
}

impl ExecutionContext {
    pub fn new(model: RuntimeModel) -> Self {
        Self {
            model,
            current_element: None,
            last_element: None,
            next_element: None,
            execution_status: ExecutionStatus::NotExecuted,
            visited: HashSet::new(),
            visit_counts: HashMap::new(),
            total_visit_count: 0,
            visited_vertex_count: 0,
            visited_edge_count: 0,
            requirement_status: HashMap::new(),
            predefined_path_current_edge_index: 0,
            floyd_warshall: OnceCell::new(),
            rng: StdRng::from_entropy(),
            engine: Self::create_engine(),
            local_scope: RefCell::new(Scope::new()),
            global_scope: Rc::new(RefCell::new(Scope::new())),
        }
    }

    pub fn new_with_seed(model: RuntimeModel, seed: u64) -> Self {
        let mut ctx = Self::new(model);
        ctx.rng = StdRng::seed_from_u64(seed);
        ctx
    }

    fn create_engine() -> Engine {
        let mut engine = Engine::new();
        engine.set_max_expr_depths(64, 32);
        engine
    }

    // -- Model --

    pub fn model(&self) -> &RuntimeModel {
        &self.model
    }

    // -- Element access --

    pub fn current_element(&self) -> Option<ElementIndex> {
        self.current_element
    }

    pub fn last_element(&self) -> Option<ElementIndex> {
        self.last_element
    }

    pub fn next_element(&self) -> Option<ElementIndex> {
        self.next_element
    }

    pub fn set_next_element(&mut self, element: Option<ElementIndex>) {
        self.next_element = element;
    }

    pub fn set_current_element(&mut self, element: ElementIndex) {
        self.last_element = self.current_element;
        self.current_element = Some(element);
        if self.visited.insert(element) {
            match element {
                ElementIndex::Vertex(_) => self.visited_vertex_count += 1,
                ElementIndex::Edge(_) => self.visited_edge_count += 1,
            }
        }
        *self.visit_counts.entry(element).or_insert(0) += 1;
        self.total_visit_count += 1;
    }

    pub fn is_at_vertex(&self) -> bool {
        matches!(self.current_element, Some(ElementIndex::Vertex(_)))
    }

    // -- Visit tracking --

    pub fn is_visited(&self, element: ElementIndex) -> bool {
        self.visited.contains(&element)
    }

    pub fn visit_count(&self, element: ElementIndex) -> u64 {
        self.visit_counts.get(&element).copied().unwrap_or(0)
    }

    pub fn total_visit_count(&self) -> u64 {
        self.total_visit_count
    }

    pub fn visited_vertex_count(&self) -> usize {
        self.visited_vertex_count
    }

    pub fn visited_edge_count(&self) -> usize {
        self.visited_edge_count
    }

    // -- Execution status --

    pub fn execution_status(&self) -> ExecutionStatus {
        self.execution_status
    }

    pub fn set_execution_status(&mut self, status: ExecutionStatus) {
        self.execution_status = status;
    }

    // -- Requirement tracking --

    pub fn set_requirement_status(&mut self, req: Requirement, status: RequirementStatus) {
        self.requirement_status.insert(req, status);
    }

    pub fn requirement_count(&self) -> usize {
        self.requirement_status.len()
    }

    pub fn requirements_with_status(&self, status: RequirementStatus) -> usize {
        self.requirement_status
            .values()
            .filter(|&&s| s == status)
            .count()
    }

    // -- Predefined path --

    pub fn predefined_path_current_edge_index(&self) -> usize {
        self.predefined_path_current_edge_index
    }

    pub fn set_predefined_path_current_edge_index(&mut self, index: usize) {
        self.predefined_path_current_edge_index = index;
    }

    // -- Element filtering --

    pub fn filter_elements(&self, elements: &[ElementIndex]) -> Vec<ElementIndex> {
        elements
            .iter()
            .filter(|&&elem| match elem {
                ElementIndex::Edge(ei) => self.is_edge_available(ei),
                ElementIndex::Vertex(_) => true,
            })
            .copied()
            .collect()
    }

    // -- Script engine --

    fn is_global_script(script: &str) -> bool {
        script.contains("global.")
    }

    fn strip_global_prefix(script: &str) -> String {
        script.replace("global.", "")
    }

    pub fn execute_action(&self, action: &Action) -> Result<(), ScriptError> {
        let script = action.script();
        if script.is_empty() {
            return Ok(());
        }

        if Self::is_global_script(script) {
            let stripped = Self::strip_global_prefix(script);
            let mut scope = self.global_scope.borrow_mut();
            let prepared = Self::prepare_script(&stripped, &scope);
            let _ = self
                .engine
                .eval_with_scope::<Dynamic>(&mut scope, &prepared)?;
        } else {
            let mut scope = self.local_scope.borrow_mut();
            let prepared = Self::prepare_script(script, &scope);
            let _ = self
                .engine
                .eval_with_scope::<Dynamic>(&mut scope, &prepared)?;
        }
        Ok(())
    }

    // Bridges JavaScript action syntax to Rhai:
    // - `x=val` → `let x=val` when x is not yet in scope
    // - `x++` → `x += 1`
    // - `x--` → `x -= 1`
    fn prepare_script(script: &str, scope: &Scope) -> String {
        let mut result = String::with_capacity(script.len() + 32);
        for stmt in script.split(';') {
            let stmt = stmt.trim();
            if stmt.is_empty() {
                continue;
            }
            if !result.is_empty() {
                result.push(';');
            }
            if let Some(var) = stmt.strip_suffix("++") {
                let var = var.trim();
                result.push_str(var);
                result.push_str(" += 1");
            } else if let Some(var) = stmt.strip_suffix("--") {
                let var = var.trim();
                result.push_str(var);
                result.push_str(" -= 1");
            } else {
                if let Some((lhs, rhs)) = stmt.split_once('=') {
                    let lhs = lhs.trim();
                    if !lhs.is_empty()
                        && !rhs.starts_with('=')
                        && !lhs.ends_with('!')
                        && !lhs.ends_with('<')
                        && !lhs.ends_with('>')
                        && !lhs.ends_with('+')
                        && !lhs.ends_with('-')
                        && lhs.chars().all(|c| c.is_ascii_alphanumeric() || c == '_')
                        && !scope.contains(lhs)
                        && !stmt.starts_with("let ")
                    {
                        result.push_str("let ");
                    }
                }
                result.push_str(stmt);
            }
        }
        result
    }

    pub fn execute_actions(&self, actions: &[Action]) -> Result<(), ScriptError> {
        for action in actions {
            self.execute_action(action)?;
        }
        Ok(())
    }

    pub fn execute_element_actions(&self, element: ElementIndex) -> Result<(), ScriptError> {
        match element {
            ElementIndex::Vertex(vi) => self.execute_actions(self.model.vertex(vi).actions()),
            ElementIndex::Edge(ei) => self.execute_actions(self.model.edge(ei).actions()),
        }
    }

    pub fn execute_model_actions(&self) -> Result<(), ScriptError> {
        self.execute_actions(self.model.actions())
    }

    pub fn evaluate_guard(&self, script: &str) -> Result<bool, ScriptError> {
        if script.is_empty() {
            return Ok(true);
        }

        if Self::is_global_script(script) {
            let stripped = Self::strip_global_prefix(script);
            let mut scope = self.global_scope.borrow_mut();
            let result: Dynamic = self.engine.eval_with_scope(&mut scope, &stripped)?;
            result.as_bool().map_err(|_| {
                ScriptError::new(format!("Guard did not evaluate to boolean: {}", script))
            })
        } else {
            let mut scope = self.local_scope.borrow_mut();
            let result: Dynamic = self.engine.eval_with_scope(&mut scope, script)?;
            result.as_bool().map_err(|_| {
                ScriptError::new(format!("Guard did not evaluate to boolean: {}", script))
            })
        }
    }

    pub fn is_edge_available(&self, edge_idx: EdgeIndex) -> bool {
        let edge = self.model.edge(edge_idx);
        match edge.guard() {
            Some(guard) if guard.has_script() => {
                self.evaluate_guard(guard.script()).unwrap_or(false)
            }
            _ => true,
        }
    }

    pub fn get_attribute(&self, name: &str) -> Option<Dynamic> {
        if Self::is_global_script(name) {
            let stripped = Self::strip_global_prefix(name);
            let scope = self.global_scope.borrow();
            scope.get_value::<Dynamic>(&stripped)
        } else {
            let scope = self.local_scope.borrow();
            scope.get_value::<Dynamic>(name)
        }
    }

    pub fn set_attribute(&self, name: &str, value: impl Into<Dynamic>) {
        let mut scope = self.local_scope.borrow_mut();
        let val = value.into();
        if scope.contains(name) {
            scope.set_value(name, val);
        } else {
            scope.push_dynamic(name, val);
        }
    }

    pub fn set_global_attribute(&self, name: &str, value: impl Into<Dynamic>) {
        let mut scope = self.global_scope.borrow_mut();
        let val = value.into();
        if scope.contains(name) {
            scope.set_value(name, val);
        } else {
            scope.push_dynamic(name, val);
        }
    }

    pub fn data(&self) -> String {
        let local = self.local_scope.borrow();
        let global = self.global_scope.borrow();
        let mut parts = Vec::new();

        for (name, _, value) in local.iter() {
            parts.push(format!("{}={}", name, value));
        }
        for (name, _, value) in global.iter() {
            parts.push(format!("global.{}={}", name, value));
        }

        parts.join(";")
    }

    pub fn global_scope(&self) -> &RefCell<Scope<'static>> {
        &self.global_scope
    }

    pub fn set_global_scope(&self, scope: Scope<'static>) {
        *self.global_scope.borrow_mut() = scope;
    }

    pub fn set_shared_global_scope(&mut self, scope: Rc<RefCell<Scope<'static>>>) {
        self.global_scope = scope;
    }

    // -- Element lifecycle --

    pub fn clear_current_element(&mut self) {
        self.current_element = None;
    }

    // -- Algorithm cache --

    pub fn floyd_warshall(&self) -> &FloydWarshall {
        self.floyd_warshall
            .get_or_init(|| FloydWarshall::new(&self.model))
    }

    // -- RNG --

    pub fn gen_usize(&mut self, bound: usize) -> usize {
        self.rng.gen_range(0..bound)
    }

    pub fn gen_range_int(&mut self, range: std::ops::Range<i32>) -> i32 {
        self.rng.gen_range(range)
    }

    pub fn shuffle<T>(&mut self, slice: &mut [T]) {
        slice.shuffle(&mut self.rng);
    }
}

// ---------------------------------------------------------------------------
// EventType
// ---------------------------------------------------------------------------

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum EventType {
    BeforeElement,
    AfterElement,
}

// ---------------------------------------------------------------------------
// MachineObserver
// ---------------------------------------------------------------------------

pub trait MachineObserver {
    fn update(&mut self, context_index: usize, element: ElementIndex, event: EventType);
}

// ---------------------------------------------------------------------------
// MachineError
// ---------------------------------------------------------------------------

#[derive(Clone, Debug, PartialEq, Eq)]
pub enum MachineError {
    NoContexts,
    NoStartContext,
    NoCurrentElement,
    Generator(String),
    Script(ScriptError),
}

impl std::fmt::Display for MachineError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::NoContexts => write!(f, "No contexts provided"),
            Self::NoStartContext => write!(f, "No start context found"),
            Self::NoCurrentElement => write!(f, "No current element"),
            Self::Generator(msg) => write!(f, "Generator error: {}", msg),
            Self::Script(e) => write!(f, "{}", e),
        }
    }
}

impl std::error::Error for MachineError {}

impl From<GeneratorError> for MachineError {
    fn from(e: GeneratorError) -> Self {
        Self::Generator(e.to_string())
    }
}

impl From<ScriptError> for MachineError {
    fn from(e: ScriptError) -> Self {
        Self::Script(e)
    }
}

// ---------------------------------------------------------------------------
// Machine
// ---------------------------------------------------------------------------

pub struct Machine {
    contexts: Vec<ExecutionContext>,
    generators: Vec<PathGenerator>,
    current_context_idx: usize,
    observers: Vec<Box<dyn MachineObserver>>,
    execution_path: Vec<(usize, ElementIndex)>,
    record_path: bool,
    last_portal_source: Option<(usize, ElementIndex)>,
    rng: StdRng,
}

impl Machine {
    pub fn new(entries: Vec<(ExecutionContext, PathGenerator)>) -> Result<Self, MachineError> {
        Self::new_internal(entries, StdRng::from_entropy())
    }

    pub fn new_with_seed(
        entries: Vec<(ExecutionContext, PathGenerator)>,
        seed: u64,
    ) -> Result<Self, MachineError> {
        Self::new_internal(entries, StdRng::seed_from_u64(seed))
    }

    fn new_internal(
        entries: Vec<(ExecutionContext, PathGenerator)>,
        rng: StdRng,
    ) -> Result<Self, MachineError> {
        if entries.is_empty() {
            return Err(MachineError::NoContexts);
        }

        let shared_global = Rc::new(RefCell::new(Scope::new()));
        let mut contexts = Vec::with_capacity(entries.len());
        let mut generators = Vec::with_capacity(entries.len());

        for (mut ctx, gen) in entries {
            ctx.set_shared_global_scope(Rc::clone(&shared_global));
            contexts.push(ctx);
            generators.push(gen);
        }

        for ctx in &contexts {
            ctx.execute_model_actions().map_err(MachineError::Script)?;
        }

        let start_idx = contexts
            .iter()
            .position(|ctx| ctx.next_element().is_some())
            .ok_or(MachineError::NoStartContext)?;

        Ok(Self {
            contexts,
            generators,
            current_context_idx: start_idx,
            observers: Vec::new(),
            execution_path: Vec::new(),
            record_path: true,
            last_portal_source: None,
            rng,
        })
    }

    // -- Accessors --

    pub fn current_context_index(&self) -> usize {
        self.current_context_idx
    }

    pub fn current_context(&self) -> &ExecutionContext {
        &self.contexts[self.current_context_idx]
    }

    pub fn context(&self, index: usize) -> &ExecutionContext {
        &self.contexts[index]
    }

    pub fn context_mut(&mut self, index: usize) -> &mut ExecutionContext {
        &mut self.contexts[index]
    }

    pub fn context_count(&self) -> usize {
        self.contexts.len()
    }

    pub fn execution_path(&self) -> &[(usize, ElementIndex)] {
        &self.execution_path
    }

    pub fn set_record_path(&mut self, record: bool) {
        self.record_path = record;
    }

    pub fn get_fulfilment(&self, index: usize) -> f64 {
        self.generators[index]
            .stop_condition
            .get_fulfilment(&self.contexts[index])
    }

    pub fn add_observer(&mut self, observer: Box<dyn MachineObserver>) {
        self.observers.push(observer);
    }

    // -- Execution --

    pub fn has_next_step(&mut self) -> bool {
        for i in 0..self.contexts.len() {
            if self.has_next_step_for_context(i) {
                if i != self.current_context_idx && self.contexts[i].next_element().is_some() {
                    self.current_context_idx = i;
                }
                return true;
            }
        }
        false
    }

    fn has_next_step_for_context(&mut self, i: usize) -> bool {
        let status = self.contexts[i].execution_status();
        if matches!(status, ExecutionStatus::Completed | ExecutionStatus::Failed) {
            return false;
        }

        if self.generators[i].has_next_step(&self.contexts[i]) {
            return true;
        }

        self.contexts[i].set_execution_status(ExecutionStatus::Completed);
        self.update_model_requirements(i);
        false
    }

    pub fn get_next_step(&mut self) -> Result<(), MachineError> {
        self.walk()?;

        let ctx_idx = self.current_context_idx;
        let element = self.contexts[ctx_idx]
            .current_element()
            .ok_or(MachineError::NoCurrentElement)?;

        self.notify_observers(ctx_idx, element, EventType::BeforeElement);

        if !self.generators[ctx_idx].skip_actions() {
            self.contexts[ctx_idx]
                .execute_element_actions(element)
                .map_err(MachineError::Script)?;
        }

        self.update_element_requirements(ctx_idx, element);
        if self.record_path {
            self.execution_path.push((ctx_idx, element));
        }

        self.notify_observers(ctx_idx, element, EventType::AfterElement);

        if self.contexts[ctx_idx].execution_status() == ExecutionStatus::NotExecuted {
            self.contexts[ctx_idx].set_execution_status(ExecutionStatus::Executing);
        }

        Ok(())
    }

    // -- Walk --

    fn walk(&mut self) -> Result<(), MachineError> {
        let ctx_idx = self.current_context_idx;

        if self.contexts[ctx_idx].current_element().is_none() {
            self.take_first_step()
        } else {
            self.take_next_step()
        }
    }

    fn take_first_step(&mut self) -> Result<(), MachineError> {
        let ctx_idx = self.current_context_idx;
        if let Some(next) = self.contexts[ctx_idx].next_element() {
            self.contexts[ctx_idx].set_current_element(next);
            self.contexts[ctx_idx].set_next_element(None);
            Ok(())
        } else {
            Err(MachineError::NoStartContext)
        }
    }

    fn take_next_step(&mut self) -> Result<(), MachineError> {
        let ctx_idx = self.current_context_idx;

        if let Some(ElementIndex::Vertex(vi)) = self.contexts[ctx_idx].current_element() {
            let has_shared = self.contexts[ctx_idx].model().vertex(vi).has_shared_state();
            if has_shared && self.contexts.len() > 1 {
                let shared_name = self.contexts[ctx_idx]
                    .model()
                    .vertex(vi)
                    .shared_state()
                    .unwrap()
                    .to_string();
                self.try_shared_state_portal(ctx_idx, &shared_name);
            }
        }

        self.advance_current_context()
    }

    fn advance_current_context(&mut self) -> Result<(), MachineError> {
        let ctx_idx = self.current_context_idx;

        if let Some(next) = self.contexts[ctx_idx].next_element() {
            self.contexts[ctx_idx].set_current_element(next);
            self.contexts[ctx_idx].set_next_element(None);
            Ok(())
        } else {
            self.generators[ctx_idx]
                .get_next_step(&mut self.contexts[ctx_idx])
                .map_err(|e| {
                    let ctx = &self.contexts[ctx_idx];
                    let model = ctx.model();
                    let model_name = model.name().unwrap_or(model.id());
                    let detail = match e {
                        GeneratorError::NoPathFound { from: Some(elem), to: Some(target) } => {
                            let from_desc = self.describe_element(ctx_idx, elem);
                            let to_desc = self.describe_element(ctx_idx, target);
                            format!(
                                "No path found from {} to {} in model '{}'",
                                from_desc, to_desc, model_name
                            )
                        }
                        GeneratorError::NoPathFound { from: Some(elem), to: None } => {
                            let elem_desc = self.describe_element(ctx_idx, elem);
                            format!(
                                "No path found from {} in model '{}'",
                                elem_desc, model_name
                            )
                        }
                        GeneratorError::NoPathFound { .. } => {
                            format!("No path found in model '{}'", model_name)
                        }
                        GeneratorError::NoCurrentElement => {
                            format!("No current element set in model '{}'", model_name)
                        }
                        other => {
                            format!("{} in model '{}'", other, model_name)
                        }
                    };
                    MachineError::Generator(detail)
                })
        }
    }

    fn describe_element(&self, ctx_idx: usize, elem: ElementIndex) -> String {
        let model = self.contexts[ctx_idx].model();
        match elem {
            ElementIndex::Vertex(vi) => {
                let v = model.vertex(vi);
                match v.name() {
                    Some(n) if !n.is_empty() => format!("vertex '{}'", n),
                    _ => format!("vertex (id: {})", v.id()),
                }
            }
            ElementIndex::Edge(ei) => {
                let e = model.edge(ei);
                match e.name() {
                    Some(n) if !n.is_empty() => format!("edge '{}'", n),
                    _ => format!("edge (id: {})", e.id()),
                }
            }
        }
    }

    // -- Shared state portal --

    fn try_shared_state_portal(&mut self, current_idx: usize, shared_name: &str) {
        let candidates = self.get_shared_state_candidates(current_idx, shared_name);
        if candidates.is_empty() {
            return;
        }

        let pick = self.rng.gen_range(0..candidates.len());
        let (target_ctx_idx, target_vertex_idx) = candidates[pick];

        if target_ctx_idx != current_idx {
            let current_element = self.contexts[current_idx].current_element();
            self.last_portal_source = current_element.map(|e| (current_idx, e));

            self.contexts[current_idx].clear_current_element();
            self.contexts[target_ctx_idx]
                .set_next_element(Some(ElementIndex::Vertex(target_vertex_idx)));
            self.current_context_idx = target_ctx_idx;
        } else {
            self.last_portal_source = None;
        }
    }

    fn get_shared_state_candidates(
        &self,
        current_idx: usize,
        shared_name: &str,
    ) -> Vec<(usize, VertexIndex)> {
        let mut candidates = Vec::new();
        let current_has_accessible = self.context_has_accessible_edges(current_idx);

        for (i, ctx) in self.contexts.iter().enumerate() {
            if i == current_idx {
                if let Some(ElementIndex::Vertex(vi)) = ctx.current_element() {
                    if current_has_accessible {
                        candidates.push((i, vi));
                    }
                }
            } else if ctx.model().has_shared_state(shared_name) {
                for &vi in ctx.model().shared_state_vertices(shared_name) {
                    if let Some((last_ctx, last_elem)) = self.last_portal_source {
                        if i == last_ctx
                            && ElementIndex::Vertex(vi) == last_elem
                            && current_has_accessible
                        {
                            continue;
                        }
                    }

                    let next = ctx.model().next_elements(ElementIndex::Vertex(vi));
                    let filtered = ctx.filter_elements(&next);
                    if !filtered.is_empty() || ctx.model().vertex(vi).has_name() {
                        candidates.push((i, vi));
                    }
                }
            }
        }

        candidates
    }

    fn context_has_accessible_edges(&self, ctx_idx: usize) -> bool {
        if let Some(elem) = self.contexts[ctx_idx].current_element() {
            let next = self.contexts[ctx_idx].model().next_elements(elem);
            let filtered = self.contexts[ctx_idx].filter_elements(&next);
            !filtered.is_empty()
        } else {
            false
        }
    }

    // -- Observers --

    fn notify_observers(&mut self, ctx_idx: usize, element: ElementIndex, event: EventType) {
        for observer in &mut self.observers {
            observer.update(ctx_idx, element, event);
        }
    }

    // -- Requirements --

    fn update_element_requirements(&mut self, ctx_idx: usize, element: ElementIndex) {
        let reqs: Vec<_> = match element {
            ElementIndex::Vertex(vi) => self.contexts[ctx_idx]
                .model()
                .vertex(vi)
                .requirements()
                .iter()
                .cloned()
                .collect(),
            ElementIndex::Edge(ei) => self.contexts[ctx_idx]
                .model()
                .edge(ei)
                .requirements()
                .iter()
                .cloned()
                .collect(),
        };
        for req in reqs {
            self.contexts[ctx_idx].set_requirement_status(req, RequirementStatus::Passed);
        }
    }

    fn update_model_requirements(&mut self, ctx_idx: usize) {
        let reqs: Vec<_> = self.contexts[ctx_idx]
            .model()
            .requirements()
            .iter()
            .cloned()
            .collect();
        for req in reqs {
            self.contexts[ctx_idx].set_requirement_status(req, RequirementStatus::Passed);
        }
    }
}

#[cfg(test)]
mod tests;
