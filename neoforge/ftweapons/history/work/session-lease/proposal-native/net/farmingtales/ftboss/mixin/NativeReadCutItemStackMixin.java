package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeObservationCarrier;
import net.farmingtales.ftboss.item.NativeObservationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"net.minecraft.world.item.ItemStack"},
   priority = 1,
   remap = false
)
public abstract class NativeReadCutItemStackMixin implements NativeObservationCarrier {
   @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", at = @At("HEAD"), order = 900, require = 1)
   private void ftboss$nativeFrostUseEntered(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callback) {
      FoodWeaponSourceLedger.nativeFrostOriginalUseEntered((ItemStack)(Object)this, level, player, hand);
      FoodWeaponSourceLedger.nativeRangedOriginalUseEntered((ItemStack)(Object)this, level, player, hand);
   }

   @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", at = @At("RETURN"), require = 1)
   private void ftboss$nativeFrostUseReturned(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callback) {
      FoodWeaponSourceLedger.nativeFrostOriginalUseReturned((ItemStack)(Object)this);
      FoodWeaponSourceLedger.nativeRangedOriginalUseReturned((ItemStack)(Object)this);
   }

   @Unique
   private NativeObservationState ftboss$nativeObservations;

   public final synchronized NativeObservationState ftboss$nativeObservationState() {
      if (this.ftboss$nativeObservations == null) {
         this.ftboss$nativeObservations = new NativeObservationState();
      }

      return this.ftboss$nativeObservations;
   }
}

