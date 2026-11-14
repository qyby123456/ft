/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.maque.support;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.maque.MaqueSupportRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public record MaqueSupportPacket(ResourceLocation dimension, UUID source, int sourceId, UUID target, int targetId, Vec3 start, Vec3 end, int kind, long sequence) {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "maque_support_v1"), () -> "1", (Predicate)NetworkRegistry.acceptMissingOr((String)"1"), (Predicate)NetworkRegistry.acceptMissingOr((String)"1"));
    private static long nextSequence;
    private static boolean registered;

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        fMLCommonSetupEvent.enqueueWork(MaqueSupportPacket::register);
    }

    public static void register() {
        if (registered) {
            return;
        }
        CHANNEL.registerMessage(0, MaqueSupportPacket.class, MaqueSupportPacket::encode, MaqueSupportPacket::decode, MaqueSupportPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registered = true;
    }

    public boolean valid() {
        return this.dimension != null && this.source != null && this.target != null && this.sourceId >= 0 && this.targetId >= 0 && this.kind >= 0 && this.kind < 12 && this.kind != 10 && MaqueSupportPacket.finite(this.start) && MaqueSupportPacket.finite(this.end) && this.start.m_82557_(this.end) <= 9216.0;
    }

    private static boolean finite(Vec3 vec3) {
        return vec3 != null && Double.isFinite(vec3.f_82479_) && Double.isFinite(vec3.f_82480_) && Double.isFinite(vec3.f_82481_);
    }

    public static void encode(MaqueSupportPacket maqueSupportPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.m_130085_(maqueSupportPacket.dimension);
        friendlyByteBuf.m_130077_(maqueSupportPacket.source);
        friendlyByteBuf.m_130130_(maqueSupportPacket.sourceId);
        friendlyByteBuf.m_130077_(maqueSupportPacket.target);
        friendlyByteBuf.m_130130_(maqueSupportPacket.targetId);
        for (Vec3 vec3 : new Vec3[]{maqueSupportPacket.start, maqueSupportPacket.end}) {
            friendlyByteBuf.writeDouble(vec3.f_82479_);
            friendlyByteBuf.writeDouble(vec3.f_82480_);
            friendlyByteBuf.writeDouble(vec3.f_82481_);
        }
        friendlyByteBuf.writeByte(maqueSupportPacket.kind);
        friendlyByteBuf.writeLong(maqueSupportPacket.sequence);
    }

    public static MaqueSupportPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new MaqueSupportPacket(friendlyByteBuf.m_130281_(), friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130242_(), new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()), new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()), friendlyByteBuf.readUnsignedByte(), friendlyByteBuf.readLong());
    }

    public static void handle(MaqueSupportPacket maqueSupportPacket, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (maqueSupportPacket.valid()) {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> MaqueSupportRenderer.accept(maqueSupportPacket)));
        }
        context.setPacketHandled(true);
    }

    public static void send(Entity entity, Entity entity2, int n) {
        ServerLevel serverLevel;
        Object object;
        block6: {
            block5: {
                object = entity2.m_9236_();
                if (!(object instanceof ServerLevel)) break block5;
                serverLevel = (ServerLevel)object;
                if (entity.m_9236_() == serverLevel && registered) break block6;
            }
            return;
        }
        object = new MaqueSupportPacket(serverLevel.m_46472_().m_135782_(), entity.m_20148_(), entity.m_19879_(), entity2.m_20148_(), entity2.m_19879_(), entity.m_20182_(), entity2.m_20182_(), n, ++nextSequence);
        if (!((MaqueSupportPacket)object).valid()) {
            return;
        }
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (!(Math.min(serverPlayer.m_20280_(entity), serverPlayer.m_20280_(entity2)) <= 9216.0) || !CHANNEL.isRemotePresent(serverPlayer.f_8906_.f_9742_)) continue;
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), object);
        }
    }
}

