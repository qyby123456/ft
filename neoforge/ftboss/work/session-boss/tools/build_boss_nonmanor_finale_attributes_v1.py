import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-nonmanor-finale-attributes-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=150,creationflags=subprocess.CREATE_NO_WINDOW)
    try:text=(result.stdout+result.stderr).decode('utf-8')
    except UnicodeDecodeError:text=(result.stdout+result.stderr).decode('gb18030')
    (OUT/(name+'.txt')).write_text(text,encoding='utf-8');save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8')
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
prior_path=WORK/'reports/earned-finale-zhuni-closeout-20261114.json';prior=load(prior_path)
base=Path(owner['latestScopedBossCandidate']);assert sha(base)==owner['latestScopedBossCandidateSha256']=='b751ae8fbf5552342ae5e887cc5d7675b7fcdf995fcde1773dcc0dc32e691345'
surface_path=WORK/'reports/original-finale-attribute-surface-v1-20261114/receipt.json';surface=load(surface_path);assert surface['candidateJarSha256']==sha(base) and surface['allFrozenInputsUnchanged']
for path,digest in surface['inputHashes'].items():assert Path(path).is_file() and sha(path)==digest
source=WORK/'src/nonmanor-finale-attributes-v1/net/farmingtales/ftboss/boss/NonManorFinaleAttributes.java';composer=WORK/'tests/NonManorFinaleAttributeComposerV1.java';context_path=WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json';context=load(context_path);cp=str(base)+';'+context[context.index('-cp')+1]
inputs={str(path):sha(path) for path in [Path(__file__),prior_path,base,surface_path,source,composer,context_path,ROOT/'reference/forge-current.jar']}
assert not OUT.exists();OUT.mkdir();classes=OUT/'compiled-classes';classes.mkdir()
command=[str(JAVA/'javac.exe'),'-J-Dfile.encoding=UTF-8','-J-Duser.language=en','-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source),str(composer)];save('compile-command.json',command);run('compile',command)
proof=json.loads(run('composition',[str(JAVA/'java.exe'),'-Dfile.encoding=UTF-8','-Xmx768m','-cp',str(classes)+';'+cp,'NonManorFinaleAttributeComposerV1',str(base),str(classes),str(OUT)]))
target='net/farmingtales/ftboss/boss/NonManorFinale.class';added=['net/farmingtales/ftboss/boss/NonManorFinaleAttributes.class','net/farmingtales/ftboss/boss/NonManorFinaleAttributes$AttributeBase.class'];candidate=OUT/'ftboss-boss-core-1.21.1-stage43-base-only-finale-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate,'x',compression=zipfile.ZIP_DEFLATED) as after:
    assert not set(added)&set(before.namelist())
    for info in before.infolist():after.writestr(info,(OUT/'NonManorFinale.class').read_bytes() if info.filename==target else before.read(info.filename))
    for name in added:after.write(classes/name,name)
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate) as after:
    differences=[name for name in before.namelist() if before.read(name)!=after.read(name)];new=sorted(set(after.namelist())-set(before.namelist()));assert differences==[target] and new==sorted(added);resources=sum(not name.endswith('.class') for name in before.namelist());assert resources==1530
assert all(sha(path)==digest for path,digest in inputs.items())
receipt={'jar':str(candidate),'jarSha256':sha(candidate),'baseJarSha256':sha(base),'changedJarEntries':differences,'addedJarEntries':new,'allOtherExistingEntriesIdentical':True,'allResourcesByteIdentical':True,'unchangedResourceEntries':resources,'compositionProof':proof,'frozenInputsUnchanged':True,'inputHashes':inputs,'scope':'Existing legitimately admitted non-manor finale attribute application only; original explicit max-health plus syncable base snapshots, sanitized base doubling, other modifiers retained, idempotent saved preparation and original maximum-health repair; legacy own total modifier removed only during first preparation. Existing ingress, cast, reward and Tea-specific challenge setup are unchanged and not independently accepted by this build.','actualNativeAttributeParityAccepted':False,'actualLegacySavedWorldMigrationAccepted':False,'resourcePayloadsUploaded':False,'weaponModuleOrSessionModified':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False}
save('candidate-build-receipt.json',receipt);print(json.dumps({'jarSha256':receipt['jarSha256'],'proof':proof,'resourcesUnchanged':resources}))
