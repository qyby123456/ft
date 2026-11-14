import importlib.util
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
helper=WORK/'tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
create=module.create
replace=module.replace

probe=(WORK/'tests/blood-tide-v1/BossBloodTideRuntimeProbe.java').read_text(encoding='utf-8')
probe=probe.replace('BossBloodTideRuntimeProbe','BossCleaverSweepRuntimeProbe').replace('butcher_blood_tide','butcher_cleaver_sweep').replace('ftboss.bloodtide.mode','ftboss.cleaversweep.mode').replace('actual-blood-tide-probe.json','actual-cleaver-sweep-probe.json').replace('actualBloodTideMissingDemonstrated','actualCleaverSweepMissingDemonstrated').replace('bloodTideFeatureAccepted','cleaverSweepFeatureAccepted')
probe=replace(probe,'import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;','import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;\nimport net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;\nimport net.neoforged.neoforge.event.PlayLevelSoundEvent;\nimport net.minecraft.sounds.SoundEvents;')
probe=replace(probe,'private static boolean previouslyBrand;','private static boolean previouslyBrand;\n    private static final List<Map<String,Object>> knockbacks=new ArrayList<>();\n    private static final List<Map<String,Object>> sounds=new ArrayList<>();\n    private static final List<Long> tideStarts=new ArrayList<>();\n    private static boolean previouslyTide;')
probe=replace(probe,'NeoForge.EVENT_BUS.addListener(BossCleaverSweepRuntimeProbe::incomingDamage);','NeoForge.EVENT_BUS.addListener(BossCleaverSweepRuntimeProbe::incomingDamage);\n        NeoForge.EVENT_BUS.addListener(BossCleaverSweepRuntimeProbe::knockback);\n        NeoForge.EVENT_BUS.addListener(BossCleaverSweepRuntimeProbe::sound);')
probe=replace(probe,'report.put("allBloodDamageEvents",allBloodDamages);','report.put("allNativeDamageEvents",allBloodDamages);\n        report.put("nativeSweepKnockbackEvents",knockbacks);\n        report.put("nativeSweepSoundEvents",sounds);')
probe=replace(probe,'index<4;index++','index<5;index++')
probe=replace(probe,'animal.moveTo(index==2?0.5:6.5,70.0+(index==3?2.6:0.0),index==1?2.5:(index==2?6.5:0.5),0.0F,0.0F);','position(animal,index,false);')
probe=replace(probe,'height<=74;height++) for (int depth=-1;depth<=1;depth++)','height<=75;height++) for (int depth=-5;depth<=5;depth++)')
probe=replace(probe,'new BlockPos(4,height,depth)','new BlockPos(2,height,depth)')
probe=replace(probe,'previouslyBrand=false;','previouslyBrand=false;\n        previouslyTide=false;\n        tideStarts.clear();')
probe=replace(probe,'if (!key.equals("ftboss:butcher_blood")) return;','if (!key.equals("ftboss:butcher_melee")) return;',2)
probe=replace(probe,'14.0*(attack/5.5)','20.0*(attack/5.5)')
probe=replace(probe,'if (!key.equals("ftboss:butcher_melee")) return;\n        Map<String,Object> row','Map<String,Object> row')
probe=replace(probe,'if (ACTION.equals(owner.getButcherAction())) damages.add(row);','if (key.equals("ftboss:butcher_melee") && ACTION.equals(owner.getButcherAction())) damages.add(row);')
probe=replace(probe,'if (scenarioTicks>600)','if (scenarioTicks>800)')
probe=replace(probe,'double targetX=scenario==4?20.5:(index==2?0.5:6.5);\n                animal.moveTo(targetX,70.0+(index==3?2.6:0.0),index==1?2.5:(index==2 || index==0 && moveAway?6.5:0.5),0.0F,0.0F);','position(animal,index,moveAway);')
probe=replace(probe,'boolean casting=ACTION.equals(owner.getButcherAction());','boolean tidal="butcher_blood_tide".equals(owner.getButcherAction());\n            if (tidal && !previouslyTide) tideStarts.add(now);\n            previouslyTide=tidal;\n            require(!"butcher_bone_crush".equals(owner.getButcherAction()),"UnrequestedBoneCrushRemainsDisabled"+totalTicks);\n            boolean casting=ACTION.equals(owner.getButcherAction());')
probe=replace(probe,'int duration=scenario==0?490:(scenario==5?260:240);','int duration=scenario==0?710:(scenario==5?370:350);')
probe=replace(probe,'row.put("brandStarts",new ArrayList<>(brandStarts));','row.put("brandStarts",new ArrayList<>(brandStarts));\n        row.put("tideStarts",new ArrayList<>(tideStarts));')
probe=replace(probe,'starts.getFirst()-firstTick==200,"OriginalFirstBrand80ThenTide200"','starts.getFirst()-firstTick==314,"OriginalFirstSweepAfterBrand80AndTide200"')
probe=replace(probe,'starts.get(1)-starts.getFirst()==240,"OriginalTwoSkillRotationInterval240"','starts.get(1)-starts.getFirst()==338,"OriginalThreeSkillRotationInterval338"')
probe=replace(probe,'ends.get(0)-starts.get(0)==34 && ends.get(1)-starts.get(1)==34,"OriginalWindupPlusRecovery34"','ends.get(0)-starts.get(0)==24 && ends.get(1)-starts.get(1)==24,"OriginalWindupPlusRecovery24"')
probe=replace(probe,'==22),"OriginalReleaseOnWindupTick22"','==14),"OriginalReleaseOnWindupTick14"')
probe=replace(probe,'"OriginalTideConeAndVerticalExemptions"','"OriginalSweepConeRadiusAndVerticalExemptions"')
probe=replace(probe,'brandStarts.size()==2 && brandStarts.getFirst()-firstTick==80 && brandStarts.get(1)-brandStarts.getFirst()==240,"OriginalBloodBrandTimingPreserved"','brandStarts.size()==2 && brandStarts.getFirst()-firstTick==80 && brandStarts.get(1)-brandStarts.getFirst()==338 && tideStarts.size()==2 && tideStarts.getFirst()-firstTick==200 && tideStarts.get(1)-tideStarts.getFirst()==338,"ExistingBloodSkillsRetainOriginalFirstDelayAndSharedRotation"')
probe=replace(probe,'"ObservedBloodBrandCastCountMatchesMode"','"ObservedCleaverSweepCastCountMatchesMode"')
probe=replace(probe,'"ActualNativeBloodDamageCountMatchesMode"','"ActualNativeSweepDamageCountMatchesMode"')
probe=replace(probe,'"CanceledWindupDealsNoBloodDamage"','"CanceledWindupDealsNoSweepDamage"')
probe=replace(probe,'private static void incomingDamage(LivingIncomingDamageEvent event) {','''private static void position(Cow animal,int index,boolean moveAway) {
        double offsetX=index==2?0.0:(index==1?3.5:(index==4?4.6:4.0));
        double offsetZ=index==2?4.0:(index==1?1.5:0.0);
        if (moveAway && index==0) offsetZ=6.0;
        if (scenario==4) offsetX+=6.0;
        animal.moveTo(0.5+offsetX,70.0+(index==3?2.6:0.0),0.5+offsetZ,0.0F,0.0F);
    }

    private static void knockback(LivingKnockBackEvent event) {
        if (owner==null || !animals.contains(event.getEntity()) || !ACTION.equals(owner.getButcherAction())) return;
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("scenario",scenario);
        row.put("targetIndex",animals.indexOf(event.getEntity()));
        row.put("gameTime",level.getGameTime());
        row.put("strength",event.getOriginalStrength());
        row.put("ratioX",event.getOriginalRatioX());
        row.put("ratioZ",event.getOriginalRatioZ());
        row.put("expectedRatioX",owner.getX()-event.getEntity().getX());
        row.put("expectedRatioZ",owner.getZ()-event.getEntity().getZ());
        knockbacks.add(row);
    }

    private static void sound(PlayLevelSoundEvent.AtPosition event) {
        if (owner==null || event.getLevel()!=level || !ACTION.equals(owner.getButcherAction())) return;
        if (event.getSound()==null || event.getSound().value()!=SoundEvents.PLAYER_ATTACK_STRONG || event.getOriginalVolume()!=1.0F) return;
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("scenario",scenario);
        row.put("gameTime",level.getGameTime());
        row.put("pitch",event.getOriginalPitch());
        row.put("volume",event.getOriginalVolume());
        row.put("sound","minecraft:entity.player.attack.strong");
        sounds.add(row);
    }

    private static void incomingDamage(LivingIncomingDamageEvent event) {''')
probe=replace(probe,'report.put("otherActiveSkillsAccepted",false);','report.put("otherActiveSkillsAccepted",false);\n        report.put("fullFourSkillRotationAccepted",false);')
probe=replace(probe,'require(brandStarts.size()==2', '''var explicitKnocks=knockbacks.stream().filter(value->((Integer)value.get("scenario"))==0 && Math.abs(((Float)value.get("strength"))-0.45F)<0.00001F).toList();
                require(explicitKnocks.size()==3 && explicitKnocks.stream().allMatch(value->Math.abs(((Double)value.get("ratioX"))-((Double)value.get("expectedRatioX")))<0.00001 && Math.abs(((Double)value.get("ratioZ"))-((Double)value.get("expectedRatioZ")))<0.00001),"OriginalNativeSweepKnockbackPoint45AndDirection");
                var releases=sounds.stream().filter(value->((Integer)value.get("scenario"))==0).toList();
                require(releases.size()==2 && releases.stream().allMatch(value->Math.abs(((Float)value.get("pitch"))-0.65F)<0.00001F),"OriginalStrongAttackSoundAndPoint65Pitch");
                require(brandStarts.size()==2''')
probe=replace(probe,'require(!"butcher_bone_crush".equals(owner.getButcherAction()),"UnrequestedBoneCrushRemainsDisabled"+totalTicks);','if ("butcher_bone_crush".equals(owner.getButcherAction())) throw new AssertionError("Unrequested Bone Crush enabled");')
create(WORK/'tests/cleaver-sweep-v1/BossCleaverSweepRuntimeProbe.java',probe)
runner=(WORK/'tools/run_boss_blood_tide_probe_v1.py').read_text(encoding='utf-8')
runner=runner.replace('blood-tide-v1/BossBloodTideRuntimeProbe.java','cleaver-sweep-v1/BossCleaverSweepRuntimeProbe.java').replace('blood-tide-','cleaver-sweep-').replace('ftboss.bloodtide.mode','ftboss.cleaversweep.mode').replace('bloodTideFeatureAccepted','cleaverSweepFeatureAccepted').replace('actualBloodTideMissingDemonstrated','actualCleaverSweepMissingDemonstrated').replace('Blood Tide with original Blood Brand regression','Cleaver Sweep alone with necessary shared existing-skill controls')
create(WORK/'tools/run_boss_cleaver_sweep_probe_v1.py',runner)
print(json.dumps({'prepared':True,'selectedSkill':'CLEAVER_SWEEP','weaponValidationResumed':False}),flush=True)
