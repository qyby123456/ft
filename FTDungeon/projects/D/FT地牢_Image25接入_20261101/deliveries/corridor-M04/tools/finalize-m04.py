import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8"))


def fingerprint(relative):
    return hashlib.sha256((ROOT / relative).read_bytes()).hexdigest()


def write_json(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    metadata = load("room.json")
    static = load("qa/static-qa.json")
    native = load("qa/forge-placement.json")
    rendered = load("qa/render-qa.json")
    structure_hash = fingerprint("room.nbt")
    assert metadata["sha256"] == static["structure_sha256"] == native["structure_sha256"] == structure_hash
    assert all(item["source_sha256"] == structure_hash for item in rendered["images"])
    assert static["status"] == native["status"] == rendered["status"] == "PASS"
    assert all(static["checks"].values())
    assert native["rotations"] == 4 and native["actual_world_placements"] == 8
    assert native["elapsed_world_ticks"] >= 200
    assert "M04_FORGE_PASS sha256=" + structure_hash in (ROOT / "qa/forge-console.log").read_text(encoding="utf-8", errors="replace")
    assert fingerprint("qa/rebuild/room.nbt") == structure_hash
    font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 22)
    for category in ("interior", "exterior"):
        sheet = Image.new("RGB", (1600, 1048), "#151b2b")
        draw = ImageDraw.Draw(sheet)
        draw.text((22, 10), "M04 末地淹水吊笼长廊 · " + ("内部四向剖切" if category == "interior" else "完整外壳四向"), fill="#e4d9f1", font=font)
        for index, direction in enumerate(("north", "south", "west", "east")):
            with Image.open(ROOT / f"views/{category}-{direction}.png") as source:
                source.verify()
            with Image.open(ROOT / f"views/{category}-{direction}.png") as source:
                sheet.paste(source.convert("RGB").resize((800, 500), Image.Resampling.LANCZOS), ((index % 2) * 800, 48 + (index // 2) * 500))
        sheet.save(ROOT / f"views/{category}-four-views.png")
    for source in [ROOT / "preview.png", *sorted((ROOT / "views").glob("*.png"))]:
        with Image.open(source) as image:
            image.verify()
    report = {
        "id": "corridor-M04", "source_id": "M04", "status": "PASS",
        "minecraft": "1.20.1", "forge": native["forge"],
        "structure_sha256": structure_hash, "reference_sha256": metadata["reference_sha256"],
        "size": metadata["size"], "length_blocks": 41, "length_chunks": 2.5625,
        "checks": {
            "static_qa": static["status"], "forge_four_rotations": native["status"],
            "forge_repeated_placements": native["actual_world_placements"],
            "elapsed_native_server_ticks": native["elapsed_world_ticks"],
            "source_water_blocks_per_rotation": 222,
            "dry_route_cells_per_rotation": 123,
            "structural_state_mismatches": sum(item["structural_state_mismatches"] for item in native["immediate"] + native["delayed"]),
            "can_survive_failures": sum(item["can_survive_failures"] for item in native["immediate"] + native["delayed"]),
            "leaked_fluid_blocks": sum(item["leaked_fluid_blocks"] for item in native["immediate"] + native["delayed"]),
            "allowed_natural_vine_changes": [item["allowed_natural_wall_vine_changes"] for item in native["delayed"]],
            "deterministic_rebuild": True, "final_nbt_rendered_views": len(rendered["images"]),
            "decoded_png_files": 13,
        },
        "evidence": {"static": "qa/static-qa.json", "forge": "qa/forge-placement.json", "console": "qa/forge-console.log", "render": "qa/render-qa.json", "static_human_readable": "STATIC_QA.md", "interior_four_views": "views/interior-four-views.png", "exterior_four_views": "views/exterior-four-views.png", "rebuild": "qa/rebuild/room.nbt", "initial_failed_fixture_run": "qa/forge-console-initial-aquifer-failure.log", "initial_failure_diagnosis": "qa/fixture-diagnosis.json"},
        "deployment": {"deployed": False, "player_saves_opened": False, "mods_modified": False, "existing_dungeon_modified": False},
        "limits": ["Forge evidence is from an isolated vanilla-plus-test Forge GameTest server, not a full-modpack graphical client.", "Preview images are final-NBT Lodestone software renders, not Minecraft screenshots.", "No FT lobby connection, automatic placement, persistent generation marker or deployment is provided.", "The default random tick speed remains 3; any harmless sidewall-vine growth is separately counted, never hidden as an exact match."]
    }
    write_json("qa.json", report)
    assets = ["room.nbt", "room.json", "summary.json", "preview.png", "qa.json", "README.md", "STATIC_QA.md", "review.html", "build.gradle", "settings.gradle", "gradle.properties", "qa/static-qa.json", "qa/forge-placement.json", "qa/forge-console.log", "qa/render-qa.json", "qa/rebuild/room.nbt", "qa/fixture-diagnosis.json", "qa/forge-console-initial-aquifer-failure.log"]
    for directory in ("views", "tools", "qa-resources"):
        assets.extend(str(file.relative_to(ROOT)).replace("\\", "/") for file in sorted((ROOT / directory).rglob("*")) if file.is_file() and "__pycache__" not in file.parts)
    manifest = {"id": "corridor-M04", "status": "PASS", "structure_sha256": structure_hash, "reference_sha256": metadata["reference_sha256"], "files": [{"path": relative, "bytes": (ROOT / relative).stat().st_size, "sha256": fingerprint(relative)} for relative in assets]}
    write_json("delivery-manifest.json", manifest)
    print(json.dumps({"status": "PASS", "structure_sha256": structure_hash, "files": len(assets), "elapsed_ticks": native["elapsed_world_ticks"]}))


if __name__ == "__main__":
    main()
