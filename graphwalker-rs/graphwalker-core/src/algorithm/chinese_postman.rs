use std::collections::{HashMap, VecDeque};

use crate::model::{EdgeIndex, ElementIndex, RuntimeModel, VertexIndex};

use super::AlgorithmError;

pub struct ChinesePostmanResult {
    pub path: Vec<ElementIndex>,
    pub warnings: Vec<String>,
}

pub fn chinese_postman_path(
    model: &RuntimeModel,
    start: VertexIndex,
) -> Result<ChinesePostmanResult, AlgorithmError> {
    let num_vertices = model.vertices().len();
    let num_edges = model.edges().len();

    if num_edges == 0 {
        return Ok(ChinesePostmanResult {
            path: Vec::new(),
            warnings: Vec::new(),
        });
    }

    let mut warnings = collect_warnings(model);
    let mut adj = build_adjacency_list(model, &mut warnings);

    let has_edges = !adj[start.0].is_empty()
        || adj
            .iter()
            .any(|edges| edges.iter().any(|&(_, t)| t == start));
    if !has_edges {
        return Err(AlgorithmError::NotStronglyConnected(format!(
            "Start vertex '{}' is not connected to any edges",
            model
                .vertex(start)
                .name()
                .unwrap_or(model.vertex(start).id())
        )));
    }

    check_strong_connectivity(model, &adj, num_vertices, start)?;

    let mut in_degree = vec![0usize; num_vertices];
    let mut out_degree = vec![0usize; num_vertices];
    for (v, edges) in adj.iter().enumerate() {
        out_degree[v] = edges.len();
        for &(_, target) in edges {
            in_degree[target.0] += 1;
        }
    }

    let excess: Vec<i32> = (0..num_vertices)
        .map(|v| in_degree[v] as i32 - out_degree[v] as i32)
        .collect();

    let is_eulerian = excess.iter().all(|&e| e == 0);

    if !is_eulerian {
        augment_graph(&mut adj, &excess, num_vertices)?;
    }

    let path = hierholzer(&mut adj, start.0);

    Ok(ChinesePostmanResult { path, warnings })
}

fn collect_warnings(model: &RuntimeModel) -> Vec<String> {
    let mut warnings = Vec::new();

    for edge in model.edges() {
        let name = edge.name().unwrap_or(edge.id());

        if edge.has_guard() {
            warnings.push(format!(
                "Edge '{}' has a guard which will be ignored",
                name
            ));
        }
        if edge.has_actions() {
            warnings.push(format!(
                "Edge '{}' has actions which will be ignored",
                name
            ));
        }
        if edge.weight() != 0.0 && edge.weight() != 1.0 {
            warnings.push(format!(
                "Edge '{}' has weight {} which will be treated as 1",
                name,
                edge.weight()
            ));
        }
    }

    for vertex in model.vertices() {
        if vertex.has_actions() {
            let name = vertex.name().unwrap_or(vertex.id());
            warnings.push(format!(
                "Vertex '{}' has actions which will be ignored",
                name
            ));
        }
    }

    warnings
}

fn build_adjacency_list(
    model: &RuntimeModel,
    warnings: &mut Vec<String>,
) -> Vec<Vec<(EdgeIndex, VertexIndex)>> {
    let num_vertices = model.vertices().len();
    let mut adj: Vec<Vec<(EdgeIndex, VertexIndex)>> = vec![Vec::new(); num_vertices];

    for (i, edge) in model.edges().iter().enumerate() {
        let ei = EdgeIndex(i);

        let target = match edge.target_vertex() {
            Some(t) => t,
            None => {
                warnings.push(format!(
                    "Edge '{}' has no target vertex and will be skipped",
                    edge.name().unwrap_or(edge.id())
                ));
                continue;
            }
        };

        let source = match edge.source_vertex() {
            Some(s) => s,
            None => {
                let target_name = model
                    .vertex(target)
                    .name()
                    .unwrap_or(model.vertex(target).id());
                warnings.push(format!(
                    "Edge '{}' has no source vertex; using target vertex '{}' as source",
                    edge.name().unwrap_or(edge.id()),
                    target_name
                ));
                target
            }
        };

        adj[source.0].push((ei, target));
    }

    adj
}

fn check_strong_connectivity(
    model: &RuntimeModel,
    adj: &[Vec<(EdgeIndex, VertexIndex)>],
    num_vertices: usize,
    start: VertexIndex,
) -> Result<(), AlgorithmError> {
    let mut participating = vec![false; num_vertices];
    for (v, edges) in adj.iter().enumerate() {
        if !edges.is_empty() {
            participating[v] = true;
        }
        for &(_, target) in edges {
            participating[target.0] = true;
        }
    }

    let mut forward: Vec<Vec<usize>> = vec![Vec::new(); num_vertices];
    let mut reverse: Vec<Vec<usize>> = vec![Vec::new(); num_vertices];
    for (v, edges) in adj.iter().enumerate() {
        for &(_, target) in edges {
            forward[v].push(target.0);
            reverse[target.0].push(v);
        }
    }

    let forward_reachable = dfs_reachable(&forward, start.0, num_vertices);
    let reverse_reachable = dfs_reachable(&reverse, start.0, num_vertices);

    let mut unreachable = Vec::new();
    for v in 0..num_vertices {
        if participating[v] && (!forward_reachable[v] || !reverse_reachable[v]) {
            unreachable.push(v);
        }
    }

    if !unreachable.is_empty() {
        let start_name = model
            .vertex(start)
            .name()
            .unwrap_or(model.vertex(start).id());
        let unreachable_names: Vec<String> = unreachable
            .iter()
            .map(|&v| {
                let vi = VertexIndex(v);
                model
                    .vertex(vi)
                    .name()
                    .unwrap_or(model.vertex(vi).id())
                    .to_string()
            })
            .collect();

        let msg = format!(
            "Graph is not strongly connected. The following vertices are not \
             bidirectionally reachable from '{}': {}. \
             Consider adding edges to connect these vertices.",
            start_name,
            unreachable_names.join(", ")
        );

        return Err(AlgorithmError::NotStronglyConnected(msg));
    }

    Ok(())
}

fn dfs_reachable(neighbors: &[Vec<usize>], start: usize, num_vertices: usize) -> Vec<bool> {
    let mut visited = vec![false; num_vertices];
    let mut stack = vec![start];
    while let Some(v) = stack.pop() {
        if visited[v] {
            continue;
        }
        visited[v] = true;
        for &u in &neighbors[v] {
            if !visited[u] {
                stack.push(u);
            }
        }
    }
    visited
}

fn augment_graph(
    adj: &mut [Vec<(EdgeIndex, VertexIndex)>],
    excess: &[i32],
    num_vertices: usize,
) -> Result<(), AlgorithmError> {
    let mut supply_units: Vec<usize> = Vec::new();
    let mut demand_units: Vec<usize> = Vec::new();

    for (v, &e) in excess.iter().enumerate() {
        if e > 0 {
            for _ in 0..e {
                supply_units.push(v);
            }
        } else if e < 0 {
            for _ in 0..(-e) {
                demand_units.push(v);
            }
        }
    }

    debug_assert_eq!(supply_units.len(), demand_units.len());

    let n = supply_units.len();
    if n == 0 {
        return Ok(());
    }

    let unique_supply: Vec<usize> = {
        let mut v = supply_units.clone();
        v.sort();
        v.dedup();
        v
    };

    let mut bfs_cache: HashMap<usize, BfsResult> = HashMap::new();
    for &v in &unique_supply {
        bfs_cache.insert(v, vertex_bfs(adj, v, num_vertices));
    }

    let mut cost = vec![vec![0i32; n]; n];
    for (i, &sv) in supply_units.iter().enumerate() {
        let bfs_result = &bfs_cache[&sv];
        for (j, &dv) in demand_units.iter().enumerate() {
            let dist = bfs_result.dist[dv];
            cost[i][j] = if dist < 0 { i32::MAX / 2 } else { dist };
        }
    }

    let assignment = hungarian(&cost);

    for (i, &j) in assignment.iter().enumerate() {
        let sv = supply_units[i];
        let dv = demand_units[j];
        if sv == dv {
            continue;
        }

        let bfs_result = &bfs_cache[&sv];
        let path = reconstruct_path(bfs_result, sv, dv);

        for (src, ei, tgt) in path {
            adj[src].push((ei, VertexIndex(tgt)));
        }
    }

    Ok(())
}

struct BfsResult {
    dist: Vec<i32>,
    pred: Vec<Option<(usize, EdgeIndex)>>,
}

fn vertex_bfs(
    adj: &[Vec<(EdgeIndex, VertexIndex)>],
    start: usize,
    num_vertices: usize,
) -> BfsResult {
    let mut dist = vec![-1i32; num_vertices];
    let mut pred: Vec<Option<(usize, EdgeIndex)>> = vec![None; num_vertices];
    let mut queue = VecDeque::new();

    dist[start] = 0;
    queue.push_back(start);

    while let Some(v) = queue.pop_front() {
        for &(ei, target) in &adj[v] {
            if dist[target.0] == -1 {
                dist[target.0] = dist[v] + 1;
                pred[target.0] = Some((v, ei));
                queue.push_back(target.0);
            }
        }
    }

    BfsResult { dist, pred }
}

fn reconstruct_path(
    bfs_result: &BfsResult,
    from: usize,
    to: usize,
) -> Vec<(usize, EdgeIndex, usize)> {
    let mut path = Vec::new();
    let mut current = to;
    while current != from {
        let (prev, edge) = bfs_result.pred[current].unwrap();
        path.push((prev, edge, current));
        current = prev;
    }
    path.reverse();
    path
}

// O(n^3) Kuhn-Munkres algorithm for min-cost perfect matching.
fn hungarian(cost: &[Vec<i32>]) -> Vec<usize> {
    let n = cost.len();
    if n == 0 {
        return Vec::new();
    }

    let inf = i32::MAX / 2;

    let mut u = vec![0i32; n + 1];
    let mut v = vec![0i32; n + 1];
    let mut p = vec![0usize; n + 1];
    let mut way = vec![0usize; n + 1];

    for i in 1..=n {
        p[0] = i;
        let mut j0 = 0usize;
        let mut minv = vec![inf; n + 1];
        let mut used = vec![false; n + 1];

        loop {
            used[j0] = true;
            let i0 = p[j0];
            let mut delta = inf;
            let mut j1 = 0usize;

            for j in 1..=n {
                if !used[j] {
                    let cur = cost[i0 - 1][j - 1] - u[i0] - v[j];
                    if cur < minv[j] {
                        minv[j] = cur;
                        way[j] = j0;
                    }
                    if minv[j] < delta {
                        delta = minv[j];
                        j1 = j;
                    }
                }
            }

            for j in 0..=n {
                if used[j] {
                    u[p[j]] += delta;
                    v[j] -= delta;
                } else {
                    minv[j] -= delta;
                }
            }

            j0 = j1;
            if p[j0] == 0 {
                break;
            }
        }

        loop {
            let j1 = way[j0];
            p[j0] = p[j1];
            j0 = j1;
            if j0 == 0 {
                break;
            }
        }
    }

    let mut assignment = vec![0usize; n];
    for j in 1..=n {
        if p[j] > 0 {
            assignment[p[j] - 1] = j - 1;
        }
    }

    assignment
}

fn hierholzer(adj: &mut [Vec<(EdgeIndex, VertexIndex)>], start: usize) -> Vec<ElementIndex> {
    let mut vertex_stack: Vec<usize> = vec![start];
    let mut edge_stack: Vec<EdgeIndex> = Vec::new();
    let mut circuit_vertices: Vec<usize> = Vec::new();
    let mut circuit_edges: Vec<EdgeIndex> = Vec::new();

    loop {
        let v = *vertex_stack.last().unwrap();
        if let Some((ei, u)) = adj[v].pop() {
            vertex_stack.push(u.0);
            edge_stack.push(ei);
        } else {
            vertex_stack.pop();
            circuit_vertices.push(v);
            if let Some(e) = edge_stack.pop() {
                circuit_edges.push(e);
            }
            if vertex_stack.is_empty() {
                break;
            }
        }
    }

    circuit_vertices.reverse();
    circuit_edges.reverse();

    let mut path = Vec::with_capacity(circuit_edges.len() * 2);
    for i in 0..circuit_edges.len() {
        path.push(ElementIndex::Edge(circuit_edges[i]));
        path.push(ElementIndex::Vertex(VertexIndex(circuit_vertices[i + 1])));
    }

    path
}
