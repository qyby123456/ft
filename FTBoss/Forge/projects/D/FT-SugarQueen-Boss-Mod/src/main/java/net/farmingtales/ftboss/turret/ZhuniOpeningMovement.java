package net.farmingtales.ftboss.turret;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.phys.Vec3;

public final class ZhuniOpeningMovement {
    private static final double HORIZONTAL_RANGE = 1.2;
    private static final double HEIGHT = 0.85;
    private static final double BOB_RANGE = 0.16;

    private ZhuniOpeningMovement() {}

    public static Vec3 offset(long tick) {
        return new Vec3(Math.sin(tick * 0.045) * HORIZONTAL_RANGE,
                HEIGHT + Math.sin(tick * 0.06) * BOB_RANGE, Math.cos(tick * 0.033));
    }

    public static Vec3 preferred(EasterBossEntity boss) {
        return boss.position().add(offset(boss.level().getGameTime()));
    }
}
