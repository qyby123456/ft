package net.farmingtales.zoner03qa;

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
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftzoner03qa")
@PrefixGameTestTemplate(false)
public final class ZoneR03Tests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(25, 16, 27);
    private static final BlockPos ENTRANCE = new BlockPos(12, 2, 0);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.TNT, Blocks.CHEST, Blocks.BARREL, Blocks.HOPPER, Blocks.DISPENSER,
            Blocks.DROPPER, Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK,
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
            require(position.getX() >= 0 && position.getX() < 25 && position.getY() >= 0 && position.getY() < 16 && position.getZ() >= 0 && position.getZ() < 27, "Out of bounds record");
            require(!entry.contains("nbt"), "Unexpected block entity payload");
            require(result.put(position, states.get(entry.getInt("state"))) == null, "Duplicate coordinate");
        }
        require(result.size() == 10800, "Incomplete template volume");
        return result;
    }
    private static boolean safeFeet(ServerLevel level, BlockPos feet) {
        for (int height = 0; height < 2; height++) {
            BlockPos position = feet.above(height);
            BlockState state = level.getBlockState(position);
            if (FORBIDDEN.contains(state.getBlock()) || !state.getFluidState().isEmpty() || !state.getCollisionShape(level, position).isEmpty()) return false;
        }
        BlockState floor = level.getBlockState(feet.below());
        return !FORBIDDEN.contains(floor.getBlock()) && floor.getFluidState().isEmpty()
                && floor.isFaceSturdy(level, feet.below(), Direction.UP)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }
    private static int walk(ServerLevel level, Placement placement) {
        require(safeFeet(level, transform(ENTRANCE, placement)), "Unsafe entrance");
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(ENTRANCE); pending.add(ENTRANCE);
        while (!pending.isEmpty()) {
            BlockPos position = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = position.relative(direction);
                if (next.getX() < 0 || next.getX() >= 25 || next.getZ() < 0 || next.getZ() >= 27 || visited.contains(next)) continue;
                if (safeFeet(level, transform(next, placement))) { visited.add(next); pending.add(next); }
            }
        }
        for (int targetZ : new int[] {4, 16, 19}) require(visited.contains(new BlockPos(12, 2, targetZ)), "Disconnected interior, safe point or shrine approach");
        for (int blockX = 10; blockX <= 14; blockX++) for (int blockZ = 0; blockZ <= 19; blockZ++) {
            BlockPos local = new BlockPos(blockX, 2, blockZ);
            require(visited.contains(local) && level.getBlockState(transform(local.above(2), placement)).isAir(), "Five-wide three-high route obstructed");
        }
        return visited.size();
    }
    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int lanterns = 0;
        int chains = 0;
        int vines = 0;
        int water = 0;
        int plants = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState state = level.getBlockState(position);
            require(state.equals(entry.getValue()), "State changed " + position + ": " + entry.getValue() + " -> " + state);
            require(state.canSurvive(level, position), "Unsupported state " + state + " " + position);
            require(level.getBlockEntity(position) == null, "Unexpected inventory or block entity");
            if (!state.getFluidState().isEmpty()) { require(state.is(Blocks.WATER), "Unexpected fluid"); water++; }
            if (state.is(Blocks.VINE)) vines++;
            if (state.is(Blocks.FERN) || state.is(Blocks.POTTED_FERN) || state.is(Blocks.POTTED_FLOWERING_AZALEA)) plants++;
            if (state.is(Blocks.LANTERN)) { lanterns++; require(level.getBlockState(position.above()).is(Blocks.CHAIN), "Lamp lost chain"); }
            if (state.is(Blocks.CHAIN)) { chains++; BlockState above = level.getBlockState(position.above()); require(above.is(Blocks.CHAIN) || above.isFaceSturdy(level, position.above(), Direction.DOWN), "Unanchored chain"); }
            if (state.is(Blocks.WATER)) {
                BlockPos floor = position.below();
                require(level.getBlockState(floor).isCollisionShapeFullBlock(level, floor), "Pool bottom missing");
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbor = position.relative(direction);
                    BlockState border = level.getBlockState(neighbor);
                    require(border.is(Blocks.WATER) || border.isCollisionShapeFullBlock(level, neighbor), "Pool side missing");
                }
            }
        }
        require(lanterns == 4 && chains == 8 && water == 24 && vines >= 60 && plants == 13, "Feature counts changed");
        int roofCells = 0;
        for (int blockX = 0; blockX < 25; blockX++) for (int blockZ = 0; blockZ < 27; blockZ++) {
            for (int height : new int[] {0, 13}) {
                BlockPos position = transform(new BlockPos(blockX, height, blockZ), placement);
                require(level.getBlockState(position).isCollisionShapeFullBlock(level, position), "Foundation or skylight roof gap");
            }
            roofCells++;
        }
        for (int columnX : new int[] {5, 19}) for (int stationZ : new int[] {9, 18}) for (int blockY = 1; blockY <= 13; blockY++) {
            BlockPos position = transform(new BlockPos(columnX, blockY, stationZ), placement);
            require(level.getBlockState(position).isCollisionShapeFullBlock(level, position), "Broken column to roof load path");
        }
        for (int blockX = 10; blockX <= 14; blockX++) for (int blockY = 2; blockY <= 6; blockY++) require(level.getBlockState(transform(new BlockPos(blockX, blockY, 0), placement)).isAir(), "Portal obstruction");
        BlockPos first = transform(BlockPos.ZERO, placement);
        BlockPos last = transform(SIZE.offset(-1, -1, -1), placement);
        BlockPos lower = new BlockPos(Math.min(first.getX(), last.getX()) - 1, first.getY() - 1, Math.min(first.getZ(), last.getZ()) - 1);
        BlockPos upper = new BlockPos(Math.max(first.getX(), last.getX()) + 1, last.getY() + 1, Math.max(first.getZ(), last.getZ()) + 1);
        for (BlockPos position : BlockPos.betweenClosed(lower, upper)) if (!placement.expected().containsKey(position)) require(level.getBlockState(position).isAir() && level.getFluidState(position).isEmpty(), "Block or fluid escaped template");
        require(level.getEntitiesOfClass(ItemEntity.class, new AABB(lower, upper.offset(1, 1, 1))).isEmpty(), "Dropped attachment or block item");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", placement.rotation().name());
        result.put("origin", List.of(placement.origin().getX(), placement.origin().getY(), placement.origin().getZ()));
        result.put("states_preserved", placement.expected().size());
        result.put("collision_reachable_feet", walk(level, placement));
        result.put("route_cells", 100); result.put("roof_cells", roofCells); result.put("water_cells", water);
        result.put("vines_survive", vines); result.put("plants_survive", plants); result.put("hanging_lanterns", lanterns); result.put("anchored_chains", chains);
        result.put("dropped_items", 0); result.put("escaped_fluids_or_blocks", 0); result.put("all_states_can_survive", true);
        return result;
    }
    @GameTest(template = "empty", batch = "zone_r03_only", timeoutTicks = 1000)
    public static void overgrownShrineSurvivesFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Isolated GameTest server required");
        Path root = Path.of(System.getProperty("ftzoner03qa.root")).toAbsolutePath().normalize();
        require(root.getFileName().toString().equals("zone-R03") && Files.exists(root.resolve("forge/build.gradle")), "Wrong isolated workspace");
        Files.createDirectories(root.resolve("qa"));
        Files.deleteIfExists(root.resolve("qa/forge-native.json"));
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve("room.nbt"))));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("zone-R03") && metadata.get("sha256").getAsString().equals(digest), "Identity or hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(root.resolve("room.nbt").toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes") && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Wrong version or entities");
        Map<BlockPos, BlockState> local = decode(tag);
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(SIZE), "Wrong native dimensions");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(decode(roundtrip).equals(local) && roundtrip.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Native roundtrip changed content");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Files.writeString(root.resolve("qa/roundtrip.json"), JSON.toJson(Map.of("id", "zone-R03", "status", "PASS", "sha256", digest, "native_states_equal", true, "recorded_cells", 10800)) + "\n", StandardCharsets.UTF_8);
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, level.getServer());
        Rotation[] rotations = Rotation.values();
        BlockPos[] origins = {new BlockPos(20, 2, 20), new BlockPos(120, 2, 20), new BlockPos(120, 2, 120), new BlockPos(20, 2, 120)};
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
            BlockPos upper = new BlockPos(Math.max(origin.getX(), corner.getX()) + 1, 196, Math.max(origin.getZ(), corner.getZ()) + 1);
            for (int chunkX = lower.getX() >> 4; chunkX <= upper.getX() >> 4; chunkX++) for (int chunkZ = lower.getZ() >> 4; chunkZ <= upper.getZ() >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
            for (BlockPos position : BlockPos.betweenClosed(lower, upper)) require(level.getBlockState(position).isAir() && level.getFluidState(position).isEmpty(), "Fixture not empty " + position);
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement failed");
            inspect(level, placement);
            expected.forEach((position, state) -> { state.updateNeighbourShapes(level, position, Block.UPDATE_ALL); level.updateNeighborsAt(position, state.getBlock()); });
            placements.add(placement);
        }
        System.out.println("ZONE_R03_INITIAL_PASS: four rotations, 43200 records, forced neighbor updates, contained source water");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) evidence.add(inspect(level, placement));
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("id", "zone-R03"); result.put("status", "PASS"); result.put("sha256", digest);
                result.put("minecraft", "1.20.1"); result.put("forge", "47.4.10"); result.put("java", System.getProperty("java.version"));
                result.put("test", "StructureTemplate.placeInWorld in isolated Forge GameTest server");
                result.put("ticks_observed", 240); result.put("random_tick_speed", 0); result.put("forced_neighbor_shape_and_event_updates", true);
                result.put("fixture_absolute_y", 180); result.put("chunk_forcing", true); result.put("preflight_empty_volume_with_one_block_margin", true);
                result.put("native_registry_complete_state_properties", "PASS"); result.put("native_roundtrip", "PASS"); result.put("rotations", evidence);
                result.put("player_aabb", "0.6 x 1.8"); result.put("client_visual_tested", false); result.put("player_saves_accessed", false); result.put("deployed", false);
                result.put("limits", List.of("Vegetation random growth excluded", "No full modpack client or gameplay test", "No FT hall integration or persistence verification"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("ZONE_R03_FORGE_PASS: four rotations stable after 240 ticks; sha256=" + digest);
                helper.succeed();
            } catch (Exception exception) { helper.fail(exception.toString()); }
        });
    }
}

