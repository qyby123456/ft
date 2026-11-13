from __future__ import annotations

import argparse
import hashlib
import json
from datetime import datetime, timezone, timedelta
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8"))


def dump(relative, data):
    (ROOT / relative).write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--native-log", type=Path, default=ROOT / "evidence/forge-gametest-rerun-1.log")
    arguments = parser.parse_args()
    metadata = read("room.json")
    fingerprint = digest(ROOT / "room.nbt")
    static = read("qa/static-qa.json")
    registry = read("qa/native-registry-qa.json")
    native = read("qa/native-world-qa.json")
    render = read("evidence/render-session.json")
    assert metadata["sha256"] == fingerprint
    assert digest(metadata["reference"]) == metadata["referenceSha256"]
    assert all(report["status"] == "PASS" and report["sha256"] == fingerprint for report in (static, registry, native))
    assert len(native["rotations"]) == 4 and native["stable_ticks"] == 240
    assert all(result["status"] == "PASS" and result["after_240_ticks"]["status"] == "PASS" and result["fixture_rollback"] == "PASS" for result in native["rotations"])
    assert not render["errors"] and not render.get("failedResources")
    assert len(render["views"]) == 6 and all(view["exactStates"] for view in render["views"])
    native_log = arguments.native_log.read_text(encoding="utf-8", errors="replace")
    assert "S14_NATIVE_QA_PASS" in native_log and "BUILD SUCCESSFUL" in native_log
    assert Image.open(metadata["reference"]).size == (1536, 1024)
    sheet = Image.new("RGB", (2304, 1632), "#181520")
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 28)
    small = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 18)
    draw.text((28, 18), "S14  /  末地蔓生短廊 · 北 / 南 / 西 / 东四向内部剖视", font=font, fill="#f1e5cc")
    draw.text((28, 58), "最终 room.nbt · Lodestone 0.6.0 · 保留方块状态完全一致 · 剖切不修改 NBT · 非实机截图", font=small, fill="#c7b7d5")
    for index, name in enumerate(("north", "south", "west", "east")):
        image = Image.open(ROOT / f"evidence/{name}-cutaway.png").convert("RGB")
        assert image.size == (1536, 1024)
        sheet.paste(image.resize((1152, 768), Image.Resampling.LANCZOS), (index % 2 * 1152, 96 + index // 2 * 768))
    sheet.save(ROOT / "evidence/four-view-contact-sheet.png")
    timestamp = datetime.now(timezone(timedelta(hours=8))).isoformat()
    previews = ["preview.png", "evidence/north-cutaway.png", "evidence/south-cutaway.png", "evidence/west-cutaway.png", "evidence/east-cutaway.png", "evidence/full-envelope.png", "evidence/four-view-contact-sheet.png"]
    provenance = {
        "status": "PASS", "id": metadata["id"], "sha256": fingerprint,
        "source_nbt": str(ROOT / "room.nbt"), "renderer": render["renderer"],
        "reference_is_not_preview": True, "cutaway_changes_nbt": False,
        "retained_block_states_exact": True, "images": [{"path": str(ROOT / relative), "sha256": digest(ROOT / relative), "size": list(Image.open(ROOT / relative).size)} for relative in previews],
        "render_session": str(ROOT / "evidence/render-session.json"),
        "limits": "Vanilla bundled resources and software WebGL lighting. Not Minecraft client or shader screenshots. Far south opening intentionally replaces the closed-door concept."
    }
    dump("qa/preview-provenance.json", provenance)
    review = {
        "status": "PASS", "id": metadata["id"], "sha256": fingerprint, "review_type": "maker self-review, not separate integrator acceptance", "reviewed_at": timestamp,
        "views": [str(ROOT / relative) for relative in previews],
        "checks": [
            {"item": "reference palette and rhythm", "status": "PASS", "evidence": "Cream end-stone, purpur piers at z=0/5/10/15, obsidian bases/recesses, moss/vines and warm torches with end-rod pairs retained."},
            {"item": "floor and roof", "status": "PASS", "evidence": "208 solid floor cells and 208 roof cells; final full envelope preserved. Cutaways only remove display blocks."},
            {"item": "pier and beam continuity", "status": "PASS", "evidence": "15 static checks include a single face-connected non-air component. Chipped soffits retain complete upper beams; piers rest on full obsidian footings."},
            {"item": "plants and fixtures supported", "status": "PASS", "evidence": "Static exact attachment directions plus native canSurvive on every non-air cell, before/after 240 ticks in four rotations."},
            {"item": "straight route and interfaces", "status": "PASS", "evidence": "North [6,1,0], safe [6,1,2], south [6,1,15]; x=5..7 has 3-high clearance. Native swept 0.6x1.8 collision checks at 48 foot cells per rotation."},
            {"item": "inventory, hazards and fluids", "status": "PASS", "evidence": "Zero containers, block entities, template entities, real liquids, lava, fires, command/structure/jigsaw blocks or spawners."},
            {"item": "reference deviations documented", "status": "PASS", "evidence": "Concept closed end becomes open south arch for through-corridor specification; white vanilla end-rod emission replaces cyan shader bloom."}
        ],
        "remaining_integration": "Unique production ID/slot, hall connections, occupancy/idempotence and full modpack gameplay remain integrator responsibility. No deployment performed."
    }
    dump("qa/four-view-review.json", review)
    qa = {
        "id": metadata["id"], "source_id": "S14", "status": "PASS_INDEPENDENT_DELIVERY", "timestamp": timestamp,
        "sha256": fingerprint, "reference": metadata["reference"], "reference_sha256": metadata["referenceSha256"],
        "size": metadata["size"], "records": static["totalRecords"], "non_air_blocks": static["nonAirBlocks"],
        "checks": {"static": static["status"], "static_count": len(static["checks"]), "native_registry": registry["status"], "native_four_rotation_placement": native["status"], "native_stability_ticks": 240, "four_view_self_review": review["status"], "preview_provenance": provenance["status"]},
        "natural_vine_changes_by_rotation": {result["rotation"]: result["after_240_ticks"]["natural_vine_changes"] for result in native["rotations"]},
        "evidence": {"static": str(ROOT / "qa/static-qa.json"), "native_registry": str(ROOT / "qa/native-registry-qa.json"), "native_world": str(ROOT / "qa/native-world-qa.json"), "forge_log": str(arguments.native_log), "four_view_review": str(ROOT / "qa/four-view-review.json"), "four_view_sheet": str(ROOT / "evidence/four-view-contact-sheet.png"), "full_envelope": str(ROOT / "evidence/full-envelope.png"), "preview": str(ROOT / "preview.png")},
        "history": {"initial_native_compile": "FAIL: removed unsupported GameRules constant; no NBT changes", "final_native_run": "BUILD SUCCESSFUL; all 1 required tests passed", "preview_iteration": "Cutaway renderer corrected to preserve explicit original block properties; all 6 final views verify exact retained states."},
        "boundaries": {"deployed": False, "player_saves_opened": False, "production_core_modified": False, "other_deliveries_modified": False, "paid_image_generation_used": False, "production_id_reserved": False, "full_modpack_gameplay_tested": False},
        "limitations": ["240 ticks is bounded structural validation, not long-term random vine growth or server performance certification.", "Only independent Forge 47.4.10 GameTest fixtures were used. No FT catalog, world-slot assignment, lobby connection, production idempotence or multiplayer/client gameplay was tested.", "One synchronous four-fixture placement produced a server catch-up warning (28.715 s); performance is not certified.", "Software preview is not a Minecraft screenshot; white end rods are not custom cyan lights."]
    }
    dump("qa.json", qa)
    paths = [ROOT / relative for relative in ["room.nbt", "room.json", "qa.json", "summary.json", *previews, "qa/static-qa.json", "qa/native-registry-qa.json", "qa/native-world-qa.json", "qa/preview-provenance.json", "qa/four-view-review.json", "evidence/render-session.json"]]
    paths.extend(sorted((ROOT / "tools").glob("*.mjs")))
    paths.extend(sorted((ROOT / "tools").glob("*.cjs")))
    paths.extend(sorted((ROOT / "tools").glob("*.py")))
    paths.append(arguments.native_log)
    paths.extend(path for path in (ROOT / "native-test/src").rglob("*") if path.is_file())
    paths.extend(ROOT / "native-test" / name for name in ("build.gradle", "settings.gradle", "gradle.properties"))
    paths.extend(path for path in (ROOT / "native-test/gradle/wrapper").iterdir() if path.is_file())
    for extra in ("README.md", "Run-QA.ps1", "viewer.html"):
        if (ROOT / extra).is_file(): paths.append(ROOT / extra)
    dump("delivery-manifest.json", {"id": metadata["id"], "status": qa["status"], "timestamp": timestamp, "reference_sha256": metadata["referenceSha256"], "nbt_sha256": fingerprint, "excluded": ["native-test/build", "native-test/.gradle", "native-test/run", "node_modules", "delivery-manifest.json"], "files": [{"path": str(path.relative_to(ROOT)), "bytes": path.stat().st_size, "sha256": digest(path)} for path in paths]})
    print(json.dumps({"status": qa["status"], "sha256": fingerprint, "files": len(paths), "static_checks": len(static["checks"]), "native_rotations": 4, "stable_ticks": 240}, ensure_ascii=False))


if __name__ == "__main__":
    main()
