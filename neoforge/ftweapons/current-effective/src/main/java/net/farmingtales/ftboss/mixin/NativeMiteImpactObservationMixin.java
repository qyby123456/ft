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
@Mixin(
    targets = {"net.gqstavo.armamentarium.entity.MiteArrowEntity"},
    remap = false
)
public abstract class NativeMiteImpactObservationMixin {
    @Redirect(
        method = {"onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/gqstavo/armamentarium/procedures/MiteArrowProjectileHitsLivingEntityProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V"
        ),
        require = 1,
        expect = 1,
        allow = 1,
        remap = false
    )
    private void nativeMiteImpact(LevelAccessor var1, double var2, double var4, double var6, Entity var8) {
        NativeActionObservation.impact((Entity)(Object)this, var8, () -> ((NativeMiteImpactCaller)(Object)this).ftboss$miteArrow(var1, var2, var4, var6, var8));
    }
}
