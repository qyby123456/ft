/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 *  org.spongepowered.asm.mixin.injection.Group
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Group;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponEvolution"}, remap=false)
public abstract class NativeExplicitTimerPutMixin {
    @WrapOperation(method={"writePlanHitTimer(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;ILnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/nbt/CompoundTag;m_128365_(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;")}, require=0, allow=1, remap=false)
    @Group(name="ftboss$r2$timerPut", min=1, max=1)
    private static Tag ftboss$r2$timerPutSrg(CompoundTag compoundTag, String string, Tag tag, Operation<Tag> operation, ItemStack itemStack, FoodWeaponEvolution.Profile profile, String string2, Tag tag2, FoodWeaponSourceLedger.OwnerWriterStep ownerWriterStep, @Coerce Object object, int n, @Coerce Object object2, Object object3) {
        return NativeExpectedFieldObservation.timerPut(ownerWriterStep, itemStack, compoundTag, string, tag, operation);
    }
}

