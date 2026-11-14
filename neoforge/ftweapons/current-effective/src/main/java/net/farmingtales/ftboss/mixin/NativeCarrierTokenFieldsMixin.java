package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeIngressCarrier;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeCarrierToken;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeIngressReceipt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class})
public abstract class NativeCarrierTokenFieldsMixin implements NativeIngressCarrier {
    @Unique
    private NativeCarrierToken ftboss$nativeToken;
    @Unique
    private NativeIngressReceipt ftboss$nativeReceipt;

    @Override
    public NativeCarrierToken ftboss$nativeCarrierToken() {
        return this.ftboss$nativeToken;
    }

    @Override
    public NativeIngressReceipt ftboss$nativeIngressReceipt() {
        return this.ftboss$nativeReceipt;
    }

    @Override
    public void ftboss$bindNativeCarrier(NativeCarrierToken var1) {
        if (this.ftboss$nativeToken != var1) {
            this.ftboss$nativeToken = var1;
            this.ftboss$nativeReceipt = null;
        }
    }

    @Override
    public void ftboss$bindNativeReceipt(NativeIngressReceipt var1) {
        this.ftboss$nativeReceipt = var1;
    }

    @Override
    public void ftboss$clearNativeCarrier() {
        this.ftboss$nativeToken = null;
        this.ftboss$nativeReceipt = null;
    }

    @Inject(
        method = {"remove"},
        at = {@At("RETURN")},
        require = 1
    )
    private void ftboss$nativeRemoved(RemovalReason var1, CallbackInfo var2) {
        this.ftboss$clearNativeCarrier();
    }
}
