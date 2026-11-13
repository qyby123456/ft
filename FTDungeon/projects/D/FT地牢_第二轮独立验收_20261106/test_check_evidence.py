import hashlib
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import check_evidence


class EvidenceTests(unittest.TestCase):
    def run_fixture(self, mutation=None):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            room = root / "room"
            qa = room / "qa"
            qa.mkdir(parents=True)
            candidate = room / "room.nbt"
            candidate.write_bytes(b"fixture-only-not-a-real-structure")
            digest = hashlib.sha256(candidate.read_bytes()).hexdigest()
            target = {"label": "exit", "position": [1, 2, 3]}
            reports = {
                qa / "independent-native-frozen.json": [{"sha256": digest, "registry_properties": "PASS", "template_roundtrip_block_count": "PASS", "survival_failures": [], "survival_unsupported": []}],
                qa / "independent-routes.json": [{"sha256": digest, "start_clear": True, "targets": [{**target, "standing_clear": True, "reachable": True}]}],
                qa / "route-input.json": [{"sha256": digest, "targets": [target]}],
                qa / "render.json": {"source_sha256": digest},
            }
            if mutation:
                mutation(reports, qa)
            for path, data in reports.items():
                path.write_text(json.dumps(data), encoding="utf-8")
            inventory = {"checked_at": "fixture", "groups": [{"candidates": [{"id": "S1-11", "candidate_evidence": [{"source": str(candidate)}]}]}]}
            (root / "inventory.json").write_text(json.dumps(inventory), encoding="utf-8")
            with patch.object(check_evidence, "ROOT", root):
                check_evidence.main()
            return json.loads((root / "evidence-consistency.json").read_text(encoding="utf-8"))

    def test_nested_current_reports_are_evidence_not_acceptance(self):
        result = self.run_fixture()
        self.assertEqual(result["rooms_with_findings"], 0)
        self.assertEqual(result["accepted"], 0)
        self.assertFalse(result["rooms"][0]["accepted"])

    def test_stale_native_hash_rejected(self):
        def change(reports, qa):
            reports[qa / "independent-native-frozen.json"][0]["sha256"] = "stale"
        result = self.run_fixture(change)
        self.assertIn("NO_CURRENT_HASH_BOUND_NATIVE_REPORT", [entry["code"] for entry in result["rooms"][0]["findings"]])

    def test_unreachable_target_rejected(self):
        def change(reports, qa):
            reports[qa / "independent-routes.json"][0]["targets"][0]["reachable"] = False
        result = self.run_fixture(change)
        self.assertIn("ROUTE_FAILURE_OR_EMPTY", [entry["code"] for entry in result["rooms"][0]["findings"]])

    def test_removed_target_detected(self):
        def change(reports, qa):
            reports[qa / "route-input.json"][0]["targets"].append({"label": "hidden", "position": [4, 2, 3]})
        result = self.run_fixture(change)
        self.assertIn("ROUTE_TARGET_LIST_DIFFERS", [entry["code"] for entry in result["rooms"][0]["findings"]])

    def test_unsupported_survival_not_silently_accepted(self):
        def change(reports, qa):
            reports[qa / "independent-native-frozen.json"][0]["survival_unsupported"] = ["unknown"]
        result = self.run_fixture(change)
        self.assertIn("NATIVE_FAILURE_OR_UNSUPPORTED", [entry["code"] for entry in result["rooms"][0]["findings"]])


if __name__ == "__main__":
    unittest.main(verbosity=2)
