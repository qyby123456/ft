import copy
import hashlib
import json
from pathlib import Path,PurePosixPath
import zipfile
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/boss-independent-material-local-resource-closure-v2-20261114.json'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(data):return hashlib.sha256(data).hexdigest()
build_path=WORK/'build/boss-independent-material-cumulative-v1-20261114/candidate-build-receipt.json';built=load(build_path);candidate=Path(built['jar']);original=ROOT/'reference/forge-current.jar';assert sha(candidate.read_bytes())==built['jarSha256'] and sha(original.read_bytes())=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
owner=load(ROOT/'WRITE_OWNER.json');assert owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited'];assert not OUT.exists();rows={};external=set();adapters=[]
with zipfile.ZipFile(candidate) as current,zipfile.ZipFile(original) as old:
    def adapt(value,path):
        if isinstance(value,dict):
            for key,item in value.items():
                if key=='loader' and item in ['forge:separate_transforms','forge:obj']:
                    value[key]=item.replace('forge:','neoforge:',1);adapters.append({'path':path+'/loader','originalLoader':item,'nativeLoader':value[key]})
                else:adapt(item,path+'/'+key)
        elif isinstance(value,list):
            for index,item in enumerate(value):adapt(item,path+'/'+str(index))
    def register(name,kind):
        assert name.startswith('assets/ftboss/') and '..' not in PurePosixPath(name).parts and name in current.namelist() and name in old.namelist(),name
        payload=current.read(name);previous=old.read(name);exact=payload==previous
        if kind=='model':
            expected=json.loads(previous);adapt(expected,name);assert json.dumps(json.loads(payload),sort_keys=True,separators=(',',':'))==json.dumps(expected,sort_keys=True,separators=(',',':')),name
        else:assert exact,name
        rows[name]={'path':name,'kind':kind,'bytes':len(payload),'sha256':sha(payload),'originalForgeSha256':sha(previous),'exactOriginalBytes':exact,'onlyExplicitNativeLoaderJsonAdaptation':not exact and kind=='model'}
        return payload
    def texture(value):
        if value.startswith('#'):return
        namespace,path=value.split(':',1) if ':' in value else ('minecraft',value)
        if namespace=='ftboss':
            name='assets/ftboss/textures/'+path+( '' if path.endswith('.png') else '.png')
            if name not in rows:register(name,'texture')
        else:external.add(value)
    def material(name):
        if name in rows:return
        payload=register(name,'material-library').decode('utf-8-sig')
        for line in payload.splitlines():
            if line.strip().startswith('map_Kd '):texture(line.split()[-1])
    def obj(name):
        if name in rows:return
        payload=register(name,'mesh').decode('utf-8-sig')
        for line in payload.splitlines():
            if line.strip().startswith('mtllib '):
                for reference in line.split()[1:]:material(str(PurePosixPath(name).parent/reference))
    def references(value):
        if isinstance(value,dict):
            for key,item in value.items():
                if key=='parent' and isinstance(item,str):model(item)
                elif key=='model' and isinstance(item,str) and item.startswith('ftboss:') and item.endswith('.obj'):obj('assets/ftboss/'+item.split(':',1)[1])
                elif key=='textures' and isinstance(item,dict):
                    for reference in item.values():texture(reference)
                else:references(item)
        elif isinstance(value,list):
            for item in value:references(item)
    def model(reference):
        namespace,path=reference.split(':',1) if ':' in reference else ('minecraft',reference)
        if namespace!='ftboss':external.add(reference);return
        name='assets/ftboss/models/'+path+'.json'
        if name in rows:return
        references(json.loads(register(name,'model')))
    for material_id in ['frost','butcher','tea','wine','cheese','capacitor','spore','apple','shepherd','tide']:model('ftboss:item/'+material_id+'_material')
assert len([row for row in rows.values() if row['kind']=='texture'])==10
receipt={'candidate':str(candidate),'candidateJarSha256':built['jarSha256'],'originalForgeJarSha256':sha(original.read_bytes()),'originalMaterialIds':10,'localResourceDependencyFiles':list(rows.values()),'explicitNativeLoaderAdaptations':adapters,'externalVanillaReferences':sorted(external),'allMaterialDependenciesCompleteWithOnlyProvenNativeLoaderAdapters':True,'allMeshTextureAndMaterialBytesExactlyOriginalForge':True,'strictByteEqualityPreflightRejected':{'path':'assets/ftboss/models/item/frost_material.json','reason':'Existing proven forge:separate_transforms to neoforge:separate_transforms adapter; no gameplay or resource file changed by audit'},'allExcludedResourcePayloadsRemainLocalOnly':True,'modelOrMusicUploaded':False,'pathAndHashMetadataOnly':True,'actualVisualRenderingAcceptanceClaimed':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False};OUT.write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({'localClosureFiles':len(rows),'textures':10,'nativeLoaderAdapters':len(adapters),'meshAndTextureBytesExactlyOriginal':True,'uploadedResourcePayloads':False}),flush=True)
