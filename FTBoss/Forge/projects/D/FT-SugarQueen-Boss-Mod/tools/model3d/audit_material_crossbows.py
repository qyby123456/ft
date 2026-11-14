import argparse
import itertools
import json
from pathlib import Path
import zipfile

import numpy as np

from gen_material_crossbows import ASSETS, ART, PARTS, VARIANTS
from render_material_crossbows import transform


def overlaps(first, second, tolerance=0):
    return np.all(np.minimum(first[1], second[1]) - np.maximum(first[0], second[0]) >= -tolerance)


def bounds(cube, part, progress):
    corners = list(itertools.product(*zip(cube["from"], cube["to"])))
    points = transform(corners, part, progress)
    return np.array([points.min(axis=0), points.max(axis=0)])


def connected(cubes):
    boxes = [np.array([cube["from"], cube["to"]]) for cube in cubes]
    visited = {0}
    while True:
        expanded = visited | {index for index, box in enumerate(boxes)
                              if any(overlaps(box, boxes[other], 1e-7) for other in visited)}
        if expanded == visited:
            break
        visited = expanded
    return [cube["name"] for index, cube in enumerate(cubes) if index not in visited]


def segment_intersects(start, end, box):
    expanded = box + np.array([[-0.001, -0.1, -0.12], [0.001, 0.1, 0.12]])
    lower, upper = 0.0, 1.0
    delta = end - start
    for axis in range(3):
        if abs(delta[axis]) < 1e-9:
            if start[axis] < expanded[0, axis] or start[axis] > expanded[1, axis]:
                return False
        else:
            first, second = sorted((expanded[:, axis] - start[axis]) / delta[axis])
            lower, upper = max(lower, first), min(upper, second)
            if lower > upper:
                return False
    return True


def audit(parts):
    for part, cubes in parts.items():
        assert not connected(cubes), (part, "floating", connected(cubes))
        for first, second in itertools.combinations(cubes, 2):
            for axis in range(3):
                other_axes = [index for index in range(3) if index != axis]
                overlap = np.minimum(first["to"], second["to"]) - np.maximum(first["from"], second["from"])
                if not np.all(overlap[other_axes] > 1e-7):
                    continue
                for face in ("from", "to"):
                    assert abs(first[face][axis] - second[face][axis]) > 1e-7, (part, "coplanar faces", first["name"], second["name"], axis, face)
    body = [bounds(cube, "body", 0) for cube in parts["body"]]
    for step in range(101):
        progress = step / 100
        for side, tip_x, pivot_x in (("left", -6, 4), ("right", 22, 12)):
            pivot = np.array([pivot_x, 7, 1])
            assert any(np.all(pivot >= box[0]) and np.all(pivot <= box[1]) for box in body), "pivot detached from receiver"
            tip = transform([[tip_x, 9.55, -2]], side, progress)[0]
            center = np.array([8, 9.55, -2 + 9 * progress])
            for cube, box in zip(parts["body"], body):
                assert not segment_intersects(tip, center, box), (step, side, "string/body clipping", cube["name"])
            for cube in parts[side]:
                if cube["name"].endswith("string_anchor"):
                    continue
                assert cube["to"][1] < 9.45 - 1e-7, (step, side, "arm intrudes into string plane", cube["name"])
            anchor = next(cube for cube in parts[side] if cube["name"].endswith("string_anchor"))
            assert np.all(np.array([tip_x, 9.55, -2]) >= anchor["from"]) and np.all(np.array([tip_x, 9.55, -2]) <= anchor["to"]), "string detached from anchor"
        latch = bounds(parts["latch"][0], "latch", progress)
        assert any(overlaps(latch, box) for box in body), (step, "slider detached from guide")
    obstacles = [(cube["name"], bounds(cube, part, 1)) for part in ("body", "latch") for cube in parts[part]]
    for part in ("arrow", "firework"):
        for cube in parts[part]:
            box = bounds(cube, part, 1)
            for name, obstacle in obstacles:
                penetration = np.minimum(box[1], obstacle[1]) - np.maximum(box[0], obstacle[0])
                assert not np.all(penetration > 1e-7), (part, "ammunition/body clipping", cube["name"], name)
        shaft = bounds(parts[part][0], part, 1)
        assert any(overlaps(shaft, box, 1e-7) for _, box in obstacles), (part, "unsupported ammunition")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", type=Path)
    args = parser.parse_args()
    results = []
    archive = zipfile.ZipFile(args.jar) if args.jar else None
    try:
        for material, *_ in VARIANTS:
            item_id = material + "_crossbow"
            parts = {}
            for part in PARTS:
                relative = "models/item/" + item_id + "_parts/" + part + ".json"
                data = archive.read("assets/ftboss/" + relative) if archive else (ASSETS / relative).read_bytes()
                parts[part] = json.loads(data)["elements"]
            audit(parts)
            results.append({"id": item_id, "status": "PASS", "poseSamples": 101, "connectedParts": len(parts),
                            "checks": ["part connectivity", "coplanar face rejection", "fixed receiver pivots", "continuous string anchors", "string/body clearance", "slider attachment", "arrow and rocket clearance and support"]})
            print(item_id, "PASS: connectivity, coplanar surfaces, 101 poses, string and ammunition clearance", flush=True)
    finally:
        if archive:
            archive.close()
    ART.mkdir(parents=True, exist_ok=True)
    (ART / "geometry_audit.json").write_text(json.dumps({"sourceJar": str(args.jar) if args.jar else None, "results": results}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
