package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Player.class, priority = 1, remap = false)
public abstract class NativeReadCutPlayerMixin {
    @WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
        require = 1, expect = 1, allow = 1)
    private boolean ftboss$nativeMeleeHurt(Entity target, DamageSource source, float amount, Operation<Boolean> original) {
        return FoodWeaponSourceLedger.nativeMeleeActualHurt((Player)(Object)this, target, source, amount, original);
    }

    @WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"), require = 1, expect = 1, allow = 1)
    private ItemStack ftboss$nativeMeleeCopy(ItemStack stack, Operation<ItemStack> original) {
        return FoodWeaponSourceLedger.nativeMeleeOriginalCopy((Player)(Object)this, stack, original);
    }

    @WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)Z"),
        require = 1, expect = 1, allow = 1)
    private boolean ftboss$nativeMeleeEnemy(ItemStack stack, LivingEntity target, Player actor, Operation<Boolean> original) {
        return FoodWeaponSourceLedger.nativeMeleeOriginalEnemy((Player)(Object)this, stack, target, actor, original);
    }

    @WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;postHurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)V"),
        require = 1, expect = 1, allow = 1)
    private void ftboss$nativeMeleePost(ItemStack stack, LivingEntity target, Player actor, Operation<Void> original) {
        FoodWeaponSourceLedger.nativeMeleeOriginalPost((Player)(Object)this, stack, target, actor, original);
    }
}
