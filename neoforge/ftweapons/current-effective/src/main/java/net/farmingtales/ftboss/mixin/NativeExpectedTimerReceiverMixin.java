package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.Profile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponEvolution"},
    remap = false
)
public abstract class NativeExpectedTimerReceiverMixin {
    @WrapOperation(
        method = {"writePlanHitTimer(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;ILnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/farmingtales/ftboss/item/FoodWeaponEvolution;writableData(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;)Lnet/minecraft/nbt/CompoundTag;",
            remap = false
        )},
        require = 1,
        expect = 1,
        allow = 1,
        remap = false
    )
    private static CompoundTag ftboss$actualOriginalReceiver(ItemStack var0, Profile var1, Operation<CompoundTag> var2) {
        return NativeExpectedFieldObservation.timerReceiver(var0, var1, var2);
    }
}
