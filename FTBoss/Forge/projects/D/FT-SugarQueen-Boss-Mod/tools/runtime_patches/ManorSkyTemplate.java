package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayDeque;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** 仅移植新增的入队方法，不替换当前运行版的多人天罚状态机。 */
final class ManorSkyTemplate {
    private EasterBossEntity boss;
    private Vec3 home;
    private ArrayDeque<PendingStrike> pendingStrikes;
    private record PendingStrike(Vec3 locked, Vec3 beamTop, int impactAge) {}

    private void queuePrediction(ServerLevel level, LivingEntity target, int impactAge) {
        Vec3 point = ManorPredictiveAttacks.skyPrediction(level, boss, target, home);
        if (point == null) return;
        Vec3 top = point.add(0, Math.max(24, target.getY() - point.y + 8), 0);
        pendingStrikes.addLast(new PendingStrike(point, top, impactAge));
        XuanlanFxPacket.send(level, XuanlanFxPacket.SKY_WARNING, boss.getId(), point, top);
    }
}
