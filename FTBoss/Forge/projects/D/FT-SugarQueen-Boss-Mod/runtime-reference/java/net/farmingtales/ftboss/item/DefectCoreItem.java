/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.BossCollision;
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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DefectCoreItem
extends Item {
    public DefectCoreItem() {
        super(new Item.Properties().m_41487_(16).m_41497_(Rarity.EPIC));
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (!level.f_46443_) {
            if (!SummonGate.allow(level, player, "\u6545\u969c\u673a\u5668\u4eba")) {
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            try {
                Vec3 look = player.m_20154_();
                Vec3 pos = player.m_20182_().m_82520_(look.f_82479_ * 4.0, 0.0, look.f_82481_ * 4.0);
                Entity raw = ((EntityType)FTEntities.DEFECT_CORE.get()).m_20615_(level);
                if (raw instanceof DefectCoreEntity) {
                    ServerLevel server;
                    DefectCoreEntity core = (DefectCoreEntity)raw;
                    if (level instanceof ServerLevel && BossCollision.place(server = (ServerLevel)level, (Entity)core, new Vec3(pos.f_82479_, player.m_20186_() + 2.4, pos.f_82481_), BossCollision.SpawnMode.AIR, player.m_146908_()) && level.m_7967_((Entity)core)) {
                        stack.m_41774_(1);
                        level.m_6263_(null, pos.f_82479_, pos.f_82480_, pos.f_82481_, SoundEvents.f_11736_, SoundSource.HOSTILE, 1.4f, 0.8f);
                        player.m_5661_((Component)Component.m_237113_((String)"\u00a75\u6545\u969c\u673a\u5668\u4eba \u00a77\u5df2\u542f\u52a8\u2026"), true);
                        FTBossMod.LOGGER.info("[FTBoss] \u6545\u969c\u673a\u5668\u4eba\u88ab\u53ec\u5524 @ {} {} {}", new Object[]{String.format("%.1f", core.m_20185_()), String.format("%.1f", core.m_20186_()), String.format("%.1f", core.m_20189_())});
                    } else {
                        BossCollision.tellNoRoom(player, "\u6545\u969c\u673a\u5668\u4eba");
                    }
                } else {
                    player.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u6545\u969c\u673a\u5668\u4eba\u521b\u5efa\u5931\u8d25"), true);
                }
            }
            catch (Throwable t) {
                FTBossMod.LOGGER.warn("[FTBoss] \u6545\u969c\u673a\u5668\u4eba\u53ec\u5524\u5f02\u5e38: {}", (Object)t.toString());
            }
        }
        return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.f_46443_);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add((Component)Component.m_237113_((String)"\u00a77\u53f3\u952e\u53ec\u5524 \u00a7d\u6545\u969c\u673a\u5668\u4eba\u00a77(\u53ef\u540c\u65f6\u53ec\u5524\u591a\u53ea)"));
        tip.add((Component)Component.m_237113_((String)"\u00a78\u4e24\u9636\u6bb5\u9608\u503c: 70% / 35% \u00b7 \u6b7b\u4ea1\u65f6\u6838\u5fc3\u8d85\u8f7d\u7206\u70b8"));
    }
}

