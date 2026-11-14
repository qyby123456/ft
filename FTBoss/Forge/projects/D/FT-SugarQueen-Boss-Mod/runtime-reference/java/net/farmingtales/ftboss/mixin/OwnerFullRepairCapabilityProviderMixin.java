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

@Mixin(targets={"net.minecraftforge.common.capabilities.CapabilityProvider"}, remap=false)
public abstract class OwnerFullRepairCapabilityProviderMixin {
    @Inject(method={"gatherCapabilities()V", "gatherCapabilities(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)V", "gatherCapabilities(Ljava/util/function/Supplier;)V", "doGatherCapabilities(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)V", "deserializeCaps(Lnet/minecraft/nbt/CompoundTag;)V", "invalidateCaps()V", "reviveCaps()V"}, at={@At(value="HEAD")}, require=7)
    private void ftboss$fullRepairVoidMutation(CallbackInfo callbackInfo) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"getCapabilities()Lnet/minecraftforge/common/capabilities/CapabilityDispatcher;"}, at={@At(value="HEAD")}, require=1)
    private void ftboss$fullRepairReturnedMutation(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFullRepairGuard.changed(this, 21);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"getCapabilities()Lnet/minecraftforge/common/capabilities/CapabilityDispatcher;", "serializeCaps()Lnet/minecraft/nbt/CompoundTag;", "getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;"}, at={@At(value="RETURN")}, require=3)
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFullRepairGuard.alias(this, callbackInfoReturnable.getReturnValue(), true);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

