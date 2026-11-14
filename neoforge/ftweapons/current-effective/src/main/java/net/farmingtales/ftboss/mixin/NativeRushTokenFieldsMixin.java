package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeCapabilityCarrier;
import net.farmingtales.ftboss.item.NativeIngressExecution;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeCapabilityToken;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeIngressReceipt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
    targets = {"com.github.L_Ender.cataclysm.capabilities.RenderRushCapability$RenderRushCapabilityImp"},
    remap = false
)
public abstract class NativeRushTokenFieldsMixin implements NativeCapabilityCarrier {
    @Unique
    private NativeCapabilityToken ftboss$v9$capability;
    @Unique
    private NativeIngressReceipt ftboss$v9$receipt;

    @Override
    public NativeCapabilityToken ftboss$nativeCapabilityToken() {
        return this.ftboss$v9$capability;
    }

    @Override
    public NativeIngressReceipt ftboss$nativeCapabilityReceipt() {
        return this.ftboss$v9$receipt;
    }

    @Override
    public void ftboss$bindNativeCapability(NativeCapabilityToken var1) {
        if (var1 != this.ftboss$v9$capability) {
            this.ftboss$v9$capability = var1;
            this.ftboss$v9$receipt = null;
        }
    }

    @Override
    public void ftboss$bindNativeCapabilityReceipt(NativeIngressReceipt var1) {
        this.ftboss$v9$receipt = var1;
    }

    @Override
    public void ftboss$clearNativeCapability() {
        this.ftboss$v9$capability = null;
        this.ftboss$v9$receipt = null;
    }

    @Inject(
        method = {"setRush(Z)V"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$rushReturn(boolean var1, CallbackInfo var2) {
        if (!var1) {
            FoodWeaponSourceLedger.cancelNativeCapability(this.ftboss$v9$capability);
            this.ftboss$clearNativeCapability();
        } else {
            NativeIngressExecution.capability(this, FoodWeaponSourceLedger.nativeIngressRushReturned(this));
        }
    }
}
