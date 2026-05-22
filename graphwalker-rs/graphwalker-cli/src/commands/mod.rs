pub mod check;
pub mod convert;
pub mod methods;
pub mod offline;
pub mod online;
pub mod requirements;
pub mod source;

use std::path::Path;

use graphwalker_core::machine::ExecutionContext;
use graphwalker_core::model::ElementIndex;
use graphwalker_dsl::generator::parse_generator;
use graphwalker_io::ModelContext;

pub type CliResult = Result<(), Box<dyn std::error::Error>>;

fn load_models(model_args: &[String]) -> Result<Vec<ModelContext>, Box<dyn std::error::Error>> {
    if !model_args.len().is_multiple_of(2) {
        return Err("--model requires pairs of <file> <generator>".into());
    }

    let mut all_contexts = Vec::new();

    for pair in model_args.chunks(2) {
        let file = &pair[0];
        let generator = &pair[1];
        let path = Path::new(file);

        let mut contexts = graphwalker_io::read_model(path)?;
        for ctx in &mut contexts {
            ctx.generator = Some(generator.clone());
        }
        all_contexts.extend(contexts);
    }

    Ok(all_contexts)
}

fn load_models_plain(
    model_args: &[String],
) -> Result<Vec<ModelContext>, Box<dyn std::error::Error>> {
    let mut all_contexts = Vec::new();
    for file in model_args {
        let path = Path::new(file);
        let contexts = graphwalker_io::read_model(path)?;
        all_contexts.extend(contexts);
    }
    Ok(all_contexts)
}

fn prepare_entries_with_seed(
    contexts: Vec<ModelContext>,
    seed: Option<u64>,
) -> Result<
    Vec<(ExecutionContext, graphwalker_core::generator::PathGenerator)>,
    Box<dyn std::error::Error>,
> {
    let mut entries = Vec::new();

    for ctx in contexts {
        let gen_str = ctx
            .generator
            .as_deref()
            .ok_or("Model has no generator specified")?;
        let generator = parse_generator(gen_str)?;

        let mut exec_ctx = if let Some(s) = seed {
            ExecutionContext::new_with_seed(ctx.model, s)
        } else {
            ExecutionContext::new(ctx.model)
        };

        if let Some(ref start_id) = ctx.start_element_id {
            if let Some(element) = exec_ctx.model().element_by_id(start_id) {
                exec_ctx.set_next_element(Some(element));
            }
        }

        entries.push((exec_ctx, generator));
    }

    Ok(entries)
}

fn element_name(ctx: &ExecutionContext, element: ElementIndex) -> String {
    match element {
        ElementIndex::Vertex(vi) => ctx.model().vertex(vi).name().unwrap_or("").to_string(),
        ElementIndex::Edge(ei) => ctx.model().edge(ei).name().unwrap_or("").to_string(),
    }
}
