use std::collections::HashMap;
use std::path::Path;

use serde::{Deserialize, Deserializer, Serialize, Serializer};

use graphwalker_core::model::{
    Action, EdgeBuilder, Guard, ModelBuilder, Requirement, RuntimeModel, VertexBuilder,
};

use crate::{IoError, ModelContext};

// ---------------------------------------------------------------------------
// Serde DTOs — match Java's Gson-serialized JSON format exactly
// ---------------------------------------------------------------------------

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct JsonMultimodel {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub name: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub seed: Option<i64>,
    pub models: Vec<JsonModel>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct JsonModel {
    #[serde(default)]
    pub name: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub id: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub generator: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub start_element_id: Option<String>,
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub actions: Vec<String>,
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub requirements: Vec<String>,
    #[serde(
        default,
        skip_serializing_if = "HashMap::is_empty",
        deserialize_with = "deserialize_properties"
    )]
    pub properties: HashMap<String, serde_json::Value>,
    #[serde(default)]
    pub vertices: Vec<JsonVertex>,
    #[serde(default)]
    pub edges: Vec<JsonEdge>,
    #[serde(
        default,
        skip_serializing_if = "Vec::is_empty",
        rename = "predefinedPathEdgeIds"
    )]
    pub predefined_path_edge_ids: Vec<String>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct JsonVertex {
    pub id: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub name: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub shared_state: Option<String>,
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub actions: Vec<String>,
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub requirements: Vec<String>,
    #[serde(
        default,
        skip_serializing_if = "HashMap::is_empty",
        deserialize_with = "deserialize_properties"
    )]
    pub properties: HashMap<String, serde_json::Value>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct JsonEdge {
    pub id: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub name: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub guard: Option<String>,
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub actions: Vec<String>,
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub requirements: Vec<String>,
    #[serde(
        default,
        skip_serializing_if = "HashMap::is_empty",
        deserialize_with = "deserialize_properties"
    )]
    pub properties: HashMap<String, serde_json::Value>,
    #[serde(
        default,
        skip_serializing_if = "Option::is_none",
        deserialize_with = "deserialize_optional_f64",
        serialize_with = "serialize_optional_f64"
    )]
    pub weight: Option<f64>,
    #[serde(
        default,
        skip_serializing_if = "Option::is_none",
        deserialize_with = "deserialize_optional_i32"
    )]
    pub dependency: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub source_vertex_id: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub target_vertex_id: Option<String>,
}

// ---------------------------------------------------------------------------
// Custom deserializers for Java format quirks
// ---------------------------------------------------------------------------

fn deserialize_properties<'de, D>(
    deserializer: D,
) -> Result<HashMap<String, serde_json::Value>, D::Error>
where
    D: Deserializer<'de>,
{
    let value = serde_json::Value::deserialize(deserializer)?;
    match value {
        serde_json::Value::Object(map) => Ok(map.into_iter().collect()),
        serde_json::Value::Array(_) => Ok(HashMap::new()),
        serde_json::Value::Null => Ok(HashMap::new()),
        _ => Err(serde::de::Error::custom(
            "expected object or array for properties",
        )),
    }
}

fn deserialize_optional_f64<'de, D>(deserializer: D) -> Result<Option<f64>, D::Error>
where
    D: Deserializer<'de>,
{
    let value = Option::<serde_json::Value>::deserialize(deserializer)?;
    match value {
        None | Some(serde_json::Value::Null) => Ok(None),
        Some(serde_json::Value::Number(n)) => n
            .as_f64()
            .map(Some)
            .ok_or_else(|| serde::de::Error::custom("invalid number")),
        Some(serde_json::Value::String(s)) => {
            s.parse::<f64>().map(Some).map_err(serde::de::Error::custom)
        }
        _ => Err(serde::de::Error::custom(
            "expected number or string for weight",
        )),
    }
}

fn serialize_optional_f64<S>(value: &Option<f64>, serializer: S) -> Result<S::Ok, S::Error>
where
    S: Serializer,
{
    match value {
        Some(v) => serializer.serialize_f64(*v),
        None => serializer.serialize_none(),
    }
}

fn deserialize_optional_i32<'de, D>(deserializer: D) -> Result<Option<i32>, D::Error>
where
    D: Deserializer<'de>,
{
    let value = Option::<serde_json::Value>::deserialize(deserializer)?;
    match value {
        None | Some(serde_json::Value::Null) => Ok(None),
        Some(serde_json::Value::Number(n)) => n
            .as_i64()
            .map(|v| Some(v as i32))
            .ok_or_else(|| serde::de::Error::custom("invalid integer")),
        Some(serde_json::Value::String(s)) => {
            s.parse::<i32>().map(Some).map_err(serde::de::Error::custom)
        }
        _ => Err(serde::de::Error::custom(
            "expected number or string for dependency",
        )),
    }
}

// ---------------------------------------------------------------------------
// JSON → ModelBuilder conversion
// ---------------------------------------------------------------------------

fn json_model_to_builder(jm: &JsonModel) -> ModelBuilder {
    let mut mb = ModelBuilder::new();

    if let Some(ref id) = jm.id {
        mb.id(id.clone());
    }
    if let Some(ref name) = jm.name {
        mb.name(name);
    }

    if !jm.properties.is_empty() {
        mb.set_properties(jm.properties.clone());
    }

    for action_str in &jm.actions {
        let trimmed = action_str.trim();
        if !trimmed.is_empty() {
            mb.add_action(Action::new(trimmed));
        }
    }

    for req_str in &jm.requirements {
        let trimmed = req_str.trim();
        if !trimmed.is_empty() {
            mb.add_requirement(Requirement::new(trimmed));
        }
    }

    for jv in &jm.vertices {
        let mut vb = VertexBuilder::new().id(&jv.id);
        if let Some(ref name) = jv.name {
            vb = vb.name(name);
        }
        if let Some(ref ss) = jv.shared_state {
            vb = vb.shared_state(ss);
        }
        for a in &jv.actions {
            let trimmed = a.trim();
            if !trimmed.is_empty() {
                vb = vb.add_action(Action::new(trimmed));
            }
        }
        for r in &jv.requirements {
            let trimmed = r.trim();
            if !trimmed.is_empty() {
                vb = vb.add_requirement(Requirement::new(trimmed));
            }
        }
        if !jv.properties.is_empty() {
            vb = vb.properties(jv.properties.clone());
        }
        mb.add_vertex(vb);
    }

    for je in &jm.edges {
        let mut eb = EdgeBuilder::new().id(&je.id);
        if let Some(ref name) = je.name {
            eb = eb.name(name);
        }
        if let Some(ref guard) = je.guard {
            if !guard.is_empty() {
                eb = eb.guard(Guard::new(guard));
            }
        }
        if let Some(w) = je.weight {
            eb = eb.weight(w);
        }
        if let Some(d) = je.dependency {
            eb = eb.dependency(d);
        }
        for a in &je.actions {
            let trimmed = a.trim();
            if !trimmed.is_empty() {
                eb = eb.add_action(Action::new(trimmed));
            }
        }
        for r in &je.requirements {
            let trimmed = r.trim();
            if !trimmed.is_empty() {
                eb = eb.add_requirement(Requirement::new(trimmed));
            }
        }
        if !je.properties.is_empty() {
            eb = eb.properties(je.properties.clone());
        }

        if let Some(ref src_id) = je.source_vertex_id {
            eb = eb.source_vertex(VertexBuilder::new().id(src_id));
        }
        if let Some(ref tgt_id) = je.target_vertex_id {
            eb = eb.target_vertex(VertexBuilder::new().id(tgt_id));
        }

        mb.add_edge(eb);
    }

    if !jm.predefined_path_edge_ids.is_empty() {
        mb.set_predefined_path_edge_ids(jm.predefined_path_edge_ids.clone());
    }

    mb
}

// ---------------------------------------------------------------------------
// RuntimeModel → JSON conversion
// ---------------------------------------------------------------------------

fn runtime_model_to_json(
    model: &RuntimeModel,
    generator: Option<&str>,
    start_element_id: Option<&str>,
) -> JsonModel {
    let vertices: Vec<JsonVertex> = model
        .vertices()
        .iter()
        .map(|v| {
            let requirements: Vec<String> = v
                .requirements()
                .iter()
                .map(|r| r.key().to_string())
                .collect();
            let actions: Vec<String> = v.actions().iter().map(|a| a.script().to_string()).collect();
            JsonVertex {
                id: v.id().to_string(),
                name: v.name().map(|n| n.to_string()),
                shared_state: v.shared_state().map(|s| s.to_string()),
                actions,
                requirements,
                properties: v.properties().clone(),
            }
        })
        .collect();

    let edges: Vec<JsonEdge> = model
        .edges()
        .iter()
        .map(|e| {
            let source_vertex_id = e
                .source_vertex()
                .map(|vi| model.vertex(vi).id().to_string());
            let target_vertex_id = e
                .target_vertex()
                .map(|vi| model.vertex(vi).id().to_string());
            let requirements: Vec<String> = e
                .requirements()
                .iter()
                .map(|r| r.key().to_string())
                .collect();
            let actions: Vec<String> = e.actions().iter().map(|a| a.script().to_string()).collect();

            JsonEdge {
                id: e.id().to_string(),
                name: e.name().map(|n| n.to_string()),
                guard: e.guard().map(|g| g.script().to_string()),
                actions,
                requirements,
                properties: e.properties().clone(),
                weight: if e.weight() != 0.0 {
                    Some(e.weight())
                } else {
                    None
                },
                dependency: if e.dependency() != 0 {
                    Some(e.dependency())
                } else {
                    None
                },
                source_vertex_id,
                target_vertex_id,
            }
        })
        .collect();

    let model_actions: Vec<String> = model
        .actions()
        .iter()
        .map(|a| a.script().to_string())
        .collect();
    let model_requirements: Vec<String> = model
        .requirements()
        .iter()
        .map(|r| r.key().to_string())
        .collect();

    let predefined_path_edge_ids: Vec<String> = model
        .predefined_path()
        .iter()
        .map(|&ei| model.edge(ei).id().to_string())
        .collect();

    JsonModel {
        name: model.name().map(|n| n.to_string()),
        id: Some(model.id().to_string()),
        generator: generator.map(|s| s.to_string()),
        start_element_id: start_element_id.map(|s| s.to_string()),
        actions: model_actions,
        requirements: model_requirements,
        properties: model.properties().clone(),
        vertices,
        edges,
        predefined_path_edge_ids,
    }
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

pub fn read_json_string(json: &str) -> Result<Vec<ModelContext>, IoError> {
    let raw: serde_json::Value =
        serde_json::from_str(json).map_err(|e| IoError::Json(describe_json_error(json, &e)))?;

    let models_val = raw
        .get("models")
        .and_then(|v| v.as_array())
        .ok_or_else(|| {
            IoError::Json(
                "The JSON file is not a valid GraphWalker model: missing top-level \"models\" array."
                    .to_string(),
            )
        })?;

    let mut contexts = Vec::new();
    for (i, model_val) in models_val.iter().enumerate() {
        let jm: JsonModel = serde_json::from_value(model_val.clone()).map_err(|e| {
            let model_label = model_val
                .get("name")
                .and_then(|n| n.as_str())
                .or_else(|| model_val.get("id").and_then(|n| n.as_str()));
            let location = match model_label {
                Some(name) => format!("model \"{}\" (index {})", name, i),
                None => format!("model at index {}", i),
            };
            IoError::Json(format!("Error in {}: {}", location, e))
        })?;
        let mb = json_model_to_builder(&jm);
        let model = mb.build();
        contexts.push(ModelContext {
            model,
            generator: jm.generator.clone(),
            start_element_id: jm.start_element_id.as_deref().filter(|s| !s.is_empty()).map(String::from),
        });
    }

    Ok(contexts)
}

fn describe_json_error(json: &str, err: &serde_json::Error) -> String {
    let line = err.line();
    let col = err.column();
    let context = json
        .lines()
        .nth(line.saturating_sub(1))
        .map(|l| l.trim())
        .unwrap_or("");
    if context.is_empty() {
        format!("Invalid JSON at line {}, column {}: {}", line, col, err)
    } else {
        format!(
            "Invalid JSON at line {}, column {}: {}\n  --> {}",
            line, col, err, context
        )
    }
}

pub fn read_json_file(path: &Path) -> Result<Vec<ModelContext>, IoError> {
    let content = std::fs::read_to_string(path)?;
    read_json_string(&content)
}

pub fn write_json_string(contexts: &[ModelContext]) -> Result<String, IoError> {
    let models: Vec<JsonModel> = contexts
        .iter()
        .map(|ctx| {
            runtime_model_to_json(
                &ctx.model,
                ctx.generator.as_deref(),
                ctx.start_element_id.as_deref(),
            )
        })
        .collect();

    let multimodel = JsonMultimodel {
        name: None,
        seed: None,
        models,
    };

    serde_json::to_string_pretty(&multimodel).map_err(IoError::from)
}

pub fn write_json_file(contexts: &[ModelContext], path: &Path) -> Result<(), IoError> {
    let json = write_json_string(contexts)?;
    std::fs::write(path, json)?;
    Ok(())
}

#[cfg(test)]
mod tests;
