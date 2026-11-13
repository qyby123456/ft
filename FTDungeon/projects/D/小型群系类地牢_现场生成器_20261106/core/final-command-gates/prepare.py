import hashlib
import json
import shutil
from pathlib import Path

ROOT=Path(__file__).resolve().parent
PROJECT=ROOT.parent.parent
INTEGRATION=PROJECT/'integration'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')


def inventory(root):
    return [{'path':str(path),'sha256':sha(path)} for path in sorted(root.rglob('*')) if path.is_file()]


def main():
    frozen=json.loads((INTEGRATION/'v6-final/progress.json').read_text(encoding='utf-8'))
    installed=json.loads((INTEGRATION/'v6-final/installed-progress.json').read_text(encoding='utf-8'))
    assert frozen['jarSha256']==installed['jarSha256']=='bab12080dae0e8fb2290f73b6988f40af2094d932a3303cdabc12aaac250263f'
    assert installed['anchor']==[1000,200,1000]
    source_snapshot=Path(frozen['snapshot'])
    shutil.copytree(source_snapshot,ROOT/'inputs/snapshot')
    records=inventory(source_snapshot)
    sources=Path(installed['root'])
    for turn in range(4):
        source=sources/('q'+str(turn))
        assert all(process.get('exitCode')==0 for process in installed['cases'][str(turn)]['processes'])
        records.extend(inventory(source/'world'))
        for name in ('world','libraries','mods'):
            shutil.copytree(source/name,ROOT/'installed'/('q'+str(turn))/name)
        for name in ('eula.txt','server.properties','result.json'):
            shutil.copyfile(source/name,ROOT/'installed'/('q'+str(turn))/name)
        mods=list((ROOT/'installed'/('q'+str(turn))/'mods').glob('*.jar'))
        assert len(mods)==1 and sha(mods[0])==frozen['jarSha256']
    frozen['snapshot']=str(ROOT/'inputs/snapshot')
    installed['root']=str(ROOT/'installed')
    installed['snapshot']=frozen['snapshot']
    save(ROOT/'progress.json',frozen)
    save(ROOT/'installed-progress.json',installed)
    (ROOT/'references').mkdir()
    scripts=('pause_io.py','commit_negatives.py','restart_saved.py','installed.py','readback.py')
    for name in scripts:
        source=INTEGRATION/'v6-final'/name
        records.append({'path':str(source),'sha256':sha(source)})
        shutil.copyfile(source,ROOT/'references'/name)
        text=source.read_text(encoding='utf-8')
        text=text.replace("sys.path.insert(0,str(HOME.parent));sys.path.insert(0,str(HOME.parent/'v5-diagnostics'))", "INTEGRATION=HOME.parent.parent/'integration'\nsys.path.insert(0,str(INTEGRATION));sys.path.insert(0,str(INTEGRATION/'v5-diagnostics'))")
        text=text.replace("sys.path.insert(0,str(HOME.parent))", "INTEGRATION=HOME.parent.parent/'integration'\nsys.path.insert(0,str(INTEGRATION))")
        text=text.replace("sys.path.insert(0,str(HOME.parent/'v5-diagnostics'))", "sys.path.insert(0,str(INTEGRATION/'v5-diagnostics'))")
        text=text.replace("HOME.parent/'staging/20261106_132015_129205'", "INTEGRATION/'staging/20261106_132015_129205'")
        if name=='readback.py':
            text=text.replace("sys.path.insert(0, str(Path(__file__).resolve().parents[1]))", "sys.path.insert(0, str(Path(__file__).resolve().parents[2]/'integration'))")
        (ROOT/name).write_text(text,encoding='utf-8')
    helpers=[INTEGRATION/'v5-diagnostics/faults.py',INTEGRATION/'integrate.py',INTEGRATION/'verify_saved_world.py',INTEGRATION/'v2/read_native_be.py',INTEGRATION/'staging/20261106_132015_129205/catalog/nbtio.py']
    records.extend({'path':str(path),'sha256':sha(path)} for path in helpers)
    previous=ROOT.parent/'final-runtime-faults/ready.json'
    old=json.loads(previous.read_text(encoding='utf-8'))
    for record in old['files']:
        assert sha(previous.parent/record['path'])==record['sha256']
    save(ROOT/'input-lock.json',{'files':records,'jarSha256':frozen['jarSha256'],'originalInstalledRoot':str(sources),'originalSnapshot':str(source_snapshot),'copiedInputs':inventory(ROOT/'inputs')+inventory(ROOT/'installed'),'frozen1115ReadySha256':sha(previous),'anchor':[1000,200,1000],'powerloss':False,'actualCommitted':False})
    print('COMMAND_GATES_INPUTS_LOCKED worlds=4 anchor=1000,200,1000 copiedFinalFTD5Reader=true',flush=True)


if __name__=='__main__':main()
