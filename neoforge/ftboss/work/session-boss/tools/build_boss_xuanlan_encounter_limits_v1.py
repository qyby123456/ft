import argparse
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-xuanlan-encounter-limits-v1'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
parser=argparse.ArgumentParser();parser.add_argument('baseline',type=Path);arguments=parser.parse_args()
baseline=load(arguments.baseline)
assert baseline['probe']['failures']==0 and baseline['probe']['actualOriginal15CapMissingDemonstrated']
assert baseline['terminal']['exitCode']==0 and baseline['terminal']['completeSave'] and baseline['terminal']['inputHashesUnchanged']
for name,wanted in baseline['inputHashes'].items():assert sha(name)==wanted,name
owner=load(ROOT/'WRITE_OWNER.json');assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns'] and owner['activeWriterThreadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
previous_path=WORK/'releases/boss-core-stage14-client-resources-20261113/deployment-receipt.json';previous=load(previous_path)
base=WORK/'releases/boss-core-stage14-client-resources-20261113/ftboss-boss-core-1.21.1-stage14-resources.jar'
assert sha(base)==sha(previous['target'])==previous['deployedSha256']=='6c8ab5192bd95aa9ec8684211b37c0313a11fae81d19357b350c043ae468e50c'
assert not OUT.exists();OUT.mkdir();(OUT/'classes').mkdir()
source=WORK/'proposal-xuanlan-encounter-limits/net/farmingtales/ftboss/boss/ai/XuanlanEncounterLimits.java'
context_path=WORK/'build/boss-flight-ban-v1/compile-context.json';context=load(context_path)
sdk=[]
for name in context['classpath'].split(';'):
    path=Path(name)
    if path.suffix.lower()!='.jar':continue
    with zipfile.ZipFile(path) as bundle:
        if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    sdk.append(str(path))
command=[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',str(base)+';'+';'.join(sdk),'-d',str(OUT/'classes'),str(source)]
save(OUT/'compile-command.json',command)
with (OUT/'compile.log').open('wb') as stream:result=subprocess.run(command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT)
save(OUT/'compile-terminal.json',{'exitCode':result.returncode});assert result.returncode==0
changed=list((OUT/'classes').rglob('*.class'));assert len(changed)==1
entry='net/farmingtales/ftboss/boss/ai/XuanlanEncounterLimits.class';payload=(OUT/'classes'/entry).read_bytes()
jar=OUT/'ftboss-boss-core-1.21.1-stage15-xuanlan-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as original,zipfile.ZipFile(jar,'x',compression=zipfile.ZIP_DEFLATED) as current:
    assert entry not in original.namelist()
    for info in original.infolist():current.writestr(info,original.read(info.filename))
    current.writestr(entry,payload)
with zipfile.ZipFile(base) as original,zipfile.ZipFile(jar) as current:
    assert current.testzip() is None and set(current.namelist())-set(original.namelist())=={entry}
    assert all(original.read(name)==current.read(name) for name in original.namelist())
    count=sum(name.endswith('.class') for name in original.namelist());assert count==1443
    assert not any(name.startswith('net/farmingtales/ftboss/butcherskills/ButcherSummonFx') for name in current.namelist())
inputs={str(path):sha(path) for path in [Path(__file__),arguments.baseline.resolve(),source,base,previous_path,context_path]}
save(OUT/'candidate-build-receipt.json',{'jar':str(jar),'jarSha256':sha(jar),'baseJar':str(base),'baseJarSha256':sha(base),'proposal':str(source.parent),'originalNativeBaseline':str(arguments.baseline.resolve()),'existingEntriesByteIdentical':True,'unchangedExistingClassCount':count,'addedClass':entry,'originalRule':'variant6;15 live wheels per actual owner across level;highest admission;every20 native ticks keep oldest15;support owner/fight exclusion','stage13Included':False,'inputHashes':inputs,'installable':False,'deployed':False,'fullBossMigrationComplete':False,'weaponsPaused':True})
print(json.dumps({'jar':str(jar),'sha256':sha(jar),'addedClasses':1,'all1443ExistingClassesAndResourcesUnchanged':True,'installable':False}),flush=True)
