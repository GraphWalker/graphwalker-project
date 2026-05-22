mod commands;

use std::process;

use clap::{Parser, Subcommand};

#[derive(Parser)]
#[command(name = "graphwalker", version, about = "Model-based testing tool")]
struct Cli {
    /// Enable debug logging
    #[arg(long, global = true)]
    debug: bool,

    #[command(subcommand)]
    command: Command,
}

#[derive(Subcommand)]
enum Command {
    /// Generate a test sequence offline
    Offline(commands::offline::Args),
    /// Start an online service (REST or WebSocket)
    Online(commands::online::Args),
    /// List all method names in the model(s)
    Methods(commands::methods::Args),
    /// List all requirements in the model(s)
    Requirements(commands::requirements::Args),
    /// Convert a model to another format
    Convert(commands::convert::Args),
    /// Generate source code from a model using a template
    Source(commands::source::Args),
    /// Check model(s) for issues
    Check(commands::check::Args),
}

fn main() {
    let cli = Cli::parse();

    if cli.debug {
        tracing_subscriber::fmt()
            .with_env_filter("graphwalker_restful=debug,graphwalker=debug")
            .with_target(true)
            .init();
    }

    let result = match cli.command {
        Command::Offline(args) => commands::offline::run(args),
        Command::Online(args) => commands::online::run(args),
        Command::Methods(args) => commands::methods::run(args),
        Command::Requirements(args) => commands::requirements::run(args),
        Command::Convert(args) => commands::convert::run(args),
        Command::Source(args) => commands::source::run(args),
        Command::Check(args) => commands::check::run(args),
    };

    if let Err(e) = result {
        eprintln!("{}", e);
        process::exit(1);
    }
}
