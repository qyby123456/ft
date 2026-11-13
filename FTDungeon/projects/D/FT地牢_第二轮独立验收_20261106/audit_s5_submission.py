import argparse
import hashlib
import json
from itertools import combinations, product
from pathlib import Path

from audit_s8_submission import decode, load


def turn_position(point, size, turns):
    result = list(point)
    extent = list(size)
    for turn in range(turns):
        result = [result[2], result[1], extent[0] - 1 - result[0]]
        extent = [extent[2], extent[1], extent[0]]
    return result


def canonical(value):
    return json.dumps(value, sort_keys=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    parser.add_argument("--prepare", action="store_true")
    arguments = parser.parse_args()
    root = arguments.directory.resolve()
    manifest = load(root / "ready-for-review.json")
    assert manifest["group"] == "S5"
    snapshot = load(root / "submission-snapshot.json")
    assert hashlib.sha256((root / "ready-for-review.json").read_bytes()).hexdigest() == snapshot["manifest_sha256"]
    paths = {}
    for record in [*snapshot["files"], *load(root / "supplemental-snapshot.json")["files"]]:
        frozen = Path(record["snapshot"])
        assert frozen.resolve().is_relative_to(root)
        assert hashlib.sha256(frozen.read_bytes()).hexdigest() == record["sha256"]
        assert record["source"] not in paths
        paths[record["source"]] = frozen
    assembly = manifest["merged"]
    merged_path = paths[assembly["candidate"]]
    layout = load(merged_path.parent / "design.json")
    combined, blocks = decode(merged_path)
    assert combined["size"] == layout["size"] == assembly["size"]
    assert len(blocks) == assembly["block_records"]
    assert not combined["entities"]
    rooms = {room["id"]: room for room in manifest["rooms"]}
    assert len(rooms) == 10 and set(rooms) == {module["id"] for module in layout["modules"]}
    qa = root / "independent-qa"
    qa.mkdir(exist_ok=True)
    inputs = []
    rotation_inputs = []
    targets = []
    source_data = []
    boxes = []
    for module in layout["modules"]:
        room = rooms[module["id"]]
        assert module["source"] == room["candidate"] and module["source_sha256"] == room["sha256"]
        source = paths[room["candidate"]]
        assert hashlib.sha256(source.read_bytes()).hexdigest() == room["sha256"]
        original, payload = decode(source)
        assert not original["entities"]
        rotated_size = [original["size"][2], original["size"][1], original["size"][0]] if module["turns_ccw"] % 2 else original["size"]
        assert rotated_size == module["size"]
        boxes.append((module["origin"], module["size"]))
        route = load(source.parent / "qa/route-input.json")
        assert len(route) == 1 and route[0]["sha256"] == room["sha256"]
        inputs.append(dict(route[0], candidate=str(source), group=500 + int(room["id"].split("-")[1])))
        rotation_inputs.append({"id": room["id"], "candidate": str(source), "sha256": room["sha256"], "turns_ccw": module["turns_ccw"]})
        for target in route[0]["targets"]:
            rotated = turn_position(target["position"], original["size"], module["turns_ccw"])
            targets.append({"label": room["id"] + "-" + target["label"], "position": [rotated[axis] + module["origin"][axis] for axis in range(3)]})
        source_data.append((module, original, payload))
    rotation_inputs.append({"id": "merged", "candidate": str(merged_path), "sha256": assembly["sha256"], "turns_ccw": 0})
    merged_routes = load(merged_path.parent / "qa/route-input.json")
    assert len(merged_routes) == 1 and merged_routes[0]["sha256"] == assembly["sha256"]
    missing_targets = [target for target in targets if not any(target == actual for actual in merged_routes[0]["targets"])]
    inputs.append(dict(merged_routes[0], candidate=str(merged_path), group=599, targets=[*merged_routes[0]["targets"], *missing_targets]))
    (qa / "route-input.json").write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding="utf-8")
    (qa / "rotation-input.json").write_text(json.dumps(rotation_inputs, ensure_ascii=False, indent=2), encoding="utf-8")
    if arguments.prepare:
        print("Prepared exact frozen native rotation and route inputs; acceptance pending.")
        return
    palettes = {entry["id"]: entry for entry in load(qa / "native-rotation-palettes.json")}
    merged_palette = {canonical(state): palettes["merged"]["normalized_original_palette"][index] for index, state in enumerate(combined["palette"])}
    caps = set()
    for module in (layout["modules"][0], layout["modules"][-1]):
        port = next(port for port in module["ports"] if port["facing"] == "west")
        for across, rise in product(range(-2, 3), range(4)):
            caps.add((port["feet"][0], port["feet"][1] + rise, port["feet"][2] + across))
    assert len(caps) == 40
    assert all(blocks[point]["state"]["Name"] == "minecraft:cyan_stained_glass" for point in caps)
    mismatches = []
    source_reports = []
    for module, original, payload in source_data:
        native = palettes[module["id"]]
        assert native["sha256"] == module["source_sha256"] and native["turns_ccw"] == module["turns_ccw"]
        source_palette = {canonical(state): native["normalized_rotated_palette"][index] for index, state in enumerate(original["palette"])}
        count_caps = 0
        for point, value in payload.items():
            rotated = turn_position(point, original["size"], module["turns_ccw"])
            global_point = tuple(rotated[axis] + module["origin"][axis] for axis in range(3))
            actual = blocks[global_point]
            if global_point in caps:
                assert value["state"]["Name"] in {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
                count_caps += 1
                continue
            expected_state = source_palette[canonical(value["state"])]
            actual_state = merged_palette[canonical(actual["state"])]
            if expected_state != actual_state or value["nbt"] != actual["nbt"]:
                mismatches.append({"room": module["id"], "local": list(point), "merged": list(global_point), "expected_native_state": expected_state, "actual_native_state": actual_state, "block_entity_equal": value["nbt"] == actual["nbt"]})
        source_reports.append({"id": module["id"], "records_compared": len(payload), "block_entities_compared": sum(value["nbt"] is not None for value in payload.values()), "terminal_cap_exceptions": count_caps})
    for first, second in combinations(boxes, 2):
        assert not all(max(first[0][axis], second[0][axis]) < min(first[0][axis] + first[1][axis], second[0][axis] + second[1][axis]) for axis in range(3))
    boundary = set()
    size = combined["size"]
    for axis in range(3):
        others = [other for other in range(3) if other != axis]
        for edge, first, second in product((0, size[axis] - 1), range(size[others[0]]), range(size[others[1]])):
            point = [0, 0, 0]
            point[axis], point[others[0]], point[others[1]] = edge, first, second
            boundary.add(tuple(point))
    assert all(blocks[point]["state"]["Name"] == "minecraft:barrier" for point in boundary)
    parts_manifest = load(merged_path.parent / "parts.json")
    assert parts_manifest["source_sha256"] == assembly["sha256"] and len(parts_manifest["parts"]) == assembly["part_count"]
    rebuilt = {}
    part_boxes = []
    for part in parts_manifest["parts"]:
        source = paths[part["path"]]
        assert hashlib.sha256(source.read_bytes()).hexdigest() == part["sha256"]
        data, payload = decode(source)
        assert not data["entities"] and data["size"] == part["size"] and max(data["size"]) <= 48
        origin = part["origin"]
        assert all(0 <= origin[axis] and origin[axis] + data["size"][axis] <= size[axis] for axis in range(3))
        part_boxes.append((origin, data["size"]))
        for point, value in payload.items():
            global_point = tuple(point[axis] + origin[axis] for axis in range(3))
            assert global_point not in rebuilt
            rebuilt[global_point] = value
    assert rebuilt == blocks
    assert sum(box[1][0] * box[1][1] * box[1][2] for box in part_boxes) == size[0] * size[1] * size[2]
    result = {"stage": "INDEPENDENT_NATIVE_ROTATION_AND_FROZEN_PAYLOAD_PARTS_CHECK", "sha256": assembly["sha256"], "merged_records": len(blocks), "rooms": source_reports, "native_rotation_or_payload_mismatches": mismatches, "terminal_glass_cells": len(caps), "barrier_boundary_cells": len(boundary), "parts_exact_full_payload_and_volume": True, "part_count": len(part_boxes), "original_targets_transformed": len(targets), "original_targets_missing_in_declared_merged_routes": missing_targets, "missing_target_is_terminal_cap": [tuple(target["position"]) in caps for target in missing_targets], "merged_declared_targets": len(merged_routes[0]["targets"]), "missing_targets_added_for_independent_route_test": len(missing_targets), "accepted": False, "world_run": "NOT_RUN", "visual": "PENDING"}
    (qa / "native-rotation-payload-parts-barrier.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({key: value for key, value in result.items() if key not in {"rooms", "native_rotation_or_payload_mismatches"}}, ensure_ascii=False))
    print("Native rotation or payload mismatches:", len(mismatches))


if __name__ == "__main__":
    main()
