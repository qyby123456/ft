import unittest
from pathlib import Path

from inventory import excluded_summary


class InventoryArchiveTests(unittest.TestCase):
    def test_explicit_group_archives_are_excluded(self):
        for directory in ("archive-preliminary-v1", "revision-before-visual-r2", "archive-pre-revision-20261106"):
            with self.subTest(directory=directory):
                self.assertTrue(excluded_summary(Path(directory) / "S2-11" / "summary.json"))

    def test_live_alternatives_remain_visible(self):
        for directory in ("11-library", "S5-11", "candidate-v2", "archive-current"):
            with self.subTest(directory=directory):
                self.assertFalse(excluded_summary(Path(directory) / "summary.json"))

    def test_nested_qa_remains_excluded(self):
        self.assertTrue(excluded_summary(Path("S1-11/qa/copy/summary.json")))


if __name__ == "__main__":
    unittest.main()
