/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
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

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.MaqueFeatherVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public record MaqueFeatherPacket(ResourceLocation dimension, int casterId, Vec3 position, boolean wind) {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "maque_feather"), () -> "1", (Predicate)NetworkRegistry.acceptMissingOr((String)"1"), (Predicate)NetworkRegistry.acceptMissingOr((String)"1"));
    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.messageBuilder(MaqueFeatherPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(MaqueFeatherPacket::encode).decoder(MaqueFeatherPacket::decode).consumerMainThread(MaqueFeatherPacket::handle).add();
    }

    public static void encode(MaqueFeatherPacket message, FriendlyByteBuf buffer) {
        buffer.m_130085_(message.dimension);
        buffer.m_130130_(message.casterId);
        buffer.writeDouble(message.position.f_82479_);
        buffer.writeDouble(message.position.f_82480_);
        buffer.writeDouble(message.position.f_82481_);
        buffer.writeBoolean(message.wind);
    }

    public static MaqueFeatherPacket decode(FriendlyByteBuf buffer) {
        return new MaqueFeatherPacket(buffer.m_130281_(), buffer.m_130242_(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readBoolean());
    }

    public static void handle(MaqueFeatherPacket message, Supplier<NetworkEvent.Context> context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MaqueFeatherVisual.receive(message);
        }
        context.get().setPacketHandled(true);
    }

    public static void send(ServerPlayer caster, boolean wind) {
        MaqueFeatherPacket message = new MaqueFeatherPacket(caster.m_9236_().m_46472_().m_135782_(), caster.m_19879_(), caster.m_20182_(), wind);
        for (ServerPlayer viewer : caster.m_284548_().m_6907_()) {
            if (!(viewer.m_20280_((Entity)caster) <= 2304.0) || !CHANNEL.isRemotePresent(viewer.f_8906_.f_9742_)) continue;
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), (Object)message);
        }
    }
}

