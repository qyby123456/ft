package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.util.ManorDamageLimitRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** 幽匿特调的单次伤害归因；2026-10-28：最终扣血最多 25 点，不改变其他音爆或非庄园目标。 */
public final class SculkSpecialDamage {
    public static final float MAX_HIT_DAMAGE = 25.0F;
    private static final ThreadLocal<DamageSource> ACTIVE_SOURCE = new ThreadLocal<>();

    private SculkSpecialDamage() {}

    public static float hitLimit(DamageSource source) {
        return source != null && ACTIVE_SOURCE.get() == source ? MAX_HIT_DAMAGE : ManorDamageLimitRules.MAX_HIT_DAMAGE;
    }

    public static boolean hurt(LivingEntity target, DamageSource source, float amount) {
        if (!ManorDamageLimit.applies(target) || target.level().isClientSide()) return target.hurt(source, amount);
        DamageSource previous = ACTIVE_SOURCE.get();
        ACTIVE_SOURCE.set(source);
        try {
            return target.hurt(source, amount);
        } finally {
            if (previous == null) ACTIVE_SOURCE.remove();
            else ACTIVE_SOURCE.set(previous);
        }
    }
}
