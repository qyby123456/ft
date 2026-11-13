import hashlib
import json
from pathlib import Path
import subprocess
import zipfile

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')

def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:
        json.dump(value,stream,ensure_ascii=False,indent=2)

def execute(command,log):
    with log.open('xb') as stream:
        result=subprocess.run(command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT)
    save(log.with_suffix('.terminal.json'),{'exitCode':result.returncode,'logSha256':digest(log)})
    assert result.returncode==0,str(log)

baseline_path=WORK/'reports/butcher-cadence-baseline-48ec8c31.json'
baseline=json.loads(baseline_path.read_text(encoding='utf-8'))
assert baseline['baselineGapObserved'] and baseline['probe']['failures']==0 and baseline['probe']['completedScenarios']==9
assert baseline['terminal']['exitCode']==0 and baseline['terminal']['completeSave'] and baseline['terminal']['inputHashesUnchanged']
base=WORK/'releases/boss-core-stage07-butcher-phase-20261112/ftboss-boss-core-1.21.1-stage07.jar'
assert digest(base)=='bf7eee564d54816eaa46d7529a06eb26c907361d7bc427f20c4d9742f0597f08'
original=ROOT/'reference/forge-current.jar'
context_path=WORK/'build/boss-flight-ban-v1/compile-context.json'
context=json.loads(context_path.read_text(encoding='utf-8'))
sdk=[]
for entry in context['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()!='.jar':
        continue
    with zipfile.ZipFile(path) as archive:
        if 'net/farmingtales/ftboss/FTBossMod.class' in archive.namelist():
            continue
    sdk.append(str(path))
classpath=str(base)+';'+';'.join(sdk)
output=WORK/'build/boss-butcher-cadence-v1'
output.mkdir()
compiled=output/'api-link-classes'
compiled.mkdir()
source=WORK/'boss-core/src/main/java/net/farmingtales/ftboss/boss/ai/ButcherCombat.java'
proof=WORK/'tests/ButcherCadenceBoundaryProof.java'
inputs=[base,original,baseline_path,context_path,source,proof,Path(__file__),WORK/'build/boss-butcher-cadence-audit-20261112/stage08-preflight.json',WORK/'build/boss-butcher-cadence-audit-20261112/callsite-audit.json']
before={str(path):digest(path) for path in inputs}
save(output/'inputs-before.json',before)
execute([str(JAVA/'javac.exe'),'-proc:none','-implicit:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(compiled),str(source)],output/'api-link-compile.log')
proof_classes=output/'proof-classes'
proof_classes.mkdir()
execute([str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(proof_classes),str(proof)],output/'proof-compile.log')
classes=output/'classes'
execute([str(JAVA/'java.exe'),'-Xmx512m','-cp',str(proof_classes)+';'+classpath,'ButcherCadenceBoundaryProof',str(base),str(original),str(compiled),str(classes)],output/'proof-run.log')
proof_text=(output/'proof-run.log').read_text(encoding='utf-8')
assert 'failures=0;' in proof_text and 'singleSchedulerReplacementOnly=true' in proof_text and 'normalizedUpdateInstructionsIdentical=true' in proof_text
name='net/farmingtales/ftboss/boss/ai/ButcherCombat.class'
assert {path.relative_to(classes).as_posix() for path in classes.rglob('*.class')}=={name}
candidate=output/'ftboss-boss-core-1.21.1-stage08-melee-cadence-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as previous,zipfile.ZipFile(candidate,'x',compression=zipfile.ZIP_DEFLATED) as archive:
    for entry in previous.infolist():
        archive.writestr(entry,(classes/name).read_bytes() if entry.filename==name else previous.read(entry.filename))
with zipfile.ZipFile(base) as previous,zipfile.ZipFile(candidate) as archive:
    assert archive.testzip() is None and set(archive.namelist())==set(previous.namelist())
    changed=sorted(entry for entry in previous.namelist() if archive.read(entry)!=previous.read(entry))
    assert changed==[name]
    class_count=sum(entry.endswith('.class') for entry in archive.namelist())
assert all(digest(name)==wanted for name,wanted in before.items())
save(output/'compile-context.json',{'classpath':classpath,'inputHashes':before,'allInputsUnchanged':True,'proof':proof_text,'apiLinkSourceCompilationOnly':True,'productionClassDerivedFromStage07BytecodeWithSingleSchedulerPatch':True,'stage07AcceptanceReusedNotRerun':True,'stage03MaterialChangesIncluded':False,'weaponCandidateCompiledOrValidated':False})
result={'jar':str(candidate),'jarSha256':digest(candidate),'proposal':str(WORK/'build/boss-split-9339206b/boss-core-proposal'),'baseJar':str(base),'baseJarSha256':digest(base),'addedClasses':[],'changedClasses':changed,'classCount':class_count,'allOtherClassAndResourceBytesPreserved':True,'singleSchedulerReplacementOnly':True,'chaseDamageLifestealDebuffsActiveSkillsUnchanged':True,'fullBossInstallable':False,'deployed':False,'weaponFunctionalDevelopmentPaused':True,'inputHashes':before}
save(output/'candidate-build-receipt.json',result)
print(json.dumps({'jarSha256':result['jarSha256'],'changedClasses':changed,'classCount':class_count,'proof':proof_text}),flush=True)
