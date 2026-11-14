import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-shadow-sigil-buffer-v2-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
prior_path=WORK/'build/boss-independent-material-cumulative-v1-20261114/candidate-build-receipt.json';prior=load(prior_path);base=Path(prior['jar']);assert sha(base)==prior['jarSha256']=='6c0acbcdda6fdecba9db9cc512ebdaceff36f84dd52ddf09b2879563716f54d9'
failed_path=WORK/'reports/earned-finale-attribute-baseline-native-v1-e7140217.json';failed=load(failed_path);assert not failed['actualEarnedUnlockAndFinaleAttributeBaselineAccepted'] and failed['terminal']['clientProcessReaped'] and failed['terminal']['integratedWorldCompleteSave'] and failed['terminal']['inputHashesUnchanged'] and failed['terminal']['exitCode']==4294967295
crash=Path(failed['attempt'])/'game/crash-reports/crash-2026-11-14_19.48.13-client.txt';text=crash.read_text(encoding='utf-8');assert 'java.lang.IllegalStateException: Not building!' in text and 'ShadowSigilVisual.renderSigil' in text
composer=WORK/'tests/ShadowSigilBufferComposerV2.java';cp_input=WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json';context=load(cp_input);cp=str(base)+';'+context[context.index('-cp')+1];inputs={str(path):sha(path) for path in [Path(__file__),base,prior_path,composer,cp_input,failed_path,crash]}
assert not OUT.exists();OUT.mkdir();classes=OUT/'compiled-classes';classes.mkdir();run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(composer)])
proof=run('composition',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'ShadowSigilBufferComposerV2',str(base),str(OUT)])
changed='net/farmingtales/ftboss/client/render/ShadowSigilVisual.class';candidate=OUT/'ftboss-boss-core-1.21.1-stage40-shadow-sigil-buffer-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate,'x') as after:
    for entry in before.infolist():after.writestr(entry,(OUT/changed).read_bytes() if entry.filename==changed else before.read(entry.filename))
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate) as after:
    assert len(after.namelist())==len(set(after.namelist())) and before.namelist()==after.namelist() and [name for name in before.namelist() if before.read(name)!=after.read(name)]==[changed]
    resource_count=sum(not name.endswith('.class') for name in before.namelist())
assert inputs=={path:sha(path) for path in inputs};save('candidate-build-receipt.json',{'jar':str(candidate),'jarSha256':sha(candidate),'baseJarSha256':sha(base),'changedJarEntries':[changed],'addedJarEntries':[],'allOtherExistingEntriesIdentical':len(before.namelist())-1,'compositionProof':proof,'allResourcesByteIdentical':True,'unchangedResourceEntries':resource_count,'frozenInputsUnchanged':True,'inputHashes':inputs,'resourcePayloadsUploaded':False,'weaponModuleOrSessionModified':False,'actualNativeShadowRenderingAccepted':False,'earnedFinaleAttributeBaselineAccepted':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False});print(json.dumps({'candidate':str(candidate),'sha256':sha(candidate),'proof':proof}),flush=True)
