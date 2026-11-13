import hashlib
import json
from collections import Counter
from pathlib import Path
from check import READER

root = Path(__file__).resolve().parent
folder = root/'merged'
candidate = folder/'merged.nbt'
template = READER.decode(candidate)
design = json.loads((folder/'design.json').read_text(encoding='utf-8'))
blocks = {tuple(entry['pos']):(template['palette'][entry['state']],entry.get('nbt')) for entry in template['blocks']}
directions = ['north','west','south','east']
def position_turn(position, size, turns):
    result = list(position)
    dimensions = list(size)
    for turn in range(turns):
        result = [result[2],result[1],dimensions[0]-1-result[0]]
        dimensions = [dimensions[2],dimensions[1],dimensions[0]]
    return result
def property_turn(properties, turns):
    result = {}
    for key,value in properties.items():
        if key in directions:
            key = directions[(directions.index(key)+turns)%4]
        if value in directions:
            value = directions[(directions.index(value)+turns)%4]
        elif key=='axis' and turns%2 and value in ['x','z']:
            value = 'z' if value=='x' else 'x'
        elif key=='rotation':
            value = str((int(value)-4*turns+16)%16)
        result[key] = value
    return result
sources = []
for module in design['modules']:
    source = Path(module['source'])
    assert hashlib.sha256(source.read_bytes()).hexdigest()==module['source_sha256']
    original = READER.decode(source)
    exceptions = set()
    if module['id'] in ['S5-11','S5-20']:
        terminal = module['ports'][0 if module['id']=='S5-11' else 1]['feet']
        exceptions = {(terminal[0],terminal[1]+rise,terminal[2]+offset) for rise in range(4) for offset in range(-2,3)}
    compared = 0
    for entry in original['blocks']:
        transformed = position_turn(entry['pos'], original['size'], module['turns_ccw'])
        absolute = tuple(value+change for value,change in zip(transformed,module['origin']))
        state = original['palette'][entry['state']]
        expected = {'Name':state['Name']}
        properties = property_turn(state.get('Properties',{}),module['turns_ccw'])
        if properties:
            expected['Properties'] = properties
        actual,payload = blocks[absolute]
        if absolute in exceptions:
            assert actual['Name']=='minecraft:cyan_stained_glass'
        else:
            assert actual['Name']==expected['Name'] and actual.get('Properties',{})==expected.get('Properties',{}), (module['id'],absolute,expected,actual)
            assert payload==entry.get('nbt'), (module['id'],absolute,'payload mismatch')
        compared += 1
    sources.append({'id':module['id'],'source_sha256':module['source_sha256'],'records_compared':compared,'terminal_glass_exceptions':len(exceptions),'passed':True})
size = template['size']
shell = 0
for position,(state,payload) in blocks.items():
    if any(value==0 or value==size[axis]-1 for axis,value in enumerate(position)):
        assert state['Name']=='minecraft:barrier',position
        shell += 1
assert not template.get('entities',[])
report = {'sha256':hashlib.sha256(candidate.read_bytes()).hexdigest(),'passed':True,'sources':sources,'shell_barriers':shell,'entities':0,'block_entity_payloads':sum(payload is not None for state,payload in blocks.values()),'limitations':['Terminal caps intentionally replace 20 cells each; no public open external door.','Barrier shell topology only; no flooded-ocean placement or oxygen certification.']}
(folder/'source-integrity.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False))
