package net.farmingtales.roomqa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftroomqa")
@PrefixGameTestTemplate(false)
public final class RoomPlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK,
            Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW);

    private record Placement(String revision, String id, BlockPos origin, Rotation rotation, BlockPos size,
                             BlockPos entrance, Map<BlockPos, BlockState> expected) {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static BlockPos vector(JsonObject data, String key) {
        return new BlockPos(data.getAsJsonArray(key).get(0).getAsInt(), data.getAsJsonArray(key).get(1).getAsInt(), data.getAsJsonArray(key).get(2).getAsInt());
    }

    private static BlockPos transform(BlockPos local, Placement placement) {
        return StructureTemplate.transform(local, Mirror.NONE, placement.rotation(), BlockPos.ZERO).offset(placement.origin());
    }

    private static boolean dry(ServerLevel level, BlockPos feet) {
        for (int height = 0; height < 2; height++) {
            BlockPos position = feet.above(height);
            BlockState block = level.getBlockState(position);
            if (HAZARDS.contains(block.getBlock()) || !block.getFluidState().isEmpty() || !block.getCollisionShape(level, position).isEmpty()) {
                return false;
            }
        }
        BlockState floor = level.getBlockState(feet.below());
        return !HAZARDS.contains(floor.getBlock()) && floor.getFluidState().isEmpty() && (floor.isFaceSturdy(level, feet.below(), Direction.UP) || floor.getBlock() instanceof StairBlock)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static Set<BlockPos> walk(ServerLevel level, Placement placement) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos entry = transform(placement.entrance(), placement);
        require(dry(level, entry), placement.revision() + " " + placement.id() + " " + placement.rotation() + " entrance blocked " + entry
                + " floor=" + level.getBlockState(entry.below()) + " feet=" + level.getBlockState(entry) + " head=" + level.getBlockState(entry.above()));
        visited.add(placement.entrance());
        queue.add(placement.entrance());
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int rise : new int[] {0, 1, -1}) {
                    BlockPos next = current.relative(direction).offset(0, rise, 0);
                    if (next.getX() < 0 || next.getY() < 0 || next.getZ() < 0 || next.getX() >= placement.size().getX()
                            || next.getY() + 1 >= placement.size().getY() || next.getZ() >= placement.size().getZ() || visited.contains(next)) {
                        continue;
                    }
                    if (rise == 1 && !level.getBlockState(transform(current.above(2), placement)).getCollisionShape(level, transform(current.above(2), placement)).isEmpty()) {
                        continue;
                    }
                    if (dry(level, transform(next, placement))) {
                        visited.add(next);
                        queue.add(next);
                        break;
                    }
                }
            }
        }
        return visited;
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        List<Map<String, Object>> unsupported = new ArrayList<>();
        List<Map<String, Object>> changedTypes = new ArrayList<>();
        int stateUpdates = 0;
        int water = 0;
        int flowers = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            if (actual.getBlock() != entry.getValue().getBlock()) {
                changedTypes.add(Map.of("position", position.toShortString(), "from", entry.getValue().toString(), "to", actual.toString()));
            } else if (!actual.equals(entry.getValue())) {
                stateUpdates++;
            }
            if (!actual.canSurvive(level, position)) {
                unsupported.add(Map.of("position", position.toShortString(), "block", actual.toString()));
            }
            if (actual.is(Blocks.BLUE_ORCHID) || actual.is(Blocks.AZURE_BLUET) || actual.is(Blocks.CORNFLOWER) || actual.is(Blocks.WHITE_TULIP)) {
                flowers++;
            }
            if (actual.is(Blocks.WATER)) {
                water++;
                require(actual.getFluidState().isSource(), "Flowing water inside " + placement.id());
                for (Direction direction : List.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
                    BlockPos neighbor = position.relative(direction);
                    require(level.getFluidState(neighbor).is(FluidTags.WATER) || level.getBlockState(neighbor).isFaceSturdy(level, neighbor, direction.getOpposite()), "Unsealed water " + position);
                }
            }
        }
        BlockPos end = transform(placement.size().offset(-1, -1, -1), placement);
        BlockPos lower = new BlockPos(Math.min(end.getX(), placement.origin().getX()), placement.origin().getY(), Math.min(end.getZ(), placement.origin().getZ()));
        BlockPos upper = new BlockPos(Math.max(end.getX(), placement.origin().getX()), end.getY(), Math.max(end.getZ(), placement.origin().getZ()));
        int leaks = 0;
        for (BlockPos position : BlockPos.betweenClosed(lower.offset(-1, -1, -1), upper.offset(1, 1, 1))) {
            if (!placement.expected().containsKey(position) && !level.getFluidState(position).isEmpty()) {
                leaks++;
            }
        }
        AABB bounds = new AABB(lower.getX() - 1, lower.getY() - 1, lower.getZ() - 1, upper.getX() + 2, upper.getY() + 2, upper.getZ() + 2);
        int dropped = level.getEntitiesOfClass(ItemEntity.class, bounds).size();
        Set<BlockPos> visited = walk(level, placement);
        List<String> unreachable = new ArrayList<>();
        if (placement.id().equals("L2")) {
            for (int blockX = 6; blockX <= 14; blockX++) {
                for (int blockZ = 6; blockZ <= 14; blockZ++) {
                    if ((blockX <= 7 || blockX >= 13 || blockZ <= 7 || blockZ >= 13) && !visited.contains(new BlockPos(blockX, 2, blockZ))) {
                        unreachable.add(new BlockPos(blockX, 2, blockZ).toShortString());
                    }
                }
            }
        } else {
            for (BlockPos target : List.of(new BlockPos(3, 2, 6), new BlockPos(10, 2, 6), new BlockPos(7, 2, 12), new BlockPos(12, 2, 7))) {
                if (!visited.contains(target)) {
                    unreachable.add(target.toShortString());
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revision", placement.revision());
        result.put("id", placement.id());
        result.put("rotation", placement.rotation().name());
        result.put("changed_block_types", changedTypes);
        result.put("neighbor_state_updates", stateUpdates);
        result.put("unsupported_blocks", unsupported);
        result.put("water", water);
        result.put("flowers", flowers);
        result.put("leaks", leaks);
        result.put("item_drops", dropped);
        result.put("dry_reachable", visited.size());
        result.put("unreachable_targets", unreachable);
        return result;
    }

    private static void testDoor(ServerLevel level, Placement placement) {
        BlockPos door = transform(new BlockPos(12, 2, 8), placement);
        for (BlockPos local : List.of(new BlockPos(12, 2, 7), new BlockPos(12, 2, 9))) {
            BlockPos plate = transform(local, placement);
            BlockState idle = level.getBlockState(plate);
            require(idle.is(Blocks.STONE_PRESSURE_PLATE), "Missing pressure plate");
            level.setBlockAndUpdate(plate, idle.setValue(BlockStateProperties.POWERED, true));
            level.updateNeighborsAt(plate, Blocks.STONE_PRESSURE_PLATE);
            require(level.getBlockState(door).getValue(BlockStateProperties.OPEN), "Door did not open from " + local);
            level.setBlockAndUpdate(plate, idle.setValue(BlockStateProperties.POWERED, false));
            level.updateNeighborsAt(plate, Blocks.STONE_PRESSURE_PLATE);
            require(!level.getBlockState(door).getValue(BlockStateProperties.OPEN), "Door failed to close");
        }
    }

    private static Placement place(ServerLevel level, BlockPos origin, String revision, String identity, Rotation rotation, Path root) throws Exception {
        Path folder = root.resolve(revision).resolve("incoming").resolve(identity);
        JsonObject metadata = JSON.fromJson(Files.readString(folder.resolve("room.json")), JsonObject.class);
        CompoundTag nbt = NbtIo.readCompressed(folder.resolve("room.nbt").toFile());
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), nbt);
        ListTag palette = nbt.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(index)));
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        Placement placement = new Placement(revision, identity, origin, rotation, vector(metadata, "size"), vector(metadata, "entrance"), expected);
        for (BlockPos position : BlockPos.betweenClosed(BlockPos.ZERO, placement.size().offset(-1, -1, -1))) {
            expected.put(transform(position, placement), Blocks.AIR.defaultBlockState());
        }
        ListTag records = nbt.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            expected.put(transform(local, placement), states.get(record.getInt("state")).rotate(rotation));
        }
        for (BlockPos position : expected.keySet()) {
            level.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
            level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
        require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement rejected");
        return placement;
    }

    @GameTest(template = "empty", batch = "room_02_03", timeoutTicks = 1200)
    public static void compareFourRotationsAfterUpdates(GameTestHelper helper) throws Exception {
        Path root = Path.of(System.getProperty("ftroomqa.root"));
        Files.createDirectories(root.resolve("qa"));
        ServerLevel level = helper.getLevel();
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(false, level.getServer());
        List<Placement> placements = new ArrayList<>();
        List<Map<String, Object>> initial = new ArrayList<>();
        int index = 0;
        for (String revision : List.of("baseline", "candidate")) {
            for (String identity : List.of("L2", "M1")) {
                for (Rotation rotation : Rotation.values()) {
                    BlockPos horizontal = helper.absolutePos(new BlockPos(96 + (index % 4) * 64, 0, 96 + (index / 4) * 64));
                    BlockPos origin = new BlockPos(horizontal.getX(), 80, horizontal.getZ());
                    Placement placement = place(level, origin, revision, identity, rotation, root);
                    initial.add(inspect(level, placement));
                    Files.writeString(root.resolve("qa/initial.json"), JSON.toJson(initial), StandardCharsets.UTF_8);
                    for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
                        BlockState block = level.getBlockState(entry.getKey());
                        block.updateNeighbourShapes(level, entry.getKey(), Block.UPDATE_ALL);
                        level.updateNeighborsAt(entry.getKey(), block.getBlock());
                        if (!block.getFluidState().isEmpty()) {
                            level.scheduleTick(entry.getKey(), block.getFluidState().getType(), 1);
                        }
                    }
                    placements.add(placement);
                    index++;
                }
            }
        }
        Files.writeString(root.resolve("qa/initial.json"), JSON.toJson(initial), StandardCharsets.UTF_8);
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) {
                    Map<String, Object> result = inspect(level, placement);
                    evidence.add(result);
                    if (placement.id().equals("M1")) {
                        testDoor(level, placement);
                        result.put("both_pressure_plate_signal_paths", "PASS");
                    }
                }
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("ticks_observed", 240);
                report.put("placements", evidence);
                report.put("initial", initial);
                report.put("scope", "Fresh isolated Forge 47.4.10 server, both versions, four rotations, forced neighbor and water ticks, native block survival and collision, two pressure-plate signal paths; not full modpack client gameplay");
                List<String> failures = new ArrayList<>();
                for (Map<String, Object> row : evidence) {
                    if (row.get("revision").equals("candidate")) {
                        if (!((List<?>) row.get("changed_block_types")).isEmpty() || !((List<?>) row.get("unsupported_blocks")).isEmpty()
                                || ((int) row.get("leaks")) != 0 || ((int) row.get("item_drops")) != 0 || !((List<?>) row.get("unreachable_targets")).isEmpty()) {
                            failures.add(row.get("id") + " " + row.get("rotation"));
                        }
                        if (row.get("id").equals("L2") && (((int) row.get("water")) != 17 || ((int) row.get("flowers")) != 8)) {
                            failures.add("Garden feature loss " + row.get("rotation"));
                        }
                    }
                }
                report.put("failures", failures);
                report.put("passed", failures.isEmpty());
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
                require(failures.isEmpty(), failures.toString());
                System.out.println("ROOM_02_03_NATIVE_PASS: 16 placements, 240 ticks, native survival, dry routes, fluid containment and door signals");
                helper.succeed();
            } catch (Exception failure) {
                helper.fail(failure.toString());
            }
        });
    }
}

