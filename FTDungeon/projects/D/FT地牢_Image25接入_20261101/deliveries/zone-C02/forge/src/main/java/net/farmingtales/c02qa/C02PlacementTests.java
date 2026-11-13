package net.farmingtales.c02qa;

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
import java.util.zip.GZIPOutputStream;
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
import net.minecraft.world.Container;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftzonec02qa")
@GameTestHolder("ftzonec02qa")
@PrefixGameTestTemplate(false)
public final class C02PlacementTests {
    private static final BlockPos SIZE = new BlockPos(19, 11, 19);
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.COBWEB, Blocks.POWDER_SNOW,
            Blocks.MAGMA_BLOCK, Blocks.CACTUS);
    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           BlockPos minimum, BlockPos maximum, List<BlockPos> waters) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static BlockPos at(Fixture fixture, int blockX, int blockY, int blockZ) {
        return transform(new BlockPos(blockX, blockY, blockZ), fixture.origin, fixture.rotation);
    }

    @GameTest(template = "empty", batch = "c02_native", timeoutTicks = 900)
    public static void fourRotationsAndFloodStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("c02.delivery"));
        Path source = root.resolve("room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        require(JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject()
                .get("sha256").getAsString().equals(hash), "Metadata fingerprint mismatch");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected 1.20.1 single palette");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Template entities are forbidden");
        List<BlockState> states = new ArrayList<>();
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        for (int stateIndex = 0; stateIndex < palette.size(); stateIndex++) {
            CompoundTag entry = palette.getCompound(stateIndex);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            for (String key : entry.getCompound("Properties").getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(entry.getCompound("Properties").getString(key)).isPresent(), "Invalid property " + entry);
            }
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(!FORBIDDEN.contains(state.getBlock()), "Forbidden block " + state);
            states.add(state);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        require(template.getSize().equals(SIZE), "Size mismatch");
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == 3971, "Expected full explicit volume");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == records.size(), "Native NBT roundtrip loss");
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(2048 + rotation.ordinal() * 80, 100, 2048);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            List<BlockPos> waters = new ArrayList<>();
            for (int recordIndex = 0; recordIndex < records.size(); recordIndex++) {
                CompoundTag record = records.getCompound(recordIndex);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getX() < 19 && local.getY() >= 0 && local.getY() < 11
                        && local.getZ() >= 0 && local.getZ() < 19, "Out-of-bounds record");
                BlockState state = states.get(record.getInt("state")).rotate(rotation);
                if (record.contains("nbt")) {
                    CompoundTag payload = record.getCompound("nbt");
                    require(state.is(Blocks.BARREL) && payload.getString("id").equals("minecraft:barrel")
                            && payload.getList("Items", Tag.TAG_COMPOUND).isEmpty() && !payload.contains("LootTable"), "Unexpected container data");
                }
                BlockPos world = transform(local, origin, rotation);
                require(expected.put(world, state) == null, "Duplicate record");
                if (state.is(Blocks.WATER)) waters.add(world);
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 10, Math.max(origin.getZ(), corner.getZ()));
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Occupied QA fixture " + position);
            }
            for (int chunkX = (minimum.getX() - 2) >> 4; chunkX <= (maximum.getX() + 2) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 2) >> 4; chunkZ <= (maximum.getZ() + 2) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true).setFinalizeEntities(false);
            require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placement failed");
            fixtures.add(new Fixture(rotation, origin, expected, minimum, maximum, waters));
        }
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.setWeatherParameters(12000, 0, false, false);
        List<String> negatives = negativeProbes(level, fixtures.get(0));
        List<Map<String, Object>> initial = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            initial.add(inspect(level, fixture));
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
            for (BlockPos water : fixture.waters) level.scheduleTick(water, level.getFluidState(water).getType(), 1);
            System.out.println("C02_NATIVE_INITIAL_PASS: " + fixture.rotation + "; " + initial.get(initial.size() - 1));
        }
        helper.runAfterDelay(400, () -> {
            try {
                List<Map<String, Object>> settled = new ArrayList<>();
                for (Fixture fixture : fixtures) settled.add(inspect(level, fixture));
                Map<String, Object> capture = new LinkedHashMap<>();
                capture.put("sha256", hash);
                capture.put("records", capture(level, fixtures, records));
                Path capturePath = root.resolve("qa/native-capture.json.gz");
                try (GZIPOutputStream output = new GZIPOutputStream(Files.newOutputStream(capturePath))) {
                    output.write(new GsonBuilder().create().toJson(capture).getBytes(StandardCharsets.UTF_8));
                }
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
                report.put("random_tick_speed", 3);
                report.put("fire_tick_enabled", true);
                report.put("production_save_used", false);
                report.put("full_modpack_playtest", false);
                report.put("capture", "qa/native-capture.json.gz");
                report.put("capture_sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(capturePath))));
                Files.writeString(root.resolve("qa/forge-native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("C02_NATIVE_GAME_TEST_PASS: rotations=4/4; records=15884; ticks=400; sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                throw new IllegalStateException("C02 post-tick verification failed", exception);
            }
        });
    }

    private static List<Map<String, Object>> capture(ServerLevel level, List<Fixture> fixtures, ListTag records) {
        List<Map<String, Object>> captured = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            for (int recordIndex = 0; recordIndex < records.size(); recordIndex++) {
                ListTag coordinates = records.getCompound(recordIndex).getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                BlockPos world = transform(local, fixture.origin, fixture.rotation);
                CompoundTag state = NbtUtils.writeBlockState(level.getBlockState(world));
                Map<String, String> properties = new LinkedHashMap<>();
                for (String property : state.getCompound("Properties").getAllKeys()) properties.put(property, state.getCompound("Properties").getString(property));
                captured.add(Map.of("rotation", fixture.rotation.toString(), "local", List.of(local.getX(), local.getY(), local.getZ()),
                        "world", List.of(world.getX(), world.getY(), world.getZ()), "name", state.getString("Name"), "properties", properties));
            }
        }
        return captured;
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int waters = 0;
        int barrels = 0;
        int lanterns = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed " + position + ": " + entry.getValue() + " -> " + actual);
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native canSurvive failed " + position);
                supported++;
            }
            BlockEntity blockEntity = level.getBlockEntity(position);
            if (actual.is(Blocks.BARREL)) {
                require(blockEntity instanceof BarrelBlockEntity && ((Container) blockEntity).isEmpty(), "Barrel content/type mismatch");
                require(!blockEntity.saveWithFullMetadata().contains("LootTable"), "Unexpected barrel loot table");
                barrels++;
            } else require(blockEntity == null, "Unexpected block entity");
            if (actual.is(Blocks.LANTERN)) lanterns++;
            if (!level.getFluidState(position).isEmpty()) {
                require(actual.is(Blocks.WATER) && level.getFluidState(position).isSource(), "Unexpected fluid or waterlogging");
                waters++;
            }
        }
        require(waters == 131 && barrels == 2 && lanterns == 5, "Feature count mismatch");
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary leak " + position);
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Unexpected entity/item");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("supported_native_blocks", supported);
        report.put("source_water_blocks", waters);
        report.put("sealed_water_faces", fluidSeal(level, fixture));
        report.put("dry_player_positions", spatial(level, fixture));
        report.put("empty_barrels", barrels);
        report.put("supported_lanterns", lanterns);
        report.put("unchanged_boundary_air", boundary);
        report.put("two_way_route", true);
        report.put("north_east_turn", true);
        return report;
    }

    private static int fluidSeal(ServerLevel level, Fixture fixture) {
        int faces = 0;
        for (BlockPos water : fixture.waters) {
            for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)) {
                BlockPos neighbor = water.relative(direction);
                require(level.getBlockState(neighbor).is(Blocks.WATER) || level.getBlockState(neighbor).isFaceSturdy(level, neighbor, direction.getOpposite()), "Unsealed water face " + neighbor);
                faces++;
            }
        }
        return faces;
    }

    private static int spatial(ServerLevel level, Fixture fixture) {
        Set<BlockPos> route = new HashSet<>();
        for (int blockX = 0; blockX < 19; blockX++) {
            for (int blockZ = 0; blockZ < 19; blockZ++) {
                if ((blockX >= 8 && blockX <= 10 && blockZ <= 10) || (blockX >= 8 && blockZ >= 8 && blockZ <= 10)) {
                    BlockPos feet = at(fixture, blockX, 2, blockZ);
                    require(level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP), "Route floor missing " + feet);
                    require(level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty(), "Wet player route " + feet);
                    require(level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                            feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8)), "Blocked player route " + feet);
                    require(level.getBlockState(feet.above(2)).getCollisionShape(level, feet.above(2)).isEmpty(), "Three-high headroom obstruction");
                    route.add(feet);
                }
                if (blockX == 0 || blockX == 18 || blockZ == 0 || blockZ == 18) {
                    for (int blockY = 1; blockY <= 9; blockY++) {
                        boolean port = blockY >= 2 && blockY <= 5
                                && ((blockZ == 0 && blockX >= 8 && blockX <= 10) || (blockX == 18 && blockZ >= 8 && blockZ <= 10));
                        BlockPos shell = at(fixture, blockX, blockY, blockZ);
                        require(port ? level.isEmptyBlock(shell) : level.getBlockState(shell).isFaceSturdy(level, shell, Direction.UP), "Shell/port mismatch " + shell);
                    }
                }
                for (int blockY : List.of(0, 10)) {
                    BlockPos surface = at(fixture, blockX, blockY, blockZ);
                    require(level.getBlockState(surface).isFaceSturdy(level, surface, Direction.UP), "Incomplete floor/roof " + surface);
                }
            }
        }
        BlockPos entrance = at(fixture, 9, 2, 0);
        BlockPos exit = at(fixture, 18, 2, 9);
        require(route.size() == 57 && reachable(route, entrance).contains(exit) && reachable(route, exit).contains(entrance), "No two-way corner path");
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
        probe(level, at(fixture, 9, 1, 9), Blocks.AIR.defaultBlockState(), () -> spatial(level, fixture), "missing_turn_floor", rejected);
        probe(level, at(fixture, 9, 2, 9), Blocks.STONE.defaultBlockState(), () -> spatial(level, fixture), "blocked_turn_route", rejected);
        probe(level, at(fixture, 7, 1, 4), Blocks.AIR.defaultBlockState(), () -> fluidSeal(level, fixture), "broken_water_dam", rejected);
        probe(level, at(fixture, 9, 7, 4), Blocks.AIR.defaultBlockState(), () -> {
            BlockPos lantern = at(fixture, 9, 6, 4);
            require(level.getBlockState(lantern).canSurvive(level, lantern), "Lantern lost chain support");
        }, "missing_lantern_chain", rejected);
        require(rejected.size() == 4, "Negative probe missed a defect");
        return rejected;
    }

    private static void probe(ServerLevel level, BlockPos position, BlockState replacement, Runnable validation,
                              String name, List<String> rejected) {
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

