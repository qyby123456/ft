/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TridentItem
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.item;

import java.util.function.Supplier;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FourWeaponUseGuard;
import net.farmingtales.ftboss.item.NativeEvergaleBridge;
import net.farmingtales.ftboss.item.NativeWeaponTypes;
import net.farmingtales.ftboss.item.weapons.MaterialBowItem;
import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;

public final class FoodWeaponUseBridge {
    private FoodWeaponUseBridge() {
    }

    public static FoodWeaponFamilyRules.Kind kind(ItemStack itemStack) {
        return NativeWeaponTypes.kind(itemStack);
    }

    private static <Result> Result invoke(ItemStack itemStack, Level level, LivingEntity livingEntity, InteractionHand interactionHand, Supplier<Result> supplier, Result Result2) {
        ServerPlayer serverPlayer;
        if (FourWeaponUseGuard.blocksNativeSourceAction(itemStack)) {
            return Result2;
        }
        if (itemStack.m_41720_() instanceof MaterialCrossbowItem || itemStack.m_41720_() instanceof MaterialBowItem) {
            return supplier.get();
        }
        FoodWeaponFamilyRules.Kind kind = FoodWeaponUseBridge.kind(itemStack);
        if (kind != FoodWeaponFamilyRules.Kind.MELEE && FoodWeaponLifecycle.isBroken(itemStack)) {
            return Result2;
        }
        ServerPlayer serverPlayer2 = !level.f_46443_ && livingEntity instanceof ServerPlayer ? (serverPlayer = (ServerPlayer)livingEntity) : null;
        serverPlayer = serverPlayer2 == null ? new CompoundTag() : FoodWeaponRanged.captureBound(serverPlayer2, itemStack, kind, interactionHand);
        return FoodWeaponRanged.withSource(serverPlayer2, (CompoundTag)serverPlayer, supplier);
    }

    public static InteractionResultHolder<ItemStack> use(Item item, ItemStack itemStack, Level level, Player player, InteractionHand interactionHand) {
        if (FourWeaponUseGuard.blocksNativeSourceAction(itemStack)) {
            return InteractionResultHolder.m_19100_((Object)itemStack);
        }
        if (item.getClass() == CrossbowItem.class) {
            return FoodWeaponRanged.withSource(null, new CompoundTag(), () -> FoodWeaponSourceLedger.nativeUse(item, itemStack, level, player, interactionHand));
        }
        return FoodWeaponUseBridge.invoke(itemStack, level, (LivingEntity)player, interactionHand, () -> NativeEvergaleBridge.use(item, itemStack, level, player, interactionHand), InteractionResultHolder.m_19100_((Object)itemStack));
    }

    public static void release(Item item, ItemStack itemStack, Level level, LivingEntity livingEntity, int n) {
        if (FourWeaponUseGuard.blocksNativeSourceAction(itemStack)) {
            return;
        }
        if (item.getClass() == BowItem.class || item.getClass() == TridentItem.class) {
            FoodWeaponRanged.withSource(null, new CompoundTag(), () -> {
                FoodWeaponSourceLedger.nativeRelease(item, itemStack, level, livingEntity, n);
                return null;
            });
            return;
        }
        if (!level.f_46443_ && livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            if (FoodWeaponSourceLedger.isTrident(itemStack)) {
                FoodWeaponSourceLedger.releaseTrident(serverPlayer, itemStack, livingEntity.m_7655_(), () -> item.m_5551_(itemStack, level, livingEntity, n));
                return;
            }
        }
        FoodWeaponUseBridge.invoke(itemStack, level, livingEntity, livingEntity.m_7655_(), () -> {
            item.m_5551_(itemStack, level, livingEntity, n);
            return null;
        }, null);
    }

    public static void useTick(Item item, ItemStack itemStack, Level level, LivingEntity livingEntity, int n) {
        FoodWeaponUseBridge.invoke(itemStack, level, livingEntity, livingEntity.m_7655_(), () -> {
            item.m_5929_(level, livingEntity, itemStack, n);
            return null;
        }, null);
    }

    public static ItemStack finish(Item item, ItemStack itemStack, Level level, LivingEntity livingEntity) {
        return FoodWeaponUseBridge.invoke(itemStack, level, livingEntity, livingEntity.m_7655_(), () -> item.m_5922_(itemStack, level, livingEntity), itemStack);
    }
}

