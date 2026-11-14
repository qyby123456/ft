import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-manor-native-events-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path):
    with path.open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=90,creationflags=subprocess.CREATE_NO_WINDOW)
    (OUT/(name+'.log')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.log'))
    return result.stdout.decode('utf-8')
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids']
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
assert not OUT.exists();OUT.mkdir();classes=OUT/'classes';classes.mkdir();proofs=OUT/'proof-classes';proofs.mkdir()
base_receipt=WORK/'build/boss-sparrow-evasion-v2-20261114/candidate-build-receipt.json';base=Path(load(base_receipt)['jar']);assert sha(base)=='877309d1ff2332ab6d407cd1b456d39987109d17e8f6a8e98184abe55703c25b'
arguments=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(base)+';'+arguments[arguments.index('-cp')+1]
source_root=WORK/'src/manor-native-events-v1/net/farmingtales/ftboss/boss/ai';installer=source_root/'NativeManorDamageBridgeInstaller.java';composer=WORK/'tests/ManorNativeDamageBridgeComposerV1.java';sources=sorted(source_root.glob('*.java'));assert len(sources)==4
run('compile-pins',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),*[str(path) for path in sources],str(composer)])
actual=WORK/'build/sparrow-hit-evasion-native-v4-ba1eddca/captured-defined-classes/net_farmingtales_ftboss_entity_EasterBossEntity.class'
pins=dict(line.split('=',1) for line in run('read-accepted-pins',[str(JAVA/'java.exe'),'-cp',str(classes)+';'+cp,'ManorNativeDamageBridgeComposerV1','pins',str(actual),str(base)]).splitlines());save('accepted-pins.json',pins)
helper=ROOT/'work/session-lease/tools/reactivate_goal_after_boss_stage12.py';namespace={'__file__':str(helper),'__name__':'replace_only'};exec(compile(helper.read_text(encoding='utf-8').split('assert not OUT.exists()')[0],str(helper),'exec'),namespace)
before=installer.read_text(encoding='utf-8');after=before
for marker,key in [('PIN_MANOR','manorHurt'),('PIN_REDUCE','ftboss$reduceManorDamage'),('PIN_SETTLE','settle')]:assert len(pins[key])==64 and marker in after;after=after.replace(marker,pins[key])
namespace['replace_file'](installer,before,after)
run('compile-final',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),*[str(path) for path in sources],str(composer)])
proof=run('structural-proof',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'ManorNativeDamageBridgeComposerV1','prove',str(actual),str(base),str(OUT)]).strip();assert proof.startswith('failures=0;')
changes={path.relative_to(classes).as_posix():path.read_bytes() for path in classes.rglob('*.class') if path.relative_to(classes).as_posix().startswith('net/farmingtales/ftboss/')};changes['net/farmingtales/ftboss/boss/ai/ManorFinalDamage.class']=(OUT/'ManorFinalDamage.class').read_bytes()
jar=OUT/'ftboss-boss-core-1.21.1-stage31-manor-native-events-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar,'x',compression=zipfile.ZIP_DEFLATED) as after:
    for info in before.infolist():after.writestr(info,changes.pop(info.filename,before.read(info.filename)))
    for name,data in sorted(changes.items()):after.writestr(name,data)
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar) as after:
    assert after.testzip() is None
    changed=[name for name in before.namelist() if before.read(name)!=after.read(name)];added=sorted(set(after.namelist())-set(before.namelist()))
    assert set(changed)=={'net/farmingtales/ftboss/boss/ai/NativeManorFinalDamageInstaller.class','net/farmingtales/ftboss/boss/ai/ManorFinalDamage.class'} and len(added)==4
save('candidate-build-receipt.json',{'jar':str(jar),'jarSha256':sha(jar),'baseJar':str(base),'baseJarSha256':sha(base),'changedEntries':changed,'addedEntries':added,'allOtherBaseEntriesByteIdentical':True,'proof':proof,'inputHashes':{str(path):sha(path) for path in [Path(__file__),composer,actual,base_receipt,*sources]},'scope':'Full Boss-only native Incoming/Pre/Post context bridge, original settlement and reversible exact Boss instrumentation. Native acceptance pending.','candidateNativeAccepted':False,'nativeDamageCompatibilityAccepted':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False})
print(json.dumps({'jarSha256':sha(jar),'proof':proof,'changedEntries':len(changed),'addedEntries':len(added),'nativeAcceptancePending':True}),flush=True)
