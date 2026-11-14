/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.youzi.motion;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.youzi.motion.YouziBodyState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public record YouziBodyPacket(String dimension, int actorId, UUID actor, long tick) {
    public YouziBodyPacket {
        if (string == null || string.length() > 256 || uUID == null || l < 0L) {
            throw new IllegalArgumentException("Invalid summon body packet");
        }
    }

    public static void encode(YouziBodyPacket youziBodyPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.m_130072_(youziBodyPacket.dimension(), 256);
        friendlyByteBuf.m_130130_(youziBodyPacket.actorId());
        friendlyByteBuf.writeLong(youziBodyPacket.actor().getMostSignificantBits());
        friendlyByteBuf.writeLong(youziBodyPacket.actor().getLeastSignificantBits());
        friendlyByteBuf.writeLong(youziBodyPacket.tick());
    }

    public static YouziBodyPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new YouziBodyPacket(friendlyByteBuf.m_130136_(256), friendlyByteBuf.m_130242_(), new UUID(friendlyByteBuf.readLong(), friendlyByteBuf.readLong()), friendlyByteBuf.readLong());
    }

    public static void handle(YouziBodyPacket youziBodyPacket, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> YouziBodyState.summon(youziBodyPacket)));
        }
        context.setPacketHandled(true);
    }
}

