/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 */
package net.farmingtales.ftboss.mixin;

import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativePaidCarrierState;
import net.farmingtales.ftboss.item.NativePaidWeaponCarrier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets={"net.minecraft.world.entity.projectile.Projectile"}, remap=false)
public abstract class NativePaidWeaponCarrierMixin
implements NativePaidWeaponCarrier {
    @Shadow(remap=false)
    private UUID f_37244_;
    @Unique
    private final NativePaidCarrierState<FoodWeaponSourceLedger.NativePaidWeapon> ftboss$paidWeapon = new NativePaidCarrierState();

    @Override
    public void ftboss$bindOwnerPaidWeapon(FoodWeaponSourceLedger.NativePaidWeapon nativePaidWeapon) {
        this.ftboss$paidWeapon.bind(nativePaidWeapon, this.f_37244_);
    }

    @Override
    public FoodWeaponSourceLedger.NativePaidWeapon ftboss$originalOwnerPaidWeapon() {
        return this.ftboss$paidWeapon.original(this.f_37244_);
    }

    @Override
    public void ftboss$retireOwnerPaidWeapon() {
        this.ftboss$paidWeapon.retire();
    }
}

