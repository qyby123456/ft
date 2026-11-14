package net.farmingtales.ftboss.mixin;

import java.util.Set;
import net.farmingtales.ftboss.item.NativePotionEffectFields;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MobEffectInstance.class, remap = false)
public abstract class NativeMobEffectInstanceFieldsMixin implements NativePotionEffectFields.Effect {
    @Shadow @Final private Holder<MobEffect> effect;
    @Shadow private int duration;
    @Shadow private int amplifier;
    @Shadow private boolean ambient;
    @Shadow private boolean visible;
    @Shadow private boolean showIcon;
    @Shadow private MobEffectInstance hiddenEffect;
    @Shadow @Final private Set<EffectCure> cures;
    @Unique private long ftboss$nativeEffectVersion;
    @Unique private boolean ftboss$nativeCuresEscaped;

    @Override
    public final NativePotionEffectFields.EffectState ftboss$nativeEffectState() {
        NativePotionEffectFields.requireObserver(MobEffectInstance.class, "ftboss$nativeEffectState");
        return new NativePotionEffectFields.EffectState(this.effect, this.duration, this.amplifier, this.ambient, this.visible,
            this.showIcon, this.hiddenEffect, this.cures, this.ftboss$nativeEffectVersion, this.ftboss$nativeCuresEscaped);
    }

    @Unique
    private void ftboss$advanceNativeEffectVersion() {
        if (this.ftboss$nativeEffectVersion != Long.MAX_VALUE) this.ftboss$nativeEffectVersion++;
    }

    @Inject(method = {"setDetailsFrom(Lnet/minecraft/world/effect/MobEffectInstance;)V", "copyBlendState(Lnet/minecraft/world/effect/MobEffectInstance;)V", "skipBlending()V"}, at = @At("HEAD"), require = 3, remap = false)
    private void ftboss$nativeEffectVoidMutation(CallbackInfo callback) {
        ftboss$advanceNativeEffectVersion();
    }

    @Inject(method = {"update(Lnet/minecraft/world/effect/MobEffectInstance;)Z", "tick(Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/Runnable;)Z", "tickDownDuration()I"}, at = @At("HEAD"), require = 3, remap = false)
    private void ftboss$nativeEffectReturnedMutation(CallbackInfoReturnable<?> callback) {
        ftboss$advanceNativeEffectVersion();
    }

    @Inject(method = "getCures()Ljava/util/Set;", at = @At("RETURN"), require = 1, remap = false)
    private void ftboss$nativeEffectCuresEscape(CallbackInfoReturnable<?> callback) {
        if (callback.getReturnValue() != null) this.ftboss$nativeCuresEscaped = true;
    }
}
