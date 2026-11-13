import json
from pathlib import Path


ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "TACZ视角与检视重制-v7" / "农场物语-橙子果酒-TACZ视角与检视重制-v7.bbmodel"
OUTPUT_DIR = ROOT / "TACZ动态视角-v8"
OUTPUT = OUTPUT_DIR / "农场物语-橙子果酒-TACZ动态视角-v8.bbmodel"

CAMERA_NAME = "camera_first_person"
BASE_POSITION = [0, 16, 16.5]

# Runtime camera peaks are intentionally visible. These multipliers bring the
# Blockbench authoring view to the same readable range while preserving timing.
ROTATION_SCALE = {
    "animation.orange_wine.preview_complete": 2.25,
    "animation.orange_wine.preview_draw_hold_put_away": 2.25,
    "animation.orange_wine.draw": 2.25,
    "animation.orange_wine.put_away": 3.75,
    "animation.orange_wine.inspect": 2.15,
    "animation.orange_wine.drink": 4.2,
    "animation.orange_wine.drink_stow_empty_bottle": 4.2,
    "animation.orange_wine.drink_empty_hand_recover": 4.2,
    "animation.orange_wine.new_bottle_offscreen_pause": 4.2,
    "animation.orange_wine.drink_draw_new_bottle": 4.2,
}

POSITION_SCALE = {
    "animation.orange_wine.preview_complete": 1.35,
    "animation.orange_wine.preview_draw_hold_put_away": 1.35,
    "animation.orange_wine.draw": 1.35,
    "animation.orange_wine.put_away": 1.35,
    "animation.orange_wine.inspect": 1.3,
    "animation.orange_wine.drink": 1.3,
    "animation.orange_wine.drink_stow_empty_bottle": 1.3,
    "animation.orange_wine.drink_empty_hand_recover": 1.3,
    "animation.orange_wine.new_bottle_offscreen_pause": 1.3,
    "animation.orange_wine.drink_draw_new_bottle": 1.3,
}


def camera_animator(animation):
    for animator in animation.get("animators", {}).values():
        if animator.get("type") == "camera" or animator.get("name") == CAMERA_NAME:
            return animator
    return None


def scale_track(animation, rotation_scale, position_scale):
    animator = camera_animator(animation)
    if animator is None:
        return 0
    changed = 0
    for keyframe in animator.get("keyframes", []):
        scale = rotation_scale if keyframe.get("channel") == "rotation" else position_scale
        if scale == 1:
            continue
        for point in keyframe.get("data_points", []):
            for axis in ("x", "y", "z"):
                point[axis] = round(float(point.get(axis, 0)) * scale, 6)
        for tangent in ("bezier_left_value", "bezier_right_value"):
            if tangent in keyframe:
                keyframe[tangent] = [round(float(value) * scale, 6) for value in keyframe[tangent]]
        changed += 1
    return changed


def main():
    model = json.loads(SOURCE.read_text(encoding="utf-8"))
    model["name"] = "农场物语-橙子果酒-TACZ动态视角-v8"

    cameras = [element for element in model.get("elements", []) if element.get("type") == "camera"]
    if len(cameras) != 1 or cameras[0].get("name") != CAMERA_NAME:
        raise RuntimeError("Expected exactly one linked first-person camera")
    cameras[0]["position"] = BASE_POSITION

    previews = model.setdefault("editor_state", {}).setdefault("previews", {})
    if "main" not in previews:
        raise RuntimeError("Missing linked main preview")
    previews["main"]["position"] = BASE_POSITION

    changed = {}
    for animation in model.get("animations", []):
        name = animation.get("name")
        if name in ROTATION_SCALE:
            changed[name] = scale_track(
                animation,
                ROTATION_SCALE[name],
                POSITION_SCALE[name],
            )

    missing = sorted(set(ROTATION_SCALE) - set(changed))
    empty = sorted(name for name, count in changed.items() if count == 0)
    if missing or empty:
        raise RuntimeError(f"Camera tracks missing={missing}, empty={empty}")

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(json.dumps(model, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    print(OUTPUT)
    print(f"camera position={BASE_POSITION}; updated tracks={len(changed)}")


if __name__ == "__main__":
    main()
