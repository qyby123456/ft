import sys
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent))
from freeze_catalog import load,digest
from nbtio import Structure,plain


class ReviewTests(unittest.TestCase):
    def test_independent_collision_reproduction(self):
        review=load(ROOT/'geometry-review.json')
        self.assertEqual(review['failedCount'],112)
        self.assertEqual(set(review['perCase'].values()),{28})
        self.assertTrue(review['intervalReproduction']['same'])
        self.assertEqual(review['intervalReproduction']['openDoorFreePhysicalGap'],1.625)
        self.assertEqual(digest(review['source']),review['sourceSha256'])

    def test_proposed_exact_edits_preserve_stairs_and_payloads(self):
        review=load(ROOT/'geometry-review.json')
        plan=load(ROOT/'repair-proposal.json')
        source=Structure.read(review['source'])
        self.assertEqual(plan['changeCount'],20)
        self.assertFalse(plan['accepted'])
        self.assertEqual(plan['derivedSha256'],digest(ROOT/'derived-candidate/data/biomedungeon/structures/s8_07_widearch_side_doublegate_v5.nbt'))
        self.assertEqual(len({tuple(edit['position']) for edit in plan['edits']}),20)
        for edit in plan['edits']:
            position=tuple(edit['position'])
            self.assertEqual(edit['before'],plain(source.get(position).as_tag()))
            self.assertNotIn(position,source.block_entities)
            self.assertFalse(source.get(position).name.endswith('_stairs'))
            self.assertGreaterEqual(position[1],4)
        after={tuple(edit['position']):edit['after'] for edit in plan['edits']}
        for position_x in (11,12,13):
            for position_y in (4,5,6):
                for position_z in (4,5):
                    state=after.get((position_x,position_y,position_z),plain(source.get((position_x,position_y,position_z)).as_tag()))
                    self.assertEqual(state['Name'],'minecraft:air')
        for old_x,new_x in ((11,8),(12,9)):
            for position_y in (4,5):
                self.assertEqual(after[(new_x,position_y,4)],plain(source.get((old_x,position_y,4)).as_tag()))

    def test_complete_target_preservation(self):
        audit=load(ROOT/'target-audit.json')
        self.assertEqual((audit['cases'],audit['directedChecksWithReturn'],audit['originalTargetsPerRotation']),(36,2200,148))
        self.assertEqual(audit['targetsRemoved'],0)
        self.assertEqual(audit['integrationActual']['originalTargetsRemoved'],0)
        self.assertEqual(audit['originalTargetCounts']['s8_07'],16)


if __name__=='__main__':
    unittest.main(verbosity=2)
