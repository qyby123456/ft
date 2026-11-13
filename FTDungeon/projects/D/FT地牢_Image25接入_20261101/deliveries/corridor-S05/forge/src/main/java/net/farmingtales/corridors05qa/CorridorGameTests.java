package net.farmingtales.corridors05qa;

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
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("corridors05qa")
@PrefixGameTestTemplate(false)
public final class CorridorGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> ALLOWED = Set.of("air", "deepslate_tiles", "deepslate_bricks", "polished_blackstone", "polished_blackstone_bricks", "cracked_polished_blackstone_bricks", "chiseled_polished_blackstone", "mossy_stone_bricks", "water", "wall_torch", "polished_blackstone_brick_stairs", "iron_bars", "iron_trapdoor", "chain");
    private static final int FLAGS = Block.UPDATE_CLIENTS;

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("corridors05qa.root")).toAbsolutePath().normalize();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void report(Map<String, Object> result) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static BlockPos coordinate(JsonArray value) {
        return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt());
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require single-palette Java 1.20.1 NBT");
        ListTag size = data.getList("size", Tag.TAG_INT);
        require(size.size() == 3 && size.getInt(0) == 11 && size.getInt(1) == 10 && size.getInt(2) == 16, "Wrong corridor dimensions");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected entities");
        List<BlockState> palette = new ArrayList<>();
        for (Tag paletteEntry : data.getList("palette", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) paletteEntry;
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity) && ALLOWED.contains(identity.getPath()), "Forbidden or unknown block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Illegal state " + identity + " " + key);
            }
            palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        for (Tag blockEntry : data.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) blockEntry;
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            require(position.size() == 3, "Malformed coordinate");
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(inside(relative), "Out of bounds " + relative);
            int stateIndex = entry.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Bad state index");
            BlockState state = palette.get(stateIndex);
            require(!state.hasBlockEntity() && !entry.contains("nbt"), "Unexpected container/payload");
            require(expected.put(relative, state) == null, "Duplicate coordinate");
        }
        require(expected.size() == 1760, "Explicit volume incomplete");
        return expected;
    }

    private static boolean inside(BlockPos position) {
        return position.getX() >= 0 && position.getX() < 11 && position.getY() >= 0 && position.getY() < 10 && position.getZ() >= 0 && position.getZ() < 16;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getCollisionShape(level, position).isEmpty() && state.getFluidState().isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos bed = feet.below();
        BlockState floor = level.getBlockState(bed);
        return clear(level, feet) && clear(level, feet.above()) && clear(level, feet.above(2)) && floor.getFluidState().isEmpty() && floor.isFaceSturdy(level, bed, Direction.UP);
    }

    private static boolean waterSeal(ServerLevel level, Fixture fixture, JsonObject blueprint) {
        for (JsonElement encoded : blueprint.getAsJsonArray("water_positions")) {
            BlockPos relative = coordinate(encoded.getAsJsonArray());
            BlockPos world = fixture.world(relative);
            if (!level.getBlockState(world).is(Blocks.WATER) || !level.getFluidState(world).isSource()) {
                return false;
            }
            for (Direction direction : List.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
                BlockPos neighbor = fixture.world(relative.relative(direction));
                BlockState state = level.getBlockState(neighbor);
                if (!state.is(Blocks.WATER) && !state.isCollisionShapeFullBlock(level, neighbor)) {
                    return false;
                }
            }
        }
        return blueprint.getAsJsonArray("water_positions").size() == 52;
    }

    private static boolean cageAnchors(ServerLevel level, Fixture fixture, JsonObject blueprint) {
        for (JsonElement encoded : blueprint.getAsJsonArray("cage_roof_anchors")) {
            BlockPos relative = coordinate(encoded.getAsJsonArray());
            BlockPos top = fixture.world(relative.above());
            if (!level.getBlockState(top).isCollisionShapeFullBlock(level, top)
                    || !level.getBlockState(fixture.world(relative)).is(Blocks.CHAIN)
                    || !level.getBlockState(fixture.world(relative.below())).is(Blocks.CHAIN)
                    || !level.getBlockState(fixture.world(relative.below(2))).is(Blocks.IRON_BARS)
                    || !level.getBlockState(fixture.world(relative.below(3))).is(Blocks.IRON_BARS)
                    || !level.getBlockState(fixture.world(relative.below(4))).is(Blocks.IRON_TRAPDOOR)) {
                return false;
            }
        }
        return blueprint.getAsJsonArray("cage_roof_anchors").size() == 4;
    }

    private static AABB bounds(Fixture fixture) {
        BlockPos first = fixture.world(BlockPos.ZERO);
        BlockPos last = fixture.world(new BlockPos(10, 9, 15));
        return new AABB(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()), Math.max(first.getX(), last.getX()) + 1, last.getY() + 1, Math.max(first.getZ(), last.getZ()) + 1);
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject metadata, JsonObject blueprint) {
        int waters = 0;
        int torches = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos world = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(world);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + ": " + actual + " expected " + entry.getValue().rotate(fixture.rotation()));
            require(actual.canSurvive(level, world), "Unsupported block " + entry.getKey());
            require(level.getBlockEntity(world) == null, "Unexpected block entity or inventory");
            if (!actual.getFluidState().isEmpty()) {
                require(actual.is(Blocks.WATER) && actual.getFluidState().isSource(), "Unexpected liquid");
                waters++;
            }
            if (actual.is(Blocks.WALL_TORCH)) {
                torches++;
            }
        }
        require(waters == 52 && torches == 6, "Lost water or torches");
        require(waterSeal(level, fixture, blueprint), "Water seal failed");
        require(cageAnchors(level, fixture, blueprint), "Cage support chain failed");
        for (int posX = 0; posX < 11; posX++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                for (int posY : List.of(0, 9)) {
                    BlockPos surface = fixture.world(new BlockPos(posX, posY, posZ));
                    require(level.getBlockState(surface).isCollisionShapeFullBlock(level, surface), "Incomplete floor/roof");
                }
            }
        }
        for (int posX = -1; posX <= 11; posX++) {
            for (int posY = -1; posY <= 10; posY++) {
                for (int posZ = -1; posZ <= 16; posZ++) {
                    BlockPos relative = new BlockPos(posX, posY, posZ);
                    if (!inside(relative)) {
                        require(level.getFluidState(fixture.world(relative)).isEmpty(), "Fluid escaped to external moat " + relative);
                    }
                }
            }
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos entrance = coordinate(metadata.getAsJsonArray("entrance"));
        require(entrance.equals(new BlockPos(5, 2, 0)) && walkable(level, fixture.world(entrance)), "Entrance unsafe");
        reached.add(entrance);
        queue.add(entrance);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (inside(next) && !reached.contains(next) && walkable(level, fixture.world(next))) {
                    reached.add(next);
                    queue.add(next);
                }
            }
        }
        require(reached.contains(coordinate(metadata.getAsJsonArray("interior"))) && reached.contains(coordinate(metadata.getAsJsonArray("exit"))), "North/south route or safe point disconnected");
        for (JsonElement value : blueprint.getAsJsonArray("required_feet")) {
            require(reached.contains(coordinate(value.getAsJsonArray())), "Three-wide route interrupted");
        }
        require(level.getEntities(null, bounds(fixture)).isEmpty(), "Unexpected entities or item drops");
        return Map.of("rotation", fixture.rotation().name(), "records", expected.size(), "water_sources", waters, "supported_torches", torches, "reachable_dry_feet", reached.size(), "route_seal_roof_supports_external_moat", "PASS");
    }

    private static CompoundTag capture(ServerLevel level, Fixture fixture, CompoundTag source, Map<BlockPos, BlockState> expected) {
        Rotation inverse = switch (fixture.rotation()) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> fixture.rotation();
        };
        CompoundTag result = source.copy();
        ListTag palette = new ListTag();
        ListTag records = new ListTag();
        Map<BlockState, Integer> indices = new LinkedHashMap<>();
        for (BlockPos relative : expected.keySet()) {
            BlockState state = level.getBlockState(fixture.world(relative)).rotate(inverse);
            if (!indices.containsKey(state)) {
                indices.put(state, palette.size());
                palette.add(NbtUtils.writeBlockState(state));
            }
            CompoundTag entry = new CompoundTag();
            ListTag position = new ListTag();
            position.add(IntTag.valueOf(relative.getX()));
            position.add(IntTag.valueOf(relative.getY()));
            position.add(IntTag.valueOf(relative.getZ()));
            entry.put("pos", position);
            entry.putInt("state", indices.get(state));
            records.add(entry);
        }
        result.put("palette", palette);
        result.put("blocks", records);
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void corridorFourRotations(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "RUNNING");
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Isolated GameTestServer required");
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            CompoundTag source = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json"), StandardCharsets.UTF_8), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("blueprint.json"), StandardCharsets.UTF_8), JsonObject.class);
            String fingerprint = hash(root().resolve("room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Metadata hash differs");
            Map<BlockPos, BlockState> expected = audit(source);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), source);
            require(template.getSize().equals(new Vec3i(11, 10, 16)), "Native size mismatch");
            require(audit(template.save(new CompoundTag())).equals(expected), "Native template roundtrip mismatch");
            result.put("source_nbt_sha256", fingerprint);
            result.put("minecraft", "1.20.1");
            result.put("forge", net.minecraftforge.versions.forge.ForgeVersion.getVersion());
            result.put("java", System.getProperty("java.version"));
            result.put("isolated_run_directory", System.getProperty("user.dir"));
            result.put("fluid_ticks_enabled", true);
            result.put("random_tick_speed", 0);
            List<Fixture> fixtures = new ArrayList<>();
            int rotationIndex = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos origin = new BlockPos(64 + rotationIndex * 64, 224, 64);
                Fixture fixture = new Fixture(origin, rotation);
                for (int chunkX = (origin.getX() - 32) >> 4; chunkX <= (origin.getX() + 32) >> 4; chunkX++) {
                    for (int chunkZ = (origin.getZ() - 32) >> 4; chunkZ <= (origin.getZ() + 32) >> 4; chunkZ++) {
                        level.setChunkForced(chunkX, chunkZ, true);
                    }
                }
                for (int posX = -1; posX <= 11; posX++) {
                    for (int posY = -1; posY <= 10; posY++) {
                        for (int posZ = -1; posZ <= 16; posZ++) {
                            level.setBlock(fixture.world(new BlockPos(posX, posY, posZ)), Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
                        }
                    }
                }
                level.getEntities(null, bounds(fixture).inflate(2)).forEach(entity -> entity.discard());
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placement failed");
                for (BlockPos relative : expected.keySet()) {
                    BlockPos world = fixture.world(relative);
                    level.updateNeighborsAt(world, level.getBlockState(world).getBlock());
                    if (!level.getFluidState(world).isEmpty()) {
                        level.scheduleTick(world, Fluids.WATER, 5);
                    }
                }
                BlockPos aisle = fixture.world(new BlockPos(5, 2, 3));
                level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, aisle), "Negative blocked-path probe missed");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos bed = fixture.world(new BlockPos(2, 0, 2));
                BlockState savedBed = level.getBlockState(bed);
                level.setBlock(bed, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!waterSeal(level, fixture, blueprint), "Negative leaking-bed probe missed");
                level.setBlock(bed, savedBed, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos anchor = fixture.world(new BlockPos(2, 9, 3));
                BlockState savedAnchor = level.getBlockState(anchor);
                level.setBlock(anchor, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!cageAnchors(level, fixture, blueprint), "Negative roof-anchor probe missed");
                level.setBlock(anchor, savedAnchor, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                fixtures.add(fixture);
                rotationIndex++;
            }
            result.put("negative_probes", Map.of("blocked_route", 4, "perforated_pool_bed", 4, "missing_cage_roof_anchor", 4));
            Map<Rotation, CompoundTag> stableAt200 = new LinkedHashMap<>();
            helper.runAfterDelay(200, () -> {
                for (Fixture fixture : fixtures) {
                    stableAt200.put(fixture.rotation(), capture(level, fixture, source, expected));
                }
            });
            report(result);
            helper.runAfterDelay(240, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        rotations.add(verify(level, fixture, expected, metadata, blueprint));
                        CompoundTag captured = capture(level, fixture, source, expected);
                        require(captured.equals(stableAt200.get(fixture.rotation())), "Still changing between ticks 200 and 240");
                        NbtIo.writeCompressed(captured, root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase() + ".nbt").toFile());
                    }
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source changed during QA");
                    result.put("rotations", rotations);
                    result.put("records_checked_after_ticks", expected.size() * 4);
                    result.put("stable_snapshot_ticks", List.of(200, 240));
                    result.put("native_registry_properties_and_roundtrip", "PASS");
                    result.put("status", "PASS");
                    result.put("limitations", List.of("Isolated Forge server, not a full-modpack client/playthrough/FPS/multiplayer test.", "No deployment, FT core integration, world slot assignment or production-world occupancy/idempotence test."));
                    report(result);
                    System.out.println("S05_NATIVE_GAME_TEST_PASS: rotations=4/4 records=" + expected.size() * 4 + " ticks=240 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception exception) {
                    result.put("status", "FAIL");
                    result.put("error", exception.toString());
                    try {
                        report(result);
                    } catch (Exception secondary) {
                        exception.addSuppressed(secondary);
                    }
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
