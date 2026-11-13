package net.farmingtales.n01qa;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftzonen01qa")
@GameTestHolder("ftzonen01qa")
@PrefixGameTestTemplate(false)
public final class N01PlacementTests {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final BlockPos SIZE = new BlockPos(25, 12, 25);
    private static final BlockPos SAFE = new BlockPos(12, 2, 9);
    private static final List<BlockPos> PORTS = List.of(new BlockPos(12, 2, 0), new BlockPos(24, 2, 12),
            new BlockPos(12, 2, 24), new BlockPos(0, 2, 12));
    private static final List<BlockPos> OUTSIDE = List.of(new BlockPos(12, 2, -4), new BlockPos(28, 2, 12),
            new BlockPos(12, 2, 28), new BlockPos(-4, 2, 12));
    private static final List<String> LABELS = List.of("N", "E", "S", "W");
    private static final List<Direction> DIRECTIONS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.WATER, Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.COBWEB,
            Blocks.POWDER_SNOW, Blocks.MAGMA_BLOCK, Blocks.CACTUS, Blocks.TNT);

    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           Map<BlockPos, CompoundTag> signs, Set<BlockPos> stubs, BlockPos minimum, BlockPos maximum) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static BlockPos transform(int blockX, int blockY, int blockZ, Fixture fixture) {
        return transform(new BlockPos(blockX, blockY, blockZ), fixture.origin, fixture.rotation);
    }

    private static void writeReport(Path root, Map<String, Object> report) throws Exception {
        Files.writeString(root.resolve("qa/forge-native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int lanterns = 0;
        int rails = 0;
        int signCount = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed " + position + ": " + entry.getValue() + " -> " + actual);
            require(level.getFluidState(position).isEmpty(), "Unexpected fluid " + position);
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native canSurvive failed " + position);
                supported++;
            }
            if (actual.is(Blocks.LANTERN)) lanterns++;
            if (actual.is(Blocks.RAIL)) rails++;
            BlockEntity entity = level.getBlockEntity(position);
            if (fixture.signs.containsKey(position)) {
                require(entity != null && entity.getType() == BlockEntityType.SIGN, "Sign block entity missing");
                CompoundTag actualSign = entity.saveWithFullMetadata();
                CompoundTag expectedSign = fixture.signs.get(position);
                require(actualSign.getBoolean("is_waxed"), "Sign lost wax");
                for (String side : List.of("front_text", "back_text")) {
                    CompoundTag actualText = actualSign.getCompound(side);
                    CompoundTag expectedText = expectedSign.getCompound(side);
                    require(actualText.getString("color").equals(expectedText.getString("color")), "Sign color changed");
                    require(actualText.getBoolean("has_glowing_text") == expectedText.getBoolean("has_glowing_text"), "Sign glow changed");
                    ListTag actualMessages = actualText.getList("messages", Tag.TAG_STRING);
                    ListTag expectedMessages = expectedText.getList("messages", Tag.TAG_STRING);
                    require(actualMessages.size() == 4 && expectedMessages.size() == 4, "Invalid sign lines");
                    for (int line = 0; line < 4; line++) {
                        Component expectedComponent = Component.Serializer.fromJson(expectedMessages.getString(line));
                        Component actualComponent = Component.Serializer.fromJson(actualMessages.getString(line));
                        require(actualComponent != null && expectedComponent != null && actualComponent.getString().equals(expectedComponent.getString()), "Sign content changed");
                    }
                    require(!actualText.toString().contains("clickEvent"), "Unexpected clickable sign");
                }
                signCount++;
            } else require(entity == null, "Unexpected block entity");
        }
        require(lanterns == 13 && rails == 4 && signCount == 12, "Feature count mismatch");
        for (BlockPos floor : fixture.stubs) require(level.getBlockState(floor).is(Blocks.STONE_BRICKS), "External connector floor changed");
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (fixture.expected.containsKey(position) || fixture.stubs.contains(position)) continue;
            require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary changed " + position);
            boundary++;
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-5, -1, -5), fixture.maximum.offset(6, 2, 6))).isEmpty(), "Unexpected entity, item, or falling block");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("origin", coordinates(fixture.origin));
        report.put("records", fixture.expected.size());
        report.put("supported_blocks", supported);
        report.put("stable_lanterns", lanterns);
        report.put("stable_rails", rails);
        report.put("preserved_sign_block_entities", signCount);
        report.put("external_stub_floor_blocks", fixture.stubs.size());
        report.put("unchanged_boundary_air", boundary);
        report.put("unwanted_entities", 0);
        report.put("spatial", spatial(level, fixture));
        return report;
    }

    private static List<Integer> coordinates(BlockPos position) {
        return List.of(position.getX(), position.getY(), position.getZ());
    }

    private static boolean reservedRoute(int blockX, int blockZ) {
        boolean north = blockX >= 11 && blockX <= 13 && blockZ <= 10;
        boolean south = blockX >= 11 && blockX <= 13 && blockZ >= 14;
        boolean west = blockZ >= 11 && blockZ <= 13 && blockX <= 10;
        boolean east = blockZ >= 11 && blockZ <= 13 && blockX >= 14;
        boolean ring = blockX >= 8 && blockX <= 16 && blockZ >= 8 && blockZ <= 16
                && !(blockX >= 11 && blockX <= 13 && blockZ >= 11 && blockZ <= 13);
        return north || south || west || east || ring;
    }

    private static BlockPos localNorth(int blockX, int blockY, int blockZ, int quarterTurns) {
        BlockPos result = new BlockPos(blockX, blockY, blockZ);
        for (int turn = 0; turn < quarterTurns; turn++) result = new BlockPos(24 - result.getZ(), result.getY(), result.getX());
        return result;
    }

    private static void clear(ServerLevel level, BlockPos position) {
        require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Clearance blocked " + position);
    }

    private static Map<String, Object> spatial(ServerLevel level, Fixture fixture) {
        int reserved = 0;
        for (int blockX = 0; blockX < 25; blockX++) {
            for (int blockZ = 0; blockZ < 25; blockZ++) {
                for (int blockY : List.of(0, 1, 11)) {
                    BlockPos position = transform(blockX, blockY, blockZ, fixture);
                    require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Floor/roof hole " + position);
                }
                if (!reservedRoute(blockX, blockZ)) continue;
                for (int blockY = 2; blockY <= 6; blockY++) clear(level, transform(blockX, blockY, blockZ, fixture));
                reserved++;
            }
        }
        require(reserved == 168, "Protected route area changed");
        for (int portIndex = 0; portIndex < 4; portIndex++) {
            for (int lateral = 10; lateral <= 14; lateral++) {
                for (int depth = -4; depth <= 2; depth++) {
                    for (int blockY = 2; blockY <= 6; blockY++) {
                        BlockPos local = localNorth(lateral, blockY, depth, portIndex);
                        clear(level, transform(local, fixture.origin, fixture.rotation));
                    }
                }
            }
        }
        for (int blockX = 11; blockX <= 13; blockX++) {
            for (int blockZ = 8; blockZ <= 10; blockZ++) {
                for (int blockY = 2; blockY <= 6; blockY++) clear(level, transform(blockX, blockY, blockZ, fixture));
            }
        }
        for (BlockPos corner : List.of(new BlockPos(4, 0, 4), new BlockPos(19, 0, 4), new BlockPos(4, 0, 19), new BlockPos(19, 0, 19))) {
            for (int blockX = corner.getX(); blockX <= corner.getX() + 1; blockX++) {
                for (int blockZ = corner.getZ(); blockZ <= corner.getZ() + 1; blockZ++) {
                    for (int blockY = 1; blockY <= 11; blockY++) {
                        BlockPos position = transform(blockX, blockY, blockZ, fixture);
                        require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Pillar not continuous");
                    }
                }
            }
        }
        Set<BlockPos> walkable = new HashSet<>();
        for (int blockX = -4; blockX <= 28; blockX++) {
            for (int blockZ = -4; blockZ <= 28; blockZ++) {
                BlockPos position = transform(blockX, 2, blockZ, fixture);
                BlockPos below = position.below();
                if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
                AABB player = new AABB(position.getX() + 0.2, position.getY() + 0.01, position.getZ() + 0.2,
                        position.getX() + 0.8, position.getY() + 1.8, position.getZ() + 0.8);
                if (level.noCollision(player) && level.getFluidState(position).isEmpty()) walkable.add(position);
            }
        }
        BlockPos safe = transform(SAFE, fixture.origin, fixture.rotation);
        require(walkable.contains(safe), "Unsafe center point");
        List<Map<String, Object>> paths = new ArrayList<>();
        List<Map<String, Object>> ports = new ArrayList<>();
        for (int startIndex = 0; startIndex < 4; startIndex++) {
            BlockPos start = transform(OUTSIDE.get(startIndex), fixture.origin, fixture.rotation);
            require(walkable.contains(start), "Unsafe outside endpoint");
            Map<BlockPos, BlockPos> parents = breadthFirst(walkable, start);
            require(parents.containsKey(safe), "External port cannot reach safe point");
            BlockPos port = transform(PORTS.get(startIndex), fixture.origin, fixture.rotation);
            require(parents.containsKey(port), "External-to-internal seam disconnected");
            ports.add(Map.of("local_id", LABELS.get(startIndex), "world_direction", fixture.rotation.rotate(DIRECTIONS.get(startIndex)).getName(),
                    "world_entrance", coordinates(port), "world_outside_endpoint", coordinates(start), "width", 5, "height", 5));
            for (int finishIndex = 0; finishIndex < 4; finishIndex++) {
                if (startIndex == finishIndex) continue;
                BlockPos finish = transform(OUTSIDE.get(finishIndex), fixture.origin, fixture.rotation);
                require(parents.containsKey(finish), "Port pair disconnected");
                List<List<Integer>> path = new ArrayList<>();
                for (BlockPos cursor = finish; cursor != null; cursor = parents.get(cursor)) path.add(coordinates(cursor));
                Collections.reverse(path);
                paths.add(Map.of("from", LABELS.get(startIndex), "to", LABELS.get(finishIndex), "steps", path.size() - 1, "world_path", path));
            }
        }
        require(paths.size() == 12, "Expected all 12 directed port pairs");
        return Map.of("native_collision_player_cells", walkable.size(), "protected_route_cells", reserved,
                "route_width", 3, "minimum_clearance", 5, "center_safe_world", coordinates(safe),
                "safe_area", "3 x 3 with 5-block clearance", "ports", ports, "ordered_port_pairs", paths.size(), "paths", paths);
    }

    private static Map<BlockPos, BlockPos> breadthFirst(Set<BlockPos> route, BlockPos start) {
        Map<BlockPos, BlockPos> parents = new LinkedHashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        parents.put(start, null);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos position = queue.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = position.relative(direction);
                if (route.contains(neighbor) && !parents.containsKey(neighbor)) {
                    parents.put(neighbor, position);
                    queue.add(neighbor);
                }
            }
        }
        return parents;
    }

    private static List<String> negativeProbes(ServerLevel level, Fixture fixture) {
        List<String> rejected = new ArrayList<>();
        probe(level, transform(12, 1, 5, fixture), Blocks.AIR.defaultBlockState(), () -> spatial(level, fixture), "missing_route_floor", rejected);
        probe(level, transform(12, 2, 0, fixture), Blocks.STONE.defaultBlockState(), () -> spatial(level, fixture), "blocked_port", rejected);
        probe(level, transform(9, 6, 12, fixture), Blocks.STONE.defaultBlockState(), () -> spatial(level, fixture), "ring_headroom_loss", rejected);
        probe(level, transform(12, 3, 9, fixture), Blocks.STONE.defaultBlockState(), () -> spatial(level, fixture), "unsafe_center", rejected);
        probe(level, transform(12, 3, 12, fixture), Blocks.AIR.defaultBlockState(), () -> {
            BlockPos lantern = transform(12, 4, 12, fixture);
            require(level.getBlockState(lantern).canSurvive(level, lantern), "Standing lantern lost support");
        }, "standing_lantern_support_loss", rejected);
        probe(level, transform(7, 8, 7, fixture), Blocks.AIR.defaultBlockState(), () -> {
            BlockPos lantern = transform(7, 7, 7, fixture);
            require(level.getBlockState(lantern).canSurvive(level, lantern), "Hanging lantern lost support");
        }, "hanging_lantern_support_loss", rejected);
        probe(level, transform(12, 8, 1, fixture), Blocks.AIR.defaultBlockState(), () -> {
            BlockPos sign = transform(12, 8, 2, fixture);
            require(level.getBlockState(sign).canSurvive(level, sign), "Sign lost backing");
        }, "sign_backing_loss", rejected);
        require(rejected.size() == 7, "Negative probe missed a defect");
        return rejected;
    }

    private static void probe(ServerLevel level, BlockPos position, BlockState replacement, Runnable validation,
                              String name, List<String> rejected) {
        BlockState previous = level.getBlockState(position);
        try {
            level.setBlock(position, replacement, FLAGS);
            boolean detected = false;
            try { validation.run(); }
            catch (IllegalStateException expected) { detected = true; }
            require(detected, "Negative probe was not rejected: " + name);
            rejected.add(name);
        } finally { level.setBlock(position, previous, FLAGS); }
    }

    @GameTest(template = "empty", batch = "n01_native", timeoutTicks = 950)
    public static void fourRotationsStableConnected(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("n01.delivery"));
        require(root.getFileName().toString().equals("zone-N01"), "Unexpected QA root");
        Path source = root.resolve("room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        writeReport(root, Map.of("status", "RUNNING", "sha256", hash));
        try {
            CompoundTag data = NbtIo.readCompressed(source.toFile());
            require(JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject().get("sha256").getAsString().equals(hash), "Metadata fingerprint mismatch");
            require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected 1.20.1 single palette");
            require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Template entities are forbidden");
            ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
            List<BlockState> states = new ArrayList<>();
            for (int stateIndex = 0; stateIndex < palette.size(); stateIndex++) {
                CompoundTag entry = palette.getCompound(stateIndex);
                ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
                require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown block " + identity);
                Block block = BuiltInRegistries.BLOCK.get(identity);
                CompoundTag properties = entry.getCompound("Properties");
                for (String key : properties.getAllKeys()) {
                    Property<?> property = block.getStateDefinition().getProperty(key);
                    require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + entry);
                }
                BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
                require(!FORBIDDEN.contains(block), "Forbidden block " + state);
                states.add(state);
            }
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(SIZE), "Size mismatch");
            ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
            require(records.size() == 7500, "Expected full explicit volume");
            CompoundTag roundtrip = template.save(new CompoundTag());
            require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 7500, "Native roundtrip record loss");
            List<Fixture> fixtures = new ArrayList<>();
            for (Rotation rotation : Rotation.values()) {
                BlockPos origin = new BlockPos(1024 + rotation.ordinal() * 96, 100, 1024);
                Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
                Map<BlockPos, CompoundTag> signs = new LinkedHashMap<>();
                for (int recordIndex = 0; recordIndex < records.size(); recordIndex++) {
                    CompoundTag record = records.getCompound(recordIndex);
                    ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                    BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                    require(local.getX() >= 0 && local.getX() < 25 && local.getY() >= 0 && local.getY() < 12 && local.getZ() >= 0 && local.getZ() < 25, "Out of bounds");
                    BlockPos world = transform(local, origin, rotation);
                    BlockState state = states.get(record.getInt("state")).rotate(rotation);
                    require(expected.put(world, state) == null, "Duplicate record");
                    if (record.contains("nbt")) {
                        CompoundTag signData = record.getCompound("nbt");
                        require(state.is(Blocks.SPRUCE_WALL_SIGN) && signData.getString("id").equals("minecraft:sign"), "Unexpected block entity");
                        require(signData.getBoolean("is_waxed"), "Unwaxed sign");
                        require(!signData.toString().contains("clickEvent") && !signData.toString().contains("run_command"), "Active sign event");
                        signs.put(world, signData);
                    }
                }
                require(signs.size() == 12, "Sign count mismatch");
                BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
                BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), 100, Math.min(origin.getZ(), corner.getZ()));
                BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), 111, Math.max(origin.getZ(), corner.getZ()));
                for (int chunkX = (minimum.getX() - 5) >> 4; chunkX <= (maximum.getX() + 5) >> 4; chunkX++) {
                    for (int chunkZ = (minimum.getZ() - 5) >> 4; chunkZ <= (maximum.getZ() + 5) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
                }
                for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-5, -1, -5), maximum.offset(5, 1, 5))) {
                    require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Occupied isolated fixture " + position);
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true).setFinalizeEntities(false);
                require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placement failed");
                Set<BlockPos> stubs = new HashSet<>();
                for (int distance = 1; distance <= 4; distance++) {
                    for (int lateral = 10; lateral <= 14; lateral++) {
                        for (BlockPos local : List.of(new BlockPos(lateral, 1, -distance), new BlockPos(24 + distance, 1, lateral), new BlockPos(lateral, 1, 24 + distance), new BlockPos(-distance, 1, lateral))) {
                            BlockPos world = transform(local, origin, rotation);
                            level.setBlock(world, Blocks.STONE_BRICKS.defaultBlockState(), FLAGS);
                            stubs.add(world);
                        }
                    }
                }
                fixtures.add(new Fixture(rotation, origin, expected, signs, stubs, minimum, maximum));
            }
            level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            level.setWeatherParameters(12000, 0, false, false);
            List<String> negatives = negativeProbes(level, fixtures.get(0));
            List<Map<String, Object>> initial = new ArrayList<>();
            for (Fixture fixture : fixtures) {
                initial.add(inspect(level, fixture));
                for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
                System.out.println("N01_NATIVE_INITIAL_PASS: " + fixture.rotation + "; records=7500; connected_pairs=12");
            }
            helper.runAfterDelay(400, () -> {
                try {
                    List<Map<String, Object>> settled = new ArrayList<>();
                    for (Fixture fixture : fixtures) settled.add(inspect(level, fixture));
                    Map<String, Object> report = new LinkedHashMap<>();
                    report.put("status", "PASS");
                    report.put("sha256", hash);
                    report.put("minecraft", "1.20.1");
                    report.put("forge", "47.4.10");
                    report.put("evidence", "Isolated GameTestServer / ServerLevel / StructureTemplate.placeInWorld / native collision shapes");
                    report.put("rotations", 4);
                    report.put("template_records_per_phase", 30000);
                    report.put("stability_ticks", 400);
                    report.put("initial", initial);
                    report.put("after_400_ticks", settled);
                    report.put("negative_probes_rejected", negatives);
                    report.put("neighbor_updates_dispatched", true);
                    report.put("random_tick_speed", 3);
                    report.put("fire_tick_enabled", true);
                    report.put("four_external_test_stubs_per_rotation", "5 wide x 4 long; floor-only fixture, excluded from delivered NBT");
                    report.put("production_save_used", false);
                    report.put("production_mods_modified", false);
                    report.put("full_modpack_client_playtest", false);
                    writeReport(root, report);
                    System.out.println("N01_NATIVE_GAME_TEST_PASS: rotations=4/4; records=30000; directed_paths=48; ticks=400; sha256=" + hash);
                    helper.succeed();
                } catch (Exception exception) {
                    try { writeReport(root, Map.of("status", "FAIL", "sha256", hash, "error", exception.toString())); }
                    catch (Exception reportError) { exception.addSuppressed(reportError); }
                    throw new IllegalStateException("N01 post-tick verification failed", exception);
                }
            });
        } catch (Exception exception) {
            writeReport(root, Map.of("status", "FAIL", "sha256", hash, "error", exception.toString()));
            throw exception;
        }
    }
}
