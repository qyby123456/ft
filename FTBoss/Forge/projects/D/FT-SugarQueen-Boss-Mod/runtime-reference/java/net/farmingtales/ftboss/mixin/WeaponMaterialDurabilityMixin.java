/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemStack.class}, remap=false)
public abstract class WeaponMaterialDurabilityMixin {
    @ModifyVariable(method={"hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", "m_220157_(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0, remap=false)
    private int ftboss$materialWear(int n) {
        return WeaponMaterialBuild.durabilityCost((ItemStack)this, n);
    }

    @ModifyVariable(method={"setDamageValue(I)V", "m_41721_(I)V"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0, remap=false)
    private int ftboss$protectedDamage(int n) {
        return FoodWeaponLifecycle.beforeDamageChange((ItemStack)this, n);
    }

    @Inject(method={"setDamageValue(I)V", "m_41721_(I)V"}, at={@At(value="RETURN")}, remap=false)
    private void ftboss$wearLoss(int n, CallbackInfo callbackInfo) {
        FoodWeaponLifecycle.afterDamageChange((ItemStack)this);
    }

    @Inject(method={"hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", "m_220157_(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z"}, at={@At(value="RETURN")}, cancellable=true, remap=false)
    private void ftboss$keepExhausted(int n, RandomSource randomSource, ServerPlayer serverPlayer, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue((Object)FoodWeaponLifecycle.finishHurt((ItemStack)this, (Boolean)callbackInfoReturnable.getReturnValue(), serverPlayer));
    }

    @Inject(method={"getMaxDamage()I", "m_41776_()I"}, at={@At(value="RETURN")}, cancellable=true, remap=false)
    private void ftboss$materialMaxDamage(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        ItemStack itemStack = (ItemStack)this;
        int n = (Integer)callbackInfoReturnable.getReturnValue();
        int n2 = WeaponMaterialBuild.maxDurability(itemStack, n);
        if (n2 != n) {
            callbackInfoReturnable.setReturnValue((Object)n2);
        }
    }
}

