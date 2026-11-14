/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneActorVisuals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record ArcaneWardPacket(int entityId, UUID identity, ResourceLocation dimension, long tick, boolean active) {
    public static void encode(ArcaneWardPacket arcaneWardPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(arcaneWardPacket.entityId());
        friendlyByteBuf.m_130077_(arcaneWardPacket.identity());
        friendlyByteBuf.m_130085_(arcaneWardPacket.dimension());
        friendlyByteBuf.writeLong(arcaneWardPacket.tick());
        friendlyByteBuf.writeBoolean(arcaneWardPacket.active());
    }

    public static ArcaneWardPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new ArcaneWardPacket(friendlyByteBuf.readInt(), friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130281_(), friendlyByteBuf.readLong(), friendlyByteBuf.readBoolean());
    }

    public static void handle(ArcaneWardPacket arcaneWardPacket, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ArcaneActorVisuals.receive(arcaneWardPacket)));
        context.setPacketHandled(true);
    }
}

