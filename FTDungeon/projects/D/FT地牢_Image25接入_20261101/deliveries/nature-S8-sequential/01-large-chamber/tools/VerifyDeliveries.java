import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class VerifyDeliveries {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final class Hazards {
        private static final Set<Block> ALL = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK,
                Blocks.CACTUS, Blocks.POWDER_SNOW, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.WITHER_ROSE,
                Blocks.SWEET_BERRY_BUSH, Blocks.COBWEB);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static BlockState state(Map<BlockPos, BlockState> blocks, BlockPos position) {
        return blocks.getOrDefault(position, Blocks.AIR.defaultBlockState());
    }

    private static boolean clear(Map<BlockPos, BlockState> blocks, BlockPos position) {
        BlockState block = state(blocks, position);
        return !Hazards.ALL.contains(block.getBlock()) && block.getFluidState().isEmpty()
                && block.getCollisionShape(EmptyBlockGetter.INSTANCE, position).isEmpty();
    }

    private static boolean walkable(Map<BlockPos, BlockState> blocks, BlockPos feet) {
        BlockState floor = state(blocks, feet.below());
        return clear(blocks, feet) && clear(blocks, feet.above()) && floor.getFluidState().isEmpty()
                && !Hazards.ALL.contains(floor.getBlock()) && (floor.isFaceSturdy(EmptyBlockGetter.INSTANCE, feet.below(), Direction.UP)
                || floor.getBlock() instanceof StairBlock);
    }

    private static BlockPos coordinate(JsonObject metadata, String key) {
        return new BlockPos(metadata.getAsJsonArray(key).get(0).getAsInt(), metadata.getAsJsonArray(key).get(1).getAsInt(), metadata.getAsJsonArray(key).get(2).getAsInt());
    }

    private static int checkPath(Map<BlockPos, BlockState> blocks, int[] size, BlockPos entrance, BlockPos interior) {
        for (int offset = -1; offset <= 1; offset++) {
            BlockPos feet = entrance.offset(offset, 0, 0);
            require(walkable(blocks, feet) && clear(blocks, feet.above(2))
                    && state(blocks, feet.below()).isFaceSturdy(EmptyBlockGetter.INSTANCE, feet.below(), Direction.UP), "North entrance not a supported dry 3x3 opening: " + feet);
        }
        require(walkable(blocks, interior), "Interior point has collision or unsafe floor: " + interior);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int height : new int[]{0, -1, 1}) {
                    BlockPos next = current.relative(direction).offset(0, height, 0);
                    if (next.getX() < 0 || next.getX() >= size[0] || next.getZ() < 0 || next.getZ() >= size[2]
                            || next.getY() < 1 || next.getY() + 1 >= size[1] || visited.contains(next)) {
                        continue;
                    }
                    if (height != 0 && !(state(blocks, current.below()).getBlock() instanceof StairBlock)
                            && !(state(blocks, next.below()).getBlock() instanceof StairBlock)) {
                        continue;
                    }
                    if (height > 0 && !clear(blocks, current.above(2)) || height < 0 && !clear(blocks, next.above(2))) {
                        continue;
                    }
                    if (walkable(blocks, next)) {
                        visited.add(next);
                        pending.addLast(next);
                    }
                }
            }
        }
        require(visited.contains(interior), "No stair-compatible, dry collision-free path to interior");
        return visited.size();
    }

    private static Map<String, Object> inspect(Path folder) throws Exception {
        JsonObject metadata = JSON.fromJson(Files.readString(folder.resolve("room.json")), JsonObject.class);
        String expectedHash = metadata.get("sha256").getAsString();
        String actualHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(folder.resolve("room.nbt"))));
        require(expectedHash.equalsIgnoreCase(actualHash), "NBT fingerprint mismatch");
        CompoundTag data = NbtIo.readCompressed(folder.resolve("room.nbt").toFile());
        require(data.getInt("DataVersion") == 3465, "Expected Minecraft 1.20.1 DataVersion 3465");
        ListTag dimensions = data.getList("size", Tag.TAG_INT);
        require(dimensions.size() == 3, "Missing XYZ size");
        int[] size = {dimensions.getInt(0), dimensions.getInt(1), dimensions.getInt(2)};
        for (int axis = 0; axis < 3; axis++) {
            require(size[axis] == metadata.getAsJsonArray("size").get(axis).getAsInt(), "Metadata size mismatch");
        }
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag stateData = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(stateData.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown vanilla block " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            CompoundTag properties = stateData.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identifier + " " + key);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), stateData));
        }
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        Set<BlockPos> unique = new HashSet<>();
        Map<BlockPos, BlockState> stateMap = new LinkedHashMap<>();
        int loadedEntities = 0;
        int loadedBooks = 0;
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag record = blocks.getCompound(index);
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            require(coordinates.size() == 3, "Missing block position");
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getX() < size[0] && position.getY() >= 0 && position.getY() < size[1] && position.getZ() >= 0 && position.getZ() < size[2], "Block outside bounds");
            require(unique.add(position), "Duplicate block position");
            int stateIndex = record.getInt("state");
            require(stateIndex >= 0 && stateIndex < states.size(), "Invalid state index");
            stateMap.put(position, states.get(stateIndex));
            if (record.contains("nbt", Tag.TAG_COMPOUND)) {
                CompoundTag blockData = record.getCompound("nbt").copy();
                BlockEntity entity = BlockEntity.loadStatic(position, states.get(stateIndex), blockData);
                require(entity != null && entity.getType().isValid(states.get(stateIndex)), "Invalid block entity at " + position);
                entity.saveWithFullMetadata();
                loadedEntities++;
                if (blockData.contains("Book", Tag.TAG_COMPOUND)) {
                    ItemStack book = ItemStack.of(blockData.getCompound("Book"));
                    require(book.is(Items.WRITTEN_BOOK) && book.getCount() == 1 && WrittenBookItem.makeSureTagIsValid(book.getTag()), "Invalid written book");
                    loadedBooks++;
                }
            }
        }
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected spawned entities");
        int reachable = checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(metadata, "interior"));
        for (com.google.gson.JsonElement element : metadata.getAsJsonArray("route_targets")) {
            checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(element.getAsJsonObject(), "position"));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        CompoundTag saved = template.save(new CompoundTag());
        require(saved.getList("blocks", Tag.TAG_COMPOUND).size() == blocks.size(), "StructureTemplate roundtrip lost blocks");
        require(template.getSize().getX() == size[0] && template.getSize().getY() == size[1] && template.getSize().getZ() == size[2], "StructureTemplate dimensions mismatch");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("id", folder.getFileName().toString());
        report.put("status", "PASS");
        report.put("size", size);
        report.put("blocks", blocks.size());
        report.put("palette_states", palette.size());
        report.put("loaded_block_entities", loadedEntities);
        report.put("valid_written_books", loadedBooks);
        report.put("native_collision_path_reachable_positions", reachable);
        report.put("sha256", actualHash);
        return report;
    }

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        List<Map<String, Object>> rooms = new ArrayList<>();
        int failures = 0;
        int pending = 0;
        for (String roomId : List.of("N8_IMG01")) {
            Path folder = root.resolve("incoming").resolve(roomId);
            if (!Files.exists(folder.resolve("room.json")) || !Files.exists(folder.resolve("room.nbt"))) {
                rooms.add(Map.of("id", roomId, "status", "PENDING"));
                pending++;
                continue;
            }
            try {
                rooms.add(inspect(folder));
            } catch (Exception failure) {
                rooms.add(Map.of("id", roomId, "status", "FAIL", "error", failure.toString()));
                failures++;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", failures > 0 ? "FAIL" : pending > 0 ? "PENDING" : "PASS");
        result.put("minecraft", SharedConstants.getCurrentVersion().getName());
        result.put("rooms", rooms);
        result.put("scope", "Actual vanilla Minecraft NBT decoder, registries, block entity loading, written-book parser, and StructureTemplate roundtrip. Not a world placement or gameplay test.");
        Files.createDirectories(root.resolve("qa"));
        Files.writeString(root.resolve("qa/native-review.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
        System.out.println(new Gson().toJson(result));
        if (failures > 0) {
            throw new IllegalStateException(failures + " room(s) failed native validation");
        }
    }
}
