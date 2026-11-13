package net.farmingtales.ftdungeonmobs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BloodPoolDecorBlock extends HorizontalDirectionalBlock {
    public enum Kind { RUNE, RIM, CORNER, SKULL, SHRINE }

    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    private final VoxelShape[] shapes = new VoxelShape[4];

    public BloodPoolDecorBlock(Properties properties, Kind kind) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
        VoxelShape north = switch (kind) {
            case RIM -> Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 3, 10, 16, 16, 16));
            case CORNER -> Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 3, 10, 16, 16, 16), Block.box(10, 3, 0, 16, 16, 10));
            case SHRINE -> Shapes.or(Block.box(0, 0, 2, 16, 4, 16), Block.box(2, 4, 3, 14, 14, 15),
                    Block.box(0, 12, 7, 4, 16, 15), Block.box(12, 12, 7, 16, 16, 15));
            default -> Shapes.block();
        };
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int turns = switch (facing) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
            VoxelShape rotated = north;
            for (int turn = 0; turn < turns; turn++) {
                VoxelShape next = Shapes.empty();
                for (AABB box : rotated.toAabbs()) {
                    next = Shapes.or(next, Shapes.box(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX));
                }
                rotated = next;
            }
            shapes[facing.get2DDataValue()] = rotated;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return shapes[state.getValue(FACING).get2DDataValue()];
    }
}
