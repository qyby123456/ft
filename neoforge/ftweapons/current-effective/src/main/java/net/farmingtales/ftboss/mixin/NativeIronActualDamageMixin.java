package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(
    targets = {"io.redspace.ironsspellbooks.damage.DamageSources"},
    remap = false
)
public abstract class NativeIronActualDamageMixin {
    @Redirect(
        method = {"applyDamage(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)Z"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        ),
        require = 1,
        remap = false
    )
    private static boolean ftboss$v10$ironLivingHurt(LivingEntity var0, DamageSource var1, float var2) {
        return FoodWeaponSourceLedger.nativeIronActualHurt(var0, var1, var2);
    }

    @Redirect(
        method = {"applyDamage(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)Z"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        ),
        require = 1,
        remap = false
    )
    private static boolean ftboss$v10$ironEntityHurt(Entity var0, DamageSource var1, float var2) {
        return FoodWeaponSourceLedger.nativeIronActualHurt(var0, var1, var2);
    }
}
