package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 酒桶王的特效与伤害编排。
 * 从实体中抽离，实体只保留"动作状态机 + 数值"，这里负责粒子/音效/命中判定。
 */
public final class BarrelTactics {

    private final net.minecraft.world.entity.Mob e;
    /** 伤害倍率 —— 真身酒桶王用默认 1.0；柠檬猫继承来的那一份传 5.0（v159，见 LemonInheritedAbilityRules）。 */
    private final float damageScale;

    public BarrelTactics(net.minecraft.world.entity.Mob entity) {
        this(entity, 1.0F);
    }

    public BarrelTactics(net.minecraft.world.entity.Mob entity, float damageScale) {
        this.e = entity;
        this.damageScale = damageScale > 0.0F ? damageScale : 1.0F;
    }

    /** 这一份战术组件的伤害倍率（真身 = 1.0）。 */
    public float damageScale() {
        return this.damageScale;
    }

    private ServerLevel sl() {
        return e.level() instanceof ServerLevel s ? s : null;
    }

    private void snd(SoundEvent ev, float vol, float pitch) {
        Fx.sound(e.level(), e.getX(), e.getY() + 0.6, e.getZ(), ev, vol, pitch);
    }

    private void part(double x, double y, double z, int n, SimpleParticleType t, double spread, double speed) {
        Fx.particles(e.level(), x, y, z, n, t, spread, speed);
    }

    private void fx(double x, double y, double z, int n, float r, float g, float b, double spread, double speed) {
        Fx.dust(e.level(), x, y, z, n, r, g, b, spread, speed);
    }

    /** 最近的玩家(用于选目标); 无则 null。
     *  <p>v114 按用户反馈「boss 索敌会锁定创造模式玩家」: **创造/旁观玩家不算目标**。 */
    public net.minecraft.world.entity.LivingEntity nearestPlayer(double range) {
        return BossTargets.combatTarget(e, range);
    }

    /** 醉酒毒雾光环: 半透明酒雾 + 淡红酒尘 */
    public void mist() {
        double x = e.getX() + (e.getRandom().nextDouble() - 0.5) * 3.0;
        double y = e.getY() + e.getRandom().nextDouble() * 2.4;
        double z = e.getZ() + (e.getRandom().nextDouble() - 0.5) * 3.0;
        part(x, y, z, 2, ParticleTypes.SPORE_BLOSSOM_AIR, 0.4, 0.01);
        fx(x, y, z, 1, 0.72f, 0.10f, 0.14f, 0.35, 0.01);
    }

    /** 滚动酒渍 + 木屑 */
    public void rollTrail() {
        double x = e.getX() + (e.getRandom().nextDouble() - 0.5) * 1.6;
        double z = e.getZ() + (e.getRandom().nextDouble() - 0.5) * 1.6;
        fx(x, e.getY() + 0.15, z, 2, 0.62f, 0.10f, 0.16f, 0.25, 0.005);
        part(x, e.getY() + 0.1, z, 1, ParticleTypes.CRIT, 0.3, 0.02);
    }

    /** 砸地: 木屑 + 冲击波环 + 6.5 格 AoE 击退/反胃 */
    public void slam() {
        ServerLevel s = sl();
        if (s == null) return;
        double x = e.getX(), y = e.getY() + 0.3, z = e.getZ();
        snd(SoundEvents.WOOD_BREAK, 1.7f, 0.5f);
        snd(SoundEvents.GENERIC_EXPLODE, 0.5f, 1.6f);
        Fx.ring(e.level(), x, y, z, 6.5, 36, ParticleTypes.CLOUD, 0.02);
        part(x, y, z, 20, ParticleTypes.CRIT, 1.3, 0.28);
        part(x, y, z, 12, ParticleTypes.POOF, 1.1, 0.06);
        fx(x, y, z, 14, 0.85f, 0.62f, 0.18f, 0.9, 0.15);
        for (ServerPlayer p : e.level().getEntitiesOfClass(ServerPlayer.class, e.getBoundingBox().inflate(6.5))) {
            if (!BossTargets.valid(p) || e.distanceToSqr(p) > 6.5 * 6.5) continue;
            if (p.hurt(e.damageSources().mobAttack(e), 4.75f * this.damageScale)) {   // ×0.5(v77); v159 继承 ×5
                net.farmingtales.ftboss.util.BossKnockback.knockback(p, 1.5, x - p.getX(), z - p.getZ());
                part(p.getX(), p.getY() + 1.9, p.getZ(), 8, ParticleTypes.WITCH, 0.35, 0.06);
            }
        }
    }

    /** 开塞喷射: 正面约70°锥形酒液 + 缓慢/醉酒 */
    public void corkSpray() {
        ServerLevel s = sl();
        if (s == null) return;
        snd(SoundEvents.BOTTLE_EMPTY, 1.5f, 0.8f);
        snd(SoundEvents.HONEY_BLOCK_SLIDE, 1.0f, 0.7f);
        Vec3 dir = new Vec3(-Math.sin(Math.toRadians(e.getYRot())), 0, Math.cos(Math.toRadians(e.getYRot())));
        double x = e.getX() + dir.x * 1.4, y = e.getY() + 1.7, z = e.getZ() + dir.z * 1.4;
        for (int i = 0; i < 26; i++) {
            double sp = 1.2 + e.getRandom().nextDouble() * 2.4;
            s.sendParticles(new DustParticleOptions(new Vector3f(0.78f, 0.10f, 0.16f), 1.1f), x, y, z, 1,
                    dir.x * sp + (e.getRandom().nextDouble() - 0.5) * 0.5,
                    0.25 + e.getRandom().nextDouble() * 0.6,
                    dir.z * sp + (e.getRandom().nextDouble() - 0.5) * 0.5, 0.12);
        }
        fx(x, y, z, 10, 0.88f, 0.66f, 0.20f, 0.6, 0.12);
        for (ServerPlayer p : e.level().getEntitiesOfClass(ServerPlayer.class, e.getBoundingBox().inflate(11))) {
            if (!BossTargets.valid(p) || !e.hasLineOfSight(p) || Math.abs(p.getY() - e.getY()) > 3) continue;
            Vec3 to = new Vec3(p.getX() - e.getX(), 0, p.getZ() - e.getZ());
            if (to.length() > 11.0 || to.lengthSqr() < 0.001) continue;
            if (dir.dot(to.normalize()) < 0.35) continue;
            if (p.hurt(e.damageSources().mobAttack(e), 3.0f * this.damageScale)) {    // ×0.5(v77); v159 继承 ×5
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, false, true));
                part(p.getX(), p.getY() + 1.9, p.getZ(), 8, ParticleTypes.WITCH, 0.35, 0.06);
            }
        }
    }

    public void warnSlam() {
        Fx.ring(e.level(), e.getX(), e.getY() + 0.12, e.getZ(), 6.5, 24, ParticleTypes.CRIT, 0.0);
    }

    public void warnCharge(double forwardX, double forwardZ) {
        for (int step = 2; step <= 10; step += 2) {
            part(e.getX() + forwardX * step, e.getY() + 0.12, e.getZ() + forwardZ * step,
                    1, ParticleTypes.CRIT, 0.04, 0.0);
        }
    }

    public void sprayTrail() {
        double yaw = Math.toRadians(e.getYRot());
        for (int step = 2; step <= 10; step += 2) {
            fx(e.getX() - Math.sin(yaw) * step, e.getY() + 1.2, e.getZ() + Math.cos(yaw) * step,
                    2, 0.78f, 0.10f, 0.16f, step * 0.12, 0.015);
        }
    }

    /** 冲撞接触伤害 + 击退 */
    public void hitPlayers(double radius, float dmg, double kb) {
        for (ServerPlayer p : e.level().getEntitiesOfClass(ServerPlayer.class, e.getBoundingBox().inflate(radius))) {
            if (!p.isAlive()) continue;
            if (p.hurt(e.damageSources().mobAttack(e), dmg * 0.5f * this.damageScale)) {   // ×0.5(v77); v159 继承 ×5
                net.farmingtales.ftboss.util.BossKnockback.knockback(p, kb, e.getX() - p.getX(), e.getZ() - p.getZ());
            }
        }
    }
}
