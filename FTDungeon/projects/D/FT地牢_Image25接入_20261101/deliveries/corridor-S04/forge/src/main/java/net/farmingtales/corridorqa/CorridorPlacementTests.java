package net.farmingtales.corridorqa;

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

@GameTestHolder("ftcorridors04qa")
@PrefixGameTestTemplate(false)
public final class CorridorPlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(11, 9, 16);
    private static final BlockPos ENTRANCE = new BlockPos(5, 1, 0);
    private static final BlockPos EXIT = new BlockPos(5, 1, 15);
    private static final int RECORDS = 1584;
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
            if (!state.isAir() || !state.getFluidState().isEmpty()) return false;
        }
        BlockPos floor = feet.below();
        return level.getBlockState(floor).getFluidState().isEmpty()
                && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static int walk(ServerLevel level, Placement placement) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(ENTRANCE);
        visited.add(ENTRANCE);
        while (!pending.isEmpty()) {
            BlockPos local = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = local.relative(direction);
                if (next.getX() < 4 || next.getX() > 6 || next.getZ() < 0 || next.getZ() >= 16 || visited.contains(next)) continue;
                if (dryBody(level, transform(next, placement))) {
                    visited.add(next);
                    pending.addLast(next);
                }
            }
        }
        require(visited.contains(EXIT) && visited.size() == 48, "Three-wide continuous route is obstructed: " + placement.rotation());
        for (int blockZ = 0; blockZ < 16; blockZ++) {
            for (int blockX = 4; blockX <= 6; blockX++) require(dryBody(level, transform(new BlockPos(blockX, 1, blockZ), placement)), "Unsafe route cell");
        }
        return visited.size();
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int attachments = 0;
        int lights = 0;
        int nonAir = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed at " + position + ": expected " + entry.getValue() + ", got " + actual);
            require(actual.canSurvive(level, position), "Native canSurvive failed: " + actual + " at " + position);
            require(actual.getFluidState().isEmpty(), "Unexpected fluid");
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (!actual.isAir()) nonAir++;
            if (actual.is(Blocks.HANGING_ROOTS) || actual.is(Blocks.RED_MUSHROOM) || actual.is(Blocks.WARPED_FUNGUS) || actual.is(Blocks.MOSS_CARPET)) attachments++;
            if (actual.getLightEmission() > 0) lights++;
        }
        for (int blockZ = 0; blockZ < 16; blockZ++) {
            for (int blockX = 0; blockX < 11; blockX++) {
                BlockPos roof = transform(new BlockPos(blockX, 8, blockZ), placement);
                require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.UP), "Roof gap");
            }
            for (int blockY = 1; blockY < 8; blockY++) {
                for (int blockX : new int[] {0, 10}) require(!level.getBlockState(transform(new BlockPos(blockX, blockY, blockZ), placement)).isAir(), "Side-wall gap");
            }
        }
        BlockPos lower = transform(BlockPos.ZERO, placement);
        BlockPos upper = transform(new BlockPos(10, 8, 15), placement);
        AABB bounds = new AABB(Math.min(lower.getX(), upper.getX()) - 1, lower.getY() - 1, Math.min(lower.getZ(), upper.getZ()) - 1,
                Math.max(lower.getX(), upper.getX()) + 2, upper.getY() + 2, Math.max(lower.getZ(), upper.getZ()) + 2);
        require(level.getEntitiesOfClass(ItemEntity.class, bounds).isEmpty(), "A block or attachment dropped an item");
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("rotation", placement.rotation().name());
        evidence.put("status", "PASS");
        evidence.put("origin", List.of(placement.origin().getX(), placement.origin().getY(), placement.origin().getZ()));
        evidence.put("exact_state_records", placement.expected().size());
        evidence.put("non_air", nonAir);
        evidence.put("reachable_three_wide_route_cells", walk(level, placement));
        evidence.put("supported_attachments", attachments);
        evidence.put("emissive_blocks", lights);
        evidence.put("dropped_items", 0);
        evidence.put("block_entities", 0);
        return evidence;
    }

    @GameTest(template = "empty", batch = "s04_only", timeoutTicks = 600)
    public static void fungalCorridorFourRotationsStable(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTest server is permitted");
        Path root = Path.of(System.getProperty("ftcorridors04qa.root")).toAbsolutePath().normalize();
        require(root.getFileName().toString().equals("corridor-S04") && Files.exists(root.resolve("forge/build.gradle")), "Wrong independent workspace");
        Path source = root.resolve("room.nbt");
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("S04") && metadata.get("sha256").getAsString().equals(digest), "Metadata identity/hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(source.toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes") && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Wrong version, multi-palette or entities");
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown/nonvanilla block: " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
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
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == RECORDS, "Roundtrip lost records");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getY() >= 0 && position.getZ() >= 0
                    && position.getX() < 11 && position.getY() < 9 && position.getZ() < 16, "Out of bounds");
            require(!record.contains("nbt") && local.put(position, states.get(record.getInt("state"))) == null, "Payload or duplicate");
        }
        require(local.size() == RECORDS, "Incomplete volume");
        List<Placement> placements = new ArrayList<>();
        Set<BlockPos> occupied = new HashSet<>();
        BlockPos[] origins = { new BlockPos(64, 80, 64), new BlockPos(160, 80, 64), new BlockPos(256, 80, 64), new BlockPos(352, 80, 64) };
        for (int index = 0; index < Rotation.values().length; index++) {
            Rotation rotation = Rotation.values()[index];
            Placement placement = new Placement(origins[index], rotation, new LinkedHashMap<>());
            local.forEach((position, state) -> placement.expected().put(transform(position, placement), state.rotate(rotation)));
            require(placement.expected().keySet().stream().allMatch(occupied::add), "Placements overlap");
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, placement.origin(), placement.origin(), settings, level.random,
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Placement rejected");
            inspect(level, placement);
            placement.expected().forEach((position, state) -> {
                state.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
                level.updateNeighborsAt(position, state.getBlock());
            });
            placements.add(placement);
        }
        System.out.println("S04_NATIVE_INITIAL_PASS: four rotations, 6336 exact state records and forced neighbour updates");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> rotations = new ArrayList<>();
                for (Placement placement : placements) rotations.add(inspect(level, placement));
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("id", "S04");
                report.put("status", "PASS");
                report.put("sha256", digest);
                report.put("minecraft", "1.20.1");
                report.put("forge", "47.4.10");
                report.put("ticks_observed", 240);
                report.put("test", "Actual StructureTemplate.placeInWorld in isolated Forge GameTest server");
                report.put("registry_complete_properties", "PASS");
                report.put("native_structure_roundtrip", "PASS");
                report.put("forced_neighbor_updates", true);
                report.put("rotations", rotations);
                report.put("client_visual_tested", false);
                report.put("deployed", false);
                report.put("player_save_modified", false);
                report.put("limits", List.of("Isolated test world only; no production saves or core catalog touched", "Lodestone screenshots are renderer evidence, not full-modpack client gameplay", "No FT hall connections, multiplayer, FPS or external generation idempotence tested"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("S04_FORGE_PLACEMENT_PASS: four rotations stable after 240 ticks; sha256=" + digest);
                helper.succeed();
            } catch (Exception failure) {
                helper.fail(failure.toString());
            }
        });
    }
}
