/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.level.LevelAccessor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeMiteImpactCaller;
import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets={"net.gqstavo.armamentarium.entity.MiteArrowEntity"}, remap=false)
public abstract class NativeMiteCallerMixin
implements NativeMiteImpactCaller {
    @Override
    public final void ftboss$miteArrow(LevelAccessor levelAccessor, double d, double d2, double d3, Entity entity) {
        NativeSpecialCaller.mite((AbstractArrow)this, "net.gqstavo.armamentarium.procedures.MiteArrowProjectileHitsLivingEntityProcedure", levelAccessor, d, d2, d3, entity);
    }
}

