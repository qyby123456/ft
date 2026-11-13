package net.farmingtales.r5qa;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftr5qa")
@PrefixGameTestTemplate(false)
public final class StonemasonGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(17, 11, 17);
    private static final int RECORDS = 3179;
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "campfire", "soul_campfire", "wither_rose", "sweet_berry_bush", "cobweb", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private StonemasonGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("ftr5qa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path file) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
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
            if ((key.equals("Items") || key.equals("Item")) && (!(value instanceof ListTag list) || !list.isEmpty())) {
                return false;
            }
            if (value instanceof CompoundTag child && !stockEmpty(child)) {
                return false;
            }
        }
        return true;
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla block: " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden block: " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        require(properties.size() == block.getStateDefinition().getProperties().size(), "Incomplete blockstate: " + identity);
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property: " + identity + " " + key);
        }
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(state.getFluidState().isEmpty(), "Free fluid/waterlogging forbidden in this dry workshop: " + identity);
        return state;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require 1.20.1 single palette");
        ListTag size = data.getList("size", Tag.TAG_INT);
        require(size.size() == 3 && size.getInt(0) == 17 && size.getInt(1) == 11 && size.getInt(2) == 17, "Incorrect NBT size");
        require(coordinate(metadata.getAsJsonArray("size")).equals(new BlockPos(SIZE)), "Metadata size differs");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Preplaced entity");
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < encodedPalette.size(); index++) {
            palette.add(decode(encodedPalette.getCompound(index)));
        }
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        ListTag entries = data.getList("blocks", Tag.TAG_COMPOUND);
        int containers = 0;
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            require(position.size() == 3, "Coordinate length");
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 17 && relative.getY() >= 0 && relative.getY() < 11 && relative.getZ() >= 0 && relative.getZ() < 17, "Out of bounds");
            int stateIndex = entry.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Palette index");
            BlockState state = palette.get(stateIndex);
            require(result.put(relative, state) == null, "Duplicate coordinate");
            require(state.hasBlockEntity() == entry.contains("nbt", Tag.TAG_COMPOUND), "Block entity payload mismatch");
            if (state.hasBlockEntity()) {
                CompoundTag blockData = entry.getCompound("nbt");
                require(stockEmpty(blockData), "Nonempty inventory or loot");
                BlockEntity entity = BlockEntity.loadStatic(relative, state, blockData.copy());
                require(entity != null && entity.getType().isValid(state), "Invalid native block entity");
                require(entity instanceof Container container && container.isEmpty(), "Unexpected block entity / stock");
                containers++;
            }
        }
        require(result.size() == RECORDS && containers == 2, "Full volume or container count differs");
        return result;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getFluidState().isEmpty() && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath())
                && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState floor = level.getBlockState(below);
        return clear(level, feet) && clear(level, feet.above()) && floor.getFluidState().isEmpty()
                && floor.isFaceSturdy(level, below, Direction.UP) && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(floor.getBlock()).getPath());
    }

    private static void full(ServerLevel level, Fixture fixture, BlockPos relative, String message) {
        BlockPos position = fixture.world(relative);
        require(level.getBlockState(position).isCollisionShapeFullBlock(level, position), message + " " + relative);
    }

    private static void verifySupports(ServerLevel level, Fixture fixture, JsonObject blueprint) {
        for (JsonElement value : blueprint.getAsJsonArray("table_legs")) {
            JsonArray pair = value.getAsJsonArray();
            for (int height = 0; height <= 2; height++) {
                full(level, fixture, new BlockPos(pair.get(0).getAsInt(), height, pair.get(1).getAsInt()), "Table leg unsupported");
            }
        }
        for (int posX = 11; posX <= 14; posX++) {
            for (int posZ = 5; posZ <= 7; posZ++) {
                full(level, fixture, new BlockPos(posX, 2, posZ), "Tabletop missing");
            }
        }
        for (JsonElement value : blueprint.getAsJsonArray("scaffold_posts")) {
            JsonArray pair = value.getAsJsonArray();
            for (int height = 0; height <= 5; height++) {
                full(level, fixture, new BlockPos(pair.get(0).getAsInt(), height, pair.get(1).getAsInt()), "Scaffold post unsupported");
            }
        }
        for (String key : List.of("statue_plinth", "gargoyle_plinth", "shelf_support", "table_supports")) {
            for (JsonElement value : blueprint.getAsJsonArray(key)) {
                full(level, fixture, coordinate(value.getAsJsonArray()), "Missing masonry/shelf support");
            }
        }
        for (JsonElement value : blueprint.getAsJsonArray("ladders")) {
            JsonObject ladder = value.getAsJsonObject();
            full(level, fixture, coordinate(ladder.getAsJsonArray("backing")), "Ladder backing missing");
            require(level.getBlockState(fixture.world(coordinate(ladder.getAsJsonArray("position")))).is(Blocks.LADDER), "Ladder missing");
        }
        for (JsonElement value : blueprint.getAsJsonArray("lamps")) {
            JsonObject lamp = value.getAsJsonObject();
            full(level, fixture, coordinate(lamp.getAsJsonArray("anchor")), "Lamp ceiling anchor missing");
            for (JsonElement chain : lamp.getAsJsonArray("chains")) {
                require(level.getBlockState(fixture.world(coordinate(chain.getAsJsonArray()))).is(Blocks.CHAIN), "Lamp chain missing");
            }
            require(level.getBlockState(fixture.world(coordinate(lamp.getAsJsonArray("lantern")))).is(Blocks.LANTERN), "Lantern missing");
        }
        for (int posX = 0; posX < 17; posX++) {
            for (int posZ = 0; posZ < 17; posZ++) {
                full(level, fixture, new BlockPos(posX, 0, posZ), "Floor hole");
                full(level, fixture, new BlockPos(posX, 10, posZ), "Roof hole");
            }
        }
    }

    private static Map<String, Object> verifyPlacement(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected,
            JsonObject metadata, JsonObject blueprint, boolean capture) throws Exception {
        int blockEntities = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " at " + entry.getKey() + ": expected " + entry.getValue().rotate(fixture.rotation()) + ", actual " + actual);
            require(actual.canSurvive(level, position), "Unsupported native block " + entry.getKey());
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity != null && entity.getType().isValid(actual), "Missing/wrong placed block entity");
                require(stockEmpty(entity.saveWithFullMetadata()), "Placed inventory or loot");
                require(entity instanceof Container container && container.isEmpty(), "Placed container stock");
                blockEntities++;
            }
        }
        verifySupports(level, fixture, blueprint);
        for (JsonElement value : blueprint.getAsJsonArray("main_aisle")) {
            require(clear(level, fixture.world(coordinate(value.getAsJsonArray()))), "3x3 main aisle obstructed");
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos entrance = coordinate(metadata.getAsJsonArray("entrance"));
        require(entrance.equals(new BlockPos(8, 1, 0)) && walkable(level, fixture.world(entrance)), "Unsafe north entrance");
        reached.add(entrance);
        queue.add(entrance);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() >= 0 && next.getX() < 17 && next.getZ() >= 0 && next.getZ() < 17
                        && !reached.contains(next) && walkable(level, fixture.world(next))) {
                    reached.add(next);
                    queue.addLast(next);
                }
            }
        }
        require(reached.contains(coordinate(metadata.getAsJsonArray("interior"))), "Interior point unreachable");
        for (JsonElement value : blueprint.getAsJsonArray("required_walkable")) {
            require(reached.contains(coordinate(value.getAsJsonArray())), "Workstation approach unreachable " + value);
        }
        for (int offset = -1; offset <= 1; offset++) {
            BlockPos feet = entrance.offset(offset, 0, 0);
            require(walkable(level, fixture.world(feet)) && clear(level, fixture.world(feet.above(2))), "North 3x3 opening failed");
        }
        int entities = level.getEntities(null, new AABB(fixture.origin().offset(-17, -1, -17), fixture.origin().offset(18, 12, 18))).size();
        require(entities == 0, "Unexpected dropped item or entity: " + level.getEntities(null, new AABB(fixture.origin().offset(-17, -1, -17), fixture.origin().offset(18, 12, 18))).stream().map(entity -> entity.getType() + " " + entity.position()).toList());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("status", "PASS");
        result.put("records_checked", expected.size());
        result.put("block_entities_checked", blockEntities);
        result.put("reachable_foot_positions", reached.size());
        result.put("unexpected_entities", entities);
        result.put("roof_cells", 289);
        result.put("workshop_supports", "PASS: table, scaffold, stone plinths, shelf, ladders, lanterns");
        result.put("main_aisle", "PASS: 3 wide by 3 high, 16 blocks long");
        if (capture) {
            BlockPos opposite = fixture.world(new BlockPos(16, 10, 16));
            BlockPos minimum = new BlockPos(Math.min(fixture.origin().getX(), opposite.getX()), fixture.origin().getY(), Math.min(fixture.origin().getZ(), opposite.getZ()));
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, SIZE, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            Path export = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, export.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(export.toFile()));
            require(reloaded.getSize().equals(SIZE) && saved.getList("blocks", Tag.TAG_COMPOUND).size() == RECORDS, "World capture roundtrip failed");
            result.put("world_capture", root().relativize(export).toString());
            result.put("world_capture_sha256", hash(export));
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 280)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("java", System.getProperty("java.version"));
        report.put("scope", "Isolated Forge GameTestServer only; no player save or FT core");
        writeReport(report);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Fixture writes require isolated GameTestServer");
            Path isolatedWorld = level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            require(isolatedWorld.startsWith(root().resolve("qa/forge-runs")), "World path is not inside this room's isolated QA folder");
            report.put("isolated_world", isolatedWorld.toString());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            report.put("random_tick_speed", level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING));
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("qa/blueprint.json")), JsonObject.class);
            String fingerprint = hash(root().resolve("room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "NBT metadata hash mismatch");
            require(hash(Path.of(metadata.get("reference").getAsString())).equals(metadata.get("reference_sha256").getAsString()), "Original image fingerprint changed");
            report.put("structure_sha256", fingerprint);
            report.put("reference_sha256", metadata.get("reference_sha256").getAsString());
            CompoundTag data = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(data, metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(SIZE), "Native template dimensions differ");
            CompoundTag roundtrip = template.save(new CompoundTag());
            require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == RECORDS, "Native roundtrip lost records");
            NbtIo.writeCompressed(roundtrip, root().resolve("qa/native-roundtrip.nbt").toFile());
            report.put("native_decode_records", expected.size());
            report.put("native_roundtrip_sha256", hash(root().resolve("qa/native-roundtrip.nbt")));
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> immediate = new ArrayList<>();
            Set<ChunkPos> forcedChunks = new HashSet<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos anchor = helper.absolutePos(new BlockPos(40 + index * 48, 0, 40));
                BlockPos origin = new BlockPos(anchor.getX(), 240, anchor.getZ());
                Fixture fixture = new Fixture(origin, rotation);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    ChunkPos chunk = new ChunkPos(position);
                    if (forcedChunks.add(chunk)) {
                        level.setChunkForced(chunk.x, chunk.z, true);
                    }
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placeInWorld returned false");
                immediate.add(verifyPlacement(level, fixture, expected, metadata, blueprint, false));
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                BlockPos leg = fixture.world(new BlockPos(11, 1, 5));
                BlockState savedLeg = level.getBlockState(leg);
                level.setBlock(leg, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                boolean missingLegRejected = false;
                try {
                    verifySupports(level, fixture, blueprint);
                } catch (IllegalStateException expectedFailure) {
                    missingLegRejected = true;
                }
                require(missingLegRejected, "Missing table-leg negative probe undetected");
                level.setBlock(leg, savedLeg, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos chain = fixture.world(new BlockPos(10, 8, 4));
                BlockState savedChain = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lamp = fixture.world(new BlockPos(10, 7, 4));
                require(!level.getBlockState(lamp).canSurvive(level, lamp), "Missing lamp support undetected");
                level.setBlock(chain, savedChain, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos entrance = fixture.world(new BlockPos(8, 1, 0));
                level.setBlock(entrance, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, entrance), "Blocked entrance negative probe undetected");
                level.setBlock(entrance, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos roof = fixture.world(new BlockPos(8, 10, 8));
                BlockState savedRoof = level.getBlockState(roof);
                level.setBlock(roof, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                boolean missingRoofRejected = false;
                try {
                    verifySupports(level, fixture, blueprint);
                } catch (IllegalStateException expectedFailure) {
                    missingRoofRejected = true;
                }
                require(missingRoofRejected, "Roof-hole negative probe undetected");
                level.setBlock(roof, savedRoof, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                fixtures.add(fixture);
                index++;
            }
            CompoundTag stocked = new CompoundTag();
            ListTag items = new ListTag();
            CompoundTag item = new CompoundTag();
            item.putString("id", "minecraft:diamond");
            item.putByte("Count", (byte) 1);
            items.add(item);
            stocked.put("Items", items);
            require(!stockEmpty(stocked), "Stocked inventory negative probe undetected");
            report.put("immediate_placements", immediate);
            report.put("forced_ticking_chunks", forcedChunks.size());
            report.put("negative_probes", Map.of("missing_table_leg", 4, "missing_lamp_support", 4, "blocked_entrance", 4, "roof_hole", 4, "stocked_inventory", 1));
            report.put("stabilization_ticks", 100);
            writeReport(report);
            helper.runAfterDelay(100, () -> {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        results.add(verifyPlacement(level, fixture, expected, metadata, blueprint, true));
                    }
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source changed during native test");
                    report.put("rotations", results);
                    report.put("records_checked_after_ticks", results.size() * expected.size());
                    report.put("status", "PASS");
                    report.put("limitations", List.of("No FT catalog slot, hall connection, idempotence, occupancy rejection or production deployment tested.", "No full-modpack player client, multiplayer, visual screenshot or FPS test.", "100-tick stability under the isolated Forge runtime is bounded, not a long-running server guarantee."));
                    writeReport(report);
                    for (ChunkPos chunk : forcedChunks) {
                        level.setChunkForced(chunk.x, chunk.z, false);
                    }
                    System.out.println("R5_STONEMASON_NATIVE_PASS: rotations=4/4 records=12716 ticks=100 stock=0 source=" + fingerprint);
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
