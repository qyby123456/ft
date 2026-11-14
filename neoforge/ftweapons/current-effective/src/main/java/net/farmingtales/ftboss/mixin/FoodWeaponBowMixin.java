package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = {BowItem.class},
    remap = false
)
public abstract class FoodWeaponBowMixin {
    @Inject(
        method = {"releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V", "m_5551_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private void ftboss$brokenBow(ItemStack var1, Level var2, LivingEntity var3, int var4, CallbackInfo var5) {
        if (FoodWeaponLifecycle.isBroken(var1)) {
            var5.cancel();
        }
    }

}
