package net.farmingtales.corridors13qa;

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
import net.minecraft.world.Container;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftcorridors13qa")
@PrefixGameTestTemplate(false)
public final class CorridorGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(15, 11, 16);
    private static final int RECORDS = 2640;
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light", "water", "lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "campfire", "soul_campfire", "sweet_berry_bush", "wither_rose");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private CorridorGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static Path root() {
        return Path.of(System.getProperty("ftcorridors13qa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path file) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(report) + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    private static BlockPos coordinate(JsonArray value) {
        return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt());
    }

    private static boolean stockEmpty(CompoundTag data) {
        for (String key : data.getAllKeys()) {
            Tag value = data.get(key);
            if (Set.of("LootTable", "LootTableSeed", "Book", "RecordItem").contains(key)) return false;
            if ((key.equals("Items") || key.equals("Item")) && (!(value instanceof ListTag list) || !list.isEmpty())) return false;
            if (value instanceof CompoundTag child && !stockEmpty(child)) return false;
        }
        return true;
    }

    private static <Value extends Comparable<Value>> void checkProperty(BlockState state, Property<Value> property, String value) {
        require(property.getValue(value).isPresent(), "Invalid property value: " + property.getName() + "=" + value);
        require(property.getName(state.getValue(property)).equals(value), "State property did not decode: " + property.getName());
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla block: " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden block: " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String name : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(name);
            require(property != null, "Unknown property: " + identity + ":" + name);
            checkProperty(state, property, properties.getString(name));
        }
        return state;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && data.contains("palette") && !data.contains("palettes"), "Wrong NBT version/palette");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Template contains entities");
        ListTag dimensions = data.getList("size", Tag.TAG_INT);
        require(dimensions.getInt(0) == 15 && dimensions.getInt(1) == 11 && dimensions.getInt(2) == 16, "Wrong size");
        List<BlockState> palette = new ArrayList<>();
        for (Tag value : data.getList("palette", Tag.TAG_COMPOUND)) palette.add(decode((CompoundTag) value));
        Map<BlockPos, BlockState> records = new LinkedHashMap<>();
        int banners = 0;
        for (Tag value : data.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            ListTag location = entry.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(location.getInt(0), location.getInt(1), location.getInt(2));
            require(position.getX() >= 0 && position.getX() < 15 && position.getY() >= 0 && position.getY() < 11 && position.getZ() >= 0 && position.getZ() < 16, "Record out of bounds");
            int index = entry.getInt("state");
            require(index >= 0 && index < palette.size(), "Palette index out of range");
            BlockState state = palette.get(index);
            require(records.put(position, state) == null, "Duplicate record coordinate");
            if (entry.contains("nbt")) {
                CompoundTag blockData = entry.getCompound("nbt");
                require(stockEmpty(blockData) && blockData.getString("id").equals("minecraft:banner"), "Invalid block entity payload");
                require(blockData.getList("Patterns", Tag.TAG_COMPOUND).size() == 2, "Missing banner design");
                banners++;
            }
        }
        require(records.size() == RECORDS && banners == 8, "Wrong record/banner count");
        return records;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        return level.getBlockState(position).getFluidState().isEmpty() && level.getBlockState(position).getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState floor = level.getBlockState(below);
        return clear(level, feet) && clear(level, feet.above()) && floor.getFluidState().isEmpty()
                && floor.isFaceSturdy(level, below, Direction.UP) && !floor.is(Blocks.COBWEB)
                && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(floor.getBlock()).getPath());
    }

    private static void full(ServerLevel level, Fixture fixture, BlockPos relative, String label) {
        BlockPos position = fixture.world(relative);
        require(level.getBlockState(position).isCollisionShapeFullBlock(level, position), label + " " + relative);
    }

    private static void verifySupports(ServerLevel level, Fixture fixture, JsonObject blueprint) {
        for (int posX = 0; posX < 15; posX++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                full(level, fixture, new BlockPos(posX, 0, posZ), "Floor hole");
                full(level, fixture, new BlockPos(posX, 10, posZ), "Roof hole");
            }
        }
        for (JsonElement pillar : blueprint.getAsJsonArray("pillar_z")) {
            int posZ = pillar.getAsInt();
            for (int posX : new int[] {1, 13}) {
                for (int height = 0; height <= 9; height++) full(level, fixture, new BlockPos(posX, height, posZ), "Column-beam discontinuity");
            }
            for (int posX = 1; posX <= 13; posX++) full(level, fixture, new BlockPos(posX, 9, posZ), "Beam break");
        }
        for (JsonElement item : blueprint.getAsJsonArray("cages")) {
            JsonObject cage = item.getAsJsonObject();
            require(level.getBlockState(fixture.world(coordinate(cage.getAsJsonArray("chain")))) .is(Blocks.CHAIN), "Missing cage chain");
            full(level, fixture, coordinate(cage.getAsJsonArray("anchor")), "Missing cage ceiling anchor");
            full(level, fixture, coordinate(cage.getAsJsonArray("cap")), "Missing cage bearing cap");
            BlockPos center = coordinate(cage.getAsJsonArray("cap"));
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    BlockPos base = fixture.world(new BlockPos(center.getX() + offsetX, 4, center.getZ() + offsetZ));
                    require(level.getBlockState(base).is(Blocks.POLISHED_DEEPSLATE_SLAB), "Missing cage base");
                    if (Math.abs(offsetX) == 1 || Math.abs(offsetZ) == 1) {
                        for (int height = 5; height <= 6; height++) require(level.getBlockState(fixture.world(new BlockPos(center.getX() + offsetX, height, center.getZ() + offsetZ))).is(Blocks.IRON_BARS), "Cage side break");
                    }
                }
            }
        }
        for (String category : List.of("candles", "end_rods")) {
            for (JsonElement item : blueprint.getAsJsonArray(category)) full(level, fixture, coordinate(item.getAsJsonArray()).below(), "Missing lamp/candle bracket");
        }
        for (JsonElement item : blueprint.getAsJsonArray("banners")) {
            BlockPos position = coordinate(item.getAsJsonArray());
            full(level, fixture, position.offset(position.getX() == 1 ? -1 : 1, 0, 0), "Banner backing missing");
        }
    }

    private static Map<String, Object> verifyPlacement(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected,
            JsonObject metadata, JsonObject blueprint, boolean capture) throws Exception {
        int blockEntities = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " at " + entry.getKey() + " expected=" + entry.getValue().rotate(fixture.rotation()) + " actual=" + actual);
            require(actual.canSurvive(level, position), "Native unsupported block " + entry.getKey());
            require(actual.getFluidState().isEmpty(), "Unexpected fluid");
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity instanceof BannerBlockEntity && entity.getType().isValid(actual), "Missing/wrong banner block entity");
                CompoundTag payload = entity.saveWithFullMetadata();
                require(stockEmpty(payload) && payload.getList("Patterns", Tag.TAG_COMPOUND).size() == 2, "Banner data lost or inventory present");
                require(!(entity instanceof Container), "Unexpected inventory container");
                blockEntities++;
            }
        }
        require(blockEntities == 8, "Wrong placed banner count");
        verifySupports(level, fixture, blueprint);
        for (int posX = 6; posX <= 8; posX++) {
            for (int posZ = 0; posZ <= 15; posZ++) {
                BlockPos feet = fixture.world(new BlockPos(posX, 1, posZ));
                require(walkable(level, feet), "Main route obstructed or floor unsafe");
                for (int height = 1; height <= 3; height++) require(level.getBlockState(fixture.world(new BlockPos(posX, height, posZ))).isAir(), "Three-high route not air");
            }
        }
        for (String key : List.of("entrance", "interior", "exit", "exit_interior")) require(walkable(level, fixture.world(coordinate(metadata.getAsJsonArray(key)))), "Unsafe metadata point " + key);
        Set<BlockPos> reachable = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        BlockPos start = coordinate(metadata.getAsJsonArray("entrance"));
        reachable.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() < 6 || next.getX() > 8 || next.getZ() < 0 || next.getZ() > 15) continue;
                if (walkable(level, fixture.world(next)) && reachable.add(next)) pending.add(next);
            }
        }
        require(reachable.size() == 48 && reachable.contains(coordinate(metadata.getAsJsonArray("exit"))), "Endpoint graph not connected");
        BlockPos opposite = fixture.world(new BlockPos(14, 10, 15));
        BlockPos minimum = new BlockPos(Math.min(fixture.origin().getX(), opposite.getX()), fixture.origin().getY(), Math.min(fixture.origin().getZ(), opposite.getZ()));
        BlockPos maximum = new BlockPos(Math.max(fixture.origin().getX(), opposite.getX()), fixture.origin().getY() + 10, Math.max(fixture.origin().getZ(), opposite.getZ()));
        require(level.getEntities(null, new AABB(minimum, maximum.offset(1, 1, 1))).isEmpty(), "Unexpected entity or dropped unsupported block");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("status", "PASS");
        result.put("records", expected.size());
        result.put("banner_block_entities", blockEntities);
        result.put("route_cells", reachable.size());
        result.put("supports_and_beams", "PASS");
        result.put("free_fluids", 0);
        result.put("inventory_containers", 0);
        if (capture) {
            Vec3i rotatedSize = fixture.rotation() == Rotation.CLOCKWISE_90 || fixture.rotation() == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(16, 11, 15) : SIZE;
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, rotatedSize, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            Path export = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, export.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(export.toFile()));
            require(reloaded.getSize().equals(rotatedSize) && saved.getList("blocks", Tag.TAG_COMPOUND).size() == RECORDS, "World capture roundtrip failed");
            result.put("world_capture", root().relativize(export).toString());
            result.put("world_capture_sha256", hash(export));
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 500)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("java", System.getProperty("java.version"));
        report.put("scope", "Producer isolated Forge GameTestServer; no player save, integration catalog or deployment");
        writeReport(report);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Only isolated GameTestServer may place fixtures");
            Path isolatedWorld = level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            require(isolatedWorld.startsWith(root().resolve("qa/forge-runs")), "World path escaped this delivery's isolated QA folder");
            report.put("isolated_world", isolatedWorld.toString());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("qa/blueprint.json")), JsonObject.class);
            String fingerprint = hash(root().resolve("room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "NBT metadata hash mismatch");
            require(hash(Path.of(metadata.get("reference").getAsString())).equals(metadata.get("reference_sha256").getAsString()), "Reference hash mismatch");
            report.put("structure_sha256", fingerprint);
            report.put("reference_sha256", metadata.get("reference_sha256").getAsString());
            CompoundTag data = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(data);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(SIZE), "Native template size mismatch");
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> immediate = new ArrayList<>();
            Set<ChunkPos> forcedChunks = new HashSet<>();
            int ordinal = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos anchor = helper.absolutePos(new BlockPos(40 + ordinal * 48, 0, 40));
                Fixture fixture = new Fixture(new BlockPos(anchor.getX(), 240, anchor.getZ()), rotation);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    ChunkPos chunk = new ChunkPos(position);
                    if (forcedChunks.add(chunk)) level.setChunkForced(chunk.x, chunk.z, true);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placeInWorld returned false");
                immediate.add(verifyPlacement(level, fixture, expected, metadata, blueprint, false));
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                for (BlockPos probe : List.of(new BlockPos(3, 8, 4), new BlockPos(7, 10, 7), new BlockPos(1, 9, 4))) {
                    BlockPos world = fixture.world(probe);
                    BlockState saved = level.getBlockState(world);
                    level.setBlock(world, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                    boolean rejected = false;
                    try { verifySupports(level, fixture, blueprint); } catch (IllegalStateException expectedFailure) { rejected = true; }
                    require(rejected, "Missing support/roof/beam negative probe undetected");
                    level.setBlock(world, saved, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                }
                BlockPos blocked = fixture.world(new BlockPos(7, 1, 0));
                level.setBlock(blocked, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, blocked), "Blocked endpoint negative probe undetected");
                level.setBlock(blocked, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                fixtures.add(fixture);
                ordinal++;
            }
            CompoundTag stocked = new CompoundTag();
            stocked.putString("LootTable", "minecraft:chests/simple_dungeon");
            require(!stockEmpty(stocked), "Loot negative probe undetected");
            report.put("immediate_placements", immediate);
            report.put("stabilization_ticks", 300);
            report.put("random_tick_speed", level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING));
            report.put("forced_ticking_chunks", forcedChunks.size());
            report.put("negative_probes", Map.of("missing_cage_chain", 4, "roof_hole", 4, "beam_break", 4, "blocked_north_endpoint", 4, "loot_payload", 1));
            writeReport(report);
            helper.runAfterDelay(300, () -> {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) results.add(verifyPlacement(level, fixture, expected, metadata, blueprint, true));
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source changed during verification");
                    report.put("rotations", results);
                    report.put("records_checked_after_ticks", results.size() * expected.size());
                    report.put("status", "PASS");
                    report.put("limitations", List.of("Producer QA only; receiving coordinator must independently revalidate frozen files.", "No hall links, occupancy rejection, idempotence, persistent markers or catalog integration tested.", "No full-modpack client, multiplayer, performance or screenshot claims. 300 ticks is bounded."));
                    writeReport(report);
                    for (ChunkPos chunk : forcedChunks) level.setChunkForced(chunk.x, chunk.z, false);
                    System.out.println("CORRIDOR_S13_NATIVE_PASS: rotations=4/4 records=10560 ticks=300 source=" + fingerprint);
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
