package net.farmingtales.c03qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.GameRules;
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

@GameTestHolder("ftc03qa")
@PrefixGameTestTemplate(false)
public final class ZoneTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
    private static final Set<String> ALLOWED = Set.of("air", "stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "chiseled_stone_bricks", "polished_andesite", "mossy_stone_brick_stairs", "cut_sandstone", "chiseled_sandstone", "sandstone", "candle", "flower_pot", "dark_oak_log", "dark_oak_planks", "potted_fern", "potted_brown_mushroom", "barrel", "red_wall_banner", "chain", "lantern", "rooted_dirt", "hanging_roots", "vine", "moss_block", "moss_carpet", "azalea_leaves");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private static Path root() { return Path.of(System.getProperty("ftc03qa.root")).toAbsolutePath().normalize(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static String hash(Path file) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))); }
    private static void report(Map<String, Object> result) throws Exception { Files.writeString(root().resolve("qa/forge-native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8); }

    private static Map<BlockPos, BlockState> audit(CompoundTag source) {
        ListTag size = source.getList("size", Tag.TAG_INT);
        require(size.size() == 3 && size.getInt(0) == 19 && size.getInt(1) == 12 && size.getInt(2) == 19, "Wrong dimensions");
        require(source.getInt("DataVersion") == 3465 && !source.contains("palettes"), "Wrong version or multiple palettes");
        require(source.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected template entities");
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : source.getList("palette", Tag.TAG_COMPOUND)) {
            CompoundTag encoded = (CompoundTag) entry;
            ResourceLocation name = new ResourceLocation(encoded.getString("Name"));
            require(name.getNamespace().equals("minecraft") && ALLOWED.contains(name.getPath()) && BuiltInRegistries.BLOCK.containsKey(name), "Unknown or forbidden block: " + name);
            Block block = BuiltInRegistries.BLOCK.get(name);
            CompoundTag properties = encoded.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid state: " + name + " " + key);
            }
            palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded));
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        int barrels = 0;
        int banners = 0;
        for (Tag entry : source.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag encoded = (CompoundTag) entry;
            ListTag position = encoded.getList("pos", Tag.TAG_INT);
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 19 && relative.getY() >= 0 && relative.getY() < 12 && relative.getZ() >= 0 && relative.getZ() < 19, "Out of bounds");
            int index = encoded.getInt("state");
            require(index >= 0 && index < palette.size(), "Invalid palette index");
            BlockState state = palette.get(index);
            require(expected.put(relative, state) == null, "Duplicate record");
            if (encoded.contains("nbt")) {
                CompoundTag blockNbt = encoded.getCompound("nbt");
                require(!blockNbt.contains("LootTable") && !blockNbt.contains("LootTableSeed") && blockNbt.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Inventory or loot table found");
                if (state.is(Blocks.BARREL)) { require(blockNbt.getString("id").equals("minecraft:barrel"), "Barrel entity mismatch"); barrels++; }
                else { require(state.is(Blocks.RED_WALL_BANNER) && blockNbt.getString("id").equals("minecraft:banner"), "Unexpected block entity"); banners++; }
            }
        }
        require(expected.size() == 4332 && barrels == 3 && banners == 1, "Incomplete explicit volume or decorative entities");
        return expected;
    }

    private static boolean solid(ServerLevel level, BlockPos position) { return level.getBlockState(position).isCollisionShapeFullBlock(level, position) && level.getFluidState(position).isEmpty(); }
    private static boolean clear(ServerLevel level, BlockPos position) { return level.getBlockState(position).getCollisionShape(level, position).isEmpty() && level.getFluidState(position).isEmpty(); }
    private static boolean vineBackingValid(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        int faces = 0;
        for (Direction direction : new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP}) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(direction.getName());
            if (property != null && state.getValue(property).toString().equals("true")) {
                faces++;
                if (!solid(level, position.relative(direction))) return false;
            }
        }
        return faces > 0;
    }
    private static boolean routeCell(int east, int south) { return east >= 7 && east <= 11 && south <= 11 || east >= 9 && south >= 7 && south <= 11; }
    private static boolean routeClear(ServerLevel level, Fixture fixture) {
        for (int east = 0; east < 19; east++) for (int south = 0; south < 19; south++) {
            if (!routeCell(east, south)) continue;
            if (!solid(level, fixture.world(new BlockPos(east, 1, south)))) return false;
            for (int up = 2; up <= 4; up++) if (!clear(level, fixture.world(new BlockPos(east, up, south)))) return false;
        }
        return true;
    }
    private static Set<BlockPos> reachable(ServerLevel level, Fixture fixture) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(new BlockPos(9, 2, 0));
        visited.add(queue.peek());
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() < 0 || next.getX() > 18 || next.getZ() < 0 || next.getZ() > 18 || visited.contains(next)) continue;
                BlockPos world = fixture.world(next);
                if (solid(level, world.below()) && clear(level, world) && clear(level, world.above())) { visited.add(next); queue.add(next); }
            }
        }
        return visited;
    }
    private static boolean statueGrounded(ServerLevel level, Fixture fixture) {
        for (int up = 1; up <= 7; up++) if (!solid(level, fixture.world(new BlockPos(5, up, 15)))) return false;
        return true;
    }
    private static CompoundTag capture(ServerLevel level, Fixture fixture, CompoundTag source) {
        StructureTemplate captured = new StructureTemplate();
        BlockPos first = fixture.world(BlockPos.ZERO);
        BlockPos last = fixture.world(new BlockPos(18, 11, 18));
        BlockPos minimum = new BlockPos(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()));
        captured.fillFromWorld(level, minimum, new Vec3i(19, 12, 19), false, null);
        CompoundTag result = captured.save(new CompoundTag());
        result.putInt("DataVersion", 3465);
        return result;
    }
    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected) {
        int vines = 0;
        int lamps = 0;
        int blockEntities = 0;
        int supported = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            BlockState wanted = entry.getValue().rotate(fixture.rotation());
            require(actual.equals(wanted), "State drift " + fixture.rotation() + " " + entry.getKey() + ": " + actual + " expected " + wanted);
            require(actual.canSurvive(level, position), "Unsupported decoration " + fixture.rotation() + " " + entry.getKey() + " " + actual);
            require(actual.getFluidState().isEmpty(), "Unexpected fluid");
            supported++;
            if (actual.is(Blocks.VINE)) { require(vineBackingValid(level, position), "Vine face has no direct solid backing"); vines++; }
            if (actual.is(Blocks.LANTERN)) {
                lamps++;
                for (int up = 1; up <= 3; up++) require(level.getBlockState(position.above(up)).is(Blocks.CHAIN), "Broken suspension chain");
                require(solid(level, position.above(4)), "Lamp chain has no roof anchor");
            }
            BlockEntity blockEntity = level.getBlockEntity(position);
            if (actual.hasBlockEntity()) {
                require(blockEntity != null, "Missing block entity");
                CompoundTag encoded = blockEntity.saveWithFullMetadata();
                require(!encoded.contains("LootTable") && !encoded.contains("LootTableSeed"), "World loot table appeared");
                if (actual.is(Blocks.BARREL)) require(blockEntity instanceof Container && ((Container) blockEntity).isEmpty(), "Barrel not empty");
                else require(actual.is(Blocks.RED_WALL_BANNER), "Unexpected block entity type");
                blockEntities++;
            } else require(blockEntity == null, "Stray block entity");
        }
        require(lamps == 4 && vines == 58 && blockEntities == 4, "Decorative count mismatch " + vines);
        require(routeClear(level, fixture), "Five-wide three-high corner route blocked");
        require(statueGrounded(level, fixture), "Shrine statue has no foundation");
        Set<BlockPos> visited = reachable(level, fixture);
        require(visited.contains(new BlockPos(9, 2, 4)) && visited.contains(new BlockPos(13, 2, 9)) && visited.contains(new BlockPos(18, 2, 9)), "Safe point or exit unreachable");
        for (int up : new int[] {0, 1, 10, 11}) for (int east = 0; east < 19; east++) for (int south = 0; south < 19; south++) require(solid(level, fixture.world(new BlockPos(east, up, south))), "Incomplete foundation/roof");
        BlockPos first = fixture.world(BlockPos.ZERO);
        BlockPos last = fixture.world(new BlockPos(18, 11, 18));
        AABB bounds = new AABB(Math.min(first.getX(), last.getX()) - 1, first.getY() - 1, Math.min(first.getZ(), last.getZ()) - 1, Math.max(first.getX(), last.getX()) + 2, last.getY() + 2, Math.max(first.getZ(), last.getZ()) + 2);
        require(level.getEntities(null, bounds).isEmpty(), "Dropped items or unexpected entities");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name()); result.put("status", "PASS"); result.put("records_checked", expected.size()); result.put("supported_states_checked", supported); result.put("vines", vines); result.put("lanterns", lamps); result.put("block_entities", blockEntities); result.put("empty_barrels", 3); result.put("reachable_feet_cells", visited.size()); result.put("route_cells", 95); result.put("complete_two_layer_roof", true); result.put("zero_fluids_entities_drops", true);
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void cornerFourRotations(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "RUNNING");
        try {
            Path input = root().resolve("room.nbt");
            String fingerprint = hash(input);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json"), StandardCharsets.UTF_8), JsonObject.class);
            require(metadata.get("sha256").getAsString().equals(fingerprint), "Metadata hash mismatch");
            CompoundTag source = NbtIo.readCompressed(input.toFile());
            Map<BlockPos, BlockState> expected = audit(source);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), source);
            require(template.getSize().equals(new Vec3i(19, 12, 19)), "Native template size mismatch");
            CompoundTag roundTrip = template.save(new CompoundTag());
            roundTrip.putInt("DataVersion", 3465);
            require(audit(roundTrip).equals(expected), "Native structure round trip mismatch");
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Only isolated GameTestServer is allowed");
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            result.put("source_nbt_sha256", fingerprint); result.put("minecraft", "1.20.1"); result.put("forge", net.minecraftforge.versions.forge.ForgeVersion.getVersion()); result.put("java", System.getProperty("java.version")); result.put("isolated_run_directory", System.getProperty("user.dir")); result.put("records_per_rotation", expected.size()); result.put("scheduled_neighbor_ticks", true); result.put("random_tick_speed", 0); result.put("snapshot_ticks", List.of(0, 200, 240));
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> probes = new ArrayList<>();
            int rotationIndex = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(new BlockPos(128 + rotationIndex++ * 64, 192, 128), rotation);
                for (int chunkX = (fixture.origin().getX() - 24) >> 4; chunkX <= (fixture.origin().getX() + 24) >> 4; chunkX++) for (int chunkZ = (fixture.origin().getZ() - 24) >> 4; chunkZ <= (fixture.origin().getZ() + 24) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
                for (int east = -2; east <= 20; east++) for (int up = -2; up <= 13; up++) for (int south = -2; south <= 20; south++) level.setBlock(fixture.world(new BlockPos(east, up, south)), Blocks.AIR.defaultBlockState(), FLAGS);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false), level.random, FLAGS), "placeInWorld failed");
                verify(level, fixture, expected);
                BlockPos blocker = fixture.world(new BlockPos(9, 2, 3));
                level.setBlock(blocker, Blocks.STONE_BRICKS.defaultBlockState(), FLAGS);
                require(!routeClear(level, fixture), "Route blocker probe not detected");
                level.setBlock(blocker, Blocks.AIR.defaultBlockState(), FLAGS);
                BlockPos vineBacking = fixture.world(new BlockPos(1, 5, 8));
                BlockState savedBacking = level.getBlockState(vineBacking);
                level.setBlock(vineBacking, Blocks.AIR.defaultBlockState(), FLAGS);
                BlockPos vinePosition = fixture.world(new BlockPos(2, 5, 8));
                require(!vineBackingValid(level, vinePosition), "Unsupported vine face probe not detected");
                level.setBlock(vineBacking, savedBacking, FLAGS);
                BlockPos statueFoot = fixture.world(new BlockPos(5, 2, 15));
                BlockState savedFoot = level.getBlockState(statueFoot);
                level.setBlock(statueFoot, Blocks.AIR.defaultBlockState(), FLAGS);
                require(!statueGrounded(level, fixture), "Floating statue probe not detected");
                level.setBlock(statueFoot, savedFoot, FLAGS);
                BlockPos chain = fixture.world(new BlockPos(3, 7, 9));
                BlockState savedChain = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS);
                BlockPos lamp = fixture.world(new BlockPos(3, 6, 9));
                require(!level.getBlockState(lamp).canSurvive(level, lamp), "Broken hanging lamp probe not detected");
                level.setBlock(chain, savedChain, FLAGS);
                probes.add(Map.of("rotation", rotation.name(), "blocked_route_detected", true, "unsupported_vine_detected", true, "floating_statue_detected", true, "broken_lantern_chain_detected", true, "fixture_restored", true));
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.getBlockState(position).updateNeighbourShapes(level, position, FLAGS);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                fixtures.add(fixture);
            }
            result.put("negative_probes", probes);
            report(result);
            Map<Rotation, CompoundTag> snapshots = new LinkedHashMap<>();
            helper.runAfterDelay(200, () -> {
                for (Fixture fixture : fixtures) snapshots.put(fixture.rotation(), capture(level, fixture, source));
            });
            helper.runAfterDelay(240, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    List<Map<String, Object>> captures = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        rotations.add(verify(level, fixture, expected));
                        CompoundTag captured = capture(level, fixture, source);
                        require(captured.equals(snapshots.get(fixture.rotation())), "World changed between ticks 200 and 240");
                        StructureTemplate reloaded = new StructureTemplate();
                        reloaded.load(BuiltInRegistries.BLOCK.asLookup(), captured);
                        require(reloaded.getSize().equals(new Vec3i(19, 12, 19)), "Captured template reload failed");
                        Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase() + ".nbt");
                        NbtIo.writeCompressed(captured, output.toFile());
                        captures.add(Map.of("rotation", fixture.rotation().name(), "file", "qa/" + output.getFileName(), "sha256", hash(output), "native_reload", true));
                    }
                    result.put("rotations", rotations); result.put("world_captures", captures); result.put("records_checked_after_ticks", 4 * expected.size()); result.put("stable_between_200_and_240", true); result.put("status", "PASS");
                    result.put("limitations", List.of("Isolated Forge GameTest, not a full modpack client, multiplayer or FPS test.", "Natural vine growth disabled while support/neighbor scheduled ticks run.", "No deployment or FT core integration, hall connectivity, production occupancy or persistence test."));
                    report(result);
                    System.out.println("C03_NATIVE_GAME_TEST_PASS rotations=4/4 records=" + 4 * expected.size() + " ticks=240 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Throwable error) {
                    result.put("status", "FAIL"); result.put("error", error.toString());
                    try { report(result); } catch (Exception failure) { error.addSuppressed(failure); }
                    helper.fail(error.toString());
                }
            });
        } catch (Throwable error) {
            result.put("status", "FAIL"); result.put("error", error.toString()); report(result); helper.fail(error.toString());
        }
    }
}

