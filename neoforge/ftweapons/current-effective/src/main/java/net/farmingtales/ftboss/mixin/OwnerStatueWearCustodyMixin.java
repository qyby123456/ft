package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerStatueWearAdmission;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {NonNullList.class},
    remap = true
)
public abstract class OwnerStatueWearCustodyMixin {
    @Inject(
        method = {"set(ILjava/lang/Object;)Ljava/lang/Object;", "remove(I)Ljava/lang/Object;"},
        at = {@At("HEAD")},
        require = 2
    )
    private void ftboss$statueCustodyReturned(CallbackInfoReturnable<?> var1) {
        try {
            OwnerStatueWearAdmission.custodyChanged(this);
        } catch (Throwable var3) {
        }
    }

    @Inject(
        method = {"add(ILjava/lang/Object;)V", "clear()V"},
        at = {@At("HEAD")},
        require = 2
    )
    private void ftboss$statueCustodyVoid(CallbackInfo var1) {
        try {
            OwnerStatueWearAdmission.custodyChanged(this);
        } catch (Throwable var3) {
        }
    }
}
