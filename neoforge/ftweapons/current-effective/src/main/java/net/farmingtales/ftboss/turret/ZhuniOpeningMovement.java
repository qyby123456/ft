package net.farmingtales.ftboss.turret;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.phys.Vec3;

public final class ZhuniOpeningMovement {
    private static final double HORIZONTAL_RANGE = 1.2;
    private static final double HEIGHT = 0.85;
    private static final double BOB_RANGE = 0.16;

    private ZhuniOpeningMovement() {
    }

    public static Vec3 offset(long var0) {
        return new Vec3(Math.sin((double)var0 * 0.045) * 1.2, 0.85 + Math.sin((double)var0 * 0.06) * 0.16, Math.cos((double)var0 * 0.033));
    }

    public static Vec3 preferred(EasterBossEntity var0) {
        return var0.position().add(offset(var0.level().getGameTime()));
    }
}
