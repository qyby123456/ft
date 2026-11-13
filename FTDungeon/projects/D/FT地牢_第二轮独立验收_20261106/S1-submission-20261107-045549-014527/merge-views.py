import json
import pathlib

ROOT=pathlib.Path(__file__).parent
path=ROOT/'merged'/'design.json'
design=json.loads(path.read_text(encoding='utf-8'))
integrity=json.loads((ROOT/'merged'/'integrity.json').read_text(encoding='utf-8'))
ranges=[]
for room in integrity['rooms']:
    local=json.loads((pathlib.Path(room['candidate']).parent/'design.json').read_text(encoding='utf-8'))
    east,up,south=room['offset']
    width,height,depth=local['size']
    ranges.append({'from':[east,up+local['cutaway']['removeAbove'],south],'to':[east+width-1,up+height-1,south+depth-1]})
    ranges.append({'from':[east,up+2,south+local['cutaway']['frontDepth']],'to':[east+width-1,up+height-1,south+depth-1]})
    ranges.append({'from':[east+local['cutaway']['sideX'],up+2,south],'to':[east+width-1,up+height-1,south+depth-1]})
ranges.append({'from':[2,11,62],'to':[489,32,68]})
design['cutaway']['hiddenRanges']=ranges
design['focus_views']=[
    {'id':'atlas-11-13','position':[100,120,180],'target':[80,9,36],'fov':51},
    {'id':'atlas-14-17','position':[278,145,220],'target':[235,9,36],'fov':51},
    {'id':'atlas-18-20','position':[435,125,185],'target':[417,9,36],'fov':51},
    {'id':'connector-watch','position':[408,38,85],'target':[397,11,34],'fov':65},
]
path.write_text(json.dumps(design,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
