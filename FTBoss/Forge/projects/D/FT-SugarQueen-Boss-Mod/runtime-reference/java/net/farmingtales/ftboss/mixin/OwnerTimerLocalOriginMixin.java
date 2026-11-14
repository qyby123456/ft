/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponEvolution"}, remap=false)
public abstract class OwnerTimerLocalOriginMixin {
    @WrapOperation(method={"quoteFoodBlessingHits"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getPersistentData()Lnet/minecraft/nbt/CompoundTag;", remap=false)}, require=8, expect=8, allow=8)
    private static CompoundTag ftboss$ownerTargetReceiver(LivingEntity livingEntity, Operation<CompoundTag> operation, FoodWeaponHitPlan.Builder builder, ServerPlayer serverPlayer, LivingEntity livingEntity2, CompoundTag compoundTag, long l, ItemStack itemStack, FoodWeaponHitPlan.RandomQuotes randomQuotes) {
        CompoundTag compoundTag2 = (CompoundTag)operation.call(new Object[]{livingEntity});
        try {
            OwnerNativeSemanticAuthority.targetReceiverReturned(builder, livingEntity2, livingEntity, compoundTag2);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return compoundTag2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapOperation(method={"quoteFoodBlessingHits"}, at={@At(value="INVOKE", target="Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;write(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V")}, require=7, expect=7, allow=7)
    private static void ftboss$ownerTargetWrite(FoodWeaponHitPlan.Builder builder, FoodWeaponSourceLedger.AttemptDomain attemptDomain, CompoundTag compoundTag, String string, Tag tag, Operation<Void> operation, FoodWeaponHitPlan.Builder builder2, ServerPlayer serverPlayer, LivingEntity livingEntity, CompoundTag compoundTag2, long l, ItemStack itemStack, FoodWeaponHitPlan.RandomQuotes randomQuotes) throws Throwable {
        OwnerNativeSemanticAuthority.TargetWriteCall targetWriteCall = null;
        try {
            targetWriteCall = OwnerNativeSemanticAuthority.targetWriteBegins(builder, builder2, serverPlayer, livingEntity, itemStack, compoundTag2, compoundTag, string, attemptDomain, tag);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            operation.call(new Object[]{builder, attemptDomain, compoundTag, string, tag});
        }
        catch (Throwable throwable) {
            try {
                OwnerNativeSemanticAuthority.targetWriteEnds(targetWriteCall);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
            throw throwable;
        }
        try {
            OwnerNativeSemanticAuthority.targetWriteEnds(targetWriteCall);
        }
        catch (Throwable throwable) {}
    }

    @WrapOperation(method={"quoteFinalWeaponHit"}, at={@At(value="INVOKE", target="Lnet/minecraft/nbt/CompoundTag;copy()Lnet/minecraft/nbt/CompoundTag;", ordinal=1, remap=true)}, require=1)
    private static CompoundTag ftboss$ownerLocalDataCopy(CompoundTag compoundTag, Operation<CompoundTag> operation) {
        CompoundTag compoundTag2 = (CompoundTag)operation.call(new Object[]{compoundTag});
        try {
            OwnerTimerSemanticAuthority.localDataCopied(compoundTag, compoundTag2);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return compoundTag2;
    }

    @WrapOperation(method={"quoteFinalWeaponHit"}, at={@At(value="INVOKE", target="Lnet/minecraft/nbt/CompoundTag;copy()Lnet/minecraft/nbt/CompoundTag;", ordinal=2, remap=true)}, require=1)
    private static CompoundTag ftboss$ownerLocalBaselineCopy(CompoundTag compoundTag, Operation<CompoundTag> operation) {
        CompoundTag compoundTag2 = (CompoundTag)operation.call(new Object[]{compoundTag});
        try {
            OwnerTimerSemanticAuthority.localBaselineCopied(compoundTag, compoundTag2);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return compoundTag2;
    }
}

