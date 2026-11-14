/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponHitPlan"}, remap=false)
public abstract class NativeExpectedActualStepMixin {
    @Inject(method={"beginOriginalTimer(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;"}, at={@At(value="RETURN")}, require=1, remap=false)
    private static void ftboss$actualReturnedTimerStep(@Coerce Object object, Object object2, String string, String string2, CallbackInfoReturnable<Object> callbackInfoReturnable) {
        NativeExpectedFieldObservation.actualTimerStep(object, object2, callbackInfoReturnable.getReturnValue());
    }
}

