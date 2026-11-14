import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-youzi-original-summon-v3-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.log')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.log'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
base=Path(owner['latestScopedBossCandidate']);assert sha(base)=='a49f1772a60d72a4240eb3f293b29fb38629a2112ad326120e81377eeeeca1ec'
original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
mapping=WORK/'build/boss-manor-original-helpers-platform-audit-v1-20261114/mapping-proof.json';source=WORK/'tests/ManorYouziOriginalSummonComposerV3.java';cp_input=WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json';prior=load(cp_input);cp=str(base)+';'+prior[prior.index('-cp')+1]
inputs=[Path(__file__),base,original,mapping,source,cp_input];hashes={str(path):sha(path) for path in inputs}
assert not OUT.exists();OUT.mkdir();classes=OUT/'proof-classes';classes.mkdir()
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source)])
prepared=run('prepare-installer',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'ManorYouziOriginalSummonComposerV3',str(original),str(base),str(mapping),str(OUT)])
installer_entry='net/farmingtales/ftboss/boss/ai/NativeAnimationEmitterInstaller.class'
installer_target=classes/installer_entry;installer_target.parent.mkdir(parents=True,exist_ok=True);installer_target.write_bytes((OUT/'NativeAnimationEmitterInstaller.class').read_bytes())
actual=WORK/'build/manor-raw-guard-boundary-fixed-v1-241e09ea/captured-defined-classes/net_farmingtales_ftboss_entity_EasterBossEntity.class'
assert actual.is_file()
proof=run('composition',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'ManorYouziOriginalSummonComposerV3',str(original),str(base),str(mapping),str(OUT),str(actual)])
entry='net/farmingtales/ftboss/entity/EasterBossEntity.class';helpers=['net/farmingtales/ftboss/youzi/YouziSummonScaling.class','net/farmingtales/ftboss/util/YouziSummonRules.class'];candidate=OUT/'ftboss-boss-core-1.21.1-stage33-youzi-original-summon-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate,'x') as after:
    assert len(before.namelist())==len(set(before.namelist()))
    for item in before.infolist():after.writestr(item,(OUT/'EasterBossEntity.class').read_bytes() if item.filename==entry else (OUT/'NativeAnimationEmitterInstaller.class').read_bytes() if item.filename==installer_entry else before.read(item.filename))
    for name in helpers:after.writestr(name,(OUT/name).read_bytes())
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate) as after:
    changed=[name for name in before.namelist() if before.read(name)!=after.read(name)];assert set(changed)=={entry,installer_entry};assert set(after.namelist())-set(before.namelist())==set(helpers);preserved=len(before.namelist())-2
assert hashes=={str(path):sha(path) for path in inputs}
save('candidate-build-receipt.json',{'jar':str(candidate),'jarSha256':sha(candidate),'baseJarSha256':sha(base),'changedJarEntries':changed,'unchangedOtherJarEntries':preserved,'addedJarEntries':helpers,'compositionProof':proof,'inputHashes':hashes,'frozenInputsUnchanged':True,'allExcludedLocalResourceBytesPreserved':True,'modelOrMusicUploaded':False,'nativeGameplayAccepted':False,'fullManagedSparrowMultiplayerRestored':False,'installable':False,'deployed':False,'fullBossMigrationComplete':False});print(json.dumps({'candidate':str(candidate),'sha256':sha(candidate),'proof':proof}),flush=True)
