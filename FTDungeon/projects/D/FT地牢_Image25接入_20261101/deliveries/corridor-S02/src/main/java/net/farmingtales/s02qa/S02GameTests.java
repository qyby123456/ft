package net.farmingtales.s02qa;

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
import java.util.Locale;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fts02qa")
@PrefixGameTestTemplate(false)
public final class S02GameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(11, 9, 16);
    private static final int RECORDS = 1584;
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final Set<String> ALLOWED = Set.of("air", "stone_bricks", "cracked_stone_bricks", "mossy_stone_bricks", "andesite", "chiseled_stone_bricks", "stone_brick_stairs", "stone_brick_slab", "wall_torch", "iron_bars", "cobweb");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("fts02qa.root")).toAbsolutePath().normalize();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String hash(Path file) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
    }

    private static BlockPos coordinate(JsonArray value) {
        return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt());
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(report) + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require 1.20.1 single-palette NBT");
        ListTag size = data.getList("size", Tag.TAG_INT);
        require(size.size() == 3 && size.getInt(0) == 11 && size.getInt(1) == 9 && size.getInt(2) == 16, "NBT size mismatch");
        require(coordinate(metadata.getAsJsonArray("size")).equals(new BlockPos(SIZE)), "Metadata size mismatch");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Preplaced entity");
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < encodedPalette.size(); index++) {
            CompoundTag encoded = encodedPalette.getCompound(index);
            ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && ALLOWED.contains(identity.getPath()) && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown or forbidden block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = encoded.getCompound("Properties");
            require(properties.size() == block.getStateDefinition().getProperties().size(), "Incomplete state " + identity);
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid state property " + identity + " " + key);
            }
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
            require(state.getFluidState().isEmpty() && !state.hasBlockEntity(), "Unexpected fluid or block entity");
            palette.add(state);
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        ListTag entries = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            require(position.size() == 3, "Coordinate length");
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 11 && relative.getY() >= 0 && relative.getY() < 9 && relative.getZ() >= 0 && relative.getZ() < 16, "Coordinate outside bounds");
            int stateIndex = entry.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Invalid palette index");
            require(expected.put(relative, palette.get(stateIndex)) == null, "Duplicate position");
            require(!entry.contains("nbt"), "Unexpected block entity payload");
        }
        require(expected.size() == RECORDS, "Full explicit volume missing");
        return expected;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getFluidState().isEmpty() && !state.is(Blocks.COBWEB) && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState floor = level.getBlockState(below);
        return clear(level, feet) && clear(level, feet.above()) && clear(level, feet.above(2))
                && floor.isCollisionShapeFullBlock(level, below) && floor.isFaceSturdy(level, below, Direction.UP) && floor.getFluidState().isEmpty();
    }

    private static void envelope(ServerLevel level, Fixture fixture) {
        for (int posX = 0; posX < 11; posX++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                for (int posY : new int[] {0, 8}) {
                    BlockPos position = fixture.world(new BlockPos(posX, posY, posZ));
                    require(level.getBlockState(position).isCollisionShapeFullBlock(level, position), "Floor/roof hole " + position);
                }
            }
        }
        for (int posZ = 0; posZ < 16; posZ++) {
            for (int posY = 1; posY < 8; posY++) {
                for (int posX : new int[] {0, 10}) {
                    BlockPos position = fixture.world(new BlockPos(posX, posY, posZ));
                    require(level.getBlockState(position).isCollisionShapeFullBlock(level, position), "Side-wall hole");
                }
            }
        }
    }

    private static int verifyLane(ServerLevel level, Fixture fixture) {
        for (int posZ = 0; posZ < 16; posZ++) {
            for (int posX = 4; posX <= 6; posX++) {
                require(walkable(level, fixture.world(new BlockPos(posX, 1, posZ))), "Main lane is unsafe at " + posX + ",1," + posZ);
            }
        }
        BlockPos start = new BlockPos(5, 1, 0);
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        reached.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() < 0 || next.getX() > 10 || next.getZ() < 0 || next.getZ() > 15 || reached.contains(next)) continue;
                if (walkable(level, fixture.world(next))) {
                    reached.add(next);
                    pending.addLast(next);
                }
            }
        }
        require(reached.contains(new BlockPos(5, 1, 3)) && reached.contains(new BlockPos(5, 1, 15)), "Safe point or south exit unreachable");
        return reached.size();
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, boolean capture) throws Exception {
        int torches = 0;
        int webs = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " expected=" + entry.getValue().rotate(fixture.rotation()) + " actual=" + actual);
            require(actual.canSurvive(level, position), "Unsupported block " + position);
            require(actual.getFluidState().isEmpty() && level.getBlockEntity(position) == null, "Unexpected fluid or block entity");
            if (actual.is(Blocks.WALL_TORCH)) torches++;
            if (actual.is(Blocks.COBWEB)) {
                webs++;
                require(entry.getKey().getY() == 5 && (entry.getKey().getX() == 1 || entry.getKey().getX() == 9), "Web intersects player route");
            }
        }
        require(torches == 6 && webs == 6, "Lost torches or corner webs");
        envelope(level, fixture);
        int reached = verifyLane(level, fixture);
        BlockPos opposite = fixture.world(new BlockPos(10, 8, 15));
        BlockPos minimum = new BlockPos(Math.min(fixture.origin().getX(), opposite.getX()), fixture.origin().getY(), Math.min(fixture.origin().getZ(), opposite.getZ()));
        Vec3i rotatedSize = new Vec3i(Math.abs(opposite.getX() - fixture.origin().getX()) + 1, 9, Math.abs(opposite.getZ() - fixture.origin().getZ()) + 1);
        AABB bounds = new AABB(minimum, minimum.offset(rotatedSize)).inflate(0.1);
        int entities = level.getEntities(null, bounds).size();
        require(entities == 0, "Unexpected spawned or dropped entity");
        int fluidLeaks = 0;
        for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), minimum.offset(rotatedSize))) {
            if (!level.getFluidState(position).isEmpty()) fluidLeaks++;
        }
        require(fluidLeaks == 0, "Unexpected fluid in template or one-block perimeter");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("status", "PASS");
        result.put("records_checked", expected.size());
        result.put("native_can_survive_checked", expected.size());
        result.put("roof_cells", 176);
        result.put("floor_cells", 176);
        result.put("wall_torches", torches);
        result.put("high_corner_webs", webs);
        result.put("reachable_foot_positions", reached);
        result.put("main_lane", "3 wide x 3 high x 16 long, both ends open; no steps, turns or branches");
        result.put("unexpected_entities", entities);
        result.put("fluid_cells_including_perimeter", fluidLeaks);
        if (capture) {
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, rotatedSize, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            require(saved.getList("blocks", Tag.TAG_COMPOUND).size() == RECORDS && saved.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "World capture record mismatch");
            Path export = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, export.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(export.toFile()));
            require(reloaded.getSize().equals(rotatedSize), "World capture roundtrip dimensions differ");
            result.put("world_capture", root().relativize(export).toString());
            result.put("world_capture_sha256", hash(export));
            result.put("world_capture_size", List.of(rotatedSize.getX(), rotatedSize.getY(), rotatedSize.getZ()));
        }
        return result;
    }

    private static void negativeProbes(ServerLevel level, Fixture fixture) {
        BlockPos entrance = fixture.world(new BlockPos(5, 1, 0));
        level.setBlock(entrance, Blocks.STONE.defaultBlockState(), FLAGS);
        boolean blockedRejected = false;
        try { verifyLane(level, fixture); } catch (IllegalStateException expected) { blockedRejected = true; }
        require(blockedRejected, "Blocked entrance probe undetected");
        level.setBlock(entrance, Blocks.AIR.defaultBlockState(), FLAGS);
        BlockPos roof = fixture.world(new BlockPos(5, 8, 8));
        BlockState savedRoof = level.getBlockState(roof);
        level.setBlock(roof, Blocks.AIR.defaultBlockState(), FLAGS);
        boolean roofRejected = false;
        try { envelope(level, fixture); } catch (IllegalStateException expected) { roofRejected = true; }
        require(roofRejected, "Roof hole probe undetected");
        level.setBlock(roof, savedRoof, FLAGS);
        BlockPos support = fixture.world(new BlockPos(0, 3, 2));
        BlockState savedSupport = level.getBlockState(support);
        BlockPos torch = fixture.world(new BlockPos(1, 3, 2));
        level.setBlock(support, Blocks.AIR.defaultBlockState(), FLAGS);
        require(!level.getBlockState(torch).canSurvive(level, torch), "Missing wall-torch support probe undetected");
        level.setBlock(support, savedSupport, FLAGS);
    }

    @GameTest(template = "empty", timeoutTicks = 380)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("java", System.getProperty("java.version"));
        report.put("scope", "Isolated Forge GameTestServer only; no production world, FT core, deployment or integration");
        writeReport(report);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "This fixture requires an isolated GameTestServer");
            Path isolatedWorld = level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            require(isolatedWorld.startsWith(root().resolve("qa/forge-runs")), "Refusing a world outside S02 QA");
            report.put("isolated_world", isolatedWorld.toString());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            report.put("random_tick_speed", level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING));
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            String fingerprint = hash(root().resolve("room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "NBT hash mismatch");
            require(hash(Path.of(metadata.get("reference").getAsString())).equals(metadata.get("reference_sha256").getAsString()), "Reference image hash mismatch");
            report.put("structure_sha256", fingerprint);
            report.put("reference_sha256", metadata.get("reference_sha256").getAsString());
            CompoundTag data = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(data, metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(SIZE), "Native template dimensions differ");
            CompoundTag roundtrip = template.save(new CompoundTag());
            require(audit(roundtrip, metadata).equals(expected), "Native NBT roundtrip differs");
            NbtIo.writeCompressed(roundtrip, root().resolve("qa/native-roundtrip.nbt").toFile());
            report.put("native_roundtrip_sha256", hash(root().resolve("qa/native-roundtrip.nbt")));
            report.put("native_decode_records", expected.size());
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> immediate = new ArrayList<>();
            Set<ChunkPos> forcedChunks = new HashSet<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(helper.absolutePos(new BlockPos(32 + index * 48, 2, 64)), rotation);
                for (BlockPos relative : BlockPos.betweenClosed(-2, -2, -2, 12, 11, 17)) {
                    BlockPos position = fixture.world(relative);
                    ChunkPos chunk = new ChunkPos(position);
                    if (forcedChunks.add(chunk)) level.setChunkForced(chunk.x, chunk.z, true);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, Block.UPDATE_CLIENTS), "placeInWorld returned false");
                immediate.add(verify(level, fixture, expected, false));
                negativeProbes(level, fixture);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                fixtures.add(fixture);
                index++;
            }
            report.put("immediate_placements", immediate);
            report.put("forced_ticking_chunks", forcedChunks.size());
            report.put("negative_probes", Map.of("blocked_entrance", 4, "roof_hole", 4, "missing_wall_torch_support", 4));
            report.put("stabilization_ticks", 200);
            writeReport(report);
            helper.runAfterDelay(100, () -> {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) results.add(verify(level, fixture, expected, false));
                    report.put("after_100_ticks", results);
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
                    require(!"FAIL".equals(report.get("status")), "Earlier stabilization check failed");
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) results.add(verify(level, fixture, expected, true));
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "NBT changed during QA");
                    report.put("after_200_ticks", results);
                    report.put("status", "PASS");
                    writeReport(report);
                    for (ChunkPos chunk : forcedChunks) level.setChunkForced(chunk.x, chunk.z, false);
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
