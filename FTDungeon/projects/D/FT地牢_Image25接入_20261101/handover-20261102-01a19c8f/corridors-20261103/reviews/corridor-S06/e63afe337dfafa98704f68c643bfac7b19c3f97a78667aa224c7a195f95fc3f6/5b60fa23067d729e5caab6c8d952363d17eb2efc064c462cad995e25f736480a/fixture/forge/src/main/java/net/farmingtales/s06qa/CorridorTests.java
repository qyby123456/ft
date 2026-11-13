package net.farmingtales.s06qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
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

@GameTestHolder("fts06qa")
@PrefixGameTestTemplate(false)
public final class CorridorTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> ALLOWED = Set.of("air", "stone_bricks", "mossy_stone_bricks", "chiseled_stone_bricks", "stone_brick_stairs", "water", "vine", "chain", "soul_lantern");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("fts06qa.root")).toAbsolutePath().normalize();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static void report(Map<String, Object> result) throws Exception {
        String rotation = result.get("requested_rotation").toString().toLowerCase();
        Files.writeString(root().resolve("qa/forge-" + rotation + ".json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag source) {
        ListTag dimensions = source.getList("size", Tag.TAG_INT);
        require(dimensions.getInt(0) == 13 && dimensions.getInt(1) == 10 && dimensions.getInt(2) == 16, "Wrong template dimensions");
        require(!source.contains("palettes"), "Multiple palettes not permitted");
        require(source.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Entities not permitted");
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : source.getList("palette", Tag.TAG_COMPOUND)) {
            CompoundTag encoded = (CompoundTag) entry;
            ResourceLocation name = new ResourceLocation(encoded.getString("Name"));
            require(name.getNamespace().equals("minecraft") && ALLOWED.contains(name.getPath()), "Disallowed block: " + name);
            require(BuiltInRegistries.BLOCK.containsKey(name), "Unknown 1.20.1 block: " + name);
            Block block = BuiltInRegistries.BLOCK.get(name);
            CompoundTag properties = encoded.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid block property: " + name + " " + key);
            }
            palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded));
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        for (Tag entry : source.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag encoded = (CompoundTag) entry;
            ListTag position = encoded.getList("pos", Tag.TAG_INT);
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 13 && relative.getY() >= 0 && relative.getY() < 10 && relative.getZ() >= 0 && relative.getZ() < 16, "Out-of-bounds position");
            require(!encoded.contains("nbt"), "No block entities, inventories or loot permitted");
            int index = encoded.getInt("state");
            require(index >= 0 && index < palette.size(), "Invalid palette index");
            require(expected.put(relative, palette.get(index)) == null, "Duplicate position");
        }
        require(expected.size() == 2080, "Expected explicit complete volume");
        return expected;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getFluidState().isEmpty() && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean routeClear(ServerLevel level, Fixture fixture) {
        for (int posX = 5; posX <= 7; posX++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                BlockPos floor = fixture.world(new BlockPos(posX, 1, posZ));
                if (!level.getBlockState(floor).isCollisionShapeFullBlock(level, floor) || !level.getFluidState(floor).isEmpty()) return false;
                for (int posY = 2; posY <= 4; posY++) {
                    if (!clear(level, fixture.world(new BlockPos(posX, posY, posZ)))) return false;
                }
            }
        }
        return true;
    }

    private static boolean horizontalLayer(ServerLevel level, Fixture fixture, int posY) {
        for (int posX = 0; posX < 13; posX++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                BlockPos position = fixture.world(new BlockPos(posX, posY, posZ));
                if (!level.getBlockState(position).isCollisionShapeFullBlock(level, position)) return false;
            }
        }
        return true;
    }

    private static boolean waterEnvelope(BlockPos relative) {
        return relative.getY() == 1 && relative.getZ() >= 2 && relative.getZ() <= 13 && Set.of(3, 4, 8, 9).contains(relative.getX());
    }

    private static int escapedWater(ServerLevel level, Fixture fixture) {
        int escaped = 0;
        for (int posX = -1; posX <= 13; posX++) {
            for (int posY = -1; posY <= 10; posY++) {
                for (int posZ = -1; posZ <= 16; posZ++) {
                    BlockPos relative = new BlockPos(posX, posY, posZ);
                    if (!level.getFluidState(fixture.world(relative)).isEmpty() && !waterEnvelope(relative)) escaped++;
                }
            }
        }
        return escaped;
    }

    private static CompoundTag capture(ServerLevel level, Fixture fixture, CompoundTag source) {
        CompoundTag captured = source.copy();
        ListTag palette = new ListTag();
        Map<BlockState, Integer> indices = new HashMap<>();
        Rotation inverse = switch (fixture.rotation()) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> fixture.rotation();
        };
        for (Tag entry : captured.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag encoded = (CompoundTag) entry;
            ListTag position = encoded.getList("pos", Tag.TAG_INT);
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            BlockState state = level.getBlockState(fixture.world(relative)).rotate(inverse);
            Integer index = indices.get(state);
            if (index == null) {
                index = indices.size();
                indices.put(state, index);
                palette.add(NbtUtils.writeBlockState(state));
            }
            encoded.putInt("state", index);
        }
        captured.put("palette", palette);
        return captured;
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected) {
        int water = 0;
        int lanterns = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            BlockState wanted = entry.getValue().rotate(fixture.rotation());
            require(actual.equals(wanted), "State drift " + fixture.rotation() + " at " + entry.getKey() + ": " + actual + " expected " + wanted);
            require(actual.canSurvive(level, position), "Unsupported decoration: " + entry.getKey() + " " + actual);
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (actual.is(Blocks.WATER)) {
                require(actual.getFluidState().isSource(), "Water source became flow");
                require(waterEnvelope(entry.getKey()), "Water escaped basin");
                BlockPos below = position.below();
                require(level.getBlockState(below).isCollisionShapeFullBlock(level, below), "Water has no solid bed");
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbor = position.relative(direction);
                    require(level.getBlockState(neighbor).is(Blocks.WATER) || level.getBlockState(neighbor).isCollisionShapeFullBlock(level, neighbor), "Unsealed water neighbor");
                }
                water++;
            }
            if (actual.is(Blocks.SOUL_LANTERN)) lanterns++;
        }
        require(water == 42 && lanterns == 8, "Lost water sources or lanterns");
        require(routeClear(level, fixture), "3-wide/3-high dry level path obstructed");
        require(horizontalLayer(level, fixture, 0), "Basin foundation perforated");
        require(horizontalLayer(level, fixture, 8) && horizontalLayer(level, fixture, 9), "Incomplete roof");
        int escaped = escapedWater(level, fixture);
        require(escaped == 0, "Escaped water: " + escaped);
        BlockPos first = fixture.world(BlockPos.ZERO);
        BlockPos last = fixture.world(new BlockPos(12, 9, 15));
        AABB bounds = new AABB(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()), Math.max(first.getX(), last.getX()) + 1, last.getY() + 1, Math.max(first.getZ(), last.getZ()) + 1);
        require(level.getEntities(null, bounds).isEmpty(), "Dropped items or other unexpected entities");
        return Map.of("rotation", fixture.rotation().name(), "records_checked", expected.size(), "supported_blocks", expected.size(), "water_sources", water, "escaped_water", escaped, "lanterns", lanterns, "dry_route_cells", 48, "complete_roof", true, "no_block_entities_or_entities", true, "status", "PASS");
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void corridorNorth(GameTestHelper helper) throws Exception {
        verifyRotation(helper, Rotation.NONE);
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void corridorEast(GameTestHelper helper) throws Exception {
        verifyRotation(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void corridorSouth(GameTestHelper helper) throws Exception {
        verifyRotation(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void corridorWest(GameTestHelper helper) throws Exception {
        verifyRotation(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    private static void verifyRotation(GameTestHelper helper, Rotation requestedRotation) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("requested_rotation", requestedRotation.name());
        result.put("status", "RUNNING");
        try {
            Path input = root().resolve("room.nbt");
            String fingerprint = hash(input);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json"), StandardCharsets.UTF_8), JsonObject.class);
            require(metadata.get("sha256").getAsString().equals(fingerprint), "Metadata fingerprint mismatch");
            CompoundTag source = NbtIo.readCompressed(input.toFile());
            require(source.getInt("DataVersion") == 3465, "Wrong data version");
            Map<BlockPos, BlockState> expected = audit(source);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), source);
            require(template.getSize().equals(new Vec3i(13, 10, 16)), "Native template size mismatch");
            require(audit(template.save(new CompoundTag())).equals(expected), "Native NBT roundtrip mismatch");
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "This fixture may only run in isolated GameTestServer");
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            result.put("source_nbt_sha256", fingerprint);
            result.put("minecraft", "1.20.1");
            result.put("forge", net.minecraftforge.versions.forge.ForgeVersion.getVersion());
            result.put("java", System.getProperty("java.version"));
            result.put("isolated_run_directory", System.getProperty("user.dir"));
            result.put("fluid_ticks_enabled", true);
            result.put("random_tick_speed", 0);
            result.put("stable_snapshot_ticks", List.of(200, 240));
            result.put("records_per_rotation", expected.size());
            List<Fixture> fixtures = new ArrayList<>();
            int rotationIndex = requestedRotation.ordinal();
            for (Rotation rotation : List.of(requestedRotation)) {
                Fixture fixture = new Fixture(new BlockPos(128 + rotationIndex * 64, 192, 128), rotation);
                for (int chunkX = (fixture.origin().getX() - 24) >> 4; chunkX <= (fixture.origin().getX() + 24) >> 4; chunkX++) {
                    for (int chunkZ = (fixture.origin().getZ() - 24) >> 4; chunkZ <= (fixture.origin().getZ() + 24) >> 4; chunkZ++) {
                        level.setChunkForced(chunkX, chunkZ, true);
                    }
                }
                for (int posX = -2; posX <= 14; posX++) {
                    for (int posY = -2; posY <= 11; posY++) {
                        for (int posZ = -2; posZ <= 17; posZ++) {
                            level.setBlock(fixture.world(new BlockPos(posX, posY, posZ)), Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
                        }
                    }
                }
                BlockPos origin = fixture.origin();
                level.getEntities(null, new AABB(origin.getX() - 20, origin.getY() - 5, origin.getZ() - 20, origin.getX() + 25, origin.getY() + 15, origin.getZ() + 25)).forEach(entity -> entity.discard());
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placement failed");
                BlockPos obstruction = fixture.world(new BlockPos(6, 2, 3));
                level.setBlock(obstruction, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!routeClear(level, fixture), "Blocked-path negative probe missed");
                level.setBlock(obstruction, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos bed = fixture.world(new BlockPos(4, 0, 3));
                BlockState savedBed = level.getBlockState(bed);
                level.setBlock(bed, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!horizontalLayer(level, fixture, 0), "Perforated-floor negative probe missed");
                level.setBlock(bed, savedBed, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos anchor = fixture.world(new BlockPos(3, 6, 2));
                BlockState savedAnchor = level.getBlockState(anchor);
                level.setBlock(anchor, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lantern = fixture.world(new BlockPos(3, 5, 2));
                require(!level.getBlockState(lantern).canSurvive(level, lantern), "Missing-lantern-anchor negative probe missed");
                level.setBlock(anchor, savedAnchor, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                    if (!level.getFluidState(position).isEmpty()) level.scheduleTick(position, Fluids.WATER, 5);
                }
                fixtures.add(fixture);
                rotationIndex++;
            }
            result.put("negative_probes_per_rotation", List.of("blocked_path", "perforated_basin_floor", "missing_lantern_anchor"));
            Map<Rotation, CompoundTag> stableAt200 = new HashMap<>();
            helper.runAfterDelay(200, () -> {
                for (Fixture fixture : fixtures) stableAt200.put(fixture.rotation(), capture(level, fixture, source));
            });
            helper.runAfterDelay(240, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        rotations.add(verify(level, fixture, expected));
                        CompoundTag captured = capture(level, fixture, source);
                        require(captured.equals(stableAt200.get(fixture.rotation())), "Still changing between ticks 200 and 240");
                        NbtIo.writeCompressed(captured, root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase() + ".nbt").toFile());
                    }
                    result.put("rotations", rotations);
                    result.put("records_checked_after_ticks", expected.size());
                    result.put("stabilization_ticks", 240);
                    result.put("status", "PASS");
                    result.put("limitations", List.of("Isolated Forge 47.4.10 server, not full-modpack client, multiplayer or FPS acceptance.", "Random vine growth disabled; scheduled support/fluid ticks enabled.", "No deployment, FT core integration, hall connection, occupancy or idempotence tested."));
                    report(result);
                    System.out.println("S06_RECEIVER_GAME_TEST_PASS rotation=" + requestedRotation + " records=" + expected.size() + " ticks=240 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Throwable error) {
                    result.put("status", "FAIL");
                    result.put("error", error.toString());
                    try { report(result); } catch (Exception failure) { error.addSuppressed(failure); }
                    helper.fail(error.toString());
                }
            });
        } catch (Throwable error) {
            result.put("status", "FAIL");
            result.put("error", error.toString());
            report(result);
            helper.fail(error.toString());
        }
    }
}
