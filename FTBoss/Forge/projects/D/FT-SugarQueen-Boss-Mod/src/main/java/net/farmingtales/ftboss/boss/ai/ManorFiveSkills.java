package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Shape;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

public final class ManorFiveSkills {
    private static final UUID ATTACK_MODIFIER = UUID.fromString("f33bc9ab-89d1-466d-b648-11fd69710517");
    private static final DustParticleOptions GOLD = dust(1.0f, 0.74f, 0.20f);
    private static final DustParticleOptions VIOLET = dust(0.74f, 0.40f, 1.0f);
    private static final DustParticleOptions LIME = dust(0.72f, 1.0f, 0.20f);
    private static final DustParticleOptions CYAN = dust(0.20f, 0.90f, 1.0f);
    private static final DustParticleOptions SHADOW = dust(0.42f, 0.18f, 0.76f);
    private final EasterBossEntity boss;
    private Skill skill;
    private int rotation;
    private int age;
    private UUID targetId;
    private Vec3 origin = Vec3.ZERO;
    private Vec3 direction = new Vec3(0.0, 0.0, 1.0);
    private Vec3 side = new Vec3(1.0, 0.0, 0.0);

    public ManorFiveSkills(EasterBossEntity boss) {
        this.boss = boss;
    }

    public boolean isCasting() {
        return this.skill != null;
    }

    public void cancel() {
        this.skill = null;
        this.targetId = null;
        this.age = 0;
    }

    public void reset() {
        this.cancel();
        this.rotation = 0;
    }

    public int rotation() {
        return this.rotation;
    }

    public void restoreRotation(int saved) {
        this.rotation = Math.floorMod(saved, ManorFiveCombatRules.rotationLength(this.boss.getVariant()));
    }

    public void refreshAttackBonus() {
        var attack = this.boss.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) return;
        boolean enabled = ManorFiveCombatRules.supports(this.boss.getVariant()) && !this.boss.isMini();
        if (!enabled) attack.removeModifier(ATTACK_MODIFIER);
        else if (attack.getModifier(ATTACK_MODIFIER) == null) {
            attack.addTransientModifier(new AttributeModifier(ATTACK_MODIFIER, "ftboss.manor_five_attack",
                    ManorFiveCombatRules.ATTACK_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    public boolean tryStart(ServerLevel level, LivingEntity target) {
        if (!ManorFiveCombatRules.hasExpandedSkills(this.boss.getVariant()) || this.boss.isMini()
                || this.boss.isNoAi() || !this.boss.isAlive() || this.skill != null
                || !(target instanceof Player player) || !validPlayer(player)
                || !this.boss.hasLineOfSight(player)
                || this.boss.distanceToSqr(player) > ManorFiveCombatRules.MAX_CAST_DISTANCE * ManorFiveCombatRules.MAX_CAST_DISTANCE) return false;
        Skill next = ManorFiveCombatRules.select(this.boss.getVariant(), this.rotation);
        this.rotation = (this.rotation + 1) % ManorFiveCombatRules.rotationLength(this.boss.getVariant());
        if (next == null) return false;
        this.skill = next;
        this.age = net.farmingtales.ftboss.balance.ManorBalanceHooks.fiveAge(next.windup - this.boss.getChallengeDifficulty().acceleratedTicks(next.windup), next);
        this.targetId = player.getUUID();
        Vec3 flat = player.position().subtract(this.boss.position()).multiply(1.0, 0.0, 1.0);
        this.direction = flat.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : flat.normalize();
        this.side = new Vec3(-this.direction.z, 0.0, this.direction.x);
        this.origin = next.shape == Shape.WAVE || next.shape == Shape.BEAM || next.shape == Shape.CONE
                ? this.boss.position() : player.position();
        boolean circleCue = next.shape == Shape.CIRCLE || next.shape == Shape.WAVE;
        this.boss.beginManorFiveCast(next.duration() - this.age, next.windup - this.age, this.origin, next.radius, circleCue);
        this.boss.getLookControl().setLookAt(player, 60.0f, 60.0f);
        /* ★ 2026-09-24 给折耳根的钩子：起手这一招时先传送、并按概率留下紫色残影
           （残影会记下 next 这一招，见 EasterBossEntity.onManorFiveCastStart）。 */
        this.boss.onManorFiveCastStart(next);
        for (Player nearby : level.players()) {
            if (validPlayer(nearby) && nearby.distanceToSqr(this.boss) <= 32.0 * 32.0) {
                nearby.displayClientMessage(Component.literal("§e" + EasterBossEntity.NAMES[next.variant]
                        + " §6【" + next.title + "】§f 蓄力中，离开预警区域！"), true);
            }
        }
        this.sound(level, SoundEvents.RESPAWN_ANCHOR_CHARGE, 0.85f);
        this.telegraph(level);
        FTBossMod.LOGGER.info("[FTBoss][ManorFive] {} 蓄力: {} tick，方向与落点已锁定", next.title, next.windup - this.age);
        return true;
    }

    public void tick(ServerLevel level) {
        if (this.boss.tickCount % 20 == 0) this.refreshAttackBonus();
        if (this.skill == null) return;
        Player target = level.getPlayerByUUID(this.targetId);
        if (this.boss.isMini() || this.boss.isNoAi() || !this.boss.isAlive()
                || this.boss.getVariant() != this.skill.variant || target == null || !validPlayer(target)
                || target.distanceToSqr(this.boss) > 40.0 * 40.0
                || net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this.boss)) {
            this.boss.cancelManorFiveCast();
            this.cancel();
            return;
        }
        if (!this.boss.allowsParallelMelee() && !ManorKiting.enabled(this.boss)) {
            this.boss.getNavigation().stop();
            this.boss.setDeltaMovement(this.boss.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        }
        this.age++;
        if (this.age % 4 == 0) this.telegraph(level);
        int pulse = ManorFiveCombatRules.pulseAt(this.skill, this.age);
        if (pulse >= 0) this.strike(level, pulse);
        if (this.skill == null) return;
        if (this.age >= this.skill.duration()) {
            this.boss.finishManorFiveCast(this.origin);
            this.cancel();
        }
    }

    private void strike(ServerLevel level, int pulse) {
        this.hitShape(level, this.skill, this.center(pulse), this.direction, this.side, pulse, 1.0, false);
    }

    /**
     * ★ 2026-09-24 按用户要求给折耳根加「紫色残影」：技能命中结算抽成**一份**，本体与残影重放共用。
     *
     * <p>与 {@link #strike} 的区别只有两个参数：
     * {@code damageMultiplier}（残影 = 原伤害 25%）与 {@code trueDamage}
     * （残影那一份**整份走真伤/穿甲**，即 {@code fellOutOfWorld} 那条路，
     * 与折耳根本体"虚空伤害"同一套机制）。形状判定、视线判定、粒子/音效、
     * 减益与击退（{@link #applyEffects}）全部照旧。
     *
     * @param skill    要结算的技能（残影重放时是它当时记录下来的那一招）
     * @param center   形状中心
     * @param direction 形状朝向
     * @param side     形状侧向
     * @param pulse    第几段（本体按 cast 推进；残影重放只放第 0 段）
     */
    private int hitShape(ServerLevel level, Skill skill, Vec3 center, Vec3 direction, Vec3 side,
                         int pulse, double damageMultiplier, boolean trueDamage) {
        if (skill == null) return 0;
        int hits = 0;
        for (Player player : level.players()) {
            if (!validPlayer(player) || player.distanceToSqr(this.boss) > 40.0 * 40.0) continue;
            Vec3 relative = player.position().subtract(center);
            if (!ManorFiveCombatRules.contains(skill, pulse, relative.dot(direction),
                    relative.dot(side), relative.y, player.getBbWidth() * 0.5)) continue;
            if (!this.clearLine(level, center.add(0.0, 1.0, 0.0), player.getBoundingBox().getCenter())) continue;
            player.invulnerableTime = 0;
            float damage = (float) (this.boss.manorSkillDamage(player, skill.damage, skill.meleeMultiplier())
                    * damageMultiplier);
            boolean successful;
            if (trueDamage) {
                /* 残影重放：整份真伤/穿甲（不吃护甲），与本体那半"虚空伤害"同一条路。 */
                net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage.hurtSkill(player, this.boss,
                        this.boss.damageSources().fellOutOfWorld(), damage);
                successful = true;
            } else {
                successful = player.hurt(this.boss.damageSources().mobAttack(this.boss), damage);
            }
            if (!this.boss.isAlive()) return hits;
            if (!successful) continue;
            hits++;
            this.applyEffects(player, skill, center, direction, pulse);
            this.impact(level, player.position().add(0.0, 1.0, 0.0), 12, skill);
        }
        ParticleOptions particle = impactParticle(skill);
        if (skill.shape == Shape.WAVE) {
            this.ring(level, center.add(0.0, 0.8, 0.0), ManorFiveCombatRules.waveRadius(skill, pulse), particle, 40);
        } else if (skill.shape == Shape.BEAM) {
            this.line(level, center.add(0.0, 1.0, 0.0), center.add(direction.scale(skill.radius)).add(0.0, 1.0, 0.0), particle, 32);
        } else if (skill.shape == Shape.CROSS) {
            Vec3 raised = center.add(0.0, 0.8, 0.0);
            this.line(level, raised.subtract(direction.scale(skill.radius)), raised.add(direction.scale(skill.radius)), particle, 24);
            this.line(level, raised.subtract(side.scale(skill.radius)), raised.add(side.scale(skill.radius)), particle, 24);
        } else if (skill == Skill.DANDAN_STAR) {
            this.line(level, center.add(0.0, 7.0, 0.0), center.add(0.0, 0.2, 0.0), ParticleTypes.END_ROD, 24);
            this.ring(level, center.add(0.0, 0.3, 0.0), skill.radius, particle, 28);
        } else if (skill == Skill.QINGYI_THUNDER) {
            this.line(level, center.add(0.0, 8.0, 0.0), center.add(0.0, 0.2, 0.0), ParticleTypes.ELECTRIC_SPARK, 28);
            this.ring(level, center.add(0.0, 0.4, 0.0), skill.radius, ParticleTypes.END_ROD, 32);
        } else if (skill == Skill.QINGYI_TIDE) {
            for (int segment = 0; segment <= 24; segment++) {
                double angle = Math.toRadians(-50.0 + 100.0 * segment / 24.0);
                Vec3 ray = direction.scale(Math.cos(angle)).add(side.scale(Math.sin(angle)));
                this.particle(level, ParticleTypes.SPLASH, center.add(ray.scale(skill.radius)).add(0.0, 0.6, 0.0));
                this.particle(level, ParticleTypes.ELECTRIC_SPARK, center.add(ray.scale(skill.radius * 0.5)).add(0.0, 0.8, 0.0));
            }
        } else {
            this.impact(level, center.add(0.0, 0.6, 0.0), 24, skill);
        }
        this.sound(level, impactSound(skill), center, 0.90f + pulse * 0.12f);
        FTBossMod.LOGGER.info("[FTBoss][ManorFive] {} 第 {}/{} 段: 成功命中 {} 人，基础下限 {}，普攻基准倍率 {}"
                        + (damageMultiplier == 1.0 ? "" : "（残影重放 ×{}）"),
                skill.title, pulse + 1, skill.pulses, hits, skill.damage, skill.meleeMultiplier(),
                String.format("%.2f", damageMultiplier));
        return hits;
    }

    /**
     * ★ 2026-09-24：**残影重放**一次记录下来的技能。
     *
     * <p>以残影所在位置为形状中心、朝当时锁定的目标定向，只放第 0 段，
     * 伤害按 {@code damageMultiplier}（用户口径 = 原伤害 25%）且整份真伤。
     * 走的是与本体完全相同的 {@link #hitShape}，所以形状判定/减益/击退/粒子都一致。
     */
    public int echoCast(ServerLevel level, Skill skill, Vec3 origin, LivingEntity target,
                        double damageMultiplier, boolean trueDamage) {
        if (skill == null || origin == null) return 0;
        Vec3 direction = Vec3.ZERO;
        if (target != null) {
            Vec3 flat = target.position().subtract(origin).multiply(1.0, 0.0, 1.0);
            if (flat.lengthSqr() > 1.0E-6) direction = flat.normalize();
        }
        if (direction.lengthSqr() < 1.0E-6) {
            direction = new Vec3(this.boss.getLookAngle().x, 0.0, this.boss.getLookAngle().z);
            if (direction.lengthSqr() < 1.0E-6) direction = new Vec3(0.0, 0.0, 1.0);
            direction = direction.normalize();
        }
        Vec3 side = new Vec3(-direction.z, 0.0, direction.x);
        return this.hitShape(level, skill, origin, direction, side, 0, damageMultiplier, trueDamage);
    }

    private void applyEffects(Player player, Skill skill, Vec3 center, Vec3 direction, int pulse) {
        boolean last = pulse == skill.pulses - 1;
        Vec3 away = player.position().subtract(center).multiply(1.0, 0.0, 1.0);
        if (away.lengthSqr() < 1.0E-6) away = direction;
        away = away.normalize();
        if (skill == Skill.SHADOW_CAGE && !last) away = away.scale(-1.0);
        double strength = last ? skill.knockback : skill.knockback * 0.55;
        net.farmingtales.ftboss.util.BossKnockback.knockback(this.boss, player, strength, -away.x, -away.z);
        double resistance = Math.max(0.0, 1.0 - player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, Math.max(motion.y,
                net.farmingtales.ftboss.util.BossKnockback.vertical(this.boss, player, skill.lift * resistance)), motion.z);
        player.hurtMarked = true;
        switch (skill) {
            case YOUZI_SEAL, YOUZI_ECHO -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 1));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            }
            case DANDAN_REAP, DANDAN_STAR -> {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                if (last) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1));
            }
            case SHADOW_CROSS, SHADOW_CAGE -> {
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            }
            case XUANLAN_RAIL, XUANLAN_RING -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 80, 0));
            }
            case QINGYI_TIDE, QINGYI_THUNDER, QINGYI_STAR -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            }
            case LEMON_ACID -> {
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 1));
            }
            case LEMON_FLAMBE -> {
                player.setSecondsOnFire(4);
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 1));
            }
        }
    }

    private void telegraph(ServerLevel level) {
        ParticleOptions color = color(this.skill);
        Vec3 ground = this.origin.add(0.0, 0.12, 0.0);
        switch (this.skill.shape) {
            case CIRCLE -> this.ring(level, ground, this.skill.radius, color, 28);
            case WAVE -> {
                int pulse = Math.min(this.skill.pulses - 1, Math.max(0, (this.age - this.skill.windup) / this.skill.interval));
                this.ring(level, ground, ManorFiveCombatRules.waveRadius(this.skill, pulse), color, 32);
            }
            case ECHO -> {
                for (int pulse = 0; pulse < this.skill.pulses; pulse++) {
                    this.ring(level, this.center(pulse).add(0.0, 0.12, 0.0), this.skill.radius, color, 14);
                }
            }
            case CROSS -> {
                this.line(level, ground.subtract(this.direction.scale(this.skill.radius)), ground.add(this.direction.scale(this.skill.radius)), color, 20);
                this.line(level, ground.subtract(this.side.scale(this.skill.radius)), ground.add(this.side.scale(this.skill.radius)), color, 20);
            }
            case BEAM -> {
                Vec3 beam = this.origin.add(0.0, 1.0, 0.0);
                double halfWidth = ManorFiveCombatRules.beamHalfWidth(this.skill);
                for (double offset : new double[]{-halfWidth, halfWidth}) {
                    Vec3 start = beam.add(this.side.scale(offset));
                    this.line(level, start, start.add(this.direction.scale(this.skill.radius)), color, 20);
                }
            }
            case CONE -> {
                for (int segment = 0; segment <= 24; segment++) {
                    double angle = Math.toRadians(-50.0 + 100.0 * segment / 24.0);
                    Vec3 ray = this.direction.scale(Math.cos(angle)).add(this.side.scale(Math.sin(angle)));
                    Vec3 point = ground.add(ray.scale(this.skill.radius));
                    this.particle(level, color, point);
                    if (segment == 0 || segment == 24) this.line(level, ground, point, color, 10);
                }
            }
        }
    }

    private Vec3 center(int pulse) {
        if (this.skill.shape != Shape.ECHO) return this.origin;
        double angle = pulse * Math.PI * 2.0 / 3.0;
        return this.origin.add(this.direction.scale(Math.cos(angle) * 3.0)).add(this.side.scale(Math.sin(angle) * 3.0));
    }

    private void ring(ServerLevel level, Vec3 center, double radius, ParticleOptions particle, int count) {
        for (int index = 0; index < count; index++) {
            double angle = index * Math.PI * 2.0 / count;
            this.particle(level, particle, center.add(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius));
        }
    }

    private void line(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int count) {
        Vec3 clipped = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this.boss)).getLocation();
        for (int index = 0; index <= count; index++) {
            this.particle(level, particle, start.lerp(clipped, index / (double) count));
        }
    }

    private void particle(ServerLevel level, ParticleOptions particle, Vec3 position) {
        level.sendParticles(particle, position.x, position.y, position.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    private void impact(ServerLevel level, Vec3 position, int count, Skill skill) {
        level.sendParticles(impactParticle(skill), position.x, position.y, position.z, count, 0.65, 0.65, 0.65, 0.05);
    }

    private ParticleOptions color(Skill skill) {
        return switch (skill.variant) {
            case 0 -> GOLD;
            case 1 -> VIOLET;
            case 4 -> skill == Skill.LEMON_FLAMBE ? GOLD : LIME;
            case 5, 6 -> CYAN;
            default -> SHADOW;
        };
    }

    private static ParticleOptions impactParticle(Skill skill) {
        return switch (skill.variant) {
            case 0 -> ParticleTypes.ENCHANTED_HIT;
            case 1 -> ParticleTypes.REVERSE_PORTAL;
            case 4 -> skill == Skill.LEMON_FLAMBE ? ParticleTypes.FLAME : ParticleTypes.SPORE_BLOSSOM_AIR;
            case 5 -> skill == Skill.QINGYI_STAR ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK;
            case 6 -> ParticleTypes.ELECTRIC_SPARK;
            default -> ParticleTypes.SCULK_SOUL;
        };
    }

    private static SoundEvent impactSound(Skill skill) {
        return switch (skill.variant) {
            case 0 -> SoundEvents.IRON_GOLEM_ATTACK;
            case 1 -> SoundEvents.ENDERMAN_TELEPORT;
            case 4 -> SoundEvents.FIRE_EXTINGUISH;
            case 5 -> skill == Skill.QINGYI_TIDE ? SoundEvents.TRIDENT_RIPTIDE_3 : SoundEvents.TRIDENT_THUNDER;
            case 6 -> SoundEvents.WARDEN_SONIC_BOOM;
            default -> SoundEvents.PLAYER_ATTACK_SWEEP;
        };
    }

    /** 在指定位置播技能音效（残影重放要按**残影**的位置播，不能老用本体的 origin）。 */
    private void sound(ServerLevel level, SoundEvent sound, Vec3 at, float pitch) {
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.HOSTILE, 1.0f, pitch);
    }

    private void sound(ServerLevel level, SoundEvent sound, float pitch) {
        this.sound(level, sound, this.origin, pitch);
    }

    private boolean clearLine(ServerLevel level, Vec3 start, Vec3 end) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this.boss)).getType() == HitResult.Type.MISS;
    }

    private static boolean validPlayer(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private static DustParticleOptions dust(float red, float green, float blue) {
        return new DustParticleOptions(new Vector3f(red, green, blue), 1.15f);
    }
}
