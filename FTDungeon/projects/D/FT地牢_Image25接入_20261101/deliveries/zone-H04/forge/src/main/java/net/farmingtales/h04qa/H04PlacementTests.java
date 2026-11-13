package net.farmingtales.h04qa;

import com.google.gson.GsonBuilder;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftzoneh04qa")
@GameTestHolder("ftzoneh04qa")
@PrefixGameTestTemplate(false)
public final class H04PlacementTests {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final BlockPos SIZE = new BlockPos(33, 23, 37);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.COBWEB, Blocks.POWDER_SNOW,
            Blocks.MAGMA_BLOCK, Blocks.CACTUS);

    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           BlockPos minimum, BlockPos maximum, List<BlockPos> water) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static BlockPos transform(int blockX, int blockY, int blockZ, Fixture fixture) {
        return transform(new BlockPos(blockX, blockY, blockZ), fixture.origin, fixture.rotation);
    }

    @GameTest(template = "empty", batch = "h04_native", timeoutTicks = 850)
    public static void fourRotationsAndFloodStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("h04.delivery"));
        Path source = root.resolve("room.nbt");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        require(JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject()
                .get("sha256").getAsString().equals(hash), "Metadata fingerprint mismatch");
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
            require(!FORBIDDEN.contains(state.getBlock()), "Forbidden block " + state);
            states.add(state);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        require(template.getSize().equals(SIZE), "Size mismatch");
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == 28083, "Expected full explicit volume");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == records.size(), "Native NBT roundtrip loss");
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(1024 + rotation.ordinal() * 128, 100, 1024);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            List<BlockPos> water = new ArrayList<>();
            for (int recordIndex = 0; recordIndex < records.size(); recordIndex++) {
                CompoundTag record = records.getCompound(recordIndex);
                require(!record.contains("nbt") || record.getCompound("nbt").getString("id").equals("minecraft:banner"), "Only decorative banner block entities allowed");
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getX() < 33 && local.getY() >= 0 && local.getY() < 23
                        && local.getZ() >= 0 && local.getZ() < 37, "Out of bounds");
                BlockPos world = transform(local, origin, rotation);
                BlockState state = states.get(record.getInt("state")).rotate(rotation);
                require(expected.put(world, state) == null, "Duplicate record");
                if (state.is(Blocks.WATER)) water.add(world);
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 22, Math.max(origin.getZ(), corner.getZ()));
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Occupied QA fixture " + position);
            }
            for (int chunkX = (minimum.getX() - 2) >> 4; chunkX <= (maximum.getX() + 2) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 2) >> 4; chunkZ <= (maximum.getZ() + 2) >> 4; chunkZ++) {
                    level.setChunkForced(chunkX, chunkZ, true);
                }
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true).setFinalizeEntities(false);
            require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placement failed");
            fixtures.add(new Fixture(rotation, origin, expected, minimum, maximum, water));
        }
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.setWeatherParameters(12000, 0, false, false);
        List<String> negatives = negativeProbes(level, fixtures.get(0));
        List<Map<String, Object>> initial = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            initial.add(inspect(level, fixture));
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
                level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
                if (entry.getValue().getBlock() instanceof FallingBlock) level.scheduleTick(entry.getKey(), entry.getValue().getBlock(), 1);
            }
            for (BlockPos sourcePosition : fixture.water) level.scheduleTick(sourcePosition, level.getFluidState(sourcePosition).getType(), 1);
            System.out.println("H04_NATIVE_INITIAL_PASS: " + fixture.rotation + "; " + initial.get(initial.size() - 1));
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
                report.put("evidence", "real isolated GameTestServer / ServerLevel / StructureTemplate.placeInWorld");
                report.put("rotations", 4);
                report.put("initial", initial);
                report.put("after_400_ticks", settled);
                report.put("negative_probes_rejected", negatives);
                report.put("neighbor_updates_dispatched", true);
                report.put("water_ticks_explicitly_scheduled", true);
                report.put("gravity_ticks_explicitly_scheduled", true);
                report.put("random_tick_speed", 3);
                report.put("fire_tick_enabled", true);
                report.put("production_save_used", false);
                report.put("full_modpack_playtest", false);
                Files.writeString(root.resolve("qa/forge-native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("H04_NATIVE_GAME_TEST_PASS: rotations=4/4; records=112332; ticks=400; sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                throw new IllegalStateException("H04 post-tick verification failed", exception);
            }
        });
    }


    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int fluids = 0;
        int lanterns = 0;
        int banners = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed " + position + ": " + entry.getValue() + " -> " + actual);
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native canSurvive failed " + position);
                supported++;
            }
            if (actual.is(Blocks.BLUE_WALL_BANNER)) {
                require(level.getBlockEntity(position) instanceof net.minecraft.world.level.block.entity.BannerBlockEntity, "Missing banner entity");
                CompoundTag banner = level.getBlockEntity(position).saveWithFullMetadata();
                require(banner.getList("Patterns", Tag.TAG_COMPOUND).size() == 2, "Banner pattern roundtrip loss");
                banners++;
            } else require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (actual.is(Blocks.LANTERN)) lanterns++;
            if (!level.getFluidState(position).isEmpty()) {
                require(actual.is(Blocks.WATER) && level.getFluidState(position).isSource(), "Unexpected fluid");
                fluids++;
            }
        }
        require(fluids == 40 && lanterns == 26 && banners == 16, "Feature count mismatch");
        architecture(level, fixture);
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary leak " + position);
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Unexpected item / falling block / entity");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("native_supported_blocks", supported);
        report.put("supported_lanterns", lanterns);
        report.put("banner_entities_with_preserved_patterns", banners);
        report.put("sealed_source_water", fluids);
        report.put("fluid_seal_faces", fluidSeal(level, fixture));
        report.put("spatial", spatial(level, fixture));
        report.put("continuous_grounded_pillars", 4);
        report.put("pillar_beam_joints", 4);
        report.put("closed_roof_cells", 1221);
        report.put("roof_anchored_pendant_lights", 5);
        report.put("unchanged_boundary_air", boundary);
        report.put("unwanted_entities", 0);
        return report;
    }

    private static int fluidSeal(ServerLevel level, Fixture fixture) {
        int faces = 0;
        for (BlockPos position : fixture.water) {
            for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)) {
                BlockPos neighbor = position.relative(direction);
                require(level.getBlockState(neighbor).is(Blocks.WATER) || level.getBlockState(neighbor).isFaceSturdy(level, neighbor, direction.getOpposite()), "Unsealed water face " + neighbor);
                faces++;
            }
        }
        return faces;
    }

    private static void sturdy(ServerLevel level, BlockPos position, String label) {
        require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), label + " " + position);
    }

    private static void architecture(ServerLevel level, Fixture fixture) {
        for (int blockX : List.of(7, 25)) for (int blockZ = 15; blockZ <= 29; blockZ++) {
            BlockState rail = level.getBlockState(transform(blockX, 10, blockZ, fixture));
            require(rail.is(Blocks.IRON_BARS) || rail.is(Blocks.CHISELED_STONE_BRICKS), "Gallery edge rail missing");
        }
        for (int blockX = 0; blockX < 33; blockX++) for (int blockZ = 0; blockZ < 37; blockZ++) {
            for (int blockY : List.of(0, 1, 21)) sturdy(level, transform(blockX, blockY, blockZ, fixture), "Floor/roof hole");
        }
        for (int centerX : List.of(9, 23)) for (int centerZ : List.of(10, 28)) {
            for (int blockY = 0; blockY <= 21; blockY++) sturdy(level, transform(centerX, blockY, centerZ, fixture), "Interrupted pillar");
            for (int offsetX = -1; offsetX <= 1; offsetX++) for (int offsetZ = -1; offsetZ <= 1; offsetZ++) for (int blockY = 4; blockY <= 17; blockY++) sturdy(level, transform(centerX + offsetX, blockY, centerZ + offsetZ, fixture), "Pillar shaft gap");
            for (int blockX = 1; blockX <= 31; blockX++) sturdy(level, transform(blockX, 20, centerZ, fixture), "Crossbeam gap");
            for (int blockZ = 1; blockZ <= 35; blockZ++) sturdy(level, transform(centerX, 20, blockZ, fixture), "Longitudinal beam gap");
        }
        for (int[] anchor : List.of(new int[]{4,17}, new int[]{28,17}, new int[]{4,23}, new int[]{28,23}, new int[]{16,32})) {
            for (int blockY = 15; blockY <= 20; blockY++) require(level.getBlockState(transform(anchor[0], blockY, anchor[1], fixture)).is(Blocks.CHAIN), "Pendant chain discontinuity");
            sturdy(level, transform(anchor[0], 21, anchor[1], fixture), "Pendant roof anchor absent");
        }
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        return level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty()
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY() + 0.001, feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static Set<BlockPos> reachable(Set<BlockPos> route, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        require(route.contains(start), "Route start is blocked " + start);
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

    private static Set<BlockPos> floorRoute(ServerLevel level, Fixture fixture, int elevation) {
        Set<BlockPos> route = new HashSet<>();
        for (int blockX = 0; blockX < 33; blockX++) for (int blockZ = 0; blockZ < 37; blockZ++) {
            BlockPos feet = transform(blockX, elevation, blockZ, fixture);
            if (walkable(level, feet)) route.add(feet);
        }
        return route;
    }

    private static Map<String, Object> spatial(ServerLevel level, Fixture fixture) {
        Set<BlockPos> ground = floorRoute(level, fixture, 2);
        Set<BlockPos> reached = reachable(ground, transform(16, 2, 0, fixture));
        List<int[]> points = List.of(new int[]{16,2,7}, new int[]{16,2,36}, new int[]{0,2,19}, new int[]{32,2,19}, new int[]{4,2,6}, new int[]{28,2,6});
        for (int[] point : points) require(reached.contains(transform(point[0], point[1], point[2], fixture)), "Disconnected ground safe point/port/stair");
        for (int offset = -1; offset <= 1; offset++) for (int depth = 0; depth < 2; depth++) {
            for (int[] point : List.of(new int[]{16+offset,2,depth}, new int[]{16+offset,2,36-depth}, new int[]{depth,2,19+offset}, new int[]{32-depth,2,19+offset})) {
                BlockPos feet = transform(point[0], point[1], point[2], fixture);
                require(walkable(level, feet), "Blocked three-wide port");
                for (int extraY = 0; extraY < 4; extraY++) require(level.isEmptyBlock(feet.above(extraY)), "Port head clearance");
            }
        }
        Set<BlockPos> upper = reachable(floorRoute(level, fixture, 10), transform(4, 10, 17, fixture));
        for (int[] point : List.of(new int[]{28,10,17}, new int[]{16,10,32}, new int[]{4,10,15}, new int[]{28,10,15})) require(upper.contains(transform(point[0], point[1], point[2], fixture)), "Disconnected upper gallery");
        int samples = stairSamples(level, fixture, 4) + stairSamples(level, fixture, 28);
        return Map.of("connected_ground_nodes", reached.size(), "connected_upper_nodes", upper.size(), "ports_3x4", 4, "two_way_safe_point", true, "stairs", 2, "player_size", "0.6 x 1.8", "stair_collision_samples", samples, "maximum_step", 0.5);
    }

    private static int stairSamples(ServerLevel level, Fixture fixture, int localX) {
        double previous = fixture.origin.getY() + 2.0;
        int samples = 0;
        for (double localZ = 6.1; localZ <= 16.1; localZ += 0.25) {
            double positionX;
            double positionZ;
            switch (fixture.rotation) {
                case CLOCKWISE_90 -> { positionX = 1.0 - localZ; positionZ = localX + 0.5; }
                case CLOCKWISE_180 -> { positionX = 1.0 - (localX + 0.5); positionZ = 1.0 - localZ; }
                case COUNTERCLOCKWISE_90 -> { positionX = localZ; positionZ = 1.0 - (localX + 0.5); }
                default -> { positionX = localX + 0.5; positionZ = localZ; }
            }
            positionX += fixture.origin.getX();
            positionZ += fixture.origin.getZ();
            double surface = fixture.origin.getY();
            for (int blockX = (int)Math.floor(positionX - 0.29); blockX <= (int)Math.floor(positionX + 0.29); blockX++) for (int blockZ = (int)Math.floor(positionZ - 0.29); blockZ <= (int)Math.floor(positionZ + 0.29); blockZ++) {
                for (int blockY = fixture.origin.getY(); blockY < fixture.origin.getY() + 10; blockY++) {
                    BlockPos position = new BlockPos(blockX, blockY, blockZ);
                    for (AABB localBox : level.getBlockState(position).getCollisionShape(level, position).toAabbs()) {
                        AABB shape = localBox.move(position);
                        if (shape.maxX > positionX - 0.29 && shape.minX < positionX + 0.29 && shape.maxZ > positionZ - 0.29 && shape.minZ < positionZ + 0.29) surface = Math.max(surface, shape.maxY);
                    }
                }
            }
            require(Math.abs(surface - previous) <= 0.60001, "Stair exceeds player step height");
            require(level.noCollision(new AABB(positionX - 0.29, surface + 0.001, positionZ - 0.29, positionX + 0.29, surface + 1.801, positionZ + 0.29)), "Stair player collision");
            previous = surface;
            samples++;
        }
        require(Math.abs(previous - (fixture.origin.getY() + 10)) < 0.01, "Stair does not reach gallery");
        return samples;
    }

    private static List<String> negativeProbes(ServerLevel level, Fixture fixture) {
        List<String> rejected = new ArrayList<>();
        probe(level, transform(16,1,0,fixture), Blocks.AIR.defaultBlockState(), () -> spatial(level,fixture), "missing_entrance_floor", rejected);
        probe(level, transform(16,2,0,fixture), Blocks.STONE.defaultBlockState(), () -> spatial(level,fixture), "blocked_entrance", rejected);
        probe(level, transform(12,2,19,fixture), Blocks.AIR.defaultBlockState(), () -> fluidSeal(level,fixture), "broken_fountain_dam", rejected);
        probe(level, transform(9,4,10,fixture), Blocks.AIR.defaultBlockState(), () -> architecture(level,fixture), "missing_pillar_segment", rejected);
        probe(level, transform(4,20,17,fixture), Blocks.AIR.defaultBlockState(), () -> architecture(level,fixture), "broken_pendant_anchor", rejected);
        probe(level, transform(16,21,7,fixture), Blocks.AIR.defaultBlockState(), () -> architecture(level,fixture), "roof_hole", rejected);
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
