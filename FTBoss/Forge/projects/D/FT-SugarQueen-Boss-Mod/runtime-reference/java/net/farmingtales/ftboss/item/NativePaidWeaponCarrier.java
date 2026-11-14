/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;

public interface NativePaidWeaponCarrier {
    public void ftboss$bindOwnerPaidWeapon(FoodWeaponSourceLedger.NativePaidWeapon var1);

    public FoodWeaponSourceLedger.NativePaidWeapon ftboss$originalOwnerPaidWeapon();

    public void ftboss$retireOwnerPaidWeapon();
}

