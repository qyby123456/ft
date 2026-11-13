package net.farmingtales.s20qa;

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

@Mod("ftcorridors20qa")
@GameTestHolder("ftcorridors20qa")
@PrefixGameTestTemplate(false)
public final class S20PlacementTests {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final BlockPos SIZE = new BlockPos(11, 9, 16);
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

    @GameTest(template = "empty", batch = "s20_native", timeoutTicks = 850)
    public static void fourRotationsAndFloodStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("s20.delivery"));
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
        require(records.size() == 1584, "Expected full explicit volume");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == records.size(), "Native NBT roundtrip loss");
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(1024 + rotation.ordinal() * 64, 100, 1024);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            List<BlockPos> water = new ArrayList<>();
            for (int recordIndex = 0; recordIndex < records.size(); recordIndex++) {
                CompoundTag record = records.getCompound(recordIndex);
                require(!record.contains("nbt"), "Block entities are forbidden");
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getX() < 11 && local.getY() >= 0 && local.getY() < 9
                        && local.getZ() >= 0 && local.getZ() < 16, "Out of bounds");
                BlockPos world = transform(local, origin, rotation);
                BlockState state = states.get(record.getInt("state")).rotate(rotation);
                require(expected.put(world, state) == null, "Duplicate record");
                if (state.is(Blocks.WATER)) water.add(world);
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 8, Math.max(origin.getZ(), corner.getZ()));
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
            System.out.println("S20_NATIVE_INITIAL_PASS: " + fixture.rotation + "; " + initial.get(initial.size() - 1));
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
                System.out.println("S20_NATIVE_GAME_TEST_PASS: rotations=4/4; records=6336; ticks=400; sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                throw new IllegalStateException("S20 post-tick verification failed", exception);
            }
        });
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int torches = 0;
        int rails = 0;
        int fluids = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed " + position + ": " + entry.getValue() + " -> " + actual);
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native canSurvive failed " + position);
                supported++;
            }
            if (actual.is(Blocks.WALL_TORCH)) torches++;
            if (actual.is(Blocks.RAIL)) rails++;
            if (!level.getFluidState(position).isEmpty()) {
                require(actual.is(Blocks.WATER) && level.getFluidState(position).isSource(), "Unexpected fluid");
                fluids++;
            }
        }
        require(fluids == 44 && torches == 8 && rails == 16, "Feature count mismatch");
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary leak " + position);
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Unexpected entity / item / falling block");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("native_supported_blocks", supported);
        report.put("supported_wall_torches", torches);
        report.put("stable_dry_rails", rails);
        report.put("sealed_source_water", fluids);
        report.put("fluid_seal_faces", fluidSeal(level, fixture));
        report.put("dry_player_positions", spatial(level, fixture));
        report.put("both_ports_open", true);
        report.put("two_way_route", true);
        report.put("unchanged_boundary_air", boundary);
        report.put("unwanted_entities", 0);
        return report;
    }

    private static int fluidSeal(ServerLevel level, Fixture fixture) {
        int faces = 0;
        for (BlockPos position : fixture.water) {
            for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)) {
                BlockPos neighbor = position.relative(direction);
                require(level.getBlockState(neighbor).is(Blocks.WATER)
                        || level.getBlockState(neighbor).isFaceSturdy(level, neighbor, direction.getOpposite()), "Unsealed water face " + neighbor);
                faces++;
            }
        }
        return faces;
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        return level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty()
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static int spatial(ServerLevel level, Fixture fixture) {
        Set<BlockPos> route = new HashSet<>();
        for (int blockZ = 0; blockZ < 16; blockZ++) {
            for (int blockX = 4; blockX <= 6; blockX++) {
                BlockPos feet = transform(blockX, 2, blockZ, fixture);
                require(walkable(level, feet), "Blocked dry 0.6x1.8 player route " + feet);
                require(level.getBlockState(feet.above(2)).getCollisionShape(level, feet.above(2)).isEmpty(), "Three-high route obstruction");
                route.add(feet);
            }
        }
        BlockPos entrance = transform(5, 2, 0, fixture);
        BlockPos exit = transform(5, 2, 15, fixture);
        require(reachable(route, entrance).contains(exit), "No entry-to-exit path");
        require(reachable(route, exit).contains(entrance), "No return path");
        for (int blockX = 0; blockX < 11; blockX++) {
            for (int blockZ = 0; blockZ < 16; blockZ++) {
                for (int blockY : List.of(0, 8)) {
                    BlockPos position = transform(blockX, blockY, blockZ, fixture);
                    require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Floor/roof hole");
                }
            }
        }
        return route.size();
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
        probe(level, transform(5, 1, 7, fixture), Blocks.AIR.defaultBlockState(), () -> spatial(level, fixture), "missing_route_floor", rejected);
        probe(level, transform(5, 2, 7, fixture), Blocks.STONE.defaultBlockState(), () -> spatial(level, fixture), "blocked_route", rejected);
        probe(level, transform(3, 1, 3, fixture), Blocks.AIR.defaultBlockState(), () -> fluidSeal(level, fixture), "broken_water_dam", rejected);
        probe(level, transform(1, 4, 5, fixture), Blocks.AIR.defaultBlockState(), () -> {
            BlockPos torch = transform(2, 4, 5, fixture);
            require(level.getBlockState(torch).canSurvive(level, torch), "Torch lost support");
        }, "missing_torch_support", rejected);
        require(rejected.size() == 4, "Negative probe missed a defect");
        return rejected;
    }

    private static void probe(ServerLevel level, BlockPos position, BlockState replacement, Runnable validation,
                              String name, List<String> rejected) {
        BlockState previous = level.getBlockState(position);
        try {
            level.setBlock(position, replacement, FLAGS);
            boolean detected = false;
            try {
                validation.run();
            } catch (IllegalStateException expected) {
                detected = true;
            }
            require(detected, "Negative probe was not rejected " + name);
            rejected.add(name);
        } finally {
            level.setBlock(position, previous, FLAGS);
        }
    }
}
