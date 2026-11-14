package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponFinalDamage"},
    remap = false
)
public abstract class OwnerOriginalPublicationAdmissionMixin {
    @WrapOperation(
        method = {"reserveListedAction"},
        at = {@At(
            value = "FIELD",
            opcode = 181,
            target = "Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$Publication;writerPublication:Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;"
        )},
        require = 1
    )
    private static void ftboss$ownerOriginalPublicationAssigned(@Coerce Object var0, @Coerce Object var1, Operation<Void> var2) {
        var2.call(new Object[]{var0, var1});

        try {
            FoodWeaponSourceLedger.ownerOriginalPublicationAssigned(var0, var1);
        } catch (Throwable var4) {
        }
    }
}
