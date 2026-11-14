package net.farmingtales.ftboss.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 【蓝火】(2026-09-14 v119 按用户要求
 * 「柠檬现在的火焰伤害会独立计算成新的 debuff：蓝火：每秒造成 20 点虚空伤害」)。
 *
 * <p>柠檬猫的「猛火翻锅」除了原本的点燃, 还会给玩家挂上这个独立 debuff:
 * **每秒 10 点虚空伤害**(`fellOutOfWorld`: 自带穿甲、不吃护甲与魔法减免) ——
 * 火焰那部分伤害从此独立结算, 玩家能在 HUD 上看到"蓝火"还剩几秒。
 */
public class BlueFireEffect extends MobEffect {

    /** 每秒的虚空伤害 */
    public static final float DAMAGE_PER_SECOND = 10.0F;
    /** 默认时长(和翻锅的点燃一样 18 秒) */
    public static final int TICKS = (int) (6 * 20 * 3);

    public BlueFireEffect() {
        super(MobEffectCategory.HARMFUL, 0x2F7BE6);   // 蓝火
    }

    @Override
    public boolean isBeneficial() {
        return false;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        entity.invulnerableTime = 0;
        entity.hurt(entity.damageSources().fellOutOfWorld(),
                net.farmingtales.ftboss.boss.ai.ManorCombatHalving.statusDamage(DAMAGE_PER_SECOND));
    }
}
