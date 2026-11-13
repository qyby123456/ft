package net.farmingtales.ftdungeonmobs;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftdungeonmobs.DungeonSpawnData.Area;
import net.farmingtales.ftdungeonmobs.DungeonSpawnData.Point;
import net.farmingtales.ftdungeonmobs.DungeonSpawnData.Room;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

public final class DungeonSpawnPoints {
    static final String ROUND_TAG = "FTDungeonSpawnRound";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FLAGS = Block.UPDATE_CLIENTS;

    private DungeonSpawnPoints() {}

    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level
                && level.dimension().equals(DungeonGateTravel.DUNGEON) && level.getGameTime() % 10 == 0) {
            tick(level, level.players());
        }
    }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity().getPersistentData().hasUUID(ROUND_TAG)) {
            DungeonGateData round = DungeonGateData.get(level.getServer());
            if (!level.dimension().equals(DungeonGateTravel.DUNGEON) || !round.roundActive()
                    || !event.getEntity().getPersistentData().getUUID(ROUND_TAG).equals(round.roundId())) {
                event.setCanceled(true);
            }
        }
    }

    static void tick(ServerLevel level, List<ServerPlayer> players) {
        if (!level.dimension().equals(DungeonGateTravel.DUNGEON) || !DungeonSpawnConfig.enabled()) {
            return;
        }
        DungeonGateData round = DungeonGateData.get(level.getServer());
        if (!round.roundActive()) {
            return;
        }
        DungeonSpawnData data = DungeonSpawnData.get(level);
        if (!round.roundId().equals(data.roundId)) {
            beginRound(level, data, round.roundId(), round.tier());
        }
        clearRetired(level, data);
        List<ServerPlayer> eligible = players.stream().filter(player -> player.serverLevel() == level
                && player.isAlive() && !player.isSpectator() && !player.isCreative() && round.hasParticipant(player.getUUID())).toList();
        if (eligible.isEmpty()) {
            return;
        }
        if (data.catalog == null) {
            data.catalog = catalog(level);
        }
        DungeonRoomsData installed = DungeonRoomsData.get(level);
        for (Area area : data.catalog) {
            boolean ready = area.fingerprint().equals("legacy") ? installed.ready()
                    : area.fingerprint().equals(installed.roomFingerprint(area.id()));
            if (ready && !data.rooms.containsKey(area.id()) && loaded(level, area)
                    && eligible.stream().anyMatch(player -> area.bounds().inflate(12).contains(player.position()))) {
                generate(level, data, area);
                break;
            }
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        for (Room room : data.rooms.values()) {
            for (Point point : room.points()) {
                if (point.spent() || !level.hasChunkAt(point.position)
                        || !level.getBlockState(point.position).is(FTDungeonMobs.DUNGEON_SPAWN_POINT.get())) {
                    continue;
                }
                ServerPlayer target = eligible.stream().filter(player -> canTrigger(level, room.area(), point, player))
                        .findFirst().orElse(null);
                if (target == null) {
                    continue;
                }
                if (!point.activated) {
                    point.activated = true;
                    data.setDirty();
                    level.playSound(null, point.position, SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 0.65F, 1.25F);
                }
                while (!point.spent() && spawn(level, data, room.area(), point, target)) {
                    point.spawned++;
                    data.setDirty();
                }
                if (point.spent()) {
                    level.setBlock(point.position, FTDungeonMobs.DUNGEON_SPAWN_POINT.get().defaultBlockState()
                            .setValue(DungeonSpawnPointBlock.SPENT, true), FLAGS);
                }
            }
        }
    }

    static void beginRound(ServerLevel level, DungeonSpawnData data, UUID identity, int tier) {
        for (Room room : data.rooms.values()) {
            room.points().forEach(point -> data.retired.add(point.position));
        }
        data.rooms.clear();
        data.roundId = identity;
        data.tier = tier;
        data.catalog = null;
        data.setDirty();
        List<Entity> previous = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity.getPersistentData().hasUUID(ROUND_TAG) && !entity.getPersistentData().getUUID(ROUND_TAG).equals(identity)) {
                previous.add(entity);
            }
        }
        previous.forEach(Entity::discard);
        clearRetired(level, data);
    }

    private static void clearRetired(ServerLevel level, DungeonSpawnData data) {
        if (data.retired.removeIf(position -> level.hasChunkAt(position)
                && (!level.getBlockState(position).is(FTDungeonMobs.DUNGEON_SPAWN_POINT.get()) || level.removeBlock(position, false)))) {
            data.setDirty();
        }
    }

    static List<Area> catalog(ServerLevel level) {
        List<Area> areas = new ArrayList<>();
        for (DungeonRooms.Room room : DungeonRooms.ROOMS) {
            BlockPos entrance = room.name().equals("31") ? new BlockPos(0, 67, 32) : new BlockPos(49, 66, 32);
            areas.add(new Area("LEGACY_" + room.name(), room.origin(), room.size(), entrance, "legacy"));
        }
        try {
            for (DungeonRoomCatalog.Entry entry : DungeonRoomCatalog.entries(level.getServer().getResourceManager())) {
                DungeonRoomCatalog.Definition room = DungeonRoomCatalog.definition(level.getServer().getResourceManager(), entry);
                areas.add(new Area(room.id(), room.origin(), room.size(), room.origin().offset(room.entrance()), room.fingerprint()));
            }
        } catch (Exception exception) {
            LOGGER.warn("Could not discover all FT dungeon encounter rooms", exception);
        }
        return List.copyOf(areas);
    }

    private static boolean loaded(ServerLevel level, Area area) {
        BlockPos maximum = area.origin().offset(area.size()).offset(-1, -1, -1);
        return level.hasChunksAt(area.origin(), maximum);
    }

    static Room generate(ServerLevel level, DungeonSpawnData data, Area area) {
        List<BlockPos> candidates = reachable(level, area);
        for (int index = candidates.size() - 1; index > 0; index--) {
            int other = level.random.nextInt(index + 1);
            BlockPos previous = candidates.set(index, candidates.get(other));
            candidates.set(other, previous);
        }
        List<Point> points = new ArrayList<>();
        List<SkeletonVariant> pool = DungeonSpawnConfig.mobPool();
        int clearance = (int) Math.ceil(pool.stream().mapToDouble(SkeletonVariant::height).max().orElse(2));
        int desired = DungeonSpawnConfig.pointCount(data.tier);
        if (!pool.isEmpty()) {
            for (BlockPos position : candidates) {
                BlockPos local = position.subtract(area.origin());
                if (points.size() >= desired) {
                    break;
                }
                if (local.getX() < 1 || local.getZ() < 1 || local.getX() >= area.size().getX() - 1
                        || local.getZ() >= area.size().getZ() - 1 || position.distSqr(area.entrance()) < 16
                        || !level.getBlockState(position).isAir() || !safeFloor(level, position)
                        || !clearance(level, position, clearance)
                        || points.stream().anyMatch(point -> point.position.distSqr(position) < 9)) {
                    continue;
                }
                List<SkeletonVariant> group = new ArrayList<>();
                int count = DungeonSpawnConfig.groupSize(level.random);
                for (int index = 0; index < count; index++) {
                    group.add(pool.get(level.random.nextInt(pool.size())));
                }
                if (level.setBlock(position, FTDungeonMobs.DUNGEON_SPAWN_POINT.get().defaultBlockState(), FLAGS)) {
                    points.add(new Point(position, group));
                }
            }
        }
        Room room = new Room(area, List.copyOf(points));
        data.rooms.put(area.id(), room);
        data.setDirty();
        LOGGER.info("FT dungeon encounter room {}: {} / {} points at tier {}", area.id(), points.size(), desired, data.tier);
        return room;
    }

    static List<BlockPos> reachable(ServerLevel level, Area area) {
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> result = new ArrayList<>();
        boolean naturalTerrain = area.fingerprint().equals("legacy");
        if (!area.contains(area.entrance()) || !walkable(level, area.entrance(), naturalTerrain)) {
            return result;
        }
        pending.add(area.entrance());
        visited.add(area.entrance());
        while (!pending.isEmpty() && visited.size() <= 32768) {
            BlockPos current = pending.removeFirst();
            result.add(current);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int height : new int[]{0, -1, 1}) {
                    BlockPos next = current.relative(direction).offset(0, height, 0);
                    if (!area.contains(next) || visited.contains(next) || !walkable(level, next, naturalTerrain)) {
                        continue;
                    }
                    if (height != 0 && (!naturalTerrain
                            && !(level.getBlockState(current.below()).getBlock() instanceof StairBlock)
                            && !(level.getBlockState(next.below()).getBlock() instanceof StairBlock)
                            || !clearance(level, (height > 0 ? current : next).above(), 2, naturalTerrain))) {
                        continue;
                    }
                    visited.add(next);
                    pending.addLast(next);
                }
            }
        }
        return result;
    }

    private static boolean walkable(ServerLevel level, BlockPos position, boolean allowWater) {
        if (!clearance(level, position, 2, allowWater)) {
            return false;
        }
        BlockState floor = level.getBlockState(position.below());
        return !hazard(floor) && floor.getFluidState().isEmpty() && (floor.isFaceSturdy(level, position.below(), Direction.UP)
                || floor.getBlock() instanceof StairBlock);
    }

    private static boolean safeFloor(ServerLevel level, BlockPos position) {
        if (!level.hasChunkAt(position.below()) || !level.getWorldBorder().isWithinBounds(position)
                || !level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP)) {
            return false;
        }
        for (BlockPos nearby : BlockPos.betweenClosed(position.offset(-1, -1, -1), position.offset(1, 0, 1))) {
            if (!level.hasChunkAt(nearby) || hazard(level.getBlockState(nearby)) || !level.getFluidState(nearby).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean clearance(ServerLevel level, BlockPos position, int height) {
        return clearance(level, position, height, false);
    }

    private static boolean clearance(ServerLevel level, BlockPos position, int height, boolean allowWater) {
        for (int offset = 0; offset < height; offset++) {
            BlockPos current = position.above(offset);
            if (!level.isInWorldBounds(current) || !level.hasChunkAt(current)) {
                return false;
            }
            BlockState state = level.getBlockState(current);
            if (hazard(state) || !state.getFluidState().isEmpty() && !(allowWater && state.getFluidState().is(FluidTags.WATER))
                    || !state.getCollisionShape(level, current).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean hazard(BlockState state) {
        return state.is(BlockTags.FIRE) || state.is(BlockTags.CAMPFIRES) || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.CACTUS) || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.WITHER_ROSE)
                || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.POINTED_DRIPSTONE)
                || state.is(ForgeRegistries.BLOCKS.getValue(FTDungeonMobs.resource("dungeon_spike_barrier")));
    }

    static boolean canTrigger(ServerLevel level, Area area, Point point, ServerPlayer player) {
        Vec3 position = Vec3.atBottomCenterOf(point.position).add(0, 0.25, 0);
        if (!area.bounds().contains(player.position()) || player.distanceToSqr(position) > Math.pow(DungeonSpawnConfig.activationRadius(), 2)) {
            return false;
        }
        return level.clip(new ClipContext(player.getEyePosition(), position, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
    }

    private static boolean spawn(ServerLevel level, DungeonSpawnData data, Area area, Point point, ServerPlayer target) {
        SkeletonVariant variant = point.group.get(point.spawned);
        Entity entity = ForgeRegistries.ENTITY_TYPES.getValue(FTDungeonMobs.resource(variant.id())).create(level);
        if (!(entity instanceof DungeonSkeletonEntity skeleton)) {
            return false;
        }
        for (int distance = 0; distance <= 2; distance++) {
            for (int blockX = -distance; blockX <= distance; blockX++) {
                for (int blockZ = -distance; blockZ <= distance; blockZ++) {
                    if (Math.max(Math.abs(blockX), Math.abs(blockZ)) != distance) {
                        continue;
                    }
                    BlockPos feet = point.position.offset(blockX, 0, blockZ);
                    if (!area.contains(feet) || !safeFloor(level, feet) || !clearance(level, feet, (int) Math.ceil(variant.height()))) {
                        continue;
                    }
                    skeleton.moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, level.random.nextFloat() * 360, 0);
                    if (target.distanceToSqr(skeleton) < 2.25 || !area.bounds().contains(skeleton.getBoundingBox().minX, skeleton.getY(), skeleton.getBoundingBox().minZ)
                            || !area.bounds().contains(skeleton.getBoundingBox().maxX, skeleton.getBoundingBox().maxY, skeleton.getBoundingBox().maxZ)
                            || !level.getWorldBorder().isWithinBounds(skeleton.getBoundingBox()) || !level.noCollision(skeleton)
                            || level.containsAnyLiquid(skeleton.getBoundingBox())
                            || !level.getEntitiesOfClass(LivingEntity.class, skeleton.getBoundingBox(), LivingEntity::isAlive).isEmpty()) {
                        continue;
                    }
                    skeleton.setPersistenceRequired();
                    skeleton.setCanPickUpLoot(false);
                    skeleton.getPersistentData().putUUID(ROUND_TAG, data.roundId);
                    DungeonDifficultyApi.setTier(skeleton, data.tier);
                    if (!level.addFreshEntity(skeleton)) {
                        skeleton.discard();
                        return false;
                    }
                    skeleton.setTarget(target);
                    level.sendParticles(ParticleTypes.POOF, skeleton.getX(), skeleton.getY() + 0.5, skeleton.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
                    return true;
                }
            }
        }
        skeleton.discard();
        return false;
    }

    public static Component status(ServerLevel level) {
        DungeonSpawnData data = DungeonSpawnData.get(level);
        int total = data.rooms.values().stream().mapToInt(room -> room.points().size()).sum();
        long spent = data.rooms.values().stream().flatMap(room -> room.points().stream()).filter(Point::spent).count();
        int remaining = data.rooms.values().stream().flatMap(room -> room.points().stream())
                .mapToInt(point -> point.group.size() - point.spawned).sum();
        return Component.translatable("message.ftdungeonmobs.spawns.status", data.tier, data.rooms.size(), total, spent, remaining);
    }
}
