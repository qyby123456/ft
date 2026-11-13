import hashlib
import json
import sys
from datetime import datetime
from pathlib import Path


def main():
    group = Path(sys.argv[1]).resolve()
    source_manifest = Path(sys.argv[2]).resolve()
    payload = source_manifest.read_bytes()
    manifest = json.loads(payload.decode("utf-8-sig"))
    destination = Path(__file__).parent / (group.name + "-supplement-" + datetime.now().strftime("%Y%m%d-%H%M%S-%f"))
    destination.mkdir()
    captures = []
    for item in manifest["supplement_artifacts"]:
        source = Path(item["path"]).resolve()
        assert source.is_relative_to(group)
        assert source == (group / item["relative_path"]).resolve()
        contents = source.read_bytes()
        assert hashlib.sha256(contents).hexdigest() == item["sha256"]
        assert len(contents) == item["bytes"]
        output = destination / source.relative_to(group)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_bytes(contents)
        captures.append({"source": str(source), "snapshot": str(output), "sha256": item["sha256"], "bytes": len(contents)})
    for item in captures:
        assert hashlib.sha256(Path(item["source"]).read_bytes()).hexdigest() == item["sha256"]
    assert source_manifest.read_bytes() == payload
    (destination / "supplement-manifest.json").write_bytes(payload)
    result = {"stage": "INDEPENDENT_SCOPED_SUPPLEMENT_CAPTURE_NOT_ACCEPTANCE", "source_sha256": manifest["source_sha256"], "manifest_sha256": hashlib.sha256(payload).hexdigest(), "files": captures, "accepted": False}
    (destination / "supplement-snapshot.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"directory": str(destination), "files": len(captures), "source_sha256": result["source_sha256"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
