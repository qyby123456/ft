import hashlib
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parent.parent
EXPECTED = "bab12080dae0e8fb2290f73b6988f40af2094d932a3303cdabc12aaac250263f"


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def inventory(root):
    return [{"path": path.relative_to(root).as_posix(), "sha256": sha(path)} for path in sorted(root.rglob("*")) if path.is_file()]


def main():
    runtime = PROJECT / "runtime/next-commit-iofix"
    ready = json.loads((runtime / "ready.json").read_text(encoding="utf-8"))
    assert ready["artifactSha256"] == EXPECTED
    for relative, expected in ready["files"].items():
        assert sha(runtime / relative) == expected, relative
    ROOT.joinpath("inputs").mkdir(exist_ok=True)
    source = runtime / "run/installed-v5-q3-forcekill"
    original_world = inventory(source / "world")
    assert not (source / "world/session.lock").is_symlink()
    baseline = ROOT / "inputs/baseline"
    shutil.copytree(source / "world", baseline / "world")
    assert inventory(baseline / "world") == original_world
    shutil.copytree(source / "libraries", baseline / "libraries")
    for name in ("eula.txt", "server.properties"):
        shutil.copyfile(source / name, baseline / name)
    jar = ROOT / "inputs" / Path(ready["artifact"]).name
    shutil.copyfile(runtime / ready["artifact"], jar)
    assert sha(jar) == EXPECTED
    snapshot = ROOT / "inputs/snapshot"
    shutil.copytree(PROJECT / "catalog/s8-07-doorway-review-v5/nine-room-package-v5", snapshot / "catalog/nine-room-package-v5")
    component_files = {item["path"]: item["sha256"] for item in inventory(snapshot / "catalog")}
    save(snapshot / "snapshot-lock.json", {"components": {"catalog": {"files": component_files}}})
    helpers = [PROJECT / relative for relative in (
        "integration/v5-diagnostics/faults.py", "integration/v6-final/faults_final.py", "integration/v6-final/readback.py",
        "integration/integrate.py", "integration/verify_saved_world.py", "integration/v2/read_native_be.py",
        "integration/staging/20261106_132015_129205/catalog/nbtio.py", "integration/staging/20261106_132015_129205/snapshot-lock.json")]
    for path in helpers:
        shutil.copyfile(path, ROOT / "inputs" / ("reference-" + str(path.relative_to(PROJECT)).replace("\\", "--").replace("/", "--")))
    info = json.loads((runtime / "installed-v5-q3-forcekill-result.json").read_text(encoding="utf-8"))
    save(ROOT / "input-lock.json", {"runtimeReady": str(runtime / "ready.json"), "runtimeReadySha256": sha(runtime / "ready.json"),
         "runtimeFiles": ready["files"], "jar": str(jar), "jarSha256": EXPECTED,
         "sourceWorld": str(source / "world"), "sourceWorldFiles": original_world,
         "baselineWorldFiles": inventory(baseline / "world"), "libraryFiles": inventory(baseline / "libraries"),
         "helperFiles": [{"path": str(path), "sha256": sha(path)} for path in helpers],
         "sourceInstanceId": info["instanceId"], "independentExecutor": "core-final-runtime-faults-not-runtime-maker",
         "powerloss": False, "actualCommitted": False})
    print("INPUTS_LOCKED exactJar=" + EXPECTED + " worldFiles=" + str(len(original_world)))


if __name__ == "__main__":
    main()
