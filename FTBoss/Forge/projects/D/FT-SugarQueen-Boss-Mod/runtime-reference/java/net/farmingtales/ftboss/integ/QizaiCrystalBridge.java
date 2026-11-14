/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.integ;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class QizaiCrystalBridge {
    private static Shields shields;

    private QizaiCrystalBridge() {
    }

    public static void install(Shields value) {
        shields = value;
    }

    public static Entity create(ServerLevel level, EasterBossEntity owner, Vec3 position, float yaw) {
        return shields == null ? null : shields.create(level, owner, position, yaw);
    }

    public static void move(Entity shield, Vec3 position, float yaw) {
        if (shields != null && shield != null && !shield.m_213877_()) {
            shields.move(shield, position, yaw);
        }
    }

    public static interface Shields {
        public Entity create(ServerLevel var1, EasterBossEntity var2, Vec3 var3, float var4);

        public void move(Entity var1, Vec3 var2, float var3);
    }
}

