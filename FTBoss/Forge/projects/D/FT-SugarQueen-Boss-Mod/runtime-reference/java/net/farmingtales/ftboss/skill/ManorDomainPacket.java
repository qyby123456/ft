/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.skill;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.ManorDomainVisual;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public record ManorDomainPacket(ResourceLocation dimension, UUID owner, int ownerId, Vec3 center, int variant, boolean supported) {
    public static void register() {
        FTNetwork.CHANNEL.messageBuilder(ManorDomainPacket.class, 9, NetworkDirection.PLAY_TO_CLIENT).encoder(ManorDomainPacket::encode).decoder(ManorDomainPacket::decode).consumerMainThread(ManorDomainPacket::handle).add();
    }

    public static void encode(ManorDomainPacket manorDomainPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.m_130085_(manorDomainPacket.dimension);
        friendlyByteBuf.m_130077_(manorDomainPacket.owner);
        friendlyByteBuf.m_130130_(manorDomainPacket.ownerId);
        friendlyByteBuf.writeDouble(manorDomainPacket.center.f_82479_).writeDouble(manorDomainPacket.center.f_82480_).writeDouble(manorDomainPacket.center.f_82481_);
        friendlyByteBuf.m_130130_(manorDomainPacket.variant);
        friendlyByteBuf.writeBoolean(manorDomainPacket.supported);
    }

    public static ManorDomainPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new ManorDomainPacket(friendlyByteBuf.m_130281_(), friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130242_(), new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()), friendlyByteBuf.m_130242_(), friendlyByteBuf.readBoolean());
    }

    public static void handle(ManorDomainPacket manorDomainPacket, Supplier<NetworkEvent.Context> supplier) {
        if (manorDomainPacket.variant >= -1 && manorDomainPacket.variant <= 11 && Double.isFinite(manorDomainPacket.center.f_82479_) && Double.isFinite(manorDomainPacket.center.f_82480_) && Double.isFinite(manorDomainPacket.center.f_82481_)) {
            DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ManorDomainVisual.accept(manorDomainPacket));
        }
        supplier.get().setPacketHandled(true);
    }
}

