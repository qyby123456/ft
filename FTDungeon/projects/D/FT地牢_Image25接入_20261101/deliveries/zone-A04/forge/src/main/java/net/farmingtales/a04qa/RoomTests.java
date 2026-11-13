package net.farmingtales.a04qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fta04qa")
@PrefixGameTestTemplate(false)
public final class RoomTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int COUNT = 3971;
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "structure_void", "jigsaw", "spawner", "tnt", "respawn_anchor");
    private static final List<BlockPos> TARGETS = List.of(new BlockPos(9, 1, 3), new BlockPos(9, 1, 14), new BlockPos(12, 1, 7), new BlockPos(7, 1, 12), new BlockPos(3, 1, 13), new BlockPos(15, 1, 15), new BlockPos(15, 1, 14));
    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos local) { return origin.offset(StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO)); }
        BlockPos minimum() {
            BlockPos result = origin;
            for (BlockPos corner : List.of(new BlockPos(18, 0, 0), new BlockPos(0, 0, 18), new BlockPos(18, 0, 18))) {
                BlockPos position = world(corner);
                result = new BlockPos(Math.min(result.getX(), position.getX()), origin.getY(), Math.min(result.getZ(), position.getZ()));
            }
            return result;
        }
    }
    private static Path root() { return Path.of(System.getProperty("fta04qa.root")).toAbsolutePath().normalize(); }
    private static void require(boolean passed, String message) { if (!passed) throw new IllegalStateException(message); }
    private static String hash(Path file) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))); }
    private static void report(Map<String, Object> result) throws Exception { Files.writeString(root().resolve("qa/native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8); }
    private static BlockPos coordinate(ListTag value) { require(value.size() == 3, "Invalid XYZ"); return new BlockPos(value.getInt(0), value.getInt(1), value.getInt(2)); }
    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identifier = new ResourceLocation(encoded.getString("Name"));
        require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown block " + identifier);
        require(!FORBIDDEN.contains(identifier.getPath()), "Forbidden block " + identifier);
        Block block = BuiltInRegistries.BLOCK.get(identifier);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid state " + identifier + " " + key);
        }
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
    }
    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465, "DataVersion must be 3465");
        require(coordinate(data.getList("size", Tag.TAG_INT)).equals(new BlockPos(19, 11, 19)), "Wrong size");
        require(!data.contains("palettes") && data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected entities/palettes");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) states.add(decode(palette.getCompound(index)));
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        int containers = 0;
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag block = blocks.getCompound(index);
            BlockPos position = coordinate(block.getList("pos", Tag.TAG_INT));
            require(position.getX() >= 0 && position.getX() < 19 && position.getY() >= 0 && position.getY() < 11 && position.getZ() >= 0 && position.getZ() < 19, "Out of bounds");
            int stateIndex = block.getInt("state");
            require(stateIndex >= 0 && stateIndex < states.size(), "Bad state index");
            BlockState state = states.get(stateIndex);
            require(expected.put(position, state) == null, "Duplicate voxel");
            if (state.hasBlockEntity()) {
                require(block.contains("nbt", Tag.TAG_COMPOUND), "Missing container NBT");
                CompoundTag nbt = block.getCompound("nbt");
                require(nbt.contains("Items") && nbt.getList("Items", Tag.TAG_COMPOUND).isEmpty() && !nbt.contains("LootTable"), "Container loot");
                containers++;
            } else require(!block.contains("nbt"), "Unexpected block NBT");
        }
        require(expected.size() == COUNT && containers == 5, "Wrong full volume/container count");
        return expected;
    }
    private static boolean clear(ServerLevel level, BlockPos position) { BlockState state = level.getBlockState(position); return state.getFluidState().isEmpty() && state.getCollisionShape(level, position).isEmpty(); }
    private static boolean floor(ServerLevel level, BlockPos position) { BlockState state = level.getBlockState(position); return state.getFluidState().isEmpty() && state.isFaceSturdy(level, position, Direction.UP); }
    private static boolean walkable(ServerLevel level, BlockPos feet) { return clear(level, feet) && clear(level, feet.above()) && floor(level, feet.below()); }
    private static boolean emptyVolume(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected) {
        for (BlockPos local : expected.keySet()) if (!level.getBlockState(fixture.world(local)).isAir()) return false;
        return true;
    }
    private static boolean emptyContainer(BlockEntity entity) {
        if (!(entity instanceof Container container) || !container.isEmpty()) return false;
        CompoundTag data = entity.saveWithoutMetadata();
        return !data.contains("LootTable") && data.getList("Items", Tag.TAG_COMPOUND).isEmpty();
    }
    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, boolean capture) throws Exception {
        int containers = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos local = entry.getKey();
            BlockPos position = fixture.world(local);
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + local + " actual=" + actual);
            require(actual.canSurvive(level, position), "Cannot survive " + local);
            require(actual.getFluidState().isEmpty(), "Unexpected fluid " + local);
            BlockEntity entity = level.getBlockEntity(position);
            if (actual.hasBlockEntity()) { require(emptyContainer(entity), "Nonempty/missing container " + local); containers++; }
            else require(entity == null, "Unexpected block entity " + local);
            if (local.getY() == 1 && !actual.isAir()) require(floor(level, position.below()), "Unsupported floor prop " + local);
        }
        require(containers == 5, "Wrong containers after placement");
        for (int east = 0; east < 19; east++) for (int south = 0; south < 19; south++) {
            require(floor(level, fixture.world(new BlockPos(east, 0, south))), "Floor hole");
            BlockPos roof = fixture.world(new BlockPos(east, 10, south));
            require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Roof hole");
        }
        for (int east = 8; east <= 10; east++) for (int south = 0; south <= 14; south++) {
            BlockPos feet = fixture.world(new BlockPos(east, 1, south));
            require(walkable(level, feet) && clear(level, feet.above(2)), "Three-wide/three-high route blocked");
        }
        for (int east : List.of(4, 14)) for (int south : List.of(5, 13)) for (int up = 1; up <= 8; up++) require(!level.getBlockState(fixture.world(new BlockPos(east, up, south))).isAir(), "Disconnected pillar");
        for (int east : List.of(6, 12)) {
            for (int up = 6; up <= 7; up++) require(level.getBlockState(fixture.world(new BlockPos(east, up, 9))).is(Blocks.CHAIN), "Lantern chain gap");
            BlockPos anchor = fixture.world(new BlockPos(east, 8, 9));
            require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Lantern anchor missing");
        }
        for (int east : List.of(6, 8, 10)) for (int up = 3; up <= 5; up++) require(!level.getBlockState(fixture.world(new BlockPos(east, up, 16))).isAir(), "Tool rack disconnected");
        for (int east = 5; east <= 12; east++) for (int south = 16; south <= 17; south++) require(floor(level, fixture.world(new BlockPos(east, 1, south))), "Worktop lacks full base");
        require(clear(level, fixture.world(new BlockPos(15, 2, 16))), "Chest lid blocked");
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(new BlockPos(9, 1, 0));
        while (!queue.isEmpty()) {
            BlockPos local = queue.removeFirst();
            if (visited.contains(local) || local.getX() < 1 || local.getX() > 17 || local.getZ() < 0 || local.getZ() > 17 || !walkable(level, fixture.world(local))) continue;
            visited.add(local);
            for (Direction direction : Direction.Plane.HORIZONTAL) queue.add(local.relative(direction));
        }
        for (BlockPos target : TARGETS) require(visited.contains(target), "Unreachable station/safety point " + target);
        BlockPos minimum = fixture.minimum();
        require(level.getEntities(null, new AABB(minimum, minimum.offset(19, 11, 19))).isEmpty(), "Unexpected room entities/dropped props");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records", COUNT);
        result.put("empty_containers", containers);
        result.put("reachable_level_feet", visited.size());
        result.put("reachable_targets", TARGETS.size());
        result.put("native_support_collision_roof_route", "PASS");
        if (capture) {
            StructureTemplate saved = new StructureTemplate();
            saved.fillFromWorld(level, minimum, new Vec3i(19, 11, 19), false, Blocks.STRUCTURE_VOID);
            CompoundTag captured = saved.save(new CompoundTag());
            Path output = root().resolve("qa/placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(captured, output.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(output.toFile()));
            require(reloaded.getSize().equals(new Vec3i(19, 11, 19)), "Roundtrip size changed");
            ListTag capturedBlocks = captured.getList("blocks", Tag.TAG_COMPOUND);
            ListTag capturedPalette = captured.getList("palette", Tag.TAG_COMPOUND);
            require(capturedBlocks.size() == COUNT, "Roundtrip count changed");
            Map<BlockPos, BlockState> savedStates = new LinkedHashMap<>();
            for (int index = 0; index < capturedBlocks.size(); index++) {
                CompoundTag encoded = capturedBlocks.getCompound(index);
                savedStates.put(coordinate(encoded.getList("pos", Tag.TAG_INT)), decode(capturedPalette.getCompound(encoded.getInt("state"))));
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(savedStates.get(fixture.world(entry.getKey()).subtract(minimum))), "Roundtrip state differs");
            result.put("roundtrip_states_match", true);
            result.put("capture", root().relativize(output).toString());
            result.put("capture_sha256", hash(output));
        }
        return result;
    }
    @GameTest(template = "empty", timeoutTicks = 700)
    public static void fourRotations(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "RUNNING");
        result.put("runtime", "Forge 47.4.10 / Minecraft Java 1.20.1");
        Path source = root().resolve("room.nbt");
        String fingerprint = hash(source);
        result.put("source_sha256", fingerprint);
        try {
            CompoundTag data = NbtIo.readCompressed(source.toFile());
            Map<BlockPos, BlockState> expected = audit(data);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            ServerLevel level = helper.getLevel();
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(false, level.getServer());
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> before = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(new BlockPos(320 + index * 64, 100, 320), rotation);
                BlockPos minimum = fixture.minimum();
                for (int chunkEast = (minimum.getX() - 2) >> 4; chunkEast <= (minimum.getX() + 21) >> 4; chunkEast++) for (int chunkSouth = (minimum.getZ() - 2) >> 4; chunkSouth <= (minimum.getZ() + 21) >> 4; chunkSouth++) level.setChunkForced(chunkEast, chunkSouth, true);
                require(emptyVolume(level, fixture, expected), "Fresh isolated volume occupied");
                BlockPos sentinel = fixture.world(new BlockPos(9, 5, 6));
                level.setBlock(sentinel, Blocks.GOLD_BLOCK.defaultBlockState(), FLAGS);
                require(!emptyVolume(level, fixture, expected), "Full-volume occupancy probe missed");
                level.setBlock(sentinel, Blocks.AIR.defaultBlockState(), FLAGS);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false), level.random, FLAGS), "Placement failed");
                before.add(verify(level, fixture, expected, false));
                BlockPos chain = fixture.world(new BlockPos(6, 6, 9));
                BlockState previous = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lantern = fixture.world(new BlockPos(6, 5, 9));
                require(!level.getBlockState(lantern).canSurvive(level, lantern), "Missing lamp support probe missed");
                level.setBlock(chain, previous, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos route = fixture.world(new BlockPos(9, 1, 3));
                level.setBlock(route, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, route), "Blocked safety point probe missed");
                level.setBlock(route, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos ground = fixture.world(new BlockPos(9, 0, 3));
                previous = level.getBlockState(ground);
                level.setBlock(ground, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, route), "Missing floor probe missed");
                level.setBlock(ground, previous, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockEntity chest = level.getBlockEntity(fixture.world(new BlockPos(15, 1, 16)));
                ((Container) chest).setItem(0, new ItemStack(Items.STONE));
                require(!emptyContainer(chest), "Nonempty inventory probe missed");
                ((Container) chest).clearContent();
                for (BlockPos local : expected.keySet()) {
                    BlockPos position = fixture.world(local);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                fixtures.add(fixture);
                index++;
            }
            result.put("before_ticks", before);
            result.put("stability_ticks", 240);
            result.put("chunks_forced_to_tick", true);
            result.put("negative_probes_per_rotation", List.of("occupied_air_cell", "missing_chain", "blocked_safety_point", "missing_floor", "nonempty_inventory"));
            report(result);
            helper.runAfterDelay(240, () -> {
                try {
                    List<Map<String, Object>> after = new ArrayList<>();
                    for (Fixture fixture : fixtures) after.add(verify(level, fixture, expected, true));
                    require(hash(source).equals(fingerprint), "Source changed");
                    result.put("rotations", after);
                    result.put("records_checked_after_ticks", COUNT * after.size());
                    result.put("source_unchanged", true);
                    result.put("status", "PASS");
                    report(result);
                    System.out.println("A04_NATIVE_PASS rotations=4/4 records=15884 ticks=240 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception failure) {
                    result.put("status", "FAIL");
                    result.put("error", failure.toString());
                    try { report(result); } catch (Exception secondary) { failure.addSuppressed(secondary); }
                    throw new IllegalStateException(failure);
                }
            });
        } catch (Exception failure) {
            result.put("status", "FAIL");
            result.put("error", failure.toString());
            report(result);
            throw failure;
        }
    }
}
