use graphwalker_core::model::{Action, Guard, Requirement};

#[derive(Clone, Debug, Default, PartialEq)]
pub struct VertexLabel {
    pub names: Vec<String>,
    pub shared_state: Option<String>,
    pub blocked: bool,
    pub actions: Vec<Action>,
    pub requirements: Vec<Requirement>,
    pub is_start: bool,
}

#[derive(Clone, Debug, Default, PartialEq)]
pub struct EdgeLabel {
    pub names: Vec<String>,
    pub guard: Option<Guard>,
    pub actions: Vec<Action>,
    pub blocked: bool,
    pub requirements: Vec<Requirement>,
    pub weight: Option<f64>,
    pub dependency: Option<u32>,
}

#[derive(Clone, Debug, PartialEq, Eq)]
pub enum YedParseError {
    InvalidWeight(String),
    InvalidDependency(String),
    UnmatchedBracket,
}

impl std::fmt::Display for YedParseError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::InvalidWeight(s) => write!(f, "Invalid weight: {}", s),
            Self::InvalidDependency(s) => write!(f, "Invalid dependency: {}", s),
            Self::UnmatchedBracket => write!(f, "Unmatched bracket in guard"),
        }
    }
}

impl std::error::Error for YedParseError {}

/// Parse a yEd vertex label.
///
/// Format (line-oriented):
/// - Plain text lines are names (semicolon-separated for multiple)
/// - `SHARED:state_name` — shared state portal
/// - `BLOCKED` — marks vertex as blocked
/// - `INIT:script;` — initialization action
/// - `REQTAG:REQ001,REQ002` — requirements (comma-separated)
/// - `START` — marks as start vertex
pub fn parse_vertex_label(input: &str) -> Result<VertexLabel, YedParseError> {
    let mut label = VertexLabel::default();

    for line in input.lines() {
        let trimmed = line.trim();
        if trimmed.is_empty() {
            continue;
        }

        let upper = trimmed.to_ascii_uppercase();

        if upper == "BLOCKED" {
            label.blocked = true;
        } else if upper == "START" {
            label.is_start = true;
        } else if let Some(rest) = strip_prefix_ci(trimmed, "SHARED:") {
            label.shared_state = Some(rest.trim().to_string());
        } else if let Some(rest) = strip_prefix_ci(trimmed, "INIT:") {
            for action_str in rest.split(';') {
                let s = action_str.trim();
                if !s.is_empty() {
                    label.actions.push(Action::new(s));
                }
            }
        } else if let Some(rest) = strip_prefix_ci(trimmed, "REQTAG:") {
            for tag in rest.split(',') {
                let s = tag.trim();
                if !s.is_empty() {
                    label.requirements.push(Requirement::new(s));
                }
            }
        } else {
            for name in trimmed.split(';') {
                let s = name.trim();
                if !s.is_empty() {
                    label.names.push(s.to_string());
                }
            }
        }
    }

    Ok(label)
}

/// Parse a yEd edge label.
///
/// Format (line-oriented):
/// - Plain text lines are names (semicolon-separated for multiple)
/// - `[guard expression]` — guard condition
/// - `/ action;` — action (slash prefix)
/// - `BLOCKED` — marks edge as blocked
/// - `REQTAG:REQ001,REQ002` — requirements
/// - `WEIGHT=0.5` — edge weight (0.0-1.0)
/// - `DEPENDENCY=75` — dependency percentage (0-100)
pub fn parse_edge_label(input: &str) -> Result<EdgeLabel, YedParseError> {
    let mut label = EdgeLabel::default();

    for line in input.lines() {
        let trimmed = line.trim();
        if trimmed.is_empty() {
            continue;
        }

        let upper = trimmed.to_ascii_uppercase();

        if upper == "BLOCKED" {
            label.blocked = true;
        } else if trimmed.starts_with('[') {
            let guard_str = extract_bracketed(trimmed)?;
            label.guard = Some(Guard::new(guard_str));
        } else if let Some(rest) = trimmed.strip_prefix('/') {
            let action_part = rest.trim();
            for action_str in action_part.split(';') {
                let s = action_str.trim();
                if !s.is_empty() {
                    label.actions.push(Action::new(s));
                }
            }
        } else if let Some(rest) = strip_prefix_ci(trimmed, "REQTAG:") {
            for tag in rest.split(',') {
                let s = tag.trim();
                if !s.is_empty() {
                    label.requirements.push(Requirement::new(s));
                }
            }
        } else if let Some(rest) = strip_prefix_ci(trimmed, "WEIGHT=") {
            let w: f64 = rest
                .trim()
                .parse()
                .map_err(|_| YedParseError::InvalidWeight(rest.trim().to_string()))?;
            label.weight = Some(w);
        } else if let Some(rest) = strip_prefix_ci(trimmed, "DEPENDENCY=") {
            let d: u32 = rest
                .trim()
                .parse()
                .map_err(|_| YedParseError::InvalidDependency(rest.trim().to_string()))?;
            label.dependency = Some(d);
        } else {
            for name in trimmed.split(';') {
                let s = name.trim();
                if !s.is_empty() {
                    label.names.push(s.to_string());
                }
            }
        }
    }

    Ok(label)
}

fn strip_prefix_ci<'a>(s: &'a str, prefix: &str) -> Option<&'a str> {
    let upper_s = s[..s.len().min(prefix.len())].to_ascii_uppercase();
    let upper_prefix = prefix.to_ascii_uppercase();
    if upper_s == upper_prefix {
        Some(&s[prefix.len()..])
    } else {
        None
    }
}

fn extract_bracketed(s: &str) -> Result<String, YedParseError> {
    let mut depth = 0;
    let mut start = None;

    for (i, ch) in s.char_indices() {
        match ch {
            '[' => {
                if depth == 0 {
                    start = Some(i + 1);
                }
                depth += 1;
            }
            ']' => {
                depth -= 1;
                if depth == 0 {
                    let begin = start.ok_or(YedParseError::UnmatchedBracket)?;
                    return Ok(s[begin..i].to_string());
                }
            }
            _ => {}
        }
    }

    Err(YedParseError::UnmatchedBracket)
}

#[cfg(test)]
mod tests;
