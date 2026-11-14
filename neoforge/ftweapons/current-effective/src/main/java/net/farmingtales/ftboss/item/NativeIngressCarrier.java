package net.farmingtales.ftboss.item;

public interface NativeIngressCarrier {
    FoodWeaponSourceLedger.NativeCarrierToken ftboss$nativeCarrierToken();

    FoodWeaponSourceLedger.NativeIngressReceipt ftboss$nativeIngressReceipt();

    void ftboss$bindNativeCarrier(FoodWeaponSourceLedger.NativeCarrierToken var1);

    void ftboss$bindNativeReceipt(FoodWeaponSourceLedger.NativeIngressReceipt var1);

    void ftboss$clearNativeCarrier();
}
