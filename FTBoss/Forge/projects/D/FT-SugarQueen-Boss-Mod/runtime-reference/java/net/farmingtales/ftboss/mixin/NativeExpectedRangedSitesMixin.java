/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Group
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponRanged"}, remap=false)
public abstract class NativeExpectedRangedSitesMixin {
    @WrapOperation(method={"prepareHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V", "saveHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/level/ServerPlayer;getPersistentData()Lnet/minecraft/nbt/CompoundTag;", remap=false)}, require=3, expect=3, allow=3, remap=false)
    private static CompoundTag ftboss$originalRangedPersistentData(ServerPlayer serverPlayer, Operation<CompoundTag> operation) {
        return NativeExpectedFieldObservation.persistentData((Entity)serverPlayer, operation);
    }

    @WrapOperation(method={"prepareHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V", "saveHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/nbt/CompoundTag;m_128469_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;", remap=false)}, require=0, allow=2, remap=false)
    @Group(name="ftboss$rangedCompoundSite", min=2, max=2)
    private static CompoundTag ftboss$originalRangedCompoundSrg(CompoundTag compoundTag, String string, Operation<CompoundTag> operation) {
        return NativeExpectedFieldObservation.compound(compoundTag, string, operation);
    }

    @WrapOperation(method={"prepareHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V", "saveHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/nbt/CompoundTag;m_128356_(Ljava/lang/String;J)V", remap=false)}, require=0, allow=2, remap=false)
    @Group(name="ftboss$rangedLongSite", min=2, max=2)
    private static void ftboss$originalRangedLongSrg(CompoundTag compoundTag, String string, long l, Operation<Void> operation) {
        NativeExpectedFieldObservation.rangedLong(compoundTag, string, l, operation);
    }

    @WrapOperation(method={"saveHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/nbt/CompoundTag;m_128365_(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", remap=false)}, require=0, allow=1, remap=false)
    @Group(name="ftboss$rangedRootSite", min=1, max=1)
    private static Tag ftboss$originalRangedRootSrg(CompoundTag compoundTag, String string, Tag tag, Operation<Tag> operation) {
        return NativeExpectedFieldObservation.rangedRoot(compoundTag, string, tag, operation);
    }
}

