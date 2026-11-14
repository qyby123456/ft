package net.farmingtales.ftboss.boss.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;

public final class SparrowDisruptionTargets {
    private SparrowDisruptionTargets() {}

    public static boolean valid(Player player) {
        return player instanceof ServerPlayer && !(player instanceof FakePlayer) && BossTargets.valid(player);
    }
}
