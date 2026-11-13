import argparse
import hashlib
import json
import subprocess
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    manifest = load(root / "ready-for-review.json")
    records = load(root / "submission-snapshot.json")["files"] + load(root / "supplemental-snapshot.json")["files"]
    captured = {}
    for record in records:
        source = Path(record["snapshot"])
        assert source.is_relative_to(root)
        assert hashlib.sha256(source.read_bytes()).hexdigest() == record["sha256"]
        captured[record["source"]] = source
    destination = root / "independent-reproduction"
    destination.mkdir()
    skill = Path(r"C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill")
    runner = skill / "scripts/run-build.mjs"
    assert (skill / "node_modules/@mattzh72/lodestone").is_dir()
    results = []
    for room in manifest["rooms"]:
        candidate = captured[room["candidate"]]
        recipe = candidate.parent / "recipe.mjs"
        assert recipe.is_file()
        output = destination / room["id"]
        command = [r"C:\Program Files\nodejs\node.exe", str(runner), str(recipe), "--out-dir", str(output), "--json"]
        process = subprocess.run(command, cwd=skill, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=180)
        (destination / (room["id"] + ".log")).write_text(process.stdout + "\nSTDERR\n" + process.stderr, encoding="utf-8")
        result = {"id": room["id"], "source_sha256": room["sha256"], "recipe": str(recipe), "command": command, "exit_code": process.returncode}
        if process.returncode == 0:
            files = list(output.glob("*.nbt"))
            assert len(files) == 1
            generated = files[0]
            expected_data, expected_blocks = decode(candidate)
            actual_data, actual_blocks = decode(generated)
            result.update({"output": str(generated), "output_sha256": hashlib.sha256(generated.read_bytes()).hexdigest(), "binary_equal": generated.read_bytes() == candidate.read_bytes(), "size_equal": actual_data["size"] == expected_data["size"], "block_state_and_be_payload_equal": actual_blocks == expected_blocks, "entities_equal": actual_data["entities"] == expected_data["entities"], "decoded_root_equal": actual_data == expected_data})
        results.append(result)
        (destination / "progress.json").write_text(json.dumps({"rooms": results, "stage": "RUNNING"}, ensure_ascii=False, indent=2), encoding="utf-8")
        print(json.dumps(result, ensure_ascii=False), flush=True)
    unchanged = all(hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"] for record in records)
    assert unchanged
    report = {"stage": "INDEPENDENT_FROZEN_ROOM_RECIPE_REEXECUTION", "runner": str(runner), "runner_sha256": hashlib.sha256(runner.read_bytes()).hexdigest(), "rooms": results, "all_ten_exact_binary": len(results) == 10 and all(result.get("binary_equal", False) for result in results), "all_ten_equal_size_states_be_entities": len(results) == 10 and all(result.get("size_equal", False) and result.get("block_state_and_be_payload_equal", False) and result.get("entities_equal", False) for result in results), "frozen_evidence_unchanged": unchanged, "accepted": False, "limitations": ["Individual room recipes only; merged assembly not reproduced", "No world execution or visual acceptance"]}
    (destination / "result.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
