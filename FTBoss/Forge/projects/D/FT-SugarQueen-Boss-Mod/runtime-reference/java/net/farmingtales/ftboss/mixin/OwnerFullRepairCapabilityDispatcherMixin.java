/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.minecraftforge.common.capabilities.CapabilityDispatcher"}, remap=false)
public abstract class OwnerFullRepairCapabilityDispatcherMixin {
    @Inject(method={"deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", "invalidate()V"}, at={@At(value="HEAD")}, require=2)
    private void ftboss$fullRepairVoidMutation(CallbackInfo callbackInfo) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;", "serializeNBT()Lnet/minecraft/nbt/CompoundTag;"}, at={@At(value="RETURN")}, require=2)
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFullRepairGuard.alias(this, callbackInfoReturnable.getReturnValue(), true);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

