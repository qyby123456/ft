package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 能力实际变更及别名观察；2026-10-25 移除已初始化能力读取的误撤销，惰性初始化仍由实际变更入口覆盖。 */
@Mixin(targets = "net.minecraftforge.common.capabilities.CapabilityProvider", remap = false)
public abstract class OwnerFullRepairCapabilityProviderMixin {
    @Inject(method = {"gatherCapabilities()V", "gatherCapabilities(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)V",
            "gatherCapabilities(Ljava/util/function/Supplier;)V", "doGatherCapabilities(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)V",
            "deserializeCaps(Lnet/minecraft/nbt/CompoundTag;)V", "invalidateCaps()V", "reviveCaps()V"}, at = @At("HEAD"), require = 7)
    private void ftboss$fullRepairVoidMutation(CallbackInfo callback) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        } catch (Throwable rejected) {
        }
    }

    @Inject(method = {"getCapabilities()Lnet/minecraftforge/common/capabilities/CapabilityDispatcher;",
            "serializeCaps()Lnet/minecraft/nbt/CompoundTag;",
            "getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;"},
            at = @At("RETURN"), require = 3)
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> callback) {
        try {
            OwnerFullRepairGuard.alias(this, callback.getReturnValue(), true);
        } catch (Throwable rejected) {
        }
    }
}
