use crate::algorithm::*;
use crate::model::*;

fn triangle_model() -> RuntimeModel {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");
    let v_c = VertexBuilder::new().id("vc").name("C");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .name("e_AB")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_bc")
            .name("e_BC")
            .source_vertex(v_b.clone())
            .target_vertex(v_c.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ca")
            .name("e_CA")
            .source_vertex(v_c.clone())
            .target_vertex(v_a.clone()),
    );
    mb.build()
}

fn linear_model() -> RuntimeModel {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");
    let v_c = VertexBuilder::new().id("vc").name("C");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_bc")
            .source_vertex(v_b.clone())
            .target_vertex(v_c.clone()),
    );
    mb.build()
}

// ---------------------------------------------------------------------------
// FloydWarshall tests
// ---------------------------------------------------------------------------

#[test]
fn floyd_warshall_self_distance_is_zero() {
    let model = triangle_model();
    let fw = FloydWarshall::new(&model);

    let va = ElementIndex::Vertex(VertexIndex(0));
    assert_eq!(fw.shortest_distance(va, va), 0);
}

#[test]
fn floyd_warshall_adjacent_distance() {
    let model = triangle_model();
    let fw = FloydWarshall::new(&model);

    let va = ElementIndex::Vertex(VertexIndex(0));
    let e_ab = ElementIndex::Edge(EdgeIndex(0));
    let vb = ElementIndex::Vertex(VertexIndex(1));

    assert_eq!(fw.shortest_distance(va, e_ab), 1);
    assert_eq!(fw.shortest_distance(e_ab, vb), 1);
    assert_eq!(fw.shortest_distance(va, vb), 2);
}

#[test]
fn floyd_warshall_cycle_distances() {
    let model = triangle_model();
    let fw = FloydWarshall::new(&model);

    let va = ElementIndex::Vertex(VertexIndex(0));
    let vb = ElementIndex::Vertex(VertexIndex(1));
    let vc = ElementIndex::Vertex(VertexIndex(2));

    assert_eq!(fw.shortest_distance(va, vb), 2);
    assert_eq!(fw.shortest_distance(vb, vc), 2);
    assert_eq!(fw.shortest_distance(vc, va), 2);
    assert_eq!(fw.shortest_distance(va, vc), 4);
    assert_eq!(fw.shortest_distance(vb, va), 4);
}

#[test]
fn floyd_warshall_maximum_distance() {
    let model = triangle_model();
    let fw = FloydWarshall::new(&model);

    let va = ElementIndex::Vertex(VertexIndex(0));
    let max = fw.maximum_distance(va);
    assert!(max > 0);
}

#[test]
fn floyd_warshall_unreachable() {
    let model = linear_model();
    let fw = FloydWarshall::new(&model);

    let vc = ElementIndex::Vertex(VertexIndex(2));
    let va = ElementIndex::Vertex(VertexIndex(0));

    let dist = fw.shortest_distance(vc, va);
    assert!(dist >= i32::MAX / 2);
}

// ---------------------------------------------------------------------------
// A* tests
// ---------------------------------------------------------------------------

#[test]
fn astar_same_element() {
    let model = triangle_model();
    let fw = FloydWarshall::new(&model);

    let va = ElementIndex::Vertex(VertexIndex(0));
    let result = astar_next_element(&fw, va, va, &[]);
    assert_eq!(result, Some(va));
}

#[test]
fn astar_picks_closest_neighbor() {
    let model = triangle_model();
    let fw = FloydWarshall::new(&model);

    let va = ElementIndex::Vertex(VertexIndex(0));
    let vb = ElementIndex::Vertex(VertexIndex(1));
    let e_ab = ElementIndex::Edge(EdgeIndex(0));

    let neighbors = model.next_elements(va);
    let result = astar_next_element(&fw, va, vb, &neighbors);
    assert_eq!(result, Some(e_ab));
}

#[test]
fn astar_no_neighbors() {
    let model = triangle_model();
    let fw = FloydWarshall::new(&model);

    let va = ElementIndex::Vertex(VertexIndex(0));
    let vb = ElementIndex::Vertex(VertexIndex(1));

    let result = astar_next_element(&fw, va, vb, &[]);
    assert_eq!(result, None);
}

// ---------------------------------------------------------------------------
// Eulerian tests
// ---------------------------------------------------------------------------

#[test]
fn eulerian_cycle_is_eulerian() {
    let model = triangle_model();
    assert_eq!(eulerian_type(&model), EulerianType::Eulerian);
}

#[test]
fn linear_is_semi_eulerian() {
    let model = linear_model();
    assert_eq!(eulerian_type(&model), EulerianType::SemiEulerian);
}

#[test]
fn not_eulerian_graph() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");
    let v_c = VertexBuilder::new().id("vc").name("C");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .source_vertex(v_a.clone())
            .target_vertex(v_c.clone()),
    );
    let model = mb.build();

    assert_eq!(eulerian_type(&model), EulerianType::NotEulerian);
}

// ---------------------------------------------------------------------------
// Euler path tests
// ---------------------------------------------------------------------------

#[test]
fn euler_path_on_cycle() {
    let model = triangle_model();
    let va = ElementIndex::Vertex(VertexIndex(0));

    let path = euler_path(&model, va).unwrap();
    assert_eq!(path.len(), 6);

    for (i, elem) in path.iter().enumerate() {
        if i % 2 == 0 {
            assert!(matches!(elem, ElementIndex::Edge(_)));
        } else {
            assert!(matches!(elem, ElementIndex::Vertex(_)));
        }
    }
}

#[test]
fn euler_path_not_eulerian_errors() {
    let v_a = VertexBuilder::new().id("va");
    let v_b = VertexBuilder::new().id("vb");
    let v_c = VertexBuilder::new().id("vc");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .source_vertex(v_a.clone())
            .target_vertex(v_c.clone()),
    );
    let model = mb.build();

    let result = euler_path(&model, ElementIndex::Vertex(VertexIndex(0)));
    assert_eq!(result, Err(AlgorithmError::NotEulerian));
}

#[test]
fn euler_path_on_linear() {
    let model = linear_model();
    let va = ElementIndex::Vertex(VertexIndex(0));

    let path = euler_path(&model, va).unwrap();
    assert_eq!(path.len(), 4);
}

#[test]
fn euler_path_starting_from_edge() {
    let model = triangle_model();
    let e0 = ElementIndex::Edge(EdgeIndex(0));

    let path = euler_path(&model, e0).unwrap();
    assert!(!path.is_empty());
    assert!(matches!(path[0], ElementIndex::Vertex(_)));
}

// ---------------------------------------------------------------------------
// Chinese Postman tests
// ---------------------------------------------------------------------------

#[test]
fn chinese_postman_eulerian_triangle() {
    let model = triangle_model();
    let result = chinese_postman_path(&model, VertexIndex(0)).unwrap();

    assert_eq!(result.path.len(), 6);

    for (i, elem) in result.path.iter().enumerate() {
        if i % 2 == 0 {
            assert!(matches!(elem, ElementIndex::Edge(_)));
        } else {
            assert!(matches!(elem, ElementIndex::Vertex(_)));
        }
    }

    let mut visited_edges = std::collections::HashSet::new();
    for elem in &result.path {
        if let ElementIndex::Edge(ei) = elem {
            visited_edges.insert(*ei);
        }
    }
    assert_eq!(visited_edges.len(), 3);
}

#[test]
fn chinese_postman_unbalanced_graph() {
    // A→B, B→C, C→A, A→B (two edges from A to B)
    // out: A=2, B=1, C=1, in: A=1, B=2, C=1
    // excess (in-out): A=-1, B=+1, C=0
    // Need to augment: shortest path from B to A (B→C→A, cost 2)
    // Total path: 4 original + 2 augmented = 6 edges = 12 elements
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");
    let v_c = VertexBuilder::new().id("vc").name("C");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e1")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e2")
            .source_vertex(v_b.clone())
            .target_vertex(v_c.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e3")
            .source_vertex(v_c.clone())
            .target_vertex(v_a.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e4")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    let model = mb.build();

    let result = chinese_postman_path(&model, VertexIndex(0)).unwrap();

    assert_eq!(result.path.len(), 12);

    let mut edge_visits = std::collections::HashMap::new();
    for elem in &result.path {
        if let ElementIndex::Edge(ei) = elem {
            *edge_visits.entry(*ei).or_insert(0) += 1;
        }
    }
    for i in 0..4 {
        assert!(
            edge_visits.contains_key(&EdgeIndex(i)),
            "Edge {} should be visited at least once",
            i
        );
    }
}

#[test]
fn chinese_postman_not_strongly_connected() {
    // A→B, A→C (no way back)
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");
    let v_c = VertexBuilder::new().id("vc").name("C");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e1")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e2")
            .source_vertex(v_a.clone())
            .target_vertex(v_c.clone()),
    );
    let model = mb.build();

    let result = chinese_postman_path(&model, VertexIndex(0));
    assert!(matches!(
        result,
        Err(AlgorithmError::NotStronglyConnected(_))
    ));
}

#[test]
fn chinese_postman_start_edge_warning() {
    // Edge with no source vertex (start edge) → self-loop on target
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_start")
            .name("e_start")
            .target_vertex(v_a.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone()),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    let model = mb.build();

    let result = chinese_postman_path(&model, VertexIndex(0)).unwrap();

    assert!(
        result
            .warnings
            .iter()
            .any(|w| w.contains("no source vertex")),
        "Should warn about missing source vertex"
    );

    let mut visited_edges = std::collections::HashSet::new();
    for elem in &result.path {
        if let ElementIndex::Edge(ei) = elem {
            visited_edges.insert(*ei);
        }
    }
    assert_eq!(visited_edges.len(), 3, "All 3 edges should be visited");
}

#[test]
fn chinese_postman_weight_warning() {
    let v_a = VertexBuilder::new().id("va").name("A");
    let v_b = VertexBuilder::new().id("vb").name("B");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ab")
            .name("e_AB")
            .source_vertex(v_a.clone())
            .target_vertex(v_b.clone())
            .weight(0.5),
    );
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_ba")
            .source_vertex(v_b.clone())
            .target_vertex(v_a.clone()),
    );
    let model = mb.build();

    let result = chinese_postman_path(&model, VertexIndex(0)).unwrap();

    assert!(
        result
            .warnings
            .iter()
            .any(|w| w.contains("weight") && w.contains("0.5")),
        "Should warn about non-unit weight"
    );
}

#[test]
fn chinese_postman_empty_graph() {
    let mut mb = ModelBuilder::new();
    mb.add_vertex(VertexBuilder::new().id("va").name("A"));
    let model = mb.build();

    let result = chinese_postman_path(&model, VertexIndex(0)).unwrap();
    assert!(result.path.is_empty());
}

#[test]
fn chinese_postman_self_loop() {
    let v_a = VertexBuilder::new().id("va").name("A");

    let mut mb = ModelBuilder::new();
    mb.add_edge(
        EdgeBuilder::new()
            .id("e_aa")
            .source_vertex(v_a.clone())
            .target_vertex(v_a.clone()),
    );
    let model = mb.build();

    let result = chinese_postman_path(&model, VertexIndex(0)).unwrap();
    assert_eq!(result.path.len(), 2);
    assert!(matches!(result.path[0], ElementIndex::Edge(EdgeIndex(0))));
    assert!(matches!(
        result.path[1],
        ElementIndex::Vertex(VertexIndex(0))
    ));
}

#[test]
fn chinese_postman_returns_to_start() {
    let model = triangle_model();
    let result = chinese_postman_path(&model, VertexIndex(0)).unwrap();

    let last = result.path.last().unwrap();
    assert_eq!(*last, ElementIndex::Vertex(VertexIndex(0)));
}
