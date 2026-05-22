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
    let mut reqs = BTreeSet::new();

    for ctx in &contexts {
        for vertex in ctx.model.vertices() {
            for req in vertex.requirements() {
                let key = req.key();
                if !key.is_empty() {
                    reqs.insert(key.to_string());
                }
            }
        }
        for edge in ctx.model.edges() {
            for req in edge.requirements() {
                let key = req.key();
                if !key.is_empty() {
                    reqs.insert(key.to_string());
                }
            }
        }
    }

    for req in &reqs {
        println!("{}", req);
    }

    Ok(())
}
