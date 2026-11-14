import hashlib
import json
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)

def execute(command,path):
    with path.open('xb') as stream:result=subprocess.run(command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT)
    save(path.with_suffix('.terminal.json'),{'exitCode':result.returncode})
    assert result.returncode==0,str(path)

baseline_path=WORK/'reports/cleaver-sweep-baseline-5db0470f.json'
baseline=load(baseline_path)
assert baseline['baselineGapObserved'] and baseline['probe']['failures']==0 and baseline['probe']['completedScenarios']==7
assert baseline['terminal']['exitCode']==0 and baseline['terminal']['completeSave'] and baseline['terminal']['inputHashesUnchanged']
assert not baseline['probe']['nativeBloodDamageEvents']
base=WORK/'releases/boss-core-stage11-cleaver-sweep-20261112/ftboss-boss-core-1.21.1-stage10.jar'
assert digest(base)=='6f3482b32f0361c664f5e524d44ee48e3d9965198300ea5b37484e299a57a211'
context_path=WORK/'build/boss-flight-ban-v1/compile-context.json'
sdk=[]
for entry in load(context_path)['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()!='.jar':continue
    with zipfile.ZipFile(path) as bundle:
        if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    sdk.append(str(path))
classpath=str(base)+';'+';'.join(sdk)
output=WORK/'build/boss-cleaver-sweep-v1'
output.mkdir()
compiled=output/'classes'
compiled.mkdir()
source=WORK/'proposal-stage11-cleaver-sweep/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java'
proof=WORK/'tests/CleaverSweepBoundaryProof.java'
original=ROOT/'reference/forge-current.jar'
callsite=WORK/'build/boss-butcher-active-skill-audit-20261112/original-sources/net/farmingtales/ftboss/butcherskills/ButcherSkills.java'
inputs={str(path):digest(path) for path in (base,baseline_path,context_path,source,proof,original,callsite,WORK/"build/boss-cleaver-sweep-audit-20261112/confirmed-gap-and-callsite.json",Path(__file__))}
save(output/'inputs-before.json',inputs)
execute([str(JAVA/'javac.exe'),'-proc:none','-implicit:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(compiled),str(source)],output/'compile.log')
proof_classes=output/'proof-classes'
proof_classes.mkdir()
execute([str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(proof_classes),str(proof)],output/'proof-compile.log')
execute([str(JAVA/'java.exe'),'-Xmx512m','-cp',str(proof_classes)+';'+classpath,'CleaverSweepBoundaryProof',str(base),str(original),str(compiled)],output/'proof-run.log')
overlay={path.relative_to(compiled).as_posix():path.read_bytes() for path in compiled.rglob('*.class')}
assert len(overlay)==2
overlay={name:payload for name,payload in overlay.items() if name.endswith("/ButcherBloodBrand.class")}
assert len(overlay)==1
candidate=output/'ftboss-boss-core-1.21.1-stage11-cleaver-sweep-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate,'x',compression=zipfile.ZIP_DEFLATED) as after:
    assert set(overlay).issubset(before.namelist())
    for entry in before.infolist():after.writestr(entry,overlay.get(entry.filename,before.read(entry.filename)))
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate) as after:
    assert before.namelist()==after.namelist() and after.testzip() is None
    changed=sorted(name for name in before.namelist() if before.read(name)!=after.read(name))
    assert changed==sorted(overlay)
assert all(digest(name)==wanted for name,wanted in inputs.items())
receipt={'jar':str(candidate),'jarSha256':digest(candidate),'baseJar':str(base),'baseJarSha256':digest(base),'proposal':str(WORK/'boss-core'),'scopedProposal':str(source.parent),'changedEntries':changed,'allOtherClassAndResourceBytesPreserved':True,'inputHashes':inputs,'proof':(output/'proof-run.log').read_text(encoding='utf-8'),'fullBossInstallable':False,'featureRuntimeAccepted':False,'deployed':False,'weaponFunctionalDevelopmentPaused':True,'stage03MaterialChangesIncluded':False,'scope':'Original Cleaver Sweep release, 60-degree cone, melee source/sound/knockback and one additional entry in the unchanged shared scheduler; Bone Crush, summon and client motion deferred'}
save(output/'candidate-build-receipt.json',receipt)
save(output/'compile-context.json',{'classpath':classpath,'inputHashes':inputs})
print(json.dumps({'jarSha256':receipt['jarSha256'],'changedEntries':changed,'proof':receipt['proof']}),flush=True)
