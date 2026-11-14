package net.farmingtales.ftboss.item;

public interface NativeCapabilityCarrier {
    FoodWeaponSourceLedger.NativeCapabilityToken ftboss$nativeCapabilityToken();

    FoodWeaponSourceLedger.NativeIngressReceipt ftboss$nativeCapabilityReceipt();

    void ftboss$bindNativeCapability(FoodWeaponSourceLedger.NativeCapabilityToken var1);

    void ftboss$bindNativeCapabilityReceipt(FoodWeaponSourceLedger.NativeIngressReceipt var1);

    void ftboss$clearNativeCapability();
}
