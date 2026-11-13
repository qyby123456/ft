package net.farmingtales.a03qa;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("fta03qa")
@GameTestHolder("fta03qa")
@PrefixGameTestTemplate(false)
public final class A03PlacementTests {
    private static final Path ROOT = Path.of(System.getProperty("fta03qa.root"));
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "spawner", "end_portal", "nether_portal", "end_gateway", "lava", "water", "fire", "soul_fire", "powder_snow", "tnt", "sand", "gravel");
    private static final int[][] LAMPS = {{4, 5, 5}, {14, 5, 5}, {10, 5, 10}, {14, 5, 15}, {4, 5, 12}};
    private static final int[][] COLUMNS = {{3, 14, 5}, {7, 14, 5}, {2, 4, 5}, {2, 7, 5}, {16, 12, 5}, {16, 16, 5}, {7, 10, 2}, {7, 12, 2}, {12, 10, 2}, {12, 12, 2}};
    private static final int[][] WAYPOINTS = {{9, 1, 0}, {9, 1, 4}, {5, 1, 9}, {5, 1, 11}, {9, 1, 6}, {14, 1, 10}, {14, 1, 14}, {9, 1, 15}};

    public A03PlacementTests() {}

    private static <Value extends Comparable<Value>> String propertyValue(BlockState state, Property<Value> property) {
        return property.getName(state.getValue(property));
    }

    private static BlockState strictState(GameTestHelper helper, CompoundTag entry) {
        ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
        helper.assertTrue(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown/non-vanilla palette block " + identity);
        helper.assertTrue(!FORBIDDEN.contains(identity.getPath()), "Forbidden block " + identity);
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
        CompoundTag properties = entry.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(key);
            helper.assertTrue(property != null && propertyValue(state, property).equals(properties.getString(key)), "Invalid property " + identity + " " + key);
        }
        return state;
    }

    private static BlockPos world(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static BlockPos position(int width, int height, int depth, BlockPos origin, Rotation rotation) {
        return world(new BlockPos(width, height, depth), origin, rotation);
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        AABB body = new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8);
        return level.noCollision(body) && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty()
                && level.getFluidState(feet.below()).isEmpty() && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                && !level.getBlockState(feet).is(Blocks.COBWEB) && !level.getBlockState(feet.above()).is(Blocks.COBWEB);
    }

    private static Map<String, Object> verify(GameTestHelper helper, Map<BlockPos, BlockState> states, BlockPos origin, Rotation rotation, String phase) {
        ServerLevel level = helper.getLevel();
        int supported = 0;
        int emptyInventories = 0;
        int decorations = 0;
        for (Map.Entry<BlockPos, BlockState> entry : states.entrySet()) {
            BlockPos actualPosition = world(entry.getKey(), origin, rotation);
            BlockState expected = entry.getValue().rotate(rotation);
            BlockState actual = level.getBlockState(actualPosition);
            helper.assertTrue(actual.equals(expected), phase + " state drift " + rotation + " " + entry.getKey() + " expected=" + expected + " actual=" + actual);
            helper.assertTrue(actual.canSurvive(level, actualPosition), phase + " unsupported native block " + actualPosition + " " + actual);
            helper.assertTrue(level.getFluidState(actualPosition).isEmpty(), "Unexpected liquid");
            if (!actual.isAir()) supported++;
            BlockEntity blockEntity = level.getBlockEntity(actualPosition);
            if (expected.hasBlockEntity()) {
                helper.assertTrue(blockEntity != null, "Missing block entity " + actualPosition);
                CompoundTag payload = blockEntity.saveWithFullMetadata();
                helper.assertTrue(!payload.contains("LootTable") && !payload.contains("LootTableSeed") && payload.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Nonempty/deferred inventory");
                if (blockEntity instanceof Container inventory) {
                    for (int slot = 0; slot < inventory.getContainerSize(); slot++) helper.assertTrue(inventory.getItem(slot).isEmpty(), "Unexpected native inventory item");
                }
                emptyInventories++;
            } else helper.assertTrue(blockEntity == null, "Unexpected block entity");
            String identifier = BuiltInRegistries.BLOCK.getKey(actual.getBlock()).getPath();
            if (identifier.contains("flower_pot") || identifier.startsWith("potted_") || identifier.endsWith("_carpet") || actual.is(Blocks.CAULDRON)) {
                BlockPos underDecoration = actualPosition.below();
                helper.assertTrue(level.getBlockState(underDecoration).isFaceSturdy(level, underDecoration, Direction.UP), "Decoration lacks full supporting top face " + actualPosition);
                decorations++;
            }
            if (actual.is(Blocks.CAMPFIRE) || actual.is(Blocks.FURNACE)) helper.assertTrue(!actual.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT), "Heat source became lit");
        }
        helper.assertTrue(emptyInventories == 5, "Expected three empty barrels, one empty furnace and one empty campfire");
        for (int[] column : COLUMNS) {
            for (int height = 0; height <= column[2]; height++) helper.assertTrue(!level.getBlockState(position(column[0], height, column[1], origin, rotation)).isAir(), "Broken furniture leg / stone pier");
        }
        for (int[] lamp : LAMPS) {
            helper.assertTrue(level.getBlockState(position(lamp[0], lamp[1], lamp[2], origin, rotation)).is(Blocks.LANTERN), "Missing lamp");
            for (int height = 6; height <= 7; height++) helper.assertTrue(level.getBlockState(position(lamp[0], height, lamp[2], origin, rotation)).is(Blocks.CHAIN), "Broken roof chain");
            BlockPos beam = position(lamp[0], 8, lamp[2], origin, rotation);
            helper.assertTrue(level.getBlockState(beam).is(Blocks.STRIPPED_SPRUCE_LOG) && level.getBlockState(beam).isFaceSturdy(level, beam, Direction.DOWN), "Lamp ceiling beam missing");
            BlockPos anchor = position(lamp[0], 9, lamp[2], origin, rotation);
            helper.assertTrue(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Lamp roof anchor not sturdy");
        }
        for (int width = 10; width <= 12; width++) {
            BlockPos utensil = position(width, 5, 17, origin, rotation);
            BlockPos beam = utensil.above();
            helper.assertTrue(level.getBlockState(utensil).is(Blocks.GRINDSTONE) && level.getBlockState(beam).isFaceSturdy(level, beam, Direction.DOWN), "Hanging utensil beam missing");
            BlockPos wall = position(width, 6, 18, origin, rotation);
            helper.assertTrue(level.getBlockState(wall).isSolid(), "Rack wall anchor missing");
        }
        for (int width = 0; width < 19; width++) {
            for (int depth = 0; depth < 19; depth++) {
                BlockPos floor = position(width, 0, depth, origin, rotation);
                BlockPos roof = position(width, 9, depth, origin, rotation);
                helper.assertTrue(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP) && level.getBlockState(roof).isSolid(), "Floor/roof hole");
            }
        }
        for (int width = 8; width <= 10; width++) {
            for (int depth = -3; depth <= 6; depth++) {
                helper.assertTrue(walkable(level, position(width, 1, depth, origin, rotation)), "Native player AABB approach collision");
                if (depth >= 0) {
                    for (int height = 1; height <= 3; height++) helper.assertTrue(level.getBlockState(position(width, height, depth, origin, rotation)).isAir(), "Approach three-high clearance failure");
                }
            }
            helper.assertTrue(level.getBlockState(position(width, 4, 0, origin, rotation)).isAir(), "Four-high entrance failure");
        }
        List<BlockPos> queue = new ArrayList<>();
        Set<BlockPos> reached = new HashSet<>();
        BlockPos start = new BlockPos(9, 1, 0);
        queue.add(start);
        reached.add(start);
        for (int cursor = 0; cursor < queue.size(); cursor++) {
            BlockPos current = queue.get(cursor);
            for (Direction direction : List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
                BlockPos next = current.relative(direction);
                if (next.getX() < 0 || next.getX() >= 19 || next.getZ() < 0 || next.getZ() >= 19 || reached.contains(next)) continue;
                if (walkable(level, world(next, origin, rotation))) { reached.add(next); queue.add(next); }
            }
        }
        for (int[] waypoint : WAYPOINTS) helper.assertTrue(reached.contains(new BlockPos(waypoint[0], waypoint[1], waypoint[2])), "Unreachable native inspection point");
        int boundaryCells = 0;
        for (int width = -1; width <= 19; width++) {
            for (int depth = -1; depth <= 19; depth++) {
                for (int height = 0; height <= 11; height++) {
                    helper.assertTrue(level.getFluidState(position(width, height, depth, origin, rotation)).isEmpty(), "Fluid found in enlarged template boundary");
                    boundaryCells++;
                }
            }
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", rotation.name());
        report.put("phase", phase);
        report.put("origin", List.of(origin.getX(), origin.getY(), origin.getZ()));
        report.put("exact_record_matches", states.size());
        report.put("native_surviving_nonair_blocks", supported);
        report.put("empty_block_entities", emptyInventories);
        report.put("supported_decorations", decorations);
        report.put("supported_furniture_columns", COLUMNS.length);
        report.put("roof_anchored_lanterns", LAMPS.length);
        report.put("wall_anchored_utensils", 3);
        report.put("roof_cells", 361);
        report.put("player_aabb", List.of(0.6, 1.8, 0.6));
        report.put("native_reachable_floor_cells", reached.size());
        report.put("checked_approach_cells_with_external_apron", 30);
        report.put("reachable_inspection_points", WAYPOINTS.length);
        report.put("liquid_scan_cells", boundaryCells);
        report.put("live_fire_sources", 0);
        return report;
    }

    @GameTest(template = "empty", batch = "a03_native", timeoutTicks = 900)
    public static void kitchenFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(level.getServer() instanceof GameTestServer, "Only an isolated disposable GameTest server is permitted");
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        helper.assertTrue(level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING) == 3, "Keep default random ticks");
        helper.assertTrue(level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK), "Fire ticks must remain enabled");
        byte[] bytes = Files.readAllBytes(ROOT.resolve("room.nbt"));
        String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        String declared = JsonParser.parseString(Files.readString(ROOT.resolve("room.json"))).getAsJsonObject().get("sha256").getAsString();
        helper.assertTrue(sha256.equals(declared), "Structure fingerprint mismatch");
        CompoundTag data = NbtIo.readCompressed(ROOT.resolve("room.nbt").toFile());
        helper.assertTrue(data.getInt("DataVersion") == 3465 && data.getList("entities", Tag.TAG_COMPOUND).isEmpty() && !data.contains("palettes"), "Version/entities/palette contract failure");
        List<BlockState> palette = new ArrayList<>();
        for (Tag tag : data.getList("palette", Tag.TAG_COMPOUND)) palette.add(strictState(helper, (CompoundTag) tag));
        Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        for (Tag tag : data.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) tag;
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            helper.assertTrue(local.getX() >= 0 && local.getX() < 19 && local.getY() >= 0 && local.getY() < 11 && local.getZ() >= 0 && local.getZ() < 19 && !states.containsKey(local), "Bounds/duplicate failure");
            if (record.contains("nbt")) {
                CompoundTag payload = record.getCompound("nbt");
                helper.assertTrue(Set.of("minecraft:barrel", "minecraft:furnace", "minecraft:campfire").contains(payload.getString("id")) && !payload.contains("LootTable") && payload.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Unsafe block entity payload");
            }
            states.put(local, palette.get(record.getInt("state")));
        }
        helper.assertTrue(states.size() == 3971, "Missing explicit bounded volume");
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        helper.assertTrue(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == 3971, "Native template roundtrip record loss");
        List<Rotation> rotations = List.of(Rotation.NONE, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90);
        List<BlockPos> origins = new ArrayList<>();
        List<Map<String, Object>> immediate = new ArrayList<>();
        for (int index = 0; index < rotations.size(); index++) {
            Rotation rotation = rotations.get(index);
            BlockPos origin = helper.absolutePos(new BlockPos(80 + index * 64, 160, 80));
            origins.add(origin);
            BlockPos first = position(-2, 0, -4, origin, rotation);
            BlockPos last = position(20, 0, 20, origin, rotation);
            for (int chunkX = Math.min(first.getX(), last.getX()) >> 4; chunkX <= Math.max(first.getX(), last.getX()) >> 4; chunkX++) {
                for (int chunkZ = Math.min(first.getZ(), last.getZ()) >> 4; chunkZ <= Math.max(first.getZ(), last.getZ()) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
            }
            for (int width = -2; width <= 20; width++) {
                for (int depth = -4; depth <= 20; depth++) {
                    for (int height = -1; height <= 12; height++) level.setBlock(position(width, height, depth, origin, rotation), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true);
            helper.assertTrue(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_ALL), "First placement failed");
            helper.assertTrue(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_ALL), "Repeat placement failed");
            for (int width = 8; width <= 10; width++) {
                for (int depth = -3; depth < 0; depth++) level.setBlock(position(width, 0, depth, origin, rotation), Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
            }
            for (BlockPos local : states.keySet()) {
                BlockPos actualPosition = world(local, origin, rotation);
                level.updateNeighborsAt(actualPosition, level.getBlockState(actualPosition).getBlock());
            }
            immediate.add(verify(helper, states, origin, rotation, "repeat_placement_and_neighbor_updates"));
            System.out.println("A03_IMMEDIATE_PASS rotation=" + rotation);
        }
        long placementTime = level.getGameTime();
        helper.runAfterDelay(200, () -> {
            try {
                helper.assertTrue(level.getGameTime() - placementTime >= 200, "Insufficient elapsed native world ticks");
                List<Map<String, Object>> delayed = new ArrayList<>();
                for (int index = 0; index < rotations.size(); index++) delayed.add(verify(helper, states, origins.get(index), rotations.get(index), "after_200_server_ticks"));
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("status", "PASS");
                report.put("id", "A03");
                report.put("minecraft", SharedConstants.getCurrentVersion().getName());
                report.put("forge", net.minecraftforge.versions.forge.ForgeVersion.getVersion());
                report.put("structure_sha256", sha256);
                report.put("strict_palette_states", palette.size());
                report.put("template_records", states.size());
                report.put("rotations", 4);
                report.put("actual_world_placements", 8);
                report.put("post_placement_server_ticks", 200);
                report.put("elapsed_world_ticks", level.getGameTime() - placementTime);
                report.put("random_tick_speed", 3);
                report.put("fire_ticks_enabled", true);
                report.put("placement_chunks_force_loaded", true);
                report.put("immediate", immediate);
                report.put("delayed", delayed);
                report.put("scope", "Isolated native Forge GameTest ServerLevel; exact registry/properties, native NBT roundtrip, four rotations, repeat placement, all template records, neighbor updates, 200 real server ticks, all native canSurvive, furniture/roof/rack support, empty inventory, player AABB and walking flood-fill. No player world, modpack client or lobby integration was used.");
                report.put("world", ROOT.resolve("qa/forge-run").toString());
                Files.writeString(ROOT.resolve("qa/forge-placement.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("A03_FORGE_PASS sha256=" + sha256 + " rotations=4 placements=8 ticks=200");
                helper.succeed();
            } catch (Exception failure) {
                throw new IllegalStateException(failure);
            }
        });
    }
}

