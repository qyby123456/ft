package net.farmingtales.ftdungeontrading.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record TradeContext(ServerLevel level, BlockPos position, ServerPlayer player) {
    public TradeContext {
        position = position.immutable();
    }
}
