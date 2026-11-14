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
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("ftboss_bossprobe")
public final class BossBloodBrandRuntimeProbe {
    private static final String ACTION="butcher_blood_brand";
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> rows=new ArrayList<>();
    private static final List<Map<String,Object>> damages=new ArrayList<>();
    private static final List<Map<String,Object>> incoming=new ArrayList<>();
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

    public BossBloodBrandRuntimeProbe() {
        NeoForge.EVENT_BUS.addListener(BossBloodBrandRuntimeProbe::started);
        NeoForge.EVENT_BUS.addListener(BossBloodBrandRuntimeProbe::tick);
        NeoForge.EVENT_BUS.addListener(BossBloodBrandRuntimeProbe::damage);
        NeoForge.EVENT_BUS.addListener(BossBloodBrandRuntimeProbe::incomingDamage);
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
        report.put("failures",0);
        report.put("authenticatedRewardsAccepted",false);
        report.put("clientRenderingAccepted",false);
        report.put("completeBattleAccepted",false);
        report.put("authorityWrittenByReflection",false);
        try {
            server=event.getServer();
            level=server.overworld();
            candidate="candidate".equals(System.getProperty("ftboss.bloodbrand.mode"));
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
        for (int index=0;index<4;index++) {
            Cow animal=EntityType.COW.create(level);
            animal.setNoAi(true);
            animal.setNoGravity(true);
            animal.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
            animal.setHealth(1000.0F);
            animal.moveTo(8.5+(index==1?2.0:(index==2?3.1:0.0)),70.0+(index==3?2.6:0.0),0.5,0.0F,0.0F);
            require(level.addFreshEntity(animal),"Scenario"+scenario+"RegisteredNpcTarget"+index);
            animals.add(animal);
        }
        if (scenario==6) {
            for (int height=69;height<=74;height++) for (int depth=-1;depth<=1;depth++) {
                var position=new BlockPos(4,height,depth);
                level.setBlock(position,Blocks.STONE.defaultBlockState(),3);
                wall.add(position);
            }
        }
        starts=new ArrayList<>();
        ends=new ArrayList<>();
        scenarioTicks=0;
        firstTick=-1;
        previouslyCasting=false;
        targetLost=false;
        attackScaled=false;
    }

    private static void incomingDamage(LivingIncomingDamageEvent event) {
        if (!animals.contains(event.getEntity())) return;
        String key=event.getSource().typeHolder().unwrapKey().map(type->type.location().toString()).orElse("unknown");
        if (!key.equals("ftboss:butcher_blood")) return;
        double attack=owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float expected=(float)Math.min(Float.MAX_VALUE,16.0*(attack/5.5))*2.0F;
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
        if (!key.equals("ftboss:butcher_blood")) return;
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("scenario",scenario);
        row.put("targetIndex",animals.indexOf(event.getEntity()));
        row.put("gameTime",level.getGameTime());
        row.put("actualDamage",event.getNewDamage());
        row.put("actualSourceIsButcher",event.getSource().getEntity()==owner);
        damages.add(row);
    }

    private static void tick(ServerTickEvent.Post event) {
        if (finished || server==null || event.getServer()!=server) return;
        try {
            totalTicks++;
            scenarioTicks++;
            if (scenarioTicks>400) throw new AssertionError("Scenario tick budget exceeded");
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
                double offset=index==1?2.0:(index==2?3.1:0.0);
                double targetX=scenario==4?20.5:8.5;
                animal.moveTo(targetX+offset+(index==0 && moveAway?4.0:0.0),70.0+(index==3?2.6:0.0),0.5,0.0F,0.0F);
                animal.setDeltaMovement(0.0,0.0,0.0);
            }
            if (scenario==5 && !starts.isEmpty() && now-starts.getFirst()>=10 && !targetLost) {
                animals.getFirst().discard();
                targetLost=true;
            }
            owner.setTarget(targetLost?null:animals.getFirst());
            owner.tick();
            boolean casting=ACTION.equals(owner.getButcherAction());
            if (casting && !previouslyCasting) starts.add(now);
            if (!casting && previouslyCasting) ends.add(now);
            previouslyCasting=casting;
            int duration=scenario==0?370:(scenario==5?150:120);
            if (scenarioTicks==duration) complete();
        } catch (Throwable failure) { fail(failure); }
    }

    private static void complete() {
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("scenario",scenario);
        row.put("firstAiTick",firstTick);
        row.put("castStarts",starts);
        row.put("castEnds",ends);
        row.put("npcHealth",animals.stream().map(Cow::getHealth).toList());
        rows.add(row);
        var hits=damages.stream().filter(hit->((Integer)hit.get("scenario"))==scenario).toList();
        if (scenario==0) {
            require(starts.size()==(candidate?2:0),"ObservedBloodBrandCastCountMatchesMode");
            require(hits.size()==(candidate?3:0),"ActualNativeBloodDamageCountMatchesMode");
            if (candidate) {
                require(starts.getFirst()-firstTick==80,"OriginalFirstCastDelay80");
                require(starts.get(1)-starts.getFirst()==240,"OriginalPerSkillCooldown240");
                require(ends.size()==2 && ends.get(0)-starts.get(0)==40 && ends.get(1)-starts.get(1)==40,"OriginalWindupPlusRecovery40");
                var originalAmounts=incoming.stream().filter(hit->((Integer)hit.get("scenario"))==scenario).toList();
                require(originalAmounts.size()==3 && originalAmounts.stream().allMatch(hit->Math.abs(((Float)hit.get("originalIncomingAmount"))-((Float)hit.get("expectedOriginalScaledDamage")))<0.0001F),"NativeIncomingDamageMatchesFrozenOriginalAttributeFormula");
                require(hits.stream().allMatch(hit->Boolean.TRUE.equals(hit.get("actualSourceIsButcher")) && ((Float)hit.get("actualDamage"))>0.0F),"ActualNativeBloodDamageAndSourceOwner");
                require(((Double)originalAmounts.get(2).get("liveAttackAttribute"))>((Double)originalAmounts.get(0).get("liveAttackAttribute")),"PublicAttackModifierControlChangesOriginalScaledDamage");
                require(hits.stream().allMatch(hit->((Long)hit.get("gameTime"))-(((Long)hit.get("gameTime"))<starts.get(1)?starts.get(0):starts.get(1))==28),"OriginalReleaseOnWindupTick28");
                float expectedTargetHealth=1000.0F-(Float)hits.get(0).get("actualDamage");
                float expectedNearHealth=1000.0F-(Float)hits.get(1).get("actualDamage")-(Float)hits.get(2).get("actualDamage");
                require(Math.abs(animals.get(0).getHealth()-expectedTargetHealth)<0.001F && Math.abs(animals.get(1).getHealth()-expectedNearHealth)<0.001F,"MovedTargetEscapesLockedCenterWhileNearbyNpcStillHit");
                require(animals.get(2).getHealth()==1000.0F && animals.get(3).getHealth()==1000.0F,"OriginalRadiusThreeAndVerticalTwoPointFiveExemptions");
            }
        } else if (scenario==5 && candidate) {
            require(starts.size()==1 && ends.size()==1 && ends.getFirst()-starts.getFirst()==10,"LostNativeTargetCancelsPendingSkill");
            require(hits.isEmpty(),"CanceledWindupDealsNoBloodDamage");
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
        report.put("actualBloodBrandMissingDemonstrated",!candidate && scenario==7);
        report.put("bloodBrandFeatureAccepted",candidate && scenario==7);
        report.put("otherActiveSkillsAccepted",false);
        cleanup();
        try { Files.writeString(Path.of("actual-blood-brand-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report)); } catch (Exception failure) { throw new RuntimeException(failure); }
    }
}
