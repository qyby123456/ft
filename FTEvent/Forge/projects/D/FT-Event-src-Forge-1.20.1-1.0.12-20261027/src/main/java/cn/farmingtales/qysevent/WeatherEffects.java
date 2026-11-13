package cn.farmingtales.qysevent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

/** Runtime-only weather effects, separated from scheduling and persisted state. */
final class WeatherEffects {
    static void applyPlayerAtmosphere(ServerLevel level, WeatherType type) {
        if (type == WeatherType.NONE) return;
        long time = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            boolean outdoors = level.canSeeSky(player.blockPosition().above());
            if (!outdoors && type != WeatherType.BLOOD_MOON && type != WeatherType.SOLAR_ECLIPSE && type != WeatherType.DENSE_FOG) continue;

            switch (type) {
                case HARVEST_MOON -> player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false));
                case CHRISTMAS_EVE -> {
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 1, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false));
                }
                case BLOOD_MOON -> {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false));
                    if (time % 40L == 0L) buffNearbyMonsters(level, player);
                }
                case SOLAR_ECLIPSE -> player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, true, false));
                case METEOR_SHOWER -> player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false));
                case SLIME_RAIN -> player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 0, true, false));
                case BLIZZARD -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false));
                    player.setTicksFrozen(Math.min(player.getTicksRequiredToFreeze() + 20, player.getTicksFrozen() + 8));
                }
                case HURRICANE -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, true, false));
                    pushNearbyEntities(level, player);
                }
                case SANDSTORM, DENSE_FOG ->
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, true, false));
                case GREEN_RAIN, MISTY_DRIZZLE ->
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
                case AURORA -> {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, true, false));
                }
                case HEATWAVE, DRY_LIGHTNING -> {
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false));
                }
                case RAINBOW_RAIN -> {
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 1, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false));
                }
                case POLLEN_STORM -> player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, true, false));
                case SPRING_BLOOM -> {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false));
                }
                case HAILSTORM -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 0, true, false));
                    player.setTicksFrozen(Math.min(player.getTicksRequiredToFreeze(), player.getTicksFrozen() + 4));
                }
                case MONSOON -> pushNearbyEntities(level, player, 0.06D);
                case GOLDEN_WIND, BEE_FLIGHT -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false));
                }
                case FROST_NIGHT -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, true, false));
                    player.setTicksFrozen(Math.min(player.getTicksRequiredToFreeze(), player.getTicksFrozen() + 5));
                }
                case SILENT_SNOW -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, true, false));
                }
                case COMET_NIGHT -> {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 1, true, false));
                }
                case ACID_RAIN -> {
                    if (player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                        player.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, true, false));
                    }
                }
                case ASH_FALL -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false));
                }
                case STAR_DEW -> {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 1, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
                }
                case THUNDERSNOW -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false));
                    player.setTicksFrozen(Math.min(player.getTicksRequiredToFreeze() + 20, player.getTicksFrozen() + 7));
                }
                case FIREFLY_NIGHT -> {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false));
                }
                default -> { }
            }
        }
    }

    private static void buffNearbyMonsters(ServerLevel level, ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(28.0D);
        for (Monster monster : level.getEntitiesOfClass(Monster.class, area)) {
            monster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0, true, false));
            monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0, true, false));
        }
    }

    private static void pushNearbyEntities(ServerLevel level, ServerPlayer center) {
        pushNearbyEntities(level, center, 0.16D);
    }

    private static void pushNearbyEntities(ServerLevel level, ServerPlayer center, double strength) {
        double angle = level.getGameTime() * 0.045D;
        double dx = Math.cos(angle) * strength;
        double dz = Math.sin(angle) * strength;
        AABB area = center.getBoundingBox().inflate(18.0D, 8.0D, 18.0D);
        for (Entity entity : level.getEntities(center, area, Entity::isAlive)) {
            if (!level.canSeeSky(entity.blockPosition().above())) continue;
            entity.push(dx, entity.onGround() ? 0.03D : 0.01D, dz);
            entity.hurtMarked = true;
        }
    }

    static void pulseBeneficialTerrain(ServerLevel level, WeatherType type) {
        double growChance = switch (type) {
            case GREEN_RAIN -> .22D;
            case RAINBOW_RAIN -> .16D;
            case POLLEN_STORM -> .13D;
            case SPRING_BLOOM -> .18D;
            case GOLDEN_WIND -> .10D;
            case MONSOON -> .04D;
            case STAR_DEW -> .17D;
            case MISTY_DRIZZLE -> .10D;
            case FIREFLY_NIGHT -> .06D;
            case BEE_FLIGHT -> .21D;
            default -> 0.0D;
        };
        if (growChance <= 0.0D) return;
        int samples = Math.max(6, WeatherConfig.TERRAIN_SAMPLES_PER_PLAYER.get());
        for (ServerPlayer player : level.players()) {
            for (int i = 0; i < samples; i++) {
                int x = Mth.floor(player.getX()) + level.random.nextInt(41) - 20;
                int z = Mth.floor(player.getZ()) + level.random.nextInt(41) - 20;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                BlockPos pos = new BlockPos(x, y, z);
                if (!level.hasChunkAt(pos)) continue;
                BlockState state = level.getBlockState(pos);
                if (state.is(Blocks.FARMLAND) && state.hasProperty(FarmBlock.MOISTURE) && state.getValue(FarmBlock.MOISTURE) < 7) {
                    level.setBlockAndUpdate(pos, state.setValue(FarmBlock.MOISTURE, 7));
                } else if (CropUtil.isCrop(state) && !CropUtil.isMature(state) && level.random.nextDouble() < growChance) {
                    growOnce(level, pos, state);
                }
            }
        }
    }

    static void growOnce(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BonemealableBlock growable &&
                growable.isValidBonemealTarget(level, pos, state, false) &&
                growable.isBonemealSuccess(level, level.random, pos, state)) {
            growable.performBonemeal(level, level.random, pos, state);
        }
    }

    static void pulseTerrain(ServerLevel level, WeatherType type) {
        if (!WeatherConfig.DESTRUCTIVE_WEATHER.get()) return;
        if (WeatherConfig.REQUIRE_MOB_GRIEFING.get() && !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;
        int samples = WeatherConfig.TERRAIN_SAMPLES_PER_PLAYER.get();
        if (samples <= 0) return;

        for (ServerPlayer player : level.players()) {
            RandomSource random = level.random;
            for (int i = 0; i < samples; i++) {
                int x = Mth.floor(player.getX()) + random.nextInt(49) - 24;
                int z = Mth.floor(player.getZ()) + random.nextInt(49) - 24;
                int y = (i & 1) == 0
                        ? level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1
                        : Mth.floor(player.getY()) + random.nextInt(9) - 4;
                BlockPos pos = new BlockPos(x, y, z);
                if (!level.hasChunkAt(pos) || !level.isInWorldBounds(pos)) continue;
                damageOneBlock(level, pos, type, random);
            }
        }
    }

    private static void damageOneBlock(ServerLevel level, BlockPos pos, WeatherType type, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        double glassChance = switch (type) {
            case HURRICANE -> .24D;
            case BLIZZARD, SANDSTORM, THUNDERSTORM -> .06D;
            case REDSTONE_STORM -> .04D;
            case HAILSTORM -> .10D;
            case THUNDERSNOW -> .08D;
            case DRY_LIGHTNING -> .04D;
            default -> 0.0D;
        };
        double cropChance = switch (type) {
            case HURRICANE -> .38D;
            case BLIZZARD -> .30D;
            case SANDSTORM -> .34D;
            case THUNDERSTORM -> .18D;
            case REDSTONE_STORM -> .14D;
            case HEATWAVE -> .30D;
            case BLOOD_MOON, SOLAR_ECLIPSE, SLIME_RAIN -> .08D;
            case HAILSTORM -> .26D;
            case FROST_NIGHT -> .16D;
            case MONSOON -> .04D;
            case ACID_RAIN -> .30D;
            case ASH_FALL -> .24D;
            case THUNDERSNOW -> .25D;
            case DRY_LIGHTNING -> .32D;
            default -> 0.0D;
        };
        double landChance = switch (type) {
            case HURRICANE -> .16D;
            case BLIZZARD -> .08D;
            case SANDSTORM -> .22D;
            case THUNDERSTORM -> .06D;
            case REDSTONE_STORM -> .05D;
            case HEATWAVE -> .30D;
            case HAILSTORM -> .04D;
            case ACID_RAIN -> .05D;
            case ASH_FALL -> .14D;
            case THUNDERSNOW -> .06D;
            case DRY_LIGHTNING -> .30D;
            default -> 0.0D;
        };

        if (glassChance > 0.0D && (state.is(Tags.Blocks.GLASS) || state.is(Tags.Blocks.GLASS_PANES)) && random.nextDouble() < glassChance) {
            level.levelEvent(2001, pos, Block.getId(state));
            level.destroyBlock(pos, false);
            return;
        }
        if (cropChance > 0.0D && CropUtil.isCrop(state) && random.nextDouble() < cropChance) {
            level.levelEvent(2001, pos, Block.getId(state));
            level.destroyBlock(pos, false);
            return;
        }
        if (landChance <= 0.0D || random.nextDouble() >= landChance) return;
        if (state.is(Blocks.FARMLAND)) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        } else if ((type == WeatherType.HURRICANE || type == WeatherType.SANDSTORM || type == WeatherType.HEATWAVE ||
                type == WeatherType.ASH_FALL || type == WeatherType.DRY_LIGHTNING) &&
                (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT))) {
            level.setBlockAndUpdate(pos, Blocks.COARSE_DIRT.defaultBlockState());
        }
    }

    static void pulseRareHazards(ServerLevel level, WeatherType type) {
        for (ServerPlayer player : level.players()) {
            if (!level.canSeeSky(player.blockPosition().above())) continue;
            RandomSource random = level.random;
            double lightningChance = switch (type) {
                case THUNDERSTORM -> 0.055D;
                case REDSTONE_STORM -> 0.065D;
                case THUNDERSNOW -> 0.040D;
                case DRY_LIGHTNING -> 0.080D;
                default -> 0.0D;
            };
            // At most one nearby creeper is checked for charging every five-second hazard pulse.
            if (type == WeatherType.REDSTONE_STORM && random.nextDouble() < 0.20D) {
                chargeOneNearbyCreeper(level, player);
            }
            if (lightningChance > 0.0D && random.nextDouble() < lightningChance) {
                int x = player.blockPosition().getX() + random.nextInt(33) - 16;
                int z = player.blockPosition().getZ() + random.nextInt(33) - 16;
                BlockPos strike = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(strike));
                    if (!WeatherConfig.DESTRUCTIVE_WEATHER.get()) bolt.setVisualOnly(true);
                    level.addFreshEntity(bolt);
                }
            }
            if (type == WeatherType.METEOR_SHOWER && random.nextDouble() < 0.035D) {
                int x = player.blockPosition().getX() + random.nextInt(41) - 20;
                int z = player.blockPosition().getZ() + random.nextInt(41) - 20;
                BlockPos impact = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
                level.sendParticles(ParticleTypes.EXPLOSION, impact.getX() + .5D, impact.getY() + 1.0D, impact.getZ() + .5D,
                        4, 1.0D, 1.0D, 1.0D, 0.02D);
                Level.ExplosionInteraction interaction = WeatherConfig.DESTRUCTIVE_WEATHER.get() ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
                level.explode(null, impact.getX() + .5D, impact.getY() + 1.0D, impact.getZ() + .5D, 1.35F, interaction);
                if (random.nextDouble() < WeatherConfig.METEOR_MINERAL_CHANCE.get()) {
                    spawnFallingMeteorMineral(level, player, x, z, random);
                }
            }
        }
    }

    private static void chargeOneNearbyCreeper(ServerLevel level, ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(20.0D, 10.0D, 20.0D);
        for (Creeper creeper : level.getEntitiesOfClass(Creeper.class, area, candidate ->
                candidate.isAlive() && !candidate.isPowered() && level.canSeeSky(candidate.blockPosition().above()))) {
            LightningBolt source = EntityType.LIGHTNING_BOLT.create(level);
            if (source != null) {
                source.setVisualOnly(true);
                source.moveTo(creeper.position());
                creeper.thunderHit(level, source);
            }
            return;
        }
    }

    private static void spawnFallingMeteorMineral(ServerLevel level, ServerPlayer player, int x, int z, RandomSource random) {
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int startY = Math.min(level.getMaxBuildHeight() - 2, Math.max(surfaceY + 24, Mth.floor(player.getY()) + 20));
        BlockPos start = new BlockPos(x, startY, z);
        if (!level.getBlockState(start).isAir()) return;

        BlockState mineral = chooseMeteorMineral(random);
        FallingBlockEntity falling = FallingBlockEntity.fall(level, start, mineral);
        falling.dropItem = true;
        falling.setHurtsEntities(1.0F, 16);
        level.sendParticles(ParticleTypes.END_ROD, x + 0.5D, startY + 0.5D, z + 0.5D,
                24, 1.2D, 2.5D, 1.2D, 0.08D);
        player.sendSystemMessage(Component.literal("§b[流星雨] §f一颗含有§l" + mineral.getBlock().getName().getString() + "§r§f的稀有陨石正在坠落！"));
    }

    private static BlockState chooseMeteorMineral(RandomSource random) {
        int roll = random.nextInt(1000);
        if (roll < 270) return Blocks.COPPER_ORE.defaultBlockState();
        if (roll < 530) return Blocks.IRON_ORE.defaultBlockState();
        if (roll < 770) return Blocks.COAL_ORE.defaultBlockState();
        if (roll < 870) return Blocks.GOLD_ORE.defaultBlockState();
        if (roll < 940) return Blocks.REDSTONE_ORE.defaultBlockState();
        if (roll < 980) return Blocks.LAPIS_ORE.defaultBlockState();
        if (roll < 995) return Blocks.DIAMOND_ORE.defaultBlockState();
        return Blocks.EMERALD_ORE.defaultBlockState();
    }

    static void spawnFallingSlimes(ServerLevel level) {
        int cap = WeatherConfig.SLIME_CAP_PER_PLAYER.get();
        if (cap <= 0) return;
        for (ServerPlayer player : level.players()) {
            if (!level.canSeeSky(player.blockPosition().above())) continue;
            AABB area = player.getBoundingBox().inflate(28.0D, 24.0D, 28.0D);
            if (level.getEntitiesOfClass(Slime.class, area).size() >= cap || level.random.nextDouble() > .68D) continue;
            int amount = 1 + level.random.nextInt(2);
            for (int i = 0; i < amount; i++) {
                Slime slime = EntityType.SLIME.create(level);
                if (slime == null) continue;
                double x = player.getX() + level.random.nextInt(29) - 14;
                double z = player.getZ() + level.random.nextInt(29) - 14;
                double y = Math.min(level.getMaxBuildHeight() - 3, player.getY() + 18 + level.random.nextInt(8));
                slime.setSize(1 + level.random.nextInt(3), true);
                slime.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
                slime.finalizeSpawn(level, level.getCurrentDifficultyAt(slime.blockPosition()), MobSpawnType.EVENT, null, null);
                level.addFreshEntity(slime);
            }
        }
    }

    private WeatherEffects() {}
}
