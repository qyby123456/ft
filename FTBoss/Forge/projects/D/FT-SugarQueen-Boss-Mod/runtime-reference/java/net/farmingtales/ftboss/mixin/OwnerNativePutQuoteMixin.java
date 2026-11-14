/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerNativePutQuoteFields;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$OutcomeTagLeaf"}, remap=false)
public abstract class OwnerNativePutQuoteMixin
implements OwnerNativePutQuoteFields {
    @Shadow(remap=false)
    @Final
    private Tag value;
    @Unique
    private OwnerNativeSemanticAuthority.PutQuote ftboss$ownerNativePutQuote;

    @Override
    @Unique
    public final OwnerNativeSemanticAuthority.PutQuote ftboss$ownerNativePutQuote() {
        return this.ftboss$ownerNativePutQuote;
    }

    @Override
    @Unique
    public final Tag ftboss$ownerNativeOriginalValue() {
        return this.value;
    }

    @WrapOperation(method={"<init>"}, at={@At(value="INVOKE", target="Lnet/minecraft/nbt/Tag;copy()Lnet/minecraft/nbt/Tag;", remap=true)}, require=1)
    private Tag ftboss$ownerNativePutQuoted(Tag tag, Operation<Tag> operation, FoodWeaponSourceLedger.AttemptDomain attemptDomain, CompoundTag compoundTag, String string, Tag tag2) {
        try {
            if (tag == tag2) {
                this.ftboss$ownerNativePutQuote = OwnerNativeSemanticAuthority.putQuoted(this, compoundTag, string, tag2);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return (Tag)operation.call(new Object[]{tag});
    }
}

