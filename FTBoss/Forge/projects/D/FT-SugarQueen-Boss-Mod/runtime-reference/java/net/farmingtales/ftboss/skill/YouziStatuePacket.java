/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.skill;

import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.YouziStatueVisual;
import net.farmingtales.ftboss.youzi.YouziCombatEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public record YouziStatuePacket(int actorId, int kind, int duration, Vec3 origin, Vec3 target) {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "youzi_statue"), () -> "1", "1"::equals, "1"::equals);
    public static final int ECHO = 0;
    public static final int WARD = 1;
    public static final int BEAM = 2;
    public static final int STOP = 3;
    public static final int BLOCK = 4;

    public static void register() {
        CHANNEL.messageBuilder(YouziStatuePacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(YouziStatuePacket::encode).decoder(YouziStatuePacket::decode).consumerMainThread(YouziStatuePacket::handle).add();
    }

    /*
     * WARNING - void declaration
     */
    public static void send(ServerPlayer serverPlayer, int n, int n2, Vec3 vec3, Vec3 vec32) {
        void target;
        void origin;
        void duration;
        void kind;
        ServerPlayer player;
        YouziCombatEvents.statueFeedback(serverPlayer, n, vec3, vec32);
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), (Object)new YouziStatuePacket(player.m_19879_(), (int)kind, (int)duration, (Vec3)origin, (Vec3)target));
    }

    public static void encode(YouziStatuePacket packet, FriendlyByteBuf buffer) {
        buffer.m_130130_(packet.actorId);
        buffer.writeByte(packet.kind);
        buffer.m_130130_(packet.duration);
        buffer.writeDouble(packet.origin.f_82479_);
        buffer.writeDouble(packet.origin.f_82480_);
        buffer.writeDouble(packet.origin.f_82481_);
        buffer.writeDouble(packet.target.f_82479_);
        buffer.writeDouble(packet.target.f_82480_);
        buffer.writeDouble(packet.target.f_82481_);
    }

    public static YouziStatuePacket decode(FriendlyByteBuf buffer) {
        return new YouziStatuePacket(buffer.m_130242_(), buffer.readUnsignedByte(), buffer.m_130242_(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }

    public static void handle(YouziStatuePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            YouziStatueVisual.receive(packet);
        }
    }
}

