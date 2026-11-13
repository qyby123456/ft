package net.farmingtales.corridorm07qa;

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

@GameTestHolder("ftcorridorm07qa")
@PrefixGameTestTemplate(false)
public final class CorridorM07Tests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(17, 13, 41);
    private static final BlockPos ENTRANCE = new BlockPos(8, 1, 0);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.TNT, Blocks.CHEST, Blocks.BARREL, Blocks.HOPPER, Blocks.DISPENSER,
            Blocks.DROPPER, Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK,
            Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW);
    private record Placement(BlockPos origin, Rotation rotation, Map<BlockPos, BlockState> expected) {}
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static BlockPos transform(BlockPos local, Placement placement) {
        return StructureTemplate.transform(local, Mirror.NONE, placement.rotation(), BlockPos.ZERO).offset(placement.origin());
    }
    private static Map<BlockPos, BlockState> decode(CompoundTag tag) {
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown block " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            require(!FORBIDDEN.contains(block), "Forbidden block " + identifier);
            CompoundTag properties = entry.getCompound("Properties");
            require(properties.size() == block.getStateDefinition().getProperties().size(), "Incomplete properties " + identifier);
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identifier + " " + key);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag entry = records.getCompound(index);
            ListTag coordinates = entry.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getX() < 17 && position.getY() >= 0 && position.getY() < 13
                    && position.getZ() >= 0 && position.getZ() < 41, "Out of bounds record");
            require(!entry.contains("nbt"), "Unexpected block entity payload");
            require(result.put(position, states.get(entry.getInt("state"))) == null, "Duplicate coordinate");
        }
        require(result.size() == 9061, "Incomplete template volume");
        return result;
    }
    private static boolean safeFeet(ServerLevel level, BlockPos feet) {
        for (int height = 0; height < 2; height++) {
            BlockPos position = feet.above(height);
            BlockState state = level.getBlockState(position);
            if (FORBIDDEN.contains(state.getBlock()) || !state.getFluidState().isEmpty()
                    || !state.getCollisionShape(level, position).isEmpty()) return false;
        }
        BlockState floor = level.getBlockState(feet.below());
        return !FORBIDDEN.contains(floor.getBlock()) && floor.getFluidState().isEmpty()
                && floor.isFaceSturdy(level, feet.below(), Direction.UP)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }
    private static int walk(ServerLevel level, Placement placement) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(ENTRANCE); pending.add(ENTRANCE);
        while (!pending.isEmpty()) {
            BlockPos position = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = position.relative(direction);
                if (next.getX() < 0 || next.getX() >= 17 || next.getZ() < 0 || next.getZ() >= 41 || visited.contains(next)) continue;
                if (safeFeet(level, transform(next, placement))) { visited.add(next); pending.add(next); }
            }
        }
        require(visited.contains(new BlockPos(8, 1, 3)) && visited.contains(new BlockPos(8, 1, 40)), "Disconnected interior or exit");
        for (int blockX = 6; blockX <= 10; blockX++) for (int blockZ = 0; blockZ <= 40; blockZ++) {
            BlockPos local = new BlockPos(blockX, 1, blockZ);
            require(visited.contains(local) && level.getBlockState(transform(local.above(2), placement)).isAir(), "Five-wide route obstructed");
        }
        return visited.size();
    }
    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int lanterns = 0;
        int chains = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState state = level.getBlockState(position);
            require(state.equals(entry.getValue()), "State changed " + position + ": " + entry.getValue() + " -> " + state);
            require(state.canSurvive(level, position), "Unsupported state " + state + " " + position);
            require(level.getBlockEntity(position) == null && state.getFluidState().isEmpty(), "Unexpected block entity or fluid");
            if (state.is(Blocks.LANTERN)) {
                lanterns++;
                require(level.getBlockState(position.above()).is(Blocks.CHAIN), "Lamp lost suspension chain");
            }
            if (state.is(Blocks.CHAIN)) {
                chains++;
                BlockState above = level.getBlockState(position.above());
                require(above.is(Blocks.CHAIN) || above.isFaceSturdy(level, position.above(), Direction.DOWN), "Unanchored chain");
            }
        }
        require(lanterns == 7 && chains == 14, "Lighting feature counts changed");
        int roofCells = 0;
        for (int blockX = 0; blockX < 17; blockX++) for (int blockZ = 0; blockZ < 41; blockZ++) {
            BlockPos floor = transform(new BlockPos(blockX, 0, blockZ), placement);
            require(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Foundation hole");
            for (int roofY : new int[] {11, 12}) {
                BlockPos roof = transform(new BlockPos(blockX, roofY, blockZ), placement);
                require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.UP), "Roof hole"); roofCells++;
            }
        }
        for (int endZ : new int[] {0, 40}) for (int blockX = 6; blockX <= 10; blockX++) for (int blockY = 1; blockY <= 6; blockY++)
            require(level.getBlockState(transform(new BlockPos(blockX, blockY, endZ), placement)).isAir(), "Portal obstruction");
        BlockPos first = transform(BlockPos.ZERO, placement);
        BlockPos last = transform(SIZE.offset(-1, -1, -1), placement);
        AABB bounds = new AABB(Math.min(first.getX(), last.getX()) - 1, first.getY() - 1, Math.min(first.getZ(), last.getZ()) - 1,
                Math.max(first.getX(), last.getX()) + 2, last.getY() + 2, Math.max(first.getZ(), last.getZ()) + 2);
        require(level.getEntitiesOfClass(ItemEntity.class, bounds).isEmpty(), "Dropped attachment or block item");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", placement.rotation().name());
        result.put("states_preserved", placement.expected().size());
        result.put("collision_reachable_feet", walk(level, placement));
        result.put("five_wide_route_cells", 205);
        result.put("hanging_lanterns", lanterns);
        result.put("anchored_chains", chains);
        result.put("double_roof_cells", roofCells);
        result.put("dropped_items", 0);
        result.put("unexpected_fluids", 0);
        return result;
    }
    @GameTest(template = "empty", batch = "corridor_m07_only", timeoutTicks = 800)
    public static void straightCorridorSurvivesFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Isolated GameTest server required");
        Path root = Path.of(System.getProperty("ftcorridorm07qa.root")).toAbsolutePath().normalize();
        require(root.getFileName().toString().equals("corridor-M07") && Files.exists(root.resolve("forge/build.gradle")), "Wrong isolated workspace");
        Files.createDirectories(root.resolve("qa"));
        Files.deleteIfExists(root.resolve("qa/forge-native.json"));
        Path source = root.resolve("room.nbt");
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("corridor-M07") && metadata.get("sha256").getAsString().equals(digest), "Identity or hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(source.toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes") && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Wrong NBT version or entities");
        Map<BlockPos, BlockState> local = decode(tag);
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(SIZE), "Wrong native dimensions");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(decode(roundtrip).equals(local) && roundtrip.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Native roundtrip changed content");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Files.writeString(root.resolve("qa/roundtrip.json"), JSON.toJson(Map.of("id", "corridor-M07", "status", "PASS", "sha256", digest,
                "native_states_equal", true, "recorded_cells", 9061, "block_entities", 0, "entities", 0)) + "\n", StandardCharsets.UTF_8);
        Rotation[] rotations = Rotation.values();
        BlockPos[] origins = {new BlockPos(10, 2, 10), new BlockPos(120, 2, 10), new BlockPos(120, 2, 120), new BlockPos(10, 2, 120)};
        List<Placement> placements = new ArrayList<>();
        Set<BlockPos> occupied = new HashSet<>();
        for (int index = 0; index < rotations.length; index++) {
            BlockPos relativeOrigin = helper.absolutePos(origins[index]);
            BlockPos origin = new BlockPos(relativeOrigin.getX(), 180, relativeOrigin.getZ());
            Rotation rotation = rotations[index];
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Placement placement = new Placement(origin, rotation, expected);
            local.forEach((position, state) -> expected.put(transform(position, placement), state.rotate(rotation)));
            require(expected.keySet().stream().allMatch(occupied::add), "Overlapping QA placements");
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), placement);
            BlockPos lower = new BlockPos(Math.min(origin.getX(), corner.getX()) - 1, 179, Math.min(origin.getZ(), corner.getZ()) - 1);
            BlockPos upper = new BlockPos(Math.max(origin.getX(), corner.getX()) + 1, 193, Math.max(origin.getZ(), corner.getZ()) + 1);
            for (BlockPos position : BlockPos.betweenClosed(lower, upper)) {
                require(level.getBlockState(position).isAir() && level.getFluidState(position).isEmpty(), "QA fixture is not empty: " + position);
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement failed");
            inspect(level, placement);
            expected.forEach((position, state) -> { state.updateNeighbourShapes(level, position, Block.UPDATE_ALL); level.updateNeighborsAt(position, state.getBlock()); });
            placements.add(placement);
        }
        System.out.println("CORRIDOR_M07_INITIAL_PASS: four nonoverlapping rotations; 36244 explicit records; forced neighbor updates");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) evidence.add(inspect(level, placement));
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("id", "corridor-M07"); result.put("status", "PASS"); result.put("sha256", digest);
                result.put("minecraft", "1.20.1"); result.put("forge", "47.4.10"); result.put("java", System.getProperty("java.version"));
                result.put("test", "StructureTemplate.placeInWorld in isolated Forge GameTest server");
                result.put("ticks_observed", 240); result.put("forced_neighbor_shape_and_event_updates", true);
                result.put("fixture_absolute_y", 180); result.put("preflight_empty_volume_with_one_block_margin", true);
                result.put("native_registry_complete_state_properties", "PASS"); result.put("native_roundtrip", "PASS");
                result.put("rotations", evidence); result.put("player_aabb", "0.6 x 1.8");
                result.put("client_visual_tested", false); result.put("player_saves_accessed", false); result.put("deployed", false);
                result.put("limits", List.of("No full modpack client or gameplay test", "Not FT hall integration, occupancy, persistence or deployment verification"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("CORRIDOR_M07_FORGE_PASS: four rotations stable after 240 ticks; sha256=" + digest);
                helper.succeed();
            } catch (Exception exception) { helper.fail(exception.toString()); }
        });
    }
}

