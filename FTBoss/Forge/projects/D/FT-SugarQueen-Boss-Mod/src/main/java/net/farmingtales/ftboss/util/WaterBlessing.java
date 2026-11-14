package net.farmingtales.ftboss.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 水族增益（2026-09-17 按用户要求新增）。
 *
 * <p>适用范围：潮宴鱼王本体与其水族召唤物（鱼卫 / 河豚）。召唤物在自己的 tick 里调用同一入口，
 * 以后要再挂到别的"水类 Boss"上，只需在那只 Boss 的服务端 tick 里加一行 {@code WaterBlessing.tick(this)}。
 *
 * <p>规则：常驻至少 3 个正面效果（速度 I、抗性提升 I、力量 I），每 {@link #INTERVAL_TICKS} 刷新一次；
 * 下雨（或雷雨）时额外再挂 2 个（生命恢复 I、水下呼吸 I）。效果不可见（不刷粒子、不占 HUD 图标位）。
 */
public final class WaterBlessing {
    public static final int INTERVAL_TICKS = 40;
    private static final int DURATION_TICKS = 60;

    private WaterBlessing() {}

    /** 服务端 tick 调用；内部自行按 {@link #INTERVAL_TICKS} 限流。 */
    public static void tick(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide() || !entity.isAlive()) return;
        if (entity.tickCount % INTERVAL_TICKS != 0) return;
        apply(entity);
    }

    /** 立刻整套挂一遍（召唤时也可以用）。 */
    public static void apply(LivingEntity entity) {
        if (entity.level().isClientSide() || !entity.isAlive()) return;
        hidden(entity, MobEffects.MOVEMENT_SPEED);
        hidden(entity, MobEffects.DAMAGE_RESISTANCE);
        hidden(entity, MobEffects.DAMAGE_BOOST);
        if (isRainy(entity.level())) {
            hidden(entity, MobEffects.REGENERATION);
            hidden(entity, MobEffects.WATER_BREATHING);
        }
    }

    public static boolean isRainy(Level level) {
        return level != null && (level.isRaining() || level.isThundering());
    }

    private static void hidden(LivingEntity entity, net.minecraft.world.effect.MobEffect effect) {
        entity.addEffect(new MobEffectInstance(effect, DURATION_TICKS, 0, true, false));
    }
}
