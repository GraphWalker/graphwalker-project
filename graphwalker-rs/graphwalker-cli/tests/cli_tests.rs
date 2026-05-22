use assert_cmd::Command;
use predicates::prelude::*;

fn gw() -> Command {
    Command::cargo_bin("graphwalker").unwrap()
}

fn fixture(path: &str) -> String {
    format!("tests/fixtures/{}", path)
}

// ---------------------------------------------------------------------------
// Help & version
// ---------------------------------------------------------------------------

#[test]
fn help_shows_usage() {
    gw().arg("--help")
        .assert()
        .success()
        .stdout(predicate::str::contains("Model-based testing tool"));
}

#[test]
fn version_shows_version() {
    gw().arg("--version")
        .assert()
        .success()
        .stdout(predicate::str::contains("graphwalker"));
}

// ---------------------------------------------------------------------------
// Offline — correct models
// ---------------------------------------------------------------------------

#[test]
fn offline_simplest_model() {
    let out = gw()
        .args(["offline", "-m", &fixture("json/SimplestModel.json"), "random(vertex_coverage(100))"])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    assert!(stdout.contains("\"currentElementName\""));
    assert!(stdout.contains("e_Init"));
    assert!(stdout.contains("v_Start"));
}

#[test]
fn offline_small_model_edge_coverage() {
    let out = gw()
        .args(["offline", "-m", &fixture("json/SmallModel.json"), "random(edge_coverage(100))"])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let lines: Vec<&str> = stdout.lines().collect();
    assert!(lines.len() >= 4, "Expected at least 4 steps for 4 edges, got {}", lines.len());

    let names: Vec<String> = lines
        .iter()
        .filter_map(|l| {
            serde_json::from_str::<serde_json::Value>(l)
                .ok()
                .and_then(|v| v["currentElementName"].as_str().map(String::from))
        })
        .collect();
    assert!(names.contains(&"e_FirstAction".to_string()));
    assert!(names.contains(&"v_VerifySomeAction".to_string()));
    assert!(names.contains(&"v_VerifySomeOtherAction".to_string()));
}

#[test]
fn offline_with_seed_is_deterministic() {
    let run = |seed: u64| -> String {
        let out = gw()
            .args([
                "offline", "-s", &seed.to_string(),
                "-m", &fixture("json/SmallModel.json"), "random(edge_coverage(100))",
            ])
            .assert()
            .success();
        String::from_utf8(out.get_output().stdout.clone()).unwrap()
    };

    let first = run(42);
    let second = run(42);
    assert_eq!(first, second, "Same seed should produce identical output");

    let different = run(99);
    assert_ne!(first, different, "Different seeds should produce different output");
}

#[test]
fn offline_verbose_includes_data() {
    let out = gw()
        .args([
            "offline", "-o",
            "-m", &fixture("json/SmallModel.json"), "random(edge_coverage(100))",
        ])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    for line in stdout.lines() {
        let v: serde_json::Value = serde_json::from_str(line).expect("Each line should be valid JSON");
        assert!(v.get("data").is_some(), "Verbose mode should include 'data' field: {}", line);
    }
}

#[test]
fn offline_unvisited_shows_counts() {
    let out = gw()
        .args([
            "offline", "-u",
            "-m", &fixture("json/SmallModel.json"), "random(edge_coverage(100))",
        ])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let first_line: serde_json::Value = serde_json::from_str(stdout.lines().next().unwrap()).unwrap();
    assert!(first_line.get("numberOfElements").is_some());
    assert!(first_line.get("numberOfUnvisitedElements").is_some());
    assert!(first_line.get("unvisitedElements").is_some());

    let last_line: serde_json::Value = serde_json::from_str(stdout.lines().last().unwrap()).unwrap();
    let unvisited = last_line["numberOfUnvisitedElements"].as_u64().unwrap();
    assert_eq!(unvisited, 0, "Last step should have 0 unvisited elements");
}

#[test]
fn offline_with_gw_flag() {
    gw().args(["offline", "-g", &fixture("json/SmallModel.json")])
        .assert()
        .success()
        .stdout(predicate::str::contains("currentElementName"));
}

#[test]
fn offline_custom_start_element() {
    let out = gw()
        .args([
            "offline", "-e", "v_VerifySomeOtherAction",
            "-m", &fixture("json/SmallModel.json"), "random(vertex_coverage(100))",
        ])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let first: serde_json::Value = serde_json::from_str(stdout.lines().next().unwrap()).unwrap();
    assert_eq!(
        first["currentElementName"].as_str().unwrap(),
        "v_VerifySomeOtherAction",
        "First element should be the custom start element"
    );
}

#[test]
fn offline_login_model_with_guards_and_actions() {
    let out = gw()
        .args(["offline", "-g", &fixture("json/Login.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let names: Vec<String> = stdout
        .lines()
        .filter_map(|l| {
            serde_json::from_str::<serde_json::Value>(l)
                .ok()
                .and_then(|v| v["currentElementName"].as_str().map(String::from))
        })
        .collect();

    assert!(names.contains(&"e_Init".to_string()));
    assert!(names.contains(&"v_ClientNotRunning".to_string()));
    assert!(names.contains(&"e_StartClient".to_string()));
    assert!(names.contains(&"v_LoginPrompted".to_string()));
    assert!(names.contains(&"e_ValidPremiumCredentials".to_string()));
    assert!(names.contains(&"v_Browse".to_string()));
}

#[test]
fn offline_multi_model_shared_state() {
    let out = gw()
        .args(["offline", "-g", &fixture("json/MultiModelSharedState.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let names: Vec<String> = stdout
        .lines()
        .filter_map(|l| {
            serde_json::from_str::<serde_json::Value>(l)
                .ok()
                .and_then(|v| v["currentElementName"].as_str().map(String::from))
        })
        .collect();

    assert!(names.contains(&"e_StartA".to_string()), "Should visit ModelA's start edge");
    assert!(names.contains(&"v_A1".to_string()), "Should visit ModelA vertex");
    assert!(names.contains(&"e_Explore".to_string()), "Should cross into ModelB via shared state");
    assert!(names.contains(&"v_B1".to_string()), "Should visit ModelB vertex");
}

#[test]
fn offline_a_star_reached_vertex() {
    let out = gw()
        .args([
            "offline",
            "-m", &fixture("json/SmallModel.json"), "a_star(reached_vertex(v_VerifySomeOtherAction))",
        ])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let names: Vec<String> = stdout
        .lines()
        .filter_map(|l| {
            serde_json::from_str::<serde_json::Value>(l)
                .ok()
                .and_then(|v| v["currentElementName"].as_str().map(String::from))
        })
        .collect();

    assert_eq!(
        names.last().unwrap(),
        "v_VerifySomeOtherAction",
        "A* should end at the target vertex"
    );
}

#[test]
fn offline_a_star_reached_edge() {
    let out = gw()
        .args([
            "offline",
            "-m", &fixture("json/SmallModel.json"), "a_star(reached_edge(e_AnotherAction))",
        ])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let names: Vec<String> = stdout
        .lines()
        .filter_map(|l| {
            serde_json::from_str::<serde_json::Value>(l)
                .ok()
                .and_then(|v| v["currentElementName"].as_str().map(String::from))
        })
        .collect();

    assert!(names.contains(&"e_AnotherAction".to_string()));
}

#[test]
fn offline_vertex_coverage() {
    let out = gw()
        .args([
            "offline",
            "-m", &fixture("json/SmallModel.json"), "random(vertex_coverage(100))",
        ])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let names: Vec<String> = stdout
        .lines()
        .filter_map(|l| {
            serde_json::from_str::<serde_json::Value>(l)
                .ok()
                .and_then(|v| v["currentElementName"].as_str().map(String::from))
        })
        .collect();

    assert!(names.contains(&"v_VerifySomeAction".to_string()));
    assert!(names.contains(&"v_VerifySomeOtherAction".to_string()));
}

// ---------------------------------------------------------------------------
// Offline — error cases
// ---------------------------------------------------------------------------

#[test]
fn offline_nonexistent_file() {
    gw().args(["offline", "-m", "nonexistent.json", "random(edge_coverage(100))"])
        .assert()
        .failure();
}

#[test]
fn offline_no_model_args() {
    gw().args(["offline"])
        .assert()
        .failure();
}

#[test]
fn offline_invalid_generator() {
    gw().args(["offline", "-m", &fixture("json/SmallModel.json"), "not_a_generator(100)"])
        .assert()
        .failure();
}

#[test]
fn offline_start_element_not_found() {
    gw().args([
        "offline", "-e", "v_DoesNotExist",
        "-m", &fixture("json/SmallModel.json"), "random(edge_coverage(100))",
    ])
    .assert()
    .failure()
    .stderr(predicate::str::contains("not found"));
}

#[test]
fn offline_invalid_json() {
    std::fs::write("tests/fixtures/json/_invalid.json", "{ not valid json }").unwrap();
    gw().args(["offline", "-g", "tests/fixtures/json/_invalid.json"])
        .assert()
        .failure();
    let _ = std::fs::remove_file("tests/fixtures/json/_invalid.json");
}

// ---------------------------------------------------------------------------
// Check
// ---------------------------------------------------------------------------

#[test]
fn check_valid_model() {
    gw().args(["check", "-m", &fixture("json/SmallModel.json"), "random(edge_coverage(100))"])
        .assert()
        .success()
        .stdout(predicate::str::contains("No issues found"));
}

#[test]
fn check_valid_model_with_gw_flag() {
    gw().args(["check", "-g", &fixture("json/SmallModel.json")])
        .assert()
        .success()
        .stdout(predicate::str::contains("No issues found"));
}

#[test]
fn check_login_model_reports_nameless_vertex() {
    gw().args(["check", "-g", &fixture("json/Login.json")])
        .assert()
        .failure()
        .stdout(predicate::str::contains("Name of vertex cannot be null"));
}

#[test]
fn check_multi_model() {
    gw().args(["check", "-g", &fixture("json/MultiModelSharedState.json")])
        .assert()
        .success()
        .stdout(predicate::str::contains("No issues found"));
}

#[test]
fn check_statistics_single_model() {
    let out = gw()
        .args(["check", "-g", &fixture("json/SmallModel.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    assert!(stdout.contains("Statistics:"));
    assert!(stdout.contains("Model: Small model"));
    assert!(stdout.contains("Unique edges:    3"));
    assert!(stdout.contains("Unique vertices: 2"));
    assert!(stdout.contains("Edge instances:    4"));
    assert!(stdout.contains("Vertex instances:  2"));
    assert!(!stdout.contains("Total:"));
}

#[test]
fn check_statistics_multi_model() {
    let out = gw()
        .args(["check", "-g", &fixture("json/MultiModelSharedState.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    assert!(stdout.contains("Model: ModelA"));
    assert!(stdout.contains("Model: ModelB"));
    assert!(stdout.contains("Total:"));
}

#[test]
fn check_no_model_args() {
    gw().args(["check"])
        .assert()
        .failure();
}

#[test]
fn check_model_no_start_element() {
    gw().args(["check", "-g", &fixture("json/NoStartElement.json")])
        .assert()
        .failure();
}

// ---------------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------------

#[test]
fn methods_small_model() {
    let out = gw()
        .args(["methods", "-m", &fixture("json/SmallModel.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let names: Vec<&str> = stdout.lines().collect();

    assert!(names.contains(&"e_FirstAction"));
    assert!(names.contains(&"e_AnotherAction"));
    assert!(names.contains(&"e_SomeOtherAction"));
    assert!(names.contains(&"v_VerifySomeAction"));
    assert!(names.contains(&"v_VerifySomeOtherAction"));
}

#[test]
fn methods_login_model() {
    let out = gw()
        .args(["methods", "-m", &fixture("json/Login.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let names: Vec<&str> = stdout.lines().collect();

    assert!(names.contains(&"e_Init"));
    assert!(names.contains(&"e_StartClient"));
    assert!(names.contains(&"e_ValidPremiumCredentials"));
    assert!(names.contains(&"e_Logout"));
    assert!(names.contains(&"e_Exit"));
    assert!(names.contains(&"e_ToggleRememberMe"));
    assert!(names.contains(&"e_Close"));
    assert!(names.contains(&"e_InvalidCredentials"));
    assert!(names.contains(&"v_ClientNotRunning"));
    assert!(names.contains(&"v_LoginPrompted"));
    assert!(names.contains(&"v_Browse"));
}

#[test]
fn methods_sorted_alphabetically() {
    let out = gw()
        .args(["methods", "-m", &fixture("json/SmallModel.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let names: Vec<&str> = stdout.lines().collect();
    let mut sorted = names.clone();
    sorted.sort();
    assert_eq!(names, sorted, "Methods output should be sorted alphabetically");
}

#[test]
fn methods_deduplicates() {
    let out = gw()
        .args(["methods", "-m", &fixture("json/SmallModel.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let names: Vec<&str> = stdout.lines().collect();
    let unique_count = names.len();
    let mut deduped = names.clone();
    deduped.sort();
    deduped.dedup();
    assert_eq!(
        unique_count,
        deduped.len(),
        "e_SomeOtherAction appears on two edges but should be listed once"
    );
}

#[test]
fn methods_multi_model() {
    let out = gw()
        .args(["methods", "-m", &fixture("json/MultiModelSharedState.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let names: Vec<&str> = stdout.lines().collect();

    assert!(names.contains(&"e_StartA"));
    assert!(names.contains(&"e_GoToPortal"));
    assert!(names.contains(&"e_Explore"));
    assert!(names.contains(&"e_Return"));
    assert!(names.contains(&"v_A1"));
    assert!(names.contains(&"v_Portal"));
    assert!(names.contains(&"v_B1"));
}

// ---------------------------------------------------------------------------
// Requirements
// ---------------------------------------------------------------------------

#[test]
fn requirements_model_with_requirements() {
    let out = gw()
        .args(["requirements", "-m", &fixture("json/WithRequirements.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let reqs: Vec<&str> = stdout.lines().collect();

    assert!(reqs.contains(&"REQ001"));
    assert!(reqs.contains(&"REQ002"));
    assert!(reqs.contains(&"REQ003"));
    assert!(reqs.contains(&"REQ004"));
    assert!(reqs.contains(&"REQ005"));
    assert_eq!(reqs.len(), 5);
}

#[test]
fn requirements_model_without_requirements() {
    let out = gw()
        .args(["requirements", "-m", &fixture("json/SmallModel.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    assert!(stdout.trim().is_empty(), "Model without requirements should produce empty output");
}

#[test]
fn requirements_sorted() {
    let out = gw()
        .args(["requirements", "-m", &fixture("json/WithRequirements.json")])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let reqs: Vec<&str> = stdout.lines().collect();
    let mut sorted = reqs.clone();
    sorted.sort();
    assert_eq!(reqs, sorted, "Requirements should be sorted");
}

// ---------------------------------------------------------------------------
// Convert
// ---------------------------------------------------------------------------

#[test]
fn convert_graphml_to_json() {
    let out = gw()
        .args(["convert", "--input", &fixture("graphml/Login.graphml"), "--format", "json"])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let parsed: serde_json::Value = serde_json::from_str(&stdout).expect("Output should be valid JSON");
    assert!(parsed.get("models").is_some(), "JSON output should have 'models' key");

    let models = parsed["models"].as_array().unwrap();
    assert!(!models.is_empty(), "Should have at least one model");
    assert!(models[0].get("vertices").is_some());
    assert!(models[0].get("edges").is_some());
}

#[test]
fn convert_json_to_json() {
    let out = gw()
        .args(["convert", "--input", &fixture("json/SmallModel.json"), "--format", "json"])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let parsed: serde_json::Value = serde_json::from_str(&stdout).expect("Output should be valid JSON");
    let models = parsed["models"].as_array().unwrap();
    assert_eq!(models.len(), 1);

    let verts = models[0]["vertices"].as_array().unwrap();
    assert_eq!(verts.len(), 2);
    let edges = models[0]["edges"].as_array().unwrap();
    assert_eq!(edges.len(), 4);
}

#[test]
fn convert_preserves_element_names() {
    let out = gw()
        .args(["convert", "--input", &fixture("json/SmallModel.json"), "--format", "json"])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    assert!(stdout.contains("v_VerifySomeAction"));
    assert!(stdout.contains("v_VerifySomeOtherAction"));
    assert!(stdout.contains("e_FirstAction"));
    assert!(stdout.contains("e_AnotherAction"));
    assert!(stdout.contains("e_SomeOtherAction"));
}

#[test]
fn convert_unsupported_format() {
    gw().args(["convert", "--input", &fixture("json/SmallModel.json"), "--format", "xml"])
        .assert()
        .failure()
        .stderr(predicate::str::contains("Unsupported"));
}

#[test]
fn convert_nonexistent_file() {
    gw().args(["convert", "--input", "nonexistent.json", "--format", "json"])
        .assert()
        .failure();
}

// ---------------------------------------------------------------------------
// Source
// ---------------------------------------------------------------------------

#[test]
fn source_generates_from_template() {
    let out = gw()
        .args(["source", "--input", &fixture("json/SmallModel.json"), "tests/test.template"])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    assert!(stdout.contains("# Generated methods"), "Should contain header");
    assert!(stdout.contains("# End of file"), "Should contain footer");
    assert!(stdout.contains("def v_VerifySomeAction():"));
    assert!(stdout.contains("def v_VerifySomeOtherAction():"));
    assert!(stdout.contains("def e_FirstAction():"));
    assert!(stdout.contains("def e_AnotherAction():"));
    assert!(stdout.contains("def e_SomeOtherAction():"));
}

#[test]
fn source_login_model() {
    let out = gw()
        .args(["source", "--input", &fixture("json/Login.json"), "tests/test.template"])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    assert!(stdout.contains("def e_Init():"));
    assert!(stdout.contains("def e_StartClient():"));
    assert!(stdout.contains("def v_ClientNotRunning():"));
    assert!(stdout.contains("def v_Browse():"));
}

#[test]
fn source_missing_template() {
    gw().args(["source", "--input", &fixture("json/SmallModel.json"), "nonexistent.template"])
        .assert()
        .failure();
}

// ---------------------------------------------------------------------------
// Offline — generator & stop condition combinations
// ---------------------------------------------------------------------------

#[test]
fn offline_random_length() {
    let out = gw()
        .args([
            "offline",
            "-m", &fixture("json/SmallModel.json"), "random(length(10))",
        ])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let count = stdout.lines().count();
    assert_eq!(count, 10, "length(10) should produce exactly 10 steps, got {}", count);
}

#[test]
fn offline_quick_random() {
    gw().args([
        "offline",
        "-m", &fixture("json/SmallModel.json"), "quick_random(edge_coverage(100))",
    ])
    .assert()
    .success()
    .stdout(predicate::str::contains("currentElementName"));
}

#[test]
fn offline_weighted_random() {
    gw().args([
        "offline",
        "-m", &fixture("json/SmallModel.json"), "weighted_random(edge_coverage(100))",
    ])
    .assert()
    .success()
    .stdout(predicate::str::contains("currentElementName"));
}

// ---------------------------------------------------------------------------
// Offline — NewYorkStreetSweeper
// ---------------------------------------------------------------------------

#[test]
fn offline_new_york_street_sweeper() {
    let out = gw()
        .args([
            "offline",
            "-m", &fixture("json/SmallModel.json"), "new_york_street_sweeper()",
        ])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let lines: Vec<&str> = stdout.lines().collect();
    assert!(lines.len() >= 8, "Should visit all 4 edges (at least 8 steps), got {}", lines.len());

    let names: Vec<String> = lines
        .iter()
        .filter_map(|l| serde_json::from_str::<serde_json::Value>(l).ok())
        .filter_map(|v| v["currentElementName"].as_str().map(String::from))
        .collect();

    assert!(names.contains(&"e_FirstAction".to_string()), "Should visit e_FirstAction");
    assert!(names.contains(&"e_AnotherAction".to_string()), "Should visit e_AnotherAction");
    assert!(names.contains(&"e_SomeOtherAction".to_string()), "Should visit e_SomeOtherAction");
    assert!(names.contains(&"v_VerifySomeAction".to_string()), "Should visit v_VerifySomeAction");
    assert!(names.contains(&"v_VerifySomeOtherAction".to_string()), "Should visit v_VerifySomeOtherAction");
}

#[test]
fn offline_new_york_street_sweeper_camel_case() {
    gw().args([
        "offline",
        "-m", &fixture("json/SmallModel.json"), "newyorkstreetsweeper()",
    ])
    .assert()
    .success()
    .stdout(predicate::str::contains("currentElementName"));
}

// ---------------------------------------------------------------------------
// Edge cases
// ---------------------------------------------------------------------------

#[test]
fn offline_model_with_no_start_element_fails() {
    gw().args([
        "offline",
        "-m", &fixture("json/NoStartElement.json"), "random(edge_coverage(100))",
    ])
    .assert()
    .failure();
}

#[test]
fn offline_model_no_start_with_custom_start_succeeds() {
    gw().args([
        "offline", "-e", "v_A",
        "-m", &fixture("json/NoStartElement.json"), "random(edge_coverage(100))",
    ])
    .assert()
    .success()
    .stdout(predicate::str::contains("v_A"));
}

#[test]
fn no_subcommand_shows_help() {
    gw().assert()
        .failure()
        .stderr(predicate::str::contains("Usage"));
}

#[test]
fn offline_verbose_and_unvisited_combined() {
    let out = gw()
        .args([
            "offline", "-o", "-u",
            "-m", &fixture("json/SmallModel.json"), "random(edge_coverage(100))",
        ])
        .assert()
        .success();

    let stdout = String::from_utf8(out.get_output().stdout.clone()).unwrap();
    let first: serde_json::Value = serde_json::from_str(stdout.lines().next().unwrap()).unwrap();
    assert!(first.get("data").is_some(), "Should have data from -o");
    assert!(first.get("numberOfElements").is_some(), "Should have unvisited info from -u");
}
