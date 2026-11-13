package net.farmingtales.r2qa;

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
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftr2qa")
@PrefixGameTestTemplate(false)
public final class KitchenPlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(15, 9, 15);
    private static final BlockPos ENTRANCE = new BlockPos(7, 1, 0);
    private static final List<BlockPos> TARGETS = List.of(new BlockPos(7, 1, 3), new BlockPos(7, 1, 7),
            new BlockPos(7, 1, 11), new BlockPos(3, 1, 11), new BlockPos(11, 1, 11),
            new BlockPos(2, 1, 4), new BlockPos(12, 1, 4));
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE, Blocks.MAGMA_BLOCK, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.SWEET_BERRY_BUSH);
    private static final Set<String> ENTITY_IDS = Set.of("minecraft:barrel", "minecraft:smoker", "minecraft:campfire");

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
        BlockPos below = feet.below();
        BlockState floor = level.getBlockState(below);
        return !HAZARDS.contains(floor.getBlock()) && floor.getFluidState().isEmpty()
                && floor.isFaceSturdy(level, below, Direction.UP)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static int walk(ServerLevel level, Placement placement) {
        for (int blockX = 6; blockX <= 8; blockX++) {
            for (int blockZ = 0; blockZ <= 11; blockZ++) {
                BlockPos feet = transform(new BlockPos(blockX, 1, blockZ), placement);
                require(dryBody(level, feet) && level.getBlockState(feet.above(2)).isAir(), "Three-wide/high aisle obstructed: " + feet);
            }
        }
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(ENTRANCE);
        pending.add(ENTRANCE);
        while (!pending.isEmpty()) {
            BlockPos local = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = local.relative(direction);
                if (next.getX() < 0 || next.getX() >= 15 || next.getZ() < 0 || next.getZ() >= 15 || visited.contains(next)) {
                    continue;
                }
                if (dryBody(level, transform(next, placement))) {
                    visited.add(next);
                    pending.add(next);
                }
            }
        }
        for (BlockPos target : TARGETS) {
            require(visited.contains(target), "Collision-safe route missing: " + target);
        }
        return visited.size();
    }

    private static AABB bounds(Placement placement) {
        BlockPos first = transform(BlockPos.ZERO, placement);
        BlockPos second = transform(SIZE.offset(-1, -1, -1), placement);
        return new AABB(Math.min(first.getX(), second.getX()), first.getY(), Math.min(first.getZ(), second.getZ()),
                Math.max(first.getX(), second.getX()) + 1, second.getY() + 1, Math.max(first.getZ(), second.getZ()) + 1);
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int entities = 0;
        int campfires = 0;
        int candles = 0;
        int lanterns = 0;
        int chains = 0;
        int roots = 0;
        int furniture = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed at " + position + ": expected " + entry.getValue() + ", got " + actual);
            require(actual.canSurvive(level, position), "Native support failed at " + position + ": " + actual);
            require(actual.getFluidState().isEmpty(), "Unexpected free fluid at " + position);
            require(!actual.is(Blocks.FIRE) && !actual.is(Blocks.SOUL_FIRE), "Spreading fire appeared");
            BlockEntity blockEntity = level.getBlockEntity(position);
            require(actual.hasBlockEntity() == (blockEntity != null), "Missing/unexpected block entity at " + position);
            if (blockEntity instanceof Container container) {
                require(container.isEmpty(), "Nonempty container at " + position);
            }
            if (blockEntity != null) {
                CompoundTag saved = blockEntity.saveWithFullMetadata();
                require(ENTITY_IDS.contains(saved.getString("id")), "Unexpected entity ID " + saved.getString("id"));
                require(!saved.contains("LootTable") && saved.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Inventory/loot is not empty");
                entities++;
            }
            if (actual.is(Blocks.CHAIN)) {
                chains++;
                require(actual.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y, "Chain not vertical");
                BlockPos anchor = position.above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN)) {
                    anchor = anchor.above();
                }
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Suspension lacks anchor");
            }
            if (actual.is(Blocks.LANTERN)) {
                lanterns++;
                require(actual.getValue(BlockStateProperties.HANGING) && level.getBlockState(position.above()).is(Blocks.CHAIN), "Lantern lacks its chain");
            }
            if (actual.is(Blocks.HANGING_ROOTS)) {
                roots++;
                require(level.getBlockState(position.above()).isFaceSturdy(level, position.above(), Direction.DOWN), "Herbs lack overhead support");
            }
            if (actual.is(Blocks.CANDLE)) {
                candles++;
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Candle lacks a top surface");
            }
            if (actual.is(Blocks.CAMPFIRE)) {
                campfires++;
                require(actual.getValue(BlockStateProperties.LIT), "Hearth is unlit");
                require(level.getBlockState(position.below()).is(Blocks.POLISHED_ANDESITE), "Hearth lacks stone bed");
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockState neighbor = level.getBlockState(position.relative(direction));
                    require(neighbor.is(Blocks.SMOKER) || neighbor.is(Blocks.IRON_BARS) || neighbor.is(Blocks.STONE_BRICKS), "Hearth safety shell missing");
                }
            }
            if (actual.is(Blocks.SPRUCE_STAIRS) || actual.is(Blocks.SPRUCE_SLAB) || actual.is(Blocks.DARK_OAK_SLAB)) {
                furniture++;
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Furniture footprint lacks floor");
            }
        }
        require(entities == 7 && campfires == 1 && candles == 3 && lanterns == 2 && chains == 5 && roots == 4 && furniture == 42, "Feature counts changed");
        for (int blockX = 0; blockX < 15; blockX++) {
            for (int blockZ = 0; blockZ < 15; blockZ++) {
                BlockPos floor = transform(new BlockPos(blockX, 0, blockZ), placement);
                BlockPos roof = transform(new BlockPos(blockX, 8, blockZ), placement);
                require(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Incomplete floor");
                require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Incomplete roof");
            }
        }
        AABB bounds = bounds(placement);
        require(level.getEntitiesOfClass(ItemEntity.class, bounds.inflate(1)).isEmpty(), "Attachment/furniture dropped an item");
        for (BlockPos position : BlockPos.betweenClosed(BlockPos.containing(bounds.minX - 1, bounds.minY - 1, bounds.minZ - 1),
                BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
            if (!placement.expected().containsKey(position)) {
                require(level.getFluidState(position).isEmpty() && !level.getBlockState(position).is(Blocks.FIRE), "Fluid/fire outside structure at " + position + ": " + level.getBlockState(position));
            }
        }
        return Map.of("rotation", placement.rotation().name(), "states_preserved", placement.expected().size(),
                "native_block_entities", entities, "collision_reachable_feet", walk(level, placement),
                "anchored_chains", chains, "supported_hanging_lanterns", lanterns, "supported_herbs", roots,
                "stone_enclosed_campfire", campfires, "supported_furniture_cells", furniture, "inventory_items", 0);
    }

    @GameTest(template = "empty", batch = "r2_only", timeoutTicks = 600)
    public static void kitchenSurvivesFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Refuse to run outside isolated GameTest server");
        Path root = Path.of(System.getProperty("ftr2qa.root")).toAbsolutePath().normalize();
        require(Files.exists(root.resolve("forge/build.gradle")) && root.getFileName().toString().contains("FT地牢_R2_"), "Independent R2 workspace required");
        Path path = root.resolve("out/room.nbt");
        String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("out/room.json")), JsonObject.class);
        require(metadata.get("sha256").getAsString().equals(sha256), "Metadata hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(path.toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes") && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Version/palette/entity failure");
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown/nonvanilla block " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid block property: " + identifier + " " + key);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(SIZE), "Template size mismatch");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 2025, "Native roundtrip lost positions");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        ListTag blocks = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag block = blocks.getCompound(index);
            ListTag coordinates = block.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(local.put(position, states.get(block.getInt("state"))) == null, "Duplicate position");
            if (block.contains("nbt")) {
                CompoundTag entity = block.getCompound("nbt");
                require(ENTITY_IDS.contains(entity.getString("id")) && entity.getList("Items", Tag.TAG_COMPOUND).isEmpty() && !entity.contains("LootTable"), "Input block entity/loot failure");
            }
        }
        require(local.size() == 2025, "Incomplete dense template");
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        List<Placement> placements = new ArrayList<>();
        BlockPos[] origins = {new BlockPos(8, 3, 8), new BlockPos(56, 3, 8), new BlockPos(56, 3, 56), new BlockPos(8, 3, 56)};
        Rotation[] rotations = Rotation.values();
        for (int index = 0; index < rotations.length; index++) {
            BlockPos origin = helper.absolutePos(origins[index]);
            Rotation rotation = rotations[index];
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Placement placement = new Placement(origin, rotation, expected);
            local.forEach((position, block) -> expected.put(transform(position, placement), block.rotate(rotation)));
            AABB box = bounds(placement).inflate(2);
            for (BlockPos position : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
                level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement refused");
            placements.add(placement);
            inspect(level, placement);
            expected.forEach((position, block) -> {
                level.updateNeighborsAt(position, block.getBlock());
                BlockState refreshed = Block.updateFromNeighbourShapes(level.getBlockState(position), level, position);
                level.setBlock(position, refreshed, Block.UPDATE_ALL);
            });
        }
        System.out.println("R2_NATIVE_INITIAL_PASS: four rotations, 8100 states, native survival and collision routes");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) {
                    evidence.add(inspect(level, placement));
                    AABB bounds = bounds(placement);
                    StructureTemplate capture = new StructureTemplate();
                    capture.fillFromWorld(level, BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ), SIZE, false, null);
                    NbtIo.writeCompressed(capture.save(new CompoundTag()), root.resolve("qa/native-placed-" + placement.rotation().name().toLowerCase() + ".nbt").toFile());
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("id", "R2");
                result.put("status", "PASS");
                result.put("sha256", sha256);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("test", "StructureTemplate.placeInWorld on isolated Forge GameTest server");
                result.put("ticks_observed", 240);
                result.put("forced_neighbor_updates", true);
                result.put("forced_native_shape_recalculation", true);
                result.put("doFireTick", true);
                result.put("native_registry_and_properties", "PASS");
                result.put("native_structure_roundtrip", "PASS");
                result.put("rotations", evidence);
                result.put("dropped_items", 0);
                result.put("inventory_items", 0);
                result.put("loot_tables", 0);
                result.put("client_visual_tested", false);
                result.put("limitations", List.of("Not a full modpack client or multiplayer play test", "Campfire does not spread fire in vanilla; no loose fire or lava was used", "No FT core catalog, world connections or production deployment"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("R2_FORGE_PLACEMENT_PASS: four rotations stable after 240 ticks; no drops, free fluid, fire spread or reward inventory; sha256=" + sha256);
                helper.succeed();
            } catch (Exception exception) {
                helper.fail(exception.toString());
            }
        });
    }
}
