import hashlib
import json
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parent


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    results = []
    for name in ("core", "catalog", "runtime"):
        component = PROJECT / name
        ready_path = component / "ready.json"
        ready_hash = digest(ready_path)
        ready = json.loads(ready_path.read_text(encoding="utf-8-sig"))
        assert ready.get("ready", ready.get("status") == "READY")
        assert ready["apiVersion"] == 1
        if "files" in ready:
            files = ready["files"]
        else:
            inventory = (component / ready["inventory"]).resolve()
            assert inventory.is_relative_to(component.resolve())
            assert digest(inventory) == ready["inventorySha256"]
            files = json.loads(inventory.read_text(encoding="utf-8-sig"))["files"]
        entries = files.items() if isinstance(files, dict) else ((entry["path"], entry["sha256"]) for entry in files)
        checked = []
        for relative, expected in entries:
            path = (component / relative).resolve()
            assert path.is_relative_to(component.resolve()), relative
            assert digest(path) == expected, (name, relative)
            checked.append(relative)
        assert digest(ready_path) == ready_hash, "Concurrent handoff change"
        results.append({"component": name, "ready_sha256": ready_hash, "verified_files": len(checked)})
    runtime = PROJECT / "runtime"
    ready = json.loads((runtime / "ready.json").read_text(encoding="utf-8-sig"))
    jar = runtime / ready["artifact"]
    assert digest(jar) == ready["artifactSha256"]
    with zipfile.ZipFile(jar) as archive:
        assert archive.testzip() is None
        names = archive.namelist()
        assert not any("gametest" in name.lower() or "RuntimeSelfTest" in name or "RuntimeRestartGameTests" in name for name in names)
        manifest = json.loads(archive.read("data/biomedungeon/manifest.json"))
        templates = manifest["templates"]
        assert {entry["id"] for entry in templates} == {"s8_05", "s8_06"}
        for entry in templates:
            namespace, identifier = entry["resourceId"].split(":")
            payload = archive.read(f"data/{namespace}/structures/{identifier}.nbt")
            assert hashlib.sha256(payload).hexdigest() == entry["sha256"]
        core_sources = sorted((PROJECT / "core/src/main/java").rglob("*.java"))
        core_class_names = [str(path.relative_to(PROJECT / "core/src/main/java")).replace("\\", "/").replace(".java", ".class") for path in core_sources]
        assert all(name in names for name in core_class_names)
    world_log = (runtime / "world-final.log").read_text(encoding="utf-8-sig", errors="replace")
    restart_log = (runtime / "restart-resume.log").read_text(encoding="utf-8-sig", errors="replace")
    assert "All 2 required tests passed" in world_log
    assert "BUILD SUCCESSFUL" in restart_log
    observations = [line for line in (world_log + "\n" + restart_log).splitlines() if "RUNTIME_WORLD_PASS" in line or "RUNTIME_RESTART" in line]
    report = {"component_handoffs": results, "candidate_jar": str(jar), "candidate_sha256": digest(jar), "archive_crc": "PASS", "template_ids": [entry["id"] for entry in templates], "test_classes_excluded": True, "runtime_producer_log_observations": observations, "independent_world_retest": "DELEGATED_TO_INTEGRATION_NOT_CLAIMED_HERE", "installed_release_artifact_world_test": "NOT_VERIFIED_BY_THIS_AUDIT", "formal_instance_written": False}
    (ROOT / "handoff-audit.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"handoffs": results, "jar_crc": "PASS", "templates": report["template_ids"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
