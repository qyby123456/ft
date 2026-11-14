/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Vec3i
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
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.util.BossCollision;
import net.minecraft.core.Vec3i;
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

public class TideBanquetItem
extends Item {
    public TideBanquetItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        boolean placed;
        ItemStack stack = player.m_21120_(hand);
        if (player.m_36335_().m_41519_((Item)this)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (!(level instanceof ServerLevel)) {
            return InteractionResultHolder.m_19090_((Object)stack);
        }
        ServerLevel server = (ServerLevel)level;
        if (!SummonGate.allow(level, player, "\u6f6e\u5bb4\u9c7c\u738b")) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        TideFishBossEntity boss = (TideFishBossEntity)((EntityType)FTEntities.TIDE_FISH_BOSS.get()).m_20615_((Level)server);
        if (boss == null) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        Vec3 direction = player.m_20154_().m_82542_(1.0, 0.0, 1.0).m_82541_();
        if (direction.m_82556_() < 0.01) {
            direction = Vec3.m_82528_((Vec3i)player.m_6350_().m_122436_());
        }
        Vec3 center = player.m_20182_().m_82549_(direction.m_82490_(5.0));
        boolean bl = placed = BossCollision.place(server, (Entity)boss, new Vec3(center.f_82479_, player.m_20186_(), center.f_82481_), BossCollision.SpawnMode.FLUID, player.m_146908_() + 180.0f) && server.m_7967_((Entity)boss);
        if (!placed) {
            player.m_5661_((Component)Component.m_237115_((String)"message.ftboss.tide_no_room"), true);
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (!player.m_150110_().f_35937_) {
            stack.m_41774_(1);
        }
        player.m_36335_().m_41524_((Item)this, 40);
        server.m_5594_(null, boss.m_20183_(), SoundEvents.f_11767_, SoundSource.HOSTILE, 1.5f, 0.6f);
        return InteractionResultHolder.m_19096_((Object)stack);
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.tide_banquet"));
    }
}

