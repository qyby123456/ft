package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.EquipmentSlot;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({TridentItem.class})
public abstract class NativeTridentWearEntryMixin {
    @WrapOperation(
        method = {"releaseUsing"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"
        ),
        require = 1,
        expect = 1,
        allow = 1
    )
    private void ftboss$v6$wear(ItemStack var1, int var2, LivingEntity var3, EquipmentSlot var4, Operation<Void> original) {
        FoodWeaponSourceLedger.nativeWear(var1, var2, var3, var4, original);
    }

    @WrapOperation(
        method = {"releaseUsing"},
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/ThrownTrident;"
        ),
        require = 1,
        expect = 1,
        allow = 1
    )
    private ThrownTrident ftboss$v6$factory(Level var1, LivingEntity var2, ItemStack var3, Operation<ThrownTrident> original) {
        return FoodWeaponSourceLedger.nativeTridentFactory(var1, var2, var3, original);
    }

    @WrapOperation(
        method = {"releaseUsing"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
        ),
        require = 1,
        expect = 1,
        allow = 1
    )
    private boolean ftboss$v6$add(Level var1, Entity var2, Operation<Boolean> original) {
        return FoodWeaponSourceLedger.nativeAdd(var1, var2, original);
    }
}
