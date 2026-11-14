package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Origin;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class DiamondAlloyArmorCore {
    private static final Set<String> ARMOR = Set.of("kubejs:ne_helmet", "kubejs:ne_chestplate", "kubejs:ne_leggings", "kubejs:ne_boots");

    private DiamondAlloyArmorCore() {}

    public static boolean applies(ItemStack stack) {
        var identifier = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return EquipmentWeaponBuild.applies(stack) && identifier != null && ARMOR.contains(identifier.toString());
    }

    public static int index(ItemStack stack, List<Piece> parts) {
        if (!applies(stack)) return -1;
        String identifier = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        for (int index = 0; index < parts.size(); index++) {
            Piece piece = parts.get(index);
            if (piece.count() == 1 && piece.origin() == Origin.THEME && piece.itemId().equals(identifier)) return index;
        }
        return -1;
    }

    public static boolean fixed(ItemStack stack, int index) {
        return applies(stack) && index >= 0 && index == index(stack, EquipmentWeaponBuild.parts(stack));
    }

    public static List<Piece> parts(ItemStack stack, List<Piece> original) {
        if (!applies(stack) || index(stack, original) >= 0) return original;
        var structure = stack.getTagElement(SwordCavityBuild.ROOT);
        for (int index = original.size() - 1; index >= 0; index--) {
            if (original.get(index).count() != 0) continue;
            if (structure != null) {
                var cell = structure.getCompound("Cells").getCompound(SwordCavityRules.Slot.byIndex(index).name());
                if (cell.getString("State").equals("installed") || cell.contains("Item")) continue;
            }
            List<Piece> result = new ArrayList<>(original);
            result.set(index, new Piece(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString(), 1, Origin.THEME));
            return List.copyOf(result);
        }
        return original;
    }

    public static SwordFunctionRules.Profile profile(SwordFunctionRules.Profile original, SwordFunctionRules.Values base, int core) {
        if (core < 0) return original;
        List<SwordFunctionRules.Values> values = new ArrayList<>(Collections.nCopies(SwordCavityRules.CELL_COUNT, SwordFunctionRules.ZERO));
        values.set(core, base);
        return new SwordFunctionRules.Profile(original.minimum(), Collections.nCopies(SwordCavityRules.CELL_COUNT, SwordFunctionRules.ZERO),
                original.unlockLevels(), original.materials(), values, original.itemValues());
    }

    public static ItemStack display(ItemStack stack) {
        ItemStack icon = new ItemStack(stack.getItem());
        icon.setHoverName(Component.translatable("item.ftboss.diamond_alloy_armor_core"));
        return icon;
    }
}
