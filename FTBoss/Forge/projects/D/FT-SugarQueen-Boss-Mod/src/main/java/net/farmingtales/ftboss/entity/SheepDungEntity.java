package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 【羊屎蛋】(2026-09-12 按用户要求新增, 炫彩答辩超人的召唤物)。
 *
 * <p>本体是**用岩浆怪套皮**: 直接继承 {@link MagmaCube}(→Slime), 所以弹跳、挤压形变、
 * 死亡分裂成小颗粒全部白送, 只换一张暗棕黑贴图
 * ({@code tools/model3d/gen_sheep_dung_tex.cjs}, 拿原版 magmacube.png 的形状与明暗做色相映射)。
 *
 * <p>额外行为: **死亡时炸开** —— 小范围无破坏爆炸 + 反胃 + 刷新「屎？！！」。
 * 大颗粒炸完还会分裂成小颗粒, 可以连锁炸。
 */
public class SheepDungEntity extends MagmaCube {

    /** 存活时间(45 秒) */
    public static final int LIFE = 900;
    /** 爆炸半径(不破坏地形) */
    private static final float BLAST_RADIUS = 0.95F;    // 1.9 → ×0.5(v77)
    /** 直接伤害半径内的玩家 */
    private static final double HURT_RANGE = 3.0;

    private boolean blasted = false;

    public SheepDungEntity(EntityType<? extends SheepDungEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount > LIFE) this.discard();
    }

    /** 死亡即炸(寿命到点消散则不炸 —— 那是"化掉"不是"炸开") */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.blast();
    }

    private void blast() {
        if (this.level().isClientSide || this.blasted) return;
        this.blasted = true;
        double x = this.getX(), y = this.getY() + 0.25, z = this.getZ();
        this.level().explode(this, x, y, z, BLAST_RADIUS,
                net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        this.level().playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.9F, 1.5F);
        Fx.particles(this.level(), x, y, z, 18, ParticleTypes.ITEM_SLIME, 0.9, 0.12);
        Fx.particles(this.level(), x, y, z, 8, ParticleTypes.LARGE_SMOKE, 0.7, 0.06);
        for (Player p : this.level().players()) {
            if (!p.isAlive() || p.distanceToSqr(x, y, z) > HURT_RANGE * HURT_RANGE) continue;
            p.hurt(this.damageSources().mobAttack(this), 2.5F);   // 5 → ×0.5(v77)
            /* 2026-09-12 晚按用户要求: 召唤物**不再**施放「屎？！！」(那现在只由 Boss 概率施放一次) */
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
