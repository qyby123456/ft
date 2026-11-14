import argparse
import json
import shutil
import subprocess
import zipfile
from pathlib import Path

from import_models import MATERIALS, PROJECT, SOURCE, WORK, digest, validate, write_json


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("runtime", type=Path)
    arguments = parser.parse_args()
    runtime = arguments.runtime.resolve(strict=True)
    java_bin = Path("C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot/bin")
    live_hash = digest(runtime.read_bytes())
    baseline = WORK / f"baseline-{live_hash[:16]}.jar"
    if not baseline.exists():
        shutil.copyfile(runtime, baseline)
    if digest(baseline.read_bytes()) != live_hash or digest(runtime.read_bytes()) != live_hash:
        raise ValueError("Runtime changed while preparing baseline")
    manifest = json.loads((WORK / "import-manifest.json").read_text(encoding="utf-8"))
    additions = {}
    for entry in manifest["models"]:
        for kind in ("model", "texture"):
            relative = entry[kind]
            data = (WORK / "resources" / relative).read_bytes()
            if digest(data) != entry[kind + "Sha256"] or (SOURCE / relative).read_bytes() != data:
                raise ValueError("Imported resource changed: " + relative)
            additions[relative] = data
        model = json.loads(additions[entry["model"]])
        validate(model, WORK / "resources" / entry["texture"])
    preview_manifest = json.loads((WORK / "verification/render-manifest.json").read_text(encoding="utf-8"))
    for item, rendered in zip(manifest["models"], preview_manifest, strict=True):
        if any(item[key] != rendered[key] for key in ("id", "modelSha256", "textureSha256")):
            raise ValueError("Preview used different model or texture")
        if set(rendered["directions"]) != {"north", "south", "west", "east"}:
            raise ValueError("Four-direction preview incomplete")
    candidate = WORK / "qys-bosses.jar"
    shutil.copyfile(baseline, candidate)
    command = [str(java_bin / "jar.exe"), "--update", "--file", str(candidate)]
    for relative in additions:
        command.extend(["-C", str(WORK / "resources"), relative])
    subprocess.run(command, check=True)
    with zipfile.ZipFile(baseline) as before, zipfile.ZipFile(candidate) as after:
        old_names = {entry.filename for entry in before.infolist() if not entry.is_dir()}
        new_names = {entry.filename for entry in after.infolist() if not entry.is_dir()}
        if len(after.namelist()) != len(set(after.namelist())) or after.testzip() is not None:
            raise ValueError("Invalid or duplicate archive entry")
        removed = old_names - new_names
        changed = {name for name in old_names & new_names if before.read(name) != after.read(name)}
        added = new_names - old_names
        expected_changed = {name for name in additions if name in old_names and before.read(name) != additions[name]}
        if removed or changed != expected_changed or added != set(additions) - old_names:
            raise ValueError(f"Unexpected archive changes: {removed=}, {changed=}, {added=}")
        for relative, expected in additions.items():
            if after.read(relative) != expected:
                raise ValueError("Packaged asset mismatch: " + relative)
        registered = [entry["id"] for entry in manifest["models"]
                      if "net/farmingtales/ftboss/item/MaterialStaffItems.class" in new_names]
    subprocess.run(["node", str(PROJECT / "tools/verification/check_jar_zip_rules.cjs"), str(candidate)], check=True)
    subprocess.run([str(java_bin / "java.exe"), str(PROJECT / "tools/verification/ZipStreamCheck.java"), str(candidate)], check=True)
    report = {"baseline": str(baseline), "baselineSha256": live_hash, "artifact": str(candidate),
              "artifactSha256": digest(candidate.read_bytes()), "runtime": str(runtime),
              "added": sorted(added), "changed": sorted(changed), "removed": [],
              "unchangedExistingFiles": len(old_names - changed), "previewViews": len(manifest["models"]) * 4,
              "registeredMaterialStaves": registered,
              "status": "resources packaged; the runtime has no tiered staff item registrations",
              "docsImpact": "none"}
    write_json(WORK / "build-report.json", report)
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
