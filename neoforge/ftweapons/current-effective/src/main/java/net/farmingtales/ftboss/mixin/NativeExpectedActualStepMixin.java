package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponHitPlan"},
    remap = false
)
public abstract class NativeExpectedActualStepMixin {
    @Inject(
        method = {"beginOriginalTimer(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private static void ftboss$actualReturnedTimerStep(@Coerce Object var0, Object var1, String var2, String var3, CallbackInfoReturnable<Object> var4) {
        NativeExpectedFieldObservation.actualTimerStep(var0, var1, var4.getReturnValue());
    }
}
