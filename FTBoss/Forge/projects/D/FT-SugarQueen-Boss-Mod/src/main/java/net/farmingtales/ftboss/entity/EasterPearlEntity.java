package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * 【末影珍珠 · 落地冲击】(2026-09-12 v54 用户定案):
 * 「末影珍珠落地后对周围玩家造成 45 点魔法伤害」。
 *
 * <p>以前彩蛋 Boss 那颗珍珠只是纯视觉({@code new ThrownEnderpearl(...)} 直接扔), 落地什么也不干;
 * 现在换成这个子类 —— 落地(撞到方块/实体)时对**落点周围 4 格内的玩家**造成
 * {@value #LAND_DAMAGE} 点**魔法伤害**(indirectMagic, 穿护甲), 然后照旧走原版的"把主人拉过去"。
 *
 * <p>和 {@link QingyiTridentEntity} 一样的做法: 服务端用这个子类, 客户端由原版
 * {@code minecraft:ender_pearl} 的渲染器画(子类 {@code getType()} 就是香草原版珍珠类型), 所以不用注册新实体。
 */
public class EasterPearlEntity extends ThrownEnderpearl {

    /** 落地魔法伤害(用户定案: 45) */
    public static final float LAND_DAMAGE = 30.0f;      // 45 → ×2/3(已含全局修正, v77)
    /** 落地伤害半径(格) */
    public static final double LAND_RADIUS = 4.0;

    public EasterPearlEntity(EntityType<? extends ThrownEnderpearl> type, Level level) {
        super(type, level);
    }

    public EasterPearlEntity(Level level, LivingEntity thrower) {
        super(level, thrower);
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            int hits = 0;
            LivingEntity owner = this.getOwner() instanceof LivingEntity le ? le : null;
            var src = this.damageSources().indirectMagic(this, owner);
            for (Player p : this.level().players()) {
                if (!p.isAlive() || p.isSpectator()) continue;
                if (p.distanceToSqr(this) > LAND_RADIUS * LAND_RADIUS) continue;
                p.invulnerableTime = 0;
                float damage = owner instanceof EasterBossEntity boss ? boss.manorSkillDamage(p, LAND_DAMAGE, 1.5) : LAND_DAMAGE;
                p.hurt(src, damage);
                hits++;
            }
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY() + 0.2, this.getZ(),
                        45, 1.3, 0.4, 1.3, 0.09);
                sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.2, this.getZ(),
                        30, 1.0, 0.4, 1.0, 0.25);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENDER_DRAGON_HURT, SoundSource.HOSTILE, 1.2f, 1.4f);
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] 末影珍珠落地: 命中 {} 名玩家 ({} 点魔法伤害, 半径 {} 格)",
                    hits, (int) LAND_DAMAGE, (int) LAND_RADIUS);
        }
        super.onHit(result);   // 原版行为: 把主人(扔珍珠的 Boss)拉到落点
    }
}
