package net.farmingtales.corridors16qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("corridors16qa")
@PrefixGameTestTemplate(false)
public final class CorridorGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("ice", "frosted_ice", "snow", "powder_snow", "water", "lava", "fire", "soul_fire", "magma_block", "cactus", "cobweb", "wither_rose", "sweet_berry_bush", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "structure_void", "jigsaw");
    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private CorridorGameTests() {}

    private static Path root() {
        return Path.of(System.getProperty("corridors16qa.root")).toAbsolutePath().normalize();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String hash(Path file) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/forge-native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    private static BlockPos coordinate(JsonArray value) {
        return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt());
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla identity " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden identity " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        require(properties.getAllKeys().size() == block.getStateDefinition().getProperties().size(), "Incomplete properties " + identity);
        for (String propertyName : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(propertyName);
            require(property != null && property.getValue(properties.getString(propertyName)).isPresent(), "Invalid property " + identity + " " + propertyName);
        }
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(state.getFluidState().isEmpty() && !state.hasBlockEntity(), "Fluid or block entity prohibited");
        return state;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require Java 1.20.1 single palette");
        ListTag dimensions = data.getList("size", Tag.TAG_INT);
        require(dimensions.size() == 3 && dimensions.getInt(0) == 11 && dimensions.getInt(1) == 10 && dimensions.getInt(2) == 16, "Incorrect NBT dimensions");
        require(coordinate(metadata.getAsJsonArray("size")).equals(new BlockPos(11, 10, 16)), "Metadata size mismatch");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected entity payloads");
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < encodedPalette.size(); index++) palette.add(decode(encodedPalette.getCompound(index)));
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag position = record.getList("pos", Tag.TAG_INT);
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(position.size() == 3 && relative.getX() >= 0 && relative.getX() < 11 && relative.getY() >= 0 && relative.getY() < 10 && relative.getZ() >= 0 && relative.getZ() < 16, "Invalid record coordinate");
            int stateIndex = record.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Invalid state index");
            require(result.put(relative, palette.get(stateIndex)) == null, "Duplicate record");
            require(!record.contains("nbt"), "Unexpected block NBT or inventory");
        }
        require(result.size() == 1760, "Full explicit volume required");
        return result;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getFluidState().isEmpty() && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState floor = level.getBlockState(below);
        return clear(level, feet) && clear(level, feet.above()) && floor.getFluidState().isEmpty() && floor.isFaceSturdy(level, below, Direction.UP);
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject metadata, boolean capture) throws Exception {
        int rods = 0;
        int vines = 0;
        int chains = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " expected=" + entry.getValue().rotate(fixture.rotation()) + " actual=" + actual);
            require(actual.canSurvive(level, position), "Unsupported native block " + entry.getKey());
            require(actual.getFluidState().isEmpty() && level.getBlockEntity(position) == null, "Unexpected fluid or block entity");
            if (actual.is(Blocks.VINE)) {
                boolean supported = false;
                for (Direction direction : Direction.values()) {
                    Property<?> property = actual.getBlock().getStateDefinition().getProperty(direction.getName());
                    if (property != null && actual.getValue(property).toString().equals("true")) {
                        BlockPos anchor = position.relative(direction);
                        supported |= level.getBlockState(anchor).isFaceSturdy(level, anchor, direction.getOpposite());
                    }
                }
                require(supported, "Vine lacks direct full support " + entry.getKey());
                vines++;
            }
            if (actual.is(Blocks.END_ROD)) {
                BlockState below = level.getBlockState(position.below());
                require(below.is(Blocks.END_ROD) || below.isFaceSturdy(level, position.below(), Direction.UP), "Rod strip lacks base");
                rods++;
            }
            if (actual.is(Blocks.CHAIN)) {
                BlockPos above = position.above();
                BlockState anchor = level.getBlockState(above);
                require(anchor.is(Blocks.CHAIN) || anchor.isFaceSturdy(level, above, Direction.DOWN), "Chain lacks ceiling anchor");
                chains++;
            }
        }
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos entrance = coordinate(metadata.getAsJsonArray("entrance"));
        require(walkable(level, fixture.world(entrance)), "Blocked entrance");
        visited.add(entrance);
        queue.add(entrance);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() >= 0 && next.getX() < 11 && next.getZ() >= 0 && next.getZ() < 16 && !visited.contains(next) && walkable(level, fixture.world(next))) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        require(visited.contains(coordinate(metadata.getAsJsonArray("interior"))) && visited.contains(coordinate(metadata.getAsJsonArray("exit"))), "Entrance/interior/exit disconnected");
        for (int depth = 0; depth < 16; depth++) {
            for (int horizontal = 3; horizontal <= 7; horizontal++) {
                BlockPos feet = new BlockPos(horizontal, 1, depth);
                require(visited.contains(feet) && clear(level, fixture.world(feet.above(2))), "Five-wide three-high aisle blocked");
            }
        }
        for (int depth = 0; depth < 16; depth++) {
            for (int horizontal = 0; horizontal < 11; horizontal++) {
                BlockPos roof = fixture.world(new BlockPos(horizontal, 9, depth));
                require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Roof hole");
            }
        }
        BlockPos minimum = fixture.world(BlockPos.ZERO);
        BlockPos maximum = minimum;
        for (BlockPos relative : expected.keySet()) {
            BlockPos position = fixture.world(relative);
            minimum = new BlockPos(Math.min(minimum.getX(), position.getX()), Math.min(minimum.getY(), position.getY()), Math.min(minimum.getZ(), position.getZ()));
            maximum = new BlockPos(Math.max(maximum.getX(), position.getX()), Math.max(maximum.getY(), position.getY()), Math.max(maximum.getZ(), position.getZ()));
        }
        require(level.getEntities(null, new AABB(minimum, maximum.offset(1, 1, 1))).isEmpty(), "Unexpected world entities");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records_checked", expected.size());
        result.put("reachable_foot_positions", visited.size());
        result.put("supported_end_rods", rods);
        result.put("direct_supported_vines", vines);
        result.put("roof_anchored_chain_blocks", chains);
        result.put("roof_floor_collision_inventory_fluids_entities", "PASS");
        if (capture) {
            Vec3i size = new Vec3i(maximum.getX() - minimum.getX() + 1, 10, maximum.getZ() - minimum.getZ() + 1);
            StructureTemplate captured = new StructureTemplate();
            captured.fillFromWorld(level, minimum, size, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = captured.save(new CompoundTag());
            Map<BlockPos, BlockState> roundtrip = new LinkedHashMap<>();
            ListTag palette = saved.getList("palette", Tag.TAG_COMPOUND);
            ListTag records = saved.getList("blocks", Tag.TAG_COMPOUND);
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag position = record.getList("pos", Tag.TAG_INT);
                roundtrip.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), decode(palette.getCompound(record.getInt("state"))));
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(roundtrip.get(fixture.world(entry.getKey()).subtract(minimum))), "World capture differs");
            Path export = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, export.toFile());
            result.put("capture_matches_source", true);
            result.put("world_capture", root().relativize(export).toString());
            result.put("capture_sha256", hash(export));
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void fourRotationsAndTwoHundredTickStability(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("scope", "Isolated headless Forge GameTest; no player save, FT integration or client play test");
        try {
            String fingerprint = hash(root().resolve("room.nbt"));
            report.put("structure_sha256", fingerprint);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(metadata.get("sha256").getAsString().equals(fingerprint), "Metadata hash mismatch");
            CompoundTag data = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(data, metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(new Vec3i(11, 10, 16)), "Template native dimensions mismatch");
            require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == expected.size(), "Native roundtrip lost records");
            report.put("native_decode_registry_properties_roundtrip", "PASS");
            ServerLevel level = helper.getLevel();
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DO_VINES_SPREAD).set(false, level.getServer());
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(helper.absolutePos(new BlockPos(32 + index * 40, 20, 32)), rotation);
                for (int horizontal = -1; horizontal <= 11; horizontal++) {
                    for (int vertical = -1; vertical <= 10; vertical++) {
                        for (int depth = -1; depth <= 16; depth++) {
                            BlockPos position = fixture.world(new BlockPos(horizontal, vertical, depth));
                            level.getChunk(position);
                            level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                        }
                    }
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement failed");
                verify(level, fixture, expected, metadata, false);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    BlockState state = level.getBlockState(position);
                    level.updateNeighborsAt(position, state.getBlock());
                    state.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
                }
                BlockPos aisle = fixture.world(new BlockPos(5, 1, 2));
                level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS);
                require(!walkable(level, aisle), "Blocked aisle negative probe not detected");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS);
                BlockPos backing = fixture.world(new BlockPos(4, 9, 3));
                BlockPos vine = fixture.world(new BlockPos(4, 8, 3));
                BlockState saved = level.getBlockState(backing);
                level.setBlock(backing, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!level.getBlockState(vine).canSurvive(level, vine), "Missing vine support negative probe not detected");
                level.setBlock(backing, saved, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                fixtures.add(fixture);
                index++;
            }
            report.put("negative_collision_probes", 4);
            report.put("disposable_fixture_air_halo_blocks", 1);
            report.put("negative_vine_support_probes", 4);
            report.put("stabilization_ticks", 200);
            report.put("random_tick_speed", level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING));
            report.put("doVinesSpread", false);
            writeReport(report);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) rotations.add(verify(level, fixture, expected, metadata, true));
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source changed during verification");
                    report.put("rotations", rotations);
                    report.put("records_checked_after_ticks", expected.size() * rotations.size());
                    report.put("status", "PASS");
                    report.put("limitations", List.of("No production deployment, FT slot, occupied-volume refusal or integration idempotence tested.", "No full modpack client, FPS, multiplayer or player-world construction tested.", "Vine spreading is disabled only in the disposable test world; it may grow naturally in a future deployment."));
                    writeReport(report);
                    System.out.println("CORRIDOR_S16_NATIVE_PASS rotations=4 records=" + expected.size() * rotations.size() + " ticks=200 sha256=" + fingerprint);
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
