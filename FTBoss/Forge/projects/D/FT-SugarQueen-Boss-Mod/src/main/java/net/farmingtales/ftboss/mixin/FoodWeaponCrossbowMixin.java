package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 弩材料装填速度与整次齐射快照；2026-10-13 原版先扣耐久后生成箭，故在齐射开始前冻结所有弹丸共用状态。 */
@Mixin(value = CrossbowItem.class, remap = false)
public abstract class FoodWeaponCrossbowMixin {
    @Unique
    private static final ThreadLocal<CompoundTag> FTBOSS_VOLLEY = new ThreadLocal<>();

    @Inject(method = {"getChargeDuration(Lnet/minecraft/world/item/ItemStack;)I", "m_40939_(Lnet/minecraft/world/item/ItemStack;)I"},
            at = @At("RETURN"), cancellable = true, remap = false)
    private static void ftboss$chargeTime(ItemStack weapon, CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(FoodWeaponFamilyBuild.chargeTicks(weapon, callback.getReturnValue()));
    }

    @Inject(method = {"performShooting(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V",
            "m_40887_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V"},
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void ftboss$beginVolley(Level level, LivingEntity caster, InteractionHand hand, ItemStack weapon,
                                           float velocity, float inaccuracy, CallbackInfo callback) {
        FTBOSS_VOLLEY.remove();
        if (FoodWeaponLifecycle.isBroken(weapon)) {
            callback.cancel();
            return;
        }
        if (caster instanceof ServerPlayer player) FTBOSS_VOLLEY.set(FoodWeaponRanged.capture(player, weapon));
    }

    @Inject(method = {"performShooting(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V",
            "m_40887_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V"},
            at = @At("RETURN"), remap = false)
    private static void ftboss$endVolley(Level level, LivingEntity caster, InteractionHand hand, ItemStack weapon,
                                         float velocity, float inaccuracy, CallbackInfo callback) {
        FTBOSS_VOLLEY.remove();
    }

    @Group(name = "ftboss$crossbowLaunch", min = 1, max = 1)
    @Redirect(method = "shootProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;FZFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            require = 0, remap = false)
    private static boolean ftboss$crossbowLaunch(Level level, Entity projectile) {
        return ftboss$launch(level, projectile);
    }

    @Group(name = "ftboss$crossbowLaunch", min = 1, max = 1)
    @Redirect(method = "m_40894_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;FZFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z"),
            require = 0, remap = false)
    private static boolean ftboss$crossbowLaunchSrg(Level level, Entity projectile) {
        return ftboss$launch(level, projectile);
    }

    private static boolean ftboss$launch(Level level, Entity projectile) {
        CompoundTag snapshot = FTBOSS_VOLLEY.get();
        if (snapshot != null) FoodWeaponRanged.launch(projectile, snapshot);
        return level.addFreshEntity(projectile);
    }
}
