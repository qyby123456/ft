/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class XuanlanLengths {
    public static final double BEAM = 80.0;
    public static final double RAIL = 36.0;

    private XuanlanLengths() {
    }

    public static double pressure(double d, LivingEntity livingEntity, ManorSignatureSpell manorSignatureSpell) {
        double d2;
        if (livingEntity instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)livingEntity;
            d2 = XuanlanLengths.pressureLength(d, easterBossEntity.getVariant(), manorSignatureSpell.kind);
        } else {
            d2 = d;
        }
        return d2;
    }

    public static double pressureLength(double d, int n, ManorSpellRules.Kind kind) {
        return n == 6 && kind == ManorSpellRules.Kind.PRESSURE_LANCE ? d * 2.0 : d;
    }

    public static Vec3 extend(Vec3 vec3, Vec3 vec32) {
        return vec3.m_82549_(vec32.m_82546_(vec3).m_82490_(2.0));
    }

    public static AABB segmentBounds(Vec3 vec3, Vec3 vec32, double d) {
        AABB aABB = XuanlanLengths.bounds(vec3, vec32, d);
        return aABB == null ? new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0) : aABB;
    }

    public static AABB bounds(Vec3 vec3, Vec3 vec32, double d) {
        return XuanlanLengths.finite(vec3) && XuanlanLengths.finite(vec32) && vec3.m_82557_(vec32) <= 6400.001 && Double.isFinite(d) && d > 0.0 && d <= 4.0 ? new AABB(vec3, vec32).m_82400_(d + 1.0) : null;
    }

    private static boolean finite(Vec3 vec3) {
        return Double.isFinite(vec3.f_82479_) && Double.isFinite(vec3.f_82480_) && Double.isFinite(vec3.f_82481_);
    }
}

