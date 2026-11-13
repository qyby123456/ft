import sys
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parent
PACKAGE=ROOT/'nine-room-package-v5'
sys.path.insert(0,str(ROOT.parent))
from freeze_catalog import digest,load
from nbtio import Structure,plain


class NineTests(unittest.TestCase):
    def test_nine_resources_exact_eight_originals_one_derived(self):
        mappings=load(PACKAGE/'source-map.json')
        self.assertEqual(len(mappings),9)
        self.assertEqual(sum(record['unchanged'] for record in mappings),8)
        self.assertEqual((PACKAGE/'candidate-manifest.json').read_bytes(),(PACKAGE/'data/biomedungeon/manifest.json').read_bytes())
        manifest=load(PACKAGE/'candidate-manifest.json')
        self.assertTrue(all(template['accepted'] is False for template in manifest['templates']))
        for record in mappings:
            self.assertEqual(digest(record['originalSourcePath']),record['originalSha256'])
            self.assertEqual(digest(PACKAGE/'data/biomedungeon/structures'/f"{record['candidateId']}.nbt"),record['candidateSha256'])
            self.assertEqual(record['originalSha256']==record['candidateSha256'],record['unchanged'])
        derived=next(record for record in mappings if not record['unchanged'])
        self.assertEqual(derived['candidateSha256'],'bd7d0661b818164a3617629096580c646d17d95832269e6aca989933dc815ae3')

    def test_targets_mapping_and_original_matrix_exactly_retained(self):
        original=load(ROOT.parent/'acceptance-candidate-v4/rotated-route-inputs.json')['cases']
        cases=load(PACKAGE/'rotated-route-inputs.json')['cases']
        mapping=load(PACKAGE/'all-original-target-mapping.json')
        self.assertEqual(len(cases),36)
        self.assertEqual(sum(len(case['targets']) for case in cases),592)
        self.assertEqual(mapping['originalTargetsRemoved'],0)
        for before,after in zip(original,cases):
            self.assertEqual(before['start'],after['start'])
            self.assertEqual(before['targets'],after['targets'])
            self.assertEqual(before['ports'],after['ports'])
        matrix=load(PACKAGE/'path-check-matrix.json')
        self.assertEqual(len(matrix['checks']),2248)
        original_checks=load(ROOT.parent/'acceptance-candidate-v4/path-check-matrix.json')['checks']
        for before,after in zip(original_checks,matrix['checks'][:2200]):
            for field in ('from','to','start','end','mustReturn'):
                self.assertEqual(before[field],after[field])
        self.assertEqual(sum(check.get('additionalSideGateCheck',False) for check in matrix['checks']),48)
        self.assertEqual(sum(len(case.get('additionalTargets',[])) for case in cases),16)

    def test_original_stairs_be_and_heights_against_actual_source(self):
        record=next(record for record in load(PACKAGE/'source-map.json') if not record['unchanged'])
        before=Structure.read(record['originalSourcePath'])
        after=Structure.read(PACKAGE/'data/biomedungeon/structures'/f"{record['candidateId']}.nbt")
        self.assertEqual(sum(block.name.endswith('_stairs') for block in before.blocks.values()),12)
        self.assertEqual(len(before.block_entities),6)
        for position,block in before.blocks.items():
            if position[1]<=3 or block.name.endswith('_stairs'):
                self.assertEqual(block,after.get(position))
        self.assertEqual({position:plain(tag) for position,tag in before.block_entities.items()},{position:plain(tag) for position,tag in after.block_entities.items()})
        self.assertEqual(sum(before.blocks[position]!=after.get(position) for position in before.blocks),20)

    def test_current_readme_history_and_consumer_contract(self):
        readme=(ROOT/'README.md').read_text('utf-8')
        self.assertIn('侧双门不能封闭中央主通道',readme)
        self.assertNotIn('仅方案，未写派生NBT',readme)
        self.assertNotIn('派生哈希当前为null',readme)
        self.assertTrue((ROOT/'history/README-before-seal.md').is_file())
        contract=load(PACKAGE/'native-test-contract.json')
        self.assertFalse(contract['accepted'])
        self.assertEqual(contract['additionalSideGateTests']['doorLowerCells'],[[8,4,4],[9,4,4]])


if __name__=='__main__':
    unittest.main(verbosity=2)
