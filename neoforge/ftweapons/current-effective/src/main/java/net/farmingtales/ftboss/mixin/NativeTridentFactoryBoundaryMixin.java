package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TridentItem.class, remap = false)
public abstract class NativeTridentFactoryBoundaryMixin {
    @WrapOperation(
        method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
        at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/ThrownTrident;"),
        require = 1
    )
    private ThrownTrident ftboss$v6$factory(Level level, LivingEntity shooter, ItemStack weapon, Operation<ThrownTrident> original) {
        return FoodWeaponSourceLedger.nativeTridentFactory(level, shooter, weapon, original);
    }

    @WrapOperation(
        method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
        require = 1
    )
    private boolean ftboss$v6$add(Level level, Entity entity, Operation<Boolean> original) {
        return FoodWeaponSourceLedger.nativeAdd(level, entity, original);
    }

    @WrapOperation(
        method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"),
        require = 1
    )
    private void ftboss$v6$wear(ItemStack weapon, int amount, LivingEntity shooter, EquipmentSlot slot, Operation<Void> original) {
        FoodWeaponSourceLedger.nativeWear(weapon, amount, shooter, slot, original);
    }
}
