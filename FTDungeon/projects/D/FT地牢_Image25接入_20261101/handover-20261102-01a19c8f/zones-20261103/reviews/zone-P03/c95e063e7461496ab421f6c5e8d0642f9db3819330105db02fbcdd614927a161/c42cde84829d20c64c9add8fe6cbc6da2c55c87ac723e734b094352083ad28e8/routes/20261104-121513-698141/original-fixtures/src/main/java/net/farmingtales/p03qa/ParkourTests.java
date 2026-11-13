package net.farmingtales.p03qa;

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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftp03qa")
@PrefixGameTestTemplate(false)
public final class ParkourTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final int COUNT = 27 * 13 * 29;
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "spawner", "tnt", "respawn_anchor");
    private static final Set<String> HAZARDS = Set.of("lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "wither_rose", "sweet_berry_bush", "cobweb", "campfire", "soul_campfire");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) { return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO)); }
        Vec3 world(Vec3 relative) { return StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO).add(origin.getX(), origin.getY(), origin.getZ()); }
        BlockPos minimum() {
            BlockPos minimum = world(BlockPos.ZERO);
            for (BlockPos corner : List.of(new BlockPos(26, 0, 0), new BlockPos(0, 0, 28), new BlockPos(26, 0, 28))) {
                BlockPos point = world(corner);
                minimum = new BlockPos(Math.min(minimum.getX(), point.getX()), origin.getY(), Math.min(minimum.getZ(), point.getZ()));
            }
            return minimum;
        }
        Vec3i size() { return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(29, 13, 27) : new Vec3i(27, 13, 29); }
    }

    private static Path root() { return Path.of(System.getProperty("ftp03qa.root")).toAbsolutePath().normalize(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static String hash(Path file) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))); }
    private static void report(Map<String, Object> value) throws Exception { Files.writeString(root().resolve("qa/native.json"), JSON.toJson(value) + "\n", StandardCharsets.UTF_8); }
    private static BlockPos coordinate(ListTag value) { require(value.size() == 3, "Invalid coordinate"); return new BlockPos(value.getInt(0), value.getInt(1), value.getInt(2)); }
    private static Vec3 vector(JsonArray value) { return new Vec3(value.get(0).getAsDouble(), value.get(1).getAsDouble(), value.get(2).getAsDouble()); }
    private static boolean inside(BlockPos position) { return position.getX() >= 0 && position.getX() < 27 && position.getY() >= 0 && position.getY() < 13 && position.getZ() >= 0 && position.getZ() < 29; }
    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla block " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden block " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid state " + identity + " " + key);
        }
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
    }
    private static Map<BlockPos, BlockState> audit(CompoundTag encoded) {
        require(encoded.getInt("DataVersion") == 3465, "Not Minecraft 1.20.1");
        require(coordinate(encoded.getList("size", Tag.TAG_INT)).equals(new BlockPos(27, 13, 29)), "Wrong dimensions");
        require(encoded.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected entities");
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : encoded.getList("palette", Tag.TAG_COMPOUND)) palette.add(decode((CompoundTag) entry));
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        for (Tag raw : encoded.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            BlockPos position = coordinate(entry.getList("pos", Tag.TAG_INT));
            int state = entry.getInt("state");
            require(inside(position) && state >= 0 && state < palette.size(), "Invalid voxel");
            require(!entry.contains("nbt") && !palette.get(state).hasBlockEntity(), "Unexpected block entity");
            require(result.put(position, palette.get(state)) == null, "Duplicate voxel");
        }
        require(result.size() == COUNT, "Missing explicit volume");
        return result;
    }
    private static boolean safe(BlockState state) { return state.getFluidState().isEmpty() && !HAZARDS.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()); }
    private static boolean floor(ServerLevel level, BlockPos position) { BlockState state = level.getBlockState(position); return safe(state) && state.isFaceSturdy(level, position, Direction.UP); }
    private static boolean clear(ServerLevel level, BlockPos position) { BlockState state = level.getBlockState(position); return safe(state) && state.getCollisionShape(level, position).isEmpty(); }
    private static boolean walkable(ServerLevel level, BlockPos position) { return clear(level, position) && clear(level, position.above()) && floor(level, position.below()); }
    private static boolean empty(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected) {
        for (BlockPos position : expected.keySet()) if (!level.getBlockState(fixture.world(position)).isAir()) return false;
        return true;
    }
    private static boolean sealed(ServerLevel level, BlockPos position) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = position.relative(direction);
            BlockState state = level.getBlockState(neighbor);
            if (!state.is(Blocks.LAVA) && !state.isFaceSturdy(level, neighbor, direction.getOpposite())) return false;
        }
        return true;
    }
    private static boolean bodyClear(ServerLevel level, Fixture fixture, Vec3 feet) {
        Vec3 point = fixture.world(feet);
        AABB body = new AABB(point.x - 0.3 + 1e-6, point.y + 1e-6, point.z - 0.3 + 1e-6, point.x + 0.3 - 1e-6, point.y + 1.8 - 1e-6, point.z + 0.3 - 1e-6);
        if (!level.noCollision(null, body)) return false;
        for (BlockPos position : BlockPos.betweenClosed(BlockPos.containing(body.minX, body.minY, body.minZ), BlockPos.containing(body.maxX, body.maxY, body.maxZ))) if (!safe(level.getBlockState(position))) return false;
        return true;
    }
    private static int sweep(ServerLevel level, Fixture fixture, Vec3 start, Vec3 end) {
        int count = Math.max(1, (int) Math.ceil(start.distanceTo(end) / 0.04));
        for (int sample = 0; sample <= count; sample++) require(bodyClear(level, fixture, start.lerp(end, (double) sample / count)), "Player body collision " + fixture.rotation() + " " + start + " -> " + end);
        return count + 1;
    }
    private static int walk(ServerLevel level, Fixture fixture, Vec3 start, Vec3 end) {
        int samples = sweep(level, fixture, start, end);
        for (int sample = 0; sample <= samples; sample++) {
            Vec3 point = start.lerp(end, (double) sample / samples);
            require(floor(level, fixture.world(BlockPos.containing(point.x, point.y - 1e-6, point.z))), "Unsupported walk segment " + point);
        }
        return samples;
    }
    private static Map<String, Object> route(ServerLevel level, Fixture fixture, JsonArray jumps) {
        int samples = walk(level, fixture, new Vec3(13.5, 5, 0.5), new Vec3(13.5, 5, 3.5));
        Vec3 previousLanding = new Vec3(13.5, 5, 3.5);
        for (JsonElement raw : jumps) {
            JsonObject jump = raw.getAsJsonObject();
            Vec3 takeoff = vector(jump.getAsJsonArray("takeoff"));
            Vec3 landing = vector(jump.getAsJsonArray("landing"));
            samples += walk(level, fixture, previousLanding, takeoff);
            Vec3 previous = takeoff;
            for (JsonElement rawSample : jump.getAsJsonArray("samples")) {
                Vec3 current = vector(rawSample.getAsJsonObject().getAsJsonArray("feet"));
                samples += sweep(level, fixture, previous, current);
                previous = current;
            }
            require(walkable(level, fixture.world(BlockPos.containing(landing))), "Unsafe landing");
            previousLanding = landing;
        }
        samples += walk(level, fixture, previousLanding, new Vec3(13.5, 5, 25.5));
        samples += walk(level, fixture, new Vec3(13.5, 5, 25.5), new Vec3(13.5, 5, 28.5));
        Set<BlockPos> catchCells = new HashSet<>();
        for (int east = 0; east < 27; east++) for (int south = 0; south < 29; south++) {
            BlockPos position = new BlockPos(east, 3, south);
            if (!level.getBlockState(fixture.world(position.below())).is(Blocks.GLASS)) continue;
            if (!walkable(level, fixture.world(position))) continue;
            catchCells.add(position);
            samples += sweep(level, fixture, new Vec3(east + 0.5, 5, south + 0.5), new Vec3(east + 0.5, 3, south + 0.5));
            require(level.getBlockState(fixture.world(position.below())).is(Blocks.GLASS), "Catch floor is not glass");
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(new BlockPos(6, 3, 7));
        reached.add(pending.peek());
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (catchCells.contains(next) && reached.add(next)) pending.add(next);
            }
        }
        require(catchCells.size() == 336 && reached.equals(catchCells), "Recovery floor disconnected");
        Vec3 previous = new Vec3(6.5, 3, 7.5);
        for (Vec3 next : List.of(new Vec3(6.5, 3, 7.31), new Vec3(6.5, 3.5, 7.29), new Vec3(6.5, 3.5, 6.81), new Vec3(6.5, 4, 6.79), new Vec3(6.5, 4, 6.31), new Vec3(6.5, 4.5, 6.29), new Vec3(6.5, 4.5, 5.81), new Vec3(6.5, 5, 5.79), new Vec3(6.5, 5, 4.5))) {
            require(next.y - previous.y <= 0.6, "Recovery exceeds normal step height");
            Vec3 raised = new Vec3(previous.x, next.y, previous.z);
            samples += sweep(level, fixture, previous, raised);
            samples += sweep(level, fixture, raised, next);
            previous = next;
        }
        samples += walk(level, fixture, previous, new Vec3(13.5, 5, 3.5));
        return Map.of("status", "PASS", "jumps", 5, "collision_subsamples", samples, "catch_cells", catchCells.size(), "catch_cells_reaching_entry_stairs", reached.size(), "maximum_fall_blocks", 2, "recovery_stair_step_height", 0.5, "method", "native collision shapes swept with 0.6x1.8 player AABB; vanilla discrete jump samples; no human playthrough");
    }
    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonArray jumps, boolean capture) throws Exception {
        int lava = 0;
        int lanterns = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " " + actual);
            require(actual.canSurvive(level, position), "Unsupported voxel " + entry.getKey());
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (actual.is(Blocks.LAVA)) { require(actual.getFluidState().isSource() && sealed(level, position), "Unsealed/flowing lava"); lava++; }
            if (actual.is(Blocks.LANTERN)) {
                for (int up : new int[]{9, 10}) require(level.getBlockState(fixture.world(new BlockPos(entry.getKey().getX(), up, entry.getKey().getZ()))).is(Blocks.CHAIN), "Missing chain");
                BlockPos anchor = fixture.world(new BlockPos(entry.getKey().getX(), 11, entry.getKey().getZ()));
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Missing chain anchor");
                lanterns++;
            }
        }
        require(lava == 289 && lanterns == 8, "Theme counts differ");
        for (int east = 0; east < 27; east++) for (int south = 0; south < 29; south++) {
            require(floor(level, fixture.world(new BlockPos(east, 0, south))), "Missing base");
            BlockPos roof = fixture.world(new BlockPos(east, 12, south));
            require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Missing roof");
        }
        for (int south : new int[]{0, 1, 27, 28}) for (int east = 12; east <= 14; east++) {
            require(walkable(level, fixture.world(new BlockPos(east, 5, south))), "Blocked port");
            for (int up = 5; up <= 8; up++) require(clear(level, fixture.world(new BlockPos(east, up, south))), "Port clearance");
        }
        BlockPos minimum = fixture.minimum();
        Vec3i size = fixture.size();
        int escaped = 0;
        for (int east = -1; east <= size.getX(); east++) for (int up = -1; up <= size.getY(); up++) for (int south = -1; south <= size.getZ(); south++) {
            if (east >= 0 && east < size.getX() && up >= 0 && up < size.getY() && south >= 0 && south < size.getZ()) continue;
            if (!level.getFluidState(minimum.offset(east, up, south)).isEmpty()) escaped++;
        }
        require(escaped == 0, "Fluid escaped");
        require(level.getEntities(null, new AABB(minimum.offset(-1, -1, -1), minimum.offset(size.getX() + 1, size.getY() + 1, size.getZ() + 1))).isEmpty(), "Unexpected entities/drops");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("status", "PASS");
        result.put("records_checked", expected.size());
        result.put("lava_sources_sealed_all_six_faces", lava);
        result.put("supported_lanterns", lanterns);
        result.put("external_fluid_cells", escaped);
        result.put("entities", 0);
        result.put("route", route(level, fixture, jumps));
        result.put("origin", List.of(fixture.origin().getX(), fixture.origin().getY(), fixture.origin().getZ()));
        if (capture) {
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, size, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, output.toFile());
            ListTag palette = saved.getList("palette", Tag.TAG_COMPOUND);
            Map<BlockPos, BlockState> captured = new LinkedHashMap<>();
            for (Tag raw : saved.getList("blocks", Tag.TAG_COMPOUND)) {
                CompoundTag entry = (CompoundTag) raw;
                captured.put(coordinate(entry.getList("pos", Tag.TAG_INT)), decode(palette.getCompound(entry.getInt("state"))));
            }
            require(captured.size() == COUNT, "Capture count differs");
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(captured.get(fixture.world(entry.getKey()).subtract(minimum))), "Captured world differs");
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(output.toFile()));
            require(reloaded.getSize().equals(size), "Capture native reload differs");
            result.put("capture_matches_source", true);
            result.put("world_capture", root().relativize(output).toString());
            result.put("world_capture_sha256", hash(output));
        }
        return result;
    }
    @GameTest(template = "empty", timeoutTicks = 500)
    public static void fourRotationsRoutesAndStability(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", "P03");
        result.put("status", "RUNNING");
        result.put("minecraft", "1.20.1");
        result.put("forge", "47.4.10");
        result.put("scope", "Isolated Forge GameTestServer only; native collision QA, no full modpack client/playthrough/deployment");
        report(result);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Refusing non-isolated server");
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
            level.setDayTime(6000);
            Path source = root().resolve("room.nbt");
            String fingerprint = hash(source);
            require(JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class).get("sha256").getAsString().equals(fingerprint), "Metadata hash differs");
            JsonObject routeData = JSON.fromJson(Files.readString(root().resolve("qa/route.json")), JsonObject.class);
            require(routeData.get("source_sha256").getAsString().equals(fingerprint), "Route source hash differs");
            JsonArray jumps = routeData.getAsJsonArray("jumps");
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
                for (BlockPos relative : expected.keySet()) level.setBlock(fixture.world(relative), Blocks.AIR.defaultBlockState(), FLAGS);
                require(empty(level, fixture, expected), "Isolated fixture not empty");
                BlockPos sentinel = fixture.world(new BlockPos(13, 6, 4));
                level.setBlock(sentinel, Blocks.GOLD_BLOCK.defaultBlockState(), FLAGS);
                require(!empty(level, fixture, expected) && level.getBlockState(sentinel).is(Blocks.GOLD_BLOCK), "Occupancy probe missed");
                level.setBlock(sentinel, Blocks.AIR.defaultBlockState(), FLAGS);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false), level.random, FLAGS), "placeInWorld false");
                before.add(verify(level, fixture, expected, jumps, false));
                for (BlockPos relative : List.of(new BlockPos(6, 0, 10), new BlockPos(6, 2, 10))) {
                    BlockPos position = fixture.world(relative);
                    BlockState previous = level.getBlockState(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                    require(!sealed(level, fixture.world(new BlockPos(6, 1, 10))), "Lava boundary probe missed");
                    level.setBlock(position, previous, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                }
                BlockPos chain = fixture.world(new BlockPos(6, 9, 4));
                BlockState chainState = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lantern = fixture.world(new BlockPos(6, 8, 4));
                require(!level.getBlockState(lantern).canSurvive(level, lantern), "Chain probe missed");
                level.setBlock(chain, chainState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos obstacle = fixture.world(new BlockPos(11, 6, 6));
                level.setBlock(obstacle, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!bodyClear(level, fixture, vector(jumps.get(0).getAsJsonObject().getAsJsonArray("samples").get(5).getAsJsonObject().getAsJsonArray("feet"))), "Jump collision probe missed");
                level.setBlock(obstacle, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
                    BlockPos position = fixture.world(entry.getKey());
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                    if (entry.getValue().is(Blocks.LAVA)) level.scheduleTick(position, level.getFluidState(position).getType(), 1);
                }
                fixtures.add(fixture);
                index++;
            }
            result.put("structure_sha256", fingerprint);
            result.put("rotations_before_ticks", before);
            result.put("negative_probes", Map.of("occupied_explicit_air_refused", 4, "missing_lava_bottom_detected", 4, "missing_glass_lid_detected", 4, "missing_chain_detected", 4, "jump_obstacle_detected", 4));
            result.put("chunks_forced_to_tick", true);
            result.put("lava_ticks_explicitly_scheduled", true);
            result.put("stabilization_ticks", 200);
            report(result);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> after = new ArrayList<>();
                    for (Fixture fixture : fixtures) after.add(verify(level, fixture, expected, jumps, true));
                    require(hash(source).equals(fingerprint), "Source changed during QA");
                    result.put("rotations", after);
                    result.put("records_checked_after_ticks", COUNT * after.size());
                    result.put("source_unchanged", true);
                    result.put("status", "PASS");
                    report(result);
                    System.out.println("P03_NATIVE_PASS rotations=4/4 jumps=5 catch=336 records=40716 ticks=200 sha256=" + fingerprint);
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
