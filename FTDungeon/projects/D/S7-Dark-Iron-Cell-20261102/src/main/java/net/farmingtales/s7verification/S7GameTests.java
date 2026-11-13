package net.farmingtales.s7verification;

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
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("s7verification")
@PrefixGameTestTemplate(false)
public final class S7GameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW);

    private S7GameTests() {}

    @GameTest(template = "empty", timeoutTicks = 260)
    public static void north(GameTestHelper helper) throws Exception {
        verify(helper, Rotation.NONE, "north");
    }

    @GameTest(template = "empty", timeoutTicks = 260)
    public static void east(GameTestHelper helper) throws Exception {
        verify(helper, Rotation.CLOCKWISE_90, "east");
    }

    @GameTest(template = "empty", timeoutTicks = 260)
    public static void south(GameTestHelper helper) throws Exception {
        verify(helper, Rotation.CLOCKWISE_180, "south");
    }

    @GameTest(template = "empty", timeoutTicks = 260)
    public static void west(GameTestHelper helper) throws Exception {
        verify(helper, Rotation.COUNTERCLOCKWISE_90, "west");
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

    private static Set<BlockPos> reachable(ServerLevel level, BlockPos origin, Rotation rotation, BlockPos entrance) {
        Set<BlockPos> allowed = new HashSet<>();
        for (int blockX = 0; blockX < 9; blockX++) {
            for (int blockZ = 0; blockZ < 9; blockZ++) {
                allowed.add(position(origin, new BlockPos(blockX, 3, blockZ), rotation));
            }
        }
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        if (walkable(level, entrance)) {
            visited.add(entrance);
            pending.add(entrance);
        }
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = current.relative(direction);
                if (allowed.contains(neighbor) && !visited.contains(neighbor) && walkable(level, neighbor)) {
                    visited.add(neighbor);
                    pending.add(neighbor);
                }
            }
        }
        return visited;
    }

    private static void require(GameTestHelper helper, boolean condition, String description) {
        helper.assertTrue(condition, description);
    }

    private static void verify(GameTestHelper helper, Rotation rotation, String view) throws Exception {
        ServerLevel level = helper.getLevel();
        require(helper, level.getServer() instanceof GameTestServer, "Only run in an isolated GameTest server");
        Path root = Path.of(System.getProperty("s7.project"));
        Path source = root.resolve("out/S7/room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("out/S7/room.json")), JsonObject.class);
        JsonObject features = JSON.fromJson(Files.readString(root.resolve("qa/features.json")), JsonObject.class);
        require(helper, hash.equals(metadata.get("sha256").getAsString()), "NBT fingerprint matches metadata");
        CompoundTag tag = NbtIo.readCompressed(source.toFile());
        require(helper, tag.getInt("DataVersion") == 3465 && tag.contains("palette") && !tag.contains("palettes"), "1.20.1 single-palette NBT");
        require(helper, tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "No embedded entities");
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : tag.getList("palette", Tag.TAG_COMPOUND)) {
            CompoundTag raw = (CompoundTag) entry;
            ResourceLocation identity = new ResourceLocation(raw.getString("Name"));
            require(helper, identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Vanilla registered block: " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = raw.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(helper, property != null && property.getValue(properties.getString(key)).isPresent(), "Legal block property: " + identity + "/" + key);
            }
            palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), raw));
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        for (Tag entry : tag.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            ListTag values = record.getList("pos", Tag.TAG_INT);
            BlockPos local = new BlockPos(values.getInt(0), values.getInt(1), values.getInt(2));
            require(helper, local.getX() >= 0 && local.getX() < 9 && local.getY() >= 0 && local.getY() < 9 && local.getZ() >= 0 && local.getZ() < 9, "Local position in bounds");
            require(helper, !expected.containsKey(local), "No duplicate block position");
            expected.put(local, palette.get(record.getInt("state")).rotate(rotation));
            CompoundTag entity = record.getCompound("nbt");
            require(helper, !entity.contains("LootTable") && entity.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "No reward payload");
        }
        require(helper, expected.size() == 729, "Full-volume air semantics are explicit");
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        CompoundTag saved = template.save(new CompoundTag());
        require(helper, saved.getList("blocks", Tag.TAG_COMPOUND).size() == 729 && template.getSize().equals(new BlockPos(9, 9, 9)), "Native template load/save roundtrip");
        BlockPos origin = helper.absolutePos(new BlockPos(10, 2, 10));
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true).setRotation(rotation);
        require(helper, template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS), "Native StructureTemplate.placeInWorld succeeds");
        inspect(helper, level, origin, rotation, metadata, features, expected);
        helper.runAfterDelay(100, () -> {
            try {
                int reachableCount = inspect(helper, level, origin, rotation, metadata, features, expected);
                int sourceCount = 0;
                for (int blockX = -1; blockX <= 9; blockX++) {
                    for (int blockY = -1; blockY <= 9; blockY++) {
                        for (int blockZ = -1; blockZ <= 9; blockZ++) {
                            BlockPos local = new BlockPos(blockX, blockY, blockZ);
                            BlockPos actual = position(origin, local, rotation);
                            boolean sourcePosition = expected.containsKey(local) && expected.get(local).is(Blocks.LAVA);
                            require(helper, level.getFluidState(actual).isEmpty() || sourcePosition, "No fluid outside sealed source cell: " + local);
                            if (sourcePosition) {
                                sourceCount++;
                            }
                        }
                    }
                }
                require(helper, sourceCount == 11, "All 11 sealed lava sources remain intact");
                AABB bounds = new AABB(origin.offset(-9, -1, -9), origin.offset(10, 10, 10));
                require(helper, level.getEntitiesOfClass(ItemEntity.class, bounds).isEmpty(), "Decorations do not drop as unsupported items");
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("status", "PASS");
                report.put("id", "S7");
                report.put("rotation", view);
                report.put("sha256", hash);
                report.put("minecraft", "1.20.1");
                report.put("forge", "47.4.10");
                report.put("native_block_records", 729);
                report.put("native_palette_entries", palette.size());
                report.put("observed_ticks", 100);
                report.put("collision_reachable_positions", reachableCount);
                report.put("lava_sources", 11);
                report.put("block_entities", Map.of("skulls", 3, "banner", 1));
                report.put("checks", List.of("strict_vanilla_registry_and_state_properties", "native_template_roundtrip", "native_world_placement", "all_729_states_initial_and_after_100_ticks", "native_3x3_interface_and_full_floor", "native_collision_bidirectional_route_and_rack_bypass", "skull_candle_banner_survival_and_support", "continuous_vertical_chain_ceiling_anchors", "rack_and_cage_pillar_support", "sealed_lava_no_leak_after_100_ticks", "no_reward_inventory_or_dropped_items"));
                report.put("scope", "Isolated vanilla+Forge GameTest world. Not a full modpack client screenshot, integrated gallery test, deployment, or player-world test.");
                Files.writeString(root.resolve("qa/native-" + view + ".json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("S7_NATIVE_PLACEMENT_PASS rotation=" + view + " records=729 ticks=100 lava=11 reachable=" + reachableCount + " sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                throw new IllegalStateException("S7 native placement verification failed: " + view, exception);
            }
        });
    }

    private static int inspect(GameTestHelper helper, ServerLevel level, BlockPos origin, Rotation rotation,
            JsonObject metadata, JsonObject features, Map<BlockPos, BlockState> expected) {
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos actual = position(origin, entry.getKey(), rotation);
            BlockState state = level.getBlockState(actual);
            require(helper, state.equals(entry.getValue()), "Exact native state mismatch at " + entry.getKey() + ": " + state + " != " + entry.getValue());
            require(helper, state.canSurvive(level, actual), "Native support/survival at " + entry.getKey());
            if (level.getBlockEntity(actual) instanceof Container container) {
                require(helper, container.isEmpty(), "No filled native inventory");
            }
        }
        for (int blockX = 3; blockX <= 5; blockX++) {
            for (int blockY = 3; blockY <= 5; blockY++) {
                require(helper, clear(level, position(origin, new BlockPos(blockX, blockY, 0), rotation)), "Unobstructed three-by-three interface");
            }
            BlockPos floor = position(origin, new BlockPos(blockX, 2, 0), rotation);
            require(helper, level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Full-width entrance floor");
        }
        BlockPos entrance = position(origin, coordinate(metadata.getAsJsonArray("entrance")), rotation);
        BlockPos interior = position(origin, coordinate(metadata.getAsJsonArray("interior")), rotation);
        Set<BlockPos> visited = reachable(level, origin, rotation, entrance);
        require(helper, visited.contains(interior), "Safe interior reachable from entrance using native collision");
        require(helper, reachable(level, origin, rotation, interior).contains(entrance), "Return route is safe");
        for (BlockPos station : List.of(new BlockPos(2, 3, 4), new BlockPos(6, 3, 4), new BlockPos(4, 3, 5))) {
            require(helper, visited.contains(position(origin, station, rotation)), "Safe rack bypass and cage-front access: " + station);
        }
        for (String feature : List.of("skulls", "candles")) {
            for (var values : features.getAsJsonArray(feature)) {
                BlockPos actual = position(origin, coordinate(values.getAsJsonArray()), rotation);
                BlockPos below = actual.below();
                require(helper, level.getBlockState(below).isFaceSturdy(level, below, Direction.UP), "Full support for " + feature);
                if (feature.equals("skulls")) {
                    require(helper, level.getBlockEntity(actual) instanceof SkullBlockEntity, "Native skull block entity");
                }
            }
        }
        BlockPos banner = position(origin, coordinate(features.getAsJsonArray("banner")), rotation);
        require(helper, level.getBlockEntity(banner) instanceof BannerBlockEntity, "Native banner block entity");
        for (var values : features.getAsJsonArray("chains")) {
            BlockPos actual = position(origin, coordinate(values.getAsJsonArray()), rotation);
            BlockState chain = level.getBlockState(actual);
            require(helper, chain.is(Blocks.CHAIN) && chain.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y, "Vertical chain");
            BlockPos above = actual.above();
            BlockState support = level.getBlockState(above);
            require(helper, support.is(Blocks.CHAIN) || support.isFaceSturdy(level, above, Direction.DOWN), "Continuous chain-to-ceiling support");
        }
        for (var values : features.getAsJsonArray("rack")) {
            BlockPos below = position(origin, coordinate(values.getAsJsonArray()), rotation).below();
            require(helper, level.getBlockState(below).isFaceSturdy(level, below, Direction.UP), "Rack has full floor support");
        }
        for (var values : features.getAsJsonArray("lava_sources")) {
            BlockPos actual = position(origin, coordinate(values.getAsJsonArray()), rotation);
            require(helper, level.getBlockState(actual.above()).is(Blocks.ORANGE_STAINED_GLASS), "Lava sealed above with full glass");
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = actual.relative(direction);
                BlockState state = level.getBlockState(neighbor);
                require(helper, state.is(Blocks.LAVA) || state.isFaceSturdy(level, neighbor, direction.getOpposite()), "Lava enclosure on all six sides");
            }
        }
        return visited.size();
    }
}
