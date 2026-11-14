package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class NativeMaterialCrossbowSupport {
    private NativeMaterialCrossbowSupport() {}

    public static boolean registered(Item item, ItemStack weapon) {
        if (item.getClass() != MaterialCrossbowItem.class || weapon.getItem() != item) return false;
        var id = ForgeRegistries.ITEMS.getKey(item);
        return id != null && id.getNamespace().equals("ftboss") && MaterialCrossbows.IDS.contains(id.getPath());
    }

    public static InteractionResultHolder<ItemStack> use(Item item, ItemStack weapon, Level level, Player player, InteractionHand hand) {
        if (!registered(item, weapon) || FourWeaponUseGuard.blocksNativeSourceAction(weapon)) return InteractionResultHolder.fail(weapon);
        return FoodWeaponRanged.withSource(null, new CompoundTag(), () -> FoodWeaponSourceLedger.nativeUse(item, weapon, level, player, hand));
    }
}
