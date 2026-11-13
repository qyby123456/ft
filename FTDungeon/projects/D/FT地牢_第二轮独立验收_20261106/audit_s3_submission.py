import argparse
import hashlib
import json
from itertools import combinations, product
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    snapshot = load(root / "submission-snapshot.json")
    manifest = load(root / "ready-for-review.json")
    assert manifest["style"] == "S3"
    assert hashlib.sha256((root / "ready-for-review.json").read_bytes()).hexdigest() == snapshot["manifest_sha256"]
    paths = {}
    for record in snapshot["files"]:
        frozen = Path(record["snapshot"]).resolve()
        assert frozen.is_relative_to(root)
        assert hashlib.sha256(frozen.read_bytes()).hexdigest() == record["sha256"]
        paths[str(Path(record["source"]).resolve())] = frozen

    def captured(path):
        return paths[str(Path(path).resolve())]

    assembly = manifest["merged"]
    layout = load(captured(assembly["assembly"]["path"]))
    combined, blocks = decode(captured(assembly["candidate"]["path"]))
    assert combined["size"] == layout["size"]
    assert not combined["entities"]
    rooms = {room["id"]: room for room in manifest["rooms"]}
    assert len(rooms) == 10 and set(rooms) == {module["id"] for module in layout["modules"]}
    reports = []
    boxes = []
    original_targets = []
    audit_inputs = []
    for module in layout["modules"]:
        room = rooms[module["id"]]
        assert module["sha256"] == room["candidate"]["sha256"]
        original, original_blocks = decode(captured(room["candidate"]["path"]))
        assert original["size"] == module["size"] and not original["entities"]
        origin = module["origin"]
        boxes.append((module["id"], origin, module["size"]))
        for point, value in original_blocks.items():
            transformed = tuple(point[axis] + origin[axis] for axis in range(3))
            assert blocks.get(transformed) == value, f"Changed room payload {module['id']} {point}"
        added = []
        for point, value in blocks.items():
            if all(origin[axis] <= point[axis] < origin[axis] + module["size"][axis] for axis in range(3)):
                local = tuple(point[axis] - origin[axis] for axis in range(3))
                if local not in original_blocks and value["state"]["Name"] not in {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}:
                    added.append(list(local))
        assert not added, f"New solids inside sparse source air {module['id']}: {added[:10]}"
        records = [record for record in room["qa"] if Path(record["path"]).name == "route-input.json"]
        assert len(records) == 1
        inputs = load(captured(records[0]["path"]))
        assert len(inputs) == 1 and inputs[0]["sha256"] == room["candidate"]["sha256"]
        assert inputs[0]["targets"] == module["targets"]
        specification = dict(inputs[0], candidate=str(captured(room["candidate"]["path"])), group=300 + int(module["id"].split("-")[1]))
        audit_inputs.append(specification)
        original_targets.extend({"label": module["id"] + " " + target["label"], "position": [target["position"][axis] + origin[axis] for axis in range(3)]} for target in inputs[0]["targets"])
        reports.append({"id": module["id"], "records_compared": len(original_blocks), "block_entities_compared": sum(value["nbt"] is not None for value in original_blocks.values()), "source_payload_equal": True, "nonair_added_in_omitted_cells": 0})
    for first, second in combinations(boxes, 2):
        assert not all(max(first[1][axis], second[1][axis]) < min(first[1][axis] + first[2][axis], second[1][axis] + second[2][axis]) for axis in range(3)), f"Room overlap {first[0]} {second[0]}"
    size = combined["size"]
    boundary = set()
    for axis in range(3):
        others = [other for other in range(3) if other != axis]
        for edge, first, second in product((0, size[axis] - 1), range(size[others[0]]), range(size[others[1]])):
            point = [0, 0, 0]
            point[axis], point[others[0]], point[others[1]] = edge, first, second
            boundary.add(tuple(point))
    assert all(blocks.get(point, {}).get("state", {}).get("Name") == "minecraft:barrier" for point in boundary), "Barrier boundary incomplete"
    parts_manifest = load(captured(assembly["parts_manifest"]["path"]))
    assert parts_manifest["source_sha256"] == assembly["candidate"]["sha256"]
    assert {record["path"] for record in parts_manifest["parts"]} == {record["path"] for record in assembly["parts"]}
    reconstructed = {}
    part_boxes = []
    volume = 0
    for part in parts_manifest["parts"]:
        part_path = captured(part["path"])
        assert hashlib.sha256(part_path.read_bytes()).hexdigest() == part["sha256"]
        data, payload = decode(part_path)
        assert data["size"] == part["size"] and all(extent <= 48 for extent in data["size"])
        assert not data["entities"]
        origin = part["origin"]
        assert all(0 <= origin[axis] and origin[axis] + data["size"][axis] <= size[axis] for axis in range(3))
        part_boxes.append((part["path"], origin, data["size"]))
        volume += data["size"][0] * data["size"][1] * data["size"][2]
        for point, value in payload.items():
            transformed = tuple(point[axis] + origin[axis] for axis in range(3))
            assert transformed not in reconstructed
            reconstructed[transformed] = value
    for first, second in combinations(part_boxes, 2):
        assert not all(max(first[1][axis], second[1][axis]) < min(first[1][axis] + first[2][axis], second[1][axis] + second[2][axis]) for axis in range(3)), "Overlapping part volumes"
    assert volume == size[0] * size[1] * size[2] and reconstructed == blocks
    merge_records = [record for record in assembly["qa"] if Path(record["path"]).name == "route-input.json"]
    assert len(merge_records) == 1
    merge_inputs = load(captured(merge_records[0]["path"]))
    assert len(merge_inputs) == 1 and merge_inputs[0]["sha256"] == assembly["candidate"]["sha256"]
    assert merge_inputs[0]["targets"][:len(original_targets)] == original_targets
    merge_input = dict(merge_inputs[0], candidate=str(captured(assembly["candidate"]["path"])), group=399)
    audit_inputs.append(merge_input)
    qa = root / "independent-qa"
    qa.mkdir(exist_ok=True)
    (qa / "route-input.json").write_text(json.dumps(audit_inputs, ensure_ascii=False, indent=2), encoding="utf-8")
    result = {"stage": "INDEPENDENT_PAYLOAD_PARTS_BARRIER_CHECK_ONLY", "manifest_sha256": snapshot["manifest_sha256"], "merged_sha256": assembly["candidate"]["sha256"], "rooms": reports, "merged_records": len(blocks), "barrier_boundary_cells": len(boundary), "parts": len(part_boxes), "parts_exact_payload_and_volume": True, "original_route_targets_preserved": len(original_targets), "merged_route_targets": len(merge_input["targets"]), "world_physics_verified": False, "visual_acceptance": "PENDING", "accepted": False}
    (qa / "payload-parts-barrier.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({key: value for key, value in result.items() if key != "rooms"}, ensure_ascii=False))


if __name__ == "__main__":
    main()
