/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets={"net.gqstavo.armamentarium.procedures.MiteArrowProjectileHitsLivingEntityProcedure", "net.gqstavo.armamentarium.procedures.MitePelletProjectileHitsLivingEntityProcedure"}, remap=false)
public abstract class NativeMiteAnonymousHurtMixin {
    @Redirect(method={"execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), require=0, remap=false)
    private static boolean ftboss$exactMiteHurt(Entity entity, DamageSource damageSource, float f) {
        return NativeSpecialCaller.miteHurt(entity, damageSource, f);
    }
}

