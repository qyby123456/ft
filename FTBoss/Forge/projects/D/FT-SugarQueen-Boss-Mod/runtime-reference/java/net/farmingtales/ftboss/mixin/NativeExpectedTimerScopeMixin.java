/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf"}, remap=false)
public abstract class NativeExpectedTimerScopeMixin {
    @WrapOperation(method={"apply(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V"}, at={@At(value="INVOKE", target="Lnet/farmingtales/ftboss/item/FoodWeaponEvolution;writePlanHitTimer(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;ILnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;)V", remap=false)}, require=1, expect=1, allow=1, remap=false)
    private void ftboss$originalTimerScope(ItemStack itemStack, FoodWeaponEvolution.Profile profile, String string, Tag tag, FoodWeaponSourceLedger.OwnerWriterStep ownerWriterStep, @Coerce Object object, int n, @Coerce Object object2, Object object3, Operation<Void> operation) {
        NativeExpectedFieldObservation.timer(itemStack, profile, string, tag, ownerWriterStep, object, n, object2, object3, operation);
    }
}

