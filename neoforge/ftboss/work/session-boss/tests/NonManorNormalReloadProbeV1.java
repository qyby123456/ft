package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
public final class NonManorNormalReloadProbeV1 {
    private static final ResourceLocation ADDED=ResourceLocation.fromNamespaceAndPath("ftboss_probe","ordinary_baseline_add_five");
    private static final ResourceLocation DOUBLE=ResourceLocation.fromNamespaceAndPath("ftboss","non_manor_finale_double");
    private static final Map<UUID,JsonObject> expected=new LinkedHashMap<>();
    private static final Map<UUID,LivingEntity> subjects=new LinkedHashMap<>();
    private static final Map<UUID,Map<String,Object>> rows=new LinkedHashMap<>();
    private static final Map<UUID,Integer> entityTicks=new LinkedHashMap<>();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static MinecraftServer server;
    private static int checks;
    private static int ticks;
    private static int stableTicks;
    private static boolean failed;
    private static boolean finished;

    public NonManorNormalReloadProbeV1() {
        try {
            JsonObject seed=JsonParser.parseString(Files.readString(Path.of(System.getProperty("ftboss.nonmanor.seed")))).getAsJsonObject();
            require(seed.get("acceptedNormalSaveAndExitSeed").getAsBoolean(),"only the accepted ordinary normal-save seed");
            JsonArray cases=seed.getAsJsonArray("cases");
            for (var element:cases) {
                JsonObject row=element.getAsJsonObject();UUID id=UUID.fromString(row.getAsJsonObject("savedSnapshot").get("uuid").getAsString());
                require(expected.put(id,row)==null,"seed UUID unique");
            }
            require(expected.size()==20,"exactly twenty immutable seed UUIDs");
        } catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,EntityJoinLevelEvent.class,NonManorNormalReloadProbeV1::joined);
        NeoForge.EVENT_BUS.addListener(NonManorNormalReloadProbeV1::started);
        NeoForge.EVENT_BUS.addListener(NonManorNormalReloadProbeV1::entityTick);
        NeoForge.EVENT_BUS.addListener(NonManorNormalReloadProbeV1::serverTick);
        NeoForge.EVENT_BUS.addListener(NonManorNormalReloadProbeV1::stopping);
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

    private static void same(double actual,JsonObject saved,String key,String phase) {
        require(Math.abs(actual-saved.get(key).getAsDouble())<0.00001,phase+": persisted "+key+" actual="+actual+" expected="+saved.get(key));
    }

    private static void joined(EntityJoinLevelEvent event) {
        UUID id=event.getEntity().getUUID();JsonObject seed=expected.get(id);
        if (seed==null || event.getLevel().isClientSide()) return;
        try {
            require(event.loadedFromDisk(),"expected seed entity actually loaded from disk");
            require(event.getEntity() instanceof LivingEntity,"seed UUID belongs to living registered Boss");
            require(!subjects.containsKey(id),"one native reload join per original seed UUID");
            LivingEntity subject=(LivingEntity)event.getEntity();subjects.put(id,subject);entityTicks.put(id,0);
            JsonObject saved=seed.getAsJsonObject("savedSnapshot");
            Map<String,Object> row=new LinkedHashMap<>();rows.put(id,row);
            row.put("originalSavedSnapshot",saved);row.put("withPermanentAddedModifier",seed.get("withPermanentAddedModifier").getAsBoolean());
            row.put("actualJoinLoadedFromDisk",true);row.put("highestPriorityReloadSnapshot",snapshot(subject));
            require(BuiltInRegistries.ENTITY_TYPE.getKey(subject.getType()).toString().equals(saved.get("type").getAsString()),"same native registry type and original UUID");
            ordinary(subject,seed.get("withPermanentAddedModifier").getAsBoolean(),"actual disk join "+id);
            same(subject.getHealth(),saved,"health","actual disk join");same(subject.getMaxHealth(),saved,"maximumHealth","actual disk join");
            same(subject.getAttribute(Attributes.MAX_HEALTH).getBaseValue(),saved,"maximumHealthBase","actual disk join");
            same(subject.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue(),saved,"attackBase","actual disk join");
            require(subject.getPersistentData().contains("ftboss_base_hp")==saved.get("baseHpTagPresent").getAsBoolean(),"base HP tag presence persisted");
            if (saved.get("baseHpTagPresent").getAsBoolean()) same(subject.getPersistentData().getDouble("ftboss_base_hp"),saved,"baseHpTag","actual disk join");
            require(subject.getHealth()>0.0F && subject.getHealth()<subject.getMaxHealth(),"saved nonfatal health fixture persists, not regenerated full health");
            if (seed.get("publicButcherCloneInitializationUsed").getAsBoolean()) {
                require(subject instanceof net.farmingtales.ftboss.entity.MeatMountainEntity,"original public clone fixture native type");
                require(((net.farmingtales.ftboss.entity.MeatMountainEntity)subject).isCloneButcher(),"public clone state persisted without invoking setter on reload");
                row.put("persistedPublicCloneStateObserved",true);
            }
            row.put("persistedSnapshotChecksAccepted",true);
        } catch (Throwable failure) { fail(failure); }
    }

    private static void started(ServerStartedEvent event) {
        server=event.getServer();report.put("actualDedicatedServer",server.isDedicatedServer());report.put("actualServerStartedEvent",true);
        report.put("scenario","new JVM normal disk reload of exactly the prior twenty ordinary Boss UUIDs; no players");
        try { require(server.isDedicatedServer(),"actual dedicated native server");require(server.getPlayerList().getPlayerCount()==0,"no fabricated player login"); }
        catch (Throwable failure) { fail(failure); }
    }

    private static void entityTick(EntityTickEvent.Post event) {
        UUID id=event.getEntity().getUUID();if (entityTicks.containsKey(id) && !event.getEntity().level().isClientSide()) entityTicks.merge(id,1,Integer::sum);
    }

    private static void serverTick(ServerTickEvent.Post event) {
        if (server==null || failed || finished) return;ticks++;
        try {
            require(server.getPlayerList().getPlayerCount()==0,"ordinary reload has no actual players");
            require(ticks<=200,"bounded native loading of all twenty original entities");
            if (subjects.size()!=20) return;stableTicks++;
            if (stableTicks==1 || stableTicks==20 || stableTicks==40) for (Map.Entry<UUID,LivingEntity> entry:subjects.entrySet()) {
                LivingEntity subject=entry.getValue();JsonObject seed=expected.get(entry.getKey());
                require(server.overworld().getEntity(entry.getKey())==subject,"original UUID remains the same native loaded entity");
                ordinary(subject,seed.get("withPermanentAddedModifier").getAsBoolean(),"natural reload tick "+stableTicks);
                same(subject.getAttribute(Attributes.MAX_HEALTH).getBaseValue(),seed.getAsJsonObject("savedSnapshot"),"maximumHealthBase","natural reload tick");
                rows.get(entry.getKey()).put("naturalTick"+stableTicks+"Snapshot",snapshot(subject));
            }
            if (stableTicks==40) {
                for (UUID id:subjects.keySet()) require(entityTicks.get(id)>=20,"actual entity callbacks after natural reload, no direct tick invocation");
                finished=true;report.put("ordinaryNoPlayerNewJvmEntityReloadAccepted",true);write();
            }
        } catch (Throwable failure) { fail(failure); }
    }

    private static void stopping(ServerStoppingEvent event) {
        if (server==null) return;
        try {
            if (!finished || failed) { write();return; }
            List<Object> saved=new ArrayList<>();
            for (Map.Entry<UUID,LivingEntity> entry:subjects.entrySet()) {
                ordinary(entry.getValue(),expected.get(entry.getKey()).get("withPermanentAddedModifier").getAsBoolean(),"actual server stopping after reload");
                saved.add(snapshot(entry.getValue()));
            }
            report.put("serverStoppingSnapshots",saved);report.put("actualServerStoppingEventObserved",true);write();
        } catch (Throwable failure) { fail(failure); }
    }

    private static void fail(Throwable failure) {
        failed=true;StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.putIfAbsent("failure",trace.toString());System.err.print(trace);write();
    }

    private static void write() {
        report.put("checks",checks);report.put("failures",failed?1:0);report.put("naturalServerPostTicks",ticks);report.put("naturalTicksAfterAllTwentyLoaded",stableTicks);
        report.put("expectedOriginalUuids",expected.keySet().stream().map(UUID::toString).toList());report.put("cases",new ArrayList<>(rows.values()));
        report.put("actualPlayerCount",server==null?0:server.getPlayerList().getPlayerCount());
        report.put("difficultyUnlockVictoryOrAuthenticationStateModified",false);report.put("authenticatedSummonAccepted",false);report.put("curiosActualPlayerNewJvmLoginAccepted",false);
        report.put("actualImpossibleAdmissionAccepted",false);report.put("playerCountScalingChangeAccepted",false);report.put("productChanged",false);
        try { Files.writeString(Path.of("actual-nonmanor-normal-reload.json"),new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(report)); }
        catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
    }
}
