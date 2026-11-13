import argparse
import hashlib
import json
from collections import deque
from itertools import combinations, product
from pathlib import Path

from audit_s8_submission import decode, load
from audit_s5_submission import canonical, turn_position
from audit_s1_s6_payload import overlapping


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    parser.add_argument("--prepare", action="store_true")
    arguments = parser.parse_args()
    root = arguments.directory.resolve()
    manifest = load(root / "ready-for-review.json")
    snapshot = load(root / "submission-snapshot.json")
    assert snapshot["style"] == "S2"
    assert hashlib.sha256((root / "ready-for-review.json").read_bytes()).hexdigest() == snapshot["manifest_sha256"]
    records = snapshot["files"] + load(root / "supplemental-snapshot.json")["files"]
    if (root / "independent-maker-world-inspection.json").is_file():
        records += load(root / "independent-maker-world-inspection.json")["files"]
    captured = {}
    for record in records:
        source = Path(record["snapshot"])
        assert source.is_relative_to(root)
        assert hashlib.sha256(source.read_bytes()).hexdigest() == record["sha256"]
        captured[record["source"]] = source
    assembly = manifest["assembly"]
    merged = captured[assembly["candidate"]]
    combined, blocks = decode(merged)
    assert not combined["entities"]
    layout = load(merged.parent / "layout.json")
    assert layout["size"] == combined["size"] and layout["source_sha256"] == assembly["sha256"]
    rooms = {room["id"]: room for room in manifest["rooms"]}
    assert set(rooms) == {module["id"] for module in layout["modules"]} and len(rooms) == 10
    rotation_input = []
    sources = []
    boxes = []
    expected_targets = []
    for module in layout["modules"]:
        room = rooms[module["id"]]
        assert module["candidate"] == room["candidate"] and module["source_sha256"] == room["sha256"]
        source = captured[room["candidate"]]
        original, payload = decode(source)
        assert original["size"] == module["source_size"] and not original["entities"]
        assert module["rotation_clockwise"] in (0, 90, 180, 270)
        turns = (4 - module["rotation_clockwise"] // 90) % 4
        extent = [original["size"][2], original["size"][1], original["size"][0]] if turns % 2 else original["size"]
        assert extent == module["size"]
        boxes.append((module["origin"], extent))
        rotation_input.append({"id": room["id"], "candidate": str(source), "sha256": room["sha256"], "turns_ccw": turns})
        sources.append((module, original, payload, turns))
        route = load(source.parent / "qa/route-input.json")[0]
        assert route["sha256"] == room["sha256"]
        for target in route["targets"]:
            rotated = turn_position(target["position"], original["size"], turns)
            expected_targets.append({"room": room["id"], "label": target["label"], "position": [rotated[axis] + module["origin"][axis] for axis in range(3)]})
    assert not any(overlapping(first, second) for first, second in combinations(boxes, 2))
    rotation_input.append({"id": "merged", "candidate": str(merged), "sha256": assembly["sha256"], "turns_ccw": 0})
    qa = root / "independent-qa"
    (qa / "rotation-input.json").write_text(json.dumps(rotation_input, ensure_ascii=False, indent=2), encoding="utf-8")
    if arguments.prepare:
        print("Prepared native rotation input for ten frozen sources and merged source")
        return
    palettes = {entry["id"]: entry for entry in load(qa / "native-rotation-palettes.json")}
    actual_palette = {canonical(state): palettes["merged"]["normalized_original_palette"][index] for index, state in enumerate(combined["palette"])}
    changes = []
    for module, original, payload, turns in sources:
        native = palettes[module["id"]]
        assert native["sha256"] == module["source_sha256"] and native["turns_ccw"] == turns
        expected_palette = {canonical(state): native["normalized_rotated_palette"][index] for index, state in enumerate(original["palette"])}
        for point, value in payload.items():
            rotated = turn_position(point, original["size"], turns)
            world = tuple(rotated[axis] + module["origin"][axis] for axis in range(3))
            actual = blocks.get(world)
            if actual is None or expected_palette[canonical(value["state"])] != actual_palette[canonical(actual["state"])] or value["nbt"] != actual["nbt"]:
                changes.append({"room": module["id"], "position": world, "source": value, "merged": actual})
        assert len(payload) == original["size"][0] * original["size"][1] * original["size"][2]
    index_path = merged.parent / "parts/index.json"
    if not index_path.is_file():
        index_path = root / "maker-world-evidence/assembly/parts/index.json"
    index = load(index_path)
    assert index["source_sha256"] == assembly["sha256"] and len(index["parts"]) == assembly["parts"]
    rebuilt = {}
    part_boxes = []
    for part in index["parts"]:
        source = captured[part["path"]]
        assert hashlib.sha256(source.read_bytes()).hexdigest() == part["sha256"]
        data, payload = decode(source)
        assert not data["entities"] and data["size"] == part["size"] and max(data["size"]) <= 48
        origin = part["origin"]
        assert all(0 <= origin[axis] and origin[axis] + data["size"][axis] <= combined["size"][axis] for axis in range(3))
        part_boxes.append((origin, data["size"]))
        for point, value in payload.items():
            world = tuple(point[axis] + origin[axis] for axis in range(3))
            assert world not in rebuilt
            rebuilt[world] = value
    assert rebuilt == blocks and not any(overlapping(first, second) for first, second in combinations(part_boxes, 2))
    route = load(merged.parent / "qa/route-input.json")[0]
    missing = [target for target in expected_targets if not any(target["position"] == other["position"] for other in route["targets"])]
    size = combined["size"]
    dimensions = [extent + 2 for extent in size]
    stride = dimensions[0] * dimensions[1]
    def cell_index(point):
        return int(point[0]) + 1 + (int(point[1]) + 1) * dimensions[0] + (int(point[2]) + 1) * stride
    occupancy = bytearray(stride * dimensions[2])
    barriers = [point for point, value in blocks.items() if value["state"]["Name"] == "minecraft:barrier"]
    for point in barriers:
        occupancy[cell_index(point)] = 1
    queue = deque([0])
    occupancy[0] = 2
    while queue:
        current = queue.popleft()
        east = current % dimensions[0]
        height = current // dimensions[0] % dimensions[1]
        south = current // stride
        neighbors = []
        if east:
            neighbors.append(current - 1)
        if east + 1 < dimensions[0]:
            neighbors.append(current + 1)
        if height:
            neighbors.append(current - dimensions[0])
        if height + 1 < dimensions[1]:
            neighbors.append(current + dimensions[0])
        if south:
            neighbors.append(current - stride)
        if south + 1 < dimensions[2]:
            neighbors.append(current + stride)
        for neighbor in neighbors:
            if occupancy[neighbor] == 0:
                occupancy[neighbor] = 2
                queue.append(neighbor)
    enclosed = 0
    for origin, extent in boxes:
        for point in product(*(range(origin[axis], origin[axis] + extent[axis]) for axis in range(3))):
            assert occupancy[cell_index(point)] == 0, "Room volume not enclosed"
            enclosed += 1
    for target in route["targets"]:
        assert all(float(value).is_integer() for value in target["position"])
        assert occupancy[cell_index(target["position"])] == 0, "Route target outside barrier shell"
    report = {"stage": "INDEPENDENT_NATIVE_ROTATION_PAYLOAD_PARTS_BARRIER", "sha256": assembly["sha256"], "source_differences": changes, "original_targets": len(expected_targets), "missing_original_target_positions": missing, "part_count": len(part_boxes), "parts_exact_state_be_reconstruction": True, "parts_volume": sum(box[1][0] * box[1][1] * box[1][2] for box in part_boxes), "full_volume": size[0] * size[1] * size[2], "merged_records": len(blocks), "barrier_cells": len(barriers), "room_volume_cells_enclosed": enclosed, "merged_target_cells_enclosed": len(route["targets"]), "barrier_method": "Padded six-neighbor external flood ignoring all blocks except barriers", "accepted": False, "limitations": ["Source differences require classification", "No world or visual acceptance", "Part bounds cover volume; sparse payload still requires clearing before placement"]}
    (qa / "native-rotation-payload-parts-barrier.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({key: value for key, value in report.items() if key != "source_differences"}, ensure_ascii=False))
    print("source_difference_count", len(changes))


if __name__ == "__main__":
    main()
