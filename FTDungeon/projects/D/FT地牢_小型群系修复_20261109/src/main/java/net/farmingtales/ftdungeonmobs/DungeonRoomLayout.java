package net.farmingtales.ftdungeonmobs;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class DungeonRoomLayout {
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK,
            Blocks.CACTUS, Blocks.POWDER_SNOW, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.WITHER_ROSE,
            Blocks.SWEET_BERRY_BUSH, Blocks.COBWEB);

    private DungeonRoomLayout() {}

    public static BlockPos minimum(DungeonRoomCatalog.Definition room) {
        return room.origin().offset(-1, -1, -1);
    }

    public static BlockPos maximum(DungeonRoomCatalog.Definition room) {
        return room.origin().offset(room.size().getX(), room.size().getY(), room.size().getZ());
    }

    public static Map<BlockPos, BlockState> paths(DungeonRoomCatalog.Definition room) {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        for (int blockX = 2; blockX <= 81; blockX++) {
            for (int blockZ = 22; blockZ <= 24; blockZ++) {
                column(blocks, blockX, blockZ, blockX < 62 ? 63 : 64,
                        blockX < 62 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : blockX == 62
                                ? Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST) : floor(blockX, blockZ));
            }
        }
        for (int blockZ = 23; blockZ <= room.rowZ(); blockZ++) {
            for (int blockX = 79; blockX <= 81; blockX++) {
                column(blocks, blockX, blockZ, 64, floor(blockX, blockZ));
            }
        }
        int entryX = room.origin().getX() + room.entrance().getX();
        for (int blockX = 80; blockX <= entryX + 1; blockX++) {
            for (int blockZ = room.rowZ() - 1; blockZ <= room.rowZ() + 1; blockZ++) {
                column(blocks, blockX, blockZ, 64, floor(blockX, blockZ));
            }
        }
        int rampStart = room.origin().getZ() - room.entrance().getY() - 1;
        for (int blockZ = room.rowZ() + 2; blockZ < room.origin().getZ(); blockZ++) {
            int rise = Math.min(room.entrance().getY(), Math.max(0, blockZ - rampStart + 1));
            boolean stair = rise > 0 && blockZ < room.origin().getZ() - 1;
            for (int blockX = entryX - 1; blockX <= entryX + 1; blockX++) {
                column(blocks, blockX, blockZ, 64 + rise, stair
                        ? Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH)
                        : floor(blockX, blockZ));
            }
        }
        return blocks;
    }

    public static Map<BlockPos, BlockState> plan(DungeonRoomCatalog.Definition room) {
        Map<BlockPos, BlockState> blocks = paths(room);
        for (int blockX = 0; blockX < room.size().getX(); blockX++) {
            for (int blockZ = 0; blockZ < room.size().getZ(); blockZ++) {
                blocks.put(room.origin().offset(blockX, -1, blockZ), Blocks.MOSS_BLOCK.defaultBlockState());
            }
        }
        return blocks;
    }

    public static void validate(DungeonRoomCatalog.Definition room, CompoundTag tag, Map<BlockPos, BlockState> states) {
        for (Tag value : tag.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) value;
            CompoundTag blockEntity = record.getCompound("nbt");
            if (blockEntity.contains("LootTable") || !blockEntity.getList("Items", Tag.TAG_COMPOUND).isEmpty()) {
                throw new IllegalArgumentException("Non-empty reward container: " + room.id());
            }
        }
        Set<String> forbidden = Set.of("minecraft:command_block", "minecraft:chain_command_block", "minecraft:repeating_command_block",
                "minecraft:structure_block", "minecraft:jigsaw", "minecraft:spawner");
        for (Map.Entry<BlockPos, BlockState> entry : states.entrySet()) {
            String identity = BuiltInRegistries.BLOCK.getKey(entry.getValue().getBlock()).toString();
            if (!identity.startsWith("minecraft:") || forbidden.contains(identity)) {
                throw new IllegalArgumentException("Forbidden room block: " + identity);
            }
            BlockPos local = entry.getKey().subtract(room.origin());
            if (!entry.getValue().getFluidState().isEmpty() && (local.getX() == 0 || local.getZ() == 0
                    || local.getX() == room.size().getX() - 1 || local.getZ() == room.size().getZ() - 1)) {
                throw new IllegalArgumentException("Exposed boundary fluid: " + local.toShortString());
            }
            if (entry.getValue().is(Blocks.FIRE) || entry.getValue().is(Blocks.SOUL_FIRE)) {
                for (Direction direction : Direction.values()) {
                    BlockPos neighbour = entry.getKey().relative(direction);
                    if (states.getOrDefault(neighbour, Blocks.AIR.defaultBlockState())
                            .isFlammable(EmptyBlockGetter.INSTANCE, neighbour, direction.getOpposite())) {
                        throw new IllegalArgumentException("Fire touches flammable construction: " + local.toShortString());
                    }
                }
            }
        }
        BlockPos entrance = room.origin().offset(room.entrance());
        for (int offset = -1; offset <= 1; offset++) {
            BlockPos feet = entrance.offset(offset, 0, 0);
            if (!standable(states, feet) || !clear(states, feet.above(2))
                    || !state(states, feet.below()).isFaceSturdy(EmptyBlockGetter.INSTANCE, feet.below(), Direction.UP)) {
                throw new IllegalArgumentException("North opening needs a dry, supported 3x3 passage: " + feet.toShortString());
            }
        }
        BlockPos interior = room.origin().offset(room.interior());
        if (!standable(states, interior) || !reachable(room, states, entrance, interior)) {
            throw new IllegalArgumentException("Entrance and interior are not walk-connected: " + room.id());
        }
    }

    private static boolean reachable(DungeonRoomCatalog.Definition room, Map<BlockPos, BlockState> states, BlockPos start, BlockPos target) {
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        pending.add(start);
        visited.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            if (current.equals(target)) {
                return true;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int height : new int[]{0, -1, 1}) {
                    BlockPos next = current.relative(direction).offset(0, height, 0);
                    BlockPos local = next.subtract(room.origin());
                    if (local.getX() < 0 || local.getX() >= room.size().getX() || local.getZ() < 0 || local.getZ() >= room.size().getZ()
                            || local.getY() < 1 || local.getY() + 1 >= room.size().getY() || visited.contains(next)) {
                        continue;
                    }
                    if (height != 0 && !(state(states, current.below()).getBlock() instanceof StairBlock)
                            && !(state(states, next.below()).getBlock() instanceof StairBlock)) {
                        continue;
                    }
                    if (standable(states, next)) {
                        visited.add(next);
                        pending.addLast(next);
                    }
                }
            }
        }
        return false;
    }

    private static boolean standable(Map<BlockPos, BlockState> states, BlockPos feet) {
        BlockState floor = state(states, feet.below());
        return clear(states, feet) && clear(states, feet.above()) && !HAZARDS.contains(floor.getBlock()) && floor.getFluidState().isEmpty()
                && (floor.isFaceSturdy(EmptyBlockGetter.INSTANCE, feet.below(), Direction.UP) || floor.getBlock() instanceof StairBlock);
    }

    private static boolean clear(Map<BlockPos, BlockState> states, BlockPos position) {
        BlockState block = state(states, position);
        return !HAZARDS.contains(block.getBlock()) && block.getFluidState().isEmpty()
                && block.getCollisionShape(EmptyBlockGetter.INSTANCE, position).isEmpty();
    }

    private static BlockState state(Map<BlockPos, BlockState> states, BlockPos position) {
        return states.getOrDefault(position, Blocks.AIR.defaultBlockState());
    }

    private static BlockState floor(int blockX, int blockZ) {
        return ((blockX == 80 && Math.floorMod(blockZ - 23, 16) == 0)
                || (Math.floorMod(blockZ - 23, 160) == 0 && blockX % 16 == 0)
                ? Blocks.SEA_LANTERN : Blocks.POLISHED_BLACKSTONE_BRICKS).defaultBlockState();
    }

    private static void column(Map<BlockPos, BlockState> blocks, int blockX, int blockZ, int height, BlockState surface) {
        for (int blockY = 63; blockY <= height; blockY++) {
            blocks.put(new BlockPos(blockX, blockY, blockZ), blockY == height ? surface : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        }
    }
}
