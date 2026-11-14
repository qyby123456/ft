package net.farmingtales.ftboss.network;

import java.util.function.Supplier;
import net.farmingtales.ftboss.util.AppleBlastLightRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

/** 苹果自爆光效同步：2026-10-12，独立 S2C 通道，不修改既有法术协议或共享入口。 */
@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AppleBlastNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("ftboss", "apple_blast"), () -> PROTOCOL,
            PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered;

    private AppleBlastNetwork() {}

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(AppleBlastNetwork::register);
    }

    private static synchronized void register() {
        if (registered) return;
        registered = true;
        CHANNEL.messageBuilder(Burst.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Burst::encode).decoder(Burst::decode).consumerMainThread(Burst::handle).add();
    }

    public static void show(ServerLevel level, double x, double y, double z, double radius) {
        if (level == null || !AppleBlastLightRules.valid(x, y, z, radius)) return;
        Burst burst = new Burst(level.dimension().location(), x, y, z, radius);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(x, y, z) <= AppleBlastLightRules.VIEW_DISTANCE * AppleBlastLightRules.VIEW_DISTANCE) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), burst);
            }
        }
    }

    public record Burst(ResourceLocation dimension, double x, double y, double z, double radius) {
        public boolean valid() {
            return dimension != null && AppleBlastLightRules.valid(x, y, z, radius);
        }

        public static void encode(Burst burst, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(burst.dimension);
            buffer.writeDouble(burst.x).writeDouble(burst.y).writeDouble(burst.z).writeDouble(burst.radius);
        }

        public static Burst decode(FriendlyByteBuf buffer) {
            return new Burst(buffer.readResourceLocation(), buffer.readDouble(), buffer.readDouble(),
                    buffer.readDouble(), buffer.readDouble());
        }

        public static void handle(Burst burst, Supplier<NetworkEvent.Context> context) {
            if (burst.valid()) DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> net.farmingtales.ftboss.client.render.AppleBlastLightVisual.accept(burst));
            context.get().setPacketHandled(true);
        }
    }
}
