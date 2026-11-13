import hashlib
import json
import math
from collections import Counter
from pathlib import Path

from audit_s8_submission import decode, load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S3-access-revision-20261107-1103'
runtime=Path(load(root/'independent-run.json')['runtime'])
original,before=decode(root/'rooms/s3-merged/s3-merged.nbt')
captured,after=decode(runtime/'installed-full.nbt')
assert captured['size']==original['size'] and len(after)==math.prod(original['size'])==1572165
assert (runtime/'installed-full.nbt').read_bytes()==(root/'accessqa-r1/installed-full.nbt').read_bytes()
connections=Counter();defaults=Counter();extra_fields=Counter()
for position,actual in after.items():
    expected=before.get(position,{'state':{'Name':'minecraft:air'},'nbt':None})
    assert actual['state']['Name']==expected['state']['Name'],position
    name=actual['state']['Name']
    for key,value in expected['state'].get('Properties',{}).items():
        if actual['state'].get('Properties',{}).get(key)!=value:
            assert name in ('minecraft:iron_bars','minecraft:light_blue_stained_glass_pane','minecraft:glass_pane','minecraft:blue_stained_glass_pane') and key in ('east','west','north','south')
            connections[name]+=1
    for key,value in actual['state'].get('Properties',{}).items():
        if key not in expected['state'].get('Properties',{}):
            assert (key in ('waterlogged','powered') and value=='false') or (name=='minecraft:target' and key=='power' and value=='0') or (name=='minecraft:stone_brick_wall' and ((key in ('east','west','north','south') and value=='none') or (key=='up' and value=='true')))
            defaults[name+':'+key]+=1
    first,second=expected['nbt'] or {},actual['nbt'] or {}
    assert all(second.get(key)==value for key,value in first.items()),(position,first,second)
    for key,value in second.items():
        if key not in first:
            assert (key=='id' and name=='minecraft:lectern' and value=='minecraft:lectern') or (key=='Patterns' and value==[]) or (key in ('CookTime','BurnTime','CookTimeTotal') and value==0) or (key=='RecipesUsed' and value=={})
            extra_fields[name+':'+key]+=1
    assert not second.get('Items') and not second.get('LootTable')
result={'accepted':False,'status':'PARENT_FULL_INSTALLED_PAYLOAD_CLASSIFIED','full_cells':1572165,'zero_type_changes':True,'source_be_fields_preserved':True,'empty_inventories':True,'producer_capture_binary_equal':True,'connection_property_changes':dict(connections),'default_properties':dict(defaults),'default_be_fields':dict(extra_fields),'scope':'Parent actual install capture before natural route; finite installed state, inherited1200tick source evidence handled separately'}
(root/'independent-installed-inspection.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
