package net.farmingtales.ftboss.boss.ai;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class XueliCrushDamage {
    private static final ThreadLocal<LivingEntity> ACTIVE_VICTIM = new ThreadLocal<>();

    private XueliCrushDamage() {}

    public static boolean isActive(LivingEntity victim) {
        return victim == ACTIVE_VICTIM.get();
    }

    public static boolean hurt(LivingEntity victim, DamageSource source, float amount) {
        LivingEntity previous = ACTIVE_VICTIM.get();
        ACTIVE_VICTIM.set(victim);
        try {
            return victim.hurt(source, amount);
        } finally {
            if (previous == null) ACTIVE_VICTIM.remove();
            else ACTIVE_VICTIM.set(previous);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() == ACTIVE_VICTIM.get()) event.setCanceled(true);
    }
}
