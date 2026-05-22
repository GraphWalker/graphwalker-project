use std::cell::Cell;
use std::time::{Duration, Instant};

use crate::machine::ExecutionContext;
use crate::model::{EdgeIndex, ElementIndex, RequirementStatus};

pub const FULFILLMENT_LEVEL: f64 = 0.999999;

#[derive(Clone, Debug)]
pub enum StopCondition {
    EdgeCoverage(u32),
    VertexCoverage(u32),
    RequirementCoverage(u32),
    DependencyEdgeCoverage(u32),
    ReachedVertex { name: String, fulfilled: Cell<bool> },
    ReachedEdge { name: String, fulfilled: Cell<bool> },
    ReachedSharedState { name: String, fulfilled: Cell<bool> },
    TimeDuration { duration: Duration, start: Instant },
    Length(u64),
    Never,
    Alternative(Vec<StopCondition>),
    Combined(Vec<StopCondition>),
    PredefinedPath,
    InternalState(String),
}

impl StopCondition {
    pub fn reached_vertex(name: impl Into<String>) -> Self {
        Self::ReachedVertex {
            name: name.into(),
            fulfilled: Cell::new(false),
        }
    }

    pub fn reached_edge(name: impl Into<String>) -> Self {
        Self::ReachedEdge {
            name: name.into(),
            fulfilled: Cell::new(false),
        }
    }

    pub fn reached_shared_state(name: impl Into<String>) -> Self {
        Self::ReachedSharedState {
            name: name.into(),
            fulfilled: Cell::new(false),
        }
    }

    pub fn time_duration(duration: Duration) -> Self {
        Self::TimeDuration {
            duration,
            start: Instant::now(),
        }
    }

    pub fn is_fulfilled(&self, ctx: &ExecutionContext) -> bool {
        match self {
            Self::EdgeCoverage(_)
            | Self::VertexCoverage(_)
            | Self::RequirementCoverage(_)
            | Self::DependencyEdgeCoverage(_)
            | Self::TimeDuration { .. }
            | Self::Length(_) => {
                self.get_fulfilment(ctx) >= FULFILLMENT_LEVEL && ctx.is_at_vertex()
            }

            Self::ReachedVertex { fulfilled, .. }
            | Self::ReachedEdge { fulfilled, .. }
            | Self::ReachedSharedState { fulfilled, .. } => {
                if fulfilled.get() {
                    return true;
                }
                let result = self.get_fulfilment(ctx) >= FULFILLMENT_LEVEL;
                if result {
                    fulfilled.set(true);
                }
                result
            }

            Self::Never => false,

            Self::Alternative(conditions) => conditions.iter().any(|c| c.is_fulfilled(ctx)),

            Self::Combined(conditions) => conditions.iter().all(|c| c.is_fulfilled(ctx)),

            Self::PredefinedPath => predefined_current_step(ctx) == predefined_total_steps(ctx),

            Self::InternalState(script) => {
                ctx.evaluate_guard(script).unwrap_or(false) && ctx.is_at_vertex()
            }
        }
    }

    pub fn get_fulfilment(&self, ctx: &ExecutionContext) -> f64 {
        match self {
            Self::EdgeCoverage(percent) => {
                let total = ctx.model().edges().len() as f64;
                if total == 0.0 {
                    return 1.0;
                }
                let visited = ctx.visited_edge_count() as f64;
                (visited / total) / (*percent as f64 / 100.0)
            }

            Self::VertexCoverage(percent) => {
                let total = ctx.model().vertices().len() as f64;
                if total == 0.0 {
                    return 1.0;
                }
                let visited = ctx.visited_vertex_count() as f64;
                (visited / total) / (*percent as f64 / 100.0)
            }

            Self::RequirementCoverage(percent) => {
                let total = ctx.requirement_count() as f64;
                if total == 0.0 {
                    return 1.0;
                }
                let passed = ctx.requirements_with_status(RequirementStatus::Passed) as f64;
                let failed = ctx.requirements_with_status(RequirementStatus::Failed) as f64;
                ((passed + failed) / total) / (*percent as f64 / 100.0)
            }

            Self::DependencyEdgeCoverage(dependency) => {
                let threshold = *dependency as f64 / 100.0;
                let matching: Vec<usize> = (0..ctx.model().edges().len())
                    .filter(|&i| ctx.model().edge(EdgeIndex(i)).dependency_as_f64() >= threshold)
                    .collect();
                let total = matching.len() as f64;
                if total == 0.0 {
                    return 0.0;
                }
                let visited = matching
                    .iter()
                    .filter(|&&i| ctx.is_visited(ElementIndex::Edge(EdgeIndex(i))))
                    .count() as f64;
                visited / total
            }

            Self::ReachedVertex { .. }
            | Self::ReachedEdge { .. }
            | Self::ReachedSharedState { .. } => {
                let targets = self.target_elements(ctx);
                reached_fulfilment(ctx, &targets)
            }

            Self::TimeDuration { duration, start } => {
                let elapsed = start.elapsed();
                elapsed.as_nanos() as f64 / duration.as_nanos() as f64
            }

            Self::Length(length) => ctx.total_visit_count() as f64 / *length as f64,

            Self::Never => 0.0,

            Self::Alternative(conditions) => conditions
                .iter()
                .map(|c| c.get_fulfilment(ctx))
                .fold(0.0_f64, f64::max),

            Self::Combined(conditions) => {
                if conditions.is_empty() {
                    return 0.0;
                }
                let sum: f64 = conditions.iter().map(|c| c.get_fulfilment(ctx)).sum();
                sum / conditions.len() as f64
            }

            Self::PredefinedPath => {
                let current_step = predefined_current_step(ctx);
                let total = predefined_total_steps(ctx);
                if total == 0 {
                    return 1.0;
                }
                current_step as f64 / total as f64
            }

            Self::InternalState(script) => {
                if ctx.evaluate_guard(script).unwrap_or(false) {
                    1.0
                } else {
                    0.0
                }
            }
        }
    }

    pub fn target_elements(&self, ctx: &ExecutionContext) -> Vec<ElementIndex> {
        match self {
            Self::ReachedVertex { name, .. } => ctx
                .model()
                .find_vertices(name)
                .iter()
                .map(|&vi| ElementIndex::Vertex(vi))
                .collect(),
            Self::ReachedEdge { name, .. } => ctx
                .model()
                .find_edges(name)
                .iter()
                .map(|&ei| ElementIndex::Edge(ei))
                .collect(),
            Self::ReachedSharedState { name, .. } => ctx
                .model()
                .shared_state_vertices(name)
                .iter()
                .map(|&vi| ElementIndex::Vertex(vi))
                .collect(),
            _ => vec![],
        }
    }
}

fn reached_fulfilment(ctx: &ExecutionContext, targets: &[ElementIndex]) -> f64 {
    let current = match ctx.current_element() {
        Some(elem) => elem,
        None => return 0.0,
    };

    let fw = ctx.floyd_warshall();
    let mut max_fulfilment = 0.0_f64;

    for &target in targets {
        let distance = fw.shortest_distance(current, target);
        let max_dist = fw.maximum_distance(target);
        if max_dist > 0 {
            let fulfilment = 1.0 - (distance as f64) / (max_dist as f64);
            max_fulfilment = max_fulfilment.max(fulfilment);
        } else if distance == 0 {
            max_fulfilment = 1.0;
        }
    }

    max_fulfilment
}

fn predefined_current_step(ctx: &ExecutionContext) -> usize {
    let step = ctx.predefined_path_current_edge_index() * 2;
    if ctx.is_at_vertex() {
        step + 1
    } else {
        step
    }
}

fn predefined_total_steps(ctx: &ExecutionContext) -> usize {
    ctx.model().predefined_path().len() * 2 + 1
}

impl std::fmt::Display for StopCondition {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::EdgeCoverage(p) => write!(f, "EdgeCoverage({})", p),
            Self::VertexCoverage(p) => write!(f, "VertexCoverage({})", p),
            Self::RequirementCoverage(p) => write!(f, "RequirementCoverage({})", p),
            Self::DependencyEdgeCoverage(d) => write!(f, "DependencyEdgeCoverage({})", d),
            Self::ReachedVertex { name, .. } => write!(f, "ReachedVertex({})", name),
            Self::ReachedEdge { name, .. } => write!(f, "ReachedEdge({})", name),
            Self::ReachedSharedState { name, .. } => write!(f, "ReachedSharedState({})", name),
            Self::TimeDuration { duration, .. } => {
                write!(f, "TimeDuration({}s)", duration.as_secs())
            }
            Self::Length(l) => write!(f, "Length({})", l),
            Self::Never => write!(f, "Never"),
            Self::Alternative(conditions) => {
                let strs: Vec<String> = conditions.iter().map(|c| c.to_string()).collect();
                write!(f, "({})", strs.join(" OR "))
            }
            Self::Combined(conditions) => {
                let strs: Vec<String> = conditions.iter().map(|c| c.to_string()).collect();
                write!(f, "({})", strs.join(" AND "))
            }
            Self::PredefinedPath => write!(f, "PredefinedPath"),
            Self::InternalState(script) => write!(f, "InternalState({})", script),
        }
    }
}

#[cfg(test)]
mod tests;
