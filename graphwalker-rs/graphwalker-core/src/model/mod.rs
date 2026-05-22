mod builder;
mod runtime;

pub use self::builder::{EdgeBuilder, ModelBuilder, VertexBuilder};
pub use self::runtime::{RuntimeEdge, RuntimeModel, RuntimeVertex};

use std::fmt;

// ---------------------------------------------------------------------------
// Index types
// ---------------------------------------------------------------------------

#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub struct VertexIndex(pub usize);

#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub struct EdgeIndex(pub usize);

#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub enum ElementIndex {
    Vertex(VertexIndex),
    Edge(EdgeIndex),
}

// ---------------------------------------------------------------------------
// Value types
// ---------------------------------------------------------------------------

#[derive(Clone, Debug, PartialEq, Eq, Hash)]
pub struct Action {
    script: String,
}

impl Action {
    pub fn new(script: impl Into<String>) -> Self {
        Self {
            script: script.into(),
        }
    }

    pub fn script(&self) -> &str {
        &self.script
    }
}

impl fmt::Display for Action {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.script)
    }
}

#[derive(Clone, Debug, PartialEq, Eq, Hash)]
pub struct Guard {
    script: String,
}

impl Guard {
    pub fn new(script: impl Into<String>) -> Self {
        Self {
            script: script.into(),
        }
    }

    pub fn script(&self) -> &str {
        &self.script
    }

    pub fn has_script(&self) -> bool {
        !self.script.is_empty()
    }
}

impl fmt::Display for Guard {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.script)
    }
}

#[derive(Clone, Debug, PartialEq, Eq, Hash)]
pub struct Requirement {
    key: String,
}

impl Requirement {
    pub fn new(key: impl Into<String>) -> Self {
        Self { key: key.into() }
    }

    pub fn key(&self) -> &str {
        &self.key
    }
}

impl fmt::Display for Requirement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.key)
    }
}

// ---------------------------------------------------------------------------
// Enums
// ---------------------------------------------------------------------------

#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub enum ExecutionStatus {
    NotExecuted,
    Executing,
    Completed,
    Failed,
}

#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub enum RequirementStatus {
    NotCovered,
    Passed,
    Failed,
}

#[cfg(test)]
mod tests;
