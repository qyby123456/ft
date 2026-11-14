import hashlib
import importlib.util
import json
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

baseline_path=WORK/'reports/cleaver-sweep-baseline-5db0470f.json'
baseline=load(baseline_path)
assert baseline['baselineGapObserved'] and baseline['probe']['actualCleaverSweepMissingDemonstrated']
assert baseline['probe']['failures']==0 and baseline['probe']['completedScenarios']==7
assert baseline['terminal']['exitCode']==0 and baseline['terminal']['completeSave'] and baseline['terminal']['stopSent'] and baseline['terminal']['inputHashesUnchanged']
assert not baseline['probe']['nativeBloodDamageEvents']
original=ROOT/'reference/forge-current.jar'
recovery=load(WORK/'build/boss-butcher-active-skill-audit-20261112/source-recovery-receipt.json')
assert digest(original)==recovery['originalJarSha256']
with zipfile.ZipFile(original) as bundle:
    key='net/farmingtales/ftboss/butcherskills/ButcherSkills.class'
    assert hashlib.sha256(bundle.read(key)).hexdigest()==recovery['classHashes'][key]
original_source=WORK/'build/boss-butcher-active-skill-audit-20261112/original-sources/net/farmingtales/ftboss/butcherskills/ButcherSkills.java'
frozen=original_source.read_text(encoding='utf-8')
assert 'var3 == ButcherSkillRules.Skill.CLEAVER_SWEEP ? 0.5 : Math.sqrt(0.5)' in frozen
assert 'var3.melee() ? 0.65F : 0.85F' in frozen and 'var10.m_147240_(0.45,' in frozen
native=WORK/'boss-core/src/main/java/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java'
text=native.read_text(encoding='utf-8')
assert 'CLEAVER_SWEEP' not in text and 'BONE_CRUSH' not in text
output=WORK/'build/boss-cleaver-sweep-audit-20261112'
base=WORK/'releases/boss-core-stage10-blood-tide-20261112/ftboss-boss-core-1.21.1-stage10.jar'
for label,jar,name in (('original-forge',original,'net.farmingtales.ftboss.butcherskills.ButcherSkills'),('native-before',base,'net.farmingtales.ftboss.butcherskills.ButcherBloodBrand')):
    with (output/(label+'.javap.txt')).open('xb') as stream:
        result=subprocess.run(['C:/Program Files/Zulu/zulu-21/bin/javap.exe','-p','-c','-classpath',str(jar),name],stdout=stream,stderr=subprocess.STDOUT)
    assert result.returncode==0
audit={'selectedSkill':'CLEAVER_SWEEP','explicitGapConfirmed':True,'frozenOriginal':str(original),'frozenOriginalSha256':digest(original),'frozenOriginalSkillClassSha256':recovery['classHashes']['net/farmingtales/ftboss/butcherskills/ButcherSkills.class'],'originalSource':str(original_source),'originalSourceSha256':digest(original_source),'nativeBeforeSourceSha256':digest(native),'originalCaller':'ButcherCombat.update -> ButcherSkills.tick before ordinary melee','originalSoundMappingEvidence':'Original ButcherCombat.hit uses SoundEvents.f_12316_; frozen native equivalent uses PLAYER_ATTACK_STRONG. Original skill release uses f_12316_ for melee.','baselineReceipt':str(baseline_path),'baselineReceiptSha256':digest(baseline_path),'actualBaselineSweepCasts':0,'actualBaselineSweepDamageEvents':0,'nativeServerCallbacks':baseline['probe']['nativeServerTickCallbacks'],'windup':14,'recovery':10,'cooldown':160,'globalCooldown':80,'castRange':4.5,'radius':4.5,'coneCosineThreshold':0.5,'coneHalfAngleDegrees':60,'verticalHalfRange':2.5,'baseDamage':20.0,'damageType':'ftboss:butcher_melee','sound':'minecraft:entity.player.attack.strong','soundPitch':0.65,'soundVolume':1.0,'successfulHitKnockback':0.45,'boneCrushIncluded':False,'weaponDevelopmentPaused':True,'stage10EvidenceReusedWithoutRerun':True}
(output/'confirmed-gap-and-callsite.json').write_text(json.dumps(audit,ensure_ascii=False,indent=2),encoding='utf-8')
helper=WORK/'tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
content=helper.read_text(encoding='utf-8');exec(compile(content[:content.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
replace=module.replace
text=replace(text,'ButcherSkillRules.Skill.BLOOD_BRAND,ButcherSkillRules.Skill.BLOOD_TIDE};','ButcherSkillRules.Skill.BLOOD_BRAND,ButcherSkillRules.Skill.BLOOD_TIDE,ButcherSkillRules.Skill.CLEAVER_SWEEP};')
text=replace(text,'private static final Map<MeatMountainEntity,State> STATES=new WeakHashMap<>();','private static final ResourceKey<DamageType> MELEE=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("ftboss","butcher_melee"));\n    private static final Map<MeatMountainEntity,State> STATES=new WeakHashMap<>();')
text=replace(text,'SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.HOSTILE,1.0F,0.85F','state.skill.melee()?SoundEvents.PLAYER_ATTACK_STRONG:SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.HOSTILE,1.0F,state.skill.melee()?0.65F:0.85F')
text=replace(text,'getHolderOrThrow(BLOOD)','getHolderOrThrow(state.skill.melee()?MELEE:BLOOD)')
text=replace(text,'&& (state.skill!=ButcherSkillRules.Skill.BLOOD_TIDE || ButcherSkillRules.inCone(offsetX,offsetZ,state.forward.x,state.forward.z,radius,Math.sqrt(0.5)))) target.hurt(source,amount);','&& (state.skill!=ButcherSkillRules.Skill.CLEAVER_SWEEP && state.skill!=ButcherSkillRules.Skill.BLOOD_TIDE || ButcherSkillRules.inCone(offsetX,offsetZ,state.forward.x,state.forward.z,radius,state.skill==ButcherSkillRules.Skill.CLEAVER_SWEEP?0.5:Math.sqrt(0.5)))\n                && target.hurt(source,amount) && state.skill.melee()) target.knockback(0.45,entity.getX()-target.getX(),entity.getZ()-target.getZ());')
module.create(WORK/'proposal-stage11-cleaver-sweep/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java',text)
builder=(WORK/'tools/build_boss_blood_tide_stage10_v2.py').read_text(encoding='utf-8')
builder=builder.replace('blood-tide-baseline-eb7853cb','cleaver-sweep-baseline-5db0470f').replace('boss-core-stage09-blood-brand-20261112/ftboss-boss-core-1.21.1-stage09.jar','boss-core-stage10-blood-tide-20261112/ftboss-boss-core-1.21.1-stage10.jar').replace('85de7ad1daa2fc35663a80ac2a5d551b44797ffe8dc72b2d80d2dd832d4a36b0','6f3482b32f0361c664f5e524d44ee48e3d9965198300ea5b37484e299a57a211').replace('boss-blood-tide-v2','boss-cleaver-sweep-v1').replace('proposal-stage10-blood-tide','proposal-stage11-cleaver-sweep').replace('tests/blood-tide-boundary-v2/BloodTideBoundaryProof.java','tests/CleaverSweepBoundaryProof.java').replace('BloodTideBoundaryProof','CleaverSweepBoundaryProof').replace('stage10-blood-tide','stage11-cleaver-sweep')
builder=replace(builder,'assert len(overlay)==2','assert len(overlay)==2\n+overlay={name:payload for name,payload in overlay.items() if name.endswith("/ButcherBloodBrand.class")}\n+assert len(overlay)==1'.replace('\n+','\n'))
builder=replace(builder,'callsite,Path(__file__))','callsite,WORK/"build/boss-cleaver-sweep-audit-20261112/confirmed-gap-and-callsite.json",Path(__file__))')
builder=builder.replace('Original Blood Tide server windup/release with shared original per-skill cooldowns and two-skill rotation; other two active skills, summon and client motion deferred','Original Cleaver Sweep release, 60-degree cone, melee source/sound/knockback and one additional entry in the unchanged shared scheduler; Bone Crush, summon and client motion deferred')
module.create(WORK/'tools/build_boss_cleaver_sweep_stage11.py',builder)
print(json.dumps({'gapConfirmed':True,'selectedSkill':'CLEAVER_SWEEP','baselineCallbacks':audit['nativeServerCallbacks'],'scopedProposalPrepared':True}),flush=True)
