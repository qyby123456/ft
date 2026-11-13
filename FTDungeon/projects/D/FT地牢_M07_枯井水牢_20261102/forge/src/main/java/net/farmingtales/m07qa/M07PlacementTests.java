package net.farmingtales.m07qa;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftm07qa")
@PrefixGameTestTemplate(false)
public final class M07PlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(15, 13, 17);
    private static final BlockPos ENTRANCE = new BlockPos(7, 4, 0);
    private static final List<BlockPos> TARGETS = List.of(new BlockPos(7, 4, 3), new BlockPos(3, 4, 9),
            new BlockPos(11, 4, 9), new BlockPos(7, 4, 13), new BlockPos(7, 4, 15));
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.TNT, Blocks.CHEST, Blocks.BARREL, Blocks.DISPENSER, Blocks.DROPPER, Blocks.HOPPER);

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
        for (int height = 0; height < 2; height++) {
            BlockPos position = feet.above(height);
            BlockState state = level.getBlockState(position);
            if (HAZARDS.contains(state.getBlock()) || !state.getFluidState().isEmpty()
                    || !state.getCollisionShape(level, position).isEmpty()) {
                return false;
            }
        }
        BlockState floor = level.getBlockState(feet.below());
        return !HAZARDS.contains(floor.getBlock()) && floor.getFluidState().isEmpty()
                && floor.isFaceSturdy(level, feet.below(), Direction.UP)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static int walk(ServerLevel level, Placement placement) {
        for (int offset = -1; offset <= 1; offset++) {
            BlockPos feet = transform(ENTRANCE.offset(offset, 0, 0), placement);
            require(dryBody(level, feet) && level.getBlockState(feet.above(2)).isAir(), "Obstructed 3x3 entrance: " + feet);
        }
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(ENTRANCE);
        pending.add(ENTRANCE);
        while (!pending.isEmpty()) {
            BlockPos local = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = local.relative(direction);
                if (next.getX() < 0 || next.getX() >= SIZE.getX() || next.getZ() < 0 || next.getZ() >= SIZE.getZ()
                        || visited.contains(next)) {
                    continue;
                }
                if (dryBody(level, transform(next, placement))) {
                    visited.add(next);
                    pending.addLast(next);
                }
            }
        }
        require(visited.containsAll(TARGETS), "Collision route to safe points or well circuit failed: " + placement.rotation());
        for (int blockX = 6; blockX <= 8; blockX++) {
            for (int blockZ = 0; blockZ <= 5; blockZ++) {
                require(visited.contains(new BlockPos(blockX, 4, blockZ)), "Front approach narrowed");
            }
        }
        return visited.size();
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int skulls = 0;
        int lanterns = 0;
        int chains = 0;
        int hooks = 0;
        int water = 0;
        int grates = 0;
        int bars = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed at " + position + ": expected " + entry.getValue() + ", got " + actual);
            require(actual.canSurvive(level, position), "Native canSurvive failed: " + actual + " at " + position);
            BlockEntity blockEntity = level.getBlockEntity(position);
            if (blockEntity != null) {
                require(blockEntity instanceof SkullBlockEntity && actual.is(Blocks.SKELETON_SKULL), "Unexpected block entity");
                CompoundTag saved = blockEntity.saveWithoutMetadata();
                require(!saved.contains("LootTable") && !saved.contains("Items") && !saved.contains("SkullOwner"), "Unexpected inventory/loot/owner");
                skulls++;
            }
            if (actual.is(Blocks.LANTERN)) {
                lanterns++;
                require(actual.getValue(BlockStateProperties.HANGING) && level.getBlockState(position.above()).is(Blocks.CHAIN), "Lantern lacks chain");
            }
            if (actual.is(Blocks.CHAIN)) {
                chains++;
                require(actual.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y, "Suspension is not vertical");
                BlockPos anchor = position.above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN)) {
                    anchor = anchor.above();
                }
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Chain has no solid overhead anchor");
            }
            if (actual.is(Blocks.TRIPWIRE_HOOK)) {
                hooks++;
                BlockPos support = position.relative(actual.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
                require(level.getBlockState(support).isFaceSturdy(level, support, actual.getValue(BlockStateProperties.HORIZONTAL_FACING)), "Wall shackle lacks masonry");
                require(!actual.getValue(BlockStateProperties.ATTACHED) && !actual.getValue(BlockStateProperties.POWERED), "Decorative hook activated");
            }
            if (actual.is(Blocks.IRON_BARS)) {
                bars++;
            }
            if (actual.is(Blocks.IRON_TRAPDOOR)) {
                grates++;
                require(!actual.getValue(BlockStateProperties.OPEN) && !actual.getValue(BlockStateProperties.POWERED)
                        && !actual.getValue(BlockStateProperties.WATERLOGGED), "Grate changed or became waterlogged");
                boolean supported = level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP);
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos side = position.relative(direction);
                    supported |= level.getBlockState(side).isFaceSturdy(level, side, direction.getOpposite());
                }
                require(supported, "Grate lacks masonry hinge or central support");
            }
            if (actual.is(Blocks.WATER)) {
                water++;
                require(actual.getFluidState().isSource(), "Water is not a source");
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos side = position.relative(direction);
                    require(level.getFluidState(side).is(FluidTags.WATER)
                            || level.getBlockState(side).isFaceSturdy(level, side, direction.getOpposite()), "Water side not sealed");
                }
                BlockPos bottom = position.below();
                require(level.getFluidState(bottom).is(FluidTags.WATER)
                        || level.getBlockState(bottom).isFaceSturdy(level, bottom, Direction.UP), "Water bottom not sealed");
                BlockPos above = position.above();
                require(level.getFluidState(above).is(FluidTags.WATER) || level.getBlockState(above).is(Blocks.IRON_TRAPDOOR), "Water lid missing");
                level.scheduleTick(position, actual.getFluidState().getType(), 1);
            }
        }
        require(skulls == 2 && lanterns == 4 && chains == 30 && hooks == 4 && water == 33 && grates == 18 && bars == 18, "Theme feature counts changed");
        BlockPos first = transform(BlockPos.ZERO, placement);
        BlockPos last = transform(SIZE.offset(-1, -1, -1), placement);
        BlockPos lower = new BlockPos(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()));
        BlockPos upper = new BlockPos(Math.max(first.getX(), last.getX()), last.getY(), Math.max(first.getZ(), last.getZ()));
        for (BlockPos position : BlockPos.betweenClosed(lower.offset(-1, -1, -1), upper.offset(1, 1, 1))) {
            if (!placement.expected().containsKey(position)) {
                require(level.getFluidState(position).isEmpty(), "Fluid escaped outside template: " + position);
            }
        }
        AABB bounds = new AABB(lower.getX() - 1, lower.getY() - 1, lower.getZ() - 1,
                upper.getX() + 2, upper.getY() + 2, upper.getZ() + 2);
        require(level.getEntitiesOfClass(ItemEntity.class, bounds).isEmpty(), "Attachment or block dropped an item");
        for (int blockX = 0; blockX < SIZE.getX(); blockX++) {
            for (int blockZ = 0; blockZ < SIZE.getZ(); blockZ++) {
                for (int blockY : new int[] {11, 12}) {
                    BlockPos roof = transform(new BlockPos(blockX, blockY, blockZ), placement);
                    require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.UP), "Roof has a gap");
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", placement.rotation().name());
        result.put("states_preserved", placement.expected().size());
        result.put("collision_reachable_positions", walk(level, placement));
        result.put("skull_block_entities", skulls);
        result.put("hanging_lanterns", lanterns);
        result.put("overhead_anchored_chains", chains);
        result.put("wall_anchored_hooks", hooks);
        result.put("sealed_source_water_cells", water);
        result.put("supported_closed_iron_grates", grates);
        result.put("stable_iron_bars", bars);
        result.put("roof_blocks_verified", 510);
        result.put("fluid_leaks", 0);
        result.put("dropped_items", 0);
        return result;
    }

    @GameTest(template = "empty", batch = "m07_only", timeoutTicks = 600)
    public static void waterPrisonSurvivesFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTest server is permitted");
        Path root = Path.of(System.getProperty("ftm07qa.root")).toAbsolutePath().normalize();
        require(Files.exists(root.resolve("forge/build.gradle")), "Expected independent M07 workspace");
        Path path = root.resolve("out/M07/room.nbt");
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("out/M07/room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("M07") && metadata.get("sha256").getAsString().equals(digest), "Metadata identity/hash mismatch");
        require(metadata.getAsJsonArray("entrance").get(1).getAsInt() == 4, "Wrong entrance elevation");
        CompoundTag tag = NbtIo.readCompressed(path.toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes")
                && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Wrong version, multiple palettes or entities");
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown or nonvanilla block: " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            require(!FORBIDDEN.contains(block) && !HAZARDS.contains(block), "Forbidden/hazardous block");
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
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 3315, "Native roundtrip lost records");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getY() >= 0 && position.getZ() >= 0
                    && position.getX() < SIZE.getX() && position.getY() < SIZE.getY() && position.getZ() < SIZE.getZ(), "Out of bounds record");
            require(local.put(position, states.get(record.getInt("state"))) == null, "Duplicate record");
            if (record.contains("nbt")) {
                CompoundTag blockEntity = record.getCompound("nbt");
                require(blockEntity.size() == 1 && blockEntity.getString("id").equals("minecraft:skull"), "Forbidden block entity payload");
            }
        }
        require(local.size() == 3315, "Incomplete volume");
        List<Placement> placements = new ArrayList<>();
        Rotation[] rotations = Rotation.values();
        BlockPos[] origins = {new BlockPos(6, 2, 6), new BlockPos(45, 2, 6), new BlockPos(45, 2, 45), new BlockPos(6, 2, 45)};
        Set<BlockPos> allPositions = new HashSet<>();
        for (int index = 0; index < rotations.length; index++) {
            BlockPos origin = helper.absolutePos(origins[index]);
            Rotation rotation = rotations[index];
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Placement placement = new Placement(origin, rotation, expected);
            local.forEach((position, block) -> expected.put(transform(position, placement), block.rotate(rotation)));
            require(expected.keySet().stream().allMatch(allPositions::add), "Test placements overlap");
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random,
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement rejected");
            placements.add(placement);
            inspect(level, placement);
            expected.forEach((position, block) -> {
                block.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
                level.updateNeighborsAt(position, block.getBlock());
            });
        }
        System.out.println("M07_NATIVE_INITIAL_PASS: four rotations, 13260 records, source water ticks and forced neighbour shape updates");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) {
                    evidence.add(inspect(level, placement));
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("id", "M07");
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
                result.put("limits", List.of("No full modpack client screenshot, FPS or multiplayer play test", "Static winch, shackles and grates are not scripted gameplay traps", "No core catalog edits, hall connection, permanent-world placement or deployment"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("M07_FORGE_PLACEMENT_PASS: four rotations stable after 240 ticks, no fluid leaks or item drops; sha256=" + digest);
                helper.succeed();
            } catch (Exception exception) {
                helper.fail(exception.toString());
            }
        });
    }
}
