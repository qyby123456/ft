package net.farmingtales.spiralqa;

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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftspiralqa")
@PrefixGameTestTemplate(false)
public final class SpiralTowerGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("lava", "water", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
        Vec3 center(BlockPos relative) {
            BlockPos position = world(relative);
            return new Vec3(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        }
    }

    private record Pose(double feetY, double headroom) {}

    private SpiralTowerGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("ftspiralqa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    private static BlockPos coordinate(JsonElement value) {
        JsonArray array = value.getAsJsonArray();
        return new BlockPos(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
    }

    private static boolean empty(CompoundTag payload) {
        for (String key : payload.getAllKeys()) {
            if (Set.of("LootTable", "LootTableSeed").contains(key)) {
                return false;
            }
            Tag value = payload.get(key);
            if (key.equals("Items") && (!(value instanceof ListTag items) || !items.isEmpty())) {
                return false;
            }
        }
        return true;
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown/custom block " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden block " + identity);
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String name : properties.getAllKeys()) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(name);
            require(property != null && property.getValue(properties.getString(name)).isPresent(), "Unknown property " + name);
            require(state.getValue(property).toString().equals(properties.getString(name)), "Property changed " + encoded);
        }
        require(properties.getAllKeys().size() == state.getProperties().size(), "Implicit property default " + encoded);
        return state;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Wrong format/version");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Embedded entities");
        List<BlockState> palette = new ArrayList<>();
        for (Tag value : data.getList("palette", Tag.TAG_COMPOUND)) {
            palette.add(decode((CompoundTag) value));
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        for (Tag value : data.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            ListTag coordinates = entry.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getX() < 27 && position.getY() >= 0 && position.getY() < 27 && position.getZ() >= 0 && position.getZ() < 27, "Coordinate out of bounds");
            BlockState state = palette.get(entry.getInt("state"));
            require(expected.put(position, state) == null, "Duplicate coordinate");
            require(state.hasBlockEntity() == entry.contains("nbt", Tag.TAG_COMPOUND), "Block entity payload mismatch");
            if (state.hasBlockEntity()) {
                CompoundTag payload = entry.getCompound("nbt");
                BlockEntity entity = BlockEntity.loadStatic(position, state, payload.copy());
                require(entity != null && entity.getType().isValid(state) && empty(payload), "Bad block entity");
                require(!(entity instanceof Container container) || container.isEmpty(), "Stocked native inventory");
            }
        }
        require(expected.size() == 19683, "Expected explicit 27 cubed volume");
        return expected;
    }

    private static Pose pose(ServerLevel level, double centerX, double centerZ, double previousY) {
        double radius = 0.3;
        double floorY = Double.NEGATIVE_INFINITY;
        List<AABB> collision = new ArrayList<>();
        for (int posX = (int) Math.floor(centerX - radius); posX <= Math.floor(centerX + radius); posX++) {
            for (int posZ = (int) Math.floor(centerZ - radius); posZ <= Math.floor(centerZ + radius); posZ++) {
                for (int posY = (int) Math.floor(previousY) - 2; posY <= Math.ceil(previousY + 5); posY++) {
                    BlockPos position = new BlockPos(posX, posY, posZ);
                    BlockState state = level.getBlockState(position);
                    require(state.getFluidState().isEmpty(), "Route fluid " + position);
                    require(!state.is(Blocks.COBWEB), "Route cobweb " + position);
                    for (AABB local : state.getCollisionShape(level, position).toAabbs()) {
                        AABB shape = local.move(position);
                        if (shape.maxX > centerX - radius + 0.00001 && shape.minX < centerX + radius - 0.00001 && shape.maxZ > centerZ - radius + 0.00001 && shape.minZ < centerZ + radius - 0.00001) {
                            collision.add(shape);
                            if (shape.maxY <= previousY + 0.60001 && shape.maxY >= previousY - 0.60001) {
                                floorY = Math.max(floorY, shape.maxY);
                            }
                        }
                    }
                }
            }
        }
        require(Double.isFinite(floorY), "Missing floor/step exceeds 0.6 at " + centerX + "," + previousY + "," + centerZ);
        AABB body = new AABB(centerX - radius, floorY + 0.00001, centerZ - radius, centerX + radius, floorY + 1.8, centerZ + radius);
        require(level.noCollision(null, body), "0.6x1.8 player collision at " + centerX + "," + floorY + "," + centerZ);
        double ceiling = floorY + 5;
        for (AABB shape : collision) {
            if (shape.minY >= floorY + 0.00001) {
                ceiling = Math.min(ceiling, shape.minY);
            }
        }
        return new Pose(floorY, ceiling - floorY);
    }

    private static Map<String, Object> walkRoute(ServerLevel level, Fixture fixture, JsonArray waypoints, boolean reverse) {
        List<BlockPos> points = new ArrayList<>();
        for (JsonElement value : waypoints) {
            points.add(coordinate(value));
        }
        if (reverse) {
            java.util.Collections.reverse(points);
        }
        Vec3 first = fixture.center(points.get(0));
        Pose current = pose(level, first.x, first.z, first.y);
        int samples = 1;
        double minimumHeadroom = current.headroom();
        double maximumStep = 0;
        for (int index = 1; index < points.size(); index++) {
            Vec3 previous = fixture.center(points.get(index - 1));
            Vec3 next = fixture.center(points.get(index));
            require(previous.x == next.x || previous.z == next.z, "Waypoint segment not axis aligned");
            int count = (int) Math.ceil(Math.hypot(next.x - previous.x, next.z - previous.z) / 0.125);
            for (int sample = 1; sample <= count; sample++) {
                double fraction = (double) sample / count;
                Pose result = pose(level, previous.x + (next.x - previous.x) * fraction, previous.z + (next.z - previous.z) * fraction, current.feetY());
                maximumStep = Math.max(maximumStep, Math.abs(result.feetY() - current.feetY()));
                minimumHeadroom = Math.min(minimumHeadroom, result.headroom());
                current = result;
                samples++;
            }
            require(Math.abs(current.feetY() - next.y) <= 0.501, "Wrong floor at waypoint " + points.get(index) + ": " + current.feetY());
        }
        return Map.of("status", "PASS", "samples", samples, "min_headroom_blocks", minimumHeadroom, "max_step_blocks", maximumStep, "player_size", List.of(0.6, 1.8), "sample_interval_blocks", 0.125, "direction", reverse ? "return" : "outward");
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject blueprint, boolean capture) throws Exception {
        int entityCount = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " " + actual + " != " + entry.getValue().rotate(fixture.rotation()));
            require(actual.canSurvive(level, position), "Unsupported native block " + entry.getKey());
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity != null && empty(entity.saveWithFullMetadata()), "Missing/stocked placed block entity");
                require(!(entity instanceof Container container) || container.isEmpty(), "Placed container has stock");
                entityCount++;
            }
        }
        for (JsonElement value : blueprint.getAsJsonArray("stairs")) {
            JsonObject stair = value.getAsJsonObject();
            BlockPos support = fixture.world(coordinate(stair.get("support")));
            require(level.getBlockState(support).isFaceSturdy(level, support, Direction.UP), "Missing stair support");
            BlockPos tread = fixture.world(coordinate(stair.get("pos")));
            require(level.getBlockState(tread).is(Blocks.STONE_BRICK_STAIRS), "Missing native stair");
            pose(level, tread.getX() + 0.5, tread.getZ() + 0.5, tread.getY() + 1);
        }
        for (JsonElement value : blueprint.getAsJsonArray("guardrails")) {
            BlockPos rail = fixture.world(coordinate(value));
            require(level.getBlockState(rail).is(Blocks.IRON_BARS) && level.getBlockState(rail.below()).isCollisionShapeFullBlock(level, rail.below()), "Missing/unsupported guardrail");
        }
        for (JsonElement value : blueprint.getAsJsonArray("safe_points")) {
            Vec3 safe = fixture.center(coordinate(value));
            pose(level, safe.x, safe.z, safe.y);
        }
        List<Map<String, Object>> routes = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : blueprint.getAsJsonObject("routes").entrySet()) {
            for (boolean reverse : List.of(false, true)) {
                Map<String, Object> route = new LinkedHashMap<>(walkRoute(level, fixture, entry.getValue().getAsJsonArray(), reverse));
                route.put("route", entry.getKey());
                routes.add(route);
            }
        }
        if (capture) {
            BlockPos diagonal = fixture.world(new BlockPos(26, 26, 26));
            BlockPos minimum = new BlockPos(Math.min(diagonal.getX(), fixture.origin().getX()), fixture.origin().getY(), Math.min(diagonal.getZ(), fixture.origin().getZ()));
            StructureTemplate snapshot = new StructureTemplate();
            snapshot.fillFromWorld(level, minimum, new Vec3i(27, 27, 27), false, null);
            NbtIo.writeCompressed(snapshot.save(new CompoundTag()), root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase() + ".nbt").toFile());
        }
        return Map.of("rotation", fixture.rotation().name(), "status", "PASS", "records", expected.size(), "empty_containers", entityCount, "routes", routes, "treads_width_three_and_headroom_checked", 57, "guardrails", blueprint.getAsJsonArray("guardrails").size());
    }

    @GameTest(template = "empty", timeoutTicks = 1400)
    public static void nativeFourRotations(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Only isolated GameTestServer may write fixture blocks");
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("out/room.json")), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("out/blueprint.json")), JsonObject.class);
            String fingerprint = hash(root().resolve("out/room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Fingerprint mismatch");
            report.put("structure_sha256", fingerprint);
            CompoundTag data = NbtIo.readCompressed(root().resolve("out/room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(data);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(new Vec3i(27, 27, 27)), "Wrong native template size");
            require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == expected.size(), "Native roundtrip lost records");
            report.put("native_registry_properties_block_entities_roundtrip", "PASS");
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> immediate = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos horizontal = helper.absolutePos(new BlockPos(60 + index * 80, 0, 60));
                Fixture fixture = new Fixture(new BlockPos(horizontal.getX(), 180, horizontal.getZ()), rotation);
                for (int chunkX = (fixture.origin().getX() - 28) >> 4; chunkX <= (fixture.origin().getX() + 28) >> 4; chunkX++) {
                    for (int chunkZ = (fixture.origin().getZ() - 28) >> 4; chunkZ <= (fixture.origin().getZ() + 28) >> 4; chunkZ++) {
                        level.setChunkForced(chunkX, chunkZ, true);
                    }
                }
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                }
                level.getEntities(null, new AABB(fixture.origin().offset(-28, -2, -28), fixture.origin().offset(28, 29, 28))).forEach(entity -> entity.discard());
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement failed");
                immediate.add(verify(level, fixture, expected, blueprint, false));
                BlockPos missing = fixture.world(new BlockPos(15, 7, 9));
                BlockState saved = level.getBlockState(missing);
                level.setBlock(missing, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!level.getBlockState(missing).isFaceSturdy(level, missing, Direction.UP), "Missing tread support negative probe not detected");
                level.setBlock(missing, saved, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos blocker = fixture.world(new BlockPos(13, 11, 9));
                saved = level.getBlockState(blocker);
                level.setBlock(blocker, Blocks.STONE.defaultBlockState(), FLAGS);
                boolean rejected = false;
                try {
                    walkRoute(level, fixture, blueprint.getAsJsonObject("routes").getAsJsonArray("spiral_up"), false);
                } catch (IllegalStateException expectedFailure) {
                    rejected = true;
                }
                require(rejected, "Blocked spiral negative probe not detected");
                level.setBlock(blocker, saved, FLAGS);
                BlockPos chain = fixture.world(new BlockPos(6, 22, 17));
                saved = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lamp = fixture.world(new BlockPos(6, 21, 17));
                require(!level.getBlockState(lamp).canSurvive(level, lamp), "Missing lamp support negative probe not detected");
                level.setBlock(chain, saved, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                fixtures.add(fixture);
                index++;
            }
            report.put("immediate_rotations", immediate);
            report.put("negative_missing_tread_support", 4);
            report.put("negative_blocked_spiral_route", 4);
            report.put("negative_missing_lantern_chain", 4);
            report.put("stabilization_ticks", 200);
            report.put("random_tick_speed", 0);
            report.put("fixture_origin_world_y", 180);
            report.put("fixture_chunks_forced", true);
            report.put("fixture_isolation", "Sky volume above generated terrain, isolated test world only; avoids natural gravel falling into template air.");
            writeReport(report);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> stabilized = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        stabilized.add(verify(level, fixture, expected, blueprint, true));
                    }
                    require(fingerprint.equals(hash(root().resolve("out/room.nbt"))), "Source changed during native run");
                    report.put("rotations", stabilized);
                    report.put("records_after_ticks", 4 * expected.size());
                    report.put("status", "PASS");
                    report.put("limitations", List.of("No FT core catalog, external connecting hall, idempotence, occupancy preflight or deployment tested.", "No player save, full modpack client, multiplayer or FPS test. Native snapshots are machine evidence, not game screenshots.", "Routes use native 0.6x1.8 AABB collision sweeps at 0.125-block intervals with 0.6 step limit, not a live player input recording. Random ticks disabled; neighbor/scheduled ticks enabled in forced chunks."));
                    writeReport(report);
                    System.out.println("SPIRAL_TOWER_NATIVE_PASS rotations=4/4 records=" + (4 * expected.size()) + " ticks=200 source=" + fingerprint);
                    helper.succeed();
                } catch (Exception exception) {
                    report.put("status", "FAIL");
                    report.put("error", exception.toString());
                    try {
                        writeReport(report);
                    } catch (Exception secondary) {
                        exception.addSuppressed(secondary);
                    }
                    throw new IllegalStateException(exception);
                }
            });
        } catch (Exception exception) {
            report.put("status", "FAIL");
            report.put("error", exception.toString());
            writeReport(report);
            throw exception;
        }
    }
}
