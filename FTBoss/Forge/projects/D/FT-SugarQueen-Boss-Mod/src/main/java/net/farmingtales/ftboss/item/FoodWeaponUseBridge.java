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
import net.minecraft.world.level.Level;

public final class FoodWeaponUseBridge {
    private FoodWeaponUseBridge() {}

    public static Kind kind(ItemStack weapon) { return NativeWeaponTypes.kind(weapon); }

    private static <Result> Result invoke(ItemStack weapon, Level level, LivingEntity caster, InteractionHand hand, Supplier<Result> action, Result blocked) {
        if (FourWeaponUseGuard.blocksNativeSourceAction(weapon)) return blocked;
        if (weapon.getItem() instanceof MaterialCrossbowItem || weapon.getItem() instanceof MaterialBowItem) return action.get();
        Kind family = kind(weapon);
        if (family != Kind.MELEE && FoodWeaponLifecycle.isBroken(weapon)) return blocked;
        ServerPlayer player = !level.isClientSide && caster instanceof ServerPlayer owner ? owner : null;
        CompoundTag snapshot = player == null ? new CompoundTag() : FoodWeaponRanged.captureBound(player, weapon, family, hand);
        return FoodWeaponRanged.withSource(player, snapshot, action);
    }

    public static InteractionResultHolder<ItemStack> use(Item item, ItemStack weapon, Level level, Player player, InteractionHand hand) {
        if (NativeMaterialCrossbowSupport.registered(item, weapon)) return NativeMaterialCrossbowSupport.use(item, weapon, level, player, hand);
        if (FourWeaponUseGuard.blocksNativeSourceAction(weapon)) return InteractionResultHolder.fail(weapon);
        if (item.getClass() == CrossbowItem.class) return FoodWeaponRanged.withSource(null, new CompoundTag(),
                () -> FoodWeaponSourceLedger.nativeUse(item, weapon, level, player, hand));
        return invoke(weapon, level, player, hand, () -> NativeEvergaleBridge.use(item, weapon, level, player, hand), InteractionResultHolder.fail(weapon));
    }

    public static void release(Item item, ItemStack weapon, Level level, LivingEntity caster, int remaining) {
        if (FourWeaponUseGuard.blocksNativeSourceAction(weapon)) return;
        if (NativeMaterialBowSupport.registered(item, weapon)) {
            NativeMaterialBowSupport.release(item, weapon, level, caster, remaining);
            return;
        }
        if (item.getClass() == BowItem.class || item.getClass() == net.minecraft.world.item.TridentItem.class) {
            FoodWeaponRanged.withSource(null, new CompoundTag(), () -> { FoodWeaponSourceLedger.nativeRelease(item, weapon, level, caster, remaining); return null; });
            return;
        }
        if (!level.isClientSide && caster instanceof ServerPlayer player && FoodWeaponSourceLedger.isTrident(weapon)) {
            FoodWeaponSourceLedger.releaseTrident(player, weapon, caster.getUsedItemHand(),
                    () -> item.releaseUsing(weapon, level, caster, remaining));
            return;
        }
        invoke(weapon, level, caster, caster.getUsedItemHand(), () -> {
            item.releaseUsing(weapon, level, caster, remaining);
            return null;
        }, null);
    }

    public static void useTick(Item item, ItemStack weapon, Level level, LivingEntity caster, int remaining) {
        invoke(weapon, level, caster, caster.getUsedItemHand(), () -> {
            item.onUseTick(level, caster, weapon, remaining);
            return null;
        }, null);
    }

    public static ItemStack finish(Item item, ItemStack weapon, Level level, LivingEntity caster) {
        return invoke(weapon, level, caster, caster.getUsedItemHand(), () -> item.finishUsingItem(weapon, level, caster), weapon);
    }
}
