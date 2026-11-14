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

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponHitPlan$RepairLeaf"},
    remap = false
)
public abstract class OwnerOriginalRepairContinuationMixin {
    @Unique
    private FoodWeaponSourceLedger.OriginalRepairCall ftboss$ownerRepairCall;

    @WrapOperation(
        method = {"apply"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getDamageValue()I",
            remap = true
        )},
        require = 1
    )
    private int ftboss$ownerRepairGetter(ItemStack var1, Operation<Integer> var2, @Coerce Object var3, @Coerce Object var4) throws Throwable {
        try {
            this.ftboss$ownerRepairCall = FoodWeaponSourceLedger.ownerOriginalRepairEntered(var3, var4, this, var1);
        } catch (Throwable var13) {
            try {
                OwnerFullRepairGuard.changed(var1, 41);
            } catch (Throwable var12) {
            }
        }

        int var5;
        try {
            var5 = (Integer)var2.call(new Object[]{var1});
        } catch (Throwable var11) {
            Throwable var6 = var11;

            try {
                FoodWeaponSourceLedger.ownerOriginalRepairFailed(this.ftboss$ownerRepairCall, var6, true);
            } catch (Throwable var8) {
            }

            throw var11;
        }

        try {
            FoodWeaponSourceLedger.ownerOriginalRepairGetterReturned(this.ftboss$ownerRepairCall, var1, var5);
        } catch (Throwable var10) {
            Throwable var14 = var10;

            try {
                FoodWeaponSourceLedger.ownerOriginalRepairFailed(this.ftboss$ownerRepairCall, var14, false);
            } catch (Throwable var9) {
            }
        }

        return var5;
    }

    @WrapOperation(
        method = {"apply"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V",
            remap = true
        )},
        require = 1
    )
    private void ftboss$ownerRepairSetter(ItemStack var1, int var2, Operation<Void> var3) throws Throwable {
        FoodWeaponSourceLedger.OriginalRepairCall var4 = this.ftboss$ownerRepairCall;

        try {
            FoodWeaponSourceLedger.ownerOriginalRepairSetterEntered(var4, var1, var2);
        } catch (Throwable var20) {
            Throwable var5 = var20;

            try {
                FoodWeaponSourceLedger.ownerOriginalRepairFailed(var4, var5, false);
            } catch (Throwable var19) {
            }
        }

        try {
            var3.call(new Object[]{var1, var2});
        } catch (Throwable var18) {
            Throwable var22 = var18;

            try {
                FoodWeaponSourceLedger.ownerOriginalRepairFailed(var4, var22, true);
            } catch (Throwable var15) {
            }

            if (this.ftboss$ownerRepairCall == var4) {
                this.ftboss$ownerRepairCall = null;
            }

            throw var18;
        }

        try {
            FoodWeaponSourceLedger.ownerOriginalRepairSetterReturned(var4, var1);
        } catch (Throwable var17) {
            Throwable var23 = var17;

            try {
                FoodWeaponSourceLedger.ownerOriginalRepairFailed(var4, var23, false);
            } catch (Throwable var16) {
            }
        } finally {
            if (this.ftboss$ownerRepairCall == var4) {
                this.ftboss$ownerRepairCall = null;
            }
        }
    }
}
