use std::collections::{HashMap, HashSet};

use uuid::Uuid;

use super::runtime::{RuntimeEdge, RuntimeModel, RuntimeVertex};
use super::{Action, EdgeIndex, Guard, Requirement, VertexIndex};

// ---------------------------------------------------------------------------
// VertexBuilder
// ---------------------------------------------------------------------------

#[derive(Clone, Debug)]
pub struct VertexBuilder {
    pub(crate) id: String,
    pub(crate) name: Option<String>,
    pub(crate) shared_state: Option<String>,
    pub(crate) actions: Vec<Action>,
    pub(crate) requirements: HashSet<Requirement>,
    pub(crate) properties: HashMap<String, serde_json::Value>,
}

impl VertexBuilder {
    pub fn new() -> Self {
        Self {
            id: Uuid::new_v4().to_string(),
            name: None,
            shared_state: None,
            actions: Vec::new(),
            requirements: HashSet::new(),
            properties: HashMap::new(),
        }
    }

    pub fn id(mut self, id: impl Into<String>) -> Self {
        self.id = id.into();
        self
    }

    pub fn name(mut self, name: impl Into<String>) -> Self {
        self.name = Some(name.into());
        self
    }

    pub fn shared_state(mut self, state: impl Into<String>) -> Self {
        self.shared_state = Some(state.into());
        self
    }

    pub fn add_action(mut self, action: Action) -> Self {
        self.actions.push(action);
        self
    }

    pub fn actions(mut self, actions: Vec<Action>) -> Self {
        self.actions = actions;
        self
    }

    pub fn add_requirement(mut self, requirement: Requirement) -> Self {
        self.requirements.insert(requirement);
        self
    }

    pub fn requirements(mut self, requirements: HashSet<Requirement>) -> Self {
        self.requirements = requirements;
        self
    }

    pub fn property(mut self, key: impl Into<String>, value: serde_json::Value) -> Self {
        self.properties.insert(key.into(), value);
        self
    }

    pub fn properties(mut self, properties: HashMap<String, serde_json::Value>) -> Self {
        self.properties = properties;
        self
    }

    pub fn get_id(&self) -> &str {
        &self.id
    }

    pub fn get_name(&self) -> Option<&str> {
        self.name.as_deref()
    }

    pub fn get_shared_state(&self) -> Option<&str> {
        self.shared_state.as_deref()
    }

    pub fn get_actions(&self) -> &[Action] {
        &self.actions
    }

    pub fn get_requirements(&self) -> &HashSet<Requirement> {
        &self.requirements
    }

    pub fn get_properties(&self) -> &HashMap<String, serde_json::Value> {
        &self.properties
    }
}

impl Default for VertexBuilder {
    fn default() -> Self {
        Self::new()
    }
}

// ---------------------------------------------------------------------------
// EdgeBuilder
// ---------------------------------------------------------------------------

#[derive(Clone, Debug)]
pub struct EdgeBuilder {
    pub(crate) id: String,
    pub(crate) name: Option<String>,
    pub(crate) source_vertex: Option<VertexBuilder>,
    pub(crate) target_vertex: Option<VertexBuilder>,
    pub(crate) guard: Option<Guard>,
    pub(crate) actions: Vec<Action>,
    pub(crate) requirements: HashSet<Requirement>,
    pub(crate) properties: HashMap<String, serde_json::Value>,
    pub(crate) weight: f64,
    pub(crate) dependency: i32,
}

impl EdgeBuilder {
    pub fn new() -> Self {
        Self {
            id: Uuid::new_v4().to_string(),
            name: None,
            source_vertex: None,
            target_vertex: None,
            guard: None,
            actions: Vec::new(),
            requirements: HashSet::new(),
            properties: HashMap::new(),
            weight: 0.0,
            dependency: 0,
        }
    }

    pub fn id(mut self, id: impl Into<String>) -> Self {
        self.id = id.into();
        self
    }

    pub fn name(mut self, name: impl Into<String>) -> Self {
        self.name = Some(name.into());
        self
    }

    pub fn source_vertex(mut self, vertex: VertexBuilder) -> Self {
        self.source_vertex = Some(vertex);
        self
    }

    pub fn target_vertex(mut self, vertex: VertexBuilder) -> Self {
        self.target_vertex = Some(vertex);
        self
    }

    pub fn guard(mut self, guard: Guard) -> Self {
        self.guard = Some(guard);
        self
    }

    pub fn add_action(mut self, action: Action) -> Self {
        self.actions.push(action);
        self
    }

    pub fn actions(mut self, actions: Vec<Action>) -> Self {
        self.actions = actions;
        self
    }

    pub fn add_requirement(mut self, requirement: Requirement) -> Self {
        self.requirements.insert(requirement);
        self
    }

    pub fn requirements(mut self, requirements: HashSet<Requirement>) -> Self {
        self.requirements = requirements;
        self
    }

    pub fn property(mut self, key: impl Into<String>, value: serde_json::Value) -> Self {
        self.properties.insert(key.into(), value);
        self
    }

    pub fn properties(mut self, properties: HashMap<String, serde_json::Value>) -> Self {
        self.properties = properties;
        self
    }

    pub fn weight(mut self, weight: f64) -> Self {
        self.weight = weight;
        self
    }

    pub fn dependency(mut self, dependency: i32) -> Self {
        self.dependency = dependency;
        self
    }

    pub fn get_id(&self) -> &str {
        &self.id
    }

    pub fn get_name(&self) -> Option<&str> {
        self.name.as_deref()
    }

    pub fn get_guard(&self) -> Option<&Guard> {
        self.guard.as_ref()
    }

    pub fn get_weight(&self) -> f64 {
        self.weight
    }

    pub fn get_dependency(&self) -> i32 {
        self.dependency
    }
}

impl Default for EdgeBuilder {
    fn default() -> Self {
        Self::new()
    }
}

// ---------------------------------------------------------------------------
// ModelBuilder
// ---------------------------------------------------------------------------

#[derive(Clone, Debug)]
pub struct ModelBuilder {
    id: String,
    name: Option<String>,
    vertices: Vec<VertexBuilder>,
    edges: Vec<EdgeBuilder>,
    actions: Vec<Action>,
    requirements: HashSet<Requirement>,
    properties: HashMap<String, serde_json::Value>,
    predefined_path_edge_ids: Vec<String>,
}

impl ModelBuilder {
    pub fn new() -> Self {
        Self {
            id: Uuid::new_v4().to_string(),
            name: None,
            vertices: Vec::new(),
            edges: Vec::new(),
            actions: Vec::new(),
            requirements: HashSet::new(),
            properties: HashMap::new(),
            predefined_path_edge_ids: Vec::new(),
        }
    }

    pub fn id(&mut self, id: impl Into<String>) -> &mut Self {
        self.id = id.into();
        self
    }

    pub fn name(&mut self, name: impl Into<String>) -> &mut Self {
        self.name = Some(name.into());
        self
    }

    pub fn add_vertex(&mut self, vertex: VertexBuilder) -> &mut Self {
        self.vertices.push(vertex);
        self
    }

    pub fn add_edge(&mut self, edge: EdgeBuilder) -> &mut Self {
        self.edges.push(edge);
        self
    }

    pub fn add_action(&mut self, action: Action) -> &mut Self {
        self.actions.push(action);
        self
    }

    pub fn set_actions(&mut self, actions: Vec<Action>) -> &mut Self {
        self.actions = actions;
        self
    }

    pub fn add_requirement(&mut self, requirement: Requirement) -> &mut Self {
        self.requirements.insert(requirement);
        self
    }

    pub fn set_requirements(&mut self, requirements: HashSet<Requirement>) -> &mut Self {
        self.requirements = requirements;
        self
    }

    pub fn property(&mut self, key: impl Into<String>, value: serde_json::Value) -> &mut Self {
        self.properties.insert(key.into(), value);
        self
    }

    pub fn set_properties(&mut self, properties: HashMap<String, serde_json::Value>) -> &mut Self {
        self.properties = properties;
        self
    }

    pub fn set_predefined_path_edge_ids(&mut self, ids: Vec<String>) -> &mut Self {
        self.predefined_path_edge_ids = ids;
        self
    }

    pub fn get_vertices(&self) -> &[VertexBuilder] {
        &self.vertices
    }

    pub fn get_edges(&self) -> &[EdgeBuilder] {
        &self.edges
    }

    pub fn delete_vertex(&mut self, vertex: &VertexBuilder) -> &mut Self {
        self.delete_vertex_by_id(&vertex.id.clone())
    }

    pub fn delete_vertex_by_id(&mut self, id: &str) -> &mut Self {
        self.vertices.retain(|v| v.id != id);
        self.edges.retain(|e| {
            let src_ok = e.source_vertex.as_ref().is_none_or(|v| v.id != id);
            let tgt_ok = e.target_vertex.as_ref().is_none_or(|v| v.id != id);
            src_ok && tgt_ok
        });
        self
    }

    pub fn delete_edge(&mut self, edge: &EdgeBuilder) -> &mut Self {
        self.delete_edge_by_id(&edge.id.clone())
    }

    pub fn delete_edge_by_id(&mut self, id: &str) -> &mut Self {
        self.edges.retain(|e| e.id != id);
        self
    }

    pub fn build(self) -> RuntimeModel {
        // 1. Collect all unique vertices (model-level first, then from edges).
        //    Dedup by ID — first occurrence wins.
        let mut id_to_index: HashMap<String, VertexIndex> = HashMap::new();
        let mut vertex_builders: Vec<VertexBuilder> = Vec::new();

        let insert_vertex = |vb: VertexBuilder,
                             map: &mut HashMap<String, VertexIndex>,
                             verts: &mut Vec<VertexBuilder>|
         -> VertexIndex {
            if let Some(&idx) = map.get(&vb.id) {
                idx
            } else {
                let idx = VertexIndex(verts.len());
                map.insert(vb.id.clone(), idx);
                verts.push(vb);
                idx
            }
        };

        for v in self.vertices {
            insert_vertex(v, &mut id_to_index, &mut vertex_builders);
        }

        for edge in &self.edges {
            if let Some(ref v) = edge.source_vertex {
                insert_vertex(v.clone(), &mut id_to_index, &mut vertex_builders);
            }
            if let Some(ref v) = edge.target_vertex {
                insert_vertex(v.clone(), &mut id_to_index, &mut vertex_builders);
            }
        }

        // 2. Build RuntimeVertex list.
        let vertices: Vec<RuntimeVertex> = vertex_builders
            .into_iter()
            .map(|vb| RuntimeVertex {
                id: vb.id,
                name: vb.name,
                shared_state: vb.shared_state,
                actions: vb.actions,
                requirements: vb.requirements,
                properties: vb.properties,
            })
            .collect();

        // 3. Build RuntimeEdge list, resolving vertex references to indices.
        let mut edge_id_to_index: HashMap<String, EdgeIndex> = HashMap::new();
        let edges: Vec<RuntimeEdge> = self
            .edges
            .into_iter()
            .enumerate()
            .map(|(i, eb)| {
                let source = eb
                    .source_vertex
                    .as_ref()
                    .and_then(|v| id_to_index.get(&v.id).copied());
                let target = eb
                    .target_vertex
                    .as_ref()
                    .and_then(|v| id_to_index.get(&v.id).copied());
                edge_id_to_index.insert(eb.id.clone(), EdgeIndex(i));
                RuntimeEdge {
                    id: eb.id,
                    name: eb.name,
                    source_vertex: source,
                    target_vertex: target,
                    guard: eb.guard,
                    actions: eb.actions,
                    requirements: eb.requirements,
                    properties: eb.properties,
                    weight: eb.weight,
                    dependency: eb.dependency,
                }
            })
            .collect();

        // 4. Resolve predefined path edge IDs to EdgeIndex.
        let predefined_path: Vec<EdgeIndex> = self
            .predefined_path_edge_ids
            .iter()
            .filter_map(|id| edge_id_to_index.get(id).copied())
            .collect();

        // 5. Build the RuntimeModel with caches.
        RuntimeModel::new(
            self.id,
            self.name,
            vertices,
            edges,
            self.actions,
            self.requirements,
            self.properties,
            predefined_path,
        )
    }
}

impl Default for ModelBuilder {
    fn default() -> Self {
        Self::new()
    }
}
