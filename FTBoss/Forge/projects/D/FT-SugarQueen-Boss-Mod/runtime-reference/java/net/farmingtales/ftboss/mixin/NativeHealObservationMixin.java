/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.event.ForgeEventFactory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Group
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeActionObservation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={LivingEntity.class}, remap=false)
public abstract class NativeHealObservationMixin {
    @Redirect(method={"m_5634_(F)V"}, at=@At(value="INVOKE", target="Lnet/minecraftforge/event/ForgeEventFactory;onLivingHeal(Lnet/minecraft/world/entity/LivingEntity;F)F"), require=0, remap=false)
    @Group(name="native$heal", min=1, max=1)
    private float nativeHealSrg(LivingEntity livingEntity, float f) {
        return NativeHealObservationMixin.observe(livingEntity, f);
    }

    private static float observe(LivingEntity livingEntity, float f) {
        float f2 = ForgeEventFactory.onLivingHeal((LivingEntity)livingEntity, (float)f);
        NativeActionObservation.heal(livingEntity, f2);
        return f2;
    }
}

