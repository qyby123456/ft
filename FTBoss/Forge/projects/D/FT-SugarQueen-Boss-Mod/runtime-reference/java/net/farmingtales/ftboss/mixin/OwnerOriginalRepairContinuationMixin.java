/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponHitPlan$RepairLeaf"}, remap=false)
public abstract class OwnerOriginalRepairContinuationMixin {
    @Unique
    private FoodWeaponSourceLedger.OriginalRepairCall ftboss$ownerRepairCall;

    @WrapOperation(method={"apply"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;getDamageValue()I", remap=true)}, require=1)
    private int ftboss$ownerRepairGetter(ItemStack itemStack, Operation<Integer> operation, @Coerce Object object, @Coerce Object object2) throws Throwable {
        int n;
        try {
            this.ftboss$ownerRepairCall = FoodWeaponSourceLedger.ownerOriginalRepairEntered(object, object2, this, itemStack);
        }
        catch (Throwable throwable) {
            try {
                OwnerFullRepairGuard.changed(itemStack, 41);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
        }
        try {
            n = (Integer)operation.call(new Object[]{itemStack});
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.ownerOriginalRepairFailed(this.ftboss$ownerRepairCall, throwable, true);
            }
            catch (Throwable throwable3) {
                // empty catch block
            }
            throw throwable;
        }
        try {
            FoodWeaponSourceLedger.ownerOriginalRepairGetterReturned(this.ftboss$ownerRepairCall, itemStack, n);
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.ownerOriginalRepairFailed(this.ftboss$ownerRepairCall, throwable, false);
            }
            catch (Throwable throwable4) {
                // empty catch block
            }
        }
        return n;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapOperation(method={"apply"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V", remap=true)}, require=1)
    private void ftboss$ownerRepairSetter(ItemStack itemStack, int n, Operation<Void> operation) throws Throwable {
        FoodWeaponSourceLedger.OriginalRepairCall originalRepairCall = this.ftboss$ownerRepairCall;
        try {
            FoodWeaponSourceLedger.ownerOriginalRepairSetterEntered(originalRepairCall, itemStack, n);
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.ownerOriginalRepairFailed(originalRepairCall, throwable, false);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
        }
        try {
            operation.call(new Object[]{itemStack, n});
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.ownerOriginalRepairFailed(originalRepairCall, throwable, true);
            }
            catch (Throwable throwable3) {
                // empty catch block
            }
            if (this.ftboss$ownerRepairCall == originalRepairCall) {
                this.ftboss$ownerRepairCall = null;
            }
            throw throwable;
        }
        try {
            FoodWeaponSourceLedger.ownerOriginalRepairSetterReturned(originalRepairCall, itemStack);
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.ownerOriginalRepairFailed(originalRepairCall, throwable, false);
            }
            catch (Throwable throwable4) {
                // empty catch block
            }
        }
        finally {
            if (this.ftboss$ownerRepairCall == originalRepairCall) {
                this.ftboss$ownerRepairCall = null;
            }
        }
    }
}

