/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponUseBridge"}, remap=false)
public abstract class NativeExpectedCaptureBoundaryMixin {
    @WrapOperation(method={"invoke(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Ljava/util/function/Supplier;Ljava/lang/Object;)Ljava/lang/Object;"}, at={@At(value="INVOKE", target="Lnet/farmingtales/ftboss/item/FoodWeaponRanged;captureBound(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/util/FoodWeaponFamilyRules$Kind;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/nbt/CompoundTag;", remap=false)}, require=1, expect=1, allow=1, remap=false)
    private static CompoundTag ftboss$originalCapture(ServerPlayer serverPlayer, ItemStack itemStack, FoodWeaponFamilyRules.Kind kind, InteractionHand interactionHand, Operation<CompoundTag> operation) {
        return NativeExpectedFieldObservation.capture(serverPlayer, itemStack, kind, interactionHand, operation);
    }
}

