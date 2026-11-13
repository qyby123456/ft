package net.farmingtales.stairaqa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftstairaqa")
@PrefixGameTestTemplate(false)
public final class StairAGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(23, 19, 25);
    private static final int VOLUME = 10925;
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final double RADIUS = 0.2999;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "campfire", "soul_campfire", "wither_rose", "sweet_berry_bush", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light", "end_portal", "nether_portal", "end_gateway");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }

        Vec3 world(double posX, double posY, double posZ) {
            return switch (rotation) {
                case NONE -> new Vec3(origin.getX() + posX, origin.getY() + posY, origin.getZ() + posZ);
                case CLOCKWISE_90 -> new Vec3(origin.getX() + 1 - posZ, origin.getY() + posY, origin.getZ() + posX);
                case CLOCKWISE_180 -> new Vec3(origin.getX() + 1 - posX, origin.getY() + posY, origin.getZ() + 1 - posZ);
                case COUNTERCLOCKWISE_90 -> new Vec3(origin.getX() + posZ, origin.getY() + posY, origin.getZ() + 1 - posX);
            };
        }

        BlockPos minimum() {
            BlockPos result = world(BlockPos.ZERO);
            for (BlockPos corner : List.of(new BlockPos(22, 0, 0), new BlockPos(0, 0, 24), new BlockPos(22, 0, 24))) {
                BlockPos transformed = world(corner);
                result = new BlockPos(Math.min(result.getX(), transformed.getX()), result.getY(), Math.min(result.getZ(), transformed.getZ()));
            }
            return result;
        }

        Vec3i rotatedSize() {
            return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(25, 19, 23) : SIZE;
        }
    }

    private StairAGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("ftstairaqa.root")).toAbsolutePath().normalize();
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
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown or non-vanilla block " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden block " + identity);
        CompoundTag properties = encoded.getCompound("Properties");
        require(properties.size() == block.defaultBlockState().getProperties().size(), "Incomplete state properties " + identity);
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identity + "/" + key);
        }
        BlockState result = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(result.getFluidState().isEmpty(), "Fluid or waterlogged state");
        return result;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465, "Wrong DataVersion");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Template entity present");
        require(!data.contains("palettes"), "Require a single palette");
        List<BlockState> palette = new ArrayList<>();
        for (Tag value : data.getList("palette", Tag.TAG_COMPOUND)) {
            palette.add(decode((CompoundTag) value));
        }
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        for (Tag value : data.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) value;
            ListTag encoded = record.getList("pos", Tag.TAG_INT);
            require(encoded.size() == 3, "Wrong block coordinate");
            BlockPos position = new BlockPos(encoded.getInt(0), encoded.getInt(1), encoded.getInt(2));
            require(position.getX() >= 0 && position.getX() < 23 && position.getY() >= 0 && position.getY() < 19 && position.getZ() >= 0 && position.getZ() < 25, "Out of bounds record");
            int index = record.getInt("state");
            require(index >= 0 && index < palette.size(), "Bad palette index");
            BlockState state = palette.get(index);
            require(result.put(position, state) == null, "Duplicate coordinate");
            require(state.hasBlockEntity() == record.contains("nbt"), "Missing or unexpected block entity");
            if (record.contains("nbt")) {
                CompoundTag blockData = record.getCompound("nbt");
                require(stockEmpty(blockData), "Template stock or loot");
                BlockEntity entity = BlockEntity.loadStatic(position, state, blockData);
                require(entity != null && entity.getType().isValid(state), "Invalid native block entity");
                require(!(entity instanceof Container container) || container.isEmpty(), "Nonempty source container");
            }
        }
        require(result.size() == VOLUME && metadata.getAsJsonArray("size").get(1).getAsInt() == SIZE.getY(), "Wrong full volume");
        return result;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        String identity = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return state.getFluidState().isEmpty() && !FORBIDDEN.contains(identity) && !identity.equals("cobweb") && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean fullSupport(ServerLevel level, BlockPos position) {
        return level.getBlockState(position).getFluidState().isEmpty() && level.getBlockState(position).isFaceSturdy(level, position, Direction.UP);
    }

    private static boolean attached(ServerLevel level, Fixture fixture, JsonObject entry) {
        BlockPos position = fixture.world(coordinate(entry.getAsJsonArray("position")));
        BlockPos anchor = fixture.world(coordinate(entry.getAsJsonArray("anchor")));
        Direction face = fixture.rotation().rotate(Direction.valueOf(entry.get("support_face").getAsString().toUpperCase(Locale.ROOT)));
        BlockState support = level.getBlockState(anchor);
        return anchor.relative(face).equals(position)
                && (Block.canSupportCenter(level, anchor, face) || support.is(Blocks.CHAIN))
                && BuiltInRegistries.BLOCK.getKey(level.getBlockState(position).getBlock()).getPath().equals(entry.get("block").getAsString());
    }

    private static boolean guardPresent(ServerLevel level, Fixture fixture, JsonObject entry) {
        BlockPos position = fixture.world(coordinate(entry.getAsJsonArray("position")));
        BlockPos anchor = fixture.world(coordinate(entry.getAsJsonArray("anchor")));
        return fullSupport(level, anchor) && !level.getBlockState(position).getCollisionShape(level, position).isEmpty();
    }

    private static double standingHeight(ServerLevel level, double posX, double posZ, double previousY) {
        double height = Double.NEGATIVE_INFINITY;
        for (int blockX = (int) Math.floor(posX - RADIUS); blockX <= (int) Math.floor(posX + RADIUS); blockX++) {
            for (int blockZ = (int) Math.floor(posZ - RADIUS); blockZ <= (int) Math.floor(posZ + RADIUS); blockZ++) {
                for (int blockY = (int) Math.floor(previousY - 1.1); blockY <= (int) Math.floor(previousY + 0.6); blockY++) {
                    BlockPos position = new BlockPos(blockX, blockY, blockZ);
                    BlockState state = level.getBlockState(position);
                    require(state.getFluidState().isEmpty(), "Walking probe encountered fluid");
                    for (AABB local : state.getCollisionShape(level, position).toAabbs()) {
                        AABB shape = local.move(position);
                        if (shape.maxX > posX - RADIUS + 0.00001 && shape.minX < posX + RADIUS - 0.00001
                                && shape.maxZ > posZ - RADIUS + 0.00001 && shape.minZ < posZ + RADIUS - 0.00001
                                && shape.maxY >= previousY - 0.60001 && shape.maxY <= previousY + 0.60001) {
                            height = Math.max(height, shape.maxY);
                        }
                    }
                }
            }
        }
        return height;
    }

    private static List<double[]> samples(JsonObject route, int lane, boolean reverse) {
        List<double[]> points = new ArrayList<>();
        for (JsonElement value : route.getAsJsonArray("points_xz")) {
            JsonArray point = value.getAsJsonArray();
            points.add(new double[] {point.get(0).getAsDouble() + lane, point.get(1).getAsDouble()});
        }
        if (reverse) {
            Collections.reverse(points);
        }
        List<double[]> result = new ArrayList<>();
        result.add(points.get(0));
        for (int index = 1; index < points.size(); index++) {
            double[] previous = points.get(index - 1);
            double[] next = points.get(index);
            int count = (int) Math.round((Math.abs(next[0] - previous[0]) + Math.abs(next[1] - previous[1])) * 4);
            for (int step = 1; step <= count; step++) {
                result.add(new double[] {previous[0] + (next[0] - previous[0]) * step / count, previous[1] + (next[1] - previous[1]) * step / count});
            }
        }
        return result;
    }

    private static Map<String, Object> walkRoute(ServerLevel level, Fixture fixture, JsonObject route, int lane, boolean reverse) {
        double feetY = fixture.origin().getY() + route.get(reverse ? "end_y" : "start_y").getAsDouble();
        int checked = 0;
        double maximumRise = 0;
        for (double[] point : samples(route, lane, reverse)) {
            Vec3 world = fixture.world(point[0], 0, point[1]);
            double nextY = standingHeight(level, world.x, world.z, feetY);
            require(Double.isFinite(nextY) && Math.abs(nextY - feetY) <= 0.60001, "Route loses support or requires jumping: " + route.get("id") + " at " + point[0] + "," + point[1] + " y=" + feetY + " -> " + nextY);
            AABB player = new AABB(world.x - RADIUS, nextY + 0.00001, world.z - RADIUS, world.x + RADIUS, nextY + 1.8, world.z + RADIUS);
            require(level.noCollision(null, player), "Native 0.6x1.8 collision on " + route.get("id") + " lane=" + lane + " at " + point[0] + "," + point[1] + " y=" + nextY);
            for (BlockPos position : BlockPos.betweenClosed(BlockPos.containing(player.minX, player.minY, player.minZ), BlockPos.containing(player.maxX, player.maxY, player.maxZ))) {
                require(!level.getBlockState(position).is(Blocks.COBWEB), "Route intersects cobweb");
            }
            maximumRise = Math.max(maximumRise, Math.abs(nextY - feetY));
            feetY = nextY;
            checked++;
        }
        double expectedY = fixture.origin().getY() + route.get(reverse ? "start_y" : "end_y").getAsDouble();
        require(Math.abs(feetY - expectedY) < 0.0001, "Route did not reach the declared level");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("route", route.get("id").getAsString());
        result.put("lane", lane);
        result.put("reverse", reverse);
        result.put("collision_samples", checked);
        result.put("maximum_vertical_step", maximumRise);
        result.put("status", "PASS");
        return result;
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject blueprint, boolean capture) throws Exception {
        int blockEntities = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " at " + entry.getKey() + ": " + actual);
            require(actual.canSurvive(level, position), "Unsupported native state " + entry.getKey());
            require(Block.updateFromNeighbourShapes(actual, level, position).equals(actual), "Unstable neighbor shape " + entry.getKey() + ": " + actual);
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity != null && entity.getType().isValid(actual) && stockEmpty(entity.saveWithFullMetadata()), "Missing or stocked placed block entity");
                require(!(entity instanceof Container container) || container.isEmpty(), "Placed container stocked");
                blockEntities++;
            }
        }
        for (JsonElement value : blueprint.getAsJsonArray("stair_steps")) {
            JsonObject step = value.getAsJsonObject();
            BlockPos position = fixture.world(coordinate(step.getAsJsonArray("position")));
            require(level.getBlockState(position).is(Blocks.STONE_BRICK_STAIRS) && fullSupport(level, fixture.world(coordinate(step.getAsJsonArray("anchor")))), "Unsupported tread " + step);
        }
        for (JsonElement value : blueprint.getAsJsonArray("guards")) {
            require(guardPresent(level, fixture, value.getAsJsonObject()), "Missing or unsupported safety guard " + value);
        }
        for (JsonElement value : blueprint.getAsJsonArray("supported_decor")) {
            require(attached(level, fixture, value.getAsJsonObject()), "Detached lantern or chain " + value);
        }
        for (JsonElement value : blueprint.getAsJsonArray("portals")) {
            BlockPos entrance = coordinate(value.getAsJsonObject().getAsJsonArray("feet"));
            for (int offset = -1; offset <= 1; offset++) {
                BlockPos feet = fixture.world(entrance.offset(offset, 0, 0));
                require(fullSupport(level, feet.below()) && clear(level, feet) && clear(level, feet.above()) && clear(level, feet.above(2)), "Portal is not 3x3 clear with full floors");
            }
        }
        BlockPos safe = fixture.world(coordinate(blueprint.getAsJsonObject("landing").getAsJsonArray("safe_feet")));
        require(fullSupport(level, safe.below()) && clear(level, safe) && clear(level, safe.above()), "Unsafe interior point");
        List<Map<String, Object>> routes = new ArrayList<>();
        for (JsonElement value : blueprint.getAsJsonArray("routes")) {
            for (int lane = -1; lane <= 1; lane++) {
                routes.add(walkRoute(level, fixture, value.getAsJsonObject(), lane, false));
                routes.add(walkRoute(level, fixture, value.getAsJsonObject(), lane, true));
            }
        }
        Vec3i rotatedSize = fixture.rotatedSize();
        BlockPos minimum = fixture.minimum();
        int entities = level.getEntities(null, new AABB(minimum.offset(-1, -1, -1), minimum.offset(rotatedSize.getX() + 1, 20, rotatedSize.getZ() + 1))).size();
        require(entities == 0, "Unexpected world entities or dropped blocks");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records_checked", expected.size());
        result.put("block_entities_checked", blockEntities);
        result.put("stair_cells_supported", blueprint.getAsJsonArray("stair_steps").size());
        result.put("guards_checked", blueprint.getAsJsonArray("guards").size());
        result.put("decor_supports_checked", blueprint.getAsJsonArray("supported_decor").size());
        result.put("portals_3x3_checked", 3);
        result.put("three_lane_bidirectional_routes", routes);
        result.put("unexpected_world_entities", entities);
        result.put("state_survival_neighbor_shape_collision", "PASS");
        if (capture) {
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, rotatedSize, false, null);
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(placed.save(new CompoundTag()), output.toFile());
            result.put("world_capture", output.toString());
            result.put("capture_sha256", hash(output));
        }
        return result;
    }

    private static void negativeProbes(ServerLevel level, Fixture fixture, JsonObject blueprint) {
        JsonObject step = blueprint.getAsJsonArray("stair_steps").get(0).getAsJsonObject();
        BlockPos anchor = fixture.world(coordinate(step.getAsJsonArray("anchor")));
        BlockState saved = level.getBlockState(anchor);
        level.setBlock(anchor, Blocks.AIR.defaultBlockState(), FLAGS);
        require(!fullSupport(level, anchor), "Negative missing tread support was accepted");
        level.setBlock(anchor, saved, FLAGS);
        JsonObject guard = blueprint.getAsJsonArray("guards").get(0).getAsJsonObject();
        BlockPos rail = fixture.world(coordinate(guard.getAsJsonArray("position")));
        saved = level.getBlockState(rail);
        level.setBlock(rail, Blocks.AIR.defaultBlockState(), FLAGS);
        require(!guardPresent(level, fixture, guard), "Negative missing guard was accepted");
        level.setBlock(rail, saved, FLAGS);
        JsonObject decor = blueprint.getAsJsonArray("supported_decor").get(0).getAsJsonObject();
        BlockPos bracket = fixture.world(coordinate(decor.getAsJsonArray("anchor")));
        saved = level.getBlockState(bracket);
        level.setBlock(bracket, Blocks.AIR.defaultBlockState(), FLAGS);
        require(!attached(level, fixture, decor), "Negative missing lantern support was accepted");
        level.setBlock(bracket, saved, FLAGS);
        BlockPos blocker = fixture.world(new BlockPos(11, 7, 3));
        saved = level.getBlockState(blocker);
        level.setBlock(blocker, Blocks.STONE.defaultBlockState(), FLAGS);
        boolean rejected = false;
        try {
            walkRoute(level, fixture, blueprint.getAsJsonArray("routes").get(0).getAsJsonObject(), 0, false);
        } catch (IllegalStateException expected) {
            rejected = true;
        } finally {
            level.setBlock(blocker, saved, FLAGS);
        }
        require(rejected, "Negative blocked middle aisle was accepted");
    }

    @GameTest(template = "empty", timeoutTicks = 700)
    public static void stoneBrickSwitchbackFourRotations(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("runtime", "Minecraft Java 1.20.1 / Forge 47.4.10 / isolated headless GameTest");
        ServerLevel level = helper.getLevel();
        try {
            Path source = root().resolve("room.nbt");
            String digest = hash(source);
            report.put("source_sha256", digest);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json"), StandardCharsets.UTF_8), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("qa/blueprint.json"), StandardCharsets.UTF_8), JsonObject.class);
            require(digest.equals(metadata.get("sha256").getAsString()), "Metadata hash mismatch");
            CompoundTag data = NbtIo.readCompressed(source.toFile());
            Map<BlockPos, BlockState> expected = audit(data, metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(SIZE) && template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == VOLUME, "Native template roundtrip failed");
            report.put("native_registry_properties_block_entities_template_roundtrip", "PASS");
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos origin = helper.absolutePos(new BlockPos(60 + index * 80, 30, 60));
                Fixture fixture = new Fixture(origin, rotation);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.getChunk(position);
                    level.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placeInWorld failed");
                verify(level, fixture, expected, blueprint, false);
                negativeProbes(level, fixture, blueprint);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    BlockState state = level.getBlockState(position);
                    level.setBlock(position, Block.updateFromNeighbourShapes(state, level, position), Block.UPDATE_ALL);
                    level.updateNeighborsAt(position, state.getBlock());
                }
                fixtures.add(fixture);
                index++;
            }
            report.put("negative_probes", Map.of("missing_tread_support", 4, "missing_safety_guard", 4, "missing_lantern_support", 4, "blocked_middle_aisle", 4));
            report.put("forced_chunks_and_full_neighbor_updates", "PASS");
            report.put("stabilization_ticks", 100);
            long initialTime = level.getGameTime();
            writeReport(report);
            helper.runAfterDelay(100, () -> {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        results.add(verify(level, fixture, expected, blueprint, true));
                    }
                    require(level.getGameTime() - initialTime >= 100, "Not enough stabilization ticks");
                    require(hash(source).equals(digest), "Source changed during test");
                    report.put("rotations", results);
                    report.put("records_checked_after_ticks", results.size() * expected.size());
                    report.put("elapsed_world_ticks", level.getGameTime() - initialTime);
                    report.put("status", "PASS");
                    report.put("limitations", List.of("No production FT catalog, slot, actual external corridors, mob rules, return or SavedData idempotence tested.", "Three north portals require separately reserved exterior landings and fall protection before production use.", "No full-modpack client, multiplayer, FPS or human gameplay acceptance. Captures are native world NBT, not screenshots."));
                    writeReport(report);
                    System.out.println("STAIR_A_NATIVE_GAME_TEST_PASS: rotations=4/4, records=" + results.size() * expected.size() + ", ticks=100, stair_cells=36, routes=48, source=" + digest);
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
