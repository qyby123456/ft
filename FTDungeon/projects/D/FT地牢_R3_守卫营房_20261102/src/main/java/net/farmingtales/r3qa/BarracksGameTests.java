package net.farmingtales.r3qa;

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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftr3qa")
@PrefixGameTestTemplate(false)
public final class BarracksGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final int COUNT = 17 * 10 * 17;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "wither_rose", "sweet_berry_bush", "cobweb", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private BarracksGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("ftr3qa.root")).toAbsolutePath().normalize();
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

    private static boolean stockEmpty(Tag data) {
        if (data instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                Tag value = compound.get(key);
                if (Set.of("LootTable", "LootTableSeed", "Book", "RecordItem").contains(key)) {
                    return false;
                }
                if ((key.equals("Items") || key.equals("Item")) && (!(value instanceof ListTag list) || !list.isEmpty())) {
                    return false;
                }
                if (!stockEmpty(value)) {
                    return false;
                }
            }
        } else if (data instanceof ListTag list) {
            for (Tag child : list) {
                if (!stockEmpty(child)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla block: " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden block: " + identity);
        require(!identity.getPath().endsWith("_bed") && !identity.getPath().equals("respawn_anchor"), "Explosive interaction unsafe in FT dungeon: " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property: " + identity + " " + key);
        }
        require(properties.getAllKeys().size() == block.getStateDefinition().getProperties().size(), "Incomplete properties: " + identity);
        BlockState result = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(result.getFluidState().isEmpty(), "Fluid/waterlogged state: " + identity);
        require(!result.is(Blocks.CAMPFIRE) || !result.getValue(BlockStateProperties.LIT), "Hearth must be extinguished");
        return result;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Require single-palette Java 1.20.1 template");
        ListTag size = data.getList("size", Tag.TAG_INT);
        require(size.size() == 3 && size.getInt(0) == 17 && size.getInt(1) == 10 && size.getInt(2) == 17, "Wrong dimensions");
        require(coordinate(metadata.getAsJsonArray("size")).equals(new BlockPos(17, 10, 17)), "Metadata size differs");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "No preplaced entities");
        ListTag paletteData = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < paletteData.size(); index++) {
            palette.add(decode(paletteData.getCompound(index)));
        }
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag entry = blocks.getCompound(index);
            BlockPos relative = coordinateList(entry.getList("pos", Tag.TAG_INT));
            require(relative.getX() >= 0 && relative.getX() < 17 && relative.getY() >= 0 && relative.getY() < 10 && relative.getZ() >= 0 && relative.getZ() < 17, "Out-of-bounds record");
            int paletteIndex = entry.getInt("state");
            require(paletteIndex >= 0 && paletteIndex < palette.size(), "Invalid palette index");
            BlockState state = palette.get(paletteIndex);
            require(result.put(relative, state) == null, "Duplicate coordinate");
            require(state.hasBlockEntity() == entry.contains("nbt", Tag.TAG_COMPOUND), "Block entity payload mismatch at " + relative);
            if (state.hasBlockEntity()) {
                CompoundTag blockData = entry.getCompound("nbt");
                require(stockEmpty(blockData), "Reward inventory/loot table at " + relative);
                BlockEntity entity = BlockEntity.loadStatic(relative, state, blockData.copy());
                require(entity != null && entity.getType().isValid(state), "Invalid block entity at " + relative);
                require(!(entity instanceof Container container) || container.isEmpty(), "Nonempty native inventory");
            }
        }
        require(result.size() == COUNT, "Require explicit full volume");
        return result;
    }

    private static BlockPos coordinateList(ListTag value) {
        require(value.size() == 3, "Invalid coordinate length");
        return new BlockPos(value.getInt(0), value.getInt(1), value.getInt(2));
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getFluidState().isEmpty() && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()) && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean floor(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getFluidState().isEmpty() && state.isFaceSturdy(level, position, Direction.UP) && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        return clear(level, feet) && clear(level, feet.above()) && floor(level, feet.below());
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject metadata, JsonObject blueprint, boolean capture) throws Exception {
        int blockEntities = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " at " + entry.getKey() + ": " + actual + ", expected " + entry.getValue().rotate(fixture.rotation()));
            require(actual.canSurvive(level, position), "Unsupported block at " + entry.getKey());
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity != null && entity.getType().isValid(actual), "Missing placed block entity");
                require(stockEmpty(entity.saveWithFullMetadata()), "Placed reward inventory/loot table");
                require(!(entity instanceof Container container) || container.isEmpty(), "Placed nonempty inventory");
                blockEntities++;
            }
        }
        for (JsonElement value : blueprint.getAsJsonArray("main_aisle")) {
            require(clear(level, fixture.world(coordinate(value.getAsJsonArray()))), "Three-wide/three-high main aisle blocked");
        }
        for (JsonElement value : blueprint.getAsJsonArray("bunks")) {
            JsonObject bunk = value.getAsJsonObject();
            BlockPos foot = coordinate(bunk.getAsJsonArray("foot"));
            BlockPos head = coordinate(bunk.getAsJsonArray("head"));
            require(level.getBlockState(fixture.world(foot)).is(Blocks.RED_CARPET) && level.getBlockState(fixture.world(head)).is(Blocks.WHITE_CARPET), "Decorative mattress/pillow pair missing");
            require(floor(level, fixture.world(foot.below())) && floor(level, fixture.world(head.below())), "Bed has no continuous load-bearing platform");
            for (JsonElement post : bunk.getAsJsonArray("posts")) {
                require(level.getBlockState(fixture.world(coordinate(post.getAsJsonArray()))).is(Blocks.STRIPPED_DARK_OAK_LOG), "Bunk floor-to-platform post missing");
            }
        }
        for (JsonElement value : blueprint.getAsJsonArray("racks")) {
            JsonObject rack = value.getAsJsonObject();
            for (JsonElement base : rack.getAsJsonArray("base")) {
                BlockPos position = fixture.world(coordinate(base.getAsJsonArray()));
                require(level.getBlockState(position).is(Blocks.DARK_OAK_PLANKS) && floor(level, position.below()), "Rack base unsupported");
            }
            for (JsonElement shaft : rack.getAsJsonArray("shafts")) {
                require(level.getBlockState(fixture.world(coordinate(shaft.getAsJsonArray()))).is(Blocks.DARK_OAK_FENCE), "Rack shaft missing");
            }
            for (JsonElement blade : rack.getAsJsonArray("blades")) {
                require(level.getBlockState(fixture.world(coordinate(blade.getAsJsonArray()))).is(Blocks.IRON_BARS), "Rack blade missing");
            }
        }
        for (JsonElement value : blueprint.getAsJsonArray("lamps")) {
            JsonObject lamp = value.getAsJsonObject();
            require(level.getBlockState(fixture.world(coordinate(lamp.getAsJsonArray("lantern")))).is(Blocks.LANTERN), "Lantern missing");
            BlockPos anchor = fixture.world(coordinate(lamp.getAsJsonArray("anchor")));
            require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Lamp beam anchor missing");
            for (JsonElement chain : lamp.getAsJsonArray("chains")) {
                require(level.getBlockState(fixture.world(coordinate(chain.getAsJsonArray()))).is(Blocks.CHAIN), "Lamp chain discontinuous");
            }
        }
        for (int posX = 0; posX < 17; posX++) {
            for (int posZ = 0; posZ < 17; posZ++) {
                BlockPos roof = fixture.world(new BlockPos(posX, 9, posZ));
                require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Incomplete roof");
            }
        }
        for (int posX = 7; posX <= 9; posX++) {
            require(walkable(level, fixture.world(new BlockPos(posX, 1, 0))), "Entrance floor/clearance failure");
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        BlockPos entrance = coordinate(metadata.getAsJsonArray("entrance"));
        require(walkable(level, fixture.world(entrance)), "Entrance unsafe");
        reached.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() >= 0 && next.getX() < 17 && next.getZ() >= 0 && next.getZ() < 17 && !reached.contains(next) && walkable(level, fixture.world(next))) {
                    reached.add(next);
                    pending.addLast(next);
                }
            }
        }
        require(reached.contains(coordinate(metadata.getAsJsonArray("interior"))), "Safe interior unreachable");
        for (String group : List.of("circulation", "required_walkable")) {
            for (JsonElement value : blueprint.getAsJsonArray(group)) {
                require(reached.contains(coordinate(value.getAsJsonArray())), "Bunk/rack/table circulation unreachable: " + value);
            }
        }
        BlockPos minimum = fixture.world(BlockPos.ZERO);
        for (BlockPos corner : List.of(new BlockPos(16, 0, 0), new BlockPos(0, 0, 16), new BlockPos(16, 0, 16))) {
            BlockPos transformed = fixture.world(corner);
            minimum = new BlockPos(Math.min(minimum.getX(), transformed.getX()), minimum.getY(), Math.min(minimum.getZ(), transformed.getZ()));
        }
        var unexpected = level.getEntities(null, new AABB(minimum, minimum.offset(17, 10, 17)));
        int worldEntities = unexpected.size();
        require(worldEntities == 0, "Unexpected entities/drops in fixture: " + unexpected.stream().map(entity -> entity.toString()).toList());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("origin_world", List.of(fixture.origin().getX(), fixture.origin().getY(), fixture.origin().getZ()));
        result.put("records_checked", expected.size());
        result.put("block_entities_checked", blockEntities);
        result.put("reachable_foot_positions", reached.size());
        result.put("beds_supported", blueprint.getAsJsonArray("bunks").size());
        result.put("bedding_noninteractive_safe_in_ft", true);
        result.put("racks_supported", blueprint.getAsJsonArray("racks").size());
        result.put("world_entities", worldEntities);
        result.put("roof_blocks", 289);
        result.put("status", "PASS");
        if (capture) {
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, new Vec3i(17, 10, 17), false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            Path export = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, export.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(export.toFile()));
            require(reloaded.getSize().equals(new Vec3i(17, 10, 17)), "Native world capture roundtrip size differs");
            ListTag blocks = saved.getList("blocks", Tag.TAG_COMPOUND);
            ListTag palette = saved.getList("palette", Tag.TAG_COMPOUND);
            require(blocks.size() == COUNT, "Capture record count differs");
            Map<BlockPos, BlockState> captured = new LinkedHashMap<>();
            for (int index = 0; index < blocks.size(); index++) {
                CompoundTag entry = blocks.getCompound(index);
                captured.put(coordinateList(entry.getList("pos", Tag.TAG_INT)), decode(palette.getCompound(entry.getInt("state"))));
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
                require(entry.getValue().rotate(fixture.rotation()).equals(captured.get(fixture.world(entry.getKey()).subtract(minimum))), "World capture differs at " + entry.getKey());
            }
            result.put("world_capture", root().relativize(export).toString());
            result.put("world_capture_sha256", hash(export));
            result.put("capture_matches_source", true);
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 360)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("java", System.getProperty("java.version"));
        report.put("scope", "Isolated Forge GameTestServer; no FT core, production save, client or deployment");
        writeReport(report);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Fixture writes require isolated GameTestServer");
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("blueprint.json")), JsonObject.class);
            String fingerprint = hash(root().resolve("room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Source fingerprint differs");
            report.put("structure_sha256", fingerprint);
            Map<BlockPos, BlockState> expected = audit(NbtIo.readCompressed(root().resolve("room.nbt").toFile()), metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(root().resolve("room.nbt").toFile()));
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos horizontal = helper.absolutePos(new BlockPos(32 + index * 56, 0, 32));
                BlockPos origin = new BlockPos(horizontal.getX(), 288, horizontal.getZ());
                Fixture fixture = new Fixture(origin, rotation);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                }
                level.getEntities(null, new AABB(origin.offset(-17, -1, -17), origin.offset(18, 11, 18))).forEach(entity -> entity.discard());
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native placement returned false");
                verify(level, fixture, expected, metadata, blueprint, false);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                BlockPos chain = fixture.world(new BlockPos(4, 6, 5));
                BlockState chainState = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lamp = fixture.world(new BlockPos(4, 5, 5));
                require(!level.getBlockState(lamp).canSurvive(level, lamp), "Missing lamp support negative probe missed");
                level.setBlock(chain, chainState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos platform = fixture.world(new BlockPos(2, 3, 11));
                BlockState platformState = level.getBlockState(platform);
                level.setBlock(platform, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!floor(level, platform), "Missing bed platform negative probe missed");
                level.setBlock(platform, platformState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos rackBase = fixture.world(new BlockPos(2, 1, 15));
                BlockState rackState = level.getBlockState(rackBase);
                level.setBlock(rackBase, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!floor(level, rackBase), "Missing rack base negative probe missed");
                level.setBlock(rackBase, rackState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos aisle = fixture.world(new BlockPos(8, 1, 2));
                level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, aisle), "Blocked aisle negative probe missed");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                fixtures.add(fixture);
                index++;
            }
            report.put("negative_probes", Map.of("missing_chain", 4, "missing_bed_platform", 4, "missing_rack_base", 4, "blocked_aisle", 4));
            report.put("stabilization_ticks", 200);
            writeReport(report);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        results.add(verify(level, fixture, expected, metadata, blueprint, true));
                    }
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source changed during test");
                    report.put("rotations", results);
                    report.put("records_checked_after_ticks", COUNT * results.size());
                    report.put("status", "PASS");
                    writeReport(report);
                    System.out.println("R3_BARRACKS_NATIVE_PASS: rotations=4/4 records=11560 ticks=200 stock=0 source=" + fingerprint);
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
