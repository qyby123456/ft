package net.farmingtales.m10verification;

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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftm10verification")
@GameTestHolder("ftm10verification")
@PrefixGameTestTemplate(false)
public final class M10Verification {
    private static final BlockPos SIZE = new BlockPos(17, 11, 17);
    private static final BlockPos ENTRANCE = new BlockPos(8, 2, 0);
    private static final BlockPos INTERIOR = new BlockPos(8, 2, 4);
    private static final int VOLUME = 17 * 11 * 17;
    private static final int SETTLE_TICKS = 600;
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CACTUS, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE, Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE, Blocks.WATER);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID,
            Blocks.JIGSAW, Blocks.SPAWNER, Blocks.TNT);
    private static final List<BlockPos> LANDMARKS = List.of(new BlockPos(8, 2, 6), new BlockPos(5, 2, 7),
            new BlockPos(5, 2, 11), new BlockPos(6, 2, 9), new BlockPos(10, 2, 9), new BlockPos(8, 2, 12),
            new BlockPos(4, 2, 12), new BlockPos(12, 2, 12), new BlockPos(12, 2, 7), new BlockPos(13, 2, 6),
            new BlockPos(8, 2, 14));

    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           BlockPos minimum, BlockPos maximum) {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static void save(Path path, Object report) throws Exception {
        Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    @GameTest(template = "empty", batch = "m10_native_placement", timeoutTicks = 1200)
    public static void nativePlacementFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("ftm10.project")).toRealPath();
        require(root.getFileName().toString().equals("FT地牢_M10_熔渣铸造间_20261102"), "Wrong independent project directory");
        Path source = root.resolve("room.nbt");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        require(metadata.get("sha256").getAsString().equals(hash), "Metadata fingerprint mismatch");
        require(metadata.get("id").getAsString().equals("M10"), "Wrong room identity");
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected Java 1.20.1 single palette");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Template entities are forbidden");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
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
            BlockState loaded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(!FORBIDDEN.contains(loaded.getBlock()) && !loaded.hasBlockEntity(), "Forbidden block or block entity: " + loaded);
            states.add(loaded);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        require(template.getSize().equals(SIZE), "Template size mismatch");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == VOLUME, "Roundtrip record loss");
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == VOLUME, "Incomplete template volume");
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(128 + rotation.ordinal() * 64, 100, 128);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getY() >= 0 && local.getZ() >= 0
                        && local.getX() < 17 && local.getY() < 11 && local.getZ() < 17, "Record outside bounds");
                require(record.getInt("state") >= 0 && record.getInt("state") < states.size(), "Invalid state index");
                require(!record.contains("nbt"), "No inventories, loot or block entities allowed");
                BlockPos world = transform(local, origin, rotation);
                require(expected.put(world, states.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate position");
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 10, Math.max(origin.getZ(), corner.getZ()));
            for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) {
                    level.setChunkForced(chunkX, chunkZ, true);
                }
            }
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Fixture area occupied: " + position);
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true).setFinalizeEntities(false);
            require(template.placeInWorld(level, origin, origin, settings, level.random,
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement failed: " + rotation);
            fixtures.add(new Fixture(rotation, origin, expected, minimum, maximum));
        }
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        List<Map<String, Object>> initial = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            initial.add(inspect(level, fixture, true));
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
                if (entry.getValue().is(Blocks.LAVA)) {
                    level.scheduleTick(entry.getKey(), Fluids.LAVA, 1);
                    level.scheduleTick(entry.getKey(), Fluids.FLOWING_LAVA, 1);
                }
                if (entry.getValue().is(Blocks.FIRE)) {
                    level.scheduleTick(entry.getKey(), Blocks.FIRE, 1);
                }
                level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
            }
            System.out.println("M10_NATIVE_INITIAL_PASS: " + fixture.rotation + "; " + initial.get(initial.size() - 1));
        }
        long start = level.getGameTime();
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            try {
                require(level.getGameTime() - start >= SETTLE_TICKS, "Insufficient native simulation ticks");
                List<Map<String, Object>> settled = new ArrayList<>();
                for (Fixture fixture : fixtures) {
                    settled.add(inspect(level, fixture, false));
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("sha256", hash);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("test", "Isolated GameTestServer / ServerLevel / StructureTemplate.placeInWorld");
                result.put("rotations", 4);
                result.put("palette_states_validated", states.size());
                result.put("record_comparisons_per_snapshot", VOLUME * 4);
                result.put("settle_ticks", level.getGameTime() - start);
                result.put("initial", initial);
                result.put("after_600_ticks", settled);
                result.put("fire_tick_enabled", true);
                result.put("random_tick_speed", 3);
                result.put("lava_and_fire_ticks_explicitly_scheduled", true);
                result.put("fixture_chunks_forced", true);
                result.put("production_save_used", false);
                result.put("full_modpack_playtest", false);
                save(root.resolve("qa/native.json"), result);
                System.out.println("M10_NATIVE_GAME_TEST_PASS: rotations=4/4; records=" + (VOLUME * 4) + "; ticks=" + SETTLE_TICKS + "; sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                try {
                    save(root.resolve("qa/native.json"), Map.of("status", "FAIL", "sha256", hash, "error", exception.toString()));
                } catch (Exception writeFailure) {
                    exception.addSuppressed(writeFailure);
                }
                throw new IllegalStateException("M10 native stability verification failed", exception);
            }
        });
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture, boolean initial) {
        int supports = 0;
        int lava = 0;
        int fire = 0;
        int chains = 0;
        int lamps = 0;
        int devices = 0;
        int wallTools = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState expected = entry.getValue();
            BlockState actual = level.getBlockState(position);
            if (!initial && (expected.is(Blocks.FIRE) || expected.is(Blocks.LAVA))) {
                require(actual.is(expected.getBlock()), "Heat source disappeared: " + position);
                if (expected.is(Blocks.LAVA)) {
                    require(actual.getFluidState().isSource(), "Contained lava source changed into flow");
                }
            } else {
                require(actual.equals(expected), "State mismatch: " + position + " / " + expected + " -> " + actual);
            }
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native support failure: " + position + " " + actual);
                for (Direction direction : Direction.values()) {
                    require(!actual.isFlammable(level, position, direction), "Combustible construction: " + position + " " + actual);
                }
                supports++;
            }
            if (actual.is(Blocks.LAVA) || actual.is(Blocks.FIRE)) {
                boolean isLava = actual.is(Blocks.LAVA);
                for (Direction direction : Direction.values()) {
                    BlockPos adjacent = position.relative(direction);
                    BlockState neighbor = level.getBlockState(adjacent);
                    require(neighbor.is(actual.getBlock()) || Block.isShapeFullBlock(neighbor.getCollisionShape(level, adjacent)),
                            "Unsealed heat source: " + position + " " + direction + " " + neighbor);
                }
                if (isLava) {
                    lava++;
                    require(level.getBlockState(position.above()).is(Blocks.GLASS), "Missing full lava cover");
                } else {
                    fire++;
                    require(level.getBlockState(position.below()).is(Blocks.NETHERRACK), "Fire lacks stable netherrack base");
                }
            }
            if (actual.is(Blocks.CHAIN) || actual.is(Blocks.LANTERN)) {
                BlockPos anchor = position.above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN) && anchor.getY() <= fixture.maximum.getY()) {
                    anchor = anchor.above();
                }
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Floating suspension: " + position);
                if (actual.is(Blocks.CHAIN)) {
                    chains++;
                } else {
                    lamps++;
                }
            }
            if (actual.is(Blocks.ANVIL) || actual.is(Blocks.GRINDSTONE) || actual.is(Blocks.RAIL)) {
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Unsupported equipment");
                devices++;
            }
            if (actual.is(Blocks.LEVER) || actual.is(Blocks.TRIPWIRE_HOOK)) {
                Direction facing = actual.getValue(BlockStateProperties.HORIZONTAL_FACING);
                BlockPos anchor = position.relative(facing.getOpposite());
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, facing), "Unsupported wall tool");
                wallTools++;
            }
        }
        require(lava == 6 && fire == 4 && lamps == 3 && chains == 8 && devices == 9 && wallTools == 6, "Theme component count mismatch");
        for (int localX = 0; localX < 17; localX++) {
            for (int localZ = 0; localZ < 17; localZ++) {
                for (int localY : new int[]{0, 1, 10}) {
                    BlockPos position = transform(new BlockPos(localX, localY, localZ), fixture.origin, fixture.rotation);
                    require(Block.isShapeFullBlock(level.getBlockState(position).getCollisionShape(level, position)), "Incomplete floor or roof");
                }
            }
        }
        int perimeter = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Leak, spread or dropped block at boundary");
                perimeter++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Falling block, item or other unexpected entity");
        BlockPos entrance = transform(ENTRANCE, fixture.origin, fixture.rotation);
        BlockPos interior = transform(INTERIOR, fixture.origin, fixture.rotation);
        for (int localX = 7; localX <= 9; localX++) {
            for (int localZ = 0; localZ < 6; localZ++) {
                BlockPos feet = transform(new BlockPos(localX, 2, localZ), fixture.origin, fixture.rotation);
                require(walkable(level, feet) && clear(level, feet.above(2)), "Unsafe three-wide three-high north approach");
            }
        }
        Set<BlockPos> reachable = flood(level, fixture, entrance);
        require(reachable.contains(interior) && flood(level, fixture, interior).contains(entrance), "Entry or return route disconnected");
        for (BlockPos target : LANDMARKS) {
            require(reachable.contains(transform(target, fixture.origin, fixture.rotation)), "Disconnected dry workstation approach: " + target);
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("native_supported_non_air", supports);
        report.put("block_entities", 0);
        report.put("lava_cells", lava);
        report.put("enclosed_fire_cells", fire);
        report.put("anchored_chain_cells", chains);
        report.put("supported_lamps", lamps);
        report.put("supported_devices", devices);
        report.put("supported_wall_tools", wallTools);
        report.put("reachable_dry_feet_positions", reachable.size());
        report.put("dry_landmark_routes", LANDMARKS.size());
        report.put("roof_cells", 289);
        report.put("boundary_air_cells_checked", perimeter);
        report.put("flammable_blocks", 0);
        report.put("unexpected_entities", 0);
        return report;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState actual = level.getBlockState(position);
        return !HAZARDS.contains(actual.getBlock()) && actual.getFluidState().isEmpty()
                && actual.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos floorPosition = feet.below();
        BlockState floor = level.getBlockState(floorPosition);
        return clear(level, feet) && clear(level, feet.above()) && !HAZARDS.contains(floor.getBlock())
                && floor.getFluidState().isEmpty() && floor.isFaceSturdy(level, floorPosition, Direction.UP);
    }

    private static Set<BlockPos> flood(ServerLevel level, Fixture fixture, BlockPos start) {
        require(walkable(level, start), "Unsafe start: " + start);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int height : new int[]{0, 1, -1}) {
                    BlockPos candidate = current.relative(direction).offset(0, height, 0);
                    if (candidate.getX() < fixture.minimum.getX() || candidate.getX() > fixture.maximum.getX()
                            || candidate.getY() < fixture.minimum.getY() || candidate.getY() > fixture.maximum.getY()
                            || candidate.getZ() < fixture.minimum.getZ() || candidate.getZ() > fixture.maximum.getZ()) {
                        continue;
                    }
                    if (height > 0 && !clear(level, current.above(2)) || height < 0 && !clear(level, candidate.above(2))) {
                        continue;
                    }
                    if (walkable(level, candidate) && visited.add(candidate)) {
                        pending.add(candidate);
                    }
                }
            }
        }
        return visited;
    }
}
