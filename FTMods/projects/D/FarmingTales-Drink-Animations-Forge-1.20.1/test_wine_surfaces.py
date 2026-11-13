"""Regressions for the two visible XO windows and culled wine shoulders."""
from pathlib import Path
import json,zipfile,unittest
from PIL import Image
from container_assets import bounds,FACE_AXIS,FACE_UV
from prepare_wine_surfaces import V20,ITEMS,PREFIX

ROOT=Path(__file__).absolute().parent

class WineSurfaceTests(unittest.TestCase):
    def test_only_the_missing_viewing_sides_are_added_to_v20(self):
        with zipfile.ZipFile(V20) as z:
            for item in ITEMS:
                p=PREFIX+'models/animation/drinks/'+item+'_body.json'
                old=json.loads(z.read(p));new=json.loads((ROOT/'src/main/resources'/p).read_text())
                self.assertEqual(old['textures'],new['textures'])
                self.assertEqual(len(new['elements']),len(old['elements'])+(2 if item=='brandy_xo' else 0))
                tops=0
                for a,b in zip(old['elements'],new['elements']):
                    self.assertEqual({k:v for k,v in a.items() if k!='faces'},{k:v for k,v in b.items() if k!='faces'})
                    for face,spec in a['faces'].items():self.assertEqual(spec,b['faces'][face])
                    additions=set(b['faces'])-set(a['faces'])
                    if additions:
                        self.assertEqual(additions,{'up'});self.assertEqual(a['from'][1],a['to'][1])
                        u0,v0,u1,v1=a['faces']['down']['uv'];self.assertEqual(b['faces']['up']['uv'],[u0,v1,u1,v0]);tops+=1
                self.assertGreater(tops,0,item)

    def test_every_transparent_window_pixel_has_an_opaque_inner_backing(self):
        p=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
        model=json.loads((p/'models/animation/drinks/brandy_xo_body.json').read_text())
        texture=Image.open(p/'textures/drinks/brandy_xo_8.png').convert('RGBA')
        count=0
        for source_index,normal in [(17,'east'),(18,'south')]:
            glass=model['elements'][source_index];spec=glass['faces'][normal]
            backing=next(e for e in model['elements'] if e.get('name')=='label_inner_back_'+normal)
            axis=FACE_AXIS[normal][0];ua,ur,va,vr=FACE_UV[normal];lo,hi=bounds(backing)
            self.assertGreater(lo[axis],glass['from'][axis]);self.assertLess(lo[axis]-glass['from'][axis],.011)
            u0,v0,u1,v1=[v*4 for v in spec['uv']]
            for x in range(int(min(u0,u1)),int(max(u0,u1))):
                for y in range(int(min(v0,v1)),int(max(v0,v1))):
                    if texture.getpixel((x,y))[3]:continue
                    count+=1
                    for u,v in [(x,y),(x+1,y+1)]:
                        for j,t,reverse in [(ua,(u-u0)/(u1-u0),ur),(va,(v-v0)/(v1-v0),vr)]:
                            q=glass['from'][j]+(glass['to'][j]-glass['from'][j])*(1-t if reverse else t)
                            self.assertGreaterEqual(q+1e-6,lo[j]);self.assertLessEqual(q-1e-6,hi[j])
            a,b,c,d=[v*4 for v in backing['faces'][normal]['uv']]
            self.assertEqual(texture.crop((min(a,c),min(b,d),max(a,c),max(b,d))).getchannel('A').getextrema(),(255,255))
        self.assertEqual(count,33)

    def test_tops_keep_the_open_neck_center(self):
        for item in ITEMS:
            model=json.loads((ROOT/'src/main/resources'/PREFIX/f'models/animation/drinks/{item}_body.json').read_text())
            top=max(max(e['from'][1],e['to'][1]) for e in model['elements'] if e.get('name','').startswith('cube_outline'))
            for e in model['elements']:
                if 'up' not in e['faces'] or abs(e['from'][1]-top)>1e-6:continue
                lo,hi=bounds(e);self.assertFalse(lo[0]<8<hi[0] and lo[2]<8<hi[2],item)

if __name__=='__main__':unittest.main()
