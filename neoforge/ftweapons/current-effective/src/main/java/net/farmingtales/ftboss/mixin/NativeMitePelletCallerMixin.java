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
@Mixin(
    targets = {"net.gqstavo.armamentarium.entity.MitePelletEntity"},
    remap = false
)
public abstract class NativeMitePelletCallerMixin {
    @Redirect(
        method = {"onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/gqstavo/armamentarium/procedures/MitePelletProjectileHitsLivingEntityProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V"
        ),
        require = 0,
        remap = false
    )
    private void ftboss$mitePellet(LevelAccessor var1, double var2, double var4, double var6, Entity var8) {
        NativeSpecialCaller.mite(
            (AbstractArrow)(Object)this, "net.gqstavo.armamentarium.procedures.MitePelletProjectileHitsLivingEntityProcedure", var1, var2, var4, var6, var8
        );
    }
}
