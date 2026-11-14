package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeEvergaleImpact;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
    value = {AbstractArrow.class},
    remap = false
)
public abstract class NativeVanillaArrowImpactMixin {
    @Redirect(
        method = {"onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            remap = false
        ),
        require = 1,
        expect = 1,
        allow = 1,
        remap = false
    )
    private boolean ftboss$nativeVanillaArrowRawHurt(Entity var1, DamageSource var2, float var3) {
        return NativeEvergaleImpact.arrowHurt((AbstractArrow)(Object)this, var1, var2, var3);
    }
}
