import copy
import json
import subprocess
import zipfile

import numpy as np
from PIL import Image

from import_models import ART, ASSETS, MATERIALS, PROJECT, SUFFIXES, WORK, corners, digest, face_uv, rotate, write_json
from arrows import arrow_elements


def rotate_element(point, rotation):
    origin = np.asarray(rotation["origin"])
    offset = np.subtract(point, origin)
    axis = np.zeros(3)
    axis["xyz".index(rotation.get("axis", "x"))] = 1
    angle = np.radians(rotation["angle"])
    return (origin + offset * np.cos(angle) + np.cross(axis, offset) * np.sin(angle)
            + axis * np.dot(axis, offset) * (1 - np.cos(angle))).tolist()


def verify_shared_arrow(model, nock):
    source = json.loads((ASSETS / "models/item/wooden_crossbow_parts/arrow.json").read_text(encoding="utf-8"))
    old_nock = next(element for element in arrow_elements(nock) if element["name"] == "arrow_nock")
    target = [round((old_nock["from"][axis] + old_nock["to"][axis]) / 2, 6) for axis in range(3)]
    target[0] = round(target[0] + 1.9, 6)
    origin = [8, 11.01, 9.02]
    scales = [1, 1, 30 / 14.02]
    expected = copy.deepcopy(source["elements"])
    for element in expected:
        for key in ("from", "to"):
            element[key] = [round(target[axis] + (element[key][axis] - origin[axis]) * scales[axis], 6)
                            for axis in range(3)]
        if "rotation" in element:
            pivot = element["rotation"]["origin"]
            element["rotation"]["origin"] = [round(target[axis] + (pivot[axis] - origin[axis]) * scales[axis], 6)
                                               for axis in range(3)]
        for face in element["faces"].values():
            face["texture"] = "#arrow"
    expected[0]["from"][0] = 7.95
    for element in expected:
        if element["name"].startswith("arrow_fletching"):
            shift = target[2] + 1.25 - element["to"][2]
            for key in ("from", "to"):
                element[key][2] = round(element[key][2] + shift, 6)
            element["rotation"]["origin"][2] = round(element["rotation"]["origin"][2] + shift, 6)
    assert model["elements"][16:] == expected, "Shared bow/crossbow arrow geometry or UV differs"


def world_faces(element):
    rotation = element.get("rotation", {"angle": 0, "origin": [8, 8, 8]})
    return [(name, [rotate_element(point, rotation) for point in vertices],
             face_uv(element["faces"][name])) for name, vertices in corners(element).items()
            if name in element["faces"]]


def intersects(first, second):
    points, directions = [], []
    for element in (first, second):
        rotation = element.get("rotation", {"angle": 0, "axis": "x", "origin": [0, 0, 0]})
        points.append(np.asarray([rotate_element(point, rotation)
                                  for vertices in corners(element).values() for point in vertices]))
        local = dict(rotation, origin=[0, 0, 0])
        directions.append([np.asarray(rotate_element(vector, local)) for vector in np.eye(3)])
    axes = directions[0] + directions[1] + [np.cross(first_axis, second_axis)
                                           for first_axis in directions[0] for second_axis in directions[1]]
    for axis in axes:
        length = np.linalg.norm(axis)
        if length < 0.000001:
            continue
        projections = [vertices @ (axis / length) for vertices in points]
        overlap = min(values.max() for values in projections) - max(values.min() for values in projections)
        if overlap <= 0.0005:
            return False
    return True


def close(first, second):
    return np.allclose(first, second, atol=0.00001, rtol=0)


def verify_rigid_faces(source, model, state):
    for index, (original, actual) in enumerate(zip(source["elements"][:14], model["elements"][:14])):
        lower = index < 7
        angle = 0 if state < 2 or index == 0 else -22.5 if lower else 22.5
        pivot = [8, 6 if lower else 12, 8]
        expected = [(name, [rotate(point, angle, pivot) for point in vertices], coordinates)
                    for name, vertices, coordinates in world_faces(original)]
        observed = world_faces(actual)
        for name, vertices, coordinates in expected:
            found = False
            for _, candidate_vertices, candidate_uv in observed:
                matching = [next((offset for offset, point in enumerate(candidate_vertices)
                                  if close(point, vertex)), -1) for vertex in vertices]
                if -1 not in matching and close(coordinates, [candidate_uv[offset] for offset in matching]):
                    found = True
                    break
            assert found, f"Rigid limb/UV mismatch: state {state}, element {index}, face {name}"


def verify():
    manifest = json.loads((WORK / "manifest.json").read_text(encoding="utf-8"))
    assert digest((ART / "source/bow.zip").read_bytes()) == manifest["archiveSha256"]
    with zipfile.ZipFile(ART / "source/bow.zip") as archive:
        source = json.loads(archive.read("bow/models/bow.json"))
        for material, source_name in MATERIALS.items():
            assert (ASSETS / f"textures/item/{material}_bow.png").read_bytes() == archive.read(
                f"bow/textures/{source_name}_bow.png")
    rows, models = [], []
    replaced_resources = set()
    for material in MATERIALS:
        item = material + "_bow"
        texture = Image.open(ASSETS / f"textures/item/{item}.png").convert("RGBA")
        assert texture.size == (128, 128)
        pixels = np.asarray(texture)
        for state, suffix in enumerate(SUFFIXES):
            model_path = ASSETS / f"models/item/{item}{suffix}.json"
            model = json.loads(model_path.read_text(encoding="utf-8"))
            assert "parent" not in model
            assert model["display"] == source["display"]
            expected_textures = {"bow": f"ftboss:item/{item}", "particle": f"ftboss:item/{item}"}
            shared_arrow = state and model.get("textures", {}).get("arrow") == "ftboss:item/wooden_crossbow"
            if state:
                expected_textures["arrow"] = "ftboss:item/wooden_crossbow" if shared_arrow else "ftboss:item/material_bow_arrow"
            assert model["textures"] == expected_textures
            assert len(model["elements"]) == (15 if state == 0 else 23 if shared_arrow else 22)
            if state == 0:
                expected = copy.deepcopy(source["elements"])
                for element in expected:
                    for face in element["faces"].values():
                        face["texture"] = "#bow"
                assert model["elements"] == expected, "Imported idle geometry or UV changed"
                old = json.loads((WORK / f"backup/src/main/resources/assets/ftboss/models/item/{item}.json").read_text())
                assert model["overrides"] == old["overrides"]
                for override in model["overrides"]:
                    target = ASSETS / (override["model"].split(":")[1] + ".json")
                    assert (ASSETS / "models" / target.relative_to(ASSETS)).is_file()
            verify_rigid_faces(source, model, state)
            arrow_texture = Image.open(ASSETS / ("textures/item/wooden_crossbow.png" if shared_arrow
                                               else "textures/item/material_bow_arrow.png")).convert("RGBA")
            coverage, densities = [], []
            for element in model["elements"]:
                assert all(-16 <= value <= 32 for key in ("from", "to") for value in element[key])
                if "rotation" in element:
                    assert element["rotation"]["axis"] in "xyz"
                    assert element["rotation"]["angle"] in (-45, -22.5, 0, 22.5, 45)
                for face, vertices, coordinates in world_faces(element):
                    binding = element["faces"][face]["texture"]
                    assert binding in ("#bow", "#arrow")
                    assert all(0 <= value <= 16 for pair in coordinates for value in pair)
                    area = np.linalg.norm(np.cross(np.subtract(vertices[1], vertices[0]),
                                                  np.subtract(vertices[2], vertices[0])))
                    if area < 0.000001:
                        continue
                    texture_uv = np.array(coordinates)
                    positions = (np.arange(16) + 0.5) / 16
                    sample_u, sample_v = np.meshgrid(positions, positions)
                    sample = (texture_uv[0] + sample_u[:, :, None] * (texture_uv[3] - texture_uv[0])
                              + sample_v[:, :, None] * (texture_uv[1] - texture_uv[0]))
                    image = arrow_texture if binding == "#arrow" else texture
                    active_pixels = np.asarray(image)
                    indices = np.clip((sample * image.width / 16).astype(int), 0, image.width - 1)
                    ratio = float(np.mean(active_pixels[indices[:, :, 1], indices[:, :, 0], 3] > 8))
                    if shared_arrow and binding == "#arrow":
                        assert ratio > 0, f"Empty authored arrow face: {item}{suffix}:{face}"
                    else:
                        assert ratio >= 0.99, f"Transparent body face: {item}{suffix}:{face}, {ratio}"
                    coverage.append(ratio)
                    for offset in (1, 3):
                        length = np.linalg.norm(np.subtract(vertices[offset], vertices[0]))
                        densities.append(float(np.linalg.norm(texture_uv[offset] - texture_uv[0]) * image.width / 16 / length))
            if state:
                nock = manifest["states"][str(state)]["nock"]
                for string in model["elements"][14:16]:
                    points = [point for _, vertices, _ in world_faces(string) for point in vertices]
                    assert min(np.linalg.norm(np.subtract(point, nock)) for point in points) <= 0.50001
                arrows = model["elements"][16:]
                if shared_arrow:
                    verify_shared_arrow(model, nock)
                    replaced_resources.add(f"src/main/resources/assets/ftboss/models/item/{item}{suffix}.json")
                else:
                    assert {element["name"] for element in arrows} == {
                        "arrow_shaft", "arrow_head", "arrow_tip", "arrow_fletching", "arrow_fletching_cross", "arrow_nock"}
                nock_element = next(element for element in arrows
                                    if element["name"] == ("arrow_nock_plate" if shared_arrow else "arrow_nock"))
                assert all(nock_element["from"][axis] - 0.00001 <= nock[axis] <= nock_element["to"][axis] + 0.00001
                           for axis in range(3))
                for arrow in arrows:
                    for body in model["elements"][:14]:
                        assert not intersects(arrow, body), f"Arrow intersects bow: {item}{suffix}:{arrow['name']}"
            rows.append({"item": item, "state": state, "elements": len(model["elements"]),
                         "minimumOpaqueCoverage": min(coverage), "pixelDensityRange": [min(densities), max(densities)],
                         "modelSha256": digest(model_path.read_bytes())})
            models.append(str(model_path))
    lint = subprocess.run(["node", str(PROJECT / "tools/model3d/lint_java_model.cjs"), *models],
                          capture_output=True, text=True, encoding="utf-8")
    (WORK / "java-model-lint.log").write_text(lint.stdout + lint.stderr, encoding="utf-8")
    assert lint.returncode == 0, "Java model lint failed"
    for record in manifest["resources"]:
        if record["path"].replace("\\", "/") in replaced_resources:
            continue
        assert digest((PROJECT / record["path"]).read_bytes()) == record["sha256"]
    write_json(WORK / "verification.json", {"passed": True, "models": rows, "runtimeResources": 31,
               "checks": ["byte-identical input textures", "exact idle model/UV/display", "rigid limb UV correspondence",
                          "Java rotation/coordinates", "opaque UV coverage", "string joins", "unchanged pull predicates",
                          "arrows in all 18 pulling models", "arrow nock joins", "arrow/body clearance"]})
    print("PASS: 24 models, 6 original bow textures + arrow atlas; arrows in all 18 pulling models")


if __name__ == "__main__":
    verify()
