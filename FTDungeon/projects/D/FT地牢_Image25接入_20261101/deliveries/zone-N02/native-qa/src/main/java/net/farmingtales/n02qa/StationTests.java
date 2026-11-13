package net.farmingtales.n02qa;

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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftn02qa")
@PrefixGameTestTemplate(false)
public final class StationTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final int COUNT = 33 * 15 * 31;
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "spawner", "tnt", "respawn_anchor", "powered_rail", "detector_rail", "activator_rail", "lava", "water", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "cobweb");
    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) { return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO)); }
        BlockPos minimum() {
            BlockPos minimum = world(BlockPos.ZERO);
            for (BlockPos corner : List.of(new BlockPos(32, 0, 0), new BlockPos(0, 0, 30), new BlockPos(32, 0, 30))) {
                BlockPos point = world(corner);
                minimum = new BlockPos(Math.min(minimum.getX(), point.getX()), origin.getY(), Math.min(minimum.getZ(), point.getZ()));
            }
            return minimum;
        }
        Vec3i size() { return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(31, 15, 33) : new Vec3i(33, 15, 31); }
    }
    private static Path root() { return Path.of(System.getProperty("ftn02qa.root")).toAbsolutePath().normalize(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static String hash(Path file) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))); }
    private static void report(Map<String, Object> result) throws Exception { Files.writeString(root().resolve("qa/native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8); }
    private static BlockPos coordinate(ListTag value) { require(value.size() == 3, "Invalid coordinate"); return new BlockPos(value.getInt(0), value.getInt(1), value.getInt(2)); }
    private static BlockPos coordinate(JsonElement value) { JsonArray tuple = value.getAsJsonArray(); return new BlockPos(tuple.get(0).getAsInt(), tuple.get(1).getAsInt(), tuple.get(2).getAsInt()); }
    private static boolean inside(BlockPos position, int margin) { return position.getX() >= -margin && position.getX() < 33 + margin && position.getY() >= 0 && position.getY() < 15 && position.getZ() >= -margin && position.getZ() < 31 + margin; }
    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity) && !FORBIDDEN.contains(identity.getPath()), "Invalid vanilla ID " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identity + " " + key);
        }
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
    }
    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && coordinate(data.getList("size", Tag.TAG_INT)).equals(new BlockPos(33, 15, 31)), "Version or size differs");
        require(!data.contains("palettes") && data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected palettes/entities");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> decoded = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) decoded.add(decode(palette.getCompound(index)));
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag entry = blocks.getCompound(index);
            BlockPos position = coordinate(entry.getList("pos", Tag.TAG_INT));
            int state = entry.getInt("state");
            require(inside(position, 0) && state >= 0 && state < decoded.size(), "Invalid voxel");
            require(!entry.contains("nbt") && !decoded.get(state).hasBlockEntity(), "Unexpected inventory or block entity");
            require(result.put(position, decoded.get(state)) == null, "Duplicate voxel");
        }
        require(result.size() == COUNT, "Incomplete explicit volume");
        return result;
    }
    private static boolean walkable(ServerLevel level, BlockPos position) {
        BlockState floor = level.getBlockState(position.below());
        if (!floor.getFluidState().isEmpty() || !floor.isFaceSturdy(level, position.below(), Direction.UP)) return false;
        for (int height = 0; height < 3; height++) {
            BlockPos cell = position.above(height);
            BlockState state = level.getBlockState(cell);
            if (state.is(Blocks.RAIL) || !state.getFluidState().isEmpty() || !state.getCollisionShape(level, cell).isEmpty()) return false;
        }
        return true;
    }
    private static boolean emptyVolume(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected) {
        for (BlockPos relative : expected.keySet()) if (!level.getBlockState(fixture.world(relative)).isAir()) return false;
        return true;
    }
    private static void attachTestStubs(ServerLevel level, Fixture fixture, JsonObject metadata) {
        for (JsonElement element : metadata.getAsJsonArray("walking_ports")) {
            JsonObject port = element.getAsJsonObject();
            BlockPos center = coordinate(port.get("feet"));
            Direction direction = Direction.byName(port.get("facing").getAsString());
            Direction cross = direction.getClockWise();
            for (int distance = 1; distance <= 2; distance++) for (int width = -2; width <= 2; width++) {
                BlockPos relative = center.relative(direction, distance).relative(cross, width);
                level.setBlock(fixture.world(relative.below()), Blocks.POLISHED_ANDESITE.defaultBlockState(), FLAGS);
            }
        }
        for (JsonElement element : metadata.getAsJsonArray("rail_ports")) {
            JsonObject port = element.getAsJsonObject();
            BlockPos center = coordinate(port.get("rail"));
            Direction direction = Direction.byName(port.get("facing").getAsString());
            BlockState state = level.getBlockState(fixture.world(center));
            for (int distance = 1; distance <= 2; distance++) {
                BlockPos position = fixture.world(center.relative(direction, distance));
                level.setBlock(position.below(), Blocks.POLISHED_ANDESITE.defaultBlockState(), FLAGS);
                level.setBlock(position, state, FLAGS);
            }
        }
    }
    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject metadata, boolean capture) throws Exception {
        int rails = 0;
        int lamps = 0;
        int supports = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "Voxel differs " + fixture.rotation() + " " + entry.getKey() + " expected=" + entry.getValue().rotate(fixture.rotation()) + " actual=" + actual);
            require(actual.canSurvive(level, position), "Unsupported block " + position);
            require(actual.getFluidState().isEmpty(), "Unexpected fluid");
            if (actual.is(Blocks.RAIL)) {
                rails++;
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Unsupported track");
            }
            if (actual.is(Blocks.LANTERN)) {
                lamps++;
                BlockPos chain = position.above();
                while (chain.getY() < fixture.origin().getY() + 13) {
                    require(level.getBlockState(chain).is(Blocks.CHAIN), "Broken chain");
                    chain = chain.above();
                }
                require(level.getBlockState(chain).isFaceSturdy(level, chain, Direction.DOWN), "Unanchored chain");
            }
        }
        require(rails == 73 && lamps == 7, "Rail/lamp count differs");
        for (int east = 0; east < 33; east++) for (int south = 0; south < 31; south++) require(level.getBlockState(fixture.world(new BlockPos(east, 13, south))).is(Blocks.STONE_BRICKS), "Roof hole");
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        BlockPos entrance = coordinate(metadata.get("entrance"));
        reached.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (inside(next, 2) && !reached.contains(next) && walkable(level, fixture.world(next))) { reached.add(next); pending.add(next); }
            }
        }
        for (int east = 14; east <= 18; east++) for (int south = 0; south < 31; south++) require(reached.contains(new BlockPos(east, 2, south)), "North-south public route blocked");
        for (int east = 16; east <= 32; east++) for (int south = 21; south <= 25; south++) require(reached.contains(new BlockPos(east, 2, south)), "East public route blocked");
        for (JsonElement element : metadata.getAsJsonArray("safe_points")) require(reached.contains(coordinate(element)), "Safe point unreachable");
        for (JsonElement element : metadata.getAsJsonArray("walking_ports")) {
            JsonObject port = element.getAsJsonObject();
            BlockPos center = coordinate(port.get("feet"));
            Direction direction = Direction.byName(port.get("facing").getAsString());
            for (int distance = 1; distance <= 2; distance++) require(reached.contains(center.relative(direction, distance)), "External walking attachment unreachable");
        }
        for (JsonElement element : metadata.getAsJsonArray("rail_ports")) {
            JsonObject port = element.getAsJsonObject();
            BlockPos center = coordinate(port.get("rail"));
            Direction direction = Direction.byName(port.get("facing").getAsString());
            BlockState boundary = level.getBlockState(fixture.world(center));
            for (int distance = 1; distance <= 2; distance++) {
                BlockPos position = fixture.world(center.relative(direction, distance));
                require(level.getBlockState(position).equals(boundary) && level.getBlockState(position).canSurvive(level, position), "External rail attachment differs");
                supports++;
            }
        }
        BlockPos minimum = fixture.minimum();
        Vec3i size = fixture.size();
        int haloFluids = 0;
        for (int east = -2; east <= size.getX() + 1; east++) for (int up = -1; up <= size.getY(); up++) for (int south = -2; south <= size.getZ() + 1; south++) if (!level.getFluidState(minimum.offset(east, up, south)).isEmpty()) haloFluids++;
        var entities = level.getEntities(null, new AABB(minimum.offset(-3, -1, -3), minimum.offset(size.getX() + 3, size.getY() + 1, size.getZ() + 3)));
        require(haloFluids == 0 && entities.isEmpty(), "Unexpected fluids/entities/dropped rails");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("status", "PASS");
        result.put("records_checked", expected.size());
        result.put("reachable_foot_positions_without_rail_crossings", reached.size());
        result.put("main_public_route_cells", 225);
        result.put("supported_internal_rails", rails);
        result.put("supported_external_test_rails", supports);
        result.put("supported_lanterns", lamps);
        result.put("connected_walking_ports", 3);
        result.put("connected_rail_ports", 5);
        result.put("external_fluid_cells", haloFluids);
        result.put("world_entities", entities.size());
        result.put("origin_world", List.of(fixture.origin().getX(), fixture.origin().getY(), fixture.origin().getZ()));
        if (capture) {
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, size, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, output.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(output.toFile()));
            require(reloaded.getSize().equals(size), "Capture reload size differs");
            ListTag palette = saved.getList("palette", Tag.TAG_COMPOUND);
            ListTag blocks = saved.getList("blocks", Tag.TAG_COMPOUND);
            require(blocks.size() == COUNT, "Native capture volume differs");
            Map<BlockPos, BlockState> captured = new LinkedHashMap<>();
            for (int index = 0; index < blocks.size(); index++) {
                CompoundTag entry = blocks.getCompound(index);
                captured.put(coordinate(entry.getList("pos", Tag.TAG_INT)), decode(palette.getCompound(entry.getInt("state"))));
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(captured.get(fixture.world(entry.getKey()).subtract(minimum))), "Native capture differs from source");
            result.put("capture_matches_rotated_source", true);
            result.put("world_capture", root().relativize(output).toString());
            result.put("world_capture_sha256", hash(output));
        }
        return result;
    }
    @GameTest(template = "empty", timeoutTicks = 600)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", "N02");
        result.put("status", "RUNNING");
        result.put("minecraft", "1.20.1");
        result.put("forge", "47.4.10");
        result.put("java", System.getProperty("java.version"));
        result.put("scope", "Isolated Forge GameTestServer with disposable external attachment stubs; no player save/core/deployment.");
        report(result);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Refusing a non-isolated server");
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
            level.setDayTime(6000);
            Path source = root().resolve("room.nbt");
            String fingerprint = hash(source);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(metadata.get("sha256").getAsString().equals(fingerprint), "Source fingerprint mismatch");
            result.put("structure_sha256", fingerprint);
            CompoundTag encoded = NbtIo.readCompressed(source.toFile());
            Map<BlockPos, BlockState> expected = audit(encoded);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), encoded);
            StructureTemplate roundtrip = new StructureTemplate();
            roundtrip.load(BuiltInRegistries.BLOCK.asLookup(), template.save(new CompoundTag()));
            require(roundtrip.getSize().equals(template.getSize()), "Template roundtrip size differs");
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> before = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos horizontal = helper.absolutePos(new BlockPos(80 + index * 112, 0, 80));
                Fixture fixture = new Fixture(new BlockPos(horizontal.getX(), 192, horizontal.getZ()), rotation);
                BlockPos minimum = fixture.minimum();
                Vec3i size = fixture.size();
                for (int chunkX = (minimum.getX() - 4) >> 4; chunkX <= (minimum.getX() + size.getX() + 4) >> 4; chunkX++) for (int chunkZ = (minimum.getZ() - 4) >> 4; chunkZ <= (minimum.getZ() + size.getZ() + 4) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
                for (BlockPos relative : expected.keySet()) level.setBlock(fixture.world(relative), Blocks.AIR.defaultBlockState(), FLAGS);
                require(emptyVolume(level, fixture, expected), "Fresh fixture occupied");
                BlockPos sentinel = fixture.world(new BlockPos(16, 5, 4));
                level.setBlock(sentinel, Blocks.GOLD_BLOCK.defaultBlockState(), FLAGS);
                require(!emptyVolume(level, fixture, expected) && level.getBlockState(sentinel).is(Blocks.GOLD_BLOCK), "Occupied air probe missed");
                level.setBlock(sentinel, Blocks.AIR.defaultBlockState(), FLAGS);
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "placeInWorld returned false");
                attachTestStubs(level, fixture, metadata);
                before.add(verify(level, fixture, expected, metadata, false));
                BlockPos chain = fixture.world(new BlockPos(16, 9, 6));
                BlockState chainState = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lantern = fixture.world(new BlockPos(16, 8, 6));
                require(!level.getBlockState(lantern).canSurvive(level, lantern), "Missing lantern support probe missed");
                level.setBlock(chain, chainState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos support = fixture.world(new BlockPos(5, 1, 10));
                BlockState supportState = level.getBlockState(support);
                level.setBlock(support, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos rail = fixture.world(new BlockPos(5, 2, 10));
                require(!level.getBlockState(rail).canSurvive(level, rail), "Missing rail support probe missed");
                level.setBlock(support, supportState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos route = fixture.world(new BlockPos(16, 2, 10));
                level.setBlock(route, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, route), "Blocked route probe missed");
                level.setBlock(route, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                fixtures.add(fixture);
                index++;
            }
            result.put("rotations_before_ticks", before);
            result.put("negative_probes", Map.of("occupied_air_voxel_refused", 4, "missing_chain_detected", 4, "missing_rail_floor_detected", 4, "blocked_public_route_detected", 4));
            result.put("forced_chunk_ticking", true);
            result.put("neighbor_updates_applied", true);
            result.put("stabilization_ticks", 200);
            report(result);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> after = new ArrayList<>();
                    for (Fixture fixture : fixtures) after.add(verify(level, fixture, expected, metadata, true));
                    require(hash(source).equals(fingerprint), "Source changed during validation");
                    result.put("rotations", after);
                    result.put("records_checked_after_ticks", COUNT * after.size());
                    result.put("source_unchanged", true);
                    result.put("status", "PASS");
                    report(result);
                    System.out.println("N02_NATIVE_PASS rotations=4/4 records=61380 ticks=200 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception error) {
                    result.put("status", "FAIL");
                    result.put("error", error.toString());
                    try { report(result); } catch (Exception secondary) { error.addSuppressed(secondary); }
                    throw new IllegalStateException(error);
                }
            });
        } catch (Exception error) {
            result.put("status", "FAIL");
            result.put("error", error.toString());
            report(result);
            throw error;
        }
    }
}
