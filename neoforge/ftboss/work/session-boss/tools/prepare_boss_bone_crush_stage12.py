import hashlib
import importlib.util
import json
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
helper=WORK/'tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
content=helper.read_text(encoding='utf-8')
exec(compile(content[:content.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
create=module.create
replace=module.replace

def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

previous=WORK/'releases/boss-core-stage11-cleaver-sweep-20261112'
prior=load(previous/'deployment-receipt.json')
assert digest(prior['target'])==prior['deployedSha256']=='5378b5657405a14d46fa5b7ad8bad78fd89764c83a9c89d901fbf51789b5e75e'
archive=load(previous/'archive-receipt.json')
assert digest(archive['archive'])==archive['archiveSha256']
paused=WORK/'archives/weapons-paused-613d4f6f-boss-priority-v2-20261112/archive-receipt.json'
pause=load(paused)
assert pause['failures']==0 and pause['weaponDevelopmentPaused'] and not pause['weaponStandaloneJarAccepted']
assert digest(pause['archive'])==pause['archiveSha256'] and digest(pause['snapshotJar'])==pause['snapshotJarSha256']
source=WORK/'proposal-stage11-cleaver-sweep/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java'
text=source.read_text(encoding='utf-8')
assert 'BONE_CRUSH' not in text
original_source=WORK/'build/boss-butcher-active-skill-audit-20261112/original-sources/net/farmingtales/ftboss/butcherskills/ButcherSkills.java'
original=original_source.read_text(encoding='utf-8')
assert 'ButcherSkillRules.Skill.values()' in original and 'var3 != ButcherSkillRules.Skill.CLEAVER_SWEEP && var3 != ButcherSkillRules.Skill.BLOOD_TIDE' in original
probe=(WORK/'tests/cleaver-sweep-v1/BossCleaverSweepRuntimeProbe.java').read_text(encoding='utf-8')
for old,new in [('BossCleaverSweepRuntimeProbe','BossBoneCrushRuntimeProbe'),('butcher_cleaver_sweep','butcher_bone_crush'),('ftboss.cleaversweep.mode','ftboss.bonecrush.mode'),('actual-cleaver-sweep-probe.json','actual-bone-crush-probe.json'),('actualCleaverSweepMissingDemonstrated','actualBoneCrushMissingDemonstrated'),('cleaverSweepFeatureAccepted','boneCrushFeatureAccepted'),('nativeSweep','nativeBoneCrush'),('CleaverSweep','BoneCrush'),('Sweep','BoneCrush')]:probe=probe.replace(old,new)
probe=replace(probe,'    private static boolean previouslyTide;','    private static boolean previouslyTide;\n    private static final List<Long> sweepStarts=new ArrayList<>();\n    private static boolean previouslySweep;')
probe=replace(probe,'        tideStarts.clear();','        tideStarts.clear();\n        sweepStarts.clear();\n        previouslySweep=false;')
probe=replace(probe,'double offsetX=index==2?0.0:(index==1?3.5:(index==4?4.6:4.0));','double offsetX=index==2?0.0:(index==1?-2.8:(index==4?3.1:2.8));')
probe=replace(probe,'double offsetZ=index==2?4.0:(index==1?1.5:0.0);','double offsetZ=index==2?2.8:0.0;')
probe=replace(probe,'20.0*(attack/5.5)','28.0*(attack/5.5)')
probe=replace(probe,'scenarioTicks>800','scenarioTicks>1200')
probe=replace(probe,'            if ("butcher_bone_crush".equals(owner.getButcherAction())) throw new AssertionError("Unrequested Bone Crush enabled");','            boolean sweeping="butcher_cleaver_sweep".equals(owner.getButcherAction());\n            if (sweeping && !previouslySweep) sweepStarts.add(now);\n            previouslySweep=sweeping;')
probe=replace(probe,'int duration=scenario==0?710:(scenario==5?370:350);','int duration=scenario==0?960:(scenario==5?460:450);')
probe=replace(probe,'row.put("tideStarts",new ArrayList<>(tideStarts));','row.put("tideStarts",new ArrayList<>(tideStarts));\n        row.put("sweepStarts",new ArrayList<>(sweepStarts));')
probe=replace(probe,'hits.size()==(candidate?3:0)','hits.size()==(candidate?5:0)')
probe=replace(probe,'starts.getFirst()-firstTick==314','starts.getFirst()-firstTick==418')
probe=replace(probe,'OriginalFirstBoneCrushAfterBrand80AndTide200','OriginalFirstBoneCrushAfterBrand80Tide200AndSweep314')
probe=replace(probe,'starts.get(1)-starts.getFirst()==338','starts.get(1)-starts.getFirst()==452')
probe=replace(probe,'OriginalThreeSkillRotationInterval338','OriginalFourSkillDirectedRotationInterval452')
probe=replace(probe,'ends.get(0)-starts.get(0)==24 && ends.get(1)-starts.get(1)==24','ends.get(0)-starts.get(0)==34 && ends.get(1)-starts.get(1)==34')
probe=replace(probe,'OriginalWindupPlusRecovery24','OriginalWindup20PlusRecovery14')
probe=replace(probe,'originalAmounts.size()==3','originalAmounts.size()==5')
probe=replace(probe,'originalAmounts.get(2).get("liveAttackAttribute")','originalAmounts.get(4).get("liveAttackAttribute")')
probe=replace(probe,'starts.get(1))==14','starts.get(1))==20')
probe=replace(probe,'OriginalReleaseOnWindupTick14','OriginalReleaseOnWindupTick20')
probe=replace(probe,'require(hits.stream().noneMatch(hit->((Integer)hit.get("targetIndex"))>=2),"OriginalBoneCrushConeRadiusAndVerticalExemptions");','require(hits.stream().map(hit->(Integer)hit.get("targetIndex")).toList().equals(List.of(0,1,2,1,2)),"OriginalCircularBoneCrushHitsBehindAndSideButNotOutsideRadiusOrVerticalRange");')
probe=replace(probe,'explicitKnocks.size()==3','explicitKnocks.size()==5')
probe=replace(probe,'brandStarts.get(1)-brandStarts.getFirst()==338','brandStarts.get(1)-brandStarts.getFirst()==452')
probe=replace(probe,'tideStarts.get(1)-tideStarts.getFirst()==338','tideStarts.get(1)-tideStarts.getFirst()==452')
probe=replace(probe,'"ExistingBloodSkillsRetainOriginalFirstDelayAndSharedRotation"','"ExistingBloodSkillsRetainOriginalFirstDelayAndFourSkillRotation"')
probe=replace(probe,'                require(brandStarts.size()==2','                require(sweepStarts.size()==2 && sweepStarts.getFirst()-firstTick==314 && sweepStarts.get(1)-sweepStarts.getFirst()==452,"ExistingCleaverSweepRetainsOrderAndSharedRotation");\n                require(brandStarts.size()==2')
create(WORK/'tests/bone-crush-v1/BossBoneCrushRuntimeProbe.java',probe)
runner=(WORK/'tools/run_boss_cleaver_sweep_probe_v1.py').read_text(encoding='utf-8')
for old,new in [('cleaver-sweep-v1/BossCleaverSweepRuntimeProbe.java','bone-crush-v1/BossBoneCrushRuntimeProbe.java'),('cleaver-sweep-','bone-crush-'),('ftboss.cleaversweep.mode','ftboss.bonecrush.mode'),('cleaverSweepFeatureAccepted','boneCrushFeatureAccepted'),('actualCleaverSweepMissingDemonstrated','actualBoneCrushMissingDemonstrated'),('Cleaver Sweep alone with necessary shared existing-skill controls','Bone Crush alone with directed four-skill order, circular-range and existing-skill controls')]:runner=runner.replace(old,new)
create(WORK/'tools/run_boss_bone_crush_probe_v1.py',runner)
proof=(WORK/'tests/CleaverSweepBoundaryProof.java').read_text(encoding='utf-8').replace('CleaverSweepBoundaryProof','BoneCrushBoundaryProof')
proof=replace(proof,' || name.equals("ButcherBloodBrand")','')
proof=replace(proof,'selectedSkills(before).equals(List.of("BLOOD_BRAND","BLOOD_TIDE"))','selectedSkills(before).equals(List.of("BLOOD_BRAND","BLOOD_TIDE","CLEAVER_SWEEP"))')
proof=replace(proof,'selectedSkills(changed).equals(List.of("BLOOD_BRAND","BLOOD_TIDE","CLEAVER_SWEEP"))','selectedSkills(changed).equals(List.of("BLOOD_BRAND","BLOOD_TIDE","CLEAVER_SWEEP","BONE_CRUSH"))')
proof=replace(proof,'Only Cleaver Sweep may be enabled','Only Bone Crush may be added')
proof=replace(proof,'List.of("release","<clinit>")','List.of("<clinit>")')
line='                    for (var instruction:method.instructions) if (instruction instanceof FieldInsnNode field && field.getOpcode()==Opcodes.GETSTATIC && field.owner.equals(SKILLS+"ButcherSkillRules$Skill")) require(!field.name.equals("BONE_CRUSH"),"Unrequested Bone Crush enabled");\n'
proof=replace(proof,line,'')
proof=replace(proof,'verified==8 && preserved==6','verified==8 && preserved==7')
proof=replace(proof,'unrelatedMethodsPreserved=6','unrelatedMethodsPreserved=7')
proof=replace(proof,'onlyCleaverSweepAdded=true','onlyBoneCrushAdded=true')
create(WORK/'tests/BoneCrushBoundaryProof.java',proof)
preflight={'previousFeature':str(previous/'feature-verification.json'),'previousArchive':str(previous/'archive-receipt.json'),'previousDeployment':str(previous/'deployment-receipt.json'),'stage11EvidenceReusedWithoutRerun':True,'weaponPauseReceipt':str(paused),'weaponArchiveSha256':pause['archiveSha256'],'selectedSkill':'BONE_CRUSH','originalSourceSha256':digest(original_source),'stage11SourceSha256':digest(source),'stage11JarSha256':prior['deployedSha256'],'onlyScheduledSkillAdditionPlanned':True,'fullBossMigrationComplete':False,'deployed':False}
audit=WORK/'build/boss-bone-crush-audit-20261112'
audit.mkdir()
(audit/'preflight.json').write_text(json.dumps(preflight,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'prepared':True,'weaponsPaused':True,'selectedSkill':'BONE_CRUSH','baselineReady':True}),flush=True)
