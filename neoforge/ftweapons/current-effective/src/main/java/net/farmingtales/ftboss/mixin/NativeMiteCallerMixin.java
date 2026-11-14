package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeMiteImpactCaller;
import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(
    targets = {"net.gqstavo.armamentarium.entity.MiteArrowEntity"},
    remap = false
)
public abstract class NativeMiteCallerMixin implements NativeMiteImpactCaller {
    @Override
    public final void ftboss$miteArrow(LevelAccessor var1, double var2, double var4, double var6, Entity var8) {
        NativeSpecialCaller.mite(
            (AbstractArrow)(Object)this, "net.gqstavo.armamentarium.procedures.MiteArrowProjectileHitsLivingEntityProcedure", var1, var2, var4, var6, var8
        );
    }
}
