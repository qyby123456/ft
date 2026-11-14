/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BowItem.class}, remap=false)
public abstract class FoodWeaponBowMixin {
    @Inject(method={"releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V", "m_5551_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void ftboss$brokenBow(ItemStack itemStack, Level level, LivingEntity livingEntity, int n, CallbackInfo callbackInfo) {
        if (FoodWeaponLifecycle.isBroken(itemStack)) {
            callbackInfo.cancel();
        }
    }

    private AbstractArrow ftboss$prepareArrow(BowItem bowItem, AbstractArrow abstractArrow, ItemStack itemStack, Level level, LivingEntity livingEntity, int n) {
        return this.ftboss$prepare(bowItem, this.ftboss$v6$factory(bowItem, abstractArrow, itemStack, level, livingEntity, n), itemStack, livingEntity);
    }

    private AbstractArrow ftboss$prepareArrowSrg(BowItem bowItem, AbstractArrow abstractArrow, ItemStack itemStack, Level level, LivingEntity livingEntity, int n) {
        return this.ftboss$prepare(bowItem, this.ftboss$v6$factory(bowItem, abstractArrow, itemStack, level, livingEntity, n), itemStack, livingEntity);
    }

    private AbstractArrow ftboss$prepare(BowItem bowItem, AbstractArrow abstractArrow, ItemStack itemStack, LivingEntity livingEntity) {
        AbstractArrow abstractArrow2 = abstractArrow;
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            FoodWeaponRanged.prepareBowShot((Entity)abstractArrow2, FoodWeaponRanged.capture(serverPlayer, itemStack));
        }
        return abstractArrow2;
    }

    private boolean ftboss$bowLaunch(Level level, Entity entity, ItemStack itemStack, Level level2, LivingEntity livingEntity, int n) {
        return this.ftboss$launch(level, entity, itemStack, livingEntity);
    }

    private boolean ftboss$bowLaunchSrg(Level level, Entity entity, ItemStack itemStack, Level level2, LivingEntity livingEntity, int n) {
        return this.ftboss$launch(level, entity, itemStack, livingEntity);
    }

    private boolean ftboss$launch(Level level, Entity entity, ItemStack itemStack, LivingEntity livingEntity) {
        FoodWeaponRanged.launchPreparedBow(entity);
        return this.ftboss$v6$add(level, entity);
    }

    private AbstractArrow ftboss$v6$factory(BowItem bowItem, AbstractArrow abstractArrow, ItemStack itemStack, Level level, LivingEntity livingEntity, int n) {
        return FoodWeaponSourceLedger.nativeBowArrow(bowItem, abstractArrow, itemStack);
    }

    private boolean ftboss$v6$add(Level level, Entity entity) {
        return FoodWeaponSourceLedger.nativeAdd(level, entity);
    }
}

