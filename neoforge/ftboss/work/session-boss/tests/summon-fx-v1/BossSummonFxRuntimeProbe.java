package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.MeatBoss;
import net.farmingtales.ftboss.butcherskills.ButcherFxPlan;
import net.farmingtales.ftboss.butcherskills.ButcherSkillFx;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("ftboss_bossprobe")
public final class BossSummonFxRuntimeProbe {
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> rows=new ArrayList<>();
    private static final List<Entity> entities=new ArrayList<>();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static MinecraftServer server;
    private static ServerLevel level;
    private static Class<?> helper;
    private static Method add;
    private static Method tickSummons;
    private static MeatMountainEntity owner;
    private static Cow rejected;
    private static List<Object> initialPulses;
    private static Object previousLevel;
    private static Object previousBoss;
    private static long start;
    private static int stage;
    private static int callbacks;
    private static boolean finished;
    private static boolean candidate;

    public BossSummonFxRuntimeProbe() {
        NeoForge.EVENT_BUS.addListener(BossSummonFxRuntimeProbe::started);
        NeoForge.EVENT_BUS.addListener(BossSummonFxRuntimeProbe::tick);
        NeoForge.EVENT_BUS.addListener(BossSummonFxRuntimeProbe::admission);
    }

    private static void require(boolean condition,String name) {
        if (!condition) throw new AssertionError(name);
        checks.add(name);
    }

    private static Object read(Object object,String name) throws Exception {
        Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);
    }

    private static Object staticRead(Class<?> type,String name) throws Exception {
        Field field=type.getDeclaredField(name);field.setAccessible(true);return field.get(null);
    }

    private static void fixtureWrite(String name,Object value) throws Exception {
        Field field=MeatBoss.class.getDeclaredField(name);field.setAccessible(true);field.set(null,value);
    }

    private static List<Object> entries() throws Exception {
        Map<?,?> map=(Map<?,?>)staticRead(helper,"SUMMONS");Object pulses=map.get(level);
        return pulses==null?List.of():new ArrayList<>((List<?>)read(pulses,"entries"));
    }

    private static int budget() throws Exception {
        Object value=((Map<?,?>)staticRead(ButcherSkillFx.class,"BUDGETS")).get(level);
        return value==null?0:(Integer)read(value,"summons");
    }

    private static MeatMountainEntity butcher(double position) {
        MeatMountainEntity entity=(MeatMountainEntity)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("ftboss:meat_mountain")).create(level);
        entity.setNoAi(true);entity.setNoGravity(true);entity.setPersistenceRequired();entity.moveTo(position,70.0,0.5,0.0F,0.0F);entities.add(entity);return entity;
    }

    private static Cow cow(int index) {
        Cow entity=EntityType.COW.create(level);entity.setUUID(new UUID(0L,200L+index));entity.setNoAi(true);entity.setNoGravity(true);entity.moveTo(14.5+(index%8),70.0,14.5+(index/8),0.0F,0.0F);entities.add(entity);return entity;
    }

    private static boolean spawn(Entity entity) throws Exception {
        return (Boolean)add.invoke(null,level,entity);
    }

    private static void admission(EntityJoinLevelEvent event) {
        if (event.getEntity()==rejected) event.setCanceled(true);
    }

    private static void started(ServerStartedEvent event) {
        report.put("cases",checks);report.put("rows",rows);report.put("failures",0);
        report.put("authenticatedPlayerIngressAccepted",false);report.put("authenticatedRewardsAccepted",false);report.put("clientRenderingAccepted",false);report.put("completeBattleAccepted",false);report.put("weaponDevelopmentOrValidationPerformed",false);report.put("bossFixtureStaticLevelAndUuidWrittenByReflection",true);
        try {
            server=event.getServer();level=server.overworld();candidate="candidate".equals(System.getProperty("ftboss.summonfx.mode"));
            require(server.getPlayerList().getPlayerCount()==0,"NoAuthenticatedPlayerFabricated");
            try {Class.forName("net.farmingtales.ftboss.item.FoodWeaponFinalDamage");throw new AssertionError("Paused weapon implementation loaded");}catch(ClassNotFoundException absent){checks.add("PausedWeaponImplementationAbsent");}
            for (int horizontal=-18;horizontal<=18;horizontal++) for (int depth=-18;depth<=18;depth++) level.setBlock(new BlockPos(horizontal,69,depth),Blocks.STONE.defaultBlockState(),3);
            owner=butcher(0.5);require(level.addFreshEntity(owner),"ActualRegisteredButcherAdmitted");
            previousLevel=staticRead(MeatBoss.class,"level");previousBoss=staticRead(MeatBoss.class,"bossUUID");fixtureWrite("level",level);fixtureWrite("bossUUID",owner.getUUID());
            Method wave=MeatBoss.class.getDeclaredMethod("summonWave",int.class,int.class,LivingEntity.class);wave.setAccessible(true);
            int spawned=(Integer)wave.invoke(null,2,1,null);
            require(spawned==2,"OriginalWaveSpawnsTwoRealMinions");
            List<?> minions=(List<?>)staticRead(MeatBoss.class,"minions");require(minions.size()==2,"OriginalMinionRegistrationPreserved");
            for(Object id:minions){Entity entity=level.getEntity((UUID)id);require(entity instanceof LivingEntity,"OriginalWaveActualLivingEntityPresent");entities.add(entity);}
            if (!candidate) {
                try {Class.forName("net.farmingtales.ftboss.butcherskills.ButcherSummonFx");throw new AssertionError("Baseline unexpectedly has helper");}catch(ClassNotFoundException absent){checks.add("ActualBaselineSummonQueueAbsent");}
                require(budget()==0,"ActualBaselineWaveEmitsNoOriginalSummonParticles");report.put("actualSummonFxMissingDemonstrated",true);finish();return;
            }
            helper=Class.forName("net.farmingtales.ftboss.butcherskills.ButcherSummonFx");add=helper.getMethod("addWithFx",ServerLevel.class,Entity.class);tickSummons=helper.getMethod("tickSummons",ServerLevel.class,long.class);
            require(entries().size()==2 && budget()==32,"ActualWaveIngressStartsBothOriginalMinionPulses");
            for (int phase=0;phase<4;phase++) require(spawn(cow(phase)),"FourActualUuidPhaseControlsAdmitted"+phase);
            MeatMountainEntity extra=butcher(7.5);require(spawn(extra),"ActualButcherDirectSummonAdmitted");
            require(entries().size()==7 && budget()==124,"OriginalBossAndMinionImmediatePointCounts");
            initialPulses=entries();
            for (int index=0;index<4;index++) require((Integer)read(initialPulses.get(index+2),"phase")==index,"OriginalFloorModUuidPhase"+index);
            require((Boolean)read(initialPulses.getLast(),"boss"),"OriginalBossSpecificPulseFlag");
            ItemEntity item=new ItemEntity(level,0.5,72.0,0.5,new ItemStack(Items.STONE));entities.add(item);require(spawn(item) && entries().size()==7 && budget()==124,"AcceptedNonlivingAdmissionHasNoPulse");
            require(!spawn(entities.stream().filter(value->value instanceof Cow).findFirst().orElseThrow()) && entries().size()==7 && budget()==124,"RejectedDuplicateAdmissionHasNoPulse");
            rejected=cow(99);require(!spawn(rejected) && entries().size()==7 && budget()==124,"CanceledActualLivingAdmissionHasNoPulse");rejected=null;
            Cow moved=(Cow)entities.stream().filter(value->value instanceof Cow).findFirst().orElseThrow();Vec3 center=(Vec3)read(initialPulses.get(2),"center");moved.moveTo(40.5,75.0,40.5,0.0F,0.0F);require(read(entries().get(2),"center").equals(center),"OriginalPulseCenterRemainsSpawnPosition");
            start=level.getGameTime();
        } catch (Throwable failure) {fail(failure);}
    }

    private static void tick(ServerTickEvent.Post event) {
        if (finished || !candidate || helper==null) return;
        try {
            callbacks++;long now=level.getGameTime();long age=now-start;
            if (callbacks>110) throw new AssertionError("Bounded summon observation exceeded");
            List<Object> pulses=entries();
            if (age<=32) {
                Object group=((Map<?,?>)staticRead(helper,"SUMMONS")).get(level);
                require(group!=null && (Long)read(group,"lastTick")==now,"ActualButcherNativeTickServicesSummonQueue");
                int expected=0;
                for (Object pulse:pulses) {
                    int elapsed=(int)(now-(Long)read(pulse,"start"));
                    if (elapsed>0 && elapsed%4==(Integer)read(pulse,"phase")) expected+=ButcherFxPlan.summon((Boolean)read(pulse,"boss"),elapsed).size();
                }
                Object particleBudget=((Map<?,?>)staticRead(ButcherSkillFx.class,"BUDGETS")).get(level);
                int observed=particleBudget!=null && (Long)read(particleBudget,"tick")==now?budget():0;
                if (age>0) require(observed==Math.min(160,expected),"OriginalPhaseAndPerTickSummonBudget");
                int before=budget();tickSummons.invoke(null,level,now);tickSummons.invoke(null,level,now);require(budget()==before,"SameTickTwoButchersAndRepeatedCallsDeduplicate");
                if (age==4 || age==16 || age==32) {Map<String,Object> row=new LinkedHashMap<>();row.put("stage",stage);row.put("age",age);row.put("entries",pulses.size());row.put("expectedPoints",Math.min(160,expected));row.put("actualPoints",observed);rows.add(row);}
                return;
            }
            require(pulses.isEmpty() && !((Map<?,?>)staticRead(helper,"SUMMONS")).containsKey(level),"OriginalAgeGreaterThan32ExpiresAndRemovesLevelQueue");
            if (stage==0) {
                List<Vec3> centers=new ArrayList<>();
                for (int index=0;index<27;index++) {Cow entity=cow(300+index);require(spawn(entity),"CapacityActualLivingAdmission"+index);centers.add(entity.position());}
                List<Object> capped=entries();require(capped.size()==24,"OriginalQueueCapacity24");
                require(read(capped.getFirst(),"center").equals(centers.get(3)) && read(capped.getLast(),"center").equals(centers.getLast()),"OriginalOldestThreeEvictedInOrder");
                require(budget()==160,"OriginalSummonParticleCeiling160");
                stage=1;start=now;
            } else {report.put("summonFxFeatureAccepted",true);finish();}
        } catch(Throwable failure) {fail(failure);}
    }

    private static void fail(Throwable failure) {
        report.put("failures",1);report.put("failure",failure.toString());failure.printStackTrace();finish();
    }

    private static void finish() {
        if (finished) return;finished=true;
        try {
            for(Entity entity:entities) entity.discard();
            fixtureWrite("level",previousLevel);fixtureWrite("bossUUID",previousBoss);
            ((List<?>)staticRead(MeatBoss.class,"minions")).clear();
            report.put("nativeTickCallbacks",callbacks);report.put("completedStages",stage+1);
            Files.writeString(Path.of("actual-summon-fx-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
        }catch(Exception failure){throw new RuntimeException(failure);}
    }
}
