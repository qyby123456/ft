/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeIngressCarrier;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Entity.class})
public abstract class NativeCarrierTokenFieldsMixin
implements NativeIngressCarrier {
    @Unique
    private FoodWeaponSourceLedger.NativeCarrierToken ftboss$nativeToken;
    @Unique
    private FoodWeaponSourceLedger.NativeIngressReceipt ftboss$nativeReceipt;

    @Override
    public FoodWeaponSourceLedger.NativeCarrierToken ftboss$nativeCarrierToken() {
        return this.ftboss$nativeToken;
    }

    @Override
    public FoodWeaponSourceLedger.NativeIngressReceipt ftboss$nativeIngressReceipt() {
        return this.ftboss$nativeReceipt;
    }

    @Override
    public void ftboss$bindNativeCarrier(FoodWeaponSourceLedger.NativeCarrierToken nativeCarrierToken) {
        if (this.ftboss$nativeToken != nativeCarrierToken) {
            this.ftboss$nativeToken = nativeCarrierToken;
            this.ftboss$nativeReceipt = null;
        }
    }

    @Override
    public void ftboss$bindNativeReceipt(FoodWeaponSourceLedger.NativeIngressReceipt nativeIngressReceipt) {
        this.ftboss$nativeReceipt = nativeIngressReceipt;
    }

    @Override
    public void ftboss$clearNativeCarrier() {
        this.ftboss$nativeToken = null;
        this.ftboss$nativeReceipt = null;
    }

    @Inject(method={"remove"}, at={@At(value="RETURN")}, require=1)
    private void ftboss$nativeRemoved(Entity.RemovalReason removalReason, CallbackInfo callbackInfo) {
        this.ftboss$clearNativeCarrier();
    }
}

