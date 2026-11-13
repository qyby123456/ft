import hashlib
import json
import pathlib
import sys

ROOT=pathlib.Path(__file__).parent
sys.path.insert(0,r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
from nbtio import Structure

def main():
    folder=ROOT/'merged'
    candidate=folder/'s1-mossy-functional-11-20.nbt'
    structure=Structure.read(candidate)
    surfaces=json.loads((folder/'corridor-surfaces.json').read_text(encoding='utf-8'))['surfaces']
    by_position={(entry['east'],entry['south']):entry['feet'] for entry in surfaces}
    issues=[]
    for entry in surfaces:
        east,south,feet=entry['east'],entry['south'],entry['feet']
        for up in range(int(feet)+(1 if feet%1 else 0),int(feet)+4):
            actual=structure.get((east,up,south))
            if actual.name!='minecraft:air':
                issues.append({'kind':'corridor_headroom','position':[east,up,south],'state':actual.name})
        support=structure.get((east,int(feet) if feet%1 else int(feet)-1,south))
        if support.name not in ['minecraft:mossy_stone_bricks','minecraft:stone_brick_slab']:
            issues.append({'kind':'corridor_floor','position':[east,feet,south],'state':support.name})
        for delta_east,delta_south in [(1,0),(0,1)]:
            other=by_position.get((east+delta_east,south+delta_south))
            if other is not None and abs(feet-other)>0.5:
                issues.append({'kind':'adjacent_floor_step_gt_half','position':[east,feet,south],'other':other})
    integrity=json.loads((folder/'integrity.json').read_text(encoding='utf-8'))
    ports=[]
    for room in integrity['rooms']:
        design=json.loads((pathlib.Path(room['candidate']).parent/'design.json').read_text(encoding='utf-8'))
        east_offset,up_offset,south_offset=room['offset']
        for port in design['ports']:
            east,feet,south=[coordinate+offset for coordinate,offset in zip(port['feet'],room['offset'])]
            half=port['width']//2
            checked=0
            for across in range(-half,half+1):
                for inward in range(-2,3):
                    for rise in range(port['clearHeight']):
                        position=(east+inward,feet+rise,south+across) if port['side'] in ['west','east'] else (east+across,feet+rise,south+inward)
                        actual=structure.get(position)
                        if actual.name!='minecraft:air':
                            issues.append({'kind':'port_full_interface','room':room['id'],'side':port['side'],'position':position,'state':actual.name})
                        checked+=1
            ports.append({'room':room['id'],'side':port['side'],'width':port['width'],'height':port['clearHeight'],'depth':5,'checked_cells':checked})
    report={'source_sha256':hashlib.sha256(candidate.read_bytes()).hexdigest(),'corridor_surface_cells':len(surfaces),'clear_height_checked':4,'all_declared_width_cells_checked':True,'ports':ports,'issues':issues,'status':'PASS' if not issues else 'REQUEST_CHANGES','scope':'Conservative all-cell air envelope, solid/slab floor and adjacent half-step audit; native collision route report remains separate.'}
    (folder/'corridor-audit.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('Corridor cells',len(surfaces),'ports',len(ports),'issues',len(issues))

if __name__=='__main__':
    main()
