use clap::Args as ClapArgs;

use super::CliResult;

#[derive(ClapArgs)]
pub struct Args {
    /// Service type: RESTFUL or WEBSOCKET
    #[arg(short, long, default_value = "WEBSOCKET")]
    pub service: String,

    /// Port to listen on
    #[arg(short, long, default_value_t = 8887)]
    pub port: u16,

    /// Model file and generator pairs: -m <file> <generator>
    #[arg(short, long = "model", num_args = 2, action = clap::ArgAction::Append)]
    pub model: Vec<String>,

    /// Start element name
    #[arg(short = 'e', long = "start-element")]
    pub start_element: Option<String>,

    /// Seed for random generator (0 = no seed)
    #[arg(long, default_value_t = 0)]
    pub seed: u64,
}

pub fn run(args: Args) -> CliResult {
    let seed = if args.seed == 0 {
        None
    } else {
        Some(args.seed)
    };

    let rt = tokio::runtime::Runtime::new()?;
    rt.block_on(async {
        match args.service.to_uppercase().as_str() {
            "RESTFUL" => graphwalker_restful::start_rest_server(args.port, seed).await,
            _ => graphwalker_restful::start_websocket_server(args.port).await,
        }
    })
}
