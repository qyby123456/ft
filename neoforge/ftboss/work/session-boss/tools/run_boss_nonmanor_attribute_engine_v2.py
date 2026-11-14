import hashlib
import json
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
PARENT=WORK/'build/boss-nonmanor-finale-effective-v1-20261114'
OUT=PARENT/'native-engine-v2'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):
    with (OUT/name).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and not owner['activeRuntimePids']
prior_inputs=load(PARENT/'audit-inputs.json')
for name,expected in prior_inputs.items():assert sha(name)==expected,name
first=load(PARENT/'native-attribute-engine.json');assert first['checks']==0 and first['failures']==1 and 'Not bootstrapped' in first['failure']
OUT.mkdir();classes=OUT/'classes';classes.mkdir()
for folder in ['isolated-home','isolated-tmp']:(OUT/folder).mkdir()
source=WORK/'tests/NonManorAttributeEngineV2.java';inputs=dict(prior_inputs);inputs.update({str(Path(__file__)):sha(__file__),str(source):sha(source)})
save('inputs-before.json',inputs)
prior_compile=load(PARENT/'compile-command.json');classpath=prior_compile[prior_compile.index('-cp')+1]
compile_command=[prior_compile[0],'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(classes),str(source)]
save('compile-command.json',compile_command)
with (OUT/'compile.log').open('xb') as stream:compiled=subprocess.run(compile_command,cwd=OUT,stdout=stream,stderr=subprocess.STDOUT,timeout=60,creationflags=subprocess.CREATE_NO_WINDOW)
save('compile-terminal.json',{'exitCode':compiled.returncode});assert compiled.returncode==0
command=[str(Path(prior_compile[0]).with_name('java.exe')),'-Xmx256m','-Duser.home='+str(OUT/'isolated-home'),'-Djava.io.tmpdir='+str(OUT/'isolated-tmp'),'-cp',str(classes)+';'+classpath,'NonManorAttributeEngineV2',str(OUT/'native-attribute-engine.json')]
save('runtime-command.json',command)
with (OUT/'runtime.log').open('xb') as stream:result=subprocess.run(command,cwd=OUT,stdout=stream,stderr=subprocess.STDOUT,timeout=60,creationflags=subprocess.CREATE_NO_WINDOW)
save('runtime-terminal.json',{'exitCode':result.returncode})
for name,expected in inputs.items():assert sha(name)==expected,name
report=load(OUT/'native-attribute-engine.json') if (OUT/'native-attribute-engine.json').exists() else None
receipt={'originalV1FailureRetained':True,'differenceFromV1':'Only normal SharedConstants version detection and public Bootstrap.bootStrap before unchanged arithmetic checks. No game or Boss state modified.','allFrozenInputsUnchanged':True,'runtimeExitCode':result.returncode,'nativeAttributeArithmeticAccepted':result.returncode==0 and report is not None and report['failures']==0,'checks':report['checks'] if report else 0,'actualBossAdmissionOrFullRuntimeAccepted':False,'difficultyOrUnlockStateModified':False,'productChanged':False,'installable':False,'deployed':False}
save('receipt.json',receipt);print(json.dumps(receipt),flush=True)
