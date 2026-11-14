/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;

public interface NativeCapabilityCarrier {
    public FoodWeaponSourceLedger.NativeCapabilityToken ftboss$nativeCapabilityToken();

    public FoodWeaponSourceLedger.NativeIngressReceipt ftboss$nativeCapabilityReceipt();

    public void ftboss$bindNativeCapability(FoodWeaponSourceLedger.NativeCapabilityToken var1);

    public void ftboss$bindNativeCapabilityReceipt(FoodWeaponSourceLedger.NativeIngressReceipt var1);

    public void ftboss$clearNativeCapability();
}

