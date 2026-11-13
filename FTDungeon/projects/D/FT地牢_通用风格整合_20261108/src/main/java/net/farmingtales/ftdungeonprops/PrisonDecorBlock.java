package net.farmingtales.ftdungeonprops;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PrisonDecorBlock extends HorizontalDirectionalBlock {
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    public PrisonDecorBlock(BlockBehaviour.Properties properties, PrisonCatalog.Spec spec) {
        super(properties);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            VoxelShape shape = Shapes.empty();
            for (int[] bounds : spec.boxes()) {
                int[] rotated = rotateBox(bounds, direction);
                shape = Shapes.or(shape, Block.box(rotated[0], rotated[1], rotated[2],
                        rotated[3], rotated[4], rotated[5]));
            }
            shapes.put(direction, shape.optimize());
        }
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public static int[] rotateBox(int[] bounds, Direction direction) {
        return switch (direction) {
            case EAST -> new int[]{16 - bounds[5], bounds[1], bounds[0], 16 - bounds[2], bounds[4], bounds[3]};
            case SOUTH -> new int[]{16 - bounds[3], bounds[1], 16 - bounds[5], 16 - bounds[0], bounds[4], 16 - bounds[2]};
            case WEST -> new int[]{bounds[2], bounds[1], 16 - bounds[3], bounds[5], bounds[4], 16 - bounds[0]};
            default -> bounds.clone();
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
}
