/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
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
import net.farmingtales.ftboss.boss.BossChallengePreference;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.util.BossCollision;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TeaSeedItem
extends Item {
    public TeaSeedItem() {
        super(new Item.Properties().m_41487_(16).m_41497_(Rarity.EPIC));
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        if (!level.f_46443_) {
            ServerLevel server;
            boolean finale;
            ServerPlayer sp;
            ItemStack stack = player.m_21120_(hand);
            if (!SummonGate.allow(level, player, "\u6c99\u8036")) {
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            ServerPlayer serverPlayer = player instanceof ServerPlayer ? (sp = (ServerPlayer)player) : null;
            boolean bl = finale = player.m_6144_() || serverPlayer != null && BossChallengePreference.selected((Player)serverPlayer) == ManorDifficulty.IMPOSSIBLE;
            if (finale && (serverPlayer == null || !ManorDifficulty.unlocked(serverPlayer))) {
                player.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u7ec8\u7ed3\u96be\u5ea6\u5c1a\u672a\u89e3\u9501\uff1a\u8bf7\u5148\u5b8c\u6574\u901a\u5173\u666e\u901a\u5e84\u56ed\u6311\u6218\u3002"), true);
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            Vec3 look = player.m_20154_();
            Vec3 pos = player.m_20182_().m_82520_(look.f_82479_ * 3.0, 0.0, look.f_82481_ * 3.0);
            TeaTreeBossEntity tree = (TeaTreeBossEntity)((EntityType)FTEntities.TEA_TREE.get()).m_20615_(level);
            if (tree == null) {
                player.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u6b64\u5904\u65e0\u6cd5\u957f\u51fa\u8336\u6811"), true);
                return InteractionResultHolder.m_19092_((Object)stack, (boolean)false);
            }
            if (!(level instanceof ServerLevel) || !BossCollision.place(server = (ServerLevel)level, (Entity)tree, new Vec3(pos.f_82479_, player.m_20186_(), pos.f_82481_), BossCollision.SpawnMode.GROUND, player.m_146908_())) {
                BossCollision.tellNoRoom(player, finale ? "\u7ec8\u7ed3\u96be\u5ea6\u6c99\u8036" : "\u6c99\u8036");
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            NonManorFinaleDifficulty.prepare((LivingEntity)tree, finale);
            tree.m_6593_((Component)Component.m_237115_((String)"boss.ftboss.saya").m_130940_(ChatFormatting.BOLD).m_7220_((Component)(finale ? Component.m_237113_((String)" \u00a74[\u7ec8\u7ed3]") : Component.m_237119_())));
            tree.m_20340_(true);
            tree.m_21153_(tree.m_21233_());
            tree.m_21530_();
            if (!level.m_7967_((Entity)tree)) {
                BossCollision.tellNoRoom(player, finale ? "\u7ec8\u7ed3\u96be\u5ea6\u6c99\u8036" : "\u6c99\u8036");
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            stack.m_41774_(1);
            level.m_6263_(null, pos.f_82479_, pos.f_82480_, pos.f_82481_, SoundEvents.f_11839_, SoundSource.HOSTILE, 1.0f, 0.8f);
        }
        return InteractionResultHolder.m_19092_((Object)player.m_21120_(hand), (boolean)level.f_46443_);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add((Component)Component.m_237113_((String)"\u00a77\u8336\u7cfbBoss\u53ec\u5524\u7269\uff1a\u00a7f\u53f3\u952e\u957f\u51fa \u6c99\u8036"));
        tip.add((Component)Component.m_237113_((String)"\u00a74\u77f3\u50cf\u9884\u8bbe\u4e3a\u7ec8\u7ed3\u96be\u5ea6\uff0c\u6216\u6f5c\u884c\u53f3\u952e\uff1a\u53ec\u5524\u7ec8\u7ed3\u96be\u5ea6\u6c99\u8036"));
        tip.add((Component)Component.m_237113_((String)"\u00a78\u7ec8\u7ed3\u96be\u5ea6\u9996\u6b21\u6218\u5229\u54c1\u5305\u542b\u9970\u54c1\u3010\u94f6\u4e4b\u94a5\u3011"));
        tip.add((Component)Component.m_237113_((String)"\u00a78\u5408\u6210\uff1a8\u79cd\u6700\u9ad8\u4ef7\u8336\u73af\u7ed5 1\u4e2a\u91d1\u5e01\u9505"));
    }
}

