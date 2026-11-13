import copy
import hashlib
import json
import pathlib
import sys

ROOT=pathlib.Path(__file__).parent
sys.path.insert(0,r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
from nbtio import Structure
from merge import translated_entity

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

world=ROOT/'worldqa-run4'
output=world/'posttick-candidates'
output.mkdir(exist_ok=True)
reports=json.loads((world/'world-report.json').read_text(encoding='utf-8'))['candidates']
inputs=json.loads((world/'inputs.json').read_text(encoding='utf-8'))
assembled=[]
for report in reports:
    source=next(source for source in inputs if source['id']==report['id'])
    original=Structure.read(source['candidate'])
    result=Structure(original.size)
    coverage=0
    for capture in report['after_captures']:
        path=pathlib.Path(capture['file'])
        assert sha(path)==capture['sha256']
        chunk=Structure.read(path)
        offset=capture['offset']
        coverage+=chunk.size[0]*chunk.size[1]*chunk.size[2]
        for position,block in chunk.blocks.items():
            target=tuple(coordinate+shift for coordinate,shift in zip(position,offset))
            if target in original.blocks or block.name!='minecraft:air':
                result.set(target,block,copy.deepcopy(chunk.block_entities.get(position)))
        result.entities.extend(translated_entity(entity,offset) for entity in chunk.entities)
    assert coverage==original.size[0]*original.size[1]*original.size[2]
    path=output/(report['id']+'-after-1200.nbt')
    result.write(path)
    design=ROOT/'merged'/'design.json' if report['id']=='merged' else ROOT/report['id']/'design.json'
    assembled.append({'id':report['id'],'candidate':str(path),'design':str(design),'sha256':sha(path),'original_sha256':report['source_sha256'],'full_capture_volume':coverage,'explicit_records':len(result.blocks),'capture_manifest':report['after_captures'],'rule':'Retain all original explicit positions and all post-world nonair cells; omit only post-world air at source-omitted air positions.'})
(world/'posttick-inputs.json').write_text(json.dumps(assembled,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Reassembled eleven posttick candidates from full-volume captures with exact source bindings.')
