/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 */
package net.farmingtales.ftboss.item;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public enum FTWeaponTier implements Tier
{
    INSTANCE;


    public int m_6609_() {
        return 1500;
    }

    public float m_6624_() {
        return 6.0f;
    }

    public float m_6631_() {
        return 3.0f;
    }

    public int m_6604_() {
        return Tiers.IRON.m_6604_();
    }

    public int m_6601_() {
        return 18;
    }

    public Ingredient m_6282_() {
        return Ingredient.m_43929_((ItemLike[])new ItemLike[]{Items.f_42416_});
    }
}

