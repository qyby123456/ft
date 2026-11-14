import argparse
import atexit
import copy
import functools
import hashlib
import json
import math
import os
import shutil
import subprocess
import zipfile
from pathlib import Path

from arrows import arrow_elements, texture_bytes


PROJECT = Path(__file__).resolve().parents[3]
ASSETS = Path(os.environ.get("FTBOSS_BOW_ASSETS", PROJECT / "src/main/resources/assets/ftboss"))
ART = Path(os.environ.get("FTBOSS_BOW_ART", PROJECT / "art/material_bows_completion"))
WORK = Path(os.environ.get("FTBOSS_BOW_WORK", PROJECT / "_work/material-bows-completion-20261022"))
MATERIALS = {
    "wooden": "wood", "stone": "stone", "iron": "iron",
    "golden": "gold", "diamond": "diamond", "netherite": "netherite",
}
SUFFIXES = ["", "_pulling_0", "_pulling_1", "_pulling_2"]
GEOMETRY = None


def geometry(request):
    global GEOMETRY
    if GEOMETRY is None:
        GEOMETRY = subprocess.Popen(["node", str(Path(__file__).with_name("geometry.cjs"))],
                                    stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True, encoding="utf-8")
    GEOMETRY.stdin.write(json.dumps(request) + "\n")
    GEOMETRY.stdin.flush()
    return json.loads(GEOMETRY.stdout.readline())


@atexit.register
def close_geometry():
    if GEOMETRY is not None:
        GEOMETRY.stdin.close()
        GEOMETRY.wait(timeout=5)


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def digest(data):
    return hashlib.sha256(data).hexdigest()


def rotate(point, angle, origin):
    return rotated_point(tuple(point), angle, tuple(origin))


@functools.lru_cache(maxsize=None)
def rotated_point(point, angle, origin):
    return geometry({"type": "rotate", "point": point, "angle": angle, "origin": origin})


def corners(element):
    return box_corners(tuple(element["from"]), tuple(element["to"]))


@functools.lru_cache(maxsize=None)
def box_corners(start, end):
    return geometry({"type": "corners", "from": start, "to": end})


def face_uv(face):
    first_u, first_v, last_u, last_v = face["uv"]
    values = [[first_u, first_v], [first_u, last_v], [last_u, last_v], [last_u, first_v]]
    turns = face.get("rotation", 0) // 90
    return [values[(index + turns) % 4] for index in range(4)]


def near(first, second):
    return max(abs(left - right) for left, right in zip(first, second)) < 0.00001


def bend(element, angle, pivot):
    result = copy.deepcopy(element)
    original_rotation = element.get("rotation", {"angle": 0, "origin": [8, 8, 8]})
    total = original_rotation["angle"] + angle
    quarter = 90 if total > 45 else -90 if total < -45 else 0
    origin = original_rotation["origin"]
    new_origin = rotate(origin, angle, pivot)
    displacement = [new_origin[axis] - origin[axis] for axis in range(3)]
    transformed = {
        face: [[value + displacement[axis] for axis, value in enumerate(rotate(point, quarter, origin))]
               for point in vertices] for face, vertices in corners(element).items()
    }
    vertices = [point for face in transformed.values() for point in face]
    result["from"] = [min(point[axis] for point in vertices) for axis in range(3)]
    result["to"] = [max(point[axis] for point in vertices) for axis in range(3)]
    result["rotation"] = {"axis": "x", "angle": total - quarter, "origin": new_origin}
    result["faces"] = {}
    for old_name, old_vertices in transformed.items():
        for new_name, new_vertices in corners(result).items():
            matching = [next((index for index, point in enumerate(old_vertices) if near(point, vertex)), -1)
                        for vertex in new_vertices]
            if -1 in matching:
                continue
            definition = copy.deepcopy(element["faces"][old_name])
            expected_uv = [face_uv(definition)[index] for index in matching]
            for rotation in (0, 90, 180, 270):
                definition["rotation"] = rotation
                if face_uv(definition) == expected_uv:
                    if rotation == 0:
                        definition.pop("rotation")
                    result["faces"][new_name] = definition
                    break
            else:
                raise ValueError("Cannot preserve UV orientation")
            break
        else:
            raise ValueError("Cannot remap rotated face")
    return result


def draw_strings(source, limb_angle, string_angle):
    lower = rotate([8, -8, 11.3], -limb_angle, [8, 6, 8])
    upper = rotate([8, 25, 11.3], limb_angle, [8, 12, 8])
    tangent = math.tan(math.radians(string_angle))
    nock_y = (upper[1] + lower[1]) / 2 + (upper[2] - lower[2]) / (2 * tangent)
    nock_z = lower[2] + (nock_y - lower[1]) * tangent
    elements = []
    for attachment, direction in ((lower, 1), (upper, -1)):
        length = abs(nock_y - attachment[1]) / math.cos(math.radians(string_angle))
        element = {
            "from": [8, attachment[1] if direction == 1 else attachment[1] - length, attachment[2] - 0.5],
            "to": [8, attachment[1] + length if direction == 1 else attachment[1], attachment[2] + 0.5],
            "rotation": {"axis": "x", "angle": direction * string_angle, "origin": attachment},
            "faces": {},
        }
        for name in ("east", "west"):
            face = copy.deepcopy(source["faces"][name])
            face["uv"][3] = face["uv"][1] + length / 8
            element["faces"][name] = face
        elements.append(element)
    return elements, [8, nock_y, nock_z]


def state_elements(source, state):
    if state == 0:
        return copy.deepcopy(source["elements"]), None
    limb_angle = 0 if state == 1 else 22.5
    result = [copy.deepcopy(source["elements"][0])]
    for index, element in enumerate(source["elements"][1:14], start=1):
        lower = index < 7
        result.append(bend(element, -limb_angle if lower else limb_angle, [8, 6 if lower else 12, 8]))
    strings, nock = draw_strings(source["elements"][14], limb_angle, 45 if state == 3 else 22.5)
    return result + strings + arrow_elements(nock), nock


def import_models(archive_path):
    WORK.mkdir(parents=True, exist_ok=True)
    source_dir = ART / "source"
    source_dir.mkdir(parents=True, exist_ok=True)
    original_archive = archive_path.read_bytes()
    saved_archive = source_dir / "bow.zip"
    if saved_archive.exists() and saved_archive.read_bytes() != original_archive:
        raise ValueError("Preserved import archive differs; use a new task directory")
    saved_archive.write_bytes(original_archive)
    with zipfile.ZipFile(archive_path) as archive:
        source = json.loads(archive.read("bow/models/bow.json"))
        for entry in archive.infolist():
            if entry.is_dir():
                continue
            target = source_dir / entry.filename
            if not target.resolve().is_relative_to(source_dir.resolve()):
                raise ValueError("Unsafe archive path")
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(archive.read(entry))
        if len(source["elements"]) != 15 or source["texture_size"] != [128, 128]:
            raise ValueError("Unexpected source model layout")
        arrow_texture = ASSETS / "textures/item/material_bow_arrow.png"
        arrow_texture.parent.mkdir(parents=True, exist_ok=True)
        arrow_texture.write_bytes(texture_bytes())
        records = [{"path": arrow_texture.relative_to(PROJECT).as_posix(),
                    "entry": arrow_texture.relative_to(PROJECT / "src/main/resources").as_posix(),
                    "sha256": digest(arrow_texture.read_bytes())}]
        states = {}
        for material, source_name in MATERIALS.items():
            item = material + "_bow"
            texture = archive.read(f"bow/textures/{source_name}_bow.png")
            outputs = {ASSETS / f"textures/item/{item}.png": texture}
            for state, suffix in enumerate(SUFFIXES):
                elements, nock = state_elements(source, state)
                for element in elements:
                    for face in element["faces"].values():
                        if face["texture"] == "#missing":
                            face["texture"] = "#bow"
                model = {"texture_size": source["texture_size"], "gui_light": source["gui_light"],
                         "textures": {"bow": f"ftboss:item/{item}", "particle": f"ftboss:item/{item}"},
                         "display": source["display"], "elements": elements}
                if state:
                    model["textures"]["arrow"] = "ftboss:item/material_bow_arrow"
                if state == 0:
                    previous = json.loads((ASSETS / f"models/item/{item}.json").read_text(encoding="utf-8"))
                    model["overrides"] = previous["overrides"]
                outputs[ASSETS / f"models/item/{item}{suffix}.json"] = (
                    json.dumps(model, ensure_ascii=False, indent=2) + "\n").encode("utf-8")
                states[str(state)] = {"nock": nock, "elements": len(elements)}
            for target, data in outputs.items():
                backup = WORK / "backup" / target.relative_to(PROJECT)
                if target.exists() and not backup.exists():
                    backup.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(target, backup)
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(data)
                records.append({"path": target.relative_to(PROJECT).as_posix(),
                                "entry": target.relative_to(PROJECT / "src/main/resources").as_posix(),
                                "sha256": digest(data)})
    manifest = {"archive": str(archive_path), "archiveSha256": digest(original_archive),
                "mapping": MATERIALS, "states": states, "resources": records,
                "docsImpact": "none; resource-only import, no identifiers or gameplay changes"}
    write_json(WORK / "manifest.json", manifest)
    print(json.dumps({"models": 24, "textures": 7, "manifest": str(WORK / "manifest.json")}, ensure_ascii=False))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("archive", type=Path)
    import_models(parser.parse_args().archive)
