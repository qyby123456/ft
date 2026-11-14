/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class WineBottleMaceItem
extends SwordItem {
    public WineBottleMaceItem() {
        super((Tier)FTWeaponTier.INSTANCE, 5, -3.2f, new Item.Properties().m_41497_(Rarity.RARE));
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.wine_mace.pressure").m_130940_(ChatFormatting.GOLD));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.wine_mace.damage").m_130940_(ChatFormatting.GRAY));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.wine_mace.impact").m_130940_(ChatFormatting.GRAY));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.wine_mace.rules").m_130940_(ChatFormatting.GRAY));
    }
}

