import json
import os
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parent
WORKSPACE = ROOT.parent.parent


def main():
    if not ROOT.is_relative_to(WORKSPACE):
        raise ValueError("Unexpected workspace")
    server = WORKSPACE / "kubejs/server_scripts/block_events/wishing_well.js"
    steps = [("geometry-unit-tests", [sys.executable, str(ROOT / "test_optimization.py")]),
             ("asset-verification", [sys.executable, str(ROOT / "verify_assets.py")]),
             ("recipe-and-reward-mock", ["node", str(ROOT.parent / "verify_wishing_well.mjs"), str(server)]),
             ("interaction-mock", ["node", str(ROOT.parent / "verify_wishing_well_runtime.mjs"), str(server)]),
             ("command-mock", ["node", str(ROOT.parent / "verify_wishing_well_command.mjs"), str(server)]),
             ("nbt-layout-mock", ["node", str(ROOT.parent / "verify_wishing_well_against_nbt.mjs"), str(server), str(ROOT.parent / "xyj_layout.json")]),
             ("prefixed-web-controls", ["node", str(ROOT / "preview.js")]),
             ("unified-web-controls", ["node", str(ROOT / "preview.js")]),
             ("42-view-visual-regression", ["node", str(ROOT / "compare_visuals.js")])]
    report = {"scope": "offline validation, not actual Minecraft gameplay", "steps": [], "all_passed": False}
    output = ROOT / "final-release/full-validation.json"
    output.parent.mkdir(exist_ok=True)
    logs = ROOT / "final-release/test-logs"
    logs.mkdir(exist_ok=True)
    for name, command in steps:
        environment = os.environ.copy()
        environment["PYTHONIOENCODING"] = "utf-8"
        environment.pop("FT_PREVIEW_TEST_URL", None)
        if name == "unified-web-controls":
            environment["FT_PREVIEW_TEST_URL"] = "http://127.0.0.1:18790/instruments/viewer.html"
        result = subprocess.run(command, cwd=WORKSPACE, env=environment, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, encoding="utf-8", errors="replace")
        (logs / (name + ".txt")).write_text(result.stdout, encoding="utf-8")
        report["steps"].append({"name": name, "exit_code": result.returncode, "log": str((logs / (name + ".txt")).relative_to(ROOT))})
        output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
        print(name, "PASS" if result.returncode == 0 else "FAIL", flush=True)
        if result.returncode:
            print(result.stdout)
            raise SystemExit(result.returncode)
    report["all_passed"] = True
    output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
