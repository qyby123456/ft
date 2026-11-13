import collections
import dataclasses
import gzip
import hashlib
import json
import zipfile
from pathlib import Path

from structure_io import Byte, load_structure, nbt_payload, write_json


ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "out/datapack"
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-room-bloodthorn-chapel-20261101/out/dungeon-room-bloodthorn-chapel-hero.png")
ATTACHMENT = Path("C:/Users/YOUR_USERNAME/AppData/Local/Temp/codex-clipboard-46e826e0-bfd7-45db-b846-e5560fc2459c.png")
NAMESPACE = "ftbloodthorn"
SIZE = (15, 12, 15)
BLOCKS = {}
CAGES = ((2, 7), (12, 7), (3, 11), (11, 11))
PASSABLE = {"red_carpet", "stone_pressure_plate", "iron_door"}
NON_SOLID = PASSABLE | {"candle", "skeleton_skull", "campfire", "iron_bars", "chain", "lantern", "crimson_fence", "weeping_vines", "weeping_vines_plant", "cobweb", "red_wall_banner", "lectern"}


@dataclasses.dataclass
class Block:
    name: str
    properties: dict
    data: dict | None = None

    def state(self):
        result = {"Name": "minecraft:" + self.name}
        if self.properties:
            result["Properties"] = self.properties
        return result


def put(block_x, block_y, block_z, name, data=None, **properties):
    position = (block_x, block_y, block_z)
    assert all(0 <= position[axis] < SIZE[axis] for axis in range(3)), position
    BLOCKS[position] = Block(name, {key: str(value).lower() for key, value in properties.items()}, data)


def box(minimum, maximum, name, **properties):
    for block_x in range(minimum[0], maximum[0] + 1):
        for block_y in range(minimum[1], maximum[1] + 1):
            for block_z in range(minimum[2], maximum[2] + 1):
                put(block_x, block_y, block_z, name, **properties)


def clear(minimum, maximum):
    for position in list(BLOCKS):
        if all(minimum[axis] <= position[axis] <= maximum[axis] for axis in range(3)):
            del BLOCKS[position]


def noise(block_x, block_y, block_z):
    return abs(block_x * 73856093 ^ block_y * 19349663 ^ block_z * 83492791) % 100


def stairs(block_x, block_y, block_z, facing, half="bottom"):
    put(block_x, block_y, block_z, "deepslate_brick_stairs", facing=facing, half=half, shape="straight", waterlogged=False)


def trapdoor(block_x, block_y, block_z, facing="north", half="bottom", opened=False):
    put(block_x, block_y, block_z, "iron_trapdoor", facing=facing, half=half, open=opened, powered=False, waterlogged=False)


def candle(block_x, block_y, block_z, count=3):
    put(block_x, block_y, block_z, "candle", candles=count, lit=True, waterlogged=False)


def skull(block_x, block_y, block_z, rotation=0):
    put(block_x, block_y, block_z, "skeleton_skull", rotation=rotation)


def banner(block_x, block_y, block_z, facing="north"):
    data = {"id": "minecraft:banner", "Patterns": [{"Pattern": "cs", "Color": 15}, {"Pattern": "ms", "Color": 15}, {"Pattern": "bts", "Color": 15}]}
    put(block_x, block_y, block_z, "red_wall_banner", data=data, facing=facing)


def room_shell():
    box((0, 0, 1), (14, 0, 14), "deepslate_bricks")
    for block_x in range(15):
        for block_z in range(1, 15):
            value = noise(block_x, 1, block_z)
            material = "cracked_deepslate_bricks" if value < 33 else "deepslate_bricks"
            if value > 86 and 1 < block_x < 13 and 2 < block_z < 14:
                material = "red_nether_bricks" if value % 2 else "red_terracotta"
            if block_x == 7 and block_z < 6:
                material = "red_nether_bricks"
            put(block_x, 1, block_z, material)
            if block_x in (0, 14) or block_z in (1, 14):
                for block_y in range(2, 10):
                    material = "cracked_deepslate_bricks" if noise(block_x, block_y, block_z) < 27 else "deepslate_bricks"
                    put(block_x, block_y, block_z, material)
    box((0, 10, 1), (14, 11, 14), "deepslate_tiles")
    for block_z in (3, 11):
        box((1, 10, block_z), (13, 10, block_z), "dark_oak_log", axis="x")
    for block_x in (3, 11):
        box((block_x, 10, 2), (block_x, 10, 13), "dark_oak_log", axis="z")
    for block_x in (1, 13):
        for block_z in (2, 7, 13):
            box((block_x, 2, block_z), (block_x, 8, block_z), "deepslate_bricks")
            put(block_x, 2, block_z, "chiseled_deepslate")
            put(block_x, 7, block_z, "chiseled_deepslate")
    clear((6, 2, 1), (8, 5, 1))
    clear((7, 6, 1), (7, 6, 1))
    stairs(6, 5, 1, "east", "top")
    stairs(8, 5, 1, "west", "top")
    for block_x in range(6, 9):
        stairs(block_x, 0, 0, "south")
        stairs(block_x, 1, 1, "south")
    for block_x in (5, 9):
        put(block_x, 1, 1, "chiseled_deepslate")
        candle(block_x, 2, 1, 2)


def altar():
    box((5, 2, 6), (9, 2, 10), "deepslate_bricks")
    for block_x in range(5, 10):
        stairs(block_x, 2, 6, "south")
        stairs(block_x, 2, 10, "north")
    for block_z in range(7, 10):
        stairs(5, 2, block_z, "east")
        stairs(9, 2, block_z, "west")
    box((6, 3, 7), (8, 3, 9), "red_nether_bricks")
    for block_x, block_z in ((6, 7), (8, 7), (6, 9), (8, 9)):
        put(block_x, 3, block_z, "red_stained_glass")
    put(7, 3, 8, "chiseled_polished_blackstone")
    put(7, 4, 8, "campfire", data={"id": "minecraft:campfire", "Items": []}, facing="north", lit=True, signal_fire=False, waterlogged=False)
    for block_x, block_z, facing in ((6, 8, "west"), (8, 8, "east"), (7, 7, "north"), (7, 9, "south")):
        trapdoor(block_x, 4, block_z, facing, opened=True)
    for block_x in (5, 9):
        for block_z in (5, 11):
            put(block_x, 2, block_z, "chiseled_deepslate")
            put(block_x, 3, block_z, "polished_deepslate")
            candle(block_x, 4, block_z, 4)
    for block_x, block_z in ((6, 4), (7, 4), (8, 4), (7, 5), (3, 8), (3, 9), (4, 8), (10, 12), (9, 12), (8, 12), (12, 5), (11, 6)):
        put(block_x, 2, block_z, "red_carpet")


def suspended_cages():
    for center_x, center_z in CAGES:
        for block_x in range(center_x - 1, center_x + 2):
            for block_z in range(center_z - 1, center_z + 2):
                put(block_x, 5, block_z, "nether_brick_slab", type="top", waterlogged=False)
                put(block_x, 8, block_z, "nether_brick_slab", type="bottom", waterlogged=False)
                if block_x != center_x or block_z != center_z:
                    box((block_x, 6, block_z), (block_x, 7, block_z), "iron_bars")
        put(center_x, 9, center_z, "chain", axis="y", waterlogged=False)
        put(center_x, 10, center_z, "dark_oak_log", axis="x")
        put(center_x, 6, center_z, "lantern", hanging=False, waterlogged=False)


def thorn_totem():
    box((7, 2, 13), (7, 8, 13), "dark_oak_log", axis="y")
    for block_x, block_y in ((4, 5), (4, 6), (5, 3), (5, 4), (5, 7), (6, 2), (6, 8), (8, 2), (8, 8), (9, 3), (9, 4), (9, 7), (10, 5), (10, 6)):
        put(block_x, block_y, 13, "crimson_hyphae", axis="y")
    for block_x, block_y in ((6, 5), (8, 5), (6, 6), (8, 6), (6, 7), (8, 7)):
        put(block_x, block_y, 13, "crimson_fence")
    put(7, 6, 12, "red_nether_bricks")
    skull(7, 7, 12)
    for block_x in (2, 12):
        banner(block_x, 7, 13)
    banner(1, 7, 4, "east")
    banner(13, 7, 4, "west")
    for block_x, block_z in ((1, 3), (13, 3), (1, 11), (13, 11)):
        put(block_x, 9, block_z, "dark_oak_log", axis="x")
        for block_y in (8, 7):
            put(block_x, block_y, block_z, "weeping_vines_plant")
        put(block_x, 6, block_z, "weeping_vines", age=25)


def skull_shelves():
    for block_x, rotation in ((1, 4), (13, 12)):
        for block_z in (5, 12):
            for offset_z in (-1, 0):
                put(block_x, 2, block_z + offset_z, "chiseled_deepslate")
                put(block_x, 4, block_z + offset_z, "deepslate_brick_slab", type="top", waterlogged=False)
            skull(block_x, 3, block_z, rotation)
            candle(block_x, 3, block_z - 1, 3)
            skull(block_x, 5, block_z - 1, rotation)
            candle(block_x, 5, block_z, 2)
    for block_x, block_z in ((2, 10), (12, 12)):
        put(block_x, 2, block_z, "bone_block", axis="y")
        skull(block_x, 3, block_z, 2 if block_x < 7 else 14)
    for block_x, block_z in ((4, 2), (10, 2), (2, 8), (10, 13)):
        candle(block_x, 2, block_z, 3)
    for block_x, block_z in ((2, 2), (12, 2), (2, 12)):
        put(block_x, 2, block_z, "barrel", data={"id": "minecraft:barrel", "Items": [], "CustomName": json.dumps({"text": "封存祭器 · 空"}, ensure_ascii=False)}, facing="up", open=False)


def relic_alcove():
    box((11, 2, 8), (11, 3, 10), "iron_bars")
    box((13, 2, 8), (13, 3, 10), "iron_bars")
    box((11, 4, 8), (13, 4, 8), "deepslate_bricks")
    for block_y, half in ((2, "lower"), (3, "upper")):
        put(12, block_y, 8, "iron_door", facing="south", half=half, hinge="left", open=False, powered=False)
    for block_z in (7, 9):
        put(12, 2, block_z, "stone_pressure_plate", powered=False)
    put(12, 1, 10, "crying_obsidian")
    put(12, 2, 10, "barrel", data={"id": "minecraft:barrel", "Items": [], "CustomName": json.dumps({"text": "祭品预留 · 空"}, ensure_ascii=False)}, facing="up", open=False)


def book_and_details():
    pages = ["血棘礼拜堂\n\n四盏囚灯悬在梁下，火盆永不熄灭。\n\n祭坛后的名字被一遍遍划去，却又在天明之前出现。", "末次值守\n\n木箱里早已没有祭器。\n但每次推开那扇铁门，仍会听见锁链在墙内移动。"]
    item = {"id": "minecraft:written_book", "Count": Byte(1), "tag": {"title": "血棘祭仪残卷", "author": "无名守炉者", "pages": [json.dumps({"text": page}, ensure_ascii=False) for page in pages]}}
    put(2, 2, 7, "lectern", data={"id": "minecraft:lectern", "Book": item, "Page": 0}, facing="east", has_book=True, powered=False)
    for block_x, block_y, block_z in ((1, 8, 2), (13, 8, 12), (2, 8, 13)):
        put(block_x, block_y, block_z, "cobweb")


def solid_top(block):
    if block is None or block.name in NON_SOLID:
        return False
    if block.name.endswith("_slab"):
        return block.properties["type"] in ("top", "double")
    if block.name.endswith("_stairs"):
        return block.properties["half"] == "top"
    if block.name.endswith("_trapdoor"):
        return block.properties["open"] == "false" and block.properties["half"] == "top"
    return True


def connections():
    for (block_x, block_y, block_z), block in BLOCKS.items():
        if block.name not in ("iron_bars", "crimson_fence"):
            continue
        for direction, delta_x, delta_z in (("north", 0, -1), ("south", 0, 1), ("east", 1, 0), ("west", -1, 0)):
            neighbor = BLOCKS.get((block_x + delta_x, block_y, block_z + delta_z))
            connected = neighbor and (neighbor.name == block.name or solid_top(neighbor))
            block.properties[direction] = str(bool(connected)).lower()
        block.properties["waterlogged"] = "false"


def walkable(position):
    block_x, block_y, block_z = position
    support = BLOCKS.get((block_x, block_y - 1, block_z))
    if not (solid_top(support) or support and support.name.endswith("_stairs")):
        return False
    for offset_y in (0, 1):
        obstruction = BLOCKS.get((block_x, block_y + offset_y, block_z))
        if obstruction and obstruction.name not in PASSABLE:
            return False
    return True


def verify_geometry():
    failures = []
    start = (7, 1, 0)
    assert walkable(start)
    reachable = {start}
    pending = collections.deque([start])
    while pending:
        block_x, block_y, block_z = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for delta_y in (0, 1, -1):
                target = (block_x + delta_x, block_y + delta_y, block_z + delta_z)
                if target in reachable or not all(0 <= target[axis] < SIZE[axis] for axis in range(3)):
                    continue
                if delta_y == 1 and BLOCKS.get((block_x, block_y + 2, block_z)):
                    continue
                if delta_y == -1 and BLOCKS.get((target[0], block_y + 1, target[2])):
                    continue
                if walkable(target):
                    reachable.add(target)
                    pending.append(target)
    targets = {"入口": (7, 2, 2), "祭坛前": (7, 2, 3), "祭坛台面": (6, 4, 7), "祭坛后侧": (7, 2, 11), "图腾前": (7, 2, 12), "左侧骨龛": (2, 2, 5), "右侧骨龛": (12, 2, 5), "右侧铁门": (12, 2, 8), "铁门内踏板": (12, 2, 9), "左侧残卷": (2, 2, 6), "左后储物": (3, 2, 12), "右前储物": (12, 2, 3)}
    for title, position in targets.items():
        if position not in reachable:
            failures.append(f"Unreachable: {title}: {position}")
    supported = 0
    for (block_x, block_y, block_z), block in BLOCKS.items():
        below = BLOCKS.get((block_x, block_y - 1, block_z))
        above = BLOCKS.get((block_x, block_y + 1, block_z))
        if block.name in {"candle", "skeleton_skull", "red_carpet", "stone_pressure_plate", "lectern", "campfire", "lantern"}:
            supported += 1
            if not solid_top(below):
                failures.append(f"Unsupported: {block.name}: {(block_x, block_y, block_z)}")
        if block.name == "chain" and (not above or above.name != "chain" and not solid_top(above)):
            failures.append(f"Unanchored chain: {(block_x, block_y, block_z)}")
        if block.name == "iron_door" and block.properties["half"] == "lower":
            if not above or above.name != "iron_door" or above.properties["half"] != "upper":
                failures.append("Missing upper iron door")
            for offset_z in (-1, 1):
                plate = BLOCKS.get((block_x, block_y, block_z + offset_z))
                if not plate or plate.name != "stone_pressure_plate":
                    failures.append("Missing two-sided pressure plate")
        if block.name.endswith("_wall_banner"):
            support_offsets = {"north": (0, 1), "south": (0, -1), "east": (-1, 0), "west": (1, 0)}
            offset_x, offset_z = support_offsets[block.properties["facing"]]
            if not solid_top(BLOCKS.get((block_x + offset_x, block_y, block_z + offset_z))):
                failures.append(f"Unsupported banner: {(block_x, block_y, block_z)}")
    cage_checks = []
    for center_x, center_z in CAGES:
        counts = collections.Counter()
        for block_x in range(center_x - 1, center_x + 2):
            for block_z in range(center_z - 1, center_z + 2):
                for block_y in range(5, 9):
                    state = BLOCKS.get((block_x, block_y, block_z))
                    if state:
                        counts[state.name] += 1
        valid = counts["nether_brick_slab"] == 18 and counts["iron_bars"] == 16 and counts["lantern"] == 1
        cage_checks.append({"center_xz": [center_x, center_z], "status": "PASS" if valid else "FAIL", "blocks": dict(counts)})
        if not valid:
            failures.append(f"Damaged cage: {(center_x, center_z)}: {counts}")
    result = {"status": "FAIL" if failures else "PASS", "size_xyz": list(SIZE), "room_footprint_xz": [15, 15], "wall_height": 8, "altar_footprint_xz": [5, 5], "block_count": len(BLOCKS), "reachable_positions": len(reachable), "two_block_clear_routes": {name: position in reachable for name, position in targets.items()}, "supported_details": supported, "hanging_cages": cage_checks, "empty_storage": all(not block.data["Items"] for block in BLOCKS.values() if block.name == "barrel"), "light_sources": sum(block.name in {"campfire", "lantern", "candle", "crying_obsidian"} for block in BLOCKS.values()), "door_assumption": "Native iron door opens on either stone pressure plate. No redstone runtime emulated.", "entities": 0, "failures": failures}
    write_json(ROOT / "qa/geometry.json", result)
    assert not failures, failures
    return result


def write_structure():
    palette, indexes, entries = [], {}, []
    for position, block in sorted(BLOCKS.items()):
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
    target = PACK / f"data/{NAMESPACE}/structures/chapel.nbt"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(gzip.compress(b"\x0a\x00\x00" + nbt_payload(template)[1], mtime=0))
    assert load_structure(target) == template
    (ROOT / "out/FT_Bloodthorn_Chapel.nbt").write_bytes(target.read_bytes())
    return template, target


def function(name, lines):
    target = PACK / f"data/{NAMESPACE}/functions/{name}.mcfunction"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text("\n".join(lines) + "\n", encoding="utf-8")


def tell(text, color="gold"):
    return "tellraw @s " + json.dumps({"text": text, "color": color}, ensure_ascii=False)


def build_functions():
    write_json(PACK / "pack.mcmeta", {"pack": {"pack_format": 15, "description": "血棘祭炉祭坛厅 · 参考拼图第三张 M1 · 原版1.20.1建筑"}})
    write_json(PACK / "data/minecraft/tags/functions/load.json", {"values": ["ftbloodthorn:load"]})
    write_json(PACK / f"data/{NAMESPACE}/tags/blocks/air.json", {"replace": False, "values": ["minecraft:air", "minecraft:cave_air", "minecraft:void_air"]})
    function("load", ["scoreboard objectives add ftbc.build dummy"])
    function("help", [tell("血棘祭炉祭坛厅：15×15格，墙高8格，含基础与屋顶需15×12×15空气区域。入口朝-Z。", "red"), tell("在空域西北角、最低基础高度执行 /function ftbloodthorn:build。主地面比执行高度高2格。"), tell("先在测试存档使用。仅建筑、无挑战/刷怪/奖励联动；营火接触会造成伤害。", "gray")])
    function("build", ["execute if entity @s[type=minecraft:player] align xyz run function ftbloodthorn:internal/build"])
    lines = ["scoreboard players set #ok ftbc.build 1", "scoreboard players set #placed ftbc.build 0", "tag @s add ftbc.builder"]
    for block_x in (0, 14):
        for block_z in (0, 14):
            lines.append(f"execute unless loaded ~{block_x} ~0 ~{block_z} run scoreboard players set #ok ftbc.build 0")
    for coordinates in ("~0 ~0 ~0", "~14 ~11 ~14"):
        lines.extend(["scoreboard players set #bounds ftbc.build 0", f"execute store success score #bounds ftbc.build if block {coordinates} #ftbloodthorn:air", "execute if score #bounds ftbc.build matches 0 run scoreboard players set #ok ftbc.build 0"])
    lines.extend(["execute if entity @e[tag=!ftbc.builder,dx=14,dy=11,dz=14] run scoreboard players set #ok ftbc.build 0", "execute if score #ok ftbc.build matches 1 run function ftbloodthorn:internal/scan", "execute if score #ok ftbc.build matches 1 store success score #placed ftbc.build run place template ftbloodthorn:chapel ~ ~ ~ none none 1.0 0", "tag @s remove ftbc.builder", "execute if score #placed ftbc.build matches 1 run tp @s ~7.5 ~2 ~2.5 0 0", "execute if score #placed ftbc.build matches 1 run " + tell("血棘祭炉祭坛厅已完成：五格祭坛、四只吊笼、血棘图腾、红幡、烛龛和侧门遗物龛。"), "execute if score #ok ftbc.build matches 0 run " + tell("拒绝建造：空域有方块/其它实体、区块未加载或高度越界。未放置结构。", "red"), "execute if score #ok ftbc.build matches 1 if score #placed ftbc.build matches 0 run " + tell("模板放置失败，请检查服务器日志和数据包资源。", "red")])
    function("internal/build", lines)
    function("internal/scan", [f"execute if score #ok ftbc.build matches 1 run function ftbloodthorn:internal/scan_{block_y:02d}" for block_y in range(SIZE[1])])
    for block_y in range(SIZE[1]):
        function(f"internal/scan_{block_y:02d}", [f"execute unless block ~{block_x} ~{block_y} ~{block_z} #ftbloodthorn:air run scoreboard players set #ok ftbc.build 0" for block_x in range(SIZE[0]) for block_z in range(SIZE[2])])


def verify_pack():
    errors, commands = [], []
    for path in PACK.rglob("*.json"):
        json.loads(path.read_text(encoding="utf-8"))
    json.loads((PACK / "pack.mcmeta").read_text(encoding="utf-8"))
    for path in PACK.rglob("*.mcfunction"):
        for line in path.read_text(encoding="utf-8").splitlines():
            if not line:
                continue
            commands.append(line)
            if " run function " in line:
                identifier = line.rsplit(" run function ", 1)[1]
                target = PACK / "data" / identifier.replace(":", "/functions/")
                if not target.with_suffix(".mcfunction").is_file():
                    errors.append("Missing function: " + identifier)
    assert sum(line.startswith("execute unless block ") for line in commands) == SIZE[0] * SIZE[1] * SIZE[2]
    assert len(commands) < 65536
    assert not any(line.startswith(("fill ", "kill ", "summon ", "forceload ", "gamerule ")) for line in commands)
    assert not (PACK / "data/minecraft/tags/functions/tick.json").exists()
    result = {"status": "FAIL" if errors else "PASS", "commands": len(commands), "air_scan_cells": SIZE[0] * SIZE[1] * SIZE[2], "max_chain_upper_bound": len(commands), "default_chain_limit": 65536, "automatic_build": False, "tick_function": False, "worldgen": False, "errors": errors}
    write_json(ROOT / "qa/datapack.json", result)
    assert not errors, errors
    return result


def main():
    BLOCKS.clear()
    for step in (room_shell, altar, suspended_cages, thorn_totem, skull_shelves, relic_alcove, book_and_details, connections):
        step()
    geometry = verify_geometry()
    template, structure = write_structure()
    build_functions()
    checks = verify_pack()
    materials = collections.Counter(block.name for block in BLOCKS.values())
    write_json(ROOT / "out/materials.json", dict(materials.most_common()))
    write_json(ROOT / "qa/blueprint.json", template)
    archive = ROOT / "out/ft-bloodthorn-chapel-1.20.1.zip"
    with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED) as bundle:
        for path in sorted(PACK.rglob("*")):
            if path.is_file():
                info = zipfile.ZipInfo(path.relative_to(PACK).as_posix(), (2026, 11, 1, 0, 0, 0))
                info.compress_type = zipfile.ZIP_DEFLATED
                bundle.writestr(info, path.read_bytes())
    references = [{"path": str(path), "sha256": hashlib.sha256(path.read_bytes()).hexdigest()} for path in (REFERENCE, ATTACHMENT) if path.is_file()]
    manifest = {"title": "血棘祭炉祭坛厅", "reference_panel": "图三 / M1 Bloodthorn Chapel", "minecraft": "1.20.1", "namespace": NAMESPACE, "size_xyz": list(SIZE), "footprint_xz": [15, 15], "wall_height": 8, "walk_height_from_origin": 2, "entrance": [7.5, 2, 2.5], "references": references, "geometry": geometry, "datapack": checks, "materials": len(materials), "palette_states": len(template["palette"]), "structure_sha256": hashlib.sha256(structure.read_bytes()).hexdigest(), "archive_sha256": hashlib.sha256(archive.read_bytes()).hexdigest(), "installed": False, "worlds_modified": False, "paid_image_requests": 0, "scope": "Standalone architecture and one written book; no mobs, Boss runtime, rewards or worldgen. Vanilla campfire/iron frame used instead of unregistered custom brazier."}
    write_json(ROOT / "out/manifest.json", manifest)
    print(json.dumps({"archive": str(archive), "structure": str(structure), "blocks": len(BLOCKS), "palette": len(template["palette"]), "geometry": geometry["status"], "datapack": checks["status"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
