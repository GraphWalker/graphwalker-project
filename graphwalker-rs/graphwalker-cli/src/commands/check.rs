use std::collections::HashSet;

use clap::Args as ClapArgs;

use super::{load_models, load_models_plain, CliResult};

#[derive(ClapArgs)]
pub struct Args {
    /// Model file and generator pairs: -m <file> <generator>
    #[arg(short, long = "model", num_args = 2, action = clap::ArgAction::Append)]
    pub model: Vec<String>,

    /// Model file with embedded generator (JSON)
    #[arg(short = 'g', long = "gw")]
    pub gw: Option<String>,
}

pub fn run(args: Args) -> CliResult {
    if args.model.is_empty() && args.gw.is_none() {
        return Err("Either --model (-m) or --gw (-g) is required for check command".into());
    }

    let contexts = if let Some(ref gw_file) = args.gw {
        load_models_plain(std::slice::from_ref(gw_file))?
    } else {
        load_models(&args.model)?
    };
    let issues = graphwalker_model_checker::check_contexts(&contexts);

    if issues.is_empty() {
        println!("No issues found with the model(s).");
    } else {
        for issue in &issues {
            println!("{}", issue);
        }
        return Err(format!("{} issue(s) found", issues.len()).into());
    }

    print_statistics(&contexts);

    Ok(())
}

fn print_statistics(contexts: &[graphwalker_io::ModelContext]) {
    println!();
    println!("Statistics:");

    let mut total_edges = 0usize;
    let mut total_vertices = 0usize;
    let mut total_unique_edges = 0usize;
    let mut total_unique_vertices = 0usize;

    for ctx in contexts {
        let model = &ctx.model;
        let name = model.name().unwrap_or(model.id());

        let num_edges = model.edges().len();
        let num_vertices = model.vertices().len();

        let unique_edge_names: HashSet<&str> = model
            .edges()
            .iter()
            .filter_map(|e| e.name())
            .filter(|n| !n.is_empty())
            .collect();
        let unique_vertex_names: HashSet<&str> = model
            .vertices()
            .iter()
            .filter_map(|v| v.name())
            .filter(|n| !n.is_empty())
            .collect();

        println!("  Model: {}", name);
        println!("    Unique edges:    {}", unique_edge_names.len());
        println!("    Unique vertices: {}", unique_vertex_names.len());
        println!("    Edge instances:    {}", num_edges);
        println!("    Vertex instances:  {}", num_vertices);

        total_edges += num_edges;
        total_vertices += num_vertices;
        total_unique_edges += unique_edge_names.len();
        total_unique_vertices += unique_vertex_names.len();
    }

    if contexts.len() > 1 {
        println!("  Total:");
        println!("    Unique edges:    {}", total_unique_edges);
        println!("    Unique vertices: {}", total_unique_vertices);
        println!("    Edge instances:    {}", total_edges);
        println!("    Vertex instances:  {}", total_vertices);
    }
}
