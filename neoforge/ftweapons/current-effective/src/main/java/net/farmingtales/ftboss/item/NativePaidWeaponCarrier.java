package net.farmingtales.ftboss.item;

public interface NativePaidWeaponCarrier {
    void ftboss$bindOwnerPaidWeapon(FoodWeaponSourceLedger.NativePaidWeapon var1);

    FoodWeaponSourceLedger.NativePaidWeapon ftboss$originalOwnerPaidWeapon();

    void ftboss$retireOwnerPaidWeapon();
}
