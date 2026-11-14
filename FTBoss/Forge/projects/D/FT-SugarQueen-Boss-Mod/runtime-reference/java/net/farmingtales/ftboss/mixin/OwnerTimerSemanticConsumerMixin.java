/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponHitPlan"}, remap=false)
public abstract class OwnerTimerSemanticConsumerMixin {
    @WrapOperation(method={"applyFinalEffects"}, at={@At(value="INVOKE", target="Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$TypedLeaf;apply(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V")}, require=1)
    private static void ftboss$ownerSemanticLeaf(@Coerce Object object, @Coerce Object object2, @Coerce Object object3, Operation<Void> operation) throws Throwable {
        OwnerTimerSemanticAuthority.LeafObservation leafObservation = null;
        try {
            leafObservation = FoodWeaponSourceLedger.ownerTimerLeafEntered(object2, object3, object);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            operation.call(new Object[]{object, object2, object3});
        }
        catch (Throwable throwable) {
            try {
                OwnerTimerSemanticAuthority.leave(leafObservation, throwable);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
            throw throwable;
        }
        try {
            OwnerTimerSemanticAuthority.leave(leafObservation, null);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

