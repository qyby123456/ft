package net.farmingtales.ftboss.item;

import java.util.function.Supplier;
import net.farmingtales.ftboss.item.weapons.MaterialBowItem;
import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
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

    public static Kind kind(ItemStack var0) {
        return NativeWeaponTypes.kind(var0);
    }

    private static <Result> Result invoke(ItemStack var0, Level var1, LivingEntity var2, InteractionHand var3, Supplier<Result> var4, Result var5) {
        if (FourWeaponUseGuard.blocksNativeSourceAction(var0)) {
            return (Result)var5;
        } else if (!(var0.getItem() instanceof MaterialCrossbowItem) && !(var0.getItem() instanceof MaterialBowItem)) {
            Kind var6 = kind(var0);
            if (var6 != Kind.MELEE && FoodWeaponLifecycle.isBroken(var0)) {
                return (Result)var5;
            } else {
                ServerPlayer var7 = !var1.isClientSide && var2 instanceof ServerPlayer var8 ? var8 : null;
                CompoundTag var9 = var7 == null || var0.getItem().getClass() == BowItem.class ? new CompoundTag() : FoodWeaponRanged.captureBound(var7, var0, var6, var3);
                return FoodWeaponRanged.withSource(var7, var9, var4);
            }
        } else {
            return (Result)var4.get();
        }
    }

    public static InteractionResultHolder<ItemStack> use(Item var0, ItemStack var1, Level var2, Player var3, InteractionHand var4) {
        if (FourWeaponUseGuard.blocksNativeSourceAction(var1)) {
            return InteractionResultHolder.fail(var1);
        } else {
            if (FoodWeaponSourceLedger.nativeFrostGateAllowed(var1)) return FoodWeaponSourceLedger.nativeFrostOriginalUse(var1, var0, var2, var3, var4,
                arguments -> NativeEvergaleBridge.use(var0, var1, var2, var3, var4));
            return var0.getClass() == CrossbowItem.class
                || var0.getClass() == MaterialCrossbowItem.class && OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(var1)
                ? FoodWeaponRanged.withSource(null, new CompoundTag(), () -> FoodWeaponSourceLedger.nativeUse(var0, var1, var2, var3, var4))
                : invoke(var1, var2, var3, var4, () -> NativeEvergaleBridge.use(var0, var1, var2, var3, var4), InteractionResultHolder.fail(var1));
        }
    }

    public static void release(Item var0, ItemStack var1, Level var2, LivingEntity var3, int var4) {
        if (!FourWeaponUseGuard.blocksNativeSourceAction(var1)) {
            if (var0.getClass() != BowItem.class && var0.getClass() != TridentItem.class
                && !(var0.getClass() == MaterialBowItem.class && OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(var1))) {
                if (!var2.isClientSide && var3 instanceof ServerPlayer var5 && FoodWeaponSourceLedger.isTrident(var1)) {
                    FoodWeaponSourceLedger.releaseTrident(var5, var1, var3.getUsedItemHand(), () -> var0.releaseUsing(var1, var2, var3, var4));
                    return;
                }

                invoke(var1, var2, var3, var3.getUsedItemHand(), () -> {
                    var0.releaseUsing(var1, var2, var3, var4);
                    return null;
                }, null);
            } else {
                FoodWeaponRanged.withSource(null, new CompoundTag(), () -> {
                    FoodWeaponSourceLedger.nativeRelease(var0, var1, var2, var3, var4);
                    return null;
                });
            }
        }
    }

    public static void useTick(Item var0, ItemStack var1, Level var2, LivingEntity var3, int var4) {
        invoke(var1, var2, var3, var3.getUsedItemHand(), () -> {
            var0.onUseTick(var2, var3, var1, var4);
            return null;
        }, null);
    }

    public static ItemStack finish(Item var0, ItemStack var1, Level var2, LivingEntity var3) {
        return invoke(var1, var2, var3, var3.getUsedItemHand(), () -> var0.finishUsingItem(var1, var2, var3), var1);
    }
}

