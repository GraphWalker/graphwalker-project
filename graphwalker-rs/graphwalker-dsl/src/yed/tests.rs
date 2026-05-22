use crate::yed::*;
use graphwalker_core::model::{Action, Guard, Requirement};

// ---------------------------------------------------------------------------
// Vertex label parsing
// ---------------------------------------------------------------------------

#[test]
fn vertex_simple_name() {
    let label = parse_vertex_label("v_Login").unwrap();
    assert_eq!(label.names, vec!["v_Login"]);
    assert!(!label.blocked);
    assert!(!label.is_start);
    assert!(label.shared_state.is_none());
    assert!(label.actions.is_empty());
    assert!(label.requirements.is_empty());
}

#[test]
fn vertex_multiple_names() {
    let label = parse_vertex_label("v_A; v_B").unwrap();
    assert_eq!(label.names, vec!["v_A", "v_B"]);
}

#[test]
fn vertex_shared_state() {
    let label = parse_vertex_label("v_Login\nSHARED:LOGGED_IN").unwrap();
    assert_eq!(label.names, vec!["v_Login"]);
    assert_eq!(label.shared_state, Some("LOGGED_IN".to_string()));
}

#[test]
fn vertex_shared_state_case_insensitive() {
    let label = parse_vertex_label("v_X\nShared: MY_STATE").unwrap();
    assert_eq!(label.shared_state, Some("MY_STATE".to_string()));
}

#[test]
fn vertex_blocked() {
    let label = parse_vertex_label("v_A\nBLOCKED").unwrap();
    assert!(label.blocked);
}

#[test]
fn vertex_blocked_case_insensitive() {
    let label = parse_vertex_label("v_A\nblocked").unwrap();
    assert!(label.blocked);
}

#[test]
fn vertex_start() {
    let label = parse_vertex_label("v_Start\nSTART").unwrap();
    assert!(label.is_start);
}

#[test]
fn vertex_init_actions() {
    let label = parse_vertex_label("v_A\nINIT: x = 0; y = 1").unwrap();
    assert_eq!(
        label.actions,
        vec![Action::new("x = 0"), Action::new("y = 1")]
    );
}

#[test]
fn vertex_requirements() {
    let label = parse_vertex_label("v_A\nREQTAG:REQ001,REQ002").unwrap();
    assert_eq!(
        label.requirements,
        vec![Requirement::new("REQ001"), Requirement::new("REQ002")]
    );
}

#[test]
fn vertex_full_label() {
    let input = "v_Login\nSHARED:LOGGED_IN\nINIT: count = 0\nREQTAG:REQ001\nSTART";
    let label = parse_vertex_label(input).unwrap();
    assert_eq!(label.names, vec!["v_Login"]);
    assert_eq!(label.shared_state, Some("LOGGED_IN".to_string()));
    assert_eq!(label.actions, vec![Action::new("count = 0")]);
    assert_eq!(label.requirements, vec![Requirement::new("REQ001")]);
    assert!(label.is_start);
}

#[test]
fn vertex_empty_label() {
    let label = parse_vertex_label("").unwrap();
    assert!(label.names.is_empty());
}

#[test]
fn vertex_whitespace_only() {
    let label = parse_vertex_label("  \n  \n  ").unwrap();
    assert!(label.names.is_empty());
}

// ---------------------------------------------------------------------------
// Edge label parsing
// ---------------------------------------------------------------------------

#[test]
fn edge_simple_name() {
    let label = parse_edge_label("e_Login").unwrap();
    assert_eq!(label.names, vec!["e_Login"]);
    assert!(label.guard.is_none());
    assert!(label.actions.is_empty());
    assert!(!label.blocked);
    assert!(label.weight.is_none());
    assert!(label.dependency.is_none());
}

#[test]
fn edge_multiple_names() {
    let label = parse_edge_label("e_A; e_B").unwrap();
    assert_eq!(label.names, vec!["e_A", "e_B"]);
}

#[test]
fn edge_guard() {
    let label = parse_edge_label("e_Click\n[isLoggedIn == true]").unwrap();
    assert_eq!(label.names, vec!["e_Click"]);
    assert_eq!(label.guard, Some(Guard::new("isLoggedIn == true")));
}

#[test]
fn edge_nested_brackets_guard() {
    let label = parse_edge_label("[arr[0] > 5]").unwrap();
    assert_eq!(label.guard, Some(Guard::new("arr[0] > 5")));
}

#[test]
fn edge_actions() {
    let label = parse_edge_label("e_Click\n/ x = x + 1; y = 0").unwrap();
    assert_eq!(
        label.actions,
        vec![Action::new("x = x + 1"), Action::new("y = 0")]
    );
}

#[test]
fn edge_blocked() {
    let label = parse_edge_label("e_A\nBLOCKED").unwrap();
    assert!(label.blocked);
}

#[test]
fn edge_requirements() {
    let label = parse_edge_label("e_A\nREQTAG:REQ003").unwrap();
    assert_eq!(label.requirements, vec![Requirement::new("REQ003")]);
}

#[test]
fn edge_weight() {
    let label = parse_edge_label("e_A\nWEIGHT=0.75").unwrap();
    assert_eq!(label.weight, Some(0.75));
}

#[test]
fn edge_dependency() {
    let label = parse_edge_label("e_A\nDEPENDENCY=50").unwrap();
    assert_eq!(label.dependency, Some(50));
}

#[test]
fn edge_full_label() {
    let input =
        "e_Submit\n[count > 0]\n/ count = count - 1\nREQTAG:REQ005\nWEIGHT=0.5\nDEPENDENCY=80";
    let label = parse_edge_label(input).unwrap();
    assert_eq!(label.names, vec!["e_Submit"]);
    assert_eq!(label.guard, Some(Guard::new("count > 0")));
    assert_eq!(label.actions, vec![Action::new("count = count - 1")]);
    assert_eq!(label.requirements, vec![Requirement::new("REQ005")]);
    assert_eq!(label.weight, Some(0.5));
    assert_eq!(label.dependency, Some(80));
}

#[test]
fn edge_empty_label() {
    let label = parse_edge_label("").unwrap();
    assert!(label.names.is_empty());
}

// ---------------------------------------------------------------------------
// Error cases
// ---------------------------------------------------------------------------

#[test]
fn edge_invalid_weight() {
    let err = parse_edge_label("e_A\nWEIGHT=abc").unwrap_err();
    assert!(matches!(err, YedParseError::InvalidWeight(_)));
}

#[test]
fn edge_invalid_dependency() {
    let err = parse_edge_label("e_A\nDEPENDENCY=xyz").unwrap_err();
    assert!(matches!(err, YedParseError::InvalidDependency(_)));
}

#[test]
fn edge_unmatched_bracket() {
    let err = parse_edge_label("[guard without closing").unwrap_err();
    assert!(matches!(err, YedParseError::UnmatchedBracket));
}

#[test]
fn display_errors() {
    assert_eq!(
        YedParseError::InvalidWeight("abc".into()).to_string(),
        "Invalid weight: abc"
    );
    assert_eq!(
        YedParseError::InvalidDependency("xyz".into()).to_string(),
        "Invalid dependency: xyz"
    );
    assert_eq!(
        YedParseError::UnmatchedBracket.to_string(),
        "Unmatched bracket in guard"
    );
}
