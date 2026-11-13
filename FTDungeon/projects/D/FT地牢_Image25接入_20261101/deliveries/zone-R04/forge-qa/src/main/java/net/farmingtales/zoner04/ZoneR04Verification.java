package net.farmingtales.zoner04;

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

@Mod("ftzoner04")
@GameTestHolder("ftzoner04")
@PrefixGameTestTemplate(false)
public final class ZoneR04Verification {
    private static final BlockPos SIZE = new BlockPos(25, 13, 25);
    private static final int VOLUME = 8125;
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

    @GameTest(template = "empty", batch = "zone_r04_four_rotations", timeoutTicks = 1600)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Isolated GameTestServer required");
        Path root = Path.of(System.getProperty("ftzoner04.project")).toRealPath();
        require(root.getFileName().toString().equals("zone-R04"), "Wrong delivery directory");
        Path source = root.resolve("room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        try {
            CompoundTag data = NbtIo.readCompressed(source.toFile());
            JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
            require(metadata.get("sha256").getAsString().equals(hash), "Fingerprint mismatch");
            require(metadata.get("id").getAsString().equals("zone-R04"), "Wrong identity");
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
                    require(local.getX() >= 0 && local.getX() < 25 && local.getY() >= 0 && local.getY() < 13
                            && local.getZ() >= 0 && local.getZ() < 25, "Out of bounds");
                    require(!record.contains("nbt"), "Inventories and loot forbidden");
                    require(record.getInt("state") >= 0 && record.getInt("state") < states.size(), "Bad palette index");
                    require(expected.put(transform(local, origin, rotation), states.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate coordinate");
                }
                BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
                BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
                BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 12, Math.max(origin.getZ(), corner.getZ()));
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
                System.out.println("ZONE_R04_INITIAL_PASS " + fixture.rotation);
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
                    System.out.println("ZONE_R04_NATIVE_PASS rotations=4 ticks=600 sha256=" + hash);
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
        int ladders = 0;
        int carpets = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "Changed state " + position + " expected=" + entry.getValue() + " actual=" + actual);
            require(actual.getFluidState().isEmpty() && level.getBlockEntity(position) == null, "Fluid or block entity present");
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native support failure " + position + " " + actual);
                supported++;
            }
            if (actual.is(Blocks.CHAIN) || actual.is(Blocks.LANTERN) && actual.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HANGING)) {
                BlockPos anchor = position.above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN) && anchor.getY() <= fixture.maximum.getY()) anchor = anchor.above();
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Unanchored hanging block " + position);
                if (actual.is(Blocks.CHAIN)) chains++;
            }
            if (actual.is(Blocks.LANTERN)) lamps++;
            if (actual.is(Blocks.LADDER)) ladders++;
            if (actual.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock) carpets++;
        }
        require(lamps == 6 && chains == 8 && ladders == 10 && carpets == 31, "Theme count mismatch lamps=" + lamps + " chains=" + chains + " ladders=" + ladders + " carpets=" + carpets);
        for (int posX = 0; posX < 25; posX++) for (int posZ = 0; posZ < 25; posZ++) {
            for (int posY : new int[]{0, 1, 11}) {
                BlockPos position = transform(new BlockPos(posX, posY, posZ), fixture.origin, fixture.rotation);
                require(Block.isShapeFullBlock(level.getBlockState(position).getCollisionShape(level, position)), "Incomplete floor or sealed roof");
            }
        }
        for (int posZ = 0; posZ < 25; posZ++) for (int posX = 11; posX <= 13; posX++) {
            BlockPos feet = transform(new BlockPos(posX, 2, posZ), fixture.origin, fixture.rotation);
            require(walkable(level, feet) && clear(level, feet.above(2)), "Three-wide three-high route obstructed");
        }
        for (int[] pillar : new int[][]{{6,6},{17,6},{6,17},{17,17}}) {
            for (int posX = pillar[0]; posX <= pillar[0] + 1; posX++) for (int posZ = pillar[1]; posZ <= pillar[1] + 1; posZ++) {
                for (int posY = 0; posY <= 11; posY++) {
                    BlockPos position = transform(new BlockPos(posX, posY, posZ), fixture.origin, fixture.rotation);
                    require(Block.isShapeFullBlock(level.getBlockState(position).getCollisionShape(level, position)), "Pillar load path interrupted");
                }
            }
        }
        for (int[] beam : new int[][]{{3,2,4},{4,2,4},{5,3,4},{6,3,4},{7,4,4},{18,2,20},{19,2,20},{20,3,20},{21,3,20},{22,4,20}}) {
            for (int posY = 0; posY <= beam[1]; posY++) {
                BlockPos position = transform(new BlockPos(beam[0], posY, beam[2]), fixture.origin, fixture.rotation);
                require(Block.isShapeFullBlock(level.getBlockState(position).getCollisionShape(level, position)), "Fallen beam is unsupported");
            }
        }
        for (int[] bunk : new int[][]{{2,4},{20,22}}) {
            for (int posX : bunk) for (int posZ : new int[]{9,15}) for (int posY = 2; posY <= 7; posY++) {
                BlockPos position = transform(new BlockPos(posX, posY, posZ), fixture.origin, fixture.rotation);
                require(level.getBlockState(position).is(Blocks.DARK_OAK_LOG), "Bunk support post missing");
            }
            for (int posX = bunk[0]; posX <= bunk[1]; posX++) for (int posZ = 10; posZ <= 14; posZ++) for (int posY : new int[]{2,5}) {
                BlockPos position = transform(new BlockPos(posX, posY, posZ), fixture.origin, fixture.rotation);
                require(level.getBlockState(position).is(Blocks.DARK_OAK_SLAB) && level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Unsupported bunk deck or rag");
            }
        }
        BlockPos entrance = transform(new BlockPos(12, 2, 0), fixture.origin, fixture.rotation);
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
        for (BlockPos target : List.of(new BlockPos(12,2,24), new BlockPos(12,2,5), new BlockPos(12,2,12), new BlockPos(5,2,12), new BlockPos(19,2,12))) {
            require(reached.contains(transform(target, fixture.origin, fixture.rotation)), "Entrance/safe point/exit/bunk aisle disconnected");
        }
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Escape outside fixture boundary");
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Dropped block or other entity");
        return Map.of("rotation", fixture.rotation.toString(), "states_compared", VOLUME, "survival_checks", supported,
                "reachable_cells", reached.size(), "lamps", lamps, "chains", chains, "ladders", ladders,
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
