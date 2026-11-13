import importlib.util
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
source=(WORK/'tools/build_boss_cleaver_sweep_stage11_v2.py').read_text(encoding='utf-8')
source=replace(source,'cleaver-sweep-baseline-5db0470f.json','bone-crush-baseline-f3fca1ed.json')
source=replace(source,'boss-core-stage10-blood-tide-20261112/ftboss-boss-core-1.21.1-stage10.jar','boss-core-stage11-cleaver-sweep-20261112/ftboss-boss-core-1.21.1-stage11.jar')
source=replace(source,'6f3482b32f0361c664f5e524d44ee48e3d9965198300ea5b37484e299a57a211','5378b5657405a14d46fa5b7ad8bad78fd89764c83a9c89d901fbf51789b5e75e')
source=replace(source,'boss-cleaver-sweep-v1','boss-bone-crush-v1')
source=replace(source,'proposal-stage11-cleaver-sweep','proposal-stage12-bone-crush')
source=replace(source,'CleaverSweepBoundaryProof','BoneCrushBoundaryProof',2)
source=replace(source,'boss-cleaver-sweep-audit-20261112','boss-bone-crush-audit-20261112')
source=replace(source,'ftboss-boss-core-1.21.1-stage11-cleaver-sweep-NOT-INSTALLABLE.jar','ftboss-boss-core-1.21.1-stage12-bone-crush-NOT-INSTALLABLE.jar')
source=replace(source,"'scope':'Original Cleaver Sweep release, 60-degree cone, melee source/sound/knockback and one additional entry in the unchanged shared scheduler; Bone Crush, summon and client motion deferred'","'scope':'Original Bone Crush only: one fourth scheduled choice; exact existing scheduler/release/sound/knockback instructions preserved. Summon/client/authenticated/full-battle acceptance remains pending'")
needle="assert not baseline['probe']['nativeBloodDamageEvents']\n"
addition=r"""assert baseline['probe']['actualBoneCrushMissingDemonstrated']
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
"""
source=replace(source,needle,needle+addition)
create(WORK/'tools/build_boss_bone_crush_stage12.py',source)
print('stage12 build tool prepared; source creation guarded by completed baseline')
