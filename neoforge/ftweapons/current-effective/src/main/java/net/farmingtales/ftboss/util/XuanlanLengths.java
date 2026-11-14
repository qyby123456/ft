package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class XuanlanLengths {
    public static final double BEAM = 80.0;
    public static final double RAIL = 36.0;

    private XuanlanLengths() {
    }

    public static double pressure(double original, LivingEntity actor, ManorSignatureSpell spell) {
        return actor instanceof EasterBossEntity boss ? pressureLength(original, boss.getVariant(), spell.kind) : original;
    }

    public static double pressureLength(double original, int variant, ManorSpellRules.Kind kind) {
        return variant == 6 && kind == ManorSpellRules.Kind.PRESSURE_LANCE ? original * 2.0 : original;
    }

    public static Vec3 extend(Vec3 start, Vec3 end) {
        return start.add(end.subtract(start).scale(2.0));
    }

    public static AABB segmentBounds(Vec3 start, Vec3 end, double radius) {
        AABB result = bounds(start, end, radius);
        return result == null ? new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0) : result;
    }

    public static AABB bounds(Vec3 start, Vec3 end, double radius) {
        return finite(start) && finite(end) && start.distanceToSqr(end) <= 6400.001 && Double.isFinite(radius) && radius > 0.0 && radius <= 4.0 ? new AABB(start, end).inflate(radius + 1.0) : null;
    }

    private static boolean finite(Vec3 value) {
        return Double.isFinite(value.x) && Double.isFinite(value.y) && Double.isFinite(value.z);
    }
}

