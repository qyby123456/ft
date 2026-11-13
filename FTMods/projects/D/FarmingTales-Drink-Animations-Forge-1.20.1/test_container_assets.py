"""Regression coverage for disappearing shoulders on inverted source cuboids."""
import json
import unittest
from pathlib import Path
from container_assets import exterior_box, bounds, GLASS, FACE_AXIS

ROOT=Path(__file__).resolve().parent
ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim/models/animation/drinks'

class ExteriorTests(unittest.TestCase):
    def test_inverted_top_becomes_up_without_mirroring_the_physical_texture(self):
        source={'from':[3,4,5],'to':[0,0,0],'faces':{
            'down':{'texture':'#0','uv':[1,2,7,9]},
            'north':{'texture':'#0','uv':[2,3,8,10]}}}
        result=exterior_box(source)
        self.assertEqual(result['from'],[0,0,0])
        self.assertEqual(result['to'],[3,4,5])
        self.assertEqual(result['faces']['up']['uv'],[7,2,1,9])
        self.assertEqual(result['faces']['south']['uv'],[2,10,8,3])
        self.assertEqual(source['from'],[3,4,5])

    def test_both_drinks_have_the_full_upward_facing_shoulder_and_open_mouth(self):
        for name,height,outer,neck,mouth in [
            ('sweet_orange_juice',11.7,6,3,12.7),
            ('vitamin_drink',11.4332,5.01,3.34,12.502)]:
            model=json.loads((ASSETS/(name+'_body.json')).read_text())
            area=0
            for e in model['elements']:
                if 'up' not in e['faces']:continue
                lo,hi=bounds(e)
                if abs(lo[1]-height)<1e-5:
                    area+=(hi[0]-lo[0])*(hi[2]-lo[2])
                if abs(lo[1]-mouth)<1e-5:
                    self.assertFalse(lo[0]<8<hi[0] and lo[2]<8<hi[2],name+' mouth was filled')
            self.assertAlmostEqual(area,outer*outer-neck*neck,places=5,msg=name+' shoulder coverage')

    def test_label_and_glass_do_not_compete_for_the_same_surface(self):
        for item in GLASS:
            model=json.loads((ASSETS/(item+'_body.json')).read_text())
            surfaces=[]
            for e in model['elements']:
                if e.get('rotation',{}).get('angle',0):continue
                lo,hi=bounds(e)
                for face in e['faces']:
                    axis,end=FACE_AXIS[face];other=[i for i in range(3) if i!=axis]
                    if any(hi[i]-lo[i]<1e-7 for i in other):continue
                    plane=(hi if end else lo)[axis]
                    rect=[lo[i] for i in other]+[hi[i] for i in other]
                    for old_face,old_plane,old_rect in surfaces:
                        if old_face!=face or abs(old_plane-plane)>1e-7:continue
                        area=max(0,min(rect[2],old_rect[2])-max(rect[0],old_rect[0]))*max(0,min(rect[3],old_rect[3])-max(rect[1],old_rect[1]))
                        self.assertLess(area,1e-7,item+' duplicate '+face+' surface')
                    surfaces.append((face,plane,rect))

if __name__=='__main__':unittest.main()
