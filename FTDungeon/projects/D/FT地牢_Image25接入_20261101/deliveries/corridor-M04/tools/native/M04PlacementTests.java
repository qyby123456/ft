package net.farmingtales.m04qa;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
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

@Mod("ftm04qa")
@GameTestHolder("ftm04qa")
@PrefixGameTestTemplate(false)
public final class M04PlacementTests {
    private static final Path ROOT = Path.of(System.getProperty("ftm04qa.root"));
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "spawner", "end_portal", "nether_portal", "end_gateway", "lava", "fire", "soul_fire", "cobweb", "powder_snow");

    public M04PlacementTests() {}

    private static <Value extends Comparable<Value>> String propertyValue(BlockState state, Property<Value> property) {
        return property.getName(state.getValue(property));
    }

    private static BlockState strictState(GameTestHelper helper, CompoundTag palette) {
        ResourceLocation identity = new ResourceLocation(palette.getString("Name"));
        helper.assertTrue(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown or non-vanilla block " + identity);
        helper.assertTrue(!FORBIDDEN.contains(identity.getPath()) && !identity.getPath().endsWith("_stairs"), "Forbidden block " + identity);
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette);
        CompoundTag properties = palette.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(key);
            helper.assertTrue(property != null && propertyValue(state, property).equals(properties.getString(key)), "Invalid block property " + identity + " " + key);
        }
        return state;
    }

    private static BlockPos world(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        AABB body = new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8);
        return level.noCollision(body) && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty()
                && level.getFluidState(feet.below()).isEmpty() && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP);
    }

    private static Map<String, Object> verifyWorld(GameTestHelper helper, Map<BlockPos, BlockState> states, BlockPos origin, Rotation rotation, String phase) {
        ServerLevel level = helper.getLevel();
        int lamps = 0;
        int rods = 0;
        int cageBars = 0;
        int water = 0;
        int supportedVines = 0;
        int naturalVineChanges = 0;
        for (Map.Entry<BlockPos, BlockState> entry : states.entrySet()) {
            BlockPos position = world(entry.getKey(), origin, rotation);
            BlockState expected = entry.getValue().rotate(rotation);
            BlockState actual = level.getBlockState(position);
            if (!actual.equals(expected)) {
                boolean naturalVine = (expected.isAir() || expected.is(Blocks.VINE)) && actual.is(Blocks.VINE) && (entry.getKey().getX() == 1 || entry.getKey().getX() == 11);
                helper.assertTrue(naturalVine, phase + " state mismatch " + rotation + " " + entry.getKey() + " expected=" + expected + " actual=" + actual);
                naturalVineChanges++;
            }
            helper.assertTrue(actual.canSurvive(level, position), phase + " unsupported block " + actual + " " + entry.getKey());
            helper.assertTrue(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (actual.is(Blocks.WATER)) water++;
            if (actual.is(Blocks.LANTERN)) {
                lamps++;
                helper.assertTrue(level.getBlockState(position.above()).is(Blocks.CHAIN), "Lantern without a chain");
            }
            if (actual.is(Blocks.END_ROD)) rods++;
            if (actual.is(Blocks.IRON_BARS)) cageBars++;
            if (actual.is(Blocks.VINE)) supportedVines++;
        }
        helper.assertTrue(water == 222 && lamps == 4 && rods == 20 && cageBars == 64, "Theme or fluid block count changed");
        int dryCells = 0;
        for (int depth = -3; depth <= 43; depth++) {
            for (int width = 5; width <= 7; width++) {
                BlockPos position = world(new BlockPos(width, 2, depth), origin, rotation);
                helper.assertTrue(walkable(level, position), "Player collision or fluid on straight route " + rotation + " z=" + depth + " x=" + width);
                dryCells++;
            }
        }
        int checkedBoundary = 0;
        for (int width = -1; width <= 13; width++) {
            for (int depth = -1; depth <= 41; depth++) {
                for (int height = -1; height <= 12; height++) {
                    BlockPos position = world(new BlockPos(width, height, depth), origin, rotation);
                    if (!level.getFluidState(position).isEmpty()) {
                        boolean basin = height == 1 && depth >= 2 && depth <= 38 && (width >= 2 && width <= 4 || width >= 8 && width <= 10);
                        helper.assertTrue(basin && level.getFluidState(position).isSource(), "Fluid leaked or changed level " + position);
                    }
                    checkedBoundary++;
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", rotation.name());
        result.put("origin", List.of(origin.getX(), origin.getY(), origin.getZ()));
        result.put("phase", phase);
        result.put("status", "PASS");
        result.put("compared_template_records", states.size());
        result.put("structural_state_mismatches", 0);
        result.put("allowed_natural_wall_vine_changes", naturalVineChanges);
        result.put("can_survive_failures", 0);
        result.put("dry_player_collision_cells_with_approaches", dryCells);
        result.put("template_dry_cells", 123);
        result.put("player_aabb", List.of(0.6, 1.8, 0.6));
        result.put("source_water_blocks", water);
        result.put("fluid_boundary_scanned_cells", checkedBoundary);
        result.put("leaked_fluid_blocks", 0);
        result.put("supported_lanterns", lamps);
        result.put("end_rods", rods);
        result.put("cage_bars", cageBars);
        result.put("supported_vines", supportedVines);
        result.put("block_entities", 0);
        return result;
    }

    @GameTest(template = "empty", batch = "m04_native", timeoutTicks = 800)
    public static void straightCorridorFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(level.getServer() instanceof GameTestServer, "Only an isolated disposable GameTest server is permitted");
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        helper.assertTrue(level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING) == 3, "Keep default vegetation random ticks for honest stability evidence");
        Path source = ROOT.resolve("room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        String declaredHash = JsonParser.parseString(Files.readString(ROOT.resolve("room.json"))).getAsJsonObject().get("sha256").getAsString();
        helper.assertTrue(hash.equals(declaredHash), "Structure fingerprint mismatch");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        helper.assertTrue(data.getInt("DataVersion") == 3465 && data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Wrong version or entity payload");
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : data.getList("palette", Tag.TAG_COMPOUND)) palette.add(strictState(helper, (CompoundTag) entry));
        Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        for (Tag entry : data.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            helper.assertTrue(local.getX() >= 0 && local.getX() < 13 && local.getY() >= 0 && local.getY() < 12 && local.getZ() >= 0 && local.getZ() < 41, "Out-of-bounds coordinate");
            helper.assertTrue(!states.containsKey(local) && !record.contains("nbt"), "Duplicate coordinate or block entity");
            states.put(local, palette.get(record.getInt("state")));
        }
        helper.assertTrue(states.size() == 6396, "Template must record the full bounded volume");
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        CompoundTag roundtrip = template.save(new CompoundTag());
        helper.assertTrue(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == states.size(), "Template roundtrip lost records");
        List<Rotation> rotations = List.of(Rotation.NONE, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90);
        List<BlockPos> origins = new ArrayList<>();
        List<Map<String, Object>> immediate = new ArrayList<>();
        for (int index = 0; index < rotations.size(); index++) {
            Rotation rotation = rotations.get(index);
            BlockPos origin = helper.absolutePos(new BlockPos(96 + index * 96, 260, 96));
            origins.add(origin);
            BlockPos firstCorner = world(new BlockPos(-1, 0, -4), origin, rotation);
            BlockPos lastCorner = world(new BlockPos(13, 0, 44), origin, rotation);
            for (int chunkX = Math.min(firstCorner.getX(), lastCorner.getX()) >> 4; chunkX <= Math.max(firstCorner.getX(), lastCorner.getX()) >> 4; chunkX++) {
                for (int chunkZ = Math.min(firstCorner.getZ(), lastCorner.getZ()) >> 4; chunkZ <= Math.max(firstCorner.getZ(), lastCorner.getZ()) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
            }
            for (int width = -1; width <= 13; width++) {
                for (int depth = -4; depth <= 44; depth++) {
                    for (int height = -1; height <= 12; height++) {
                        level.setBlock(world(new BlockPos(width, height, depth), origin, rotation), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }
            for (int width = -4; width <= 16; width++) {
                for (int depth = -7; depth <= 47; depth++) {
                    for (int height = -2; height <= 13; height++) helper.assertTrue(level.getFluidState(world(new BlockPos(width, height, depth), origin, rotation)).isEmpty(), "Ambient terrain fluid in test fixture");
                }
            }
            System.out.println("M04_PLACING rotation=" + rotation + " origin=" + origin + " ambientFluidBuffer=0");
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true);
            helper.assertTrue(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_ALL), "First placement failed " + rotation);
            helper.assertTrue(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_ALL), "Repeat placement failed " + rotation);
            for (int width = 5; width <= 7; width++) {
                for (int depth = -3; depth <= 43; depth++) {
                    if (depth < 0 || depth > 40) level.setBlock(world(new BlockPos(width, 1, depth), origin, rotation), Blocks.END_STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            for (BlockPos local : states.keySet()) {
                BlockPos position = world(local, origin, rotation);
                level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                if (!level.getFluidState(position).isEmpty()) level.scheduleTick(position, level.getFluidState(position).getType(), 1);
            }
            immediate.add(verifyWorld(helper, states, origin, rotation, "repeat_placement_and_neighbor_updates"));
            System.out.println("M04_IMMEDIATE_PASS rotation=" + rotation);
        }
        long placementTime = level.getGameTime();
        helper.runAfterDelay(200, () -> {
            try {
                helper.assertTrue(level.getGameTime() - placementTime >= 200, "Insufficient elapsed native server ticks");
                List<Map<String, Object>> delayed = new ArrayList<>();
                for (int index = 0; index < rotations.size(); index++) delayed.add(verifyWorld(helper, states, origins.get(index), rotations.get(index), "after_200_server_ticks"));
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("id", "corridor-M04");
                result.put("minecraft", SharedConstants.getCurrentVersion().getName());
                result.put("forge", net.minecraftforge.versions.forge.ForgeVersion.getVersion());
                result.put("structure_sha256", hash);
                result.put("palette_states_strictly_validated", palette.size());
                result.put("roundtrip_records", states.size());
                result.put("actual_world_placements", 8);
                result.put("rotations", 4);
                result.put("post_placement_server_ticks", 200);
                result.put("random_tick_speed", 3);
                result.put("elapsed_world_ticks", level.getGameTime() - placementTime);
                result.put("placement_chunks_force_loaded", true);
                result.put("fixture", "Dry sky-level placement with zero ambient fluid verified in a larger surrounding buffer before each rotation; no external containment blocks mask template leaks.");
                result.put("water_ticks_explicitly_scheduled", true);
                result.put("immediate", immediate);
                result.put("delayed", delayed);
                result.put("scope", "Actual Forge 47.4.10 GameTest ServerLevel, native StructureTemplate placement, four rotations and repeated placements, neighbor and water updates, native player AABB, canSurvive and extended liquid-boundary checks. Natural vine growth is allowed only on the two sidewall strips and counted separately. No player save, full-modpack client playtest, lobby integration or persistent generation test.");
                result.put("world", ROOT.resolve("qa/forge-run").toString());
                Files.createDirectories(ROOT.resolve("qa"));
                Files.writeString(ROOT.resolve("qa/forge-placement.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("M04_FORGE_PASS sha256=" + hash + " rotations=4 placements=8 ticks=200 route=123 water=222");
                helper.succeed();
            } catch (Exception failure) {
                throw new IllegalStateException(failure);
            }
        });
    }
}
