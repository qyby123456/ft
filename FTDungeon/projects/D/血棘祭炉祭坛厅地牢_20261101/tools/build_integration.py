import argparse
import collections
import copy
import gzip
import hashlib
import json
import shutil
import zipfile
from pathlib import Path

from PIL import ImageDraw

import voxel_preview as engine
from build_chapel import Block, CAGES, PASSABLE, REFERENCE, ROOT, SIZE, solid_top
from render_chapel import isometric
from structure_io import load_structure, nbt_payload, write_json


STAGING = ROOT / "out/integration/M1"
INCOMING = Path("D:/ft工程文件/FT地牢_Image25接入_20261101/incoming/M1")
ENTRANCE = (7, 2, 0)
INTERIOR = (7, 2, 3)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def source_blocks(template):
    result = {}
    for entry in template["blocks"]:
        state = template["palette"][entry["state"]]
        result[tuple(entry["pos"])] = Block(state["Name"].removeprefix("minecraft:"), copy.deepcopy(state.get("Properties", {})), copy.deepcopy(entry.get("nbt")))
    return result


def prepare(blocks):
    for block_x in range(15):
        for block_y in (0, 1):
            blocks[block_x, block_y, 0] = Block("deepslate_bricks", {})
        for block_y in range(2, 10):
            position = (block_x, block_y, 0)
            if 6 <= block_x <= 8 and 2 <= block_y <= 4:
                blocks.pop(position, None)
            else:
                blocks[position] = Block("deepslate_bricks", {})
        for block_y in (10, 11):
            blocks[block_x, block_y, 0] = Block("deepslate_tiles", {})
    for block_x in range(6, 9):
        blocks[block_x, 1, 1] = Block("deepslate_bricks", {})
        for block_y in range(2, 5):
            blocks.pop((block_x, block_y, 1), None)
        for block_y in (5, 6):
            blocks[block_x, block_y, 1] = Block("deepslate_bricks", {})


def walkable(position, blocks):
    block_x, block_y, block_z = position
    support = blocks.get((block_x, block_y - 1, block_z))
    if not (solid_top(support) or support and support.name.endswith("_stairs")):
        return False
    return all(not blocks.get((block_x, block_y + offset_y, block_z)) or blocks[block_x, block_y + offset_y, block_z].name in PASSABLE for offset_y in (0, 1))


def geometry(blocks):
    assert walkable(ENTRANCE, blocks) and walkable(INTERIOR, blocks)
    for block_x in range(6, 9):
        assert solid_top(blocks.get((block_x, 1, 0)))
        for block_y in range(2, 5):
            assert (block_x, block_y, 0) not in blocks
            assert (block_x, block_y, 1) not in blocks
    assert all((block_x, block_y, block_z) in blocks for block_x in range(15) for block_y in (10, 11) for block_z in range(15))
    for block_z in range(15):
        for block_y in range(2, 10):
            assert (0, block_y, block_z) in blocks and (14, block_y, block_z) in blocks
    assert all((block_x, block_y, 14) in blocks for block_x in range(15) for block_y in range(2, 10))
    pending = collections.deque([ENTRANCE])
    reachable = {ENTRANCE}
    while pending:
        block_x, block_y, block_z = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for offset_y in (0, 1, -1):
                target = (block_x + offset_x, block_y + offset_y, block_z + offset_z)
                if target in reachable or not all(0 <= target[axis] < SIZE[axis] for axis in range(3)):
                    continue
                if offset_y == 1 and blocks.get((block_x, block_y + 2, block_z)):
                    continue
                if offset_y == -1 and blocks.get((target[0], block_y + 1, target[2])):
                    continue
                if walkable(target, blocks):
                    reachable.add(target)
                    pending.append(target)
    targets = {"safe_interior": INTERIOR, "brazier_side": (6, 4, 7), "altar_front": (7, 2, 5), "totem_front": (7, 2, 12), "relic_door_inside": (12, 2, 9)}
    assert all(position in reachable for position in targets.values()), targets
    cage_anchors = []
    for block_x, block_z in CAGES:
        chain = blocks.get((block_x, 9, block_z))
        anchored = chain and chain.name == "chain" and solid_top(blocks.get((block_x, 10, block_z)))
        assert anchored
        cage_anchors.append({"chain": [block_x, 9, block_z], "anchor": [block_x, 10, block_z], "status": "PASS"})
    fire_positions = [position for position, block in blocks.items() if block.name == "campfire"]
    wood_positions = [position for position, block in blocks.items() if block.name in ("dark_oak_log", "barrel", "lectern")]
    distance = min(max(abs(fire[axis] - wood[axis]) for axis in range(3)) for fire in fire_positions for wood in wood_positions)
    assert distance >= 4
    assert all(block.name not in ("fire", "soul_fire", "lava") for block in blocks.values())
    return {"status": "PASS", "entrance_opening_wh": [3, 3], "opening_depth": 2, "threshold_support_in_template": True, "full_walls_and_roof": True, "reachable_positions": len(reachable), "routes": {name: {"foot": list(position), "reachable": position in reachable} for name, position in targets.items()}, "hanging_cage_anchors": cage_anchors, "campfire_to_wood_chebyshev_min": distance, "spreading_fire_or_lava": False, "entities": 0, "door_assumption": "Side relic-room iron door opens through its two supported pressure plates; no runtime redstone simulation."}


def encode(blocks):
    palette, indexes, entries = [], {}, []
    for position, block in sorted(blocks.items()):
        state = block.state()
        key = json.dumps(state, sort_keys=True)
        if key not in indexes:
            indexes[key] = len(palette)
            palette.append(state)
        entry = {"pos": list(position), "state": indexes[key]}
        if block.data is not None:
            entry["nbt"] = block.data
        entries.append(entry)
    template = {"DataVersion": 3465, "size": list(SIZE), "palette": palette, "blocks": entries, "entities": []}
    target = STAGING / "room.nbt"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(gzip.compress(b"\x0a\x00\x00" + nbt_payload(template)[1], mtime=0))
    assert load_structure(target) == template
    return template, target


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--publish", action="store_true")
    arguments = parser.parse_args()
    source = ROOT / "out/FT_Bloodthorn_Chapel.nbt"
    source_hash = digest(source)
    blocks = source_blocks(load_structure(source))
    prepare(blocks)
    checks = geometry(blocks)
    template, target = encode(blocks)
    metadata = {"id": "M1", "name": "血棘祭炉祭坛厅", "size": list(SIZE), "entrance": list(ENTRANCE), "interior": list(INTERIOR), "facing": "north", "sha256": digest(target), "reference": str(REFERENCE)}
    write_json(STAGING / "room.json", metadata)
    engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
    render_blocks = {tuple(entry["pos"]): template["palette"][entry["state"]] for entry in template["blocks"]}
    image = isometric(render_blocks, size=(1800, 1450))
    ImageDraw.Draw(image).text((48, 82), "M1 接入版 · 北向3×3开口 · 入口脚部(7,2,0) · 门槛内置", font=engine.font(24), fill="#d47670")
    image.save(STAGING / "preview.png")
    native_path = ROOT / "qa/minecraft-format-verification.json"
    native = json.loads(native_path.read_text(encoding="utf-8")) if native_path.is_file() else {}
    verified = native.get("integration", {})
    native_ok = native.get("status") == "PASS" and verified.get("sha256") == digest(target) and verified.get("roundtrip") == "PASS" and native.get("package_sha256") == digest(ROOT / "out/ft-bloodthorn-chapel-1.20.1.zip")
    report = {"status": "PASS" if native_ok else "GEOMETRY_PASS_NATIVE_PENDING", "id": "M1", "producer": "bloodthorn-chapel-20261101", "room_sha256": digest(target), "size": list(SIZE), "blocks": len(template["blocks"]), "palette": len(template["palette"]), "geometry": checks, "native_format": verified if native_ok else {"status": "PENDING"}, "reference_sha256": digest(REFERENCE), "source_standalone_sha256": source_hash, "standalone_unchanged": digest(source) == source_hash, "core_modified": False, "deployed": False, "worlds_modified": False, "scope": "Independent M1 import candidate only. Not a live-world gameplay test; no core edits, deployment, rewards or mob runtime."}
    write_json(STAGING / "qa.json", report)
    if arguments.publish:
        assert native_ok, "Native validation of these exact integration NBT bytes is required before publishing"
        INCOMING.mkdir(parents=True, exist_ok=True)
        for filename in ("room.nbt", "room.json", "preview.png", "qa.json"):
            destination = INCOMING / filename
            if destination.exists() and destination.read_bytes() != (STAGING / filename).read_bytes():
                raise FileExistsError(f"Refusing to overwrite different existing handoff: {destination}")
        for filename in ("room.nbt", "room.json", "preview.png", "qa.json"):
            shutil.copyfile(STAGING / filename, INCOMING / filename)
    assert digest(source) == source_hash
    print(json.dumps({"status": report["status"], "staging": str(STAGING), "incoming": str(INCOMING) if arguments.publish else None, "size": list(SIZE), "sha256": digest(target), "geometry": checks}, ensure_ascii=False))


if __name__ == "__main__":
    main()
