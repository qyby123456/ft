package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = {"net.minecraftforge.common.capabilities.CapabilityProvider"},
    remap = false
)
public abstract class OwnerFullRepairCapabilityProviderMixin {
    @Inject(
        method = {"gatherCapabilities()V", "gatherCapabilities(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)V", "gatherCapabilities(Ljava/util/function/Supplier;)V", "doGatherCapabilities(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)V", "deserializeCaps(Lnet/minecraft/nbt/CompoundTag;)V", "invalidateCaps()V", "reviveCaps()V"},
        at = {@At("HEAD")},
        require = 7
    )
    private void ftboss$fullRepairVoidMutation(CallbackInfo var1) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        } catch (Throwable var3) {
        }
    }

    @Inject(
        method = {"getCapabilities()Lnet/minecraftforge/common/capabilities/CapabilityDispatcher;"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$fullRepairReturnedMutation(CallbackInfoReturnable<?> var1) {
        try {
            OwnerFullRepairGuard.changed(this, 21);
        } catch (Throwable var3) {
        }
    }

    @Inject(
        method = {"getCapabilities()Lnet/minecraftforge/common/capabilities/CapabilityDispatcher;", "serializeCaps()Lnet/minecraft/nbt/CompoundTag;", "getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;"},
        at = {@At("RETURN")},
        require = 3
    )
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> var1) {
        try {
            OwnerFullRepairGuard.alias(this, var1.getReturnValue(), true);
        } catch (Throwable var3) {
        }
    }
}
