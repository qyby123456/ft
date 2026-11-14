/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.ThrownTrident
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeVanillaImpact;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={ThrownTrident.class}, remap=false)
public abstract class NativeVanillaTridentImpactMixin {
    @Redirect(method={"m_5790_(Lnet/minecraft/world/phys/EntityHitResult;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z", remap=false), require=1, expect=1, allow=1, remap=false)
    private boolean ftboss$nativeVanillaTridentRawHurt(Entity entity, DamageSource damageSource, float f) {
        return NativeVanillaImpact.tridentHurt((ThrownTrident)this, entity, damageSource, f);
    }
}

