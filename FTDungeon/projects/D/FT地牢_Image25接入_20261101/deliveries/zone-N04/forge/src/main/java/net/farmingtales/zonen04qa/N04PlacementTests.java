package net.farmingtales.zonen04qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftzn04qa")
@PrefixGameTestTemplate(false)
public final class N04PlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(35, 16, 35);
    private static final List<BlockPos> PORTS = List.of(new BlockPos(17, 2, 0), new BlockPos(34, 2, 17), new BlockPos(17, 2, 34), new BlockPos(0, 2, 17));
    private static final List<BlockPos> SAFE = List.of(new BlockPos(17, 2, 17), new BlockPos(17, 2, 6), new BlockPos(6, 2, 17), new BlockPos(28, 2, 17), new BlockPos(17, 2, 28));
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW, Blocks.SPAWNER, Blocks.TNT, Blocks.CHEST, Blocks.BARREL,
            Blocks.DISPENSER, Blocks.DROPPER, Blocks.HOPPER, Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK,
            Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_GATEWAY);

    private record Placement(BlockPos origin, Rotation rotation, Map<BlockPos, BlockState> expected) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, Placement placement) {
        return StructureTemplate.transform(local, Mirror.NONE, placement.rotation(), BlockPos.ZERO).offset(placement.origin());
    }

    private static boolean dryBody(ServerLevel level, BlockPos feet, int height) {
        for (int offset = 0; offset < height; offset++) {
            BlockPos position = feet.above(offset);
            BlockState state = level.getBlockState(position);
            if (!state.getFluidState().isEmpty() || !state.getCollisionShape(level, position).isEmpty() || FORBIDDEN.contains(state.getBlock())) return false;
        }
        BlockPos floor = feet.below();
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static int walk(ServerLevel level, Placement placement) {
        for (int blockX = 0; blockX < 35; blockX++) {
            for (int blockZ = 0; blockZ < 35; blockZ++) {
                if ((blockX >= 15 && blockX <= 19) || (blockZ >= 15 && blockZ <= 19))
                    require(dryBody(level, transform(new BlockPos(blockX, 2, blockZ), placement), 5), "Five-wide cross blocked: " + blockX + "," + blockZ);
            }
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        reached.add(PORTS.get(0));
        pending.add(PORTS.get(0));
        while (!pending.isEmpty()) {
            BlockPos position = pending.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = position.relative(direction);
                if (next.getX() < 0 || next.getX() > 34 || next.getZ() < 0 || next.getZ() > 34 || reached.contains(next)) continue;
                if (dryBody(level, transform(next, placement), 3)) {
                    reached.add(next);
                    pending.add(next);
                }
            }
        }
        require(reached.containsAll(PORTS) && reached.containsAll(SAFE), "Four ports or safe points unreachable");
        return reached.size();
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int hanging = 0;
        int standing = 0;
        int chains = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState state = level.getBlockState(position);
            require(state.equals(entry.getValue()), "State changed " + position + ": " + entry.getValue() + " -> " + state);
            require(state.canSurvive(level, position), "Unsupported block " + position + ": " + state);
            require(state.getFluidState().isEmpty() && !FORBIDDEN.contains(state.getBlock()), "Fluid or forbidden block");
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (state.is(Blocks.LANTERN)) {
                if (state.getValue(net.minecraft.world.level.block.LanternBlock.HANGING)) {
                    hanging++;
                    require(level.getBlockState(position.above()).is(Blocks.CHAIN), "Lantern not on chain");
                } else {
                    standing++;
                    require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Standing lamp unsupported");
                }
            }
            if (state.is(Blocks.CHAIN)) {
                chains++;
                BlockPos support = position.above();
                while (level.getBlockState(support).is(Blocks.CHAIN)) support = support.above();
                require(level.getBlockState(support).isFaceSturdy(level, support, Direction.DOWN), "Unanchored chain");
            }
        }
        require(hanging == 8 && standing == 4 && chains == 32, "Lamp feature counts changed");
        for (int blockX = 0; blockX < 35; blockX++) {
            for (int blockZ = 0; blockZ < 35; blockZ++) {
                for (int blockY : new int[] {0, 14, 15}) {
                    BlockPos position = transform(new BlockPos(blockX, blockY, blockZ), placement);
                    require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Roof or foundation gap");
                }
            }
        }
        BlockPos first = transform(BlockPos.ZERO, placement);
        BlockPos last = transform(SIZE.offset(-1, -1, -1), placement);
        BlockPos lower = new BlockPos(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()));
        BlockPos upper = new BlockPos(Math.max(first.getX(), last.getX()), last.getY(), Math.max(first.getZ(), last.getZ()));
        AABB bounds = new AABB(lower.getX() - 1, lower.getY() - 1, lower.getZ() - 1, upper.getX() + 2, upper.getY() + 2, upper.getZ() + 2);
        require(level.getEntitiesOfClass(ItemEntity.class, bounds).isEmpty(), "Dropped blocks after updates");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", placement.rotation().name());
        result.put("origin", List.of(placement.origin().getX(), placement.origin().getY(), placement.origin().getZ()));
        result.put("block_states_unchanged", placement.expected().size());
        result.put("native_collision_reachable_feet", walk(level, placement));
        result.put("four_ports_reachable", true);
        result.put("five_safe_points_reachable", true);
        result.put("five_wide_five_high_cross_cells", 325);
        result.put("hanging_lanterns", hanging);
        result.put("standing_lanterns", standing);
        result.put("anchored_chains", chains);
        result.put("roof_cells", 2450);
        result.put("foundation_cells", 1225);
        result.put("fluids", 0);
        result.put("runtime_portals", 0);
        result.put("dropped_items", 0);
        result.put("block_entities", 0);
        return result;
    }

    @GameTest(template = "empty", batch = "zone_n04_only", timeoutTicks = 700)
    public static void hubSurvivesFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only isolated GameTest servers allowed");
        Path root = Path.of(System.getProperty("ftzn04qa.root")).toAbsolutePath().normalize();
        require(root.getFileName().toString().equals("zone-N04"), "Wrong delivery directory");
        Path input = root.resolve("room.nbt");
        String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(input)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("zone-N04") && metadata.get("sha256").getAsString().equals(sha256), "Metadata hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(input.toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes") && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Bad version/palettes/entities");
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown block " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            require(!FORBIDDEN.contains(block), "Forbidden block");
            CompoundTag properties = entry.getCompound("Properties");
            require(properties.size() == block.getStateDefinition().getProperties().size(), "Incomplete properties " + identifier);
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identifier + " " + key);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(SIZE), "Wrong size");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 19600, "Roundtrip lost records");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getX() < 35 && position.getY() >= 0 && position.getY() < 16 && position.getZ() >= 0 && position.getZ() < 35, "Bounds");
            require(!record.contains("nbt"), "Unexpected block entity payload");
            require(local.put(position, states.get(record.getInt("state"))) == null, "Duplicate record");
        }
        require(local.size() == 19600, "Incomplete volume");
        List<Placement> placements = new ArrayList<>();
        BlockPos[] origins = {new BlockPos(4, 2, 4), new BlockPos(100, 2, 4), new BlockPos(100, 2, 100), new BlockPos(4, 2, 100)};
        Rotation[] rotations = Rotation.values();
        Set<BlockPos> allPositions = new HashSet<>();
        for (int index = 0; index < rotations.length; index++) {
            BlockPos horizontalOrigin = helper.absolutePos(origins[index]);
            BlockPos origin = new BlockPos(horizontalOrigin.getX(), 200, horizontalOrigin.getZ());
            Rotation rotation = rotations[index];
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Placement placement = new Placement(origin, rotation, expected);
            local.forEach((position, state) -> expected.put(transform(position, placement), state.rotate(rotation)));
            require(expected.keySet().stream().allMatch(allPositions::add), "QA placements overlap");
            require(expected.keySet().stream().allMatch(position -> level.getBlockState(position).isAir()), "QA site is not empty air");
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Placement failed");
            placements.add(placement);
            inspect(level, placement);
            expected.forEach((position, state) -> {
                state.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
                level.updateNeighborsAt(position, state.getBlock());
            });
        }
        System.out.println("N04_INITIAL_PASS: 4 rotations; neighbor updates forced");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) evidence.add(inspect(level, placement));
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("id", "zone-N04");
                report.put("status", "PASS");
                report.put("sha256", sha256);
                report.put("minecraft", "1.20.1");
                report.put("forge", "47.4.10");
                report.put("native_registry_complete_properties", "PASS");
                report.put("native_structure_roundtrip", "PASS");
                report.put("test", "StructureTemplate.placeInWorld in isolated Forge GameTest");
                report.put("ticks_observed", 240);
                report.put("forced_neighbor_updates", true);
                report.put("rotations", evidence);
                report.put("client_visual_tested", false);
                report.put("deployed", false);
                report.put("limits", List.of("No modpack client, multiplayer, FPS or hall-route integration test", "No player world, runtime portals, occupancy or persistence implementation"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("N04_FORGE_PASS: four rotations stable after 240 ticks; sha256=" + sha256);
                helper.succeed();
            } catch (Exception exception) {
                helper.fail(exception.toString());
            }
        });
    }
}
