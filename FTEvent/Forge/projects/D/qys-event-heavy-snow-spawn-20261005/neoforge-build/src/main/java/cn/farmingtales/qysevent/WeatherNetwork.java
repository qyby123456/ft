package cn.farmingtales.qysevent;

import cn.farmingtales.qysevent.worldevent.DailyWorldEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class WeatherNetwork {
    private static final String PROTOCOL_VERSION = "2";

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION).playToClient(WeatherSyncPacket.TYPE,
                WeatherSyncPacket.STREAM_CODEC, WeatherSyncPacket::handle);
    }

    public static void sync(ServerPlayer player, WeatherType weather, OrdinaryWeatherType ordinaryWeather,
                            DailyWorldEvent dailyEvent) {
        PacketDistributor.sendToPlayer(player, new WeatherSyncPacket(weather, ordinaryWeather, dailyEvent));
    }

    private WeatherNetwork() {}
}

