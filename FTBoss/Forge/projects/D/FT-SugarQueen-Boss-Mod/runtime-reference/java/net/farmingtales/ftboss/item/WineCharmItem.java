/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
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
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.farmingtales.ftboss.util.BossCollision;
import net.minecraft.ChatFormatting;
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

public class WineCharmItem
extends Item {
    public WineCharmItem() {
        super(new Item.Properties().m_41487_(16).m_41497_(Rarity.EPIC));
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        if (!level.f_46443_) {
            ServerLevel server;
            ItemStack stack = player.m_21120_(hand);
            if (!SummonGate.allow(level, player, "\u9152\u9b54\u00b7\u9152\u6876\u738b")) {
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            Vec3 look = player.m_20154_();
            Vec3 pos = player.m_20182_().m_82520_(look.f_82479_ * 2.5, 0.0, look.f_82481_ * 2.5);
            WineBarrelBossEntity barrel = (WineBarrelBossEntity)((EntityType)FTEntities.WINE_BARREL.get()).m_20615_(level);
            if (barrel == null) {
                player.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u6b64\u5904\u653e\u4e0d\u4e0b\u9152\u6876"), true);
                return InteractionResultHolder.m_19092_((Object)stack, (boolean)false);
            }
            if (!(level instanceof ServerLevel) || !BossCollision.place(server = (ServerLevel)level, (Entity)barrel, new Vec3(pos.f_82479_, player.m_20186_(), pos.f_82481_), BossCollision.SpawnMode.GROUND, player.m_146908_())) {
                BossCollision.tellNoRoom(player, "\u9152\u9b54\u00b7\u9152\u6876\u738b");
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            barrel.m_6593_((Component)Component.m_237115_((String)"boss.ftboss.wine_barrel").m_130940_(ChatFormatting.BOLD));
            barrel.m_20340_(true);
            barrel.m_21153_(barrel.m_21233_());
            barrel.m_21530_();
            if (!level.m_7967_((Entity)barrel)) {
                BossCollision.tellNoRoom(player, "\u9152\u9b54\u00b7\u9152\u6876\u738b");
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            stack.m_41774_(1);
            level.m_6263_(null, pos.f_82479_, pos.f_82480_, pos.f_82481_, SoundEvents.f_11772_, SoundSource.HOSTILE, 1.0f, 0.9f);
        }
        return InteractionResultHolder.m_19092_((Object)player.m_21120_(hand), (boolean)level.f_46443_);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add((Component)Component.m_237113_((String)"\u00a77\u9152\u7cfbBoss\u53ec\u5524\u7269\uff1a\u00a7f\u53f3\u952e\u53ec\u5524 \u9152\u9b54\u00b7\u9152\u6876\u738b"));
        tip.add((Component)Component.m_237113_((String)"\u00a78\u5408\u6210\uff1a8\u79cd\u6700\u9ad8\u4ef7\u9152\u73af\u7ed5 1\u4e2a\u91d1\u5e01\u9505"));
    }
}

