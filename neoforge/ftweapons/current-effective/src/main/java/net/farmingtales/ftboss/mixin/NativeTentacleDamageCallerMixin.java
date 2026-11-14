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
    targets = {"com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity"},
    remap = false
)
public abstract class NativeTentacleDamageCallerMixin {
    @Redirect(
        method = {"tick"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        ),
        require = 0,
        remap = false
    )
    private boolean ftboss$tentacleNativeHurt(Entity var1, DamageSource var2, float var3) {
        return NativeSpecialCaller.delayedHurt((Entity)(Object)this, var1, var2, var3);
    }
}
