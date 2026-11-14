package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("ftboss_bossprobe")
public final class StatusRemovalRuntimeProbe {
    private static final Map<String, Object> report = new LinkedHashMap<>();
    private static final List<String> cases = new ArrayList<>();
    private static final List<LivingEntity> constructed = new ArrayList<>();
    private static final boolean baseline = System.getProperty("ftboss.removal.mode").equals("baseline");
    private static Cow expiringHeal;
    private static Cow expiringShadow;
    private static Cow canceledControl;
    private static EasterBossEntity ticking;
    private static EasterBossEntity canceledBoss;
    private static int bossCanceledCallbacks;
    private static int ticks;
    private static boolean active;

    public StatusRemovalRuntimeProbe() {
        NeoForge.EVENT_BUS.addListener(StatusRemovalRuntimeProbe::started);
        NeoForge.EVENT_BUS.addListener(StatusRemovalRuntimeProbe::tick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true, MobEffectEvent.Remove.class, StatusRemovalRuntimeProbe::priorCancellation);
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        cases.add(label);
    }

    private static void priorCancellation(MobEffectEvent.Remove event) {
        if (event.getEntity() == canceledControl && event.getEffect().equals(MobEffects.MOVEMENT_SPEED)) event.setCanceled(true);
        if (!baseline && event.getEntity() == canceledBoss
                && (event.getEffect().equals(FTEffects.HEAL_BAN) || event.getEffect().equals(FTEffects.SHADOW_EROSION))) {
            bossCanceledCallbacks++;
            event.setCanceled(true);
        }
    }

    private static Cow cow(ServerLevel world) {
        Cow actor = EntityType.COW.create(world);
        if (actor == null) throw new AssertionError("Actual vanilla cow creation failed");
        constructed.add(actor);
        return actor;
    }

    private static Cow affected(ServerLevel world, Holder<MobEffect> effect, String label) {
        Cow actor = cow(world);
        require(actor.addEffect(new MobEffectInstance(effect, 600)), "nativeEffectApplied:" + label);
        return actor;
    }

    private static EasterBossEntity boss(ServerLevel world) {
        var actual = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("ftboss", "easter_boss")).create(world);
        if (!(actual instanceof EasterBossEntity actor)) throw new AssertionError("Native Boss creation failed");
        actor.setVariant(0);
        constructed.add(actor);
        return actor;
    }

    private static void loadPersistedEffects(EasterBossEntity actor) {
        var uuid = actor.getUUID();
        float health = actor.getHealth();
        int age = actor.tickCount;
        var snapshot = actor.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        ListTag effects = new ListTag();
        effects.add(new MobEffectInstance(FTEffects.HEAL_BAN, 600).save());
        effects.add(new MobEffectInstance(FTEffects.SHADOW_EROSION, 600).save());
        effects.add(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600).save());
        snapshot.put("active_effects", effects);
        actor.load(snapshot);
        require(actor.getUUID().equals(uuid) && actor.getHealth() == health && actor.tickCount == age, "nativeNbtLoadPreservesBossIdentityHealthAge");
        require(actor.hasEffect(FTEffects.HEAL_BAN) && actor.hasEffect(FTEffects.SHADOW_EROSION), "nativeNbtLoadContainsForbiddenEffects");
    }

    private static void checkRemoval(ServerLevel world, Holder<MobEffect> effect, String name) {
        MobEffectInstance fresh = new MobEffectInstance(effect, 600);
        require(baseline ? fresh.getCures().contains(EffectCures.MILK) && fresh.getCures().contains(EffectCures.PROTECTED_BY_TOTEM) : fresh.getCures().isEmpty(), "nativeCureSet:" + name);
        Cow direct = affected(world, effect, name + ":direct");
        require(direct.removeEffect(effect) == baseline && direct.hasEffect(effect) != baseline, "nativeDirectRemoval:" + name);
        Cow all = affected(world, effect, name + ":all");
        require(all.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600)), "nativeSpeedControlApplied:" + name);
        require(all.removeAllEffects() && !all.hasEffect(MobEffects.MOVEMENT_SPEED) && all.hasEffect(effect) != baseline, "nativeRemoveAllPreservesOnlyProtectedEffect:" + name);
        Cow milk = affected(world, effect, name + ":milk");
        require(milk.removeEffectsCuredBy(EffectCures.MILK) == baseline && milk.hasEffect(effect) != baseline, "nativeMilkCure:" + name);
        Cow totem = affected(world, effect, name + ":totem");
        require(totem.removeEffectsCuredBy(EffectCures.PROTECTED_BY_TOTEM) == baseline && totem.hasEffect(effect) != baseline, "nativeTotemCure:" + name);
        Cow extended = affected(world, effect, name + ":extended");
        extended.getEffect(effect).getCures().add(EffectCures.MILK);
        require(extended.getEffect(effect).getCures().contains(EffectCures.MILK), "publicForeignCureExtensionPresent:" + name);
        require(extended.removeEffectsCuredBy(EffectCures.MILK) == baseline && extended.hasEffect(effect) != baseline, "nativeExtendedCureStillProtected:" + name);
    }

    private static void started(ServerStartedEvent event) {
        report.put("cases", cases);
        report.put("failures", 0);
        report.put("mode", baseline ? "baseline" : "candidate");
        report.put("authenticatedPlayersTested", false);
        report.put("clientRenderingAccepted", false);
        try {
            ServerLevel world = event.getServer().overworld();
            var spawn = world.getSharedSpawnPos();
            world.getChunk(spawn.getX() >> 4, spawn.getZ() >> 4);
            checkRemoval(world, FTEffects.HEAL_BAN, "HealBan");
            checkRemoval(world, FTEffects.SHADOW_EROSION, "ShadowErosion");
            Cow control = affected(world, MobEffects.MOVEMENT_SPEED, "vanillaDirectControl");
            require(control.removeEffect(MobEffects.MOVEMENT_SPEED), "nativeVanillaDirectRemovalUnchanged");
            require(control.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600)) && control.removeEffectsCuredBy(EffectCures.MILK), "nativeVanillaMilkCureUnchanged");
            canceledControl = affected(world, MobEffects.MOVEMENT_SPEED, "priorCanceledControl");
            require(!canceledControl.removeEffect(MobEffects.MOVEMENT_SPEED) && canceledControl.hasEffect(MobEffects.MOVEMENT_SPEED), "unrelatedPriorCancellationPreserved");
            expiringHeal = cow(world);
            expiringShadow = cow(world);
            expiringHeal.moveTo(spawn.getX() + 10.5, spawn.getY() + 1, spawn.getZ() + 0.5, 0, 0);
            expiringShadow.moveTo(spawn.getX() + 11.5, spawn.getY() + 1, spawn.getZ() + 0.5, 0, 0);
            world.getChunk(expiringHeal.blockPosition());
            require(world.addFreshEntity(expiringHeal) && world.addFreshEntity(expiringShadow), "actualExpiryActorsJoinRealServer");
            require(expiringHeal.addEffect(new MobEffectInstance(FTEffects.HEAL_BAN, 6)) && expiringShadow.addEffect(new MobEffectInstance(FTEffects.SHADOW_EROSION, 2)), "nativeShortDurationEffectsApplied");
            require(expiringHeal.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120)) && expiringShadow.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120)), "nativeExpiryControlEffectsApplied");
            expiringHeal.setHealth(expiringHeal.getMaxHealth() - 4);
            float before = expiringHeal.getHealth();
            expiringHeal.heal(2);
            require(expiringHeal.getHealth() == before, "healingStillBannedBeforeNaturalExpiry");
            EasterBossEntity joining = boss(world);
            joining.moveTo(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, 0, 0);
            loadPersistedEffects(joining);
            canceledBoss = joining;
            require(world.addFreshEntity(joining), "actualManorBossJoinsRealServer");
            require(!joining.hasEffect(FTEffects.HEAL_BAN) && !joining.hasEffect(FTEffects.SHADOW_EROSION) && joining.hasEffect(MobEffects.MOVEMENT_SPEED), "nativeBossJoinPurgeExceptionPreserved");
            if (!baseline) require(bossCanceledCallbacks >= 2, "lowestReceiveCanceledAllowsOriginalJoinPurge");
            ticking = boss(world);
            ticking.moveTo(spawn.getX() + 4.5, spawn.getY() + 1, spawn.getZ() + 0.5, 0, 0);
            require(world.addFreshEntity(ticking), "actualTickingBossJoinsRealServer");
            loadPersistedEffects(ticking);
            canceledBoss = ticking;
            report.put("actualMissingOriginalRemovalProtectionDemonstrated", baseline);
            active = true;
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private static void tick(ServerTickEvent.Post event) {
        if (!active) return;
        try {
            ticks++;
            if (ticks < 20) return;
            require(expiringHeal.tickCount >= 19 && expiringShadow.tickCount >= 19 && ticking.tickCount >= 19, "actualEntitiesNaturallyTicked");
            require(!expiringHeal.hasEffect(FTEffects.HEAL_BAN) && !expiringShadow.hasEffect(FTEffects.SHADOW_EROSION), "nativeNaturalExpirationUnchanged");
            require(expiringHeal.hasEffect(MobEffects.MOVEMENT_SPEED) && expiringShadow.hasEffect(MobEffects.MOVEMENT_SPEED), "nativeNaturalExpirationPreservesOtherEffects");
            float before = expiringHeal.getHealth();
            expiringHeal.heal(2);
            require(expiringHeal.getHealth() > before, "healingReturnsAfterNaturalExpiration");
            require(!ticking.hasEffect(FTEffects.HEAL_BAN) && !ticking.hasEffect(FTEffects.SHADOW_EROSION) && ticking.hasEffect(MobEffects.MOVEMENT_SPEED), "nativeBossTickPurgeExceptionPreserved");
            if (!baseline) require(bossCanceledCallbacks >= 4, "lowestReceiveCanceledAllowsOriginalTickPurge");
            report.put("actualNaturalPostTicks", ticks);
            report.put("bossCanceledCallbacks", bossCanceledCallbacks);
            finish(null);
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private static void finish(Throwable failure) {
        active = false;
        if (failure != null) {
            report.put("failures", 1);
            report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter();
            failure.printStackTrace(new java.io.PrintWriter(trace));
            report.put("trace", trace.toString());
        }
        for (LivingEntity actor : constructed) actor.discard();
        try {
            Files.writeString(Path.of("actual-status-removal-probe.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception recordingFailure) {
            throw new IllegalStateException(recordingFailure);
        }
    }
}
