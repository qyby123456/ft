/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"com.github.L_Ender.cataclysm.items.Ancient_Spear", "com.github.L_Ender.cataclysm.items.Tidal_Claws"}, remap=false)
public abstract class CataclysmCavityUseMixin {
    @Inject(method={"onLeftClick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"}, at={@At(value="HEAD")}, cancellable=true, require=1, remap=false)
    private void ftboss$guardLeftClick(ItemStack itemStack, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (FoodWeaponLifecycle.isBroken(itemStack)) {
            callbackInfoReturnable.setReturnValue((Object)false);
        }
    }
}

