import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent))
from freeze_catalog import digest,load
from prepare_v4 import transform


class V4Tests(unittest.TestCase):
    def test_independent_capability_frozen_and_not_installed_original_claim(self):
        layers = load(ROOT / "evidence-layers.json")
        capability = layers["independentV2Capability"]
        self.assertEqual(capability["verifiedFileCount"],389)
        for record in capability["snapshots"]:
            self.assertEqual(digest(ROOT / record["path"]),record["sha256"])
        final = load(ROOT / "integration-v2-evidence/final-evidence.json")
        self.assertEqual(final["actualInstalledExperimental"]["originalFiveRoomInstalledBE"],"NOT_RUN_NOT_PACKAGED")
        self.assertEqual(final["independentNativeSavedBE"]["status"],"PASS")
        self.assertTrue(layers["noPromotion"])

    def test_exact_original_hash_and_every_target_preserved(self):
        rooms = load(ROOT / "release-gate-candidates.json")["templates"]
        cases = load(ROOT / "rotated-route-inputs.json")["cases"]
        self.assertEqual(len(rooms),9)
        self.assertEqual(len(cases),36)
        for room in rooms:
            self.assertFalse(room["accepted"])
            self.assertEqual(digest(ROOT / room["resource"]["path"]),room["sourceSha256"])
            entries = load(ROOT / f"evidence/{room['id']}/route-input-original.json")
            matches = [entry for entry in entries if entry.get("sha256")==room["sourceSha256"]]
            self.assertEqual(len(matches),1)
            original = matches[0]
            for case in [case for case in cases if case["templateId"]==room["id"]]:
                self.assertEqual(len(case["targets"]),len(original["targets"]))
                self.assertEqual(case["start"],transform(original["start"],case["quarterTurns"],case["origin"]))
                for source,target in zip(original["targets"],case["targets"]):
                    self.assertEqual(source["label"],target["label"])
                    self.assertEqual(source["position"],target["sourcePosition"])
                    self.assertEqual(target["position"],transform(source["position"],case["quarterTurns"],case["origin"]))

    def test_rotation_inverse_fractional_and_no_normalization(self):
        for turns in range(4):
            for position in ((17,1,0),(12,4,26),(1.5,2.5,3.5),(-3,6,4)):
                rotated = transform(position,turns,(0,96,0))
                local = [rotated[0],rotated[1]-96,rotated[2]]
                self.assertEqual(transform(local,(4-turns)%4,(0,0,0)),list(position))
        case = next(case for case in load(ROOT / "rotated-route-inputs.json")["cases"] if case["caseId"]=="s8_07_r1")
        self.assertEqual(case["ports"][1]["worldFeet"],[-26,100,12])
        self.assertEqual(case["ports"][1]["worldFacing"],"west")

    def test_complete_forward_return_matrix_and_stair_targets(self):
        cases = load(ROOT / "rotated-route-inputs.json")["cases"]
        matrix = load(ROOT / "path-check-matrix.json")["checks"]
        self.assertEqual(len(matrix),2200)
        for case in cases:
            checks = [check for check in matrix if check["caseId"]==case["caseId"]]
            count_ports = len(case["ports"])
            self.assertEqual(len(checks),(count_ports+1)*len(case["targets"])+count_ports*(count_ports-1))
            self.assertTrue(all(check["mustReturn"] and check["status"]=="NOT_RUN" for check in checks))
        altar = next(case for case in cases if case["caseId"]=="s8_01_r0")
        self.assertTrue(any(target["label"]=="dais-landing" and target["sourcePosition"][1]==6 for target in altar["targets"]))
        entrance = next(case for case in cases if case["caseId"]=="s8_07_r0")
        self.assertEqual({port["worldFeet"][1] for port in entrance["ports"]},{98,100})
        self.assertTrue(entrance["stairBlockProbes"])

    def test_authorization_gates_and_prior_freezes(self):
        for room in load(ROOT / "release-gate-candidates.json")["templates"]:
            self.assertEqual(room["parentAuthorization"],"NOT_GRANTED_BY_THIS_DOCUMENT")
            self.assertIn("INSTALLED_CANDIDATE_ARTIFACT_ORIGINAL_ROOM_GENERATION_AND_SAVED_READBACK",room["requiredGates"])
            self.assertIn("ALL_ORIGINAL_TARGETS_4_ROTATIONS_NATIVE_ROUTES_AND_RETURN",room["requiredGates"])
        for path,expected in load(ROOT / "prior-baseline-lock.json").items():
            self.assertEqual(digest(ROOT.parent/path),expected)


if __name__ == "__main__":
    unittest.main(verbosity=2)
