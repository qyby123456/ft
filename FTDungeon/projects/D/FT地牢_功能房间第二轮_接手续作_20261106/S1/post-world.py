import hashlib
import json
import pathlib
import sys

ROOT=pathlib.Path(__file__).parent
sys.path.insert(0,r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
from nbtio import Structure, encode_payload

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    world=ROOT/sys.argv[1]
    report=json.loads((world/'world-report.json').read_text(encoding='utf-8'))
    inputs=json.loads((world/'inputs.json').read_text(encoding='utf-8'))
    details=[]
    for item in report['candidates']:
        source=next(source for source in inputs if source['id']==item['id'])
        original=Structure.read(source['candidate'])
        assert digest(pathlib.Path(source['candidate']))==item['source_sha256']
        changed=[]
        unexpected_water=[]
        for capture in item['after_captures']:
            path=pathlib.Path(capture['file'])
            assert digest(path)==capture['sha256']
            after=Structure.read(path)
            offset=capture['offset']
            for position,state in after.blocks.items():
                target=tuple(coordinate+shift for coordinate,shift in zip(position,offset))
                before=original.get(target)
                if state!=before:
                    changed.append({'position':target,'before':before.name,'before_properties':dict(before.properties),'after':state.name,'after_properties':dict(state.properties)})
                if state.name=='minecraft:water' and before.name!='minecraft:water' and dict(before.properties).get('waterlogged')!='true':
                    unexpected_water.append(target)
        details.append({'id':item['id'],'source_sha256':item['source_sha256'],'capture_count':len(item['after_captures']),'state_changes':changed,'water_in_previously_dry_cells':unexpected_water,'interaction_issues':item['interaction_issues'],'support_failures':item['support_failures'],'dropped_items':item['dropped_items'],'elapsed_ticks':item['elapsed_ticks']})
    (world/'decoded-post-world-analysis.json').write_text(json.dumps({'candidates':details,'scope':'All decoded post-tick capture cells compared with source including source-omitted air; actual interaction results retained separately in world-report.json'},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    for item in details:
        print(item['id'],'changes',len(item['state_changes']),'new water',len(item['water_in_previously_dry_cells']),'interaction issues',item['interaction_issues'],'drops',len(item['dropped_items']))

if __name__=='__main__':
    main()
