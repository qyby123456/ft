package com.farmingtales.drinkanim.mixin;

import com.farmingtales.drinkanim.client.ModelAnimationCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Punchy's camera sample can outlive its last rendered hand frame. */
@Pseudo
@Mixin(targets="punchy.client.render.CameraAnimationState",remap=false)
public abstract class PunchyCameraMixin {
    @Inject(method="isActive()Z",at=@At("HEAD"),cancellable=true,require=0,remap=false)
    private static void livelyBites$yieldCamera(CallbackInfoReturnable<Boolean> cir) {
        if(ModelAnimationCompat.ownsFirstPerson())cir.setReturnValue(false);
    }
}
