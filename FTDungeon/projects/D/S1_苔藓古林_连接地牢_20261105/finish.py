import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

root=Path(__file__).resolve().parent
layout=json.loads((root/'layout.json').read_text(encoding='utf-8'))
scale=6
padding=60
image=Image.new('RGB',(145*scale+2*padding,173*scale+2*padding+100),'#101d1b')
draw=ImageDraw.Draw(image)
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',18)
small=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',15)
title=ImageFont.truetype('C:/Windows/Fonts/msyhbd.ttc',29)
draw.text((padding,18),'苔藓古林 · 九域连通地牢',font=title,fill='#e3e9cf')
def point(east,south):
    return (padding+east*scale,padding+south*scale+55)
for link in layout['links']:
    points=[point(east,south) for east,south in link['waypoints_xz']]
    draw.line(points,fill='#a3a780',width=28,joint='curve')
    draw.line(points,fill='#314e3f',width=17,joint='curve')
for module in layout['modules']:
    east,_,south=module['origin']
    width,_,depth=module['size']
    box=(*point(east,south),*point(east+width-1,south+depth-1))
    draw.rounded_rectangle(box,radius=6,fill='#405b42',outline='#a9b78a',width=3)
    labels={1:'古树大厅',2:'泉室书房',3:'水景小室',4:'中央仪式主厅',5:'盘根直廊',6:'厚墙转角',7:'双门阶庭入口',8:'上层回廊庭院',9:'双守卫宝库'}
    label=f"{module['image']:02}\n{labels[module['image']]}"
    draw.text((box[0]+8,box[1]+9),label,font=small,fill='#f4ecd4')
entry=point(75,172)
draw.ellipse((entry[0]-7,entry[1]-7,entry[0]+7,entry[1]+7),fill='#f4bc66')
draw.text((entry[0]+15,entry[1]-10),'南入口 ↑',font=font,fill='#f4bc66')
draw.text((padding,image.height-45),'145 × 24 × 173  |  九个原模块 + 八段连接廊  |  示意图，非游戏截图',font=small,fill='#bdcbb7')
image.save(root/'layout-map.png')
render=json.loads((root/'qa/render.json').read_text(encoding='utf-8'))
assert render['source_sha256']==layout['sha256'] and len(render['views'])==8
manifest={}
for path in sorted(root.rglob('*')):
    if path.is_file() and path.name!='SHA256.json':
        manifest[path.relative_to(root).as_posix()]=hashlib.sha256(path.read_bytes()).hexdigest()
(root/'SHA256.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
print('Wrote layout map and',len(manifest),'hashes')
