import hashlib
import json
import zipfile
from pathlib import Path, PurePosixPath

from PIL import Image

from structure_io import load_structure, write_json


ROOT = Path(__file__).resolve().parents[1]
INCOMING = Path("D:/ft工程文件/FT地牢_Image25接入_20261101/incoming/M1")


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read_json(path):
    return json.loads(path.read_text(encoding="utf-8"))


def main():
    manifest = read_json(ROOT / "out/manifest.json")
    native = read_json(ROOT / "qa/minecraft-format-verification.json")
    geometry = read_json(ROOT / "qa/geometry.json")
    datapack = read_json(ROOT / "qa/datapack.json")
    archive = ROOT / "out/ft-bloodthorn-chapel-1.20.1.zip"
    structure = ROOT / "out/FT_Bloodthorn_Chapel.nbt"
    template = load_structure(structure)
    assert native["status"] == geometry["status"] == datapack["status"] == "PASS"
    assert digest(archive) == manifest["archive_sha256"] == native["package_sha256"]
    assert digest(structure) == manifest["structure_sha256"]
    assert template["size"] == [15, 12, 15]
    assert len(template["blocks"]) == native["template"]["blocks"] == geometry["block_count"]
    assert len(template["palette"]) == native["template"]["palette"]
    assert native["template"]["roundtrip"] == "PASS"
    assert native["parsed_commands"] == datapack["commands"] and not native["command_errors"]
    with zipfile.ZipFile(archive) as package:
        assert package.testzip() is None
        assert json.loads(package.read("pack.mcmeta"))["pack"]["pack_format"] == 15
        assert package.read("data/ftbloodthorn/structures/chapel.nbt") == structure.read_bytes()
        for name in package.namelist():
            assert not PurePosixPath(name).is_absolute() and ".." not in PurePosixPath(name).parts
            if not name.endswith("/"):
                assert package.read(name) == (ROOT / "out/datapack" / name).read_bytes()
    references = []
    for reference in manifest["references"]:
        path = Path(reference["path"])
        assert digest(path) == reference["sha256"]
        references.append({"path": str(path), "sha256": reference["sha256"]})
    required = {"room.nbt", "room.json", "preview.png", "qa.json"}
    assert {path.name for path in INCOMING.iterdir() if path.is_file()} == required
    copies = {}
    for name in sorted(required):
        delivered = INCOMING / name
        assert delivered.read_bytes() == (ROOT / "out/integration/M1" / name).read_bytes()
        copies[name] = digest(delivered)
    metadata = read_json(INCOMING / "room.json")
    integration_qa = read_json(INCOMING / "qa.json")
    integration = load_structure(INCOMING / "room.nbt")
    assert {"id", "name", "size", "entrance", "interior", "facing", "sha256", "reference"} <= metadata.keys()
    assert metadata["id"] == "M1" and metadata["facing"] == "north"
    assert metadata["size"] == integration["size"] == [15, 12, 15]
    assert metadata["entrance"] == [7, 2, 0] and metadata["interior"] == [7, 2, 3]
    assert copies["room.nbt"] == metadata["sha256"] == integration_qa["room_sha256"] == native["integration"]["sha256"]
    assert integration_qa["status"] == integration_qa["geometry"]["status"] == "PASS"
    assert native["integration"]["roundtrip"] == "PASS"
    assert len(integration["blocks"]) == native["integration"]["blocks"]
    assert integration_qa["standalone_unchanged"] and integration_qa["source_standalone_sha256"] == digest(structure)
    assert not integration_qa["core_modified"] and not integration_qa["deployed"] and not integration_qa["worlds_modified"]
    previews = []
    for path in sorted((ROOT / "preview").glob("*.png")) + [INCOMING / "preview.png"]:
        with Image.open(path) as image:
            dimensions = list(image.size)
            image.verify()
        previews.append({"path": str(path), "size": dimensions, "sha256": digest(path)})
    report = {"status": "PASS", "minecraft": "1.20.1", "package_sha256": digest(archive), "structure_sha256": digest(structure), "main_blocks": len(template["blocks"]), "parsed_commands": native["parsed_commands"], "zip_integrity": "PASS", "packed_template_matches_standalone": True, "reference_files_unchanged": references, "previews": previews, "integration": {"path": str(INCOMING), "size": metadata["size"], "entrance": metadata["entrance"], "interior": metadata["interior"], "files": copies, "native_roundtrip": "PASS"}, "core_modified": False, "deployed": False, "worlds_modified": False, "live_gameplay_tested": False}
    write_json(ROOT / "qa/delivery-verification.json", report)
    print(json.dumps({"status": "PASS", "main_blocks": report["main_blocks"], "parsed_commands": report["parsed_commands"], "integration": str(INCOMING), "preview_images_checked": len(previews), "report": str(ROOT / "qa/delivery-verification.json")}, ensure_ascii=False))


if __name__ == "__main__":
    main()
