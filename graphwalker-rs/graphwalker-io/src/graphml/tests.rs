use std::path::PathBuf;

use crate::graphml::*;

fn fixture(name: &str) -> PathBuf {
    PathBuf::from(env!("CARGO_MANIFEST_DIR"))
        .join("tests/fixtures/graphml")
        .join(name)
}

#[test]
fn read_guards_graphml() {
    let contexts = read_graphml_file(&fixture("Guards.graphml")).unwrap();
    assert_eq!(contexts.len(), 1);

    let model = &contexts[0].model;
    assert_eq!(model.vertices().len(), 3);
    assert_eq!(model.edges().len(), 2);

    let start_v = model.vertices().iter().find(|v| v.name() == Some("Start"));
    assert!(start_v.is_some());

    let end_v = model.vertices().iter().find(|v| v.name() == Some("End"));
    assert!(end_v.is_some());

    for edge in model.edges() {
        assert!(edge.has_guard());
    }
}

#[test]
fn read_shared_state_login_graphml() {
    let contexts = read_graphml_file(&fixture("SharedStateLogin.graphml")).unwrap();
    assert_eq!(contexts.len(), 1);

    let model = &contexts[0].model;

    let v_client = model
        .vertices()
        .iter()
        .find(|v| v.name() == Some("v_ClientNotRunning"));
    assert!(v_client.is_some());
    assert_eq!(v_client.unwrap().shared_state(), Some("CLIENT_NOT_RUNNNG"));

    let v_browse = model
        .vertices()
        .iter()
        .find(|v| v.name() == Some("v_Browse"));
    assert!(v_browse.is_some());
    assert_eq!(v_browse.unwrap().shared_state(), Some("LOGGED_IN"));

    let e_init = model.edges().iter().find(|e| e.name() == Some("e_Init"));
    assert!(e_init.is_some());
    assert!(e_init.unwrap().has_actions());

    let e_start_client = model
        .edges()
        .iter()
        .find(|e| e.name() == Some("e_StartClient") && e.has_guard());
    assert!(e_start_client.is_some());

    let e_valid = model
        .edges()
        .iter()
        .find(|e| e.name() == Some("e_ValidPremiumCredentials"));
    assert!(e_valid.is_some());
    assert!(e_valid.unwrap().has_actions());
}

#[test]
fn graphml_start_detection() {
    let contexts = read_graphml_file(&fixture("SharedStateLogin.graphml")).unwrap();
    assert!(contexts[0].start_element_id.is_some());
}

#[test]
fn parse_inline_edge_with_guard_and_action() {
    let xml = r#"<?xml version="1.0" encoding="UTF-8"?>
<graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:y="http://www.yworks.com/xml/graphml">
  <key for="node" id="d6" yfiles.type="nodegraphics"/>
  <key for="edge" id="d10" yfiles.type="edgegraphics"/>
  <graph edgedefault="directed" id="G">
    <node id="n0">
      <data key="d6"><y:ShapeNode><y:NodeLabel>v_A</y:NodeLabel></y:ShapeNode></data>
    </node>
    <node id="n1">
      <data key="d6"><y:ShapeNode><y:NodeLabel>v_B</y:NodeLabel></y:ShapeNode></data>
    </node>
    <edge id="e0" source="n0" target="n1">
      <data key="d10"><y:PolyLineEdge><y:EdgeLabel>e_Go[x &gt; 0]/x=x-1;</y:EdgeLabel></y:PolyLineEdge></data>
    </edge>
  </graph>
</graphml>"#;

    let contexts = read_graphml_string(xml).unwrap();
    let model = &contexts[0].model;

    assert_eq!(model.vertices().len(), 2);
    assert_eq!(model.edges().len(), 1);

    let edge = &model.edges()[0];
    assert_eq!(edge.name(), Some("e_Go"));
    assert!(edge.has_guard());
    assert_eq!(edge.guard().unwrap().script(), "x > 0");
    assert_eq!(edge.actions().len(), 1);
    assert_eq!(edge.actions()[0].script(), "x=x-1");
}

#[test]
fn parse_vertex_with_shared_state() {
    let xml = r#"<?xml version="1.0" encoding="UTF-8"?>
<graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:y="http://www.yworks.com/xml/graphml">
  <key for="node" id="d6" yfiles.type="nodegraphics"/>
  <key for="edge" id="d10" yfiles.type="edgegraphics"/>
  <graph edgedefault="directed" id="G">
    <node id="n0">
      <data key="d6"><y:ShapeNode><y:NodeLabel>v_Login
SHARED:LOGGED_IN</y:NodeLabel></y:ShapeNode></data>
    </node>
    <node id="n1">
      <data key="d6"><y:ShapeNode><y:NodeLabel>v_Home</y:NodeLabel></y:ShapeNode></data>
    </node>
    <edge id="e0" source="n0" target="n1">
      <data key="d10"><y:PolyLineEdge><y:EdgeLabel>e_Navigate</y:EdgeLabel></y:PolyLineEdge></data>
    </edge>
  </graph>
</graphml>"#;

    let contexts = read_graphml_string(xml).unwrap();
    let model = &contexts[0].model;

    let v_login = model
        .vertices()
        .iter()
        .find(|v| v.name() == Some("v_Login"))
        .unwrap();
    assert_eq!(v_login.shared_state(), Some("LOGGED_IN"));
}

#[test]
fn parse_empty_edge_label() {
    let xml = r#"<?xml version="1.0" encoding="UTF-8"?>
<graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:y="http://www.yworks.com/xml/graphml">
  <key for="node" id="d6" yfiles.type="nodegraphics"/>
  <key for="edge" id="d10" yfiles.type="edgegraphics"/>
  <graph edgedefault="directed" id="G">
    <node id="n0">
      <data key="d6"><y:ShapeNode><y:NodeLabel>v_A</y:NodeLabel></y:ShapeNode></data>
    </node>
    <node id="n1">
      <data key="d6"><y:ShapeNode><y:NodeLabel>v_B</y:NodeLabel></y:ShapeNode></data>
    </node>
    <edge id="e0" source="n0" target="n1">
      <data key="d10"><y:PolyLineEdge><y:EdgeLabel></y:EdgeLabel></y:PolyLineEdge></data>
    </edge>
  </graph>
</graphml>"#;

    let contexts = read_graphml_string(xml).unwrap();
    let model = &contexts[0].model;
    assert_eq!(model.edges().len(), 1);
    assert!(!model.edges()[0].has_name());
}
