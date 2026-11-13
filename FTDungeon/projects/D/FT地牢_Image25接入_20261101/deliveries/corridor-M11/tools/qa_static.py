import argparse
import hashlib
import json
import sys
from collections import Counter, deque
from pathlib import Path

sys.dont_write_bytecode = True
NBT_TOOLS = Path("C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools")
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure, plain, read_nbt

ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M11-bone-crypt-overgrown.png")
REFERENCE_HASH = "78c7104c420851a657b80c0e4194028b8d57f07a42721f6c7989925d0dd25e00"
FORBIDDEN = {"command_block", "chain_command_block", "repeating_command_block", "structure_block", "structure_void", "jigsaw", "spawner", "fire", "soul_fire", "water", "chest", "barrel", "hopper", "dispenser", "dropper"}
NON_FULL = {"air", "candle", "cave_vines", "cave_vines_plant", "skeleton_skull", "dark_oak_trapdoor", "smooth_sandstone_stairs"}


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--finalize", action="store_true")
    arguments = parser.parse_args()
    structure = Structure.read(ROOT / "room.nbt")
    _, raw = read_nbt(ROOT / "room.nbt")
    checks = []

    def check(name, condition, detail):
        checks.append({"check": name, "passed": bool(condition), "detail": detail})

    def name(position):
        return structure.get(position).name.removeprefix("minecraft:")

    def full(position):
        return name(position) not in NON_FULL and name(position) != "lava"

    def walkable(position):
        pos_x, pos_y, pos_z = position
        return name(position) == "air" and name((pos_x, pos_y + 1, pos_z)) == "air" and full((pos_x, pos_y - 1, pos_z))

    check("reference_sha256", digest(REFERENCE) == REFERENCE_HASH, REFERENCE_HASH)
    check("gzip_single_palette_java_1_20_1", (ROOT / "room.nbt").read_bytes()[:2] == b"\x1f\x8b" and structure.data_version == 3465 and "palettes" not in raw.value, "gzip, DataVersion 3465, one palette")
    check("medium_size", structure.size == (15, 12, 40), {"size": structure.size, "axis_length_chunks": 2.5})
    check("full_volume_explicit", len(structure.blocks) == 7200 and all(all(0 <= value < limit for value, limit in zip(position, structure.size)) for position in structure.blocks), "7200 unique bounded positions, including explicit air")
    check("no_template_entities", not structure.entities, len(structure.entities))
    counts = Counter(block.name.removeprefix("minecraft:") for block in structure.blocks.values())
    check("vanilla_no_forbidden_blocks", all(block.name.startswith("minecraft:") and block.name.removeprefix("minecraft:") not in FORBIDDEN for block in structure.blocks.values()), sorted(FORBIDDEN & set(counts)))
    roof = [(pos_x, 11, pos_z) for pos_x in range(15) for pos_z in range(40)]
    check("complete_roof", all(full(position) for position in roof), {"full_roof_cells": sum(full(position) for position in roof), "required": 600})
    check("closed_parallel_side_walls", all(full((pos_x, pos_y, pos_z)) for pos_x in (0, 14) for pos_y in range(2, 12) for pos_z in range(40)), "No side exits or branches")
    route = [(pos_x, 2, pos_z) for pos_x in range(5, 10) for pos_z in range(40)]
    check("level_five_wide_straight_route", all(walkable(position) and name((position[0], 4, position[2])) == "air" for position in route), {"safe_feet_cells": len(route), "width": 5, "length": 40, "headroom_minimum": 3, "steps": 0})
    check("north_south_ports", all(walkable((pos_x, 2, pos_z)) and all(name((pos_x, pos_y, pos_z)) == "air" for pos_y in range(2, 5)) for pos_x in range(6, 9) for pos_z in (0, 39)), {"entrance": [7, 2, 0], "exit": [7, 2, 39], "minimum_clearance": [3, 3]})
    visited = {(7, 2, 0)}
    pending = deque(visited)
    while pending:
        pos_x, pos_y, pos_z = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            adjacent = (pos_x + delta_x, pos_y, pos_z + delta_z)
            if adjacent not in visited and all(0 <= value < limit for value, limit in zip(adjacent, structure.size)) and walkable(adjacent):
                visited.add(adjacent)
                pending.append(adjacent)
    check("entrance_interior_exit_reciprocal_path", (7, 2, 5) in visited and (7, 2, 39) in visited, {"reachable_level_feet_cells": len(visited), "undirected_graph": True})
    lava = [position for position in structure.blocks if name(position) == "lava"]
    sealed = []
    for pos_x, pos_y, pos_z in lava:
        neighbors = [(pos_x + delta_x, pos_y + delta_y, pos_z + delta_z) for delta_x, delta_y, delta_z in ((1, 0, 0), (-1, 0, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))]
        top = [(pos_x + delta_x, pos_y + 1, pos_z + delta_z) for delta_x in (-1, 0, 1) for delta_z in (-1, 0, 1)]
        sealed.append(all(full(position) or name(position) == "lava" for position in neighbors) and name((pos_x, pos_y + 1, pos_z)) == "orange_stained_glass" and all(full(position) for position in top))
    check("lava_containment_and_fire_escape_cap", len(lava) == 36 and all(sealed), {"source_cells": len(lava), "covered_cells": sum(sealed), "top": "3x3 solid nonflammable cap around each source", "main_lane_exposure": 0})
    skulls = [position for position in structure.blocks if name(position) == "skeleton_skull"]
    candles = [position for position in structure.blocks if name(position) == "candle"]
    check("skull_and_candle_support", all(full((position[0], position[1] - 1, position[2])) for position in skulls + candles), {"skulls": len(skulls), "lit_candle_clusters": len(candles)})
    check("lit_candles", len(candles) == 24 and all(structure.get(position).get("lit") == "true" for position in candles), 24)
    vines = [position for position in structure.blocks if name(position) in ("cave_vines", "cave_vines_plant")]
    check("anchored_mature_greenery", bool(vines) and all(full((position[0], position[1] + 1, position[2])) or name((position[0], position[1] + 1, position[2])) == "cave_vines_plant" for position in vines) and all(structure.get(position).get("age") == "25" for position in vines if name(position) == "cave_vines"), {"cells": len(vines), "mature_tips": counts["cave_vines"], "age": 25})
    check("skull_block_entities_only_no_loot", set(structure.block_entities) == set(skulls) and all(plain(data).get("id") == "minecraft:skull" and not ({"Items", "LootTable", "Command", "SkullOwner"} & set(plain(data))) for data in structure.block_entities.values()), {"block_entities": len(structure.block_entities), "inventories": 0})
    metadata = {
        "id": "corridor-M11", "reference_id": "M11", "name": "蔓生白骨墓廊", "kind": "corridor", "length_class": "medium",
        "size": [15, 12, 40], "entrance": [7, 2, 0], "interior": [7, 2, 5], "exit": [7, 2, 39], "facing": "north",
        "sha256": digest(ROOT / "room.nbt"), "reference": str(REFERENCE), "reference_sha256": REFERENCE_HASH,
        "minecraft": "1.20.1", "data_version": 3465, "generator": "Minecraft Builder / Lodestone ESM recipe",
        "ports": [{"side": "north", "feet": [7, 2, 0], "minimum_width": 3, "minimum_height": 3}, {"side": "south", "feet": [7, 2, 39], "minimum_width": 3, "minimum_height": 3}],
        "main_route": {"x": [5, 9], "feet_y": 2, "z": [0, 39], "width": 5, "height_clear": 3, "turns": 0, "branches": 0, "stairs": 0},
        "interpretation": "12 dark-wood coffin niches, bone rib vault, eroded trim at z14/z28, mossy masonry, mature cave vines, skulls, candles and glass-sealed lava slots; concept art interpreted using 1.20.1 vanilla blocks",
        "placement_policy": {"deploy": False, "player_saves_touched": False, "must_reject_occupied_full_volume": True, "full_volume_check_before_any_write": True, "preserve_generated_player_edits": True, "standalone_no_hall_connection_claim": True},
    }
    write_json(ROOT / "room.json", metadata)
    report = {"status": "PASS" if all(item["passed"] for item in checks) else "FAIL", "sha256": metadata["sha256"], "checks": checks, "block_counts": dict(counts.most_common()), "scope": "Static NBT checks only; native placement reported separately"}
    write_json(ROOT / "qa/static.json", report)
    native_path = ROOT / "qa/forge-placement.json"
    render_path = ROOT / "qa/render.json"
    native = json.loads(native_path.read_text(encoding="utf-8")) if native_path.exists() else {"status": "NOT_RUN"}
    render = json.loads(render_path.read_text(encoding="utf-8")) if render_path.exists() else {"status": "NOT_RUN"}
    success = report["status"] == "PASS" and native.get("status") == "PASS" and native.get("sha256") == metadata["sha256"] and render.get("status") == "PASS" and render.get("sha256") == metadata["sha256"]
    rebuild_path = ROOT / "qa/rebuild.json"
    rebuild = json.loads(rebuild_path.read_text(encoding="utf-8-sig")) if rebuild_path.exists() else {"status": "NOT_RUN"}
    combined = {"id": metadata["id"], "status": "PASS" if success else "INCOMPLETE" if report["status"] == "PASS" else "FAIL", "sha256": metadata["sha256"], "reference_sha256": REFERENCE_HASH, "static": report, "four_view": render, "forge_placement_stability": native, "deterministic_rebuild": rebuild, "deployment": "NOT_DEPLOYED", "production_save_touched": False, "full_modpack_gameplay_test": "NOT_PERFORMED", "evidence_boundary": "Static NBT, software/Lodestone preview and isolated Forge GameTestServer are separate evidence; no production hall integration or player-world placement performed"}
    write_json(ROOT / "qa.json", combined)
    print(json.dumps({"static": report["status"], "combined": combined["status"], "sha256": metadata["sha256"], "counts": dict(counts)}, ensure_ascii=False))
    failures = [item for item in checks if not item["passed"]]
    if failures:
        print(json.dumps(failures, ensure_ascii=False, indent=2))
    if failures or arguments.finalize and not success:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
