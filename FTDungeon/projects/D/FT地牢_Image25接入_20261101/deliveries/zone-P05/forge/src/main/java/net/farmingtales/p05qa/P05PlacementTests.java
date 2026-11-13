package net.farmingtales.p05qa;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
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
import java.util.UUID;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.MoverType;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftzonep05qa")
@GameTestHolder("ftzonep05qa")
@PrefixGameTestTemplate(false)
public final class P05PlacementTests {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final BlockPos SIZE = new BlockPos(17, 14, 29);
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

    private static Vec3 point(double blockX, double blockY, double blockZ, Fixture fixture) {
        Vec3 rotated = switch (fixture.rotation) {
            case NONE -> new Vec3(blockX, blockY, blockZ);
            case CLOCKWISE_90 -> new Vec3(1 - blockZ, blockY, blockX);
            case CLOCKWISE_180 -> new Vec3(1 - blockX, blockY, 1 - blockZ);
            case COUNTERCLOCKWISE_90 -> new Vec3(blockZ, blockY, 1 - blockX);
        };
        return rotated.add(fixture.origin.getX(), fixture.origin.getY(), fixture.origin.getZ());
    }

    private static Vec3 delta(double blockX, double blockY, double blockZ, Fixture fixture) {
        return point(blockX, blockY, blockZ, fixture).subtract(point(0, 0, 0, fixture));
    }

    @GameTest(template = "empty", batch = "p05_native", timeoutTicks = 900)
    public static void fourRotationsParkourAndRecovery(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("p05.delivery"));
        Path source = root.resolve("room.nbt");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        require(JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject()
                .get("sha256").getAsString().equals(hash), "Metadata hash mismatch");
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected 1.20.1 single palette");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Entities forbidden");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int stateIndex = 0; stateIndex < palette.size(); stateIndex++) {
            CompoundTag entry = palette.getCompound(stateIndex);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String propertyName : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(propertyName);
                require(property != null && property.getValue(properties.getString(propertyName)).isPresent(), "Invalid block state " + entry);
            }
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(!FORBIDDEN.contains(state.getBlock()), "Forbidden block " + state);
            states.add(state);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        require(template.getSize().equals(SIZE), "Size mismatch");
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == 6902, "Expected complete explicit volume");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == records.size(), "Native NBT roundtrip loss");
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(1024 + rotation.ordinal() * 96, 300, 1024);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            List<BlockPos> water = new ArrayList<>();
            for (int recordIndex = 0; recordIndex < records.size(); recordIndex++) {
                CompoundTag record = records.getCompound(recordIndex);
                require(!record.contains("nbt"), "Block entities forbidden");
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getX() < 17 && local.getY() >= 0 && local.getY() < 14
                        && local.getZ() >= 0 && local.getZ() < 29, "Out of bounds");
                BlockPos world = transform(local, origin, rotation);
                BlockState state = states.get(record.getInt("state")).rotate(rotation);
                require(expected.put(world, state) == null, "Duplicate coordinate");
                if (!state.getFluidState().isEmpty()) water.add(world);
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 13, Math.max(origin.getZ(), corner.getZ()));
            for (int chunkX = (minimum.getX() - 2) >> 4; chunkX <= (maximum.getX() + 2) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 2) >> 4; chunkZ <= (maximum.getZ() + 2) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
            }
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Occupied QA fixture " + position);
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
            for (BlockPos water : fixture.water) level.scheduleTick(water, level.getFluidState(water).getType(), 1);
            System.out.println("P05_NATIVE_INITIAL_PASS: " + fixture.rotation + "; records=" + fixture.expected.size() + "; jumps=12; recovery=304");
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
                report.put("evidence", "Isolated Forge GameTestServer / ServerLevel / StructureTemplate / FakePlayer swept Entity.move");
                report.put("rotations", 4);
                report.put("records_per_phase", 27608);
                report.put("stability_ticks", 400);
                report.put("initial", initial);
                report.put("after_400_ticks", settled);
                report.put("negative_probes_rejected", negatives);
                report.put("neighbor_updates_dispatched", true);
                report.put("fluid_ticks_explicitly_scheduled", true);
                report.put("random_tick_speed", 3);
                report.put("fire_tick_enabled", true);
                report.put("route_model", Map.of("player_width", 0.6, "player_height", 1.8,
                        "jump_impulse", 0.42, "gravity", 0.08, "vertical_drag", 0.98,
                        "controlled_horizontal_speed", 0.27, "climb_speed", 0.2));
                report.put("route_limits", "Deterministic ballistic and climb clearance checks with native swept collisions; keyboard input, acceleration, water motor, full modpack and multiplayer are not playtested.");
                report.put("production_save_used", false);
                report.put("production_files_modified", false);
                report.put("full_modpack_client_playtest", false);
                Files.writeString(root.resolve("qa/forge-native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("P05_NATIVE_GAME_TEST_PASS: rotations=4/4; records=27608; jumps=96; ticks=400; sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                throw new IllegalStateException("P05 post-tick verification failed", exception);
            }
        });
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int stableNonAir = 0;
        int waterCount = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed " + position + ": " + entry.getValue() + " -> " + actual);
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native canSurvive failed " + position);
                stableNonAir++;
            }
            if (!level.getFluidState(position).isEmpty()) {
                require(level.getFluidState(position).isSource(), "Flowing or unexpected fluid " + position);
                waterCount++;
            }
        }
        require(waterCount == 304, "Fluid cell count changed");
        validatePillars(level, fixture);
        validatePorts(level, fixture);
        validateHangers(level, fixture);
        int fluidFaces = validateFluid(level, fixture);
        Map<String, Object> recovery = validateRecovery(level, fixture);
        List<Map<String, Object>> forward = validateRoute(level, fixture, false);
        List<Map<String, Object>> backward = validateRoute(level, fixture, true);
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary leak " + position);
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Unexpected entity or dropped fixture");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("stable_non_air", stableNonAir);
        report.put("water_cells", waterCount);
        report.put("sealed_fluid_faces", fluidFaces);
        report.put("rooted_pillars", 5);
        report.put("complete_floor_and_roof", true);
        report.put("ports_open", true);
        report.put("forward_jumps", forward);
        report.put("reverse_jumps", backward);
        report.put("recovery", recovery);
        report.put("unchanged_boundary_air", boundary);
        report.put("unwanted_entities", 0);
        return report;
    }

    private static void validatePillars(ServerLevel level, Fixture fixture) {
        for (int startZ : List.of(6, 10, 14, 18, 22)) {
            for (int blockX = 7; blockX <= 8; blockX++) {
                for (int blockZ = startZ; blockZ <= startZ + 1; blockZ++) {
                    for (int blockY = 0; blockY <= 4; blockY++) {
                        BlockPos position = transform(blockX, blockY, blockZ, fixture);
                        require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Unrooted pillar " + position);
                    }
                }
            }
        }
    }

    private static void validatePorts(ServerLevel level, Fixture fixture) {
        for (int portZ : List.of(0, 28)) {
            for (int blockX = 7; blockX <= 9; blockX++) {
                BlockPos feet = transform(blockX, 5, portZ, fixture);
                require(level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP), "Port floor missing");
                for (int blockY = 5; blockY <= 7; blockY++) require(level.getBlockState(transform(blockX, blockY, portZ, fixture)).isAir(), "Port obstruction");
                require(level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8)), "Native port collision");
            }
        }
        for (int blockX = 0; blockX <= 16; blockX++) {
            for (int blockZ = 0; blockZ <= 28; blockZ++) {
                for (int blockY : List.of(0, 12)) {
                    BlockPos position = transform(blockX, blockY, blockZ, fixture);
                    require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Floor/roof hole " + position);
                }
            }
        }
    }

    private static void validateHangers(ServerLevel level, Fixture fixture) {
        for (int lampX : List.of(4, 12)) {
            for (int lampZ : List.of(8, 14, 20)) {
                require(level.getBlockState(transform(lampX, 8, lampZ, fixture)).is(Blocks.LANTERN), "Missing hanging lantern");
                for (int blockY = 9; blockY <= 10; blockY++) require(level.getBlockState(transform(lampX, blockY, lampZ, fixture)).is(Blocks.CHAIN), "Broken chain");
                BlockPos anchor = transform(lampX, 11, lampZ, fixture);
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Missing hanger anchor");
            }
        }
    }

    private static int validateFluid(ServerLevel level, Fixture fixture) {
        int faces = 0;
        for (BlockPos position : fixture.water) {
            require(level.getFluidState(position).isSource(), "Missing water source");
            for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)) {
                BlockPos neighbor = position.relative(direction);
                require(!level.getFluidState(neighbor).isEmpty()
                        || level.getBlockState(neighbor).isFaceSturdy(level, neighbor, direction.getOpposite()), "Unsealed water " + neighbor);
                faces++;
            }
        }
        return faces;
    }

    private static FakePlayer probePlayer(ServerLevel level, Vec3 start) {
        FakePlayer player = new FakePlayer(level, new GameProfile(UUID.fromString("7093da45-9000-4000-8000-000000000005"), "P05RouteProbe"));
        require(Math.abs(player.getBbWidth() - 0.6) < 0.0001 && Math.abs(player.getBbHeight() - 1.8) < 0.0001, "Unexpected player dimensions");
        player.setPos(start);
        player.setOnGround(true);
        player.setDeltaMovement(Vec3.ZERO);
        require(level.noCollision(player, player.getBoundingBox()), "Player starts in collision");
        return player;
    }

    private static void walkTo(ServerLevel level, FakePlayer player, Vec3 target) {
        for (int step = 0; step < 400; step++) {
            Vec3 offset = target.subtract(player.position()).multiply(1, 0, 1);
            if (offset.length() < 0.00001) return;
            Vec3 motion = offset.normalize().scale(Math.min(0.1, offset.length())).add(0, -0.08, 0);
            player.setDeltaMovement(motion);
            player.move(MoverType.SELF, motion);
            require(!player.horizontalCollision && player.onGround(), "Grounded route walk obstructed " + player.position());
            require(level.noCollision(player, player.getBoundingBox().deflate(0.0000001)), "Grounded route collision " + player.position() + "; target=" + target + "; box=" + player.getBoundingBox());
        }
        throw new IllegalStateException("Grounded walk did not reach waypoint");
    }

    private static List<Map<String, Object>> validateRoute(ServerLevel level, Fixture fixture, boolean reverse) {
        FakePlayer player = probePlayer(level, point(8, 5, reverse ? 28.5 : 0.5, fixture));
        List<Map<String, Object>> jumps = new ArrayList<>();
        for (int leg = 0; leg < 6; leg++) {
            double launchZ = reverse ? 26.4 - leg * 4 : 3.6 + leg * 4;
            walkTo(level, player, point(8, 5, launchZ, fixture));
            Vec3 horizontal = delta(0, 0, reverse ? -0.27 : 0.27, fixture);
            double vertical = 0.42;
            double maximumY = player.getY();
            Vec3 launch = player.position();
            List<List<Double>> trajectory = new ArrayList<>();
            boolean landed = false;
            int ticks = 0;
            player.setOnGround(false);
            for (int tick = 1; tick <= 20; tick++) {
                Vec3 motion = horizontal.add(0, vertical, 0);
                player.setDeltaMovement(motion);
                player.move(MoverType.SELF, motion);
                require(!player.horizontalCollision, "Jump hit horizontal obstruction at leg " + (leg + 1));
                require(level.noCollision(player, player.getBoundingBox().deflate(0.0000001)), "Jump body collision " + player.position());
                require(player.getY() >= fixture.origin.getY() + 5 - 0.00001, "Jump fell below target level at leg " + (leg + 1));
                maximumY = Math.max(maximumY, player.getY());
                trajectory.add(List.of(player.getX(), player.getY(), player.getZ()));
                if (player.onGround() && tick > 1) {
                    landed = true;
                    ticks = tick;
                    break;
                }
                require(!player.verticalCollision, "Jump hit ceiling");
                vertical = (vertical - 0.08) * 0.98;
            }
            require(landed, "Jump did not land at leg " + (leg + 1));
            double expectedZ = launchZ + (reverse ? -1 : 1) * 0.27 * ticks;
            Vec3 expected = point(8, 5, expectedZ, fixture);
            require(player.position().distanceTo(expected) < 0.0001, "Unexpected native landing");
            int floorZ = (int) Math.floor(expectedZ);
            int targetMinimum = reverse ? (leg == 5 ? 0 : 22 - leg * 4) : (leg == 5 ? 26 : 6 + leg * 4);
            int targetMaximum = reverse ? (leg == 5 ? 3 : 23 - leg * 4) : (leg == 5 ? 28 : 7 + leg * 4);
            require(floorZ >= targetMinimum && floorZ <= targetMaximum, "Landed outside destination platform");
            BlockPos below = BlockPos.containing(player.getX(), player.getY() - 0.01, player.getZ());
            require(level.getBlockState(below).isFaceSturdy(level, below, Direction.UP), "Landing lacks sturdy floor");
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("leg", leg + 1);
            result.put("launch", List.of(launch.x, launch.y, launch.z));
            result.put("landing", List.of(player.getX(), player.getY(), player.getZ()));
            result.put("air_ticks", ticks);
            result.put("horizontal_distance", 0.27 * ticks);
            result.put("rise", maximumY - launch.y);
            result.put("empty_block_gap", 2);
            result.put("trajectory", trajectory);
            jumps.add(result);
        }
        walkTo(level, player, point(8, 5, reverse ? 0.5 : 28.5, fixture));
        return jumps;
    }

    private static Map<String, Object> validateRecovery(ServerLevel level, Fixture fixture) {
        Set<BlockPos> water = new HashSet<>(fixture.water);
        for (BlockPos feet : water) {
            require(feet.getY() == fixture.origin.getY() + 1, "Unexpected basin depth");
            require(level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP), "Basin floor missing");
            require(level.getFluidState(feet.above()).isEmpty() && level.getFluidState(feet.above(2)).isEmpty(), "Submerged recovery headroom");
            require(level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                    feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8)), "Recovery wading obstruction");
        }
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(transform(3, 1, 4, fixture));
        while (!queue.isEmpty()) {
            BlockPos position = queue.remove();
            if (!water.contains(position) || !visited.add(position)) continue;
            for (Direction direction : Direction.Plane.HORIZONTAL) queue.add(position.relative(direction));
        }
        require(visited.size() == 304, "A fall-recovery area cannot reach north ladder");
        List<Map<String, Object>> climbs = new ArrayList<>();
        for (int ladderX : List.of(3, 13)) {
            for (int blockY = 1; blockY <= 4; blockY++) {
                BlockPos position = transform(ladderX, blockY, 4, fixture);
                BlockState state = level.getBlockState(position);
                require(state.is(Blocks.LADDER) && state.is(BlockTags.CLIMBABLE) && state.canSurvive(level, position), "Recovery ladder missing or unsupported");
            }
            FakePlayer player = probePlayer(level, point(ladderX + 0.5, 1, 4.5, fixture));
            for (int tick = 0; tick < 20; tick++) {
                Vec3 motion = delta(0, 0.2, -0.1, fixture);
                player.setDeltaMovement(motion);
                player.move(MoverType.SELF, motion);
                require(level.noCollision(player, player.getBoundingBox().deflate(0.0000001)), "Ladder shaft collision " + player.position());
            }
            require(Math.abs(player.getY() - fixture.origin.getY() - 5) < 0.0001, "Ladder failed to reach bank height");
            walkTo(level, player, point(ladderX + 0.5, 5, 3.5, fixture));
            climbs.add(Map.of("ladder_x", ladderX, "climb_sweep_ticks", 20, "returned_to_bank", true));
        }
        return Map.of("reachable_wading_cells", visited.size(), "one_block_deep", true, "dry_headspace", true, "climb_sweeps", climbs);
    }

    private static List<String> negativeProbes(ServerLevel level, Fixture fixture) {
        List<String> rejected = new ArrayList<>();
        probe(level, transform(7, 1, 10, fixture), () -> validatePillars(level, fixture), "missing_pillar_root", rejected);
        probe(level, transform(0, 1, 6, fixture), () -> validateFluid(level, fixture), "broken_water_dam", rejected);
        probe(level, transform(3, 3, 3, fixture), () -> validateRecovery(level, fixture), "missing_ladder_support", rejected);
        probe(level, transform(4, 11, 8, fixture), () -> validateHangers(level, fixture), "missing_lamp_anchor", rejected);
        BlockPos port = transform(8, 5, 0, fixture);
        probe(level, port, Blocks.STONE.defaultBlockState(), () -> validatePorts(level, fixture), "blocked_port", rejected);
        probe(level, transform(8, 6, 9, fixture), Blocks.STONE.defaultBlockState(), () -> validateRoute(level, fixture, false), "blocked_jump_arc", rejected);
        require(rejected.size() == 6, "Negative probe failed to detect a defect");
        return rejected;
    }

    private static void probe(ServerLevel level, BlockPos position, Runnable validation, String name, List<String> rejected) {
        probe(level, position, Blocks.AIR.defaultBlockState(), validation, name, rejected);
    }

    private static void probe(ServerLevel level, BlockPos position, BlockState replacement, Runnable validation, String name, List<String> rejected) {
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
