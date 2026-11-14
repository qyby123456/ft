import argparse
import copy
import hashlib
import json
import zipfile
from pathlib import Path

from PIL import Image


PROJECT = Path(__file__).resolve().parents[3]
WORK = PROJECT / "_work/material-staff-model-import-20261026"
SOURCE = PROJECT / "src/main/resources"
MATERIALS = {"wooden": "wood", "stone": "stone", "iron": "iron",
             "golden": "gold", "diamond": "diamond", "netherite": "netherite"}


def digest(data):
    return hashlib.sha256(data).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def validate(model, texture_path):
    if model.get("parent"):
        raise ValueError("Model elements must not use a generated parent")
    expected = {"gui", "ground", "fixed", "firstperson_righthand", "firstperson_lefthand",
                "thirdperson_righthand", "thirdperson_lefthand"}
    if not expected <= model["display"].keys():
        raise ValueError("Missing display transforms")
    with Image.open(texture_path) as texture:
        texture.load()
        if texture.size != (64, 64):
            raise ValueError("Unexpected texture dimensions")
    for element in model["elements"]:
        for endpoint in (element["from"], element["to"]):
            if len(endpoint) != 3 or any(not -16 <= value <= 32 for value in endpoint):
                raise ValueError("Element outside Java model bounds")
        rotation = element.get("rotation", {})
        if rotation and (rotation["axis"] not in "xyz" or rotation["angle"] not in (-45, -22.5, 0, 22.5, 45)):
            raise ValueError("Unsupported Java element rotation")
        for face in element["faces"].values():
            if face["texture"][1:] not in model["textures"]:
                raise ValueError("Unresolved face texture")
            if len(face["uv"]) != 4 or any(not 0 <= value <= 16 for value in face["uv"]):
                raise ValueError("UV outside Java coordinates")
            if face.get("rotation", 0) not in (0, 90, 180, 270):
                raise ValueError("Invalid face rotation")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("archive", type=Path)
    arguments = parser.parse_args()
    WORK.mkdir(parents=True, exist_ok=True)
    archive_bytes = arguments.archive.read_bytes()
    archived_copy = WORK / "staff-original.zip"
    if archived_copy.exists() and archived_copy.read_bytes() != archive_bytes:
        raise ValueError("Original archive backup already differs")
    archived_copy.write_bytes(archive_bytes)
    report = {"archive": str(arguments.archive), "archiveSha256": digest(archive_bytes),
              "scope": "model and texture resources only", "models": []}
    with zipfile.ZipFile(arguments.archive) as archive:
        original = json.loads(archive.read("staff/models/staff.json"))
        if original["texture_size"] != [64, 64] or len(original["elements"]) != 8:
            raise ValueError("Unexpected shared model")
        for material, filename in MATERIALS.items():
            item_name = material + "_staff"
            model = copy.deepcopy(original)
            for key in ("format_version", "credit", "texture_size", "groups"):
                model.pop(key, None)
            model["textures"] = {key: "ftboss:item/" + item_name for key in original["textures"]}
            model_relative = Path(f"assets/ftboss/models/item/{item_name}.json")
            texture_relative = Path(f"assets/ftboss/textures/item/{item_name}.png")
            staged_texture = WORK / "resources" / texture_relative
            staged_texture.parent.mkdir(parents=True, exist_ok=True)
            staged_texture.write_bytes(archive.read(f"staff/textures/{filename}_staff.png"))
            staged_model = WORK / "resources" / model_relative
            write_json(staged_model, model)
            validate(model, staged_texture)
            if model["elements"] != original["elements"] or model["display"] != original["display"]:
                raise ValueError("Geometry or display transforms changed")
            for relative in (model_relative, texture_relative):
                destination = SOURCE / relative
                if destination.exists():
                    backup = WORK / "source-before" / relative
                    if not backup.exists():
                        backup.parent.mkdir(parents=True, exist_ok=True)
                        backup.write_bytes(destination.read_bytes())
                destination.parent.mkdir(parents=True, exist_ok=True)
                destination.write_bytes((WORK / "resources" / relative).read_bytes())
            report["models"].append({"id": "ftboss:" + item_name, "model": model_relative.as_posix(),
                                     "modelSha256": digest(staged_model.read_bytes()),
                                     "texture": texture_relative.as_posix(), "textureSha256": digest(staged_texture.read_bytes()),
                                     "elements": 8, "textureSize": [64, 64], "originalGeometryAndDisplayPreserved": True})
    write_json(WORK / "import-manifest.json", report)
    print(json.dumps({"models": len(report["models"]), "textures": len(report["models"]),
                      "geometryAndDisplay": "unchanged", "work": str(WORK)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
