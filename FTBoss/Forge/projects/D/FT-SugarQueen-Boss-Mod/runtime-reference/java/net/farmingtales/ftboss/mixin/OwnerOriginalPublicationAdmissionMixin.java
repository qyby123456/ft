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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponFinalDamage"}, remap=false)
public abstract class OwnerOriginalPublicationAdmissionMixin {
    @WrapOperation(method={"reserveListedAction"}, at={@At(value="FIELD", opcode=181, target="Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$Publication;writerPublication:Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;")}, require=1)
    private static void ftboss$ownerOriginalPublicationAssigned(@Coerce Object object, @Coerce Object object2, Operation<Void> operation) {
        operation.call(new Object[]{object, object2});
        try {
            FoodWeaponSourceLedger.ownerOriginalPublicationAssigned(object, object2);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

