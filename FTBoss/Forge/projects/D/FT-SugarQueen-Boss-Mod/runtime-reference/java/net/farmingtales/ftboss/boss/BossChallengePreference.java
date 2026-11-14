/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class BossChallengePreference {
    public static final String TAG = "ftboss:challenge_difficulty";

    private BossChallengePreference() {
    }

    public static ManorDifficulty selected(Player player) {
        ServerPlayer serverPlayer;
        if (player == null) {
            return ManorDifficulty.NORMAL;
        }
        ManorDifficulty selected = ManorDifficulty.fromId(player.getPersistentData().m_128461_(TAG));
        if (selected == ManorDifficulty.IMPOSSIBLE && player instanceof ServerPlayer && !ManorDifficulty.unlocked(serverPlayer = (ServerPlayer)player)) {
            return ManorDifficulty.NORMAL;
        }
        return selected;
    }

    public static boolean select(ServerPlayer player, ManorDifficulty requested) {
        ManorDifficulty difficulty;
        ManorDifficulty manorDifficulty = difficulty = requested == null ? ManorDifficulty.NORMAL : requested;
        if (difficulty == ManorDifficulty.IMPOSSIBLE && !ManorDifficulty.unlocked(player)) {
            player.m_5661_((Component)Component.m_237115_((String)"message.ftboss.statue_hub.challenge.finale_locked"), true);
            return false;
        }
        player.getPersistentData().m_128359_(TAG, difficulty.id());
        player.m_5661_((Component)Component.m_237110_((String)"message.ftboss.statue_hub.challenge.selected", (Object[])new Object[]{Component.m_237115_((String)(difficulty == ManorDifficulty.IMPOSSIBLE ? "screen.ftboss.statue_hub.challenge.finale" : "screen.ftboss.statue_hub.challenge.normal"))}), true);
        FTBossMod.LOGGER.info("[FTBoss][\u77f3\u50cf\u6311\u6218] {} \u5c06\u9ed8\u8ba4\u6311\u6218\u96be\u5ea6\u5207\u6362\u4e3a {}", (Object)player.m_7755_().getString(), (Object)difficulty.id());
        return true;
    }

    @SubscribeEvent
    public static void copyAfterDeath(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().m_128441_(TAG)) {
            event.getEntity().getPersistentData().m_128359_(TAG, event.getOriginal().getPersistentData().m_128461_(TAG));
        }
    }
}

