package net.farmingtales.corridors15qa;

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

@GameTestHolder("ftcorridors15qa")
@PrefixGameTestTemplate(false)
public final class CorridorGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(15, 10, 16);
    private static final int COUNT = 2400;
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light", "respawn_anchor");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private CorridorGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static Path root() {
        return Path.of(System.getProperty("ftcorridors15qa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path file) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
    }

    private static void report(Map<String, Object> value) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(value) + "\n", StandardCharsets.UTF_8);
    }

    private static BlockPos coordinate(JsonArray value) {
        require(value.size() == 3, "Invalid metadata coordinate");
        return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt());
    }

    private static BlockPos coordinate(ListTag value) {
        require(value.size() == 3, "Invalid NBT coordinate");
        return new BlockPos(value.getInt(0), value.getInt(1), value.getInt(2));
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown block " + identity);
        require(!FORBIDDEN.contains(identity.getPath()) && !identity.getPath().endsWith("_bed"), "Unsafe block " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identity + " " + key);
        }
        BlockState result = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(!result.hasBlockEntity() && result.getFluidState().isEmpty(), "Containers, block entities or liquid not allowed");
        return result;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465, "Wrong data version");
        require(coordinate(data.getList("size", Tag.TAG_INT)).equals(new BlockPos(SIZE)), "Wrong template dimensions");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Preplaced entities forbidden");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) states.add(decode(palette.getCompound(index)));
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            BlockPos position = coordinate(record.getList("pos", Tag.TAG_INT));
            require(position.getX() >= 0 && position.getX() < SIZE.getX() && position.getY() >= 0 && position.getY() < SIZE.getY() && position.getZ() >= 0 && position.getZ() < SIZE.getZ(), "Out of bounds");
            int state = record.getInt("state");
            require(state >= 0 && state < states.size(), "Invalid palette index");
            require(!record.contains("nbt"), "Block entity payload forbidden");
            require(expected.put(position, states.get(state)) == null, "Duplicate coordinate");
        }
        require(expected.size() == COUNT, "Explicit full-volume placement required");
        return expected;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.isAir() && state.getCollisionShape(level, position).isEmpty() && state.getFluidState().isEmpty();
    }

    private static boolean floor(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.isFaceSturdy(level, position, Direction.UP) && state.getFluidState().isEmpty() && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        return clear(level, feet) && clear(level, feet.above()) && clear(level, feet.above(2)) && floor(level, feet.below());
    }

    private static void supports(ServerLevel level, Fixture fixture, JsonObject blueprint) {
        for (JsonElement value : blueprint.getAsJsonArray("lamps")) {
            JsonObject lamp = value.getAsJsonObject();
            require(level.getBlockState(fixture.world(coordinate(lamp.getAsJsonArray("lamp")))).is(Blocks.SOUL_LANTERN), "Lamp missing");
            for (JsonElement chain : lamp.getAsJsonArray("chain")) require(level.getBlockState(fixture.world(coordinate(chain.getAsJsonArray()))).is(Blocks.CHAIN), "Lamp chain missing");
            require(floor(level, fixture.world(coordinate(lamp.getAsJsonArray("anchor")))), "Lamp beam anchor missing");
        }
        for (JsonElement value : blueprint.getAsJsonArray("cages")) {
            for (JsonElement entry : value.getAsJsonObject().getAsJsonArray("suspension")) {
                JsonObject support = entry.getAsJsonObject();
                require(level.getBlockState(fixture.world(coordinate(support.getAsJsonArray("chain")))).is(Blocks.CHAIN), "Cage chain missing");
                require(floor(level, fixture.world(coordinate(support.getAsJsonArray("anchor")))), "Cage roof anchor missing");
                for (String key : List.of("base", "cap")) require(level.getBlockState(fixture.world(coordinate(support.getAsJsonArray(key)))).is(Blocks.POLISHED_DEEPSLATE), "Cage load-bearing plate missing");
                for (JsonElement column : support.getAsJsonArray("sides")) require(level.getBlockState(fixture.world(coordinate(column.getAsJsonArray()))).is(Blocks.IRON_BARS), "Cage vertical frame missing");
            }
        }
        for (JsonElement value : blueprint.getAsJsonArray("beams")) {
            JsonObject beam = value.getAsJsonObject();
            for (JsonElement valueSupport : beam.getAsJsonArray("supports")) {
                BlockPos foot = coordinate(valueSupport.getAsJsonArray());
                for (int posY = 0; posY <= 7; posY++) require(floor(level, fixture.world(new BlockPos(foot.getX(), posY, foot.getZ()))), "Beam support column interrupted");
            }
            BlockPos start = coordinate(beam.getAsJsonArray("from"));
            BlockPos end = coordinate(beam.getAsJsonArray("to"));
            for (int posX = start.getX(); posX <= end.getX(); posX++) require(level.getBlockState(fixture.world(new BlockPos(posX, start.getY(), start.getZ()))).is(Blocks.STRIPPED_DARK_OAK_LOG), "Beam interrupted");
        }
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject blueprint, boolean capture) throws Exception {
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " at " + entry.getKey() + ": " + actual + " expected " + entry.getValue().rotate(fixture.rotation()));
            require(actual.canSurvive(level, position), "Unsupported block " + position);
            require(actual.getFluidState().isEmpty() && level.getBlockEntity(position) == null, "Fluid or block entity appeared");
            require(Block.updateFromNeighbourShapes(actual, level, position).equals(actual), "Neighbor-derived shape mismatch at " + entry.getKey());
        }
        for (JsonElement value : blueprint.getAsJsonArray("clear_passage")) require(clear(level, fixture.world(coordinate(value.getAsJsonArray()))), "Three-wide three-high corridor obstructed");
        for (int posX = 6; posX <= 8; posX++) {
            for (int posZ = 0; posZ < 16; posZ++) require(walkable(level, fixture.world(new BlockPos(posX, 1, posZ))), "Unsafe straight through-route");
        }
        for (JsonElement value : blueprint.getAsJsonArray("safe_points")) {
            BlockPos center = coordinate(value.getAsJsonArray());
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) require(walkable(level, fixture.world(center.offset(offsetX, 0, offsetZ))), "Unsafe landing pad");
            }
        }
        for (int posX = 0; posX < 15; posX++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                require(floor(level, fixture.world(new BlockPos(posX, 9, posZ))), "Roof hole");
                require(floor(level, fixture.world(new BlockPos(posX, 0, posZ))), "Floor hole");
            }
        }
        supports(level, fixture, blueprint);
        BlockPos minimum = null;
        BlockPos maximum = null;
        for (BlockPos relative : expected.keySet()) {
            BlockPos world = fixture.world(relative);
            minimum = minimum == null ? world : new BlockPos(Math.min(minimum.getX(), world.getX()), Math.min(minimum.getY(), world.getY()), Math.min(minimum.getZ(), world.getZ()));
            maximum = maximum == null ? world : new BlockPos(Math.max(maximum.getX(), world.getX()), Math.max(maximum.getY(), world.getY()), Math.max(maximum.getZ(), world.getZ()));
        }
        require(level.getEntities(null, new AABB(minimum, maximum.offset(1, 1, 1))).isEmpty(), "Unexpected world entities");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records", expected.size());
        result.put("clear_passage_air_cells", 144);
        result.put("walkable_floor_cells", 48);
        result.put("roof_blocks", 240);
        result.put("survival_and_neighbor_shapes", "PASS");
        result.put("supported_lamps", 4);
        result.put("supported_cages", 2);
        result.put("containers_items_liquids_entities", 0);
        if (capture) {
            Vec3i dimensions = new Vec3i(maximum.getX() - minimum.getX() + 1, 10, maximum.getZ() - minimum.getZ() + 1);
            StructureTemplate snapshot = new StructureTemplate();
            snapshot.fillFromWorld(level, minimum, dimensions, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = snapshot.save(new CompoundTag());
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, output.toFile());
            ListTag records = saved.getList("blocks", Tag.TAG_COMPOUND);
            ListTag palette = saved.getList("palette", Tag.TAG_COMPOUND);
            require(records.size() == COUNT, "World capture omitted volume");
            Map<BlockPos, BlockState> recovered = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                recovered.put(coordinate(record.getList("pos", Tag.TAG_INT)), decode(palette.getCompound(record.getInt("state"))));
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(recovered.get(fixture.world(entry.getKey()).subtract(minimum))), "Native capture mismatch");
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(output.toFile()));
            require(reloaded.getSize().equals(dimensions), "Native snapshot reload differs");
            result.put("capture", root().relativize(output).toString());
            result.put("capture_sha256", hash(output));
            result.put("capture_matches_source", true);
        }
        result.put("status", "PASS");
        return result;
    }

    private static void expectSupportFailure(ServerLevel level, Fixture fixture, JsonObject blueprint, BlockPos relative) {
        BlockPos position = fixture.world(relative);
        BlockState original = level.getBlockState(position);
        level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
        boolean rejected = false;
        try {
            supports(level, fixture, blueprint);
        } catch (IllegalStateException expected) {
            rejected = true;
        } finally {
            level.setBlock(position, original, FLAGS | Block.UPDATE_KNOWN_SHAPE);
        }
        require(rejected, "Negative missing-support probe not detected " + relative);
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void shortCorridorFourRotations(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "RUNNING");
        result.put("minecraft", "1.20.1");
        result.put("forge", "47.4.10");
        result.put("java", System.getProperty("java.version"));
        result.put("scope", "Isolated headless Forge GameTestServer; no player world, client, full modpack, deployment or production JAR changes");
        report(result);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Only isolated GameTestServer may write fixtures");
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("blueprint.json")), JsonObject.class);
            String fingerprint = hash(root().resolve("room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Metadata fingerprint mismatch");
            require(hash(Path.of(metadata.get("reference").getAsString())).equals(metadata.get("reference_sha256").getAsString()), "Reference fingerprint mismatch");
            result.put("structure_sha256", fingerprint);
            CompoundTag encoded = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(encoded);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), encoded);
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos horizontal = helper.absolutePos(new BlockPos(32 + index * 56, 0, 32));
                Fixture fixture = new Fixture(new BlockPos(horizontal.getX(), 288, horizontal.getZ()), rotation);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.getChunk(position);
                    level.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placeInWorld failed");
                verify(level, fixture, expected, blueprint, false);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                expectSupportFailure(level, fixture, blueprint, new BlockPos(5, 6, 5));
                expectSupportFailure(level, fixture, blueprint, new BlockPos(6, 8, 7));
                expectSupportFailure(level, fixture, blueprint, new BlockPos(6, 9, 7));
                BlockPos aisle = fixture.world(new BlockPos(7, 1, 2));
                level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, aisle), "Blocked-passage negative probe missed");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                fixtures.add(fixture);
                index++;
            }
            result.put("negative_probes", Map.of("missing_lamp_chain", 4, "missing_cage_chain", 4, "missing_cage_roof_anchor", 4, "blocked_aisle", 4));
            result.put("stabilization_ticks", 200);
            result.put("fixture_chunks_forced_ticking", true);
            report(result);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) rotations.add(verify(level, fixture, expected, blueprint, true));
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source changed during test");
                    result.put("rotations", rotations);
                    result.put("records_checked_after_ticks", COUNT * rotations.size());
                    result.put("status", "PASS");
                    report(result);
                    System.out.println("S15_CORRIDOR_NATIVE_PASS rotations=4/4 records=9600 ticks=200 items=0 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception exception) {
                    result.put("status", "FAIL");
                    result.put("error", exception.toString());
                    try { report(result); } catch (Exception secondary) { exception.addSuppressed(secondary); }
                    throw new IllegalStateException(exception);
                }
            });
        } catch (Exception exception) {
            result.put("status", "FAIL");
            result.put("error", exception.toString());
            report(result);
            throw exception;
        }
    }
}
