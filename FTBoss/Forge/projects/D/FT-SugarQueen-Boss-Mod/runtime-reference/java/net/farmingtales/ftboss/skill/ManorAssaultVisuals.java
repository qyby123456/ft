/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.PacketDistributor$TargetPoint
 */
package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ManorAssaultPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public final class ManorAssaultVisuals {
    private ManorAssaultVisuals() {
    }

    public static void show(ServerLevel level, EasterBossEntity boss, Vec3 center, boolean release) {
        ManorAssaultPacket packet = new ManorAssaultPacket(level.m_46472_().m_135782_(), boss.m_20148_(), boss.m_19879_(), center, boss.getVariant(), release);
        FTNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(null, center.f_82479_, center.f_82480_, center.f_82481_, 96.0, level.m_46472_())), (Object)packet);
    }
}

