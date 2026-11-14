package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** 武器配方的横置轮廓与环境参考；2026-10-19 仅映射视觉坐标，不改材料账本。 */
public final class WeaponCavityShapeRules {
    public static final int CELLS = 27;

    private WeaponCavityShapeRules() {}

    public static int[] outline(String family) {
        return switch (family) {
            case "helmet" -> new int[]{2, 4, 6, 11, 15, 3, 5, 10, 16, 20, 21, 23, 24};
            case "chestplate" -> new int[]{2, 6, 11, 13, 15, 20, 22, 24, 1, 7, 10, 12, 14, 16, 21, 23};
            case "leggings" -> new int[]{2, 4, 6, 11, 15, 20, 24, 3, 5, 12, 14, 21, 23};
            case "boots" -> new int[]{11, 15, 20, 24, 2, 3, 5, 6, 12, 14, 19, 21, 23, 25};
            case "bow" -> new int[]{20, 4, 24, 21, 22, 23, 11, 15, 3, 5, 12, 14};
            case "crossbow" -> new int[]{9, 10, 11, 12, 13, 14, 15, 16, 4, 22, 3, 21, 5, 23};
            case "staff", "axe" -> new int[]{9, 10, 11, 12, 13, 14, 15, 6, 7, 16, 24, 25};
            case "trident" -> new int[]{9, 10, 11, 12, 13, 14, 15, 6, 7, 8, 16, 17, 24, 25, 26};
            default -> new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17, 2, 20, 3, 21, 4, 22, 5, 23};
        };
    }

    public static int[] layout(String family, List<String> items, List<Integer> unlocks) {
        if (items.size() != CELLS || unlocks.size() != CELLS) throw new IllegalArgumentException("Expected 27 logical cells");
        int[] result = new int[CELLS];
        Arrays.fill(result, -1);
        boolean[] occupied = new boolean[CELLS];
        for (int index = 0; index < CELLS; index++) {
            String item = items.get(index);
            if (!item.isEmpty() && !item.equals("minecraft:air")) assign(result, occupied, index, preferred(family, item));
        }
        List<Integer> remaining = new ArrayList<>();
        for (int index = 0; index < CELLS; index++) if (result[index] < 0) remaining.add(index);
        remaining.sort(Comparator.comparingInt(index -> unlocks.get(index) < 0 ? Integer.MAX_VALUE : unlocks.get(index)));
        for (int index : remaining) {
            assign(result, occupied, index, outline(family));
            if (result[index] < 0) {
                for (int position = 0; position < CELLS; position++) if (!occupied[position]) {
                    result[index] = position;
                    occupied[position] = true;
                    break;
                }
            }
        }
        return result;
    }

    private static int[] preferred(String family, String item) {
        boolean binding = item.equals("minecraft:string") || item.equals("minecraft:leather") || item.endsWith(":magic_cloth");
        boolean handle = item.equals("minecraft:stick") || item.equals("minecraft:blaze_rod")
                || item.endsWith("_helve") || item.equals("minecraft:wooden_shovel");
        return switch (family) {
            case "bow" -> binding ? new int[]{21, 22, 23} : new int[]{20, 4, 24, 11, 15, 3, 5};
            case "crossbow" -> binding ? new int[]{4, 22, 3, 21} : handle ? new int[]{9, 10, 11} : new int[]{13, 14, 15, 16};
            case "staff" -> handle || item.endsWith("_planks") ? new int[]{9, 10, 11, 12, 13}
                    : binding ? new int[]{14, 15} : new int[]{16, 7, 25, 6, 24, 15};
            case "axe", "trident" -> handle ? new int[]{9, 10, 11, 12, 13} : new int[]{15, 6, 24, 16, 7, 25, 17, 8, 26};
            default -> handle ? new int[]{9, 10} : binding ? new int[]{10, 11}
                    : item.equals("minecraft:flint") || item.equals("minecraft:cobblestone") && !family.equals("stone_sword")
                            ? new int[]{2, 20, 11} : new int[]{13, 14, 15, 16, 17, 12, 2, 20};
        };
    }

    private static void assign(int[] result, boolean[] occupied, int index, int[] candidates) {
        for (int position : candidates) if (!occupied[position]) {
            result[index] = position;
            occupied[position] = true;
            return;
        }
    }

    public static List<String> environment(String family, String weaponId) {
        if (List.of("helmet", "chestplate", "leggings", "boots").contains(family)) {
            if (weaponId.contains("netherite") || weaponId.contains("ignit") || weaponId.contains("infernal")) return List.of("minecraft:obsidian", "minecraft:blaze_rod");
            if (weaponId.startsWith("irons_spellbooks:")) return List.of("irons_spellbooks:arcane_essence", "minecraft:amethyst_shard");
            return List.of("minecraft:leather", "minecraft:string");
        }
        List<String> references = new ArrayList<>();
        for (var piece : SwordCompatibilityRules.upgradeReferenceBudget(weaponId, List.of())) {
            if (piece.origin() == SwordNativeMaterialRules.Origin.THEME && !references.contains(piece.itemId()) && references.size() < 2) {
                references.add(piece.itemId());
            }
        }
        if (!references.isEmpty()) return List.copyOf(references);
        if (weaponId.contains("wooden_")) return List.of("minecraft:grass", "minecraft:oak_leaves");
        return switch (family) {
            case "bow", "crossbow" -> List.of("minecraft:slime_ball", "minecraft:grass");
            case "staff" -> List.of("irons_spellbooks:arcane_essence", "minecraft:amethyst_shard");
            case "trident" -> List.of("minecraft:prismarine_shard", "minecraft:kelp");
            default -> List.of("minecraft:cobblestone", "minecraft:string");
        };
    }
}
