import hashlib
import json
from pathlib import Path

from PIL import Image
import zstandard


ROOT = Path(__file__).resolve().parent
LIBRARY = Path(r"D:\ft工程文件\image2.5\nature-dungeon-styles-20261104")
SESSIONS = Path(r"C:\Users\YOUR_USERNAME\.dsh\sessions")


def main():
    index = json.loads((LIBRARY / "out/index_batch3.json").read_text(encoding="utf-8"))
    metadata = json.loads((LIBRARY / "out/nature_meta_batch3.json").read_text(encoding="utf-8"))
    assert len(index) == 80
    old_hashes = {
        value["sha256"] for value in metadata["images"].values()
        if int(value["type"].split("-")[0]) <= 10
    }
    images = []
    groups = []
    for style in metadata["styles"]:
        entries = [entry for entry in index if entry["style"] == style["id"]]
        assert sorted(int(entry["id"].split("-")[1]) for entry in entries) == list(range(11, 21))
        directory = ROOT / style["id"]
        directory.mkdir(exist_ok=True)
        references = []
        for entry in entries:
            image_path = LIBRARY / entry["dir"] / "out" / entry["filename"]
            prompt_path = LIBRARY / entry["dir"] / "prompts" / entry["file"]
            assert prompt_path.is_file()
            with Image.open(image_path) as image:
                image.load()
                size = list(image.size)
                assert size == [1536, 1024]
            digest = hashlib.sha256(image_path.read_bytes()).hexdigest()
            assert digest == metadata["images"][image_path.stem]["sha256"]
            assert digest not in old_hashes
            references.append({**entry, "image_path": str(image_path), "prompt_path": str(prompt_path), "sha256": digest, "size": size})
        group = {"style": style["id"], "theme": style["cn"], "output_directory": str(directory), "references": references}
        (directory / "references.json").write_text(json.dumps(group, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        groups.append(group)
        images.extend(references)
    assert len({entry["sha256"] for entry in images}) == 80
    evidence = []
    for session in SESSIONS.rglob("*.zstd"):
        with session.open("rb") as handle:
            text = zstandard.ZstdDecompressor().stream_reader(handle).read().decode("utf-8")
        if "nature-dungeon-styles-20261104" not in text:
            continue
        if any(term in text for term in ("secret-vault", "上述八种风格再跑", "11–20", "index_batch3")):
            evidence.append({"session_file": str(session), "compressed_sha256": hashlib.sha256(session.read_bytes()).hexdigest(), "matched_batch3_reference": True})
    assert evidence
    report = {"found": True, "group_count": 8, "images": 80, "numbers_per_group": list(range(11, 21)), "all_decoded": True, "all_manifest_hashes_match": True, "distinct_from_previous_01_10": True, "unique_image_hashes": 80, "dsh_session_evidence": evidence, "groups": groups}
    (ROOT / "reference-audit.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"found": True, "groups": 8, "images": 80, "dsh_sessions": len(evidence)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
