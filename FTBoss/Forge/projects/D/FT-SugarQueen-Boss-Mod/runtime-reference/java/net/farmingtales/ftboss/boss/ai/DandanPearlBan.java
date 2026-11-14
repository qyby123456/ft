/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.EntityTeleportEvent$EnderPearl
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.dandannativelights.LightHooks;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class DandanPearlBan {
    public static final double RANGE = 48.0;
    private static final Map<UUID, Long> NOTICE = new ConcurrentHashMap<UUID, Long>();

    private DandanPearlBan() {
    }

    public static boolean isSealed(ServerPlayer player) {
        if (player == null || !player.m_6084_() || player.m_5833_()) {
            return false;
        }
        Level level = player.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        return DandanPearlBan.nearestSeal(level2, player) != null;
    }

    private static EasterBossEntity nearestSeal(ServerLevel level, ServerPlayer player) {
        EasterBossEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (EasterBossEntity boss : level.m_45976_(EasterBossEntity.class, player.m_20191_().m_82400_(48.0))) {
            double distance;
            if (boss.getVariant() != 1 || boss.isMini() || boss.m_21525_() || !boss.m_6084_() || boss.m_213877_() || (distance = boss.m_20280_((Entity)player)) > 2304.0 || distance >= bestDistance) continue;
            bestDistance = distance;
            best = boss;
        }
        return best;
    }

    public static void notice(ServerPlayer player, String what) {
        Level level;
        if (player == null || !((level = player.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        long now = level2.m_46467_();
        Long last = NOTICE.get(player.m_20148_());
        if (last != null && now - last < 40L) {
            return;
        }
        if (NOTICE.size() > 64) {
            NOTICE.entrySet().removeIf(entry -> now - (Long)entry.getValue() >= 40L);
        }
        NOTICE.put(player.m_20148_(), now);
        player.m_5661_((Component)Component.m_237113_((String)("\u00a75\u86cb\u86cb \u00a77\u7684\u672b\u5f71\u5c01\u9501: \u00a7d" + what + " \u00a77\u88ab\u7981\u6b62")), true);
        player.m_9236_().m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 0.7f, 0.6f);
        LightHooks.seal(player);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u86cb\u86cb\u672b\u5f71\u5c01\u9501\u62e6\u622a {} \u7684{}", (Object)player.m_7755_().getString(), (Object)what);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        try {
            if (event.getLevel().m_5776_()) {
                return;
            }
            Player player = event.getEntity();
            if (!(player instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer player2 = (ServerPlayer)player;
            if (!event.getItemStack().m_150930_(Items.f_42584_)) {
                return;
            }
            if (!DandanPearlBan.isSealed(player2)) {
                return;
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            DandanPearlBan.notice(player2, "\u672b\u5f71\u73cd\u73e0\u6295\u63b7");
        }
        catch (Throwable failure) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u86cb\u86cb\u672b\u5f71\u5c01\u9501(\u6295\u63b7)\u5f02\u5e38: {}", (Object)failure.toString());
        }
    }

    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        try {
            ServerPlayer player = event.getPlayer();
            if (player == null || player.m_9236_().m_5776_()) {
                return;
            }
            if (!DandanPearlBan.isSealed(player)) {
                return;
            }
            event.setCanceled(true);
            DandanPearlBan.notice(player, "\u672b\u5f71\u73cd\u73e0\u4f20\u9001");
        }
        catch (Throwable failure) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u86cb\u86cb\u672b\u5f71\u5c01\u9501(\u4f20\u9001)\u5f02\u5e38: {}", (Object)failure.toString());
        }
    }

    public static boolean blockThrow(ServerPlayer player, String what) {
        if (!DandanPearlBan.isSealed(player)) {
            return false;
        }
        DandanPearlBan.notice(player, what);
        return true;
    }

    public static boolean blockLanding(ServerPlayer player, String what) {
        if (!DandanPearlBan.isSealed(player)) {
            return false;
        }
        DandanPearlBan.notice(player, what);
        return true;
    }
}

