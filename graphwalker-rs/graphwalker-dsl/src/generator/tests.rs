use crate::generator::*;

#[test]
fn parse_random_edge_coverage() {
    let gen = parse_generator("random(edge_coverage(100))").unwrap();
    assert_eq!(gen.to_string(), "RandomPath(EdgeCoverage(100))");
}

#[test]
fn parse_random_vertex_coverage() {
    let gen = parse_generator("random(vertex_coverage(80))").unwrap();
    assert_eq!(gen.to_string(), "RandomPath(VertexCoverage(80))");
}

#[test]
fn parse_quick_random() {
    let gen = parse_generator("quick_random(edge_coverage(100))").unwrap();
    assert_eq!(gen.to_string(), "QuickRandomPath(EdgeCoverage(100))");
}

#[test]
fn parse_weighted_random() {
    let gen = parse_generator("weighted_random(vertex_coverage(50))").unwrap();
    assert_eq!(gen.to_string(), "WeightedRandomPath(VertexCoverage(50))");
}

#[test]
fn parse_a_star_reached_vertex() {
    let gen = parse_generator("a_star(reached_vertex(v_SomeVertex))").unwrap();
    assert_eq!(gen.to_string(), "AStarPath(ReachedVertex(v_SomeVertex))");
}

#[test]
fn parse_a_star_reached_edge() {
    let gen = parse_generator("a_star(reached_edge(e_SomeEdge))").unwrap();
    assert_eq!(gen.to_string(), "AStarPath(ReachedEdge(e_SomeEdge))");
}

#[test]
fn parse_shortest_all_paths() {
    let gen = parse_generator("shortest_all_paths(edge_coverage(100))").unwrap();
    assert_eq!(gen.to_string(), "ShortestAllPaths(EdgeCoverage(100))");
}

#[test]
fn parse_predefined_path() {
    let gen = parse_generator("predefined_path(predefined_path)").unwrap();
    assert_eq!(gen.to_string(), "PredefinedPath(PredefinedPath)");
}

#[test]
fn parse_length_condition() {
    let gen = parse_generator("random(length(42))").unwrap();
    assert_eq!(gen.to_string(), "RandomPath(Length(42))");
}

#[test]
fn parse_never_condition() {
    let gen = parse_generator("random(never)").unwrap();
    assert_eq!(gen.to_string(), "RandomPath(Never)");
}

#[test]
fn parse_time_duration() {
    let gen = parse_generator("random(time_duration(30))").unwrap();
    assert_eq!(gen.to_string(), "RandomPath(TimeDuration(30s))");
}

#[test]
fn parse_requirement_coverage() {
    let gen = parse_generator("random(requirement_coverage(100))").unwrap();
    assert_eq!(gen.to_string(), "RandomPath(RequirementCoverage(100))");
}

#[test]
fn parse_dependency_edge_coverage() {
    let gen = parse_generator("random(dependency_edge_coverage(75))").unwrap();
    assert_eq!(gen.to_string(), "RandomPath(DependencyEdgeCoverage(75))");
}

#[test]
fn parse_reached_shared_state() {
    let gen = parse_generator("a_star(reached_shared_state(LOGGED_IN))").unwrap();
    assert_eq!(gen.to_string(), "AStarPath(ReachedSharedState(LOGGED_IN))");
}

// ---------------------------------------------------------------------------
// Case insensitivity and name aliases
// ---------------------------------------------------------------------------

#[test]
fn parse_camel_case_generator() {
    let gen = parse_generator("randomPath(edgeCoverage(100))").unwrap();
    assert_eq!(gen.to_string(), "RandomPath(EdgeCoverage(100))");
}

#[test]
fn parse_camel_case_quick_random() {
    let gen = parse_generator("quickRandomPath(vertexCoverage(100))").unwrap();
    assert_eq!(gen.to_string(), "QuickRandomPath(VertexCoverage(100))");
}

#[test]
fn parse_astarpath_alias() {
    let gen = parse_generator("astarpath(reachedvertex(target))").unwrap();
    assert_eq!(gen.to_string(), "AStarPath(ReachedVertex(target))");
}

#[test]
fn parse_shortestallpaths_alias() {
    let gen = parse_generator("shortestallpaths(edgecoverage(100))").unwrap();
    assert_eq!(gen.to_string(), "ShortestAllPaths(EdgeCoverage(100))");
}

#[test]
fn parse_predefinedpath_alias() {
    let gen = parse_generator("predefinedpath(predefinedpath)").unwrap();
    assert_eq!(gen.to_string(), "PredefinedPath(PredefinedPath)");
}

// ---------------------------------------------------------------------------
// AND / OR combinations
// ---------------------------------------------------------------------------

#[test]
fn parse_and_combination() {
    let gen = parse_generator("random(edge_coverage(100) and reached_vertex(target))").unwrap();
    assert_eq!(
        gen.to_string(),
        "RandomPath((EdgeCoverage(100) AND ReachedVertex(target)))"
    );
}

#[test]
fn parse_or_combination() {
    let gen = parse_generator("random(edge_coverage(100) or reached_vertex(target))").unwrap();
    assert_eq!(
        gen.to_string(),
        "RandomPath((EdgeCoverage(100) OR ReachedVertex(target)))"
    );
}

#[test]
fn parse_and_symbol() {
    let gen = parse_generator("random(edge_coverage(100) && length(50))").unwrap();
    assert_eq!(
        gen.to_string(),
        "RandomPath((EdgeCoverage(100) AND Length(50)))"
    );
}

#[test]
fn parse_or_symbol() {
    let gen = parse_generator("random(edge_coverage(100) || length(50))").unwrap();
    assert_eq!(
        gen.to_string(),
        "RandomPath((EdgeCoverage(100) OR Length(50)))"
    );
}

// ---------------------------------------------------------------------------
// Combined generators (multiple in one string)
// ---------------------------------------------------------------------------

#[test]
fn parse_combined_generators() {
    let gen = parse_generator("random(edge_coverage(100)) a_star(reached_vertex(v_End))").unwrap();
    assert_eq!(
        gen.to_string(),
        "CombinedPath(RandomPath(EdgeCoverage(100)), AStarPath(ReachedVertex(v_End)))"
    );
}

#[test]
fn parse_three_combined_generators() {
    let gen = parse_generator(
        "random(length(10)) quick_random(edge_coverage(100)) a_star(reached_vertex(v_X))",
    )
    .unwrap();
    assert_eq!(
        gen.to_string(),
        "CombinedPath(RandomPath(Length(10)), QuickRandomPath(EdgeCoverage(100)), AStarPath(ReachedVertex(v_X)))"
    );
}

// ---------------------------------------------------------------------------
// Whitespace handling
// ---------------------------------------------------------------------------

#[test]
fn parse_with_extra_whitespace() {
    let gen = parse_generator("  random (  edge_coverage ( 100 )  )  ").unwrap();
    assert_eq!(gen.to_string(), "RandomPath(EdgeCoverage(100))");
}

// ---------------------------------------------------------------------------
// Error cases
// ---------------------------------------------------------------------------

#[test]
fn error_unknown_generator() {
    let err = parse_generator("foobar(edge_coverage(100))").unwrap_err();
    assert!(matches!(err, DslError::UnknownGenerator(_)));
}

#[test]
fn error_unknown_condition() {
    let err = parse_generator("random(foobar(100))").unwrap_err();
    assert!(matches!(err, DslError::UnknownCondition(_)));
}

#[test]
fn error_empty_input() {
    let err = parse_generator("").unwrap_err();
    assert!(matches!(err, DslError::UnexpectedEnd));
}

#[test]
fn error_missing_paren() {
    let err = parse_generator("random(edge_coverage(100)").unwrap_err();
    assert!(matches!(err, DslError::Expected(_)));
}

// ---------------------------------------------------------------------------
// NewYorkStreetSweeper
// ---------------------------------------------------------------------------

#[test]
fn parse_new_york_street_sweeper() {
    let gen = parse_generator("new_york_street_sweeper()").unwrap();
    assert_eq!(gen.to_string(), "NewYorkStreetSweeper()");
}

#[test]
fn parse_newyorkstreetsweeper_alias() {
    let gen = parse_generator("newyorkstreetsweeper()").unwrap();
    assert_eq!(gen.to_string(), "NewYorkStreetSweeper()");
}

#[test]
fn parse_new_york_street_sweeper_with_whitespace() {
    let gen = parse_generator("  new_york_street_sweeper ( ) ").unwrap();
    assert_eq!(gen.to_string(), "NewYorkStreetSweeper()");
}

// ---------------------------------------------------------------------------
// Error display
// ---------------------------------------------------------------------------

#[test]
fn display_errors() {
    assert_eq!(
        DslError::UnexpectedEnd.to_string(),
        "Unexpected end of input"
    );
    assert_eq!(
        DslError::UnknownGenerator("foo".into()).to_string(),
        "Unknown generator: foo"
    );
    assert_eq!(
        DslError::UnknownCondition("bar".into()).to_string(),
        "Unknown stop condition: bar"
    );
    assert_eq!(
        DslError::InvalidNumber("abc".into()).to_string(),
        "Invalid number: abc"
    );
    assert_eq!(DslError::Expected("')'".into()).to_string(), "Expected ')'");
}
