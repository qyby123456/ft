import sys
import unittest
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT.parent))
from freeze_catalog import digest, load
from nbtio import Structure, plain


class ExpansionTests(unittest.TestCase):
    def test_frozen_baseline_unchanged(self):
        lock = load(ROOT / "snapshot-lock.json")
        for path, expected in lock["baselineFrozenFiles"].items():
            self.assertEqual(digest(ROOT.parent / path), expected)
        for record in load(ROOT.parent / "SHA256SUMS.json")["files"]:
            self.assertEqual(digest(ROOT.parent / record["path"]), record["sha256"])

    def test_current_final_core_snapshot_matches_ready(self):
        ready = load(ROOT / "core-snapshot/ready.json")
        for source in ready["sources"]:
            self.assertEqual(digest(ROOT / "core-snapshot" / Path(source["path"]).name), source["sha256"])
        log = (ROOT / "core-smoke-java17.log").read_text(encoding="utf-8")
        self.assertIn('openjdk version "17.', log)
        self.assertIn("layoutVariants=32", log)
        self.assertIn("PASS real catalog + core snapshot", log)

    def test_all_72_actual_sources_payloads_and_evidence(self):
        records = load(ROOT / "all-72-expansion-audit.json")
        self.assertEqual(len(records), 72)
        self.assertEqual(len({record["id"] for record in records}), 72)
        self.assertTrue(all(1 <= record["image"] <= 9 for record in records))
        for record in records:
            self.assertEqual(digest(record["source"]), record["sha256"])
            structure = Structure.read(record["source"])
            self.assertEqual(len(structure.block_entities), len(record["blockEntities"]))
            self.assertEqual(sum(block.get("waterlogged") == "true" for block in structure.blocks.values()), record["waterloggedCells"])
            for payload in record["blockEntities"]:
                self.assertEqual(plain(structure.block_entities[tuple(payload["position"])]), payload["nbt"])
            self.assertIn(record["sha256"], Path(record["independentFileAudit"]).read_text(encoding="utf-8"))

    def test_full_biome_roles_and_missing_implicit_payloads(self):
        records = {record["id"]:record for record in load(ROOT / "s8-complete-biome-requirements.json")["templates"]}
        self.assertEqual(len(records), 9)
        self.assertEqual(len(records["S8-07"]["blockEntities"]), 6)
        self.assertEqual(len(records["S8-04"]["blockEntities"]), 8)
        self.assertEqual(len(records["S8-09"]["blockEntities"]), 8)
        self.assertEqual(len(records["S8-09"]["blockEntityBlocks"]), 8)
        self.assertEqual(len(records["S8-02"]["blockEntities"]), 0)
        self.assertEqual(len(records["S8-02"]["blockEntityBlocks"]), 1)
        self.assertEqual(records["S8-08"]["fluidsAndFire"], [{"name":"minecraft:lava", "count":9}])
        self.assertTrue(all(not record["acceptedForExpansionRuntime"] for record in records.values()))

    def test_no_unsupported_new_admissions(self):
        summary = load(ROOT / "compatibility-summary.json")
        self.assertEqual(summary["currentRuntimeStaticScreenCompatibleIds"], ["S8-05", "S8-06"])
        self.assertEqual(summary["additionalUnchangedTemplatesAdmitted"], [])
        candidate = load(ROOT / "candidate-manifest.json")
        self.assertEqual(len(candidate["templates"]), 7)
        for template in candidate["templates"]:
            self.assertFalse(template["accepted"])
            self.assertEqual(digest(ROOT / "candidate-resources/data/biomedungeon/structures" / (template["id"] + ".nbt")), template["sha256"])


if __name__ == "__main__":
    unittest.main(verbosity=2)
