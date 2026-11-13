import argparse
import hashlib
import importlib.util
import json
from itertools import combinations, product
from pathlib import Path


READER_PATH = Path(r"D:\ft工程文件\结构参考_20261103\analyze_structures.py")
SPEC = importlib.util.spec_from_file_location("independent_nbt_reader", READER_PATH)
READER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(READER)


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def decode(path):
    reader = READER.NbtReader(path.read_bytes())
    assert reader.number(">B") == 10, "Root must be compound"
    reader.string()
    data = reader.value(10)
    assert reader.offset == len(reader.payload), "Trailing NBT data"
    assert data["DataVersion"] == 3465
    size = data["size"]
    assert len(size) == 3 and all(isinstance(extent, int) and extent > 0 for extent in size)
    palette = data["palette"]
    blocks = {}
    for block in data["blocks"]:
        point = tuple(block["pos"])
        assert len(point) == 3 and all(0 <= point[axis] < size[axis] for axis in range(3)), f"Out of bounds {point}"
        assert point not in blocks, f"Duplicate {point}"
        assert 0 <= block["state"] < len(palette)
        blocks[point] = {"state": palette[block["state"]], "nbt": block.get("nbt")}
    return data, blocks


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    snapshot = load(root / "submission-snapshot.json")
    manifest = load(root / "ready-for-review.json")
    assert manifest["style"] == "S8"
    assert hashlib.sha256((root / "ready-for-review.json").read_bytes()).hexdigest() == snapshot["manifest_sha256"]
    paths = {}
    records = list(snapshot["files"])
    if (root / "supplemental-snapshot.json").is_file():
        records += load(root / "supplemental-snapshot.json")["files"]
    for record in records:
        frozen = Path(record["snapshot"])
        assert frozen.is_relative_to(root)
        assert hashlib.sha256(frozen.read_bytes()).hexdigest() == record["sha256"]
        paths[str(Path(record["source"]).resolve())] = frozen

    def captured(path):
        return paths[str(Path(path).resolve())]

    assembly = manifest["assembly"]
    layout_source = assembly.get("layout", {}).get("path") or str(Path(assembly["source"]["path"]).parent / "layout.json")
    layout = load(captured(layout_source))
    combined, blocks = decode(captured(assembly["source"]["path"]))
    assert combined["size"] == layout["size"]
    if "size" in assembly:
        assert combined["size"] == assembly["size"]
    assert layout["sha256"] == assembly["source"]["sha256"]
    assert not combined["entities"], "Structure entities require explicit transform audit"
    assert len(layout["modules"]) == 10
    rooms = {room["id"]: room for room in manifest["rooms"]}
    assert set(rooms) == {module["id"] for module in layout["modules"]}
    room_reports = []
    all_original_targets = []
    port_targets = []
    boxes = []
    for module in layout["modules"]:
        room = rooms[module["id"]]
        assert module["sha256"] == room["source"]["sha256"]
        assert captured(module["source"]) == captured(room["source"]["path"])
        original, original_blocks = decode(captured(module["source"]))
        assert original["size"] == module["size"]
        assert not original["entities"], "Room entities require transform audit"
        origin = module["origin"]
        boxes.append((module["id"], origin, module["size"]))
        for point, value in original_blocks.items():
            transformed = tuple(point[axis] + origin[axis] for axis in range(3))
            assert blocks.get(transformed) == value, f"Room payload changed {module['id']} {point}"
        extra_nonair = []
        for point, value in blocks.items():
            if not all(origin[axis] <= point[axis] < origin[axis] + module["size"][axis] for axis in range(3)):
                continue
            local = tuple(point[axis] - origin[axis] for axis in range(3))
            if local not in original_blocks and value["state"]["Name"] not in {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}:
                extra_nonair.append({"local_position": local, "merged_position": point, "state": value})
        bound_inputs = [entry for entry in room["qa"] if Path(entry["path"]).name == "route-input.json"]
        assert len(bound_inputs) <= 1
        route_source = bound_inputs[0]["path"] if bound_inputs else str(Path(room["source"]["path"]).parent / "qa/route-input.json")
        inputs = load(captured(route_source))
        assert len(inputs) == 1 and inputs[0]["sha256"] == room["source"]["sha256"]
        for target in inputs[0]["targets"]:
            all_original_targets.append({"label": module["id"] + ": " + target["label"], "position": [target["position"][axis] + origin[axis] for axis in range(3)]})
        assert len(module["ports"]) == len(room["interfaces"])
        for port, interface in zip(module["ports"], room["interfaces"]):
            assert port["side"] == interface["side"]
            assert port["width"] == interface["width"] and port["height"] == interface["height"]
            expected = [interface["foot"][axis] + origin[axis] for axis in range(3)]
            assert port["foot"] == expected
            port_targets.append({"label": f"{module['id']}: {port['side']} full-port center", "position": expected})
        room_reports.append({"id": module["id"], "sha256": module["sha256"], "records_compared": len(original_blocks), "block_entities_compared": sum(value["nbt"] is not None for value in original_blocks.values()), "all_present_states_and_block_entity_payloads_equal": True, "nonair_added_in_omitted_source_cells": extra_nonair})
    for first, second in combinations(boxes, 2):
        overlap = all(max(first[1][axis], second[1][axis]) < min(first[1][axis] + first[2][axis], second[1][axis] + second[2][axis]) for axis in range(3))
        assert not overlap, f"Overlapping room boxes {first[0]} {second[0]}"
    size = combined["size"]
    boundary = set()
    for axis in range(3):
        other_axes = [other for other in range(3) if other != axis]
        for edge, first, second in product((0, size[axis] - 1), range(size[other_axes[0]]), range(size[other_axes[1]])):
            point = [0, 0, 0]
            point[axis], point[other_axes[0]], point[other_axes[1]] = edge, first, second
            boundary.add(tuple(point))
    for point in boundary:
        assert blocks.get(point, {}).get("state", {}).get("Name") == "minecraft:barrier", f"Boundary gap {point}"
    rebuilt = {}
    part_boxes = []
    for part in assembly["parts"]:
        data, part_blocks = decode(captured(part["path"]))
        assert data["size"] == part["size"] and max(part["size"]) <= 48
        assert not data["entities"]
        assert len(part_blocks) == part["blocks"]
        origin = part["origin"]
        assert all(0 <= origin[axis] and origin[axis] + part["size"][axis] <= size[axis] for axis in range(3))
        part_boxes.append((part["path"], origin, part["size"]))
        for point, value in part_blocks.items():
            transformed = tuple(point[axis] + origin[axis] for axis in range(3))
            assert transformed not in rebuilt, f"Part overlap {transformed}"
            rebuilt[transformed] = value
    for first, second in combinations(part_boxes, 2):
        assert not all(max(first[1][axis], second[1][axis]) < min(first[1][axis] + first[2][axis], second[1][axis] + second[2][axis]) for axis in range(3)), "Part volume overlap"
    assert sum(part[2][0] * part[2][1] * part[2][2] for part in part_boxes) == size[0] * size[1] * size[2], "Part volume gap"
    assert rebuilt == blocks, "Part reconstruction differs"
    route_path = next((entry for entry in assembly.get("qa", []) if Path(entry["path"]).name == "route-input.json"), {"path": str(Path(assembly["source"]["path"]).parent / "qa/route-input.json")})
    plans = load(captured(route_path["path"]))
    assert len(plans) == 1 and plans[0]["sha256"] == assembly["source"]["sha256"]
    assert plans[0]["targets"][:len(all_original_targets)] == all_original_targets, "Original route targets changed or missing in assembly"
    supplied_extras = plans[0]["targets"][len(all_original_targets):]
    assert len(supplied_extras) in (0, len(port_targets))
    if supplied_extras:
        assert [target["position"] for target in supplied_extras] == [target["position"] for target in port_targets]
    plan = plans[0]
    plan["candidate"] = str(captured(assembly["source"]["path"]))
    plan["targets"] = all_original_targets + port_targets
    (root / "route-input.json").write_text(json.dumps([plan], ensure_ascii=False, indent=2), encoding="utf-8")
    report = {"stage": "INDEPENDENT_FROZEN_ASSEMBLY_AND_PARTS_STATIC_AUDIT", "source_sha256": assembly["source"]["sha256"], "assembly_records": len(blocks), "rooms": room_reports, "original_room_records_preserved": sum(room["records_compared"] for room in room_reports), "nonair_added_inside_omitted_room_cells": sum(len(room["nonair_added_in_omitted_source_cells"]) for room in room_reports), "parts": len(part_boxes), "partition_cells_compared": len(rebuilt), "partition_volume_coverage": "EXACT_NONOVERLAPPING_FULL_VOLUME", "outer_boundary_barrier_cells": len(boundary), "original_targets_preserved": len(all_original_targets), "additional_port_targets": len(port_targets), "native_routes": "INPUT_PREPARED_NOT_YET_EXECUTED", "source_entities": "ALL_EMPTY", "group_accepted": False, "limitations": ["No world placement or ticks", "Sparse room templates omit unspecified air; actual placement over existing terrain still needs explicit clearing strategy", "Barrier boundary verified; player approach and functional routes require native audit", "Visual fidelity remains separate and unapproved"]}
    (root / "independent-assembly-parts.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({key: value for key, value in report.items() if key != "rooms"}, ensure_ascii=False))


if __name__ == "__main__":
    main()
