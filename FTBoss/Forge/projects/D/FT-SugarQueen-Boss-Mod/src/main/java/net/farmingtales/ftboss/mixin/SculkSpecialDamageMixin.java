package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.boss.ai.SculkSpecialDamage;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 可选森罗酒馆兼容；2026-10-28：仅标记幽匿特调的伤害调用，支持开发与正式服方法名。 */
@Pseudo
@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopetavern.effect.ShriekAttackEffect", remap = false)
public abstract class SculkSpecialDamageMixin {
    @Group(name = "ftboss$sculkSpecialDamage", min = 1, max = 1)
    @Redirect(method = "performEffect(Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            require = 0, remap = false)
    private boolean ftboss$sculkSpecialDamage(LivingEntity target, DamageSource source, float amount) {
        return SculkSpecialDamage.hurt(target, source, amount);
    }

    @Group(name = "ftboss$sculkSpecialDamage", min = 1, max = 1)
    @Redirect(method = "performEffect(Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            require = 0, remap = false)
    private boolean ftboss$sculkSpecialDamageSrg(LivingEntity target, DamageSource source, float amount) {
        return SculkSpecialDamage.hurt(target, source, amount);
    }
}
