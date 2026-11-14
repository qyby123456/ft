package net.farmingtales.ftboss.integ;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class QizaiCrystalBridge {
    public interface Shields {
        Entity create(ServerLevel level, EasterBossEntity owner, Vec3 position, float yaw);
        void move(Entity shield, Vec3 position, float yaw);
    }

    private static Shields shields;

    private QizaiCrystalBridge() {}

    public static void install(Shields value) { shields = value; }

    public static Entity create(ServerLevel level, EasterBossEntity owner, Vec3 position, float yaw) {
        return shields == null ? null : shields.create(level, owner, position, yaw);
    }

    public static void move(Entity shield, Vec3 position, float yaw) {
        if (shields != null && shield != null && !shield.isRemoved()) shields.move(shield, position, yaw);
    }
}
