package net.farmingtales.ftdungeonmobs;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

public final class DungeonGateBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 11);
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty MOVING = BooleanProperty.create("moving");
    public static final IntegerProperty PORTAL_TIER = IntegerProperty.create("tier", 0, 5);
    public static final int ANCHOR_PART = 1;
    private static final VoxelShape NORTH_SOUTH_SHAPE = Block.box(0, 0, 4, 16, 16, 12);
    private static final VoxelShape EAST_WEST_SHAPE = Block.box(4, 0, 0, 12, 16, 16);
    private static final VoxelShape[][] OPEN_SHAPES = openShapes();
    private final boolean returning;

    public DungeonGateBlock(Properties properties, boolean returning) {
        super(properties);
        this.returning = returning;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, ANCHOR_PART)
                .setValue(OPEN, false).setValue(MOVING, false).setValue(PORTAL_TIER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, OPEN, MOVING, PORTAL_TIER);
    }

    public boolean returning() {
        return returning;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return state.getValue(PART) == ANCHOR_PART ? new DungeonGateBlockEntity(position, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide || state.getValue(PART) != ANCHOR_PART || type != FTDungeonMobs.DUNGEON_GATE_ENTITY.get()
                ? null : (world, position, current, entity) -> DungeonGateBlockEntity.serverTick((ServerLevel) world,
                        position, current, (DungeonGateBlockEntity) entity);
    }

    public static Vec3 canonical(BlockPos anchor, Direction facing, Vec3 point) {
        Vec3 relative = point.subtract(Vec3.atBottomCenterOf(anchor));
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();
        return new Vec3(24 + (relative.x * right.getStepX() + relative.z * right.getStepZ()) * 16,
                relative.y * 16, 8 + (relative.x * back.getStepX() + relative.z * back.getStepZ()) * 16);
    }

    public static AABB passage(BlockPos anchor, Direction facing) {
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();
        Vec3 center = Vec3.atBottomCenterOf(anchor);
        Vec3 first = center.add(right.getStepX() * -14.0 / 16 + back.getStepX() * -3.0 / 16, 3.0 / 16,
                right.getStepZ() * -14.0 / 16 + back.getStepZ() * -3.0 / 16);
        Vec3 second = center.add(right.getStepX() * 14.0 / 16 + back.getStepX() * 7.0 / 16, 54.0 / 16,
                right.getStepZ() * 14.0 / 16 + back.getStepZ() * 7.0 / 16);
        return new AABB(first, second);
    }

    public static BlockPos anchor(BlockPos position, BlockState state) {
        int part = state.getValue(PART);
        return position.relative(state.getValue(FACING).getClockWise(), 1 - part % 3).below(part / 3);
    }

    public static BlockPos partPosition(BlockPos anchor, Direction facing, int part) {
        return anchor.relative(facing.getClockWise(), part % 3 - 1).above(part / 3);
    }

    public static boolean complete(LevelReader level, BlockPos anchor, DungeonGateBlock gate, Direction facing) {
        for (int part = 0; part < 12; part++) {
            BlockPos position = partPosition(anchor, facing, part);
            if (level instanceof ServerLevel serverLevel && !serverLevel.hasChunkAt(position)) {
                return false;
            }
            BlockState state = level.getBlockState(position);
            if (!state.is(gate) || state.getValue(PART) != part || state.getValue(FACING) != facing) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) {
            return state;
        }
        int part = state.getValue(PART);
        return state.rotate(mirror.getRotation(state.getValue(FACING))).setValue(PART, part / 3 * 3 + 2 - part % 3);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos anchor = context.getClickedPos();
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (context.getPlayer() == null) {
            return null;
        }
        for (int part = 0; part < 12; part++) {
            BlockPos position = partPosition(anchor, facing, part);
            if (!level.isInWorldBounds(position) || !level.getWorldBorder().isWithinBounds(position)
                    || !level.getFluidState(position).isEmpty() || !level.mayInteract(context.getPlayer(), position)
                    || !level.getBlockState(position).canBeReplaced(context)
                    || !context.getPlayer().mayUseItemAt(position, context.getClickedFace(), context.getItemInHand())) {
                return null;
            }
        }
        BlockState state = defaultBlockState().setValue(FACING, facing);
        return state.canSurvive(level, anchor) ? state : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos position, BlockState state, LivingEntity placer, ItemStack stack) {
        for (int part = 0; part < 12; part++) {
            if (part != ANCHOR_PART) {
                level.setBlock(partPosition(position, state.getValue(FACING), part), state.setValue(PART, part), UPDATE_ALL);
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos position) {
        BlockPos anchor = anchor(position, state);
        for (int column = 0; column < 3; column++) {
            BlockPos support = partPosition(anchor, state.getValue(FACING), column).below();
            if (!level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
            BlockPos position, BlockPos neighborPosition) {
        if (direction == Direction.DOWN && !state.canSurvive(level, position)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, position, neighborPosition);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? NORTH_SOUTH_SHAPE : EAST_WEST_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        if (!state.getValue(OPEN)) {
            return getShape(state, level, position, context);
        }
        return OPEN_SHAPES[state.getValue(FACING).get2DDataValue()][state.getValue(PART)];
    }

    private static VoxelShape[][] openShapes() {
        VoxelShape[][] result = new VoxelShape[4][12];
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (int part = 0; part < 12; part++) {
                result[facing.get2DDataValue()][part] = openShape(part, facing);
            }
        }
        return result;
    }

    private static VoxelShape openShape(int part, Direction facing) {
        int column = part % 3;
        int row = part / 3;
        VoxelShape frame = Shapes.empty();
        if (row == 0) {
            frame = Block.box(0, 0, 4, 16, 3, 16);
        }
        if (column == 0) {
            frame = Shapes.or(frame, Block.box(0, 0, 3, 10, 16, 16));
        } else if (column == 2) {
            frame = Shapes.or(frame, Block.box(6, 0, 3, 16, 16, 16));
        }
        if (row == 3) {
            frame = Shapes.or(frame, Block.box(0, 6, 3, 16, 16, 16));
        }
        return facing == Direction.NORTH ? frame : rotateFrame(frame, facing);
    }

    private static VoxelShape rotateFrame(VoxelShape frame, Direction facing) {
        int turns = switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        VoxelShape result = frame;
        for (int turn = 0; turn < turns; turn++) {
            VoxelShape next = Shapes.empty();
            for (AABB box : result.toAabbs()) {
                next = Shapes.or(next, Shapes.box(1 - box.maxZ, box.minY, box.minX,
                        1 - box.minZ, box.maxY, box.maxX));
            }
            result = next;
        }
        return result;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator() || !level.mayInteract(player, position)) {
            return InteractionResult.FAIL;
        }
        if (hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            BlockPos root = anchor(position, state);
            if (moss.generator.MossBossPortals.handleUse(serverPlayer, root)) return InteractionResult.SUCCESS;
            Vec3 local = canonical(root, state.getValue(FACING), hit.getLocation());
            if (!returning && player.isShiftKeyDown()) {
                DungeonGateGenerators.open(serverPlayer, root);
            } else if ((local.x <= 10 || local.x >= 38) && local.y >= 25 && local.y <= 44 && local.z <= 6) {
                DungeonGateInteraction.useWallTorch(serverPlayer, root, this);
            } else {
                if (returning) DungeonGateTravel.use(serverPlayer, root, this, true);
                else DungeonGateGenerators.open(serverPlayer, root);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos position, Entity entity) {
        if (entity instanceof ServerPlayer player && state.getValue(OPEN) && !state.getValue(MOVING)
                && level.getBlockEntity(anchor(position, state)) instanceof DungeonGateBlockEntity gateEntity) {
            gateEntity.tryTravel(player);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos position, BlockState replacement, boolean moving) {
        super.onRemove(state, level, position, replacement, moving);
        if (!level.isClientSide && !replacement.is(this)) {
            BlockPos anchor = anchor(position, state);
            for (int part = 0; part < 12; part++) {
                BlockPos sibling = partPosition(anchor, state.getValue(FACING), part);
                BlockState other = level.getBlockState(sibling);
                if (other.is(this) && other.getValue(PART) == part
                        && other.getValue(FACING) == state.getValue(FACING)) {
                    level.setBlock(sibling, Blocks.AIR.defaultBlockState(), UPDATE_ALL | UPDATE_SUPPRESS_DROPS);
                }
            }
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftdungeonmobs.gate.size").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftdungeonmobs.gate.control").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable(returning ? "tooltip.ftdungeonmobs.gate.return"
                : "tooltip.ftdungeonmobs.gate.entry").withStyle(returning ? ChatFormatting.AQUA : ChatFormatting.DARK_RED));
    }
}
