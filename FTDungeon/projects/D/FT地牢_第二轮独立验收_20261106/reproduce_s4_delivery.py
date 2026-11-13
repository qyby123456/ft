import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path

from audit_s8_submission import load


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def run(command, cwd, log):
    result = subprocess.run(command, cwd=cwd, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=240)
    log.write_text(result.stdout + "\nSTDERR\n" + result.stderr, encoding="utf-8")
    assert result.returncode == 0, str(log)


def main():
    root = Path(sys.argv[1]).resolve()
    manifest = load(root / "ready-for-review.json")
    records = []
    for name in ("submission-snapshot.json", "supplemental-snapshot.json", "delivery-snapshot.json"):
        records.extend(load(root / name)["files"])
    records = list({record["source"]: record for record in records}.values())
    for record in records:
        assert digest(Path(record["snapshot"])) == record["sha256"]
    destination = root / "independent-reproduction"
    destination.mkdir()
    skill = Path(r"C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill")
    runner = skill / "scripts/run-build.mjs"
    assert (skill / "node_modules/@mattzh72/lodestone").is_dir()
    for name in ("common.mjs", "enrich.mjs", "qa/postprocess.py", "assembly/build.py"):
        output = destination / name
        output.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(root / name, output)
    rebuilt = []
    for room in manifest["rooms"]:
        folder = destination / room["folder"]
        folder.mkdir()
        source = root / room["folder"]
        shutil.copyfile(source / "recipe.mjs", folder / "recipe.mjs")
        run([r"C:\Program Files\nodejs\node.exe", str(runner), str(folder / "recipe.mjs"), "--out-dir", str(folder), "--data-version", "3465", "--json"], skill, destination / (room["id"] + ".log"))
        if room["id"] == "S4-14":
            run([sys.executable, str(destination / "qa/postprocess.py"), str(folder)], destination, destination / "S4-14-postprocess.log")
        outputs = list(folder.glob("*.nbt"))
        assert len(outputs) == 1 and digest(outputs[0]) == room["sha256"], room["id"]
        (folder / "qa").mkdir()
        for name in ("self-review.json", "inventory.json", "vanilla-block-tags.json"):
            shutil.copyfile(source / "qa" / name, folder / "qa" / name)
        rebuilt.append({"id": room["id"], "sha256": digest(outputs[0]), "binary_equal": True})
        print(room["id"], "EXACT", flush=True)
    run([sys.executable, str(destination / "assembly/build.py"), str(destination / "assembly")], destination, destination / "assembly.log")
    expected = root / "assembly"
    expected_files = [expected / "s4-wetland-connected.nbt", *sorted((expected / "parts").glob("*.nbt"))]
    assert len(expected_files) == 19
    assembly = []
    for path in expected_files:
        output = destination / "assembly" / path.relative_to(expected)
        assert digest(output) == digest(path), str(path)
        assembly.append({"file": str(path.relative_to(expected)), "sha256": digest(output), "binary_equal": True})
    for record in records:
        assert digest(Path(record["snapshot"])) == record["sha256"]
    dependency = Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools\nbtio.py")
    result = {"stage": "INDEPENDENT_RECIPE_REEXECUTION_ALL_TEN_ROOMS_AND_MERGED_PARTS", "rooms": rebuilt, "assembly_and_parts": assembly, "room_outputs_copied_from_candidate": False, "all_29_nbt_outputs_binary_equal": True, "frozen_evidence_unchanged": len(records), "runner_sha256": digest(runner), "readonly_external_nbtio_sha256": digest(dependency), "accepted": False, "limitations": ["Recipe reproducibility does not replace world or visual acceptance", "Captured self-review/inventory provide assembly metadata, no candidate NBT copied as reproduction input"]}
    (root / "independent-qa/reproduction.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({key: value for key, value in result.items() if key not in {"rooms", "assembly_and_parts"}}, ensure_ascii=False))


if __name__ == "__main__":
    main()
