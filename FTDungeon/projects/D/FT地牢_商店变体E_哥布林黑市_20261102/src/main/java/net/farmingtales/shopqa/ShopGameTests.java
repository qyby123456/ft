package net.farmingtales.shopqa;

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
import net.minecraft.world.Container;
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

@GameTestHolder("ftgoblinqa")
@PrefixGameTestTemplate(false)
public final class ShopGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final int WIDTH = 13;
    private static final int HEIGHT = 9;
    private static final int DEPTH = 13;
    private static final int RECORDS = WIDTH * HEIGHT * DEPTH;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "campfire", "soul_campfire", "wither_rose", "sweet_berry_bush", "cobweb", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private ShopGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("ftgoblinqa.root")).toAbsolutePath().normalize();
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
            if ((key.equals("Items") || key.equals("Item")) && value != null
                    && (!(value instanceof ListTag list) || !list.isEmpty())) {
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
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid state property: " + identity + " " + key);
        }
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(state.getFluidState().isEmpty(), "No fluid or waterlogged state allowed: " + identity);
        return state;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require 1.20.1 single-palette template");
        ListTag size = data.getList("size", Tag.TAG_INT);
        require(size.size() == 3 && size.getInt(0) == WIDTH && size.getInt(1) == HEIGHT && size.getInt(2) == DEPTH, "Wrong native dimensions");
        require(coordinate(metadata.getAsJsonArray("size")).equals(new BlockPos(WIDTH, HEIGHT, DEPTH)), "Metadata dimensions differ");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "No preplaced entities");
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < encodedPalette.size(); index++) {
            palette.add(decode(encodedPalette.getCompound(index)));
        }
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag entry = blocks.getCompound(index);
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            require(position.size() == 3, "Invalid coordinate length");
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < WIDTH && relative.getY() >= 0 && relative.getY() < HEIGHT && relative.getZ() >= 0 && relative.getZ() < DEPTH, "Out of bounds record");
            int stateIndex = entry.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Invalid palette index");
            BlockState state = palette.get(stateIndex);
            require(result.put(relative, state) == null, "Duplicate coordinate");
            require(state.hasBlockEntity() == entry.contains("nbt", Tag.TAG_COMPOUND), "Block entity payload presence differs at " + relative);
            if (state.hasBlockEntity()) {
                CompoundTag blockData = entry.getCompound("nbt");
                require(stockEmpty(blockData), "Reward inventory or loot table at " + relative);
                BlockEntity entity = BlockEntity.loadStatic(relative, state, blockData.copy());
                require(entity != null && entity.getType().isValid(state), "Invalid native block entity at " + relative);
                require(!(entity instanceof Container container) || container.isEmpty(), "Native inventory is not empty");
            }
        }
        require(result.size() == RECORDS, "Require full explicit 13x9x13 volume");
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

    private static boolean suspendedPotSupported(ServerLevel level, Fixture fixture, JsonObject support) {
        BlockPos previous = fixture.world(coordinate(support.getAsJsonArray("pot")));
        if (!level.getBlockState(previous).is(Blocks.CAULDRON)) {
            return false;
        }
        for (JsonElement value : support.getAsJsonArray("chains")) {
            BlockPos chain = fixture.world(coordinate(value.getAsJsonArray()));
            if (!chain.equals(previous.above()) || !level.getBlockState(chain).is(Blocks.CHAIN)) {
                return false;
            }
            previous = chain;
        }
        BlockPos beam = fixture.world(coordinate(support.getAsJsonArray("beam")));
        return beam.equals(previous.above()) && level.getBlockState(beam).is(Blocks.DARK_OAK_LOG);
    }

    private static Map<String, Object> verifyPlacement(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected,
            JsonObject metadata, JsonObject blueprint, boolean capture) throws Exception {
        int entities = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " at " + entry.getKey() + ": " + actual);
            require(actual.canSurvive(level, position), "Unsupported native block at " + entry.getKey());
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity != null && entity.getType().isValid(actual), "Missing/wrong placed block entity");
                require(stockEmpty(entity.saveWithFullMetadata()), "Placed inventory or loot table is not empty");
                require(!(entity instanceof Container container) || container.isEmpty(), "Placed native container has stock");
                entities++;
            }
        }
        for (JsonElement value : blueprint.getAsJsonArray("entrance_to_counter_aisle")) {
            require(clear(level, fixture.world(coordinate(value.getAsJsonArray()))), "Main 3x3 aisle collision");
        }
        for (JsonElement value : blueprint.getAsJsonArray("counter_clearance")) {
            require(clear(level, fixture.world(coordinate(value.getAsJsonArray()))), "Counter circulation collision");
        }
        for (JsonElement value : blueprint.getAsJsonArray("lamp_supports")) {
            JsonObject support = value.getAsJsonObject();
            BlockPos lamp = fixture.world(coordinate(support.getAsJsonArray("lantern")));
            BlockPos chain = fixture.world(coordinate(support.getAsJsonArray("chain")));
            BlockPos beam = fixture.world(coordinate(support.getAsJsonArray("beam")));
            require(level.getBlockState(lamp).is(Blocks.LANTERN) && level.getBlockState(chain).is(Blocks.CHAIN)
                    && level.getBlockState(beam).is(Blocks.DARK_OAK_LOG) && beam.above().getY() == fixture.origin().getY() + HEIGHT - 1,
                    "Chain and beam support missing");
            BlockPos previous = lamp;
            for (JsonElement segmentValue : support.getAsJsonArray("chains")) {
                BlockPos segment = fixture.world(coordinate(segmentValue.getAsJsonArray()));
                require(segment.equals(previous.above()) && level.getBlockState(segment).is(Blocks.CHAIN), "Lamp chain has a gap");
                previous = segment;
            }
            require(beam.equals(previous.above()), "Lamp chain is not attached to beam");
        }
        int potSupports = 0;
        for (JsonElement value : blueprint.getAsJsonArray("suspended_pot_supports")) {
            require(suspendedPotSupported(level, fixture, value.getAsJsonObject()), "Suspended pot chain/frame support missing");
            potSupports++;
        }
        for (JsonElement value : blueprint.getAsJsonArray("chest_lid_clearance")) {
            require(clear(level, fixture.world(coordinate(value.getAsJsonArray()))), "Chest lid clearance blocked");
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        BlockPos entrance = coordinate(metadata.getAsJsonArray("entrance"));
        require(walkable(level, fixture.world(entrance)), "Entrance not walkable");
        reached.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() >= 0 && next.getX() < WIDTH && next.getZ() >= 0 && next.getZ() < DEPTH
                        && !reached.contains(next) && walkable(level, fixture.world(next))) {
                    reached.add(next);
                    pending.addLast(next);
                }
            }
        }
        require(reached.contains(coordinate(metadata.getAsJsonArray("interior"))), "Interior point unreachable");
        for (JsonElement value : blueprint.getAsJsonArray("required_walkable")) {
            require(reached.contains(coordinate(value.getAsJsonArray())), "Counter circuit or chest approach unreachable");
        }
        for (int offset = -1; offset <= 1; offset++) {
            BlockPos feet = entrance.offset(offset, 0, 0);
            require(walkable(level, fixture.world(feet)) && clear(level, fixture.world(feet.above(2))), "North 3x3 entry not clear");
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation().name());
        report.put("records_checked", expected.size());
        report.put("block_entities_checked", entities);
        report.put("reachable_foot_positions", reached.size());
        report.put("suspended_pot_supports_checked", potSupports);
        int unexpectedEntities = level.getEntities(null, new AABB(fixture.origin().offset(-WIDTH, -1, -DEPTH), fixture.origin().offset(WIDTH + 1, HEIGHT + 1, DEPTH + 1))).size();
        require(unexpectedEntities == 0, "Unexpected world entities around the standalone fixture");
        report.put("unexpected_world_entities", unexpectedEntities);
        report.put("state_collision_survival_inventory", "PASS");
        if (capture) {
            BlockPos minimum = fixture.world(BlockPos.ZERO);
            for (BlockPos corner : List.of(new BlockPos(WIDTH - 1, 0, 0), new BlockPos(0, 0, DEPTH - 1), new BlockPos(WIDTH - 1, 0, DEPTH - 1))) {
                BlockPos transformed = fixture.world(corner);
                minimum = new BlockPos(Math.min(minimum.getX(), transformed.getX()), minimum.getY(), Math.min(minimum.getZ(), transformed.getZ()));
            }
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, new Vec3i(WIDTH, HEIGHT, DEPTH), false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            Path export = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, export.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(export.toFile()));
            require(reloaded.getSize().equals(new Vec3i(WIDTH, HEIGHT, DEPTH)) && saved.getList("blocks", Tag.TAG_COMPOUND).size() == RECORDS, "World capture roundtrip failed");
            report.put("world_capture", root().relativize(export).toString());
            report.put("world_capture_sha256", hash(export));
        }
        return report;
    }

    @GameTest(template = "empty", timeoutTicks = 260)
    public static void placeFourRotationsAndStabilize(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("java", System.getProperty("java.version"));
        report.put("scope", "Isolated Forge GameTestServer, not FT core integration or a player/client save");
        writeReport(report);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Fixture writes only allowed in isolated GameTestServer");
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("out/room.json")), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("out/blueprint.json")), JsonObject.class);
            String fingerprint = hash(root().resolve("out/room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Source fingerprint mismatch");
            report.put("structure_sha256", fingerprint);
            CompoundTag data = NbtIo.readCompressed(root().resolve("out/room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(data, metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(new Vec3i(WIDTH, HEIGHT, DEPTH)), "Native template size mismatch");
            CompoundTag roundtrip = template.save(new CompoundTag());
            require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == expected.size(), "Native template roundtrip lost records");
            report.put("native_decode_registry_properties_block_entities_roundtrip", "PASS");
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos origin = helper.absolutePos(new BlockPos(32 + index * 32, 20, 32));
                Fixture fixture = new Fixture(origin, rotation);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                }
                level.getEntities(null, new AABB(origin.offset(-WIDTH, -1, -DEPTH), origin.offset(WIDTH + 1, HEIGHT + 1, DEPTH + 1))).forEach(entity -> entity.discard());
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placeInWorld returned false");
                verifyPlacement(level, fixture, expected, metadata, blueprint, false);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                JsonObject firstLamp = blueprint.getAsJsonArray("lamp_supports").get(0).getAsJsonObject();
                BlockPos chain = fixture.world(coordinate(firstLamp.getAsJsonArray("chain")));
                BlockState savedChain = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lamp = fixture.world(coordinate(firstLamp.getAsJsonArray("lantern")));
                require(!level.getBlockState(lamp).canSurvive(level, lamp), "Negative missing-chain probe was not detected");
                level.setBlock(chain, savedChain, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                JsonObject firstPot = blueprint.getAsJsonArray("suspended_pot_supports").get(0).getAsJsonObject();
                BlockPos potChain = fixture.world(coordinate(firstPot.getAsJsonArray("chains").get(0).getAsJsonArray()));
                BlockState savedPotChain = level.getBlockState(potChain);
                level.setBlock(potChain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!suspendedPotSupported(level, fixture, firstPot), "Negative missing-pot-chain probe was not detected");
                level.setBlock(potChain, savedPotChain, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos aisle = fixture.world(new BlockPos(6, 1, 2));
                level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS);
                require(!walkable(level, aisle), "Negative collision probe was not detected");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS);
                fixtures.add(fixture);
                index++;
            }
            report.put("negative_missing_support_probes", 4);
            report.put("negative_missing_pot_chain_probes", 4);
            report.put("negative_blocked_aisle_probes", 4);
            report.put("stabilization_ticks", 100);
            writeReport(report);
            helper.runAfterDelay(100, () -> {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        results.add(verifyPlacement(level, fixture, expected, metadata, blueprint, true));
                    }
                    require(hash(root().resolve("out/room.nbt")).equals(fingerprint), "Source changed during verification");
                    report.put("rotations", results);
                    report.put("records_checked_after_ticks", results.size() * expected.size());
                    report.put("status", "PASS");
                    report.put("limitations", List.of("No FT catalog slot, gallery walkway, idempotence or production deployment tested.", "No full modpack client, multiplayer, FPS or merchant trade behavior tested.", "Native world capture is machine evidence, not an in-game screenshot."));
                    writeReport(report);
                    System.out.println("GOBLIN_E_NATIVE_GAME_TEST_PASS: rotations=4/4, records=" + (4 * RECORDS) + ", ticks=100, stock=0, source=" + fingerprint);
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
