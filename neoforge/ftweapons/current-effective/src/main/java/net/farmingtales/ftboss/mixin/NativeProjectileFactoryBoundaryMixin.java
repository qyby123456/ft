package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ProjectileWeaponItem.class, remap = false)
public abstract class NativeProjectileFactoryBoundaryMixin {
    @WrapOperation(
        method = "createProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/projectile/Projectile;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArrowItem;createArrow(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"),
        require = 1
    )
    private AbstractArrow ftboss$v6$ammoFactory(ArrowItem item, Level level, ItemStack ammunition, LivingEntity shooter, ItemStack weapon, Operation<AbstractArrow> original) {
        return FoodWeaponSourceLedger.nativeArrowFactory(item, level, ammunition, shooter, weapon, original);
    }

    @WrapOperation(
        method = "createProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/projectile/Projectile;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;customArrow(Lnet/minecraft/world/entity/projectile/AbstractArrow;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"),
        require = 1
    )
    private AbstractArrow ftboss$v6$factory(ProjectileWeaponItem item, AbstractArrow arrow, ItemStack ammunition, ItemStack weapon, Operation<AbstractArrow> original) {
        return FoodWeaponSourceLedger.nativeCustomArrow(item, arrow, ammunition, weapon, original);
    }

    @WrapOperation(
        method = "shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
        require = 1
    )
    private boolean ftboss$v6$add(ServerLevel level, Entity entity, Operation<Boolean> original) {
        return FoodWeaponSourceLedger.nativeAdd(level, entity, original);
    }

    @WrapOperation(
        method = "shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"),
        require = 1
    )
    private void ftboss$v6$wear(ItemStack weapon, int amount, LivingEntity shooter, EquipmentSlot slot, Operation<Void> original) {
        FoodWeaponSourceLedger.nativeWear(weapon, amount, shooter, slot, original);
    }
}
