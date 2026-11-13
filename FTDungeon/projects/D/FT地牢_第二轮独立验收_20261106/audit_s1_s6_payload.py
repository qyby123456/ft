import argparse
import hashlib
import json
import math
from collections import Counter, deque
from copy import deepcopy
from itertools import combinations
from pathlib import Path

from audit_s8_submission import decode, load


def translated(entity, origin):
    result = deepcopy(entity)
    assert result["nbt"]["id"] == "minecraft:armor_stand"
    for field in ("pos", "blockPos"):
        result[field] = [result[field][axis] + origin[axis] for axis in range(3)]
    result["nbt"]["Pos"] = [result["nbt"]["Pos"][axis] + origin[axis] for axis in range(3)]
    return result


def entities(values):
    return Counter(json.dumps(value, sort_keys=True) for value in values)


def overlapping(first, second):
    return all(max(first[0][axis], second[0][axis]) < min(first[0][axis] + first[1][axis], second[0][axis] + second[1][axis]) for axis in range(3))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    manifest = load(root / "ready-for-review.json")
    group = manifest.get("style") or manifest.get("group") or load(root / "submission-snapshot.json")["style"]
    assert group in {"S1", "S4", "S6"}
    snapshot = load(root / "submission-snapshot.json")
    assert hashlib.sha256((root / "ready-for-review.json").read_bytes()).hexdigest() == snapshot["manifest_sha256"]
    records = snapshot["files"] + load(root / "supplemental-snapshot.json")["files"]
    captured = {}
    for record in records:
        frozen = Path(record["snapshot"])
        assert frozen.is_relative_to(root)
        assert hashlib.sha256(frozen.read_bytes()).hexdigest() == record["sha256"]
        captured[record["source"]] = frozen
    assembly = manifest.get("assembly") or manifest["merged"]
    candidate = captured[assembly["candidate"]]
    combined, blocks = decode(candidate)
    layout = load(candidate.parent / ("integrity.json" if group == "S1" else "layout.json"))
    modules = layout["rooms" if group == "S1" else "modules"]
    assert len(modules) == 10
    rooms = {room["id"]: room for room in manifest["rooms"]}
    assert set(rooms) == {module["id"] for module in modules}
    differences = []
    source_reports = []
    expected_entities = []
    boxes = []
    transformed_targets = []
    for module in modules:
        room = rooms[module["id"]]
        source = captured[room["candidate"]]
        data, payload = decode(source)
        origin = module["offset" if group == "S1" else "origin"]
        assert module["sha256" if group in {"S1", "S4"} else "source_sha256"] == room["sha256"]
        assert hashlib.sha256(source.read_bytes()).hexdigest() == room["sha256"]
        boxes.append((origin, data["size"]))
        for point, value in payload.items():
            world = tuple(point[axis] + origin[axis] for axis in range(3))
            actual = blocks.get(world)
            if actual != value:
                differences.append({"room": room["id"], "local": point, "position": world, "source": value, "merged": actual})
        added = []
        for point, value in blocks.items():
            local = tuple(point[axis] - origin[axis] for axis in range(3))
            if all(0 <= local[axis] < data["size"][axis] for axis in range(3)) and local not in payload and value["state"]["Name"] not in {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}:
                added.append(local)
        expected_entities.extend(translated(entity, origin) for entity in data["entities"])
        route = load(source.parent / "qa/route-input.json")[0]
        assert route["sha256"] == room["sha256"]
        transformed_targets.extend({"room": room["id"], "label": target["label"], "position": [target["position"][axis] + origin[axis] for axis in range(3)]} for target in route["targets"])
        source_reports.append({"id": room["id"], "records": len(payload), "block_entities": sum(value["nbt"] is not None for value in payload.values()), "entities": len(data["entities"]), "added_solids_in_omitted_air": added})
    assert not any(overlapping(first, second) for first, second in combinations(boxes, 2))
    assert entities(expected_entities) == entities(combined["entities"])
    part_records = layout["chunks"] if group == "S1" else load(candidate.parent / "parts.json")["parts"] if group == "S4" else load(candidate.parent / "qa/split-payload-verification.json")["pieces"]
    rebuilt = {}
    rebuilt_entities = []
    part_boxes = []
    for part in part_records:
        source = captured[part["path"] if group == "S4" else part["file"]]
        assert hashlib.sha256(source.read_bytes()).hexdigest() == part["sha256"]
        data, payload = decode(source)
        assert data["size"] == part["size"] and all(extent <= 48 for extent in data["size"])
        origin = part["origin"]
        assert all(0 <= origin[axis] and origin[axis] + data["size"][axis] <= combined["size"][axis] for axis in range(3))
        part_boxes.append((origin, data["size"]))
        for point, value in payload.items():
            world = tuple(point[axis] + origin[axis] for axis in range(3))
            assert world not in rebuilt
            rebuilt[world] = value
        rebuilt_entities.extend(translated(entity, origin) for entity in data["entities"])
    assert not any(overlapping(first, second) for first, second in combinations(part_boxes, 2))
    assert rebuilt == blocks and entities(rebuilt_entities) == entities(combined["entities"])
    merged_route = load(candidate.parent / "qa/route-input.json")[0]
    missing = [target for target in transformed_targets if not any(other["position"] == target["position"] for other in merged_route["targets"])]
    size = combined["size"]
    boundary = set()
    for axis in range(3):
        other_axes = [other for other in range(3) if other != axis]
        for edge in (0, size[axis] - 1):
            for first in range(size[other_axes[0]]):
                for second in range(size[other_axes[1]]):
                    point = [0, 0, 0]
                    point[axis], point[other_axes[0]], point[other_axes[1]] = edge, first, second
                    boundary.add(tuple(point))
    boundary_gaps = sum(blocks.get(point, {}).get("state", {}).get("Name") != "minecraft:barrier" for point in boundary)
    enclosure = None
    cap_verified = None
    if group == "S6":
        caps = layout["unused_ports_closed"]
        assert len(caps) == 1 and caps[0]["module"] == "S6-20"
        port = caps[0]["port"]
        assert port["side"] == "west" and port["width"] == 3 and port["clearHeight"] == 4
        feet = port["feet"]
        expected_caps = {(feet[0], feet[1] + rise, feet[2] + across) for rise in range(4) for across in range(-1, 2)}
        assert {tuple(change["position"]) for change in differences} == expected_caps
        assert all(change["source"] == {"state": {"Name": "minecraft:air"}, "nbt": None} and change["merged"] == {"state": {"Name": "minecraft:orange_stained_glass"}, "nbt": None} for change in differences)
        cap_verified = 12
    if group in {"S4", "S6"}:
        dimensions = [extent + 2 for extent in size]
        stride = dimensions[0] * dimensions[1]
        def cell_index(point):
            return point[0] + 1 + (point[1] + 1) * dimensions[0] + (point[2] + 1) * stride
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
        enclosed_count = 0
        for origin, extent in boxes:
            for east in range(origin[0], origin[0] + extent[0]):
                for height in range(origin[1], origin[1] + extent[1]):
                    for south in range(origin[2], origin[2] + extent[2]):
                        assert occupancy[cell_index((east, height, south))] == 0, "Room volume not enclosed by barrier-only shell"
                        enclosed_count += 1
        for target in merged_route["targets"]:
            assert occupancy[cell_index([math.floor(value) for value in target["position"]])] == 0, "Target outside barrier shell"
        enclosure = {"method": "Padded six-neighbor exterior flood ignoring every block except barriers", "barrier_cells": len(barriers), "all_room_volume_cells_enclosed": enclosed_count, "all_merged_target_cells_enclosed": len(merged_route["targets"])}
    report = {"stage": "INDEPENDENT_PAYLOAD_ENTITY_PARTS_AND_ROUTE_RETENTION_CHECK", "group": group, "sha256": assembly["sha256"], "rooms": source_reports, "room_source_differences": differences, "merged_records": len(blocks), "translated_entities_exact": len(expected_entities), "part_count": len(part_records), "parts_payload_and_entities_exact": True, "parts_volume": sum(box[1][0] * box[1][1] * box[1][2] for box in part_boxes), "full_volume": size[0] * size[1] * size[2], "original_targets": len(transformed_targets), "original_target_positions_missing_in_merged": missing, "rectangular_boundary_cells": len(boundary), "rectangular_boundary_nonbarrier_cells": boundary_gaps, "limitations": ["Nonrectangular union shell needs separate exterior flood verification if rectangular boundary is incomplete", "Changed source cells need explicit reason and payload classification", "Target retention compares positions; functional meaning and paths need separate review", "No world or visual acceptance"], "accepted": False}
    report.update({"explicit_terminal_glass_cap_cells_verified": cap_verified, "union_barrier_enclosure": enclosure})
    (root / "independent-qa/payload-entities-parts.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({key: value for key, value in report.items() if key not in {"rooms", "room_source_differences"}}, ensure_ascii=False))
    print("source_difference_count", len(differences))


if __name__ == "__main__":
    main()
