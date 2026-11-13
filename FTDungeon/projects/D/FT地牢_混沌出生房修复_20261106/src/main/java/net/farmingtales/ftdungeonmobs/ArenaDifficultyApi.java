package net.farmingtales.ftdungeonmobs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ArenaDifficultyApi {
    private ArenaDifficultyApi() {}

    public static OptionalInt findTier(ServerLevel level, Vec3 position) {
        requireServer(level);
        return data(level).findTier(level, position, DungeonDifficultyConfig.settings().arenaRadius());
    }

    public static Optional<ArenaSource> findSource(ServerLevel level, Vec3 position, int radius) {
        requireServer(level);
        if (radius < 1 || radius > 64) {
            throw new IllegalArgumentException("Brazier search radius must be 1..64.");
        }
        return data(level).findSource(level, position, radius);
    }

    public record ArenaSource(BlockPos position, int tier) {}

    public static int setTier(ServerLevel level, BlockPos position, int tier) {
        requireServer(level);
        if (tier != 0) {
            DungeonDifficultyConfig.validateTier(tier);
        }
        BlockState state = level.getBlockState(position);
        BlockPos controller = state.is(FTDungeonMobs.ARENA_TORCH.get())
                && state.getValue(ArenaTorchBlock.HALF) == DoubleBlockHalf.UPPER ? position.below() : position;
        if (DungeonGateTravel.brazierLocked(level, controller)) {
            throw new IllegalStateException("The entrance braziers are locked for the active dungeon round.");
        }
        if (state.is(FTDungeonMobs.BLOODTHORN_BRAZIER.get())) {
            level.setBlock(position, state.setValue(BloodthornBrazierBlock.TIER, tier), 3);
            data(level).put(position, tier);
            return refreshNearby(level, position) + DungeonGateTravel.onArenaChanged(level, position);
        }
        if (!state.is(FTDungeonMobs.ARENA_TORCH.get())) {
            throw new IllegalArgumentException("Position does not contain an arena torch or bloodthorn brazier.");
        }
        BlockPos base = state.getValue(ArenaTorchBlock.HALF) == DoubleBlockHalf.UPPER ? position.below() : position;
        BlockState lower = level.getBlockState(base);
        BlockState upper = level.getBlockState(base.above());
        if (!lower.is(FTDungeonMobs.ARENA_TORCH.get()) || lower.getValue(ArenaTorchBlock.HALF) != DoubleBlockHalf.LOWER
                || !upper.is(FTDungeonMobs.ARENA_TORCH.get()) || upper.getValue(ArenaTorchBlock.HALF) != DoubleBlockHalf.UPPER) {
            throw new IllegalArgumentException("Arena spear torch is incomplete.");
        }
        level.setBlock(base, lower.setValue(ArenaTorchBlock.TIER, tier), 3);
        level.setBlock(base.above(), upper.setValue(ArenaTorchBlock.TIER, tier), 3);
        data(level).put(base, tier);
        return refreshNearby(level, base) + DungeonGateTravel.onArenaChanged(level, base);
    }

    static void register(ServerLevel level, BlockPos position, int tier) {
        data(level).put(position, tier);
        refreshNearby(level, position);
        DungeonGateTravel.onArenaChanged(level, position);
    }

    static void unregister(ServerLevel level, BlockPos position) {
        data(level).remove(position);
        refreshNearby(level, position);
        DungeonGateTravel.onArenaChanged(level, position);
    }

    private static int refreshNearby(ServerLevel level, BlockPos position) {
        int radius = DungeonDifficultyConfig.settings().arenaRadius();
        int count = 0;
        for (DungeonSkeletonEntity entity : level.getEntitiesOfClass(DungeonSkeletonEntity.class, new AABB(position).inflate(radius))) {
            if (entity.automaticDifficulty() && entity.position().distanceToSqr(position.getX() + 0.5,
                    position.getY() + 1, position.getZ() + 0.5) <= radius * radius) {
                DungeonDifficultyApi.refresh(entity);
                count++;
            }
        }
        return count;
    }

    private static ArenaData data(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ArenaData::load, ArenaData::new, FTDungeonMobs.ID + "_arenas");
    }

    private static void requireServer(ServerLevel level) {
        if (!level.getServer().isSameThread()) {
            throw new IllegalStateException("Arena difficulty changes must run on the server thread.");
        }
    }

    static final class ArenaData extends SavedData {
        private final Map<Long, Map<BlockPos, Integer>> chunks = new HashMap<>();

        Optional<ArenaSource> findSource(ServerLevel level, Vec3 position, int radius) {
            ArenaSource selected = null;
            double closest = radius * radius;
            int minimumChunkX = ((int) Math.floor(position.x - radius)) >> 4;
            int maximumChunkX = ((int) Math.floor(position.x + radius)) >> 4;
            int minimumChunkZ = ((int) Math.floor(position.z - radius)) >> 4;
            int maximumChunkZ = ((int) Math.floor(position.z + radius)) >> 4;
            for (int chunkX = minimumChunkX; chunkX <= maximumChunkX; chunkX++) {
                for (int chunkZ = minimumChunkZ; chunkZ <= maximumChunkZ; chunkZ++) {
                    Map<BlockPos, Integer> entries = chunks.get(ChunkPos.asLong(chunkX, chunkZ));
                    if (entries == null) {
                        continue;
                    }
                    for (BlockPos candidate : entries.keySet()) {
                        if (!level.hasChunkAt(candidate)) {
                            continue;
                        }
                        BlockState state = level.getBlockState(candidate);
                        boolean legacy = state.is(FTDungeonMobs.ARENA_TORCH.get())
                                && state.getValue(ArenaTorchBlock.HALF) == DoubleBlockHalf.LOWER;
                        if (!state.is(FTDungeonMobs.BLOODTHORN_BRAZIER.get()) && !legacy) {
                            continue;
                        }
                        if (state.getValue(BloodthornBrazierBlock.TIER) == 0) {
                            continue;
                        }
                        double distance = position.distanceToSqr(Vec3.atCenterOf(candidate));
                        if (distance < closest || distance == closest
                                && (selected == null || candidate.asLong() < selected.position().asLong())) {
                            closest = distance;
                            selected = new ArenaSource(candidate.immutable(), state.getValue(BloodthornBrazierBlock.TIER));
                        }
                    }
                }
            }
            return Optional.ofNullable(selected);
        }

        static ArenaData load(CompoundTag tag) {
            ArenaData data = new ArenaData();
            ListTag entries = tag.getList("Torches", Tag.TAG_COMPOUND);
            for (int index = 0; index < entries.size(); index++) {
                CompoundTag entry = entries.getCompound(index);
                int tier = entry.getInt("Tier");
                if (tier >= 1 && tier <= 5) {
                    data.put(BlockPos.of(entry.getLong("Position")), tier);
                }
            }
            data.setDirty(false);
            return data;
        }

        void put(BlockPos position, int tier) {
            if (tier == 0) {
                remove(position);
                return;
            }
            Integer previous = chunks.computeIfAbsent(ChunkPos.asLong(position), ignored -> new HashMap<>())
                    .put(position.immutable(), tier);
            if (previous == null || previous != tier) {
                setDirty();
            }
        }

        void remove(BlockPos position) {
            Map<BlockPos, Integer> entries = chunks.get(ChunkPos.asLong(position));
            if (entries != null && entries.remove(position) != null) {
                if (entries.isEmpty()) {
                    chunks.remove(ChunkPos.asLong(position));
                }
                setDirty();
            }
        }

        OptionalInt findTier(ServerLevel level, Vec3 position, int radius) {
            int minimumChunkX = ((int) Math.floor(position.x - radius)) >> 4;
            int maximumChunkX = ((int) Math.floor(position.x + radius)) >> 4;
            int minimumChunkZ = ((int) Math.floor(position.z - radius)) >> 4;
            int maximumChunkZ = ((int) Math.floor(position.z + radius)) >> 4;
            double closest = radius * radius;
            long selectedPosition = Long.MAX_VALUE;
            int selectedTier = 0;
            for (int chunkX = minimumChunkX; chunkX <= maximumChunkX; chunkX++) {
                for (int chunkZ = minimumChunkZ; chunkZ <= maximumChunkZ; chunkZ++) {
                    Map<BlockPos, Integer> entries = chunks.get(ChunkPos.asLong(chunkX, chunkZ));
                    if (entries == null) {
                        continue;
                    }
                    Iterator<Map.Entry<BlockPos, Integer>> iterator = entries.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<BlockPos, Integer> entry = iterator.next();
                        BlockPos torch = entry.getKey();
                        if (level.hasChunkAt(torch)) {
                            BlockState state = level.getBlockState(torch);
                            if (!state.is(FTDungeonMobs.BLOODTHORN_BRAZIER.get())
                                    && (!state.is(FTDungeonMobs.ARENA_TORCH.get())
                                    || state.getValue(ArenaTorchBlock.HALF) != DoubleBlockHalf.LOWER)) {
                                iterator.remove();
                                setDirty();
                                continue;
                            }
                            int tier = state.getValue(BloodthornBrazierBlock.TIER);
                            if (tier == 0) {
                                iterator.remove();
                                setDirty();
                                continue;
                            }
                            if (entry.getValue() != tier) {
                                entry.setValue(tier);
                                setDirty();
                            }
                        }
                        double distance = position.distanceToSqr(torch.getX() + 0.5, torch.getY() + 1, torch.getZ() + 0.5);
                        if (distance < closest || (distance == closest && torch.asLong() < selectedPosition)) {
                            closest = distance;
                            selectedPosition = torch.asLong();
                            selectedTier = entry.getValue();
                        }
                    }
                }
            }
            return selectedTier == 0 ? OptionalInt.empty() : OptionalInt.of(selectedTier);
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            ListTag entries = new ListTag();
            chunks.values().forEach(chunk -> chunk.forEach((position, tier) -> {
                CompoundTag entry = new CompoundTag();
                entry.putLong("Position", position.asLong());
                entry.putInt("Tier", tier);
                entries.add(entry);
            }));
            tag.put("Torches", entries);
            return tag;
        }
    }
}
