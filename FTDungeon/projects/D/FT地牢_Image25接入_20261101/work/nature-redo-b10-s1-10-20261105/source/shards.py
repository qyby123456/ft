import copy
import hashlib
import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FOLDER = ROOT / 'nature-redo-b10-s1-10'
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure, plain


def fingerprint(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def logical_hash(structure):
    content = {'size':structure.size,'data_version':structure.data_version,'states':[[list(point),state.name,dict(state.properties),plain(structure.block_entities[point]) if point in structure.block_entities else None] for point,state in sorted(structure.blocks.items())],'entities':[plain(entity) for entity in structure.entities]}
    return hashlib.sha256(json.dumps(content,sort_keys=True,separators=(',',':')).encode()).hexdigest()


def main():
    source = Structure.read(FOLDER/'room.nbt')
    assert source.size == (61,13,61) and not source.entities
    output = FOLDER/'shards'
    output.mkdir(exist_ok=True)
    rebuilt = Structure(source.size,data_version=source.data_version)
    pieces = []
    covered = set()
    for ordinal,(minX,minZ,maxX,maxZ) in enumerate([(0,0,31,31),(32,0,60,31),(0,32,31,60),(32,32,60,60)],1):
        offset = (minX,0,minZ)
        fragment = Structure((maxX-minX+1,13,maxZ-minZ+1),data_version=source.data_version)
        for point,state in source.blocks.items():
            if minX<=point[0]<=maxX and minZ<=point[2]<=maxZ:
                local = tuple(coordinate-shift for coordinate,shift in zip(point,offset))
                fragment.set(local,state,copy.deepcopy(source.block_entities.get(point)))
        path = output/f'part-{ordinal:02}.nbt'
        fragment.write(path)
        loaded = Structure.read(path)
        assert all(limit<=48 for limit in loaded.size) and len(loaded.blocks)==loaded.size[0]*loaded.size[1]*loaded.size[2]
        for local,state in loaded.blocks.items():
            point = tuple(coordinate+shift for coordinate,shift in zip(local,offset))
            assert point not in covered
            covered.add(point)
            rebuilt.set(point,state,copy.deepcopy(loaded.block_entities.get(local)))
        pieces.append({'order':ordinal,'file':str(path.resolve()),'sha256':fingerprint(path),'bytes':path.stat().st_size,'offset_from_common_source_origin':offset,'size':loaded.size,'explicit_records':len(loaded.blocks),'air_records':sum(state.name=='minecraft:air' for state in loaded.blocks.values()),'block_entities':len(loaded.block_entities),'entities':len(loaded.entities)})
    assert source.blocks == rebuilt.blocks and source.data_version == rebuilt.data_version
    assert {point:plain(entity) for point,entity in source.block_entities.items()} == {point:plain(entity) for point,entity in rebuilt.block_entities.items()}
    assert source.entities == rebuilt.entities == [] and len(covered)==48373
    rebuilt_path = output/'reassembled-source.nbt'
    rebuilt.write(rebuilt_path)
    reread = Structure.read(rebuilt_path)
    assert logical_hash(source)==logical_hash(rebuilt)==logical_hash(reread)
    result = {'status':'PASS_EXACT_OFFLINE_RECONSTRUCTION','source_nbt_sha256':fingerprint(FOLDER/'room.nbt'),'source_size':source.size,'common_origin':[0,0,0],'parts':pieces,'placement_order':[1,2,3,4],'rotation':'Translations only, no independent rotation or mirror of fragments. Rotate the entire assembly and offsets together if needed.','air_policy':'Overwrite every declared position, including explicit minecraft:air; do not skip air, use structure_void, or preserve preexisting blocks. Disjoint half-open tiles [0,32)/[32,61), no shared cells; all 48373 cells covered exactly once.','placement_note':'Place all four pieces with identical integrity=1 and seed, finish assembly before final neighbor/support validation. Fragment survival in isolation is not certified. Only source assembly has native four-rotation tests.','block_state_cells_compared':48373,'duplicate_cells':0,'uncovered_cells':0,'state_mismatches':0,'block_entity_payload_mismatches':0,'block_entities_equal':2,'ordinary_entities_equal':0,'data_version_equal':3465,'source_logical_sha256':logical_hash(source),'reassembled_logical_sha256':logical_hash(reread),'reassembled_nbt':str(rebuilt_path.resolve()),'reassembled_nbt_sha256':fingerprint(rebuilt_path),'byte_identity_required':False,'scope':'Full state properties including air, block-entity payloads, ordinary entities, bounds and DataVersion compared after reading the four on-disk files. Offline reconstruction, not native fragment placement execution.'}
    (output/'ASSEMBLY.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    (output/'README.md').write_text('\n'.join(['# S1-10 分片放置契约','','共同原点O为完整源[0,0,0]。仅平移，完整性1，镜像none、旋转none；依序放置：','1. part-01.nbt：O+[0,0,0]，32×13×32。','2. part-02.nbt：O+[32,0,0]，29×13×32。','3. part-03.nbt：O+[0,0,32]，32×13×29。','4. part-04.nbt：O+[32,0,32]，29×13×29。','','每个分片包含其整个体积的空气；必须覆盖目标空气和非空气，不能跳空气或保留旧方块。无重叠，任何单片均不超过48。组装完成再检查整体附着/邻居存活，单片临时状态不作为验收。','ASSEMBLY.json记录各片SHA、48373格全部属性、2方块实体及0普通实体严格重组一致。reassembled-source.nbt仅供复核，逻辑一致无需gzip字节一致。','四向原生400tick测试针对完整总源，未声称实际分片部署已运行；正式存档未部署。','']),encoding='utf-8')
    print(json.dumps({'status':result['status'],'parts':len(pieces),'cells':len(covered),'source_sha256':result['source_nbt_sha256']}))


if __name__=='__main__':
    main()
