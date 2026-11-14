/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class NonManorDamageX2 {
    private NonManorDamageX2() {
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        try {
            Projectile pr;
            Entity src = e.getSource().m_7639_();
            if (src == null) {
                src = e.getSource().m_7640_();
            }
            if (src instanceof Projectile && (pr = (Projectile)src).m_19749_() != null) {
                src = pr.m_19749_();
            }
            if (src == null) {
                return;
            }
            ResourceLocation key = EntityType.m_20613_((EntityType)src.m_6095_());
            if (key == null || !"ftboss".equals(key.m_135827_())) {
                return;
            }
            if (src instanceof EasterBossEntity) {
                return;
            }
            if (!(src instanceof Mob)) {
                return;
            }
            float amt = e.getAmount();
            if (amt <= 0.0f) {
                return;
            }
            e.setAmount(amt * 2.0f);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

