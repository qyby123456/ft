import hashlib
import json
import re
import sys
import zipfile
from pathlib import Path

from audit_s8_submission import decode, load


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    root = Path(sys.argv[1]).resolve()
    prior = Path(sys.argv[2]).resolve()
    records = []
    for name in ("submission-snapshot.json", "supplemental-snapshot.json", "delivery-snapshot.json"):
        records.extend(load(root / name)["files"])
    records = {record["source"]: record for record in records}
    for record in records.values():
        assert digest(Path(record["snapshot"])) == record["sha256"]
    manifest = load(root / "ready-for-review.json")
    old = load(prior / "ready-for-review.json")
    for current, previous in zip(manifest["rooms"], old["rooms"]):
        assert current["id"] == previous["id"] and current["sha256"] == previous["sha256"]
    assert manifest["assembly"]["sha256"] == old["assembly"]["sha256"]
    access = root / "assembly/access"
    contract = load(access / "contract.json")
    assert contract["source_sha256"] == manifest["assembly"]["sha256"]
    package = load(access / "package.json")
    archive = access / "fts6-access-mc1.20.1.zip"
    assert digest(archive) == package["sha256"] == manifest["external_access"]["datapack_sha256"]
    pack = access / "datapack"
    with zipfile.ZipFile(archive) as zipped:
        assert set(zipped.namelist()) == {entry["path"] for entry in package["files"]}
        for entry in package["files"]:
            contents = zipped.read(entry["path"])
            assert hashlib.sha256(contents).hexdigest() == entry["sha256"]
            assert len(contents) == entry["bytes"] and contents == (pack / entry["path"]).read_bytes()
            if entry["path"].startswith("data/fts6_access/"):
                assert contents == (root / "qa/runtime/src/main/resources" / entry["path"]).read_bytes()
    assert load(pack / "data/minecraft/tags/functions/load.json") == {"values": ["fts6_access:load"]}
    assert load(pack / "data/minecraft/tags/functions/tick.json") == {"values": ["fts6_access:tick"]}
    pieces = load(root / "assembly/qa/split-payload-verification.json")["pieces"]
    assert len(pieces) == 20
    functions = pack / "data/fts6_access/functions"
    installation = (functions / "install_once.mcfunction").read_text(encoding="utf-8")
    commands = re.findall(r"^place template fts6_access:(\S+) (~[-\d]+) (~[-\d]+) (~[-\d]+)$", installation, re.M)
    assert len(commands) == len(pieces)
    for piece, command in zip(pieces, commands):
        filename = Path(piece["file"]).name
        expected = root / "assembly/slices" / filename
        packed = pack / "data/fts6_access/structures" / (command[0] + ".nbt")
        assert packed.read_bytes() == expected.read_bytes()
        assert [int(value[1:]) for value in command[1:]] == piece["origin"]
    enter = (functions / "enter.mcfunction").read_text(encoding="utf-8")
    exit_command = (functions / "exit.mcfunction").read_text(encoding="utf-8")
    tick = (functions / "tick.mcfunction").read_text(encoding="utf-8")
    assert "if dimension minecraft:overworld" in tick
    assert "if dimension minecraft:overworld" in enter and "tag=fts6_authorized" in enter and "distance=..2" in enter
    assert "distance=..2" in exit_command and "tag=fts6_authorized" not in exit_command
    captured, blocks = decode(access / "after-access-zone.nbt")
    assert captured["size"] == [13, 8, 9]
    def block(east, height, south):
        return blocks[(east + 10, height, south - 32)]["state"]["Name"]
    assert block(-6, 3, 36) == "minecraft:chiseled_polished_blackstone"
    for east in range(-10, 0):
        for south in range(32, 41):
            assert block(east, 3, south) in {"minecraft:polished_blackstone_bricks", "minecraft:chiseled_polished_blackstone"}
    for east in range(-7, -3):
        for height in (4, 5):
            assert block(east, height, 40) == "minecraft:air"
    for east in (-6, -5):
        for south in (36, 37):
            for height in (4, 5):
                assert block(east, height, south) == "minecraft:air"
    runtime = load(access / "runtime-result.json")
    assert runtime["status"] == "PASS" and runtime["source_sha256"] == manifest["assembly"]["sha256"]
    assert len(runtime["checks"]) == 13
    assert "BUILD SUCCESSFUL" in (root / "qa/runtime/access-run.log").read_text(encoding="utf-8-sig")
    report = {"stage": "INDEPENDENT_ACCESS_PACKAGE_AND_PRODUCER_EVIDENCE_INSPECTION", "source_sha256": manifest["assembly"]["sha256"], "datapack_sha256": digest(archive), "frozen_files_verified": len(records), "unchanged_ten_room_and_merged_hashes": True, "prior_static_runtime_review": str(prior), "archive_files_exact": len(package["files"]), "packaged_slices_exact_and_install_offsets": len(commands), "packaged_namespace_functions_and_structures_equal_test_resources": True, "minecraft_load_tick_tags": "Statically verified in zip; absent from GameTest resources, harness explicitly invokes load/tick", "external_lobby_floor_cells": 90, "external_south_4wide_opening_verified": True, "authorization_and_distance_guards_source_inspected": True, "producer_native_assertions_inspected": len(runtime["checks"]), "runtime_source_sha256": digest(root / "qa/runtime/src/main/java/net/farmingtales/s6qa/AccessTests.java"), "limitations": ["Producer actual GameTest inspected, not independently rerun by parent", "Ordinary datapack reload and automatic tick tag dispatch not exercised by existing harness", "FakePlayer position assertions do not prove network client teleport acknowledgement", "Only empty pre-reserved unrotated overworld single-instance placement is supported", "Full natural movement and container reach/ray coverage still not provided by this access-only change"], "accepted": False}
    destination = root / "independent-qa"
    destination.mkdir()
    (destination / "access-inspection.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
