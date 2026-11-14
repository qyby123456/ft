package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialDefinition;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.SwordCavityRules.Link;
import net.farmingtales.ftboss.util.WeaponAccessoryLinkRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.world.item.ItemStack;

/** 四武器羁绊桥；2026-10-22 统一实际构筑、公开查询与界面，原生参考和锁定格不参与激活。 */
public final class FoundationAccessoryBonds {
    private FoundationAccessoryBonds() {}

    public static List<ItemStack> paid(ItemStack weapon) {
        var view = FourWeaponFoundationAdapter.read(weapon);
        if (!view.valid() || !view.status().ready()) return List.of();
        List<ItemStack> paid = new ArrayList<>();
        view.entries().forEach((cell, entry) -> {
            if (entry.paid() && FourWeaponFoundationRules.unlocked(view.profile(), cell, view.level())) paid.add(entry.storedItem());
        });
        return List.copyOf(paid);
    }

    public static List<SwordMaterialDefinition> installed(ItemStack weapon) {
        return paid(weapon).stream().map(SwordMaterialExtensions::resolve).flatMap(java.util.Optional::stream).toList();
    }

    public static List<Link> links(ItemStack weapon) {
        return WeaponAccessoryLinkRules.links(FourWeaponFoundationAdapter.powerMaterials(weapon), true);
    }

    public static Build apply(ItemStack weapon, Build base) {
        return WeaponAccessoryLinkRules.apply(base, FourWeaponFoundationAdapter.powerMaterials(weapon), true);
    }

    public static List<SwordCavityExtensions.BondDefinition> cavityBonds(ItemStack weapon) {
        return SwordCavityExtensions.activeBonds(weapon, paid(weapon), true);
    }

    public static List<SwordMaterialExtensions.BondDefinition> materialBonds(ItemStack weapon) {
        return SwordMaterialExtensions.activeBonds(weapon, paid(weapon), true);
    }
}
