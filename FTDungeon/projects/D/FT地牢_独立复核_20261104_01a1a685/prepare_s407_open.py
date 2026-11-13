import json
import sys
from pathlib import Path
import audit

sys.path.insert(0,'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import read_nbt, write_nbt, Tag
folder=Path(__file__).resolve().parent/'S4-07-independent'
target=folder/'open-door-analysis'
target.mkdir(exist_ok=False)
name,root=read_nbt(folder/'room.nbt')
changed_palette=0
for state in root.value['palette'].value:
    if state.value['Name'].value.endswith('_door'):
        assert state.value['Properties'].value['open'].value=='false'
        state.value['Properties'].value['open']=Tag(8,'true')
        changed_palette+=1
write_nbt(target/'room.nbt',root,name)
def cells(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
closed,before=cells(folder/'room.nbt')
opened,after=cells(target/'room.nbt')
assert before.keys()==after.keys() and closed['size']==opened['size'] and closed['entities']==opened['entities']
assert {tuple(entry['pos']):entry.get('nbt') for entry in closed['blocks']}=={tuple(entry['pos']):entry.get('nbt') for entry in opened['blocks']}
changed=[position for position in before if before[position]!=after[position]]
assert len(changed)==4
for position in changed:
    first,last=before[position],after[position]
    assert first['Name']==last['Name'] and first['Name'].endswith('_door')
    assert first['Properties']['open']=='false' and last['Properties']['open']=='true'
    assert {key:value for key,value in first['Properties'].items() if key!='open'}=={key:value for key,value in last['Properties'].items() if key!='open'}
spec=json.loads((folder/'route-input.json').read_text(encoding='utf-8'))
spec[0].update(candidate=str(target/'room.nbt'),sha256=audit.digest(target/'room.nbt'))
(target/'route-input.json').write_text(json.dumps(spec,indent=2),encoding='utf-8')
record={'scope':'Non-deployable independent open-door analysis only','changed_positions':changed,'only_four_open_properties':True,'block_entities_unchanged':True,'sha256':audit.digest(target/'room.nbt')}
(folder/'independent-door-diff.json').write_text(json.dumps(record,indent=2),encoding='utf-8')
print(json.dumps(record))
