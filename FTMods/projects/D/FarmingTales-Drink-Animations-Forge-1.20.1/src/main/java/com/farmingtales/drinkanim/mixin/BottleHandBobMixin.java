package com.farmingtales.drinkanim.mixin;

import com.farmingtales.drinkanim.DrinkConfig;
import com.farmingtales.drinkanim.client.DrinkController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class BottleHandBobMixin {
    @Unique private boolean farmingtales$bottlePass;
    @Inject(method="renderItemInHand",at=@At("HEAD"))
    private void beginBottlePass(CallbackInfo ci) {
        var mc=Minecraft.getInstance();
        farmingtales$bottlePass=mc.player!=null&&DrinkConfig.ENABLED.get()
            &&DrinkController.supports(DrinkController.renderStack(mc.player.getMainHandItem()))&&!DrinkController.vanillaUseActive();
    }
    @Inject(method="renderItemInHand",at=@At("RETURN"))
    private void endBottlePass(CallbackInfo ci) {farmingtales$bottlePass=false;}
    @Inject(method="bobView",at=@At("HEAD"),cancellable=true)
    private void useBottleMovement(CallbackInfo ci) {if(farmingtales$bottlePass)ci.cancel();}
}
