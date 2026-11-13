package cn.farmingtales.qysevent.worldevent;

import cn.farmingtales.qysevent.QysEventMod;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Gameplay hooks for all daily events, with no compile-time Boss Mod dependency. */
public final class DailyWorldEventHooks {
    private static final String DAILY_SPAWN = "qys_event_daily_spawn";
    private static final String WILD_SPAWN = "qys_event_wild_spawn";
    private static final String REPLACEMENT = "qys_event_replacement";
    private static final ResourceLocation POOP_CONFUSION = ResourceLocation.fromNamespaceAndPath("ftboss", "poop_confusion");
    private static final String LEGACY_REPLACEMENT = "ftboss_daily_event_replacement";
    private static final String HARVEST_ADJUSTED = "qys_event_harvest_adjusted";
    private static final String FAR_SINCE = "qys_event_far_since";
    private static final long FAR_GRACE_TICKS = 600L;
    private static final long FAR_HARD_LIMIT_TICKS = 1200L;
    private static final int EVENT_PULSE_INTERVAL_TICKS = 600;
    private static final int EVENT_MOB_CAP_PER_PLAYER = 6;
    private static final double EVENT_MOB_CAP_RADIUS = 48.0D;
    private static final double PULSE_MIN_RADIUS = 24.0D;
    private static final double PULSE_MAX_RADIUS = 40.0D;
    private static final double PREVIEW_MIN_RADIUS = 12.0D;
    private static final double PREVIEW_MAX_RADIUS = 20.0D;
    private static final int[][] REPLACEMENT_OFFSETS = {
            {0, 0, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
            {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1}, {0, 1, 0}
    };
    private static final Map<HarvestKey, HarvestContext> HARVESTS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Set<UUID>> REPLACEMENTS = new HashMap<>();

    private DailyWorldEventHooks() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        int tick = server.getTickCount();
        if (tick % 20 != 0) return;
        DailyWorldEventManager.tick(server);
        cleanupHarvests(server);
        if (tick % 100 == 0) despawnFarReplacements(server);
        DailyWorldEvent active = DailyWorldEventManager.active(server);
        int pulseInterval = active == DailyWorldEvent.HEAVY_SNOW
                ? DailyWorldEventRules.HEAVY_SNOW_PULSE_INTERVAL_TICKS : EVENT_PULSE_INTERVAL_TICKS;
        if (active != null && tick % pulseInterval == 0) {
            pulseEventCreatures(server, active);
        }
        if (active == DailyWorldEvent.NO_SUCH_CREATURE && tick % 80 == 0) playCreeperWhispers(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (active == DailyWorldEvent.HEAVY_SNOW) applyHeavySnowRainEffects(player);
            if (active == DailyWorldEvent.GRAVITY_GLITCH && tick % 100 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 140, 1, true, false, true));
            } else if (active == DailyWorldEvent.LUCKY_DAY && tick % 200 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 240, 0, true, false, true));
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        HARVESTS.clear();
        REPLACEMENTS.clear();
        DailyWorldEventManager.reset(event.getServer());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) DailyWorldEventManager.onLogin(player);
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void markDailySpawns(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();
        MobSpawnType spawnType = event.getSpawnType();
        if (event.isSpawnCancelled() || !isDailySpawnType(spawnType)
                || mob.getPersistentData().getBoolean(REPLACEMENT)) return;
        mob.getPersistentData().putBoolean(DAILY_SPAWN, true);
        if (isWildSpawnType(spawnType)) mob.getPersistentData().putBoolean(WILD_SPAWN, true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void alterDailySpawns(EntityJoinLevelEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Mob mob)) return;
        if (mob.getPersistentData().getBoolean(REPLACEMENT)
                || mob.getPersistentData().getBoolean(LEGACY_REPLACEMENT)) {
            mob.getPersistentData().putBoolean(REPLACEMENT, true);
            trackReplacement(level, mob);
            return;
        }
        if (!mob.getPersistentData().getBoolean(DAILY_SPAWN) || protectedMob(mob)) return;
        DailyWorldEvent active = DailyWorldEventManager.active(level.getServer());
        if (active == null) return;

        switch (active) {
            case BLOOD_MOON -> {
                if (mob.getType().getCategory() == MobCategory.MONSTER) {
                    mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 24000, 0));
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 24000, 0));
                    mob.setGlowingTag(true);
                }
            }
            case BIG_FISH_MIGRATION -> replaceAquatic(event, level, mob);
            case KITCHEN_CHAOS -> replaceKitchenMonster(event, level, mob);
            case NO_SUCH_CREATURE -> { if (mob instanceof Creeper) mountCreeper(level, mob); }
            case UNDEAD_MIGRATION -> replaceWithUndead(event, level, mob);
            case PIG_MIGRATION -> { if (!(mob instanceof Pig)) replace(event, level, mob, EntityType.PIG); }
            case HEAVY_SNOW -> replaceHeavySnowMob(event, level, mob);
            case SLIME_RAIN -> replaceWithSlime(event, level, mob);
            case STARFALL -> { if (level.random.nextFloat() < .45F) mob.setGlowingTag(true); }
            case RABBIT_TIDE -> {
                if (mob.getType().getCategory() == MobCategory.CREATURE && mob.getType() != EntityType.RABBIT
                        && level.random.nextFloat() < .65F) replace(event, level, mob, EntityType.RABBIT);
            }
            case WITCH_CONVENTION -> {
                if (mob.getType().getCategory() == MobCategory.MONSTER && mob.getType() != EntityType.WITCH
                        && level.random.nextFloat() < .40F) replace(event, level, mob, EntityType.WITCH);
            }
            case GRAVITY_GLITCH -> {
                if (mob.getType().getCategory() != MobCategory.MISC && level.random.nextFloat() < .70F) {
                    mob.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 50 + level.random.nextInt(50), 0));
                    mob.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 500, 0));
                }
            }
            case MUSHROOM_BLOOM -> {
                if (mob.getType().getCategory() == MobCategory.CREATURE && mob.getType() != EntityType.MOOSHROOM
                        && level.random.nextFloat() < .35F) replace(event, level, mob, EntityType.MOOSHROOM);
            }
            case ENDER_ECLIPSE -> {
                if (mob.getType().getCategory() == MobCategory.MONSTER && mob.getType() != EntityType.ENDERMAN
                        && level.random.nextFloat() < .28F) replace(event, level, mob, EntityType.ENDERMAN);
            }
            default -> { }
        }
    }

    private static boolean isDailySpawnType(MobSpawnType type) {
        return isWildSpawnType(type) || type == MobSpawnType.SPAWNER;
    }

    private static boolean isWildSpawnType(MobSpawnType type) {
        return type == MobSpawnType.NATURAL || type == MobSpawnType.CHUNK_GENERATION
                || type == MobSpawnType.PATROL || type == MobSpawnType.REINFORCEMENT;
    }

    private static void applyHeavySnowRainEffects(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (player.isSpectator() || level.dimension() != Level.OVERWORLD
                || !level.isRainingAt(player.blockPosition().above())) return;

        // The brown rain itself applies both effects; eating is no longer required.
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, true, true));
        MobEffect poop = ForgeRegistries.MOB_EFFECTS.getValue(POOP_CONFUSION);
        if (poop != null) player.addEffect(new MobEffectInstance(poop, 80, 0, false, true, true));
        else player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 1, false, true, true));
    }

    private static boolean protectedMob(Mob mob) {
        return mob.getType().getCategory() == MobCategory.MISC
                || mob.isPersistenceRequired() || mob.hasCustomName()
                || (mob instanceof TamableAnimal tame && tame.isTame())
                || mob instanceof EnderDragon || mob instanceof WitherBoss;
    }

    private static void replaceAquatic(EntityJoinLevelEvent event, ServerLevel level, Mob source) {
        if (!(source instanceof AbstractFish fish)) return;
        EntityType<?> target = switch (DailyWorldEventRules.aquaticOutcome(level.random.nextDouble())) {
            case DailyWorldEventRules.AQUATIC_AMETHYST_CRAB -> optionalType("cataclysm", "amethyst_crab", EntityType.GUARDIAN);
            case DailyWorldEventRules.AQUATIC_ELDER_GUARDIAN -> EntityType.ELDER_GUARDIAN;
            case DailyWorldEventRules.AQUATIC_GUARDIAN -> EntityType.GUARDIAN;
            case DailyWorldEventRules.AQUATIC_DROWNED -> EntityType.DROWNED;
            case DailyWorldEventRules.AQUATIC_VALUABLE_FISH ->
                    fish instanceof TropicalFish || fish instanceof Pufferfish ? null
                            : level.random.nextBoolean() ? EntityType.COD : EntityType.SALMON;
            default -> null;
        };
        if (target != null) replace(event, level, source, target);
    }

    private static void replaceKitchenMonster(EntityJoinLevelEvent event, ServerLevel level, Mob source) {
        if (source.getType().getCategory() != MobCategory.MONSTER) return;
        EntityType<?> target = switch (DailyWorldEventRules.kitchenOutcome(level.random.nextDouble())) {
            case DailyWorldEventRules.KITCHEN_GINGERBREAD -> optionalType("ftboss", "gingerbread_minion", EntityType.SLIME);
            case DailyWorldEventRules.KITCHEN_BEER_ELEMENTAL -> optionalType("brewery", "beer_elemental", EntityType.WITCH);
            case DailyWorldEventRules.KITCHEN_EXPLOSIVE_BARREL -> optionalType("ftboss", "barrel_minion", EntityType.CREEPER);
            case DailyWorldEventRules.KITCHEN_SAYA_FLOWER -> optionalType("ftboss", "tea_minion", EntityType.BEE);
            default -> null;
        };
        if (target != null && target != source.getType()) replace(event, level, source, target);
    }

    private static void replaceHeavySnowMob(EntityJoinLevelEvent event, ServerLevel level, Mob source) {
        if (level.dimension() != Level.OVERWORLD
                || !source.getPersistentData().getBoolean(WILD_SPAWN)
                || source.getType().getCategory() != MobCategory.MONSTER) return;

        int outcome = DailyWorldEventRules.heavySnowOutcome(level.random.nextDouble());
        String path = DailyWorldEventRules.heavySnowEntityPath(outcome);
        if (path == null) return;

        EntityType<?> fallback = switch (outcome) {
            case DailyWorldEventRules.HEAVY_SNOW_FLY -> EntityType.SPIDER;
            case DailyWorldEventRules.HEAVY_SNOW_MAGGOT, DailyWorldEventRules.HEAVY_SNOW_APPLE_WORM -> EntityType.SILVERFISH;
            case DailyWorldEventRules.HEAVY_SNOW_POOP, DailyWorldEventRules.HEAVY_SNOW_COMPOST_SLIME,
                    DailyWorldEventRules.HEAVY_SNOW_SHEEP_DUNG -> EntityType.SLIME;
            case DailyWorldEventRules.HEAVY_SNOW_APPLE_MOTH -> EntityType.PHANTOM;
            case DailyWorldEventRules.HEAVY_SNOW_ROTTEN_APPLE -> EntityType.CREEPER;
            case DailyWorldEventRules.HEAVY_SNOW_GIANT_GRUB -> EntityType.RAVAGER;
            default -> null;
        };
        if (fallback == null) return;
        EntityType<?> target = optionalType("ftboss", path, fallback);

        // Giant grubs are rare and never stack around one player or boss arena.
        if (outcome == DailyWorldEventRules.HEAVY_SNOW_GIANT_GRUB
                && hasNearbyType(level, source, target, 96.0D)) return;
        replace(event, level, source, target);
    }

    private static boolean hasNearbyType(ServerLevel level, Entity source, EntityType<?> type, double radius) {
        return !level.getEntities(source, source.getBoundingBox().inflate(radius),
                entity -> entity.isAlive() && entity.getType() == type).isEmpty();
    }

    private static void replaceWithUndead(EntityJoinLevelEvent event, ServerLevel level, Mob source) {
        if (source.getMobType() == MobType.UNDEAD) return;
        EntityType<?> target = source instanceof WaterAnimal || source.isInWaterOrBubble()
                ? EntityType.DROWNED
                : level.dimension() == Level.NETHER ? EntityType.WITHER_SKELETON
                : level.random.nextBoolean() ? EntityType.ZOMBIE : EntityType.SKELETON;
        replace(event, level, source, target);
    }

    private static void replaceWithSlime(EntityJoinLevelEvent event, ServerLevel level, Mob source) {
        if (source.getType().getCategory() != MobCategory.MONSTER || source instanceof Slime
                || level.random.nextFloat() >= .25F) return;
        replace(event, level, source, level.dimension() == Level.NETHER ? EntityType.MAGMA_CUBE : EntityType.SLIME);
    }

    private static void mountCreeper(ServerLevel level, Mob source) {
        Phantom phantom = EntityType.PHANTOM.create(level);
        if (phantom == null) return;
        phantom.moveTo(source.getX(), source.getY() + 1.0D, source.getZ(), source.getYRot(), 0.0F);
        phantom.setPhantomSize(1);
        phantom.getPersistentData().putBoolean(REPLACEMENT, true);
        phantom.finalizeSpawn(level, level.getCurrentDifficultyAt(phantom.blockPosition()), MobSpawnType.EVENT, null, null);
        if (!level.addFreshEntity(phantom)) return;
        source.getPersistentData().putBoolean(REPLACEMENT, true);
        trackReplacement(level, phantom);
        trackReplacement(level, source);
        source.startRiding(phantom, true);
    }

    private static EntityType<?> optionalType(String namespace, String path, EntityType<?> fallback) {
        EntityType<?> found = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.fromNamespaceAndPath(namespace, path));
        return found == null ? fallback : found;
    }

    private static boolean replace(EntityJoinLevelEvent event, ServerLevel level, Mob source, EntityType<?> type) {
        Entity created = type.create(level);
        if (!(created instanceof Mob replacement)) return false;
        markReplacement(replacement);
        replacement.moveTo(source.getX(), source.getY(), source.getZ(), source.getYRot(), source.getXRot());
        replacement.finalizeSpawn((ServerLevelAccessor)level, level.getCurrentDifficultyAt(replacement.blockPosition()),
                MobSpawnType.EVENT, null, null);
        for (int[] offset : REPLACEMENT_OFFSETS) {
            replacement.moveTo(source.getX() + offset[0], source.getY() + offset[1], source.getZ() + offset[2],
                    source.getYRot(), source.getXRot());
            if (!level.noCollision(replacement)) continue;
            if (!level.addFreshEntity(replacement)) return false;
            trackReplacement(level, replacement);
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    /** Immediately exposes creature-based events when an operator uses the test command. */
    public static int spawnImmediatePreview(MinecraftServer server, DailyWorldEvent event) {
        if (!DailyWorldEventRules.hasCreatureSpawns(event)) return 0;
        int spawned = 0;
        boolean heavySnow = event == DailyWorldEvent.HEAVY_SNOW;
        int attemptsPerPlayer = heavySnow ? DailyWorldEventRules.HEAVY_SNOW_PREVIEW_SPAWNS_PER_PLAYER : 1;
        double minRadius = heavySnow ? DailyWorldEventRules.HEAVY_SNOW_PREVIEW_MIN_RADIUS : PREVIEW_MIN_RADIUS;
        double maxRadius = heavySnow ? DailyWorldEventRules.HEAVY_SNOW_PREVIEW_MAX_RADIUS : PREVIEW_MAX_RADIUS;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator()) continue;
            for (int attempt = 0; attempt < attemptsPerPlayer; attempt++) {
                if (spawnEventCreature(player, event, minRadius, maxRadius) != null) spawned++;
            }
        }
        return spawned;
    }

    private static void pulseEventCreatures(MinecraftServer server, DailyWorldEvent event) {
        if (!DailyWorldEventRules.hasCreatureSpawns(event)) return;
        int eligiblePlayers = 0;
        int cappedPlayers = 0;
        int spawned = 0;
        boolean heavySnow = event == DailyWorldEvent.HEAVY_SNOW;
        double minRadius = heavySnow ? DailyWorldEventRules.HEAVY_SNOW_PULSE_MIN_RADIUS : PULSE_MIN_RADIUS;
        double maxRadius = heavySnow ? DailyWorldEventRules.HEAVY_SNOW_PULSE_MAX_RADIUS : PULSE_MAX_RADIUS;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator()) continue;
            eligiblePlayers++;
            if (nearbyEventMobCount(player) >= EVENT_MOB_CAP_PER_PLAYER) {
                cappedPlayers++;
                continue;
            }
            if (spawnEventCreature(player, event, minRadius, maxRadius) != null) spawned++;
        }
        if (event == DailyWorldEvent.HEAVY_SNOW) {
            QysEventMod.LOGGER.info("[QysEvent][HeavySnowSpawn] players={} spawned={} capped={}",
                    eligiblePlayers, spawned, cappedPlayers);
        }
    }

    private static int nearbyEventMobCount(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        return level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(EVENT_MOB_CAP_RADIUS),
                mob -> mob.isAlive() && (mob.getPersistentData().getBoolean(REPLACEMENT)
                        || mob.getPersistentData().getBoolean(LEGACY_REPLACEMENT))).size();
    }

    private static Mob spawnEventCreature(ServerPlayer player, DailyWorldEvent event,
                                          double minRadius, double maxRadius) {
        ServerLevel level = player.serverLevel();
        return switch (event) {
            case BIG_FISH_MIGRATION -> spawnNearPlayer(player, aquaticPulseType(level), true, false,
                    minRadius, maxRadius);
            case KITCHEN_CHAOS -> spawnNearPlayer(player, kitchenPulseType(level), false, false,
                    minRadius, maxRadius);
            case NO_SUCH_CREATURE -> {
                Mob creeper = spawnNearPlayer(player, EntityType.CREEPER, false, false, minRadius, maxRadius);
                if (creeper != null) mountCreeper(level, creeper);
                yield creeper;
            }
            case UNDEAD_MIGRATION -> spawnNearPlayer(player,
                    level.dimension() == Level.NETHER ? EntityType.WITHER_SKELETON
                            : level.random.nextBoolean() ? EntityType.ZOMBIE : EntityType.SKELETON,
                    false, false, minRadius, maxRadius);
            case PIG_MIGRATION -> spawnNearPlayer(player, EntityType.PIG, false, false, minRadius, maxRadius);
            case HEAVY_SNOW -> level.dimension() == Level.OVERWORLD
                    ? spawnNearPlayer(player, heavySnowPulseType(level, player), false,
                            DailyWorldEventRules.HEAVY_SNOW_PULSE_REQUIRES_SKY, minRadius, maxRadius)
                    : null;
            case SLIME_RAIN -> spawnNearPlayer(player,
                    level.dimension() == Level.NETHER ? EntityType.MAGMA_CUBE : EntityType.SLIME,
                    false, false, minRadius, maxRadius);
            case RABBIT_TIDE -> spawnNearPlayer(player, EntityType.RABBIT, false, false, minRadius, maxRadius);
            case WITCH_CONVENTION -> spawnNearPlayer(player, EntityType.WITCH, false, false, minRadius, maxRadius);
            case MUSHROOM_BLOOM -> spawnNearPlayer(player, EntityType.MOOSHROOM, false, false, minRadius, maxRadius);
            case ENDER_ECLIPSE -> spawnNearPlayer(player, EntityType.ENDERMAN, false, false, minRadius, maxRadius);
            default -> null;
        };
    }

    private static EntityType<?> aquaticPulseType(ServerLevel level) {
        int outcome = DailyWorldEventRules.aquaticOutcome(level.random.nextDouble() * .535D);
        return switch (outcome) {
            case DailyWorldEventRules.AQUATIC_AMETHYST_CRAB ->
                    optionalType("cataclysm", "amethyst_crab", EntityType.GUARDIAN);
            case DailyWorldEventRules.AQUATIC_ELDER_GUARDIAN -> EntityType.ELDER_GUARDIAN;
            case DailyWorldEventRules.AQUATIC_GUARDIAN -> EntityType.GUARDIAN;
            case DailyWorldEventRules.AQUATIC_DROWNED -> EntityType.DROWNED;
            default -> switch (level.random.nextInt(4)) {
                case 0 -> EntityType.COD;
                case 1 -> EntityType.SALMON;
                case 2 -> EntityType.TROPICAL_FISH;
                default -> EntityType.PUFFERFISH;
            };
        };
    }

    private static EntityType<?> kitchenPulseType(ServerLevel level) {
        return switch (level.random.nextInt(4)) {
            case 0 -> optionalType("ftboss", "gingerbread_minion", EntityType.SLIME);
            case 1 -> optionalType("brewery", "beer_elemental", EntityType.WITCH);
            case 2 -> optionalType("ftboss", "barrel_minion", EntityType.CREEPER);
            default -> optionalType("ftboss", "tea_minion", EntityType.BEE);
        };
    }

    private static EntityType<?> heavySnowPulseType(ServerLevel level, Entity source) {
        int outcome = DailyWorldEventRules.heavySnowOutcome(.55D + level.random.nextDouble() * .45D);
        EntityType<?> target = heavySnowType(outcome);
        if (outcome == DailyWorldEventRules.HEAVY_SNOW_GIANT_GRUB
                && target != null && hasNearbyType(level, source, target, 96.0D)) {
            target = heavySnowType(DailyWorldEventRules.HEAVY_SNOW_FLY);
        }
        return target == null ? EntityType.SPIDER : target;
    }

    private static EntityType<?> heavySnowType(int outcome) {
        String path = DailyWorldEventRules.heavySnowEntityPath(outcome);
        EntityType<?> fallback = switch (outcome) {
            case DailyWorldEventRules.HEAVY_SNOW_FLY -> EntityType.SPIDER;
            case DailyWorldEventRules.HEAVY_SNOW_MAGGOT, DailyWorldEventRules.HEAVY_SNOW_APPLE_WORM -> EntityType.SILVERFISH;
            case DailyWorldEventRules.HEAVY_SNOW_POOP, DailyWorldEventRules.HEAVY_SNOW_COMPOST_SLIME,
                    DailyWorldEventRules.HEAVY_SNOW_SHEEP_DUNG -> EntityType.SLIME;
            case DailyWorldEventRules.HEAVY_SNOW_APPLE_MOTH -> EntityType.PHANTOM;
            case DailyWorldEventRules.HEAVY_SNOW_ROTTEN_APPLE -> EntityType.CREEPER;
            case DailyWorldEventRules.HEAVY_SNOW_GIANT_GRUB -> EntityType.RAVAGER;
            default -> null;
        };
        return path == null || fallback == null ? null : optionalType("ftboss", path, fallback);
    }

    private static Mob spawnNearPlayer(ServerPlayer player, EntityType<?> type, boolean water,
                                       boolean requireSky, double minRadius, double maxRadius) {
        if (type == null) return null;
        ServerLevel level = player.serverLevel();
        Entity created = type.create(level);
        if (!(created instanceof Mob mob)) return null;
        markReplacement(mob);
        boolean finalized = false;
        for (int attempt = 0; attempt < 20; attempt++) {
            BlockPos pos = water
                    ? waterSpawnCandidate(level, player, minRadius, maxRadius)
                    : groundSpawnCandidate(level, player, requireSky, minRadius, maxRadius);
            if (pos == null) continue;
            mob.moveTo(pos.getX() + .5D, pos.getY(), pos.getZ() + .5D,
                    level.random.nextFloat() * 360.0F, 0.0F);
            if (!finalized) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
                finalized = true;
            }
            if (!level.noCollision(mob)) continue;
            if (!level.addFreshEntity(mob)) return null;
            trackReplacement(level, mob);
            return mob;
        }
        return null;
    }

    private static BlockPos groundSpawnCandidate(ServerLevel level, ServerPlayer player, boolean requireSky,
                                                  double minRadius, double maxRadius) {
        int[] horizontal = randomHorizontal(level, player, minRadius, maxRadius);
        BlockPos chunkCheck = new BlockPos(horizontal[0], player.blockPosition().getY(), horizontal[1]);
        if (!level.hasChunkAt(chunkCheck)) return null;

        if (requireSky) {
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, horizontal[0], horizontal[1]);
            BlockPos pos = new BlockPos(horizontal[0], y, horizontal[1]);
            return validGround(level, pos) && level.canSeeSky(pos) ? pos : null;
        }

        int top = Mth.clamp(player.blockPosition().getY() + 8,
                level.getMinBuildHeight() + 1, level.getMaxBuildHeight() - 3);
        int bottom = Math.max(level.getMinBuildHeight() + 1, player.blockPosition().getY() - 20);
        for (int y = top; y >= bottom; y--) {
            BlockPos pos = new BlockPos(horizontal[0], y, horizontal[1]);
            if (validGround(level, pos)) return pos;
        }
        // A player in a narrow cave or high in the air may have no nearby floor at their Y level.
        // Non-ceiling dimensions can safely fall back to the surface without loading a new chunk.
        if (!level.dimensionType().hasCeiling()) {
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    horizontal[0], horizontal[1]);
            BlockPos surface = new BlockPos(horizontal[0], surfaceY, horizontal[1]);
            if (validGround(level, surface)) return surface;
        }
        return null;
    }

    private static boolean validGround(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos)
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.getFluidState(pos).isEmpty() && level.getFluidState(pos.above()).isEmpty()
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }

    private static BlockPos waterSpawnCandidate(ServerLevel level, ServerPlayer player,
                                                 double minRadius, double maxRadius) {
        int[] horizontal = randomHorizontal(level, player, minRadius, maxRadius);
        BlockPos chunkCheck = new BlockPos(horizontal[0], player.blockPosition().getY(), horizontal[1]);
        if (!level.hasChunkAt(chunkCheck)) return null;
        int top = Math.min(level.getMaxBuildHeight() - 2, player.blockPosition().getY() + 12);
        int bottom = Math.max(level.getMinBuildHeight() + 1, player.blockPosition().getY() - 16);
        for (int y = top; y >= bottom; y--) {
            BlockPos pos = new BlockPos(horizontal[0], y, horizontal[1]);
            if (level.getFluidState(pos).is(FluidTags.WATER)
                    && level.getWorldBorder().isWithinBounds(pos)) return pos;
        }
        int oceanFloor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, horizontal[0], horizontal[1]);
        BlockPos pos = new BlockPos(horizontal[0], oceanFloor, horizontal[1]);
        return level.getFluidState(pos).is(FluidTags.WATER)
                && level.getWorldBorder().isWithinBounds(pos) ? pos : null;
    }

    private static int[] randomHorizontal(ServerLevel level, ServerPlayer player,
                                          double minRadius, double maxRadius) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        double distance = minRadius + level.random.nextDouble() * (maxRadius - minRadius);
        return new int[]{Mth.floor(player.getX() + Math.cos(angle) * distance),
                Mth.floor(player.getZ() + Math.sin(angle) * distance)};
    }

    private static void markReplacement(Entity entity) {
        entity.getPersistentData().putBoolean(REPLACEMENT, true);
    }

    private static void trackReplacement(ServerLevel level, Entity entity) {
        REPLACEMENTS.computeIfAbsent(level.dimension(), ignored -> new HashSet<>()).add(entity.getUUID());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onCropGrowth(BlockEvent.CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (DailyWorldEventManager.active(level.getServer()) == DailyWorldEvent.HARVEST_MOON
                && level.random.nextFloat() < .35F) event.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void rememberCropHarvest(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || event.getPlayer().isCreative() || !(event.getLevel() instanceof ServerLevel level)) return;
        if (!isMatureCrop(event.getState())) return;
        DailyWorldEvent active = DailyWorldEventManager.active(level.getServer());
        int multiplier = active == DailyWorldEvent.HARVEST_MOON ? 2
                : active == DailyWorldEvent.KITCHEN_CHAOS
                ? DailyWorldEventRules.kitchenHarvestMultiplier(level.random.nextDouble()) : 0;
        if (multiplier > 0) HARVESTS.put(new HarvestKey(level.dimension(), event.getPos().asLong()),
                new HarvestContext(multiplier, level.getGameTime() + 3L));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void adjustCropDrops(EntityJoinLevelEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ItemEntity item)
                || item.getPersistentData().getBoolean(HARVEST_ADJUSTED)) return;
        HarvestContext context = HARVESTS.get(new HarvestKey(level.dimension(), item.blockPosition().asLong()));
        if (context == null || context.expiresAt() < level.getGameTime()) return;
        item.getPersistentData().putBoolean(HARVEST_ADJUSTED, true);
        ItemStack adjusted = item.getItem().copy();
        adjusted.setCount(Math.min(adjusted.getMaxStackSize(), adjusted.getCount() * context.multiplier()));
        item.setItem(adjusted);
    }

    private static boolean isMatureCrop(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) return crop.isMaxAge(state);
        if (block instanceof NetherWartBlock) return state.getValue(NetherWartBlock.AGE) >= 3;
        if (block instanceof CocoaBlock) return state.getValue(CocoaBlock.AGE) >= 2;
        return block instanceof StemGrownBlock;
    }

    private static void cleanupHarvests(MinecraftServer server) {
        Iterator<Map.Entry<HarvestKey, HarvestContext>> it = HARVESTS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<HarvestKey, HarvestContext> entry = it.next();
            ServerLevel level = server.getLevel(entry.getKey().dimension());
            if (level == null || level.getGameTime() > entry.getValue().expiresAt()) it.remove();
        }
    }

    private static void despawnFarReplacements(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            Set<UUID> ids = REPLACEMENTS.get(level.dimension());
            if (ids == null || ids.isEmpty()) continue;
            long now = level.getGameTime();
            Iterator<UUID> it = ids.iterator();
            while (it.hasNext()) {
                Entity entity = level.getEntity(it.next());
                if (!(entity instanceof Mob mob) || !mob.isAlive()) { it.remove(); continue; }
                // Event entities from qys-bosses deliberately mark themselves persistent.
                // Distance cleanup must still own them; only explicit player ownership protects one.
                if (mob.hasCustomName() || mob.isLeashed()
                        || (mob instanceof TamableAnimal tame && tame.isTame())) {
                    mob.getPersistentData().remove(FAR_SINCE); continue;
                }
                if (level.getNearestPlayer(mob, 48.0D) != null) {
                    mob.getPersistentData().remove(FAR_SINCE); continue;
                }
                CompoundTag tag = mob.getPersistentData();
                long since = tag.getLong(FAR_SINCE);
                if (since <= 0L || since > now) { tag.putLong(FAR_SINCE, now); continue; }
                long farTicks = now - since;
                if (farTicks >= FAR_HARD_LIMIT_TICKS || (farTicks >= FAR_GRACE_TICKS && level.random.nextFloat() < .35F)) {
                    mob.discard(); it.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Enemy)
                || !(event.getSource().getEntity() instanceof ServerPlayer)) return;
        DailyWorldEvent active = DailyWorldEventManager.active(level.getServer());
        if (active == DailyWorldEvent.BLOOD_MOON && level.random.nextFloat() < .30F) {
            addDrop(event, level, new ItemStack(Items.REDSTONE, 1 + level.random.nextInt(3)));
        } else if (active == DailyWorldEvent.FIREFLY_NIGHT && level.random.nextFloat() < .24F) {
            addDrop(event, level, new ItemStack(Items.GLOWSTONE_DUST, 1 + level.random.nextInt(2)));
        } else if (active == DailyWorldEvent.LUCKY_DAY) {
            float roll = level.random.nextFloat();
            if (roll < .025F) addDrop(event, level, new ItemStack(Items.EMERALD));
            else if (roll < .12F) addDrop(event, level, new ItemStack(Items.IRON_NUGGET, 1 + level.random.nextInt(4)));
        } else if (active == DailyWorldEvent.STARFALL) {
            float roll = level.random.nextFloat();
            if (roll < .02F) addDrop(event, level, new ItemStack(Items.AMETHYST_SHARD));
            else if (roll < .10F) addDrop(event, level, new ItemStack(Items.EMERALD));
        }
    }

    private static void addDrop(LivingDropsEvent event, ServerLevel level, ItemStack stack) {
        event.getDrops().add(new ItemEntity(level, event.getEntity().getX(), event.getEntity().getY() + .4D,
                event.getEntity().getZ(), stack));
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || event.getPlayer().isCreative() || !(event.getLevel() instanceof ServerLevel level)) return;
        DailyWorldEvent active = DailyWorldEventManager.active(level.getServer());
        if (active == DailyWorldEvent.GOLD_RUSH && event.getState().is(Tags.Blocks.ORES)) {
            float roll = level.random.nextFloat();
            if (roll < .012F) Block.popResource(level, event.getPos(), new ItemStack(Items.RAW_GOLD));
            else if (roll < .13F) Block.popResource(level, event.getPos(), new ItemStack(Items.GOLD_NUGGET, 1 + level.random.nextInt(3)));
        } else if (active == DailyWorldEvent.MUSHROOM_BLOOM
                && (event.getState().is(BlockTags.LEAVES) || event.getState().is(BlockTags.FLOWERS))
                && level.random.nextFloat() < .30F) {
            Block.popResource(level, event.getPos(), new ItemStack(level.random.nextBoolean() ? Items.RED_MUSHROOM : Items.BROWN_MUSHROOM,
                    1 + level.random.nextInt(2)));
        }
    }

    private static void playCreeperWhispers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getRandom().nextFloat() < .20F) {
                player.playSound(net.minecraft.sounds.SoundEvents.CREEPER_PRIMED, .12F, .65F + player.getRandom().nextFloat() * .2F);
            }
        }
    }

    private record HarvestKey(ResourceKey<Level> dimension, long position) {}
    private record HarvestContext(int multiplier, long expiresAt) {}

}
