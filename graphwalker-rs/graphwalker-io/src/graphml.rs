use std::collections::HashMap;
use std::path::Path;

use quick_xml::events::Event;
use quick_xml::reader::Reader;

use graphwalker_core::model::{
    Action, EdgeBuilder, Guard, ModelBuilder, Requirement, VertexBuilder,
};
use graphwalker_dsl::yed;

use crate::{IoError, ModelContext};

struct RawNode {
    label: String,
}

struct RawEdge {
    id: String,
    source: String,
    target: String,
    label: String,
}

pub fn read_graphml_string(xml: &str) -> Result<Vec<ModelContext>, IoError> {
    let mut reader = Reader::from_str(xml);

    let mut node_map: HashMap<String, RawNode> = HashMap::new();
    let mut edge_map: HashMap<String, RawEdge> = HashMap::new();

    let mut in_node = false;
    let mut in_edge = false;
    let mut current_node_id = String::new();
    let mut current_edge_id = String::new();
    let mut in_node_label = false;
    let mut in_edge_label = false;
    let mut label_text = String::new();

    let mut buf = Vec::new();
    loop {
        match reader.read_event_into(&mut buf) {
            Ok(Event::Start(ref e)) => {
                let local_name = e.local_name();
                match local_name.as_ref() {
                    b"node" => {
                        in_node = true;
                        current_node_id = extract_attr(e, b"id").unwrap_or_default();
                        node_map.entry(current_node_id.clone()).or_insert(RawNode {
                            label: String::new(),
                        });
                    }
                    b"edge" => {
                        in_edge = true;
                        current_edge_id = extract_attr(e, b"id").unwrap_or_default();
                        let source = extract_attr(e, b"source").unwrap_or_default();
                        let target = extract_attr(e, b"target").unwrap_or_default();
                        edge_map.entry(current_edge_id.clone()).or_insert(RawEdge {
                            id: current_edge_id.clone(),
                            source,
                            target,
                            label: String::new(),
                        });
                    }
                    b"NodeLabel" if in_node => {
                        in_node_label = true;
                        label_text.clear();
                    }
                    b"EdgeLabel" if in_edge => {
                        in_edge_label = true;
                        label_text.clear();
                    }
                    _ => {}
                }
            }
            Ok(Event::Empty(ref e)) => {
                let local_name = e.local_name();
                match local_name.as_ref() {
                    b"node" => {
                        let id = extract_attr(e, b"id").unwrap_or_default();
                        node_map.entry(id.clone()).or_insert(RawNode {
                            label: String::new(),
                        });
                    }
                    b"edge" => {
                        let id = extract_attr(e, b"id").unwrap_or_default();
                        let source = extract_attr(e, b"source").unwrap_or_default();
                        let target = extract_attr(e, b"target").unwrap_or_default();
                        edge_map.entry(id.clone()).or_insert(RawEdge {
                            id,
                            source,
                            target,
                            label: String::new(),
                        });
                    }
                    _ => {}
                }
            }
            Ok(Event::Text(ref e)) if in_node_label || in_edge_label => {
                if let Ok(text) = e.unescape() {
                    label_text.push_str(&text);
                }
            }
            Ok(Event::End(ref e)) => {
                let local_name = e.local_name();
                match local_name.as_ref() {
                    b"node" => {
                        in_node = false;
                    }
                    b"edge" => {
                        in_edge = false;
                    }
                    b"NodeLabel" if in_node_label => {
                        in_node_label = false;
                        let label = label_text.trim().to_string();
                        if !label.is_empty() {
                            if let Some(node) = node_map.get_mut(&current_node_id) {
                                if node.label.is_empty() {
                                    node.label = label;
                                }
                            }
                        }
                    }
                    b"EdgeLabel" if in_edge_label => {
                        in_edge_label = false;
                        let label = label_text.trim().to_string();
                        if !label.is_empty() {
                            if let Some(edge) = edge_map.get_mut(&current_edge_id) {
                                if edge.label.is_empty() {
                                    edge.label = label;
                                }
                            }
                        }
                    }
                    _ => {}
                }
            }
            Ok(Event::Eof) => break,
            Err(e) => return Err(IoError::Xml(e.to_string())),
            _ => {}
        }
        buf.clear();
    }

    build_model_from_raw(node_map, edge_map)
}

fn build_model_from_raw(
    node_map: HashMap<String, RawNode>,
    edge_map: HashMap<String, RawEdge>,
) -> Result<Vec<ModelContext>, IoError> {
    let mut mb = ModelBuilder::new();
    let mut start_element_id: Option<String> = None;

    let mut vertex_builders: HashMap<String, VertexBuilder> = HashMap::new();

    for (id, node) in &node_map {
        let parsed = yed::parse_vertex_label(&node.label)
            .map_err(|e| IoError::Xml(format!("Vertex label parse error: {}", e)))?;

        let name = if !parsed.names.is_empty() {
            parsed.names.first().cloned()
        } else if parsed.is_start && !node.label.is_empty() {
            Some(node.label.clone())
        } else {
            None
        };
        let mut vb = VertexBuilder::new().id(id);
        if let Some(ref n) = name {
            vb = vb.name(n);
        }
        if let Some(ref ss) = parsed.shared_state {
            vb = vb.shared_state(ss);
        }
        for action in &parsed.actions {
            vb = vb.add_action(action.clone());
        }
        for req in &parsed.requirements {
            vb = vb.add_requirement(req.clone());
        }
        if parsed.is_start {
            start_element_id = Some(id.clone());
        }
        vertex_builders.insert(id.clone(), vb);
    }

    for vb in vertex_builders.values() {
        mb.add_vertex(vb.clone());
    }

    for raw_edge in edge_map.values() {
        let parsed = parse_edge_label_inline(&raw_edge.label)
            .map_err(|e| IoError::Xml(format!("Edge label parse error: {}", e)))?;

        let mut eb = EdgeBuilder::new().id(&raw_edge.id);
        if let Some(ref n) = parsed.name {
            eb = eb.name(n);
        }
        if let Some(ref g) = parsed.guard {
            eb = eb.guard(g.clone());
        }
        for action in &parsed.actions {
            eb = eb.add_action(action.clone());
        }
        for req in &parsed.requirements {
            eb = eb.add_requirement(req.clone());
        }
        if let Some(w) = parsed.weight {
            eb = eb.weight(w);
        }
        if let Some(d) = parsed.dependency {
            eb = eb.dependency(d as i32);
        }

        eb = eb.source_vertex(VertexBuilder::new().id(&raw_edge.source));
        eb = eb.target_vertex(VertexBuilder::new().id(&raw_edge.target));

        mb.add_edge(eb);
    }

    let model = mb.build();

    // If no explicit START vertex, check if there's an edge from a "Start" node
    if start_element_id.is_none() {
        for e in model.edges() {
            if let Some(src_vi) = e.source_vertex() {
                let src = model.vertex(src_vi);
                if src.name().is_some_and(|n| n == "Start") {
                    start_element_id = Some(e.id().to_string());
                    break;
                }
            }
        }
    }

    Ok(vec![ModelContext {
        model,
        generator: None,
        start_element_id,
    }])
}

struct ParsedEdge {
    name: Option<String>,
    guard: Option<Guard>,
    actions: Vec<Action>,
    requirements: Vec<Requirement>,
    weight: Option<f64>,
    dependency: Option<u32>,
}

fn parse_edge_label_inline(label: &str) -> Result<ParsedEdge, yed::YedParseError> {
    if label.is_empty() {
        return Ok(ParsedEdge {
            name: None,
            guard: None,
            actions: Vec::new(),
            requirements: Vec::new(),
            weight: None,
            dependency: None,
        });
    }

    let upper = label.to_ascii_uppercase();
    let has_keywords = upper.contains("REQTAG:")
        || upper.contains("WEIGHT=")
        || upper.contains("DEPENDENCY=")
        || upper.contains("BLOCKED");

    if has_keywords {
        let parsed = yed::parse_edge_label(label)?;
        return Ok(ParsedEdge {
            name: parsed.names.first().cloned(),
            guard: parsed.guard,
            actions: parsed.actions,
            requirements: parsed.requirements,
            weight: parsed.weight,
            dependency: parsed.dependency,
        });
    }

    // yEd can wrap inline labels across lines — collapse to single line
    let collapsed = label.replace('\n', " ");
    parse_inline_single_line(&collapsed)
}

fn parse_inline_single_line(label: &str) -> Result<ParsedEdge, yed::YedParseError> {
    let mut name = None;
    let mut guard = None;
    let mut actions = Vec::new();

    let mut remaining = label.trim();

    // Extract guard: [...]
    if let Some(bracket_start) = remaining.find('[') {
        let before = remaining[..bracket_start].trim();
        if !before.is_empty() {
            name = Some(before.to_string());
        }

        let mut depth = 0;
        let mut guard_end = None;
        for (i, ch) in remaining[bracket_start..].char_indices() {
            match ch {
                '[' => depth += 1,
                ']' => {
                    depth -= 1;
                    if depth == 0 {
                        guard_end = Some(bracket_start + i);
                        break;
                    }
                }
                _ => {}
            }
        }

        if let Some(end) = guard_end {
            let guard_str = &remaining[bracket_start + 1..end];
            guard = Some(Guard::new(guard_str));
            remaining = remaining[end + 1..].trim();
        } else {
            return Err(yed::YedParseError::UnmatchedBracket);
        }
    } else if let Some(slash_pos) = remaining.find('/') {
        let before = remaining[..slash_pos].trim();
        if !before.is_empty() {
            name = Some(before.to_string());
        }
        remaining = &remaining[slash_pos..];
    } else {
        if !remaining.is_empty() {
            name = Some(remaining.to_string());
        }
        return Ok(ParsedEdge {
            name,
            guard: None,
            actions: Vec::new(),
            requirements: Vec::new(),
            weight: None,
            dependency: None,
        });
    }

    // Extract actions: /action1;action2;
    if let Some(stripped) = remaining.strip_prefix('/') {
        for action_str in stripped.split(';') {
            let s = action_str.trim();
            if !s.is_empty() {
                actions.push(Action::new(s));
            }
        }
    }

    Ok(ParsedEdge {
        name,
        guard,
        actions,
        requirements: Vec::new(),
        weight: None,
        dependency: None,
    })
}

fn extract_attr(e: &quick_xml::events::BytesStart, attr_name: &[u8]) -> Option<String> {
    for attr in e.attributes().flatten() {
        if attr.key.as_ref() == attr_name {
            return String::from_utf8(attr.value.to_vec()).ok();
        }
    }
    None
}

pub fn read_graphml_file(path: &Path) -> Result<Vec<ModelContext>, IoError> {
    let content = std::fs::read_to_string(path)?;
    read_graphml_string(&content)
}

#[cfg(test)]
mod tests;
