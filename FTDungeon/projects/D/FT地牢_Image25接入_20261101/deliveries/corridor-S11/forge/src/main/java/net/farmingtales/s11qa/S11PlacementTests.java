package net.farmingtales.s11qa;

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

@GameTestHolder("fts11qa")
@PrefixGameTestTemplate(false)
public final class S11PlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(13, 12, 16);
    private static final BlockPos ENTRANCE = new BlockPos(6, 3, 0);
    private static final BlockPos EXIT = new BlockPos(6, 3, 15);
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.TNT, Blocks.CHEST, Blocks.BARREL, Blocks.DISPENSER, Blocks.DROPPER, Blocks.HOPPER);

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
            if (HAZARDS.contains(state.getBlock()) || !state.getFluidState().isEmpty()
                    || !state.getCollisionShape(level, position).isEmpty()) return false;
        }
        BlockState floor = level.getBlockState(feet.below());
        return !HAZARDS.contains(floor.getBlock()) && floor.getFluidState().isEmpty()
                && floor.isFaceSturdy(level, feet.below(), Direction.UP)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static int walk(ServerLevel level, Placement placement) {
        for (int horizontal = 4; horizontal <= 8; horizontal++) {
            for (int depth = 0; depth < 16; depth++) {
                require(dryBody(level, transform(new BlockPos(horizontal, 3, depth), placement)), "Unsafe straight path cell");
            }
            for (int depth : new int[] {0, 15}) {
                require(level.getBlockState(transform(new BlockPos(horizontal, 6, depth), placement)).isAir(), "Port clearance lost");
            }
        }
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(ENTRANCE);
        pending.add(ENTRANCE);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() < 4 || next.getX() > 8 || next.getZ() < 0 || next.getZ() >= 16 || visited.contains(next)) continue;
                if (dryBody(level, transform(next, placement))) {
                    visited.add(next);
                    pending.add(next);
                }
            }
        }
        require(visited.size() == 80 && visited.contains(EXIT) && visited.contains(new BlockPos(6, 3, 3))
                && visited.contains(new BlockPos(6, 3, 12)), "Native collision walk failed");
        return visited.size();
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int lava = 0;
        int torches = 0;
        int chains = 0;
        int bars = 0;
        int trapdoors = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed at " + position + ": " + actual + " expected " + entry.getValue());
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (actual.is(Blocks.LAVA)) {
                lava++;
                require(actual.getFluidState().isSource(), "Lava stopped being a source");
                require(level.getBlockState(position.above()).is(Blocks.ORANGE_STAINED_GLASS), "Lava cover missing");
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Lava bottom missing");
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos side = position.relative(direction);
                    BlockState neighbor = level.getBlockState(side);
                    require(neighbor.is(Blocks.LAVA) || neighbor.isFaceSturdy(level, side, direction.getOpposite()), "Unsealed lava side");
                }
            }
            if (actual.is(Blocks.WALL_TORCH)) {
                torches++;
                require(actual.canSurvive(level, position), "Unsupported wall torch");
            }
            if (actual.is(Blocks.CHAIN)) {
                chains++;
                require(level.getBlockState(position.above()).isFaceSturdy(level, position.above(), Direction.DOWN), "Unanchored cage chain");
                require(level.getBlockState(position.below()).is(Blocks.IRON_TRAPDOOR), "Cage top disconnected");
            }
            if (actual.is(Blocks.IRON_BARS)) bars++;
            if (actual.is(Blocks.IRON_TRAPDOOR)) trapdoors++;
        }
        require(lava == 24 && torches == 6 && chains == 8 && bars == 38 && trapdoors == 32, "Theme feature count changed");
        for (int horizontal = 0; horizontal < 13; horizontal++) {
            for (int depth = 0; depth < 16; depth++) {
                BlockPos roof = transform(new BlockPos(horizontal, 11, depth), placement);
                require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Roof gap");
            }
        }
        BlockPos first = transform(BlockPos.ZERO, placement);
        BlockPos last = transform(SIZE.offset(-1, -1, -1), placement);
        BlockPos lower = new BlockPos(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()));
        BlockPos upper = new BlockPos(Math.max(first.getX(), last.getX()), last.getY(), Math.max(first.getZ(), last.getZ()));
        for (BlockPos position : BlockPos.betweenClosed(lower.offset(-1, -1, -1), upper.offset(1, 1, 1))) {
            if (!placement.expected().containsKey(position)) require(!level.getFluidState(position).is(FluidTags.LAVA), "Lava escaped template at " + position + " rotation=" + placement.rotation() + " lower=" + lower + " upper=" + upper);
        }
        AABB bounds = new AABB(lower, upper.offset(1, 1, 1)).inflate(1);
        require(level.getEntitiesOfClass(ItemEntity.class, bounds).isEmpty(), "Block or attachment dropped an item");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", placement.rotation().name());
        result.put("states_preserved", placement.expected().size());
        result.put("native_collision_reachable_path_cells", walk(level, placement));
        result.put("sealed_source_lava_cells", lava);
        result.put("supported_wall_torches", torches);
        result.put("anchored_chains", chains);
        result.put("stable_iron_bars", bars);
        result.put("stable_closed_cage_trapdoors", trapdoors);
        result.put("roof_blocks_verified", 208);
        result.put("fluid_leaks", 0);
        result.put("dropped_items", 0);
        return result;
    }

    @GameTest(template = "empty", batch = "s11_only", timeoutTicks = 600)
    public static void corridorSurvivesFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTest server is permitted");
        Path root = Path.of(System.getProperty("fts11qa.root")).toAbsolutePath().normalize();
        require(root.getFileName().toString().equals("corridor-S11") && Files.exists(root.resolve("forge/build.gradle")), "Wrong independent workspace");
        Path path = root.resolve("room.nbt");
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("S11") && metadata.get("sha256").getAsString().equals(digest), "Identity or hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(path.toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes")
                && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Wrong version, palette or entities");
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown/nonvanilla block " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            require(!FORBIDDEN.contains(block), "Forbidden block");
            CompoundTag properties = entry.getCompound("Properties");
            require(properties.size() == block.getStateDefinition().getProperties().size(), "Incomplete properties " + identifier);
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + key);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(SIZE), "Wrong template dimensions");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 2496, "Native roundtrip lost records");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getY() >= 0 && position.getZ() >= 0
                    && position.getX() < 13 && position.getY() < 12 && position.getZ() < 16, "Record out of bounds");
            require(local.put(position, states.get(record.getInt("state"))) == null, "Duplicate record");
            require(!record.contains("nbt"), "Unexpected block entity payload");
        }
        require(local.size() == 2496, "Incomplete explicit volume");
        List<Placement> placements = new ArrayList<>();
        Rotation[] rotations = Rotation.values();
        BlockPos[] origins = {new BlockPos(6, 2, 6), new BlockPos(45, 2, 6), new BlockPos(45, 2, 45), new BlockPos(6, 2, 45)};
        Set<BlockPos> occupied = new HashSet<>();
        for (int index = 0; index < rotations.length; index++) {
            BlockPos relativeOrigin = helper.absolutePos(origins[index]);
            BlockPos origin = new BlockPos(relativeOrigin.getX(), 290, relativeOrigin.getZ());
            Rotation rotation = rotations[index];
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Placement placement = new Placement(origin, rotation, expected);
            local.forEach((position, block) -> expected.put(transform(position, placement), block.rotate(rotation)));
            require(expected.keySet().stream().allMatch(occupied::add), "Overlapping QA placements");
            require(expected.keySet().stream().allMatch(position -> level.getFluidState(position).isEmpty()), "QA fixture contains fluid before placement");
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random,
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement rejected");
            inspect(level, placement);
            expected.forEach((position, block) -> {
                block.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
                level.updateNeighborsAt(position, block.getBlock());
                if (block.is(Blocks.LAVA)) level.scheduleTick(position, block.getFluidState().getType(), 1);
            });
            placements.add(placement);
        }
        System.out.println("S11_NATIVE_INITIAL_PASS: 9984 records, four rotations, forced lava and neighbour updates");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) evidence.add(inspect(level, placement));
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("id", "S11");
                result.put("status", "PASS");
                result.put("sha256", digest);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("test", "StructureTemplate.placeInWorld in isolated Forge GameTest server");
                result.put("ticks_observed", 240);
                result.put("fixture_origin_y", 290);
                result.put("fixture_preplacement_fluid_scan", "PASS");
                result.put("native_registry_and_complete_properties", "PASS");
                result.put("native_structure_roundtrip", "PASS");
                result.put("forced_neighbor_shape_and_event_updates", true);
                result.put("forced_source_lava_ticks", true);
                result.put("rotations", evidence);
                result.put("preplaced_entities", 0);
                result.put("block_entities", 0);
                result.put("loot_tables", 0);
                result.put("client_visual_tested", false);
                result.put("deployed", false);
                result.put("player_saves_touched", false);
                result.put("limits", List.of("Not a full modpack client, FPS or multiplayer test", "No catalog integration, live deployment or player world placement"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("S11_FORGE_PLACEMENT_PASS: four rotations stable after 240 ticks; sha256=" + digest);
                helper.succeed();
            } catch (Exception exception) {
                helper.fail(exception.toString());
            }
        });
    }
}
