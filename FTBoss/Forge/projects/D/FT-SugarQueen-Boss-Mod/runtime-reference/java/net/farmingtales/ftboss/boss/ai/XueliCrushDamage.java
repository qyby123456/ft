/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.event.entity.living.LivingKnockBackEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class XueliCrushDamage {
    private static final ThreadLocal<LivingEntity> ACTIVE_VICTIM = new ThreadLocal();

    private XueliCrushDamage() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean hurt(LivingEntity victim, DamageSource source, float amount) {
        LivingEntity previous = ACTIVE_VICTIM.get();
        ACTIVE_VICTIM.set(victim);
        try {
            boolean bl = victim.m_6469_(source, amount);
            return bl;
        }
        finally {
            if (previous == null) {
                ACTIVE_VICTIM.remove();
            } else {
                ACTIVE_VICTIM.set(previous);
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() == ACTIVE_VICTIM.get()) {
            event.setCanceled(true);
        }
    }
}

