import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-manor-original-entry-v2-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.log')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.log'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
base=WORK/'build/boss-manor-native-events-v3-20261114/ftboss-boss-core-1.21.1-stage31-manor-native-events-NOT-INSTALLABLE.jar';assert sha(base)=='e63617ebd89d5e78055fef649db3392304a678abcafa4d74996a025d93f79c6c'
actual=WORK/'build/sparrow-native-events-regression-v1-45d4271b/captured-defined-classes/net_farmingtales_ftboss_entity_EasterBossEntity.class';assert sha(actual)=='da7da38c9cda2110e65b34e8c6ebce625023ef9aa0226139113981d5805db2d9'
original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
mapping=WORK/'build/boss-manor-routing-platform-audit-v1-20261114/mapping-proof.json';source=WORK/'tests/ManorOriginalEntryGuardComposerV2.java';audit=WORK/'tests/ManorOriginalDamageRoutingMappedAuditV1.java';cp_input=WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json';prior=load(cp_input);cp=str(base)+';'+prior[prior.index('-cp')+1]
inputs=[Path(__file__),base,actual,original,mapping,source,audit,cp_input];hashes={str(path):sha(path) for path in inputs}
assert not OUT.exists();OUT.mkdir();classes=OUT/'proof-classes';classes.mkdir()
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source),str(audit)])
proof=run('composition',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'ManorOriginalEntryGuardComposerV2',str(base),str(OUT),str(actual)]);assert 'originalHurtGraphExact=true;' in proof
entry='net/farmingtales/ftboss/entity/EasterBossEntity.class';patched=(OUT/'EasterBossEntity.class').read_bytes();candidate=OUT/'ftboss-boss-core-1.21.1-stage32-original-entry-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate,'x') as after:
    assert len(before.namelist())==len(set(before.namelist()))
    for item in before.infolist():after.writestr(item,patched if item.filename==entry else before.read(item.filename))
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate) as after:
    assert before.namelist()==after.namelist();changed=[name for name in before.namelist() if before.read(name)!=after.read(name)];assert changed==[entry]
    preserved=len(before.namelist())-1
original_proof=run('original-six-method-audit',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'ManorOriginalDamageRoutingMappedAuditV1',str(original),str(mapping),str(OUT/'simulated-after.class')]);assert 'completeMappedOriginalMethods=6;' in original_proof
assert hashes=={str(path):sha(path) for path in inputs}
save('candidate-build-receipt.json',{'jar':str(candidate),'jarSha256':sha(candidate),'baseJarSha256':sha(base),'changedJarEntries':changed,'unchangedOtherJarEntries':preserved,'addedJarEntries':[],'compositionProof':proof,'originalSixMethodProof':original_proof,'inputHashes':hashes,'frozenInputsUnchanged':True,'completeEffectiveHurtEqualsMappedOriginal':True,'allExcludedLocalResourceBytesPreserved':True,'modelOrMusicUploaded':False,'nativeBoundaryAccepted':False,'installable':False,'deployed':False,'fullBossMigrationComplete':False});print(json.dumps({'candidate':str(candidate),'sha256':sha(candidate),'proof':proof,'originalProof':original_proof}),flush=True)
