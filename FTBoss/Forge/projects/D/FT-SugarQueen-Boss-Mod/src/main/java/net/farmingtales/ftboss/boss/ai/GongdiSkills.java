package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.GongdiCombatRules;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GongdiSkills {
    private static final ResourceKey<DamageType> GUILLOTINE = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation("ftboss", "gongdi_guillotine"));
    private static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.8f, 0.06f, 0.12f), 1.1f);
    /* ★ v178 功底三招轮转表(诺克萨斯之手三件套)。索引按此顺序取用, 冷却未好的会顺延到下一个。 */
    private static final int ROT_GUILLOTINE = 0;   // R 诺克萨斯断头台
    private static final int ROT_STRIKE = 1;       // W 致残打击
    private static final int ROT_PULL = 2;         // E 无情铁手
    private static final int[] ROTATION = {ROT_GUILLOTINE, ROT_STRIKE, ROT_PULL};
    private final EasterBossEntity boss;
    private final Map<UUID, BleedingTarget> bleeding = new HashMap<>();
    private String skill = "";
    private int windup;
    private int rotation;
    private Player lockedTarget;
    private Vec3 direction = Vec3.ZERO;
    private long strikeReadyAt;
    private long pullReadyAt;
    private long executeReadyAt;

    public GongdiSkills(EasterBossEntity boss) {
        this.boss = boss;
    }

    public boolean isCasting() {
        return this.windup > 0;
    }

    public void reset() {
        this.windup = 0;
        this.skill = "";
        this.lockedTarget = null;
        this.bleeding.clear();
        this.rotation = 0;
        this.strikeReadyAt = 0;
        this.pullReadyAt = 0;
        this.executeReadyAt = 0;
    }

    public boolean validTarget(Player player, double radius) {
        Vec3 offset = player.position().subtract(this.boss.position());
        return player.level() == this.boss.level() && player.isAlive() && !player.isCreative() && !player.isSpectator()
                && GongdiCombatRules.inRange(offset.x, offset.y, offset.z, radius) && this.boss.hasLineOfSight(player);
    }

    public void onHit(Player player) {
        if (!player.isAlive() || player.isCreative() || player.isSpectator()) return;
        long now = this.boss.level().getGameTime();
        BleedingTarget target = this.bleeding.get(player.getUUID());
        if (target == null || target.player() != player) {
            target = new BleedingTarget(player, new GongdiCombatRules.Bleed());
            this.bleeding.put(player.getUUID(), target);
        }
        int stacks = target.bleed().addHit(now, this.boss.isGongdiBloodRageFull());
        player.displayClientMessage(Component.literal("§c出血 " + stacks + "/5 §7· 5 秒；远离斧刃，提防断头台"), true);
    }

    public void onAbilityHit(ServerLevel level, Player player, float dealt) {
        this.boss.onGongdiAbilityHit(level, player, dealt);
        this.onHit(player);
    }

    public boolean tryStart(ServerLevel level, LivingEntity candidate) {
        if (!(candidate instanceof Player player) || !this.validTarget(player, EasterBossEntity.CLEAVE_RADIUS)) return true;
        long now = level.getGameTime();
        int stacks = this.stacks(player, now);
        if (this.validTarget(player, GongdiCombatRules.EXECUTE_RANGE)
                && (stacks >= 3 || player.getHealth() + player.getAbsorptionAmount()
                <= GongdiCombatRules.executeDamage(this.boss.gongdiMeleeDamage(player), stacks))) {
            this.executeReadyAt = 0;
            this.start(level, player, "guillotine", GongdiCombatRules.EXECUTE_WINDUP, GongdiCombatRules.EXECUTE_RANGE);
            return true;
        }
        /* ★ v178 按用户要求「功底减少铁魔法施法, 多用技能」:
           原来 rotation % 3 == 0 的这一步会直接 return false, 调用方(castSkill)于是回退成
           「断罪斩(大杀四方)」—— 等于三招轮转每 3 次就有 1 次被自己的横扫占掉。
           现在把轮转做成「按 [断头台, 致残打击, 无情铁手] 顺序找第一个可用的」:
           ① 每次出手都必定落在诺手三招上(不再有 1/3 的横扫);
           ② 原本 1/3 的"空转"顺延去补那个还差一次的技能, 所以不是单纯给某一招加权,
              而是让三招一起变密 —— 最终谁出手仍由单招冷却决定
              (致残打击 100 / 无情铁手 160 / 断头台 360 tick)。 */
        for (int slot = 0; slot < ROTATION.length; slot++) {
            int index = this.rotation++ % ROTATION.length;
            int pick = ROTATION[index];
            if (pick == ROT_GUILLOTINE) {
                if (!this.validTarget(player, GongdiCombatRules.EXECUTE_RANGE)) continue;
                this.executeReadyAt = 0;
                this.start(level, player, "guillotine", GongdiCombatRules.EXECUTE_WINDUP, GongdiCombatRules.EXECUTE_RANGE);
                return true;
            }
            if (pick == ROT_STRIKE) {
                if (now < this.strikeReadyAt || !this.validTarget(player, GongdiCombatRules.STRIKE_RANGE)) continue;
                this.strikeReadyAt = now + GongdiCombatRules.STRIKE_COOLDOWN;
                this.start(level, player, "crippling_strike", GongdiCombatRules.STRIKE_WINDUP, GongdiCombatRules.STRIKE_RANGE);
                return true;
            }
            if (now < this.pullReadyAt || !this.validTarget(player, GongdiCombatRules.PULL_RANGE)) continue;
            this.pullReadyAt = now + GongdiCombatRules.PULL_COOLDOWN;
            this.start(level, player, "apprehend", GongdiCombatRules.PULL_WINDUP, GongdiCombatRules.PULL_RANGE);
            return true;
        }
        return false;
    }

    private void start(ServerLevel level, Player target, String kind, int ticks, double radius) {
        ticks = this.boss.getChallengeDifficulty().acceleratedTicks(ticks);
        this.skill = kind;
        this.windup = ticks;
        this.lockedTarget = target;
        Vec3 offset = target.position().subtract(this.boss.position());
        this.direction = new Vec3(offset.x, 0.0, offset.z).normalize();
        if (this.direction.lengthSqr() < 0.001) {
            Vec3 look = this.boss.getLookAngle();
            this.direction = new Vec3(look.x, 0.0, look.z).normalize();
            if (this.direction.lengthSqr() < 0.001) this.direction = new Vec3(0.0, 0.0, 1.0);
        }
        this.boss.setYRot((float) Math.toDegrees(Math.atan2(-this.direction.x, this.direction.z)));
        this.boss.setYBodyRot(this.boss.getYRot());
        this.boss.beginGongdiCast(kind, ticks, radius, this.direction);
        String name = switch (kind) {
            case "apprehend" -> "无情铁手 · 侧移躲开拉回";
            case "crippling_strike" -> "致残打击 · 后撤躲开重击";
            default -> "诺克萨斯断头台 · 拉开距离或寻找掩体";
        };
        target.displayClientMessage(Component.literal("§4功底：" + name), true);
        level.playSound(null, this.boss.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 1.0f, 0.7f);
        FTBossMod.LOGGER.info("[FTBoss][Gongdi] {} windup={} target={}", kind, ticks, target.getScoreboardName());
    }

    public void tick(ServerLevel level) {
        if (this.boss.getVariant() != 3 || this.boss.isMini() || this.boss.isNoAi() || !this.boss.isAlive()) {
            this.reset();
            return;
        }
        long now = level.getGameTime();
        var iterator = this.bleeding.values().iterator();
        while (iterator.hasNext()) {
            BleedingTarget target = iterator.next();
            Player player = target.player();
            if (level.getPlayerByUUID(player.getUUID()) != player || !player.isAlive() || player.isCreative()
                    || player.isSpectator() || player.distanceToSqr(this.boss) > 64.0 * 64.0) {
                iterator.remove();
                continue;
            }
            float damage = target.bleed().pulse(now);
            if (damage > 0.0f) {
                player.hurt(this.boss.damageSources().mobAttack(this.boss), damage);
                if (!this.boss.isAlive() || this.boss.isNoAi()) return;
                level.sendParticles(BLOOD, player.getX(), player.getY() + 1.1, player.getZ(),
                        target.bleed().stacks(now) + 2, 0.2, 0.4, 0.2, 0.0);
            }
            if (target.bleed().expired(now)) iterator.remove();
        }
        if (this.windup <= 0) return;
        if (!ManorKiting.enabled(this.boss)) this.boss.getNavigation().stop();
        this.boss.setYRot((float) Math.toDegrees(Math.atan2(-this.direction.x, this.direction.z)));
        this.boss.setYBodyRot(this.boss.getYRot());
        if (this.skill.equals("guillotine") && this.lockedTarget != null && this.lockedTarget.level() == level
                && this.lockedTarget.isAlive() && this.windup % 4 == 0) {
            level.sendParticles(BLOOD, this.lockedTarget.getX(), this.lockedTarget.getY() + 2.2,
                    this.lockedTarget.getZ(), 5, 0.18, 0.3, 0.18, 0.0);
        }
        if (--this.windup > 0) return;
        this.boss.finishGongdiCast();
        switch (this.skill) {
            case "apprehend" -> this.pull(level);
            case "crippling_strike" -> this.strike(level);
            case "guillotine" -> this.execute(level);
            default -> { }
        }
        this.skill = "";
        this.lockedTarget = null;
    }

    private boolean inCone(Player player, double halfAngle) {
        Vec3 offset = player.position().subtract(this.boss.position());
        return GongdiCombatRules.inCone(offset.x, offset.z, this.direction.x, this.direction.z, halfAngle);
    }

    private void pull(ServerLevel level) {
        for (Player player : level.players()) {
            if (!this.validTarget(player, GongdiCombatRules.PULL_RANGE)
                    || !this.inCone(player, GongdiCombatRules.PULL_HALF_ANGLE)) continue;
            Vec3 offset = this.boss.position().subtract(player.position());
            double distance = Math.hypot(offset.x, offset.z);
            if (distance <= 1.8) continue;
            Vec3 velocity = new Vec3(offset.x, 0.0, offset.z).normalize().scale(Math.min(1.15, (distance - 1.8) * 0.32));
            player.setDeltaMovement(velocity.x, 0.18, velocity.z);
            player.hurtMarked = true;
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 24, 1));
        }
        level.playSound(null, this.boss.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.HOSTILE, 1.5f, 0.6f);
    }

    private void strike(ServerLevel level) {
        Player player = this.lockedTarget;
        if (player == null || !this.validTarget(player, GongdiCombatRules.STRIKE_RANGE)
                || !this.inCone(player, GongdiCombatRules.STRIKE_HALF_ANGLE)) return;
        float before = player.getHealth();
        player.invulnerableTime = 0;
        float damage = GongdiCombatRules.strikeDamage(this.boss.gongdiMeleeDamage(player));
        if (!player.hurt(this.boss.damageSources().mobAttack(this.boss), damage)) return;
        this.onAbilityHit(level, player, Math.max(0.0f, before - player.getHealth()));
        if (player.isAlive()) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 3));
        level.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(), 16, 0.3, 0.4, 0.3, 0.08);
        level.playSound(null, this.boss.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.3f, 0.65f);
    }

    private void execute(ServerLevel level) {
        Player player = this.lockedTarget;
        if (player == null || !this.validTarget(player, GongdiCombatRules.EXECUTE_RANGE)) return;
        int stacks = this.stacks(player, level.getGameTime());
        DamageSource damage = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(GUILLOTINE), this.boss, this.boss);
        player.invulnerableTime = 0;
        float amount = GongdiCombatRules.executeDamage(this.boss.gongdiMeleeDamage(player), stacks);
        if (!player.hurt(damage, amount)) return;
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0, player.getZ(), 4, 0.2, 0.5, 0.2, 0.0);
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8f, 0.7f);
        if (!player.isAlive()) {
            this.executeReadyAt = level.getGameTime() + GongdiCombatRules.EXECUTE_RESET;
            this.boss.resetGongdiComboCooldown();
            FTBossMod.LOGGER.info("[FTBoss][Gongdi] guillotine kill: cooldown reset, bleed={}", stacks);
        }
    }

    private int stacks(Player player, long now) {
        BleedingTarget target = this.bleeding.get(player.getUUID());
        return target == null || target.player() != player ? 0 : target.bleed().stacks(now);
    }

    private record BleedingTarget(Player player, GongdiCombatRules.Bleed bleed) {}
}
