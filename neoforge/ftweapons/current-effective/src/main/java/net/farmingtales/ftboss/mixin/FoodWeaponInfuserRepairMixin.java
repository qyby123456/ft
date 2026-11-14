package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
    targets = {"fuzs.enchantinginfuser.world.inventory.InfuserMenu"},
    remap = false
)
public abstract class FoodWeaponInfuserRepairMixin {
    @Shadow(
        remap = false
    )
    public abstract ItemStack getEnchantableStack();

    @Inject(
        method = {"clickRepairButton(Lnet/minecraft/world/entity/player/Player;)Z", "canRepair(Lnet/minecraft/world/entity/player/Player;)Z"},
        at = {@At("HEAD")},
        cancellable = true,
        require = 2,
        allow = 2,
        remap = false
    )
    private void ftboss$rejectRepair(Player var1, CallbackInfoReturnable<Boolean> var2) {
        if (NativeFoodWeaponWear.eligible(this.getEnchantableStack())) {
            var2.setReturnValue(false);
        }
    }
}
