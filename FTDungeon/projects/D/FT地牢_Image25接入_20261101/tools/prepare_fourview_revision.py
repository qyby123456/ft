import hashlib
import importlib.util
import json
import shutil
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
REVIEW = ROOT / "four-view-review"
STAGING = REVIEW / "candidate"
ROOMS = ("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")
REVISED = ("L1", "L3", "L4", "S3")
sys.path.insert(0, str(ROOT / "tools"))
from review_four_views import Structure, render


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def check_repairs(room_id, structure):
    if room_id == "L1":
        for block_x, block_z in ((2, 7), (22, 7), (2, 17), (22, 17)):
            for offset in (-1, 0, 1):
                assert structure.get((block_x, 2, block_z + offset)).name == "minecraft:polished_blackstone_bricks"
                assert structure.get((block_x, 3, block_z + offset)).name == "minecraft:iron_bars"
        for block_x in (8, 16):
            assert structure.get((block_x, 2, 22)).name == "minecraft:chiseled_polished_blackstone"
    if room_id == "L3":
        for block_x in (4, 18):
            for block_z in (9, 13):
                for block_y in (11, 12):
                    assert structure.get((block_x, block_y, block_z)).name == "minecraft:polished_basalt"
        for block_x in (7, 15):
            assert structure.get((block_x, 8, 7)).name == "minecraft:air"
            for offset_x in (-1, 1):
                for offset_z in (-1, 1):
                    for block_y in range(7, 13):
                        assert structure.get((block_x + offset_x, block_y, 7 + offset_z)).name == "minecraft:chain"
        for block_x in (3, 19):
            assert structure.get((block_x, 2, 11)).name == "minecraft:polished_blackstone_bricks"
            assert structure.get((block_x, 6, 11)).name == "minecraft:polished_blackstone_bricks"
    if room_id == "L4":
        for block_x in (1, 19):
            for block_z in (8, 9, 13, 14):
                assert structure.get((block_x, 1, block_z)).name == "minecraft:mossy_stone_bricks"
                assert structure.get((block_x, 5, block_z)).name == "minecraft:stone_bricks"
        for block_x, block_z in ((0, 6), (20, 14), (6, 20), (14, 20)):
            assert structure.get((block_x, 8, block_z)).name == "minecraft:dark_oak_log"
    if room_id == "S3":
        assert structure.get((7, 1, 4)).name == "minecraft:lectern"
        assert structure.get((7, 2, 4)).name == "minecraft:air"
        assert (7, 1, 4) in structure.block_entities and (7, 2, 4) not in structure.block_entities


def main():
    for room_id in ROOMS:
        baseline = REVIEW / "baseline" / room_id
        incoming = ROOT / "incoming" / room_id
        assert digest(baseline / "room.nbt") == digest(incoming / "room.nbt"), (room_id, "incoming changed since baseline")
        target = STAGING / "incoming" / room_id
        target.mkdir(parents=True, exist_ok=True)
        for filename in ("room.nbt", "room.json", "preview.png", "qa.json"):
            shutil.copy2(incoming / filename, target / filename)
    module_spec = importlib.util.spec_from_file_location("review_l1_builder", ROOT / "work/L1/build_room.py")
    builder = importlib.util.module_from_spec(module_spec)
    sys.modules[module_spec.name] = builder
    module_spec.loader.exec_module(builder)
    builder.DELIVERY = STAGING / "incoming/L1"
    builder.main()
    for room_id in ("L3", "L4", "S3"):
        source = Path("D:/ft工程文件/FT地牢_第二批_L3_L4_S3_制作_20261101/out") / room_id
        for filename in ("room.nbt", "room.json", "preview.png", "qa.json"):
            shutil.copy2(source / filename, STAGING / "incoming" / room_id / filename)
    records = []
    for room_id in REVISED:
        target = STAGING / "incoming" / room_id
        metadata = json.loads((target / "room.json").read_text(encoding="utf-8"))
        structure = Structure.read(target / "room.nbt")
        check_repairs(room_id, structure)
        image = render(structure, metadata, (-0.65, -1), "四视图修订预览", False, size=(1536, 1120))
        image.save(target / "preview.png")
        before = digest(REVIEW / "baseline" / room_id / "room.nbt")
        after = digest(target / "room.nbt")
        assert after == metadata["sha256"] and before != after
        qa = json.loads((target / "qa.json").read_text(encoding="utf-8"))
        if room_id == "L1":
            qa = json.loads((ROOT / "work/L1/qa/geometry.json").read_text(encoding="utf-8"))
            qa["sha256"] = after
        qa["four_view_revision"] = {"status": "STATIC_REPAIRS_PASS_PENDING_NATIVE", "baseline_sha256": before, "final_sha256": after, "repair_regression_assertions": "PASS", "native_world_placement": "pending integrator", "preview": str(target / "preview.png")}
        qa["preview"] = {"path": str(target / "preview.png"), "sha256": digest(target / "preview.png"), "size": [1536, 1120], "source": "Serialized NBT textured render; not a game screenshot"}
        write_json(target / "qa.json", qa)
        records.append({"id": room_id, "before_sha256": before, "sha256": after, "size": list(structure.size), "source": str(target)})
    write_json(STAGING / "revision.json", {"status": "STATIC_PASS_PENDING_NATIVE_AND_VISUAL", "revised": records, "unchanged": [room_id for room_id in ROOMS if room_id not in REVISED], "deployed": False, "player_saves_touched": False})
    print(json.dumps(records, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
