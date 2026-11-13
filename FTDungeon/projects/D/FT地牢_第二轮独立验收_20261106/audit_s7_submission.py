import argparse
import copy
import hashlib
import json
import math
from collections import Counter, deque
from itertools import combinations, product
from pathlib import Path

from audit_s8_submission import decode, load


def translated_entity(entity, origin):
    result = copy.deepcopy(entity)
    assert result["nbt"]["id"] == "minecraft:armor_stand", "Other entity types need explicit transform audit"
    for key in ("pos", "blockPos"):
        result[key] = [result[key][axis] + origin[axis] for axis in range(3)]
    result["nbt"]["Pos"] = [result["nbt"]["Pos"][axis] + origin[axis] for axis in range(3)]
    return result


def entity_counter(entities):
    return Counter(json.dumps(entity, sort_keys=True) for entity in entities)


def overlap(first, second):
    return all(max(first[0][axis], second[0][axis]) < min(first[0][axis] + first[1][axis], second[0][axis] + second[1][axis]) for axis in range(3))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    snapshot = load(root / "submission-snapshot.json")
    manifest = load(root / "ready-for-review.json")
    assert manifest.get("group", snapshot["style"]) == "S7"
    assert hashlib.sha256((root / "ready-for-review.json").read_bytes()).hexdigest() == snapshot["manifest_sha256"]
    paths = {}
    records = list(snapshot["files"])
    if (root / "supplemental-snapshot.json").is_file():
        records += load(root / "supplemental-snapshot.json")["files"]
    for record in records:
        frozen = Path(record["snapshot"]).resolve()
        assert frozen.is_relative_to(root)
        assert hashlib.sha256(frozen.read_bytes()).hexdigest() == record["sha256"]
        assert record["source"] not in paths
        paths[record["source"]] = frozen

    def captured(source):
        return paths[source]

    assembly = manifest["assembly"]
    merged_path = captured(assembly["candidate"])
    assert hashlib.sha256(merged_path.read_bytes()).hexdigest() == assembly["sha256"]
    combined, blocks = decode(merged_path)
    assert combined["size"] == assembly["size"]
    rooms = {room["id"]: room for room in manifest["rooms"]}
    assert len(rooms) == 10 and set(rooms) == {module["id"] for module in assembly["modules"]}
    source_reports = []
    expected_entities = []
    expected_targets = []
    inputs = []
    boxes = []
    for module in assembly["modules"]:
        room = rooms[module["id"]]
        assert module["candidate"] == room["candidate"] and module["sha256"] == room["sha256"]
        source = captured(room["candidate"])
        assert hashlib.sha256(source.read_bytes()).hexdigest() == room["sha256"]
        original, original_blocks = decode(source)
        assert original["size"] == module["size"] == room["size"]
        origin = module["origin"]
        boxes.append((origin, original["size"]))
        for point, value in original_blocks.items():
            global_point = tuple(point[axis] + origin[axis] for axis in range(3))
            assert blocks.get(global_point) == value, f"Changed source payload {room['id']} {point}"
        added = []
        for point, value in blocks.items():
            if all(origin[axis] <= point[axis] < origin[axis] + original["size"][axis] for axis in range(3)):
                local = tuple(point[axis] - origin[axis] for axis in range(3))
                if local not in original_blocks and value["state"]["Name"] not in {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}:
                    added.append(list(local))
        assert not added, f"New solids inside omitted source air {room['id']}"
        expected_entities.extend(translated_entity(entity, origin) for entity in original["entities"])
        route = load(source.parent / "qa/route-input.json")
        assert len(route) == 1 and route[0]["sha256"] == room["sha256"]
        inputs.append(dict(route[0], candidate=str(source), group=700 + int(room["id"].split("-")[1])))
        expected_targets.extend({"label": room["id"] + " " + target["label"], "position": [target["position"][axis] + origin[axis] for axis in range(3)]} for target in route[0]["targets"])
        source_reports.append({"id": room["id"], "records_compared": len(original_blocks), "block_entities_compared": sum(value["nbt"] is not None for value in original_blocks.values()), "entities_compared": len(original["entities"]), "source_payload_exact": True, "omitted_air_has_no_new_solids": True})
    assert all(not overlap(first, second) for first, second in combinations(boxes, 2))
    assert entity_counter(expected_entities) == entity_counter(combined["entities"]), "Merged entity payload/position differs"
    boundary = set()
    size = combined["size"]
    for axis in range(3):
        other_axes = [other for other in range(3) if other != axis]
        for edge, first, second in product((0, size[axis] - 1), range(size[other_axes[0]]), range(size[other_axes[1]])):
            point = [0, 0, 0]
            point[axis], point[other_axes[0]], point[other_axes[1]] = edge, first, second
            boundary.add(tuple(point))
    barrier_points = {point for point, value in blocks.items() if value["state"]["Name"] == "minecraft:barrier"}
    dimensions = [extent + 2 for extent in size]
    stride = dimensions[0] * dimensions[1]
    occupancy = bytearray(stride * dimensions[2])

    def index(point):
        return point[0] + 1 + (point[1] + 1) * dimensions[0] + (point[2] + 1) * stride

    for point in barrier_points:
        occupancy[index(point)] = 1
    queue = deque([0])
    occupancy[0] = 2
    while queue:
        current = queue.popleft()
        east = current % dimensions[0]
        height = current // dimensions[0] % dimensions[1]
        south = current // stride
        neighbors = []
        if east > 0:
            neighbors.append(current - 1)
        if east + 1 < dimensions[0]:
            neighbors.append(current + 1)
        if height > 0:
            neighbors.append(current - dimensions[0])
        if height + 1 < dimensions[1]:
            neighbors.append(current + dimensions[0])
        if south > 0:
            neighbors.append(current - stride)
        if south + 1 < dimensions[2]:
            neighbors.append(current + stride)
        for neighbor in neighbors:
            if occupancy[neighbor] == 0:
                occupancy[neighbor] = 2
                queue.append(neighbor)
    room_cells_enclosed = 0
    for origin, extent in boxes:
        for point in product(*(range(origin[axis], origin[axis] + extent[axis]) for axis in range(3))):
            assert occupancy[index(point)] == 0, f"Room cell not enclosed by barrier-only shell {point}"
            room_cells_enclosed += 1
    rebuilt = {}
    rebuilt_entities = []
    part_boxes = []
    for part in assembly["parts"]:
        source = captured(part["path"])
        assert hashlib.sha256(source.read_bytes()).hexdigest() == part["sha256"]
        data, payload = decode(source)
        assert data["size"] == part["size"] and max(data["size"]) <= 48 and len(payload) == part["records"]
        origin = part["origin"]
        assert all(0 <= origin[axis] and origin[axis] + data["size"][axis] <= size[axis] for axis in range(3))
        part_boxes.append((origin, data["size"]))
        for point, value in payload.items():
            global_point = tuple(point[axis] + origin[axis] for axis in range(3))
            assert global_point not in rebuilt
            rebuilt[global_point] = value
        rebuilt_entities.extend(translated_entity(entity, origin) for entity in data["entities"])
    assert all(not overlap(first, second) for first, second in combinations(part_boxes, 2))
    assert rebuilt == blocks and entity_counter(rebuilt_entities) == entity_counter(combined["entities"])
    routes = load(merged_path.parent / "qa/route-input.json")
    full_width = load(merged_path.parent / "qa/full-width-routes/route-input.json")
    assert len(routes) == len(full_width) == 1
    assert routes[0]["sha256"] == full_width[0]["sha256"] == assembly["sha256"]
    assert routes[0]["targets"][:len(expected_targets)] == expected_targets
    assert full_width[0]["targets"][:len(routes[0]["targets"])] == routes[0]["targets"]
    assert all(occupancy[index([math.floor(coordinate) for coordinate in target["position"]])] == 0 for target in full_width[0]["targets"]), "Route target outside shell"
    inputs.append(dict(full_width[0], candidate=str(merged_path), group=799))
    qa = root / "independent-qa"
    qa.mkdir(exist_ok=True)
    (qa / "route-input.json").write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding="utf-8")
    report = {"stage": "INDEPENDENT_FROZEN_PAYLOAD_ENTITIES_PARTS_BARRIER_STATIC_CHECK", "sha256": assembly["sha256"], "rooms": source_reports, "merged_records": len(blocks), "entities_exact_translation_and_part_reconstruction": len(expected_entities), "barrier_cells": len(barrier_points), "barrier_enclosure": "All room volume cells and declared route targets unreachable from padded exterior by six-neighbor flood ignoring all blocks except barriers", "room_volume_cells_enclosed": room_cells_enclosed, "part_count": len(part_boxes), "part_payload_reconstruction": "EXACT", "part_volume_cells": sum(box[1][0] * box[1][1] * box[1][2] for box in part_boxes), "full_structure_volume": size[0] * size[1] * size[2], "original_room_targets": len(expected_targets), "merged_declared_targets": len(routes[0]["targets"]), "merged_full_width_targets": len(full_width[0]["targets"]), "native_routes": "PENDING", "accepted": False, "limitations": ["Parts preserve sparse payload but omit empty partition volumes; clear full placement volume separately", "No independent world run, player interaction or full visual acceptance", "Full-width points are maker-generated and independently rerun; geometric completeness needs separate validation", "Render script changed after declared manifest; this is frozen earlier declared evidence plus separately timestamped reviewer supplements"]}
    if not (root / "supplemental-snapshot.json").is_file():
        report["limitations"] = [item for item in report["limitations"] if not item.startswith("Render script changed")]
    (qa / "payload-entities-parts-barrier.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({key: value for key, value in report.items() if key != "rooms"}, ensure_ascii=False))


if __name__ == "__main__":
    main()
