import hashlib
import json
import os
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-sparrow-evasion-effective-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')

def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def run(name,command,timeout=90):
    save(OUT/(name+'-command.json'),command)
    try:
        with (OUT/(name+'.log')).open('xb') as stream:result=subprocess.run(command,cwd=OUT,stdout=stream,stderr=subprocess.STDOUT,timeout=timeout,creationflags=subprocess.CREATE_NO_WINDOW)
        terminal={'exitCode':result.returncode,'timeout':False}
    except subprocess.TimeoutExpired:
        terminal={'exitCode':None,'timeout':True,'ownedSynchronousChildReapedBySubprocessRun':True}
    save(OUT/(name+'-terminal.json'),terminal);return terminal

owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
assert load(OUT/'preflight.json')['sourceCommit']=='fb3e345f81000d284375db418c6befbdb2c4b745';assert not (OUT/'audit-inputs.json').exists()
original=ROOT/'reference/forge-current.jar';built_path=WORK/'build/boss-zicai-curio-silence-v1-20261113/candidate-build-receipt.json';built=load(built_path);current=Path(built['jar'])
assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942' and sha(current)=='c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e'
context_path=WORK/'build/boss-flight-ban-v1/compile-context.json';classpath=[]
for entry in load(context_path)['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()=='.jar' and path.is_file():
        with zipfile.ZipFile(path) as bundle:
            if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    classpath.append(path)
assert all(path.exists() for path in classpath);cp=';'.join(str(path) for path in classpath)
for folder in ['classes','isolated-home','isolated-tmp']:(OUT/folder).mkdir()
source=WORK/'tests/SparrowEvasionCallsitesV1.java';prior_path=WORK/'reports/nonmanor-normal-reload-v3-5aff32b0.json';prior=load(prior_path);assert prior['ordinaryNoPlayerEighteenEntityReloadAccepted']
runtime_mods=sorted((Path(prior['attempt'])/'server/mods').glob('*.jar'));assert len(runtime_mods)==28
runtime_descriptor=OUT/'runtime-mod-archives.json';save(runtime_descriptor,[str(path) for path in runtime_mods])
inputs={str(path):sha(path) for path in [Path(__file__),source,original,current,built_path,context_path,prior_path,*runtime_mods,*[path for path in classpath if path.is_file()]]};save(OUT/'audit-inputs.json',inputs)
assert run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(OUT/'classes'),str(source)])['exitCode']==0
output=OUT/'callsites-and-mixin-targets.json'
assert run('audit',[str(JAVA/'java.exe'),'-Xmx768m','-Duser.home='+str(OUT/'isolated-home'),'-Djava.io.tmpdir='+str(OUT/'isolated-tmp'),'-cp',str(OUT/'classes')+';'+cp,'SparrowEvasionCallsitesV1',str(original),str(current),str(runtime_descriptor),str(output)])['exitCode']==0
for name,expected in inputs.items():assert sha(name)==expected,name
data=load(output)
summary={'originalEvasionCallsites':[row for row in data['directAndBootstrapCallsites'] if row['label']=='original' and 'SparrowHitEvasion' in row['target']],'currentEvasionCallsites':[row for row in data['directAndBootstrapCallsites'] if row['label']=='current' and 'SparrowHitEvasion' in row['target']],'mixinDeclarations':len(data['declaredRuntimeArchiveMixinTargets']),'unresolvedMixinDeclarations':len(data['unresolvedRuntimeArchiveMixinDeclarations']),'staticEvidenceOnly':True,'effectiveEquivalentBehaviorOrRuntimeDefectNotYetProven':True,'productChanged':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False};save(OUT/'summary.json',summary);print(json.dumps(summary),flush=True)
