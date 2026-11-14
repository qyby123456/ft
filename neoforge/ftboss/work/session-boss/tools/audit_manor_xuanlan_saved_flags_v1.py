import gzip,hashlib,io,json,struct,uuid,zlib
from pathlib import Path
import nbtlib
ROOT=Path(__file__).resolve().parents[3];WORK=ROOT/'work/session-boss';OUT=WORK/'reports/manor-xuanlan-saved-flags-v1-20261114'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def simple(tag):
    if isinstance(tag,nbtlib.Compound):return {str(key):simple(value) for key,value in tag.items()}
    if isinstance(tag,(nbtlib.List,nbtlib.IntArray,nbtlib.LongArray,nbtlib.ByteArray)):return [simple(value) for value in tag]
    if isinstance(tag,(nbtlib.Float,nbtlib.Double)):return float(tag)
    if isinstance(tag,(nbtlib.Byte,nbtlib.Short,nbtlib.Int,nbtlib.Long)):return int(tag)
    if isinstance(tag,nbtlib.String):return str(tag)
    return int(tag)
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['noFurtherFormalWritesOrDeployment'];native_path=WORK/'reports/manor-death-cancel-native-v2-8449e49e.json';native=load(native_path);assert native['terminal']['clientProcessReaped'] and native['terminal']['integratedWorldCompleteSave'];target=uuid.UUID(native['probe']['lastAction']['after']['uuid']);world=Path(native['attempt'])/'game/saves/manor-death-cancel-native-v2';regions=sorted((world/'entities').glob('*.mca'));assert regions and world.resolve().is_relative_to((WORK/'build').resolve());inputs={str(path):sha(path) for path in [Path(__file__),native_path,*regions]};matches=[];entity_count=0
for path in regions:
    raw=path.read_bytes();assert len(raw)>=8192
    for index in range(1024):
        location=int.from_bytes(raw[index*4:index*4+4],'big');sector=location>>8
        if not sector:continue
        offset=sector*4096;length=int.from_bytes(raw[offset:offset+4],'big');assert 1<length<=16*1024*1024;compression=raw[offset+4];assert not compression&128;data=raw[offset+5:offset+4+length];assert len(data)==length-1;payload=gzip.decompress(data) if compression==1 else zlib.decompress(data) if compression==2 else data if compression==3 else None;assert payload is not None;root=nbtlib.File.parse(io.BytesIO(payload));entities=root.get('Entities',[])
        for entity in entities:
            entity_count+=1
            if 'UUID' not in entity:continue
            actual=uuid.UUID(bytes=b''.join(struct.pack('>i',int(value)) for value in entity['UUID']))
            if actual!=target:continue
            scalars={str(key):simple(value) for key,value in entity.items() if isinstance(value,(nbtlib.Byte,nbtlib.Short,nbtlib.Int,nbtlib.Long,nbtlib.Float,nbtlib.Double,nbtlib.String))};vectors={key:simple(entity[key]) for key in ['Pos','Motion','Rotation'] if key in entity};nested={str(key):simple(value) for key,value in entity.items() if isinstance(value,nbtlib.Compound) and any(name in key.lower() for name in ['forge','ftboss','xuanlan','manor'])};matches.append({'uuid':str(actual),'region':str(path),'regionChunkIndex':index,'scalarFields':scalars,'vectors':vectors,'logicalCompoundFields':nested,'allSavedTopLevelKeys':sorted(map(str,entity.keys()))})
assert len(matches)==1 and all(sha(path)==digest for path,digest in inputs.items());assert not OUT.exists();OUT.mkdir();receipt={'target':matches[0],'savedEntitiesRead':entity_count,'inputHashes':inputs,'savedWorldReadOnly':True,'frozenWorldFilesUnchanged':True,'phaseOrIdentityOrGameplayWrites':False,'actualNewJvmWorldReloadAcceptanceClaimed':False,'modelOrMusicUploaded':False,'productChanged':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False};(OUT/'receipt.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({'target':matches[0],'savedEntitiesRead':entity_count,'readOnly':True},ensure_ascii=False),flush=True)
