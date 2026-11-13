package net.farmingtales.corridorm18qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftcorridorm18qa")
@GameTestHolder("ftcorridorm18qa")
@PrefixGameTestTemplate(false)
public final class CorridorVerification {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Vec3i SIZE = new Vec3i(15, 12, 48);
    private static final Set<String> FORBIDDEN = Set.of("ice", "frosted_ice", "water", "lava", "fire", "soul_fire", "powder_snow", "snow", "cobweb", "magma_block", "cactus", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light");

    public CorridorVerification() {}

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static Path root() {
        return Path.of(System.getProperty("ftcorridorm18qa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/forge-native.json"), JSON.toJson(report) + "\n");
    }

    private static BlockPos coordinate(JsonArray values) {
        return new BlockPos(values.get(0).getAsInt(), values.get(1).getAsInt(), values.get(2).getAsInt());
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identifier = new ResourceLocation(encoded.getString("Name"));
        require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown vanilla block " + identifier);
        require(!FORBIDDEN.contains(identifier.getPath()), "Forbidden block " + identifier);
        Block block = BuiltInRegistries.BLOCK.get(identifier);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identifier + " " + key);
        }
        require(properties.getAllKeys().size() == block.getStateDefinition().getProperties().size(), "Incomplete properties " + identifier);
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(state.getFluidState().isEmpty() && !state.hasBlockEntity(), "Unexpected fluid or block entity " + identifier);
        return state;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require 1.20.1 single palette");
        ListTag dimensions = data.getList("size", Tag.TAG_INT);
        require(dimensions.size() == 3 && dimensions.getInt(0) == 15 && dimensions.getInt(1) == 12 && dimensions.getInt(2) == 48, "Wrong dimensions");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected source entities");
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < encodedPalette.size(); index++) palette.add(decode(encodedPalette.getCompound(index)));
        Map<BlockPos, BlockState> records = new LinkedHashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag record = blocks.getCompound(index);
            ListTag position = record.getList("pos", Tag.TAG_INT);
            require(position.size() == 3 && !record.contains("nbt"), "Malformed record or unexpected block NBT");
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 15 && relative.getY() >= 0 && relative.getY() < 12 && relative.getZ() >= 0 && relative.getZ() < 48, "Out of bounds record");
            int stateIndex = record.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Bad palette index");
            require(records.put(relative, palette.get(stateIndex)) == null, "Duplicate coordinate");
        }
        require(records.size() == 8640, "Require explicit full volume");
        return records;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getFluidState().isEmpty() && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        return clear(level, feet) && clear(level, feet.above()) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject metadata, boolean capture) throws Exception {
        int candles = 0;
        int snow = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "Changed state " + fixture.rotation() + " " + entry.getKey() + " expected=" + entry.getValue().rotate(fixture.rotation()) + " actual=" + actual);
            require(actual.canSurvive(level, position), "Unsupported block " + position + " " + actual);
            require(level.getBlockEntity(position) == null && actual.getFluidState().isEmpty(), "Unexpected block entity/fluid");
            if (actual.is(Blocks.CANDLE)) candles++;
            if (actual.is(Blocks.SNOW_BLOCK)) {
                require(level.getBrightness(LightLayer.BLOCK, position) <= 11, "Snow block can melt under block light");
                snow++;
            }
        }
        require(candles == 12 && snow == 4, "Decor count differs");
        for (int depth = 0; depth < 48; depth++) {
            for (int across = 5; across <= 9; across++) {
                BlockPos feet = fixture.world(new BlockPos(across, 1, depth));
                require(walkable(level, feet) && clear(level, feet.above(2)), "Main 5x3 aisle obstruction");
            }
        }
        JsonObject blueprint = metadata.getAsJsonObject("blueprint");
        for (JsonElement value : blueprint.getAsJsonArray("cages")) {
            JsonObject cage = value.getAsJsonObject();
            BlockPos anchor = fixture.world(coordinate(cage.getAsJsonArray("anchor")));
            require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Missing cage ceiling anchor");
            require(level.getBlockState(fixture.world(coordinate(cage.getAsJsonArray("chain")))).is(Blocks.CHAIN), "Missing cage chain");
            require(level.getBlockState(fixture.world(coordinate(cage.getAsJsonArray("cap")))).is(Blocks.CHISELED_STONE_BRICKS), "Missing cage cap");
        }
        for (JsonElement value : blueprint.getAsJsonArray("icicles")) {
            JsonObject icicle = value.getAsJsonObject();
            require(level.getBlockState(fixture.world(coordinate(icicle.getAsJsonArray("cap")))).is(Blocks.BLUE_ICE), "Missing frost cap");
            require(level.getBlockState(fixture.world(coordinate(icicle.getAsJsonArray("tip")))).is(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE), "Missing frost tip");
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        BlockPos entrance = coordinate(metadata.getAsJsonArray("entrance"));
        reached.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() >= 0 && next.getX() < 15 && next.getZ() >= 0 && next.getZ() < 48 && !reached.contains(next) && walkable(level, fixture.world(next))) {
                    reached.add(next);
                    pending.addLast(next);
                }
            }
        }
        require(reached.contains(coordinate(metadata.getAsJsonArray("interior"))) && reached.contains(coordinate(metadata.getAsJsonArray("exit"))), "Entrance-interior-exit route disconnected");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("status", "PASS");
        result.put("records_checked", expected.size());
        result.put("reachable_foot_positions", reached.size());
        result.put("clear_aisle", "5 wide x 3 high x 48 long");
        result.put("candles_supported", candles);
        result.put("snow_blocks_safe_light", snow);
        BlockPos minimum = fixture.world(BlockPos.ZERO);
        BlockPos maximum = minimum;
        for (BlockPos corner : List.of(new BlockPos(14, 0, 0), new BlockPos(0, 0, 47), new BlockPos(14, 11, 47))) {
            BlockPos transformed = fixture.world(corner);
            minimum = new BlockPos(Math.min(minimum.getX(), transformed.getX()), Math.min(minimum.getY(), transformed.getY()), Math.min(minimum.getZ(), transformed.getZ()));
            maximum = new BlockPos(Math.max(maximum.getX(), transformed.getX()), Math.max(maximum.getY(), transformed.getY()), Math.max(maximum.getZ(), transformed.getZ()));
        }
        int entities = level.getEntities(null, new AABB(minimum, maximum.offset(1, 1, 1))).size();
        require(entities == 0, "Unexpected dropped/decorative entities");
        result.put("unexpected_entities", entities);
        if (capture) {
            boolean quarterTurn = fixture.rotation() == Rotation.CLOCKWISE_90 || fixture.rotation() == Rotation.COUNTERCLOCKWISE_90;
            Vec3i captureSize = quarterTurn ? new Vec3i(48, 12, 15) : SIZE;
            StructureTemplate saved = new StructureTemplate();
            saved.fillFromWorld(level, minimum, captureSize, false, Blocks.STRUCTURE_VOID);
            CompoundTag captured = saved.save(new CompoundTag());
            Map<BlockPos, BlockState> capturedRecords = new LinkedHashMap<>();
            ListTag capturedPalette = captured.getList("palette", Tag.TAG_COMPOUND);
            ListTag blocks = captured.getList("blocks", Tag.TAG_COMPOUND);
            require(blocks.size() == 8640, "Incomplete world capture");
            for (int index = 0; index < blocks.size(); index++) {
                CompoundTag record = blocks.getCompound(index);
                ListTag position = record.getList("pos", Tag.TAG_INT);
                capturedRecords.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), decode(capturedPalette.getCompound(record.getInt("state"))));
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(capturedRecords.get(fixture.world(entry.getKey()).subtract(minimum))), "World capture differs");
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(captured, output.toFile());
            StructureTemplate roundtrip = new StructureTemplate();
            roundtrip.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(output.toFile()));
            require(roundtrip.getSize().equals(captureSize), "Capture native roundtrip failed");
            result.put("capture", root().relativize(output).toString());
            result.put("capture_sha256", hash(output));
            result.put("capture_exact_match", true);
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void fourRotationsWithNeighborAndTickStability(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("scope", "Isolated headless Forge GameTestServer only, never player saves or FT deployment");
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Only isolated GameTestServer may write fixture blocks");
            Path source = root().resolve("room.nbt");
            String fingerprint = hash(source);
            report.put("structure_sha256", fingerprint);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Metadata hash differs");
            CompoundTag data = NbtIo.readCompressed(source.toFile());
            Map<BlockPos, BlockState> expected = audit(data);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(SIZE), "Native load size differs");
            require(audit(template.save(new CompoundTag())).equals(expected), "Native source roundtrip differs");
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(new BlockPos(8000 + index * 192, 120, 8000), rotation);
                for (int across = -1; across <= 15; across++) {
                    for (int depth = -1; depth <= 48; depth++) {
                        BlockPos position = fixture.world(new BlockPos(across, 0, depth));
                        level.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
                        for (int height = -1; height <= 12; height++) level.setBlock(position.offset(0, height, 0), Blocks.AIR.defaultBlockState(), FLAGS);
                    }
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement returned false");
                verify(level, fixture, expected, metadata, false);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                BlockPos probe = fixture.world(new BlockPos(7, 1, 3));
                level.setBlock(probe, Blocks.STONE.defaultBlockState(), FLAGS);
                require(!walkable(level, probe), "Negative blocked-route probe undetected");
                level.setBlock(probe, Blocks.AIR.defaultBlockState(), FLAGS);
                BlockPos candle = fixture.world(new BlockPos(2, 2, 4));
                BlockPos support = candle.below();
                BlockState savedSupport = level.getBlockState(support);
                level.setBlock(support, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!level.getBlockState(candle).canSurvive(level, candle), "Negative unsupported-candle probe undetected");
                level.setBlock(support, savedSupport, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                fixtures.add(fixture);
                index++;
            }
            report.put("neighbor_updates_per_rotation", 8640);
            report.put("negative_blocked_aisle_probes", 4);
            report.put("negative_unsupported_candle_probes", 4);
            report.put("stabilization_ticks", 120);
            writeReport(report);
            helper.runAfterDelay(120, () -> {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) results.add(verify(level, fixture, expected, metadata, true));
                    require(fingerprint.equals(hash(source)), "Source mutated during QA");
                    report.put("rotations", results);
                    report.put("records_checked_after_ticks", 34560);
                    report.put("status", "PASS");
                    report.put("limitations", List.of("Not an FT core catalog/route/occupancy/idempotence integration test.", "Not a full modpack gameplay, client screenshot, performance or multiplayer test."));
                    writeReport(report);
                    System.out.println("CORRIDOR_M18_NATIVE_PASS rotations=4 records=34560 ticks=120 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception exception) {
                    report.put("status", "FAIL");
                    report.put("error", exception.toString());
                    try { writeReport(report); } catch (Exception secondary) { exception.addSuppressed(secondary); }
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
