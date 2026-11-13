package net.farmingtales.zonem05qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftzonem05qa")
@PrefixGameTestTemplate(false)
public final class GrottoTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final int COUNT = 27 * 15 * 27;
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "spawner", "tnt", "respawn_anchor");
    private static final Set<String> HAZARDS = Set.of("lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "wither_rose", "sweet_berry_bush", "cobweb", "campfire", "soul_campfire");
    private static final int[][] PILLARS = {{5, 7}, {20, 7}, {5, 19}, {20, 19}};
    private static final int[][] LAMPS = {{9, 8, 7}, {17, 8, 7}, {9, 8, 20}, {17, 8, 20}};

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) { return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO)); }
        BlockPos minimum() {
            BlockPos minimum = world(BlockPos.ZERO);
            for (BlockPos corner : List.of(new BlockPos(26, 0, 0), new BlockPos(0, 0, 26), new BlockPos(26, 0, 26))) {
                BlockPos point = world(corner);
                minimum = new BlockPos(Math.min(minimum.getX(), point.getX()), origin.getY(), Math.min(minimum.getZ(), point.getZ()));
            }
            return minimum;
        }
    }
    private static Path root() { return Path.of(System.getProperty("ftzonem05qa.root")).toAbsolutePath().normalize(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static String hash(Path file) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))); }
    private static void report(Map<String, Object> value) throws Exception { Files.writeString(root().resolve("qa/native.json"), JSON.toJson(value) + "\n", StandardCharsets.UTF_8); }
    private static BlockPos coordinate(ListTag value) { require(value.size() == 3, "Invalid coordinate"); return new BlockPos(value.getInt(0), value.getInt(1), value.getInt(2)); }
    private static boolean inside(BlockPos position) { return position.getX() >= 0 && position.getX() < 27 && position.getY() >= 0 && position.getY() < 15 && position.getZ() >= 0 && position.getZ() < 27; }
    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla ID " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden ID " + identity);
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
        require(coordinate(data.getList("size", Tag.TAG_INT)).equals(new BlockPos(27, 15, 27)), "Size differs");
        require(!data.contains("palettes") && data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Entities or alternate palettes");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> decoded = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) decoded.add(decode(palette.getCompound(index)));
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag entry = blocks.getCompound(index);
            BlockPos position = coordinate(entry.getList("pos", Tag.TAG_INT));
            int state = entry.getInt("state");
            require(inside(position) && state >= 0 && state < decoded.size(), "Invalid voxel");
            require(!entry.contains("nbt") && !decoded.get(state).hasBlockEntity(), "Unexpected inventory or block entity");
            require(result.put(position, decoded.get(state)) == null, "Duplicate voxel");
        }
        require(result.size() == COUNT, "Not full explicit volume");
        return result;
    }
    private static boolean safe(BlockState state) { return state.getFluidState().isEmpty() && !HAZARDS.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()); }
    private static boolean clear(ServerLevel level, BlockPos position) { BlockState state = level.getBlockState(position); return safe(state) && state.getCollisionShape(level, position).isEmpty(); }
    private static boolean floor(ServerLevel level, BlockPos position) { BlockState state = level.getBlockState(position); return safe(state) && state.isFaceSturdy(level, position, Direction.UP); }
    private static boolean walkable(ServerLevel level, BlockPos position) { return clear(level, position) && clear(level, position.above()) && floor(level, position.below()); }
    private static boolean emptyVolume(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected) {
        for (BlockPos relative : expected.keySet()) if (!level.getBlockState(fixture.world(relative)).isAir()) return false;
        return true;
    }
    private static boolean contained(ServerLevel level, BlockPos fluid) {
        if (!level.getBlockState(fluid.below()).isFaceSturdy(level, fluid.below(), Direction.UP)) return false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = fluid.relative(direction);
            BlockState state = level.getBlockState(neighbor);
            if (!state.is(Blocks.WATER) && !state.isFaceSturdy(level, neighbor, direction.getOpposite())) return false;
        }
        return true;
    }
    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, boolean capture) throws Exception {
        int water = 0;
        int amethyst = 0;
        int lamps = 0;
        int rails = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " " + actual);
            require(actual.canSurvive(level, position), "Unsupported voxel " + entry.getKey());
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (actual.is(Blocks.WATER)) { require(actual.getFluidState().isSource() && contained(level, position), "Uncontained water"); water++; }
            if (actual.is(Blocks.AMETHYST_CLUSTER) || actual.is(Blocks.LARGE_AMETHYST_BUD)) amethyst++;
            if (actual.is(Blocks.LANTERN)) lamps++;
            if (actual.is(Blocks.RAIL)) rails++;
        }
        require(water == 41 && amethyst == 15 && lamps == 6 && rails == 9, "Theme counts differ");
        for (int east = 0; east < 27; east++) for (int south = 0; south < 27; south++) {
            require(floor(level, fixture.world(new BlockPos(east, 0, south))), "Missing foundation");
            BlockPos roof = fixture.world(new BlockPos(east, 14, south));
            require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Missing roof");
        }
        for (int[] pillar : PILLARS) for (int offsetEast = 0; offsetEast < 2; offsetEast++) for (int offsetSouth = 0; offsetSouth < 2; offsetSouth++) for (int up = 1; up <= 14; up++) require(floor(level, fixture.world(new BlockPos(pillar[0] + offsetEast, up, pillar[1] + offsetSouth))), "Pillar is discontinuous");
        for (int[] lamp : LAMPS) {
            for (int up = 9; up <= 11; up++) require(level.getBlockState(fixture.world(new BlockPos(lamp[0], up, lamp[2]))).is(Blocks.CHAIN), "Broken hanging chain");
            BlockPos anchor = fixture.world(new BlockPos(lamp[0], 12, lamp[2]));
            require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Missing chain anchor");
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        BlockPos entrance = new BlockPos(13, 2, 0);
        require(walkable(level, fixture.world(entrance)), "Unsafe entrance");
        reached.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (inside(next) && !reached.contains(next) && walkable(level, fixture.world(next))) { reached.add(next); pending.add(next); }
            }
        }
        require(reached.contains(new BlockPos(13, 2, 5)), "Interior safety point unreachable");
        for (int east = 11; east <= 15; east++) for (int south = 0; south <= 24; south++) {
            require(reached.contains(new BlockPos(east, 2, south)), "Main route unreachable");
            for (int up = 2; up <= 5; up++) require(clear(level, fixture.world(new BlockPos(east, up, south))), "Route less than four blocks clear");
        }
        BlockPos minimum = fixture.minimum();
        int haloFluids = 0;
        for (int east = -1; east <= 27; east++) for (int up = -1; up <= 15; up++) for (int south = -1; south <= 27; south++) {
            if (east >= 0 && east < 27 && up >= 0 && up < 15 && south >= 0 && south < 27) continue;
            if (!level.getFluidState(minimum.offset(east, up, south)).isEmpty()) haloFluids++;
        }
        require(haloFluids == 0, "Fluid escaped template");
        var entities = level.getEntities(null, new AABB(minimum.offset(-1, -1, -1), minimum.offset(28, 16, 28)));
        require(entities.isEmpty(), "Unexpected entities or dropped items " + entities);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("status", "PASS");
        result.put("records_checked", COUNT);
        result.put("reachable_dry_foot_positions", reached.size());
        result.put("main_route_foot_positions", 125);
        result.put("water_sources_contained", water);
        result.put("amethyst_supported", amethyst);
        result.put("lanterns_supported", lamps);
        result.put("support_pillars", 4);
        result.put("roof_cells", 729);
        result.put("external_fluid_cells", haloFluids);
        result.put("world_entities", entities.size());
        result.put("origin_world", List.of(fixture.origin().getX(), fixture.origin().getY(), fixture.origin().getZ()));
        if (capture) {
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, new Vec3i(27, 15, 27), false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, output.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(output.toFile()));
            require(reloaded.getSize().equals(new Vec3i(27, 15, 27)), "Native capture reload failed");
            ListTag palette = saved.getList("palette", Tag.TAG_COMPOUND);
            ListTag blocks = saved.getList("blocks", Tag.TAG_COMPOUND);
            require(blocks.size() == COUNT, "Native capture missing records");
            Map<BlockPos, BlockState> captured = new LinkedHashMap<>();
            for (int index = 0; index < blocks.size(); index++) {
                CompoundTag entry = blocks.getCompound(index);
                captured.put(coordinate(entry.getList("pos", Tag.TAG_INT)), decode(palette.getCompound(entry.getInt("state"))));
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(captured.get(fixture.world(entry.getKey()).subtract(minimum)).equals(entry.getValue().rotate(fixture.rotation())), "Native capture differs");
            result.put("placed_nbt", root().relativize(output).toString());
            result.put("placed_sha256", hash(output));
            result.put("native_capture_roundtrip", true);
        }
        return result;
    }
    @GameTest(template = "empty", timeoutTicks = 1000)
    public static void stableFourRotations(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", "zone-M05");
        result.put("status", "RUNNING");
        result.put("environment", "Isolated Forge 1.20.1-47.4.10 GameTestServer; no player saves or production mods");
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
            require(roundtrip.getSize().equals(template.getSize()), "Template roundtrip differs");
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> before = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos horizontal = helper.absolutePos(new BlockPos(80 + index * 96, 0, 80));
                Fixture fixture = new Fixture(new BlockPos(horizontal.getX(), 192, horizontal.getZ()), rotation);
                BlockPos minimum = fixture.minimum();
                for (int chunkEast = (minimum.getX() - 2) >> 4; chunkEast <= (minimum.getX() + 29) >> 4; chunkEast++) for (int chunkSouth = (minimum.getZ() - 2) >> 4; chunkSouth <= (minimum.getZ() + 29) >> 4; chunkSouth++) level.setChunkForced(chunkEast, chunkSouth, true);
                for (BlockPos relative : expected.keySet()) { BlockPos position = fixture.world(relative); level.getChunk(position); level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS); }
                require(emptyVolume(level, fixture, expected), "Isolated volume not empty");
                BlockPos sentinel = fixture.world(new BlockPos(13, 5, 5));
                level.setBlock(sentinel, Blocks.GOLD_BLOCK.defaultBlockState(), FLAGS);
                require(!emptyVolume(level, fixture, expected), "Occupied-volume negative probe missed");
                level.setBlock(sentinel, Blocks.AIR.defaultBlockState(), FLAGS);
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "placeInWorld returned false");
                before.add(verify(level, fixture, expected, false));
                BlockPos chain = fixture.world(new BlockPos(9, 9, 7));
                BlockState chainState = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lantern = fixture.world(new BlockPos(9, 8, 7));
                require(!level.getBlockState(lantern).canSurvive(level, lantern), "Missing-chain negative probe missed");
                level.setBlock(chain, chainState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos route = fixture.world(new BlockPos(13, 2, 5));
                level.setBlock(route, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, route), "Blocked-route negative probe missed");
                level.setBlock(route, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos bottom = fixture.world(new BlockPos(17, 0, 15));
                BlockState bottomState = level.getBlockState(bottom);
                level.setBlock(bottom, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!contained(level, fixture.world(new BlockPos(17, 1, 15))), "Missing-water-floor negative probe missed");
                level.setBlock(bottom, bottomState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
                    BlockPos position = fixture.world(entry.getKey());
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                    if (entry.getValue().is(Blocks.WATER)) level.scheduleTick(position, level.getFluidState(position).getType(), 1);
                }
                fixtures.add(fixture);
                index++;
            }
            result.put("rotations_before_ticks", before);
            result.put("negative_probes", Map.of("occupied_air_voxel_refused", 4, "missing_chain_detected", 4, "blocked_route_detected", 4, "missing_water_floor_detected", 4));
            result.put("chunks_forced_to_tick", true);
            result.put("water_ticks_explicitly_scheduled", true);
            result.put("stabilization_ticks", 200);
            report(result);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> after = new ArrayList<>();
                    for (Fixture fixture : fixtures) after.add(verify(level, fixture, expected, true));
                    require(hash(source).equals(fingerprint), "Source changed during native test");
                    result.put("rotations", after);
                    result.put("records_checked_after_ticks", COUNT * after.size());
                    result.put("source_unchanged", true);
                    result.put("status", "PASS");
                    report(result);
                    System.out.println("ZONE_M05_NATIVE_PASS rotations=4/4 records=43740 ticks=200 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception error) {
                    result.put("status", "FAIL"); result.put("error", error.toString());
                    try { report(result); } catch (Exception secondary) { error.addSuppressed(secondary); }
                    throw new IllegalStateException(error);
                }
            });
        } catch (Exception error) {
            result.put("status", "FAIL"); result.put("error", error.toString()); report(result); throw error;
        }
    }
}
