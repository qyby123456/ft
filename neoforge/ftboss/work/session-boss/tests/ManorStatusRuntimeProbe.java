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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("ftboss_bossprobe")
public final class ManorStatusRuntimeProbe {
    private static final List<String> cases = new ArrayList<>();
    private static final Map<String, Object> report = new LinkedHashMap<>();
    private static final List<EasterBossEntity> constructed = new ArrayList<>();
    private static EasterBossEntity ticking;
    private static Cow control;
    private static int ticks;
    private static boolean active;

    public ManorStatusRuntimeProbe() {
        NeoForge.EVENT_BUS.addListener(ManorStatusRuntimeProbe::started);
        NeoForge.EVENT_BUS.addListener(ManorStatusRuntimeProbe::tick);
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        cases.add(label);
    }

    private static EasterBossEntity boss(net.minecraft.server.level.ServerLevel world, int variant) {
        var entity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("ftboss", "easter_boss")).create(world);
        if (!(entity instanceof EasterBossEntity actual)) throw new AssertionError("Native Boss construction failed");
        actual.setVariant(variant);
        constructed.add(actual);
        return actual;
    }

    private static void started(ServerStartedEvent event) {
        boolean baseline = System.getProperty("ftboss.status.mode", "baseline").equals("baseline");
        report.put("mode", baseline ? "baseline" : "candidate");
        report.put("cases", cases);
        report.put("failures", 0);
        report.put("fullBossMigrationAccepted", false);
        report.put("authenticatedMultiplayerAccepted", false);
        report.put("clientRenderingAccepted", false);
        report.put("privateStateOrAuthorityWrites", false);
        try {
            var world = event.getServer().overworld();
            for (int variant = 0; variant < 12; variant++) {
                EasterBossEntity actor = boss(world, variant);
                boolean healAdmission = actor.canBeAffected(new MobEffectInstance(FTEffects.HEAL_BAN, 600));
                boolean erosionAdmission = actor.canBeAffected(new MobEffectInstance(FTEffects.SHADOW_EROSION, 600));
                boolean healAdded = actor.addEffect(new MobEffectInstance(FTEffects.HEAL_BAN, 600));
                boolean erosionAdded = actor.addEffect(new MobEffectInstance(FTEffects.SHADOW_EROSION, 600));
                if (baseline) {
                    require(healAdmission && healAdded && actor.hasEffect(FTEffects.HEAL_BAN), "baselineNativeHealBanAccepted:" + variant);
                    require(erosionAdmission && erosionAdded && actor.hasEffect(FTEffects.SHADOW_EROSION), "baselineNativeShadowErosionAccepted:" + variant);
                } else {
                    require(!healAdmission && !healAdded && !actor.hasEffect(FTEffects.HEAL_BAN), "nativeHealBanBlocked:" + variant);
                    require(!erosionAdmission && !erosionAdded && !actor.hasEffect(FTEffects.SHADOW_EROSION), "nativeShadowErosionBlocked:" + variant);
                }
            }
            if (baseline) {
                report.put("actualMissingOriginalImmunityDemonstrated", true);
                finish(null);
                return;
            }
            control = EntityType.COW.create(world);
            require(control != null && control.addEffect(new MobEffectInstance(FTEffects.HEAL_BAN, 600)), "ordinaryEntityHealBanStillAllowed");
            require(control.addEffect(new MobEffectInstance(FTEffects.SHADOW_EROSION, 600)), "ordinaryEntityShadowErosionStillAllowed");
            EasterBossEntity allowed = boss(world, 0);
            require(allowed.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600)), "otherBossBeneficialEffectStillAllowed");
            require(allowed.canBeAffected(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600)), "otherBossNegativeEffectAdmissionPreserved");
            var spawn = world.getSharedSpawnPos();
            world.getChunk(spawn.getX() >> 4, spawn.getZ() >> 4);
            EasterBossEntity joining = boss(world, 0);
            joining.moveTo(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, 0, 0);
            joining.forceAddEffect(new MobEffectInstance(FTEffects.HEAL_BAN, 600), null);
            joining.forceAddEffect(new MobEffectInstance(FTEffects.SHADOW_EROSION, 600), null);
            joining.forceAddEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600), null);
            require(joining.hasEffect(FTEffects.HEAL_BAN) && joining.hasEffect(FTEffects.SHADOW_EROSION), "nativeForcedEffectsExistBeforeJoin");
            require(world.addFreshEntity(joining), "actualBossJoinsRealServerLevel");
            require(!joining.hasEffect(FTEffects.HEAL_BAN) && !joining.hasEffect(FTEffects.SHADOW_EROSION), "nativeEntityJoinPurgesBothForbiddenEffects");
            require(joining.hasEffect(MobEffects.MOVEMENT_SPEED), "nativeEntityJoinKeepsOtherEffects");
            ticking = boss(world, 0);
            ticking.moveTo(spawn.getX() + 4.5, spawn.getY() + 1, spawn.getZ() + 0.5, 0, 0);
            require(world.addFreshEntity(ticking), "actualTickingBossJoinsRealServerLevel");
            ticking.forceAddEffect(new MobEffectInstance(FTEffects.HEAL_BAN, 600), null);
            ticking.forceAddEffect(new MobEffectInstance(FTEffects.SHADOW_EROSION, 600), null);
            ticking.forceAddEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600), null);
            require(ticking.hasEffect(FTEffects.HEAL_BAN) && ticking.hasEffect(FTEffects.SHADOW_EROSION), "nativeForcedEffectsExistBeforeNaturalTick");
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
            require(ticking.tickCount >= 19, "actualBossAgeAdvancedNaturally");
            require(!ticking.hasEffect(FTEffects.HEAL_BAN) && !ticking.hasEffect(FTEffects.SHADOW_EROSION), "naturalLivingTickPurgesForcedForbiddenEffects");
            require(ticking.hasEffect(MobEffects.MOVEMENT_SPEED), "naturalLivingTickPreservesOtherEffects");
            require(control.hasEffect(FTEffects.HEAL_BAN) && control.hasEffect(FTEffects.SHADOW_EROSION), "ordinaryControlEffectsRemainUnchanged");
            report.put("actualNaturalPostTicks", ticks);
            report.put("actualBossAge", ticking.tickCount);
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
        for (EasterBossEntity actor : constructed) actor.discard();
        if (control != null) control.discard();
        try {
            Files.writeString(Path.of("actual-manor-status-probe.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception failureRecording) {
            throw new IllegalStateException(failureRecording);
        }
    }
}
