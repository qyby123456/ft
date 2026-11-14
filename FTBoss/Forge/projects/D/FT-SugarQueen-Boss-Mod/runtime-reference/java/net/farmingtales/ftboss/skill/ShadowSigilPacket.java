/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.skill;

import java.util.function.Supplier;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.render.ShadowSigilVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public final class ShadowSigilPacket {
    private final double x;
    private final double y;
    private final double z;
    private final boolean pillar;

    public ShadowSigilPacket(double x, double y, double z, boolean pillar) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pillar = pillar;
    }

    public static void encode(ShadowSigilPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeBoolean(msg.pillar);
    }

    public static ShadowSigilPacket decode(FriendlyByteBuf buf) {
        return new ShadowSigilPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean());
    }

    public static void handle(ShadowSigilPacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        FTBossMod.LOGGER.info("[FTBoss][\u9547\u5370] \u5ba2\u6237\u7aef\u6536\u5230\u5305 ({}, {}, {}) \u67f1={}", new Object[]{String.format("%.2f", msg.x), String.format("%.2f", msg.y), String.format("%.2f", msg.z), msg.pillar});
        ShadowSigilVisual.spawn(msg.x, msg.y, msg.z, msg.pillar);
    }
}

