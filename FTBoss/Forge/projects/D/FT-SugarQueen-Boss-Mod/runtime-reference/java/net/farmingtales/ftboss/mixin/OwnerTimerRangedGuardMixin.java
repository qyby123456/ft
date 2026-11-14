/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponRanged"}, remap=false)
public abstract class OwnerTimerRangedGuardMixin {
    @WrapOperation(method={"saveHitTimers"}, at={@At(value="INVOKE", target="Lnet/minecraft/nbt/CompoundTag;isEmpty()Z", remap=true)}, require=1)
    private static boolean ftboss$ownerMaterialGuard(CompoundTag compoundTag, Operation<Boolean> operation, ServerPlayer serverPlayer, DamageSource damageSource, CompoundTag compoundTag2) {
        boolean bl = (Boolean)operation.call(new Object[]{compoundTag});
        try {
            OwnerTimerSemanticAuthority.rangedGuard(serverPlayer, damageSource, compoundTag, bl);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return bl;
    }
}

