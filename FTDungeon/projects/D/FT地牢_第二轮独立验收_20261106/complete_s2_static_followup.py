import hashlib
import json
from collections import Counter
from pathlib import Path
import sys

from audit_s8_submission import load


def main():
    root = Path(sys.argv[1]).resolve()
    differences = load(root / "independent-qa/block-entity-differences.json")
    categories = Counter()
    for difference in differences:
        before, after = difference["before"], difference["after"]
        block = difference["block"]
        if block == "minecraft:decorated_pot":
            assert before == {"id": block}
            assert after == {"id": block, "sherds": ["minecraft:brick"] * 4}
            category = "plain_pot_default_brick_sherds"
        elif block == "minecraft:furnace":
            assert before == {"id": block, "Items": []}
            assert after == {"id": block, "Items": [], "CookTime": 0, "BurnTime": 0, "CookTimeTotal": 0, "RecipesUsed": {}}
            category = "empty_furnace_zero_timers"
        elif block == "minecraft:lectern":
            assert before is None and after == {"id": block}
            category = "empty_lectern_generated_id"
        else:
            raise AssertionError(difference)
        categories[category] += 1
        difference["classification"] = category
    original = root / "assembly"
    reproduced = root / "independent-reproduction/assembly"
    outputs = [original / "s2-desert-functional-combined.nbt", *sorted((original / "parts").glob("*.nbt"))]
    comparisons = []
    for source in outputs:
        repeated = reproduced / source.relative_to(original)
        assert source.read_bytes() == repeated.read_bytes()
        comparisons.append({"source": str(source), "reproduced": str(repeated), "sha256": hashlib.sha256(source.read_bytes()).hexdigest(), "byte_exact": True})
    assert len(comparisons) == 16
    routes = load(root / "independent-corridor-qa/independent-routes.json")
    assert all(target["reachable"] and target["standing_clear"] for group in routes for target in group["targets"])
    report = {"stage": "INDEPENDENT_REPRODUCTION_BE_CLASSIFICATION_AND_CORRIDOR_RESULTS", "source_sha256": load(root / "ready-for-review.json")["assembly"]["sha256"], "block_entity_categories": dict(categories), "unclassified_block_entity_changes": 0, "reproduction": comparisons, "corridor_targets_passed": sum(len(group["targets"]) for group in routes), "accepted": False, "limitations": ["Only observed 30 raw BE changes classified; no occupied inventories or book text exist in those changed entries", "Reproduction executed frozen assemble.py with its existing external nbtio dependency; dependency hash recorded", "Native corridor probes test player body clearance at integer steps, not all architectural height or real movement"]}
    dependency = Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools\nbtio.py")
    report["nbtio_dependency"] = {"path": str(dependency), "sha256": hashlib.sha256(dependency.read_bytes()).hexdigest()}
    (root / "independent-qa/block-entity-differences-classified.json").write_text(json.dumps(differences, ensure_ascii=False, indent=2), encoding="utf-8")
    (root / "independent-qa/static-followup.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({key: value for key, value in report.items() if key != "reproduction"}, ensure_ascii=False))


if __name__ == "__main__":
    main()
