import hashlib
import json
import subprocess
from pathlib import Path
import nbtlib

ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/'src/main/resources/data/ftmossgen'
PATCHER=Path('C:/Users/YOUR_USERNAME/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe')

def save(path,data):
    text=json.dumps(data,ensure_ascii=False,indent=2)+'\n'
    old=path.read_text(encoding='utf-8') if path.exists() else None
    body=('@@\n'+''.join('-'+line+'\n' for line in old.splitlines())) if old is not None else ''
    body+=''.join('+'+line+'\n' for line in text.splitlines())
    subprocess.run([str(PATCHER),'--codex-run-as-apply-patch',f'*** Begin Patch\n*** {"Update" if old is not None else "Add"} File: {path.as_posix()}\n{body}*** End Patch'],check=True,capture_output=True)

def role(key):
    for prefix,value in [('straight/room_','room'),('turn/','turn'),('terminal/','terminal'),('boss/','boss')]:
        if key.startswith(prefix):return value
    return 'promenade' if key=='promenade' else None

def solid(name):
    return name in {'minecraft:cut_sandstone','minecraft:smooth_sandstone','minecraft:sandstone','minecraft:chiseled_sandstone',
        'minecraft:terracotta','minecraft:yellow_terracotta','minecraft:glowstone','minecraft:gold_block',
        'minecraft:polished_diorite','minecraft:quartz_bricks','minecraft:diorite','minecraft:chiseled_quartz_block',
        'minecraft:quartz_pillar','minecraft:packed_ice','minecraft:blue_ice','minecraft:sea_lantern',
        'minecraft:mossy_stone_bricks','minecraft:stone_bricks','minecraft:mossy_cobblestone','minecraft:cobblestone',
        'minecraft:moss_block','minecraft:dark_oak_log','minecraft:oak_log','minecraft:dark_oak_planks','minecraft:shroomlight'}

catalog=json.loads((DATA/'catalog.json').read_text(encoding='utf-8'))
profiles=[json.loads((DATA/'styles'/f'{name}.json').read_text(encoding='utf-8')) for name in ['forest','desert','frost']]
reports={}
for style in profiles:
    assert style['schema']==1 and style['catalog']=='ftmossgen:catalog'
    assert style['id'] in ['ftmossgen:forest','ftmossgen:desert','ftmossgen:frost']
    if style['id']=='ftmossgen:forest':assert not style['palette'] and not style['decorations']
    counts={}
    for template in catalog['variants']:
        matched=[decoration for decoration in style['decorations'] if decoration['role'] in ['all',role(template['key'])] and role(template['key'])]
        if not matched:continue
        data=nbtlib.load(DATA/'structures'/f"{template['key']}.nbt")
        names=[str(entry['Name']) for entry in data['palette']]
        cells={tuple(map(int,cell['pos'])):style['palette'].get(names[int(cell['state'])],names[int(cell['state'])]) for cell in data['blocks']}
        original=dict(cells)
        protected=set()
        def protect(center,radius,bottom,top):
            for dx in range(-radius,radius+1):
                for dz in range(-radius,radius+1):
                    for dy in range(bottom,top+1):protected.add((center[0]+dx,center[1]+dy,center[2]+dz))
        for cell in data['blocks']:
            position=tuple(map(int,cell['pos']))
            if 'nbt' in cell:protect(position,1,-1,2)
        for (x,y,z),name in original.items():
            if name=='minecraft:air' and original.get((x,y+1,z))=='minecraft:air' and solid(original.get((x,y-1,z))):protect((x,y,z),0,-1,2)
        for port in template['ports']:
            for point in port['cells']:protect(point,2,-1,6)
        for socket in template['sockets']:
            protect(socket['position'],2,-1,5);protect(socket['support'],1,0,1);protect(socket['spawn_position'],2,-1,5)
        if template.get('portal'):protect(template['portal'],4,-1,6)
        if template.get('portal_trigger'):protect(template['portal_trigger'],3,-1,5)
        if role(template['key'])=='boss':
            for point in [(16,2,2),(16,2,13),(16,2,25),(16,2,22)]:protect(point,4,-1,6)
        additions=0
        for decoration in matched:
            start=decoration['from'];end=decoration['to']
            assert decoration['mode']=='air' and decoration['spacing']>0 and start[1]>=8
            assert all(0<=start[axis]<=end[axis]<template['size'][axis] for axis in range(3))
            assert solid(decoration['block'])
            for y in range(end[1],start[1]-1,-1):
                for z in range(start[2],end[2]+1,decoration['spacing']):
                    for x in range(start[0],end[0]+1):
                        position=(x,y,z)
                        if position in protected or cells.get(position)!='minecraft:air':continue
                        if not all(cells.get((x,y-depth,z))=='minecraft:air' for depth in [1,2,3]):continue
                        if not any(solid(cells.get((x+dx,y+dy,z+dz))) for dx,dy,dz in [(0,1,0),(1,0,0),(-1,0,0),(0,0,1),(0,0,-1)]):continue
                        cells[position]=decoration['block'];additions+=1
        assert all(cells[position]==name for position,name in original.items() if name!='minecraft:air')
        counts[template['key']]=additions
    if style['decorations']:assert sum(counts.values())>0
    reports[style['id']]={'staticFullBlockWhitelistSimulationAdditions':counts,'total':sum(counts.values()),
        'nativeCollisionVerification':'NOT_RUN; whitelist simulation is not the actual Java runtime'}
files=['src/main/java/moss/generator/StyleGeometry.java']+[f'src/main/resources/data/ftmossgen/styles/{name}.json' for name in ['forest','desert','frost']]
result={'status':'SOURCE_AND_STATIC_READY','schema':1,'files':files,'sha256':{path:hashlib.sha256((ROOT/path).read_bytes()).hexdigest() for path in files},
    'profiles':reports,'newNBTTemplates':0,'extraDecorationFields':[],'nativeCompilation':'NOT_RUN_BY_A','worldTests':'NOT_RUN_BY_A',
    'limits':['Native host/C must verify final geometry, profile parsing, plan persistence and walk graph after palette plus geometry.',
        'Decorations only attach to existing/new full solids above or sideways; unsupported AIR candidates intentionally skipped.',
        'Forest source palette and geometry preserved; profile cap/barrier and runtime stats remain explicit host settings.'],
    'paidImages':False,'deployed':False,'playerWorldsModified':False}
save(ROOT/'style-design/ready-A.json',result)
print(json.dumps({key:value['total'] for key,value in reports.items()}))
