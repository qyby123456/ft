package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.boss.ai.FinalBossDamageGuard;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooks.class, remap = false)
public abstract class FinalBossDamageMixin {
    @Inject(method = "onLivingDamage", at = @At("RETURN"), cancellable = true, remap = false)
    private static void ftboss$finalDamageGuard(LivingEntity entity, DamageSource source, float amount,
                                              CallbackInfoReturnable<Float> callback) {
        float finalDamage = callback.getReturnValue();
        float guardedDamage = FinalBossDamageGuard.settled(entity, source, finalDamage);
        if (Float.floatToIntBits(guardedDamage) != Float.floatToIntBits(finalDamage)) {
            callback.setReturnValue(guardedDamage);
        }
    }
}
