package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.Profile;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf"},
    remap = false
)
public abstract class NativeExpectedTimerScopeMixin {
    @WrapOperation(
        method = {"apply(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/farmingtales/ftboss/item/FoodWeaponEvolution;writePlanHitTimer(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;ILnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;)V",
            remap = false
        )},
        require = 1,
        expect = 1,
        allow = 1,
        remap = false
    )
    private void ftboss$originalTimerScope(
        ItemStack var1,
        Profile var2,
        String var3,
        Tag var4,
        FoodWeaponSourceLedger.OwnerWriterStep var5,
        @Coerce Object var6,
        int var7,
        @Coerce Object var8,
        Object var9,
        Operation<Void> var10
    ) {
        NativeExpectedFieldObservation.timer(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10);
    }
}
