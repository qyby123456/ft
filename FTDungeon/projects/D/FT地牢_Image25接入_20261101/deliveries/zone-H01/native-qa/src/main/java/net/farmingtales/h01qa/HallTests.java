package net.farmingtales.h01qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fth01qa")
@PrefixGameTestTemplate(false)
public final class HallTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final int COUNT = 29 * 18 * 33;
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "spawner", "tnt", "respawn_anchor");
    private static final Map<BlockPos, CompoundTag> SOURCE_TILES = new LinkedHashMap<>();

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
        Vec3 point(double east, double up, double south) {
            return StructureTemplate.transform(new Vec3(east, up, south), Mirror.NONE, rotation, BlockPos.ZERO).add(origin.getX(), origin.getY(), origin.getZ());
        }
        BlockPos minimum() {
            BlockPos minimum = world(BlockPos.ZERO);
            for (BlockPos corner : List.of(new BlockPos(28, 0, 0), new BlockPos(0, 0, 32), new BlockPos(28, 0, 32))) {
                BlockPos point = world(corner);
                minimum = new BlockPos(Math.min(minimum.getX(), point.getX()), origin.getY(), Math.min(minimum.getZ(), point.getZ()));
            }
            return minimum;
        }
        Vec3i size() {
            return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(33, 18, 29) : new Vec3i(29, 18, 33);
        }
    }

    private static Path root() { return Path.of(System.getProperty("fth01qa.root")).toAbsolutePath().normalize(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static String hash(Path file) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))); }
    private static void report(Map<String, Object> value) throws Exception { Files.writeString(root().resolve("qa/native.json"), JSON.toJson(value) + "\n", StandardCharsets.UTF_8); }
    private static BlockPos coordinate(ListTag value) {
        require(value.size() == 3, "Invalid coordinate");
        return new BlockPos(value.getInt(0), value.getInt(1), value.getInt(2));
    }
    private static boolean inside(BlockPos relative) { return relative.getX() >= 0 && relative.getX() < 29 && relative.getY() >= 0 && relative.getY() < 18 && relative.getZ() >= 0 && relative.getZ() < 33; }
    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity) && !FORBIDDEN.contains(identity.getPath()), "Invalid vanilla ID " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identity + " " + key);
        }
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
    }
    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465, "Not Java 1.20.1");
        require(coordinate(data.getList("size", Tag.TAG_INT)).equals(new BlockPos(29, 18, 33)), "Size differs");
        require(!data.contains("palettes") && data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Alternate palette/entities");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> decoded = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) decoded.add(decode(palette.getCompound(index)));
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag entry = blocks.getCompound(index);
            BlockPos position = coordinate(entry.getList("pos", Tag.TAG_INT));
            int state = entry.getInt("state");
            require(inside(position) && state >= 0 && state < decoded.size(), "Invalid voxel");
            require(result.put(position, decoded.get(state)) == null, "Duplicate voxel");
            if (entry.contains("nbt")) {
                CompoundTag tile = entry.getCompound("nbt");
                require(Set.of("minecraft:barrel", "minecraft:banner").contains(tile.getString("id")), "Unexpected tile");
                require(!tile.contains("LootTable") && tile.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Loot stock");
                SOURCE_TILES.put(position, tile);
            }
        }
        require(result.size() == COUNT, "Not full explicit volume");
        return result;
    }
    private static boolean emptyVolume(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected) {
        for (BlockPos relative : expected.keySet()) if (!level.getBlockState(fixture.world(relative)).isAir()) return false;
        return true;
    }
    private static boolean stand(ServerLevel level, Fixture fixture, double east, double feet, double south) {
        Vec3 point = fixture.point(east, feet, south);
        AABB body = new AABB(point.x - 0.3, point.y + 0.0001, point.z - 0.3, point.x + 0.3, point.y + 1.8, point.z + 0.3);
        if (!level.noCollision(null, body) || level.noCollision(null, body.move(0, -0.02, 0))) return false;
        for (BlockPos position : BlockPos.betweenClosed(BlockPos.containing(body.minX, body.minY, body.minZ), BlockPos.containing(body.maxX, body.maxY, body.maxZ))) {
            BlockState state = level.getBlockState(position);
            if (!state.getFluidState().isEmpty() || state.is(Blocks.CAMPFIRE) || state.is(Blocks.COBWEB) || state.is(Blocks.FIRE)) return false;
        }
        return true;
    }
    private static boolean lane(ServerLevel level, Fixture fixture, double east, boolean reverse) {
        double height = reverse ? 4 : 2;
        for (int index = 0; index <= 108; index++) {
            double south = reverse ? 27.5 - index * 0.25 : 0.5 + index * 0.25;
            boolean found = false;
            for (double candidate : new double[]{height - 0.5, height, height + 0.5}) {
                if (candidate >= 2 && candidate <= 4 && stand(level, fixture, east, candidate, south)) {
                    height = candidate;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return height == (reverse ? 2 : 4);
    }
    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, boolean capture) throws Exception {
        int lamps = 0;
        int braziers = 0;
        int barrels = 0;
        int banners = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " " + actual);
            require(actual.canSurvive(level, position), "Unsupported voxel " + entry.getKey());
            if (actual.is(Blocks.LANTERN)) {
                require(level.getBlockState(position.above()).is(Blocks.CHAIN), "Unsupported lantern");
                lamps++;
            }
            if (actual.is(Blocks.CAMPFIRE)) {
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Unsupported brazier");
                braziers++;
            }
            var tile = level.getBlockEntity(position);
            if (tile != null) {
                CompoundTag saved = tile.saveWithFullMetadata();
                require(!saved.contains("LootTable") && saved.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Unexpected inventory or loot");
                if (tile instanceof Container container) require(container.isEmpty(), "Non-empty container");
                if (actual.is(Blocks.BARREL)) barrels++;
                else if (actual.is(Blocks.RED_WALL_BANNER)) banners++;
                else require(actual.is(Blocks.CAMPFIRE), "Unexpected block entity");
                CompoundTag source = SOURCE_TILES.get(entry.getKey());
                if (source != null && source.contains("Patterns")) require(saved.getList("Patterns", Tag.TAG_COMPOUND).equals(source.getList("Patterns", Tag.TAG_COMPOUND)), "Banner pattern drift");
            } else require(!SOURCE_TILES.containsKey(entry.getKey()), "Missing block entity");
        }
        require(lamps == 6 && braziers == 4 && barrels == 6 && banners == 6, "Fixture totals differ");
        for (int east = 0; east < 29; east++) for (int south = 0; south < 33; south++) {
            BlockPos floor = fixture.world(new BlockPos(east, 0, south));
            BlockPos roof = fixture.world(new BlockPos(east, 17, south));
            require(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Missing underfloor");
            require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Missing roof");
        }
        for (int east = 12; east <= 16; east++) {
            require(lane(level, fixture, east + 0.5, false) && lane(level, fixture, east + 0.5, true), "Five-wide bidirectional walking/stairs collision failed at x=" + east);
            for (int up = 2; up <= 7; up++) require(level.getBlockState(fixture.world(new BlockPos(east, up, 0))).isAir(), "Port obstructed");
        }
        for (BlockPos safe : List.of(new BlockPos(14, 2, 4), new BlockPos(14, 2, 12), new BlockPos(14, 2, 21), new BlockPos(14, 4, 26))) require(stand(level, fixture, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5), "Unsafe waypoint");
        BlockPos minimum = fixture.minimum();
        Vec3i size = fixture.size();
        int fluids = 0;
        for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), minimum.offset(size.getX(), size.getY(), size.getZ()))) if (!level.getFluidState(position).isEmpty()) fluids++;
        require(fluids == 0, "Unexpected fluid");
        var entities = level.getEntities(null, new AABB(minimum.offset(-1, -1, -1), minimum.offset(size.getX() + 1, size.getY() + 1, size.getZ() + 1)));
        require(entities.isEmpty(), "Unexpected entities/drops " + entities);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("status", "PASS");
        result.put("records_checked", expected.size());
        result.put("collision_path_samples_bidirectional", 1090);
        result.put("max_step_height", 0.5);
        result.put("player_collision_size", List.of(0.6, 1.8, 0.6));
        result.put("safe_waypoints", 4);
        result.put("roof_cells", 957);
        result.put("lanterns_supported", lamps);
        result.put("braziers_supported", braziers);
        result.put("empty_barrels", barrels);
        result.put("decorative_banners", banners);
        result.put("fluid_cells_in_volume_and_halo", fluids);
        result.put("world_entities", entities.size());
        result.put("capture_size", List.of(size.getX(), size.getY(), size.getZ()));
        if (capture) {
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, size, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, output.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(output.toFile()));
            require(reloaded.getSize().equals(size), "Capture reload size differs");
            Map<BlockPos, BlockState> captured = new LinkedHashMap<>();
            ListTag palette = saved.getList("palette", Tag.TAG_COMPOUND);
            ListTag blocks = saved.getList("blocks", Tag.TAG_COMPOUND);
            require(blocks.size() == COUNT, "Capture voxel count differs");
            for (int index = 0; index < blocks.size(); index++) {
                CompoundTag block = blocks.getCompound(index);
                captured.put(coordinate(block.getList("pos", Tag.TAG_INT)), decode(palette.getCompound(block.getInt("state"))));
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(captured.get(fixture.world(entry.getKey()).subtract(minimum))), "Capture differs");
            result.put("capture_matches_source", true);
            result.put("world_capture", root().relativize(output).toString());
            result.put("world_capture_sha256", hash(output));
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", "H01");
        result.put("status", "RUNNING");
        result.put("minecraft", "1.20.1");
        result.put("forge", "47.4.10");
        result.put("java", System.getProperty("java.version"));
        result.put("scope", "Disposable isolated Forge GameTestServer; no player save, client, FT core or deployment");
        report(result);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Refusing non-isolated server");
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
            level.setDayTime(6000);
            Path source = root().resolve("room.nbt");
            String fingerprint = hash(source);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Source fingerprint differs");
            result.put("structure_sha256", fingerprint);
            CompoundTag encoded = NbtIo.readCompressed(source.toFile());
            Map<BlockPos, BlockState> expected = audit(encoded);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), encoded);
            StructureTemplate roundtrip = new StructureTemplate();
            roundtrip.load(BuiltInRegistries.BLOCK.asLookup(), template.save(new CompoundTag()));
            require(roundtrip.getSize().equals(template.getSize()), "Native roundtrip differs");
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> before = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos horizontal = helper.absolutePos(new BlockPos(80 + index * 96, 0, 80));
                Fixture fixture = new Fixture(new BlockPos(horizontal.getX(), 192, horizontal.getZ()), rotation);
                BlockPos minimum = fixture.minimum();
                Vec3i size = fixture.size();
                for (int chunkEast = (minimum.getX() - 2) >> 4; chunkEast <= (minimum.getX() + size.getX() + 2) >> 4; chunkEast++) for (int chunkSouth = (minimum.getZ() - 2) >> 4; chunkSouth <= (minimum.getZ() + size.getZ() + 2) >> 4; chunkSouth++) level.setChunkForced(chunkEast, chunkSouth, true);
                require(emptyVolume(level, fixture, expected), "Isolated fixture volume occupied");
                BlockPos sentinel = fixture.world(new BlockPos(14, 2, 5));
                level.setBlock(sentinel, Blocks.GOLD_BLOCK.defaultBlockState(), FLAGS);
                require(!emptyVolume(level, fixture, expected), "Occupied air voxel probe missed");
                level.setBlock(sentinel, Blocks.AIR.defaultBlockState(), FLAGS);
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placeInWorld false");
                before.add(verify(level, fixture, expected, false));
                BlockPos chain = fixture.world(new BlockPos(9, 10, 8));
                BlockState chainState = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lamp = fixture.world(new BlockPos(9, 9, 8));
                require(!level.getBlockState(lamp).canSurvive(level, lamp), "Missing chain probe missed");
                level.setBlock(chain, chainState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                level.setBlock(sentinel, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!lane(level, fixture, 14.5, false), "Blocked route probe missed");
                level.setBlock(sentinel, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
                    BlockPos position = fixture.world(entry.getKey());
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                fixtures.add(fixture);
                index++;
            }
            result.put("rotations_before_ticks", before);
            result.put("negative_probes", Map.of("occupied_air_voxel_refused", 4, "missing_chain_detected", 4, "blocked_route_detected", 4));
            result.put("chunks_forced_to_tick", true);
            result.put("neighbor_updates_triggered", true);
            result.put("stabilization_ticks", 200);
            report(result);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> after = new ArrayList<>();
                    for (Fixture fixture : fixtures) after.add(verify(level, fixture, expected, true));
                    require(hash(source).equals(fingerprint), "Source changed during run");
                    result.put("rotations", after);
                    result.put("records_checked_after_ticks", COUNT * after.size());
                    result.put("source_unchanged", true);
                    result.put("status", "PASS");
                    report(result);
                    System.out.println("H01_NATIVE_PASS rotations=4 records=" + COUNT * 4 + " ticks=200 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception error) {
                    result.put("status", "FAIL");
                    result.put("error", error.toString());
                    try { report(result); } catch (Exception secondary) { error.addSuppressed(secondary); }
                    throw new IllegalStateException(error);
                }
            });
        } catch (Exception error) {
            result.put("status", "FAIL");
            result.put("error", error.toString());
            report(result);
            throw error;
        }
    }
}
