package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordCavityRules.State;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.minecraft.world.item.ItemStack;

public final class SwordCavityBuild {
    private static List<Cell> effectiveCells(ItemStack stack) {
        List<Cell> source = new ArrayList<>(cells(stack));
        boolean applicable = applies(stack);
        SwordFunctionRules.Profile profile = applicable ? SwordCavityExtensions.functionProfile(stack) : null;
        int level = applicable ? FoodWeaponEvolution.evolutionLevel(stack) : 0;
        for (Slot slot : Slot.values()) if (!applicable || !SwordFunctionRules.unlocked(profile, slot.ordinal(), level)) {
            source.set(slot.ordinal(), new Cell(State.EMPTY, Material.ORIGINAL));
        }
        return source;
    }

    private static List<Material> legacyMaterialValues(List<Cell> cells) {
        List<Material> materials = new ArrayList<>(List.of(Material.ORIGINAL, Material.ORIGINAL, Material.ORIGINAL));
        for (Slot slot : Slot.values()) if (slot.legacyPart != null) {
            materials.set(slot.legacyPart.ordinal(), SwordCavityRules.installed(cells, slot));
        }
        return materials;
    }

    private static List<Cell> cells(ItemStack stack) { throw new AssertionError(); }
    private static boolean applies(ItemStack stack) { throw new AssertionError(); }
}
