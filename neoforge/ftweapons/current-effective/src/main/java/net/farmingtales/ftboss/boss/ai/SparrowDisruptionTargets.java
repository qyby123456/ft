package net.farmingtales.ftboss.boss.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.FakePlayer;

public final class SparrowDisruptionTargets {
    private SparrowDisruptionTargets() {
    }

    public static boolean valid(Player var0) {
        return var0 instanceof ServerPlayer && !(var0 instanceof FakePlayer) && BossTargets.valid(var0);
    }
}
