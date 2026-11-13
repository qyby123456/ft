package com.farmingtales.drinkanim.mixin;

import com.farmingtales.drinkanim.client.DrinkController;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class DrinkMovementMixin {
    // These checks only gate the food slowdown and sprint input in LocalPlayer.
    // The actual using-item state and its server consumption remain intact.
    @Redirect(method="aiStep", at=@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"), require=2)
    private boolean farmingtales$drinkMovement(LocalPlayer player) {
        return player.isUsingItem()&&!DrinkController.shouldHoldUse();
    }

    @Redirect(method="canStartSprinting", at=@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean farmingtales$drinkSprint(LocalPlayer player) {
        return player.isUsingItem()&&!DrinkController.shouldHoldUse();
    }
}
