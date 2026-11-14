/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.level.LevelAccessor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets={"net.gqstavo.armamentarium.entity.MitePelletEntity"}, remap=false)
public abstract class NativeMitePelletCallerMixin {
    @Redirect(method={"m_5790_(Lnet/minecraft/world/phys/EntityHitResult;)V"}, at=@At(value="INVOKE", target="Lnet/gqstavo/armamentarium/procedures/MitePelletProjectileHitsLivingEntityProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V"), require=0, remap=false)
    private void ftboss$mitePellet(LevelAccessor levelAccessor, double d, double d2, double d3, Entity entity) {
        NativeSpecialCaller.mite((AbstractArrow)this, "net.gqstavo.armamentarium.procedures.MitePelletProjectileHitsLivingEntityProcedure", levelAccessor, d, d2, d3, entity);
    }
}

