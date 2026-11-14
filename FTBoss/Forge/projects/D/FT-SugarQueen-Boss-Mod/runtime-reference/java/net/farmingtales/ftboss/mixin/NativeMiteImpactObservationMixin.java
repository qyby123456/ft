/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.LevelAccessor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeActionObservation;
import net.farmingtales.ftboss.item.NativeMiteImpactCaller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets={"net.gqstavo.armamentarium.entity.MiteArrowEntity"}, remap=false)
public abstract class NativeMiteImpactObservationMixin {
    @Redirect(method={"m_5790_(Lnet/minecraft/world/phys/EntityHitResult;)V"}, at=@At(value="INVOKE", target="Lnet/gqstavo/armamentarium/procedures/MiteArrowProjectileHitsLivingEntityProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V"), require=1, expect=1, allow=1, remap=false)
    private void nativeMiteImpact(LevelAccessor levelAccessor, double d, double d2, double d3, Entity entity) {
        NativeActionObservation.impact((Entity)this, entity, () -> ((NativeMiteImpactCaller)((Object)this)).ftboss$miteArrow(levelAccessor, d, d2, d3, entity));
    }
}

