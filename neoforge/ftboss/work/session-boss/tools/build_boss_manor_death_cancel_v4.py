import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-manor-death-cancel-v4-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=150,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_text((result.stdout+result.stderr).decode('utf-8',errors='replace'),encoding='utf-8');save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8')
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
prior_path=WORK/'reports/shadow-sigil-render-closeout-20261114.json';prior=load(prior_path);base=Path(prior['candidate']);assert sha(base)==prior['candidateJarSha256']=='95e29f9f6986d58d4f55238776088d82e7782b0e0ece5c2d2f72e25f98ba4215'
boundary_path=WORK/'build/boss-manor-death-cancel-boundary-v2-20261114/receipt.json';boundary=load(boundary_path);assert boundary['boundary']['checks']==49 and boundary['boundary']['negativeControls']==2 and boundary['boundary']['nativeDeadFlagFollowsUncanceledDeathEvent']
for evidence in [boundary]:
    for path,digest in evidence['inputHashes'].items():assert Path(path).is_file() and sha(path)==digest
installer=WORK/'src/manor-death-cancel-v1/net/farmingtales/ftboss/boss/ai/NativeManorDeathCancelInstaller.java';composer=WORK/'tests/ManorDeathCancelComposerV4.java';context_path=WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json';context=load(context_path);cp=str(base)+';'+context[context.index('-cp')+1];actual=WORK/'build/shadow-sigil-actual-native-v2-085dc9c7/captured-defined-classes/net_farmingtales_ftboss_entity_EasterBossEntity.class';inputs={str(path):sha(path) for path in [Path(__file__),prior_path,base,boundary_path,installer,composer,context_path,actual]}
assert not OUT.exists();OUT.mkdir();classes=OUT/'compiled-classes';classes.mkdir();save('compile-command.json',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(installer),str(composer)]);run('compile',[str(JAVA/'javac.exe'),'-J-Dfile.encoding=UTF-8','-J-Duser.language=en','-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(installer),str(composer)])
proof=json.loads(run('composition',[str(JAVA/'java.exe'),'-Dfile.encoding=UTF-8','-Xmx768m','-cp',str(classes)+';'+cp,'ManorDeathCancelComposerV4',str(base),str(actual),str(OUT)]));plugin='net/farmingtales/ftboss/boss/ai/NativeManorMixinPlugin.class';added='net/farmingtales/ftboss/boss/ai/NativeManorDeathCancelInstaller.class';candidate=OUT/'ftboss-boss-core-1.21.1-stage41-native-death-cancel-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate,'x',compression=zipfile.ZIP_DEFLATED) as after:
    assert added not in before.namelist()
    for info in before.infolist():after.writestr(info,(OUT/'NativeManorMixinPlugin.class').read_bytes() if info.filename==plugin else before.read(info.filename))
    after.write(classes/added,added)
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate) as after:
    differences=[name for name in before.namelist() if before.read(name)!=after.read(name)];new=sorted(set(after.namelist())-set(before.namelist()));assert differences==[plugin] and new==[added];resources=sum(not name.endswith('.class') for name in before.namelist());assert resources==1530
assert all(sha(path)==digest for path,digest in inputs.items())
receipt={'jar':str(candidate),'jarSha256':sha(candidate),'baseJarSha256':sha(base),'changedJarEntries':differences,'addedJarEntries':new,'allOtherExistingEntriesIdentical':True,'allResourcesByteIdentical':True,'unchangedResourceEntries':resources,'compositionProof':proof,'frozenInputsUnchanged':True,'inputHashes':inputs,'originalTwoScriptedDeathAndOverrideCleanupGatesOnly':True,'actualNativeDeathCancellationAccepted':False,'actualNormalVictoryEarnedUnlock':False,'resourcePayloadsUploaded':False,'weaponModuleOrSessionModified':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False};save('candidate-build-receipt.json',receipt);print(json.dumps({'jarSha256':receipt['jarSha256'],'proof':proof,'changedEntries':differences,'addedEntries':new,'resourcesUnchanged':resources,'deployed':False}))
