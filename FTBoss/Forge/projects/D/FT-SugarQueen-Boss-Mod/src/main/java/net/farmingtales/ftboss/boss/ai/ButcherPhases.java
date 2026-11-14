package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.MeatBoss;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ButcherPhases {
    private static final String PHASE = "ftboss_butcher_phase";
    private static final String ELAPSED = "ftboss_butcher_phase_ticks";
    private static final String DODGE_CD = "ftboss_butcher_dodge_cd";
    private static final String BYPASS = "ftboss_butcher_phase_bypass";
    private static final UUID HEALTH_MODIFIER = UUID.fromString("fe0a6bc2-4ab2-40b2-971c-28c105e27533");

    private ButcherPhases() {}

    public static int phase(MeatMountainEntity boss) {
        return ButcherPhaseRules.normalize(boss.getPersistentData().getInt(PHASE));
    }

    public static int remainingSeconds(MeatMountainEntity boss) {
        int elapsed = Math.max(0, boss.getPersistentData().getInt(ELAPSED));
        return Math.max(0, (ButcherPhaseRules.RECOVERY_DURATION - elapsed + 19) / 20);
    }

    public static void tick(MeatMountainEntity boss) {
        if (boss.level().isClientSide || !boss.isAlive()) return;
        CompoundTag data = boss.getPersistentData();
        if (!data.contains(PHASE)) {
            data.putInt(PHASE, ButcherPhaseRules.REST);
            BossRage.reset(boss);
            boss.removeAllEffects();
        }
        applyHealth(boss);
        if (ButcherBalanceRules.enterAssault(phase(boss), boss.getHealth(), boss.getMaxHealth())) {
            float previousHealth = boss.getHealth();
            enter(boss, ButcherPhaseRules.ASSAULT);
            boss.setHealth(Math.min(previousHealth, boss.getMaxHealth()));
            if (!MeatBoss.isBoss(boss)) ButcherReinforcements.summon(boss);
        }
        applySpeed(boss);
        int cooldown = data.getInt(DODGE_CD);
        if (cooldown > 0) data.putInt(DODGE_CD, cooldown - 1);
        if (phase(boss) != ButcherPhaseRules.RECOVERY) return;
        int elapsed = Math.min(ButcherPhaseRules.RECOVERY_DURATION, Math.max(0, data.getInt(ELAPSED))) + 1;
        data.putInt(ELAPSED, elapsed);
        if (ButcherPhaseRules.expired(phase(boss), elapsed)) {
            enter(boss, ButcherPhaseRules.REST);
            return;
        }
        float healing = ButcherPhaseRules.regeneration(phase(boss), elapsed,
                boss.getMaxHealth(), boss.getMaxHealth() - boss.getHealth());
        if (healing > 0) {
            boss.heal(healing);
            if (boss.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.HEART, boss.getX(), boss.getY() + 1.5, boss.getZ(), 3, 0.4, 0.4, 0.4, 0);
            }
        }
    }

    public static boolean advance(MeatMountainEntity boss) {
        if (boss.getPersistentData().getBoolean(BYPASS)) return false;
        int next = ButcherPhaseRules.afterDefeat(phase(boss));
        if (next == 0) return false;
        enter(boss, next);
        return true;
    }

    private static void enter(MeatMountainEntity boss, int phase) {
        CompoundTag data = boss.getPersistentData();
        data.putInt(PHASE, phase);
        data.putInt(ELAPSED, 0);
        data.putInt(DODGE_CD, 0);
        BossRage.reset(boss);
        boss.removeAllEffects();
        applyHealth(boss);
        boss.setHealth(boss.getMaxHealth());
        boss.invulnerableTime = 0;
        boss.resetButcherCombat();
        MeatBoss.onPhaseChanged(boss, phase);
        if (boss.level() instanceof ServerLevel server) {
            server.playSound(null, boss.blockPosition(), SoundEvents.HOGLIN_ANGRY, SoundSource.HOSTILE, 1.0F, 0.8F);
        }
        FTBossMod.LOGGER.info("[FTBoss] 屠夫阶段={} 血量={} 三阶段剩余={}秒 实体={}", phase,
                boss.getMaxHealth(), phase == ButcherPhaseRules.RECOVERY ? ButcherPhaseRules.RECOVERY_DURATION / 20 : 0, boss.getUUID());
    }

    private static void applyHealth(MeatMountainEntity boss) {
        var maximumHealth = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maximumHealth == null) return;
        double adjustment = ButcherPhaseRules.healthMultiplier(phase(boss)) - 1.0;
        var previous = maximumHealth.getModifier(HEALTH_MODIFIER);
        if (previous != null && previous.getAmount() == adjustment) return;
        maximumHealth.removeModifier(HEALTH_MODIFIER);
        if (adjustment != 0) {
            maximumHealth.addTransientModifier(new AttributeModifier(HEALTH_MODIFIER,
                    "ftboss_butcher_phase_health", adjustment, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (boss.getHealth() > boss.getMaxHealth()) boss.setHealth(boss.getMaxHealth());
    }

    private static void applySpeed(MeatMountainEntity boss) {
        var speed = boss.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        UUID modifierId = UUID.fromString("64627f6e-31de-4f34-a4a8-87e5d60b42f0");
        boolean assault = phase(boss) == ButcherPhaseRules.ASSAULT;
        if (!assault) {
            speed.removeModifier(modifierId);
        } else if (speed.getModifier(modifierId) == null) {
            speed.addTransientModifier(new AttributeModifier(modifierId, "ftboss_butcher_assault_speed",
                    ButcherBalanceRules.ASSAULT_SPEED_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    public static void beginDamage(MeatMountainEntity boss, DamageSource source) {
        boss.getPersistentData().putBoolean(BYPASS, source.is(DamageTypeTags.BYPASSES_INVULNERABILITY));
    }

    public static float adjustScaledHealth(LivingEntity boss, float health) {
        if (!(boss instanceof MeatMountainEntity)) return health;
        var maximumHealth = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maximumHealth == null || maximumHealth.getBaseValue() <= 0) return health;
        return ButcherPhaseRules.scaledHealth(health / (float) maximumHealth.getBaseValue(), boss.getMaxHealth());
    }

    public static void endDamage(MeatMountainEntity boss) {
        boss.getPersistentData().remove(BYPASS);
    }

    public static boolean dodge(MeatMountainEntity boss, DamageSource source) {
        if (!boss.isAlive() || boss.isInvulnerableTo(source) || source.getEntity() == null || source.getEntity() == boss
                || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        CompoundTag data = boss.getPersistentData();
        if (phase(boss) != ButcherPhaseRules.RECOVERY || data.getInt(DODGE_CD) > 0) return false;
        if (!ButcherPhaseRules.dodge(phase(boss), data.getInt(DODGE_CD), boss.getRandom().nextFloat())) return false;
        data.putInt(DODGE_CD, ButcherPhaseRules.DODGE_COOLDOWN);
        if (boss.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CLOUD, boss.getX(), boss.getY() + 1.0, boss.getZ(), 8, 0.5, 0.6, 0.5, 0.04);
            server.playSound(null, boss.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 0.6F, 1.4F);
        }
        return true;
    }
}
