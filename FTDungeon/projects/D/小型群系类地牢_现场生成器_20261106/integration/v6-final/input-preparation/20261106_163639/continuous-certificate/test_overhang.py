import copy
import unittest

from certificate import certify
from test_certificate import fixture


class OverhangTests(unittest.TestCase):
    def wall_fixture(self):
        contract,native=fixture()
        contract['nativeShapeBoundsByBlockName']={'minecraft:stone':[0,0,0,1,1,1],'minecraft:oak_fence':[0,0,0,1,1.5,1]}
        for block in native['scenes'][0]['blocks']:
            block['blockName']='minecraft:stone'
        block=next(block for block in native['scenes'][0]['blocks'] if block['localBlock']==[1,1,1])
        block.update({'blockName':'minecraft:oak_fence','boxes':[[1.375,1,1.375,1.625,2.5,1.625]]})
        return contract,native

    def test_legal_native_overhang_not_rejected_or_clipped(self):
        contract,native=self.wall_fixture()
        original=copy.deepcopy(native)
        result=certify(contract,native)
        self.assertEqual(result['status'],'FAIL')
        self.assertTrue(any(failure['type']=='HEADROOM_COLLISION' for failure in result['results'][0]['failures']))
        self.assertEqual(native,original)

    def test_unknown_or_out_of_policy_shape_rejected(self):
        contract,native=self.wall_fixture()
        block=next(block for block in native['scenes'][0]['blocks'] if block['blockName']=='minecraft:oak_fence')
        block['boxes'][0][4]=2.6
        with self.assertRaises(ValueError):
            certify(contract,native)
        block['boxes'][0][4]=2.5
        block['blockName']='unknown:unbounded'
        with self.assertRaises(ValueError):
            certify(contract,native)

    def test_source_identity_mismatch_rejected(self):
        contract,native=self.wall_fixture()
        contract['strips'][0]['sourceBlockNames']={'1,1,1':'minecraft:stone'}
        with self.assertRaises(ValueError):
            certify(contract,native)


if __name__=='__main__':
    unittest.main(verbosity=2)
