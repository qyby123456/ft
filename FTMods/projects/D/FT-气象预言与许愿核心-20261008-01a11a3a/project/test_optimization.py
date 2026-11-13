import json
import unittest
from pathlib import Path

from optimize_geometry import FACE_AXES, face_coverage, optimize_elements, subtract_rectangle


ROOT = Path(__file__).resolve().parent


def cube(name, minimum, maximum, rotation=None):
    element = {"name": name, "from": minimum, "to": maximum, "faces": {direction: {"texture": "#atlas", "uv": [0, 0, 4, 4]} for direction in FACE_AXES}}
    if rotation:
        element["rotation"] = rotation
    return element


class OptimizationTests(unittest.TestCase):
    def test_rectangle_subtraction_preserves_area(self):
        remainder = subtract_rectangle((0, 0, 4, 4), (1, 1, 3, 3))
        self.assertEqual(sum((piece[2] - piece[0]) * (piece[3] - piece[1]) for piece in remainder), 12)

    def test_external_coplanar_face_is_retained(self):
        first = cube("first", [0, 0, 0], [4, 4, 4])
        second = cube("second", [2, 0, 0], [6, 4, 4])
        self.assertFalse(face_coverage(first, "up", [first, second])[0])

    def test_adjacent_interior_faces_are_removed(self):
        first = cube("first", [0, 0, 0], [4, 4, 4])
        second = cube("second", [4, 0, 0], [8, 4, 4])
        optimized, report = optimize_elements([first, second])
        self.assertEqual(report["removed_quads"], 2)
        self.assertNotIn("east", optimized[0]["faces"])
        self.assertNotIn("west", optimized[1]["faces"])
        self.assertEqual(len(first["faces"]), 6)

    def test_union_coverage_accepts_complete_surface(self):
        first = cube("first", [0, 0, 0], [4, 4, 4])
        second = cube("second", [0, 4, 0], [2, 6, 4])
        third = cube("third", [2, 4, 0], [4, 6, 4])
        self.assertTrue(face_coverage(first, "up", [first, second, third])[0])

    def test_tiny_gap_does_not_count_as_coverage(self):
        first = cube("first", [0, 0, 0], [4, 4, 4])
        second = cube("second", [0, 4, 0], [1.999, 6, 4])
        third = cube("third", [2, 4, 0], [4, 6, 4])
        self.assertFalse(face_coverage(first, "up", [first, second, third])[0])

    def test_enclosed_element_keeps_one_valid_face(self):
        outer = cube("outer", [0, 0, 0], [8, 8, 8])
        inner = cube("inner", [2, 2, 2], [6, 6, 6])
        optimized, report = optimize_elements([outer, inner])
        self.assertEqual(len(optimized[1]["faces"]), 1)
        self.assertEqual(report["removed_quads"], 5)

    def test_rotated_geometry_is_not_approximated(self):
        first = cube("first", [0, 0, 0], [4, 4, 4])
        second = cube("second", [0, 4, 0], [4, 6, 4], {"origin": [2, 4, 2], "axis": "y", "angle": 45})
        self.assertFalse(face_coverage(first, "up", [first, second])[0])
        self.assertFalse(face_coverage(second, "down", [first, second])[0])

    def test_model_parameters_and_visible_uv_are_identical(self):
        for namespace, name in (("qys_event", "weather_forecaster"), ("qys_event", "oracle"), ("farmingtales", "wishing_well_core")):
            relative = Path("assets") / namespace / "models/block" / f"{name}.json"
            before = json.loads((ROOT / "revisions/superseded-preoptimization/payload" / relative).read_text(encoding="utf-8"))
            after = json.loads((ROOT / "payload" / relative).read_text(encoding="utf-8"))
            self.assertEqual({key: value for key, value in before.items() if key != "elements"}, {key: value for key, value in after.items() if key != "elements"})
            self.assertEqual(len(before["elements"]), len(after["elements"]))
            for original, optimized in zip(before["elements"], after["elements"]):
                self.assertEqual({key: value for key, value in original.items() if key != "faces"}, {key: value for key, value in optimized.items() if key != "faces"})
                for direction, face in optimized["faces"].items():
                    self.assertEqual(original["faces"][direction], face)
                for direction in set(original["faces"]) - set(optimized["faces"]):
                    self.assertTrue(face_coverage(original, direction, before["elements"])[0])
            for path in (ROOT / "payload/assets" / namespace / "textures").rglob(f"{name}_*.png"):
                self.assertEqual(path.read_bytes(), (ROOT / "revisions/superseded-preoptimization/payload" / path.relative_to(ROOT / "payload")).read_bytes())


if __name__ == "__main__":
    unittest.main(verbosity=2)
