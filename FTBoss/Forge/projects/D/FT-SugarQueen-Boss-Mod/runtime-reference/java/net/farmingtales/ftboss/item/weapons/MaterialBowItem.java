/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import net.farmingtales.ftboss.item.MaterialBowItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class MaterialBowItem
extends BowItem {
    private final MaterialBowItems.Kind kind;

    public MaterialBowItem(MaterialBowItems.Kind kind) {
        super(MaterialBowItem.properties(kind));
        this.kind = kind;
    }

    private static Item.Properties properties(MaterialBowItems.Kind kind) {
        Item.Properties properties = new Item.Properties().m_41503_(kind.tier.m_6609_());
        return kind.tier == Tiers.NETHERITE ? properties.m_41486_() : properties;
    }

    public MaterialBowItems.Kind kind() {
        return this.kind;
    }

    public int m_6473_() {
        return this.kind.tier.m_6601_();
    }

    public boolean m_6832_(ItemStack itemStack, ItemStack itemStack2) {
        return this.kind.tier.m_6282_().test(itemStack2) || super.m_6832_(itemStack, itemStack2);
    }

    public void m_7373_(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        super.m_7373_(itemStack, level, list, tooltipFlag);
        list.add((Component)Component.m_237115_((String)"tooltip.ftboss.material_bow.components").m_130940_(ChatFormatting.DARK_AQUA));
    }
}

