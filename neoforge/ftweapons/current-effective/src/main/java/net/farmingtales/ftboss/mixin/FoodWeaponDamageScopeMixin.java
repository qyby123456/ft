package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.NativeActionObservation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {LivingEntity.class, Player.class}, remap = false)
public abstract class FoodWeaponDamageScopeMixin {
    @Redirect(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/CommonHooks;onEntityIncomingDamage(Lnet/minecraft/world/entity/LivingEntity;Lnet/neoforged/neoforge/common/damagesource/DamageContainer;)Z"), require = 0, remap = false)
    private boolean ftboss$incomingDamage(LivingEntity target, DamageContainer container) {
        return FoodWeaponRanged.withHitSource(target, container.getSource(), () -> CommonHooks.onEntityIncomingDamage(target, container));
    }

    @Redirect(method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/CommonHooks;onLivingDamagePre(Lnet/minecraft/world/entity/LivingEntity;Lnet/neoforged/neoforge/common/damagesource/DamageContainer;)F"), require = 1, expect = 1, allow = 1, remap = false)
    private float nativeFinalDamage(LivingEntity target, DamageContainer container) {
        return FoodWeaponRanged.withHitSource(target, container.getSource(), () -> {
            float damage = CommonHooks.onLivingDamagePre(target, container);
            NativeActionObservation.finalDamage(target, container.getSource(), damage);
            return damage;
        });
    }
}
