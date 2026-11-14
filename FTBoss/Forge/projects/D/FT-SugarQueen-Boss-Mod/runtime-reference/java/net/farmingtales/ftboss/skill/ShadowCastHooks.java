/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.slf4j.Logger
 */
package net.farmingtales.ftboss.skill;

import com.mojang.logging.LogUtils;
import net.farmingtales.ftboss.boss.ai.DandanPearlBan;
import net.farmingtales.ftboss.entity.ShadowPearlEntity;
import net.farmingtales.ftboss.item.weapons.ShadowSwordItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

final class ShadowCastHooks {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ShadowCastHooks() {
    }

    private static void anim(ServerPlayer player, InteractionHand hand, String name) {
        ItemStack stack = player.m_21120_(hand);
        Item item = stack.m_41720_();
        if (item instanceof ShadowSwordItem) {
            ShadowSwordItem sword = (ShadowSwordItem)item;
            sword.triggerSkillAnim((Entity)player, stack, name);
        }
    }

    static void started(ServerPlayer player, long castId, InteractionHand hand, long startTick) {
        LOGGER.info("[ShadowCast] START player={} id={} hand={} tick={}", new Object[]{player.m_36316_().getName(), castId, hand, startTick});
        ShadowCastHooks.anim(player, hand, "cast");
    }

    static void fired(ServerPlayer player, long castId, InteractionHand hand, ItemStack stack) {
        LOGGER.info("[ShadowCast] FIRE player={} id={} hand={} tick={}", new Object[]{player.m_36316_().getName(), castId, hand, player.m_284548_().m_46467_()});
        if (DandanPearlBan.blockThrow(player, "\u88c2\u77b3\u6295\u73e0 \u00b7 \u672b\u5f71\u73cd\u73e0\u6295\u63b7")) {
            return;
        }
        ShadowPearlEntity pearl = new ShadowPearlEntity((Level)player.m_284548_(), (LivingEntity)player);
        pearl.m_37251_((Entity)player, player.m_146909_(), player.m_146908_(), 0.0f, 1.9f, 0.0f);
        Vec3 look = player.m_20154_();
        Vec3 eye = player.m_146892_();
        pearl.m_6034_(eye.f_82479_ + look.f_82479_ * 0.6, eye.f_82480_ - 0.12, eye.f_82481_ + look.f_82481_ * 0.6);
        player.m_284548_().m_7967_((Entity)pearl);
        player.m_284548_().m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11857_, SoundSource.PLAYERS, 1.0f, 0.85f);
    }

    static void ended(ServerPlayer player, long castId, boolean fired) {
        LOGGER.info("[ShadowCast] END player={} id={} fired={} tick={}", new Object[]{player.m_36316_().getName(), castId, fired, player.m_284548_().m_46467_()});
    }

    static void aborted(ServerPlayer player, long castId, boolean fired, String reason) {
        LOGGER.info("[ShadowCast] ABORT player={} id={} fired={} reason={} tick={}", new Object[]{player.m_36316_().getName(), castId, fired, reason, player.m_284548_().m_46467_()});
        if (!fired) {
            ShadowCastHooks.anim(player, player.m_7655_(), "cancel");
        }
    }
}

