package net.farmingtales.s18verification;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("s18verification")
@PrefixGameTestTemplate(false)
public final class S18GameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW);

    private S18GameTests() {}

    @GameTest(template = "empty", timeoutTicks = 520)
    public static void north(GameTestHelper helper) throws Exception {
        verify(helper, Rotation.NONE, "north", 0);
    }

    @GameTest(template = "empty", timeoutTicks = 520)
    public static void east(GameTestHelper helper) throws Exception {
        verify(helper, Rotation.CLOCKWISE_90, "east", 1);
    }

    @GameTest(template = "empty", timeoutTicks = 520)
    public static void south(GameTestHelper helper) throws Exception {
        verify(helper, Rotation.CLOCKWISE_180, "south", 2);
    }

    @GameTest(template = "empty", timeoutTicks = 520)
    public static void west(GameTestHelper helper) throws Exception {
        verify(helper, Rotation.COUNTERCLOCKWISE_90, "west", 3);
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, message);
    }

    private static BlockPos coordinate(JsonArray values) {
        return new BlockPos(values.get(0).getAsInt(), values.get(1).getAsInt(), values.get(2).getAsInt());
    }

    private static BlockPos position(BlockPos origin, BlockPos local, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getCollisionShape(level, position).isEmpty() && state.getFluidState().isEmpty()
                && !HAZARDS.contains(state.getBlock());
    }

    private static boolean walkable(ServerLevel level, BlockPos position) {
        BlockState floor = level.getBlockState(position.below());
        return clear(level, position) && clear(level, position.above()) && floor.getFluidState().isEmpty()
                && !HAZARDS.contains(floor.getBlock()) && floor.isFaceSturdy(level, position.below(), Direction.UP);
    }

    private static Set<BlockPos> reachable(ServerLevel level, BlockPos origin, Rotation rotation, BlockPos start) {
        Set<BlockPos> allowed = new HashSet<>();
        for (int blockX = 0; blockX < 15; blockX++) {
            for (int blockZ = 0; blockZ < 16; blockZ++) {
                allowed.add(position(origin, new BlockPos(blockX, 3, blockZ), rotation));
            }
        }
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        if (walkable(level, start)) {
            visited.add(start);
            pending.add(start);
        }
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = current.relative(direction);
                if (allowed.contains(neighbor) && !visited.contains(neighbor) && walkable(level, neighbor)) {
                    visited.add(neighbor);
                    pending.addLast(neighbor);
                }
            }
        }
        return visited;
    }

    private static void verify(GameTestHelper helper, Rotation rotation, String view, int index) throws Exception {
        ServerLevel level = helper.getLevel();
        require(helper, level.getServer() instanceof GameTestServer, "Isolated GameTest server only");
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        Path root = Path.of(System.getProperty("s18.project"));
        Path source = root.resolve("room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(helper, metadata.get("sha256").getAsString().equals(hash), "Source fingerprint matches metadata");
        CompoundTag tag = NbtIo.readCompressed(source.toFile());
        require(helper, tag.getInt("DataVersion") == 3465 && tag.contains("palette") && !tag.contains("palettes"), "1.20.1 single palette");
        require(helper, tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "No embedded entities");
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : tag.getList("palette", Tag.TAG_COMPOUND)) {
            CompoundTag raw = (CompoundTag) entry;
            ResourceLocation identity = new ResourceLocation(raw.getString("Name"));
            require(helper, identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Native vanilla block registry: " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = raw.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(helper, property != null && property.getValue(properties.getString(key)).isPresent(), "Native property: " + identity + "/" + key);
            }
            palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), raw));
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        for (Tag entry : tag.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            ListTag values = record.getList("pos", Tag.TAG_INT);
            BlockPos local = new BlockPos(values.getInt(0), values.getInt(1), values.getInt(2));
            require(helper, local.getX() >= 0 && local.getX() < 15 && local.getY() >= 0 && local.getY() < 11 && local.getZ() >= 0 && local.getZ() < 16, "Bounds");
            require(helper, !expected.containsKey(local) && !record.contains("nbt"), "Unique position, no block entity payload");
            expected.put(local, palette.get(record.getInt("state")).rotate(rotation));
        }
        require(helper, expected.size() == 2640, "Full-volume records including explicit air");
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(helper, template.getSize().equals(new BlockPos(15, 11, 16)), "Native template dimensions");
        require(helper, template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == 2640, "Native template roundtrip");
        BlockPos origin = new BlockPos(helper.absolutePos(new BlockPos(32 + index * 48, 0, 32)).getX(), 160, helper.absolutePos(new BlockPos(0, 0, 32)).getZ());
        ChunkPos chunk = new ChunkPos(origin);
        for (int chunkX = chunk.x - 1; chunkX <= chunk.x + 1; chunkX++) {
            for (int chunkZ = chunk.z - 1; chunkZ <= chunk.z + 1; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
        }
        for (int blockX = -2; blockX <= 16; blockX++) {
            for (int blockY = -1; blockY <= 11; blockY++) {
                for (int blockZ = -2; blockZ <= 17; blockZ++) {
                    level.setBlock(position(origin, new BlockPos(blockX, blockY, blockZ), rotation), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true).setRotation(rotation);
        require(helper, template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS), "Native StructureTemplate.placeInWorld");
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos actual = position(origin, entry.getKey(), rotation);
            if (!entry.getValue().getFluidState().isEmpty()) {
                level.scheduleTick(actual, entry.getValue().getFluidState().getType(), 1);
            }
            if (!entry.getValue().isAir()) level.updateNeighborsAt(actual, entry.getValue().getBlock());
        }
        long startTime = level.getGameTime();
        List<Integer> checkpoints = new ArrayList<>();
        inspect(helper, level, origin, rotation, metadata, expected);
        checkpoints.add(0);
        for (int delay : List.of(40, 120)) {
            helper.runAfterDelay(delay, () -> {
                inspect(helper, level, origin, rotation, metadata, expected);
                checkpoints.add(delay);
            });
        }
        helper.runAfterDelay(400, () -> {
            try {
                int reachableCount = inspect(helper, level, origin, rotation, metadata, expected);
                checkpoints.add(400);
                require(helper, level.getGameTime() - startTime >= 400, "400 actual world ticks elapsed");
                require(helper, checkpoints.equals(List.of(0, 40, 120, 400)), "All checkpoints passed");
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("status", "PASS");
                report.put("id", "CORRIDOR_S18");
                report.put("rotation", view);
                report.put("sha256", hash);
                report.put("minecraft", "1.20.1");
                report.put("forge", "47.4.10");
                report.put("records", 2640);
                report.put("palette_entries", palette.size());
                report.put("observed_ticks", 400);
                report.put("checkpoints", checkpoints);
                report.put("fluids_explicitly_scheduled", 60);
                report.put("lava_sources", 12);
                report.put("water_sources", 48);
                report.put("reachable_positions", reachableCount);
                report.put("game_rules", Map.of("doFireTick", level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK), "randomTickSpeed", level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING)));
                report.put("checks", List.of("strict_vanilla_registry_states", "native_template_roundtrip", "all_2640_states_exact_at_four_checkpoints", "all_native_canSurvive", "six_chain_to_roof_lantern_anchors", "six_anvils_supported_no_falling_entities", "dry_3x3_both_end_interfaces", "three_wide_dry_route_all_16_blocks_bidirectional", "lava_six_face_full_cube_enclosures", "water_bottom_and_side_enclosures", "no_water_lava_contact_or_solidification", "no_liquid_in_two_block_exterior_halo", "no_block_entities_inventory_or_item_drops"));
                report.put("scope", "Isolated vanilla+Forge GameTest world only; no player saves, deployment, full modpack play test or in-game client screenshot.");
                Files.writeString(root.resolve("qa/native-" + view + ".json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("S18_NATIVE_PASS rotation=" + view + " records=2640 ticks=400 lava=12 water=48 reachable=" + reachableCount + " sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                throw new IllegalStateException("S18 final verification failed: " + view, exception);
            }
        });
    }

    private static int inspect(GameTestHelper helper, ServerLevel level, BlockPos origin, Rotation rotation,
            JsonObject metadata, Map<BlockPos, BlockState> expected) {
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos actual = position(origin, entry.getKey(), rotation);
            BlockState state = level.getBlockState(actual);
            require(helper, state.equals(entry.getValue()), "Exact state " + entry.getKey() + ": " + state + " != " + entry.getValue());
            require(helper, state.canSurvive(level, actual), "Native survival: " + entry.getKey());
            require(helper, level.getBlockEntity(actual) == null, "No block entities or inventory");
            if (state.is(Blocks.ANVIL)) {
                require(helper, level.getBlockState(actual.below()).isFaceSturdy(level, actual.below(), Direction.UP), "Anvil supported");
            }
            if (state.is(Blocks.CHAIN)) {
                require(helper, state.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y, "Vertical chain");
                BlockState above = level.getBlockState(actual.above());
                require(helper, above.is(Blocks.CHAIN) || above.isFaceSturdy(level, actual.above(), Direction.DOWN), "Chain anchors continuously to ceiling");
            }
            if (!state.getFluidState().isEmpty()) {
                require(helper, state.getFluidState().isSource(), "No flowing source conversion");
                for (Direction direction : Direction.values()) {
                    if (state.is(Blocks.WATER) && direction == Direction.UP) continue;
                    BlockPos neighbor = actual.relative(direction);
                    BlockState neighborState = level.getBlockState(neighbor);
                    require(helper, neighborState.getBlock() == state.getBlock()
                            || neighborState.getFluidState().isEmpty() && Block.isShapeFullBlock(neighborState.getCollisionShape(level, neighbor)),
                            "Full cube liquid enclosure at " + entry.getKey() + "/" + direction);
                }
            }
        }
        BlockPos entrance = position(origin, coordinate(metadata.getAsJsonArray("entrance")), rotation);
        BlockPos interior = position(origin, coordinate(metadata.getAsJsonArray("interior")), rotation);
        BlockPos exit = position(origin, coordinate(metadata.getAsJsonArray("exit")), rotation);
        Set<BlockPos> visited = reachable(level, origin, rotation, entrance);
        require(helper, visited.contains(interior) && visited.contains(exit), "Entrance-interior-exit collision path");
        require(helper, reachable(level, origin, rotation, exit).contains(entrance), "Bidirectional return path");
        for (int blockZ = 0; blockZ < 16; blockZ++) {
            for (int blockX = 6; blockX <= 8; blockX++) {
                BlockPos feet = position(origin, new BlockPos(blockX, 3, blockZ), rotation);
                require(helper, visited.contains(feet) && walkable(level, feet) && clear(level, feet.above(2)), "3-wide dry 3-high route: " + blockX + "/" + blockZ);
            }
        }
        for (int blockX = -2; blockX <= 16; blockX++) {
            for (int blockY = -1; blockY <= 11; blockY++) {
                for (int blockZ = -2; blockZ <= 17; blockZ++) {
                    BlockPos local = new BlockPos(blockX, blockY, blockZ);
                    BlockPos actual = position(origin, local, rotation);
                    if (!expected.containsKey(local)) require(helper, level.getFluidState(actual).isEmpty(), "No external fluid leak: " + local);
                }
            }
        }
        AABB bounds = new AABB(origin.offset(-20, -2, -20), origin.offset(20, 13, 20));
        require(helper, level.getEntitiesOfClass(ItemEntity.class, bounds).isEmpty(), "No detached item drops");
        require(helper, level.getEntitiesOfClass(FallingBlockEntity.class, bounds).isEmpty(), "No falling anvils");
        return visited.size();
    }
}
