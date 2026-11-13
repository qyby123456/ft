package net.farmingtales.ftdungeonmobs;

import com.mojang.brigadier.CommandDispatcher;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BloodPoolCommands {
    private BloodPoolCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ftbloodpool").requires(source -> source.hasPermission(2))
                .then(Commands.literal("build").then(Commands.argument("altar", BlockPosArgument.blockPos())
                        .executes(context -> build(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "altar"))))));
    }

    public static Map<BlockPos, BlockState> blueprint(BlockPos altar, Direction facing) {
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        for (int across = -4; across <= 4; across++) {
            for (int depth = -4; depth <= 4; depth++) {
                if (BloodPoolStructure.outer(across, depth)) {
                    BlockPos position = BloodPoolStructure.position(altar, facing, new BloodPoolStructure.Cell(across, depth));
                    result.put(position.below(), BloodPoolContent.BRICKS.get().defaultBlockState());
                }
            }
        }
        int decoration = 0;
        for (BloodPoolStructure.Cell cell : BloodPoolStructure.BORDER) {
            BlockPos position = BloodPoolStructure.position(altar, facing, cell);
            Direction inward = Math.abs(cell.across()) > Math.abs(cell.depth())
                    ? (cell.across() > 0 ? facing.getCounterClockWise() : facing.getClockWise())
                    : (cell.depth() > 0 ? facing : facing.getOpposite());
            Block block = Math.abs(cell.across()) == 4 && Math.abs(cell.depth()) == 2
                    ? BloodPoolContent.CORNER.get() : decoration++ % 3 == 0 ? BloodPoolContent.SKULL.get()
                    : decoration % 2 == 0 ? BloodPoolContent.RUNES.get() : BloodPoolContent.RIM.get();
            result.put(position, block.defaultBlockState().setValue(BloodPoolDecorBlock.FACING, inward));
        }
        result.put(altar, BloodPoolContent.ALTAR.get().defaultBlockState().setValue(BloodOfferingAltarBlock.FACING, facing));
        BlockPos center = altar.relative(facing.getOpposite(), 4);
        result.put(center, BloodPoolContent.BRICKS.get().defaultBlockState());
        result.put(center.above(), BloodPoolContent.SHRINE.get().defaultBlockState().setValue(BloodPoolDecorBlock.FACING, facing));
        for (int across : new int[]{-4, 4}) {
            for (int depth : new int[]{-4, 4}) {
                BlockPos base = BloodPoolStructure.position(altar, facing, new BloodPoolStructure.Cell(across, depth));
                result.put(base.below(), BloodPoolContent.BRICKS.get().defaultBlockState());
                result.put(base, BloodPoolContent.SKULL.get().defaultBlockState().setValue(BloodPoolDecorBlock.FACING, facing));
                result.put(base.above(), BloodPoolContent.BRICKS.get().defaultBlockState());
                result.put(base.above(2), FTDungeonMobs.BLOODTHORN_BRAZIER.get().defaultBlockState());
            }
        }
        for (int across = -1; across <= 1; across++) {
            result.put(altar.relative(facing).relative(facing.getClockWise(), across).below(),
                    BloodPoolContent.STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, facing.getOpposite()));
            result.put(altar.relative(facing, 2).relative(facing.getClockWise(), across).below(),
                    BloodPoolContent.SLAB.get().defaultBlockState());
        }
        for (BloodPoolStructure.Cell cell : BloodPoolStructure.INTERIOR) {
            result.put(BloodPoolStructure.position(altar, facing, cell), BloodPoolContent.BLOOD_BLOCK.get().defaultBlockState());
        }
        return result;
    }

    private static int build(CommandSourceStack source, BlockPos altar) {
        ServerLevel level = source.getLevel();
        Direction facing = source.getEntity() == null ? Direction.NORTH : source.getEntity().getDirection().getOpposite();
        Map<BlockPos, BlockState> plan = blueprint(altar, facing);
        for (Map.Entry<BlockPos, BlockState> entry : plan.entrySet()) {
            BlockPos position = entry.getKey();
            if (!level.isInWorldBounds(position) || !level.hasChunkAt(position) || !level.getWorldBorder().isWithinBounds(position)) {
                source.sendFailure(Component.translatable("message.ftdungeonmobs.blood_pool.build_bounds"));
                return 0;
            }
            BlockState existing = level.getBlockState(position);
            if (position.getY() == altar.getY() - 1 && entry.getValue().is(BloodPoolContent.BRICKS.get())
                    && existing.isFaceSturdy(level, position, Direction.UP) && level.getFluidState(position).isEmpty()) {
                continue;
            }
            if (existing.hasBlockEntity() || !existing.canBeReplaced() || !level.getFluidState(position).isEmpty()) {
                source.sendFailure(Component.translatable("message.ftdungeonmobs.blood_pool.build_occupied", position.toShortString()));
                return 0;
            }
        }
        int placed = 0;
        for (Map.Entry<BlockPos, BlockState> entry : plan.entrySet()) {
            BlockPos position = entry.getKey();
            if (position.getY() == altar.getY() - 1 && entry.getValue().is(BloodPoolContent.BRICKS.get())
                    && level.getBlockState(position).isFaceSturdy(level, position, Direction.UP)) {
                continue;
            }
            level.setBlock(position, entry.getValue(), Block.UPDATE_ALL);
            placed++;
        }
        int count = placed;
        source.sendSuccess(() -> Component.translatable("message.ftdungeonmobs.blood_pool.built", count), true);
        return placed;
    }
}
