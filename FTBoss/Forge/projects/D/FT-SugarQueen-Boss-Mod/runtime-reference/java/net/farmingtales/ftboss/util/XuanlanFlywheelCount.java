/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.util;

import java.util.List;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class XuanlanFlywheelCount {
    private XuanlanFlywheelCount() {
    }

    public static int peripheralLimit(int n) {
        if (n <= 1) {
            return 0;
        }
        return (int)Math.min(Integer.MAX_VALUE, ((long)n - 1L) * 10L);
    }

    public static int playerCount(EasterBossEntity easterBossEntity, ServerLevel serverLevel) {
        Vec3 vec3 = ManorFight.anchorOf(ManorFightMembers.fightId((Entity)easterBossEntity));
        if (vec3 == null) {
            vec3 = easterBossEntity.m_20182_();
        }
        List list = serverLevel.m_6907_();
        double d = 9216.0;
        int n = 0;
        for (int i = 0; i < list.size(); ++i) {
            ServerPlayer serverPlayer = (ServerPlayer)list.get(i);
            if (!serverPlayer.m_6084_() || serverPlayer.m_5833_() || serverPlayer.m_7500_() || !(serverPlayer.m_20275_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_) <= d)) continue;
            ++n;
        }
        return n;
    }

    public static int limit(EasterBossEntity easterBossEntity, ServerLevel serverLevel) {
        return XuanlanFlywheelCount.peripheralLimit(XuanlanFlywheelCount.playerCount(easterBossEntity, serverLevel));
    }
}

