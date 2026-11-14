import copy,gzip,hashlib,io,json,struct,sys,uuid,zlib
from pathlib import Path
import nbtlib
ROOT=Path(__file__).resolve().parents[3];WORK=ROOT/'work/session-boss'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def entity_uuid(entity):return str(uuid.UUID(bytes=b''.join(struct.pack('>i',int(value)) for value in entity['UUID'])))
def verify(saved,expected):
    assert saved['uuid']==expected['uuid'] and saved['type']=='ftboss:tea_tree'
    assert saved['currentFinale'] and saved['prepared'] and saved['originalFinale'] and saved['originalTeaFinale']
    assert saved['originalBases']['minecraft:generic.max_health']==340 and saved['originalBases']['minecraft:generic.attack_damage']==3.5
    assert saved['attributes']['minecraft:generic.max_health']['base']==expected['maximumHealthBase']==680
    assert saved['attributes']['minecraft:generic.attack_damage']['base']==expected['attackBase']==7
    assert all('ftboss:non_manor_finale_double' not in item['savedModifierIds'] for item in saved['attributes'].values())
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
native_path=Path(sys.argv[1]).resolve();native=load(native_path);assert native['actualOriginalTeaFinaleCombatAccepted'] and native['probe']['failures']==0 and all(native['terminal'][key] for key in ['clientProcessReaped','integratedWorldCompleteSave','inputHashesUnchanged'])
source=WORK/'tests/TeaOriginalFinaleNativeV2.java';assert 'createFreshLevel("tea-original-finale-native-v2"' in source.read_text(encoding='utf-8') and native['inputHashes'][str(source)]==sha(source)
world=Path(native['attempt'])/'game/saves/tea-original-finale-native-v2';assert world.resolve().is_relative_to((WORK/'build').resolve());regions=sorted((world/'entities').glob('*.mca'));assert regions
cases=native['probe']['teaCases'];assert len(cases)==3;target=cases[2];assert target['saveOnlyActualAdmission'] and target['alive'] and target['originalTeaFinale'] and target['naturalTicksObserved']>=40;expected={target['uuid']:target}
inputs={str(path):sha(path) for path in [Path(__file__),source,native_path,*regions]};found={};entities_read=0
for path in regions:
    raw=path.read_bytes()
    if not raw:continue
    assert len(raw)>=8192
    for index in range(1024):
        location=int.from_bytes(raw[index*4:index*4+4],'big');sector=location>>8
        if not sector:continue
        offset=sector*4096;length=int.from_bytes(raw[offset:offset+4],'big');assert 1<length<=16*1024*1024;compression=raw[offset+4];assert not compression&128;data=raw[offset+5:offset+4+length];assert len(data)==length-1;payload=gzip.decompress(data) if compression==1 else zlib.decompress(data) if compression==2 else data if compression==3 else None;assert payload is not None;root=nbtlib.File.parse(io.BytesIO(payload))
        for entity in root.get('Entities',[]):
            entities_read+=1
            if 'UUID' not in entity:continue
            identity=entity_uuid(entity)
            if identity not in expected:continue
            assert identity not in found
            data=entity['NeoForgeData'];assert isinstance(data,nbtlib.Compound)
            keys=[key for key in ['Attributes','attributes'] if key in entity];assert len(keys)==1,(identity,list(entity))
            attributes={}
            for attribute in entity[keys[0]]:
                name=str(attribute['Name'] if 'Name' in attribute else attribute['id']);base=float(attribute['Base'] if 'Base' in attribute else attribute['base']);assert name not in attributes
                modifier_list=attribute.get('Modifiers',attribute.get('modifiers',[]));modifier_ids=[str(modifier['id'] if 'id' in modifier else modifier.get('Name','')) for modifier in modifier_list]
                attributes[name]={'base':base,'savedModifierIds':modifier_ids}
            found[identity]={'uuid':identity,'case':expected[identity]['case'],'type':str(entity['id']),'region':str(path),'chunkIndex':index,'originalTeaFinale':bool(data.get('ftboss:saya_finale',0)),'currentFinale':bool(data.get('ftboss:non_manor_finale',0)),'prepared':bool(data.get('ftboss:regular_finale_prepared',0)),'originalFinale':bool(data.get('ftboss:regular_finale',0)),'originalBases':{str(key)[len('ftboss:regular_finale_base/'):]:float(value) for key,value in data.items() if str(key).startswith('ftboss:regular_finale_base/')},'attributes':attributes}
assert set(found)==set(expected),(set(expected)-set(found))
for identity,saved in found.items():verify(saved,expected[identity])
first=next(row for row in found.values() if row['originalFinale']);controls=0
bad=copy.deepcopy(first);bad['originalBases']['minecraft:generic.attack_damage']+=1
try:verify(bad,expected[first['uuid']])
except AssertionError:controls+=1
bad=copy.deepcopy(first);bad['prepared']=False
try:verify(bad,expected[first['uuid']])
except AssertionError:controls+=1
assert controls==2 and all(sha(path)==digest for path,digest in inputs.items())
out=WORK/'reports'/('tea-original-finale-saved-v2-'+Path(native['attempt']).name.rsplit('-',1)[1]+'-20261114');assert not out.exists();out.mkdir();receipt={'actualOriginalTeaFinaleStatePersistedAcrossCompleteSave':True,'actualAliveFinaleTeaSavedExactlyOnce':True,'actualSavedCaseCount':len(found),'scope':'Actual final live Tea case only; original challenge/prepared/bases saved. No new JVM reload or transient modifier persistence is claimed.','savedCases':sorted(found.values(),key=lambda row:row['case']),'savedEntitiesRead':entities_read,'negativeControls':controls,'inputHashes':inputs,'savedWorldReadOnly':True,'frozenWorldFilesUnchanged':True,'actualNewJvmWorldReloadAccepted':False,'actualLegacySavedWorldMigrationAccepted':False,'authenticated':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False};(out/'receipt.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({'actualAliveFinaleTeaSaved':True,'savedOriginalBasesAndPreparedFlags':True,'negativeControls':controls,'newJvmReloadAccepted':False,'receipt':str(out/'receipt.json')}),flush=True)
