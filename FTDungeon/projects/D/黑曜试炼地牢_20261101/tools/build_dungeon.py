import collections
import dataclasses
import gzip
import hashlib
import json
import struct
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "out/datapack"
NAMESPACE = "ftobsidian"
SIZE = (35, 18, 47)
BLOCKS = {}


@dataclasses.dataclass
class Block:
    name: str
    properties: dict

    def state(self):
        value = {"Name": "minecraft:" + self.name}
        if self.properties:
            value["Properties"] = self.properties
        return value


def put(block_x, block_y, block_z, name, **properties):
    BLOCKS[block_x, block_y, block_z] = Block(name, {key: str(value).lower() for key, value in properties.items()})


def cuboid(minimum, maximum, name, **properties):
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


def stairs(block_x, block_y, block_z, facing):
    put(block_x, block_y, block_z, "polished_blackstone_brick_stairs", facing=facing, half="bottom", shape="straight", waterlogged=False)


def lantern(block_x, block_y, block_z, hanging=False):
    put(block_x, block_y, block_z, "lantern", hanging=hanging, waterlogged=False)


def foundation():
    cuboid((0, 0, 12), (34, 1, 46), "polished_blackstone")
    cuboid((10, 0, 2), (24, 1, 13), "polished_blackstone")
    for block_x in range(35):
        for block_z in range(12, 47):
            material = "gilded_blackstone" if noise(block_x, 2, block_z) < 4 else "polished_blackstone_bricks"
            if abs(block_x - 17) <= 2 or abs(block_z - 29) <= 2:
                material = "polished_blackstone"
            put(block_x, 2, block_z, material)
    for block_x in range(10, 25):
        for block_z in range(2, 14):
            put(block_x, 2, block_z, "polished_blackstone" if 14 <= block_x <= 20 else "polished_blackstone_bricks")
    for block_x in range(13, 22):
        stairs(block_x, 0, 0, "south")
        put(block_x, 0, 1, "polished_blackstone")
        stairs(block_x, 1, 1, "south")
        stairs(block_x, 2, 2, "south")


def lava_and_platform():
    for block_x in range(2, 33):
        for block_z in range(14, 45):
            delta_x, delta_z = block_x - 17, block_z - 29
            radius = max(abs(delta_x), abs(delta_z)) + min(abs(delta_x), abs(delta_z)) * 0.5
            bridge = abs(delta_x) <= 2 or abs(delta_z) <= 2
            if 10 <= radius <= 12 and not bridge:
                put(block_x, 1, block_z, "obsidian")
                put(block_x, 2, block_z, "lava", level=0)
            elif (9 <= radius < 10 or 12 < radius <= 13) and not bridge:
                put(block_x, 2, block_z, "gilded_blackstone" if (block_x + block_z) % 4 == 0 else "chiseled_polished_blackstone")
            if max(abs(delta_x), abs(delta_z)) <= 6 and abs(delta_x) + abs(delta_z) <= 10:
                edge = max(abs(delta_x), abs(delta_z)) == 6 or abs(delta_x) + abs(delta_z) == 10
                put(block_x, 3, block_z, "gilded_blackstone" if edge else "polished_blackstone")
    for offset in range(-2, 3):
        stairs(17 + offset, 3, 22, "south")
        stairs(17 + offset, 3, 36, "north")
        stairs(10, 3, 29 + offset, "east")
        stairs(24, 3, 29 + offset, "west")
    for block_x, block_z in ((14, 19), (20, 19), (14, 39), (20, 39), (7, 26), (7, 32), (27, 26), (27, 32)):
        put(block_x, 3, block_z, "chiseled_polished_blackstone")
        lantern(block_x, 4, block_z)


def chamber_walls():
    for block_x in range(35):
        for block_z in range(12, 47):
            if 2 <= block_x <= 32 and 14 <= block_z <= 44:
                continue
            for block_y in range(3, 13):
                material = "polished_blackstone_bricks" if block_y in (3, 4, 11, 12) else "obsidian"
                if block_y in (6, 9) and noise(block_x, block_y, block_z) < 11:
                    material = "crying_obsidian"
                put(block_x, block_y, block_z, material)
    for block_x in (6, 11, 23, 28):
        for block_z in (14, 44):
            cuboid((block_x, 3, block_z), (block_x, 12, block_z), "chiseled_polished_blackstone")
            for block_y in (6, 9):
                put(block_x, block_y, block_z, "crying_obsidian")
            put(block_x, 11, block_z, "gilded_blackstone")
    for block_z in (20, 25, 33, 38):
        for block_x in (2, 32):
            cuboid((block_x, 3, block_z), (block_x, 12, block_z), "chiseled_polished_blackstone")
            for block_y in (6, 9):
                put(block_x, block_y, block_z, "crying_obsidian")
            put(block_x, 11, block_z, "gilded_blackstone")
    clear((14, 3, 12), (20, 8, 14))


def tower(block_x, block_z):
    cuboid((block_x - 1, 3, block_z - 1), (block_x + 1, 4, block_z + 1), "polished_blackstone_bricks")
    cuboid((block_x, 5, block_z), (block_x, 13, block_z), "obsidian")
    for block_y in (6, 10):
        put(block_x, block_y, block_z, "crying_obsidian")
    cuboid((block_x - 1, 12, block_z - 1), (block_x + 1, 12, block_z + 1), "polished_blackstone")
    put(block_x, 14, block_z, "gilded_blackstone")
    put(block_x, 15, block_z, "gold_block")


def roof_height(block_x, block_z):
    distance = max(abs(block_x - 17), abs(block_z - 29))
    return 13 + min(4, max(0, (16 - distance) // 3))


def roof_and_chains():
    for block_x in range(35):
        for block_z in range(12, 47):
            height = roof_height(block_x, block_z)
            rib = block_x == 17 or block_z == 29 or abs(block_x - 17) == abs(block_z - 29)
            put(block_x, height, block_z, "polished_blackstone_bricks" if rib else "deepslate_tiles")
            put(block_x, height - 1, block_z, "polished_blackstone_bricks" if rib else "deepslate_tiles")
    for block_x, block_z in ((11, 23), (23, 23), (11, 35), (23, 35)):
        for block_y in range(10, roof_height(block_x, block_z)):
            put(block_x, block_y, block_z, "chain", axis="y", waterlogged=False)
        lantern(block_x, 9, block_z, True)
    for block_x, block_z in ((4, 16), (30, 16), (4, 42), (30, 42)):
        tower(block_x, block_z)


def brazier():
    cuboid((15, 4, 27), (19, 4, 31), "polished_blackstone_bricks")
    cuboid((16, 5, 28), (18, 5, 30), "polished_blackstone")
    cuboid((17, 6, 29), (17, 7, 29), "chiseled_polished_blackstone")
    put(17, 7, 29, "gold_block")
    cuboid((16, 8, 28), (18, 8, 30), "polished_blackstone")
    cuboid((15, 9, 27), (19, 9, 31), "polished_blackstone_bricks")
    for block_x in range(15, 20):
        for block_z in range(27, 32):
            if block_x in (15, 19) or block_z in (27, 31):
                put(block_x, 10, block_z, "gilded_blackstone")
                put(block_x, 11, block_z, "gold_block")
            else:
                put(block_x, 10, block_z, "netherrack")
                put(block_x, 11, block_z, "fire", age=0, north=False, south=False, east=False, west=False, up=False)
    for block_x in (15, 19):
        for block_z in (27, 31):
            put(block_x, 5, block_z, "gold_block")
            put(block_x, 11, block_z, "gold_block")


def entry_gate():
    cuboid((10, 3, 3), (11, 9, 11), "obsidian")
    cuboid((23, 3, 3), (24, 9, 11), "obsidian")
    for block_x in range(10, 25):
        for block_z in range(3, 12):
            put(block_x, 10 if 13 <= block_x <= 21 else 9, block_z, "deepslate_tiles")
    for block_z in (4, 9):
        for block_x in (12, 22):
            put(block_x, 3, block_z, "chiseled_polished_blackstone")
            put(block_x, 4, block_z, "crying_obsidian")
            lantern(block_x, 5, block_z)
    for block_x in (13, 21):
        cuboid((block_x, 3, 11), (block_x, 9, 13), "obsidian")
        put(block_x, 6, 11, "crying_obsidian")
        put(block_x, 9, 11, "gilded_blackstone")
    cuboid((13, 10, 11), (21, 10, 13), "polished_blackstone_bricks")
    for block_x in range(14, 21):
        for block_y in (8, 9):
            put(block_x, block_y, 12, "iron_bars", north=False, south=False, west=True, east=True, waterlogged=False)
    for block_x in (15, 17, 19):
        put(block_x, 10, 11, "crying_obsidian")
        put(block_x, 11, 11, "amethyst_block")
    for block_x in (14, 20):
        cuboid((block_x, 6, 10), (block_x, 9, 10), "chain", axis="y", waterlogged=False)


def alcoves():
    for block_x in (3, 31):
        for block_z in (23, 35):
            put(block_x, 3, block_z, "chiseled_polished_blackstone")
            lantern(block_x, 4, block_z)
        put(block_x, 3, 29, "barrel", facing="up", open=False)
        for block_z in (27, 31):
            cuboid((block_x, 3, block_z), (block_x, 6, block_z), "obsidian")
            put(block_x, 5, block_z, "crying_obsidian")
    cuboid((14, 3, 41), (20, 3, 43), "polished_blackstone_bricks")
    for block_x in (14, 20):
        cuboid((block_x, 4, 43), (block_x, 7, 43), "obsidian")
        put(block_x, 6, 43, "crying_obsidian")
        lantern(block_x, 8, 43)
    stairs(17, 3, 40, "south")
    put(17, 4, 42, "barrel", facing="north", open=False)
    put(17, 6, 44, "crying_obsidian")
    put(17, 7, 44, "amethyst_block")


def encode_string(value):
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def nbt_payload(value):
    if isinstance(value, dict):
        payload = bytearray()
        for key, child in value.items():
            tag_type, encoded = nbt_payload(child)
            payload.extend(bytes([tag_type]) + encode_string(key) + encoded)
        return 10, bytes(payload) + b"\x00"
    if isinstance(value, list):
        entries = [nbt_payload(child) for child in value]
        tag_type = entries[0][0] if entries else 0
        assert all(entry[0] == tag_type for entry in entries)
        return 9, bytes([tag_type]) + struct.pack(">i", len(entries)) + b"".join(entry[1] for entry in entries)
    if isinstance(value, int):
        return 3, struct.pack(">i", value)
    if isinstance(value, str):
        return 8, encode_string(value)
    raise TypeError(type(value))


def write_structure():
    palette, indexes, entries = [], {}, []
    for position, block in sorted(BLOCKS.items()):
        state = block.state()
        key = json.dumps(state, sort_keys=True)
        if key not in indexes:
            indexes[key] = len(palette)
            palette.append(state)
        entries.append({"pos": list(position), "state": indexes[key]})
    template = {"DataVersion": 3465, "size": list(SIZE), "palette": palette, "blocks": entries, "entities": []}
    path = PACK / f"data/{NAMESPACE}/structures/chamber.nbt"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(gzip.compress(b"\x0a\x00\x00" + nbt_payload(template)[1], mtime=0))
    return template, path


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def function(name, lines):
    path = PACK / f"data/{NAMESPACE}/functions/{name}.mcfunction"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def tell(text, color="gold"):
    return "tellraw @s " + json.dumps({"text": text, "color": color}, ensure_ascii=False)


def build_functions():
    write_json(PACK / "pack.mcmeta", {"pack": {"pack_format": 15, "description": "黑曜试炼 · 35×47×18 原版建筑 / Forge 1.20.1"}})
    write_json(PACK / "data/minecraft/tags/functions/load.json", {"values": [f"{NAMESPACE}:load"]})
    write_json(PACK / f"data/{NAMESPACE}/tags/blocks/air.json", {"replace": False, "values": ["minecraft:air", "minecraft:cave_air", "minecraft:void_air"]})
    function("load", ["scoreboard objectives add ftob.build dummy"])
    function("help", [tell("黑曜试炼：选择前方 +X 35、+Z 47、高 18 格的完整空域；站在西北角执行 /function ftobsidian:build。", "light_purple"), tell("建筑地面比执行高度高 3 格，入口向 -Z；不会自动施工或刷怪。")])
    function("build", ["execute if entity @s[type=minecraft:player] align xyz run function ftobsidian:internal/build"])
    lines = ["scoreboard players set #ok ftob.build 1", "scoreboard players set #placed ftob.build 0", "tag @s add ftob.builder"]
    for block_x in (0, 16, 32, 34):
        for block_z in (0, 16, 32, 46):
            lines.append(f"execute unless loaded ~{block_x} ~0 ~{block_z} run scoreboard players set #ok ftob.build 0")
    for position in ((0, 0, 0), (34, 17, 46)):
        coordinates = " ".join("~" + str(value) for value in position)
        lines.extend(["scoreboard players set #bounds ftob.build 0", f"execute store success score #bounds ftob.build if block {coordinates} #ftobsidian:air", "execute if score #bounds ftob.build matches 0 run scoreboard players set #ok ftob.build 0"])
    lines.append("execute if entity @e[tag=!ftob.builder,dx=34,dy=17,dz=46] run scoreboard players set #ok ftob.build 0")
    lines.append("execute if score #ok ftob.build matches 1 run function ftobsidian:internal/scan")
    lines.append("execute if score #ok ftob.build matches 1 store success score #placed ftob.build run place template ftobsidian:chamber ~ ~ ~ none none 1.0 0")
    lines.extend(["tag @s remove ftob.builder", "execute if score #placed ftob.build matches 1 run tp @s ~17.5 ~3 ~5.5 0 0", "execute if score #placed ftob.build matches 1 run " + tell("黑曜试炼建造完成。四桥可通行；中央火盆为纯建筑，不自动启动挑战。"), "execute if score #ok ftob.build matches 0 run " + tell("拒绝建造：35×47×18 空域不全为空气、存在其它实体、超出高度或区块未加载。没有写入建筑。", "red"), "execute if score #ok ftob.build matches 1 if score #placed ftob.build matches 0 run " + tell("模板放置未成功；请检查服务器日志与数据包资源。", "red")])
    function("internal/build", lines)
    function("internal/scan", [f"execute if score #ok ftob.build matches 1 run function ftobsidian:internal/scan_{block_y:02d}" for block_y in range(SIZE[1])])
    for block_y in range(SIZE[1]):
        function(f"internal/scan_{block_y:02d}", [f"execute unless block ~{block_x} ~{block_y} ~{block_z} #ftobsidian:air run scoreboard players set #ok ftob.build 0" for block_x in range(SIZE[0]) for block_z in range(SIZE[2])])


def walkable(position):
    block_x, block_y, block_z = position
    support = BLOCKS.get((block_x, block_y - 1, block_z))
    return support is not None and support.name not in ("lava", "fire", "chain", "lantern", "iron_bars") and position not in BLOCKS and (block_x, block_y + 1, block_z) not in BLOCKS


def verify_geometry():
    assert all(all(0 <= position[axis] < SIZE[axis] for axis in range(3)) for position in BLOCKS)
    for block_x in range(35):
        for block_z in range(12, 47):
            height = roof_height(block_x, block_z)
            assert (block_x, height, block_z) in BLOCKS
            assert (block_x, height - 1, block_z) in BLOCKS
    lava = [position for position, block in BLOCKS.items() if block.name == "lava"]
    for block_x, block_y, block_z in lava:
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, -1, 0)):
            neighbor = BLOCKS.get((block_x + offset[0], block_y + offset[1], block_z + offset[2]))
            assert neighbor is not None and neighbor.name not in ("fire", "lantern", "chain"), ("Uncontained lava", (block_x, block_y, block_z))
    for position, block in BLOCKS.items():
        if block.name == "fire":
            assert BLOCKS[position[0], position[1] - 1, position[2]].name == "netherrack"
    origin = (17, 3, 5)
    assert walkable(origin)
    reached, pending = {origin}, collections.deque([origin])
    while pending:
        block_x, block_y, block_z = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for delta_y in (0, 1, -1):
                candidate = (block_x + delta_x, block_y + delta_y, block_z + delta_z)
                if candidate not in reached and walkable(candidate):
                    reached.add(candidate)
                    pending.append(candidate)
    anchors = {"north_bridge": (17, 3, 18), "west_bridge": (6, 3, 29), "east_bridge": (28, 3, 29), "south_bridge": (17, 3, 39), "platform_north": (17, 4, 24), "platform_east": (22, 4, 29), "platform_west": (12, 4, 29), "platform_south": (17, 4, 34), "west_cache": (4, 3, 29), "east_cache": (30, 3, 29), "rear_cache": (17, 4, 41)}
    for name, position in anchors.items():
        assert position in reached, ("Unreachable destination", name, position)
    counts = collections.Counter(block.name for block in BLOCKS.values())
    return {"status": "PASS", "dimensions": list(SIZE), "solid_and_detail_blocks": len(BLOCKS), "lava_sources": len(lava), "lava_containment": "bottom and all four horizontal neighbors closed", "roof": "two layers at all 35x35 columns, including stepped joints", "walkable_cells_reached": len(reached), "walkable_anchors": {name: list(position) for name, position in anchors.items()}, "materials": dict(counts.most_common()), "scope": "Static voxel geometry and conservative 2-block player clearance; not Minecraft gameplay or fluid simulation."}


def verify_pack():
    paths = sorted(PACK.rglob("*.mcfunction"))
    command_count = 0
    for path in paths:
        for line in path.read_text(encoding="utf-8").splitlines():
            command_count += 1
            assert not line.startswith(("kill ", "gamerule ", "forceload ", "weather ", "time "))
            if "run function ftobsidian:" in line:
                target = line.split("run function ftobsidian:", 1)[1].split()[0]
                assert (PACK / f"data/{NAMESPACE}/functions/{target}.mcfunction").is_file(), target
    assert not (PACK / "data/minecraft/tags/functions/tick.json").exists()
    assert command_count < 65536
    for path in PACK.rglob("*.json"):
        json.loads(path.read_text(encoding="utf-8"))
    return {"status": "PASS", "function_count": len(paths), "command_lines": command_count, "full_empty_volume_checks": SIZE[0] * SIZE[1] * SIZE[2], "max_command_chain_default": 65536, "writes_on_load": "scoreboard objective only", "auto_build": False, "tick_function": False, "jar_or_save_changes": False}


def main():
    for directory in (ROOT / "out", ROOT / "qa", ROOT / "preview"):
        directory.mkdir(parents=True, exist_ok=True)
    foundation()
    lava_and_platform()
    chamber_walls()
    roof_and_chains()
    brazier()
    entry_gate()
    alcoves()
    template, structure_path = write_structure()
    build_functions()
    geometry = verify_geometry()
    resources = verify_pack()
    write_json(ROOT / "qa/geometry.json", geometry)
    write_json(ROOT / "qa/datapack.json", resources)
    write_json(ROOT / "qa/blueprint.json", template)
    archive = ROOT / "out/ft-obsidian-trial-1.20.1.zip"
    with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED) as bundle:
        for path in sorted(PACK.rglob("*")):
            if path.is_file():
                info = zipfile.ZipInfo(path.relative_to(PACK).as_posix(), (2026, 11, 1, 0, 0, 0))
                info.compress_type = zipfile.ZIP_DEFLATED
                bundle.writestr(info, path.read_bytes())
    write_json(ROOT / "out/manifest.json", {"title": "黑曜试炼地牢", "minecraft": "1.20.1", "namespace": NAMESPACE, "size": list(SIZE), "main_chamber": [35, 18, 35], "entrance": [17.5, 3, 5.5], "center": [17, 4, 29], "source_reference": "D:/ft工程文件/image2.5/obsidian-trial-room-20261101/out/", "geometry": geometry, "checks": resources, "structure_sha256": hashlib.sha256(structure_path.read_bytes()).hexdigest(), "archive_sha256": hashlib.sha256(archive.read_bytes()).hexdigest(), "installed": False})
    print(json.dumps({"archive": str(archive), "structure": str(structure_path), "blocks": len(BLOCKS), "palette": len(template["palette"]), "geometry": geometry["status"], "datapack": resources["status"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
