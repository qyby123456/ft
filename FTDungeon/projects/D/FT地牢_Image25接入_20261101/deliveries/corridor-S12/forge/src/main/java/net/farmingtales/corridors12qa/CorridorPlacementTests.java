package net.farmingtales.corridors12qa;

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
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftcorridors12qa")
@PrefixGameTestTemplate(false)
public final class CorridorPlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(11, 10, 16);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.TNT, Blocks.CHEST, Blocks.BARREL, Blocks.HOPPER, Blocks.DISPENSER,
            Blocks.DROPPER, Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.COBWEB,
            Blocks.POWDER_SNOW, Blocks.MAGMA_BLOCK, Blocks.CACTUS, Blocks.WITHER_ROSE);

    private record Placement(BlockPos origin, Rotation rotation, Map<BlockPos, BlockState> expected) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, Placement placement) {
        return StructureTemplate.transform(local, Mirror.NONE, placement.rotation(), BlockPos.ZERO).offset(placement.origin());
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return !FORBIDDEN.contains(state.getBlock()) && state.getFluidState().isEmpty()
                && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockState floor = level.getBlockState(feet.below());
        return clear(level, feet) && clear(level, feet.above()) && !FORBIDDEN.contains(floor.getBlock())
                && floor.getFluidState().isEmpty() && floor.isFaceSturdy(level, feet.below(), Direction.UP);
    }

    private static int checkPath(ServerLevel level, Placement placement) {
        Set<BlockPos> possible = new HashSet<>();
        for (int blockX = 0; blockX < SIZE.getX(); blockX++) {
            for (int blockZ = 0; blockZ < SIZE.getZ(); blockZ++) {
                BlockPos feet = transform(new BlockPos(blockX, 1, blockZ), placement);
                if (walkable(level, feet)) possible.add(feet);
            }
        }
        BlockPos entrance = transform(new BlockPos(5, 1, 0), placement);
        Set<BlockPos> reachable = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        reachable.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = current.relative(direction);
                if (possible.contains(neighbor) && reachable.add(neighbor)) pending.add(neighbor);
            }
        }
        for (int blockX = 4; blockX <= 6; blockX++) {
            for (int blockZ = 0; blockZ < 16; blockZ++) {
                BlockPos feet = transform(new BlockPos(blockX, 1, blockZ), placement);
                require(reachable.contains(feet), "Straight supported lane is blocked: " + feet);
                require(clear(level, feet.above(2)) && clear(level, feet.above(3)), "Less than four blocks clear headroom");
            }
        }
        require(reachable.contains(transform(new BlockPos(5, 1, 3), placement)), "Interior point unreachable");
        require(reachable.contains(transform(new BlockPos(5, 1, 15), placement)), "South exit unreachable");
        return reachable.size();
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int lanterns = 0;
        int hangingLanterns = 0;
        int chains = 0;
        int banners = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "Native block state changed at " + position + ": " + entry.getValue() + " -> " + actual);
            require(actual.getFluidState().isEmpty(), "Unexpected fluid");
            require(!FORBIDDEN.contains(actual.getBlock()), "Forbidden block");
            if (actual.is(Blocks.SOUL_LANTERN)) {
                lanterns++;
                require(actual.canSurvive(level, position), "Lantern support is invalid");
                if (actual.getValue(BlockStateProperties.HANGING)) hangingLanterns++;
            }
            if (actual.is(Blocks.CHAIN)) {
                chains++;
                BlockPos anchor = position.above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN)) anchor = anchor.above();
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Chain lacks a full overhead anchor");
            }
            if (actual.is(Blocks.WHITE_WALL_BANNER)) {
                banners++;
                require(actual.canSurvive(level, position), "Parchment banner is detached from its wall");
                BlockEntity entity = level.getBlockEntity(position);
                require(entity instanceof BannerBlockEntity, "Banner block entity missing");
                CompoundTag data = entity.saveWithFullMetadata();
                require(!data.contains("Items") && !data.contains("LootTable")
                        && data.getList("Patterns", Tag.TAG_COMPOUND).isEmpty(), "Unexpected banner payload");
            } else {
                require(level.getBlockEntity(position) == null, "Unexpected block entity at " + position);
            }
        }
        require(lanterns == 6 && hangingLanterns == 4 && chains == 8 && banners == 2, "Theme feature count differs");
        for (int blockX = 0; blockX < SIZE.getX(); blockX++) {
            for (int blockZ = 0; blockZ < SIZE.getZ(); blockZ++) {
                for (int blockY : new int[] {0, 9}) {
                    BlockPos support = transform(new BlockPos(blockX, blockY, blockZ), placement);
                    require(level.getBlockState(support).isFaceSturdy(level, support, Direction.UP), "Floor or roof gap");
                }
            }
        }
        BlockPos first = transform(BlockPos.ZERO, placement);
        BlockPos last = transform(SIZE.offset(-1, -1, -1), placement);
        AABB bounds = new AABB(Math.min(first.getX(), last.getX()) - 1, first.getY() - 1,
                Math.min(first.getZ(), last.getZ()) - 1, Math.max(first.getX(), last.getX()) + 2,
                last.getY() + 2, Math.max(first.getZ(), last.getZ()) + 2);
        require(level.getEntitiesOfClass(ItemEntity.class, bounds).isEmpty(), "A decorative block dropped as an item");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", placement.rotation().name());
        result.put("states_preserved", placement.expected().size());
        result.put("collision_reachable_feet", checkPath(level, placement));
        result.put("full_supported_straight_lane", "3x4x16");
        result.put("roof_cells", 176);
        result.put("floor_cells", 176);
        result.put("soul_lanterns", lanterns);
        result.put("hanging_lanterns", hangingLanterns);
        result.put("anchored_chains", chains);
        result.put("wall_supported_banner_block_entities", banners);
        result.put("dropped_items", 0);
        result.put("fluid_cells", 0);
        return result;
    }

    @GameTest(template = "empty", batch = "corridor_s12_only", timeoutTicks = 600)
    public static void corridorSurvivesFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only isolated GameTest execution is allowed");
        Path root = Path.of(System.getProperty("ftcorridors12qa.root")).toAbsolutePath().normalize();
        require(root.getFileName().toString().equals("corridor-S12") && Files.exists(root.resolve("forge/build.gradle")), "Wrong isolated workspace");
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOBLOCKDROPS).set(true, level.getServer());
        Path file = root.resolve("room.nbt");
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("corridor-S12") && metadata.get("sha256").getAsString().equals(digest), "Metadata identity/hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(file.toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes")
                && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Wrong version or template kind");
        List<BlockState> palette = new ArrayList<>();
        ListTag entries = tag.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown/nonvanilla block " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            require(!FORBIDDEN.contains(block), "Forbidden block in palette");
            CompoundTag properties = entry.getCompound("Properties");
            require(properties.size() == block.getStateDefinition().getProperties().size(), "Incomplete properties for " + identifier);
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid native state property");
            }
            palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(SIZE), "Template dimensions mismatch");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 1760, "Native roundtrip lost blocks");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag coordinate = record.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinate.getInt(0), coordinate.getInt(1), coordinate.getInt(2));
            require(position.getX() >= 0 && position.getX() < 11 && position.getY() >= 0 && position.getY() < 10
                    && position.getZ() >= 0 && position.getZ() < 16, "Out of bounds record");
            require(local.put(position, palette.get(record.getInt("state"))) == null, "Duplicate block record");
            if (record.contains("nbt")) {
                CompoundTag payload = record.getCompound("nbt");
                require(payload.size() == 1 && payload.getString("id").equals("minecraft:banner"), "Unexpected block entity payload");
            }
        }
        require(local.size() == 1760, "Missing explicit volume records");
        List<Placement> placements = new ArrayList<>();
        BlockPos[] origins = {new BlockPos(20, 2, 20), new BlockPos(60, 2, 20), new BlockPos(60, 2, 60), new BlockPos(20, 2, 60)};
        Set<BlockPos> allPositions = new HashSet<>();
        Rotation[] rotations = Rotation.values();
        for (int index = 0; index < rotations.length; index++) {
            Rotation rotation = rotations[index];
            BlockPos origin = helper.absolutePos(origins[index]);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Placement placement = new Placement(origin, rotation, expected);
            local.forEach((position, state) -> expected.put(transform(position, placement), state.rotate(rotation)));
            require(expected.keySet().stream().allMatch(allPositions::add), "Test placements overlap");
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random,
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement rejected");
            placements.add(placement);
            inspect(level, placement);
            expected.forEach((position, state) -> {
                state.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
                level.updateNeighborsAt(position, state.getBlock());
            });
        }
        System.out.println("S12_INITIAL_PASS: four native rotations, 7040 records, forced neighbour updates");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) evidence.add(inspect(level, placement));
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("id", "corridor-S12");
                result.put("status", "PASS");
                result.put("sha256", digest);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("test", "StructureTemplate.placeInWorld in isolated Forge GameTest server");
                result.put("native_registry_and_complete_properties", "PASS");
                result.put("native_structure_roundtrip", "PASS");
                result.put("forced_neighbor_shape_and_event_updates", true);
                result.put("ticks_observed", 240);
                result.put("rotations", evidence);
                result.put("inventory_items", 0);
                result.put("loot_tables", 0);
                result.put("preplaced_entities", 0);
                result.put("client_gameplay_tested", false);
                result.put("production_world_modified", false);
                result.put("limits", List.of("Disposable headless test world only", "No modpack client screenshots or deployment"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("S12_FORGE_PLACEMENT_PASS: four rotations stable after 240 ticks; sha256=" + digest);
                helper.succeed();
            } catch (Exception exception) {
                helper.fail(exception.toString());
            }
        });
    }
}
