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

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$OutcomeTagLeaf"},
    remap = false
)
public abstract class OwnerNativePutQuoteMixin implements OwnerNativePutQuoteFields {
    @Shadow(
        remap = false
    )
    @Final
    private Tag value;
    @Unique
    private OwnerNativeSemanticAuthority.PutQuote ftboss$ownerNativePutQuote;

    @Unique
    @Override
    public final OwnerNativeSemanticAuthority.PutQuote ftboss$ownerNativePutQuote() {
        return this.ftboss$ownerNativePutQuote;
    }

    @Unique
    @Override
    public final Tag ftboss$ownerNativeOriginalValue() {
        return this.value;
    }

    @WrapOperation(
        method = {"<init>"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/Tag;copy()Lnet/minecraft/nbt/Tag;",
            remap = true
        )},
        require = 1
    )
    private Tag ftboss$ownerNativePutQuoted(Tag var1, Operation<Tag> var2, FoodWeaponSourceLedger.AttemptDomain var3, CompoundTag var4, String var5, Tag var6) {
        try {
            if (var1 == var6) {
                this.ftboss$ownerNativePutQuote = OwnerNativeSemanticAuthority.putQuoted(this, var4, var5, var6);
            }
        } catch (Throwable var8) {
        }

        return (Tag)var2.call(new Object[]{var1});
    }
}
