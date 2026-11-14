package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CustomData.class, remap = false)
public abstract class OwnerFullRepairCustomDataMixin {
    @Inject(method = "getUnsafe()Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"), require = 1, remap = false)
    private void ftboss$fullRepairCustomDataAlias(CallbackInfoReturnable<?> callback) {
        try {
            OwnerFullRepairGuard.alias(this, callback.getReturnValue(), true);
        } catch (Throwable failure) {
        }
    }
}
