/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 */
package net.farmingtales.ftboss.util;

import net.minecraft.nbt.CompoundTag;

public final class FoodWeaponBondFingerprint {
    private FoodWeaponBondFingerprint() {
    }

    public static CompoundTag wrap(String string) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128359_("SocketFingerprint", string);
        return compoundTag;
    }
}

