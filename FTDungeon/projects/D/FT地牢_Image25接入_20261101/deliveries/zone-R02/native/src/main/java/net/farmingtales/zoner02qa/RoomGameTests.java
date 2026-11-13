package net.farmingtales.zoner02qa;

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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ChunkPos;
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

@GameTestHolder("zoner02qa")
@PrefixGameTestTemplate(false)
public final class RoomGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "sand", "gravel", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "structure_void", "jigsaw");
    private static final Set<String> BLOCK_ENTITIES = Set.of("minecraft:chest", "minecraft:barrel", "minecraft:banner", "minecraft:campfire");
    private record Cell(BlockState state, CompoundTag nbt) {}
    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }
    private RoomGameTests() {}
    private static Path root() { return Path.of(System.getProperty("zoner02qa.root")).toAbsolutePath().normalize(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static String hash(Path file) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))); }
    private static void writeReport(Map<String, Object> report) throws Exception { Files.writeString(root().resolve("qa/forge-native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8); }
    private static BlockPos coordinate(JsonArray value) { return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt()); }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla identity " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden identity " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        require(properties.getAllKeys().size() == block.getStateDefinition().getProperties().size(), "Incomplete properties " + identity);
        for (String propertyName : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(propertyName);
            require(property != null && property.getValue(properties.getString(propertyName)).isPresent(), "Invalid property " + identity + " " + propertyName);
        }
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(state.getFluidState().isEmpty(), "Unexpected fluid state");
        if (state.is(Blocks.CAMPFIRE)) require(properties.getString("lit").equals("false"), "Brazier must remain unlit");
        return state;
    }

    private static Map<BlockPos, Cell> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require Java 1.20.1 single palette");
        ListTag dimensions = data.getList("size", Tag.TAG_INT);
        require(dimensions.size() == 3 && dimensions.getInt(0) == 21 && dimensions.getInt(1) == 11 && dimensions.getInt(2) == 21, "Incorrect dimensions");
        require(coordinate(metadata.getAsJsonArray("size")).equals(new BlockPos(21, 11, 21)), "Metadata size mismatch");
        require(metadata.get("id").getAsString().equals("zone-R02"), "Wrong room identity");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected entity payloads");
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < encodedPalette.size(); index++) palette.add(decode(encodedPalette.getCompound(index)));
        Map<BlockPos, Cell> result = new LinkedHashMap<>();
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag position = record.getList("pos", Tag.TAG_INT);
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(position.size() == 3 && relative.getX() >= 0 && relative.getX() < 21 && relative.getY() >= 0 && relative.getY() < 11 && relative.getZ() >= 0 && relative.getZ() < 21, "Out of bounds");
            int stateIndex = record.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Invalid state index");
            BlockState state = palette.get(stateIndex);
            CompoundTag payload = record.getCompound("nbt");
            require(!payload.contains("LootTable") && payload.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Inventory or loot payload");
            require(state.hasBlockEntity() == record.contains("nbt"), "Block entity payload presence mismatch " + relative);
            if (state.hasBlockEntity()) require(BLOCK_ENTITIES.contains(payload.getString("id")), "Unexpected block entity identity");
            require(result.put(relative, new Cell(state, payload)) == null, "Duplicate block position");
        }
        require(result.size() == 4851, "Full explicit volume required");
        return result;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return !state.is(Blocks.COBWEB) && state.getFluidState().isEmpty() && state.getCollisionShape(level, position).isEmpty();
    }
    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        return clear(level, feet) && clear(level, feet.above()) && level.getBlockState(below).getFluidState().isEmpty() && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    private static void inspectEntity(ServerLevel level, BlockPos position, Cell expected) {
        BlockEntity entity = level.getBlockEntity(position);
        require((entity != null) == expected.state().hasBlockEntity(), "Block entity lost or unexpectedly created " + position);
        if (entity == null) return;
        CompoundTag actual = entity.saveWithFullMetadata();
        require(BLOCK_ENTITIES.contains(actual.getString("id")) && actual.getString("id").equals(expected.nbt().getString("id")), "Block entity type mismatch");
        require(!actual.contains("LootTable") && actual.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Native inventory or loot populated");
        if (entity instanceof Container container) require(container.isEmpty(), "Native container contains items");
        if (expected.nbt().contains("Patterns")) require(actual.getList("Patterns", Tag.TAG_COMPOUND).equals(expected.nbt().getList("Patterns", Tag.TAG_COMPOUND)), "Banner patterns lost");
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, Cell> expected, JsonObject metadata, boolean capture) throws Exception {
        int entities = 0;
        int chains = 0;
        int lights = 0;
        for (Map.Entry<BlockPos, Cell> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().state().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " expected=" + entry.getValue().state().rotate(fixture.rotation()) + " actual=" + actual);
            require(actual.canSurvive(level, position), "Unsupported native block " + entry.getKey());
            require(actual.getFluidState().isEmpty(), "Unexpected fluid");
            inspectEntity(level, position, entry.getValue());
            if (actual.hasBlockEntity()) entities++;
            if (actual.is(Blocks.CHAIN)) {
                BlockPos above = position.above();
                require(level.getBlockState(above).is(Blocks.CHAIN) || level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN), "Unanchored chain");
                chains++;
            }
            if (actual.is(Blocks.LANTERN)) lights++;
        }
        require(entities == 8 && chains == 12 && lights == 5, "Missing furnishings or lights");
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos entrance = coordinate(metadata.getAsJsonArray("entrance"));
        require(walkable(level, fixture.world(entrance)), "Blocked entrance");
        visited.add(entrance);
        queue.add(entrance);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() >= 0 && next.getX() < 21 && next.getZ() >= 0 && next.getZ() < 21 && !visited.contains(next) && walkable(level, fixture.world(next))) { visited.add(next); queue.add(next); }
            }
        }
        require(visited.contains(coordinate(metadata.getAsJsonArray("interior"))), "Safe point disconnected");
        for (JsonElement required : metadata.getAsJsonArray("required_walkway")) {
            BlockPos relative = coordinate(required.getAsJsonArray());
            require(visited.contains(relative) && clear(level, fixture.world(relative.above(2))), "Required three-high route obstructed " + relative);
        }
        for (int horizontal = 0; horizontal < 21; horizontal++) for (int depth = 0; depth < 21; depth++) {
            BlockPos floor = fixture.world(new BlockPos(horizontal, 0, depth));
            require(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Floor hole");
            for (int height : new int[] {9, 10}) {
                BlockPos roof = fixture.world(new BlockPos(horizontal, height, depth));
                require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Roof hole");
            }
        }
        for (int horizontal : new int[] {4, 16}) for (int depth : new int[] {4, 16}) for (int height = 0; height <= 9; height++) {
            BlockPos pillar = fixture.world(new BlockPos(horizontal, height, depth));
            require(level.getBlockState(pillar).isFaceSturdy(level, pillar, Direction.UP), "Pillar or beam disconnected");
        }
        for (int horizontal : new int[] {8, 12}) for (int depth : new int[] {10, 12}) {
            require(level.getBlockState(fixture.world(new BlockPos(horizontal, 1, depth))).is(Blocks.SPRUCE_FENCE), "Table lacks grounded leg");
            BlockPos top = fixture.world(new BlockPos(horizontal, 2, depth));
            require(level.getBlockState(top).isFaceSturdy(level, top, Direction.UP), "Tabletop support lost");
        }
        BlockPos minimum = fixture.world(BlockPos.ZERO);
        BlockPos maximum = minimum;
        for (BlockPos relative : expected.keySet()) {
            BlockPos position = fixture.world(relative);
            minimum = new BlockPos(Math.min(minimum.getX(), position.getX()), Math.min(minimum.getY(), position.getY()), Math.min(minimum.getZ(), position.getZ()));
            maximum = new BlockPos(Math.max(maximum.getX(), position.getX()), Math.max(maximum.getY(), position.getY()), Math.max(maximum.getZ(), position.getZ()));
        }
        var foundEntities = level.getEntities(null, new AABB(minimum, maximum.offset(1, 1, 1)));
        require(foundEntities.isEmpty(), "Unexpected world entities " + foundEntities.stream().map(entity -> entity.getType() + " at " + entity.position()).toList());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records_checked", expected.size());
        result.put("reachable_foot_positions", visited.size());
        result.put("empty_or_decorative_block_entities", entities);
        result.put("roof_anchored_chain_blocks", chains);
        result.put("supported_lanterns", lights);
        result.put("survival_collision_roof_floor_inventory_fluids_entities", "PASS");
        if (capture) {
            StructureTemplate captured = new StructureTemplate();
            captured.fillFromWorld(level, minimum, new Vec3i(21, 11, 21), false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = captured.save(new CompoundTag());
            Map<BlockPos, BlockState> roundtrip = new LinkedHashMap<>();
            ListTag palette = saved.getList("palette", Tag.TAG_COMPOUND);
            ListTag records = saved.getList("blocks", Tag.TAG_COMPOUND);
            require(records.size() == 4851, "Captured volume incomplete");
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag position = record.getList("pos", Tag.TAG_INT);
                roundtrip.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), decode(palette.getCompound(record.getInt("state"))));
            }
            for (Map.Entry<BlockPos, Cell> entry : expected.entrySet()) require(entry.getValue().state().rotate(fixture.rotation()).equals(roundtrip.get(fixture.world(entry.getKey()).subtract(minimum))), "World capture differs");
            Path export = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, export.toFile());
            result.put("capture_matches_source", true);
            result.put("world_capture", root().relativize(export).toString());
            result.put("capture_sha256", hash(export));
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void fourRotationsAndTwoHundredTickStability(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("scope", "Isolated headless Forge GameTest; no player save, FT integration or client play test");
        try {
            String fingerprint = hash(root().resolve("room.nbt"));
            report.put("structure_sha256", fingerprint);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(metadata.get("sha256").getAsString().equals(fingerprint), "Metadata hash mismatch");
            CompoundTag data = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            Map<BlockPos, Cell> expected = audit(data, metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(new Vec3i(21, 11, 21)), "Native dimensions mismatch");
            require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == expected.size(), "Native roundtrip lost records");
            report.put("native_decode_registry_properties_roundtrip", "PASS");
            ServerLevel level = helper.getLevel();
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
            List<Fixture> fixtures = new ArrayList<>();
            Set<Long> forcedChunks = new HashSet<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos anchor = helper.absolutePos(new BlockPos(40 + index * 56, 0, 48));
                Fixture fixture = new Fixture(new BlockPos(anchor.getX(), 280, anchor.getZ()), rotation);
                BlockPos firstCorner = fixture.world(new BlockPos(-1, -1, -1));
                BlockPos oppositeCorner = fixture.world(new BlockPos(21, 11, 21));
                int minimumChunkHorizontal = Math.floorDiv(Math.min(firstCorner.getX(), oppositeCorner.getX()), 16);
                int maximumChunkHorizontal = Math.floorDiv(Math.max(firstCorner.getX(), oppositeCorner.getX()), 16);
                int minimumChunkDepth = Math.floorDiv(Math.min(firstCorner.getZ(), oppositeCorner.getZ()), 16);
                int maximumChunkDepth = Math.floorDiv(Math.max(firstCorner.getZ(), oppositeCorner.getZ()), 16);
                for (int chunkHorizontal = minimumChunkHorizontal; chunkHorizontal <= maximumChunkHorizontal; chunkHorizontal++) {
                    for (int chunkDepth = minimumChunkDepth; chunkDepth <= maximumChunkDepth; chunkDepth++) {
                        long key = ChunkPos.asLong(chunkHorizontal, chunkDepth);
                        level.setChunkForced(chunkHorizontal, chunkDepth, true);
                        require(level.getForcedChunks().contains(key), "Fixture chunk was not forced");
                        forcedChunks.add(key);
                    }
                }
                for (int horizontal = -1; horizontal <= 21; horizontal++) for (int vertical = -1; vertical <= 11; vertical++) for (int depth = -1; depth <= 21; depth++) {
                    BlockPos position = fixture.world(new BlockPos(horizontal, vertical, depth));
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                }
                BlockPos preparedMinimum = new BlockPos(Math.min(firstCorner.getX(), oppositeCorner.getX()), Math.min(firstCorner.getY(), oppositeCorner.getY()), Math.min(firstCorner.getZ(), oppositeCorner.getZ()));
                BlockPos preparedMaximum = new BlockPos(Math.max(firstCorner.getX(), oppositeCorner.getX()), Math.max(firstCorner.getY(), oppositeCorner.getY()), Math.max(firstCorner.getZ(), oppositeCorner.getZ()));
                require(level.getEntities(null, new AABB(preparedMinimum, preparedMaximum.offset(1, 1, 1))).isEmpty(), "Sky fixture must be entity-free before placement " + preparedMinimum);
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement failed");
                verify(level, fixture, expected, metadata, false);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    BlockState state = level.getBlockState(position);
                    level.updateNeighborsAt(position, state.getBlock());
                    state.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
                }
                BlockPos aisle = fixture.world(new BlockPos(10, 1, 3));
                level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS);
                require(!walkable(level, aisle), "Collision negative probe not detected");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS);
                BlockPos backing = fixture.world(new BlockPos(1, 5, 8));
                BlockPos banner = fixture.world(new BlockPos(2, 5, 8));
                BlockState saved = level.getBlockState(backing);
                level.setBlock(backing, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!level.getBlockState(banner).canSurvive(level, banner), "Banner support negative probe not detected");
                level.setBlock(backing, saved, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos tabletop = fixture.world(new BlockPos(12, 2, 10));
                BlockPos lamp = fixture.world(new BlockPos(12, 3, 10));
                saved = level.getBlockState(tabletop);
                level.setBlock(tabletop, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!level.getBlockState(lamp).canSurvive(level, lamp), "Table lamp support negative probe not detected");
                level.setBlock(tabletop, saved, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                fixtures.add(fixture);
                index++;
            }
            report.put("negative_collision_probes", 4);
            report.put("negative_banner_support_probes", 4);
            report.put("negative_table_lamp_support_probes", 4);
            report.put("disposable_fixture_air_halo_blocks", 1);
            report.put("stabilization_ticks", 200);
            report.put("random_tick_speed", level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING));
            report.put("forced_fixture_chunks", forcedChunks.size());
            report.put("fixture_placement", "Sky airspace, freshly generated isolated world, entity-free before every placement; drops remain enabled");
            report.put("fixture_floor_absolute_y", 280);
            long stabilizationStarted = level.getGameTime();
            writeReport(report);
            helper.runAfterDelay(200, () -> {
                try {
                    require(level.getGameTime() - stabilizationStarted >= 200, "World did not advance 200 ticks");
                    require(forcedChunks.stream().allMatch(key -> level.getForcedChunks().contains(key)), "Fixture chunk lost forced status");
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) rotations.add(verify(level, fixture, expected, metadata, true));
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source changed during validation");
                    report.put("rotations", rotations);
                    report.put("records_checked_after_ticks", expected.size() * rotations.size());
                    report.put("world_ticks_elapsed", level.getGameTime() - stabilizationStarted);
                    report.put("all_fixture_chunks_forced_throughout", true);
                    report.put("status", "PASS");
                    report.put("limitations", List.of("Isolated Forge fixture only; no production deployment or player save modified", "No FT integration slot, occupied-volume refusal or persistent idempotence test", "Software render is not a full modpack client or multiplayer performance test"));
                    writeReport(report);
                    helper.succeed();
                } catch (Throwable failure) {
                    report.put("status", "FAIL");
                    report.put("error", failure.toString());
                    try { writeReport(report); } catch (Exception ignored) {}
                    helper.fail(failure.toString());
                }
            });
        } catch (Throwable failure) {
            report.put("status", "FAIL");
            report.put("error", failure.toString());
            writeReport(report);
            helper.fail(failure.toString());
        }
    }
}
