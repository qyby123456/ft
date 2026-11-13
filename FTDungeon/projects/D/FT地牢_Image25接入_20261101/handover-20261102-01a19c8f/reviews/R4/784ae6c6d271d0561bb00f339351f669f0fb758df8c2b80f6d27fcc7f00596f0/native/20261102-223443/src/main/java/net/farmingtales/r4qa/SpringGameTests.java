package net.farmingtales.r4qa;

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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftr4qa")
@PrefixGameTestTemplate(false)
public final class SpringGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final int VOLUME = 17 * 13 * 19;
    private static final Set<String> FORBIDDEN = Set.of("lava", "fire", "soul_fire", "campfire", "soul_campfire", "magma_block", "cactus", "wither_rose", "sweet_berry_bush", "powder_snow", "cobweb", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("ftr4qa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static BlockPos coordinate(JsonArray value) {
        return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt());
    }

    private static void report(Map<String, Object> result) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
    }

    private static boolean stockEmpty(CompoundTag data) {
        for (String key : data.getAllKeys()) {
            if (Set.of("LootTable", "LootTableSeed", "Book", "RecordItem").contains(key)) {
                return false;
            }
            if (key.equals("Items") && !data.getList(key, Tag.TAG_COMPOUND).isEmpty()) {
                return false;
            }
            if (data.get(key) instanceof CompoundTag child && !stockEmpty(child)) {
                return false;
            }
        }
        return true;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require single palette 1.20.1 structure");
        ListTag size = data.getList("size", Tag.TAG_INT);
        require(size.size() == 3 && size.getInt(0) == 17 && size.getInt(1) == 13 && size.getInt(2) == 19, "Wrong dimensions");
        require(coordinate(metadata.getAsJsonArray("size")).equals(new BlockPos(17, 13, 19)), "Metadata size mismatch");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "No entities permitted");
        ListTag encoded = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < encoded.size(); index++) {
            CompoundTag value = encoded.getCompound(index);
            ResourceLocation identity = new ResourceLocation(value.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla block: " + identity);
            require(!FORBIDDEN.contains(identity.getPath()), "Forbidden block: " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = value.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Illegal block property: " + identity + " " + key);
            }
            palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), value));
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag entry = blocks.getCompound(index);
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            require(position.size() == 3, "Malformed position");
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 17 && relative.getY() >= 0 && relative.getY() < 13 && relative.getZ() >= 0 && relative.getZ() < 19, "Out of bounds position");
            int stateIndex = entry.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Bad palette index");
            BlockState state = palette.get(stateIndex);
            require(expected.put(relative, state) == null, "Duplicate coordinate");
            require(state.hasBlockEntity() == entry.contains("nbt", Tag.TAG_COMPOUND), "Block entity presence mismatch");
            if (state.hasBlockEntity()) {
                CompoundTag blockData = entry.getCompound("nbt");
                require(stockEmpty(blockData), "Nonempty payload");
                BlockEntity entity = BlockEntity.loadStatic(relative, state, blockData.copy());
                require(entity != null && entity.getType().isValid(state), "Invalid native block entity");
                require(!(entity instanceof Container inventory) || inventory.isEmpty(), "Stocked native inventory");
            }
        }
        require(expected.size() == VOLUME, "Require complete explicit volume");
        return expected;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getFluidState().isEmpty() && state.getCollisionShape(level, position).isEmpty()
                && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState floor = level.getBlockState(below);
        return clear(level, feet) && clear(level, feet.above()) && floor.getFluidState().isEmpty() && floor.isFaceSturdy(level, below, Direction.UP);
    }

    private static boolean foundation(ServerLevel level, Fixture fixture) {
        for (int posX = 0; posX < 17; posX++) {
            for (int posZ = 0; posZ < 19; posZ++) {
                BlockPos world = fixture.world(new BlockPos(posX, 0, posZ));
                if (!level.getBlockState(world).isCollisionShapeFullBlock(level, world)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean inEnvelope(BlockPos position) {
        boolean pool = position.getX() >= 3 && position.getX() <= 13 && position.getY() >= 1 && position.getY() <= 2 && position.getZ() >= 3 && position.getZ() <= 14;
        boolean cascade = position.getX() == 8 && position.getY() >= 3 && position.getY() <= 7 && position.getZ() >= 11 && position.getZ() <= 17;
        return pool || cascade;
    }

    private static AABB fixtureBounds(Fixture fixture) {
        BlockPos cornerA = fixture.world(new BlockPos(0, 0, 0));
        BlockPos cornerB = fixture.world(new BlockPos(16, 12, 18));
        return new AABB(Math.min(cornerA.getX(), cornerB.getX()), cornerA.getY(), Math.min(cornerA.getZ(), cornerB.getZ()), Math.max(cornerA.getX(), cornerB.getX()) + 1, cornerB.getY() + 1, Math.max(cornerA.getZ(), cornerB.getZ()) + 1);
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject metadata, JsonObject blueprint, boolean bootstrap) {
        int waters = 0;
        int blockEntities = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos world = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(world);
            BlockState wanted = entry.getValue().rotate(fixture.rotation());
            boolean equilibration = bootstrap && (actual.getBlock() == wanted.getBlock() || actual.is(Blocks.WATER) && wanted.isAir());
            require(actual.equals(wanted) || equilibration, "State drift " + fixture.rotation() + " at " + entry.getKey() + ": " + actual + ", expected " + wanted);
            require(actual.canSurvive(level, world), "Unsupported block at " + entry.getKey() + ": " + actual);
            if (!actual.getFluidState().isEmpty()) {
                require(actual.is(Blocks.WATER) && inEnvelope(entry.getKey()), "Water escaped bounded envelope at " + entry.getKey());
                waters++;
            }
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(world);
                require(entity != null && entity.getType().isValid(actual), "Missing block entity");
                require(stockEmpty(entity.saveWithFullMetadata()), "Placed nonempty stock/loot");
                require(!(entity instanceof Container inventory) || inventory.isEmpty(), "Placed inventory not empty");
                blockEntities++;
            }
        }
        require(waters > 100 && level.getFluidState(fixture.world(new BlockPos(8, 7, 17))).isSource(), "Spring/pool missing");
        require(foundation(level, fixture), "Pool foundation perforated");
        for (String group : List.of("main_clearance", "bridge_clearance")) {
            for (JsonElement value : blueprint.getAsJsonArray(group)) {
                require(clear(level, fixture.world(coordinate(value.getAsJsonArray()))), "Dry 3x3 route obstructed: " + value);
            }
        }
        for (int posX = 0; posX < 17; posX++) {
            for (int posZ = 0; posZ < 19; posZ++) {
                BlockPos roof = fixture.world(new BlockPos(posX, 12, posZ));
                require(level.getBlockState(roof).isCollisionShapeFullBlock(level, roof), "Incomplete roof");
            }
        }
        for (int posX = -1; posX <= 17; posX++) {
            for (int posY = -1; posY <= 13; posY++) {
                for (int posZ = -1; posZ <= 19; posZ++) {
                    if (posX < 0 || posX > 16 || posY < 0 || posY > 12 || posZ < 0 || posZ > 18) {
                        BlockPos moat = fixture.world(new BlockPos(posX, posY, posZ));
                        require(level.getFluidState(moat).isEmpty(), "External fluid at relative " + new BlockPos(posX, posY, posZ) + " world " + moat + ": " + level.getFluidState(moat));
                    }
                }
            }
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos entrance = coordinate(metadata.getAsJsonArray("entrance"));
        require(entrance.getZ() == 0 && entrance.getX() == 8 && walkable(level, fixture.world(entrance)), "Invalid entrance");
        reached.add(entrance);
        queue.add(entrance);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() >= 0 && next.getX() < 17 && next.getZ() >= 0 && next.getZ() < 19
                        && !reached.contains(next) && walkable(level, fixture.world(next))) {
                    reached.add(next);
                    queue.add(next);
                }
            }
        }
        require(reached.contains(coordinate(metadata.getAsJsonArray("interior"))), "Safety point disconnected");
        for (JsonElement value : blueprint.getAsJsonArray("required_feet")) {
            require(reached.contains(coordinate(value.getAsJsonArray())), "Required dry bank/bridge disconnected: " + value);
        }
        List<String> unexpected = level.getEntities(null, fixtureBounds(fixture)).stream().map(entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()) + " at " + entity.position()).toList();
        require(unexpected.isEmpty(), "Unexpected entity/drop inside room: " + unexpected);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("fixture_world_origin", List.of(fixture.origin().getX(), fixture.origin().getY(), fixture.origin().getZ()));
        result.put("records", expected.size());
        result.put("water_blocks", waters);
        result.put("empty_block_entities", blockEntities);
        result.put("reachable_dry_feet", reached.size());
        result.put("foundation_roof_supports_routes_envelope_external_moat", "PASS");
        return result;
    }

    private static CompoundTag capture(ServerLevel level, Fixture fixture, CompoundTag source, Map<BlockPos, BlockState> expected) {
        Rotation inverse = switch (fixture.rotation()) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> fixture.rotation();
        };
        CompoundTag result = source.copy();
        ListTag palette = new ListTag();
        ListTag records = new ListTag();
        Map<BlockState, Integer> indices = new LinkedHashMap<>();
        for (BlockPos relative : expected.keySet()) {
            BlockPos world = fixture.world(relative);
            BlockState block = level.getBlockState(world).rotate(inverse);
            if (!indices.containsKey(block)) {
                indices.put(block, palette.size());
                palette.add(NbtUtils.writeBlockState(block));
            }
            CompoundTag record = new CompoundTag();
            ListTag position = new ListTag();
            position.add(net.minecraft.nbt.IntTag.valueOf(relative.getX()));
            position.add(net.minecraft.nbt.IntTag.valueOf(relative.getY()));
            position.add(net.minecraft.nbt.IntTag.valueOf(relative.getZ()));
            record.put("pos", position);
            record.putInt("state", indices.get(block));
            BlockEntity entity = level.getBlockEntity(world);
            if (entity != null) {
                CompoundTag data = entity.saveWithFullMetadata();
                data.remove("x");
                data.remove("y");
                data.remove("z");
                record.put("nbt", data);
            }
            records.add(record);
        }
        result.put("palette", palette);
        result.put("blocks", records);
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void springFourRotations(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "RUNNING");
        boolean bootstrap = Boolean.getBoolean("ftr4qa.bootstrap");
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Fixture may only run inside isolated GameTestServer");
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            CompoundTag source = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json"), StandardCharsets.UTF_8), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("blueprint.json"), StandardCharsets.UTF_8), JsonObject.class);
            String fingerprint = hash(root().resolve("room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Metadata hash differs");
            Map<BlockPos, BlockState> expected = audit(source, metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), source);
            require(template.getSize().equals(new Vec3i(17, 13, 19)), "Native template size mismatch");
            require(audit(template.save(new CompoundTag()), metadata).equals(expected), "Native template roundtrip mismatch");
            result.put("source_nbt_sha256", fingerprint);
            result.put("java", System.getProperty("java.version"));
            result.put("minecraft", "1.20.1");
            result.put("forge", net.minecraftforge.versions.forge.ForgeVersion.getVersion());
            result.put("bootstrap", bootstrap);
            result.put("random_tick_speed", 0);
            result.put("fluid_ticks_enabled", true);
            result.put("isolated_run_directory", System.getProperty("user.dir"));
            result.put("stabilization_ticks", 240);
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> teardown = new ArrayList<>();
            int rotationIndex = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos origin = new BlockPos(48 + rotationIndex * 64, 256, 48);
                Fixture fixture = new Fixture(origin, rotation);
                for (int chunkX = (origin.getX() - 32) >> 4; chunkX <= (origin.getX() + 32) >> 4; chunkX++) {
                    for (int chunkZ = (origin.getZ() - 32) >> 4; chunkZ <= (origin.getZ() + 32) >> 4; chunkZ++) {
                        level.setChunkForced(chunkX, chunkZ, true);
                    }
                }
                for (int posX = -1; posX <= 17; posX++) {
                    for (int posY = -1; posY <= 13; posY++) {
                        for (int posZ = -1; posZ <= 19; posZ++) {
                            level.setBlock(fixture.world(new BlockPos(posX, posY, posZ)), Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
                        }
                    }
                }
                List<String> preexisting = level.getEntities(null, fixtureBounds(fixture).inflate(3)).stream().map(entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()) + " at " + entity.position()).toList();
                level.getEntities(null, fixtureBounds(fixture).inflate(3)).forEach(entity -> entity.discard());
                teardown.add(Map.of("rotation", rotation.name(), "preexisting_entities_removed_before_placement", preexisting));
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placeInWorld failed");
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                    if (!level.getFluidState(position).isEmpty()) {
                        level.scheduleTick(position, Fluids.WATER, 5);
                    }
                }
                BlockPos aisle = fixture.world(new BlockPos(8, 3, 3));
                level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, aisle), "Blocked-path negative probe missed");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos bed = fixture.world(new BlockPos(4, 0, 4));
                BlockState savedBed = level.getBlockState(bed);
                level.setBlock(bed, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!foundation(level, fixture), "Perforated-pool negative probe missed");
                level.setBlock(bed, savedBed, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos anchor = fixture.world(new BlockPos(4, 12, 2));
                BlockState savedAnchor = level.getBlockState(anchor);
                level.setBlock(anchor, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos drip = fixture.world(new BlockPos(4, 11, 2));
                require(!level.getBlockState(drip).canSurvive(level, drip), "Unsupported-dripstone negative probe missed");
                level.setBlock(anchor, savedAnchor, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                fixtures.add(fixture);
                rotationIndex++;
            }
            result.put("isolated_fixture_teardown", teardown);
            result.put("negative_blocked_path_probes", 4);
            result.put("negative_perforated_pool_probes", 4);
            result.put("negative_missing_dripstone_anchor_probes", 4);
            Map<Rotation, CompoundTag> stableAt200 = new LinkedHashMap<>();
            helper.runAfterDelay(200, () -> {
                for (Fixture fixture : fixtures) {
                    stableAt200.put(fixture.rotation(), capture(level, fixture, source, expected));
                }
            });
            report(result);
            helper.runAfterDelay(240, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        rotations.add(verify(level, fixture, expected, metadata, blueprint, bootstrap));
                        CompoundTag captured = capture(level, fixture, source, expected);
                        require(captured.equals(stableAt200.get(fixture.rotation())), "Room still changing between ticks 200 and 240");
                        NbtIo.writeCompressed(captured, root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase() + ".nbt").toFile());
                    }
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source modified during native QA");
                    result.put("rotations", rotations);
                    result.put("records_checked_after_ticks", 4 * expected.size());
                    result.put("stable_snapshot_ticks", List.of(200, 240));
                    result.put("status", bootstrap ? "BOOTSTRAP_PASS" : "PASS");
                    result.put("limitations", List.of("Isolated Forge server, not a modpack client screenshot or multiplayer/FPS test.", "No FT core/catalog, hallway connection, world occupancy/idempotence or deployment tested.", "Random plant growth disabled; scheduled fluid and support updates remain active."));
                    report(result);
                    System.out.println("R4_NATIVE_GAME_TEST_" + result.get("status") + ": rotations=4/4 records=" + 4 * expected.size() + " ticks=240 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception exception) {
                    result.put("status", "FAIL");
                    result.put("error", exception.toString());
                    try {
                        report(result);
                    } catch (Exception secondary) {
                        exception.addSuppressed(secondary);
                    }
                    throw new IllegalStateException(exception);
                }
            });
        } catch (Exception exception) {
            result.put("status", "FAIL");
            result.put("error", exception.toString());
            report(result);
            throw exception;
        }
    }
}
