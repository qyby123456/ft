package net.farmingtales.ftdungeonmobs;

import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public final class DungeonRooms {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PLACEMENT_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    public static final List<Room> ROOMS = List.of(
            new Room("31", new BlockPos(-33, 65, 32), new Vec3i(66, 29, 63)),
            new Room("30_1", new BlockPos(43, 65, 32), new Vec3i(16, 15, 17)));
    private static final List<Area> AREAS = List.of(
            new Area(new BlockPos(-34, 64, 31), new BlockPos(33, 93, 95)),
            new Area(new BlockPos(42, 64, 31), new BlockPos(59, 79, 49)),
            new Area(new BlockPos(-3, 64, 16), new BlockPos(54, 71, 32)));

    private DungeonRooms() {}

    public record Room(String name, BlockPos origin, Vec3i size) {}

    private record Area(BlockPos minimum, BlockPos maximum) {}

    private record LoadedRoom(Room room, StructureTemplate template, Map<BlockPos, BlockState> states) {}

    public record Result(String code, String detail) {
        public boolean successful() {
            return code.equals("built") || code.equals("ready");
        }

        public Component message() {
            return Component.translatable("message.ftdungeonmobs.rooms." + code, detail);
        }
    }

    public static Result prepare(ServerLevel level) {
        if (!level.dimension().equals(DungeonGateTravel.DUNGEON)) {
            return new Result("dimension", "");
        }
        DungeonRoomsData data = DungeonRoomsData.get(level);
        if (data.ready()) {
            return new Result("ready", "");
        }
        List<LoadedRoom> loaded = new ArrayList<>();
        try {
            for (Room room : ROOMS) {
                ResourceLocation resource = new ResourceLocation("ftdungeonmobs", "structures/rooms/" + room.name() + ".nbt");
                try (InputStream input = level.getServer().getResourceManager().open(resource)) {
                    CompoundTag tag = NbtIo.readCompressed(input);
                    Map<BlockPos, BlockState> states = readStates(room, tag);
                    StructureTemplate template = new StructureTemplate();
                    template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
                    loaded.add(new LoadedRoom(room, template, states));
                }
            }
        } catch (Exception exception) {
            LOGGER.warn("FT dungeon rooms could not load; no blocks were changed", exception);
            return new Result("template", exception.getMessage() == null ? "NBT" : exception.getMessage());
        }
        Map<BlockPos, BlockState> additions = blueprint();
        Result obstruction = preflight(level, additions);
        if (obstruction != null) {
            return obstruction;
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>(additions);
        for (LoadedRoom room : loaded) {
            for (Map.Entry<BlockPos, BlockState> entry : room.states().entrySet()) {
                if (expected.putIfAbsent(entry.getKey(), entry.getValue()) != null) {
                    return new Result("template", "Overlapping room blocks at " + entry.getKey().toShortString());
                }
            }
        }
        Map<BlockPos, BlockState> previous = new LinkedHashMap<>();
        expected.keySet().forEach(position -> previous.put(position, level.getBlockState(position)));
        try {
            writeBlocks(level, additions);
            for (LoadedRoom room : loaded) {
                StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true)
                        .setKeepLiquids(false).setKnownShape(true);
                if (!room.template().placeInWorld(level, room.room().origin(), room.room().origin(), settings,
                        level.random, PLACEMENT_FLAGS)) {
                    throw new IllegalStateException("Placement failed: " + room.room().name());
                }
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
                if (!sameState(level.getBlockState(entry.getKey()), entry.getValue())) {
                    throw new IllegalStateException("Placement mismatch at " + entry.getKey().toShortString()
                            + ": expected " + entry.getValue() + ", actual " + level.getBlockState(entry.getKey()));
                }
            }
            data.markBuilt();
            return new Result("built", "");
        } catch (Exception exception) {
            try {
                writeBlocks(level, previous);
                for (Area area : AREAS) {
                    level.getEntitiesOfClass(ItemEntity.class, new AABB(area.minimum(), area.maximum().offset(1, 1, 1)))
                            .forEach(ItemEntity::discard);
                }
            } catch (Exception rollback) {
                exception.addSuppressed(rollback);
                LOGGER.error("FT dungeon room rollback failed; administrator recovery required", exception);
                return new Result("rollback_failed", "");
            }
            LOGGER.error("FT dungeon room placement failed; original blocks restored", exception);
            return new Result("failed", "");
        }
    }

    public static Map<BlockPos, BlockState> readStates(Room room, CompoundTag tag) {
        ListTag size = tag.getList("size", Tag.TAG_INT);
        if (size.size() != 3 || size.getInt(0) != room.size().getX() || size.getInt(1) != room.size().getY()
                || size.getInt(2) != room.size().getZ() || tag.contains("palettes")
                || !tag.contains("palette", Tag.TAG_LIST) || !tag.getList("entities", Tag.TAG_COMPOUND).isEmpty()) {
            throw new IllegalArgumentException("Unexpected size/entities: " + room.name());
        }
        List<BlockState> states = new ArrayList<>();
        for (Tag value : tag.getList("palette", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            ResourceLocation identity = ResourceLocation.tryParse(entry.getString("Name"));
            if (identity == null || !BuiltInRegistries.BLOCK.containsKey(identity)) {
                throw new IllegalArgumentException("Missing block/mod: " + entry.getString("Name"));
            }
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                if (property == null || property.getValue(properties.getString(key)).isEmpty()) {
                    throw new IllegalArgumentException("Invalid property: " + identity + " " + key);
                }
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        ListTag blocks = tag.getList("blocks", Tag.TAG_COMPOUND);
        if (blocks.isEmpty() || blocks.size() > room.size().getX() * room.size().getY() * room.size().getZ()) {
            throw new IllegalArgumentException("Invalid block count: " + room.name());
        }
        for (Tag value : blocks) {
            CompoundTag entry = (CompoundTag) value;
            ListTag coordinates = entry.getList("pos", Tag.TAG_INT);
            int state = entry.getInt("state");
            if (coordinates.size() != 3 || !entry.contains("state", Tag.TAG_INT) || state < 0 || state >= states.size()) {
                throw new IllegalArgumentException("Invalid block record: " + room.name());
            }
            BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            if (local.getX() < 0 || local.getX() >= room.size().getX() || local.getY() < 0
                    || local.getY() >= room.size().getY() || local.getZ() < 0 || local.getZ() >= room.size().getZ()
                    || result.putIfAbsent(room.origin().offset(local), states.get(state)) != null) {
                throw new IllegalArgumentException("Out-of-bounds/duplicate block: " + room.name());
            }
        }
        return result;
    }

    static boolean sameState(BlockState actual, BlockState expected) {
        return actual.equals(expected) || actual.isAir() && expected.isAir();
    }

    public static Map<BlockPos, BlockState> blueprint() {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        BlockState floor = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        BlockState stairs = Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
        for (Room room : ROOMS) {
            for (int blockX = 0; blockX < room.size().getX(); blockX++) {
                for (int blockZ = 0; blockZ < room.size().getZ(); blockZ++) {
                    blocks.put(room.origin().offset(blockX, -1, blockZ), Blocks.MOSS_BLOCK.defaultBlockState());
                }
            }
        }
        for (int blockZ = 16; blockZ <= 31; blockZ++) {
            int height = blockZ < 28 ? 63 : Math.min(66, blockZ + 36);
            for (int blockX = -1; blockX <= 1; blockX++) {
                for (int blockY = 63; blockY <= height; blockY++) {
                    blocks.put(new BlockPos(blockX, blockY, blockZ), blockY == height && blockZ >= 28 && blockZ <= 30 ? stairs : floor);
                }
            }
        }
        for (int blockX = 2; blockX <= 50; blockX++) {
            for (int blockZ = 22; blockZ <= 24; blockZ++) {
                blocks.put(new BlockPos(blockX, 63, blockZ), floor);
            }
        }
        for (int blockZ = 25; blockZ <= 32; blockZ++) {
            for (int blockX = 48; blockX <= 50; blockX++) {
                blocks.put(new BlockPos(blockX, 63, blockZ), floor);
                if (blockZ >= 30) {
                    int height = blockZ == 30 ? 64 : 65;
                    for (int blockY = 64; blockY <= height; blockY++) {
                        blocks.put(new BlockPos(blockX, blockY, blockZ), blockY == height && blockZ <= 31 ? stairs : floor);
                    }
                }
            }
        }
        for (int blockX = -34; blockX <= 33; blockX++) {
            for (int blockZ = 31; blockZ <= 95; blockZ++) {
                if ((blockX == -34 || blockX == 33 || blockZ == 31 || blockZ == 95)
                        && !(blockZ == 31 && Math.abs(blockX) <= 1)) {
                    for (int blockY = 64; blockY <= 68; blockY++) {
                        blocks.put(new BlockPos(blockX, blockY, blockZ), Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
                    }
                }
            }
        }
        for (BlockPos position : List.of(new BlockPos(-3, 64, 20), new BlockPos(3, 64, 27),
                new BlockPos(46, 64, 28), new BlockPos(52, 64, 28))) {
            blocks.put(position, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
            blocks.put(position.above(), Blocks.SOUL_LANTERN.defaultBlockState());
        }
        return blocks;
    }

    private static Result preflight(ServerLevel level, Map<BlockPos, BlockState> additions) {
        Set<ChunkPos> chunks = new LinkedHashSet<>();
        for (Area area : AREAS) {
            if (!level.isInWorldBounds(area.minimum()) || !level.isInWorldBounds(area.maximum())
                    || !level.getWorldBorder().isWithinBounds(area.minimum()) || !level.getWorldBorder().isWithinBounds(area.maximum())) {
                return new Result("bounds", area.minimum().toShortString());
            }
            for (int chunkX = area.minimum().getX() >> 4; chunkX <= area.maximum().getX() >> 4; chunkX++) {
                for (int chunkZ = area.minimum().getZ() >> 4; chunkZ <= area.maximum().getZ() >> 4; chunkZ++) {
                    chunks.add(new ChunkPos(chunkX, chunkZ));
                }
            }
        }
        chunks.forEach(chunk -> level.getChunk(chunk.x, chunk.z));
        for (Area area : AREAS) {
            for (BlockPos position : BlockPos.betweenClosed(area.minimum(), area.maximum())) {
                if (!level.getBlockState(position).isAir() || !level.getFluidState(position).isEmpty()) {
                    return new Result("occupied", position.toShortString());
                }
            }
            if (!level.getEntities(null, new AABB(area.minimum(), area.maximum().offset(1, 1, 1))).isEmpty()) {
                return new Result("entity", area.minimum().toShortString());
            }
        }
        for (Map.Entry<BlockPos, BlockState> entry : additions.entrySet()) {
            BlockState current = level.getBlockState(entry.getKey());
            if (!current.isAir() && !(entry.getKey().getY() == 63 && current.equals(entry.getValue()))) {
                return new Result("occupied", entry.getKey().toShortString());
            }
        }
        return null;
    }

    private static void writeBlocks(ServerLevel level, Map<BlockPos, BlockState> blocks) {
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            if (!sameState(level.getBlockState(entry.getKey()), entry.getValue())
                    && !level.setBlock(entry.getKey(), entry.getValue(), PLACEMENT_FLAGS)) {
                throw new IllegalStateException("Could not place block at " + entry.getKey().toShortString());
            }
        }
    }
}
