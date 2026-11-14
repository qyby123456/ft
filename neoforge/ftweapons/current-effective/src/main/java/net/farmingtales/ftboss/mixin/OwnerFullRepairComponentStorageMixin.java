package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PatchedDataComponentMap.class, remap = false)
public abstract class OwnerFullRepairComponentStorageMixin {
    @Inject(method = {"set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", "remove(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"}, at = @At("HEAD"), require = 2, remap = false)
    private void ftboss$fullRepairStorageValue(CallbackInfoReturnable<?> callback) {
        try {
            OwnerFullRepairGuard.changed(this, 21);
        } catch (Throwable failure) {
        }
    }

    @Inject(method = {"applyPatch(Lnet/minecraft/core/component/DataComponentPatch;)V", "applyPatch(Lnet/minecraft/core/component/DataComponentType;Ljava/util/Optional;)V", "restorePatch(Lnet/minecraft/core/component/DataComponentPatch;)V", "setAll(Lnet/minecraft/core/component/DataComponentMap;)V"}, at = @At("HEAD"), require = 4, remap = false)
    private void ftboss$fullRepairStorageMap(CallbackInfo callback) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        } catch (Throwable failure) {
        }
    }
}
