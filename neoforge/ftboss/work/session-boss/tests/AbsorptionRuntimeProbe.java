package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.boss.ai.HealBan;
import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("ftboss_bossprobe")
public final class AbsorptionRuntimeProbe {
    private static final Map<String, Object> report = new LinkedHashMap<>();
    private static final List<String> cases = new ArrayList<>();
    private static Cow banned;
    private static Cow control;
    private static int ticks;
    private static boolean active;

    public AbsorptionRuntimeProbe() {
        NeoForge.EVENT_BUS.addListener(AbsorptionRuntimeProbe::started);
        NeoForge.EVENT_BUS.addListener(AbsorptionRuntimeProbe::tick);
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        cases.add(label);
    }

    private static void started(ServerStartedEvent event) {
        report.put("cases", cases);
        report.put("failures", 0);
        report.put("authenticatedPlayersTested", false);
        report.put("clientRenderingAccepted", false);
        try {
            var world = event.getServer().overworld();
            var spawn = world.getSharedSpawnPos();
            world.getChunk(spawn.getX() >> 4, spawn.getZ() >> 4);
            banned = EntityType.COW.create(world);
            control = EntityType.COW.create(world);
            require(banned != null && control != null, "actualVanillaLivingEntitiesConstructed");
            banned.moveTo(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, 0, 0);
            control.moveTo(spawn.getX() + 1.5, spawn.getY() + 1, spawn.getZ() + 0.5, 0, 0);
            require(world.addFreshEntity(banned) && world.addFreshEntity(control), "actualEntitiesAddedToRealLevel");
            require(banned.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1)), "absorptionEffectAcceptedBeforeBan");
            require(banned.hasEffect(MobEffects.ABSORPTION) && banned.getAbsorptionAmount() > 0, "preexistingAbsorptionActuallyPresent");
            require(banned.addEffect(new MobEffectInstance(FTEffects.HEAL_BAN, HealBan.BAN_TICKS)), "originalRegisteredHealBanEffectApplied");
            require(HealBan.isBanned(banned), "originalHealBanPredicateTrue");
            require(!banned.hasEffect(MobEffects.ABSORPTION) && banned.getAbsorptionAmount() == 0, "preexistingAbsorptionClearedImmediatelyAtNativeEffectAdded");
            require(!banned.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1)), "nativeEffectApplicableRejectsAbsorptionWhileBanned");
            require(!banned.hasEffect(MobEffects.ABSORPTION), "rejectedEffectNotInserted");
            require(control.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1)), "unbannedControlAbsorptionStillAllowed");
            require(control.getAbsorptionAmount() > 0, "unbannedControlAbsorptionNotCleared");
            banned.setHealth(banned.getMaxHealth() - 4);
            float beforeHeal = banned.getHealth();
            banned.heal(2);
            require(banned.getHealth() == beforeHeal, "originalNativeHealingBanStillBlocksRecovery");
            banned.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_ABSORPTION).setBaseValue(8);
            banned.setAbsorptionAmount(8);
            float health = banned.getHealth();
            require(banned.hurt(world.damageSources().generic(), 1), "actualNativeDamageApplied");
            require(banned.getAbsorptionAmount() == 0 && banned.getHealth() < health, "nativeIncomingDamageClearsRawAbsorptionBeforeDamage");
            banned.setAbsorptionAmount(8);
            require(banned.getAbsorptionAmount() == 8, "externalRawAbsorptionWritePresentBeforeNaturalTick");
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
            require(banned.tickCount >= 19, "actualEntityNaturallyTicked");
            require(banned.getAbsorptionAmount() == 0, "naturalLivingTickClearsRawAbsorptionDuringBan");
            require(control.hasEffect(MobEffects.ABSORPTION) && control.getAbsorptionAmount() > 0, "naturalUnbannedControlKeepsAbsorption");
            require(banned.removeEffect(FTEffects.HEAL_BAN) && !HealBan.isBanned(banned), "originalBanEffectCanBeRemovedNormally");
            require(banned.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1)), "absorptionAllowedAfterOriginalBanEnds");
            require(banned.getAbsorptionAmount() > 0, "restoredAbsorptionActuallyPresentAfterBan");
            report.put("actualPostTicks", ticks);
            report.put("actualBannedEntityAge", banned.tickCount);
            report.put("originalBanDurationTicks", HealBan.BAN_TICKS);
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
        if (banned != null) banned.discard();
        if (control != null) control.discard();
        try {
            Files.writeString(Path.of("actual-boss-absorption-probe.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception recordingFailure) {
            throw new IllegalStateException(recordingFailure);
        }
    }
}
