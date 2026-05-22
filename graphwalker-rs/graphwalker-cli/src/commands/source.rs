use std::collections::BTreeSet;
use std::path::Path;

use clap::Args as ClapArgs;

use super::CliResult;

#[derive(ClapArgs)]
pub struct Args {
    /// Input model file and template file
    #[arg(short, long, num_args = 2)]
    pub input: Vec<String>,
}

pub fn run(args: Args) -> CliResult {
    if args.input.len() != 2 {
        return Err("--input requires exactly 2 arguments: <model-file> <template-file>".into());
    }

    let model_path = Path::new(&args.input[0]);
    let template_path = Path::new(&args.input[1]);

    let contexts = graphwalker_io::read_model(model_path)?;
    let template = std::fs::read_to_string(template_path)?;

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

    let (header, body, footer) = parse_template(&template)?;

    print!("{}", header);
    for name in &names {
        print!("{}", body.replace("{LABEL}", name));
    }
    print!("{}", footer);

    Ok(())
}

fn parse_template(template: &str) -> Result<(&str, &str, &str), Box<dyn std::error::Error>> {
    let header_start = template
        .find("HEADER<{{")
        .ok_or("Template missing HEADER<{{")?;
    let header_content_start = header_start + "HEADER<{{".len();
    let header_end = template[header_content_start..]
        .find("}}>HEADER")
        .ok_or("Template missing }}>HEADER")?
        + header_content_start;
    let header = &template[header_content_start..header_end];

    let body_start = header_end + "}}>HEADER".len();

    let footer_marker = template[body_start..]
        .find("FOOTER<{{")
        .ok_or("Template missing FOOTER<{{")?
        + body_start;
    let body = &template[body_start..footer_marker];

    let footer_content_start = footer_marker + "FOOTER<{{".len();
    let footer_end = template[footer_content_start..]
        .find("}}>FOOTER")
        .ok_or("Template missing }}>FOOTER")?
        + footer_content_start;
    let footer = &template[footer_content_start..footer_end];

    Ok((header, body, footer))
}
