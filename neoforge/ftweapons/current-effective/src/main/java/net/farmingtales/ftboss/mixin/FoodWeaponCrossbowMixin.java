package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {CrossbowItem.class},
    remap = false
)
public abstract class FoodWeaponCrossbowMixin {
    @Unique
    private static final ThreadLocal<CompoundTag> FTBOSS_VOLLEY = new ThreadLocal<>();

    @Inject(
        method = {"getChargeDuration(Lnet/minecraft/world/item/ItemStack;)I", "m_40939_(Lnet/minecraft/world/item/ItemStack;)I"},
        at = {@At("RETURN")},
        cancellable = true,
        remap = false
    )
    private static void ftboss$chargeTime(ItemStack var0, CallbackInfoReturnable<Integer> var1) {
        var1.setReturnValue(FoodWeaponFamilyBuild.chargeTicks(var0, (Integer)var1.getReturnValue()));
    }

    @Inject(
        method = {"performShooting(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V", "m_40887_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private static void ftboss$beginVolley(Level var0, LivingEntity var1, InteractionHand var2, ItemStack var3, float var4, float var5, CallbackInfo var6) {
        FTBOSS_VOLLEY.remove();
        if (FoodWeaponLifecycle.isBroken(var3)) {
            var6.cancel();
        } else {
            if (var1 instanceof ServerPlayer var7) {
                FTBOSS_VOLLEY.set(FoodWeaponRanged.capture(var7, var3));
            }
        }
    }

    @Inject(
        method = {"performShooting(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V", "m_40887_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V"},
        at = {@At("RETURN")},
        remap = false
    )
    private static void ftboss$endVolley(Level var0, LivingEntity var1, InteractionHand var2, ItemStack var3, float var4, float var5, CallbackInfo var6) {
        FTBOSS_VOLLEY.remove();
    }

}
