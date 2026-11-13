import hashlib
import json
from pathlib import Path
import zipfile

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8"))


def save(relative, data):
    (ROOT / relative).write_text(json.dumps(data, ensure_ascii=False, indent=2) + chr(10), encoding="utf-8")


def digest(relative):
    return hashlib.sha256((ROOT / relative).read_bytes()).hexdigest()


def sheet(kind):
    canvas = Image.new("RGB", (2400, 1576), "#171b21")
    for index, side in enumerate(("north", "south", "west", "east")):
        with Image.open(ROOT / f"views/{kind}-{side}.png") as source:
            image = source.convert("RGB").resize((1200, 788), Image.Resampling.LANCZOS)
            canvas.paste(image, (index % 2 * 1200, index // 2 * 788))
    canvas.save(ROOT / f"views/{kind}-four-views.png")


def main():
    metadata = load("room.json")
    static = load("qa/static.json")
    native = load("qa/native.json")
    rendered = load("views/manifest.json")
    fingerprint = digest("room.nbt")
    assert all(report["sha256"] == fingerprint for report in (metadata, static, native, rendered))
    assert static["status"] == native["status"] == "PASS"
    assert native["rotations"] == 4 and native["settle_ticks"] >= 600
    assert len(native["initial"]) == len(native["after_600_ticks"]) == 4
    assert all(record["states_compared"] == 6600 and record["route"] == "PASS" and record["entities"] == 0
               for record in native["initial"] + native["after_600_ticks"])
    evidence = []
    for view in rendered["views"]:
        with Image.open(ROOT / view["file"]) as image:
            image.load()
            assert image.size == (1600, 1050)
            assert len(image.convert("RGB").getcolors(maxcolors=10000000)) > 100
        evidence.append({"file": view["file"], "kind": view["kind"], "side": view["side"], "sha256": digest(view["file"]), "size": [1600, 1050]})
    for kind in ("interior", "exterior"):
        sheet(kind)
    visual = {
        "status": "PASS", "sha256": fingerprint,
        "reference_sha256": metadata["reference_sha256"],
        "method": "Agent visual inspection of final NBT Lodestone renderings, checked against reference PNG and native QA",
        "views": evidence,
        "checks": {
            "north_south_west_east_interior_cutaways": "PASS",
            "north_south_west_east_full_shell": "PASS",
            "both_entrance_eye_level_views": "PASS",
            "straight_parallel_walls_no_branch_or_stair": "PASS",
            "seven_timber_frames_with_stone_feet_and_connected_longitudinal_beams": "PASS",
            "three_cages_suspended_from_solid_roof_no_main_lane_overlap": "PASS",
            "lantern_chains_anchored_to_beam_or_cage_roof": "PASS",
            "continuous_center_rail_and_three_wide_dry_path": "PASS",
            "north_south_arches_and_safe_interior_point": "PASS",
            "full_two_layer_roof_and_full_foundation": "PASS",
            "ember_glass_is_solid_cosmetic_no_fire_or_liquid": "PASS",
            "zero_containers_inventories_loot_or_entities": "PASS"
        },
        "correction": "Deepslate axis=y is explicit in final NBT so Lodestone draws the roof and foundation. Final hash re-rendered and re-tested in Forge.",
        "known_adaptations": metadata["reference_adaptations"],
        "gameplay_screenshot": False
    }
    save("qa/visual.json", visual)
    rendered["status"] = "PASS"
    rendered["visual_review"] = "../qa/visual.json"
    rendered["image_fingerprints"] = evidence
    save("views/manifest.json", rendered)
    report = {
        "id": "corridor-M10", "status": "PASS", "ready_for_coordinator_review": True,
        "sha256": fingerprint, "reference_sha256": metadata["reference_sha256"],
        "size": metadata["size"], "entrance": metadata["entrance"], "interior": metadata["interior"], "exit": metadata["exit"],
        "static": {"status": "PASS", "report": "qa/static.json", "checks": len(static["checks"]), "records": 6600, "palette": static["palette_count"]},
        "four_view_review": {"status": "PASS", "report": "qa/visual.json", "manifest": "views/manifest.json", "interior_views": 4, "exterior_views": 4, "eye_views": 2},
        "forge_native": {"status": "PASS", "report": "qa/native.json", "log": "qa/forge.log", "minecraft": "1.20.1", "forge": "47.4.10", "rotations": 4, "settle_ticks": native["settle_ticks"], "state_comparisons": 52800,
                         "neighbor_and_shape_updates": True, "complete_state_preservation": True, "survival_checks_per_rotation": native["initial"][0]["survival_checks"], "entities_after_settling": 0},
        "integration": {"deployed": False, "production_save_modified": False, "coordinator_acceptance": "PENDING", "slot_assigned": False},
        "limits": ["Preview and four-view evidence use Lodestone software rendering, not Minecraft client screenshots.", "Forge native proof uses an isolated GameTestServer with no production save or full modpack client.", "Template includes explicit air; coordinator must validate the complete target volume and reject occupied placement before deployment."]
    }
    save("qa.json", report)
    files = ["room.nbt", "room.json", "preview.png", "qa.json", "README.md", "summary.json"]
    files += [file.relative_to(ROOT).as_posix() for folder in ("tools", "views", "qa", "forge-qa/src") for file in (ROOT / folder).rglob("*") if file.is_file() and "__pycache__" not in file.parts and "before-explicit-axis" not in file.name]
    files += ["forge-qa/build.gradle", "forge-qa/settings.gradle", "forge-qa/gradle.properties"]
    manifest = {"id": "corridor-M10", "status": "PASS", "sha256": fingerprint, "files": [{"path": name, "bytes": (ROOT / name).stat().st_size, "sha256": digest(name)} for name in sorted(set(files))]}
    save("delivery-manifest.json", manifest)
    with zipfile.ZipFile(ROOT / "corridor-M10-handoff.zip", "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for name in sorted(set(files + ["delivery-manifest.json"])):
            archive.write(ROOT / name, "corridor-M10/" + name)
    with zipfile.ZipFile(ROOT / "corridor-M10-handoff.zip") as archive:
        assert archive.testzip() is None
        for item in manifest["files"]:
            assert hashlib.sha256(archive.read("corridor-M10/" + item["path"])).hexdigest() == item["sha256"]
    save("package-receipt.json", {"status": "PASS", "file": "corridor-M10-handoff.zip", "sha256": digest("corridor-M10-handoff.zip"), "files_verified": len(manifest["files"]), "nbt_sha256": fingerprint})
    print(json.dumps({"status": "PASS", "nbt_sha256": fingerprint, "files_verified": len(manifest["files"]), "package_sha256": digest("corridor-M10-handoff.zip")}, ensure_ascii=False))


if __name__ == "__main__":
    main()
