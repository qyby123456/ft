/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
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

@Mixin(value={CrossbowItem.class}, remap=false)
public abstract class FoodWeaponCrossbowMixin {
    @Unique
    private static final ThreadLocal<CompoundTag> FTBOSS_VOLLEY = new ThreadLocal();

    @Inject(method={"getChargeDuration(Lnet/minecraft/world/item/ItemStack;)I", "m_40939_(Lnet/minecraft/world/item/ItemStack;)I"}, at={@At(value="RETURN")}, cancellable=true, remap=false)
    private static void ftboss$chargeTime(ItemStack itemStack, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue((Object)FoodWeaponFamilyBuild.chargeTicks(itemStack, (Integer)callbackInfoReturnable.getReturnValue()));
    }

    @Inject(method={"performShooting(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V", "m_40887_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private static void ftboss$beginVolley(Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, float f, float f2, CallbackInfo callbackInfo) {
        FTBOSS_VOLLEY.remove();
        if (FoodWeaponLifecycle.isBroken(itemStack)) {
            callbackInfo.cancel();
            return;
        }
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            FTBOSS_VOLLEY.set(FoodWeaponRanged.capture(serverPlayer, itemStack));
        }
    }

    @Inject(method={"performShooting(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V", "m_40887_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V"}, at={@At(value="RETURN")}, remap=false)
    private static void ftboss$endVolley(Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, float f, float f2, CallbackInfo callbackInfo) {
        FTBOSS_VOLLEY.remove();
    }

    private static boolean ftboss$crossbowLaunch(Level level, Entity entity) {
        return FoodWeaponCrossbowMixin.ftboss$launch(level, entity);
    }

    private static boolean ftboss$crossbowLaunchSrg(Level level, Entity entity) {
        return FoodWeaponCrossbowMixin.ftboss$launch(level, entity);
    }

    private static boolean ftboss$launch(Level level, Entity entity) {
        CompoundTag compoundTag = FTBOSS_VOLLEY.get();
        if (compoundTag != null) {
            FoodWeaponRanged.launch(entity, compoundTag);
        }
        return FoodWeaponCrossbowMixin.ftboss$v6$add(level, entity);
    }

    private static boolean ftboss$v6$add(Level level, Entity entity) {
        return FoodWeaponSourceLedger.nativeAdd(level, entity);
    }
}

