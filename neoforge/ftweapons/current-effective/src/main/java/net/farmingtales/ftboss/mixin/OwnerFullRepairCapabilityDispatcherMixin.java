package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = {"net.minecraftforge.common.capabilities.CapabilityDispatcher"},
    remap = false
)
public abstract class OwnerFullRepairCapabilityDispatcherMixin {
    @Inject(
        method = {"deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", "invalidate()V"},
        at = {@At("HEAD")},
        require = 2
    )
    private void ftboss$fullRepairVoidMutation(CallbackInfo var1) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        } catch (Throwable var3) {
        }
    }

    @Inject(
        method = {"getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;", "serializeNBT()Lnet/minecraft/nbt/CompoundTag;"},
        at = {@At("RETURN")},
        require = 2
    )
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> var1) {
        try {
            OwnerFullRepairGuard.alias(this, var1.getReturnValue(), true);
        } catch (Throwable var3) {
        }
    }
}
