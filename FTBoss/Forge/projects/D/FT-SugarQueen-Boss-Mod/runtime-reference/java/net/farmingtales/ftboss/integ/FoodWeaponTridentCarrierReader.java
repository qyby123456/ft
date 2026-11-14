/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.projectile.ThrownTrident
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.integ;

import net.farmingtales.ftboss.item.NativeTridentFieldCarrier;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;

public final class FoodWeaponTridentCarrierReader {
    private FoodWeaponTridentCarrierReader() {
    }

    public static ItemStack readOriginalItem(ThrownTrident thrownTrident) {
        if (!(thrownTrident instanceof NativeTridentFieldCarrier)) {
            throw new IllegalStateException("Original trident field hook not installed");
        }
        NativeTridentFieldCarrier nativeTridentFieldCarrier = (NativeTridentFieldCarrier)thrownTrident;
        return nativeTridentFieldCarrier.ftboss$originalTridentStack();
    }
}

