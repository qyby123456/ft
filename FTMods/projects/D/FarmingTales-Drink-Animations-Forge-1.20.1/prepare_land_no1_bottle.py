"""Build the detailed handheld model for Paper Kite Manor's bottled Land No.1.

The source item is a two-dimensional inventory sprite backed by a coarse five-cuboid
placed block.  This generator preserves its original atlas while constructing a
handheld shell, stepped shoulder, open neck, removable screw sleeve and dynamic
liquid volume for 食刻生动.
"""
from __future__ import annotations

import argparse
import hashlib
import io
import json
import os
from copy import deepcopy
from pathlib import Path
import zipfile

from PIL import Image

from container_assets import face_piece
from liquid_textures import liquid_atlas
from prepare_cocktail_profiles import ASSETS, ROOT, READ, corners, dump

NS = "papercraft_magic_decoration"
NAME = "bottle_of_land_no1"
ITEM = f"{NS}:{NAME}"
SOURCE_MODEL = f"assets/{NS}/models/block/{NAME}.json"
SOURCE_TEXTURE = f"assets/{NS}/textures/block/{NAME}.png"
BASE = f"animation/papercraft/{NAME}"
TEXTURE = f"papercraft/{NAME}_0"
LIQUID_TEXTURE = f"papercraft/{NAME}_liquid"
ROTATION = {"angle": 22.5, "axis": "y", "origin": [8, -0.5, 8]}


def find_jar(explicit: str | None) -> Path:
    if explicit:
        path = Path(explicit)
        if path.is_file():
            return path
        raise FileNotFoundError(path)
    candidates: list[Path] = []
    manifest = ROOT / "papercraft-sources.json"
    if manifest.exists():
        recorded = Path(READ(manifest).get("jar", ""))
        if recorded.is_file():
            candidates.append(recorded)
    candidates.extend((ROOT.parents[2] / "mods").glob("*Paper_Kite*.jar"))
    appdata = Path(os.environ.get("APPDATA", ""))
    candidates.extend((appdata / ".minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods").glob("*Paper_Kite*.jar"))
    candidates.extend((appdata / ".minecraft/versions/FarmingTales Forge/mods").glob("*Paper_Kite*.jar"))
    for path in candidates:
        if path.is_file():
            return path
    raise FileNotFoundError("Paper_Kite_Manor JAR not found; pass --jar")


def texture_face(uv, face="north"):
    return {face: {"uv": list(uv), "texture": "#0"}}


def cube(name, low, high, side_uv, top_uv=None, faces=None, origin=None):
    """Create a source-textured detail cuboid in the bottle's 22.5-degree frame."""
    if faces is None:
        faces = ["north", "east", "south", "west", "up", "down"]
    data = {
        "name": name,
        "from": list(low),
        "to": list(high),
        "rotation": deepcopy(ROTATION),
        "faces": {},
    }
    if origin is not None:
        data["rotation"]["origin"] = list(origin)
    for face in faces:
        uv = top_uv if face in ("up", "down") and top_uv is not None else side_uv
        data["faces"][face] = {"uv": list(uv), "texture": "#0"}
    return data


def flat(name, low, high, face, uv):
    result = cube(name, low, high, uv, faces=[face])
    return result


def build_models(source):
    original = deepcopy(source["elements"])
    for i, element in enumerate(original):
        element["name"] = f"source_{i}"
        for face in element["faces"].values():
            face.pop("cullface", None)

    body = []
    # Keep the source foot, then add a recessed plinth and four raised corner seams.
    body.append(original[0])
    body.append(cube("recessed_bottom_plinth", [5.0, 0.04, 5.0], [11.0, 0.42, 11.0],
                     [8.1, 8.0, 9.6, 8.2], [8.15, 6.35, 9.65, 7.85]))

    source_body = original[1]
    # Three height bands per face retain exact physical UV scale while giving the
    # silhouette and lighting more structure than the source's one solid cuboid.
    for face in ("north", "south"):
        for number, (a, b) in enumerate(((0.5, 2.0), (2.0, 15.5), (15.5, 17.5))):
            body.append(face_piece(source_body, face, (4.0, a, 12.0, b), f"_{number}"))
    for face in ("east", "west"):
        for number, (a, b) in enumerate(((0.5, 2.0), (2.0, 15.5), (15.5, 17.5))):
            body.append(face_piece(source_body, face, (a, 4.0, b, 12.0), f"_{number}"))

    seam_uv = [4.0, 0.0, 4.18, 8.5]
    for number, (x, z) in enumerate(((4.0, 4.0), (11.72, 4.0), (4.0, 11.72), (11.72, 11.72))):
        body.append(cube(f"raised_corner_seam_{number}", [x, 0.72, z], [x + .28, 17.26, z + .28], seam_uv,
                         [9.5, 5.75, 9.72, 5.97]))

    # The cream label is a shallow physical plaque, with separate frame rails.
    for suffix, face, z in (("front", "north", 3.91), ("back", "south", 12.09)):
        body.append(flat(f"raised_label_{suffix}", [5.05, 3.15, z], [10.95, 14.75, z], face,
                         [0.42, 0.62, 3.58, 7.55]))
    border_uv = [0.15, 0.6, 0.42, 7.6]
    for suffix, z in (("front", 3.87), ("back", 12.13)):
        face = "north" if suffix == "front" else "south"
        body.extend([
            flat(f"label_{suffix}_left", [4.83, 2.92, z], [5.13, 14.98, z], face, border_uv),
            flat(f"label_{suffix}_right", [10.87, 2.92, z], [11.17, 14.98, z], face, border_uv),
            flat(f"label_{suffix}_top", [5.13, 14.68, z], [10.87, 14.98, z], face, [0.4, .6, 3.6, .78]),
            flat(f"label_{suffix}_bottom", [5.13, 2.92, z], [10.87, 3.22, z], face, [0.4, 7.35, 3.6, 7.55]),
        ])

    # Replace the source's single square shoulder with four readable bevel steps.
    shoulder_uv = [8.0, 5.75, 9.75, 6.25]
    shoulder_top = [9.75, 5.75, 8.0, 4.0]
    for number, (low, high) in enumerate((
        ([4.48, 17.46, 4.48], [11.52, 18.02, 11.52]),
        ([4.86, 17.98, 4.86], [11.14, 18.52, 11.14]),
        ([5.28, 18.48, 5.28], [10.72, 19.00, 10.72]),
        ([5.68, 18.96, 5.68], [10.32, 19.46, 10.32]),
    )):
        body.append(cube(f"stepped_shoulder_{number}", low, high, shoulder_uv, shoulder_top))
    body.append(cube("neck_collar", [5.82, 19.38, 5.82], [10.18, 20.18, 10.18],
                     [10.0, 4.0, 11.0, 4.25], [11.0, 4.0, 10.0, 3.0]))

    neck = []
    neck_source = original[3]
    # Four thin wall faces and a two-level lip leave a real opening after cap removal.
    for face in ("north", "east", "south", "west"):
        wall = deepcopy(neck_source)
        wall["name"] = f"open_neck_{face}"
        wall["from"] = [6.56, 19.58, 6.56]
        wall["to"] = [9.44, 23.12, 9.44]
        axis = {"north": 2, "south": 2, "east": 0, "west": 0}[face]
        plane = wall["from"][axis] if face in ("north", "east") else wall["to"][axis]
        wall["from"][axis] = wall["to"][axis] = plane
        wall["faces"] = {face: deepcopy(neck_source["faces"][face])}
        # Use only the lower portion of the original long red neck atlas.
        wall["faces"][face]["uv"] = [4.08, 0.12, 4.78, 1.92]
        neck.append(wall)
    lip_uv = [10.0, 3.0, 10.7, 3.18]
    neck.extend([
        cube("mouth_lip_north", [6.34, 22.92, 6.34], [9.66, 23.18, 6.72], lip_uv, [10, 3, 10.75, 3.75]),
        cube("mouth_lip_south", [6.34, 22.92, 9.28], [9.66, 23.18, 9.66], lip_uv, [10, 3, 10.75, 3.75]),
        cube("mouth_lip_east", [6.34, 22.92, 6.72], [6.72, 23.18, 9.28], lip_uv, [10, 3, 10.75, 3.75]),
        cube("mouth_lip_west", [9.28, 22.92, 6.72], [9.66, 23.18, 9.28], lip_uv, [10, 3, 10.75, 3.75]),
        cube("inner_lip_north", [6.72, 22.76, 6.70], [9.28, 23.02, 6.82], [10.15, 2.7, 10.65, 2.9]),
        cube("inner_lip_south", [6.72, 22.76, 9.18], [9.28, 23.02, 9.30], [10.15, 2.7, 10.65, 2.9]),
        cube("inner_lip_east", [6.70, 22.76, 6.82], [6.82, 23.02, 9.18], [10.15, 2.7, 10.65, 2.9]),
        cube("inner_lip_west", [9.18, 22.76, 6.82], [9.30, 23.02, 9.18], [10.15, 2.7, 10.65, 2.9]),
    ])

    cap = [
        cube("screw_sleeve", [6.35, 19.55, 6.35], [9.65, 28.48, 9.65], [10.0, .75, 10.75, 2.55], [10.75, .75, 10.0, 0]),
        cube("screw_sleeve_lower_band", [6.15, 19.34, 6.15], [9.85, 20.00, 9.85], [10, 2.45, 11, 2.7], [10.75, .75, 10, 0]),
        cube("screw_sleeve_top_band", [6.18, 27.82, 6.18], [9.82, 28.55, 9.82], [10, .58, 11, .82], [10.75, .75, 10, 0]),
    ]
    ridge_uv = [10.05, .9, 10.18, 2.4]
    for number, (x, z, dx, dz) in enumerate((
        (6.20, 6.20, .18, .18), (7.05, 6.18, .14, .18), (8.82, 6.18, .14, .18), (9.62, 6.20, .18, .18),
        (6.20, 9.62, .18, .18), (7.05, 9.64, .14, .18), (8.82, 9.64, .14, .18), (9.62, 9.62, .18, .18),
    )):
        cap.append(cube(f"cap_grip_ridge_{number}", [x, 20.05, z], [x + dx, 27.78, z + dz], ridge_uv,
                        [10.05, .9, 10.18, 1.03]))

    textures = {"0": f"farmingtales_drink_anim:{TEXTURE}", "particle": f"farmingtales_drink_anim:{TEXTURE}"}
    def model(elements):
        return {"textures": textures, "render_type": "minecraft:cutout", "ambientocclusion": False, "elements": elements}
    return model(body), model(cap), model(neck)


def update_mapping(path: Path, key: str, value, after: str | None = None):
    source = READ(path)
    output = {}
    inserted = False
    for old_key, old_value in source.items():
        if old_key == key:
            continue
        output[old_key] = old_value
        if after is not None and old_key == after:
            output[key] = value
            inserted = True
    if not inserted:
        output[key] = value
    dump(path, output)


def main(jar_path: str | None = None):
    jar = find_jar(jar_path)
    with zipfile.ZipFile(jar) as archive:
        model_raw = archive.read(SOURCE_MODEL)
        texture_raw = archive.read(SOURCE_TEXTURE)
    source = json.loads(model_raw)
    body, cap, neck = build_models(source)

    texture_path = ASSETS / f"textures/{TEXTURE}.png"
    texture_path.parent.mkdir(parents=True, exist_ok=True)
    texture_path.write_bytes(texture_raw)
    image = Image.open(io.BytesIO(texture_raw)).convert("RGBA")
    liquid, side_uv, surface_uv = liquid_atlas(image, [4, 0, 8, 8.5], image, [8, 0, 10, 2])
    liquid.save(ASSETS / f"textures/{LIQUID_TEXTURE}.png")

    dump(ASSETS / f"models/{BASE}_body.json", body)
    dump(ASSETS / f"models/{BASE}_cap.json", cap)
    dump(ASSETS / f"models/{BASE}_neck.json", neck)
    closed=deepcopy(body)
    closed["elements"]=deepcopy(body["elements"]+neck["elements"]+cap["elements"])
    closed["display"]={
        "gui":{"rotation":[30,225,0],"translation":[0,-3,0],"scale":[.52,.52,.52]},
        "ground":{"rotation":[0,0,0],"translation":[0,1,0],"scale":[.28,.28,.28]},
        "fixed":{"rotation":[0,180,0],"translation":[0,-3,0],"scale":[.48,.48,.48]},
        "thirdperson_righthand":{"rotation":[0,90,-35],"translation":[0,1,-2],"scale":[.38,.38,.38]},
        "thirdperson_lefthand":{"rotation":[0,-90,35],"translation":[0,1,-2],"scale":[.38,.38,.38]},
        "firstperson_righthand":{"rotation":[0,90,-20],"translation":[1,2,-1],"scale":[.42,.42,.42]},
        "firstperson_lefthand":{"rotation":[0,-90,20],"translation":[1,2,-1],"scale":[.42,.42,.42]}
    }
    dump(ASSETS / f"models/{BASE}_closed.json",closed)

    all_points = [point for element in body["elements"] + cap["elements"] + neck["elements"] for point in corners(element)]
    profile = {
        "id": ITEM,
        "style": "WINE",
        "scale": .54,
        "width": 8,
        "depth": 8,
        "capPivot": [8, 28.55, 8],
        "neckJoin": 0,
        "neckMin": 19.58 / 16,
        "neckMax": 23.18 / 16,
        "mouthY": 23.18,
        "bodyModel": BASE + "_body",
        "capModel": BASE + "_cap",
        "neckModel": BASE + "_neck",
        "boundsMin": [min(point[i] for point in all_points) for i in range(3)],
        "boundsMax": [max(point[i] for point in all_points) for i in range(3)],
        "liquid": [
            {"min": [(4.46 - 8) / 16, 1 / 16, (4.46 - 8) / 16], "max": [(11.54 - 8) / 16, 16.9 / 16, (11.54 - 8) / 16],
             "sideUV": side_uv, "surfaceUV": surface_uv, "texture": f"farmingtales_drink_anim:textures/{LIQUID_TEXTURE}.png"},
            {"min": [(5.05 - 8) / 16, 16.9 / 16, (5.05 - 8) / 16], "max": [(10.95 - 8) / 16, 18.35 / 16, (10.95 - 8) / 16],
             "sideUV": side_uv, "surfaceUV": surface_uv, "texture": f"farmingtales_drink_anim:textures/{LIQUID_TEXTURE}.png"},
            {"min": [(6.72 - 8) / 16, 18.35 / 16, (6.72 - 8) / 16], "max": [(9.28 - 8) / 16, 22.88 / 16, (9.28 - 8) / 16],
             "sideUV": side_uv, "surfaceUV": surface_uv, "texture": f"farmingtales_drink_anim:textures/{LIQUID_TEXTURE}.png"},
        ],
        "canLid": None,
        # The body is rotated in the source; this optional frame aligns only the liquid.
        "cupShape": {"gripY": 0, "radius": 0, "liquidYaw": 22.5},
    }
    profiles = [entry for entry in READ(ASSETS / "drink_profiles.json") if entry["id"] != ITEM]
    insertion = next((i + 1 for i, entry in enumerate(profiles) if entry["id"] == f"{NS}:land_no1"), len(profiles))
    profiles.insert(insertion, profile)
    dump(ASSETS / "drink_profiles.json", profiles)

    masks = [
        {"min": [4, .5, 4], "max": [12, 17.5, 12], "origin": [8, -.5, 8], "axis": "y", "angle": 22.5, "rescale": False, "shiftY": 0},
        {"min": [4.48, 17.46, 4.48], "max": [11.52, 20.18, 11.52], "origin": [8, -.5, 8], "axis": "y", "angle": 22.5, "rescale": False, "shiftY": 0},
        {"min": [6.34, 19.58, 6.34], "max": [9.66, 23.18, 9.66], "origin": [8, -.5, 8], "axis": "y", "angle": 22.5, "rescale": False, "shiftY": 0},
        {"min": [6.15, 22.34, 6.15], "max": [9.85, 28.55, 9.85], "origin": [8, -.5, 8], "axis": "y", "angle": 22.5, "rescale": False, "shiftY": 0},
    ]
    update_mapping(ASSETS / "hand_volumes.json", ITEM, masks, f"{NS}:land_no1")
    update_mapping(ASSETS / "decorations.json", ITEM, [], f"{NS}:land_no1")

    atlas_path = ROOT / "src/main/resources/assets/minecraft/atlases/blocks.json"
    atlas = READ(atlas_path)
    wanted = {f"farmingtales_drink_anim:{TEXTURE}", f"farmingtales_drink_anim:{LIQUID_TEXTURE}"}
    atlas["sources"] = [entry for entry in atlas["sources"] if entry.get("resource") not in wanted]
    for resource in sorted(wanted):
        atlas["sources"].append({"type": "minecraft:single", "resource": resource})
    dump(atlas_path, atlas)

    fixture = ROOT / f"src/smoke/resources/assets/{NS}"
    item_override={"parent": f"farmingtales_drink_anim:{BASE}_closed"}
    dump(fixture / f"models/item/{NAME}.json",item_override)
    dump(fixture / f"blockstates/{NAME}.json", {"variants": {"": {"model": f"farmingtales_drink_anim:{BASE}_closed"}}})
    # The source inventory model is item/generated; this namespace override supplies
    # the requested closed 3D model outside the custom first-person renderer too.
    dump(ROOT / f"src/main/resources/assets/{NS}/models/item/{NAME}.json",item_override)

    manifest_path = ROOT / "papercraft-sources.json"
    manifest = READ(manifest_path)
    recorded = {entry["entry"]: entry for entry in manifest.get("assets", [])}
    for entry, raw in ((SOURCE_MODEL, model_raw), (SOURCE_TEXTURE, texture_raw)):
        recorded[entry] = {"entry": entry, "sha256": hashlib.sha256(raw).hexdigest()}
    manifest["assets"] = list(recorded.values())
    details = [entry for entry in manifest.get("items", []) if entry.get("id") != ITEM]
    details.append({"id": ITEM, "source": SOURCE_MODEL, "liquid": ["body", "shoulder", "neck"],
                    "ground": [], "serving": None, "props": [], "moving_parts": ["screw_cap"],
                    "detail": "split shell, raised label, corner seams, four-step shoulder, hollow mouth"})
    manifest["items"] = details
    # Preserve the historical source path when the byte-identical JAR moved drives.
    if manifest.get("sha256") != hashlib.sha256(jar.read_bytes()).hexdigest():
        manifest["jar"] = str(jar)
        manifest["sha256"] = hashlib.sha256(jar.read_bytes()).hexdigest()
    dump(manifest_path, manifest)
    print(f"Prepared {ITEM}: {len(body['elements'])} body, {len(neck['elements'])} neck and {len(cap['elements'])} cap elements.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar")
    args = parser.parse_args()
    main(args.jar)
