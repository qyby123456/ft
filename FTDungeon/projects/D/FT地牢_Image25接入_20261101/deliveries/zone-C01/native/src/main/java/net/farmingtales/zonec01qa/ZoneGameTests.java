package net.farmingtales.zonec01qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
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
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftzonec01qa")
@PrefixGameTestTemplate(false)
public final class ZoneGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final int TICKS = 160;
    private static final Set<String> ALLOWED = Set.of("air", "stone_bricks", "cracked_stone_bricks", "mossy_stone_bricks", "chiseled_stone_bricks", "smooth_stone", "stone_brick_slab", "iron_bars", "barrel", "spruce_fence", "spruce_slab", "lantern", "spruce_stairs", "spruce_trapdoor", "cartography_table", "bookshelf", "spruce_planks", "lightning_rod", "red_wall_banner", "red_carpet", "yellow_carpet", "cobweb", "chain");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private ZoneGameTests() {}

    private static Path root() {
        return Path.of(System.getProperty("ftzonec01qa.root")).toAbsolutePath().normalize();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String hash(Path file) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/forge.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identifier = new ResourceLocation(encoded.getString("Name"));
        require(BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown block " + identifier);
        require(identifier.getNamespace().equals("minecraft") && ALLOWED.contains(identifier.getPath()), "Disallowed block " + identifier);
        Block block = BuiltInRegistries.BLOCK.get(identifier);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identifier + "/" + key);
        }
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag document) {
        require(document.getInt("DataVersion") == 3465, "DataVersion mismatch");
        require(!document.contains("palettes") && document.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Multiple palettes or entities");
        ListTag palette = document.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) states.add(decode(palette.getCompound(index)));
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        ListTag records = document.getList("blocks", Tag.TAG_COMPOUND);
        int barrels = 0;
        int banners = 0;
        for (int index = 0; index < records.size(); index++) {
            CompoundTag entry = records.getCompound(index);
            ListTag coordinates = entry.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getX() < 19 && position.getY() >= 0 && position.getY() < 11 && position.getZ() >= 0 && position.getZ() < 19, "Position outside volume");
            int stateIndex = entry.getInt("state");
            require(stateIndex >= 0 && stateIndex < states.size(), "Bad palette index");
            BlockState state = states.get(stateIndex);
            require(expected.put(position, state) == null, "Duplicate record");
            if (entry.contains("nbt")) {
                CompoundTag tag = entry.getCompound("nbt");
                require(!tag.contains("LootTable") && !tag.contains("Command"), "Unsafe block entity NBT");
                if (state.is(Blocks.BARREL)) {
                    require(tag.getString("id").equals("minecraft:barrel") && tag.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Nonempty barrel");
                    barrels++;
                } else {
                    require(state.is(Blocks.RED_WALL_BANNER) && tag.getString("id").equals("minecraft:banner"), "Unexpected block entity");
                    require(tag.getList("Patterns", Tag.TAG_COMPOUND).size() == 2, "Banner patterns missing");
                    banners++;
                }
            }
        }
        require(expected.size() == 3971 && barrels == 6 && banners == 1, "Volume/block entity counts mismatch");
        return expected;
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        AABB body = new AABB(feet.getX() + 0.2, feet.getY() + 0.01, feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8);
        return level.noCollision(body) && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP) && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty();
    }

    private static Set<BlockPos> route() {
        Set<BlockPos> cells = new HashSet<>();
        for (int across = 8; across <= 10; across++) {
            for (int depth = 0; depth <= 10; depth++) cells.add(new BlockPos(across, 2, depth));
        }
        for (int across = 8; across <= 18; across++) {
            for (int depth = 8; depth <= 10; depth++) cells.add(new BlockPos(across, 2, depth));
        }
        return cells;
    }

    private static void verifyConnectivity(ServerLevel level, Fixture fixture) {
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> queue = new ArrayList<>();
        BlockPos start = new BlockPos(9, 2, 0);
        queue.add(start);
        visited.add(start);
        for (int index = 0; index < queue.size(); index++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos adjacent = queue.get(index).relative(direction);
                if (route().contains(adjacent) && !visited.contains(adjacent) && walkable(level, fixture.world(adjacent))) {
                    visited.add(adjacent);
                    queue.add(adjacent);
                }
            }
        }
        require(visited.size() == 57 && visited.contains(new BlockPos(9, 2, 3)) && visited.contains(new BlockPos(18, 2, 9)), "Disconnected corner route");
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, boolean capture) throws Exception {
        int lanterns = 0;
        int barrels = 0;
        int banners = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState state = level.getBlockState(position);
            if (!state.equals(entry.getValue().rotate(fixture.rotation()))) {
                Map<String, String> neighbors = new LinkedHashMap<>();
                for (Direction direction : Direction.values()) neighbors.put(direction.getName(), level.getBlockState(position.relative(direction)).toString());
                throw new IllegalStateException("State changed " + fixture.rotation() + " local=" + entry.getKey() + " world=" + position + ": " + state + " expected " + entry.getValue().rotate(fixture.rotation()) + " neighbors=" + neighbors);
            }
            require(state.canSurvive(level, position), "Unsupported block " + position);
            require(level.getFluidState(position).isEmpty(), "Unexpected fluid " + position);
            BlockEntity entity = level.getBlockEntity(position);
            if (state.is(Blocks.BARREL)) {
                require(entity instanceof Container && ((Container) entity).isEmpty(), "Invalid or nonempty barrel");
                require(!entity.saveWithoutMetadata().contains("LootTable"), "Loot table found");
                barrels++;
            } else if (state.is(Blocks.RED_WALL_BANNER)) {
                require(entity != null && entity.saveWithoutMetadata().getList("Patterns", Tag.TAG_COMPOUND).size() == 2, "Missing banner data");
                banners++;
            } else require(entity == null, "Unexpected block entity " + position);
            if (state.is(Blocks.LANTERN)) lanterns++;
        }
        require(lanterns == 7 && barrels == 6 && banners == 1, "Decoration counts changed");
        for (BlockPos relative : route()) {
            require(walkable(level, fixture.world(relative)), "Blocked corner lane " + relative);
            require(level.getBlockState(fixture.world(relative.above(2))).isAir(), "Less than three-high clearance " + relative);
        }
        verifyConnectivity(level, fixture);
        for (int across = 0; across < 19; across++) {
            for (int depth = 0; depth < 19; depth++) {
                require(level.getBlockState(fixture.world(new BlockPos(across, 9, depth))).isFaceSturdy(level, fixture.world(new BlockPos(across, 9, depth)), Direction.DOWN), "Roof gap");
                require(level.getBlockState(fixture.world(new BlockPos(across, 1, depth))).isFaceSturdy(level, fixture.world(new BlockPos(across, 1, depth)), Direction.UP), "Floor gap");
            }
        }
        BlockPos first = fixture.world(BlockPos.ZERO);
        BlockPos last = fixture.world(new BlockPos(18, 10, 18));
        BlockPos minimum = new BlockPos(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()));
        AABB volume = new AABB(minimum.getX(), minimum.getY(), minimum.getZ(), minimum.getX() + 19, minimum.getY() + 11, minimum.getZ() + 19);
        require(level.getEntities(null, volume).isEmpty(), "Unexpected entities or dropped blocks: " + level.getEntities(null, volume).stream().map(entity -> entity.getType().toString()).toList());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records_exact", expected.size());
        result.put("can_survive_checks", expected.size());
        result.put("collision_tested_corner_cells", route().size());
        result.put("safe_landing", fixture.world(new BlockPos(9, 2, 3)).toShortString());
        result.put("entrance", fixture.world(new BlockPos(9, 2, 0)).toShortString());
        result.put("exit", fixture.world(new BlockPos(18, 2, 9)).toShortString());
        result.put("entry_side", fixture.rotation().rotate(Direction.NORTH).getName());
        result.put("empty_barrels", barrels);
        result.put("banner_patterns", 2);
        result.put("supported_lanterns", lanterns);
        result.put("floor_and_roof_cells", 361);
        result.put("entities", 0);
        result.put("status", "PASS");
        if (capture) {
            StructureTemplate saved = new StructureTemplate();
            saved.fillFromWorld(level, minimum, new Vec3i(19, 11, 19), false, null);
            CompoundTag encoded = saved.save(new CompoundTag());
            encoded.putInt("DataVersion", 3465);
            require(encoded.getList("blocks", Tag.TAG_COMPOUND).size() == expected.size(), "Capture record count mismatch");
            ListTag capturedPalette = encoded.getList("palette", Tag.TAG_COMPOUND);
            Map<BlockPos, BlockState> actual = new LinkedHashMap<>();
            ListTag captured = encoded.getList("blocks", Tag.TAG_COMPOUND);
            for (int index = 0; index < captured.size(); index++) {
                CompoundTag record = captured.getCompound(index);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                actual.put(minimum.offset(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2)), decode(capturedPalette.getCompound(record.getInt("state"))));
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(actual.get(fixture.world(entry.getKey()))), "Saved world capture mismatch");
            Path output = root().resolve("qa/captures/" + fixture.rotation().name().toLowerCase() + ".nbt");
            Files.createDirectories(output.getParent());
            NbtIo.writeCompressed(encoded, output.toFile());
            result.put("world_capture", root().relativize(output).toString());
            result.put("capture_sha256", hash(output));
            result.put("capture_matches_rotated_source", true);
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void cornerFourRotationsAndStabilize(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("isolated_gametest_server", helper.getLevel().getServer() instanceof GameTestServer);
        try {
            require(helper.getLevel().getServer() instanceof GameTestServer, "Not an isolated GameTest server");
            String fingerprint = hash(root().resolve("room.nbt"));
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Metadata hash mismatch");
            report.put("structure_sha256", fingerprint);
            CompoundTag document = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(document);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), document);
            require(template.getSize().equals(new Vec3i(19, 11, 19)), "Template size mismatch");
            CompoundTag roundtrip = template.save(new CompoundTag());
            require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == expected.size(), "Native roundtrip lost records");
            NbtIo.writeCompressed(roundtrip, root().resolve("qa/native-roundtrip.nbt").toFile());
            report.put("decode_registry_properties_roundtrip", "PASS");
            ServerLevel level = helper.getLevel();
            level.getGameRules().getRule(GameRules.RULE_DOBLOCKDROPS).set(false, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(helper.absolutePos(new BlockPos(40 + index * 64, 24, 40)), rotation);
                for (BlockPos relative : BlockPos.betweenClosed(new BlockPos(-2, -2, -2), new BlockPos(20, 12, 20))) {
                    BlockPos position = fixture.world(relative);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                }
                BlockPos cleanupFirst = fixture.world(new BlockPos(-2, -2, -2));
                BlockPos cleanupLast = fixture.world(new BlockPos(20, 12, 20));
                AABB cleanup = new AABB(Math.min(cleanupFirst.getX(), cleanupLast.getX()), cleanupFirst.getY(), Math.min(cleanupFirst.getZ(), cleanupLast.getZ()), Math.max(cleanupFirst.getX(), cleanupLast.getX()) + 1, cleanupLast.getY() + 1, Math.max(cleanupFirst.getZ(), cleanupLast.getZ()) + 1);
                level.getEntities(null, cleanup).forEach(entity -> entity.discard());
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement failed");
                verify(level, fixture, expected, false);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    BlockState state = level.getBlockState(position);
                    for (Direction direction : Direction.values()) state = state.updateShape(direction, level.getBlockState(position.relative(direction)), level, position, position.relative(direction));
                    level.setBlock(position, state, FLAGS);
                    level.updateNeighborsAt(position, state.getBlock());
                }
                BlockPos support = fixture.world(new BlockPos(9, 7, 3));
                BlockPos lamp = fixture.world(new BlockPos(9, 6, 3));
                BlockState savedSupport = level.getBlockState(support);
                level.setBlock(support, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!level.getBlockState(lamp).canSurvive(level, lamp), "Missing-support negative control failed");
                level.setBlock(support, savedSupport, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos aisle = fixture.world(new BlockPos(9, 2, 5));
                level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS);
                require(!walkable(level, aisle), "Blocked-path negative control failed");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS);
                fixtures.add(fixture);
                index++;
            }
            report.put("negative_missing_support_probes", 4);
            report.put("negative_blocked_path_probes", 4);
            report.put("stabilization_ticks", TICKS);
            report.put("neighbor_shape_updates", "All six faces of all 15884 placed records, plus neighbor notifications");
            writeReport(report);
            helper.runAfterDelay(TICKS, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) rotations.add(verify(level, fixture, expected, true));
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source NBT changed during test");
                    report.put("rotations", rotations);
                    report.put("records_checked_after_ticks", expected.size() * 4);
                    report.put("status", "PASS");
                    report.put("limitations", List.of("Isolated Forge GameTest, not full modpack client/multiplayer/FPS verification.", "No FT integration, production placement, deployment or player save modified."));
                    writeReport(report);
                    System.out.println("ZONE_C01_FORGE_PASS rotations=4/4 records=15884 ticks=" + TICKS + " source=" + fingerprint);
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
