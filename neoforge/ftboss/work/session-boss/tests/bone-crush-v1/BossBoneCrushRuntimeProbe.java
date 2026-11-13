package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("ftboss_bossprobe")
public final class BossBoneCrushRuntimeProbe {
    private static final String ACTION="butcher_bone_crush";
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> rows=new ArrayList<>();
    private static final List<Map<String,Object>> damages=new ArrayList<>();
    private static final List<Map<String,Object>> incoming=new ArrayList<>();
    private static final List<Map<String,Object>> allBloodDamages=new ArrayList<>();
    private static final List<Long> brandStarts=new ArrayList<>();
    private static boolean previouslyBrand;
    private static final List<Map<String,Object>> knockbacks=new ArrayList<>();
    private static final List<Map<String,Object>> sounds=new ArrayList<>();
    private static final List<Long> tideStarts=new ArrayList<>();
    private static boolean previouslyTide;
    private static final List<Long> sweepStarts=new ArrayList<>();
    private static boolean previouslySweep;
    private static final List<Cow> animals=new ArrayList<>();
    private static final List<BlockPos> wall=new ArrayList<>();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static MinecraftServer server;
    private static ServerLevel level;
    private static MeatMountainEntity owner;
    private static List<Long> starts;
    private static List<Long> ends;
    private static long firstTick;
    private static int scenario;
    private static int scenarioTicks;
    private static int totalTicks;
    private static boolean candidate;
    private static boolean finished;
    private static boolean previouslyCasting;
    private static boolean targetLost;
    private static boolean attackScaled;

    public BossBoneCrushRuntimeProbe() {
        NeoForge.EVENT_BUS.addListener(BossBoneCrushRuntimeProbe::started);
        NeoForge.EVENT_BUS.addListener(BossBoneCrushRuntimeProbe::tick);
        NeoForge.EVENT_BUS.addListener(BossBoneCrushRuntimeProbe::damage);
        NeoForge.EVENT_BUS.addListener(BossBoneCrushRuntimeProbe::incomingDamage);
        NeoForge.EVENT_BUS.addListener(BossBoneCrushRuntimeProbe::knockback);
        NeoForge.EVENT_BUS.addListener(BossBoneCrushRuntimeProbe::sound);
    }

    private static void require(boolean condition,String label) {
        if (!condition) throw new AssertionError(label);
        checks.add(label);
    }

    private static void started(ServerStartedEvent event) {
        report.put("cases",checks);
        report.put("rows",rows);
        report.put("nativeBloodDamageEvents",damages);
        report.put("nativeBloodIncomingEvents",incoming);
        report.put("allNativeDamageEvents",allBloodDamages);
        report.put("nativeBoneCrushKnockbackEvents",knockbacks);
        report.put("nativeBoneCrushSoundEvents",sounds);
        report.put("brandStarts",brandStarts);
        report.put("failures",0);
        report.put("authenticatedRewardsAccepted",false);
        report.put("clientRenderingAccepted",false);
        report.put("completeBattleAccepted",false);
        report.put("authorityWrittenByReflection",false);
        try {
            server=event.getServer();
            level=server.overworld();
            candidate="candidate".equals(System.getProperty("ftboss.bonecrush.mode"));
            require(server.getPlayerList().getPlayerCount()==0,"NoPlayerOrAuthenticationFabricated");
            try { Class.forName("net.farmingtales.ftboss.item.FoodWeaponFinalDamage"); throw new AssertionError("Paused weapon implementation loaded"); } catch (ClassNotFoundException absent) { checks.add("PausedWeaponImplementationAbsent"); }
            prepare();
        } catch (Throwable failure) { fail(failure); }
    }

    private static void cleanup() {
        if (owner!=null) owner.discard();
        for (Cow animal:animals) animal.discard();
        animals.clear();
        for (BlockPos position:wall) level.setBlock(position,Blocks.AIR.defaultBlockState(),3);
        wall.clear();
    }

    private static void prepare() {
        cleanup();
        owner=(MeatMountainEntity)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("ftboss:meat_mountain")).create(level);
        owner.moveTo(0.5,70.0,0.5,0.0F,0.0F);
        owner.setNoGravity(true);
        owner.setNoAi(true);
        owner.tick();
        int phase=scenario==1?1:(scenario==2?3:2);
        for (int current=1;current<phase;current++) {
            owner.setHealth(1.0F);
            owner.invulnerableTime=0;
            owner.hurt(level.damageSources().magic(),30.0F);
        }
        require(owner.getButcherPhase()==phase && owner.isAlive(),"Scenario"+scenario+"NaturalPhaseSetup");
        owner.setNoAi(scenario==3);
        for (int index=0;index<5;index++) {
            Cow animal=EntityType.COW.create(level);
            animal.setNoAi(true);
            animal.setNoGravity(true);
            animal.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
            animal.setHealth(1000.0F);
            position(animal,index,false);
            require(level.addFreshEntity(animal),"Scenario"+scenario+"RegisteredNpcTarget"+index);
            animals.add(animal);
        }
        if (scenario==6) {
            for (int height=69;height<=75;height++) for (int depth=-5;depth<=5;depth++) {
                var position=new BlockPos(2,height,depth);
                level.setBlock(position,Blocks.STONE.defaultBlockState(),3);
                wall.add(position);
            }
        }
        starts=new ArrayList<>();
        brandStarts.clear();
        previouslyBrand=false;
        previouslyTide=false;
        tideStarts.clear();
        sweepStarts.clear();
        previouslySweep=false;
        ends=new ArrayList<>();
        scenarioTicks=0;
        firstTick=-1;
        previouslyCasting=false;
        targetLost=false;
        attackScaled=false;
    }

    private static void position(Cow animal,int index,boolean moveAway) {
        double offsetX=index==2?0.0:(index==1?-2.8:(index==4?3.1:2.8));
        double offsetZ=index==2?2.8:0.0;
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

    private static void incomingDamage(LivingIncomingDamageEvent event) {
        if (!animals.contains(event.getEntity())) return;
        String key=event.getSource().typeHolder().unwrapKey().map(type->type.location().toString()).orElse("unknown");
        if (!key.equals("ftboss:butcher_melee")) return;
        if (!ACTION.equals(owner.getButcherAction())) return;
        double attack=owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float expected=(float)Math.min(Float.MAX_VALUE,28.0*(attack/5.5))*2.0F;
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("scenario",scenario);
        row.put("targetIndex",animals.indexOf(event.getEntity()));
        row.put("gameTime",level.getGameTime());
        row.put("liveAttackAttribute",attack);
        row.put("baseAttackAttribute",owner.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
        row.put("expectedOriginalScaledDamage",expected);
        row.put("originalIncomingAmount",event.getOriginalAmount());
        incoming.add(row);
    }

    private static void damage(LivingDamageEvent.Post event) {
        if (!animals.contains(event.getEntity())) return;
        String key=event.getSource().typeHolder().unwrapKey().map(type->type.location().toString()).orElse("unknown");
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("scenario",scenario);
        row.put("targetIndex",animals.indexOf(event.getEntity()));
        row.put("gameTime",level.getGameTime());
        row.put("actualDamage",event.getNewDamage());
        row.put("actualSourceIsButcher",event.getSource().getEntity()==owner);
        allBloodDamages.add(row);
        if (key.equals("ftboss:butcher_melee") && ACTION.equals(owner.getButcherAction())) damages.add(row);
    }

    private static void tick(ServerTickEvent.Post event) {
        if (finished || server==null || event.getServer()!=server) return;
        try {
            totalTicks++;
            scenarioTicks++;
            if (scenarioTicks>1200) throw new AssertionError("Scenario tick budget exceeded");
            long now=level.getGameTime();
            if (firstTick<0) firstTick=now;
            owner.moveTo(0.5,70.0,0.5,0.0F,0.0F);
            owner.setDeltaMovement(0.0,0.0,0.0);
            owner.setHealth(owner.getMaxHealth());
            boolean moveAway=scenario==0 && starts.size()==2 && now-starts.get(1)>=10;
            if (candidate && moveAway && !attackScaled) {
                owner.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(ResourceLocation.parse("ftboss_probe:blood_brand_attack"),5.5,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
                attackScaled=true;
            }
            for (int index=0;index<animals.size();index++) {
                Cow animal=animals.get(index);
                if (animal.isRemoved()) continue;
                position(animal,index,moveAway);
                animal.setDeltaMovement(0.0,0.0,0.0);
            }
            if (scenario==5 && !starts.isEmpty() && now-starts.getFirst()>=10 && !targetLost) {
                animals.getFirst().discard();
                targetLost=true;
            }
            owner.setTarget(targetLost?null:animals.getFirst());
            owner.tick();
            boolean branding="butcher_blood_brand".equals(owner.getButcherAction());
            if (branding && !previouslyBrand) brandStarts.add(now);
            previouslyBrand=branding;
            boolean tidal="butcher_blood_tide".equals(owner.getButcherAction());
            if (tidal && !previouslyTide) tideStarts.add(now);
            previouslyTide=tidal;
            boolean sweeping="butcher_cleaver_sweep".equals(owner.getButcherAction());
            if (sweeping && !previouslySweep) sweepStarts.add(now);
            previouslySweep=sweeping;
            boolean casting=ACTION.equals(owner.getButcherAction());
            if (casting && !previouslyCasting) starts.add(now);
            if (!casting && previouslyCasting) ends.add(now);
            previouslyCasting=casting;
            int duration=scenario==0?960:(scenario==5?460:450);
            if (scenarioTicks==duration) complete();
        } catch (Throwable failure) { fail(failure); }
    }

    private static void complete() {
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("scenario",scenario);
        row.put("firstAiTick",firstTick);
        row.put("castStarts",starts);
        row.put("castEnds",ends);
        row.put("brandStarts",new ArrayList<>(brandStarts));
        row.put("tideStarts",new ArrayList<>(tideStarts));
        row.put("sweepStarts",new ArrayList<>(sweepStarts));
        row.put("npcHealth",animals.stream().map(Cow::getHealth).toList());
        rows.add(row);
        var hits=damages.stream().filter(hit->((Integer)hit.get("scenario"))==scenario).toList();
        if (scenario==0) {
            require(starts.size()==(candidate?2:0),"ObservedBoneCrushCastCountMatchesMode");
            require(hits.size()==(candidate?5:0),"ActualNativeBoneCrushDamageCountMatchesMode");
            if (candidate) {
                require(starts.getFirst()-firstTick==418,"OriginalFirstBoneCrushAfterBrand80Tide200AndSweep314");
                require(starts.get(1)-starts.getFirst()==452,"OriginalFourSkillDirectedRotationInterval452");
                require(ends.size()==2 && ends.get(0)-starts.get(0)==34 && ends.get(1)-starts.get(1)==34,"OriginalWindup20PlusRecovery14");
                var originalAmounts=incoming.stream().filter(hit->((Integer)hit.get("scenario"))==scenario).toList();
                require(originalAmounts.size()==5 && originalAmounts.stream().allMatch(hit->Math.abs(((Float)hit.get("originalIncomingAmount"))-((Float)hit.get("expectedOriginalScaledDamage")))<0.0001F),"NativeIncomingDamageMatchesFrozenOriginalAttributeFormula");
                require(hits.stream().allMatch(hit->Boolean.TRUE.equals(hit.get("actualSourceIsButcher")) && ((Float)hit.get("actualDamage"))>0.0F),"ActualNativeBloodDamageAndSourceOwner");
                require(((Double)originalAmounts.get(4).get("liveAttackAttribute"))>((Double)originalAmounts.get(0).get("liveAttackAttribute")),"PublicAttackModifierControlChangesOriginalScaledDamage");
                require(hits.stream().allMatch(hit->((Long)hit.get("gameTime"))-(((Long)hit.get("gameTime"))<starts.get(1)?starts.get(0):starts.get(1))==20),"OriginalReleaseOnWindupTick20");
                for (int index=0;index<animals.size();index++) {
                    final int targetIndex=index;
                    double total=allBloodDamages.stream().filter(hit->((Integer)hit.get("scenario"))==scenario && ((Integer)hit.get("targetIndex"))==targetIndex).mapToDouble(hit->((Float)hit.get("actualDamage")).doubleValue()).sum();
                    require(Math.abs(animals.get(index).getHealth()-(1000.0-total))<0.002,"AllNativeDamageMatchesNpcHealth"+index);
                }
                require(hits.stream().map(hit->(Integer)hit.get("targetIndex")).toList().equals(List.of(0,1,2,1,2)),"OriginalCircularBoneCrushHitsBehindAndSideButNotOutsideRadiusOrVerticalRange");
                var explicitKnocks=knockbacks.stream().filter(value->((Integer)value.get("scenario"))==0 && Math.abs(((Float)value.get("strength"))-0.45F)<0.00001F).toList();
                require(explicitKnocks.size()==5 && explicitKnocks.stream().allMatch(value->Math.abs(((Double)value.get("ratioX"))-((Double)value.get("expectedRatioX")))<0.00001 && Math.abs(((Double)value.get("ratioZ"))-((Double)value.get("expectedRatioZ")))<0.00001),"OriginalNativeBoneCrushKnockbackPoint45AndDirection");
                var releases=sounds.stream().filter(value->((Integer)value.get("scenario"))==0).toList();
                require(releases.size()==2 && releases.stream().allMatch(value->Math.abs(((Float)value.get("pitch"))-0.65F)<0.00001F),"OriginalStrongAttackSoundAndPoint65Pitch");
                require(sweepStarts.size()==2 && sweepStarts.getFirst()-firstTick==314 && sweepStarts.get(1)-sweepStarts.getFirst()==452,"ExistingCleaverSweepRetainsOrderAndSharedRotation");
                require(brandStarts.size()==2 && brandStarts.getFirst()-firstTick==80 && brandStarts.get(1)-brandStarts.getFirst()==452 && tideStarts.size()==2 && tideStarts.getFirst()-firstTick==200 && tideStarts.get(1)-tideStarts.getFirst()==452,"ExistingBloodSkillsRetainOriginalFirstDelayAndFourSkillRotation");
            }
        } else if (scenario==5 && candidate) {
            require(starts.size()==1 && ends.size()==1 && ends.getFirst()-starts.getFirst()==10,"LostNativeTargetCancelsPendingSkill");
            require(hits.isEmpty(),"CanceledWindupDealsNoBoneCrushDamage");
        } else {
            require(starts.isEmpty() && hits.isEmpty(),"Scenario"+scenario+"PhaseAiRangeOrSightGuard");
        }
        scenario++;
        if (scenario==7) finish(); else prepare();
    }

    private static void fail(Throwable failure) {
        report.put("failures",1);
        report.put("failure",failure.toString());
        failure.printStackTrace();
        finish();
    }

    private static void finish() {
        if (finished) return;
        finished=true;
        report.put("nativeServerTickCallbacks",totalTicks);
        report.put("completedScenarios",scenario);
        report.put("actualBoneCrushMissingDemonstrated",!candidate && scenario==7);
        report.put("boneCrushFeatureAccepted",candidate && scenario==7);
        report.put("otherActiveSkillsAccepted",false);
        report.put("fullFourSkillRotationAccepted",false);
        cleanup();
        try { Files.writeString(Path.of("actual-bone-crush-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report)); } catch (Exception failure) { throw new RuntimeException(failure); }
    }
}
