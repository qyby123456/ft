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

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets={"io.redspace.ironsspellbooks.damage.DamageSources"}, remap=false)
public abstract class NativeIronActualDamageMixin {
    @Redirect(method={"applyDamage(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)Z"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), require=1, remap=false)
    private static boolean ftboss$v10$ironLivingHurt(LivingEntity livingEntity, DamageSource damageSource, float f) {
        return FoodWeaponSourceLedger.nativeIronActualHurt((Entity)livingEntity, damageSource, f);
    }

    @Redirect(method={"applyDamage(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)Z"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), require=1, remap=false)
    private static boolean ftboss$v10$ironEntityHurt(Entity entity, DamageSource damageSource, float f) {
        return FoodWeaponSourceLedger.nativeIronActualHurt(entity, damageSource, f);
    }
}

