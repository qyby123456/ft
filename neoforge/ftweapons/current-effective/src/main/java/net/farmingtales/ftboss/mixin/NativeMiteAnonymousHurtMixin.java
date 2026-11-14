package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(
    targets = {"net.gqstavo.armamentarium.procedures.MiteArrowProjectileHitsLivingEntityProcedure", "net.gqstavo.armamentarium.procedures.MitePelletProjectileHitsLivingEntityProcedure"},
    remap = false
)
public abstract class NativeMiteAnonymousHurtMixin {
    @Redirect(
        method = {"execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        ),
        require = 0,
        remap = false
    )
    private static boolean ftboss$exactMiteHurt(Entity var0, DamageSource var1, float var2) {
        return NativeSpecialCaller.miteHurt(var0, var1, var2);
    }
}
