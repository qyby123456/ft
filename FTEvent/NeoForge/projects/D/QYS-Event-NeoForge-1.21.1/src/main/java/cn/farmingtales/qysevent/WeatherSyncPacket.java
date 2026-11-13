package cn.farmingtales.qysevent;

import cn.farmingtales.qysevent.client.ClientWeatherState;
import cn.farmingtales.qysevent.worldevent.DailyWorldEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WeatherSyncPacket(WeatherType weather, OrdinaryWeatherType ordinaryWeather, DailyWorldEvent dailyEvent)
        implements CustomPacketPayload {
    public static final Type<WeatherSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QysEventMod.MOD_ID, "weather"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WeatherSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(WeatherSyncPacket::write, WeatherSyncPacket::read);

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(weather.id());
        buffer.writeUtf(ordinaryWeather.id());
        buffer.writeUtf(dailyEvent == null ? "" : dailyEvent.id());
    }

    private static WeatherSyncPacket read(RegistryFriendlyByteBuf buffer) {
        return new WeatherSyncPacket(WeatherType.byId(buffer.readUtf(64)),
                OrdinaryWeatherType.byId(buffer.readUtf(64)), DailyWorldEvent.byId(buffer.readUtf(64)));
    }

    public static void handle(WeatherSyncPacket packet, IPayloadContext context) {
        ClientWeatherState.setWeather(packet.weather, packet.ordinaryWeather, packet.dailyEvent);
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

