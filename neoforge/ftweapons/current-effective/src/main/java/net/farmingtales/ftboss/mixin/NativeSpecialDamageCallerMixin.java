package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(
    targets = {"com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity"},
    remap = false
)
public abstract class NativeSpecialDamageCallerMixin {
    @Redirect(
        method = {"damage"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        ),
        require = 0,
        remap = false
    )
    private boolean ftboss$halberdNativeHurt(LivingEntity var1, DamageSource var2, float var3) {
        return NativeSpecialCaller.delayedHurt((Entity)(Object)this, var1, var2, var3);
    }
}
