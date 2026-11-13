import copy
import hashlib
import json
import shutil
import sys
import zipfile
from pathlib import Path
from collections import Counter

sys.path.insert(0, r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
from nbtio import Structure, state, plain

root = Path(__file__).resolve().parent
layout = json.loads((root/'layout.json').read_text(encoding='utf-8'))
source = root/'mossy-forest-connected.nbt'
assert hashlib.sha256(source.read_bytes()).hexdigest() == layout['sha256']
complete = Structure.read(source)
assert len(complete.blocks) == 145*24*173 and complete.data_version == 3465 and not complete.entities
caps = {tuple(position) for cap in layout['unused_ports_capped'] for position in cap['positions']}
door_changes = {tuple(change['position']):change for change in layout['entry_doors_opened']}
assert len(door_changes)==4
preservation=[]
for module in layout['modules']:
    path=Path(module['path'])
    assert hashlib.sha256(path.read_bytes()).hexdigest()==module['sha256']
    original=Structure.read(path)
    edits=Counter()
    for position, block_state in original.blocks.items():
        destination=tuple(position[axis]+module['origin'][axis] for axis in range(3))
        expected=block_state
        if destination in caps:
            expected=state('mossy_stone_bricks')
            edits['cap_cells']+=1
        elif destination in door_changes:
            properties=dict(expected.properties)
            assert properties['open']=='false'
            properties['open']='true'
            expected=state(expected.name,**properties)
            edits['opened_door_halves']+=1
        assert complete.get(destination)==expected,(module['image'],position)
        payload=copy.deepcopy(original.block_entities.get(position))
        if payload is not None:
            for axis,coordinate in zip(('x','y','z'),destination):
                if axis in payload.value:
                    payload.value[axis].value=coordinate
        actual=complete.block_entities.get(destination)
        assert (plain(actual) if actual else None)==(plain(payload) if payload else None)
    preservation.append({'module':module['image'],'all_records_checked':len(original.blocks),'only_declared_edits':dict(edits),'original_file_hash_unchanged':True})
assembly=json.loads((root/'assembly.json').read_text(encoding='utf-8'))
restored={}
restored_entities={}
for part in assembly['parts']:
    path=root/part['path']
    assert hashlib.sha256(path.read_bytes()).hexdigest()==part['sha256']
    fragment=Structure.read(path)
    assert max(fragment.size)<=48 and len(fragment.blocks)==part['explicit_records']
    for local,block_state in fragment.blocks.items():
        position=tuple(local[axis]+part['origin'][axis] for axis in range(3))
        assert position not in restored
        restored[position]=block_state
        if local in fragment.block_entities:
            payload=copy.deepcopy(fragment.block_entities[local])
            for axis,coordinate in zip(('x','y','z'),position):
                if axis in payload.value:
                    payload.value[axis].value=coordinate
            restored_entities[position]=plain(payload)
assert restored==complete.blocks
assert restored_entities=={position:plain(payload) for position,payload in complete.block_entities.items()}
native=json.loads((root/'qa/independent-native-frozen.json').read_text(encoding='utf-8'))[0]
routes=json.loads((root/'qa/independent-routes.json').read_text(encoding='utf-8'))[0]
assert native['sha256']==layout['sha256'] and native['registry_properties']==native['template_roundtrip_block_count']=='PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert routes['sha256']==layout['sha256'] and routes['start_clear']
assert len(routes['targets'])==2254 and all(target['reachable'] and target['standing_clear'] for target in routes['targets'])
excluded=json.loads((root/'capped-port-targets.json').read_text(encoding='utf-8'))['targets']
assert len(excluded)==23 and all(tuple(target['position']) in caps for target in excluded)
fluid_edges=[]
for (east,height,south),block_state in complete.blocks.items():
    if block_state.name in ('minecraft:water','minecraft:lava'):
        for adjacent in ((east-1,height,south),(east+1,height,south),(east,height-1,south),(east,height,south-1),(east,height,south+1)):
            if complete.get(adjacent).name=='minecraft:air':
                fluid_edges.append([(east,height,south),adjacent])
assert not fluid_edges
datapack=root/'datapack'
structure_dir=datapack/'data/s1_connected/structures'
function_dir=datapack/'data/s1_connected/functions'
structure_dir.mkdir(parents=True,exist_ok=True)
function_dir.mkdir(parents=True,exist_ok=True)
(datapack/'pack.mcmeta').write_text(json.dumps({'pack':{'pack_format':15,'description':'S1 mossy forest connected dungeon, Java 1.20.1'}},indent=2),encoding='utf-8')
shutil.copyfile(source,structure_dir/'full.nbt')
commands=[]
for part in assembly['parts']:
    filename=f"part_{part['order']:02}.nbt"
    shutil.copyfile(root/part['path'],structure_dir/filename)
    east,height,south=part['origin']
    commands.append(f'place template s1_connected:part_{part["order"]:02} ~{east} ~{height} ~{south}')
(function_dir/'assemble.mcfunction').write_text('\n'.join(commands)+'\n',encoding='utf-8')
with zipfile.ZipFile(root/'s1-connected-datapack.zip','w',zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(datapack.rglob('*')):
        if path.is_file():
            archive.write(path,path.relative_to(datapack).as_posix())
checks={'status':'PASS_OFFLINE_CONNECTED_EXPORT','source_sha256':layout['sha256'],'size':layout['size'],'explicit_cells':len(complete.blocks),'modules':preservation,'source_files_unchanged':True,'door_halves_opened':4,'capped_old_port_targets_excluded':23,'reachable_retained_body_targets':2254,'native_selected_support_points':native['survival_checked'],'parts':len(assembly['parts']),'exact_reassembly_states_and_entity_payloads':True,'fluid_side_bottom_air_edges':0,'block_entities':Counter(plain(payload)['id'] for payload in complete.block_entities.values()),'world_placement_ticks_real_gui_physics_gameplay':'NOT_RUN','scope':'New assembled source verified offline. No inherited world acceptance or formal deployment claim. Numeric NBT tag-width identity is not separately certified.'}
(root/'qa/verification.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({key:value for key,value in checks.items() if key!='modules'},ensure_ascii=False,indent=2))
