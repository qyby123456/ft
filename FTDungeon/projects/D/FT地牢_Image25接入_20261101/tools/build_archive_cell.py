import collections
import gzip
import hashlib
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
TOOLS = Path("D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools")
sys.path.insert(0, str(TOOLS))
from structure_io import Byte, load_structure, nbt_payload, write_json


SIZE = (9, 8, 11)
OUTPUT = ROOT / "incoming/S1"
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-room-tomb-archive-cell-20261101/out/dungeon-room-tomb-archive-cell-hero.png")
BLOCKS = {}


def put(block_x, block_y, block_z, name, data=None, **properties):
    position = (block_x, block_y, block_z)
    assert all(0 <= position[axis] < SIZE[axis] for axis in range(3))
    state = {"Name": "minecraft:" + name}
    if properties:
        state["Properties"] = {key: str(value).lower() for key, value in properties.items()}
    BLOCKS[position] = (state, data)


def fill(minimum, maximum, name, **properties):
    for block_x in range(minimum[0], maximum[0] + 1):
        for block_y in range(minimum[1], maximum[1] + 1):
            for block_z in range(minimum[2], maximum[2] + 1):
                put(block_x, block_y, block_z, name, **properties)


def book(title, text):
    return {"id": "minecraft:written_book", "Count": Byte(1), "tag": {"title": title, "author": "墓狱书记官", "resolved": Byte(1), "pages": [json.dumps({"text": text}, ensure_ascii=False)]}}


def lectern(block_x, block_z, title, text):
    put(block_x, 1, block_z, "lectern", data={"id": "minecraft:lectern", "Book": book(title, text), "Page": 0}, facing="north", has_book=True, powered=False)


def architecture():
    for block_x in range(SIZE[0]):
        for block_z in range(SIZE[2]):
            value = (block_x * 23 + block_z * 37) % 11
            material = "cracked_deepslate_tiles" if value == 0 else "deepslate_tiles" if value < 4 else "deepslate_bricks"
            put(block_x, 0, block_z, material)
            put(block_x, 7, block_z, "deepslate_tiles")
            for block_y in range(1, 7):
                if block_x in (0, SIZE[0] - 1) or block_z in (0, SIZE[2] - 1):
                    put(block_x, block_y, block_z, "deepslate_bricks" if (block_x + block_y + block_z) % 6 else "cracked_deepslate_bricks")
    for block_x in (0, 8):
        for block_z in (0, 3, 7, 10):
            fill((block_x, 1, block_z), (block_x, 6, block_z), "polished_deepslate")
    for block_x in range(1, 8):
        put(block_x, 6, 3, "dark_oak_log", axis="x")
        put(block_x, 6, 7, "dark_oak_log", axis="x")
    for block_x in range(3, 6):
        for block_y in range(1, 4):
            BLOCKS.pop((block_x, block_y, 0), None)
    for block_x in (2, 6):
        fill((block_x, 1, 0), (block_x, 3, 0), "polished_deepslate")
    fill((2, 4, 0), (6, 4, 0), "chiseled_deepslate")


def furnishings():
    for block_x in (1, 7):
        for block_z in range(3, 7):
            fill((block_x, 1, block_z), (block_x, 4, block_z), "bookshelf")
            put(block_x, 5, block_z, "dark_oak_slab", type="bottom", waterlogged=False)
        for block_z in (2, 7):
            fill((block_x, 1, block_z), (block_x, 5, block_z), "dark_oak_log", axis="y")
    for block_x in (3, 5):
        put(block_x, 1, 4, "deepslate_bricks")
    put(3, 2, 4, "candle", candles="2", lit=True, waterlogged=False)
    put(5, 2, 4, "skeleton_skull", rotation="0")
    lectern(4, 4, "移交登记", "封存名单已转入后侧铁栅档案龛。这里保留手抄本，不存放财宝。\n\n青灯仍亮，巡查者可沿石桌两侧通行。")
    for block_x in range(2, 7):
        for block_y in range(1, 5):
            put(block_x, block_y, 7, "iron_bars", east=block_x < 6, west=block_x > 2, north=False, south=False, waterlogged=False)
    for block_x in (1, 7):
        fill((block_x, 1, 7), (block_x, 5, 7), "chiseled_deepslate")
    fill((2, 5, 7), (6, 5, 7), "polished_deepslate")
    lectern(4, 9, "封存目录", "第七号档案：苔锁地窖。\n第八号档案：祭炉厅。\n第九号档案：失联的走私商。")
    for block_x in (2, 6):
        put(block_x, 1, 9, "deepslate_bricks")
        put(block_x, 2, 9, "skeleton_skull", rotation="0")
        put(block_x, 4, 8, "soul_lantern", hanging=True, waterlogged=False)
        fill((block_x, 5, 8), (block_x, 6, 8), "chain", axis="y", waterlogged=False)
    for block_x in (2, 6):
        put(block_x, 4, 2, "soul_lantern", hanging=True, waterlogged=False)
        fill((block_x, 5, 2), (block_x, 6, 2), "chain", axis="y", waterlogged=False)
    for block_x in range(3, 6):
        put(block_x, 1, 2, "gray_carpet")
    for position in ((1, 5, 4), (7, 5, 5), (2, 5, 9)):
        put(*position, "cobweb")


def validate():
    passable = {"minecraft:air", "minecraft:gray_carpet"}

    def walkable(position):
        below = (position[0], position[1] - 1, position[2])
        above = (position[0], position[1] + 1, position[2])
        return below in BLOCKS and all(BLOCKS.get(point, ({"Name": "minecraft:air"}, None))[0]["Name"] in passable for point in (position, above))

    entrance = (4, 1, 0)
    interior = (4, 1, 3)
    reachable = {entrance}
    pending = collections.deque([entrance])
    while pending:
        current = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            following = (current[0] + delta_x, 1, current[2] + delta_z)
            if 0 <= following[0] < SIZE[0] and 0 <= following[2] < SIZE[2] and following not in reachable and walkable(following):
                reachable.add(following)
                pending.append(following)
    assert interior in reachable
    assert (2, 1, 5) in reachable and (6, 1, 5) in reachable
    assert all(walkable((block_x, 1, 0)) for block_x in range(3, 6))
    assert all((block_x, 7, block_z) in BLOCKS for block_x in range(9) for block_z in range(11))
    assert all((block_x, block_y, 0) not in BLOCKS for block_x in range(3, 6) for block_y in range(1, 4))
    materials = collections.Counter(state["Name"] for state, data in BLOCKS.values())
    assert not any(materials[name] for name in ("minecraft:chest", "minecraft:barrel", "minecraft:spawner", "minecraft:fire", "minecraft:lava"))
    return {"status": "PASS", "entrance_width": 3, "entrance_clearance": 3, "reachable_floor_positions": len(reachable), "safe_interior_reachable": True, "both_table_aisles_reachable": True, "full_roof": True, "containers": 0, "spawners": 0, "fluids": 0, "materials": dict(sorted(materials.items())), "limits": "Conservative static walkability; native Minecraft block-state validation delegated to core integration verification; not played in Minecraft."}


def main():
    architecture()
    furnishings()
    report = validate()
    palette = []
    indices = {}
    records = []
    for position, (state, data) in sorted(BLOCKS.items()):
        key = json.dumps(state, sort_keys=True)
        if key not in indices:
            indices[key] = len(palette)
            palette.append(state)
        record = {"pos": list(position), "state": indices[key]}
        if data:
            record["nbt"] = data
        records.append(record)
    template = {"DataVersion": 3465, "size": list(SIZE), "palette": palette, "blocks": records, "entities": []}
    kind, payload = nbt_payload(template)
    assert kind == 10
    OUTPUT.mkdir(parents=True, exist_ok=True)
    target = OUTPUT / "room.nbt"
    target.write_bytes(gzip.compress(b"\x0a\x00\x00" + payload, mtime=0))
    assert load_structure(target) == template
    metadata = {"id": "S1", "name": "墓狱档案囚室", "size": list(SIZE), "entrance": [4, 1, 0], "interior": [4, 1, 3], "facing": "north", "sha256": hashlib.sha256(target.read_bytes()).hexdigest(), "reference": str(REFERENCE), "reference_sha256": hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), "roof": "enclosed", "readable_books": 2, "loot": "none"}
    write_json(OUTPUT / "room.json", metadata)
    write_json(OUTPUT / "qa.json", {**report, "blocks": len(records), "palette_states": len(palette), "nbt_roundtrip": True, "sha256": metadata["sha256"]})
    print(json.dumps({"id": "S1", "size": SIZE, "blocks": len(records), "sha256": metadata["sha256"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
