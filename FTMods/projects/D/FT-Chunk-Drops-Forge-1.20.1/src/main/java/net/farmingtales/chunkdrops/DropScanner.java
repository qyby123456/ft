package net.farmingtales.chunkdrops;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;

final class DropScanner {
    private static final Comparator<Drop> DROP_ORDER = Comparator.comparingInt(Drop::count).reversed()
            .thenComparing(Drop::itemId).thenComparingDouble(Drop::x).thenComparingDouble(Drop::y)
            .thenComparingDouble(Drop::z).thenComparing(Drop::uuid);
    private static final Comparator<ChunkDrops> CHUNK_ORDER = Comparator.comparingInt(ChunkDrops::entityCount)
            .reversed().thenComparing(Comparator.comparingLong(ChunkDrops::itemCount).reversed())
            .thenComparing(chunk -> chunk.key().dimension()).thenComparingInt(chunk -> chunk.key().x())
            .thenComparingInt(chunk -> chunk.key().z());

    static Snapshot scan(MinecraftServer server) {
        long started = System.nanoTime();
        Map<ChunkKey, List<Drop>> grouped = new HashMap<>();
        int dimensions = 0;
        for (ServerLevel level : server.getAllLevels()) {
            dimensions++;
            String dimension = level.dimension().location().toString();
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof ItemEntity item && isDrop(item)) {
                    ChunkPos position = item.chunkPosition();
                    ChunkKey key = new ChunkKey(dimension, position.x, position.z);
                    grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(capture(item));
                }
            }
        }
        List<ChunkDrops> chunks = grouped.entrySet().stream()
                .map(entry -> summarize(entry.getKey(), entry.getValue())).sorted(CHUNK_ORDER).toList();
        long entities = chunks.stream().mapToLong(ChunkDrops::entityCount).sum();
        long items = chunks.stream().mapToLong(ChunkDrops::itemCount).sum();
        return new Snapshot(chunks, entities, items, dimensions, System.nanoTime() - started);
    }

    static ChunkDrops scanChunk(ServerLevel level, int chunkX, int chunkZ) {
        List<Drop> drops = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof ItemEntity item && isDrop(item)) {
                ChunkPos position = item.chunkPosition();
                if (position.x == chunkX && position.z == chunkZ) {
                    drops.add(capture(item));
                }
            }
        }
        return summarize(new ChunkKey(level.dimension().location().toString(), chunkX, chunkZ), drops);
    }

    private static boolean isDrop(ItemEntity item) {
        return item.isAlive() && !item.getItem().isEmpty();
    }

    private static Drop capture(ItemEntity item) {
        return new Drop(item.getUUID(), BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString(),
                item.getItem().getHoverName().getString(), item.getItem().getCount(),
                item.getX(), item.getY(), item.getZ());
    }

    private static ChunkDrops summarize(ChunkKey key, List<Drop> drops) {
        drops.sort(DROP_ORDER);
        return new ChunkDrops(key, List.copyOf(drops), drops.stream().mapToLong(Drop::count).sum());
    }

    record ChunkKey(String dimension, int x, int z) {}

    record Drop(UUID uuid, String itemId, String name, int count, double x, double y, double z) {}

    record ChunkDrops(ChunkKey key, List<Drop> drops, long itemCount) {
        int entityCount() {
            return drops.size();
        }
    }

    record Snapshot(List<ChunkDrops> chunks, long entityCount, long itemCount, int dimensions, long elapsedNanos) {}
}
