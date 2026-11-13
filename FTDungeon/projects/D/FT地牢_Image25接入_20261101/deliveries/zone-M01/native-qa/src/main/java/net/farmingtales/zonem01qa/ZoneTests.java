package net.farmingtales.zonem01qa;

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

@GameTestHolder("ftzonem01qa")
@PrefixGameTestTemplate(false)
public final class ZoneTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final int COUNT = 27 * 11 * 23;
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "spawner", "tnt", "respawn_anchor");
    private static final Set<String> HAZARDS = Set.of("lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "wither_rose", "sweet_berry_bush", "cobweb", "campfire", "soul_campfire");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
        BlockPos minimum() {
            BlockPos minimum = world(BlockPos.ZERO);
            for (BlockPos corner : List.of(new BlockPos(26, 0, 0), new BlockPos(0, 0, 22), new BlockPos(26, 0, 22))) {
                BlockPos point = world(corner);
                minimum = new BlockPos(Math.min(minimum.getX(), point.getX()), origin.getY(), Math.min(minimum.getZ(), point.getZ()));
            }
            return minimum;
        }
        Vec3i size() {
            return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(23, 11, 27) : new Vec3i(27, 11, 23);
        }
    }

    private static Path root() { return Path.of(System.getProperty("ftzonem01qa.root")).toAbsolutePath().normalize(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static String hash(Path file) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))); }
    private static void report(Map<String, Object> value) throws Exception { Files.writeString(root().resolve("qa/native.json"), JSON.toJson(value) + "\n", StandardCharsets.UTF_8); }
    private static BlockPos coordinate(ListTag value) {
        require(value.size() == 3, "Invalid coordinate");
        return new BlockPos(value.getInt(0), value.getInt(1), value.getInt(2));
    }
    private static boolean inside(BlockPos relative) { return relative.getX() >= 0 && relative.getX() < 27 && relative.getY() >= 0 && relative.getY() < 11 && relative.getZ() >= 0 && relative.getZ() < 23; }
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
        require(data.getInt("DataVersion") == 3465, "Not Java 1.20.1 data version");
        require(coordinate(data.getList("size", Tag.TAG_INT)).equals(new BlockPos(27, 11, 23)), "Size differs");
        require(!data.contains("palettes") && data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "No alternate palettes/entities allowed");
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
            if (decoded.get(state).hasBlockEntity()) {
                CompoundTag entity = entry.getCompound("nbt");
                require(decoded.get(state).is(Blocks.BARREL) && entity.getString("id").equals("minecraft:barrel") && entity.getList("Items", Tag.TAG_COMPOUND).isEmpty() && !entity.contains("LootTable"), "Only empty barrels allowed");
            } else require(!entry.contains("nbt"), "Unexpected block entity tag");
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
    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, boolean capture) throws Exception {
        int lamps = 0;
        int barrels = 0;
        int rails = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " " + actual);
            require(actual.canSurvive(level, position), "Unsupported voxel " + entry.getKey());
            require(actual.getFluidState().isEmpty(), "Unexpected fluid");
            if (actual.is(Blocks.BARREL)) {
                var entity = level.getBlockEntity(position);
                require(entity != null, "Missing barrel entity");
                CompoundTag saved = entity.saveWithFullMetadata();
                require(saved.getList("Items", Tag.TAG_COMPOUND).isEmpty() && !saved.contains("LootTable"), "Stocked container");
                barrels++;
            } else require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (actual.is(Blocks.RAIL)) {
                require(entry.getKey().getZ() == 10 && entry.getKey().getY() == 2, "Misplaced rail");
                require(floor(level, position.below()), "Unsupported rail");
                rails++;
            }
            if (actual.is(Blocks.LANTERN)) {
                for (int up = 6; up <= 7; up++) require(level.getBlockState(fixture.world(new BlockPos(entry.getKey().getX(), up, entry.getKey().getZ()))).is(Blocks.CHAIN), "Broken chain");
                BlockPos anchor = fixture.world(new BlockPos(entry.getKey().getX(), 8, entry.getKey().getZ()));
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Unsupported chain anchor");
                lamps++;
            }
        }
        require(rails == 27 && lamps == 6 && barrels == 4, "Fixture counts differ");
        for (int east = 0; east < 27; east++) {
            for (int south = 0; south < 23; south++) {
                require(floor(level, fixture.world(new BlockPos(east, 0, south))), "Missing underfloor");
                BlockPos roof = fixture.world(new BlockPos(east, 10, south));
                require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Missing roof");
                require(level.getBlockState(fixture.world(new BlockPos(east, 9, south))).is(Blocks.SPRUCE_PLANKS), "Missing ceiling");
            }
            for (int south : new int[]{8, 9, 11, 12}) {
                require(walkable(level, fixture.world(new BlockPos(east, 2, south))), "Pedestrian lane not walkable");
                for (int up = 2; up <= 4; up++) require(clear(level, fixture.world(new BlockPos(east, up, south))), "Lane clearance below three");
            }
        }
        for (int east = 11; east <= 15; east++) for (int south = 13; south <= 22; south++) {
            require(walkable(level, fixture.world(new BlockPos(east, 2, south))), "South entrance stem blocked");
            for (int up = 2; up <= 4; up++) require(clear(level, fixture.world(new BlockPos(east, up, south))), "South stem clearance below three");
        }
        for (int east : new int[]{6, 20}) for (int south : new int[]{5, 16}) {
            require(level.getBlockState(fixture.world(new BlockPos(east, 2, south))).is(Blocks.STONE_BRICKS), "Timber pedestal missing");
            require(floor(level, fixture.world(new BlockPos(east, 1, south))), "Floating timber pedestal");
            for (int up = 3; up <= 8; up++) require(level.getBlockState(fixture.world(new BlockPos(east, up, south))).is(Blocks.STRIPPED_SPRUCE_LOG), "Discontinuous timber post");
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        BlockPos entrance = new BlockPos(13, 2, 22);
        reached.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (inside(next) && !reached.contains(next) && walkable(level, fixture.world(next))) {
                    reached.add(next);
                    pending.add(next);
                }
            }
        }
        for (BlockPos point : List.of(new BlockPos(13, 2, 18), new BlockPos(0, 2, 9), new BlockPos(26, 2, 9), new BlockPos(13, 2, 6))) require(reached.contains(point), "Port/safe point/work bay unreachable " + point);
        for (int east = 0; east < 27; east++) for (int south : new int[]{8, 9, 11, 12}) require(reached.contains(new BlockPos(east, 2, south)), "Lane unreachable");
        BlockPos minimum = fixture.minimum();
        Vec3i size = fixture.size();
        int haloFluids = 0;
        for (int east = -1; east <= size.getX(); east++) for (int up = -1; up <= size.getY(); up++) for (int south = -1; south <= size.getZ(); south++) {
            if (east >= 0 && east < size.getX() && up >= 0 && up < size.getY() && south >= 0 && south < size.getZ()) continue;
            if (!level.getFluidState(minimum.offset(east, up, south)).isEmpty()) haloFluids++;
        }
        require(haloFluids == 0, "Fluid outside structure");
        var entities = level.getEntities(null, new AABB(minimum.offset(-1, -1, -1), minimum.offset(size.getX() + 1, size.getY() + 1, size.getZ() + 1)));
        require(entities.isEmpty(), "Unexpected entities/drops " + entities);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("status", "PASS");
        result.put("records_checked", expected.size());
        result.put("reachable_foot_positions", reached.size());
        result.put("pedestrian_route_cells", 158);
        result.put("rail_cells", rails);
        result.put("grounded_timber_posts", 4);
        result.put("lanterns_supported", lamps);
        result.put("empty_barrels", barrels);
        result.put("roof_cells", 621);
        result.put("external_fluid_cells", haloFluids);
        result.put("world_entities", entities.size());
        result.put("origin_world", List.of(fixture.origin().getX(), fixture.origin().getY(), fixture.origin().getZ()));
        result.put("capture_size", List.of(size.getX(), size.getY(), size.getZ()));
        if (capture) {
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, size, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, output.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(output.toFile()));
            require(reloaded.getSize().equals(size), "Native capture reload size differs");
            Map<BlockPos, BlockState> captured = new LinkedHashMap<>();
            ListTag palette = saved.getList("palette", Tag.TAG_COMPOUND);
            ListTag blocks = saved.getList("blocks", Tag.TAG_COMPOUND);
            require(blocks.size() == COUNT, "Native capture voxel count differs");
            for (int index = 0; index < blocks.size(); index++) {
                CompoundTag block = blocks.getCompound(index);
                captured.put(coordinate(block.getList("pos", Tag.TAG_INT)), decode(palette.getCompound(block.getInt("state"))));
                if (block.contains("nbt")) require(block.getCompound("nbt").getList("Items", Tag.TAG_COMPOUND).isEmpty() && !block.getCompound("nbt").contains("LootTable"), "Captured stocked barrel");
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(captured.get(fixture.world(entry.getKey()).subtract(minimum))), "Native capture differs");
            result.put("capture_matches_source", true);
            result.put("world_capture", root().relativize(output).toString());
            result.put("world_capture_sha256", hash(output));
        }
        return result;
    }
    @GameTest(template = "empty", timeoutTicks = 500)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", "zone-M01");
        result.put("status", "RUNNING");
        result.put("minecraft", "1.20.1");
        result.put("forge", "47.4.10");
        result.put("java", System.getProperty("java.version"));
        result.put("scope", "Isolated Forge GameTestServer only; no player save/client/FT core/deployment");
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
            require(roundtrip.getSize().equals(template.getSize()), "Native template roundtrip differs");
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> before = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos horizontal = helper.absolutePos(new BlockPos(80 + index * 96, 0, 80));
                Fixture fixture = new Fixture(new BlockPos(horizontal.getX(), 192, horizontal.getZ()), rotation);
                BlockPos minimum = fixture.minimum();
                Vec3i size = fixture.size();
                for (int chunkX = (minimum.getX() - 2) >> 4; chunkX <= (minimum.getX() + size.getX() + 2) >> 4; chunkX++) {
                    for (int chunkZ = (minimum.getZ() - 2) >> 4; chunkZ <= (minimum.getZ() + size.getZ() + 2) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
                }
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                }
                require(emptyVolume(level, fixture, expected), "Fresh isolated volume not empty");
                BlockPos sentinel = fixture.world(new BlockPos(13, 5, 18));
                level.setBlock(sentinel, Blocks.GOLD_BLOCK.defaultBlockState(), FLAGS);
                require(!emptyVolume(level, fixture, expected) && level.getBlockState(sentinel).is(Blocks.GOLD_BLOCK), "Occupancy negative probe missed");
                level.setBlock(sentinel, Blocks.AIR.defaultBlockState(), FLAGS);
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placeInWorld false");
                before.add(verify(level, fixture, expected, false));
                BlockPos chain = fixture.world(new BlockPos(6, 6, 7));
                BlockState chainState = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lantern = fixture.world(new BlockPos(6, 5, 7));
                require(!level.getBlockState(lantern).canSurvive(level, lantern), "Unsupported lamp negative probe missed");
                level.setBlock(chain, chainState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos route = fixture.world(new BlockPos(13, 2, 18));
                level.setBlock(route, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, route), "Blocked route negative probe missed");
                level.setBlock(route, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos bottom = fixture.world(new BlockPos(13, 1, 10));
                BlockState bottomState = level.getBlockState(bottom);
                level.setBlock(bottom, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos rail = fixture.world(new BlockPos(13, 2, 10));
                require(!level.getBlockState(rail).canSurvive(level, rail), "Unsupported rail negative probe missed");
                level.setBlock(bottom, bottomState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos pedestal = fixture.world(new BlockPos(6, 2, 5));
                BlockState pedestalState = level.getBlockState(pedestal);
                level.setBlock(pedestal, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!floor(level, pedestal), "Floating timber negative probe missed");
                level.setBlock(pedestal, pedestalState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
                    BlockPos position = fixture.world(entry.getKey());
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                    if (entry.getValue().is(Blocks.LAVA)) level.scheduleTick(position, level.getFluidState(position).getType(), 1);
                }
                fixtures.add(fixture);
                index++;
            }
            result.put("rotations_before_ticks", before);
            result.put("negative_probes", Map.of("occupied_air_voxel_refused", 4, "missing_chain_detected", 4, "blocked_route_detected", 4, "missing_rail_floor_detected", 4, "missing_timber_pedestal_detected", 4));
            result.put("chunks_forced_to_tick", true);

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
                    System.out.println("ZONE_M01_NATIVE_PASS rotations=4/4 records=27324 ticks=200 sha256=" + fingerprint);
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
