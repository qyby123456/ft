/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.BossCollision;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ForbiddenAppleItem
extends Item {
    public ForbiddenAppleItem(Item.Properties props) {
        super(props);
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            if (!SummonGate.allow(level, player, "\u82f9\u679c\u7235\u58eb")) {
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            BlockPos pos = player.m_20183_().m_5484_(player.m_6350_(), 4);
            AppleBossEntity boss = (AppleBossEntity)((EntityType)FTEntities.APPLE_BOSS.get()).m_20615_((Level)sl);
            if (boss == null) {
                FTBossMod.LOGGER.warn("[FTBoss] Apple Boss create() returned null");
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            if (!BossCollision.place(sl, (Entity)boss, new Vec3((double)pos.m_123341_() + 0.5, (double)pos.m_123342_(), (double)pos.m_123343_() + 0.5), BossCollision.SpawnMode.GROUND, player.m_146908_() + 180.0f) || !sl.m_7967_((Entity)boss)) {
                BossCollision.tellNoRoom(player, "\u82f9\u679c\u7235\u58eb");
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            sl.m_6263_(null, boss.m_20185_(), boss.m_20186_(), boss.m_20189_(), SoundEvents.f_12563_, SoundSource.HOSTILE, 1.6f, 0.75f);
            if (!player.m_150110_().f_35937_) {
                stack.m_41774_(1);
                if (!stack.m_41619_()) {
                    stack.m_41764_(0);
                }
                player.m_150109_().m_6596_();
            }
            FTBossMod.LOGGER.info("[FTBoss] forbidden apple used: creative={} remaining={}", (Object)player.m_150110_().f_35937_, (Object)stack.m_41613_());
            FTBossMod.LOGGER.info("[FTBoss] Apple Boss summoned at {}, {}, {}", new Object[]{pos.m_123341_(), pos.m_123342_(), pos.m_123343_()});
        }
        player.m_21011_(hand, true);
        return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.m_5776_());
    }
}

