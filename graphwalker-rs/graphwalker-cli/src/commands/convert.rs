use std::path::Path;

use clap::Args as ClapArgs;

use super::CliResult;

#[derive(ClapArgs)]
pub struct Args {
    /// Input model file
    #[arg(short, long)]
    pub input: String,

    /// Output format: json
    #[arg(short, long, default_value = "json")]
    pub format: String,
}

pub fn run(args: Args) -> CliResult {
    let path = Path::new(&args.input);
    let contexts = graphwalker_io::read_model(path)?;

    match args.format.to_ascii_lowercase().as_str() {
        "json" => {
            let json = graphwalker_io::json::write_json_string(&contexts)?;
            println!("{}", json);
        }
        other => {
            return Err(format!("Unsupported output format: {}", other).into());
        }
    }

    Ok(())
}
