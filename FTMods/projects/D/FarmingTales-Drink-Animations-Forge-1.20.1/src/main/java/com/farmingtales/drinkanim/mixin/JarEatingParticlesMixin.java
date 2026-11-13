package com.farmingtales.drinkanim.mixin;

import com.farmingtales.drinkanim.client.JarEatingParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class JarEatingParticlesMixin {
    @Inject(method="createParticle", at=@At("HEAD"), cancellable=true)
    private void farmingtales$jarEatingParticles(ParticleOptions options,double x,double y,double z,
        double dx,double dy,double dz,CallbackInfoReturnable<Particle> ci) {
        if(JarEatingParticles.hide(options,x,y,z))ci.setReturnValue(null);
    }
}
