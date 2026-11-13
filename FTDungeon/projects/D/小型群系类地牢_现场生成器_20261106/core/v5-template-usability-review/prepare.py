import hashlib
import json
import re
import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parent
PROJECT=ROOT.parent.parent
PACKAGE=PROJECT/'catalog/s8-07-doorway-review-v5/nine-room-package-v5'
ENGINE=PROJECT/'runtime/next-commit/src/main/java/cn/ft/biomedungeon/runtime/UnifiedEngine.java'
sys.dont_write_bytecode=True
sys.path.insert(0,str(PROJECT/'catalog'))
from nbtio import Structure


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def version():
    return re.search(r'static final String VERSION\s*=\s*"([^"]+)"',ENGINE.read_text(encoding='utf-8')).group(1)


def main():
    sealed=ROOT.parent/'envelope-usability-review'
    ready=load(sealed/'ready.json')
    frozen=[]
    for record in ready['files']:
        path=sealed/record['path']
        assert sha(path)==record['sha256'],path
        frozen.append({'path':str(path),'sha256':sha(path)})
    frozen.append({'path':str(sealed/'ready.json'),'sha256':sha(sealed/'ready.json')})
    manifest=PACKAGE/'candidate-manifest.json'
    snapshot=manifest.read_bytes()
    (ROOT/'inputs/candidate-manifest.json').write_bytes(snapshot)
    payload=json.loads(snapshot)
    assert len(payload['templates'])==9
    assert len({template['id']for template in payload['templates']})==9
    rows=[]
    resources=[]
    for template in sorted(payload['templates'],key=lambda item:item['id']):
        nbt=PACKAGE/'data/biomedungeon/structures'/(template['id']+'.nbt')
        assert sha(nbt)==template['sha256'],nbt
        structure=Structure.read(nbt)
        assert list(structure.size)==[template['size'][axis]for axis in ('x','y','z')]
        assert template['accepted'] is False
        resources.append({'path':str(nbt),'sha256':sha(nbt),'id':template['id'],'accepted':False})
        for port in template['ports']:
            fields=[template['id'],template['biome'],template['resourceId'],template['sha256'],template['role'],
                    *[template['size'][axis]for axis in ('x','y','z')],port['id'],
                    *[port['feet'][axis]for axis in ('x','y','z')],port['facing'],port['width'],port['height'],port['medium']]
            rows.append('\t'.join(map(str,fields)))
    exact_version=version()
    (ROOT/'inputs/runtime-version.txt').write_text(exact_version,encoding='utf-8')
    (ROOT/'inputs/UnifiedEngine.java').write_bytes(ENGINE.read_bytes())
    (ROOT/'inputs/real-nine-ports.tsv').write_text('\n'.join(rows)+'\n',encoding='utf-8')
    data={'runtimeVersion':exact_version,'runtimeSource':str(ENGINE),'runtimeSourceSha256AtStart':sha(ENGINE),
          'manifest':str(manifest),'manifestSha256':sha(manifest),'sourceResources':resources,'frozenInventory':frozen,
          'frozenPlannerReadySha256':sha(sealed/'ready.json'),'frozenPlannerJarSha256':ready['artifacts'][0]['sha256'],
          'scope':'Exact new IDs/hashes plus current VERSION; eight originals and one unaccepted derived template, not nine originals',
          'productionCoreSourceChanged':False,'worldTests':'NOT_RUN'}
    (ROOT/'inputs/input-lock.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('PASS frozen397 inventory and exact9 v5 NBT byte hashes/dimensions; VERSION='+exact_version)
    print('DERIVED entrance='+next(template['id'] for template in payload['templates']if template['role']=='entrance'))


if __name__=='__main__':
    main()
