package net.farmingtales.ftboss.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.WeaponCavityShapeRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class WeaponCavityMenuRules {
    private WeaponCavityMenuRules() {}

    public static String shapeFamily(ItemStack weapon) {
        if (net.farmingtales.ftboss.item.EquipmentWeaponBuild.applies(weapon)) return net.farmingtales.ftboss.item.EquipmentWeaponBuild.family(weapon);
        if (FourWeaponFoundationAdapter.applies(weapon)) {
            var profile = FourWeaponFoundationAdapter.profile(weapon);
            return profile == null ? "sword" : profile.family();
        }
        String identifier = BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString();
        var definition = SwordCompatibilityRules.definitions().get(identifier);
        return identifier.equals("minecraft:stone_sword") ? "stone_sword"
                : definition != null && definition.form() == SwordCompatibilityRules.Form.AXE ? "axe" : "sword";
    }

    public static int[] layout(int[] original, ItemStack weapon) {
        if (net.farmingtales.ftboss.item.EquipmentWeaponBuild.applies(weapon)) return net.farmingtales.ftboss.item.EquipmentWeaponBuild.layout(weapon);
        int[] validated = SwordCavityExtensions.validateLayout(original);
        int[] defaults = new int[SwordCavityRules.CELL_COUNT];
        Arrays.setAll(defaults, index -> SwordNativeMaterialRules.visualRow(index) * SwordCavityRules.COLUMNS
                + SwordNativeMaterialRules.visualColumn(index));
        if (!Arrays.equals(validated, defaults)) return validated;
        List<String> items = new ArrayList<>(Collections.nCopies(SwordCavityRules.CELL_COUNT, ""));
        List<Integer> unlocks = new ArrayList<>(Collections.nCopies(SwordCavityRules.CELL_COUNT, 0));
        String family;
        if (FourWeaponFoundationAdapter.applies(weapon)) {
            var profile = FourWeaponFoundationAdapter.profile(weapon);
            if (profile == null) return validated;
            family = profile.family();
            profile.nativeCells().forEach((index, cell) -> items.set(index, cell.itemId()));
            unlocks = profile.unlocks();
        } else {
            if (!SwordCavityBuild.applies(weapon)) return validated;
            family = shapeFamily(weapon);
            List<SwordNativeMaterialRules.Piece> parts = SwordNativeMaterials.parts(weapon);
            if (parts.size() != SwordCavityRules.CELL_COUNT) return validated;
            for (int index = 0; index < parts.size(); index++) items.set(index, parts.get(index).itemId());
        }
        return SwordCavityExtensions.validateLayout(WeaponCavityShapeRules.layout(family, items, unlocks));
    }

    public static boolean applies(ItemStack weapon) {
        return FourWeaponFoundationAdapter.applies(weapon) || SwordCavityBuild.applies(weapon);
    }

    public static int revision(ItemStack weapon) {
        return FourWeaponFoundationAdapter.applies(weapon) ? FourWeaponFoundationAdapter.revision(weapon) : SwordCavityBuild.revision(weapon);
    }

    public static List<ItemStack> containerItems(ItemStack weapon) {
        return FourWeaponFoundationAdapter.applies(weapon) ? FourWeaponFoundationAdapter.slots(weapon) : SwordCavityBuild.containerItems(weapon);
    }

    public static boolean containerEditable(ItemStack weapon) {
        return FourWeaponFoundationAdapter.applies(weapon) ? FourWeaponFoundationAdapter.containerEditable(weapon) : SwordCavityBuild.containerEditable(weapon);
    }

    public static boolean canSetContainerItems(ItemStack weapon, List<ItemStack> items) {
        return FourWeaponFoundationAdapter.applies(weapon) ? FourWeaponFoundationAdapter.canSetContainerItems(weapon, items) : SwordCavityBuild.canSetContainerItems(weapon, items);
    }

    public static boolean setContainerItems(ServerPlayer player, ItemStack weapon, List<ItemStack> items, int revision) {
        return FourWeaponFoundationAdapter.applies(weapon) ? FourWeaponFoundationAdapter.setContainerItems(player, weapon, items, revision)
                : SwordCavityBuild.setContainerItems(player, weapon, items, revision);
    }

    public static boolean reference(ItemStack weapon, int index) {
        if (index < 0 || index >= SwordCavityRules.CELL_COUNT) return false;
        return FourWeaponFoundationAdapter.applies(weapon) ? FourWeaponFoundationAdapter.reference(weapon, index)
                : SwordCavityBuild.applies(weapon) && SwordCavityBuild.cells(weapon).get(index).present();
    }

    public static boolean clearReference(ServerPlayer player, ItemStack weapon, SwordCavityRules.Slot slot, int revision) {
        return FourWeaponFoundationAdapter.applies(weapon) ? FourWeaponFoundationAdapter.replace(player, weapon, slot.ordinal(), -1, revision)
                : SwordCavityBuild.clearReference(player, weapon, slot, revision);
    }

    public static boolean cellUnlocked(ItemStack weapon, int index) {
        return FourWeaponFoundationAdapter.applies(weapon) ? FourWeaponFoundationAdapter.cellUnlocked(weapon, index) : SwordCavityBuild.cellUnlocked(weapon, index);
    }
}
