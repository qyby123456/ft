package net.farmingtales.ftdungeonmobs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public final class BloodPoolActivateEvent extends Event {
    private final ServerPlayer player;
    private final BlockPos altar;

    public BloodPoolActivateEvent(ServerPlayer player, BlockPos altar) {
        this.player = player;
        this.altar = altar.immutable();
    }

    public ServerPlayer player() {
        return player;
    }

    public ServerLevel level() {
        return player.serverLevel();
    }

    public BlockPos altar() {
        return altar;
    }
}
