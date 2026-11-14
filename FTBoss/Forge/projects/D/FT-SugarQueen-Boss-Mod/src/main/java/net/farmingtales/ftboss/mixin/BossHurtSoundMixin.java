package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.sound.BossHurtSounds;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntity.class, remap = false)
public abstract class BossHurtSoundMixin {
    @Inject(method = {"playHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)V",
            "m_6677_(Lnet/minecraft/world/damagesource/DamageSource;)V"},
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ftboss$hurtSound(DamageSource source, CallbackInfo callback) {
        if (BossHurtSounds.play((LivingEntity) (Object) this)) callback.cancel();
    }
}
