import json
import shutil
import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parent
CATALOG=ROOT.parent
PROJECT=CATALOG.parent
sys.path.insert(0,str(CATALOG))
from freeze_catalog import load,save,digest


def main():
    integration=PROJECT/'integration/v6-final'
    snapshot=integration/'staging/20261106_170038'
    failed=integration/'native-run/20261106_172659-boundary'
    if (ROOT/'project').exists():
        raise ValueError('prepared project already exists; do not replace')
    verified=[]
    for component,entry in load(snapshot/'snapshot-lock.json')['components'].items():
        for relative,expected in entry['files'].items():
            if digest(snapshot/component/relative)!=expected:
                raise ValueError('snapshot changed')
            verified.append({'component':component,'path':relative,'sha256':expected})
    shutil.copytree(snapshot/'runtime',ROOT/'project',ignore=lambda directory,names:{name for name in names if name in {'run','history','build','.gradle'}})
    shutil.copytree(failed/'q0/world',ROOT/'run/world')
    shutil.copytree(failed/'verification',ROOT/'failed-verification')
    shutil.copyfile(failed/'q0/native.log',ROOT/'failed-native.log')
    shutil.copyfile(snapshot/'snapshot-lock.json',ROOT/'final-snapshot-lock.json')
    for name in ('contract.json','certificate.py','native-input.schema.json','ready.json'):
        source=CATALOG/'full-width-certificate-v6'/name
        destination=ROOT/'inputs'/name
        destination.parent.mkdir(parents=True,exist_ok=True)
        shutil.copyfile(source,destination)
    shutil.copyfile(CATALOG/'continuous-native-review/finite-certificate.json',ROOT/'inputs/finite-certificate.json')
    shutil.copyfile(CATALOG/'continuous-native-review/inputs/native-shapes.json',ROOT/'inputs/native-shapes.json')
    fixture=ROOT/'verification/test-resources/data/ftbd_catalog_boundary/structures/s8_05.nbt'
    fixture.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(snapshot/'catalog/nine-room-package-v5/data/biomedungeon/structures/s8_05.nbt',fixture)
    lock=[]
    for folder in ('project','run/world','failed-verification','inputs'):
        for path in sorted((ROOT/folder).rglob('*')):
            if path.is_file():
                lock.append({'path':path.relative_to(ROOT).as_posix(),'sha256':digest(path),'bytes':path.stat().st_size})
    runtime=load(snapshot/'runtime/ready.json')
    save(ROOT/'input-lock.json',{'runtimeJarSha256':runtime['artifactSha256'],'snapshot':str(snapshot),'failedRun':str(failed),'sourceSnapshotFilesVerified':verified,'copiedFiles':lock,'copiedWorldScope':'independent catalog copy of failed isolated boundary run, not integration world','originalFilesModified':False})
    save(ROOT/'progress.json',{'component':'catalog-final-native-boundary','status':'OWNED_SNAPSHOT_AND_WORLD_PREPARED','runtimeJarSha256':runtime['artifactSha256'],'nativeWorldStatus':'NOT_STARTED','oneGameTestServerAtATime':True})
    print('PREPARED own world/project; copiedFiles='+str(len(lock)))


if __name__=='__main__':
    main()
