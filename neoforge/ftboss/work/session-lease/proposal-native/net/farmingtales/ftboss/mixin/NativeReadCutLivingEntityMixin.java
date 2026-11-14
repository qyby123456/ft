package net.farmingtales.ftboss.mixin;

import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

@Mixin(
   targets = {"net.minecraft.world.entity.LivingEntity", "net.farmingtales.ftboss.item.FrostQueenStaffItem"},
   priority = 1,
   remap = false
)
public abstract class NativeReadCutLivingEntityMixin {
   @WrapOperation(method = "strike(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;FII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), require = 0, allow = 1)
   private static boolean ftboss$nativeFrostHurt(LivingEntity target, DamageSource source, float amount, Operation<Boolean> original) {
      return FoodWeaponSourceLedger.nativeFrostActualHurt(target, source, amount, original);
   }

   @WrapOperation(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"), require = 0, allow = 1)
   private void ftboss$nativeFrostWear(ItemStack stack, int amount, LivingEntity player, EquipmentSlot slot, Operation<Void> original) {
      FoodWeaponSourceLedger.nativeFrostOriginalWear(stack, amount, player, slot, original);
   }
}

