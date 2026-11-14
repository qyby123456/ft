package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.ArrowPassiveDamage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 原版及继承弓的发射入口；2026-10-13 在真实发射物加入世界前绑定副手/主手材料，耗尽不能松弦攻击。 */
@Mixin(value = BowItem.class, remap = false)
public abstract class FoodWeaponBowMixin {
    @Inject(method = {"releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
            "m_5551_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"},
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ftboss$brokenBow(ItemStack weapon, Level level, LivingEntity caster, int remaining, CallbackInfo callback) {
        if (FoodWeaponLifecycle.isBroken(weapon)) callback.cancel();
    }

    @Group(name = "ftboss$bowPrepare", min = 1, max = 1)
    @Redirect(method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;customArrow(Lnet/minecraft/world/entity/projectile/AbstractArrow;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"),
            require = 0, remap = false)
    private AbstractArrow ftboss$prepareArrow(BowItem bow, AbstractArrow projectile, ItemStack weapon, Level level,
                                               LivingEntity caster, int remaining) {
        return ftboss$prepare(bow, projectile, weapon, caster);
    }

    @Group(name = "ftboss$bowPrepare", min = 1, max = 1)
    @Redirect(method = "m_5551_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;customArrow(Lnet/minecraft/world/entity/projectile/AbstractArrow;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"),
            require = 0, remap = false)
    private AbstractArrow ftboss$prepareArrowSrg(BowItem bow, AbstractArrow projectile, ItemStack weapon, Level level,
                                                  LivingEntity caster, int remaining) {
        return ftboss$prepare(bow, projectile, weapon, caster);
    }

    private AbstractArrow ftboss$prepare(BowItem bow, AbstractArrow projectile, ItemStack weapon, LivingEntity caster) {
        AbstractArrow arrow = ArrowPassiveDamage.apply(bow.customArrow(projectile), weapon);
        if (caster instanceof ServerPlayer player) FoodWeaponRanged.prepareBowShot(arrow, FoodWeaponRanged.capture(player, weapon));
        return arrow;
    }

    @Group(name = "ftboss$bowLaunch", min = 1, max = 1)
    @Redirect(method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            require = 0, remap = false)
    private boolean ftboss$bowLaunch(Level world, Entity projectile, ItemStack weapon, Level level, LivingEntity caster, int remaining) {
        return ftboss$launch(world, projectile, weapon, caster);
    }

    @Group(name = "ftboss$bowLaunch", min = 1, max = 1)
    @Redirect(method = "m_5551_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z"),
            require = 0, remap = false)
    private boolean ftboss$bowLaunchSrg(Level world, Entity projectile, ItemStack weapon, Level level, LivingEntity caster, int remaining) {
        return ftboss$launch(world, projectile, weapon, caster);
    }

    private boolean ftboss$launch(Level level, Entity projectile, ItemStack weapon, LivingEntity caster) {
        FoodWeaponRanged.launchPreparedBow(projectile);
        return level.addFreshEntity(projectile);
    }
}
