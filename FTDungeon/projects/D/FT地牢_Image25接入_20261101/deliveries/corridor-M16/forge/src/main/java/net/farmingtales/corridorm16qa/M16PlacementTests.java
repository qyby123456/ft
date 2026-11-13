package net.farmingtales.corridorm16qa;

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
import net.minecraft.tags.FluidTags;
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

@GameTestHolder("ftcm16qa")
@PrefixGameTestTemplate(false)
public final class M16PlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(15, 11, 40);
    private static final BlockPos ENTRANCE = new BlockPos(7, 2, 0);
    private static final BlockPos EXIT = new BlockPos(7, 2, 39);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.TNT, Blocks.CHEST, Blocks.BARREL, Blocks.DISPENSER, Blocks.DROPPER,
            Blocks.HOPPER, Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK,
            Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW);

    private record Placement(BlockPos origin, Rotation rotation, Map<BlockPos, BlockState> expected) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, Placement placement) {
        return StructureTemplate.transform(local, Mirror.NONE, placement.rotation(), BlockPos.ZERO).offset(placement.origin());
    }

    private static boolean dryBody(ServerLevel level, BlockPos feet) {
        for (int height = 0; height < 3; height++) {
            BlockPos position = feet.above(height);
            BlockState state = level.getBlockState(position);
            if (!state.getFluidState().isEmpty() || !state.getCollisionShape(level, position).isEmpty()
                    || FORBIDDEN.contains(state.getBlock())) return false;
        }
        BlockPos floorPosition = feet.below();
        BlockState floor = level.getBlockState(floorPosition);
        return floor.getFluidState().isEmpty() && floor.isFaceSturdy(level, floorPosition, Direction.UP)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static int walk(ServerLevel level, Placement placement) {
        for (int blockZ = 0; blockZ < 40; blockZ++) {
            for (int blockX = 5; blockX <= 9; blockX++) {
                require(dryBody(level, transform(new BlockPos(blockX, 2, blockZ), placement)),
                        "Blocked or wet 5x3 path: " + blockX + "," + blockZ);
            }
        }
        for (int blockZ : new int[] {0, 39}) {
            for (int blockX = 5; blockX <= 9; blockX++) {
                require(level.getBlockState(transform(new BlockPos(blockX, 5, blockZ), placement)).isAir(), "Arch is below four blocks");
            }
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        reached.add(ENTRANCE);
        pending.add(ENTRANCE);
        while (!pending.isEmpty()) {
            BlockPos position = pending.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = position.relative(direction);
                if (next.getX() < 2 || next.getX() > 12 || next.getZ() < 0 || next.getZ() > 39 || reached.contains(next)) continue;
                if (dryBody(level, transform(next, placement))) {
                    reached.add(next);
                    pending.add(next);
                }
            }
        }
        require(reached.contains(EXIT), "Exit not reachable with native collision shapes");
        require(reached.contains(new BlockPos(7, 2, 4)), "Interior safe point not reachable");
        return reached.size();
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement, boolean scheduleFluids) {
        int water = 0;
        int lanterns = 0;
        int chains = 0;
        int bars = 0;
        int vines = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState state = level.getBlockState(position);
            require(state.equals(entry.getValue()), "State changed at " + position + ": " + entry.getValue() + " => " + state);
            require(state.canSurvive(level, position), "Block cannot survive at " + position + ": " + state);
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (state.is(Blocks.WATER)) {
                water++;
                require(state.getFluidState().isSource(), "Non-source water");
                for (Direction direction : Direction.values()) {
                    if (direction == Direction.UP) continue;
                    BlockPos neighbor = position.relative(direction);
                    require(level.getFluidState(neighbor).is(FluidTags.WATER)
                                    || level.getBlockState(neighbor).isFaceSturdy(level, neighbor, direction.getOpposite()),
                            "Liquid side or bottom is not sealed at " + position + " toward " + direction);
                }
                if (scheduleFluids) level.scheduleTick(position, state.getFluidState().getType(), 1);
            }
            if (state.is(Blocks.LANTERN)) {
                lanterns++;
                require(level.getBlockState(position.above()).is(Blocks.CHAIN), "Lantern not suspended on chain");
            }
            if (state.is(Blocks.CHAIN)) {
                chains++;
                BlockPos support = position.above();
                while (level.getBlockState(support).is(Blocks.CHAIN)) support = support.above();
                require(level.getBlockState(support).isFaceSturdy(level, support, Direction.DOWN), "Chain has no overhead beam");
            }
            if (state.is(Blocks.IRON_BARS)) bars++;
            if (state.is(Blocks.VINE)) vines++;
        }
        require(water == 208 && lanterns == 12 && chains == 42 && bars == 48 && vines == 56, "Theme feature counts changed");
        for (int blockX = 0; blockX < 15; blockX++) {
            for (int blockZ = 0; blockZ < 40; blockZ++) {
                for (int blockY : new int[] {0, 10}) {
                    BlockPos position = transform(new BlockPos(blockX, blockY, blockZ), placement);
                    require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Foundation or roof has a gap");
                }
            }
        }
        BlockPos first = transform(BlockPos.ZERO, placement);
        BlockPos last = transform(SIZE.offset(-1, -1, -1), placement);
        BlockPos lower = new BlockPos(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()));
        BlockPos upper = new BlockPos(Math.max(first.getX(), last.getX()), last.getY(), Math.max(first.getZ(), last.getZ()));
        for (BlockPos position : BlockPos.betweenClosed(lower.offset(-1, -1, -1), upper.offset(1, 1, 1))) {
            if (!placement.expected().containsKey(position)) require(level.getFluidState(position).isEmpty(), "Fluid leaked beyond volume: " + position);
        }
        AABB bounds = new AABB(lower.getX() - 1, lower.getY() - 1, lower.getZ() - 1,
                upper.getX() + 2, upper.getY() + 2, upper.getZ() + 2);
        require(level.getEntitiesOfClass(ItemEntity.class, bounds).isEmpty(), "Blocks or attachments dropped items");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", placement.rotation().name());
        result.put("states_preserved", placement.expected().size());
        result.put("native_collision_reachable_positions", walk(level, placement));
        result.put("dry_path_cells", 200);
        result.put("source_water_cells", water);
        result.put("hanging_lanterns", lanterns);
        result.put("anchored_chains", chains);
        result.put("stable_iron_bars", bars);
        result.put("surviving_backed_vines", vines);
        result.put("roof_blocks_verified", 600);
        result.put("foundation_blocks_verified", 600);
        result.put("fluid_leaks", 0);
        result.put("dropped_items", 0);
        result.put("inventory_items", 0);
        return result;
    }

    @GameTest(template = "empty", batch = "corridor_m16_only", timeoutTicks = 600)
    public static void sealedCorridorSurvivesAllRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTest server is allowed");
        Path root = Path.of(System.getProperty("ftcm16qa.root")).toAbsolutePath().normalize();
        require(root.getFileName().toString().equals("corridor-M16"), "Unexpected delivery directory");
        Path path = root.resolve("room.nbt");
        String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("corridor-M16") && metadata.get("sha256").getAsString().equals(sha256), "Metadata identity or hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(path.toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes")
                && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Invalid version, palettes or entities");
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown or nonvanilla block: " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            require(!FORBIDDEN.contains(block), "Forbidden block: " + identifier);
            CompoundTag properties = entry.getCompound("Properties");
            require(properties.size() == block.getStateDefinition().getProperties().size(), "Incomplete properties: " + identifier);
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property: " + identifier + " " + key);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(SIZE), "Wrong dimensions");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 6600, "Native roundtrip lost records");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getY() >= 0 && position.getZ() >= 0
                    && position.getX() < SIZE.getX() && position.getY() < SIZE.getY() && position.getZ() < SIZE.getZ(), "Out of bounds record");
            require(!record.contains("nbt"), "Unexpected block entity payload");
            require(local.put(position, states.get(record.getInt("state"))) == null, "Duplicate record");
        }
        require(local.size() == 6600, "Incomplete volume");
        List<Placement> placements = new ArrayList<>();
        BlockPos[] origins = {new BlockPos(4, 2, 4), new BlockPos(100, 2, 4), new BlockPos(100, 2, 100), new BlockPos(4, 2, 100)};
        Rotation[] rotations = Rotation.values();
        Set<BlockPos> allPositions = new HashSet<>();
        for (int index = 0; index < rotations.length; index++) {
            BlockPos origin = helper.absolutePos(origins[index]);
            Rotation rotation = rotations[index];
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Placement placement = new Placement(origin, rotation, expected);
            local.forEach((position, state) -> expected.put(transform(position, placement), state.rotate(rotation)));
            require(expected.keySet().stream().allMatch(allPositions::add), "Test placements overlap");
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random,
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement rejected");
            placements.add(placement);
            inspect(level, placement, true);
            expected.forEach((position, state) -> {
                state.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
                level.updateNeighborsAt(position, state.getBlock());
            });
        }
        System.out.println("M16_INITIAL_PASS: four rotations, forced water ticks and neighbor updates");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) evidence.add(inspect(level, placement, false));
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("id", "corridor-M16");
                report.put("status", "PASS");
                report.put("sha256", sha256);
                report.put("minecraft", "1.20.1");
                report.put("forge", "47.4.10");
                report.put("native_registry_complete_properties", "PASS");
                report.put("native_structure_roundtrip", "PASS");
                report.put("test", "StructureTemplate.placeInWorld in a fresh isolated Forge GameTest server");
                report.put("ticks_observed", 240);
                report.put("forced_fluid_ticks", true);
                report.put("forced_neighbor_updates", true);
                report.put("rotations", evidence);
                report.put("client_visual_tested", false);
                report.put("deployed", false);
                report.put("limits", List.of("No modpack client, multiplayer, FPS or hall-route integration test", "No player save access or permanent placement", "No shared core or runtime catalog edits"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("M16_FORGE_PASS: all four rotations stable after 240 ticks; sha256=" + sha256);
                helper.succeed();
            } catch (Exception exception) {
                helper.fail(exception.toString());
            }
        });
    }
}
