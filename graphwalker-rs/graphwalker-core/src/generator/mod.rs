use crate::algorithm::{self, astar_next_element, AlgorithmError};
use crate::condition::StopCondition;
use crate::machine::ExecutionContext;
use crate::model::{EdgeIndex, ElementIndex};

// ---------------------------------------------------------------------------
// Error
// ---------------------------------------------------------------------------

#[derive(Clone, Debug, PartialEq, Eq)]
pub enum GeneratorError {
    NoPathFound {
        from: Option<ElementIndex>,
        to: Option<ElementIndex>,
    },
    NoCurrentElement,
    Algorithm(AlgorithmError),
    InvalidWeight(String),
}

impl GeneratorError {
    pub fn no_path(from: Option<ElementIndex>) -> Self {
        Self::NoPathFound { from, to: None }
    }

    pub fn no_path_to(from: ElementIndex, to: ElementIndex) -> Self {
        Self::NoPathFound {
            from: Some(from),
            to: Some(to),
        }
    }
}

impl std::fmt::Display for GeneratorError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::NoPathFound {
                from: Some(elem),
                to: Some(target),
            } => write!(f, "No path found from {:?} to {:?}", elem, target),
            Self::NoPathFound {
                from: Some(elem), ..
            } => write!(f, "No path found from {:?}", elem),
            Self::NoPathFound { .. } => write!(f, "No path found"),
            Self::NoCurrentElement => write!(f, "No current element set"),
            Self::Algorithm(e) => write!(f, "Algorithm error: {}", e),
            Self::InvalidWeight(msg) => write!(f, "Invalid weight: {}", msg),
        }
    }
}

impl std::error::Error for GeneratorError {}

impl From<AlgorithmError> for GeneratorError {
    fn from(e: AlgorithmError) -> Self {
        Self::Algorithm(e)
    }
}

// ---------------------------------------------------------------------------
// PathGenerator
// ---------------------------------------------------------------------------

#[derive(Clone, Debug)]
pub struct PathGenerator {
    pub kind: GeneratorKind,
    pub stop_condition: StopCondition,
}

#[derive(Clone, Debug)]
pub enum GeneratorKind {
    Random,
    QuickRandom {
        target: Option<ElementIndex>,
        elements: Vec<ElementIndex>,
    },
    WeightedRandom,
    AStar,
    ShortestAllPaths {
        path: Option<Vec<ElementIndex>>,
    },
    Predefined,
    NewYorkStreetSweeper {
        path: Option<Vec<ElementIndex>>,
        warnings_emitted: bool,
    },
    Combined {
        generators: Vec<PathGenerator>,
        index: usize,
    },
}

impl PathGenerator {
    pub fn random(stop: StopCondition) -> Self {
        Self {
            kind: GeneratorKind::Random,
            stop_condition: stop,
        }
    }

    pub fn quick_random(stop: StopCondition) -> Self {
        Self {
            kind: GeneratorKind::QuickRandom {
                target: None,
                elements: Vec::new(),
            },
            stop_condition: stop,
        }
    }

    pub fn weighted_random(stop: StopCondition) -> Self {
        Self {
            kind: GeneratorKind::WeightedRandom,
            stop_condition: stop,
        }
    }

    pub fn a_star(stop: StopCondition) -> Self {
        Self {
            kind: GeneratorKind::AStar,
            stop_condition: stop,
        }
    }

    pub fn shortest_all_paths(stop: StopCondition) -> Self {
        Self {
            kind: GeneratorKind::ShortestAllPaths { path: None },
            stop_condition: stop,
        }
    }

    pub fn predefined(stop: StopCondition) -> Self {
        Self {
            kind: GeneratorKind::Predefined,
            stop_condition: stop,
        }
    }

    pub fn new_york_street_sweeper() -> Self {
        Self {
            kind: GeneratorKind::NewYorkStreetSweeper {
                path: None,
                warnings_emitted: false,
            },
            stop_condition: StopCondition::Never,
        }
    }

    pub fn combined(generators: Vec<PathGenerator>) -> Self {
        Self {
            kind: GeneratorKind::Combined {
                generators,
                index: 0,
            },
            stop_condition: StopCondition::Never,
        }
    }

    pub fn has_next_step(&self, ctx: &ExecutionContext) -> bool {
        match &self.kind {
            GeneratorKind::Combined { generators, index } => generators
                .iter()
                .skip(*index)
                .any(|g| g.has_next_step(ctx)),
            GeneratorKind::NewYorkStreetSweeper { path, .. } => {
                path.as_ref().map_or(true, |p| !p.is_empty())
            }
            _ => !self.stop_condition.is_fulfilled(ctx),
        }
    }

    pub fn skip_actions(&self) -> bool {
        matches!(self.kind, GeneratorKind::NewYorkStreetSweeper { .. })
    }

    pub fn get_next_step(&mut self, ctx: &mut ExecutionContext) -> Result<(), GeneratorError> {
        let current = ctx
            .current_element()
            .ok_or(GeneratorError::NoCurrentElement)?;

        match &mut self.kind {
            GeneratorKind::Random => {
                match current {
                    ElementIndex::Vertex(vi) => {
                        let out = ctx.model().out_edges(vi);
                        if out.is_empty() {
                            return Err(GeneratorError::no_path(Some(current)));
                        }
                        let available: Vec<EdgeIndex> = out
                            .iter()
                            .copied()
                            .filter(|&ei| ctx.is_edge_available(ei))
                            .collect();
                        if available.is_empty() {
                            return Err(GeneratorError::no_path(Some(current)));
                        }
                        let idx = ctx.gen_usize(available.len());
                        ctx.set_current_element(ElementIndex::Edge(available[idx]));
                    }
                    ElementIndex::Edge(ei) => {
                        let target = ctx
                            .model()
                            .edge(ei)
                            .target_vertex()
                            .ok_or(GeneratorError::no_path(Some(current)))?;
                        ctx.set_current_element(ElementIndex::Vertex(target));
                    }
                }
                Ok(())
            }

            GeneratorKind::QuickRandom { target, elements } => {
                if elements.is_empty() {
                    *elements = ctx.model().all_elements();
                    elements.retain(|&e| e != current);
                    ctx.shuffle(elements);
                }

                if target.is_none() || *target == Some(current) {
                    if elements.is_empty() {
                        return Err(GeneratorError::no_path(Some(current)));
                    }
                    elements.sort_by_key(|&e| ctx.is_visited(e));
                    *target = Some(elements[0]);
                }

                let t = target.unwrap();
                let next_elem = {
                    let fw = ctx.floyd_warshall();
                    let neighbors = ctx.model().next_elements(current);
                    let filtered = ctx.filter_elements(&neighbors);
                    astar_next_element(fw, current, t, &filtered)
                        .ok_or(GeneratorError::no_path_to(current, t))?
                };
                elements.retain(|&e| e != next_elem);
                ctx.set_current_element(next_elem);
                Ok(())
            }

            GeneratorKind::WeightedRandom => {
                let next = ctx.model().next_elements(current);
                let elements = ctx.filter_elements(&next);
                if elements.is_empty() {
                    return Err(GeneratorError::no_path(Some(current)));
                }

                if matches!(current, ElementIndex::Vertex(_)) {
                    let selected = weighted_edge_selection(ctx, &elements)?;
                    ctx.set_current_element(selected);
                } else {
                    let idx = ctx.gen_usize(elements.len());
                    ctx.set_current_element(elements[idx]);
                }
                Ok(())
            }

            GeneratorKind::AStar => {
                let next_elem = {
                    let next = ctx.model().next_elements(current);
                    let elements = ctx.filter_elements(&next);
                    if elements.is_empty() {
                        return Err(GeneratorError::no_path(Some(current)));
                    }

                    let targets = self.stop_condition.target_elements(ctx);
                    let fw = ctx.floyd_warshall();

                    let filtered_targets = ctx.filter_elements(&targets);
                    let nearest = filtered_targets
                        .iter()
                        .min_by_key(|&&t| fw.shortest_distance(current, t))
                        .copied()
                        .ok_or(GeneratorError::no_path(Some(current)))?;

                    let neighbors = ctx.model().next_elements(current);
                    let filtered = ctx.filter_elements(&neighbors);
                    astar_next_element(fw, current, nearest, &filtered)
                        .ok_or(GeneratorError::no_path_to(current, nearest))?
                };
                ctx.set_current_element(next_elem);
                Ok(())
            }

            GeneratorKind::ShortestAllPaths { path } => {
                if path.is_none() {
                    let trail = algorithm::euler_path(ctx.model(), current)?;
                    *path = Some(trail);
                }

                let trail = path.as_mut().unwrap();
                if trail.is_empty() {
                    return Err(GeneratorError::no_path(Some(current)));
                }

                let next_elem = trail.remove(0);
                ctx.set_current_element(next_elem);
                Ok(())
            }

            GeneratorKind::Predefined => {
                let next = ctx.model().next_elements(current);
                let elements = ctx.filter_elements(&next);
                if elements.is_empty() {
                    return Err(GeneratorError::no_path(Some(current)));
                }

                let next_elem = if matches!(current, ElementIndex::Edge(_)) {
                    if elements.len() == 1 {
                        elements[0]
                    } else {
                        return Err(GeneratorError::no_path(Some(current)));
                    }
                } else {
                    let edge_index = ctx.predefined_path_current_edge_index();
                    let predefined = ctx.model().predefined_path();
                    if edge_index >= predefined.len() {
                        return Err(GeneratorError::no_path(Some(current)));
                    }
                    let next_edge = ElementIndex::Edge(predefined[edge_index]);
                    if !elements.contains(&next_edge) {
                        return Err(GeneratorError::no_path(Some(current)));
                    }
                    ctx.set_predefined_path_current_edge_index(edge_index + 1);
                    next_edge
                };

                ctx.set_current_element(next_elem);
                Ok(())
            }

            GeneratorKind::NewYorkStreetSweeper {
                path,
                warnings_emitted,
            } => {
                if path.is_none() {
                    let (start_vertex, started_on_edge) = match current {
                        ElementIndex::Vertex(vi) => (vi, false),
                        ElementIndex::Edge(ei) => {
                            let target = ctx
                                .model()
                                .edge(ei)
                                .target_vertex()
                                .ok_or(GeneratorError::no_path(Some(current)))?;
                            (target, true)
                        }
                    };

                    let result =
                        algorithm::chinese_postman_path(ctx.model(), start_vertex)?;

                    if !*warnings_emitted {
                        for warning in &result.warnings {
                            eprintln!("Warning: {}", warning);
                        }
                        *warnings_emitted = true;
                    }

                    let mut computed_path = result.path;
                    if started_on_edge {
                        computed_path
                            .insert(0, ElementIndex::Vertex(start_vertex));
                    }

                    *path = Some(computed_path);
                }

                let trail = path.as_mut().unwrap();
                if trail.is_empty() {
                    return Err(GeneratorError::no_path(Some(current)));
                }

                let next_elem = trail.remove(0);
                ctx.set_current_element(next_elem);
                Ok(())
            }

            GeneratorKind::Combined { generators, index } => {
                while *index < generators.len() {
                    if generators[*index].has_next_step(ctx) {
                        break;
                    }
                    *index += 1;
                }

                if *index >= generators.len() {
                    return Err(GeneratorError::no_path(ctx.current_element()));
                }

                generators[*index].get_next_step(ctx)
            }
        }
    }
}

fn weighted_edge_selection(
    ctx: &mut ExecutionContext,
    elements: &[ElementIndex],
) -> Result<ElementIndex, GeneratorError> {
    let mut probabilities: Vec<(ElementIndex, f64)> = Vec::new();
    let mut total_weight = 0.0;
    let mut num_zeros = 0;

    for &elem in elements {
        if let ElementIndex::Edge(ei) = elem {
            let w = ctx.model().edge(ei).weight();
            if w > 0.0 {
                probabilities.push((elem, w));
                total_weight += w;
                if total_weight > 1.0 {
                    return Err(GeneratorError::InvalidWeight(
                        "Sum of edge weights exceeds 1.0".to_string(),
                    ));
                }
            } else {
                num_zeros += 1;
                probabilities.push((elem, 0.0));
            }
        }
    }

    let rest = if num_zeros > 0 {
        (1.0 - total_weight) / num_zeros as f64
    } else {
        1.0 - total_weight
    };

    let index = ctx.gen_range_int(0..100);
    let mut cumulative = 0.0;

    for (elem, w) in &mut probabilities {
        if *w == 0.0 {
            *w = rest;
        }
        cumulative += *w * 100.0;
        if (index as f64) < cumulative {
            return Ok(*elem);
        }
    }

    elements
        .last()
        .copied()
        .ok_or(GeneratorError::no_path(None))
}

impl std::fmt::Display for PathGenerator {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match &self.kind {
            GeneratorKind::Random => write!(f, "RandomPath({})", self.stop_condition),
            GeneratorKind::QuickRandom { .. } => {
                write!(f, "QuickRandomPath({})", self.stop_condition)
            }
            GeneratorKind::WeightedRandom => {
                write!(f, "WeightedRandomPath({})", self.stop_condition)
            }
            GeneratorKind::AStar => write!(f, "AStarPath({})", self.stop_condition),
            GeneratorKind::ShortestAllPaths { .. } => {
                write!(f, "ShortestAllPaths({})", self.stop_condition)
            }
            GeneratorKind::Predefined => write!(f, "PredefinedPath({})", self.stop_condition),
            GeneratorKind::NewYorkStreetSweeper { .. } => {
                write!(f, "NewYorkStreetSweeper()")
            }
            GeneratorKind::Combined { generators, .. } => {
                let strs: Vec<String> = generators.iter().map(|g| g.to_string()).collect();
                write!(f, "CombinedPath({})", strs.join(", "))
            }
        }
    }
}

#[cfg(test)]
mod tests;
