/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;

public interface NativeIngressCarrier {
    public FoodWeaponSourceLedger.NativeCarrierToken ftboss$nativeCarrierToken();

    public FoodWeaponSourceLedger.NativeIngressReceipt ftboss$nativeIngressReceipt();

    public void ftboss$bindNativeCarrier(FoodWeaponSourceLedger.NativeCarrierToken var1);

    public void ftboss$bindNativeReceipt(FoodWeaponSourceLedger.NativeIngressReceipt var1);

    public void ftboss$clearNativeCarrier();
}

