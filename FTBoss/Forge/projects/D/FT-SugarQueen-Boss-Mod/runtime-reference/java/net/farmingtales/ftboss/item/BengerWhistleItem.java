/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
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
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.BossCollision;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BengerWhistleItem
extends Item {
    public BengerWhistleItem(Item.Properties props) {
        super(props);
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            if (!SummonGate.allow(level, player, "\u8fb9\u7267\u72ac\u738b benger")) {
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            BlockPos pos = player.m_20183_().m_5484_(player.m_6350_(), 4);
            BengerEntity boss = (BengerEntity)((EntityType)FTEntities.BENGER.get()).m_20615_((Level)sl);
            if (boss == null) {
                FTBossMod.LOGGER.warn("[FTBoss] benger create() returned null");
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            if (!BossCollision.place(sl, (Entity)boss, new Vec3((double)pos.m_123341_() + 0.5, (double)pos.m_123342_(), (double)pos.m_123343_() + 0.5), BossCollision.SpawnMode.GROUND, player.m_146908_() + 180.0f) || !sl.m_7967_((Entity)boss)) {
                BossCollision.tellNoRoom(player, "\u8fb9\u7267\u72ac\u738b benger");
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            sl.m_6263_(null, boss.m_20185_(), boss.m_20186_(), boss.m_20189_(), SoundEvents.f_12620_, SoundSource.HOSTILE, 1.8f, 0.7f);
            if (!player.m_150110_().f_35937_) {
                stack.m_41774_(1);
            }
            FTBossMod.LOGGER.info("[FTBoss] benger \u53ec\u5524\u4e8e {}, {}, {}", new Object[]{pos.m_123341_(), pos.m_123342_(), pos.m_123343_()});
        }
        player.m_21011_(hand, true);
        return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.m_5776_());
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add((Component)Component.m_237113_((String)"\u00a77\u8fb9\u7267\u72ac\u738b\u7684\u53ec\u5524\u7269\uff1a\u00a7f\u53f3\u952e\u5728\u4e3b\u4e16\u754c\u53ec\u5524 benger"));
        tip.add((Component)Component.m_237113_((String)"\u00a78\u5b83\u5634\u91cc\u53fc\u7740\u4e00\u6839\u6253\u72d7\u68d2 \u2014\u2014 \u51fb\u8d25\u5b83\u5c31\u80fd\u62ff\u8d70"));
    }
}

