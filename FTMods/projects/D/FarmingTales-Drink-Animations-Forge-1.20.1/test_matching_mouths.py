"""Check physical openings, original texture alpha, and preserved v20 drink faces."""
from pathlib import Path
import json,zipfile,unittest
from PIL import Image
from prepare_matching_mouths import ROOT,ASSETS,V20,PREFIX,DRINKS,top_views,PRIOR
from container_assets import bounds

class MatchingMouthTests(unittest.TestCase):
    def test_three_drink_bodies_preserve_all_original_v20_faces(self):
        with zipfile.ZipFile(V20) as z:
            for name in DRINKS:
                path='models/animation/drinks/'+name+'_body.json'
                source=json.loads(z.read(PREFIX+path));model=json.loads((ASSETS/path).read_text())
                self.assertEqual(model,top_views(source))
                self.assertEqual(len(model['elements']),len(source['elements']))
                for original,new in zip(source['elements'],model['elements']):
                    self.assertEqual(original['from'],new['from']);self.assertEqual(original['to'],new['to'])
                    for face,value in original['faces'].items():self.assertEqual(value,new['faces'][face])

    def test_mouths_keep_cap_bounds_original_colors_and_an_open_center(self):
        specs=json.loads((ROOT/'fixed-mouth-sources.json').read_text())
        self.assertEqual(len(specs),18)
        with zipfile.ZipFile(PRIOR) as z:
            for row in specs:
                path='models/'+row['neck']+'.json';old=json.loads(z.read(PREFIX+path))['elements'][0]
                capLo,capHi=row['closure_bounds'];lo,hi=row['shell_bounds'];m=json.loads((ASSETS/path).read_text())
                for axis in [0,2]:
                    self.assertAlmostEqual(lo[axis]-capLo[axis],.02)
                    self.assertAlmostEqual(capHi[axis]-hi[axis],.02)
                self.assertAlmostEqual(capHi[1]-hi[1],.02)
                self.assertEqual(m['render_type'],'minecraft:cutout')
                for e in m['elements']:
                    a,b=bounds(e)
                    for i in range(3):self.assertTrue(lo[i]-1e-7<=a[i]<=b[i]<=hi[i]+1e-7)
                    self.assertEqual(e.get('rotation'),old.get('rotation'))
                    if 'up' in e['faces']:
                        self.assertFalse(a[0]<(lo[0]+hi[0])/2<b[0] and a[2]<(lo[2]+hi[2])/2<b[2])
                    for face in e['faces'].values():
                        texture=m['textures'][face['texture'][1:]]
                        self.assertEqual(texture,m['textures'][row['faces'][0]['texture'][1:]]);self.assertNotIn('_open',texture)
                        im=Image.open(ASSETS/('textures/'+texture.split(':')[1]+'.png')).convert('RGBA')
                        u0,v0,u1,v1=face['uv']
                        for fx in [.001,.25,.5,.75,.999]:
                            for fy in [.001,.5,.999]:
                                x=min(im.width-1,int((u0+(u1-u0)*fx)/16*im.width))
                                y=min(im.height-1,int((v0+(v1-v0)*fy)/16*im.height))
                                self.assertEqual(im.getpixel((x,y))[3],255,row['item'])
                rim=sum((bounds(e)[1][0]-bounds(e)[0][0])*(bounds(e)[1][2]-bounds(e)[0][2]) for e in m['elements'] if 'up' in e['faces'])
                self.assertAlmostEqual(rim,(hi[0]-lo[0])*(hi[2]-lo[2])-(hi[0]-lo[0]-.28)*(hi[2]-lo[2]-.28),places=6)

if __name__=='__main__':unittest.main()
