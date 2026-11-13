import hashlib
import json
import math
from collections import Counter
from pathlib import Path
from audit_s8_submission import decode, load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S8-submission-20261107-104351-507543'
supplement=workspace/'preserved-review-snapshots/S8-runtime-supplement-20261107-1340'
runtime=Path(load(supplement/'independent-runtime-assembly-r2.json')['runtime'])
assert load(supplement/'independent-runtime-assembly-r2.json')['exit_code']==0
report=load(runtime/'assembly-runtime.json')
assert report['source_sha256']=='4d768bcaa073ba63da090ea9b6f64068c474d8a4c4d627217a81803e0c83fd37'
assert report['movement_waypoints_total']==report['movement_waypoints_reached']==4413 and report['movement_roundtrip_complete']
assert report['natural_wait_ticks']==600 and not report['lost_water'] and not report['spread_water'] and not report['dropped_items']
original,before=decode(root/'assembly/s8-functional-combined.nbt')
captured,after=decode(runtime/'assembly-after.nbt')
assert original['size']==captured['size'] and len(after)==math.prod(original['size'])==1009200
assert (runtime/'assembly-after.nbt').read_bytes()==(supplement/'runtime-assembly-r2/assembly-after.nbt').read_bytes()
properties=Counter();removed_fields=Counter();added_fields=Counter()
for position,actual in after.items():
    expected=before.get(position,{'state':{'Name':'minecraft:air'},'nbt':None})
    name=expected['state']['Name'];assert name==actual['state']['Name'],position
    first=expected['state'].get('Properties',{});second=actual['state'].get('Properties',{})
    for key,value in first.items():
        if second.get(key)!=value:
            assert (name.endswith('_stairs') and key=='shape') or (name.endswith('_wall') and key in ('east','west','north','south','up')) or (name.endswith('_fence') and key in ('east','west','north','south')), (position,name,key,value,second)
            properties[name+':'+key]+=1
    first,second=expected['nbt'] or {},actual['nbt'] or {}
    for key,value in first.items():
        if second.get(key)!=value:
            assert name=='minecraft:purple_bed' and key=='color' and value==10 and key not in second,(position,name,key,value,second)
            removed_fields[name+':'+key]+=1
    for key,value in second.items():
        if key not in first:
            assert (name=='minecraft:player_head' and key=='id' and value=='minecraft:skull') or (name in ('minecraft:furnace','minecraft:blast_furnace','minecraft:smoker') and ((key in ('CookTime','BurnTime','CookTimeTotal') and value==0) or (key=='RecipesUsed' and value=={}))) or (name=='minecraft:lectern' and key in ('id','Page')), (position,name,key,value)
            added_fields[name+':'+key]+=1
    assert not second.get('Items') and not second.get('LootTable')
geometry=[]
for folder in sorted((root/'independent-geometry').glob('S8-*')):
    item=load(folder/'geometry-report.json');assert all(sample['native_clear'] and sample.get('native_supported',True) for sample in item['samples']);geometry.append(item)
assert len(geometry)==10 and sum(len(item['samples']) for item in geometry)==47
result={'accepted':False,'status':'PARENT_ACTUAL_ASSEMBLY_NATURAL_TRAVEL_FULL_CAPTURE_AND_GEOMETRY_PASS','source_sha256':report['source_sha256'],'full_cells':1009200,'zero_type_changes':True,'world_capture_producer_byte_exact':True,'natural_wait_ticks':600,'actual_server_ticks':report['server_ticks'],'waypoints':4413,'returned_to_start':True,'properties':dict(properties),'removed_legacy_fields':dict(removed_fields),'added_default_fields':dict(added_fields),'native_geometry_samples':47,'native_apertures':20,'limits':['Existing32container and6door server use still manual hit positions; actual eye rays pending supplement','Finite unrotated isolated world; not full pack or real client certification']}
(root/'independent-world-inspection.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
