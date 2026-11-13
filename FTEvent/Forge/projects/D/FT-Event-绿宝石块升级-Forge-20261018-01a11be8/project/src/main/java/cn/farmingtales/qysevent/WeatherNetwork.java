package cn.farmingtales.qysevent;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import cn.farmingtales.qysevent.worldevent.DailyWorldEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class WeatherNetwork {
    private static final String PROTOCOL_VERSION = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(QysEventMod.MOD_ID, "weather"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        CHANNEL.registerMessage(
                0,
                WeatherSyncPacket.class,
                WeatherSyncPacket::encode,
                WeatherSyncPacket::decode,
                WeatherSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void sync(ServerPlayer player, WeatherType weather, OrdinaryWeatherType ordinaryWeather,
                           DailyWorldEvent dailyEvent) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new WeatherSyncPacket(weather, ordinaryWeather, dailyEvent));
    }

    private WeatherNetwork() {}
}
