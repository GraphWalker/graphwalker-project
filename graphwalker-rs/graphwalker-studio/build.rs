use std::path::Path;
use std::process::Command;

fn main() {
    let frontend_dir = Path::new(env!("CARGO_MANIFEST_DIR")).join("frontend");
    if !frontend_dir.join("package.json").exists() {
        return;
    }

    println!("cargo:rerun-if-changed=frontend/src");
    println!("cargo:rerun-if-changed=frontend/index.html");
    println!("cargo:rerun-if-changed=frontend/package.json");

    let node_modules = frontend_dir.join("node_modules");
    if !node_modules.exists() {
        let status = Command::new("npm")
            .arg("install")
            .current_dir(&frontend_dir)
            .status()
            .expect("Failed to run npm install");
        assert!(status.success(), "npm install failed");
    }

    let status = Command::new("npm")
        .args(["run", "build"])
        .current_dir(&frontend_dir)
        .status()
        .expect("Failed to run npm run build");
    assert!(status.success(), "Frontend build failed");
}
