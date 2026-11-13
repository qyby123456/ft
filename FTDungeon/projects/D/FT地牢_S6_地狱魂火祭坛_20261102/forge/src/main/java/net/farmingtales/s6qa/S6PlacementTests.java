package net.farmingtales.s6qa;

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
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fts6qa")
@PrefixGameTestTemplate(false)
public final class S6PlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.SOUL_FIRE, Blocks.FIRE, Blocks.MAGMA_BLOCK,
            Blocks.SOUL_CAMPFIRE, Blocks.CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW);
    private static final BlockPos ENTRANCE = new BlockPos(4, 1, 0);
    private static final List<BlockPos> TARGETS = List.of(new BlockPos(4, 1, 2), new BlockPos(2, 1, 4),
            new BlockPos(6, 1, 4), new BlockPos(4, 1, 6), new BlockPos(4, 1, 7));

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
            require(dryBody(level, feet) && level.getBlockState(feet.above(2)).isAir(), "North 3x3 interface obstructed: " + feet);
        }
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(ENTRANCE);
        pending.add(ENTRANCE);
        while (!pending.isEmpty()) {
            BlockPos local = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = local.relative(direction);
                if (next.getX() < 0 || next.getX() > 8 || next.getZ() < 0 || next.getZ() > 8 || visited.contains(next)) {
                    continue;
                }
                if (dryBody(level, transform(next, placement))) {
                    visited.add(next);
                    pending.addLast(next);
                }
            }
        }
        require(visited.containsAll(TARGETS), "Native collision route missing safe points: " + placement.rotation());
        return visited.size();
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int skulls = 0;
        int lanterns = 0;
        int chains = 0;
        int fires = 0;
        int lava = 0;
        int dripstone = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState expected = entry.getValue();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(expected), "State changed at " + position + ": expected " + expected + ", got " + actual);
            require(actual.canSurvive(level, position), "Native canSurvive failed: " + actual + " at " + position);
            BlockEntity blockEntity = level.getBlockEntity(position);
            if (blockEntity instanceof Container container) {
                require(container.isEmpty(), "Unexpected inventory at " + position);
            }
            if (blockEntity != null) {
                CompoundTag saved = blockEntity.saveWithoutMetadata();
                require(!saved.contains("LootTable") && !saved.contains("Items"), "Unexpected inventory/loot NBT");
                require(blockEntity instanceof SkullBlockEntity, "Unexpected block entity: " + blockEntity.getType());
                skulls++;
            }
            if (actual.is(Blocks.SOUL_LANTERN)) {
                lanterns++;
                if (actual.getValue(BlockStateProperties.HANGING)) {
                    require(level.getBlockState(position.above()).is(Blocks.CHAIN), "Hanging lantern lacks chain");
                } else {
                    require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Standing lantern lacks support");
                }
            }
            if (actual.is(Blocks.CHAIN)) {
                chains++;
                require(actual.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y, "Nonvertical suspension");
                BlockPos anchor = position.above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN)) {
                    anchor = anchor.above();
                }
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Chain lacks a solid overhead anchor");
            }
            if (actual.is(Blocks.SOUL_FIRE)) {
                fires++;
                require(level.getBlockState(position.below()).is(Blocks.SOUL_SOIL), "Soul fire lacks soul soil");
            }
            if (actual.is(Blocks.LAVA)) {
                lava++;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos side = position.relative(direction);
                    require(level.getBlockState(side).isFaceSturdy(level, side, direction.getOpposite()), "Lava side is not sealed");
                }
                BlockPos bottom = position.below();
                require(level.getFluidState(bottom).is(FluidTags.LAVA) || level.getBlockState(bottom).isFaceSturdy(level, bottom, Direction.UP), "Lava bottom is not sealed");
                level.scheduleTick(position, level.getFluidState(position).getType(), 1);
            }
            if (actual.is(Blocks.POINTED_DRIPSTONE)) {
                dripstone++;
                require(actual.getValue(BlockStateProperties.VERTICAL_DIRECTION) == Direction.DOWN, "Drip tip is not downward");
                require(level.getFluidState(position.above(2)).is(FluidTags.LAVA), "No native lava reservoir above stalactite support");
                require(PointedDripstoneBlock.getCauldronFillFluidType(level, position) == Fluids.LAVA, "Vanilla dripstone engine did not resolve lava above the tip");
                require(level.getBlockState(position.above()).isFaceSturdy(level, position.above(), Direction.DOWN), "Drip support missing");
                require(level.isEmptyBlock(position.below()) && level.isEmptyBlock(position.below(2)), "Drip airspace obstructed");
            }
        }
        require(skulls == 3 && lanterns == 7 && chains == 9 && fires == 1 && lava == 6 && dripstone == 1, "Feature counts changed");
        BlockPos minimum = transform(BlockPos.ZERO, placement);
        BlockPos maximum = transform(new BlockPos(8, 6, 8), placement);
        BlockPos lower = new BlockPos(Math.min(minimum.getX(), maximum.getX()), minimum.getY(), Math.min(minimum.getZ(), maximum.getZ()));
        BlockPos upper = new BlockPos(Math.max(minimum.getX(), maximum.getX()), maximum.getY(), Math.max(minimum.getZ(), maximum.getZ()));
        for (BlockPos position : BlockPos.betweenClosed(lower.offset(-1, -1, -1), upper.offset(1, 1, 1))) {
            if (!placement.expected().containsKey(position)) {
                require(level.getFluidState(position).isEmpty() && !level.getBlockState(position).is(Blocks.FIRE), "Fluid/fire leaked outside room");
            }
        }
        return Map.of("rotation", placement.rotation().name(), "states_preserved", placement.expected().size(),
                "collision_reachable_positions", walk(level, placement), "skulls", skulls, "soul_lanterns", lanterns,
                "anchored_chains", chains, "soul_fire", fires, "sealed_lava_cells", lava, "supported_lava_dripstone", dripstone);
    }

    @GameTest(template = "empty", batch = "s6_only", timeoutTicks = 500)
    public static void shrineSurvivesNativePlacement(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTest server is permitted");
        Path root = Path.of(System.getProperty("fts6qa.root")).toAbsolutePath().normalize();
        require(Files.exists(root.resolve("forge/build.gradle")), "Expected independent S6 workspace");
        Path path = root.resolve("out/S6/room.nbt");
        String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("out/S6/room.json")), JsonObject.class);
        require(metadata.get("sha256").getAsString().equals(sha256), "Metadata/structure hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(path.toFile());
        require(tag.getInt("DataVersion") == 3465 && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Wrong version or entities");
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown/nonvanilla block: " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property: " + identifier + " " + key);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(new BlockPos(9, 7, 9)), "Incorrect template size");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 567, "Template roundtrip lost blocks");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        ListTag blocks = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag block = blocks.getCompound(index);
            ListTag coordinates = block.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(local.put(position, states.get(block.getInt("state"))) == null, "Duplicate position");
            if (block.contains("nbt")) {
                require(block.getCompound("nbt").getString("id").equals("minecraft:skull"), "Forbidden block entity");
            }
        }
        require(local.size() == 567, "Incomplete dense volume");
        List<Placement> placements = new ArrayList<>();
        Rotation[] rotations = Rotation.values();
        BlockPos[] origins = {new BlockPos(6, 2, 6), new BlockPos(26, 2, 6), new BlockPos(26, 2, 26), new BlockPos(6, 2, 26)};
        for (int index = 0; index < rotations.length; index++) {
            BlockPos origin = helper.absolutePos(origins[index]);
            Rotation rotation = rotations[index];
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Placement placement = new Placement(origin, rotation, expected);
            local.forEach((position, block) -> expected.put(transform(position, placement), block.rotate(rotation)));
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement rejected");
            placements.add(placement);
            inspect(level, placement);
            expected.forEach((position, block) -> level.updateNeighborsAt(position, block.getBlock()));
        }
        System.out.println("S6_NATIVE_INITIAL_PASS: 4 rotations, 2268 positions, native registry, block entities, survival and collision routes");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) {
                    evidence.add(inspect(level, placement));
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("id", "S6");
                result.put("status", "PASS");
                result.put("sha256", sha256);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("test", "StructureTemplate.placeInWorld on isolated Forge GameTest server");
                result.put("ticks_observed", 240);
                result.put("forced_neighbor_updates", true);
                result.put("forced_lava_ticks", true);
                result.put("native_registry_and_properties", "PASS");
                result.put("native_structure_roundtrip", "PASS");
                result.put("native_dripstone_fluid_resolution", "LAVA at all four rotated tips via PointedDripstoneBlock.getCauldronFillFluidType");
                result.put("rotations", evidence);
                result.put("inventory_items", 0);
                result.put("loot_tables", 0);
                result.put("client_visual_tested", false);
                result.put("particle_emission_visually_observed", false);
                result.put("limitations", List.of("No full modpack client screenshot/FPS or multiplayer play test", "Lava-drip source/support/airspace verified; client particle appearance is not visually tested", "No FT core catalog/connection deployment in this independent structure task"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("S6_FORGE_PLACEMENT_PASS: four rotations stable after 240 ticks; no fluid escape, item inventories or attachment loss; sha256=" + sha256);
                helper.succeed();
            } catch (Exception exception) {
                helper.fail(exception.toString());
            }
        });
    }
}
