package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodBlessingCombat;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CrossbowItem.class, remap = false)
public abstract class FoodBlessingCrossbowMixin {
    @Inject(method = {"getChargeDuration(Lnet/minecraft/world/item/ItemStack;)I", "m_40939_(Lnet/minecraft/world/item/ItemStack;)I"},
            at = @At("RETURN"), cancellable = true, require = 1, remap = false)
    private static void ftboss$blessingCharge(ItemStack weapon, CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(FoodBlessingCombat.crossbowChargeTicks(callback.getReturnValue(), weapon));
    }
}
