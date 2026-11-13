import json
import sys
from collections import Counter
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    root = Path(sys.argv[1]).resolve()
    evidence = load(root / "independent-qa/world-r2-inspection.json")
    source, blocks = decode(root / "assembly/s4-wetland-connected.nbt")
    phases = []
    for phase in evidence["phases"]:
        classifications = Counter()
        for change in phase["raw_state_changes"]:
            before, after = change["before"], change["after"]
            original = before.get("Properties", {})
            actual = after.get("Properties", {})
            changed_keys = {key for key in original if original[key] != actual.get(key)}
            name = before["Name"]
            if not changed_keys:
                classifications["vanilla_completed_omitted_properties"] += 1
            elif name == "minecraft:mangrove_leaves":
                assert changed_keys == {"distance"}
                assert actual["persistent"] == "true" and actual["waterlogged"] == "false"
                classifications["persistent_leaf_distance_recalculation"] += 1
            elif name == "minecraft:vine":
                assert changed_keys <= {"up", "north", "east", "west", "south"}
                classifications["vine_attachment_recalculation"] += 1
            elif name == "minecraft:iron_door":
                assert phase["phase"] == "after-player-interactions" and changed_keys <= {"hinge", "powered", "open"}
                classifications["door_neighbor_update_after_lever_use"] += 1
            elif name == "minecraft:iron_bars":
                assert phase["phase"] == "after-player-interactions" and changed_keys <= {"north", "east", "west", "south"}
                classifications["bar_neighbor_update_after_lever_use"] += 1
            else:
                raise AssertionError(change)
        payload_kinds = Counter()
        for change in phase["raw_be_changes"]:
            assert change["before"] is None
            after = change["after"]
            kind = after["id"]
            if kind in {"minecraft:skull", "minecraft:lectern"}:
                assert after == {"id": kind}
            else:
                assert after == {"CookTime": 0, "BurnTime": 0, "Items": [], "id": "minecraft:furnace", "CookTimeTotal": 0, "RecipesUsed": {}}
            payload_kinds[kind] += 1
        assert payload_kinds == {"minecraft:skull": 12, "minecraft:lectern": 3, "minecraft:furnace": 6}
        stands = [entity for entity in phase["captured_entities"] if entity["nbt"]["id"] == "minecraft:armor_stand"]
        assert len(stands) == len(source["entities"]) == 2
        for original in source["entities"]:
            position = [original["pos"][0] + 2, original["pos"][1], original["pos"][2] + 2]
            actual = next(entity for entity in stands if entity["pos"] == position)
            armor = []
            for item in actual["nbt"]["ArmorItems"]:
                item = dict(item)
                if "tag" in item:
                    assert item.pop("tag") == {"Damage": 0}
                armor.append(item)
            assert armor == original["nbt"]["ArmorItems"]
            for field, default in (("HandItems", [{}, {}]), ("ShowArms", 0), ("Small", 0), ("Invisible", 0), ("NoBasePlate", 0), ("Rotation", [0.0, 0.0])):
                assert actual["nbt"].get(field, default) == original["nbt"].get(field, default), field
        phases.append({"phase": phase["phase"], "air_to_vine": len(phase["type_changes"]), "raw_state_changes_classified": dict(classifications), "default_be_payloads": dict(payload_kinds), "armor_stand_positions_and_equipment_preserved": 2, "armor_stack_normalization": "Vanilla adds tag.Damage=0; absent HandItems/Small/Invisible/NoBasePlate compared with explicit vanilla defaults", "captured_entity_kinds": dict(Counter(entity["nbt"]["id"] for entity in phase["captured_entities"]))})
    native = load(root / "independent-qa/independent-native-frozen.json")
    support = load(root / "independent-qa/independent-all-support.json")
    routes = load(root / "independent-qa/independent-routes.json")
    after = load(root / "independent-posttick-qa/independent-routes.json")
    assert all(item["registry_properties"] == item["template_roundtrip_block_count"] == "PASS" for item in native)
    assert all(not item["failures"] and not item["unsupported"] for item in support)
    assert all(target["reachable"] and target["standing_clear"] for item in routes + after for target in item["targets"])
    report = {"stage": "INDEPENDENT_S4_R2_STATIC_AND_WORLD_EVIDENCE_CLASSIFICATION", "source_sha256": evidence["source_sha256"], "nonair_support_checked": sum(item["nonair_checked"] for item in support), "static_targets_passed": sum(len(item["targets"]) for item in routes), "post_world_targets_passed": sum(len(item["targets"]) for item in after), "phases": phases, "limitations": evidence["limitations"][:-1], "accepted": False}
    (root / "independent-qa/world-r2-classification.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
