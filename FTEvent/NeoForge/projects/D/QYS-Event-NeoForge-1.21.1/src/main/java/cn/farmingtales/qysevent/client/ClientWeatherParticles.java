package cn.farmingtales.qysevent.client;

import cn.farmingtales.qysevent.QysEventMod;
import cn.farmingtales.qysevent.WeatherType;
import cn.farmingtales.qysevent.worldevent.DailyWorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Small, client-local accents for weather that is not represented well by
 * colored vanilla precipitation. Runs at 5 Hz and never sends particle packets.
 */
@EventBusSubscriber(modid = QysEventMod.MOD_ID, value = Dist.CLIENT)
public final class ClientWeatherParticles {
    private static final ParticleOptions SAND_DUST =
            new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
    private static final ParticleOptions ICE_CHIP =
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState());

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level == null || player == null || minecraft.isPaused() || (level.getGameTime() & 3L) != 0L) return;

        WeatherType weather = ClientWeatherState.weather();
        DailyWorldEvent daily = ClientWeatherState.dailyEvent();
        if (weather == WeatherType.NONE && daily == null || !level.canSeeSky(player.blockPosition().above())) return;
        RandomSource random = level.random;

        // Daily events that do not have their own precipitation use tiny local accents.
        // Heavy Snow uses brown-tinted vanilla rain, so it deliberately adds no snowflakes.
        if (daily != null) {
            switch (daily) {
                case ENDER_ECLIPSE -> ambient(level, player, ParticleTypes.PORTAL, 1, 9.0D, 4.0D, 0.025D);
                case FIREFLY_NIGHT -> {
                    if (weather != WeatherType.FIREFLY_NIGHT) ambient(level, player, ParticleTypes.GLOW, 1, 10.0D, 4.0D, 0.006D);
                }
                case STARFALL -> {
                    if (random.nextFloat() < .65F) falling(level, player, ParticleTypes.END_ROD, 1, 13.0D, 12.0D, 0.15D);
                }
                case BLOOD_MOON -> {
                    if (weather != WeatherType.BLOOD_MOON) falling(level, player, ParticleTypes.CRIMSON_SPORE, 1, 9.0D, 4.0D, 0.01D);
                }
                default -> { }
            }
        }

        // Snow-family weather intentionally uses Minecraft's original snowflake particle.
        switch (weather) {
            case CHRISTMAS_EVE -> falling(level, player, ParticleTypes.SNOWFLAKE, 2, 10.0D, 5.0D, 0.025D);
            case BLIZZARD -> falling(level, player, ParticleTypes.SNOWFLAKE, 4, 12.0D, 7.0D, 0.055D);
            case SILENT_SNOW -> falling(level, player, ParticleTypes.SNOWFLAKE, 2, 11.0D, 6.0D, 0.018D);
            case THUNDERSNOW -> {
                falling(level, player, ParticleTypes.SNOWFLAKE, 3, 12.0D, 7.0D, 0.045D);
                if (random.nextFloat() < 0.16F) ambient(level, player, ParticleTypes.ELECTRIC_SPARK, 1, 8.0D, 4.0D, 0.035D);
            }
            case HAILSTORM -> {
                if (random.nextFloat() < 0.45F) hailImpact(level, player, random);
            }

            // Dry/celestial weather keeps only a few of its former signature particles.
            case HARVEST_MOON -> ambient(level, player, ParticleTypes.GLOW, 1, 8.0D, 4.0D, 0.006D);
            case BLOOD_MOON -> falling(level, player, ParticleTypes.CRIMSON_SPORE, 1, 9.0D, 4.0D, 0.012D);
            case SOLAR_ECLIPSE -> falling(level, player, ParticleTypes.ASH, 1, 10.0D, 5.0D, 0.012D);
            case METEOR_SHOWER -> {
                if (random.nextFloat() < 0.38F) falling(level, player, ParticleTypes.END_ROD, 1, 14.0D, 14.0D, 0.22D);
            }
            case SANDSTORM -> falling(level, player, SAND_DUST, 2, 12.0D, 5.0D, 0.045D);
            case AURORA -> {
                ambient(level, player, ParticleTypes.WAX_ON, 1, 11.0D, 6.0D, 0.008D);
                if (random.nextBoolean()) ambient(level, player, ParticleTypes.ENCHANT, 1, 9.0D, 5.0D, 0.012D);
            }
            case HEATWAVE -> {
                ambient(level, player, ParticleTypes.SMOKE, 1, 8.0D, 2.5D, 0.012D);
                if (random.nextFloat() < 0.22F) ambient(level, player, ParticleTypes.FLAME, 1, 7.0D, 2.0D, 0.006D);
            }
            case POLLEN_STORM -> falling(level, player, ParticleTypes.SPORE_BLOSSOM_AIR, 2, 11.0D, 5.0D, 0.018D);
            case SPRING_BLOOM -> {
                ambient(level, player, ParticleTypes.SPORE_BLOSSOM_AIR, 1, 10.0D, 5.0D, 0.012D);
                ambient(level, player, ParticleTypes.HAPPY_VILLAGER, 1, 8.0D, 4.0D, 0.01D);
            }
            case GOLDEN_WIND -> falling(level, player, ParticleTypes.WAX_ON, 1, 10.0D, 5.0D, 0.02D);
            case FROST_NIGHT -> falling(level, player, ParticleTypes.SNOWFLAKE, 1, 9.0D, 5.0D, 0.015D);
            case COMET_NIGHT -> {
                if (random.nextBoolean()) falling(level, player, ParticleTypes.END_ROD, 1, 13.0D, 12.0D, 0.16D);
                ambient(level, player, ParticleTypes.ENCHANT, 1, 9.0D, 5.0D, 0.012D);
            }
            case ASH_FALL -> falling(level, player, ParticleTypes.ASH, 3, 12.0D, 7.0D, 0.022D);
            case STAR_DEW -> ambient(level, player, ParticleTypes.GLOW, 2, 10.0D, 5.0D, 0.008D);
            case DRY_LIGHTNING -> {
                if (random.nextFloat() < 0.34F) ambient(level, player, ParticleTypes.ELECTRIC_SPARK, 1, 9.0D, 5.0D, 0.04D);
            }
            case FIREFLY_NIGHT -> ambient(level, player, ParticleTypes.GLOW, 2, 10.0D, 4.0D, 0.006D);
            case BEE_FLIGHT -> {
                ambient(level, player, ParticleTypes.WAX_ON, 1, 9.0D, 4.0D, 0.018D);
                ambient(level, player, ParticleTypes.HAPPY_VILLAGER, 1, 8.0D, 3.5D, 0.012D);
            }

            // Rain-family weather is represented by its colored vanilla precipitation.
            // Dense fog uses its deeper screen filter; slime rain uses actual falling slimes.
            default -> { }
        }
    }

    private static void falling(ClientLevel level, LocalPlayer player, ParticleOptions particle,
                                int count, double radius, double height, double fallSpeed) {
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double x = player.getX() + (random.nextDouble() * 2.0D - 1.0D) * radius;
            double y = player.getY() + 3.5D + random.nextDouble() * height;
            double z = player.getZ() + (random.nextDouble() * 2.0D - 1.0D) * radius;
            level.addParticle(particle, x, y, z,
                    (random.nextDouble() - 0.5D) * 0.018D, -fallSpeed, (random.nextDouble() - 0.5D) * 0.018D);
        }
    }

    private static void ambient(ClientLevel level, LocalPlayer player, ParticleOptions particle,
                                int count, double radius, double height, double speed) {
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double x = player.getX() + (random.nextDouble() * 2.0D - 1.0D) * radius;
            double y = player.getY() + 0.5D + random.nextDouble() * height;
            double z = player.getZ() + (random.nextDouble() * 2.0D - 1.0D) * radius;
            level.addParticle(particle, x, y, z,
                    (random.nextDouble() - 0.5D) * speed, random.nextDouble() * speed,
                    (random.nextDouble() - 0.5D) * speed);
        }
    }

    private static void hailImpact(ClientLevel level, LocalPlayer player, RandomSource random) {
        int x = player.blockPosition().getX() + random.nextInt(17) - 8;
        int z = player.blockPosition().getZ() + random.nextInt(17) - 8;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos impact = new BlockPos(x, y, z);
        if (!level.hasChunkAt(impact) || !level.canSeeSky(impact)) return;

        for (int i = 0; i < 2; i++) {
            level.addParticle(ICE_CHIP, x + 0.5D, y + 0.12D, z + 0.5D,
                    (random.nextDouble() - 0.5D) * 0.16D,
                    0.08D + random.nextDouble() * 0.10D,
                    (random.nextDouble() - 0.5D) * 0.16D);
        }
        if (random.nextFloat() < 0.10F) {
            level.playLocalSound(x + 0.5D, y + 0.2D, z + 0.5D,
                    SoundEvents.GLASS_BREAK, SoundSource.WEATHER,
                    0.12F, 1.35F + random.nextFloat() * 0.35F, false);
        }
    }

    private ClientWeatherParticles() {}
}




