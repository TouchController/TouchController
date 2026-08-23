use std::path::PathBuf;

use clap::Parser;
use starlark::environment::{Globals, Module};
use starlark::eval::Evaluator;
use starlark::syntax::{AstModule, Dialect};

#[derive(Parser)]
/// Parse a .bzl file and print its public variables as JSON.
struct Args {
    /// Path to the .bzl file to parse.
    path: PathBuf,
}

fn run() -> starlark::Result<()> {
    let args = Args::parse();
    let ast = AstModule::parse_file(&args.path, &Dialect::Extended)?;
    let globals = Globals::standard();
    Module::with_temp_heap(|module| {
        let mut eval = Evaluator::new(&module);
        eval.eval_module(ast, &globals)?;
        let mut properties = serde_json::Map::new();
        for name in module.names() {
            let Some(value) = module.get(name.as_str()) else {
                continue;
            };
            if let Ok(json) = value.to_json_value() {
                properties.insert(name.as_str().to_owned(), json);
            }
        }
        serde_json::to_writer(std::io::stdout(), &properties)
            .map_err(starlark::Error::new_other)?;
        starlark::Result::Ok(())
    })
}

fn main() {
    if let Err(error) = run() {
        eprintln!("{error}");
        std::process::exit(1);
    }
}
