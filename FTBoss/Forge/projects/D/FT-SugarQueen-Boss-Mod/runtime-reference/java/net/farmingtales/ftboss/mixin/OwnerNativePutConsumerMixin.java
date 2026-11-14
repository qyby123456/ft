/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 */
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

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$OutcomeTagLeaf"}, remap=false)
public abstract class OwnerNativePutConsumerMixin {
    @Unique
    private OwnerNativeSemanticAuthority.Admission ftboss$ownerNativeAdmission;

    @WrapOperation(method={"apply"}, at={@At(value="INVOKE", target="Lnet/minecraft/nbt/Tag;copy()Lnet/minecraft/nbt/Tag;", remap=true)}, require=1)
    private Tag ftboss$ownerNativeCopy(Tag tag, Operation<Tag> operation, @Coerce Object object, @Coerce Object object2) throws Throwable {
        Tag tag2;
        OwnerNativeSemanticAuthority.Admission admission = null;
        try {
            admission = FoodWeaponSourceLedger.ownerNativePutEntered(object, object2, this, tag);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.ftboss$ownerNativeAdmission = admission;
        try {
            tag2 = (Tag)operation.call(new Object[]{tag});
        }
        catch (Throwable throwable) {
            try {
                OwnerNativeSemanticAuthority.failed(admission, throwable, true);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
            try {
                OwnerNativeSemanticAuthority.close(admission);
            }
            catch (Throwable throwable3) {
                // empty catch block
            }
            if (this.ftboss$ownerNativeAdmission == admission) {
                this.ftboss$ownerNativeAdmission = null;
            }
            throw throwable;
        }
        try {
            OwnerNativeSemanticAuthority.copied(admission, tag, tag2);
        }
        catch (Throwable throwable) {
            try {
                OwnerNativeSemanticAuthority.failed(admission, throwable, false);
            }
            catch (Throwable throwable4) {
                // empty catch block
            }
        }
        return tag2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapOperation(method={"apply"}, at={@At(value="INVOKE", target="Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", remap=true)}, require=1)
    private Tag ftboss$ownerNativePut(CompoundTag compoundTag, String string, Tag tag, Operation<Tag> operation) throws Throwable {
        OwnerNativeSemanticAuthority.Admission admission = this.ftboss$ownerNativeAdmission;
        try {
            OwnerNativeSemanticAuthority.entering(admission, compoundTag, string, tag);
        }
        catch (Throwable throwable) {
            try {
                OwnerNativeSemanticAuthority.failed(admission, throwable, false);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
        }
        try {
            Tag tag2;
            try {
                tag2 = (Tag)operation.call(new Object[]{compoundTag, string, tag});
            }
            catch (Throwable throwable) {
                try {
                    OwnerNativeSemanticAuthority.failed(admission, throwable, true);
                }
                catch (Throwable throwable3) {
                    // empty catch block
                }
                throw throwable;
            }
            try {
                OwnerNativeSemanticAuthority.returned(admission, compoundTag, string, tag, tag2);
            }
            catch (Throwable throwable) {
                try {
                    OwnerNativeSemanticAuthority.failed(admission, throwable, false);
                }
                catch (Throwable throwable4) {
                    // empty catch block
                }
            }
            Tag tag3 = tag2;
            return tag3;
        }
        finally {
            try {
                OwnerNativeSemanticAuthority.close(admission);
            }
            catch (Throwable throwable) {}
            if (this.ftboss$ownerNativeAdmission == admission) {
                this.ftboss$ownerNativeAdmission = null;
            }
        }
    }
}

