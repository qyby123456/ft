package net.farmingtales.ftdungeonmobs;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class DungeonArrival {
    public static final BlockPos RETURN_GATE = new BlockPos(0, 64, 0);
    public static final BlockPos ARRIVAL = new BlockPos(0, 64, 3);

    private DungeonArrival() {}

    public static boolean prepare(ServerLevel level, DungeonGateData data) {
        if (data.arrivalBuilt()) {
            return ensureReturnGate(level);
        }
        Map<BlockPos, BlockState> blueprint = blueprint();
        for (Map.Entry<BlockPos, BlockState> entry : blueprint.entrySet()) {
            BlockState current = level.getBlockState(entry.getKey());
            if (!current.isAir() && !current.equals(entry.getValue())
                    && !(entry.getKey().getY() == 63 && current.is(Blocks.POLISHED_BLACKSTONE_BRICKS))) {
                return false;
            }
        }
        for (Map.Entry<BlockPos, BlockState> entry : blueprint.entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), Block.UPDATE_CLIENTS);
        }
        data.markArrivalBuilt();
        return true;
    }

    private static boolean ensureReturnGate(ServerLevel level) {
        BlockState gate = FTDungeonMobs.DUNGEON_RETURN_GATE.get().defaultBlockState()
                .setValue(DungeonGateBlock.FACING, Direction.SOUTH);
        for (int part = 0; part < 12; part++) {
            BlockState current = level.getBlockState(DungeonGateBlock.partPosition(RETURN_GATE, Direction.SOUTH, part));
            if (!current.isAir() && !current.equals(gate.setValue(DungeonGateBlock.PART, part))) {
                return false;
            }
        }
        if (!gate.canSurvive(level, RETURN_GATE)) {
            return false;
        }
        for (int part = 0; part < 12; part++) {
            level.setBlock(DungeonGateBlock.partPosition(RETURN_GATE, Direction.SOUTH, part),
                    gate.setValue(DungeonGateBlock.PART, part), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    private static Map<BlockPos, BlockState> blueprint() {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        for (int blockX = -8; blockX <= 8; blockX++) {
            for (int blockZ = -5; blockZ <= 15; blockZ++) {
                boolean border = Math.abs(blockX) == 8 || blockZ == -5 || blockZ == 15;
                blocks.put(new BlockPos(blockX, 63, blockZ), (border || blockX % 4 == 0
                        ? Blocks.CHISELED_POLISHED_BLACKSTONE : Blocks.POLISHED_BLACKSTONE_BRICKS).defaultBlockState());
                blocks.put(new BlockPos(blockX, 71, blockZ), Blocks.DEEPSLATE_TILES.defaultBlockState());
                if (border) {
                    for (int blockY = 64; blockY <= 70; blockY++) {
                        if (blockZ != 15 || Math.abs(blockX) > 1 || blockY > 67) {
                            blocks.put(new BlockPos(blockX, blockY, blockZ),
                                    (blockY == 64 || blockY == 69 ? Blocks.POLISHED_BLACKSTONE
                                            : Blocks.DEEPSLATE_BRICKS).defaultBlockState());
                        }
                    }
                }
            }
        }
        for (int blockX : new int[]{-6, 6}) {
            for (int blockZ : new int[]{-2, 5, 12}) {
                blocks.put(new BlockPos(blockX, 64, blockZ), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
                blocks.put(new BlockPos(blockX, 65, blockZ), Blocks.SOUL_LANTERN.defaultBlockState());
            }
        }
        BlockState gate = FTDungeonMobs.DUNGEON_RETURN_GATE.get().defaultBlockState()
                .setValue(DungeonGateBlock.FACING, Direction.SOUTH);
        for (int part = 0; part < 12; part++) {
            blocks.put(DungeonGateBlock.partPosition(RETURN_GATE, Direction.SOUTH, part),
                    gate.setValue(DungeonGateBlock.PART, part));
        }
        return blocks;
    }
}
