package net.farmingtales.p02qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import net.minecraft.world.level.ChunkPos;
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
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftp02qa")
@PrefixGameTestTemplate(false)
public final class P02GameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(17, 11, 39);
    private static final int RECORDS = 7293;
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final int[][] SPANS = {{1, 4}, {6, 8}, {10, 12}, {14, 16}, {18, 21}, {23, 25}, {27, 29}, {31, 33}, {35, 38}};
    private static final Set<String> ALLOWED = Set.of("air", "deepslate_bricks", "stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "chiseled_stone_bricks", "polished_andesite", "stone_brick_stairs", "dark_oak_fence", "chain", "lantern", "water", "glass", "barrel", "cyan_wall_banner");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
        Vec3 vector(double posX, double posY, double posZ) {
            return switch (rotation) {
                case NONE -> new Vec3(posX, posY, posZ);
                case CLOCKWISE_90 -> new Vec3(-posZ, posY, posX);
                case CLOCKWISE_180 -> new Vec3(-posX, posY, -posZ);
                case COUNTERCLOCKWISE_90 -> new Vec3(posZ, posY, -posX);
            };
        }
        Vec3 point(double posX, double posY, double posZ) {
            return vector(posX - 0.5, posY, posZ - 0.5).add(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
        }
        Vec3 relative(Vec3 position) {
            Vec3 delta = position.subtract(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
            return switch (rotation) {
                case NONE -> delta.add(0.5, 0, 0.5);
                case CLOCKWISE_90 -> new Vec3(delta.z + 0.5, delta.y, -delta.x + 0.5);
                case CLOCKWISE_180 -> new Vec3(-delta.x + 0.5, delta.y, -delta.z + 0.5);
                case COUNTERCLOCKWISE_90 -> new Vec3(-delta.z + 0.5, delta.y, delta.x + 0.5);
            };
        }
    }

    private static final class ProbePlayer extends FakePlayer {
        ProbePlayer(ServerLevel level) {
            super(level, new GameProfile(UUID.fromString("3abfdbd1-6b84-4566-b6f1-9294c5a28502"), "P02RouteProbe"));
        }
        void launch() { jumpFromGround(); }
    }

    private static Path root() { return Path.of(System.getProperty("ftp02qa.root")).toAbsolutePath().normalize(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static String hash(Path file) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))); }
    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(report) + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    private static Map<BlockPos, BlockState> decode(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require Java 1.20.1 single-palette structure");
        ListTag dimensions = data.getList("size", Tag.TAG_INT);
        require(dimensions.getInt(0) == 17 && dimensions.getInt(1) == 11 && dimensions.getInt(2) == 39, "Dimensions differ");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Preplaced entity");
        ListTag paletteTags = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < paletteTags.size(); index++) {
            CompoundTag encoded = paletteTags.getCompound(index);
            ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && ALLOWED.contains(identity.getPath()) && BuiltInRegistries.BLOCK.containsKey(identity), "Forbidden/unknown block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = encoded.getCompound("Properties");
            require(properties.size() == block.getStateDefinition().getProperties().size(), "Incomplete properties " + identity);
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identity + " " + key);
            }
            palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded));
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        ListTag entries = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 17 && relative.getY() >= 0 && relative.getY() < 11 && relative.getZ() >= 0 && relative.getZ() < 39, "Coordinate outside bounds");
            int stateIndex = entry.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Invalid palette reference");
            BlockState state = palette.get(stateIndex);
            require(expected.put(relative, state) == null, "Duplicate coordinate");
            if (entry.contains("nbt")) {
                CompoundTag payload = entry.getCompound("nbt");
                require(state.is(Blocks.BARREL) || state.is(Blocks.CYAN_WALL_BANNER), "Unexpected block entity");
                require(!payload.contains("LootTable") && payload.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Container carries loot");
            }
        }
        require(expected.size() == RECORDS, "Full volume missing");
        return expected;
    }

    private static AABB body(Vec3 feet) { return new AABB(feet.x - 0.3, feet.y + 0.00001, feet.z - 0.3, feet.x + 0.3, feet.y + 1.8, feet.z + 0.3); }
    private static void airBody(ServerLevel level, Fixture fixture, double posX, double posY, double posZ) {
        require(level.noCollision(body(fixture.point(posX, posY, posZ))), "Player collision at relative " + List.of(posX, posY, posZ));
    }
    private static void floor(ServerLevel level, Fixture fixture, int posX, int posY, int posZ) {
        BlockPos position = fixture.world(new BlockPos(posX, posY, posZ));
        require(level.getBlockState(position).isCollisionShapeFullBlock(level, position), "Missing full support " + position);
    }
    private static void sealsAndPorts(ServerLevel level, Fixture fixture) {
        for (int posX = 0; posX < 17; posX++) for (int posZ = 0; posZ < 39; posZ++) for (int posY : new int[]{0, 1, 10}) floor(level, fixture, posX, posY, posZ);
        for (int posZ : new int[]{0, 38}) for (int posX = 7; posX <= 9; posX++) {
            floor(level, fixture, posX, 2, posZ);
            for (int posY = 3; posY <= 5; posY++) require(level.getBlockState(fixture.world(new BlockPos(posX, posY, posZ))).isAir(), "Blocked 3x3 port");
        }
        for (int posX = 0; posX < 17; posX++) for (int posZ = 0; posZ < 39; posZ++) {
            if (posX != 0 && posX != 16 && posZ != 0 && posZ != 38) continue;
            for (int posY = 2; posY < 10; posY++) {
                if (posX >= 7 && posX <= 9 && posY >= 3 && posY <= 5 && (posZ == 0 || posZ == 38)) continue;
                floor(level, fixture, posX, posY, posZ);
            }
        }
        for (BlockPos relative : BlockPos.betweenClosed(-2, -2, -2, 18, 12, 40)) {
            boolean outside = relative.getX() < 0 || relative.getX() >= 17 || relative.getY() < 0 || relative.getY() >= 11 || relative.getZ() < 0 || relative.getZ() >= 39;
            if (outside) require(level.getFluidState(fixture.world(relative)).isEmpty(), "Fluid escaped template " + relative);
        }
        airBody(level, fixture, 8.5, 3, 0.5);
        airBody(level, fixture, 8.5, 3, 3.5);
        airBody(level, fixture, 8.5, 3, 19.5);
        airBody(level, fixture, 8.5, 3, 38.5);
    }

    private static List<Map<String, Object>> jumps(ServerLevel level, Fixture fixture) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (int direction : new int[]{1, -1}) for (int leg = 0; leg < 8; leg++) {
            int from = direction > 0 ? leg : 8 - leg;
            int target = from + direction;
            double startZ = direction > 0 ? SPANS[from][1] + 0.72 : SPANS[from][0] + 0.28;
            ProbePlayer player = new ProbePlayer(level);
            player.setPos(fixture.point(8.5, 3, startZ));
            player.setOnGround(true);
            player.setSprinting(false);
            player.setDeltaMovement(fixture.vector(0, 0, direction * 0.215));
            player.launch();
            player.setOnGround(false);
            double maximumY = 3;
            int ticks = 0;
            List<List<Double>> trajectory = new ArrayList<>();
            for (int step = 0; step < 24; step++) {
                player.baseTick();
                player.travel(Vec3.ZERO);
                Vec3 point = fixture.relative(player.position());
                maximumY = Math.max(maximumY, point.y);
                trajectory.add(List.of(point.x, point.y, point.z));
                require(level.noCollision(player, player.getBoundingBox()), "Native player collided during jump");
                require(!player.isInWater() && !player.isInLava(), "Jump landed in fluid, leg=" + from + " direction=" + direction + " point=" + point);
                ticks++;
                if (player.onGround() && step > 1) break;
            }
            Vec3 end = fixture.relative(player.position());
            require(player.onGround() && Math.abs(end.y - 3) < 0.001 && end.z >= SPANS[target][0] + 0.3 && end.z <= SPANS[target][1] + 0.7, "Native jump missed target " + target + " end=" + end);
            require(player.fallDistance < 3 && player.getHealth() == player.getMaxHealth(), "Jump damage");
            results.add(Map.of("from", from, "to", target, "direction", direction, "gap_blocks", 1, "takeoff", List.of(8.5, 3, startZ), "landing", List.of(end.x, end.y, end.z), "ticks", ticks, "maximum_feet_y", maximumY, "trajectory", trajectory));
        }
        for (int[] span : SPANS) for (double posZ = span[0] + 0.3; posZ <= span[1] + 0.7; posZ += 0.1) airBody(level, fixture, 8.5, 3, posZ);
        return results;
    }

    private static List<Map<String, Object>> recoveries(ServerLevel level, Fixture fixture) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (int posZ : new int[]{8, 16, 24, 32}) for (boolean west : new boolean[]{true, false}) {
            double waterX = west ? 5.5 : 11.5;
            double lowX = west ? 4.82 : 12.18;
            double highX = west ? 4.18 : 12.82;
            double laneX = west ? 3.5 : 13.5;
            airBody(level, fixture, waterX, 2, posZ + 0.5);
            airBody(level, fixture, lowX, 2.5, posZ + 0.5);
            airBody(level, fixture, highX, 3, posZ + 0.5);
            airBody(level, fixture, laneX, 3, posZ + 0.5);
            for (double routeZ = 2.5; routeZ <= posZ + 0.5; routeZ += 0.25) airBody(level, fixture, laneX, 3, routeZ);
            double startX = Math.min(laneX, 8.5);
            for (double routeX = startX; routeX <= Math.max(laneX, 8.5); routeX += 0.25) airBody(level, fixture, routeX, 3, 2.5);
            for (double routeZ = 0.5; routeZ <= 2.5; routeZ += 0.25) airBody(level, fixture, 8.5, 3, routeZ);
            ProbePlayer player = new ProbePlayer(level);
            player.setPos(fixture.point(waterX, 3, posZ + 0.5));
            player.setOnGround(false);
            player.setDeltaMovement(Vec3.ZERO);
            boolean touchedWater = false;
            List<List<Double>> fall = new ArrayList<>();
            for (int step = 0; step < 60; step++) {
                player.baseTick();
                player.travel(Vec3.ZERO);
                Vec3 point = fixture.relative(player.position());
                fall.add(List.of(point.x, point.y, point.z));
                touchedWater |= player.isInWater();
                require(player.getAirSupply() == player.getMaxAirSupply(), "One-block pool submerged player eyes");
                if (player.onGround() && step > 2) break;
            }
            require(touchedWater && player.onGround() && player.getHealth() == player.getMaxHealth(), "Failure fall was unsafe");
            List<List<Double>> climb = new ArrayList<>();
            for (int step = 0; step < 60; step++) {
                player.baseTick();
                Vec3 velocity = fixture.vector(west ? -0.12 : 0.12, player.getDeltaMovement().y, 0);
                player.setDeltaMovement(velocity);
                player.travel(Vec3.ZERO);
                Vec3 point = fixture.relative(player.position());
                climb.add(List.of(point.x, point.y, point.z));
                require(level.noCollision(player, player.getBoundingBox()), "Player clipped recovery stairs");
                if (Math.abs(point.x - laneX) <= 0.12) break;
            }
            Vec3 recovered = fixture.relative(player.position());
            require(Math.abs(recovered.x - laneX) < 0.2 && recovered.y >= 2.999 && player.getHealth() == player.getMaxHealth(), "Native recovery could not climb to side lane " + recovered);
            results.add(Map.of("side", west ? "west" : "east", "z", posZ, "water_depth", 1, "step_rises", List.of(0.5, 0.5), "native_collision_waypoints", "PASS", "return_to_entrance", "PASS", "native_failure_fall", fall, "native_stair_travel", climb, "recovered_position", List.of(recovered.x, recovered.y, recovered.z)));
        }
        return results;
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, boolean capture, boolean route) throws Exception {
        int fluidCells = 0;
        int blockEntities = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState desired = entry.getValue().rotate(fixture.rotation());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(desired), "State changed at " + entry.getKey() + " rotation=" + fixture.rotation() + " expected=" + desired + " actual=" + actual);
            require(actual.canSurvive(level, position), "Unsupported block " + position);
            if (!actual.getFluidState().isEmpty()) fluidCells++;
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity != null, "Missing block entity");
                CompoundTag saved = entity.saveWithFullMetadata();
                require(!saved.contains("LootTable") && saved.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Unexpected loot");
                blockEntities++;
            }
        }
        sealsAndPorts(level, fixture);
        require(blockEntities == 7, "Furniture block entity count differs");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("status", "PASS");
        result.put("records_checked", expected.size());
        result.put("fluid_cells", fluidCells);
        result.put("escaped_fluid_cells", 0);
        result.put("block_entities_empty", blockEntities);
        result.put("support_checks", expected.size());
        result.put("ports", "two 3x3 dry ports, north centered");
        if (route) {
            result.put("native_player_jumps", jumps(level, fixture));
            result.put("recovery_routes", recoveries(level, fixture));
        }
        if (capture) {
            Vec3i rotatedSize = fixture.rotation() == Rotation.CLOCKWISE_90 || fixture.rotation() == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(39, 11, 17) : SIZE;
            BlockPos far = fixture.world(new BlockPos(16, 10, 38));
            BlockPos minimum = new BlockPos(Math.min(fixture.origin().getX(), far.getX()), fixture.origin().getY(), Math.min(fixture.origin().getZ(), far.getZ()));
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, rotatedSize, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            require(saved.getList("blocks", Tag.TAG_COMPOUND).size() == RECORDS && saved.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "World capture mismatch");
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, output.toFile());
            result.put("world_capture", root().relativize(output).toString());
            result.put("world_capture_sha256", hash(output));
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 500)
    public static void fourRotationsWaterAndParkour(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("scope", "Isolated Forge GameTestServer; vanilla player travel/collision probes, no full modpack client gameplay");
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Requires isolated GameTestServer");
            Path world = level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            require(world.startsWith(root().resolve("qa/forge-runs")), "Refusing world outside P02 QA");
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            String fingerprint = hash(root().resolve("room.nbt"));
            require(metadata.get("sha256").getAsString().equals(fingerprint), "NBT hash mismatch");
            require(hash(Path.of(metadata.get("reference").getAsString())).equals(metadata.get("reference_sha256").getAsString()), "Reference hash mismatch");
            report.put("structure_sha256", fingerprint);
            report.put("isolated_world", world.toString());
            report.put("random_tick_speed", level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING));
            CompoundTag data = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            Map<BlockPos, BlockState> expected = decode(data);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(SIZE), "Native dimensions differ");
            CompoundTag roundtrip = template.save(new CompoundTag());
            require(decode(roundtrip).equals(expected), "Native roundtrip differs");
            NbtIo.writeCompressed(roundtrip, root().resolve("qa/native-roundtrip.nbt").toFile());
            report.put("native_roundtrip_sha256", hash(root().resolve("qa/native-roundtrip.nbt")));
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> immediate = new ArrayList<>();
            Set<ChunkPos> chunks = new HashSet<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos horizontal = helper.absolutePos(new BlockPos(64 + index * 96, 4, 80));
                Fixture fixture = new Fixture(new BlockPos(horizontal.getX(), 160, horizontal.getZ()), rotation);
                for (BlockPos relative : BlockPos.betweenClosed(-2, -2, -2, 18, 12, 40)) {
                    BlockPos position = fixture.world(relative);
                    ChunkPos chunk = new ChunkPos(position);
                    if (chunks.add(chunk)) level.setChunkForced(chunk.x, chunk.z, true);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, Block.UPDATE_CLIENTS), "Placement failed");
                immediate.add(verify(level, fixture, expected, false, true));
                BlockPos portal = fixture.world(new BlockPos(8, 3, 0));
                level.setBlock(portal, Blocks.STONE.defaultBlockState(), FLAGS);
                boolean rejected = false;
                try { sealsAndPorts(level, fixture); } catch (IllegalStateException failure) { rejected = true; }
                require(rejected, "Blocked port probe missed");
                level.setBlock(portal, Blocks.AIR.defaultBlockState(), FLAGS);
                BlockPos basin = fixture.world(new BlockPos(8, 1, 5));
                BlockState support = level.getBlockState(basin);
                level.setBlock(basin, Blocks.AIR.defaultBlockState(), FLAGS);
                rejected = false;
                try { sealsAndPorts(level, fixture); } catch (IllegalStateException failure) { rejected = true; }
                require(rejected, "Basin hole probe missed");
                level.setBlock(basin, support, FLAGS);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                fixtures.add(fixture);
                index++;
            }
            report.put("immediate_placements", immediate);
            report.put("negative_probes", Map.of("blocked_port", 4, "basin_floor_hole", 4));
            report.put("forced_ticking_chunks", chunks.size());
            report.put("stabilization_ticks", 200);
            writeReport(report);
            helper.runAfterDelay(100, () -> {
                try {
                    List<Map<String, Object>> checked = new ArrayList<>();
                    for (Fixture fixture : fixtures) checked.add(verify(level, fixture, expected, false, false));
                    report.put("after_100_ticks", checked);
                    writeReport(report);
                } catch (Exception failure) {
                    report.put("status", "FAIL");
                    report.put("failure", failure.toString());
                    try { writeReport(report); } catch (Exception ignored) { }
                    helper.fail(failure.toString());
                }
            });
            helper.runAfterDelay(200, () -> {
                try {
                    require(!"FAIL".equals(report.get("status")), "Earlier stability check failed");
                    List<Map<String, Object>> checked = new ArrayList<>();
                    for (Fixture fixture : fixtures) checked.add(verify(level, fixture, expected, true, true));
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "NBT changed during test");
                    report.put("after_200_ticks", checked);
                    report.put("status", "PASS");
                    writeReport(report);
                    for (ChunkPos chunk : chunks) level.setChunkForced(chunk.x, chunk.z, false);
                    helper.succeed();
                } catch (Exception failure) {
                    report.put("status", "FAIL");
                    report.put("failure", failure.toString());
                    try { writeReport(report); } catch (Exception ignored) { }
                    helper.fail(failure.toString());
                }
            });
        } catch (Exception failure) {
            report.put("status", "FAIL");
            report.put("failure", failure.toString());
            writeReport(report);
            throw failure;
        }
    }
}
