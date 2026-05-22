use std::io::Write;

use clap::Args as ClapArgs;
use rand::prelude::*;

use graphwalker_core::machine::Machine;
use graphwalker_core::model::ElementIndex;

use super::{element_name, load_models, load_models_plain, prepare_entries_with_seed, CliResult};

#[derive(ClapArgs)]
pub struct Args {
    /// Model file and generator pairs: -m <file> <generator>
    #[arg(short, long = "model", num_args = 2, action = clap::ArgAction::Append)]
    pub model: Vec<String>,

    /// Model file with embedded generator (JSON)
    #[arg(short = 'g', long = "gw")]
    pub gw: Option<String>,

    /// Print verbose output (JSON with data)
    #[arg(short = 'o', long)]
    pub verbose: bool,

    /// Print unvisited elements
    #[arg(short, long)]
    pub unvisited: bool,

    /// Start element name
    #[arg(short = 'e', long = "start-element")]
    pub start_element: Option<String>,

    /// Seed for random generator (0 = no seed)
    #[arg(short = 's', long, default_value_t = 0)]
    pub seed: u64,
}

pub fn run(args: Args) -> CliResult {
    if args.model.is_empty() && args.gw.is_none() {
        return Err("Either --model (-m) or --gw (-g) is required for offline mode".into());
    }

    let mut contexts = if let Some(ref gw_file) = args.gw {
        load_models_plain(std::slice::from_ref(gw_file))?
    } else {
        load_models(&args.model)?
    };

    if let Some(ref start_name) = args.start_element {
        set_start_by_name(&mut contexts, start_name)?;
    }

    let seed = if args.seed != 0 {
        args.seed
    } else {
        rand::thread_rng().gen()
    };
    let entries = prepare_entries_with_seed(contexts, Some(seed))?;
    let mut machine = Machine::new_with_seed(entries, seed)?;
    machine.set_record_path(false);

    let stdout = std::io::stdout();
    let mut out = std::io::BufWriter::new(stdout.lock());

    while machine.has_next_step() {
        machine.get_next_step()?;

        let ctx_idx = machine.current_context_index();
        let ctx = machine.context(ctx_idx);
        let element = ctx.current_element().unwrap();
        let name = element_name(ctx, element);

        let mut json = serde_json::json!({
            "currentElementName": name,
        });

        if args.verbose {
            json["data"] = serde_json::json!(ctx.data());
        }

        if args.unvisited {
            let model = ctx.model();
            let all = model.all_elements();
            let unvisited: Vec<_> = all.iter().filter(|e| !ctx.is_visited(**e)).collect();

            json["numberOfElements"] = serde_json::json!(all.len());
            json["numberOfUnvisitedElements"] = serde_json::json!(unvisited.len());

            let unvisited_elements: Vec<_> = unvisited
                .iter()
                .map(|e| {
                    let elem_name = element_name(ctx, **e);
                    let mut obj = serde_json::json!({"elementName": elem_name});
                    if args.verbose {
                        let elem_id = match **e {
                            ElementIndex::Vertex(vi) => model.vertex(vi).id(),
                            ElementIndex::Edge(ei) => model.edge(ei).id(),
                        };
                        obj["elementId"] = serde_json::json!(elem_id);
                    }
                    obj
                })
                .collect();
            json["unvisitedElements"] = serde_json::json!(unvisited_elements);
        }

        writeln!(out, "{}", json)?;
    }

    Ok(())
}

fn set_start_by_name(
    contexts: &mut [graphwalker_io::ModelContext],
    name: &str,
) -> Result<(), Box<dyn std::error::Error>> {
    for ctx in contexts.iter_mut() {
        let elements = ctx.model.find_elements(name);
        if !elements.is_empty() {
            ctx.start_element_id = Some(match elements[0] {
                graphwalker_core::model::ElementIndex::Vertex(vi) => {
                    ctx.model.vertex(vi).id().to_string()
                }
                graphwalker_core::model::ElementIndex::Edge(ei) => {
                    ctx.model.edge(ei).id().to_string()
                }
            });
            return Ok(());
        }
    }
    Err(format!("Start element '{}' not found", name).into())
}
