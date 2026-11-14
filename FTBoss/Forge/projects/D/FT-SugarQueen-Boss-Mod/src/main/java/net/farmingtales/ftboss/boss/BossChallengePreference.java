package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Player-selected default difficulty from the Boss Statue challenge panel. */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class BossChallengePreference {
    public static final String TAG = "ftboss:challenge_difficulty";

    private BossChallengePreference() {}

    public static ManorDifficulty selected(Player player) {
        if (player == null) return ManorDifficulty.NORMAL;
        ManorDifficulty selected = ManorDifficulty.fromId(player.getPersistentData().getString(TAG));
        if (selected == ManorDifficulty.IMPOSSIBLE && player instanceof ServerPlayer serverPlayer
                && !ManorDifficulty.unlocked(serverPlayer)) return ManorDifficulty.NORMAL;
        return selected;
    }

    public static boolean select(ServerPlayer player, ManorDifficulty requested) {
        ManorDifficulty difficulty = requested == null ? ManorDifficulty.NORMAL : requested;
        if (difficulty == ManorDifficulty.IMPOSSIBLE && !ManorDifficulty.unlocked(player)) {
            player.displayClientMessage(Component.translatable(
                    "message.ftboss.statue_hub.challenge.finale_locked"), true);
            return false;
        }
        player.getPersistentData().putString(TAG, difficulty.id());
        player.displayClientMessage(Component.translatable(
                "message.ftboss.statue_hub.challenge.selected",
                Component.translatable(difficulty == ManorDifficulty.IMPOSSIBLE
                        ? "screen.ftboss.statue_hub.challenge.finale"
                        : "screen.ftboss.statue_hub.challenge.normal")), true);
        FTBossMod.LOGGER.info("[FTBoss][石像挑战] {} 将默认挑战难度切换为 {}",
                player.getName().getString(), difficulty.id());
        return true;
    }

    @SubscribeEvent
    public static void copyAfterDeath(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(TAG)) {
            event.getEntity().getPersistentData().putString(TAG,
                    event.getOriginal().getPersistentData().getString(TAG));
        }
    }
}
