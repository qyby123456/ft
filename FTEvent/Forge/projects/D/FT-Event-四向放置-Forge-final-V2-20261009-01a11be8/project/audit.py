import itertools
import json
import sys
import zipfile
from pathlib import Path

import release


ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT / "preview"))
from verify_assets import rotated, verify


def read_json(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8"))


def variants(content, model):
    actual = json.loads(content)["variants"]
    expected = {f"facing={direction}": {"model": model, **({"y": angle} if angle else {})}
                for direction, angle in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}
    if actual != expected:
        raise RuntimeError("Blockstate/model orientation mismatch for " + model)


def collision_geometry():
    asset = ROOT / "preview/payload/assets/farmingtales/models/block/wishing_well_core.json"
    model = json.loads(asset.read_text(encoding="utf-8"))
    points = [rotated(point, element.get("rotation")) for element in model["elements"]
              for point in itertools.product(*zip(element["from"], element["to"]))]
    checked = 0
    for turns in range(4):
        for point in points:
            horizontal = [point[0], point[2]]
            for _ in range(turns):
                horizontal = [16 - horizontal[1], horizontal[0]]
            minimum, maximum = (0, 16) if point[1] <= 4 + 1e-8 else (3, 13)
            if not (0 <= point[1] <= 8 + 1e-8 and all(minimum - 1e-8 <= value <= maximum + 1e-8 for value in horizontal)):
                raise RuntimeError("Rotated core vertex escaped existing collision boxes")
            checked += 1
    return {"passed": True, "rotated_vertices_checked": checked,
            "boxes": [[0, 0, 0, 16, 4, 16], [3, 4, 3, 13, 8, 13]],
            "all_four_horizontal_rotations_preserve_shape": True}


def main():
    baseline = read_json("baseline.json")["forge"]
    release.check_baseline(baseline)
    models = [verify("oracle"), verify("weather_forecaster"), verify("wishing_well_core", "farmingtales")]
    with zipfile.ZipFile(ROOT / "candidate.jar") as archive:
        for name in ("oracle", "weather_forecaster"):
            variants(archive.read(f"assets/qys_event/blockstates/{name}.json"), f"qys_event:block/{name}")
            for kind in ("block", "item"):
                entry = f"assets/qys_event/models/{kind}/{name}.json"
                if archive.read(entry) != (ROOT / "preview/payload" / entry).read_bytes():
                    raise RuntimeError("Active resourcepack overrides a different model: " + entry)
        current_config = json.loads(archive.read(release.MIXINS))
        with zipfile.ZipFile(ROOT / "baseline" / release.JAR) as old_archive:
            old_config = json.loads(old_archive.read(release.MIXINS))
        expected_config = dict(old_config)
        expected_config["mixins"] = old_config.get("mixins", []) + ["CoreFacingMixin"]
        if current_config != expected_config:
            raise RuntimeError("Unrelated Mixin configuration changed")
    for prefix in ("kubejs/assets", release.PACK):
        variants(release.target(prefix + "/farmingtales/blockstates/wishing_well_core.json").read_bytes(),
                 "farmingtales:block/wishing_well_core")
    script = release.target(release.SCRIPT).read_text(encoding="utf-8")
    if "event.create('farmingtales:wishing_well_core', 'cardinal')" not in script:
        raise RuntimeError("Installed core is not cardinal")
    for box in (".box(0, 0, 0, 16, 4, 16)", ".box(3, 4, 3, 13, 8, 13)"):
        if box not in script:
            raise RuntimeError("Core collision changed")
    data = {"scope": "Forge Event blocks only; no manor scaling or character/spell effect scope",
            "passed": True, "time": release.timestamp(), "models": models, "collision": collision_geometry(),
            "blockstates": "north=0, east=90, south=180, west=270 for all three blocks",
            "neo": release.neo_audit(), "jar_delta": read_json("candidate-delta.json"),
            "forge_runtime": read_json("forge-runtime-verification.json"),
            "core_runtime": read_json("core-runtime-verification.json"),
            "safety_tests": read_json("release-safety.json"),
            "preview": read_json("preview/preview-verification.json"),
            "unified_preview": read_json("preview/unified-preview-verification.json"),
            "event_regression": (ROOT / "QysEventVerifier.txt").read_text(encoding="utf-8").strip(),
            "payload_sha256": {name: release.sha(content) for name, content in release.payload_bytes().items()},
            "game_tested": False, "actual_mixin_application_in_game_tested": False,
            "pending_in_game": ["Full Forge restart and resource bake", "Place each block in four horizontal directions",
                                "Structure rotation and mirror, with unchanged core collision", "Old saved block states",
                                "Emerald oracle, forecast and wishing-well interaction", "Multiblock recognition, multiplayer and lighting"],
            "skill_effect_coverage": {"status": "N/A with evidence", "evidence": "Only three existing static blocks, their facing/rotation/mirror hooks and collision/state audit; no Boss skills, manor multipliers or shared qys-bosses assets/classes are modified"}}
    if not all(data[key]["passed"] for key in ("forge_runtime", "core_runtime", "safety_tests")):
        raise RuntimeError("Runtime or safety checks failed")
    if data["preview"]["errors"] or data["preview"].get("placementOrientationCases") != 12:
        raise RuntimeError("Four-way interactive preview failed")
    if data["unified_preview"]["errors"] or data["unified_preview"].get("placementOrientationCases") != 12:
        raise RuntimeError("Unified-entry four-way preview failed")
    if "19 daily events, 32 special weathers" not in data["event_regression"]:
        raise RuntimeError("Event rules regression failed")
    release.save(ROOT / "verification.json", data)
    print("Complete Forge candidate audit passed; NeoForge rollback hashes unchanged; gameplay tests remain pending")


if __name__ == "__main__":
    main()
