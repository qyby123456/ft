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
package net.farmingtales.ftboss.youzi;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.youzi.YouziCombatEvents;
import net.farmingtales.ftboss.youzi.YouziHomingFlight;
import net.farmingtales.ftboss.youzi.client.YouziCombatVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public record YouziCombatPacket(String effect, String dimension, long sequence, long startedTick, YouziHomingFlight.Vector origin, YouziHomingFlight.Vector target, double scale, double yaw, int actorId, UUID actor) {
    public YouziCombatPacket {
        if (!YouziCombatEvents.validIdentifier(string) || string2 == null || string2.length() > 256 || l < 0L || l2 < 0L || vector == null || vector2 == null || uUID == null || !Double.isFinite(d) || d <= 0.0 || d > 4.0 || !Double.isFinite(d2)) {
            throw new IllegalArgumentException("Invalid Youzi combat packet");
        }
    }

    public static void encode(YouziCombatPacket youziCombatPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.m_130072_(youziCombatPacket.effect, 32);
        friendlyByteBuf.m_130072_(youziCombatPacket.dimension, 256);
        friendlyByteBuf.writeLong(youziCombatPacket.sequence);
        friendlyByteBuf.writeLong(youziCombatPacket.startedTick);
        for (YouziHomingFlight.Vector vector : new YouziHomingFlight.Vector[]{youziCombatPacket.origin, youziCombatPacket.target}) {
            friendlyByteBuf.writeDouble(vector.coordinateX());
            friendlyByteBuf.writeDouble(vector.coordinateY());
            friendlyByteBuf.writeDouble(vector.coordinateZ());
        }
        friendlyByteBuf.writeDouble(youziCombatPacket.scale);
        friendlyByteBuf.writeDouble(youziCombatPacket.yaw);
        friendlyByteBuf.m_130130_(youziCombatPacket.actorId);
        friendlyByteBuf.writeLong(youziCombatPacket.actor.getMostSignificantBits());
        friendlyByteBuf.writeLong(youziCombatPacket.actor.getLeastSignificantBits());
    }

    public static YouziCombatPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new YouziCombatPacket(friendlyByteBuf.m_130136_(32), friendlyByteBuf.m_130136_(256), friendlyByteBuf.readLong(), friendlyByteBuf.readLong(), new YouziHomingFlight.Vector(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()), new YouziHomingFlight.Vector(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.m_130242_(), new UUID(friendlyByteBuf.readLong(), friendlyByteBuf.readLong()));
    }

    public static void handle(YouziCombatPacket youziCombatPacket, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> YouziCombatVisual.accept(youziCombatPacket)));
        }
        context.setPacketHandled(true);
    }
}

