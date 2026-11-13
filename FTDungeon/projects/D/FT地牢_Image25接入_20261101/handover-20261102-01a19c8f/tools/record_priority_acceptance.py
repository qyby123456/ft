from datetime import datetime, timedelta, timezone
from pathlib import Path

from intake import HOME, digest, file_record, load, png_record, save


def main():
    latest = load(HOME / "latest.json")
    registry = load(latest["registry"]["path"])
    findings = {
        "M07": {
            "actually_reviewed_nbt_sha256": "c12bd3be22bae6be4cb7da9a547de541fe2dac0f27839cc7352872a27ec87576",
            "design_translation": "Central stone well with closed iron grate, two prison alcoves, low-wall moss, wooden crossbeams, supported chains/hooks, four hanging lights, and covered east drain match the concept's functional layout. Non-grid rings, bucket rope and round well are vanilla approximations, not gameplay devices.",
            "checks": {
                "floating_or_clipped_furniture": "No unsupported or intersecting furniture observed in four interior views; verified wall hooks, skull floor support and chain attachment in native checks.",
                "beam_column_joinery": "Three timber crossbeams bear on masonry pilasters; native and static full-height supports pass. Cutaway-only near-wall removal does not indicate a missing real support.",
                "routes_entry_safe_point": "Three-wide north passage at [7,4,0] reaches [7,4,3] and the dry ring; 141 native reachable foot positions in every rotation. Requires core entrance ramp to local feet Y=4.",
                "water_seal": "All 33 water-source cells remain sealed below closed grates and a covered drain; zero leaks/items after forced neighbor/fluid updates and 240 ticks per rotation.",
                "inventory_entities": "Two skull block entities; no items, loot tables, preplaced mobs or reward inventory.",
                "roof_shell": "All four complete exterior views retain roof and envelope. Native verification checks 510 roof blocks in each rotation; cutaway only removes in-memory roof for viewing.",
            },
        },
        "R4": {
            "actually_reviewed_nbt_sha256": "784ae6c6d271d0561bb00f339351f669f0fb758df8c2b80f6d27fcc7f00596f0",
            "design_translation": "Connected spring pools, T-shaped dry three-wide bridge, masonry columns/crossbeams, rear stepped cascade, supported lantern posts, anchored dripstone and a small empty-barrel bank preserve the cave/spring concept. Amethyst stays vanilla purple; no claimed blue shader light.",
            "checks": {
                "floating_or_clipped_furniture": "No floating/intersecting scenery observed in four internal views; lantern arms, empty barrel bank and dripstone are attached to real supports.",
                "beam_column_joinery": "Four grounded stone columns and supported crossbeams; six bridge piers are verified by the static/native fixture, not inferred from a water shader.",
                "routes_entry_safe_point": "North [8,3,0] and safe point [8,3,3] retain dry three-wide approach and bridge; 116 dry reachable positions in all four rotations. Core approach must respect feet Y=3.",
                "water_seal": "193 connected water cells remain contained; final states compared after 200/240 ticks across four rotations, with external moat, perforated-pool and blocked-route negative checks passing.",
                "inventory_entities": "Three empty barrel block entities; no reward inventory, loot or preplaced entities.",
                "roof_shell": "Four exterior views show a continuous final roof/shell. Native fixture verifies foundation, roof, column supports, routes and envelope; no cutaway is written back into NBT.",
            },
        },
    }
    for room_id, finding in findings.items():
        record = next(value for value in registry["rooms"] if value["id"] == room_id)
        assert record["candidate_sha256"] == finding["actually_reviewed_nbt_sha256"], "A different candidate needs an actual new visual review; do not replay this decision"
        folder = HOME / "reviews" / room_id / record["candidate_sha256"]
        native_pointer = load(folder / "native/latest.json")["result"]
        assert digest(native_pointer["path"]) == native_pointer["sha256"]
        result = load(native_pointer["path"])
        assert result["status"] == "PASS" and result["sha256"] == record["candidate_sha256"]
        for key in ("static", "native", "log", "invocation"):
            information = result[key]
            assert digest(information["path"]) == information["sha256"]
        manifest_path = folder / "four-views/manifest.json"
        manifest = load(manifest_path)
        assert manifest["sha256"] == record["candidate_sha256"]
        assert len(manifest["images"]) == 10
        for information in manifest["images"]:
            assert digest(information["path"]) == information["sha256"]
        viewed = [record["reference"], png_record(Path(record["source_folder"]) / "preview.png")]
        plan = Path(record["source_root"]) / ("qa/floor-plan.png" if room_id == "M07" else "preview/plan.png")
        viewed.append(png_record(plan))
        viewed += [information for information in manifest["images"] if information["path"].endswith("four-views.png")]
        decision = {
            "id": room_id, "sha256": record["candidate_sha256"],
            "time": datetime.now(timezone(timedelta(hours=8))).isoformat(),
            "reviewer_thread": "01a19c8f-6747-7572-a77a-6e90084bbd34",
            "four_view_status": "PASS", "retest_status": "PASS", "hard_blockers": [],
            "retest_evidence": file_record(native_pointer["path"]),
            "render_manifest": file_record(manifest_path),
            "actually_opened_images": viewed,
            "visual_review_method": "Opened and inspected original concept, delivered preview, dry-route plan, freshly rendered north/south/west/east interior cutaway sheet and all-four exterior sheet; paired findings with the exact-NBT static and freshly rerun isolated Forge tests.",
            **finding,
            "candidate_acceptance": "READY_FOR_UNIFIED_INTEGRATION_STAGE_ONLY_NOT_DEPLOYED",
            "core_acceptance_conditions": [
                "Keep the reserved ID/slot and exact candidate SHA. M07 is never M7.",
                "Plan and test a real supported hall route and entrance ramp; validate whole-volume/margins, entities and occupancy refusal.",
                "Use an independent persistent generation fingerprint; existing rooms and player alterations must not be overwritten.",
                "Before shared core writes or deployment, report exact candidate JAR hash, changed files/dependencies and release conditions to the human coordinator.",
            ],
            "limits": "Pass applies to this standalone room candidate and 240-tick isolated Forge placement, not shared-core connection, production migration, modpack client, multiplayer, FPS, or deployment. No NBT repair was needed; source SHA is unchanged.",
        }
        save(folder / "acceptance.json", decision)
        print("ACCEPTED_CANDIDATE", room_id, record["candidate_sha256"], flush=True)


if __name__ == "__main__":
    main()
