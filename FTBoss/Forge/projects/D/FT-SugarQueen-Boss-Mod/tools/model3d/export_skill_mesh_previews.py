import argparse
import json
import re
import zipfile
from pathlib import Path

from optimize_skill_meshes import coordinates, flat_faces


def face_data(face, family):
    points = coordinates(face)
    uv = face.get("uv")
    if uv and not isinstance(uv[0], list):
        uv = [uv[index:index + 2] for index in range(0, len(uv), 2)]
    material = face.get("m", face.get("materials", face.get("material", 0)))
    color = face.get("color", face.get("tint", face.get("vertexColors", [0xFFFFFF])[0]))
    alpha = face.get("a", face.get("alphas", face.get("opacity", face.get("alpha", 1))))
    texture = None
    if family == "maque":
        texture = "assets/ftboss/maque_arcane/arcane_atlas.png"
        uv = [[(material * 64 + (59 if corner in (1, 2) else 4)) / 384,
               (251 if corner >= 2 else 4) / 256] for corner in range(4)]
    elif family in ("dandan", "optimized"):
        texture = "assets/ftboss/textures/fx/dandan/materials.png" if family == "dandan" else "assets/ftboss/fx/spell_materials.png"
        columns, width, height = (8, 128, 32) if family == "dandan" else (64, 1024, 1024)
        uv = [[(material % columns * 16 + 2 + point[0] * 11) / width,
               (material // columns * 16 + 2 + point[1] * 11) / height] for point in uv]
    elif family == "lemon":
        texture = "assets/ftboss/lemoncat_v3/atlas.png"
    elif family == "gongdi":
        texture = "assets/ftboss/textures/fx/gongdi/" + material + ".png"
    return {"p": points, "uv": uv, "texture": texture, "color": color, "alpha": alpha,
            "bone": face.get("b", face.get("bone", face.get("bones", 0))), "part": face.get("part", ""),
            "pivot": face.get("pivot", face.get("pivots", [0, 0, 0]))}


def scenes(name, resource):
    result = {}
    if name.endswith("fx/optimized_spell_models.json"):
        for model_id, model in resource.items():
            for label, groups in (("body", [key for key in model["groups"] if not key.startswith("beam")]),
                                  ("beam", [key for key in model["groups"] if key.startswith("beam")])):
                result[model_id + "_" + label] = [face_data(face, "optimized") for key in groups for face in (flat_faces(model["groups"][key]) or [])]
    elif name.endswith("manor/domain_geometry_v2.json"):
        for model_id, model in resource.items():
            result["domain_" + model_id] = [dict(face_data(face, "color"), poseKey=key) for key, group in model["groups"].items() for face in (flat_faces(group) or [])]
    elif "maque_arcane/" in name:
        for model in resource["models"]:
            result[model["id"]] = [face_data(face, "maque") for face in model["faces"]]
    elif "lemoncat_v3/" in name:
        for model in resource["models"]:
            result[model["id"]] = [face_data(face, "lemon") for face in model["faces"]]
    elif "gongdi_spells" in name:
        for model in resource["models"]:
            result[model["id"]] = [dict(face_data(face, "gongdi"), poseKey=part["name"]) for part in model["parts"] for face in part["faces"]]
    elif "dandan/" in name:
        result[resource["id"]] = [face_data(face, "dandan") for face in resource["faces"]]
    else:
        raise ValueError("Unsupported preview family: " + name)
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("work", type=Path)
    arguments = parser.parse_args()
    work = arguments.work
    output = work / "previews"
    output.mkdir(exist_ok=True)
    manifest = []
    with zipfile.ZipFile(work / "baseline.jar") as archive:
        for source in sorted((work / "overlay").rglob("*.json")):
            name = source.relative_to(work / "overlay").as_posix()
            before = scenes(name, json.loads(archive.read(name)))
            after = scenes(name, json.loads(source.read_text(encoding="utf-8")))
            for model_id, faces in after.items():
                if faces == before[model_id]:
                    continue
                identifier = name.split("/")[2] + "_" + model_id
                file_name = re.sub(r"[^a-zA-Z0-9_-]", "_", identifier) + ".json"
                item = {"id": identifier, "model": model_id, "resource": name,
                        "before": before[model_id], "after": faces}
                (output / file_name).write_text(json.dumps(item, separators=(",", ":")), encoding="utf-8")
                pose_key = model_id if "maque_arcane/" in name else "domain:" + model_id.removeprefix("domain_") if "/manor/" in name else "gongdi:" + model_id if "gongdi_spells" in name else "lemon:" + model_id if "lemoncat_v3" in name else "dandan:" + model_id if "/dandan/" in name else "optimized:" + model_id
                manifest.append({"id": identifier, "model": model_id, "file": file_name, "poseKey": pose_key,
                                 "before": len(before[model_id]), "after": len(faces), "animated": True})
                for face in faces:
                    texture = face["texture"]
                    if texture:
                        target = output / texture
                        if not target.exists():
                            target.parent.mkdir(parents=True, exist_ok=True)
                            target.write_bytes(archive.read(texture))
    (output / "manifest.json").write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    print("Exported", len(manifest), "changed runtime meshes with original textures")


if __name__ == "__main__":
    main()
