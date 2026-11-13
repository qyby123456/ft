import hashlib
import json
from collections import deque
from build_forest import ROOT, DATA, patch
import nbtlib as n

def verify():
    manifest=json.loads((DATA/'catalog.json').read_text(encoding='utf-8'))
    checks=[]
    for item in manifest['variants']:
        path=DATA/'structures'/f"{item['key']}.nbt"
        assert hashlib.sha256(path.read_bytes()).hexdigest()==item['sha256']
        data=n.load(path)
        size=list(map(int,data['size']))
        assert size==item['size']
        palette=[str(entry['Name']).removeprefix('minecraft:') for entry in data['palette']]
        blocks={tuple(map(int,cell['pos'])):palette[int(cell['state'])] for cell in data['blocks']}
        assert len(blocks)==size[0]*size[1]*size[2]
        assert not data['entities']
        for port in item['ports']:
            for x,y,z in port['cells']:
                assert all(blocks[x,y+rise,z]=='air' for rise in range(5)),(item['key'],'port',x,y,z)
                assert blocks[x,y-1,z] not in ['air','water'],(item['key'],'port support')
        for socket in item['sockets']:
            assert blocks[tuple(socket['position'])]=='air',(item['key'],'rune')
            assert blocks[tuple(socket['support'])] not in ['air','water'],(item['key'],'rune support')
        if 'portal' in item:
            x,y,z=item['portal']
            for across in [-1,0,1]:
                assert blocks[x+across,y-1,z] not in ['air','water']
                for depth in range(-2,3):
                    for rise in range(4):
                        assert blocks[x+across,y+rise,z+depth]=='air',(item['key'],'portal',across,rise,depth)
                    assert blocks[x+across,y-1,z+depth] not in ['air','water']
        solid=lambda material: material not in ['air','water','cave_air','void_air'] and not any(part in material for part in ['stairs','slab','fence','pane','wall','torch','lantern','chain','vine','carpet','grass','leaves','sapling','flower','candle','door','ladder','rail','mushroom'])
        walk={position for position,material in blocks.items() if material=='air'
            and blocks.get((position[0],position[1]+1,position[2]))=='air'
            and solid(blocks.get((position[0],position[1]-1,position[2]),'air'))}
        if item['ports']:
            first=tuple(item['ports'][0]['cells'][1])
            queue=deque([first]); seen={first}
            while queue:
                x,y,z=queue.popleft()
                for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
                    for dy in [0,1,-1]:
                        target=(x+dx,y+dy,z+dz)
                        if target in walk and target not in seen:
                            if dy==1 and blocks.get((x,y+2,z))!='air': continue
                            if dy==-1 and blocks.get((x+dx,y+1,z+dz))!='air': continue
                            seen.add(target);queue.append(target)
            for port in item['ports']:
                assert tuple(port['cells'][1]) in seen,(item['key'],'unreachable port',port)
            if 'portal_trigger' in item: assert tuple(item['portal_trigger']) in seen,(item['key'],'unreachable portal')
        checks.append({'key':item['key'],'sha256':item['sha256'],'size':size,'cells':len(blocks),
            'ports':len(item['ports']),'sockets':len(item['sockets']),'portalClearance': 'portal' in item,
            'walkingConnectivity':'static conservative block BFS passed' if item['ports'] else 'not route piece',
            'fullSourceSize':item.get('source_room',{}).get('size') if item.get('source_room') else None})
    assert len(checks)==27
    report={'status':'SOURCE_AND_STATIC_READY','templateCount':27,'templates':checks,'gridStride':48,'floorStride':32,
        'nativeCompilation':'coordinator only; not run by A','worldTests':'not run by A',
        'limits':['Original S1 rooms embedded uncropped; connector thresholds may clear declared doorway cells.',
            'Static conservative support heuristic complements, does not replace native block collision validation.',
            'Binary NBT produced by reproducible design/build_forest.py; text/code changes use apply_patch.',
            'Four-floor recursion random calls unchanged; world spacing and therefore world-piece selection seeds intentionally differ from Eclipse.'],
        'files':['src/main/java/moss/generator/Planner.java','src/main/java/moss/generator/Catalog.java',
            'src/main/resources/data/ftmossgen/catalog.json','src/main/resources/data/ftmossgen/structures','src/main/resources/data/ftmossgen/reference-test.json','design/PORTAL-CONTRACT.md'],
        'playerWorldsModified':False,'paidImages':False,'deployed':False}
    patch(ROOT/'design/ready-A.json',json.dumps(report,ensure_ascii=False,indent=2)+'\n')
    print(json.dumps({'static':'PASS','variants':27,'native':'NOT_RUN'}))

if __name__=='__main__': verify()
