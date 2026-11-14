package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ManorAssaultVisuals {
    private ManorAssaultVisuals() {
    }

    public static void show(ServerLevel level, EasterBossEntity boss, Vec3 center, boolean release) {
        ManorAssaultPacket packet = new ManorAssaultPacket(level.dimension().location(), boss.getUUID(), boss.getId(), center, boss.getVariant(), release);
        PacketDistributor.sendToPlayersNear(level, null, center.x, center.y, center.z, 96.0, packet);
    }
}
