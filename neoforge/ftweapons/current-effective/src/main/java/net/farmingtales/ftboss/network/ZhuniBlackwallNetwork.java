package net.farmingtales.ftboss.network;

import net.farmingtales.ftboss.client.render.ZhuniBlackwallVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = "ftboss", bus = EventBusSubscriber.Bus.MOD)
public final class ZhuniBlackwallNetwork {
    private static boolean registered;
    private ZhuniBlackwallNetwork() {
    }
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        if (!registered) {
            registered = true;
            event.registrar("1").playToClient(Breach.TYPE,Breach.STREAM_CODEC,Breach::handle);
        }
    }
    public static void show(ServerPlayer player, int seed, int count) {
        PacketDistributor.sendToPlayer(player,new Breach(player.level().dimension().location(),seed,count));
    }
    public static record Breach(ResourceLocation dimension, int seed, int count) implements CustomPacketPayload {
        public static final Type<Breach> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss","zhuni_blackwall"));
        public static final StreamCodec<FriendlyByteBuf,Breach> STREAM_CODEC = StreamCodec.of((buffer,packet) -> encode(packet,buffer),Breach::decode);
        public Type<Breach> type() { return TYPE; }
        public static void encode(ZhuniBlackwallNetwork.Breach var0, FriendlyByteBuf var1) {
            var1.writeResourceLocation(var0.dimension);
            var1.writeInt(var0.seed);
            var1.writeVarInt(var0.count);
        }

        public static ZhuniBlackwallNetwork.Breach decode(FriendlyByteBuf var0) {
            return new ZhuniBlackwallNetwork.Breach(var0.readResourceLocation(), var0.readInt(), var0.readVarInt());
        }

        public static void handle(Breach packet, IPayloadContext context) {
            if (packet.count > 0 && packet.count <= 3) context.enqueueWork(() -> ZhuniBlackwallVisual.accept(packet));
        }
    }
}
