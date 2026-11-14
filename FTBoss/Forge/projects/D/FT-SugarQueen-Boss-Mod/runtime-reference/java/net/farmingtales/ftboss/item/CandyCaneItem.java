/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 */
package net.farmingtales.ftboss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

public class CandyCaneItem
extends SwordItem {
    public CandyCaneItem(Item.Properties props) {
        super((Tier)Tiers.DIAMOND, 3, -2.2f, props);
    }
}

