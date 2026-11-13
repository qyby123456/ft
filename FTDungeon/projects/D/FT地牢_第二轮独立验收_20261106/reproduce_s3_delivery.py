import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import sys


def main():
    root = Path(sys.argv[1]).resolve()
    records = json.loads((root / "submission-snapshot.json").read_text(encoding="utf-8"))["files"]
    manifest = json.loads((root / "ready-for-review.json").read_text(encoding="utf-8-sig"))
    output = root / "independent-reproduction"
    output.mkdir()
    for filename in ["common.mjs", "assemble.mjs", *[f"room{number}.mjs" for number in range(11, 21)]]:
        shutil.copy2(root / filename, output / filename)
    reports = []
    specifications = [*manifest["rooms"], manifest["merged"]]
    for specification in specifications:
        candidate = specification["candidate"]
        identifier = Path(candidate["path"]).stem
        folder = output / "rooms" / identifier
        folder.mkdir(parents=True)
        recipe = output / ("assemble.mjs" if identifier == "s3-merged" else f"room{identifier.split('-')[1]}.mjs")
        command = [r"C:\Program Files\nodejs\node.exe", r"C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs", str(recipe), "--out-dir", str(folder)]
        if identifier == "s3-merged":
            command.append("--no-block-state-updates")
        result = subprocess.run(command, capture_output=True, timeout=120)
        (folder / "build.log").write_bytes(result.stdout + result.stderr)
        assert result.returncode == 0, result.stderr.decode("utf-8", errors="replace")
        rebuilt = folder / f"{identifier}.nbt"
        assert hashlib.sha256(rebuilt.read_bytes()).hexdigest() == candidate["sha256"]
        if identifier != "s3-merged":
            (folder / "qa").mkdir()
            for name in ("report.json", "visual-review.json"):
                shutil.copy2(root / "rooms" / identifier / "qa" / name, folder / "qa" / name)
        reports.append({"id": identifier, "sha256": candidate["sha256"], "byte_exact": True})
    for record in records:
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    report = {"stage": "PARENT_EXECUTED_FROZEN_RECIPE_REPRODUCTION", "outputs": reports, "accepted": False, "scope": "Copied frozen recipes/common helper, generated ten rooms without copying candidate NBT, copied exact-hash QA bindings for assembler, generated merged with declared no-block-state-updates flag; original evidence unchanged. Not a world run."}
    (root / "independent-qa/reproduction.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print("Eleven outputs reproduced byte-identically")


if __name__ == "__main__":
    main()
