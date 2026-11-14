package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.weapons.MaterialBowItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 材料弓原生箭矢来源；2026-10-22 仅放行注册匹配的六把弓，继续沿用扣费、归属和命中凭据链。 */
public final class NativeMaterialBowSupport {
    private NativeMaterialBowSupport() {}

    public static boolean registered(Item item, ItemStack weapon) {
        return item.getClass() == MaterialBowItem.class && weapon.getItem() == item
                && ((MaterialBowItem) item).kind().id().toString().equals(FourWeaponFoundationAdapter.registryId(weapon));
    }

    public static void release(Item item, ItemStack weapon, Level level, LivingEntity caster, int remaining) {
        if (!registered(item, weapon) || FourWeaponUseGuard.blocksNativeSourceAction(weapon)) return;
        FoodWeaponRanged.withSource(null, new CompoundTag(), () -> {
            FoodWeaponSourceLedger.nativeRelease(item, weapon, level, caster, remaining);
            return null;
        });
    }
}
