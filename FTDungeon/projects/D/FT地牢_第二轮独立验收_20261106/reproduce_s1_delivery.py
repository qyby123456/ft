import hashlib
import json
import shutil
import sys
from pathlib import Path

from audit_s8_submission import load
from reproduce_s4_delivery import run


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    root = Path(sys.argv[1]).resolve()
    manifest = load(root / "ready-for-review.json")
    records = []
    for name in ("submission-snapshot.json", "supplemental-snapshot.json", "delivery-snapshot.json"):
        records.extend(load(root / name)["files"])
    records = list({record["source"]: record for record in records}.values())
    assert all(digest(Path(record["snapshot"])) == record["sha256"] for record in records)
    destination = root / "independent-reproduction"
    destination.mkdir()
    skill = Path(r"C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill")
    runner = skill / "scripts/run-build.mjs"
    for name in ("common.mjs", "enhance.mjs", "equip.py", "merge.py", "scenery.mjs"):
        shutil.copyfile(root / name, destination / name)
    results = []
    for room in manifest["rooms"]:
        folder_name = Path(room["candidate"]).parent.name
        source = root / folder_name
        folder = destination / folder_name
        folder.mkdir()
        for name in ("recipe.mjs", "design.json"):
            shutil.copyfile(source / name, folder / name)
        run([r"C:\Program Files\nodejs\node.exe", str(runner), str(folder / "recipe.mjs"), "--out-dir", str(folder), "--data-version", "3465"], skill, destination / (room["id"] + ".log"))
        run([sys.executable, str(destination / "equip.py"), str(folder)], destination, destination / (room["id"] + "-equip.log"))
        generated = next(folder.glob("*.nbt"))
        assert digest(generated) == room["sha256"], room["id"]
        shutil.copyfile(source / "self-check.json", folder / "self-check.json")
        (folder / "qa").mkdir()
        shutil.copyfile(source / "qa/independent-all-support.json", folder / "qa/independent-all-support.json")
        results.append({"id": room["id"], "sha256": digest(generated), "binary_equal": True})
        print(room["id"], "EXACT", flush=True)
    run([r"C:\Program Files\nodejs\node.exe", str(runner), str(destination / "scenery.mjs"), "--out-dir", str(destination / "scenery"), "--data-version", "3465"], skill, destination / "scenery.log")
    scenery = destination / "scenery/s1-19-forest-depth.nbt"
    assert digest(scenery) == digest(root / "scenery/s1-19-forest-depth.nbt")
    run([sys.executable, str(destination / "merge.py"), str(destination / "merged")], destination, destination / "merge.log")
    originals = [root / "merged/s1-mossy-functional-11-20.nbt", *sorted((root / "merged/chunks").glob("*.nbt"))]
    assert len(originals) == 34
    merged = []
    for original in originals:
        generated = destination / "merged" / original.relative_to(root / "merged")
        assert digest(generated) == digest(original), str(original)
        merged.append({"file": str(original.relative_to(root / "merged")), "sha256": digest(generated), "binary_equal": True})
    assert all(digest(Path(record["snapshot"])) == record["sha256"] for record in records)
    report = {"stage": "INDEPENDENT_S1_ALL_ROOMS_SCENERY_MERGED_AND_CHUNKS_REPRODUCTION", "rooms": results, "scenery_sha256": digest(scenery), "merged_and_chunks": merged, "all_45_nbt_outputs_binary_equal": True, "candidate_nbt_copied_as_generation_input": False, "original_frozen_files_unchanged": len(records), "runner_sha256": digest(runner), "limitations": ["Reproduction uses frozen source metadata/self-check/support as assembly metadata", "No actual world or player input execution"], "accepted": False}
    (root / "independent-qa/reproduction.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print("All ten rooms, scenery, merged and 33 chunks byte-identical; frozen evidence unchanged.")


if __name__ == "__main__":
    main()
