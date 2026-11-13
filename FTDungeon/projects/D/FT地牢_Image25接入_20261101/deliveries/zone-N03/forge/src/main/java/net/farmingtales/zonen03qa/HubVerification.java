package net.farmingtales.zonen03qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftzonen03qa")
@GameTestHolder("ftzonen03qa")
@PrefixGameTestTemplate(false)
public final class HubVerification {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Vec3i SIZE = new Vec3i(35, 18, 35);
    private static final int VOLUME = 22050;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "powder_snow", "cobweb", "magma_block", "cactus", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light");

    public HubVerification() {}

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }

        Vec3 point(BlockPos relative, double offsetAcross, double feetHeight, double offsetDepth) {
            BlockPos base = world(new BlockPos(relative.getX(), 0, relative.getZ()));
            double rotatedAcross = offsetAcross;
            double rotatedDepth = offsetDepth;
            switch (rotation) {
                case CLOCKWISE_90 -> { rotatedAcross = -offsetDepth; rotatedDepth = offsetAcross; }
                case CLOCKWISE_180 -> { rotatedAcross = -offsetAcross; rotatedDepth = -offsetDepth; }
                case COUNTERCLOCKWISE_90 -> { rotatedAcross = offsetDepth; rotatedDepth = -offsetAcross; }
                default -> {}
            }
            return new Vec3(base.getX() + 0.5 + rotatedAcross, origin.getY() + feetHeight, base.getZ() + 0.5 + rotatedDepth);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static Path root() {
        return Path.of(System.getProperty("ftzonen03qa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/forge-native.json"), JSON.toJson(report) + System.lineSeparator());
    }

    private static BlockPos coordinate(JsonArray values) {
        return new BlockPos(values.get(0).getAsInt(), values.get(1).getAsInt(), values.get(2).getAsInt());
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identifier = new ResourceLocation(encoded.getString("Name"));
        require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown vanilla block " + identifier);
        require(!FORBIDDEN.contains(identifier.getPath()), "Forbidden block " + identifier);
        Block block = BuiltInRegistries.BLOCK.get(identifier);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identifier + " " + key);
        }
        require(properties.getAllKeys().size() == block.getStateDefinition().getProperties().size(), "Incomplete properties " + identifier);
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(state.getFluidState().isEmpty(), "Unexpected fluid " + identifier);
        require(!state.hasBlockEntity() || state.is(Blocks.OAK_WALL_SIGN), "Unexpected block entity " + identifier);
        return state;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require 1.20.1 single palette");
        ListTag dimensions = data.getList("size", Tag.TAG_INT);
        require(dimensions.size() == 3 && dimensions.getInt(0) == 35 && dimensions.getInt(1) == 18 && dimensions.getInt(2) == 35, "Wrong dimensions");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected source entities");
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < encodedPalette.size(); index++) palette.add(decode(encodedPalette.getCompound(index)));
        Map<BlockPos, BlockState> records = new LinkedHashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        int signs = 0;
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag record = blocks.getCompound(index);
            ListTag position = record.getList("pos", Tag.TAG_INT);
            require(position.size() == 3, "Malformed coordinate");
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 35 && relative.getY() >= 0 && relative.getY() < 18 && relative.getZ() >= 0 && relative.getZ() < 35, "Out of bounds record");
            int stateIndex = record.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Bad palette index");
            BlockState state = palette.get(stateIndex);
            if (record.contains("nbt")) {
                require(state.is(Blocks.OAK_WALL_SIGN), "Only signs may contain block NBT");
                CompoundTag sign = record.getCompound("nbt");
                require(sign.getString("id").equals("minecraft:sign") && sign.getBoolean("is_waxed"), "Unsealed sign NBT");
                require(sign.getCompound("front_text").getList("messages", Tag.TAG_STRING).size() == 4, "Invalid sign text");
                signs++;
            }
            require(records.put(relative, state) == null, "Duplicate coordinate");
        }
        require(records.size() == VOLUME && signs == 4, "Incomplete volume or signs");
        return records;
    }

    private static AABB body(Vec3 feet, double height) {
        return new AABB(feet.x - 0.299, feet.y + 0.00001, feet.z - 0.299, feet.x + 0.299, feet.y + height, feet.z + 0.299);
    }

    private static boolean standing(ServerLevel level, Vec3 feet, double height) {
        AABB support = new AABB(feet.x - 0.299, feet.y - 0.01, feet.z - 0.299, feet.x + 0.299, feet.y, feet.z + 0.299);
        return level.noCollision(body(feet, height)) && !level.noCollision(support);
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        return standing(level, new Vec3(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5), 1.8);
    }

    private static void sweptStep(ServerLevel level, Vec3 from, Vec3 to) {
        require(Math.abs(to.y - from.y) <= 0.50001, "Step exceeds normal half-block rise");
        require(standing(level, from, 1.8) && standing(level, to, 1.8), "Unsupported or colliding step");
        double raised = Math.max(from.y, to.y);
        Vec3 raisedFrom = new Vec3(from.x, raised, from.z);
        Vec3 raisedTo = new Vec3(to.x, raised, to.z);
        require(level.noCollision(body(raisedFrom, 1.8).minmax(body(raisedTo, 1.8))), "Swept player collision through step");
    }

    private static int verifyStairs(ServerLevel level, Fixture fixture, JsonObject metadata) {
        int transitions = 0;
        for (JsonElement value : metadata.getAsJsonArray("stairs")) {
            JsonObject flight = value.getAsJsonObject();
            BlockPos first = coordinate(flight.getAsJsonArray("first"));
            BlockPos low = coordinate(flight.getAsJsonArray("low"));
            BlockPos high = coordinate(flight.getAsJsonArray("high"));
            int deltaAcross = flight.getAsJsonArray("direction").get(0).getAsInt();
            int deltaDepth = flight.getAsJsonArray("direction").get(1).getAsInt();
            for (int lane = -1; lane <= 1; lane++) {
                double sideAcross = deltaAcross == 0 ? lane : 0;
                double sideDepth = deltaAcross == 0 ? 0 : lane;
                List<Vec3> samples = new ArrayList<>();
                samples.add(fixture.point(low, sideAcross, low.getY(), sideDepth));
                for (int step = 0; step < 6; step++) {
                    BlockPos current = first.offset(deltaAcross * step, step, deltaDepth * step);
                    Vec3 lowTread = fixture.point(current, sideAcross - deltaAcross * 0.3, current.getY() + 0.5, sideDepth - deltaDepth * 0.3);
                    Vec3 highTread = fixture.point(current, sideAcross + deltaAcross * 0.2, current.getY() + 1.0, sideDepth + deltaDepth * 0.2);
                    require(standing(level, lowTread, 3.0) && standing(level, highTread, 3.0), "Stair lacks three-block headroom " + flight.get("id"));
                    samples.add(lowTread);
                    samples.add(highTread);
                }
                samples.add(fixture.point(high, sideAcross, high.getY(), sideDepth));
                for (int index = 1; index < samples.size(); index++) {
                    sweptStep(level, samples.get(index - 1), samples.get(index));
                    sweptStep(level, samples.get(index), samples.get(index - 1));
                    transitions += 2;
                }
            }
        }
        return transitions;
    }

    private static Set<BlockPos> flatReachable(ServerLevel level, Fixture fixture, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() < 0 || next.getX() >= 35 || next.getZ() < 0 || next.getZ() >= 35 || visited.contains(next)) continue;
                if (!walkable(level, fixture.world(next))) continue;
                Vec3 from = fixture.point(current, 0, current.getY(), 0);
                Vec3 to = fixture.point(next, 0, next.getY(), 0);
                if (!level.noCollision(body(from, 1.8).minmax(body(to, 1.8)))) continue;
                visited.add(next);
                queue.add(next);
            }
        }
        return visited;
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject metadata, boolean capture) throws Exception {
        int lamps = 0;
        int signs = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "Changed state " + fixture.rotation() + " " + entry.getKey() + " expected=" + entry.getValue().rotate(fixture.rotation()) + " actual=" + actual);
            require(actual.canSurvive(level, position), "Unsupported native state " + entry.getKey());
            if (actual.is(Blocks.LANTERN)) lamps++;
            if (actual.is(Blocks.OAK_WALL_SIGN)) {
                signs++;
                require(level.getBlockEntity(position) != null, "Missing native sign block entity");
                CompoundTag sign = level.getBlockEntity(position).saveWithFullMetadata();
                require(sign.getBoolean("is_waxed") && sign.getCompound("front_text").getList("messages", Tag.TAG_STRING).size() == 4, "Sign text did not survive placement");
            }
        }
        require(lamps == 21 && signs == 4, "Decor count mismatch lamps=" + lamps + " signs=" + signs);
        Set<BlockPos> hub = flatReachable(level, fixture, new BlockPos(17, 7, 11));
        for (JsonElement value : metadata.getAsJsonArray("stairs")) {
            JsonObject flight = value.getAsJsonObject();
            BlockPos low = coordinate(flight.getAsJsonArray("low"));
            BlockPos high = coordinate(flight.getAsJsonArray("high"));
            require(hub.contains(low.getY() == 7 ? low : high), "Stair mouth disconnected from hub " + flight.get("id"));
        }
        for (JsonElement value : metadata.getAsJsonArray("safe_points")) {
            BlockPos feet = coordinate(value.getAsJsonObject().getAsJsonArray("feet"));
            require(standing(level, fixture.point(feet, 0, feet.getY(), 0), 3), "Unsafe refuge " + feet);
            if (feet.getY() == 7) require(hub.contains(feet), "Disconnected refuge " + feet);
        }
        for (JsonElement value : metadata.getAsJsonArray("ports")) {
            JsonObject port = value.getAsJsonObject();
            BlockPos feet = coordinate(port.getAsJsonArray("feet"));
            boolean northSouth = port.get("facing").getAsString().equals("north") || port.get("facing").getAsString().equals("south");
            for (int lane = -1; lane <= 1; lane++) require(standing(level, fixture.point(feet, northSouth ? lane : 0, feet.getY(), northSouth ? 0 : lane), 3), "Port lacks supported 3x3 clearance");
            Set<BlockPos> landing = flatReachable(level, fixture, feet);
            BlockPos flightLanding = switch (port.get("id").getAsString()) {
                case "north-lower" -> new BlockPos(17, 1, 2);
                case "south-lower" -> new BlockPos(17, 1, 32);
                case "east-upper" -> new BlockPos(32, 13, 17);
                case "west-upper" -> new BlockPos(2, 13, 17);
                default -> throw new IllegalStateException("Unknown port");
            };
            require(landing.contains(flightLanding), "External mouth disconnected from stairs");
        }
        for (JsonElement value : metadata.getAsJsonArray("balustrade")) {
            JsonArray segment = value.getAsJsonArray();
            BlockPos block = new BlockPos(segment.get(0).getAsInt(), 7, segment.get(1).getAsInt());
            BlockPos position = fixture.world(block);
            require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Missing shaft balustrade");
            require(level.getBlockState(position.above()).is(Blocks.STONE_BRICK_SLAB), "Missing balustrade cap");
        }
        require(!hub.contains(new BlockPos(17, 7, 17)), "Central shaft accessible without guard");
        int transitions = verifyStairs(level, fixture, metadata);
        require(level.getEntities((net.minecraft.world.entity.Entity) null, new AABB(fixture.world(BlockPos.ZERO), fixture.world(new BlockPos(34, 17, 34))).inflate(2)).isEmpty(), "Unexpected entities after stability tick");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records", expected.size());
        result.put("hub_flat_reachable_feet", hub.size());
        result.put("ports_connected_via_hub_and_stairs", 4);
        result.put("native_bidirectional_swept_stair_transitions", transitions);
        result.put("player_body", "0.598 x 1.8; half-block step rise, native collision shapes");
        result.put("stair_headroom", 3);
        result.put("lamps_supported", lamps);
        result.put("signs_survived", signs);
        if (capture) {
            BlockPos first = fixture.world(BlockPos.ZERO);
            BlockPos last = fixture.world(new BlockPos(34, 17, 34));
            BlockPos minimum = new BlockPos(Math.min(first.getX(), last.getX()), fixture.origin().getY(), Math.min(first.getZ(), last.getZ()));
            StructureTemplate saved = new StructureTemplate();
            saved.fillFromWorld(level, minimum, SIZE, false, Blocks.STRUCTURE_VOID);
            CompoundTag captured = saved.save(new CompoundTag());
            ListTag palette = captured.getList("palette", Tag.TAG_COMPOUND);
            ListTag blocks = captured.getList("blocks", Tag.TAG_COMPOUND);
            require(blocks.size() == VOLUME, "Incomplete world capture");
            Map<BlockPos, BlockState> capturedStates = new LinkedHashMap<>();
            for (int index = 0; index < blocks.size(); index++) {
                CompoundTag record = blocks.getCompound(index);
                ListTag position = record.getList("pos", Tag.TAG_INT);
                capturedStates.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), decode(palette.getCompound(record.getInt("state"))));
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(capturedStates.get(fixture.world(entry.getKey()).subtract(minimum))), "World capture differs");
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(captured, output.toFile());
            StructureTemplate roundtrip = new StructureTemplate();
            roundtrip.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(output.toFile()));
            require(roundtrip.getSize().equals(SIZE), "Capture roundtrip failed");
            result.put("capture", root().relativize(output).toString());
            result.put("capture_sha256", hash(output));
            result.put("capture_exact_match", true);
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void stairHub(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("runtime", "Forge 47.4.10 / Minecraft 1.20.1 / isolated GameTestServer");
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Only isolated GameTestServer allowed");
            Path source = root().resolve("room.nbt");
            String fingerprint = hash(source);
            report.put("structure_sha256", fingerprint);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Metadata fingerprint mismatch");
            CompoundTag sourceData = NbtIo.readCompressed(source.toFile());
            Map<BlockPos, BlockState> expected = audit(sourceData);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), sourceData);
            require(template.getSize().equals(SIZE), "Native template load mismatch");
            require(audit(template.save(new CompoundTag())).equals(expected), "Native source roundtrip differs");
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(new BlockPos(8000 + index * 128, 120, 8000), rotation);
                for (int across = -1; across <= 35; across++) {
                    for (int depth = -1; depth <= 35; depth++) {
                        BlockPos position = fixture.world(new BlockPos(across, 0, depth));
                        level.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
                        for (int height = -1; height <= 18; height++) level.setBlock(position.offset(0, height, 0), Blocks.AIR.defaultBlockState(), FLAGS);
                    }
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement failed");
                verify(level, fixture, expected, metadata, false);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                BlockPos routeProbe = fixture.world(new BlockPos(17, 7, 11));
                level.setBlock(routeProbe, Blocks.STONE.defaultBlockState(), FLAGS);
                require(!walkable(level, routeProbe), "Blocked-route negative probe undetected");
                level.setBlock(routeProbe, Blocks.AIR.defaultBlockState(), FLAGS);
                BlockPos lamp = fixture.world(new BlockPos(14, 8, 10));
                BlockPos support = lamp.below();
                BlockState savedSupport = level.getBlockState(support);
                level.setBlock(support, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!level.getBlockState(lamp).canSurvive(level, lamp), "Unsupported-lantern negative probe undetected");
                level.setBlock(support, savedSupport, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                fixtures.add(fixture);
                index++;
            }
            report.put("negative_blocked_route_probes", 4);
            report.put("negative_unsupported_lantern_probes", 4);
            report.put("stabilization_ticks", 120);
            report.put("neighbor_updates_per_rotation", VOLUME);
            writeReport(report);
            helper.runAfterDelay(120, () -> {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) results.add(verify(level, fixture, expected, metadata, true));
                    require(fingerprint.equals(hash(source)), "Source mutated during QA");
                    report.put("rotations", results);
                    report.put("records_checked_after_ticks", VOLUME * 4);
                    report.put("status", "PASS");
                    report.put("limitations", List.of("Native geometric player-envelope validation; not a real client-controlled walking recording.", "No FT core route integration, occupancy/idempotence deployment, multiplayer or modpack gameplay validated."));
                    writeReport(report);
                    System.out.println("ZONE_N03_NATIVE_PASS rotations=4 records=" + VOLUME * 4 + " ticks=120 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception exception) {
                    report.put("status", "FAIL");
                    report.put("error", exception.toString());
                    try { writeReport(report); } catch (Exception secondary) { exception.addSuppressed(secondary); }
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
