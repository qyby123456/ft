package net.farmingtales.corridors07qa;

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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftcorridors07qa")
@PrefixGameTestTemplate(false)
public final class S07PlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(13, 11, 16);
    private static final BlockPos ENTRANCE = new BlockPos(6, 2, 0);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW,
            Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW, Blocks.SPAWNER, Blocks.TNT,
            Blocks.CHEST, Blocks.BARREL, Blocks.DISPENSER, Blocks.DROPPER, Blocks.HOPPER);

    private record Placement(BlockPos origin, Rotation rotation, Map<BlockPos, BlockState> expected) {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static BlockPos transform(BlockPos local, Placement placement) {
        return StructureTemplate.transform(local, Mirror.NONE, placement.rotation(), BlockPos.ZERO).offset(placement.origin());
    }

    private static boolean dryBody(ServerLevel level, BlockPos feet) {
        for (int height = 0; height < 4; height++) {
            BlockPos position = feet.above(height);
            BlockState state = level.getBlockState(position);
            if (FORBIDDEN.contains(state.getBlock()) || !state.getFluidState().isEmpty()
                    || !state.getCollisionShape(level, position).isEmpty()) {
                return false;
            }
        }
        BlockState floor = level.getBlockState(feet.below());
        return floor.getFluidState().isEmpty() && !FORBIDDEN.contains(floor.getBlock())
                && floor.isFaceSturdy(level, feet.below(), Direction.UP)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static int walk(ServerLevel level, Placement placement) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        require(dryBody(level, transform(ENTRANCE, placement)), "Entrance is unsafe");
        visited.add(ENTRANCE);
        pending.add(ENTRANCE);
        while (!pending.isEmpty()) {
            BlockPos local = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = local.relative(direction);
                if (next.getX() < 0 || next.getX() >= SIZE.getX() || next.getZ() < 0 || next.getZ() >= SIZE.getZ() || visited.contains(next)) {
                    continue;
                }
                if (dryBody(level, transform(next, placement))) {
                    visited.add(next);
                    pending.addLast(next);
                }
            }
        }
        for (int horizontal = 5; horizontal <= 7; horizontal++) {
            for (int depth = 0; depth < 16; depth++) {
                require(visited.contains(new BlockPos(horizontal, 2, depth)), "Continuous three-wide route failed: " + placement.rotation());
            }
        }
        return visited.size();
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int water = 0;
        int rods = 0;
        int chains = 0;
        int bars = 0;
        int crystals = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed at " + position + ": expected " + entry.getValue() + ", got " + actual);
            require(actual.canSurvive(level, position), "Native attachment survival failed at " + position);
            require(level.getBlockEntity(position) == null, "Unexpected inventory/block entity");
            if (actual.is(Blocks.WATER)) {
                water++;
                require(actual.getFluidState().isSource(), "Non-source water");
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos side = position.relative(direction);
                    require(level.getFluidState(side).is(FluidTags.WATER)
                            || level.getBlockState(side).isFaceSturdy(level, side, direction.getOpposite()), "Unsealed water side");
                }
                BlockPos bottom = position.below();
                require(level.getBlockState(bottom).isFaceSturdy(level, bottom, Direction.UP), "Unsealed basin floor");
                level.scheduleTick(position, actual.getFluidState().getType(), 1);
            }
            if (actual.is(Blocks.END_ROD) || actual.is(Blocks.AMETHYST_CLUSTER)
                    || actual.is(Blocks.LARGE_AMETHYST_BUD) || actual.is(Blocks.MEDIUM_AMETHYST_BUD)) {
                Direction facing = actual.getValue(BlockStateProperties.FACING);
                BlockPos support = position.relative(facing.getOpposite());
                require(level.getBlockState(support).isFaceSturdy(level, support, facing), "Crystal/light lacks physical support at " + position);
                if (actual.is(Blocks.END_ROD)) {
                    rods++;
                } else {
                    crystals++;
                }
            }
            if (actual.is(Blocks.CHAIN)) {
                chains++;
                require(actual.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y
                        && level.getBlockState(position.above()).isFaceSturdy(level, position.above(), Direction.DOWN)
                        && level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Cage suspension lacks roof/cap anchor");
            }
            if (actual.is(Blocks.IRON_BARS)) {
                bars++;
            }
        }
        require(water == 72 && rods == 12 && chains == 8 && bars == 32 && crystals == 15, "Theme counts changed");
        BlockPos first = transform(BlockPos.ZERO, placement);
        BlockPos last = transform(SIZE.offset(-1, -1, -1), placement);
        BlockPos lower = new BlockPos(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()));
        BlockPos upper = new BlockPos(Math.max(first.getX(), last.getX()), last.getY(), Math.max(first.getZ(), last.getZ()));
        for (BlockPos position : BlockPos.betweenClosed(lower.offset(-2, -1, -2), upper.offset(2, 1, 2))) {
            if (!placement.expected().containsKey(position)) {
                require(level.getFluidState(position).isEmpty(), "Fluid escaped outside template: " + position);
            }
        }
        AABB bounds = new AABB(lower.getX() - 2, lower.getY() - 1, lower.getZ() - 2, upper.getX() + 3, upper.getY() + 2, upper.getZ() + 3);
        List<ItemEntity> droppedItems = level.getEntitiesOfClass(ItemEntity.class, bounds);
        require(droppedItems.isEmpty(), "Item entities: " + droppedItems.stream().map(item -> item.getItem() + " at " + item.position()).toList());
        for (int horizontal = 0; horizontal < 13; horizontal++) {
            for (int depth = 0; depth < 16; depth++) {
                for (int vertical : new int[] {0, 10}) {
                    BlockPos surface = transform(new BlockPos(horizontal, vertical, depth), placement);
                    require(level.getBlockState(surface).isFaceSturdy(level, surface, Direction.UP), "Roof/base gap");
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", placement.rotation().name());
        result.put("states_preserved", placement.expected().size());
        result.put("collision_reachable_positions", walk(level, placement));
        result.put("continuous_dry_route_cells", 48);
        result.put("clear_height", 4);
        result.put("sealed_source_water_cells", water);
        result.put("end_rods", rods);
        result.put("roof_anchored_cage_chains", chains);
        result.put("stable_iron_bars", bars);
        result.put("supported_crystals", crystals);
        result.put("roof_and_base_blocks_verified", 416);
        result.put("fluid_leaks", 0);
        result.put("dropped_items", 0);
        return result;
    }

    @GameTest(templateNamespace = "ftcorridors07qa", template = "empty", batch = "corridor_s07_only", timeoutTicks = 600)
    public static void floodedCrystalCorridorFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTest server is permitted");
        Path root = Path.of(System.getProperty("ftcorridors07qa.root")).toAbsolutePath().normalize();
        require(Files.exists(root.resolve("forge/build.gradle")) && root.getFileName().toString().equals("corridor-S07"), "Wrong independent workspace");
        Files.createDirectories(root.resolve("qa"));
        Path path = root.resolve("room.nbt");
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("corridor-S07") && metadata.get("sha256").getAsString().equals(digest), "Metadata identity/hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(path.toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes") && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Wrong version, multi-palette or entities");
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown/nonvanilla block");
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            require(!FORBIDDEN.contains(block), "Forbidden block");
            CompoundTag properties = entry.getCompound("Properties");
            require(properties.size() == block.getStateDefinition().getProperties().size(), "Incomplete state properties: " + identifier);
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property: " + identifier + " " + key);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(SIZE), "Wrong template dimensions");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 2288, "Native roundtrip lost records");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getY() >= 0 && position.getZ() >= 0 && position.getX() < 13 && position.getY() < 11 && position.getZ() < 16, "Out of bounds record");
            require(!record.contains("nbt") && local.put(position, states.get(record.getInt("state"))) == null, "Block entity or duplicate record");
        }
        require(local.size() == 2288, "Incomplete volume");
        List<Placement> placements = new ArrayList<>();
        Rotation[] rotations = Rotation.values();
        BlockPos[] origins = {new BlockPos(6, 180, 6), new BlockPos(45, 180, 6), new BlockPos(45, 180, 45), new BlockPos(6, 180, 45)};
        Set<BlockPos> allPositions = new HashSet<>();
        for (int index = 0; index < rotations.length; index++) {
            BlockPos origin = helper.absolutePos(origins[index]);
            Rotation rotation = rotations[index];
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Placement placement = new Placement(origin, rotation, expected);
            local.forEach((position, block) -> expected.put(transform(position, placement), block.rotate(rotation)));
            require(expected.keySet().stream().allMatch(allPositions::add), "Test placements overlap");
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement rejected");
            placements.add(placement);
            inspect(level, placement);
            expected.forEach((position, block) -> {
                block.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
                level.updateNeighborsAt(position, block.getBlock());
            });
        }
        System.out.println("S07_NATIVE_INITIAL_PASS: four rotations and 9152 block records; forcing neighbor updates and source water ticks");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) {
                    evidence.add(inspect(level, placement));
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("id", "corridor-S07");
                result.put("status", "PASS");
                result.put("sha256", digest);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("test", "StructureTemplate.placeInWorld in isolated Forge GameTest server");
                result.put("ticks_observed", 240);
                result.put("native_registry_and_complete_properties", "PASS");
                result.put("native_structure_roundtrip", "PASS");
                result.put("forced_neighbor_shape_and_event_updates", true);
                result.put("forced_source_water_ticks", true);
                result.put("rotations", evidence);
                result.put("preplaced_entities", 0);
                result.put("inventory_items", 0);
                result.put("loot_tables", 0);
                result.put("client_visual_tested", false);
                result.put("player_save_modified", false);
                result.put("test_environment", "Void-biome flat isolated GameTest world; placements elevated 180 blocks relative to test origin to exclude terrain and vegetation");
                result.put("deployed", false);
                result.put("limits", List.of("No full modpack client screenshots or gameplay test", "Original full-width flood converted to side basins and central dry path", "No catalog slot allocation, hall integration, player-world placement or deployment"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(result) + System.lineSeparator(), StandardCharsets.UTF_8);
                System.out.println("S07_FORGE_PLACEMENT_PASS: four rotations stable after 240 ticks; no fluid leaks, changed blocks or item drops; sha256=" + digest);
                helper.succeed();
            } catch (Exception exception) {
                helper.fail(exception.toString());
            }
        });
    }
}
