import copy
import difflib
import gzip
import io
import hashlib
import json
import subprocess
from pathlib import Path
import nbtlib as n

ROOT = Path(__file__).resolve().parents[1]
PATCHER = Path('C:/Users/YOUR_USERNAME/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe')
DATA = ROOT / 'src/main/resources/data/ftmossgen'
LAYOUT = Path('D:/ft工程文件/S1_苔藓古林_连接地牢_20261105/layout.json')

def patch(path, text):
    if len(text) > 12000 and not path.exists():
        lines = text.splitlines()
        previous = None
        while lines:
            chunk = []
            while lines and (sum(map(len,chunk)) < 7000): chunk.append(lines.pop(0))
            if previous is None:
                operation = f'*** Add File: {path.as_posix()}\n'
                body = ''.join('+'+line+'\n' for line in chunk)
            else:
                operation = f'*** Update File: {path.as_posix()}\n@@\n'
                body = ' '+previous+'\n'+''.join('+'+line+'\n' for line in chunk)+'*** End of File\n'
            subprocess.run([str(PATCHER),'--codex-run-as-apply-patch','*** Begin Patch\n'+operation+body+'*** End Patch'],check=True,capture_output=True)
            previous=chunk[-1]
        return
    if path.exists():
        old = path.read_text(encoding='utf-8')
        if old == text: return
        diff=list(difflib.unified_diff(old.splitlines(),text.splitlines(),n=3,lineterm=''))[2:]
        chunks=[]
        for line in diff:
            if line.startswith('@@'): chunks.append([])
            else: chunks[-1].append(line)
        for chunk in chunks:
            change=f'*** Begin Patch\n*** Update File: {path.as_posix()}\n@@\n'+'\n'.join(chunk)+'\n*** End Patch'
            subprocess.run([str(PATCHER),'--codex-run-as-apply-patch',change],check=True,capture_output=True)
        return
        body = '@@\n' + ''.join('-'+line+'\n' for line in old.splitlines())
        header = 'Update'
    else:
        body = ''
        header = 'Add'
    body += ''.join('+'+line+'\n' for line in text.splitlines())
    subprocess.run([str(PATCHER), '--codex-run-as-apply-patch', f'*** Begin Patch\n*** {header} File: {path.as_posix()}\n{body}*** End Patch'], check=True, capture_output=True)

def state(name, **props):
    result = n.Compound({'Name': n.String('minecraft:'+name)})
    if props:
        result['Properties'] = n.Compound({key:n.String(value) for key,value in props.items()})
    return result

def generate():
    manifest = json.loads((ROOT/'src/main/resources/data/fteclipsegen/catalog.json').read_text())
    sources = json.loads(LAYOUT.read_text(encoding='utf-8'))['modules']
    reports = []
    source_map = {'turn/pillar_room':0,'turn/big_doors':3,'turn/small_doors':5,'turn/hole':7,
        'straight/room_cells':2,'straight/room_storage':1,'straight/room_towers':8,
        'terminal/canteen':6,'terminal/storage':1,'terminal/cells':8,'promenade':7,
        'straight/simple':4,'straight/door':4,'straight/bridge':4,'straight/hole':4,'straight/platform':4}
    for item in manifest['variants']:
        key = item['key']
        original = copy.deepcopy(item)
        boss = key == 'boss/eclipse_keeper'
        child = key.startswith('straight/room/layout/')
        stairs = key.startswith('stairs/')
        spawn = key == 'spawn/spawn'
        scenery = key.startswith('spawn/outside')
        size = original['size'] if boss or child else [48,64 if stairs else 30,48]
        width,height,depth = size
        palette = []
        palette_ids = {}
        cells = {}
        entities = {}
        def setblock(x,y,z,material):
            if not (0 <= x < width and 0 <= y < height and 0 <= z < depth):
                raise ValueError((key,x,y,z))
            encoded = str(material)
            if encoded not in palette_ids:
                palette_ids[encoded] = len(palette)
                palette.append(material)
            cells[(x,y,z)] = palette_ids[encoded]
        air = state('air')
        for y in range(height):
            for z in range(depth):
                for x in range(width): setblock(x,y,z,air)
        def fill(start,end,material):
            for y in range(start[1],end[1]+1):
                for z in range(start[2],end[2]+1):
                    for x in range(start[0],end[0]+1): setblock(x,y,z,material)
        brick = state('mossy_stone_bricks')
        wood = state('dark_oak_log',axis='y')
        if boss or child:
            old = n.load(ROOT/'src/main/resources/data/fteclipsegen/structures'/f'{key}.nbt')
            for block in old['blocks']:
                position = tuple(map(int,block['pos']))
                material = copy.deepcopy(old['palette'][int(block['state'])])
                name = str(material['Name'])
                mapping = {'deepslate_bricks':'mossy_stone_bricks','polished_deepslate':'stone_bricks',
                    'deepslate_tiles':'mossy_cobblestone','purple_stained_glass':'green_stained_glass',
                    'blackstone':'dark_oak_log','amethyst_block':'moss_block','polished_blackstone_bricks':'mossy_stone_bricks'}
                short = name.split(':')[-1]
                if short in mapping: material = state(mapping[short],**({'axis':'y'} if mapping[short]=='dark_oak_log' else {}))
                setblock(*position,material)
                if 'nbt' in block: entities[position] = copy.deepcopy(block['nbt'])
            if boss:
                for x,z in [(3,4),(30,4),(3,23),(30,23)]:
                    fill((x,2,z),(x,12,z),wood)
                    fill((max(0,x-2),12,max(0,z-2)),(min(width-1,x+2),13,min(depth-1,z+2)),state('oak_leaves',persistent='true',distance='1',waterlogged='false'))
            source = None
        else:
            feet = 4 if spawn else 2
            fill((0,feet-2,0),(47,feet-1,47),brick)
            fill((0,feet,0),(47,feet+7,0),brick)
            fill((0,feet,47),(47,feet+7,47),brick)
            fill((0,feet,0),(0,feet+7,47),brick)
            fill((47,feet,0),(47,feet+7,47),brick)
            for x in range(6,43,6):
                for z in [5,42]:
                    fill((x,feet,z),(x,feet+7,z),wood)
                    setblock(x,feet+6,z+1 if z==5 else z-1,state('shroomlight'))
            source = sources[source_map[key]] if key in source_map else None
            if source:
                old = n.load(source['path'])
                ox = (48-int(old['size'][0]))//2
                oz = (48-int(old['size'][2]))//2
                for block in old['blocks']:
                    px,py,pz = map(int,block['pos'])
                    destination = (px+ox,py,pz+oz)
                    setblock(*destination,copy.deepcopy(old['palette'][int(block['state'])]))
                    if 'nbt' in block: entities[destination] = copy.deepcopy(block['nbt'])
                for port in source['ports']:
                    px,py,pz = port['feet']
                    px += ox; pz += oz
                    if port['side'] in ['north','south']:
                        end = 3 if port['side']=='north' else 44
                        fill((px-1,py-1,min(pz,end)),(px+1,py-1,max(pz,end)),brick)
                        fill((px-1,py,min(pz,end)),(px+1,py+3,max(pz,end)),air)
                    else:
                        end = 3 if port['side']=='west' else 44
                        fill((min(px,end),py-1,pz-1),(max(px,end),py-1,pz+1),brick)
                        fill((min(px,end),py,pz-1),(max(px,end),py+3,pz+1),air)
            else:
                for x,z in [(10,10),(37,10),(10,37),(37,37)]:
                    fill((x,feet,z),(x,feet+10,z),wood)
                    fill((x-3,feet+9,z-3),(x+3,feet+11,z+3),state('oak_leaves',persistent='true',distance='1',waterlogged='false'))
                    for radius in range(1,4):
                        setblock(x+radius,feet,z,state('dark_oak_log',axis='x'))
                if 'bridge' in key or 'hole' in key:
                    fill((15,feet-1,15),(32,feet-1,32),state('water',level='0'))
                if 'door' in key:
                    fill((18,feet,18),(19,feet+7,20),wood)
                    fill((28,feet,18),(29,feet+7,20),wood)
            for ybase in ([2,34] if stairs else [feet]):
                for start,end in [((2,ybase,2),(4,ybase+5,45)),((43,ybase,2),(45,ybase+5,45)),
                    ((2,ybase,2),(45,ybase+5,4)),((2,ybase,43),(45,ybase+5,45))]:
                    fill(start,end,air)
                    fill((start[0],ybase-1,start[2]),(end[0],ybase-1,end[2]),brick)
                if not source:
                    fill((22,ybase,1),(25,ybase+5,46),air)
                    fill((22,ybase-1,1),(25,ybase-1,46),brick)
                    fill((1,ybase,22),(46,ybase+5,25),air)
                    fill((1,ybase-1,22),(46,ybase-1,25),brick)
                else:
                    for start,end in [((22,ybase,1),(25,ybase+5,4)),((22,ybase,43),(25,ybase+5,46)),
                        ((1,ybase,22),(4,ybase+5,25)),((43,ybase,22),(46,ybase+5,25))]:
                        fill(start,end,air)
                        fill((start[0],ybase-1,start[2]),(end[0],ybase-1,end[2]),brick)
            if stairs:
                for step in range(33):
                    x = 7+step
                    fill((x,0,21),(x,step+1,26),brick)
                    fill((x,step+2,21),(x,step+7,26),air)
                fill((3,1,21),(7,1,26),brick)
                fill((39,33,21),(44,33,26),brick)
                fill((3,2,21),(6,7,26),air)
                fill((39,34,21),(44,40,26),air)
            item['ports'] = []
            for port in original['ports']:
                transformed=[]
                for px,py,pz in port['cells']:
                    transformed.append([px+16 if px not in [0,15] else (0 if px==0 else 47),
                        py if py < 18 else py+16,pz+16 if pz not in [0,15] else (0 if pz==0 else 47)])
                item['ports'].append({'cells':transformed})
                for px,py,pz in transformed:
                    fill((px,py,pz),(px,py+4,pz),air)
                    setblock(px,py-1,pz,brick)
            item['sockets']=[]
            positions=[(3,2,12),(44,2,12),(3,2,34),(44,2,34),(12,2,3),(34,2,3),(12,2,44),(34,2,44)]
            for ordinal,old_socket in enumerate(original['sockets']):
                px,py,pz=positions[ordinal]
                item['sockets'].append({'position':[px,py,pz],'support':[px,py-1,pz],
                    'spawn_position':[px+1 if px==3 else px-1,py,pz],'kind':old_socket['kind']})
            if source and (key.startswith('turn/') or key.startswith('straight/room_') or key.startswith('terminal/') or key=='promenade'):
                fill((8,1,41),(15,1,46),brick)
                fill((8,2,41),(15,8,46),air)
                fill((8,8,41),(15,8,46),state('dark_oak_planks'))
                item['portal']=[11,2,44]
                item['portal_facing']=0
                item['portal_trigger']=[11,2,42]
            if spawn:
                fill((6,3,6),(25,3,25),brick)
                fill((6,4,6),(25,9,25),air)
            if scenery:
                item['ports']=[]
        item['size']=size
        payload=n.File({'DataVersion':n.Int(3465),'size':n.List[n.Int](size),'palette':n.List[n.Compound](palette),
            'blocks':n.List[n.Compound]([n.Compound({'pos':n.List[n.Int](position),'state':n.Int(index),
                **({'nbt':entities[position]} if position in entities else {})}) for position,index in cells.items()]),
            'entities':n.List[n.Compound]([])})
        target=DATA/'structures'/f'{key}.nbt'
        target.parent.mkdir(parents=True,exist_ok=True)
        buffer=io.BytesIO()
        payload.write(buffer)
        target.write_bytes(gzip.compress(buffer.getvalue(),mtime=0))
        item['sha256']=hashlib.sha256(target.read_bytes()).hexdigest()
        item['style']='moss_ancient_forest'
        item['source_room']=None if not source else {'path':source['path'],'sha256':source['sha256'],'size':source['size'],'preserved_uncropped':True}
        reports.append({'key':key,'size':size,'sha256':item['sha256'],'source':item['source_room'],
            'cells':len(cells),'palette':len(palette),'portal':item.get('portal')})
    patch(DATA/'catalog.json',json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
    reference=json.loads((ROOT/'src/main/resources/data/fteclipsegen/reference-test.json').read_text())
    reference['forest_grid_step']=48
    reference['forest_floor_step']=32
    reference['forest_note']='Original random recursion reference retained; origins in rooms are legacy reference coordinates, not forest world positions.'
    patch(DATA/'reference-test.json',json.dumps(reference,ensure_ascii=False,indent=2)+'\n')
    patch(ROOT/'design/templates-A.json',json.dumps(reports,ensure_ascii=False,indent=2)+'\n')

if __name__=='__main__': generate()
