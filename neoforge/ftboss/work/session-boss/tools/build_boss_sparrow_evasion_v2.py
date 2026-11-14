import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-sparrow-evasion-v2-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path):
    with path.open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=90,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.log')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.log'));return result.stdout.decode('utf-8',errors='replace')
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
baseline_path=WORK/'reports/manor-damage-context-v1-closeout-20261114.json';baseline=load(baseline_path);assert baseline['scopedNativeAcceptance'] and baseline['actualHealthAfter']<baseline['actualHealthBefore']
built=WORK/'build/boss-manor-damage-context-v1-20261114/candidate-build-receipt.json';base=Path(load(built)['jar']);assert sha(base)=='78caa46ef163c4f3ee9cc3d1a90d5ee58e1bbf4d791f213fca390986f987692f'
assert not OUT.exists();OUT.mkdir();classes=OUT/'classes';classes.mkdir();proof_classes=OUT/'proof-classes';proof_classes.mkdir()
prior=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');classpath=prior[prior.index('-cp')+1];classpath=classpath.replace(str(WORK/'build/boss-manor-damage-context-v1-20261114/classes')+';','');classpath=str(base)+';'+classpath
sources=sorted((WORK/'src/sparrow-evasion-v2').rglob('*.java'));proof_source=WORK/'tests/SparrowHitEvasionComposerV2.java';mapping=WORK/'build/boss-sparrow-evasion-platform-audit-v1-20261114/mapping-proof.json';original=ROOT/'reference/forge-current.jar';loaded=WORK/'build/manor-damage-context-native-v1-820f458d/captured-defined-classes/net_farmingtales_ftboss_entity_EasterBossEntity.class'
run('compile-installers',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(classes),*[str(path) for path in sources]])
run('compile-composer',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',str(classes)+';'+classpath,'-d',str(proof_classes),str(proof_source)])
proof=run('original-port-proof',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(proof_classes)+';'+str(classes)+';'+classpath,'SparrowHitEvasionComposerV2',str(original),str(mapping),str(classes),str(loaded)])
assert proof.startswith('failures=0;') and 'integrationHooks=3;' in proof
changes={path.relative_to(classes).as_posix():path.read_bytes() for path in classes.rglob('*.class')};assert len(changes)==6
jar=OUT/'ftboss-boss-core-1.21.1-stage30-sparrow-hit-evasion-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar,'x',compression=zipfile.ZIP_DEFLATED) as after:
    for info in before.infolist():after.writestr(info,changes.get(info.filename,before.read(info.filename)))
    for name,payload in sorted(changes.items()):
        if name not in before.namelist():after.writestr(name,payload)
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar) as after:
    assert after.testzip() is None
    changed=[name for name in before.namelist() if before.read(name)!=after.read(name)];added=sorted(set(after.namelist())-set(before.namelist()));assert set(changed)=={'net/farmingtales/ftboss/boss/ai/NativeManorFinalDamageInstaller.class','net/farmingtales/ftboss/boss/ai/NativeManorDefenseInstaller.class'} and len(added)==4
inputs={str(path):sha(path) for path in [Path(__file__),*sources,proof_source,mapping,original,base,built,baseline_path,loaded]}
save('candidate-build-receipt.json',{'jar':str(jar),'jarSha256':sha(jar),'baseJar':str(base),'baseJarSha256':sha(base),'changedEntries':changed,'addedEntries':added,'originalRulesByteIdentical':True,'allOtherBaseEntriesByteIdentical':True,'proof':proof.strip(),'inputHashes':inputs,'scope':'Complete mapped original SparrowHitEvasion helper/state/rules and three original integration hooks installed after existing Manor defense adaptation. No algorithm simplification.','nativeAcceptanceRequired':True,'candidateNativeAccepted':False,'sparrowEvasionPortAccepted':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'modelsAndMusicLocalOnly':True})
print(json.dumps({'candidateSha256':sha(jar),'addedClasses':len(added),'changedClasses':len(changed),'proof':proof.strip(),'nativeAcceptanceRequired':True}),flush=True)
