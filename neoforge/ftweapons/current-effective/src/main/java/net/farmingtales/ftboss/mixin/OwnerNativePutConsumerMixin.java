package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$OutcomeTagLeaf"},
    remap = false
)
public abstract class OwnerNativePutConsumerMixin {
    @Unique
    private OwnerNativeSemanticAuthority.Admission ftboss$ownerNativeAdmission;

    @WrapOperation(
        method = {"apply"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/Tag;copy()Lnet/minecraft/nbt/Tag;",
            remap = true
        )},
        require = 1
    )
    private Tag ftboss$ownerNativeCopy(Tag var1, Operation<Tag> var2, @Coerce Object var3, @Coerce Object var4) throws Throwable {
        OwnerNativeSemanticAuthority.Admission var5 = null;

        try {
            var5 = FoodWeaponSourceLedger.ownerNativePutEntered(var3, var4, this, var1);
        } catch (Throwable var14) {
        }

        this.ftboss$ownerNativeAdmission = var5;

        Tag var6;
        try {
            var6 = (Tag)var2.call(new Object[]{var1});
        } catch (Throwable var13) {
            Throwable var7 = var13;

            try {
                OwnerNativeSemanticAuthority.failed(var5, var7, true);
            } catch (Throwable var10) {
            }

            try {
                OwnerNativeSemanticAuthority.close(var5);
            } catch (Throwable var9) {
            }

            if (this.ftboss$ownerNativeAdmission == var5) {
                this.ftboss$ownerNativeAdmission = null;
            }

            throw var13;
        }

        try {
            OwnerNativeSemanticAuthority.copied(var5, var1, var6);
        } catch (Throwable var12) {
            Throwable var15 = var12;

            try {
                OwnerNativeSemanticAuthority.failed(var5, var15, false);
            } catch (Throwable var11) {
            }
        }

        return var6;
    }

    @WrapOperation(
        method = {"apply"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;",
            remap = true
        )},
        require = 1
    )
    private Tag ftboss$ownerNativePut(CompoundTag var1, String var2, Tag var3, Operation<Tag> var4) throws Throwable {
        OwnerNativeSemanticAuthority.Admission var5 = this.ftboss$ownerNativeAdmission;

        try {
            OwnerNativeSemanticAuthority.entering(var5, var1, var2, var3);
        } catch (Throwable var27) {
            Throwable var6 = var27;

            try {
                OwnerNativeSemanticAuthority.failed(var5, var6, false);
            } catch (Throwable var26) {
            }
        }

        Tag var30;
        try {
            Tag var28;
            try {
                var28 = (Tag)var4.call(new Object[]{var1, var2, var3});
            } catch (Throwable var24) {
                Throwable var7 = var24;

                try {
                    OwnerNativeSemanticAuthority.failed(var5, var7, true);
                } catch (Throwable var21) {
                }

                throw var24;
            }

            try {
                OwnerNativeSemanticAuthority.returned(var5, var1, var2, var3, var28);
            } catch (Throwable var23) {
                Throwable var29 = var23;

                try {
                    OwnerNativeSemanticAuthority.failed(var5, var29, false);
                } catch (Throwable var22) {
                }
            }

            var30 = var28;
        } finally {
            try {
                OwnerNativeSemanticAuthority.close(var5);
            } catch (Throwable var20) {
            }

            if (this.ftboss$ownerNativeAdmission == var5) {
                this.ftboss$ownerNativeAdmission = null;
            }
        }

        return var30;
    }
}
