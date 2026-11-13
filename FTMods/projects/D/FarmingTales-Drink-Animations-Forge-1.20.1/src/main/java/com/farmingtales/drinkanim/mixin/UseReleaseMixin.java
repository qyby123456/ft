package com.farmingtales.drinkanim.mixin;

import com.farmingtales.drinkanim.client.DrinkController;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class UseReleaseMixin {
    @Inject(method="releaseUsingItem",at=@At("HEAD"),cancellable=true)
    private void farmingtales$keepKeyDrink(Player player, CallbackInfo ci) {
        if(DrinkController.shouldHoldUse())ci.cancel();
    }
}
