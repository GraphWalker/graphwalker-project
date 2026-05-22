use std::collections::{HashMap, HashSet};
use std::hash::{Hash, Hasher};

use super::{Action, EdgeIndex, ElementIndex, Guard, Requirement, VertexIndex};

// ---------------------------------------------------------------------------
// RuntimeVertex
// ---------------------------------------------------------------------------

#[derive(Clone, Debug)]
pub struct RuntimeVertex {
    pub(crate) id: String,
    pub(crate) name: Option<String>,
    pub(crate) shared_state: Option<String>,
    pub(crate) actions: Vec<Action>,
    pub(crate) requirements: HashSet<Requirement>,
    pub(crate) properties: HashMap<String, serde_json::Value>,
}

impl RuntimeVertex {
    pub fn id(&self) -> &str {
        &self.id
    }

    pub fn name(&self) -> Option<&str> {
        self.name.as_deref()
    }

    pub fn has_name(&self) -> bool {
        self.name.as_ref().is_some_and(|n| !n.is_empty())
    }

    pub fn shared_state(&self) -> Option<&str> {
        self.shared_state.as_deref()
    }

    pub fn has_shared_state(&self) -> bool {
        self.shared_state.as_ref().is_some_and(|s| !s.is_empty())
    }

    pub fn actions(&self) -> &[Action] {
        &self.actions
    }

    pub fn has_actions(&self) -> bool {
        !self.actions.is_empty()
    }

    pub fn requirements(&self) -> &HashSet<Requirement> {
        &self.requirements
    }

    pub fn has_requirements(&self) -> bool {
        !self.requirements.is_empty()
    }

    pub fn properties(&self) -> &HashMap<String, serde_json::Value> {
        &self.properties
    }

    pub fn property(&self, key: &str) -> Option<&serde_json::Value> {
        self.properties.get(key)
    }

    pub fn has_property(&self, key: &str) -> bool {
        self.properties.contains_key(key)
    }

    pub fn has_properties(&self) -> bool {
        !self.properties.is_empty()
    }
}

impl PartialEq for RuntimeVertex {
    fn eq(&self, other: &Self) -> bool {
        self.id == other.id
    }
}

impl Eq for RuntimeVertex {}

impl Hash for RuntimeVertex {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.id.hash(state);
    }
}

// ---------------------------------------------------------------------------
// RuntimeEdge
// ---------------------------------------------------------------------------

#[derive(Clone, Debug)]
pub struct RuntimeEdge {
    pub(crate) id: String,
    pub(crate) name: Option<String>,
    pub(crate) source_vertex: Option<VertexIndex>,
    pub(crate) target_vertex: Option<VertexIndex>,
    pub(crate) guard: Option<Guard>,
    pub(crate) actions: Vec<Action>,
    pub(crate) requirements: HashSet<Requirement>,
    pub(crate) properties: HashMap<String, serde_json::Value>,
    pub(crate) weight: f64,
    pub(crate) dependency: i32,
}

impl RuntimeEdge {
    pub fn id(&self) -> &str {
        &self.id
    }

    pub fn name(&self) -> Option<&str> {
        self.name.as_deref()
    }

    pub fn has_name(&self) -> bool {
        self.name.as_ref().is_some_and(|n| !n.is_empty())
    }

    pub fn source_vertex(&self) -> Option<VertexIndex> {
        self.source_vertex
    }

    pub fn target_vertex(&self) -> Option<VertexIndex> {
        self.target_vertex
    }

    pub fn guard(&self) -> Option<&Guard> {
        self.guard.as_ref()
    }

    pub fn has_guard(&self) -> bool {
        self.guard.as_ref().is_some_and(|g| g.has_script())
    }

    pub fn actions(&self) -> &[Action] {
        &self.actions
    }

    pub fn has_actions(&self) -> bool {
        !self.actions.is_empty()
    }

    pub fn requirements(&self) -> &HashSet<Requirement> {
        &self.requirements
    }

    pub fn has_requirements(&self) -> bool {
        !self.requirements.is_empty()
    }

    pub fn properties(&self) -> &HashMap<String, serde_json::Value> {
        &self.properties
    }

    pub fn property(&self, key: &str) -> Option<&serde_json::Value> {
        self.properties.get(key)
    }

    pub fn has_property(&self, key: &str) -> bool {
        self.properties.contains_key(key)
    }

    pub fn has_properties(&self) -> bool {
        !self.properties.is_empty()
    }

    pub fn weight(&self) -> f64 {
        self.weight
    }

    pub fn dependency(&self) -> i32 {
        self.dependency
    }

    pub fn dependency_as_f64(&self) -> f64 {
        f64::from(self.dependency) / 100.0
    }
}

impl PartialEq for RuntimeEdge {
    fn eq(&self, other: &Self) -> bool {
        self.id == other.id
    }
}

impl Eq for RuntimeEdge {}

impl Hash for RuntimeEdge {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.id.hash(state);
    }
}

// ---------------------------------------------------------------------------
// RuntimeModel
// ---------------------------------------------------------------------------

static EMPTY_EDGE_INDICES: &[EdgeIndex] = &[];
static EMPTY_VERTEX_INDICES: &[VertexIndex] = &[];
static EMPTY_ELEMENT_INDICES: &[ElementIndex] = &[];

#[derive(Clone, Debug)]
pub struct RuntimeModel {
    id: String,
    name: Option<String>,
    vertices: Vec<RuntimeVertex>,
    edges: Vec<RuntimeEdge>,
    actions: Vec<Action>,
    requirements: HashSet<Requirement>,
    properties: HashMap<String, serde_json::Value>,
    predefined_path: Vec<EdgeIndex>,

    // Caches
    out_edges: HashMap<VertexIndex, Vec<EdgeIndex>>,
    in_edges: HashMap<VertexIndex, Vec<EdgeIndex>>,
    vertices_by_name: HashMap<String, Vec<VertexIndex>>,
    edges_by_name: HashMap<String, Vec<EdgeIndex>>,
    elements_by_name: HashMap<String, Vec<ElementIndex>>,
    elements_by_id: HashMap<String, ElementIndex>,
    shared_states: HashMap<String, Vec<VertexIndex>>,
}

impl RuntimeModel {
    #[allow(clippy::too_many_arguments)]
    pub(crate) fn new(
        id: String,
        name: Option<String>,
        vertices: Vec<RuntimeVertex>,
        edges: Vec<RuntimeEdge>,
        actions: Vec<Action>,
        requirements: HashSet<Requirement>,
        properties: HashMap<String, serde_json::Value>,
        predefined_path: Vec<EdgeIndex>,
    ) -> Self {
        let out_edges = Self::build_out_edges(&edges);
        let in_edges = Self::build_in_edges(&edges);
        let vertices_by_name = Self::build_vertices_by_name(&vertices);
        let edges_by_name = Self::build_edges_by_name(&edges);
        let elements_by_name = Self::build_elements_by_name(&vertices, &edges);
        let elements_by_id = Self::build_elements_by_id(&vertices, &edges);
        let shared_states = Self::build_shared_states(&vertices);

        Self {
            id,
            name,
            vertices,
            edges,
            actions,
            requirements,
            properties,
            predefined_path,
            out_edges,
            in_edges,
            vertices_by_name,
            edges_by_name,
            elements_by_name,
            elements_by_id,
            shared_states,
        }
    }

    // -- Getters --

    pub fn id(&self) -> &str {
        &self.id
    }

    pub fn name(&self) -> Option<&str> {
        self.name.as_deref()
    }

    pub fn has_name(&self) -> bool {
        self.name.as_ref().is_some_and(|n| !n.is_empty())
    }

    pub fn vertices(&self) -> &[RuntimeVertex] {
        &self.vertices
    }

    pub fn edges(&self) -> &[RuntimeEdge] {
        &self.edges
    }

    pub fn vertex(&self, idx: VertexIndex) -> &RuntimeVertex {
        &self.vertices[idx.0]
    }

    pub fn edge(&self, idx: EdgeIndex) -> &RuntimeEdge {
        &self.edges[idx.0]
    }

    pub fn actions(&self) -> &[Action] {
        &self.actions
    }

    pub fn has_actions(&self) -> bool {
        !self.actions.is_empty()
    }

    pub fn requirements(&self) -> &HashSet<Requirement> {
        &self.requirements
    }

    pub fn has_requirements(&self) -> bool {
        !self.requirements.is_empty()
    }

    pub fn properties(&self) -> &HashMap<String, serde_json::Value> {
        &self.properties
    }

    pub fn property(&self, key: &str) -> Option<&serde_json::Value> {
        self.properties.get(key)
    }

    pub fn has_property(&self, key: &str) -> bool {
        self.properties.contains_key(key)
    }

    pub fn predefined_path(&self) -> &[EdgeIndex] {
        &self.predefined_path
    }

    pub fn has_predefined_path(&self) -> bool {
        !self.predefined_path.is_empty()
    }

    // -- Graph queries --

    pub fn out_edges(&self, vertex: VertexIndex) -> &[EdgeIndex] {
        self.out_edges
            .get(&vertex)
            .map(|v| v.as_slice())
            .unwrap_or(EMPTY_EDGE_INDICES)
    }

    pub fn in_edges(&self, vertex: VertexIndex) -> &[EdgeIndex] {
        self.in_edges
            .get(&vertex)
            .map(|v| v.as_slice())
            .unwrap_or(EMPTY_EDGE_INDICES)
    }

    pub fn find_vertices(&self, name: &str) -> &[VertexIndex] {
        self.vertices_by_name
            .get(name)
            .map(|v| v.as_slice())
            .unwrap_or(EMPTY_VERTEX_INDICES)
    }

    pub fn find_edges(&self, name: &str) -> &[EdgeIndex] {
        self.edges_by_name
            .get(name)
            .map(|v| v.as_slice())
            .unwrap_or(EMPTY_EDGE_INDICES)
    }

    pub fn find_elements(&self, name: &str) -> &[ElementIndex] {
        self.elements_by_name
            .get(name)
            .map(|v| v.as_slice())
            .unwrap_or(EMPTY_ELEMENT_INDICES)
    }

    pub fn element_by_id(&self, id: &str) -> Option<ElementIndex> {
        self.elements_by_id.get(id).copied()
    }

    pub fn shared_state_vertices(&self, state: &str) -> &[VertexIndex] {
        self.shared_states
            .get(state)
            .map(|v| v.as_slice())
            .unwrap_or(EMPTY_VERTEX_INDICES)
    }

    pub fn shared_state_names(&self) -> impl Iterator<Item = &str> {
        self.shared_states.keys().map(|s| s.as_str())
    }

    pub fn has_shared_states(&self) -> bool {
        !self.shared_states.is_empty()
    }

    pub fn has_shared_state(&self, state: &str) -> bool {
        self.shared_states.contains_key(state)
    }

    /// Returns the elements reachable from the given element in one step.
    /// For a vertex: its outgoing edges. For an edge: its target vertex.
    pub fn next_elements(&self, element: ElementIndex) -> Vec<ElementIndex> {
        match element {
            ElementIndex::Vertex(vi) => self
                .out_edges(vi)
                .iter()
                .map(|&ei| ElementIndex::Edge(ei))
                .collect(),
            ElementIndex::Edge(ei) => self.edges[ei.0]
                .target_vertex
                .into_iter()
                .map(ElementIndex::Vertex)
                .collect(),
        }
    }

    /// Returns all elements (vertices then edges) as ElementIndex.
    pub fn all_elements(&self) -> Vec<ElementIndex> {
        let mut result = Vec::with_capacity(self.vertices.len() + self.edges.len());
        for i in 0..self.vertices.len() {
            result.push(ElementIndex::Vertex(VertexIndex(i)));
        }
        for i in 0..self.edges.len() {
            result.push(ElementIndex::Edge(EdgeIndex(i)));
        }
        result
    }

    // -- Cache builders --

    fn build_out_edges(edges: &[RuntimeEdge]) -> HashMap<VertexIndex, Vec<EdgeIndex>> {
        let mut map: HashMap<VertexIndex, Vec<EdgeIndex>> = HashMap::new();
        for (i, edge) in edges.iter().enumerate() {
            if let Some(src) = edge.source_vertex {
                map.entry(src).or_default().push(EdgeIndex(i));
            }
        }
        map
    }

    fn build_in_edges(edges: &[RuntimeEdge]) -> HashMap<VertexIndex, Vec<EdgeIndex>> {
        let mut map: HashMap<VertexIndex, Vec<EdgeIndex>> = HashMap::new();
        for (i, edge) in edges.iter().enumerate() {
            if let Some(tgt) = edge.target_vertex {
                map.entry(tgt).or_default().push(EdgeIndex(i));
            }
        }
        map
    }

    fn build_vertices_by_name(vertices: &[RuntimeVertex]) -> HashMap<String, Vec<VertexIndex>> {
        let mut map: HashMap<String, Vec<VertexIndex>> = HashMap::new();
        for (i, v) in vertices.iter().enumerate() {
            if let Some(ref name) = v.name {
                if !name.is_empty() {
                    map.entry(name.clone()).or_default().push(VertexIndex(i));
                }
            }
        }
        map
    }

    fn build_edges_by_name(edges: &[RuntimeEdge]) -> HashMap<String, Vec<EdgeIndex>> {
        let mut map: HashMap<String, Vec<EdgeIndex>> = HashMap::new();
        for (i, e) in edges.iter().enumerate() {
            if let Some(ref name) = e.name {
                if !name.is_empty() {
                    map.entry(name.clone()).or_default().push(EdgeIndex(i));
                }
            }
        }
        map
    }

    fn build_elements_by_name(
        vertices: &[RuntimeVertex],
        edges: &[RuntimeEdge],
    ) -> HashMap<String, Vec<ElementIndex>> {
        let mut map: HashMap<String, Vec<ElementIndex>> = HashMap::new();
        for (i, v) in vertices.iter().enumerate() {
            if let Some(ref name) = v.name {
                if !name.is_empty() {
                    map.entry(name.clone())
                        .or_default()
                        .push(ElementIndex::Vertex(VertexIndex(i)));
                }
            }
        }
        for (i, e) in edges.iter().enumerate() {
            if let Some(ref name) = e.name {
                if !name.is_empty() {
                    map.entry(name.clone())
                        .or_default()
                        .push(ElementIndex::Edge(EdgeIndex(i)));
                }
            }
        }
        map
    }

    fn build_elements_by_id(
        vertices: &[RuntimeVertex],
        edges: &[RuntimeEdge],
    ) -> HashMap<String, ElementIndex> {
        let mut map = HashMap::new();
        for (i, v) in vertices.iter().enumerate() {
            map.insert(v.id.clone(), ElementIndex::Vertex(VertexIndex(i)));
        }
        for (i, e) in edges.iter().enumerate() {
            map.insert(e.id.clone(), ElementIndex::Edge(EdgeIndex(i)));
        }
        map
    }

    fn build_shared_states(vertices: &[RuntimeVertex]) -> HashMap<String, Vec<VertexIndex>> {
        let mut map: HashMap<String, Vec<VertexIndex>> = HashMap::new();
        for (i, v) in vertices.iter().enumerate() {
            if let Some(ref state) = v.shared_state {
                if !state.is_empty() {
                    map.entry(state.clone()).or_default().push(VertexIndex(i));
                }
            }
        }
        map
    }
}

impl PartialEq for RuntimeModel {
    fn eq(&self, other: &Self) -> bool {
        self.id == other.id
    }
}

impl Eq for RuntimeModel {}

impl Hash for RuntimeModel {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.id.hash(state);
    }
}
