/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.common.ForgeHooks
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.boss.ai.FinalBossDamageGuard;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ForgeHooks.class}, remap=false)
public abstract class FinalBossDamageMixin {
    @Inject(method={"onLivingDamage"}, at={@At(value="RETURN")}, cancellable=true, remap=false)
    private static void ftboss$finalDamageGuard(LivingEntity livingEntity, DamageSource damageSource, float f, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        float f2 = ((Float)callbackInfoReturnable.getReturnValue()).floatValue();
        float f3 = FinalBossDamageGuard.settled(livingEntity, damageSource, f2);
        if (Float.floatToIntBits(f3) != Float.floatToIntBits(f2)) {
            callbackInfoReturnable.setReturnValue((Object)Float.valueOf(f3));
        }
    }
}

