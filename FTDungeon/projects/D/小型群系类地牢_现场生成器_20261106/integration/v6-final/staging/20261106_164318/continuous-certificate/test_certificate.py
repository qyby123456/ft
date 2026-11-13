import copy
import unittest

from certificate import certify


def fixture():
    strip={'stripId':'SYNTHETIC_ONLY','resourceSha256':'0'*64,'bodyWidth':0.6,'headroom':3,'nativeStepHeight':0.6,'minFeetY':1,'maxFeetY':1,'startFeetY':1,'endFeetY':1,'centerX':[0.3,1.7],'centerZ':[0.3,1.7],'requiredBlocks':[[position_x,position_y,position_z] for position_x in range(2) for position_y in range(5) for position_z in range(2)]}
    blocks=[{'localBlock':position,'nativeState':'SYNTHETIC_FIXTURE_NOT_WORLD','fluidEmpty':True,'boxes':[[position[0],0,position[2],position[0]+1,1,position[2]+1]] if position[1]==0 else []} for position in strip['requiredBlocks']]
    contract={'inputSha256':'synthetic-test','strips':[strip]}
    native={'schemaVersion':1,'collectionMode':'WORLD_NATIVE_VOXELSHAPE','contractSha256':'synthetic-test','coverageComplete':True,'scenes':[{'stripId':strip['stripId'],'resourceSha256':'0'*64,'nativeContext':'PLAYER_STANDING_COLLISION_CONTEXT','blocks':blocks}]}
    return contract,native


class CertificateTests(unittest.TestCase):
    def test_flat_full_coverage_including_closed_boundaries(self):
        contract,native=fixture()
        result=certify(contract,native)
        self.assertEqual(result['status'],'CONDITIONAL_FINITE_COVERAGE_PASS')
        self.assertTrue(any(low==high for low,high in result['results'][0]['criticalXIntervals']))
        self.assertFalse(result['independentWorldTestsExecutedByThisScript'])

    def test_subsample_thin_obstacle_is_detected(self):
        contract,native=fixture()
        block=next(block for block in native['scenes'][0]['blocks'] if block['localBlock']==[1,2,1])
        block['boxes']=[[1.001,2,1.001,1.002,3,1.002]]
        result=certify(contract,native)
        self.assertEqual(result['status'],'FAIL')
        self.assertTrue(any(failure['type']=='HEADROOM_COLLISION' for failure in result['results'][0]['failures']))

    def test_missing_native_or_wrong_hash_rejected(self):
        contract,native=fixture()
        for field,value in [('coverageComplete',False),('collectionMode','BLOCK_NAME_INFERENCE'),('contractSha256','wrong')]:
            changed=copy.deepcopy(native)
            changed[field]=value
            with self.assertRaises(ValueError):
                certify(contract,changed)
        native['scenes'][0]['blocks'].pop()
        with self.assertRaises(ValueError):
            certify(contract,native)

    def test_step_too_high_rejected(self):
        contract,native=fixture()
        contract['strips'][0].update({'maxFeetY':2,'endFeetY':2})
        for block in native['scenes'][0]['blocks']:
            if block['localBlock'][1]==1 and block['localBlock'][2]==1:
                position=block['localBlock']
                block['boxes']=[[position[0],1,1,position[0]+1,2,2]]
        result=certify(contract,native)
        self.assertEqual(result['status'],'FAIL')
        self.assertTrue(any(failure['type']=='STEP_EXCEEDS_LIMIT' for failure in result['results'][0]['failures']))

    def test_headroom_three_not_just_player_height(self):
        contract,native=fixture()
        block=next(block for block in native['scenes'][0]['blocks'] if block['localBlock']==[0,3,0])
        block['boxes']=[[0,3,0,1,4,1]]
        self.assertEqual(certify(contract,native)['status'],'FAIL')

    def test_support_hole_rejected(self):
        contract,native=fixture()
        for block in native['scenes'][0]['blocks']:
            block['boxes']=[]
        self.assertEqual(certify(contract,native)['status'],'FAIL')


if __name__=='__main__':
    unittest.main(verbosity=2)
