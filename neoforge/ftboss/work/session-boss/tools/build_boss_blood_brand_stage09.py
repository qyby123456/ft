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

baseline_path=WORK/'reports/blood-brand-baseline-70bb399c.json'
baseline=json.loads(baseline_path.read_text(encoding='utf-8'))
assert baseline['baselineGapObserved'] and baseline['probe']['failures']==0 and baseline['probe']['completedScenarios']==7
assert baseline['terminal']['exitCode']==0 and baseline['terminal']['completeSave'] and baseline['terminal']['inputHashesUnchanged']
base=WORK/'releases/boss-core-stage08-melee-cadence-20261112/ftboss-boss-core-1.21.1-stage08.jar'
assert digest(base)=='bce65bb197a29755ec1fa46fc3a1c9a1722225fc0bcb2a080f4ccc3d7dde0ccb'
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
output=WORK/'build/boss-blood-brand-v1'
output.mkdir()
compiled=output/'api-link-classes'
compiled.mkdir()
srcroot=WORK/'boss-core/src/main/java/net/farmingtales/ftboss'
sources=[srcroot/name for name in ['boss/ai/ButcherCombat.java','butcherskills/ButcherBloodBrand.java','butcherskills/ButcherSkillFx.java','butcherskills/ButcherSkillRules.java','butcherskills/ButcherFxPlan.java']]
proof=WORK/'tests/BloodBrandBoundaryProof.java'
language_delta=WORK/'resource-patches/stage09-blood-brand-language-delta.json'
damage_source=WORK/'boss-core/src/main/resources/data/ftboss/damage_type/butcher_blood.json'
inputs=[base,original,baseline_path,context_path,proof,Path(__file__),language_delta,damage_source,*sources,WORK/'build/boss-butcher-active-skill-audit-20261112/stage09-preflight.json',WORK/'build/boss-butcher-active-skill-audit-20261112/blood-brand-callsite-audit.json']
before={str(path):digest(path) for path in inputs}
save(output/'inputs-before.json',before)
execute([str(JAVA/'javac.exe'),'-proc:none','-implicit:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(compiled),*map(str,sources)],output/'api-link-compile.log')
proof_classes=output/'proof-classes'
proof_classes.mkdir()
execute([str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(proof_classes),str(proof)],output/'proof-compile.log')
classes=output/'classes'
execute([str(JAVA/'java.exe'),'-Xmx512m','-cp',str(proof_classes)+';'+classpath,'BloodBrandBoundaryProof',str(base),str(original),str(compiled),str(classes)],output/'proof-run.log')
proof_text=(output/'proof-run.log').read_text(encoding='utf-8')
assert 'failures=0;' in proof_text and 'onlyBloodBrandEnabled=true' in proof_text and 'normalizedUpdateInstructionsIdentical=true' in proof_text
overlay={path.relative_to(classes).as_posix():path.read_bytes() for path in classes.rglob('*.class')}
assert len(overlay)==9
delta=json.loads(language_delta.read_text(encoding='utf-8'))
with zipfile.ZipFile(original) as archive:
    name='data/ftboss/damage_type/butcher_blood.json'
    overlay[name]=archive.read(name)
    assert json.loads(overlay[name])==json.loads(damage_source.read_text(encoding='utf-8'))
with zipfile.ZipFile(base) as previous:
    for name,added in delta.items():
        old=previous.read(name).decode('utf-8')
        old_values=json.loads(old)
        assert not set(added)&set(old_values)
        prefix=old.rstrip()
        assert prefix.endswith('}')
        lines=[json.dumps(key,ensure_ascii=False)+': '+json.dumps(value,ensure_ascii=False) for key,value in added.items()]
        text=prefix[:-1].rstrip()+','+chr(10)+'  '+(','+chr(10)+'  ').join(lines)+chr(10)+'}'+chr(10)
        assert json.loads(text)==dict(old_values,**added)
        overlay[name]=text.encode('utf-8')
candidate=output/'ftboss-boss-core-1.21.1-stage09-blood-brand-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as previous,zipfile.ZipFile(candidate,'x',compression=zipfile.ZIP_DEFLATED) as archive:
    for entry in previous.infolist():
        archive.writestr(entry,overlay[entry.filename] if entry.filename in overlay else previous.read(entry.filename))
    for name,payload in overlay.items():
        if name not in previous.namelist():archive.writestr(name,payload)
with zipfile.ZipFile(base) as previous,zipfile.ZipFile(candidate) as archive:
    assert archive.testzip() is None and not set(previous.namelist())-set(archive.namelist())
    added=sorted(set(archive.namelist())-set(previous.namelist()))
    changed=sorted(name for name in previous.namelist() if archive.read(name)!=previous.read(name))
    assert set(added)|set(changed)==set(overlay) and len(added)==9 and len(changed)==3
    class_count=sum(name.endswith('.class') for name in archive.namelist())
assert all(digest(name)==wanted for name,wanted in before.items())
save(output/'compile-context.json',{'classpath':classpath,'inputHashes':before,'allInputsUnchanged':True,'proof':proof_text,'apiLinkSourceCompilationOnlyForExistingCombatClass':True,'productionCombatClassDerivedFromStage08BytecodeWithSkillGateOnly':True,'stage08AcceptanceReusedNotRerun':True,'stage03MaterialChangesIncluded':False,'weaponCandidateCompiledOrValidated':False})
result={'jar':str(candidate),'jarSha256':digest(candidate),'proposal':str(WORK/'build/boss-split-9339206b/boss-core-proposal'),'baseJar':str(base),'baseJarSha256':digest(base),'addedEntries':added,'changedEntries':changed,'classCount':class_count,'allOtherClassAndResourceBytesPreserved':True,'onlyBloodBrandEnabled':True,'otherThreeSkillsAndClientMotionDeferred':True,'languageDelta':delta,'damageTypeByteIdenticalToFrozenOriginal':True,'fullBossInstallable':False,'deployed':False,'weaponFunctionalDevelopmentPaused':True,'inputHashes':before}
save(output/'candidate-build-receipt.json',result)
print(json.dumps({'jarSha256':result['jarSha256'],'addedEntries':added,'changedEntries':changed,'classCount':class_count,'proof':proof_text}),flush=True)
