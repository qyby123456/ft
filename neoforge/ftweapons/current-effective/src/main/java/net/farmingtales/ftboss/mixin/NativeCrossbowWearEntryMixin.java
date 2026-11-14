package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileWeaponItem.class)
public abstract class NativeCrossbowWearEntryMixin {
    @WrapOperation(method = "createProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;customArrow(Lnet/minecraft/world/entity/projectile/AbstractArrow;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"), require = 1, expect = 1, allow = 1)
    private AbstractArrow ftboss$v6$factory(ProjectileWeaponItem item, AbstractArrow arrow, ItemStack ammunition, ItemStack weapon, Operation<AbstractArrow> original) {
        return FoodWeaponSourceLedger.nativeCustomArrow(item, arrow, ammunition, weapon, original);
    }

    @WrapOperation(method = "shoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"), require = 1, expect = 1, allow = 1)
    private boolean ftboss$v6$add(ServerLevel level, Entity projectile, Operation<Boolean> original) {
        return FoodWeaponSourceLedger.nativeAdd(level, projectile, original);
    }
}
