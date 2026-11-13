package com.farmingtales.drinkanim.mixin;

import com.farmingtales.drinkanim.client.ModelAnimationCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Give the complete two-hand rig ownership; never modify Punchy's saved preferences. */
@Pseudo
@Mixin(targets="punchy.config.PunchyConfig",remap=false)
public abstract class PunchyConfigMixin {
    @Inject(method="isModEnabled()Z",at=@At("HEAD"),cancellable=true,require=0,remap=false)
    private static void livelyBites$yieldHands(CallbackInfoReturnable<Boolean> cir) {
        if(ModelAnimationCompat.ownsFirstPerson())cir.setReturnValue(false);
    }
}
