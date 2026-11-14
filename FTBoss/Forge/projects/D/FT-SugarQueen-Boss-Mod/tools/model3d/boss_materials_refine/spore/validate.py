from __future__ import annotations

import base64
import hashlib
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image

from build import face_dimensions
from preview import ASSETS, PROJECT, WORK, element_matrix, face_corners, rotation_matrix


def box_data(element):
    matrix, origin = element_matrix(element)
    corners = np.concatenate([face_corners(element, face) for face in ("north", "south")])
    return (corners - origin) @ matrix.T + origin, matrix


def overlaps(first, second, tolerance=.0001):
    first_points, first_matrix = first
    second_points, second_matrix = second
    candidates = [first_matrix[:, axis] for axis in range(3)] + [second_matrix[:, axis] for axis in range(3)]
    candidates += [np.cross(first_matrix[:, axis], second_matrix[:, other]) for axis in range(3) for other in range(3)]
    for axis in candidates:
        length = np.linalg.norm(axis)
        if length < 1e-8:
            continue
        axis = axis / length
        projection_first, projection_second = first_points @ axis, second_points @ axis
        if min(projection_first.max(), projection_second.max()) - max(projection_first.min(), projection_second.min()) < -tolerance:
            return False
    return True


def validate(assets=ASSETS, work=WORK, identifier="spore_material", folder="boss_materials", bb_path=None):
    wrapper_path = assets / "models/item" / (identifier + ".json")
    wrapper = json.loads(wrapper_path.read_text(encoding="utf-8"))
    assert wrapper["loader"] == "forge:separate_transforms"
    assert wrapper["base"]["parent"] == "ftboss:item/" + folder + "/" + identifier
    gui_path = assets / "models" / (wrapper["perspectives"]["gui"]["parent"].split(":")[1] + ".json")
    gui = json.loads(gui_path.read_text(encoding="utf-8"))
    assert gui["parent"] == "minecraft:item/generated"
    assert gui["textures"]["layer0"] == "ftboss:item/" + identifier
    model_path = assets / "models/item" / folder / (identifier + ".json")
    model = json.loads(model_path.read_text(encoding="utf-8"))
    assert "parent" not in model
    atlas_path = assets / "textures" / (model["textures"]["atlas"].split(":")[1] + ".png")
    image = Image.open(atlas_path).convert("RGBA")
    assert image.width == image.height
    atlas = np.array(image)
    icon_path = assets / "textures/item" / (identifier + ".png")
    icon = np.array(Image.open(icon_path).convert("RGBA"))
    assert icon.shape == (32, 32, 4)
    assert np.any(icon[:, :, 3] == 0) and np.any(icon[:, :, 3] == 255)
    faces = 0
    density = []
    uv_rects = []
    boxes = []
    for element in model["elements"]:
        assert all(-16 <= value <= 32 for value in element["from"] + element["to"])
        assert all(finish > start for start, finish in zip(element["from"], element["to"]))
        if "rotation" in element:
            assert element["rotation"]["angle"] in {-45, -22.5, 0, 22.5, 45}
            assert element["rotation"]["axis"] in "xyz"
        assert set(element["faces"]) == {"north", "south", "east", "west", "up", "down"}
        boxes.append(box_data(element))
        for face, specification in element["faces"].items():
            faces += 1
            assert specification["texture"] == "#atlas"
            assert all(0 <= value <= 16 for value in specification["uv"])
            rectangle = np.array(specification["uv"]) * image.width / 16
            minimum_u, minimum_v, maximum_u, maximum_v = rectangle
            assert maximum_u > minimum_u and maximum_v > minimum_v
            opaque = atlas[math.floor(minimum_v):math.ceil(maximum_v), math.floor(minimum_u):math.ceil(maximum_u), 3]
            assert opaque.min() == 255, element["name"] + ": transparent UV"
            dimensions = face_dimensions(element, face)
            scale_u, scale_v = (maximum_u - minimum_u) / dimensions[0], (maximum_v - minimum_v) / dimensions[1]
            assert abs(scale_u - 2) < .0001 and abs(scale_v - 2) < .0001
            density.extend([scale_u, scale_v])
            uv_rects.append(rectangle)
    for index, first in enumerate(uv_rects):
        for second in uv_rects[index + 1:]:
            intersection = np.minimum(first[2:], second[2:]) - np.maximum(first[:2], second[:2])
            assert not np.all(intersection > .0001), "UV islands overlap"
    neighbours = [set() for element in model["elements"]]
    for index, first in enumerate(boxes):
        for other in range(index + 1, len(boxes)):
            if overlaps(first, boxes[other]):
                neighbours[index].add(other)
                neighbours[other].add(index)
    connected = {0}
    pending = [0]
    while pending:
        current = pending.pop()
        for neighbour in neighbours[current] - connected:
            connected.add(neighbour)
            pending.append(neighbour)
    disconnected = [element["name"] for index, element in enumerate(model["elements"]) if index not in connected]
    bb_path = bb_path or PROJECT / "art/boss_materials/blockbench/spore_material.bbmodel"
    bb = json.loads(bb_path.read_text(encoding="utf-8"))
    assert base64.b64decode(bb["textures"][0]["source"].split(",", 1)[1]) == atlas_path.read_bytes()
    assert len(bb["elements"]) == len(model["elements"])
    for runtime, source in zip(model["elements"], bb["elements"]):
        assert runtime["name"] == source["name"]
        assert runtime["from"] == source["from"] and runtime["to"] == source["to"]
        rotation = runtime.get("rotation", {})
        assert source["rotation"] == [rotation.get("angle", 0) if rotation.get("axis") == axis else 0 for axis in "xyz"]
        for face in runtime["faces"]:
            assert np.allclose(np.array(runtime["faces"][face]["uv"]) * image.width / 16, source["faces"][face]["uv"])
    matrix, origin = element_matrix({"rotation": {"axis": "y", "angle": 45, "rescale": True, "origin": [8, 8, 8]}})
    assert np.allclose(matrix @ np.array([1., 0, 0]), [1, 0, -1])
    assert np.allclose(rotation_matrix("z", 22.5).T @ rotation_matrix("z", 22.5), np.eye(3))
    all_points = np.concatenate([entry[0] for entry in boxes])
    paths = [wrapper_path, gui_path, model_path, atlas_path, icon_path, bb_path]
    result = {
        "status": "PASS" if not disconnected else "FAIL",
        "elements": len(model["elements"]), "faces": faces,
        "rotated_elements": sum("rotation" in element for element in model["elements"]),
        "fully_opaque_uv_faces": faces, "unique_nonoverlapping_uv_islands": len(uv_rects),
        "pixel_density": [min(density), max(density)], "uv_directional_stretch": False,
        "connected_elements": len(connected), "disconnected": disconnected,
        "bounds": [all_points.min(axis=0).tolist(), all_points.max(axis=0).tolist()],
        "blockbench_geometry_uv_embedded_texture_matches_runtime": True,
        "renderer_rotation_and_rescale_math": "PASS",
        "files": {str(target.relative_to(PROJECT)): hashlib.sha256(target.read_bytes()).hexdigest() for target in paths},
    }
    work.mkdir(parents=True, exist_ok=True)
    (work / "validation.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    print(json.dumps(result, ensure_ascii=True, indent=2))
    assert not disconnected, "Disconnected geometry: " + ", ".join(disconnected)
    return result


if __name__ == "__main__":
    validate()
