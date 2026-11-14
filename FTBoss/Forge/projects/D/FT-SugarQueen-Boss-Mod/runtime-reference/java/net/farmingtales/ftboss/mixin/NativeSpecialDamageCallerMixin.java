/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets={"com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity"}, remap=false)
public abstract class NativeSpecialDamageCallerMixin {
    @Redirect(method={"damage"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), require=0, remap=false)
    private boolean ftboss$halberdNativeHurt(LivingEntity livingEntity, DamageSource damageSource, float f) {
        return NativeSpecialCaller.delayedHurt((Entity)this, (Entity)livingEntity, damageSource, f);
    }
}

