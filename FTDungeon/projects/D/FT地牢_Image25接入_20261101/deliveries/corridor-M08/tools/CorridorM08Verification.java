package net.farmingtales.corridorm08qa;

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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftcorridorm08qa")
@GameTestHolder("ftcorridorm08qa")
@PrefixGameTestTemplate(false)
public final class CorridorM08Verification {
    private static final BlockPos SIZE = new BlockPos(17, 13, 41);
    private static final int VOLUME = 17 * 13 * 41;
    private static final int SETTLE_TICKS = 600;
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CACTUS, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE, Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE, Blocks.WATER);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID,
            Blocks.JIGSAW, Blocks.SPAWNER, Blocks.TNT);

    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           BlockPos minimum, BlockPos maximum) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static void save(Path path, Object report) throws Exception {
        Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    @GameTest(template = "empty", batch = "corridor_m08_stability", timeoutTicks = 1400)
    public static void nativePlacementFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("ftcorridorm08.project")).toRealPath();
        require(root.getFileName().toString().equals("corridor-M08"), "Wrong independent project root");
        Path source = root.resolve("room.nbt");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        require(metadata.get("sha256").getAsString().equals(hash), "Metadata fingerprint mismatch");
        require(metadata.get("id").getAsString().equals("corridor-M08"), "Wrong corridor identity");
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
            require(!FORBIDDEN.contains(loaded.getBlock()) && !loaded.hasBlockEntity(), "Forbidden block or inventory");
            states.add(loaded);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        require(template.getSize().equals(SIZE), "Template dimensions mismatch");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == VOLUME, "Native roundtrip record loss");
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == VOLUME, "Template must contain the explicit complete volume");
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(128 + rotation.ordinal() * 96, 300, 128);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                require(coordinates.size() == 3, "Invalid coordinate tuple");
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getY() >= 0 && local.getZ() >= 0
                        && local.getX() < 17 && local.getY() < 13 && local.getZ() < 41, "Record outside bounds");
                require(record.getInt("state") >= 0 && record.getInt("state") < states.size(), "Invalid state index");
                require(!record.contains("nbt"), "No block entities or inventories allowed");
                BlockPos world = transform(local, origin, rotation);
                require(expected.put(world, states.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate coordinate");
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 12, Math.max(origin.getZ(), corner.getZ()));
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
        List<Map<String, Object>> initial = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            initial.add(inspect(level, fixture));
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
                if (entry.getValue().is(Blocks.LAVA)) {
                    level.scheduleTick(entry.getKey(), Fluids.LAVA, 1);
                    level.scheduleTick(entry.getKey(), Fluids.FLOWING_LAVA, 1);
                }
                level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
            }
            System.out.println("CORRIDOR_M08_INITIAL_PASS: " + fixture.rotation + "; " + initial.get(initial.size() - 1));
        }
        long start = level.getGameTime();
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            try {
                require(level.getGameTime() - start >= SETTLE_TICKS, "Insufficient native simulation ticks");
                List<Map<String, Object>> settled = new ArrayList<>();
                for (Fixture fixture : fixtures) settled.add(inspect(level, fixture));
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("sha256", hash);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("test", "Isolated Forge GameTestServer / ServerLevel / StructureTemplate.placeInWorld");
                result.put("rotations", 4);
                result.put("palette_states_validated", states.size());
                result.put("record_comparisons_per_snapshot", VOLUME * 4);
                result.put("settle_ticks", level.getGameTime() - start);
                result.put("initial", initial);
                result.put("after_600_ticks", settled);
                result.put("fire_tick_enabled", true);
                result.put("random_tick_speed", 3);
                result.put("lava_ticks_explicitly_scheduled", true);
                result.put("fixture_chunks_forced", true);
                result.put("production_save_used", false);
                result.put("full_modpack_playtest", false);
                save(root.resolve("qa/native.json"), result);
                System.out.println("CORRIDOR_M08_GAME_TEST_PASS: rotations=4/4; records=" + (VOLUME * 4) + "; ticks=" + SETTLE_TICKS + "; sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                try {
                    save(root.resolve("qa/native.json"), Map.of("status", "FAIL", "sha256", hash, "error", exception.toString()));
                } catch (Exception writeFailure) { exception.addSuppressed(writeFailure); }
                throw new IllegalStateException("M08 stability verification failed", exception);
            }
        });
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supports = 0;
        int lava = 0;
        int anvils = 0;
        int chains = 0;
        int lamps = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed: " + fixture.rotation + " " + position + "; expected=" + entry.getValue() + "; actual=" + actual);
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (actual.isAir()) continue;
            require(actual.canSurvive(level, position), "Unsupported block: " + position);
            require(!actual.isFlammable(level, position, Direction.UP), "Flammable structural material: " + actual + " at " + position);
            supports++;
            if (actual.is(Blocks.LAVA)) {
                lava++;
                require(level.getFluidState(position).isSource(), "Lava ceased to be a source");
                for (Direction direction : Direction.values()) {
                    BlockPos neighbor = position.relative(direction);
                    require(level.getBlockState(neighbor).is(Blocks.LAVA)
                            || Block.isShapeFullBlock(level.getBlockState(neighbor).getCollisionShape(level, neighbor)), "Lava is not fully enclosed: " + neighbor);
                }
            }
            if (actual.is(Blocks.ANVIL)) {
                anvils++;
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Unsupported anvil");
            }
            if (actual.is(Blocks.CHAIN)) {
                chains++;
                require(Block.isShapeFullBlock(level.getBlockState(position.above()).getCollisionShape(level, position.above())), "Unanchored hanging cage");
                require(level.getBlockState(position.below()).is(Blocks.IRON_BLOCK), "Cage suspension plate missing");
            }
            if (actual.is(Blocks.LANTERN)) {
                lamps++;
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Unsupported lantern");
            }
        }
        require(lava == 142 && anvils == 8 && chains == 8 && lamps == 8, "Theme fixture count mismatch");
        int perimeter = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary fluid or block leak");
                perimeter++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Unexpected falling block, item or mob");
        int laneCells = 0;
        for (int depth = 0; depth < 41; depth++) {
            for (int column = 6; column <= 10; column++) {
                BlockPos feet = transform(new BlockPos(column, 3, depth), fixture.origin, fixture.rotation);
                require(walkable(level, feet) && clear(level, feet.above(2)), "Five-wide flat corridor is obstructed");
                laneCells++;
            }
            for (int height = 3; height <= 10; height++) {
                for (int column : new int[]{0, 16}) {
                    BlockPos wall = transform(new BlockPos(column, height, depth), fixture.origin, fixture.rotation);
                    require(Block.isShapeFullBlock(level.getBlockState(wall).getCollisionShape(level, wall)), "Side opening or branch");
                }
            }
            for (int column = 0; column < 17; column++) {
                for (int height : new int[]{0, 12}) {
                    BlockPos shell = transform(new BlockPos(column, height, depth), fixture.origin, fixture.rotation);
                    require(Block.isShapeFullBlock(level.getBlockState(shell).getCollisionShape(level, shell)), "Foundation or roof hole");
                }
            }
        }
        BlockPos entrance = transform(new BlockPos(8, 3, 0), fixture.origin, fixture.rotation);
        BlockPos interior = transform(new BlockPos(8, 3, 3), fixture.origin, fixture.rotation);
        BlockPos exit = transform(new BlockPos(8, 3, 40), fixture.origin, fixture.rotation);
        Set<BlockPos> forward = flood(level, fixture, entrance);
        Set<BlockPos> backward = flood(level, fixture, exit);
        require(forward.contains(interior) && forward.contains(exit) && backward.contains(entrance), "Entry/exit/return disconnected");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("native_supported_non_air", supports);
        report.put("lava_cells", lava);
        report.put("fully_enclosed_lava", true);
        report.put("supported_anvils", anvils);
        report.put("anchored_empty_cages", chains);
        report.put("supported_lanterns", lamps);
        report.put("straight_safe_lane_cells", laneCells);
        report.put("reachable_dry_feet_positions", forward.size());
        report.put("round_trip_both_ports", true);
        report.put("roof_cells", 697);
        report.put("boundary_air_cells_checked", perimeter);
        report.put("unexpected_entities", 0);
        return report;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return !HAZARDS.contains(state.getBlock()) && state.getFluidState().isEmpty()
                && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos floor = feet.below();
        BlockState state = level.getBlockState(floor);
        return clear(level, feet) && clear(level, feet.above()) && !HAZARDS.contains(state.getBlock())
                && state.getFluidState().isEmpty() && state.isFaceSturdy(level, floor, Direction.UP);
    }

    private static Set<BlockPos> flood(ServerLevel level, Fixture fixture, BlockPos start) {
        require(walkable(level, start), "Unsafe route start");
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = current.relative(direction);
                if (candidate.getX() < fixture.minimum.getX() || candidate.getX() > fixture.maximum.getX()
                        || candidate.getZ() < fixture.minimum.getZ() || candidate.getZ() > fixture.maximum.getZ()) continue;
                if (walkable(level, candidate) && visited.add(candidate)) pending.add(candidate);
            }
        }
        return visited;
    }
}
