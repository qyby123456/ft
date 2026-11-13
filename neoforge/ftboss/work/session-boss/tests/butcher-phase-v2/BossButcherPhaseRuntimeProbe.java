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

    private static void defeat(net.minecraft.server.level.ServerLevel level, Zombie attacker) {
        watched.setHealth(1.0F);
        watched.invulnerableTime = 0;
        watched.hurt(level.damageSources().mobAttack(attacker), 30.0F);
    }

    private static void fresh(net.minecraft.server.level.ServerLevel level) {
        if (watched != null) watched.discard();
        var type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("ftboss:meat_mountain"));
        watched = (MeatMountainEntity) type.create(level);
        watched.moveTo(0.5, 70.0, 0.5, 0.0F, 0.0F);
        watched.setNoAi(true);
        watched.setNoGravity(true);
        deaths = 0;
        canceled = 0;
        watched.tick();
    }

    private static void lifecycleCases(net.minecraft.server.level.ServerLevel level, Zombie attacker, List<String> cases, Map<String, Object> report) throws Exception {
        require(!watched.getPersistentData().contains("ftboss_butcher_phase_bypass"), "SuccessfulDamageClearsTransientBypass", cases);
        require(watched.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue() == 380.0, "PhaseModifierPreservesBaseHealth", cases);
        net.farmingtales.ftboss.boss.ai.BossScaling.tick(watched);
        watched.getPersistentData().putDouble("ftboss_base_hp", 760.0);
        watched.setHealth(95.0F);
        net.farmingtales.ftboss.boss.ai.BossScaling.tick(watched);
        require(watched.getMaxHealth() == 380.0F && watched.getHealth() == 190.0F, "ExistingNativeScalingPreservesModifiedMaxHealthAndRatio", cases);
        watched.getPersistentData().putDouble("ftboss_base_hp", 380.0);
        net.farmingtales.ftboss.boss.ai.BossScaling.tick(watched);
        require(watched.getMaxHealth() == 190.0F && watched.getHealth() == 95.0F, "ScalingRoundTripNoSecondHalving", cases);
        watched.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 200));
        defeat(level, attacker);
        require(deaths == 2 && canceled == 2 && watched.isAlive(), "SecondRealDefeatCanceledByExistingNativeListener", cases);
        require(watched.getButcherPhase() == 3 && watched.getHealth() == 190.0F, "SecondDefeatNaturallyEnteredRecovery", cases);
        require(watched.getActiveEffects().isEmpty(), "TransitionClearedPriorEffects", cases);
        require(watched.getPersistentData().getInt("ftboss_butcher_phase_ticks") == 0, "RecoveryTimerNaturallyStartsAtZero", cases);
        require(net.farmingtales.ftboss.boss.ai.ButcherPhases.remainingSeconds(watched) == 60, "OriginalSixtySecondWindow", cases);
        var combatField = MeatMountainEntity.class.getDeclaredField("combat");
        combatField.setAccessible(true);
        Object combat = combatField.get(watched);
        var nextAttackField = combat.getClass().getDeclaredField("nextAttack");
        nextAttackField.setAccessible(true);
        require(nextAttackField.getLong(combat) == level.getGameTime() + 14, "OriginalRecoveryCombatResetIntervalReadOnlyObserved", cases);
        long dodgeSeed = -1;
        for (long seed = 0; seed < 100000; seed++) {
            watched.getRandom().setSeed(seed);
            if (watched.getRandom().nextFloat() < 0.1F) { dodgeSeed = seed; break; }
        }
        require(dodgeSeed >= 0, "DeterministicNativeRandomDodgeSeedLocated", cases);
        watched.getRandom().setSeed(dodgeSeed);
        watched.invulnerableTime = 0;
        float beforeDodge = watched.getHealth();
        boolean dodgeHit = watched.hurt(level.damageSources().mobAttack(attacker), 3.0F);
        require(!dodgeHit && watched.getHealth() == beforeDodge, "ActualRecoveryHurtDodgedAtOriginalTenPercentRoll", cases);
        require(watched.getPersistentData().getInt("ftboss_butcher_dodge_cd") == 20, "OriginalDodgeCooldownTwentyTicks", cases);
        require(!watched.getPersistentData().contains("ftboss_butcher_phase_bypass"), "DodgeEarlyReturnLeavesNoBypassMarker", cases);
        watched.setHealth(100.0F);
        watched.getRandom().setSeed(dodgeSeed);
        watched.invulnerableTime = 0;
        require(watched.hurt(level.damageSources().mobAttack(attacker), 3.0F) && watched.getHealth() < 100.0F, "CooldownBlocksSecondDodgeWithSameNativeRoll", cases);
        watched.setHealth(100.0F);
        for (int elapsed = 1; elapsed <= 19; elapsed++) watched.tick();
        require(watched.getHealth() == 100.0F, "RecoveryDoesNotHealBeforeTwentiethTick", cases);
        require(watched.getPersistentData().getInt("ftboss_butcher_dodge_cd") == 1, "DodgeCooldownDecrementsThroughNativeEntityTicks", cases);
        watched.tick();
        require(watched.getHealth() == 109.5F, "TwentiethTickHealsFivePercentOfModifiedMaximum", cases);
        require(watched.getPersistentData().getInt("ftboss_butcher_dodge_cd") == 0, "DodgeCooldownExpiresOnTwentiethTick", cases);
        watched.tick();
        require(watched.getHealth() == 109.5F, "RecoveryHealingDoesNotRepeatEveryTick", cases);
        var saved = new net.minecraft.nbt.CompoundTag();
        watched.saveWithoutId(saved);
        var restored = (MeatMountainEntity) watched.getType().create(level);
        restored.load(saved);
        restored.setNoAi(true);
        restored.setNoGravity(true);
        require(restored.getButcherPhase() == 3 && restored.getPersistentData().getInt("ftboss_butcher_phase_ticks") == 21, "NativeSaveLoadPreservesPhaseAndElapsed", cases);
        restored.tick();
        require(restored.getMaxHealth() == 190.0F, "LoadedRecoveryReappliesExactlyOneTransientHealthModifier", cases);
        restored.discard();
        watched.setHealth(1.0F);
        watched.invulnerableTime = 0;
        watched.hurt(level.damageSources().magic(), 30.0F);
        require(deaths == 3 && canceled == 2 && !watched.isAlive(), "ThirdRealDefeatIsTerminalWithoutExtraResurrection", cases);
        require(!watched.getPersistentData().contains("ftboss_butcher_phase_bypass"), "TerminalDamageFinallyClearsBypass", cases);
        report.put("naturalDefeatEvents", deaths);
        report.put("naturalCanceledDefeatEvents", canceled);
        fresh(level);
        var health = watched.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        var externalId = ResourceLocation.parse("ftboss_probe:external_health");
        health.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(externalId, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        watched.setHealth(watched.getMaxHealth());
        require(watched.getMaxHealth() == 400.0F, "IndependentAttributeControlSetup", cases);
        defeat(level, attacker);
        require(watched.getButcherPhase() == 2 && watched.getMaxHealth() == 200.0F, "PhaseHalfMultiplierStacksWithExternalHealthModifier", cases);
        defeat(level, attacker);
        require(watched.getButcherPhase() == 3 && watched.getMaxHealth() == 200.0F, "RecoveryKeepsSingleHalfMultiplier", cases);
        watched.setHealth(100.0F);
        for (int elapsed = 1; elapsed <= 1199; elapsed++) watched.tick();
        require(watched.isAlive() && watched.getButcherPhase() == 3, "RecoveryPersistsThrough1199RealEntityTicks", cases);
        require(watched.getPersistentData().getInt("ftboss_butcher_phase_ticks") == 1199, "NativeTickAdvancedEntireWindowWithoutTagFastForward", cases);
        require(net.farmingtales.ftboss.boss.ai.ButcherPhases.remainingSeconds(watched) == 1, "OriginalRemainingSecondsRoundsUp", cases);
        require(watched.getHealth() == watched.getMaxHealth(), "RecoveryRegenerationCapsAtMaximum", cases);
        watched.tick();
        require(watched.getButcherPhase() == 1 && watched.getMaxHealth() == 400.0F && watched.getHealth() == 400.0F, "Tick1200NaturallyReturnsToFullRestPhase", cases);
        require(watched.getPersistentData().getInt("ftboss_butcher_phase_ticks") == 0, "TimeoutTransitionResetsElapsedCounter", cases);
        require(health.getModifier(externalId) != null, "TimeoutPreservesIndependentHealthModifier", cases);
        require(health.getModifier(ResourceLocation.parse("ftboss:butcher_phase_health")) == null, "RestPhaseRemovesOnlyOwnHalfHealthModifier", cases);
        require(watched.getBossBar().getName().getString().contains("1/3"), "ServerBossBarComponentShowsCurrentPhase", cases);
        watched.tick();
        require(watched.getMaxHealth() == 400.0F, "RestTickDoesNotAccumulateAttributeModifiers", cases);
        defeat(level, attacker);
        require(watched.getButcherPhase() == 2 && watched.getHealth() == 200.0F, "NaturalPhaseCycleCanStartAgainAfterTimeout", cases);
        fresh(level);
        watched.hurt(level.damageSources().genericKill(), 10000.0F);
        require(deaths == 1 && canceled == 0 && !watched.isAlive(), "NativeBypassesInvulnerabilityDamageNeverAdvancesPhase", cases);
        require(!watched.getPersistentData().contains("ftboss_butcher_phase_bypass"), "BypassDeathFinallyClearsMarker", cases);
        fresh(level);
        net.farmingtales.ftboss.boss.ai.LemonSummonRewards.mark(watched);
        defeat(level, attacker);
        require(deaths == 1 && canceled == 0 && !watched.isAlive(), "OriginalSuppressedLemonHelperCannotResurrect", cases);
        Zombie vanilla = EntityType.ZOMBIE.create(level);
        vanilla.setHealth(1.0F);
        vanilla.hurt(level.damageSources().magic(), 30.0F);
        require(!vanilla.isAlive() && !vanilla.getPersistentData().contains("ftboss_butcher_phase"), "OrdinaryVanillaDeathUnaffected", cases);
        vanilla.discard();
        report.put("phaseLifecycleFeatureAccepted", true);
        report.put("completeRecoveryWindowEntityTicks", 1200);
        report.put("readOnlyCombatFieldObservation", true);
        report.put("combatAttackProfilesAccepted", false);
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
            watched.setNoAi(true);
            watched.setNoGravity(true);
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
                lifecycleCases(level, attacker, cases, report);
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
