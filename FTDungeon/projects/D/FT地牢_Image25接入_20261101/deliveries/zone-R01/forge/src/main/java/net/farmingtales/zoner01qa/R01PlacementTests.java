package net.farmingtales.zoner01qa;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftzoner01qa")
@GameTestHolder("ftzoner01qa")
@PrefixGameTestTemplate(false)
public final class R01PlacementTests {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final BlockPos SIZE = new BlockPos(25, 17, 25);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.COBWEB,
            Blocks.POWDER_SNOW, Blocks.MAGMA_BLOCK, Blocks.CACTUS);
    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           Map<BlockPos, CompoundTag> expectedNbt, BlockPos minimum, BlockPos maximum, JsonObject routes) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }
    private static BlockPos transform(int blockX, int blockY, int blockZ, Fixture fixture) {
        return transform(new BlockPos(blockX, blockY, blockZ), fixture.origin, fixture.rotation);
    }
    private static Vec3 transform(double centerX, double feetY, double centerZ, Fixture fixture) {
        return StructureTemplate.transform(new Vec3(centerX, feetY, centerZ), Mirror.NONE, fixture.rotation, BlockPos.ZERO)
                .add(fixture.origin.getX(), fixture.origin.getY(), fixture.origin.getZ());
    }

    @GameTest(template = "empty", batch = "zoner01_native", timeoutTicks = 1000)
    public static void fourRotationsAndCollapseStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("zoneR01.delivery"));
        Path source = root.resolve("room.nbt");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        require(metadata.get("id").getAsString().equals("zone-R01"), "Wrong independent room ID");
        require(metadata.get("sha256").getAsString().equals(hash), "Metadata fingerprint mismatch");
        JsonObject routes = JsonParser.parseString(Files.readString(root.resolve("qa/fixture.json"))).getAsJsonObject();
        require(routes.get("sha256").getAsString().equals(hash), "Collision fixture fingerprint mismatch");
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected 1.20.1 single palette");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Template entities are forbidden");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int stateIndex = 0; stateIndex < palette.size(); stateIndex++) {
            CompoundTag entry = palette.getCompound(stateIndex);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            require(!FORBIDDEN.contains(block) && !(block instanceof FallingBlock), "Forbidden or unstable block " + identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid state " + entry);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        require(template.getSize().equals(SIZE), "Size mismatch");
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == 10625, "Expected full explicit volume");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == records.size(), "Native roundtrip loss");
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.setWeatherParameters(6000, 0, false, false);
        level.setDayTime(6000);
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(2048 + rotation.ordinal() * 96, 256, 2048);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Map<BlockPos, CompoundTag> expectedNbt = new LinkedHashMap<>();
            for (int recordIndex = 0; recordIndex < records.size(); recordIndex++) {
                CompoundTag record = records.getCompound(recordIndex);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getX() < 25 && local.getY() >= 0 && local.getY() < 17 && local.getZ() >= 0 && local.getZ() < 25, "Out of bounds");
                BlockPos world = transform(local, origin, rotation);
                BlockState state = states.get(record.getInt("state")).rotate(rotation);
                require(expected.put(world, state) == null, "Duplicate record");
                if (record.contains("nbt")) {
                    require(state.is(Blocks.BARREL) || state.is(Blocks.RED_WALL_BANNER), "Unexpected block entity type");
                    CompoundTag entity = record.getCompound("nbt");
                    require(!entity.contains("LootTable") && entity.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Unexpected rewards");
                    expectedNbt.put(world, entity.copy());
                }
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 16, Math.max(origin.getZ(), corner.getZ()));
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Occupied isolated fixture " + position);
            }
            for (int chunkX = (minimum.getX() - 2) >> 4; chunkX <= (maximum.getX() + 2) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 2) >> 4; chunkZ <= (maximum.getZ() + 2) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placement failed");
            fixtures.add(new Fixture(rotation, origin, expected, expectedNbt, minimum, maximum, routes));
        }
        List<String> negatives = negativeProbes(level, fixtures.get(0));
        List<Map<String, Object>> initial = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            initial.add(inspect(level, fixture));
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
                level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
                if (entry.getValue().getBlock() instanceof FallingBlock) level.scheduleTick(entry.getKey(), entry.getValue().getBlock(), 1);
            }
            System.out.println("ZONE_R01_INITIAL_PASS " + fixture.rotation + " " + initial.get(initial.size() - 1));
        }
        helper.runAfterDelay(400, () -> {
            try {
                List<Map<String, Object>> settled = new ArrayList<>();
                for (Fixture fixture : fixtures) settled.add(inspect(level, fixture));
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("status", "PASS");
                report.put("id", "zone-R01");
                report.put("sha256", hash);
                report.put("minecraft", "1.20.1");
                report.put("forge", "47.4.10");
                report.put("evidence", "real isolated GameTestServer / ServerLevel / StructureTemplate.placeInWorld");
                report.put("rotations", 4);
                report.put("records_checked_per_snapshot", 42500);
                report.put("ticks", 400);
                report.put("initial", initial);
                report.put("after_400_ticks", settled);
                report.put("negative_probes_rejected", negatives);
                report.put("neighbor_updates_dispatched", true);
                report.put("random_tick_speed", 3);
                report.put("fire_tick_enabled", true);
                report.put("production_save_used", false);
                report.put("full_modpack_playtest", false);
                Files.writeString(root.resolve("qa/forge-native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("ZONE_R01_NATIVE_GAME_TEST_PASS rotations=4/4 records=42500 ticks=400 sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) { throw new IllegalStateException("zone-R01 post-tick verification failed", exception); }
        });
    }
    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int lanterns = 0;
        int barrels = 0;
        int banners = 0;
        int stairs = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed " + position + ": " + entry.getValue() + " -> " + actual);
            require(level.getFluidState(position).isEmpty(), "Unexpected fluid");
            if (!actual.isAir()) { require(actual.canSurvive(level, position), "Native canSurvive failed " + position); supported++; }
            BlockEntity entity = level.getBlockEntity(position);
            if (fixture.expectedNbt.containsKey(position)) {
                require(entity != null, "Missing block entity " + position);
                CompoundTag original = fixture.expectedNbt.get(position);
                CompoundTag saved = entity.saveWithFullMetadata();
                require(saved.getString("id").equals(original.getString("id")), "Block entity ID changed");
                require(!saved.contains("LootTable") && saved.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Unexpected container items or rewards");
                if (original.contains("Patterns")) require(saved.getList("Patterns", Tag.TAG_COMPOUND).equals(original.getList("Patterns", Tag.TAG_COMPOUND)), "Banner pattern changed");
            } else require(entity == null, "Unexpected block entity " + position);
            if (actual.is(Blocks.LANTERN)) lanterns++;
            if (actual.is(Blocks.BARREL)) barrels++;
            if (actual.is(Blocks.RED_WALL_BANNER)) banners++;
            if (actual.is(Blocks.STONE_BRICK_STAIRS)) stairs++;
        }
        require(lanterns == 6 && barrels == 4 && banners == 2 && stairs == 27, "Feature count mismatch");
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary changed " + position);
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Unexpected entity / item / falling block");
        structural(level, fixture);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("native_supported_blocks", supported);
        report.put("supported_lanterns", lanterns);
        report.put("empty_barrels", barrels);
        report.put("patterned_wall_banners", banners);
        report.put("supported_stone_stairs", stairs);
        report.put("player_collision_samples", spatial(level, fixture));
        report.put("two_way_ground_route", true);
        report.put("continuous_three_wide_stairs", true);
        report.put("guarded_mezzanine_hole", true);
        report.put("columns_connected_to_foundation", true);
        report.put("unchanged_boundary_air", boundary);
        report.put("unwanted_entities", 0);
        return report;
    }

    private static void structural(ServerLevel level, Fixture fixture) {
        for (int columnX : List.of(5, 17)) {
            for (int blockY = 0; blockY <= 9; blockY++) {
                BlockPos position = transform(columnX, blockY, 10, fixture);
                require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Missing column support " + position);
            }
        }
        for (int blockZ = 5; blockZ <= 8; blockZ++) {
            for (int blockX = 9; blockX <= 14; blockX++) require(level.isEmptyBlock(transform(blockX, 10, blockZ, fixture)), "Mezzanine collapse filled in");
            for (int blockX : List.of(8, 15)) require(level.getBlockState(transform(blockX, 11, blockZ, fixture)).is(Blocks.SPRUCE_FENCE), "Missing hole guardrail");
        }
        for (int blockX = 8; blockX <= 15; blockX++) {
            for (int blockZ : List.of(4, 9)) require(level.getBlockState(transform(blockX, 11, blockZ, fixture)).is(Blocks.SPRUCE_FENCE), "Missing hole guardrail");
        }
        for (int stepIndex = 0; stepIndex < 9; stepIndex++) {
            for (int blockX = 19; blockX <= 21; blockX++) {
                for (int blockY = 1; blockY < 2 + stepIndex; blockY++) {
                    BlockPos support = transform(blockX, blockY, 19 - stepIndex, fixture);
                    require(level.getBlockState(support).isFaceSturdy(level, support, Direction.UP), "Stair support missing");
                }
            }
            for (int railX : List.of(18, 22)) require(level.getBlockState(transform(railX, 3 + stepIndex, 19 - stepIndex, fixture)).is(Blocks.SPRUCE_FENCE), "Stair railing missing");
        }
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            if (entry.getValue().is(Blocks.CHAIN)) {
                BlockPos anchor = entry.getKey().above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN)) anchor = anchor.above();
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Chain lacks solid upper anchor");
            }
        }
    }

    private static boolean walkable(ServerLevel level, Vec3 feet) {
        AABB player = new AABB(feet.x - 0.3, feet.y + 0.0001, feet.z - 0.3, feet.x + 0.3, feet.y + 1.8, feet.z + 0.3);
        AABB support = new AABB(feet.x - 0.29, feet.y - 0.01, feet.z - 0.29, feet.x + 0.29, feet.y, feet.z + 0.29);
        return level.noCollision(player) && !level.noCollision(support) && level.getFluidState(BlockPos.containing(feet)).isEmpty();
    }

    private static int spatial(ServerLevel level, Fixture fixture) {
        Set<BlockPos> ground = new HashSet<>();
        int samples = 0;
        for (String routeName : List.of("ground_route", "upper_route", "stair_route")) {
            JsonArray route = fixture.routes.getAsJsonArray(routeName);
            for (int sampleIndex = 0; sampleIndex < route.size(); sampleIndex++) {
                JsonArray coordinates = route.get(sampleIndex).getAsJsonArray();
                Vec3 feet = transform(coordinates.get(0).getAsDouble(), coordinates.get(1).getAsDouble(), coordinates.get(2).getAsDouble(), fixture);
                require(walkable(level, feet), "Blocked/unsupported 0.6x1.8 player " + routeName + " sample=" + coordinates + " world=" + feet);
                if (!routeName.equals("stair_route")) {
                    require(level.noCollision(new AABB(feet.x - 0.3, feet.y + 1.8, feet.z - 0.3, feet.x + 0.3, feet.y + 2.99, feet.z + 0.3)), "Three-high clearance lost");
                }
                if (routeName.equals("ground_route")) ground.add(BlockPos.containing(feet));
                samples++;
            }
        }
        BlockPos entrance = transform(12, 2, 0, fixture);
        BlockPos landing = transform(12, 2, 12, fixture);
        BlockPos stairBase = transform(20, 2, 21, fixture);
        require(reachable(ground, entrance).contains(landing) && reachable(ground, entrance).contains(stairBase), "Entry/landing/stair base disconnected");
        require(reachable(ground, stairBase).contains(entrance), "No return route");
        for (int blockX = 0; blockX < 25; blockX++) {
            for (int blockZ = 0; blockZ < 25; blockZ++) {
                BlockPos floor = transform(blockX, 1, blockZ, fixture);
                require(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Foundation hole");
            }
        }
        return samples;
    }

    private static Set<BlockPos> reachable(Set<BlockPos> route, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos position = queue.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = position.relative(direction);
                if (route.contains(neighbor) && visited.add(neighbor)) queue.add(neighbor);
            }
        }
        return visited;
    }

    private static List<String> negativeProbes(ServerLevel level, Fixture fixture) {
        List<String> rejected = new ArrayList<>();
        probe(level, transform(12, 1, 6, fixture), Blocks.AIR.defaultBlockState(), () -> spatial(level, fixture), "missing_route_floor", rejected);
        probe(level, transform(12, 2, 6, fixture), Blocks.STONE.defaultBlockState(), () -> spatial(level, fixture), "blocked_route", rejected);
        probe(level, transform(5, 5, 10, fixture), Blocks.AIR.defaultBlockState(), () -> structural(level, fixture), "missing_primary_column", rejected);
        probe(level, transform(8, 11, 6, fixture), Blocks.AIR.defaultBlockState(), () -> structural(level, fixture), "missing_collapse_guardrail", rejected);
        probe(level, transform(8, 6, 10, fixture), Blocks.AIR.defaultBlockState(), () -> {
            BlockPos lamp = transform(8, 5, 10, fixture);
            require(level.getBlockState(lamp).canSurvive(level, lamp), "Lantern lost support");
        }, "missing_lantern_chain", rejected);
        probe(level, transform(20, 5, 14, fixture), Blocks.AIR.defaultBlockState(), () -> structural(level, fixture), "missing_stair_wedge_support", rejected);
        require(rejected.size() == 6, "Negative probe missed a defect");
        return rejected;
    }

    private static void probe(ServerLevel level, BlockPos position, BlockState replacement, Runnable validation, String name, List<String> rejected) {
        BlockState previous = level.getBlockState(position);
        try {
            level.setBlock(position, replacement, FLAGS);
            boolean detected = false;
            try { validation.run(); } catch (IllegalStateException expected) { detected = true; }
            require(detected, "Negative probe was not rejected " + name);
            rejected.add(name);
        } finally { level.setBlock(position, previous, FLAGS); }
    }
}
