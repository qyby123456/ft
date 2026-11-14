package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.NonManorFinale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod("ftboss_nonmanorprobe")
public final class NonManorNormalBossProbeV2 {
    private static final ResourceLocation ADDED=ResourceLocation.fromNamespaceAndPath("ftboss_probe","ordinary_baseline_add_five");
    private static final ResourceLocation DOUBLE=ResourceLocation.fromNamespaceAndPath("ftboss","non_manor_finale_double");
    private static final List<String> TYPES=List.of("sugar_queen","meat_mountain","wine_barrel","rat_boss","defect_core","compost_boss","apple_boss","tea_tree","tide_fish_boss","benger");
    private static final Map<UUID,LivingEntity> subjects=new LinkedHashMap<>();
    private static final Map<UUID,Map<String,Object>> rows=new LinkedHashMap<>();
    private static final Map<UUID,Integer> entityTicks=new LinkedHashMap<>();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static MinecraftServer server;
    private static int checks;
    private static int ticks;
    private static boolean failed;
    private static boolean finished;

    public NonManorNormalBossProbeV2() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,EntityJoinLevelEvent.class,NonManorNormalBossProbeV2::joined);
        NeoForge.EVENT_BUS.addListener(NonManorNormalBossProbeV2::started);
        NeoForge.EVENT_BUS.addListener(NonManorNormalBossProbeV2::entityTick);
        NeoForge.EVENT_BUS.addListener(NonManorNormalBossProbeV2::serverTick);
        NeoForge.EVENT_BUS.addListener(NonManorNormalBossProbeV2::stopping);
    }

    private static void require(boolean condition,String label) {
        checks++;if (!condition) throw new AssertionError(label);
    }

    private static Map<String,Object> snapshot(LivingEntity subject) {
        Map<String,Object> data=new LinkedHashMap<>();
        data.put("uuid",subject.getUUID().toString());data.put("type",BuiltInRegistries.ENTITY_TYPE.getKey(subject.getType()).toString());
        data.put("health",subject.getHealth());data.put("maximumHealth",subject.getMaxHealth());data.put("alive",subject.isAlive());
        data.put("normalFinaleFlag",NonManorFinale.isFinale(subject));
        AttributeInstance maximum=subject.getAttribute(Attributes.MAX_HEALTH);AttributeInstance attack=subject.getAttribute(Attributes.ATTACK_DAMAGE);
        data.put("maximumHealthBase",maximum==null?null:maximum.getBaseValue());data.put("attackBase",attack==null?null:attack.getBaseValue());data.put("attackValue",attack==null?null:attack.getValue());
        List<Object> modifiers=new ArrayList<>();
        if (attack!=null) for (AttributeModifier modifier:attack.getModifiers()) modifiers.add(Map.of("id",modifier.id().toString(),"amount",modifier.amount(),"operation",modifier.operation().toString()));
        data.put("attackModifiers",modifiers);data.put("baseHpTagPresent",subject.getPersistentData().contains("ftboss_base_hp"));
        if (subject.getPersistentData().contains("ftboss_base_hp")) data.put("baseHpTag",subject.getPersistentData().getDouble("ftboss_base_hp"));
        data.put("naturalEntityPostTicks",entityTicks.getOrDefault(subject.getUUID(),0));return data;
    }

    private static void ordinary(LivingEntity subject,boolean withAdded,String phase) {
        require(subject.isAlive(),phase+": registered Boss alive");
        require(!NonManorFinale.isFinale(subject),phase+": no unearned finale admission");
        require(NonManorFinale.parallelSpells(subject)==1,phase+": ordinary parallel spell contract");
        for (int duration:new int[]{-1,0,1,2,3,99,Integer.MAX_VALUE}) require(NonManorFinale.acceleratedTicks(subject,duration)==duration,phase+": ordinary duration "+duration);
        AttributeInstance maximum=subject.getAttribute(Attributes.MAX_HEALTH);AttributeInstance attack=subject.getAttribute(Attributes.ATTACK_DAMAGE);
        require(maximum!=null && Double.isFinite(maximum.getBaseValue()) && maximum.getBaseValue()>0.0,phase+": native maximum-health base");
        require(maximum.getModifier(DOUBLE)==null,phase+": ordinary max-health not finale doubled");
        require(attack!=null,phase+": native Boss attack attribute");
        AttributeModifier added=attack.getModifier(ADDED);
        require(withAdded?added!=null && added.amount()==5.0 && added.operation()==AttributeModifier.Operation.ADD_VALUE:added==null,phase+": preexisting additive modifier preserved or absent");
        for (AttributeInstance attribute:subject.getAttributes().getSyncableAttributes()) require(attribute.getModifier(DOUBLE)==null,phase+": no finale modifier in syncable attributes");
    }

    private static void joined(EntityJoinLevelEvent event) {
        Map<String,Object> row=rows.get(event.getEntity().getUUID());if (row==null || event.getLevel().isClientSide()) return;
        try {
            require(!event.loadedFromDisk(),"initial admission is actual fresh-entity event");
            row.put("actualJoinEventObserved",true);row.put("joinLoadedFromDisk",event.loadedFromDisk());
            row.put("highestPriorityJoinSnapshot",snapshot((LivingEntity)event.getEntity()));
        } catch (Throwable failure) { fail(failure); }
    }

    private static void started(ServerStartedEvent event) {
        server=event.getServer();report.put("actualDedicatedServer",server.isDedicatedServer());report.put("actualServerStartedEvent",true);
        report.put("scenario","10 registered Boss types x ordinary/no-modifier and ordinary/permanent-ADD_VALUE-five fixtures");
        try {
            require(server.isDedicatedServer(),"actual dedicated server");require(server.getPlayerList().getPlayerCount()==0,"no player authentication or preference invented");
            ServerLevel level=server.overworld();BlockPos origin=level.getSharedSpawnPos();int index=0;
            for (String name:TYPES) for (boolean withAdded:new boolean[]{false,true}) {
                ResourceLocation key=ResourceLocation.fromNamespaceAndPath("ftboss",name);require(BuiltInRegistries.ENTITY_TYPE.containsKey(key),"actual registered Boss "+key);
                Entity created=BuiltInRegistries.ENTITY_TYPE.get(key).create(level);require(created instanceof LivingEntity,"native LivingEntity construction "+key);
                LivingEntity subject=(LivingEntity)created;
                if (subject instanceof net.farmingtales.ftboss.entity.MeatMountainEntity butcher) { butcher.setCloneButcher(true);require(butcher.isCloneButcher(),"original public clone initialization required by Butcher admission"); }
                int blockX=origin.getX()+(index%5)*10-20;int blockZ=origin.getZ()+(index/5)*10-15;int blockY=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,blockX,blockZ);
                if (name.equals("tide_fish_boss")) for (int offsetX=-2;offsetX<=2;offsetX++) for (int offsetZ=-2;offsetZ<=2;offsetZ++) for (int offsetY=-1;offsetY<=1;offsetY++) level.setBlock(new BlockPos(blockX+offsetX,blockY+offsetY,blockZ+offsetZ),Blocks.WATER.defaultBlockState(),3);
                level.setChunkForced(blockX>>4,blockZ>>4,true);subject.setPos(blockX+0.5,blockY+0.1,blockZ+0.5);
                Map<String,Object> row=new LinkedHashMap<>();row.put("type",key.toString());row.put("withPermanentAddedModifier",withAdded);row.put("constructorSnapshot",snapshot(subject));row.put("publicButcherCloneInitializationUsed",subject instanceof net.farmingtales.ftboss.entity.MeatMountainEntity);
                AttributeInstance attack=subject.getAttribute(Attributes.ATTACK_DAMAGE);require(attack!=null,"registered Boss supplies attack attribute "+key);
                require(attack.getModifier(ADDED)==null,"new fixture modifier id unused");
                if (withAdded) attack.addPermanentModifier(new AttributeModifier(ADDED,5.0,AttributeModifier.Operation.ADD_VALUE));
                rows.put(subject.getUUID(),row);subjects.put(subject.getUUID(),subject);entityTicks.put(subject.getUUID(),0);
                ordinary(subject,withAdded,"before-join "+key);boolean admitted=level.addFreshEntity(subject);row.put("nativeAddFreshEntityReturned",admitted);row.put("afterNativeAddAttemptSnapshot",snapshot(subject));require(admitted,"native level.addFreshEntity admitted "+key);
                require(Boolean.TRUE.equals(row.get("actualJoinEventObserved")),"real join event observed "+key);
                ordinary(subject,withAdded,"after-join "+key);row.put("afterCompleteJoinSnapshot",snapshot(subject));index++;
            }
            require(subjects.size()==20,"exactly twenty actual registered Boss fixtures");
            report.put("allTwentyNativeAdmissionsObserved",true);
        } catch (Throwable failure) { fail(failure); }
    }

    private static void entityTick(EntityTickEvent.Post event) {
        UUID id=event.getEntity().getUUID();if (entityTicks.containsKey(id) && !event.getEntity().level().isClientSide()) entityTicks.merge(id,1,Integer::sum);
    }

    private static void serverTick(ServerTickEvent.Post event) {
        if (server==null || failed || finished) return;ticks++;
        try {
            if (ticks==1 || ticks==20 || ticks==40) for (Map.Entry<UUID,LivingEntity> entry:subjects.entrySet()) {
                Map<String,Object> row=rows.get(entry.getKey());ordinary(entry.getValue(),Boolean.TRUE.equals(row.get("withPermanentAddedModifier")),"natural tick "+ticks+" "+row.get("type"));
                row.put("naturalTick"+ticks+"Snapshot",snapshot(entry.getValue()));
            }
            if (ticks==40) {
                for (Map.Entry<UUID,LivingEntity> entry:subjects.entrySet()) {
                    require(entityTicks.get(entry.getKey())>=20,"actual entity post callbacks, not direct tick invocation");
                    LivingEntity subject=entry.getValue();subject.setHealth(subject.getMaxHealth()*0.8F);
                    require(subject.getHealth()>0.0F && subject.getHealth()<subject.getMaxHealth(),"controlled nonfatal health fixture before normal save");
                    rows.get(entry.getKey()).put("controlledHealthFixtureRatio",0.8);
                }
                finished=true;report.put("nativeOrdinaryAdmissionAndNaturalTicksAccepted",true);write();
            }
        } catch (Throwable failure) { fail(failure); }
    }

    private static void stopping(ServerStoppingEvent event) {
        if (server==null) return;
        try {
            List<Object> saved=new ArrayList<>();
            for (Map.Entry<UUID,LivingEntity> entry:subjects.entrySet()) {
                ordinary(entry.getValue(),Boolean.TRUE.equals(rows.get(entry.getKey()).get("withPermanentAddedModifier")),"native server stopping snapshot");
                saved.add(snapshot(entry.getValue()));
            }
            report.put("serverStoppingSnapshots",saved);report.put("actualServerStoppingEventObserved",true);write();
        } catch (Throwable failure) { fail(failure); }
    }

    private static void fail(Throwable failure) {
        failed=true;StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.putIfAbsent("failure",trace.toString());System.err.print(trace);write();
    }

    private static void write() {
        report.put("checks",checks);report.put("failures",failed?1:0);report.put("naturalServerPostTicks",ticks);
        report.put("cases",new ArrayList<>(rows.values()));report.put("actualPlayerCount",server==null?0:server.getPlayerList().getPlayerCount());
        report.put("difficultyUnlockVictoryOrAuthenticationStateModified",false);report.put("actualImpossibleAdmissionAccepted",false);
        report.put("playerCountScalingChangeAccepted",false);report.put("newJvmReloadAccepted",false);report.put("productChanged",false);
        try { Files.writeString(Path.of("actual-nonmanor-normal-boss.json"),new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(report)); }
        catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
    }
}
