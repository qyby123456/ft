/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeEvergaleImpact;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={AbstractArrow.class}, remap=false)
public abstract class NativeVanillaArrowImpactMixin {
    @Redirect(method={"m_5790_(Lnet/minecraft/world/phys/EntityHitResult;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z", remap=false), require=1, expect=1, allow=1, remap=false)
    private boolean ftboss$nativeVanillaArrowRawHurt(Entity entity, DamageSource damageSource, float f) {
        return NativeEvergaleImpact.arrowHurt((AbstractArrow)this, entity, damageSource, f);
    }
}

