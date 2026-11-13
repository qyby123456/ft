import argparse
import hashlib
import json
import shutil
from pathlib import Path


ROOT = Path(__file__).resolve().parent
DESTINATION = ROOT / "revisions/superseded-preoptimization"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--refresh-manifest", action="store_true")
    arguments = parser.parse_args()
    if not arguments.refresh_manifest:
        if DESTINATION.exists():
            raise ValueError("The pre-optimization snapshot already exists; do not overwrite")
        DESTINATION.mkdir(parents=True)
        for name in ("payload", "previews", "build_assets.py", "build_core.py", "viewer.html", "preview.js", "asset-verification.json", "unified-preview-verification.json"):
            source = ROOT / name
            target = DESTINATION / name
            if source.is_dir():
                shutil.copytree(source, target)
            else:
                shutil.copy2(source, target)
    files = [{"path": file.relative_to(DESTINATION).as_posix(), "sha256": hashlib.sha256(file.read_bytes()).hexdigest()}
             for file in sorted(DESTINATION.rglob("*")) if file.is_file() and file.name != "status.json"]
    metadata = {"status": "superseded", "reason": "Replaced by final conservative internal-face optimization; retained for coordinated cleanup and visual comparisons",
                "source_files_deleted": False, "files": files}
    (DESTINATION / "status.json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
    print("Preserved", len(files), "previous assets and previews; status=superseded")


if __name__ == "__main__":
    main()
