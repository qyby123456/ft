import copy
import json
import unittest
from collections import deque

from freeze_catalog import ROOT, AIR, SAFE_POOL, SUPPORT, digest, inside, load, read_port
from nbtio import Structure


class CatalogTests(unittest.TestCase):
    def setUp(self):
        self.manifest = load(ROOT / "manifest.json")

    def test_schema_and_packaging(self):
        self.assertEqual(self.manifest["schemaVersion"], 1)
        self.assertEqual((ROOT / "manifest.json").read_bytes(), (ROOT / "data/biomedungeon/manifest.json").read_bytes())
        self.assertEqual({template["independentAcceptance"]["sourceRoomId"] for template in self.manifest["templates"]}, set(SAFE_POOL))
        for template in self.manifest["templates"]:
            self.assertTrue(template["accepted"])
            self.assertEqual(template["biome"], "s8")
            self.assertEqual(template["resourceId"], "biomedungeon:" + template["id"])
            self.assertEqual(template["role"], "normal")

    def test_copied_bytes_and_independent_hashes(self):
        for template in self.manifest["templates"]:
            provenance = template["independentAcceptance"]
            output = ROOT / "data/biomedungeon/structures" / (template["id"] + ".nbt")
            self.assertEqual(digest(output), template["sha256"])
            self.assertEqual(digest(provenance["source"]), template["sha256"])
            for record in [provenance["independentFileAudit"], provenance["sourceMetadata"], provenance["layout"], *provenance["acceptanceRecords"]]:
                self.assertEqual(digest(ROOT / record["snapshot"]), record["sha256"])
            self.assertIn(template["sha256"], (ROOT / provenance["independentFileAudit"]["snapshot"]).read_text(encoding="utf-8"))
            self.assertTrue(any(template["sha256"] in (ROOT / record["snapshot"]).read_text(encoding="utf-8") for record in provenance["acceptanceRecords"]))

    def test_real_nbt_full_volume_and_ports(self):
        for template in self.manifest["templates"]:
            structure = Structure.read(ROOT / "data/biomedungeon/structures" / (template["id"] + ".nbt"))
            self.assertEqual(list(template["size"].values()), list(structure.size))
            self.assertEqual(len(structure.blocks), structure.size[0] * structure.size[1] * structure.size[2])
            self.assertFalse(structure.entities)
            self.assertFalse(structure.block_entities)
            metadata = load(ROOT / template["independentAcceptance"]["sourceMetadata"]["snapshot"])
            self.assertEqual([read_port(raw, structure, number)[0] for number, raw in enumerate(metadata["connections"])], template["ports"])

    def test_all_ports_share_real_three_high_walk_component(self):
        for template in self.manifest["templates"]:
            structure = Structure.read(ROOT / "data/biomedungeon/structures" / (template["id"] + ".nbt"))
            feet = [tuple(port["feet"][axis] for axis in ("x", "y", "z")) for port in template["ports"]]
            start = feet[0]
            reached = {start}
            pending = deque([start])
            while pending:
                current = pending.popleft()
                for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    target = (current[0] + delta_x, current[1], current[2] + delta_z)
                    floor = (target[0], target[1] - 1, target[2])
                    if target not in reached and inside(target, structure.size) and structure.get(floor).name in SUPPORT and all(structure.get((target[0], target[1] + rise, target[2])).name in AIR for rise in range(3)):
                        reached.add(target)
                        pending.append(target)
            self.assertTrue(set(feet) <= reached, template["id"])

    def test_four_rotations_preserve_unique_cells_and_apertures(self):
        def rotate(position, turns):
            position_x, position_y, position_z = position
            return ((position_x, position_y, position_z), (-position_z, position_y, position_x), (-position_x, position_y, -position_z), (position_z, position_y, -position_x))[turns]
        for template in self.manifest["templates"]:
            structure = Structure.read(ROOT / "data/biomedungeon/structures" / (template["id"] + ".nbt"))
            for turns in range(4):
                cells = {rotate(position, turns): block for position, block in structure.blocks.items()}
                self.assertEqual(len(cells), len(structure.blocks))
                self.assertTrue(all(rotate(rotate(position, turns), (4 - turns) % 4) == position for position in structure.blocks))
                for port in template["ports"]:
                    raw = {"facing": port["facing"], "position": list(port["feet"].values()), "width": port["width"], "height": port["height"]}
                    _, checked = read_port(raw, structure, 0)
                    self.assertTrue(all(cells[rotate(position, turns)].name in AIR for position in checked))

    def test_fail_closed_on_invented_or_obstructed_interface(self):
        template = self.manifest["templates"][0]
        structure = Structure.read(ROOT / "data/biomedungeon/structures" / (template["id"] + ".nbt"))
        raw = {"facing": "north", "position": [7, 2, 0], "width": 4, "height": 3}
        with self.assertRaises(ValueError):
            read_port(raw, structure, 0)
        raw["width"] = 5
        raw["position"] = [7, 1, 0]
        with self.assertRaises(ValueError):
            read_port(raw, structure, 0)
        self.assertNotEqual(__import__("hashlib").sha256(b"tampered").hexdigest(), template["sha256"])

    def test_exclusions_cover_all_base_round_and_future_policy(self):
        rejected = load(ROOT / "rejections.json")
        self.assertEqual(len(rejected["entries"]), 78)
        self.assertEqual(sum(entry.get("reason") == "OVERVIEW_NEVER_A_ROOM" for entry in rejected["entries"]), 8)
        self.assertTrue(any("11-20" in reason for reason in rejected["globalExclusions"]))


if __name__ == "__main__":
    unittest.main(verbosity=2)
