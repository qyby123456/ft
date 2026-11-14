package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.boss.ai.ManorAttackFrames;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.bmt.kaleidoscope_world_liquor.event.DamageEvents", remap = false)
public abstract class TequilaDamageEventsMixin {
    @Inject(method = "onLivingHurt", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ftboss$bypassManorDamageCap(LivingHurtEvent event, CallbackInfo callback) {
        if (!event.getEntity().level().isClientSide && !event.isCanceled()
                && ManorAttackFrames.isManorAttack(event.getSource())) {
            callback.cancel();
        }
    }
}
