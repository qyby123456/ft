/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Tier
 */
package net.farmingtales.ftboss.item.weapons;

import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

public final class MaterialCrossbowItem
extends CrossbowItem {
    private final Tier tier;

    public MaterialCrossbowItem(Tier tier, Item.Properties properties) {
        super(properties.m_41503_(tier.m_6609_()));
        this.tier = tier;
    }

    public int m_6473_() {
        return this.tier.m_6601_();
    }

    public boolean m_6832_(ItemStack itemStack, ItemStack itemStack2) {
        return this.tier.m_6282_().test(itemStack2) || super.m_6832_(itemStack, itemStack2);
    }
}

