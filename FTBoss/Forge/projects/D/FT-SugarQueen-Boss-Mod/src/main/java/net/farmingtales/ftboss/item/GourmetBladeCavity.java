package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.GourmetBladeCavityRules;
import net.farmingtales.ftboss.util.ManorWeaponCavityRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.State;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class GourmetBladeCavity {
    private static final SwordFunctionRules.Values BASE = new SwordFunctionRules.Values(1, 1, 1, 1, 1);

    private GourmetBladeCavity() {}

    public static boolean applies(ItemStack weapon) {
        return identityOnly(weapon) || ManorWeaponCavity.applies(weapon);
    }

    public static boolean identityOnly(ItemStack weapon) {
        return !weapon.isEmpty() && weapon.getCount() == 1 && FoodWeaponEvolution.isGourmetBlankBlade(weapon);
    }

    public static void ensureIdentity(ItemStack weapon) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (!applies(weapon) || server == null || !server.isSameThread()) return;
        var data = weapon.getOrCreateTagElement(FoodWeaponLifecycle.ROOT);
        if (!data.hasUUID("Weapon")) data.putUUID("Weapon", UUID.randomUUID());
    }

    public static boolean hasFunctions(SwordFunctionRules.Profile profile, ItemStack weapon) {
        return profile != null || applies(weapon);
    }

    public static int maximumCells(SwordFunctionRules.Profile profile, ItemStack weapon) {
        return applies(weapon) ? SwordCavityRules.CELL_COUNT : SwordFunctionRules.maximumCells(profile);
    }

    public static boolean fixed(ItemStack weapon, int index) {
        return DiamondAlloyArmorCore.fixed(weapon, index) || applies(weapon) && index == GourmetBladeCavityRules.CORE;
    }

    public static boolean fixedSlot(SwordCavityRules.Slot slot, ItemStack weapon) {
        return slot != null && fixed(weapon, slot.ordinal());
    }

    public static boolean effectiveUnlocked(SwordFunctionRules.Profile profile, int index, int level, ItemStack weapon) {
        return applies(weapon) ? unlocked(weapon, index) : SwordFunctionRules.unlocked(profile, index, level);
    }

    public static boolean compatibleContainer(ItemStack weapon, List<ItemStack> items) {
        if (DiamondAlloyArmorCore.applies(weapon)) {
            int core = DiamondAlloyArmorCore.index(weapon, EquipmentWeaponBuild.parts(weapon));
            return core >= 0 && items != null && items.size() == SwordCavityRules.CELL_COUNT
                    && items.get(core) != null && items.get(core).isEmpty();
        }
        return !applies(weapon) || items != null && items.size() == SwordCavityRules.CELL_COUNT
                && items.get(GourmetBladeCavityRules.CORE) != null && items.get(GourmetBladeCavityRules.CORE).isEmpty();
    }

    public static Component referenceHint(ItemStack weapon, int index) {
        if (DiamondAlloyArmorCore.fixed(weapon, index)) return Component.translatable("screen.ftboss.armor.core_fixed")
                .append(" · ").append(Component.translatable("screen.ftboss.armor.core_base"));
        return fixed(weapon, index) ? Component.translatable(ManorWeaponCavity.applies(weapon)
                ? "screen.ftboss.manor.core_fixed" : "screen.ftboss.gourmet.core_fixed")
                .append(" · ").append(Component.translatable(ManorWeaponCavity.applies(weapon)
                        ? "screen.ftboss.manor.core_base" : "screen.ftboss.gourmet.core_base"))
                : Component.translatable("screen.ftboss.cavity.reference_short");
    }

    public static boolean unlocked(ItemStack weapon, int index) {
        return ManorWeaponCavity.applies(weapon)
                ? ManorWeaponCavityRules.unlocked(index, FoodWeaponEvolution.evolutionLevel(weapon))
                : GourmetBladeCavityRules.unlocked(index, FoodWeaponEvolution.evolutionLevel(weapon));
    }

    public static List<Cell> cells(ItemStack weapon, List<Cell> original) {
        if (DiamondAlloyArmorCore.applies(weapon)) {
            int core = DiamondAlloyArmorCore.index(weapon, EquipmentWeaponBuild.parts(weapon));
            if (core < 0) return original;
            List<Cell> cells = new ArrayList<>(original);
            cells.set(core, new Cell(State.NATIVE, Material.ORIGINAL));
            return List.copyOf(cells);
        }
        if (!applies(weapon)) return original;
        List<Cell> cells = new ArrayList<>(original);
        for (int index = 0; index < cells.size(); index++) {
            if (index == GourmetBladeCavityRules.CORE) cells.set(index, new Cell(State.NATIVE, Material.ORIGINAL));
            else if (cells.get(index).state() == State.NATIVE) cells.set(index, new Cell(State.EMPTY, Material.ORIGINAL));
        }
        return List.copyOf(cells);
    }

    public static SwordFunctionRules.Status functionStatus(ItemStack weapon) {
        boolean valid = SwordCavityRules.usable(SwordCavityBuild.cells(weapon));
        return new SwordFunctionRules.Status(valid, BASE, BASE,
                valid ? List.of() : List.of(SwordFunctionRules.Function.values()),
                ManorWeaponCavity.applies(weapon) ? ManorWeaponCavityRules.capacity(FoodWeaponEvolution.evolutionLevel(weapon))
                        : GourmetBladeCavityRules.capacity(FoodWeaponEvolution.evolutionLevel(weapon)));
    }

    public static ItemStack coreDisplay(ItemStack weapon) {
        if (DiamondAlloyArmorCore.applies(weapon)) return DiamondAlloyArmorCore.display(weapon);
        ItemStack icon = new ItemStack(weapon.getItem());
        icon.setHoverName(Component.translatable(ManorWeaponCavity.applies(weapon)
                ? "item.ftboss.manor_weapon_core" : "item.ftboss.gourmet_core"));
        return icon;
    }
}
