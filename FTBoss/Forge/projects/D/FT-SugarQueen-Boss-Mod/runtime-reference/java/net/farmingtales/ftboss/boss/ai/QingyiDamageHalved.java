/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class QingyiDamageHalved {
    private QingyiDamageHalved() {
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        try {
            EasterBossEntity boss;
            Projectile projectile;
            Entity src = e.getSource().m_7639_();
            if (src == null) {
                src = e.getSource().m_7640_();
            }
            if (src instanceof Projectile && (projectile = (Projectile)src).m_19749_() != null) {
                src = projectile.m_19749_();
            }
            if (!(src instanceof EasterBossEntity) || (boss = (EasterBossEntity)src).getVariant() != 5) {
                return;
            }
            float amount = e.getAmount();
            if (!Float.isFinite(amount) || amount <= 0.0f) {
                return;
            }
            e.setAmount(amount * 0.5f);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

