package cn.farmingtales.qysevent;

import cn.farmingtales.qysevent.client.ClientWeatherState;
import cn.farmingtales.qysevent.worldevent.DailyWorldEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record WeatherSyncPacket(WeatherType weather, OrdinaryWeatherType ordinaryWeather, DailyWorldEvent dailyEvent) {
    public static void encode(WeatherSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.weather.id());
        buffer.writeUtf(packet.ordinaryWeather.id());
        buffer.writeUtf(packet.dailyEvent == null ? "" : packet.dailyEvent.id());
    }

    public static WeatherSyncPacket decode(FriendlyByteBuf buffer) {
        return new WeatherSyncPacket(
                WeatherType.byId(buffer.readUtf(64)),
                OrdinaryWeatherType.byId(buffer.readUtf(64)),
                DailyWorldEvent.byId(buffer.readUtf(64))
        );
    }

    public static void handle(WeatherSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientWeatherState.setWeather(packet.weather, packet.ordinaryWeather, packet.dailyEvent)
        ));
        context.setPacketHandled(true);
    }
}
