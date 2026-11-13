package net.farmingtales.shopcqa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftshopcqa")
@PrefixGameTestTemplate(false)
public final class ShopCGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(13, 9, 13);
    private static final int VOLUME = 1521;
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "campfire", "soul_campfire", "wither_rose", "sweet_berry_bush", "cobweb", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light", "end_portal", "nether_portal", "end_gateway");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private ShopCGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("ftshopcqa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static BlockPos coordinate(JsonArray encoded) {
        require(encoded.size() == 3, "Expected XYZ coordinate");
        return new BlockPos(encoded.get(0).getAsInt(), encoded.get(1).getAsInt(), encoded.get(2).getAsInt());
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    private static boolean stockEmpty(Tag data) {
        if (data instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                Tag child = compound.get(key);
                if (Set.of("LootTable", "LootTableSeed", "Book", "RecordItem").contains(key)) {
                    return false;
                }
                if ((key.equals("Items") || key.equals("Item")) && (!(child instanceof ListTag list) || !list.isEmpty())) {
                    return false;
                }
                if (!stockEmpty(child)) {
                    return false;
                }
            }
        } else if (data instanceof ListTag list) {
            for (Tag child : list) {
                if (!stockEmpty(child)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla block: " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden block: " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid native property: " + identity + "/" + key);
        }
        BlockState decoded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(decoded.getFluidState().isEmpty(), "Fluid or waterlogged state forbidden");
        return decoded;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require 1.20.1 single palette");
        ListTag size = data.getList("size", Tag.TAG_INT);
        require(size.size() == 3 && size.getInt(0) == 13 && size.getInt(1) == 9 && size.getInt(2) == 13, "Wrong dimensions");
        require(coordinate(metadata.getAsJsonArray("size")).equals(new BlockPos(SIZE)), "Metadata size differs");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "No preplaced entities");
        List<BlockState> palette = new ArrayList<>();
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedPalette.size(); index++) {
            palette.add(decode(encodedPalette.getCompound(index)));
        }
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag entry = blocks.getCompound(index);
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            require(position.size() == 3, "Invalid position");
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 13 && relative.getY() >= 0 && relative.getY() < 9 && relative.getZ() >= 0 && relative.getZ() < 13, "Out of bounds");
            int stateIndex = entry.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Invalid palette index");
            BlockState state = palette.get(stateIndex);
            require(result.put(relative, state) == null, "Duplicate position");
            require(state.hasBlockEntity() == entry.contains("nbt", Tag.TAG_COMPOUND), "Block entity payload differs at " + relative);
            if (state.hasBlockEntity()) {
                CompoundTag blockData = entry.getCompound("nbt");
                require(stockEmpty(blockData), "Template inventory or loot found");
                BlockEntity entity = BlockEntity.loadStatic(relative, state, blockData.copy());
                require(entity != null && entity.getType().isValid(state), "Invalid native block entity");
                require(!(entity instanceof Container container) || container.isEmpty(), "Nonempty container");
            }
        }
        require(result.size() == VOLUME, "Require full explicit volume");
        return result;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getFluidState().isEmpty() && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath())
                && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState floor = level.getBlockState(below);
        return clear(level, feet) && clear(level, feet.above()) && floor.getFluidState().isEmpty()
                && floor.isFaceSturdy(level, below, Direction.UP) && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(floor.getBlock()).getPath());
    }

    private static boolean supported(ServerLevel level, Fixture fixture, JsonObject entry) {
        BlockPos position = fixture.world(coordinate(entry.getAsJsonArray("position")));
        BlockPos anchor = fixture.world(coordinate(entry.getAsJsonArray("anchor")));
        Direction face = fixture.rotation().rotate(Direction.valueOf(entry.get("support_face").getAsString().toUpperCase(Locale.ROOT)));
        return anchor.relative(face).equals(position) && level.getBlockState(anchor).isFaceSturdy(level, anchor, face)
                && BuiltInRegistries.BLOCK.getKey(level.getBlockState(position).getBlock()).getPath().equals(entry.get("block").getAsString());
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected,
            JsonObject metadata, JsonObject blueprint, boolean capture) throws Exception {
        int blockEntities = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " at " + entry.getKey() + ": " + actual);
            require(actual.canSurvive(level, position), "Unsupported native state at " + entry.getKey());
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity != null && entity.getType().isValid(actual), "Missing native block entity");
                require(stockEmpty(entity.saveWithFullMetadata()), "Placed inventory or loot found");
                require(!(entity instanceof Container container) || container.isEmpty(), "Placed container has stock");
                blockEntities++;
            }
        }
        for (JsonElement entry : blueprint.getAsJsonArray("supported_decor")) {
            require(supported(level, fixture, entry.getAsJsonObject()), "Missing explicit support for decor " + entry);
        }
        for (JsonElement value : blueprint.getAsJsonArray("shelf_wall_connections")) {
            BlockPos anchor = fixture.world(coordinate(value.getAsJsonObject().getAsJsonArray("anchor")));
            require(level.getBlockState(anchor).isFaceSturdy(level, anchor, fixture.rotation().rotate(Direction.NORTH)), "Detached shelf slab");
        }
        for (String name : List.of("entrance_to_counter_aisle", "counter_clearance")) {
            for (JsonElement position : blueprint.getAsJsonArray(name)) {
                require(clear(level, fixture.world(coordinate(position.getAsJsonArray()))), "Aisle or counter clearance blocked");
            }
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        BlockPos entrance = coordinate(metadata.getAsJsonArray("entrance"));
        require(walkable(level, fixture.world(entrance)), "Entrance blocked");
        reached.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() >= 0 && next.getX() < 13 && next.getZ() >= 0 && next.getZ() < 13
                        && !reached.contains(next) && walkable(level, fixture.world(next))) {
                    reached.add(next);
                    pending.addLast(next);
                }
            }
        }
        require(reached.contains(coordinate(metadata.getAsJsonArray("interior"))), "Interior unreachable");
        for (JsonElement position : blueprint.getAsJsonArray("required_walkable")) {
            require(reached.contains(coordinate(position.getAsJsonArray())), "Unreachable service point");
        }
        for (int offset = -1; offset <= 1; offset++) {
            BlockPos feet = entrance.offset(offset, 0, 0);
            require(walkable(level, fixture.world(feet)) && clear(level, fixture.world(feet.above(2))), "Entry not 3x3 clear");
        }
        BlockPos chest = fixture.world(new BlockPos(6, 1, 11));
        require(level.getBlockState(chest).is(Blocks.ENDER_CHEST) && clear(level, chest.above()), "Ender chest opening obstructed");
        BlockPos minimum = fixture.world(BlockPos.ZERO);
        for (BlockPos corner : List.of(new BlockPos(12, 0, 0), new BlockPos(0, 0, 12), new BlockPos(12, 0, 12))) {
            BlockPos transformed = fixture.world(corner);
            minimum = new BlockPos(Math.min(minimum.getX(), transformed.getX()), minimum.getY(), Math.min(minimum.getZ(), transformed.getZ()));
        }
        int worldEntities = level.getEntities(null, new AABB(minimum.offset(-1, -1, -1), minimum.offset(14, 10, 14))).size();
        require(worldEntities == 0, "Unexpected entities or drops");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records_checked", expected.size());
        result.put("block_entities_checked", blockEntities);
        result.put("reachable_foot_positions", reached.size());
        result.put("explicit_decor_supports_checked", blueprint.getAsJsonArray("supported_decor").size());
        result.put("unexpected_world_entities", worldEntities);
        result.put("state_collision_survival_inventory", "PASS");
        if (capture) {
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, SIZE, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, output.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(output.toFile()));
            require(reloaded.getSize().equals(SIZE) && saved.getList("blocks", Tag.TAG_COMPOUND).size() == VOLUME, "Capture roundtrip failed");
            result.put("world_capture", root().relativize(output).toString());
            result.put("world_capture_sha256", hash(output));
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void shopCEndVoidFourRotations(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("java", System.getProperty("java.version"));
        report.put("scope", "Isolated headless Forge GameTestServer; not FT core integration or a player save");
        writeReport(report);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "World writes restricted to isolated GameTestServer");
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("out/room.json")), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("out/blueprint.json")), JsonObject.class);
            String digest = hash(root().resolve("out/room.nbt"));
            require(digest.equals(metadata.get("sha256").getAsString()), "Source hash mismatch");
            report.put("structure_sha256", digest);
            CompoundTag data = NbtIo.readCompressed(root().resolve("out/room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(data, metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(SIZE) && template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == VOLUME, "Native template roundtrip failed");
            report.put("native_registry_properties_block_entities_template_roundtrip", "PASS");
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos origin = helper.absolutePos(new BlockPos(40 + index * 40, 20, 40));
                Fixture fixture = new Fixture(origin, rotation);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placeInWorld failed");
                verify(level, fixture, expected, metadata, blueprint, false);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                JsonObject support = blueprint.getAsJsonArray("supported_decor").get(0).getAsJsonObject();
                BlockPos anchor = fixture.world(coordinate(support.getAsJsonArray("anchor")));
                BlockState saved = level.getBlockState(anchor);
                level.setBlock(anchor, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!supported(level, fixture, support), "Missing end rod support negative probe failed");
                level.setBlock(anchor, saved, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos aisle = fixture.world(new BlockPos(6, 1, 2));
                level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS);
                require(!walkable(level, aisle), "Blocked aisle negative probe failed");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS);
                fixtures.add(fixture);
                index++;
            }
            report.put("negative_missing_rod_support_probes", 4);
            report.put("negative_blocked_aisle_probes", 4);
            report.put("stabilization_ticks", 100);
            writeReport(report);
            helper.runAfterDelay(100, () -> {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        results.add(verify(level, fixture, expected, metadata, blueprint, true));
                    }
                    require(hash(root().resolve("out/room.nbt")).equals(digest), "Source changed during test");
                    report.put("rotations", results);
                    report.put("records_checked_after_ticks", results.size() * expected.size());
                    report.put("status", "PASS");
                    report.put("limitations", List.of("No FT catalog, actual gallery route, production idempotence or deployment tested.", "No full modpack client, multiplayer, FPS, NPC or trade behavior tested.", "World captures are machine evidence, not client screenshots; ender chest personal contents are outside template inventory."));
                    writeReport(report);
                    System.out.println("SHOP_C_NATIVE_GAME_TEST_PASS: rotations=4/4, records=" + results.size() * expected.size() + ", ticks=100, template_stock=0, source=" + digest);
                    helper.succeed();
                } catch (Exception exception) {
                    report.put("status", "FAIL");
                    report.put("error", exception.toString());
                    try {
                        writeReport(report);
                    } catch (Exception secondary) {
                        exception.addSuppressed(secondary);
                    }
                    throw new IllegalStateException(exception);
                }
            });
        } catch (Exception exception) {
            report.put("status", "FAIL");
            report.put("error", exception.toString());
            writeReport(report);
            throw exception;
        }
    }
}
