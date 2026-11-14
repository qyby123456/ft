package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.damagesource.DamageSource;

public interface NativeManorFinalDamageAccess {
    float ftboss$reduceManorDamage(DamageSource source, float amount);
    EasterBossEntity ftboss$manorSparrow();
    float ftboss$shareManorDamage(float amount, DamageSource source);
    boolean ftboss$blocksManorDamage();
}
