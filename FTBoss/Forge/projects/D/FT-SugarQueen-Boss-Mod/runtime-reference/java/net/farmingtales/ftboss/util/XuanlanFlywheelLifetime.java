/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.farmingtales.ftboss.entity.XuanlanEliteFlywheels;
import net.minecraft.world.entity.Entity;

public final class XuanlanFlywheelLifetime {
    private XuanlanFlywheelLifetime() {
    }

    public static int activeAge(int n, Entity entity) {
        EasterBossEntity easterBossEntity;
        if (entity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity).getVariant() == 6 && easterBossEntity.m_6084_()) {
            return Math.max(0, Math.min(3600, n));
        }
        return n;
    }

    public static void skyPosition(FlywheelProjectileEntity flywheelProjectileEntity, double d, double d2, double d3, double d4, double d5) {
        double d6 = XuanlanEliteFlywheels.widen((Entity)flywheelProjectileEntity, d5);
        flywheelProjectileEntity.m_6034_(d + Math.cos(d4) * d6, d2, d3 + Math.sin(d4) * d6);
    }
}

