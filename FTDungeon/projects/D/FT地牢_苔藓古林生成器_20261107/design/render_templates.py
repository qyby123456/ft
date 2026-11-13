import json
import math
import nbtlib
from PIL import Image, ImageDraw
from build_forest import ROOT, DATA

catalog=json.loads((DATA/'catalog.json').read_text(encoding='utf-8'))
canvas=Image.new('RGB',(6*220,5*250),'#101b14')
draw=ImageDraw.Draw(canvas)
for ordinal,item in enumerate(catalog['variants']):
    file=nbtlib.load(DATA/'structures'/f"{item['key']}.nbt")
    names=[str(block['Name']) for block in file['palette']]
    size=item['size']
    tile=Image.new('RGB',(size[0],size[2]),'#15251c')
    heights={}
    for block in file['blocks']:
        x,y,z=map(int,block['pos'])
        material=names[int(block['state'])]
        if material=='minecraft:air' or y>10: continue
        if heights.get((x,z),-1)>y: continue
        heights[x,z]=y
        color=(86,113,67)
        if 'log' in material or 'planks' in material: color=(100,72,43)
        elif 'water' in material: color=(35,110,138)
        elif 'leaves' in material or 'moss_block' in material: color=(62,132,54)
        elif 'brick' in material or 'stone' in material: color=(90,114,89)
        elif 'bookshelf' in material: color=(166,111,54)
        elif 'light' in material or 'lantern' in material: color=(232,182,82)
        tile.putpixel((x,z),tuple(min(255,int(channel*(0.65+y*0.045))) for channel in color))
    left=(ordinal%6)*220+10; top=(ordinal//6)*250+30
    tile=tile.resize((200,200),Image.Resampling.NEAREST)
    canvas.paste(tile,(left,top))
    draw.text((left,top-20),item['key'],fill='#d9e6ca')
    draw.text((left,top+205),' x '.join(map(str,size)),fill='#98bc91')
    if 'portal' in item:
        x,y,z=item['portal']
        center=(left+int(x/size[0]*200),top+int(z/size[2]*200))
        draw.ellipse((center[0]-4,center[1]-4,center[0]+4,center[1]+4),outline='#f4cf58',width=2)
canvas.save(ROOT/'design/template-contact-sheet.png')
