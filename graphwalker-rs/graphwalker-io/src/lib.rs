pub mod graphml;
pub mod json;

use std::path::Path;

use graphwalker_core::model::RuntimeModel;

#[derive(Clone, Debug)]
pub enum IoError {
    Io(String),
    Json(String),
    Xml(String),
    InvalidModel(String),
}

impl std::fmt::Display for IoError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Io(msg) => write!(f, "IO error: {}", msg),
            Self::Json(msg) => write!(f, "JSON error: {}", msg),
            Self::Xml(msg) => write!(f, "XML error: {}", msg),
            Self::InvalidModel(msg) => write!(f, "Invalid model: {}", msg),
        }
    }
}

impl std::error::Error for IoError {}

impl From<std::io::Error> for IoError {
    fn from(e: std::io::Error) -> Self {
        Self::Io(e.to_string())
    }
}

impl From<serde_json::Error> for IoError {
    fn from(e: serde_json::Error) -> Self {
        Self::Json(e.to_string())
    }
}

impl From<quick_xml::Error> for IoError {
    fn from(e: quick_xml::Error) -> Self {
        Self::Xml(e.to_string())
    }
}

#[derive(Clone, Debug)]
pub struct ModelContext {
    pub model: RuntimeModel,
    pub generator: Option<String>,
    pub start_element_id: Option<String>,
}

pub fn read_model(path: &Path) -> Result<Vec<ModelContext>, IoError> {
    match path.extension().and_then(|e| e.to_str()) {
        Some("json") => json::read_json_file(path),
        Some("graphml") => graphml::read_graphml_file(path),
        _ => Err(IoError::Io(format!(
            "Unsupported file extension: {}",
            path.display()
        ))),
    }
}
