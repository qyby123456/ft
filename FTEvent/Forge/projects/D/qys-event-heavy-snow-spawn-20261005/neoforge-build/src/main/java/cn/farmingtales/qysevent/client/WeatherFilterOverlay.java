package cn.farmingtales.qysevent.client;

import cn.farmingtales.qysevent.QysEventMod;
import cn.farmingtales.qysevent.WeatherType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = QysEventMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class WeatherFilterOverlay {
    private static WeatherType paintedWeather = WeatherType.NONE;
    private static float strength;
    private static long lastFrameNanos = System.nanoTime();

    // Reused palettes avoid allocating two record objects per rendered frame.
    private static final FilterPalette BLOOD_MOON = new FilterPalette(0x24A81824, 0x30C0252F, true);
    private static final FilterPalette SOLAR_ECLIPSE = new FilterPalette(0x3A000000, 0x32000000, false);
    private static final FilterPalette GREEN_RAIN = new FilterPalette(0x1C38A850, 0x2A309048, true);
    private static final FilterPalette DENSE_FOG = new FilterPalette(0x46C3CCCC, 0x3A879797, false);
    private static final FilterPalette SANDSTORM = new FilterPalette(0x1EC99B63, 0x2CB87338, true);
    private static final FilterPalette HAILSTORM = new FilterPalette(0x18DDEFFF, 0x209CC9E8, true);
    private static final FilterPalette FROST_NIGHT = new FilterPalette(0x1498D8E8, 0x2070AFC8, false);
    private static final FilterPalette COMET_NIGHT = new FilterPalette(0x126D55B8, 0x1A356FC8, true);
    private static final FilterPalette HEATWAVE = new FilterPalette(0x12F06A20, 0x24E83B18, true);
    private static final FilterPalette AURORA = new FilterPalette(0x16245FC0, 0x2028C89A, true);
    private static final FilterPalette ACID_RAIN = new FilterPalette(0x163D9B36, 0x245C7D24, true);
    private static final FilterPalette ASH_FALL = new FilterPalette(0x1C514B49, 0x28635A54, false);
    private static final FilterPalette STAR_DEW = new FilterPalette(0x102E4C9C, 0x1C7058B0, true);
    private static final FilterPalette THUNDERSNOW = new FilterPalette(0x16A8C7DF, 0x247887A8, true);
    private static final FilterPalette MISTY_DRIZZLE = new FilterPalette(0x14AEBFC5, 0x20899FA5, false);
    private static final FilterPalette DRY_LIGHTNING = new FilterPalette(0x14D67520, 0x248C4218, true);
    private static final FilterPalette FIREFLY_NIGHT = new FilterPalette(0x10275C38, 0x1C8A7A28, true);
    private static final FilterPalette BEE_FLIGHT = new FilterPalette(0x10E3A91E, 0x1CCA7A18, true);

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        long now = System.nanoTime();
        float deltaSeconds = Math.min(0.1F, (now - lastFrameNanos) / 1_000_000_000.0F);
        lastFrameNanos = now;

        if (minecraft.level == null || minecraft.player == null) {
            paintedWeather = WeatherType.NONE;
            strength = 0.0F;
            return;
        }

        WeatherType target = ClientWeatherState.weather();
        FilterPalette targetPalette = palette(target);
        if (targetPalette != null) paintedWeather = target;

        float desired = targetPalette == null ? 0.0F : 1.0F;
        float speed = desired > strength ? 1.8F : 2.6F;
        strength = Mth.clamp(strength + Math.signum(desired - strength) * deltaSeconds * speed, 0.0F, 1.0F);
        if (strength <= 0.001F) {
            paintedWeather = WeatherType.NONE;
            return;
        }

        FilterPalette palette = palette(paintedWeather);
        if (palette == null) return;

        float pulse = palette.pulses
                ? 0.94F + 0.06F * (float) Math.sin(now / 1_000_000_000.0D * 1.5D)
                : 1.0F;
        float opacity = strength * pulse;
        int top = scaleAlpha(palette.topColor, opacity);
        int bottom = scaleAlpha(palette.bottomColor, opacity);
        event.getGuiGraphics().fillGradient(
                0, 0,
                event.getGuiGraphics().guiWidth(),
                event.getGuiGraphics().guiHeight(),
                top, bottom
        );
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientWeatherState.clear();
        paintedWeather = WeatherType.NONE;
        strength = 0.0F;
    }

    private static FilterPalette palette(WeatherType weather) {
        return switch (weather) {
            case BLOOD_MOON -> BLOOD_MOON;
            case SOLAR_ECLIPSE -> SOLAR_ECLIPSE;
            case GREEN_RAIN -> GREEN_RAIN;
            case DENSE_FOG -> DENSE_FOG;
            case SANDSTORM -> SANDSTORM;
            case HAILSTORM -> HAILSTORM;
            case FROST_NIGHT -> FROST_NIGHT;
            case COMET_NIGHT -> COMET_NIGHT;
            case HEATWAVE -> HEATWAVE;
            case AURORA -> AURORA;
            case ACID_RAIN -> ACID_RAIN;
            case ASH_FALL -> ASH_FALL;
            case STAR_DEW -> STAR_DEW;
            case THUNDERSNOW -> THUNDERSNOW;
            case MISTY_DRIZZLE -> MISTY_DRIZZLE;
            case DRY_LIGHTNING -> DRY_LIGHTNING;
            case FIREFLY_NIGHT -> FIREFLY_NIGHT;
            case BEE_FLIGHT -> BEE_FLIGHT;
            default -> null;
        };
    }

    private static int scaleAlpha(int color, float multiplier) {
        int alpha = color >>> 24;
        int scaled = Mth.clamp(Math.round(alpha * multiplier), 0, 255);
        return (scaled << 24) | (color & 0x00FFFFFF);
    }

    private record FilterPalette(int topColor, int bottomColor, boolean pulses) {}

    private WeatherFilterOverlay() {}
}





