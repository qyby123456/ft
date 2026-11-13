package net.farmingtales.ftdungeonmobs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ArenaTorchBlockEntity extends BlockEntity {
    public ArenaTorchBlockEntity(BlockPos position, BlockState state) {
        super(FTDungeonMobs.ARENA_TORCH_ENTITY.get(), position, state);
    }
}
