use std::collections::BTreeSet;

use clap::Args as ClapArgs;

use super::{load_models_plain, CliResult};

#[derive(ClapArgs)]
pub struct Args {
    /// Model file(s)
    #[arg(short, long = "model", required = true)]
    pub model: Vec<String>,
}

pub fn run(args: Args) -> CliResult {
    let contexts = load_models_plain(&args.model)?;
    let mut names = BTreeSet::new();

    for ctx in &contexts {
        for vertex in ctx.model.vertices() {
            if let Some(name) = vertex.name() {
                if !name.is_empty() {
                    names.insert(name.to_string());
                }
            }
        }
        for edge in ctx.model.edges() {
            if let Some(name) = edge.name() {
                if !name.is_empty() {
                    names.insert(name.to_string());
                }
            }
        }
    }

    for name in &names {
        println!("{}", name);
    }

    Ok(())
}
