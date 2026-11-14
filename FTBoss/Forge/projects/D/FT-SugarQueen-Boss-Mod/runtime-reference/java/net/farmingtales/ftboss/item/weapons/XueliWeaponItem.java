/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.UseAnim
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import java.util.function.Consumer;
import net.farmingtales.ftboss.client.render.XueliWeaponRenderer;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.item.weapons.XueliWeaponCombat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class XueliWeaponItem
extends SwordItem {
    private final boolean skillet;

    public XueliWeaponItem(boolean skillet) {
        super((Tier)FTWeaponTier.INSTANCE, skillet ? 8 : 10, skillet ? -2.5f : -2.9f, new Item.Properties().m_41497_(Rarity.EPIC).m_41486_());
        this.skillet = skillet;
    }

    public boolean isSkillet() {
        return this.skillet;
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.m_19098_((Object)stack);
        }
        if (!player.m_6084_() || player.m_5833_() || player.m_36335_().m_41519_((Item)this)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (this.skillet && !player.m_6144_()) {
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                XueliWeaponCombat.sweep(serverPlayer, stack);
            }
        } else {
            player.m_6672_(hand);
            if (this.skillet && player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                serverPlayer.m_36335_().m_41524_((Item)this, 100);
                XueliWeaponCombat.guardEffect(serverPlayer);
            }
        }
        return InteractionResultHolder.m_19096_((Object)stack);
    }

    public int m_8105_(ItemStack stack) {
        return this.skillet ? 20 : 72000;
    }

    public UseAnim m_6164_(ItemStack stack) {
        return this.skillet ? UseAnim.SPEAR : UseAnim.BOW;
    }

    public void m_5929_(Level level, LivingEntity user, ItemStack stack, int remainingTicks) {
        if (!this.skillet && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)user;
            int elapsed = this.m_8105_(stack) - remainingTicks;
            if (elapsed == 8 || elapsed == 24) {
                XueliWeaponCombat.chargeEffect(player, elapsed == 24);
            }
        }
    }

    public void m_5551_(ItemStack stack, Level level, LivingEntity user, int remainingTicks) {
        if (this.skillet || !(user instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)user;
        int charge = this.m_8105_(stack) - remainingTicks;
        if (charge < 8 || player.m_21205_() != stack || !player.m_6084_() || player.m_5833_() || player.m_36335_().m_41519_((Item)this)) {
            return;
        }
        XueliWeaponCombat.roll(player, stack, Math.min(charge, 24));
    }

    public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.m_7579_(stack, target, attacker);
        if (result && this.skillet && attacker instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)attacker;
            if (target.m_6084_() && XueliWeaponCombat.canTarget(player, target)) {
                XueliWeaponCombat.mark(player, target);
            }
        }
        return result;
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.m_7373_(stack, level, tooltip, flag);
        String prefix = this.skillet ? "tooltip.ftboss.xueli_skillet." : "tooltip.ftboss.xueli_rolling_pin.";
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.xueli.origin").m_130940_(ChatFormatting.LIGHT_PURPLE));
        tooltip.add((Component)Component.m_237115_((String)(prefix + "skill")).m_130940_(ChatFormatting.GOLD));
        tooltip.add((Component)Component.m_237115_((String)(prefix + "extra")).m_130940_(ChatFormatting.LIGHT_PURPLE));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.xueli.rules").m_130940_(ChatFormatting.GRAY));
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private BlockEntityWithoutLevelRenderer renderer;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new XueliWeaponRenderer();
                }
                return this.renderer;
            }
        });
    }
}

