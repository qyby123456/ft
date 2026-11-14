package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
    targets = {"com.github.L_Ender.cataclysm.items.Ancient_Spear", "com.github.L_Ender.cataclysm.items.Tidal_Claws"},
    remap = false
)
public abstract class CataclysmCavityUseMixin {
    @Inject(
        method = {"onLeftClick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"},
        at = {@At("HEAD")},
        cancellable = true,
        require = 1,
        remap = false
    )
    private void ftboss$guardLeftClick(ItemStack var1, LivingEntity var2, CallbackInfoReturnable<Boolean> var3) {
        if (FoodWeaponLifecycle.isBroken(var1)) {
            var3.setReturnValue(false);
        }
    }
}
