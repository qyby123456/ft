import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT.parent))
from freeze_catalog import digest, load, inside
from nbtio import Structure, plain


class V3Tests(unittest.TestCase):
    def setUp(self):
        self.rooms = {record["id"]: record for record in load(ROOT / "seven-original-room-review.json")}

    def test_exact_v2_artifact_and_all_frozen_contract_files(self):
        contract = load(ROOT / "runtime-contract.json")
        expected = "f7d73680b20d390a6f08a090fea66b66000deb33bc0a6556ad282340035c6188"
        self.assertEqual(contract["artifactSha256"], expected)
        self.assertEqual(contract["runtimeJournalVersion"], 2)
        self.assertEqual(len(contract["snapshotFiles"]), 58)
        for record in contract["snapshotFiles"]:
            self.assertEqual(digest(ROOT / record["path"]), record["sha256"])
        self.assertEqual(digest(ROOT / "runtime-next-snapshot/build/libs/ft-biome-dungeon-generator-forge-1.20.1-0.2.0-experimental.jar"), expected)

    def test_original_bytes_and_unaccepted_non_corridor_candidates(self):
        self.assertEqual(set(self.rooms), {"S8-01","S8-02","S8-03","S8-04","S8-07","S8-08","S8-09"})
        candidates = load(ROOT / "candidate-manifest.json")["templates"]
        self.assertEqual(len(candidates), 7)
        self.assertTrue(all(template["accepted"] is False for template in candidates))
        for record in self.rooms.values():
            self.assertEqual(digest(ROOT / record["resource"]["path"]), record["sha256"])
            self.assertEqual(digest(record["resource"]["source"]), record["sha256"])
            self.assertTrue(record["fullExplicitVolume"])
            self.assertEqual(record["entityCount"], 0)
            for evidence in record["sourceEvidence"]:
                self.assertEqual(digest(ROOT / evidence["path"]), evidence["sha256"])

    def test_every_sample_cell_and_payload_matches_original_nbt(self):
        for record in self.rooms.values():
            structure = Structure.read(ROOT / record["resource"]["path"])
            for port in record["ports"]:
                for section in port["sections"]:
                    for column in section["columns"]:
                        for cell in column["cells"]:
                            position = tuple(cell["position"])
                            self.assertEqual(cell["inside"], inside(position, structure.size))
                            if cell["inside"]:
                                self.assertEqual(cell["state"], plain(structure.get(position).as_tag()))
            for payload in record["payloads"]:
                position = tuple(payload["position"])
                self.assertIsNone(payload["staticV2PolicyError"])
                self.assertEqual(payload["state"], plain(structure.get(position).as_tag()))
                if payload["originalNBT"] is not None:
                    self.assertEqual(payload["originalNBT"], plain(structure.block_entities[position]))

    def test_actual_stair_profiles_not_flattened_or_guessed(self):
        expected = {"S8-01": ((17,1,1),(17,2,2)), "S8-07": ((12,2,2),(12,3,3))}
        for identifier, positions in expected.items():
            structure = Structure.read(ROOT / self.rooms[identifier]["resource"]["path"])
            for position in positions:
                block = structure.get(position)
                self.assertEqual(block.name,"minecraft:deepslate_brick_stairs")
                self.assertEqual(dict(block.properties), {"facing":"south","half":"bottom","shape":"straight","waterlogged":"false"})
                self.assertNotEqual(structure.get((position[0],position[1]-1,position[2])).name,"minecraft:air")
        first = self.rooms["S8-01"]["ports"][0]
        self.assertTrue(first["rectangularBoundaryClear"])
        self.assertFalse(first["rectangularFirstInwardClear"])
        entrance = self.rooms["S8-07"]["ports"]
        self.assertEqual([(port["port"]["feet"]["y"],port["port"]["width"]) for port in entrance], [(2,3),(4,5)])
        self.assertTrue(all(port["port"]["medium"] == "dry" for port in entrance))

    def test_real_remaining_blockers_not_old_be_whitelist_blockers(self):
        for identifier in ("S8-02","S8-03","S8-04","S8-07","S8-09"):
            room = self.rooms[identifier]
            self.assertEqual(room["outsideV2Allowlist"], [])
            self.assertEqual(room["blockers"], ["INDEPENDENT_EXPANSION_RUNTIME_ACCEPTANCE_PENDING"])
        self.assertEqual(self.rooms["S8-08"]["fluidsAndFire"], {"minecraft:lava":9})
        self.assertEqual(self.rooms["S8-08"]["outsideV2Allowlist"], ["minecraft:deepslate_brick_wall","minecraft:lava","minecraft:vine"])
        self.assertTrue(all(port["port"]["medium"]=="dry" for port in self.rooms["S8-08"]["ports"]))
        self.assertTrue(self.rooms["S8-02"]["payloads"][0]["usesNativeDefault"])

    def test_baseline_and_v2_are_still_exactly_frozen(self):
        lock = load(ROOT / "baseline-lock.json")
        for path, expected in lock["priorFrozenFiles"].items():
            self.assertEqual(digest(ROOT.parent / path),expected)
        for prefix in (ROOT.parent, ROOT.parent / "expansion-v2"):
            for record in load(prefix / "SHA256SUMS.json")["files"]:
                self.assertEqual(digest(prefix / record["path"]),record["sha256"])


if __name__ == "__main__":
    unittest.main(verbosity=2)
