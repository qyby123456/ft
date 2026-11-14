package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativePotionEffectFields;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.effect.MobEffectInstance$BlendState", remap = false)
public abstract class NativeMobEffectBlendFieldsMixin implements NativePotionEffectFields.Blend {
    @Shadow private float factor;
    @Shadow private float factorPreviousFrame;
    @Unique private long ftboss$nativeBlendVersion;

    @Override
    public final NativePotionEffectFields.BlendState ftboss$nativeBlendState() {
        NativePotionEffectFields.requireObserver(this.getClass(), "ftboss$nativeBlendState");
        return new NativePotionEffectFields.BlendState(Float.floatToRawIntBits(this.factor), Float.floatToRawIntBits(this.factorPreviousFrame), this.ftboss$nativeBlendVersion);
    }

    @Inject(method = {"setImmediate(Lnet/minecraft/world/effect/MobEffectInstance;)V", "copyFrom(Lnet/minecraft/world/effect/MobEffectInstance$BlendState;)V", "tick(Lnet/minecraft/world/effect/MobEffectInstance;)V"}, at = @At("HEAD"), require = 3, remap = false)
    private void ftboss$nativeBlendMutation(CallbackInfo callback) {
        if (this.ftboss$nativeBlendVersion != Long.MAX_VALUE) this.ftboss$nativeBlendVersion++;
    }

    @Inject(method = "getFactor(Lnet/minecraft/world/entity/LivingEntity;F)F", at = @At("HEAD"), require = 1, remap = false)
    private void ftboss$nativeBlendConditionalMutation(LivingEntity entity, float partialTick, CallbackInfoReturnable<Float> callback) {
        if (entity != null && entity.isRemoved() && this.ftboss$nativeBlendVersion != Long.MAX_VALUE) this.ftboss$nativeBlendVersion++;
    }
}
