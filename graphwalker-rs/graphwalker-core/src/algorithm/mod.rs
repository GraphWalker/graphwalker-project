mod chinese_postman;
pub use chinese_postman::*;

use std::collections::HashSet;

use crate::model::{EdgeIndex, ElementIndex, RuntimeModel, VertexIndex};

// ---------------------------------------------------------------------------
// Error
// ---------------------------------------------------------------------------

#[derive(Clone, Debug, PartialEq, Eq)]
pub enum AlgorithmError {
    NotEulerian,
    NoPath,
    NotStronglyConnected(String),
}

impl std::fmt::Display for AlgorithmError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::NotEulerian => write!(f, "Graph is not Eulerian or semi-Eulerian"),
            Self::NoPath => write!(f, "No path found"),
            Self::NotStronglyConnected(msg) => write!(f, "{}", msg),
        }
    }
}

impl std::error::Error for AlgorithmError {}

// ---------------------------------------------------------------------------
// FloydWarshall — all-pairs shortest paths on element graph
// ---------------------------------------------------------------------------

const INFINITY: i32 = i32::MAX / 2;

pub struct FloydWarshall {
    distances: Vec<i32>,
    n: usize,
    num_vertices: usize,
}

impl FloydWarshall {
    pub fn new(model: &RuntimeModel) -> Self {
        let num_vertices = model.vertices().len();
        let num_edges = model.edges().len();
        let n = num_vertices + num_edges;

        if n == 0 {
            return Self {
                distances: Vec::new(),
                n: 0,
                num_vertices: 0,
            };
        }

        let mut distances = vec![INFINITY; n * n];

        for i in 0..n {
            distances[i * n + i] = 0;
        }

        for vi in 0..num_vertices {
            for &ei in model.out_edges(VertexIndex(vi)) {
                distances[vi * n + (num_vertices + ei.0)] = 1;
            }
        }

        for (ei, edge) in model.edges().iter().enumerate() {
            if let Some(target) = edge.target_vertex() {
                distances[(num_vertices + ei) * n + target.0] = 1;
            }
        }

        for k in 0..n {
            for i in 0..n {
                if distances[i * n + k] >= INFINITY {
                    continue;
                }
                for j in 0..n {
                    if distances[k * n + j] >= INFINITY {
                        continue;
                    }
                    let new_dist = distances[i * n + k] + distances[k * n + j];
                    if new_dist < distances[i * n + j] {
                        distances[i * n + j] = new_dist;
                    }
                }
            }
        }

        Self {
            distances,
            n,
            num_vertices,
        }
    }

    pub fn shortest_distance(&self, from: ElementIndex, to: ElementIndex) -> i32 {
        if from == to {
            return 0;
        }
        if self.n == 0 {
            return INFINITY;
        }
        let i = self.element_to_idx(from);
        let j = self.element_to_idx(to);
        self.distances[i * self.n + j]
    }

    pub fn maximum_distance(&self, to: ElementIndex) -> i32 {
        if self.n == 0 {
            return 0;
        }
        let j = self.element_to_idx(to);
        let mut max = i32::MIN;
        for i in 0..self.n {
            let d = self.distances[i * self.n + j];
            if d < INFINITY && d > max {
                max = d;
            }
        }
        max
    }

    fn element_to_idx(&self, elem: ElementIndex) -> usize {
        match elem {
            ElementIndex::Vertex(vi) => vi.0,
            ElementIndex::Edge(ei) => self.num_vertices + ei.0,
        }
    }
}

// ---------------------------------------------------------------------------
// A* single-step: pick the neighbor of `current` closest to `target`
// ---------------------------------------------------------------------------

pub fn astar_next_element(
    fw: &FloydWarshall,
    current: ElementIndex,
    target: ElementIndex,
    neighbors: &[ElementIndex],
) -> Option<ElementIndex> {
    if current == target {
        return Some(target);
    }
    if neighbors.is_empty() {
        return None;
    }
    neighbors.iter().copied().min_by(|&a, &b| {
        let fa = fw.shortest_distance(current, a) as i64 + fw.shortest_distance(a, target) as i64;
        let fb = fw.shortest_distance(current, b) as i64 + fw.shortest_distance(b, target) as i64;
        fa.cmp(&fb)
    })
}

// ---------------------------------------------------------------------------
// Eulerian type detection
// ---------------------------------------------------------------------------

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum EulerianType {
    Eulerian,
    SemiEulerian,
    NotEulerian,
}

pub fn eulerian_type(model: &RuntimeModel) -> EulerianType {
    let mut polarity = vec![0i32; model.vertices().len()];

    for edge in model.edges() {
        if let Some(src) = edge.source_vertex() {
            polarity[src.0] -= 1;
        }
        if let Some(tgt) = edge.target_vertex() {
            polarity[tgt.0] += 1;
        }
    }

    let non_zero = polarity.iter().filter(|&&p| p != 0).count();

    match non_zero {
        0 => EulerianType::Eulerian,
        2 => EulerianType::SemiEulerian,
        _ => EulerianType::NotEulerian,
    }
}

// ---------------------------------------------------------------------------
// Euler path via Fleury's algorithm
// ---------------------------------------------------------------------------

pub fn euler_path(
    model: &RuntimeModel,
    start: ElementIndex,
) -> Result<Vec<ElementIndex>, AlgorithmError> {
    if eulerian_type(model) == EulerianType::NotEulerian {
        return Err(AlgorithmError::NotEulerian);
    }

    match start {
        ElementIndex::Edge(ei) => {
            let target = model
                .edge(ei)
                .target_vertex()
                .ok_or(AlgorithmError::NoPath)?;
            let mut visited_edges = HashSet::new();
            visited_edges.insert(ei);
            let mut trail = fleury_trail(model, target, &mut visited_edges)?;
            trail.insert(0, ElementIndex::Vertex(target));
            Ok(trail)
        }
        ElementIndex::Vertex(vi) => {
            let mut visited_edges = HashSet::new();
            fleury_trail(model, vi, &mut visited_edges)
        }
    }
}

fn fleury_trail(
    model: &RuntimeModel,
    start: VertexIndex,
    visited_edges: &mut HashSet<EdgeIndex>,
) -> Result<Vec<ElementIndex>, AlgorithmError> {
    let mut trail = Vec::new();
    let mut current = start;
    let total_edges = model.edges().len();

    while visited_edges.len() < total_edges {
        let edge = fleury_next_edge(model, visited_edges, current)?;
        trail.push(ElementIndex::Edge(edge));
        visited_edges.insert(edge);
        let target = model
            .edge(edge)
            .target_vertex()
            .ok_or(AlgorithmError::NoPath)?;
        current = target;
        trail.push(ElementIndex::Vertex(target));
    }

    Ok(trail)
}

fn fleury_next_edge(
    model: &RuntimeModel,
    visited_edges: &HashSet<EdgeIndex>,
    vertex: VertexIndex,
) -> Result<EdgeIndex, AlgorithmError> {
    let mut bridges = Vec::new();

    for &edge_idx in model.out_edges(vertex) {
        if !visited_edges.contains(&edge_idx) {
            if !is_bridge(model, visited_edges, edge_idx) {
                return Ok(edge_idx);
            } else {
                bridges.push(edge_idx);
            }
        }
    }

    bridges.into_iter().next().ok_or(AlgorithmError::NoPath)
}

fn is_bridge(
    model: &RuntimeModel,
    visited_edges: &HashSet<EdgeIndex>,
    candidate: EdgeIndex,
) -> bool {
    let source = match model.edge(candidate).source_vertex() {
        Some(v) => v,
        None => return false,
    };

    let mut excluded = visited_edges.clone();
    excluded.insert(candidate);
    let count_with_exclusion = count_reachable_vertices(model, &excluded, source);
    let count_full = count_reachable_vertices(model, &HashSet::new(), source);

    count_with_exclusion < count_full
}

fn count_reachable_vertices(
    model: &RuntimeModel,
    excluded_edges: &HashSet<EdgeIndex>,
    start: VertexIndex,
) -> usize {
    let mut visited = HashSet::new();
    let mut stack = vec![start];

    while let Some(v) = stack.pop() {
        if !visited.insert(v) {
            continue;
        }
        for &edge_idx in model.out_edges(v) {
            if !excluded_edges.contains(&edge_idx) {
                if let Some(target) = model.edge(edge_idx).target_vertex() {
                    if !visited.contains(&target) {
                        stack.push(target);
                    }
                }
            }
        }
    }

    visited.len()
}

#[cfg(test)]
mod tests;
