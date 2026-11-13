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

baseline_path=WORK/'reports/bone-crush-baseline-f3fca1ed.json'
baseline=load(baseline_path)
assert baseline['baselineGapObserved'] and baseline['probe']['failures']==0 and baseline['probe']['completedScenarios']==7
assert baseline['terminal']['exitCode']==0 and baseline['terminal']['completeSave'] and baseline['terminal']['inputHashesUnchanged']
assert not baseline['probe']['nativeBloodDamageEvents']
assert baseline['probe']['actualBoneCrushMissingDemonstrated']
helper=WORK/'tools/prepare_boss_blood_tide_stage10.py'
import importlib.util
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
content=helper.read_text(encoding='utf-8')
exec(compile(content[:content.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
old_source=WORK/'proposal-stage11-cleaver-sweep/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java'
new_source=WORK/'proposal-stage12-bone-crush/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java'
prior_text=old_source.read_text(encoding='utf-8')
new_text=module.replace(prior_text,'ButcherSkillRules.Skill.CLEAVER_SWEEP};','ButcherSkillRules.Skill.CLEAVER_SWEEP,ButcherSkillRules.Skill.BONE_CRUSH};')
module.create(new_source,new_text)
assert new_source.read_text(encoding='utf-8').replace(',ButcherSkillRules.Skill.BONE_CRUSH','')==prior_text
original_source=WORK/'build/boss-butcher-active-skill-audit-20261112/original-sources/net/farmingtales/ftboss/butcherskills/ButcherSkills.java'
save(WORK/'build/boss-bone-crush-audit-20261112/confirmed-gap-and-callsite.json',{'selectedSkill':'BONE_CRUSH','actualMissingBaseline':True,'baselineReceipt':str(baseline_path),'baselineSha256':digest(baseline_path),'originalSourceSha256':digest(original_source),'baselineSourceSha256':digest(old_source),'candidateSourceSha256':digest(new_source),'onlyChange':'Append original fourth skill choice; no method instructions changed','windup':20,'recovery':14,'cooldown':200,'globalCooldown':80,'castRange':5.0,'radius':3.0,'circularNotCone':True,'baseDamage':28.0,'successfulHitKnockback':0.45,'soundPitch':0.65,'weaponDevelopmentPaused':True})
base=WORK/'releases/boss-core-stage11-cleaver-sweep-20261112/ftboss-boss-core-1.21.1-stage11.jar'
assert digest(base)=='5378b5657405a14d46fa5b7ad8bad78fd89764c83a9c89d901fbf51789b5e75e'
context_path=WORK/'build/boss-flight-ban-v1/compile-context.json'
sdk=[]
for entry in load(context_path)['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()!='.jar':continue
    with zipfile.ZipFile(path) as bundle:
        if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    sdk.append(str(path))
classpath=str(base)+';'+';'.join(sdk)
output=WORK/'build/boss-bone-crush-v1'
output.mkdir()
compiled=output/'classes'
compiled.mkdir()
source=WORK/'proposal-stage12-bone-crush/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java'
proof=WORK/'tests/BoneCrushBoundaryProof.java'
original=ROOT/'reference/forge-current.jar'
callsite=WORK/'build/boss-butcher-active-skill-audit-20261112/original-sources/net/farmingtales/ftboss/butcherskills/ButcherSkills.java'
inputs={str(path):digest(path) for path in (base,baseline_path,context_path,source,proof,original,callsite,WORK/"build/boss-bone-crush-audit-20261112/confirmed-gap-and-callsite.json",Path(__file__))}
save(output/'inputs-before.json',inputs)
execute([str(JAVA/'javac.exe'),'-proc:none','-implicit:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(compiled),str(source)],output/'compile.log')
proof_classes=output/'proof-classes'
proof_classes.mkdir()
execute([str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(proof_classes),str(proof)],output/'proof-compile.log')
execute([str(JAVA/'java.exe'),'-Xmx512m','-cp',str(proof_classes)+';'+classpath,'BoneCrushBoundaryProof',str(base),str(original),str(compiled)],output/'proof-run.log')
overlay={path.relative_to(compiled).as_posix():path.read_bytes() for path in compiled.rglob('*.class')}
assert len(overlay)==2
overlay={name:payload for name,payload in overlay.items() if name.endswith("/ButcherBloodBrand.class")}
assert len(overlay)==1
candidate=output/'ftboss-boss-core-1.21.1-stage12-bone-crush-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate,'x',compression=zipfile.ZIP_DEFLATED) as after:
    assert set(overlay).issubset(before.namelist())
    for entry in before.infolist():after.writestr(entry,overlay.get(entry.filename,before.read(entry.filename)))
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate) as after:
    assert before.namelist()==after.namelist() and after.testzip() is None
    changed=sorted(name for name in before.namelist() if before.read(name)!=after.read(name))
    assert changed==sorted(overlay)
assert all(digest(name)==wanted for name,wanted in inputs.items())
receipt={'jar':str(candidate),'jarSha256':digest(candidate),'baseJar':str(base),'baseJarSha256':digest(base),'proposal':str(WORK/'boss-core'),'scopedProposal':str(source.parent),'changedEntries':changed,'allOtherClassAndResourceBytesPreserved':True,'inputHashes':inputs,'proof':(output/'proof-run.log').read_text(encoding='utf-8'),'fullBossInstallable':False,'featureRuntimeAccepted':False,'deployed':False,'weaponFunctionalDevelopmentPaused':True,'stage03MaterialChangesIncluded':False,'scope':'Original Bone Crush only: one fourth scheduled choice; exact existing scheduler/release/sound/knockback instructions preserved. Summon/client/authenticated/full-battle acceptance remains pending'}
save(output/'candidate-build-receipt.json',receipt)
save(output/'compile-context.json',{'classpath':classpath,'inputHashes':inputs})
print(json.dumps({'jarSha256':receipt['jarSha256'],'changedEntries':changed,'proof':receipt['proof']}),flush=True)
