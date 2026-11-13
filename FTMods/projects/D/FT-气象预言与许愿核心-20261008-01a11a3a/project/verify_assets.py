import itertools
import json
import math
from pathlib import Path

from PIL import Image
from optimize_geometry import FACE_AXES, face_coverage


ROOT = Path(__file__).resolve().parent


def rotated(point, rotation):
    if rotation is None:
        return point
    origin = rotation["origin"]
    coordinates = [value - pivot for value, pivot in zip(point, origin)]
    axes = {"x": (1, 2), "y": (2, 0), "z": (0, 1)}
    first, second = axes[rotation["axis"]]
    angle = math.radians(rotation["angle"])
    initial_first, initial_second = coordinates[first], coordinates[second]
    coordinates[first] = initial_first * math.cos(angle) - initial_second * math.sin(angle)
    coordinates[second] = initial_first * math.sin(angle) + initial_second * math.cos(angle)
    return [value + pivot for value, pivot in zip(coordinates, origin)]


def verify(name, namespace="qys_event"):
    assets = ROOT / "payload/assets" / namespace
    model_path = assets / "models/block" / f"{name}.json"
    model = json.loads(model_path.read_text(encoding="utf-8"))
    assert model["parent"] == "minecraft:block/block"
    assert model["elements"]
    names = [element["name"] for element in model["elements"]]
    assert len(names) == len(set(names))
    vertices = []
    quads = 0
    for element in model["elements"]:
        assert all(0 <= minimum < maximum <= 16 for minimum, maximum in zip(element["from"], element["to"]))
        rotation = element.get("rotation")
        if rotation:
            assert rotation["axis"] in {"x", "y", "z"}
            assert rotation["angle"] in {-45, -22.5, 0, 22.5, 45}
            assert rotation["rescale"] is False
        vertices.extend(rotated(point, rotation) for point in itertools.product(*zip(element["from"], element["to"])))
        assert 1 <= len(element["faces"]) <= 6
        assert set(element["faces"]) <= set(FACE_AXES)
        for direction in set(FACE_AXES) - set(element["faces"]):
            assert face_coverage(element, direction, model["elements"])[0], (element["name"], direction)
        if rotation:
            assert set(element["faces"]) == set(FACE_AXES)
        for face in element["faces"].values():
            assert face["texture"] == "#atlas"
            assert all(0 <= value <= 16 for value in face["uv"])
            assert face["uv"][0] < face["uv"][2] and face["uv"][1] < face["uv"][3]
            assert "cullface" not in face
            quads += 1
    bounds = {"minimum": [min(point[axis] for point in vertices) for axis in range(3)],
              "maximum": [max(point[axis] for point in vertices) for axis in range(3)]}
    assert all(-1e-8 <= coordinate <= 16 + 1e-8 for point in vertices for coordinate in point)
    for reference in model["textures"].values():
        texture_namespace, texture_path = reference.split(":")
        assert texture_namespace == namespace
        filename = assets / "textures" / (texture_path + ".png")
        with Image.open(filename) as image:
            assert image.mode == "RGBA"
            assert image.getextrema()[3] == (255, 255)
            assert image.size == ((128, 128) if "atlas" in texture_path else (32, 32))
        with Image.open(filename) as image:
            image.verify()
    item = json.loads((assets / "models/item" / f"{name}.json").read_text(encoding="utf-8"))
    assert item["parent"] == f"{namespace}:block/{name}"
    assert len(model["display"]) == 8
    if name == "oracle":
        base = model["elements"][0]
        assert base["from"] == [0, 0, 0] and base["to"] == [16, 1.4, 16]
    if name == "wishing_well_core":
        assert all(point[1] <= 8 for point in vertices)
        assert all(3 <= point[0] <= 13 and 3 <= point[2] <= 13 for point in vertices if point[1] > 4)
    return {"name": name, "elements": len(names), "quads": quads, "bounds": bounds,
            "textures_valid": True, "item_parent_valid": True, "all_exposed_faces_present": True,
            "hidden_faces_proven_covered": True,
            "single_block_bounds": True, "minecraft_rotation_constraints": True}


def main():
    report = [verify(name) for name in ("weather_forecaster", "oracle")]
    report.append(verify("wishing_well_core", "farmingtales"))
    for kind in ("block", "item"):
        legacy = ROOT / "payload/assets/ftboss/models" / kind / "oracle.json"
        assert json.loads(legacy.read_text(encoding="utf-8"))["parent"] == "qys_event:block/oracle"
    (ROOT / "asset-verification.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
