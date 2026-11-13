from pathlib import Path
import zipfile,json,io
from PIL import Image,ImageDraw

ROOT=Path(__file__).absolute().parent
TARGETS={
 'brewery':('whiskey_highland_hearth whiskey_jamesons_malt whiskey_maggoallan whiskey_smokey_reverie whiskey_carrasconlabel whiskey_ak whiskey_jojannik whiskey_lilitusinglemalt whiskey_cristelwalker beer_wheat beer_barley beer_hops beer_nettle beer_haley beer_oat').split(),
 'vinery':('clark_wine creepers_crush jellie_wine jo_special_mixture mellohi_wine strad_wine stal_wine lilitu_wine bottle_mojang_noir villagers_fright red_wine cherry_wine kelp_cider mead glowing_wine magnetic_wine bolvar_wine eiswein noir_wine apple_wine apple_cider aegis_wine chenet_wine cristel_wine solaris_wine chorus_wine').split()
}
JARS={ns:next(p for p in Path('Z:/mods').glob('*.jar') if 'letsdo-'+ns+'-forge' in p.name) for ns in TARGETS}
def model(z,loc):
 ns,rel=loc.split(':');m=json.loads(z.read(f'assets/{ns}/models/{rel}.json'))
 if 'elements' not in m:
  p=model(z,m['parent']); textures=p.get('textures',{})|m.get('textures',{});p.update(m);p['textures']=textures;return p
 return m
def source(z,ns,name):
 state=json.loads(z.read(f'assets/{ns}/blockstates/{name}.json'))
 if 'variants' in state:
  v=next(v for k,v in state['variants'].items() if 'true' not in k and 'bottles=2' not in k and 'bottles=3' not in k and 'bottles=4' not in k)
 else:v=state['multipart'][0]['apply']
 if isinstance(v,list):v=v[0]
 return v['model']
if __name__=='__main__':
 rows=[];tiles=[];data={}
 for ns,names in TARGETS.items():
  with zipfile.ZipFile(JARS[ns]) as z:
   for name in names:
    loc=source(z,ns,name);m=model(z,loc);data[ns+':'+name]=dict(source=loc,model=m)
    rows.append('\n'+ns+':'+name+' '+loc+' '+str(m['textures']))
    for i,e in enumerate(m['elements']):rows.append(str((i,e.get('name',''),e['from'],e['to'],e.get('rotation'),{k:(f['texture'],f.get('uv')) for k,f in e['faces'].items() if k in ('north','up')})))
    seen=set()
    for key,loc in m['textures'].items():
     if loc.startswith('#') or loc.startswith('minecraft:') or loc in seen:continue
     seen.add(loc);tns,r=loc.split(':');im=Image.open(io.BytesIO(z.read(f'assets/{tns}/textures/{r}.png'))).convert('RGBA')
     tile=Image.new('RGB',(256,288),'#484848');tile.paste(im.resize((256,256),Image.Resampling.NEAREST),(0,0));ImageDraw.Draw(tile).text((2,258),name+' #'+key,fill='white');tiles.append(tile)
 (ROOT/'preview/letsdo-models.json').write_text(json.dumps(data,indent=2),encoding='utf-8')
 (ROOT/'preview/letsdo-model-research.txt').write_text('\n'.join(rows),encoding='utf-8')
 for start in range(0,len(tiles),20):
  selected=tiles[start:start+20];sheet=Image.new('RGB',(1280,288*((len(selected)+4)//5)),'#282828')
  for i,t in enumerate(selected):sheet.paste(t,((i%5)*256,(i//5)*288))
  sheet.save(ROOT/f'preview/letsdo-textures-{start//20}.jpg')
 print('Resolved',len(data),'models;',len(tiles),'textures')

