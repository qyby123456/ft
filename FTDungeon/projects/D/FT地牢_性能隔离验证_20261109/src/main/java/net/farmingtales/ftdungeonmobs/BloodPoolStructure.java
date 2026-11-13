package net.farmingtales.ftdungeonmobs;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class BloodPoolStructure {
    public record Cell(int across, int depth) {}
    public record Result(boolean valid, String reason) {}

    public static final TagKey<Block> BOUNDARY = TagKey.create(Registries.BLOCK, FTDungeonMobs.resource("blood_pool_boundary"));
    public static final List<Cell> INTERIOR = cells(true);
    public static final List<Cell> BORDER = cells(false);

    private BloodPoolStructure() {}

    public static boolean outer(int across, int depth) {
        return Math.abs(across) <= 4 && Math.abs(depth) <= 4 && Math.abs(across) + Math.abs(depth) <= 6;
    }

    public static boolean inner(int across, int depth) {
        return Math.abs(across) <= 3 && Math.abs(depth) <= 3 && Math.abs(across) + Math.abs(depth) <= 5;
    }

    private static List<Cell> cells(boolean inside) {
        List<Cell> result = new ArrayList<>();
        for (int across = -4; across <= 4; across++) {
            for (int depth = -4; depth <= 4; depth++) {
                if (inside ? inner(across, depth) && (across != 0 || depth != 0) : outer(across, depth) && !inner(across, depth)) {
                    result.add(new Cell(across, depth));
                }
            }
        }
        return List.copyOf(result);
    }

    public static BlockPos position(BlockPos altar, Direction facing, Cell cell) {
        return altar.relative(facing.getOpposite(), 4).relative(facing.getClockWise(), cell.across())
                .relative(facing.getOpposite(), cell.depth());
    }

    public static Result validate(Level level, BlockPos altar, Direction facing) {
        BlockPos center = altar.relative(facing.getOpposite(), 4);
        for (Cell cell : INTERIOR) {
            BlockPos position = position(altar, facing, cell);
            if (!loaded(level, position) || !loaded(level, position.below())) {
                return new Result(false, "unloaded");
            }
            net.minecraft.world.level.material.FluidState fluid = level.getFluidState(position);
            if (!fluid.is(BloodPoolContent.BLOOD.get()) || !fluid.isSource()) {
                return new Result(false, "blood");
            }
            if (!level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP)) {
                return new Result(false, "floor");
            }
        }
        for (Cell cell : BORDER) {
            BlockPos position = position(altar, facing, cell);
            if (!loaded(level, position)) {
                return new Result(false, "unloaded");
            }
            if (!position.equals(altar) && !level.getBlockState(position).is(BOUNDARY)) {
                return new Result(false, "border");
            }
        }
        if (!loaded(level, center.above()) || !level.getBlockState(center).is(BloodPoolContent.BRICKS.get())
                || !level.getBlockState(center.above()).is(BloodPoolContent.SHRINE.get())
                || level.getBlockState(center.above()).getValue(BloodPoolDecorBlock.FACING) != facing) {
            return new Result(false, "shrine");
        }
        return new Result(true, "ready");
    }

    public static void setLit(Level level, BlockPos altar, Direction facing, boolean lit) {
        List<BlockPos> positions = new ArrayList<>();
        for (Cell cell : BORDER) {
            positions.add(position(altar, facing, cell));
        }
        positions.add(altar.relative(facing.getOpposite(), 4).above());
        for (BlockPos position : positions) {
            if (loaded(level, position)) {
                BlockState state = level.getBlockState(position);
                if (state.getBlock() instanceof BloodPoolDecorBlock && state.getValue(BloodPoolDecorBlock.LIT) != lit) {
                    level.setBlock(position, state.setValue(BloodPoolDecorBlock.LIT, lit), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private static boolean loaded(Level level, BlockPos position) {
        return level.isInWorldBounds(position) && level.hasChunkAt(position);
    }
}
