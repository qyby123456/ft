package net.farmingtales.corridorm10;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftcorridorm10")
@GameTestHolder("ftcorridorm10")
@PrefixGameTestTemplate(false)
public final class CorridorM10Verification {
    private static final BlockPos SIZE = new BlockPos(15, 11, 40);
    private static final int VOLUME = 6600;
    private static final int SETTLE_TICKS = 600;
    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           BlockPos minimum, BlockPos maximum) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static void save(Path root, Object report) throws Exception {
        Files.writeString(root.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report), StandardCharsets.UTF_8);
    }

    @GameTest(template = "empty", batch = "corridor_m10_four_rotations", timeoutTicks = 1600)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Isolated GameTestServer required");
        Path root = Path.of(System.getProperty("ftcorridorm10.project")).toRealPath();
        require(root.getFileName().toString().equals("corridor-M10"), "Wrong delivery directory");
        Path source = root.resolve("room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        try {
            CompoundTag data = NbtIo.readCompressed(source.toFile());
            JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
            require(metadata.get("sha256").getAsString().equals(hash), "Fingerprint mismatch");
            require(metadata.get("id").getAsString().equals("corridor-M10"), "Wrong identity");
            require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Java 1.20.1 single palette required");
            require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Entities forbidden");
            List<BlockState> states = new ArrayList<>();
            ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
            for (int index = 0; index < palette.size(); index++) {
                CompoundTag entry = palette.getCompound(index);
                ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
                require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown block: " + identity);
                Block block = BuiltInRegistries.BLOCK.get(identity);
                CompoundTag properties = entry.getCompound("Properties");
                for (String key : properties.getAllKeys()) {
                    Property<?> property = block.getStateDefinition().getProperty(key);
                    require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property: " + entry);
                }
                BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
                require(!state.hasBlockEntity(), "No containers or block entities allowed");
                require(!Set.of(Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.LAVA, Blocks.WATER, Blocks.TNT, Blocks.SPAWNER,
                        Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK,
                        Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW, Blocks.MAGMA_BLOCK,
                        Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW,
                        Blocks.CACTUS, Blocks.WITHER_ROSE, Blocks.SWEET_BERRY_BUSH).contains(state.getBlock()), "Forbidden or hazardous block");
                states.add(state);
            }
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(SIZE), "Wrong size");
            require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == VOLUME, "Roundtrip loss");
            ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
            require(records.size() == VOLUME, "Full explicit volume required");
            List<Fixture> fixtures = new ArrayList<>();
            for (Rotation rotation : Rotation.values()) {
                BlockPos origin = new BlockPos(128 + rotation.ordinal() * 96, 100, 128);
                Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
                for (int index = 0; index < records.size(); index++) {
                    CompoundTag record = records.getCompound(index);
                    ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                    BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                    require(local.getX() >= 0 && local.getX() < 15 && local.getY() >= 0 && local.getY() < 11
                            && local.getZ() >= 0 && local.getZ() < 40, "Out of bounds");
                    require(!record.contains("nbt"), "Inventories and loot forbidden");
                    require(record.getInt("state") >= 0 && record.getInt("state") < states.size(), "Bad palette index");
                    require(expected.put(transform(local, origin, rotation), states.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate coordinate");
                }
                BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
                BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
                BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 10, Math.max(origin.getZ(), corner.getZ()));
                for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
                    for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
                }
                for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                    require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Occupied fixture area");
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true).setFinalizeEntities(false);
                require(template.placeInWorld(level, origin, origin, settings, level.random,
                        Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement failed");
                fixtures.add(new Fixture(rotation, origin, expected, minimum, maximum));
            }
            level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
            List<Map<String, Object>> initial = new ArrayList<>();
            for (Fixture fixture : fixtures) {
                initial.add(inspect(level, fixture));
                for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
                    entry.getValue().updateNeighbourShapes(level, entry.getKey(), Block.UPDATE_ALL);
                    level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
                }
                System.out.println("CORRIDOR_M10_INITIAL_PASS " + fixture.rotation);
            }
            long start = level.getGameTime();
            helper.runAfterDelay(SETTLE_TICKS, () -> {
                try {
                    List<Map<String, Object>> settled = new ArrayList<>();
                    for (Fixture fixture : fixtures) settled.add(inspect(level, fixture));
                    require(level.getGameTime() - start >= SETTLE_TICKS, "Insufficient simulation ticks");
                    Map<String, Object> report = new LinkedHashMap<>();
                    report.put("status", "PASS");
                    report.put("sha256", hash);
                    report.put("minecraft", "1.20.1");
                    report.put("forge", "47.4.10");
                    report.put("test", "Isolated GameTestServer / ServerLevel / StructureTemplate.placeInWorld");
                    report.put("palette_states_validated", states.size());
                    report.put("rotations", 4);
                    report.put("records_per_snapshot", VOLUME * 4);
                    report.put("settle_ticks", level.getGameTime() - start);
                    report.put("initial", initial);
                    report.put("after_600_ticks", settled);
                    report.put("neighbor_updates_triggered", true);
                    report.put("fire_tick_enabled", true);
                    report.put("random_tick_speed", 3);
                    report.put("fixture_chunks_forced", true);
                    report.put("production_save_used", false);
                    report.put("full_modpack_playtest", false);
                    save(root, report);
                    System.out.println("CORRIDOR_M10_NATIVE_PASS rotations=4 ticks=600 sha256=" + hash);
                    helper.succeed();
                } catch (Exception failure) {
                    try { save(root, Map.of("status", "FAIL", "sha256", hash, "error", failure.toString())); }
                    catch (Exception writeFailure) { failure.addSuppressed(writeFailure); }
                    helper.fail(failure.toString());
                }
            });
        } catch (Exception failure) {
            save(root, Map.of("status", "FAIL", "sha256", hash, "error", failure.toString()));
            throw failure;
        }
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int chains = 0;
        int lamps = 0;
        int rails = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "Changed state " + position + " expected=" + entry.getValue() + " actual=" + actual);
            require(actual.getFluidState().isEmpty() && level.getBlockEntity(position) == null, "Fluid or inventory present");
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native support failure " + position + " " + actual);
                supported++;
            }
            if (actual.is(Blocks.CHAIN) || actual.is(Blocks.LANTERN)) {
                BlockPos anchor = position.above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN) && anchor.getY() <= fixture.maximum.getY()) anchor = anchor.above();
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Unanchored hanging block " + position);
                if (actual.is(Blocks.CHAIN)) chains++; else lamps++;
            }
            if (actual.is(Blocks.RAIL) || actual.is(Blocks.GRAVEL)) {
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Unsupported gravity block or rail");
                if (actual.is(Blocks.RAIL)) rails++;
            }
        }
        require(rails == 36 && lamps == 12 && chains == 24, "Theme count mismatch");
        for (int posX = 0; posX < 15; posX++) for (int posZ = 0; posZ < 40; posZ++) {
            for (int posY : new int[]{0, 1, 9, 10}) {
                BlockPos position = transform(new BlockPos(posX, posY, posZ), fixture.origin, fixture.rotation);
                require(Block.isShapeFullBlock(level.getBlockState(position).getCollisionShape(level, position)), "Incomplete floor or roof");
            }
        }
        for (int posZ = 0; posZ < 40; posZ++) for (int posX = 6; posX <= 8; posX++) {
            BlockPos feet = transform(new BlockPos(posX, 2, posZ), fixture.origin, fixture.rotation);
            require(walkable(level, feet) && clear(level, feet.above(2)), "Three-wide three-high route obstructed");
        }
        BlockPos entrance = transform(new BlockPos(7, 2, 0), fixture.origin, fixture.rotation);
        BlockPos exit = transform(new BlockPos(7, 2, 39), fixture.origin, fixture.rotation);
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        reached.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (fixture.expected.containsKey(next) && !reached.contains(next) && walkable(level, next)) {
                    reached.add(next);
                    pending.add(next);
                }
            }
        }
        require(reached.contains(exit) && reached.contains(transform(new BlockPos(7, 2, 3), fixture.origin, fixture.rotation)), "Entrance/interior/exit disconnected");
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Escape outside fixture boundary");
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Dropped block or other entity");
        return Map.of("rotation", fixture.rotation.toString(), "states_compared", VOLUME, "survival_checks", supported,
                "reachable_cells", reached.size(), "lamps", lamps, "chains", chains, "rails", rails,
                "boundary_air_checks", boundary, "route", "PASS", "entities", 0);
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getFluidState().isEmpty() && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        return clear(level, feet) && clear(level, feet.above()) && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP);
    }
}
