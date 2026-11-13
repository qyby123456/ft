import copy
import json
import math
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/ftdungeontrading/models/block"
TOLERANCE = 0.000002


def equivalent(first, second):
    return all(math.isclose(left, right, rel_tol=0, abs_tol=TOLERANCE) for left, right in zip(first, second))


def main():
    source = json.loads((ASSETS / "trading_table.json").read_text(encoding="utf-8"))
    pieces = {}
    for part, offset in [("left", -8), ("right", 8)]:
        half = json.loads((ASSETS / f"trading_table_{part}.json").read_text(encoding="utf-8"))
        for element in half["elements"]:
            element = copy.deepcopy(element)
            for boundary in ["from", "to"]:
                element[boundary][0] += offset
            if "rotation" in element:
                element["rotation"]["origin"][0] += offset
            pieces.setdefault(element["name"], []).append(element)
    checked_faces = 0
    split_components = 0
    for element in source["elements"]:
        sections = sorted(pieces.pop(element["name"]), key=lambda section: section["from"][0])
        if "rotation" in element or element["from"][0] == element["to"][0]:
            if len(sections) != 1:
                raise RuntimeError("Rigid part was split: " + element["name"])
            section = sections[0]
            if not equivalent(section["from"], element["from"]) or not equivalent(section["to"], element["to"]):
                raise RuntimeError("Rigid part moved: " + element["name"])
            original_rotation, section_rotation = element.get("rotation"), section.get("rotation")
            rotation_equal = original_rotation == section_rotation if original_rotation is None else (
                    section_rotation is not None and equivalent(original_rotation["origin"], section_rotation["origin"])
                    and original_rotation["axis"] == section_rotation["axis"] and original_rotation["angle"] == section_rotation["angle"])
            if not rotation_equal or section["faces"] != element["faces"]:
                raise RuntimeError("Rigid rotation or UV changed: " + element["name"])
            checked_faces += len(element["faces"])
            continue
        if not equivalent(sections[0]["from"], element["from"]) or not equivalent(sections[-1]["to"], element["to"]):
            raise RuntimeError("Incomplete geometric coverage: " + element["name"])
        if len(sections) > 1:
            split_components += 1
            if not math.isclose(sections[0]["to"][0], sections[1]["from"][0], abs_tol=TOLERANCE):
                raise RuntimeError("Gap at table seam: " + element["name"])
            if "east" in sections[0]["faces"] or "west" in sections[1]["faces"]:
                raise RuntimeError("Internal seam faces were not removed")
        for section in sections:
            for axis in [1, 2]:
                if section["from"][axis] != element["from"][axis] or section["to"][axis] != element["to"][axis]:
                    raise RuntimeError("Non-width dimension changed: " + element["name"])
            length = element["to"][0] - element["from"][0]
            start = (section["from"][0] - element["from"][0]) / length
            end = (section["to"][0] - element["from"][0]) / length
            for side, face in section["faces"].items():
                original_face = element["faces"][side]
                if face["texture"] != original_face["texture"] or face.get("rotation", 0) != original_face.get("rotation", 0):
                    raise RuntimeError("Material changed at seam")
                expected = original_face["uv"]
                if side in {"north", "south", "up", "down"}:
                    left, top, right, bottom = expected
                    first, last = (1 - end, 1 - start) if side == "north" else (start, end)
                    expected = [left + (right - left) * first, top, left + (right - left) * last, bottom]
                if not equivalent(expected, face["uv"]):
                    raise RuntimeError("UV continuity failed: " + element["name"] + "/" + side)
                checked_faces += 1
    if pieces:
        raise RuntimeError("Unmatched runtime components")
    report = {"passed": True, "source_components": len(source["elements"]), "split_components": split_components,
              "checked_faces": checked_faces, "coordinate_uv_tolerance": TOLERANCE,
              "surface_coverage_and_uv_continuity": True, "rotation_and_materials_preserved": True,
              "limitations": ["Analytic split correspondence, not a Minecraft client or floating-point rasterization proof"]}
    (ROOT / "model_sources/qa/assembly-structure-check.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report))


if __name__ == "__main__":
    main()
