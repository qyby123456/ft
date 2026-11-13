package net.farmingtales.stairqa;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftstaircqa")
@PrefixGameTestTemplate(false)
public final class StairGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(23, 18, 25);
    private static final int VOLUME = 10350;
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "campfire", "soul_campfire", "wither_rose", "sweet_berry_bush", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light", "sand", "gravel", "anvil", "chipped_anvil", "damaged_anvil");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private record Foot(int posX, int halfY, int posZ) {
        static Foot from(BlockPos position) {
            return new Foot(position.getX(), position.getY() * 2, position.getZ());
        }
    }

    private StairGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("ftstaircqa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static BlockPos coordinate(JsonArray value) {
        return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt());
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    private static boolean stockEmpty(CompoundTag data) {
        for (String key : data.getAllKeys()) {
            Tag value = data.get(key);
            if (Set.of("LootTable", "LootTableSeed", "Book", "RecordItem").contains(key)) {
                return false;
            }
            if ((key.equals("Items") || key.equals("Item")) && value != null && (!(value instanceof ListTag list) || !list.isEmpty())) {
                return false;
            }
            if (value instanceof CompoundTag child && !stockEmpty(child)) {
                return false;
            }
        }
        return true;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require Java 1.20.1 single palette");
        require(coordinate(dataToArray(data.getList("size", Tag.TAG_INT))).equals(new BlockPos(SIZE)), "Native dimensions differ");
        require(coordinate(metadata.getAsJsonArray("size")).equals(new BlockPos(SIZE)), "Metadata dimensions differ");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Preplaced entities forbidden");
        List<BlockState> palette = new ArrayList<>();
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedPalette.size(); index++) {
            CompoundTag encoded = encodedPalette.getCompound(index);
            ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity) && !FORBIDDEN.contains(identity.getPath()), "Unknown or forbidden block: " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = encoded.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Illegal native property: " + identity + " " + key);
            }
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
            require(state.getFluidState().isEmpty(), "Fluid or waterlogged block forbidden");
            palette.add(state);
        }
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag entry = records.getCompound(index);
            BlockPos position = coordinate(dataToArray(entry.getList("pos", Tag.TAG_INT)));
            require(position.getX() >= 0 && position.getX() < 23 && position.getY() >= 0 && position.getY() < 18 && position.getZ() >= 0 && position.getZ() < 25, "Out of bounds record");
            int stateIndex = entry.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Invalid palette index");
            BlockState state = palette.get(stateIndex);
            require(result.put(position, state) == null, "Duplicate record");
            require(state.hasBlockEntity() == entry.contains("nbt", Tag.TAG_COMPOUND), "Block entity payload mismatch");
            if (state.hasBlockEntity()) {
                CompoundTag payload = entry.getCompound("nbt");
                require(stockEmpty(payload), "Unexpected reward or inventory");
                BlockEntity entity = BlockEntity.loadStatic(position, state, payload.copy());
                require(entity != null && entity.getType().isValid(state), "Invalid native block entity");
                require(!(entity instanceof Container container) || container.isEmpty(), "Nonempty native inventory");
            }
        }
        require(result.size() == VOLUME, "Full explicit volume required");
        return result;
    }

    private static JsonArray dataToArray(ListTag values) {
        require(values.size() == 3, "Expected three coordinate integers");
        JsonArray result = new JsonArray();
        for (int index = 0; index < 3; index++) {
            result.add(values.getInt(index));
        }
        return result;
    }

    private static boolean bodyClear(ServerLevel level, double centerX, double feetY, double centerZ) {
        AABB body = new AABB(centerX - 0.3, feetY + 0.000001, centerZ - 0.3, centerX + 0.3, feetY + 1.799999, centerZ + 0.3);
        if (!level.noCollision(body)) {
            return false;
        }
        for (BlockPos position : BlockPos.betweenClosed(Mth.floor(body.minX), Mth.floor(body.minY), Mth.floor(body.minZ), Mth.floor(body.maxX - 0.000001), Mth.floor(body.maxY - 0.000001), Mth.floor(body.maxZ - 0.000001))) {
            BlockState state = level.getBlockState(position);
            if (!state.getFluidState().isEmpty() || state.is(Blocks.COBWEB) || FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath())) {
                return false;
            }
        }
        return true;
    }

    private static boolean safeFoot(ServerLevel level, double centerX, double feetY, double centerZ) {
        return bodyClear(level, centerX, feetY, centerZ) && !level.noCollision(new AABB(centerX - 0.299, feetY - 0.03, centerZ - 0.299, centerX + 0.299, feetY - 0.000001, centerZ + 0.299));
    }

    private static boolean safeFoot(ServerLevel level, Fixture fixture, Foot foot) {
        BlockPos cell = fixture.world(new BlockPos(foot.posX(), 0, foot.posZ()));
        return safeFoot(level, cell.getX() + 0.5, fixture.origin().getY() + foot.halfY() * 0.5, cell.getZ() + 0.5);
    }

    private static Foot traverse(ServerLevel level, Fixture fixture, Foot current, int offsetX, int offsetZ) {
        int nextX = current.posX() + offsetX;
        int nextZ = current.posZ() + offsetZ;
        if (nextX < 0 || nextX >= 23 || nextZ < 0 || nextZ >= 25) {
            return null;
        }
        BlockPos start = fixture.world(new BlockPos(current.posX(), 0, current.posZ()));
        BlockPos end = fixture.world(new BlockPos(nextX, 0, nextZ));
        int height = current.halfY();
        double previousX = start.getX() + 0.5;
        double previousZ = start.getZ() + 0.5;
        for (int sample = 1; sample <= 8; sample++) {
            double centerX = start.getX() + 0.5 + (end.getX() - start.getX()) * sample / 8.0;
            double centerZ = start.getZ() + 0.5 + (end.getZ() - start.getZ()) * sample / 8.0;
            boolean found = false;
            for (int step : new int[] {0, 1, -1}) {
                int candidate = height + step;
                if (candidate < 2 || candidate > 32) {
                    continue;
                }
                double feetY = fixture.origin().getY() + candidate * 0.5;
                boolean swept = step <= 0 || bodyClear(level, previousX, feetY, previousZ);
                if (step < 0) {
                    swept = bodyClear(level, centerX, fixture.origin().getY() + height * 0.5, centerZ);
                }
                if (swept && safeFoot(level, centerX, feetY, centerZ)) {
                    height = candidate;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return null;
            }
            previousX = centerX;
            previousZ = centerZ;
        }
        return new Foot(nextX, height, nextZ);
    }

    private static Set<Foot> reachable(ServerLevel level, Fixture fixture, BlockPos entrance) {
        Foot start = Foot.from(entrance);
        require(safeFoot(level, fixture, start), "Entrance lacks native safe collision and support");
        Set<Foot> reached = new HashSet<>();
        ArrayDeque<Foot> pending = new ArrayDeque<>();
        reached.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            Foot current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                Foot candidate = traverse(level, fixture, current, direction.getStepX(), direction.getStepZ());
                if (candidate != null && !reached.contains(candidate)) {
                    Foot reverse = traverse(level, fixture, candidate, -direction.getStepX(), -direction.getStepZ());
                    if (current.equals(reverse)) {
                        reached.add(candidate);
                        pending.addLast(candidate);
                    }
                }
            }
        }
        return reached;
    }

    private static boolean fullBelow(ServerLevel level, BlockPos position) {
        return level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP);
    }

    private static boolean contactBelow(ServerLevel level, BlockPos position) {
        BlockPos below = position.below();
        VoxelShape shape = level.getBlockState(below).getCollisionShape(level, below);
        return !shape.isEmpty() && shape.max(Direction.Axis.Y) >= 0.999;
    }

    private static AABB bounds(Fixture fixture) {
        BlockPos opposite = fixture.world(new BlockPos(22, 17, 24));
        return new AABB(Math.min(fixture.origin().getX(), opposite.getX()), fixture.origin().getY(), Math.min(fixture.origin().getZ(), opposite.getZ()), Math.max(fixture.origin().getX(), opposite.getX()) + 1, fixture.origin().getY() + 18, Math.max(fixture.origin().getZ(), opposite.getZ()) + 1);
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject metadata, JsonObject blueprint, boolean capture) throws Exception {
        int blockEntities = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " at " + entry.getKey() + ": " + actual);
            require(actual.canSurvive(level, position), "Native canSurvive failed at " + entry.getKey());
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity != null && entity.getType().isValid(actual) && stockEmpty(entity.saveWithoutMetadata()), "Illegal or nonempty world block entity");
                require(!(entity instanceof Container container) || container.isEmpty(), "Nonempty world container");
                blockEntities++;
            }
        }
        JsonObject features = blueprint.getAsJsonObject("features");
        for (String name : List.of("stairs_up", "stairs_down", "rails")) {
            for (JsonElement value : features.getAsJsonArray(name)) {
                require(fullBelow(level, fixture.world(coordinate(value.getAsJsonArray()))), "Stair or rail lacks full native footing: " + value);
            }
        }
        for (JsonElement value : features.getAsJsonArray("debris")) {
            require(contactBelow(level, fixture.world(coordinate(value.getAsJsonArray()))), "Airborne debris: " + value);
        }
        for (JsonElement value : features.getAsJsonArray("cobwebs")) {
            BlockPos web = fixture.world(coordinate(value.getAsJsonArray()));
            require(level.getBlockState(web).is(Blocks.COBWEB), "Missing ruin cobweb");
            require(java.util.Arrays.stream(Direction.values()).anyMatch(direction -> !level.getBlockState(web.relative(direction)).getCollisionShape(level, web.relative(direction)).isEmpty()), "Unattached cobweb");
        }
        for (int posX : new int[] {14, 15}) {
            for (int posY = 0; posY <= 13; posY++) {
                BlockPos pier = fixture.world(new BlockPos(posX, posY, 22));
                require(level.getBlockState(pier).isFaceSturdy(level, pier, Direction.UP), "Broken beam support column incomplete");
            }
        }
        Set<Foot> reached = reachable(level, fixture, coordinate(metadata.getAsJsonArray("entrance")));
        for (JsonElement value : blueprint.getAsJsonArray("required_walkable")) {
            Foot foot = Foot.from(coordinate(value.getAsJsonArray()));
            require(safeFoot(level, fixture, foot) && reached.contains(foot), "Required three-wide route unreachable without jumping: " + value + " " + fixture.rotation());
        }
        for (JsonElement value : metadata.getAsJsonArray("interfaces")) {
            JsonObject portal = value.getAsJsonObject();
            BlockPos feet = coordinate(portal.getAsJsonArray("feet"));
            require(reached.contains(Foot.from(feet)) && reached.contains(Foot.from(coordinate(portal.getAsJsonArray("interior")))), "Portal or safety point unreachable");
            for (int lateral = -1; lateral <= 1; lateral++) {
                for (int height = 0; height < 3; height++) {
                    require(level.getBlockState(fixture.world(feet.offset(lateral, height, 0))).isAir(), "Portal 3x3 clearance violated");
                }
                require(fullBelow(level, fixture.world(feet.offset(lateral, 0, 0))), "Portal floor incomplete");
            }
        }
        AABB area = bounds(fixture);
        var unexpected = level.getEntities(null, area.inflate(1));
        int entities = unexpected.size();
        require(entities == 0, "Unexpected world entities " + fixture.rotation() + ": " + unexpected.stream().map(entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()) + " at " + entity.blockPosition() + " " + entity.saveWithoutId(new CompoundTag())).toList());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records_checked", expected.size());
        result.put("empty_block_entities", blockEntities);
        result.put("reachable_player_foot_nodes", reached.size());
        result.put("required_three_wide_nodes", blueprint.getAsJsonArray("required_walkable").size());
        result.put("unexpected_world_entities", entities);
        result.put("round_trip_interfaces", List.of("main", "upper", "lower"));
        result.put("collision_method", "Native 0.6x1.8 player AABB, 1/8-block horizontal samples, max half-block step; every discovered edge is reversible; no jumps or drops.");
        if (capture) {
            BlockPos minimum = new BlockPos((int) area.minX, (int) area.minY, (int) area.minZ);
            Vec3i dimensions = new Vec3i((int) (area.maxX - area.minX), 18, (int) (area.maxZ - area.minZ));
            StructureTemplate captured = new StructureTemplate();
            captured.fillFromWorld(level, minimum, dimensions, false, null);
            CompoundTag saved = captured.save(new CompoundTag());
            require(saved.getList("blocks", Tag.TAG_COMPOUND).size() == VOLUME && saved.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Native capture incomplete");
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), saved);
            require(reloaded.getSize().equals(dimensions), "Native capture reload dimensions differ");
            Path path = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase() + ".nbt");
            NbtIo.writeCompressed(saved, path.toFile());
            result.put("capture", path.toString());
            result.put("capture_sha256", hash(path));
        }
        return result;
    }

    private static int negativeProbes(ServerLevel level, Fixture fixture) {
        BlockPos footing = fixture.world(new BlockPos(6, 5, 10));
        BlockState saved = level.getBlockState(footing);
        level.setBlock(footing, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
        require(!fullBelow(level, fixture.world(new BlockPos(6, 6, 10))), "Missing-stair-footing probe was not detected");
        level.setBlock(footing, saved, FLAGS | Block.UPDATE_KNOWN_SHAPE);
        BlockPos aisle = fixture.world(new BlockPos(11, 6, 4));
        level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS);
        require(!safeFoot(level, fixture, new Foot(11, 12, 4)), "Stone aisle blockage not detected");
        level.setBlock(aisle, Blocks.COBWEB.defaultBlockState(), FLAGS);
        require(!safeFoot(level, fixture, new Foot(11, 12, 4)), "Collisionless cobweb hazard not detected");
        level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS);
        BlockPos chain = fixture.world(new BlockPos(11, 11, 6));
        BlockState chainState = level.getBlockState(chain);
        level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
        BlockPos lamp = fixture.world(new BlockPos(11, 10, 6));
        require(!level.getBlockState(lamp).canSurvive(level, lamp), "Detached hanging lantern not detected");
        level.setBlock(chain, chainState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
        BlockPos pier = fixture.world(new BlockPos(14, 13, 22));
        BlockState pierState = level.getBlockState(pier);
        level.setBlock(pier, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
        require(!fullBelow(level, fixture.world(new BlockPos(14, 14, 22))), "Broken beam pier probe not detected");
        level.setBlock(pier, pierState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
        BlockPos gap = fixture.world(new BlockPos(16, 4, 12));
        level.setBlock(gap, Blocks.STONE.defaultBlockState(), FLAGS);
        require(!safeFoot(level, fixture, new Foot(16, 8, 12)), "Narrow lower passage blockage not detected");
        level.setBlock(gap, Blocks.AIR.defaultBlockState(), FLAGS);
        return 6;
    }

    @GameTest(template = "empty", timeoutTicks = 2400)
    public static void ruinedStairsFourWayPlacement(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("java", System.getProperty("java.version"));
        report.put("test_world_only", true);
        try {
            Path source = root().resolve("out/room.nbt");
            String fingerprint = hash(source);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("out/room.json")), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("out/blueprint.json")), JsonObject.class);
            require(metadata.get("sha256").getAsString().equals(fingerprint), "Metadata hash differs");
            CompoundTag data = NbtIo.readCompressed(source.toFile());
            Map<BlockPos, BlockState> expected = audit(data, metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            ServerLevel level = helper.getLevel();
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            List<Fixture> fixtures = new ArrayList<>();
            int probes = 0;
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(helper.absolutePos(new BlockPos(40 + index * 80, 280, 40)), rotation);
                System.out.println("STAIR_C_STAGE: placing " + rotation);
                AABB area = bounds(fixture);
                for (int chunkX = Mth.floor(area.minX) >> 4; chunkX <= Mth.floor(area.maxX - 1) >> 4; chunkX++) {
                    for (int chunkZ = Mth.floor(area.minZ) >> 4; chunkZ <= Mth.floor(area.maxZ - 1) >> 4; chunkZ++) {
                        level.setChunkForced(chunkX, chunkZ, true);
                    }
                }
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
                }
                level.getEntities(null, bounds(fixture).inflate(1)).forEach(entity -> entity.discard());
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS | Block.UPDATE_KNOWN_SHAPE), "Native placeInWorld returned false");
                verify(level, fixture, expected, metadata, blueprint, false);
                System.out.println("STAIR_C_STAGE: immediate placement passed " + rotation);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                probes += negativeProbes(level, fixture);
                fixtures.add(fixture);
                index++;
            }
            report.put("room_sha256", fingerprint);
            report.put("negative_probes", probes);
            report.put("negative_probe_types", List.of("missing stair footing", "stone-blocked aisle", "collisionless cobweb aisle", "broken lantern chain", "missing broken-beam pier", "blocked lower passage"));
            report.put("stabilization_ticks", 100);
            report.put("placement_policy", "Sky-isolated empty test cells; UPDATE_KNOWN_SHAPE during native template placement, then explicit normal neighbor updates for every record; all fixture chunks forced ticking; item drops remain enabled.");
            writeReport(report);
            helper.runAfterDelay(100, () -> {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        results.add(verify(level, fixture, expected, metadata, blueprint, true));
                        System.out.println("STAIR_C_STAGE: stabilized placement passed " + fixture.rotation());
                    }
                    require(hash(source).equals(fingerprint), "Source changed during native test");
                    report.put("status", "PASS");
                    report.put("rotations", results);
                    report.put("records_checked_after_ticks", results.size() * expected.size());
                    report.put("limitations", List.of("No full-modpack client, multiplayer, FPS or actual screenshots.", "No FT catalog slot, roads, runtime integration, idempotence or production deployment tested.", "Sampled conservative collision traversal is not a live player-input playback."));
                    writeReport(report);
                    System.out.println("STAIR_C_NATIVE_GAME_TEST_PASS: rotations=4/4, records=41400, ticks=100, negative_probes=24, source=" + fingerprint);
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
