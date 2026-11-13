import hashlib
import json
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CORE = Path("D:/ft工程文件/FT-Dungeon-Mobs-Forge-1.20.1")
RELEASE = CORE / "output/room-import-20261101-211745"
ROOMS = ("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")


def digest(data):
    return hashlib.sha256(data).hexdigest()


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def main():
    static = load(ROOT / "qa/delivery-review.json")
    native = load(ROOT / "qa/native-review.json")
    assert static["status"] == native["status"] == "PASS"
    assert len(static["rooms"]) == len(native["rooms"]) == 9
    receipt = load(RELEASE / "deployment-receipt.json")
    post = load(RELEASE / "post-deployment-verification.json")
    assert receipt["status"] == "DEPLOYED" and post["status"] == "POST_DEPLOYMENT_PASS"
    installed = Path(receipt["installedJar"])
    jar_bytes = installed.read_bytes()
    jar_hash = digest(jar_bytes)
    assert jar_hash == receipt["sha256"] == post["sha256"]
    backup = Path(receipt["previousBackup"])
    assert digest(backup.read_bytes()) == receipt["previousSha256"] == post["previousSha256"]
    active = list(installed.parent.glob("FT地牢怪物-*.jar"))
    assert active == [installed], "Duplicate FT dungeon runtime JAR"
    game_test = (RELEASE / "gametest-final.log").read_text(encoding="utf-8", errors="replace")
    assert "GALLERY_GAME_TEST_PASS: rooms=9/9, records=49575" in game_test
    assert "validated=9/9, records=49575, rejected=[]" in game_test
    assert "All 2 required tests passed" in game_test and "BUILD SUCCESSFUL" in game_test
    package_data = {}
    records = []
    native_hashes = {room["id"]: room["sha256"] for room in native["rooms"]}
    with zipfile.ZipFile(installed) as jar:
        assert jar.testzip() is None
        for identity in ROOMS:
            folder = ROOT / "incoming" / identity
            metadata = load(folder / "room.json")
            template = (folder / "room.nbt").read_bytes()
            assert digest(template) == metadata["sha256"] == native_hashes[identity]
            template_path = "data/ftdungeonmobs/structures/image25/" + identity.lower() + ".nbt"
            metadata_path = "data/ftdungeonmobs/dungeon_rooms/image25/" + identity.lower() + ".json"
            assert jar.read(template_path) == template
            packed_metadata = json.loads(jar.read(metadata_path))
            for key in ("id", "name", "size", "entrance", "interior", "facing", "sha256"):
                assert packed_metadata[key] == metadata[key], f"JAR metadata differs: {identity} {key}"
            assert "GALLERY_ROUTE_PASS: " + identity + ";" in game_test
            for name in ("room.nbt", "room.json", "preview.png", "qa.json"):
                package_data[f"rooms/{identity}/{name}"] = (folder / name).read_bytes()
            records.append({"id": identity, "name": metadata["name"], "size": metadata["size"], "sha256": metadata["sha256"], "matches_installed_jar": True, "native_route_pass": True})
        catalog = json.loads(jar.read("data/ftdungeonmobs/dungeon_rooms/catalog.json"))
        assert {entry["id"] for entry in catalog["rooms"]} == set(ROOMS)
        assert digest(jar.read("data/ftdungeonmobs/structures/rooms/31.nbt")) == "d28d9a41b7a920c891546368f2d2a78c65ccc864b9e6741c77ecec880ca6bd82"
        assert digest(jar.read("data/ftdungeonmobs/structures/rooms/30_1.nbt")) == "7fbebcac1a2fa99dad419a057f09b85c856e6f5005fc945adce1c44e0b7b35d0"
    result = {"status": "PASS", "deployed_version": receipt["version"], "installed_jar": str(installed), "installed_sha256": jar_hash, "one_active_ft_dungeon_jar": True, "previous_backup_sha256_matches": True, "all_nine_match_installed_jar": True, "static_and_native_format_review": "PASS", "forge_actual_placement_and_routes": "9/9 PASS", "original_31_and_30_1_preserved": True, "rooms": records, "batch3_received_queue": "M5/M6/M7/S5/S6/S7; not built or integrated", "client_or_multiplayer_gameplay_tested": False}
    verification = ROOT / "integration/coordinator-final-verification.json"
    verification.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for relative in ("README.md", "CONTRACT.md", "preview/nine-room-delivery-sheet.png", "qa/delivery-review.json", "qa/native-review.json", "qa/reference-inventory.json", "qa/batch3-image-review.json", "queue/batch3.json", "queue/README.md", "integration/coordinator-final-verification.json"):
        package_data[relative] = (ROOT / relative).read_bytes()
    for name in ("gametest-final.log", "jar-verification.json", "deployment-receipt.json", "post-deployment-verification.json"):
        package_data["release-evidence/" + name] = (RELEASE / name).read_bytes()
    package_data["release/" + installed.name] = jar_bytes
    package_data["SHA256SUMS.json"] = (json.dumps({name: digest(data) for name, data in sorted(package_data.items())}, ensure_ascii=False, indent=2) + "\n").encode("utf-8")
    output = ROOT / "out/FT_Image25_九间接入_20261101.zip"
    output.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for name, data in sorted(package_data.items()):
            archive.writestr(name, data)
    with zipfile.ZipFile(output) as archive:
        assert archive.testzip() is None
        assert set(archive.namelist()) == set(package_data)
        for name, expected in package_data.items():
            assert archive.read(name) == expected, "ZIP roundtrip mismatch: " + name
    archive_report = {"status": "PASS", "path": str(output), "bytes": output.stat().st_size, "sha256": digest(output.read_bytes()), "files": len(package_data), "zip_crc": "PASS", "all_entries_roundtrip": True}
    (ROOT / "qa/package-verification.json").write_text(json.dumps(archive_report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"installed_verification": "PASS", "version": receipt["version"], "rooms": 9, "third_batch_queue": 6, "archive": archive_report}, ensure_ascii=False))


if __name__ == "__main__":
    main()
