package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorDamageLimitRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

public final class ManorDamageLimit {
    private static final String STATE = "ftboss:manor_damage_limit";

    private ManorDamageLimit() {}

    public static boolean applies(LivingEntity entity) {
        return entity instanceof EasterBossEntity boss && !boss.isMini() && !boss.isGiant()
                && (EasterBossEntity.isManorVariant(boss.getVariant())
                || boss.getVariant() == EasterBossEntity.SPARROW_VARIANT);
    }

    public static float limit(LivingEntity entity, DamageSource source, float finalDamage) {
        if (!applies(entity) || entity.level().isClientSide() || source == null
                || source.is(DamageTypes.GENERIC_KILL)) return finalDamage;
        if (!entity.isAlive() || !Float.isFinite(finalDamage) || finalDamage <= 0.0F) return 0.0F;
        EasterBossEntity boss = (EasterBossEntity) entity;
        CompoundTag data = boss.getPersistentData().getCompound(STATE);
        if (!data.contains("variant") || data.getInt("variant") != boss.getVariant()) {
            data = new CompoundTag();
            data.putInt("variant", boss.getVariant());
            boss.getPersistentData().put(STATE, data);
        }
        ManorDamageLimitRules.Window window = new ManorDamageLimitRules.Window(
                data.getLongArray("ticks"), data.getLongArray("amounts"), data.getLong("last_tick"));
        float accepted = window.take(entity.level().getGameTime(), ManorDamageKind.classify(source),
                Math.min(finalDamage, entity.getHealth()), SculkSpecialDamage.hitLimit(source),
                ManorDamageLimitRules.MAX_KIND_DAMAGE, ManorDamageLimitRules.MAX_TOTAL_DAMAGE);
        data.putLongArray("ticks", window.ticks());
        data.putLongArray("amounts", window.amounts());
        data.putLong("last_tick", window.lastTick());
        return accepted;
    }

    public static float sharedHealth(float health, float share, LivingEntity entity, DamageSource source) {
        return health - limit(entity, source, share);
    }
}
