import json,shutil
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S2-07-independent'
source=audit.BASE/'FT地牢_第2组重做B_20261104/S2-07/analysis-only/incoming/G2B-07/room.nbt'
def mapping(path):
    template=audit.decode(path)
    return {tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
closed=mapping(folder/'room.nbt')
opened=mapping(source)
assert closed.keys()==opened.keys()
changed=[position for position in closed if closed[position]!=opened[position]]
assert len(changed)==4
for position in changed:
    first,last=closed[position],opened[position]
    assert first['Name'].endswith('_door') and first['Name']==last['Name']
    assert first['Properties']['open']=='false' and last['Properties']['open']=='true'
    assert {key:value for key,value in first['Properties'].items() if key!='open'}=={key:value for key,value in last['Properties'].items() if key!='open'}
target=folder/'open-door-analysis'
target.mkdir(exist_ok=True)
shutil.copyfile(source,target/'room.nbt')
spec=json.loads((folder/'route-input.json').read_text())
spec[0].update(candidate=str(target/'room.nbt'),sha256=audit.digest(source))
(target/'route-input.json').write_text(json.dumps(spec,indent=2),encoding='utf-8')
(folder/'independent-door-diff.json').write_text(json.dumps({'only_four_open_properties_changed':True,'changed':changed,'analysis_sha256':audit.digest(source)},indent=2),encoding='utf-8')
