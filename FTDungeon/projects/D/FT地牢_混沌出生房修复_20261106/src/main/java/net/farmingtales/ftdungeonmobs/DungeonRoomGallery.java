package net.farmingtales.ftdungeonmobs;

import com.mojang.logging.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public final class DungeonRoomGallery {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private DungeonRoomGallery() {}

    public record Report(String id, String name, String code, String detail) {
        public Component message() {
            return Component.translatable("message.ftdungeonmobs.gallery." + code, id, name, detail);
        }

        public boolean successful() {
            return code.equals("built") || code.equals("ready");
        }
    }

    public static List<Report> prepareAll(ServerLevel level, boolean statusOnly) {
        List<Report> reports = new ArrayList<>();
        try {
            for (DungeonRoomCatalog.Entry entry : DungeonRoomCatalog.entries(level.getServer().getResourceManager())) {
                reports.add(prepareOne(level, entry, statusOnly));
            }
        } catch (Exception exception) {
            reports.add(new Report("catalog", "catalog", "template", exception.toString()));
        }
        return reports;
    }

    public static Report prepareOne(ServerLevel level, DungeonRoomCatalog.Entry entry, boolean statusOnly) {
        if (!level.dimension().equals(DungeonGateTravel.DUNGEON)) {
            return new Report(entry.id(), entry.id(), "dimension", "ftdungeonmobs:dungeon");
        }
        DungeonRoomCatalog.Definition room;
        Map<BlockPos, BlockState> states;
        Map<BlockPos, BlockState> plan;
        Map<BlockPos, BlockState> paths;
        StructureTemplate template = new StructureTemplate();
        DungeonRoomsData data = DungeonRoomsData.get(level);
        try {
            room = DungeonRoomCatalog.definition(level.getServer().getResourceManager(), entry);
            String installed = data.roomFingerprint(room.id());
            if (installed != null) {
                return report(room, installed.equals(room.fingerprint()) ? "ready" : "changed", room.origin().toShortString());
            }
            if (statusOnly) {
                return report(room, "pending", room.origin().toShortString());
            }
            byte[] bytes;
            try (InputStream input = level.getServer().getResourceManager().open(new ResourceLocation("ftdungeonmobs", "structures/" + room.template().getPath() + ".nbt"))) {
                bytes = input.readAllBytes();
            }
            if (!DungeonRoomCatalog.digest(bytes).equals(room.sha256())) {
                throw new IllegalArgumentException("Template SHA-256 mismatch");
            }
            CompoundTag tag = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
            if (tag.getInt("DataVersion") != 3465) {
                throw new IllegalArgumentException("Expected Minecraft 1.20.1 DataVersion 3465");
            }
            states = DungeonRooms.readStates(new DungeonRooms.Room(room.id(), room.origin(), room.size()), tag);
            DungeonRoomLayout.validate(room, tag, states);
            paths = DungeonRoomLayout.paths(room);
            plan = DungeonRoomLayout.plan(room);
            for (Map.Entry<BlockPos, BlockState> block : states.entrySet()) {
                if (plan.containsKey(block.getKey())) {
                    throw new IllegalArgumentException("Path/foundation overlaps template: " + block.getKey().toShortString());
                }
            }
            template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        } catch (Exception exception) {
            LOGGER.warn("FT room {} rejected before construction", entry.id(), exception);
            return new Report(entry.id(), entry.id(), "template", exception.getMessage() == null ? exception.toString() : exception.getMessage());
        }
        BlockPos minimum = DungeonRoomLayout.minimum(room);
        BlockPos maximum = DungeonRoomLayout.maximum(room);
        Map<BlockPos, Integer> columns = new LinkedHashMap<>();
        paths.keySet().forEach(position -> columns.merge(new BlockPos(position.getX(), 0, position.getZ()), position.getY(), Math::max));
        List<AABB> reservations = new ArrayList<>();
        reservations.add(new AABB(minimum, maximum.offset(1, 1, 1)));
        Set<ChunkPos> chunks = new LinkedHashSet<>();
        for (BlockPos corner : List.of(minimum, maximum)) {
            if (!inBounds(level, corner)) {
                return report(room, "bounds", corner.toShortString());
            }
        }
        for (int chunkX = minimum.getX() >> 4; chunkX <= maximum.getX() >> 4; chunkX++) {
            for (int chunkZ = minimum.getZ() >> 4; chunkZ <= maximum.getZ() >> 4; chunkZ++) {
                chunks.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        for (Map.Entry<BlockPos, Integer> column : columns.entrySet()) {
            BlockPos bottom = new BlockPos(column.getKey().getX(), 63, column.getKey().getZ());
            BlockPos top = bottom.atY(column.getValue() + 3);
            if (!inBounds(level, bottom) || !inBounds(level, top)) {
                return report(room, "bounds", top.toShortString());
            }
            chunks.add(new ChunkPos(bottom));
            reservations.add(new AABB(bottom.above(), top.offset(1, 1, 1)));
        }
        chunks.forEach(chunk -> level.getChunk(chunk.x, chunk.z));
        for (BlockPos position : BlockPos.betweenClosed(minimum, maximum)) {
            if (!level.getBlockState(position).isAir() || !level.getFluidState(position).isEmpty()) {
                return report(room, "occupied", position.toShortString());
            }
        }
        for (Map.Entry<BlockPos, Integer> column : columns.entrySet()) {
            for (int blockY = 63; blockY <= column.getValue() + 3; blockY++) {
                BlockPos position = column.getKey().atY(blockY);
                BlockState current = level.getBlockState(position);
                BlockState expected = paths.get(position);
                if (!current.getFluidState().isEmpty() || !current.isAir() && (expected == null || !DungeonRooms.sameState(current, expected))) {
                    return report(room, "path", position.toShortString());
                }
            }
        }
        AABB search = reservations.stream().reduce(AABB::minmax).orElseThrow();
        for (Entity entity : level.getEntities(null, search)) {
            if (reservations.stream().anyMatch(area -> area.intersects(entity.getBoundingBox()))) {
                return report(room, "entity", entity.blockPosition().toShortString());
            }
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>(plan);
        expected.putAll(states);
        Map<BlockPos, BlockState> previous = new LinkedHashMap<>();
        expected.keySet().forEach(position -> previous.put(position, level.getBlockState(position)));
        try {
            write(level, plan);
            if (!template.placeInWorld(level, room.origin(), room.origin(), new StructurePlaceSettings().setIgnoreEntities(true)
                    .setKnownShape(true).setKeepLiquids(false), level.random, FLAGS)) {
                throw new IllegalStateException("Native placement returned false");
            }
            for (Map.Entry<BlockPos, BlockState> block : expected.entrySet()) {
                if (!DungeonRooms.sameState(level.getBlockState(block.getKey()), block.getValue())) {
                    throw new IllegalStateException("Placement mismatch at " + block.getKey() + ": expected " + block.getValue()
                            + ", actual " + level.getBlockState(block.getKey()));
                }
                if (!block.getValue().canSurvive(level, block.getKey())) {
                    throw new IllegalStateException("Unsupported template block at " + block.getKey().toShortString()
                            + " (local " + block.getKey().subtract(room.origin()).toShortString() + "): " + block.getValue()
                            + ", above=" + level.getBlockState(block.getKey().above()) + ", below=" + level.getBlockState(block.getKey().below()));
                }
            }
            data.markRoomBuilt(room.id(), room.fingerprint());
            return report(room, "built", room.origin().offset(room.entrance()).toShortString());
        } catch (Exception exception) {
            try {
                write(level, previous);
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, search)) {
                    if (reservations.stream().anyMatch(area -> area.intersects(item.getBoundingBox()))) {
                        item.discard();
                    }
                }
            } catch (Exception rollback) {
                exception.addSuppressed(rollback);
                LOGGER.error("FT room {} rollback failed", room.id(), exception);
                return report(room, "rollback_failed", room.origin().toShortString());
            }
            LOGGER.error("FT room {} placement rolled back", room.id(), exception);
            return report(room, "failed", exception.getMessage());
        }
    }

    private static Report report(DungeonRoomCatalog.Definition room, String code, String detail) {
        return new Report(room.id(), room.name(), code, detail);
    }

    private static boolean inBounds(ServerLevel level, BlockPos position) {
        return level.isInWorldBounds(position) && level.getWorldBorder().isWithinBounds(position);
    }

    private static void write(ServerLevel level, Map<BlockPos, BlockState> blocks) {
        for (Map.Entry<BlockPos, BlockState> block : blocks.entrySet()) {
            if (!DungeonRooms.sameState(level.getBlockState(block.getKey()), block.getValue())
                    && !level.setBlock(block.getKey(), block.getValue(), FLAGS)) {
                throw new IllegalStateException("Could not place " + block.getKey().toShortString());
            }
        }
    }
}
