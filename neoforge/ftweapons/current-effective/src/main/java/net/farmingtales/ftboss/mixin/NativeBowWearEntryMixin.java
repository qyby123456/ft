package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileWeaponItem.class)
public abstract class NativeBowWearEntryMixin {
    @WrapOperation(method = "createProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArrowItem;createArrow(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"), require = 1, expect = 1, allow = 1)
    private AbstractArrow ftboss$v6$ammoFactory(ArrowItem item, Level level, ItemStack ammunition, LivingEntity actor, ItemStack weapon, Operation<AbstractArrow> original) {
        return FoodWeaponSourceLedger.nativeArrowFactory(item, level, ammunition, actor, weapon, original);
    }

    @WrapOperation(method = "shoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"), require = 1, expect = 1, allow = 1)
    private void ftboss$v6$wear(ItemStack stack, int amount, LivingEntity actor, EquipmentSlot slot, Operation<Void> original) {
        FoodWeaponSourceLedger.nativeWear(stack, amount, actor, slot, original);
    }
}
