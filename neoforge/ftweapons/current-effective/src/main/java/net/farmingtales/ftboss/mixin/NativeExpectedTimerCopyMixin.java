package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponEvolution"},
    remap = false
)
public abstract class NativeExpectedTimerCopyMixin {
    @WrapOperation(
        method = {"writePlanHitTimer(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;ILnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/Tag;copy()Lnet/minecraft/nbt/Tag;",
            remap = false
        )},
        require = 0,
        allow = 1,
        remap = false
    )
    @Group(
        name = "ftboss$actualCopySite",
        min = 1,
        max = 1
    )
    private static Tag ftboss$actualOriginalCopySrg(Tag var0, Operation<Tag> var1) {
        return NativeExpectedFieldObservation.copiedArgument(var0, var1);
    }
}
