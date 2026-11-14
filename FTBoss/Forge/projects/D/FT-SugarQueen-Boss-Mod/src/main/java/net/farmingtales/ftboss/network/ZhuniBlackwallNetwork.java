package net.farmingtales.ftboss.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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

@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ZhuniBlackwallNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("ftboss", "zhuni_blackwall"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered;
    private ZhuniBlackwallNetwork() {}

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) { event.enqueueWork(ZhuniBlackwallNetwork::register); }

    private static synchronized void register() {
        if (registered) return;
        registered = true;
        CHANNEL.messageBuilder(Breach.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Breach::encode).decoder(Breach::decode).consumerMainThread(Breach::handle).add();
    }

    public static void show(ServerPlayer player, int seed, int count) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Breach(player.level().dimension().location(), seed, count));
    }

    public record Breach(ResourceLocation dimension, int seed, int count) {
        public static void encode(Breach packet, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(packet.dimension);
            buffer.writeInt(packet.seed);
            buffer.writeVarInt(packet.count);
        }
        public static Breach decode(FriendlyByteBuf buffer) {
            return new Breach(buffer.readResourceLocation(), buffer.readInt(), buffer.readVarInt());
        }
        public static void handle(Breach packet, Supplier<NetworkEvent.Context> context) {
            if (packet.count > 0 && packet.count <= 3) DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> net.farmingtales.ftboss.client.render.ZhuniBlackwallVisual.accept(packet));
            context.get().setPacketHandled(true);
        }
    }
}
