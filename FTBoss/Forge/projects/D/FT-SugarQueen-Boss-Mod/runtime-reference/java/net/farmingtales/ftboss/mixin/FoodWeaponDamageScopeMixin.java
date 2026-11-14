/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.common.ForgeHooks
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Group
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.NativeActionObservation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={LivingEntity.class, Player.class}, remap=false)
public abstract class FoodWeaponDamageScopeMixin {
    @Redirect(method={"m_6475_(Lnet/minecraft/world/damagesource/DamageSource;F)V"}, at=@At(value="INVOKE", target="Lnet/minecraftforge/common/ForgeHooks;onLivingHurt(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F"), require=0, remap=false)
    @Group(name="ftboss$damageScope", min=1, max=1)
    private float ftboss$hurtSrg(LivingEntity livingEntity, DamageSource damageSource, float f) {
        return FoodWeaponRanged.withHitSource((Entity)livingEntity, damageSource, () -> Float.valueOf(ForgeHooks.onLivingHurt((LivingEntity)livingEntity, (DamageSource)damageSource, (float)f))).floatValue();
    }

    @Redirect(method={"m_6475_(Lnet/minecraft/world/damagesource/DamageSource;F)V"}, at=@At(value="INVOKE", target="Lnet/minecraftforge/common/ForgeHooks;onLivingDamage(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F"), require=0, remap=false)
    @Group(name="native$finalDamage", min=1, max=1)
    private float nativeFinalDamageSrg(LivingEntity livingEntity, DamageSource damageSource, float f) {
        return this.nativeFinal(livingEntity, damageSource, f);
    }

    private float nativeFinal(LivingEntity livingEntity, DamageSource damageSource, float f) {
        return FoodWeaponRanged.withHitSource((Entity)livingEntity, damageSource, () -> {
            float f2 = ForgeHooks.onLivingDamage((LivingEntity)livingEntity, (DamageSource)damageSource, (float)f);
            NativeActionObservation.finalDamage(livingEntity, damageSource, f2);
            return Float.valueOf(f2);
        }).floatValue();
    }
}

