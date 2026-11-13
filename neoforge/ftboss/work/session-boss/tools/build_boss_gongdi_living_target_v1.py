import argparse
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-gongdi-living-target-v1'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def run(name,command):
    save(OUT/(name+'-command.json'),command)
    result=subprocess.run(command,cwd=ROOT,capture_output=True,creationflags=subprocess.CREATE_NO_WINDOW)
    (OUT/(name+'.log')).write_bytes(result.stdout+result.stderr)
    save(OUT/(name+'-terminal.json'),{'exitCode':result.returncode})
    assert result.returncode==0,str(OUT/(name+'.log'))
    return result.stdout.decode('utf-8',errors='replace')
parser=argparse.ArgumentParser();parser.add_argument('baseline',type=Path);arguments=parser.parse_args()
baseline_path=arguments.baseline.resolve();baseline=load(baseline_path)
assert baseline['probe']['failures']==0 and baseline['probe']['actualOriginalLivingTargetGapDemonstrated']
assert baseline['terminal']['exitCode']==0 and baseline['terminal']['completeSave'] and baseline['terminal']['inputHashesUnchanged']
for name,wanted in baseline['inputHashes'].items():assert sha(name)==wanted,name
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noNewWeaponRuns']
previous_path=WORK/'releases/boss-core-stage16-gongdi-lifecycle-20261113/deployment-receipt.json';previous=load(previous_path)
base=previous_path.parent/'ftboss-boss-core-1.21.1-stage16-gongdi-lifecycle.jar';assert sha(base)==sha(previous['target'])==previous['deployedSha256']=='cb7bf44402aa18b2d051e8d388352285d2c5015713774dcbf3b0644a19d2d30d'
original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
assert not OUT.exists();OUT.mkdir();classes=OUT/'classes';classes.mkdir();proof_classes=OUT/'proof-classes';proof_classes.mkdir()
proposal=WORK/'proposal-stage17-gongdi-living-target';sources=sorted(proposal.rglob('*.java'));assert len(sources)==2
proof=WORK/'tests/GongdiLivingHookProof.java';context_path=WORK/'build/boss-flight-ban-v1/compile-context.json';context=load(context_path);sdk=[]
for entry in context['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()!='.jar':continue
    with zipfile.ZipFile(path) as bundle:
        if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    sdk.append(str(path))
classpath=str(base)+';'+';'.join(sdk)
run('proof-compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(proof_classes),str(proof)])
proof_output=run('entity-hook-proof',[str(JAVA/'java.exe'),'-cp',str(proof_classes)+';'+classpath,'GongdiLivingHookProof',str(base),str(original),str(classes)])
assert 'failures=0;addedOriginalLivingOverloads=2;allExistingEntityMethodsPreserved=' in proof_output
run('controller-compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',str(classes)+';'+classpath,'-d',str(classes),*[str(source) for source in sources]])
changed={path.relative_to(classes).as_posix():path.read_bytes() for path in classes.rglob('*.class')}
prefix='net/farmingtales/ftboss/';added=prefix+'boss/ai/GongdiGuillotineCooldown.class'
expected={prefix+'entity/EasterBossEntity.class',prefix+'boss/ai/GongdiSkills.class',prefix+'boss/ai/GongdiSkills$BleedingTarget.class',added};assert set(changed)==expected
jar=OUT/'ftboss-boss-core-1.21.1-stage17-gongdi-living-target-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar,'x',compression=zipfile.ZIP_DEFLATED) as after:
    for info in before.infolist():after.writestr(info,changed.get(info.filename,before.read(info.filename)))
    assert added not in before.namelist();after.writestr(added,changed[added])
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar) as after:
    assert after.testzip() is None and set(after.namelist())-set(before.namelist())=={added}
    differences=sorted(name for name in before.namelist() if before.read(name)!=after.read(name));assert differences==sorted(expected-{added})
    assert sum(name.endswith('.class') for name in after.namelist())==1446
inputs={str(path):sha(path) for path in [Path(__file__),baseline_path,*sources,proof,base,original,previous_path,context_path]}
save(OUT/'candidate-build-receipt.json',{'jar':str(jar),'jarSha256':sha(jar),'baseJar':str(base),'baseJarSha256':sha(base),'proposal':str(proposal),'originalNativeBaseline':str(baseline_path),'changedExistingEntries':differences,'addedClass':added,'allOtherEntriesByteIdentical':True,'proof':proof_output.strip(),'stage13Included':False,'originalControllerRestored':True,'nativeSkillDamageAndCancellationPending':True,'inputHashes':inputs,'installable':False,'deployed':False,'fullBossMigrationComplete':False,'weaponsPaused':True})
print(json.dumps({'jar':str(jar),'sha256':sha(jar),'proof':proof_output.strip(),'fullBossGoal':'active'}),flush=True)
