package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponHitPlan"},
    remap = false
)
public abstract class OwnerTimerSemanticConsumerMixin {
    @WrapOperation(
        method = {"applyFinalEffects"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$TypedLeaf;apply(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V"
        )},
        require = 1
    )
    private static void ftboss$ownerSemanticLeaf(@Coerce Object var0, @Coerce Object var1, @Coerce Object var2, Operation<Void> var3) throws Throwable {
        OwnerTimerSemanticAuthority.LeafObservation var4 = null;

        try {
            var4 = FoodWeaponSourceLedger.ownerTimerLeafEntered(var1, var2, var0);
        } catch (Throwable var10) {
        }

        try {
            var3.call(new Object[]{var0, var1, var2});
        } catch (Throwable var9) {
            Throwable var5 = var9;

            try {
                OwnerTimerSemanticAuthority.leave(var4, var5);
            } catch (Throwable var7) {
            }

            throw var9;
        }

        try {
            OwnerTimerSemanticAuthority.leave(var4, null);
        } catch (Throwable var8) {
        }
    }
}
