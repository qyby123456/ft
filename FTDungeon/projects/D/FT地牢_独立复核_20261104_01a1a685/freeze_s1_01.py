import json
import shutil
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
source=audit.BASE/'FT地牢_Image25接入_20261101/work/nature-redo-b01-s1-01-20261104'
module=source/'nature-redo-b01-s1-01'
folder=ROOT/'S1-01-independent'
folder.mkdir(exist_ok=False)
manifest=json.loads((source/'producer-handover-20261105/file-manifest.json').read_text(encoding='utf-8'))['files']
for entry in manifest:
    path=Path(entry['path'])
    assert path.stat().st_size==entry['bytes'] and audit.digest(path)==entry['sha256']
audit.ROOT=folder
audit.GROUPS=[(1,str(module.relative_to(audit.BASE)),'room.nbt','preview.png')]
audit.main()
expected='ca9efd56f98374bdb17a7154e8a8dc67e3d2d5578ad4be90a1734e2bdfebee7c'
assert audit.digest(module/'room.nbt')==expected
shutil.copyfile(module/'room.nbt',folder/'room.nbt')
shutil.copyfile(ROOT/'vanilla-block-tags.json',folder/'vanilla-block-tags.json')
metadata=json.loads((module/'room.json').read_text(encoding='utf-8'))
targets=[{'label':'interior','position':metadata['interior']}]+[{'label':port['side'],'position':port['feet']} for port in metadata['ports']]
specification=[{'group':1,'candidate':str(folder/'room.nbt'),'sha256':expected,'size':metadata['size'],'start':metadata['entrance'],'targets':targets,'swimming':False}]
(folder/'route-input.json').write_text(json.dumps(specification,indent=2),encoding='utf-8')
(folder/'handover-manifest-independent.json').write_text(json.dumps({'entries_verified':len(manifest),'source':str(source)},indent=2),encoding='utf-8')
print('S1 frozen;handover hashes',len(manifest))
