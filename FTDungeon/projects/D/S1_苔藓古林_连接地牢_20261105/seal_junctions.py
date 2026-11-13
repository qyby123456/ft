import copy
import hashlib
import importlib.util
import io
import json
import os
import shutil
import struct
import sys
import time
import zipfile
import zlib
from collections import Counter, defaultdict
from pathlib import Path

sys.dont_write_bytecode = True
BASE = Path(__file__).resolve().parent
ROOT = BASE.with_name(BASE.name + "_门框补缝R2")
INSTANCE = Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2")
sys.path.insert(0, str(INSTANCE / "output/dungeon-expansion-20261101/tools"))
from nbtio import AIR, Structure, State, Tag, plain, read_payload, read_string, state, encode_payload
import flat_world as writer


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding="utf-8")


def build():
    ROOT.mkdir(exist_ok=True)
    assert not any(ROOT.iterdir()), "Revision directory must be empty"
    structure = Structure.read(BASE / "mossy-forest-connected.nbt")
    layout = json.loads((BASE / "layout.json").read_text(encoding="utf-8"))
    changes, frames = [], []
    modules = {entry["image"]: entry for entry in layout["modules"]}
    protected = json.loads((BASE / "qa/route-input.json").read_text(encoding="utf-8"))[0]
    body = {tuple(target["position"]) for target in protected["targets"]}
    body |= {(pos_x, pos_y + 1, pos_z) for pos_x, pos_y, pos_z in list(body)}
    for link in layout["links"]:
        for number, side in (link["from"], link["to"]):
            module = modules[number]
            port = next(entry for entry in module["ports"] if entry["side"] == side)
            center = tuple(value + shift for value, shift in zip(port["feet"], module["origin"]))
            patched = []
            for across in range(-3, 4):
                for rise in range(6):
                    if abs(across) <= port["width"] // 2 and rise < port["clearHeight"]:
                        continue
                    pos_x, pos_y, pos_z = center
                    position = (pos_x + across, pos_y + rise, pos_z) if side in ("north", "south") else (pos_x, pos_y + rise, pos_z + across)
                    assert all(0 <= value < limit for value, limit in zip(position, structure.size))
                    if structure.get(position) != AIR:
                        continue
                    assert position not in body and position not in structure.block_entities, position
                    material = state("dark_oak_log", axis="x" if side in ("north", "south") else "z") if rise == 5 else state("mossy_stone_bricks" if (across + rise) % 3 else "cracked_stone_bricks")
                    structure.set(position, material)
                    changes.append({"position": position, "after": plain(material.as_tag()), "module": number, "side": side})
                    patched.append(position)
            frames.append({"module": number, "side": side, "center": center, "opening_width": port["width"], "opening_height": port["clearHeight"], "patched": patched})
    assert changes and len(frames) == 16
    structure.write(ROOT / "mossy-forest-connected.nbt")
    layout["sha256"] = digest(ROOT / "mossy-forest-connected.nbt")
    layout["junction_sealing"] = frames
    save(ROOT / "layout.json", layout)
    save(ROOT / "junction-changes.json", {"original_sha256": digest(BASE / "mossy-forest-connected.nbt"), "revised_sha256": layout["sha256"], "changes": changes, "materials": Counter(entry["after"]["Name"] for entry in changes)})
    protected["candidate"], protected["sha256"] = str(ROOT / "mossy-forest-connected.nbt"), layout["sha256"]
    save(ROOT / "qa/route-input.json", [protected])
    shutil.copy2(BASE / "qa/vanilla-block-tags.json", ROOT / "qa/vanilla-block-tags.json")
    shutil.copy2(BASE / "render.cjs", ROOT / "render.cjs")
    parts = json.loads((BASE / "assembly.json").read_text(encoding="utf-8"))
    pack = ROOT / "datapack"
    shutil.copytree(BASE / "datapack", pack)
    shutil.copy2(ROOT / "mossy-forest-connected.nbt", pack / "data/s1_connected/structures/full.nbt")
    for entry in parts["parts"]:
        part = Structure(tuple(entry["size"]))
        for pos_x in range(entry["size"][0]):
            for pos_y in range(entry["size"][1]):
                for pos_z in range(entry["size"][2]):
                    local = (pos_x, pos_y, pos_z)
                    position = tuple(value + shift for value, shift in zip(local, entry["origin"]))
                    entity = copy.deepcopy(structure.block_entities.get(position))
                    if entity:
                        for axis, value in zip(("x", "y", "z"), local):
                            if axis in entity.value:
                                entity.value[axis].value = value
                    part.set(local, structure.get(position), entity)
        part.write(ROOT / entry["path"])
        entry["sha256"] = digest(ROOT / entry["path"])
        shutil.copy2(ROOT / entry["path"], pack / f'data/s1_connected/structures/part_{entry["order"]:02}.nbt')
    parts["source_sha256"] = layout["sha256"]
    save(ROOT / "assembly.json", parts)
    with zipfile.ZipFile(ROOT / "s1-connected-datapack.zip", "x", zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(pack.rglob("*")):
            if path.is_file():
                archive.write(path, path.relative_to(pack).as_posix())
    print(json.dumps({"frames": len(frames), "blocks_added": len(changes), "materials": Counter(entry["after"]["Name"] for entry in changes)}, ensure_ascii=False))


def read_saved_world(world):
    sections, entities = {}, {}
    for path in sorted((world / "region").glob("*.mca")):
        payload = path.read_bytes()
        if not payload:
            continue
        assert len(payload) >= 8192
        for index in range(1024):
            location = struct.unpack_from(">I", payload, index * 4)[0]
            if not location:
                continue
            offset = (location >> 8) * 4096
            length = struct.unpack_from(">I", payload, offset)[0]
            assert payload[offset + 4] == 2
            stream = io.BytesIO(zlib.decompress(payload[offset + 5:offset + 4 + length]))
            assert stream.read(1) == b"\x0a"
            read_string(stream)
            chunk = read_payload(stream, 10).value
            coordinates = (chunk["xPos"].value, chunk["zPos"].value)
            for section in chunk.get("sections", Tag(9, [], 10)).value:
                if "block_states" not in section.value:
                    continue
                values = section.value["block_states"].value
                palette = [State.from_tag(entry) for entry in values["palette"].value]
                words = values.get("data")
                sections[(*coordinates, section.value["Y"].value)] = (palette, words.value if words else [])
            for entity in chunk.get("block_entities", Tag(9, [], 10)).value:
                entities[tuple(entity.value[axis].value for axis in ("x", "y", "z"))] = plain(entity)

    def block_at(position):
        pos_x, pos_y, pos_z = position
        palette, words = sections[(pos_x // 16, pos_z // 16, pos_y // 16)]
        if len(palette) == 1:
            return palette[0]
        bits = max(4, (len(palette) - 1).bit_length())
        per_word = 64 // bits
        index = pos_y % 16 * 256 + pos_z % 16 * 16 + pos_x % 16
        return palette[(words[index // per_word] >> (index % per_word * bits)) & ((1 << bits) - 1)]

    return block_at, entities


def install():
    native = json.loads((ROOT / "qa/independent-native-frozen.json").read_text(encoding="utf-8"))[0]
    route = json.loads((ROOT / "qa/independent-routes.json").read_text(encoding="utf-8"))[0]
    assert not native["survival_failures"] and not native["survival_unsupported"]
    assert route["start_clear"] and all(entry["reachable"] and entry["standing_clear"] for entry in route["targets"])
    world = INSTANCE / "saves/FT_S1_Mossy_Connected_20261105"
    change_report = json.loads((ROOT / "junction-changes.json").read_text(encoding="utf-8"))
    grouped = defaultdict(dict)
    for change in change_report["changes"]:
        pos_x, pos_y, pos_z = change["position"]
        grouped[(pos_x // 16, pos_z // 16)][(pos_x, pos_y - 60, pos_z)] = State(change["after"]["Name"], tuple(sorted(change["after"].get("Properties", {}).items())))
    current, previous_entities = read_saved_world(world)
    for edits in grouped.values():
        for position in edits:
            assert current(position) == AIR, ("Existing block changed, stop", position)
    backup = ROOT / "backup-before-junction-seal.zip"
    if not backup.exists():
        with zipfile.ZipFile(backup, "x", zipfile.ZIP_DEFLATED) as archive:
            for path in sorted(world.rglob("*")):
                if path.is_file():
                    archive.write(path, path.relative_to(world).as_posix())
    with zipfile.ZipFile(backup) as archive:
        assert archive.testzip() is None
        baseline = {name: hashlib.sha256(archive.read(name)).hexdigest() for name in archive.namelist()}
    assert all(digest(world / relative) == expected for relative, expected in baseline.items())
    changed_files = []
    for path in sorted((world / "region").glob("*.mca")):
        region_x, region_z = map(int, path.name.split(".")[1:3])
        if not any(chunk_x // 32 == region_x and chunk_z // 32 == region_z for chunk_x, chunk_z in grouped):
            continue
        source = path.read_bytes()
        header, records, sector_next = bytearray(8192), [], 2
        header[4096:] = source[4096:8192]
        for index in range(1024):
            location = struct.unpack_from(">I", source, index * 4)[0]
            if not location:
                continue
            sector, count = location >> 8, location & 255
            offset = sector * 4096
            length = struct.unpack_from(">I", source, offset)[0]
            record = source[offset:offset + 4 + length]
            coordinates = (region_x * 32 + index % 32, region_z * 32 + index // 32)
            if coordinates in grouped:
                stream = io.BytesIO(zlib.decompress(record[5:]))
                assert stream.read(1) == b"\x0a"
                read_string(stream)
                chunk = read_payload(stream, 10)
                for section in chunk.value["sections"].value:
                    section_y = section.value["Y"].value
                    edits = {position: block for position, block in grouped[coordinates].items() if position[1] // 16 == section_y}
                    if not edits:
                        continue
                    values = section.value["block_states"].value
                    palette = [State.from_tag(entry) for entry in values["palette"].value]
                    bits = max(4, (len(palette) - 1).bit_length())
                    words = values.get("data")
                    indices = [0] * 4096 if len(palette) == 1 else [(words.value[slot // (64 // bits)] >> (slot % (64 // bits) * bits)) & ((1 << bits) - 1) for slot in range(4096)]
                    for (pos_x, pos_y, pos_z), block in edits.items():
                        if block not in palette:
                            palette.append(block)
                        indices[pos_y % 16 * 256 + pos_z % 16 * 16 + pos_x % 16] = palette.index(block)
                    values["palette"] = Tag(9, [block.as_tag() for block in palette], 10)
                    values["data"] = writer.pack_indices(indices, max(4, (len(palette) - 1).bit_length()))
                chunk.value["isLightOn"] = writer.byte(0)
                compressed = zlib.compress(b"\x0a\x00\x00" + encode_payload(chunk), 6)
                record = struct.pack(">I", len(compressed) + 1) + b"\x02" + compressed
                struct.pack_into(">I", header, 4096 + index * 4, int(time.time()))
            sectors = (len(record) + 4095) // 4096
            struct.pack_into(">I", header, index * 4, sector_next << 8 | sectors)
            records.append(record.ljust(sectors * 4096, b"\x00"))
            sector_next += sectors
        staged = path.with_suffix(".mca.junction-pending")
        with staged.open("xb") as stream:
            stream.write(bytes(header) + b"".join(records))
        assert digest(path) == baseline[path.relative_to(world).as_posix()]
        os.replace(staged, path)
        changed_files.append(path.relative_to(world).as_posix())
    target_pack = world / "datapacks/s1-connected-datapack.zip"
    assert digest(target_pack) == baseline[target_pack.relative_to(world).as_posix()]
    shutil.copy2(ROOT / "s1-connected-datapack.zip", target_pack)
    changed_files.append(target_pack.relative_to(world).as_posix())
    actual, entities = read_saved_world(world)
    assert entities == previous_entities
    original = Structure.read(BASE / "mossy-forest-connected.nbt")
    revised = Structure.read(ROOT / "mossy-forest-connected.nbt")
    changed_positions = {tuple(entry["position"]) for entry in change_report["changes"]}
    for position, expected in revised.blocks.items():
        destination = (position[0], position[1] - 60, position[2])
        assert actual(destination) == (expected if position in changed_positions else current(destination)), position
    for position in original.blocks:
        if position not in changed_positions:
            assert revised.get(position) == original.get(position)
    assert {position: plain(entity) for position, entity in original.block_entities.items()} == {position: plain(entity) for position, entity in revised.block_entities.items()}
    for relative, expected in baseline.items():
        if relative not in changed_files:
            assert digest(world / relative) == expected, relative
    save(ROOT / "deployment-report.json", {"installed": True, "world": str(world), "backup": str(backup), "frames": 16, "blocks_added": len(changed_positions), "source_cells_compared": len(revised.blocks), "reachable_targets": len(route["targets"]), "modified_files": changed_files, "other_save_files_preserved": True, "client_runtime_tested": False})
    save(ROOT / "SHA256.json", {path.relative_to(ROOT).as_posix(): digest(path) for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name != "SHA256.json"})
    print("INSTALLED", len(changed_positions), "junction blocks; preserved all other world blocks and save files")


if __name__ == "__main__":
    build() if sys.argv[1] == "build" else install()
