import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-independent-material-cumulative-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
base=Path(owner['latestScopedBossCandidate']);assert sha(base)=='9d713622cc8c8aa3d2e5b5fa340053a7eb45f8128247346c3152a14c975b886e'
accepted_path=WORK/'reports/sparrow-standalone-attacker-closeout-20261114.json';accepted=load(accepted_path);assert accepted['actualOriginalAttackerEventAccepted'] and accepted['nativePeriodicPruneAndSavedRetirementAccepted'] and accepted['candidateJarSha256']==sha(base)
source=WORK/'boss-core/src/main/java/net/farmingtales/ftboss/item/BossMaterialDrops.java';composer=WORK/'tests/BossIndependentMaterialComposerV1.java';cp_input=WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json';prior=load(cp_input);cp=str(base)+';'+prior[prior.index('-cp')+1];inputs={str(path):sha(path) for path in [Path(__file__),base,source,composer,accepted_path,cp_input]}
assert not OUT.exists();OUT.mkdir();classes=OUT/'compiled-classes';classes.mkdir();run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source),str(composer)])
proof=run('composition',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'BossIndependentMaterialComposerV1',str(base),str(classes),str(OUT)])
prefix='net/farmingtales/ftboss/';changed=[prefix+part+'.class' for part in ['item/FTBossItems','item/FTCreativeTabs','skill/BossRelicDrops']];added=[prefix+'item/BossMaterialDrops'+suffix+'.class' for suffix in ['', '$Definition','$MaterialItem']];candidate=OUT/'ftboss-boss-core-1.21.1-stage37-independent-materials-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate,'x') as after:
    for entry in before.infolist():after.writestr(entry,(OUT/entry.filename).read_bytes() if entry.filename in changed else before.read(entry.filename))
    for name in added:after.writestr(name,(OUT/name).read_bytes())
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate) as after:
    assert len(after.namelist())==len(set(after.namelist())) and sorted(name for name in before.namelist() if before.read(name)!=after.read(name))==sorted(changed) and set(after.namelist())-set(before.namelist())==set(added)
    resources=[]
    for material in ['frost','butcher','tea','wine','cheese','capacitor','spore','apple','shepherd','tide']:
        name='assets/ftboss/models/item/'+material+'_material.json';assert name in before.namelist() and before.read(name)==after.read(name);resources.append({'path':name,'sha256':hashlib.sha256(after.read(name)).hexdigest()})
assert inputs=={path:sha(path) for path in inputs};save('candidate-build-receipt.json',{'jar':str(candidate),'jarSha256':sha(candidate),'baseJarSha256':sha(base),'changedJarEntries':changed,'addedJarEntries':added,'unchangedOtherJarEntries':len(before.namelist())-3,'compositionProof':proof,'frozenInputsUnchanged':True,'inputHashes':inputs,'allOriginalResourceBytesUnchanged':True,'localOnlyOriginalMaterialModels':resources,'resourcePayloadsUploaded':False,'weaponModuleOrSessionModified':False,'nativeRewardIntegrationAccepted':False,'authenticatedKillAccepted':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False});print(json.dumps({'candidate':str(candidate),'sha256':sha(candidate),'proof':proof}),flush=True)
