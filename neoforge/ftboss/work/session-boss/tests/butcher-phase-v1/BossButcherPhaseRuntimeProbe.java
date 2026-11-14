package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_bossprobe")
public final class BossButcherPhaseRuntimeProbe {
    private static MeatMountainEntity watched;
    private static int deaths;
    private static int canceled;

    public BossButcherPhaseRuntimeProbe() {
        NeoForge.EVENT_BUS.addListener(BossButcherPhaseRuntimeProbe::started);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, LivingDeathEvent.class, BossButcherPhaseRuntimeProbe::death);
    }

    private static void death(LivingDeathEvent event) {
        if (event.getEntity() == watched) { deaths++; if (event.isCanceled()) canceled++; }
    }

    private static void require(boolean condition, String label, List<String> cases) {
        if (!condition) throw new AssertionError(label);
        cases.add(label);
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<String> cases = new ArrayList<>();
        report.put("cases", cases);
        report.put("failures", 0);
        report.put("authenticatedMultiplayerAccepted", false);
        report.put("completeBattleAccepted", false);
        report.put("clientRenderingAccepted", false);
        report.put("authorityWrittenByReflection", false);
        try {
            boolean candidate = "candidate".equals(System.getProperty("ftboss.butcher.mode"));
            var level = event.getServer().overworld();
            var type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("ftboss:meat_mountain"));
            watched = (MeatMountainEntity) type.create(level);
            require(watched != null, "RegisteredNativeButcherConstructed", cases);
            watched.moveTo(0.5, 70.0, 0.5, 0.0F, 0.0F);
            require(level.getEntity(watched.getUUID()) == null, "UnaddedScratchNotAuthenticatedEncounter", cases);
            watched.tick();
            report.put("phaseAfterTick", watched.getPersistentData().getInt("ftboss_butcher_phase"));
            report.put("maxHealthBeforeDamage", watched.getMaxHealth());
            require(watched.getMaxHealth() == 380.0F, "OriginalBaseHealth380Preserved", cases);
            require(watched.getPersistentData().contains("ftboss_butcher_phase") == candidate, "NaturalTickPhaseInitializationMatchesMode", cases);
            Zombie attacker = EntityType.ZOMBIE.create(level);
            require(attacker != null, "NativeMobAttackerConstructed", cases);
            watched.setHealth(1.0F);
            watched.invulnerableTime = 0;
            boolean hit = watched.hurt(level.damageSources().mobAttack(attacker), 30.0F);
            report.put("hurtReturned", hit);
            report.put("healthAfterFirstDefeat", watched.getHealth());
            report.put("maxHealthAfterFirstDefeat", watched.getMaxHealth());
            report.put("phaseAfterFirstDefeat", watched.getPersistentData().getInt("ftboss_butcher_phase"));
            report.put("nativeDeathEvents", deaths);
            report.put("nativeCanceledDeathEvents", canceled);
            require(deaths == 1, "ActualHurtFiredNativeLivingDeathEvent", cases);
            require(watched.isAlive() == candidate, "FirstDefeatAliveMatchesMode", cases);
            require(canceled == (candidate ? 1 : 0), "NativeDeathCancellationMatchesMode", cases);
            if (candidate) {
                require(watched.getPersistentData().getInt("ftboss_butcher_phase") == 2, "FirstDefeatNaturallyEnteredAssaultPhase", cases);
                require(watched.getHealth() == 190.0F && watched.getMaxHealth() == 190.0F, "AssaultRestoredHalfBaseMaximum", cases);
            }
            report.put("actualMissingOriginalButcherPhaseDemonstrated", !candidate && !watched.isAlive() && deaths == 1 && canceled == 0);
            report.put("phaseLifecycleFeatureAccepted", candidate);
            try { Class.forName("net.farmingtales.ftboss.item.FoodWeaponFinalDamage"); throw new AssertionError("Paused weapon implementation loaded"); } catch (ClassNotFoundException absent) { cases.add("PausedWeaponImplementationAbsent"); }
            attacker.discard();
        } catch (Throwable failure) {
            report.put("failures", 1);
            report.put("failure", failure.toString());
            failure.printStackTrace();
        } finally {
            if (watched != null) watched.discard();
            watched = null;
        }
        try { Files.writeString(Path.of("actual-butcher-phase-probe.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); } catch (Exception failure) { throw new RuntimeException(failure); }
    }
}
