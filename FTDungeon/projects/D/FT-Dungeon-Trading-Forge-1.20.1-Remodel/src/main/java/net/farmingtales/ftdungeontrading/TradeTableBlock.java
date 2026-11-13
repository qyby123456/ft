package net.farmingtales.ftdungeontrading;

import java.util.EnumMap;
import java.util.Map;
import net.farmingtales.ftdungeontrading.api.TradeContext;
import net.farmingtales.ftdungeontrading.api.TradeResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TradeTableBlock extends BaseEntityBlock {
    public enum TablePart implements StringRepresentable {
        LEFT, RIGHT;

        @Override
        public String getSerializedName() { return this == LEFT ? "left" : "right"; }

        public TablePart opposite() { return this == LEFT ? RIGHT : LEFT; }
    }

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<TablePart> PART = EnumProperty.create("part", TablePart.class);
    private static final Map<Direction, VoxelShape> LEFT_SHAPES = rotatedShapes(Shapes.or(
            Block.box(.5, 8.25, .5, 16, 11.26, 15.5),
            Block.box(1.1, 0, 1.1, 3.4, 9.6, 3.4), Block.box(1.1, 0, 12.6, 3.4, 9.6, 14.9),
            Block.box(1.8, 1.9, 1.8, 16, 3.55, 14.4)));
    private static final Map<Direction, VoxelShape> RIGHT_SHAPES = rotatedShapes(Shapes.or(
            Block.box(0, 8.25, .5, 15.5, 11.26, 15.5),
            Block.box(12.6, 0, 1.1, 14.9, 9.6, 3.4), Block.box(12.6, 0, 12.6, 14.9, 9.6, 14.9),
            Block.box(0, 1.9, 1.8, 14.2, 3.55, 14.4)));

    public TradeTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, TablePart.LEFT));
    }

    public static BlockPos partnerPosition(BlockState state, BlockPos position) {
        Direction direction = state.getValue(FACING).getClockWise();
        return position.relative(state.getValue(PART) == TablePart.LEFT ? direction : direction.getOpposite());
    }

    public static BlockPos primaryPosition(BlockState state, BlockPos position) {
        return state.hasProperty(PART) && state.getValue(PART) == TablePart.RIGHT ? partnerPosition(state, position) : position;
    }

    public static boolean hasPartner(LevelReader level, BlockPos position, BlockState state) {
        if (!(state.getBlock() instanceof TradeTableBlock)) return false;
        BlockPos partner = partnerPosition(state, position);
        if (!level.hasChunkAt(partner)) return false;
        BlockState other = level.getBlockState(partner);
        return other.is(state.getBlock()) && other.getValue(FACING) == state.getValue(FACING)
                && other.getValue(PART) == state.getValue(PART).opposite();
    }

    public static boolean mayTrade(ServerLevel level, BlockPos primary, Player player) {
        BlockState state = level.getBlockState(primary);
        if (!hasPartner(level, primary, state)) return false;
        BlockPos partner = partnerPosition(state, primary);
        return level.mayInteract(player, primary) && level.mayInteract(player, partner)
                && Math.min(player.distanceToSqr(primary.getCenter()), player.distanceToSqr(partner.getCenter())) <= 36;
    }

    public boolean canPlacePartner(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos partner = partnerPosition(state, context.getClickedPos());
        Player player = context.getPlayer();
        return level.hasChunkAt(partner) && !level.isOutsideBuildHeight(partner) && level.getWorldBorder().isWithinBounds(partner)
                && level.getBlockState(partner).canBeReplaced(context)
                && (player == null || level.mayInteract(player, partner)
                    && player.mayUseItemAt(partner, context.getClickedFace(), context.getItemInHand()))
                && level.isUnobstructed(state.setValue(PART, TablePart.RIGHT), partner,
                    player == null ? CollisionContext.empty() : CollisionContext.of(player));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        return canPlacePartner(context, state) ? state : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, PART); }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return mirror == Mirror.NONE ? state : state.rotate(mirror.getRotation(state.getValue(FACING))).setValue(PART, state.getValue(PART).opposite());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos position, BlockPos neighborPosition) {
        if (neighborPosition.equals(partnerPosition(state, position)) && !hasPartner(level, position, state)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighbor, level, position, neighborPosition);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos position, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && hasPartner(level, position, state)) {
            level.setBlock(partnerPosition(state, position), Blocks.AIR.defaultBlockState(), 35);
        }
        super.playerWillDestroy(level, position, state, player);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return (state.getValue(PART) == TablePart.LEFT ? LEFT_SHAPES : RIGHT_SHAPES).get(state.getValue(FACING));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return state.getValue(PART) == TablePart.LEFT ? new TradeTableBlockEntity(position, state) : null;
    }

    @Override
    public <EntityType extends BlockEntity> BlockEntityTicker<EntityType> getTicker(Level level, BlockState state, BlockEntityType<EntityType> type) {
        return level.isClientSide || state.getValue(PART) != TablePart.LEFT ? null
                : createTickerHelper(type, FTDungeonTrading.TABLE_ENTITY.get(), (world, position, current, table) -> {
                    if (world.getGameTime() % 20 == 0) table.refreshDisplay();
                });
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.CONSUME;
        TradeResult result = TradeService.interact(new TradeContext((ServerLevel) level, position, serverPlayer));
        serverPlayer.displayClientMessage(result.message(), result.status() != TradeResult.Status.PREVIEW);
        return InteractionResult.CONSUME;
    }

    private static Map<Direction, VoxelShape> rotatedShapes(VoxelShape north) {
        Map<Direction, VoxelShape> result = new EnumMap<>(Direction.class);
        VoxelShape current = north;
        Direction direction = Direction.NORTH;
        for (int rotation = 0; rotation < 4; rotation++) {
            result.put(direction, current);
            VoxelShape rotated = Shapes.empty();
            for (var bounds : current.toAabbs()) {
                rotated = Shapes.or(rotated, Shapes.box(1 - bounds.maxZ, bounds.minY, bounds.minX, 1 - bounds.minZ, bounds.maxY, bounds.maxX));
            }
            current = rotated;
            direction = direction.getClockWise();
        }
        return result;
    }
}
