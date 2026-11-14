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
import net.farmingtales.ftboss.client.render.ManorAssaultVisual;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public record ManorAssaultPacket(ResourceLocation dimension, UUID owner, int ownerId, Vec3 center, int variant, boolean release) {
    public static final int PACKET_ID = 10;

    public static void register() {
        FTNetwork.CHANNEL.messageBuilder(ManorAssaultPacket.class, 10, NetworkDirection.PLAY_TO_CLIENT).encoder(ManorAssaultPacket::encode).decoder(ManorAssaultPacket::decode).consumerMainThread(ManorAssaultPacket::handle).add();
    }

    public static void encode(ManorAssaultPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130085_(packet.dimension);
        buffer.m_130077_(packet.owner);
        buffer.m_130130_(packet.ownerId);
        buffer.writeDouble(packet.center.f_82479_).writeDouble(packet.center.f_82480_).writeDouble(packet.center.f_82481_);
        buffer.m_130130_(packet.variant);
        buffer.writeBoolean(packet.release);
    }

    public static ManorAssaultPacket decode(FriendlyByteBuf buffer) {
        return new ManorAssaultPacket(buffer.m_130281_(), buffer.m_130259_(), buffer.m_130242_(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.m_130242_(), buffer.readBoolean());
    }

    public static void handle(ManorAssaultPacket packet, Supplier<NetworkEvent.Context> context) {
        if (ManorAssaultGeometrySafe.supports(packet.variant) && Double.isFinite(packet.center.f_82479_) && Double.isFinite(packet.center.f_82480_) && Double.isFinite(packet.center.f_82481_)) {
            DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ManorAssaultVisual.accept(packet));
        }
        context.get().setPacketHandled(true);
    }

    private static final class ManorAssaultGeometrySafe {
        private ManorAssaultGeometrySafe() {
        }

        static boolean supports(int variant) {
            return variant >= 0 && variant <= 11 && variant != 9;
        }
    }
}

