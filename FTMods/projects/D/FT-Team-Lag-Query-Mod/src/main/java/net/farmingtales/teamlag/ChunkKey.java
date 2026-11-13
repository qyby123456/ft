package net.farmingtales.teamlag;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record ChunkKey(String dimension, int x, int z) {
    public static ChunkKey at(ServerLevel level, BlockPos pos) {
        return new ChunkKey(level.dimension().location().toString(), pos.getX() >> 4, pos.getZ() >> 4);
    }
}
