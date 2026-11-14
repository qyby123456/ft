/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeCapabilityCarrier;
import net.farmingtales.ftboss.item.NativeIngressExecution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"com.github.L_Ender.cataclysm.capabilities.RenderRushCapability$RenderRushCapabilityImp"}, remap=false)
public abstract class NativeRushTokenFieldsMixin
implements NativeCapabilityCarrier {
    @Unique
    private FoodWeaponSourceLedger.NativeCapabilityToken ftboss$v9$capability;
    @Unique
    private FoodWeaponSourceLedger.NativeIngressReceipt ftboss$v9$receipt;

    @Override
    public FoodWeaponSourceLedger.NativeCapabilityToken ftboss$nativeCapabilityToken() {
        return this.ftboss$v9$capability;
    }

    @Override
    public FoodWeaponSourceLedger.NativeIngressReceipt ftboss$nativeCapabilityReceipt() {
        return this.ftboss$v9$receipt;
    }

    @Override
    public void ftboss$bindNativeCapability(FoodWeaponSourceLedger.NativeCapabilityToken nativeCapabilityToken) {
        if (nativeCapabilityToken != this.ftboss$v9$capability) {
            this.ftboss$v9$capability = nativeCapabilityToken;
            this.ftboss$v9$receipt = null;
        }
    }

    @Override
    public void ftboss$bindNativeCapabilityReceipt(FoodWeaponSourceLedger.NativeIngressReceipt nativeIngressReceipt) {
        this.ftboss$v9$receipt = nativeIngressReceipt;
    }

    @Override
    public void ftboss$clearNativeCapability() {
        this.ftboss$v9$capability = null;
        this.ftboss$v9$receipt = null;
    }

    @Inject(method={"setRush(Z)V"}, at={@At(value="RETURN")}, require=1, remap=false)
    private void ftboss$v9$rushReturn(boolean bl, CallbackInfo callbackInfo) {
        if (!bl) {
            FoodWeaponSourceLedger.cancelNativeCapability(this.ftboss$v9$capability);
            this.ftboss$clearNativeCapability();
        } else {
            NativeIngressExecution.capability(this, FoodWeaponSourceLedger.nativeIngressRushReturned(this));
        }
    }
}

