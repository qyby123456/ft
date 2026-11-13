package net.farmingtales.zonem03qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
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
import net.minecraft.nbt.IntTag;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftzonem03qa")
@PrefixGameTestTemplate(false)
public final class ZoneGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(23, 12, 25);
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "magma_block", "powder_snow", "cobweb", "sand", "gravel", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light", "campfire", "soul_campfire");

    private record Expected(BlockState state, CompoundTag nbt) {}

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }

        BlockPos minimum() {
            BlockPos corner = world(new BlockPos(22, 11, 24));
            return new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
        }

        Vec3i size() {
            return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(25, 12, 23) : SIZE;
        }

        Rotation inverse() {
            return switch (rotation) {
                case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
                case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
                default -> rotation;
            };
        }
    }

    private ZoneGameTests() {}

    private static Path root() {
        return Path.of(System.getProperty("ftzonem03qa.root")).toAbsolutePath().normalize();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static void report(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/forge-native.json"), JSON.toJson(report));
    }

    private static Map<BlockPos, Expected> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465, "Wrong data version");
        require(!data.contains("palettes"), "Multiple palettes forbidden");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Preplaced entities");
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : data.getList("palette", Tag.TAG_COMPOUND)) {
            CompoundTag stateTag = (CompoundTag) entry;
            ResourceLocation identifier = new ResourceLocation(stateTag.getString("Name"));
            require(identifier.getNamespace().equals("minecraft"), "Nonvanilla block: " + identifier);
            require(BuiltInRegistries.BLOCK.containsKey(identifier) && !FORBIDDEN.contains(identifier.getPath()), "Unknown/forbidden block: " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            CompoundTag properties = stateTag.getCompound("Properties");
            for (String name : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(name);
                require(property != null && property.getValue(properties.getString(name)).isPresent(), "Invalid property " + identifier + ":" + name);
            }
            palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), stateTag));
        }
        Map<BlockPos, Expected> blocks = new LinkedHashMap<>();
        int barrels = 0;
        for (Tag entry : data.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag blockTag = (CompoundTag) entry;
            ListTag coordinates = blockTag.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getX() < 23 && position.getY() >= 0 && position.getY() < 12 && position.getZ() >= 0 && position.getZ() < 25, "Out of bounds");
            int stateIndex = blockTag.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Palette index");
            BlockState state = palette.get(stateIndex);
            CompoundTag entity = blockTag.contains("nbt") ? blockTag.getCompound("nbt") : null;
            require(state.hasBlockEntity() == (entity != null), "Block entity mismatch at " + position);
            if (entity != null) {
                require(state.is(Blocks.BARREL) && entity.getString("id").equals("minecraft:barrel"), "Unexpected block entity");
                require(entity.getList("Items", Tag.TAG_COMPOUND).isEmpty() && !entity.contains("LootTable"), "Inventory/loot found");
                barrels++;
            }
            require(blocks.put(position, new Expected(state, entity)) == null, "Duplicate coordinate");
        }
        require(blocks.size() == 6900 && barrels == 6, "Wrong record/container count");
        return blocks;
    }

    private static void route(ServerLevel level, Fixture fixture) {
        for (int across = 9; across <= 13; across++) {
            for (int depth = 0; depth < 25; depth++) {
                BlockPos floor = fixture.world(new BlockPos(across, 0, depth));
                require(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Nonfull path floor " + floor);
                for (int elevation = 1; elevation <= 4; elevation++) {
                    BlockPos position = fixture.world(new BlockPos(across, elevation, depth));
                    require(level.getBlockState(position).getCollisionShape(level, position).isEmpty() && level.getFluidState(position).isEmpty(), "Blocked route " + position);
                }
            }
        }
    }

    private static void supports(ServerLevel level, Fixture fixture) {
        for (int depth : new int[] {3, 9, 15, 21}) {
            for (int across : new int[] {9, 17}) {
                BlockPos lantern = fixture.world(new BlockPos(across, 6, depth));
                require(level.getBlockState(lantern).is(Blocks.LANTERN), "Lantern missing");
                require(level.getBlockState(lantern.above()).is(Blocks.CHAIN), "Chain missing");
                require(level.getBlockState(lantern.above(2)).is(Blocks.SPRUCE_LOG), "Beam missing");
            }
            for (int across : new int[] {3, 19}) {
                require(level.getBlockState(fixture.world(new BlockPos(across, 1, depth))).is(Blocks.STONE_BRICKS), "Pillar foot replaced");
                for (int elevation = 2; elevation <= 8; elevation++) require(level.getBlockState(fixture.world(new BlockPos(across, elevation, depth))).is(Blocks.SPRUCE_LOG), "Pillar discontinuity");
            }
        }
    }

    private static void negativeProbes(ServerLevel level, Fixture fixture) {
        for (BlockPos relative : List.of(new BlockPos(9, 6, 9), new BlockPos(5, 1, 5))) {
            BlockPos position = fixture.world(relative);
            BlockPos support = relative.getY() == 6 ? position.above() : position.below();
            BlockState original = level.getBlockState(support);
            level.setBlock(support, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
            boolean rejected = !level.getBlockState(position).canSurvive(level, position);
            level.setBlock(support, original, FLAGS | Block.UPDATE_KNOWN_SHAPE);
            require(rejected, "Missing support probe went undetected");
        }
        BlockPos blocker = fixture.world(new BlockPos(11, 1, 12));
        BlockState original = level.getBlockState(blocker);
        level.setBlock(blocker, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
        boolean rejected = false;
        try { route(level, fixture); } catch (IllegalStateException expected) { rejected = true; }
        level.setBlock(blocker, original, FLAGS | Block.UPDATE_KNOWN_SHAPE);
        require(rejected, "Blocked route probe went undetected");
    }

    private static ListTag coordinates(BlockPos position) {
        ListTag result = new ListTag();
        result.add(IntTag.valueOf(position.getX()));
        result.add(IntTag.valueOf(position.getY()));
        result.add(IntTag.valueOf(position.getZ()));
        return result;
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, Expected> expected, boolean capture) throws Exception {
        int survival = 0;
        int containers = 0;
        Map<BlockState, Integer> indices = new HashMap<>();
        ListTag palette = new ListTag();
        ListTag records = new ListTag();
        for (Map.Entry<BlockPos, Expected> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            BlockState wanted = entry.getValue().state().rotate(fixture.rotation());
            require(actual.equals(wanted), fixture.rotation() + " state mismatch at " + entry.getKey() + ": " + actual + " != " + wanted);
            require(actual.canSurvive(level, position), "Cannot survive: " + position + " " + actual);
            require(level.getFluidState(position).isEmpty(), "Unexpected liquid");
            if (!actual.isAir()) survival++;
            BlockEntity entity = level.getBlockEntity(position);
            require((entity != null) == actual.hasBlockEntity(), "Missing/unexpected native block entity");
            if (entity != null) {
                require(entity instanceof Container && ((Container) entity).isEmpty(), "Nonempty container");
                require(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entity.getType()).toString().equals("minecraft:barrel"), "Wrong entity type");
                require(!entity.saveWithFullMetadata().contains("LootTable"), "Loot table in world");
                containers++;
            }
            if (capture) {
                BlockState unrotated = actual.rotate(fixture.inverse());
                if (!indices.containsKey(unrotated)) {
                    indices.put(unrotated, palette.size());
                    palette.add(NbtUtils.writeBlockState(unrotated));
                }
                CompoundTag record = new CompoundTag();
                record.put("pos", coordinates(entry.getKey()));
                record.putInt("state", indices.get(unrotated));
                if (entity != null) {
                    CompoundTag tag = entity.saveWithFullMetadata();
                    tag.remove("x"); tag.remove("y"); tag.remove("z");
                    record.put("nbt", tag);
                }
                records.add(record);
            }
        }
        route(level, fixture);
        supports(level, fixture);
        require(containers == 6, "Container count changed");
        BlockPos minimum = fixture.minimum();
        AABB bounds = new AABB(minimum, minimum.offset(fixture.size())).inflate(1);
        require(level.getEntities(null, bounds).isEmpty(), "Dropped/preplaced/unexpected world entity");
        for (int across = 0; across < 23; across++) for (int depth = 0; depth < 25; depth++) for (int elevation : new int[] {10, 11}) require(!level.getBlockState(fixture.world(new BlockPos(across, elevation, depth))).isAir(), "Roof gap");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().toString());
        result.put("records", expected.size());
        result.put("surviving_non_air", survival);
        result.put("empty_barrels", containers);
        result.put("main_lane_positions", 125);
        result.put("route_entrance_safe_point_exit", "PASS");
        result.put("lamp_chains_frames_roof", "PASS");
        result.put("dropped_entities", 0);
        result.put("status", "PASS");
        if (capture) {
            CompoundTag captured = new CompoundTag();
            captured.putInt("DataVersion", 3465);
            captured.put("size", coordinates(new BlockPos(SIZE)));
            captured.put("palette", palette);
            captured.put("blocks", records);
            captured.put("entities", new ListTag());
            Path target = root().resolve("qa/captures/" + fixture.rotation().toString().toLowerCase() + ".nbt");
            Files.createDirectories(target.getParent());
            NbtIo.writeCompressed(captured, target.toFile());
            result.put("world_capture", root().relativize(target).toString());
            result.put("capture_sha256", hash(target));
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void placementAndStability(GameTestHelper helper) throws Exception {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("id", "zone-M03");
        output.put("status", "RUNNING");
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Refusing non-GameTest server");
            require(level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().startsWith(root().resolve("forge/run")), "Save escaped isolated delivery root");
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            String fingerprint = hash(root().resolve("room.nbt"));
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(metadata.get("sha256").getAsString().equals(fingerprint), "Source fingerprint mismatch");
            output.put("structure_sha256", fingerprint);
            CompoundTag data = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            Map<BlockPos, Expected> expected = audit(data);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(SIZE), "Native template size");
            CompoundTag roundtrip = template.save(new CompoundTag());
            require(audit(roundtrip).equals(expected), "Native semantic roundtrip changed content");
            output.put("registry_properties_nbt_semantic_roundtrip", "PASS");
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> immediate = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(helper.absolutePos(new BlockPos(80 + index * 128, 25, 80)), rotation);
                BlockPos minimum = fixture.minimum();
                Vec3i size = fixture.size();
                for (int chunkX = (minimum.getX() - 2) >> 4; chunkX <= (minimum.getX() + size.getX() + 2) >> 4; chunkX++) {
                    for (int chunkZ = (minimum.getZ() - 2) >> 4; chunkZ <= (minimum.getZ() + size.getZ() + 2) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
                }
                for (BlockPos relative : expected.keySet()) level.setBlock(fixture.world(relative), Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                level.getEntities(null, new AABB(minimum, minimum.offset(size)).inflate(2)).forEach(entity -> entity.discard());
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Placement failed");
                immediate.add(verify(level, fixture, expected, false));
                negativeProbes(level, fixture);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                fixtures.add(fixture);
                index++;
            }
            output.put("immediate_rotations", immediate);
            output.put("missing_support_negative_probes", 8);
            output.put("blocked_route_negative_probes", 4);
            output.put("stabilization_ticks", 200);
            output.put("chunks_forced_to_tick", true);
            report(output);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> stabilized = new ArrayList<>();
                    for (Fixture fixture : fixtures) stabilized.add(verify(level, fixture, expected, true));
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source changed during test");
                    output.put("rotations", stabilized);
                    output.put("records_checked_after_ticks", 4 * expected.size());
                    output.put("status", "PASS");
                    report(output);
                    System.out.println("ZONE_M03_FORGE_PASS: rotations=4/4 records=27600 ticks=200 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception exception) {
                    output.put("status", "FAIL"); output.put("error", exception.toString());
                    try { report(output); } catch (Exception secondary) { exception.addSuppressed(secondary); }
                    helper.fail(exception.toString());
                }
            });
        } catch (Exception exception) {
            output.put("status", "FAIL"); output.put("error", exception.toString());
            report(output);
            throw exception;
        }
    }
}
