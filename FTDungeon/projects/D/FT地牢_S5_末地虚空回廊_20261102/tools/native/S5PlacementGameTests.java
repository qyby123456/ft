package net.farmingtales.s5qa;

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

@Mod("fts5qa")
@GameTestHolder("fts5qa")
@PrefixGameTestTemplate(false)
public final class S5PlacementGameTests {
    private static final Path ROOT = Path.of(System.getProperty("fts5qa.root"));
    private static final BlockPos ENTRANCE = new BlockPos(3, 3, 0);
    private static final BlockPos INTERIOR = new BlockPos(3, 3, 10);

    public S5PlacementGameTests() {}

    private static <Value extends Comparable<Value>> String propertyValue(BlockState state, Property<Value> property) {
        return property.getName(state.getValue(property));
    }

    private static BlockState strictState(GameTestHelper helper, CompoundTag palette) {
        ResourceLocation identity = new ResourceLocation(palette.getString("Name"));
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla block: " + identity);
        helper.assertTrue(identity.getNamespace().equals("minecraft"), "Only vanilla blocks may be delivered");
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette);
        CompoundTag properties = palette.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(key);
            helper.assertTrue(property != null, "Unknown property: " + identity + " " + key);
            helper.assertTrue(propertyValue(state, property).equals(properties.getString(key)), "Invalid property: " + identity + " " + key);
        }
        return state;
    }

    private static BlockPos world(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        AABB body = new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8);
        return level.noCollision(body) && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty()
                && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                && level.getFluidState(feet.below()).isEmpty();
    }

    private static Map<String, Object> verifyWorld(GameTestHelper helper, ServerLevel level, Map<BlockPos, BlockState> states,
            BlockPos origin, Rotation rotation, String phase) {
        int rods = 0;
        int flowers = 0;
        int frames = 0;
        int glassFloor = 0;
        for (Map.Entry<BlockPos, BlockState> entry : states.entrySet()) {
            BlockPos position = world(entry.getKey(), origin, rotation);
            BlockState actual = level.getBlockState(position);
            helper.assertTrue(actual.equals(entry.getValue().rotate(rotation)), phase + " state mismatch: " + rotation + " " + entry.getKey() + " actual=" + actual);
            helper.assertTrue(actual.getFluidState().isEmpty(), "Unexpected fluid: " + position);
            helper.assertTrue(level.getBlockEntity(position) == null, "Unexpected block entity: " + position);
            helper.assertTrue(actual.canSurvive(level, position), "Illegal unsupported state: " + position + " " + actual);
            if (actual.is(Blocks.END_ROD) || actual.is(Blocks.CHORUS_FLOWER) || actual.is(Blocks.END_PORTAL_FRAME)) {
                helper.assertTrue(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Missing full decoration support: " + position);
            }
            if (actual.is(Blocks.END_ROD)) {
                rods++;
            }
            if (actual.is(Blocks.CHORUS_FLOWER)) {
                helper.assertTrue(level.getBlockState(position.below()).is(Blocks.END_STONE), "Chorus flower lacks end stone");
                flowers++;
            }
            if (actual.is(Blocks.END_PORTAL_FRAME)) {
                helper.assertTrue(!actual.getValue(net.minecraft.world.level.block.EndPortalFrameBlock.HAS_EYE), "Portal fragment must not contain an eye");
                frames++;
            }
            if (actual.is(Blocks.TINTED_GLASS) && entry.getKey().getY() == 2) {
                glassFloor++;
            }
        }
        helper.assertTrue(rods == 6 && flowers == 2 && frames == 2 && glassFloor == 8, "Theme/support count mismatch");
        for (int blockX = 0; blockX < 7; blockX++) {
            for (int blockZ = 0; blockZ < 13; blockZ++) {
                BlockPos roof = world(new BlockPos(blockX, 7, blockZ), origin, rotation);
                helper.assertTrue(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.UP), "Unsealed roof: " + roof);
            }
        }
        for (int blockX = 2; blockX <= 4; blockX++) {
            for (int blockY = 3; blockY <= 5; blockY++) {
                BlockPos opening = world(new BlockPos(blockX, blockY, 0), origin, rotation);
                helper.assertTrue(level.getBlockState(opening).getCollisionShape(level, opening).isEmpty(), "North opening is not 3x3");
            }
            for (int blockZ = -3; blockZ <= 11; blockZ++) {
                helper.assertTrue(walkable(level, world(new BlockPos(blockX, 3, blockZ), origin, rotation)), "Dry three-lane approach or passage blocked");
            }
        }
        Set<BlockPos> reachable = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        reachable.add(ENTRANCE);
        pending.add(ENTRANCE);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() < 0 || next.getX() >= 7 || next.getZ() < 0 || next.getZ() >= 13 || reachable.contains(next)) {
                    continue;
                }
                if (walkable(level, world(next, origin, rotation))) {
                    reachable.add(next);
                    pending.add(next);
                }
            }
        }
        helper.assertTrue(reachable.contains(INTERIOR) && reachable.size() == 36, "Native collision path must reach exactly 36 safe cells");
        int guardedEdges = 0;
        for (BlockPos current : reachable) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (reachable.contains(next) || next.getZ() == -1 && next.getX() >= 2 && next.getX() <= 4) {
                    continue;
                }
                BlockPos barrier = world(next, origin, rotation);
                helper.assertTrue(Block.isShapeFullBlock(level.getBlockState(barrier).getCollisionShape(level, barrier)), "Reachable unguarded drop edge: " + next);
                guardedEdges++;
            }
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", rotation.name());
        report.put("origin", List.of(origin.getX(), origin.getY(), origin.getZ()));
        report.put("phase", phase);
        report.put("status", "PASS");
        report.put("compared_block_states", states.size());
        report.put("state_mismatches", 0);
        report.put("can_survive_failures", 0);
        report.put("native_player_collision_box", List.of(0.6, 1.8, 0.6));
        report.put("native_reachable_cells", reachable.size());
        report.put("three_lane_approach_cells", 45);
        report.put("guarded_nonentrance_edges", guardedEdges);
        report.put("unguarded_drop_edges", 0);
        report.put("supported_end_rods", rods);
        report.put("supported_chorus_flowers", flowers);
        report.put("inactive_portal_frames", frames);
        report.put("safe_glass_floor_tiles", glassFloor);
        report.put("block_entities", 0);
        return report;
    }

    @GameTest(template = "empty", batch = "s5_native", timeoutTicks = 400)
    public static void safePassageFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(level.getServer() instanceof GameTestServer, "Only the disposable isolated GameTest server is allowed");
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        Path nbtPath = ROOT.resolve("out/S5/room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(nbtPath)));
        JsonObject metadata = JsonParser.parseString(Files.readString(ROOT.resolve("out/S5/room.json"))).getAsJsonObject();
        helper.assertTrue(metadata.get("sha256").getAsString().equals(hash), "Metadata structure hash mismatch");
        CompoundTag data = NbtIo.readCompressed(nbtPath.toFile());
        helper.assertTrue(data.getInt("DataVersion") == 3465 && data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Wrong data version or spawned entities");
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : data.getList("palette", Tag.TAG_COMPOUND)) {
            palette.add(strictState(helper, (CompoundTag) entry));
        }
        Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        for (Tag entry : data.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            ListTag position = record.getList("pos", Tag.TAG_INT);
            BlockPos local = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            helper.assertTrue(local.getX() >= 0 && local.getX() < 7 && local.getY() >= 0 && local.getY() < 8 && local.getZ() >= 0 && local.getZ() < 13, "Out-of-bounds record");
            helper.assertTrue(!record.contains("nbt") && !states.containsKey(local), "Unexpected block entity or duplicate record");
            states.put(local, palette.get(record.getInt("state")));
        }
        helper.assertTrue(states.size() == 728, "Incomplete template volume");
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        helper.assertTrue(template.getSize().equals(new BlockPos(7, 8, 13)), "Native template dimensions mismatch");
        helper.assertTrue(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == states.size(), "Native template roundtrip lost records");
        List<Rotation> rotations = List.of(Rotation.NONE, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90);
        List<BlockPos> origins = new ArrayList<>();
        List<Map<String, Object>> immediate = new ArrayList<>();
        for (int index = 0; index < rotations.size(); index++) {
            Rotation rotation = rotations.get(index);
            BlockPos origin = helper.absolutePos(new BlockPos(32 + index * 32, 4, 24));
            origins.add(origin);
            for (int blockX = -1; blockX <= 7; blockX++) {
                for (int blockZ = -4; blockZ <= 13; blockZ++) {
                    for (int blockY = -1; blockY <= 8; blockY++) {
                        level.setBlock(world(new BlockPos(blockX, blockY, blockZ), origin, rotation), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true);
            helper.assertTrue(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_ALL), "First native placement failed");
            helper.assertTrue(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_ALL), "Repeat native placement failed");
            for (int blockX = 2; blockX <= 4; blockX++) {
                for (int blockZ = -3; blockZ <= -1; blockZ++) {
                    level.setBlock(world(new BlockPos(blockX, 2, blockZ), origin, rotation), Blocks.END_STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            for (BlockPos local : states.keySet()) {
                BlockPos position = world(local, origin, rotation);
                level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
            }
            immediate.add(verifyWorld(helper, level, states, origin, rotation, "after_two_placements_and_neighbor_updates"));
        }
        helper.runAtTickTime(100, () -> {
            try {
                List<Map<String, Object>> delayed = new ArrayList<>();
                for (int index = 0; index < rotations.size(); index++) {
                    delayed.add(verifyWorld(helper, level, states, origins.get(index), rotations.get(index), "after_100_server_ticks"));
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("id", "S5");
                result.put("minecraft", SharedConstants.getCurrentVersion().getName());
                result.put("forge", net.minecraftforge.versions.forge.ForgeVersion.getVersion());
                result.put("structure_sha256", hash);
                result.put("palette_states_strictly_validated", palette.size());
                result.put("roundtrip_records", states.size());
                result.put("actual_world_placements", 8);
                result.put("post_placement_server_ticks", 100);
                result.put("immediate", immediate);
                result.put("delayed", delayed);
                result.put("scope", "Actual Forge GameTest ServerLevel + StructureTemplate.placeInWorld, four native rotations, repeat placements, neighbor notifications, all-state comparison, canSurvive, player AABB collision, supported dry routes and barriers. Not a full-modpack client playtest, lobby route integration, persistent-marker test, multiplayer test or deployment.");
                result.put("world", ROOT.resolve("qa/forge-run").toString());
                Files.createDirectories(ROOT.resolve("qa"));
                Files.writeString(ROOT.resolve("qa/native-placement.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("S5_NATIVE_PLACEMENT_PASS sha256=" + hash + " rotations=4 placements=8 delayedTicks=100 reachable=36");
                helper.succeed();
            } catch (Exception failure) {
                throw new IllegalStateException(failure);
            }
        });
    }
}
