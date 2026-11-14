/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;

public final class WeaponCavityShapeRules {
    public static final int CELLS = 27;

    private WeaponCavityShapeRules() {
    }

    public static int[] outline(String string) {
        int[] nArray;
        switch (string) {
            case "bow": {
                int[] nArray2 = new int[12];
                nArray2[0] = 20;
                nArray2[1] = 4;
                nArray2[2] = 24;
                nArray2[3] = 21;
                nArray2[4] = 22;
                nArray2[5] = 23;
                nArray2[6] = 11;
                nArray2[7] = 15;
                nArray2[8] = 3;
                nArray2[9] = 5;
                nArray2[10] = 12;
                nArray = nArray2;
                nArray2[11] = 14;
                break;
            }
            case "crossbow": {
                int[] nArray3 = new int[14];
                nArray3[0] = 9;
                nArray3[1] = 10;
                nArray3[2] = 11;
                nArray3[3] = 12;
                nArray3[4] = 13;
                nArray3[5] = 14;
                nArray3[6] = 15;
                nArray3[7] = 16;
                nArray3[8] = 4;
                nArray3[9] = 22;
                nArray3[10] = 3;
                nArray3[11] = 21;
                nArray3[12] = 5;
                nArray = nArray3;
                nArray3[13] = 23;
                break;
            }
            case "staff": 
            case "axe": {
                int[] nArray4 = new int[12];
                nArray4[0] = 9;
                nArray4[1] = 10;
                nArray4[2] = 11;
                nArray4[3] = 12;
                nArray4[4] = 13;
                nArray4[5] = 14;
                nArray4[6] = 15;
                nArray4[7] = 6;
                nArray4[8] = 7;
                nArray4[9] = 16;
                nArray4[10] = 24;
                nArray = nArray4;
                nArray4[11] = 25;
                break;
            }
            case "trident": {
                int[] nArray5 = new int[15];
                nArray5[0] = 9;
                nArray5[1] = 10;
                nArray5[2] = 11;
                nArray5[3] = 12;
                nArray5[4] = 13;
                nArray5[5] = 14;
                nArray5[6] = 15;
                nArray5[7] = 6;
                nArray5[8] = 7;
                nArray5[9] = 8;
                nArray5[10] = 16;
                nArray5[11] = 17;
                nArray5[12] = 24;
                nArray5[13] = 25;
                nArray = nArray5;
                nArray5[14] = 26;
                break;
            }
            default: {
                int[] nArray6 = new int[17];
                nArray6[0] = 9;
                nArray6[1] = 10;
                nArray6[2] = 11;
                nArray6[3] = 12;
                nArray6[4] = 13;
                nArray6[5] = 14;
                nArray6[6] = 15;
                nArray6[7] = 16;
                nArray6[8] = 17;
                nArray6[9] = 2;
                nArray6[10] = 20;
                nArray6[11] = 3;
                nArray6[12] = 21;
                nArray6[13] = 4;
                nArray6[14] = 22;
                nArray6[15] = 5;
                nArray = nArray6;
                nArray6[16] = 23;
            }
        }
        return nArray;
    }

    public static int[] layout(String string, List<String> list, List<Integer> list2) {
        if (list.size() != 27 || list2.size() != 27) {
            throw new IllegalArgumentException("Expected 27 logical cells");
        }
        int[] nArray = new int[27];
        Arrays.fill(nArray, -1);
        boolean[] blArray = new boolean[27];
        for (int i = 0; i < 27; ++i) {
            String string2 = list.get(i);
            if (string2.isEmpty() || string2.equals("minecraft:air")) continue;
            WeaponCavityShapeRules.assign(nArray, blArray, i, WeaponCavityShapeRules.preferred(string, string2));
        }
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (int i = 0; i < 27; ++i) {
            if (nArray[i] >= 0) continue;
            arrayList.add(i);
        }
        arrayList.sort(Comparator.comparingInt(n -> (Integer)list2.get((int)n) < 0 ? Integer.MAX_VALUE : (Integer)list2.get((int)n)));
        Iterator iterator = arrayList.iterator();
        block2: while (iterator.hasNext()) {
            int n2 = (Integer)iterator.next();
            WeaponCavityShapeRules.assign(nArray, blArray, n2, WeaponCavityShapeRules.outline(string));
            if (nArray[n2] >= 0) continue;
            for (int i = 0; i < 27; ++i) {
                if (blArray[i]) continue;
                nArray[n2] = i;
                blArray[i] = true;
                continue block2;
            }
        }
        return nArray;
    }

    private static int[] preferred(String string, String string2) {
        int[] nArray;
        boolean bl = string2.equals("minecraft:string") || string2.equals("minecraft:leather") || string2.endsWith(":magic_cloth");
        boolean bl2 = string2.equals("minecraft:stick") || string2.equals("minecraft:blaze_rod") || string2.endsWith("_helve") || string2.equals("minecraft:wooden_shovel");
        switch (string) {
            case "bow": {
                if (bl) {
                    int[] nArray2 = new int[3];
                    nArray2[0] = 21;
                    nArray2[1] = 22;
                    nArray = nArray2;
                    nArray2[2] = 23;
                    break;
                }
                int[] nArray3 = new int[7];
                nArray3[0] = 20;
                nArray3[1] = 4;
                nArray3[2] = 24;
                nArray3[3] = 11;
                nArray3[4] = 15;
                nArray3[5] = 3;
                nArray = nArray3;
                nArray3[6] = 5;
                break;
            }
            case "crossbow": {
                if (bl) {
                    int[] nArray4 = new int[4];
                    nArray4[0] = 4;
                    nArray4[1] = 22;
                    nArray4[2] = 3;
                    nArray = nArray4;
                    nArray4[3] = 21;
                    break;
                }
                if (bl2) {
                    int[] nArray5 = new int[3];
                    nArray5[0] = 9;
                    nArray5[1] = 10;
                    nArray = nArray5;
                    nArray5[2] = 11;
                    break;
                }
                int[] nArray6 = new int[4];
                nArray6[0] = 13;
                nArray6[1] = 14;
                nArray6[2] = 15;
                nArray = nArray6;
                nArray6[3] = 16;
                break;
            }
            case "staff": {
                if (bl2 || string2.endsWith("_planks")) {
                    int[] nArray7 = new int[5];
                    nArray7[0] = 9;
                    nArray7[1] = 10;
                    nArray7[2] = 11;
                    nArray7[3] = 12;
                    nArray = nArray7;
                    nArray7[4] = 13;
                    break;
                }
                if (bl) {
                    int[] nArray8 = new int[2];
                    nArray8[0] = 14;
                    nArray = nArray8;
                    nArray8[1] = 15;
                    break;
                }
                int[] nArray9 = new int[6];
                nArray9[0] = 16;
                nArray9[1] = 7;
                nArray9[2] = 25;
                nArray9[3] = 6;
                nArray9[4] = 24;
                nArray = nArray9;
                nArray9[5] = 15;
                break;
            }
            case "axe": 
            case "trident": {
                if (bl2) {
                    int[] nArray10 = new int[5];
                    nArray10[0] = 9;
                    nArray10[1] = 10;
                    nArray10[2] = 11;
                    nArray10[3] = 12;
                    nArray = nArray10;
                    nArray10[4] = 13;
                    break;
                }
                int[] nArray11 = new int[9];
                nArray11[0] = 15;
                nArray11[1] = 6;
                nArray11[2] = 24;
                nArray11[3] = 16;
                nArray11[4] = 7;
                nArray11[5] = 25;
                nArray11[6] = 17;
                nArray11[7] = 8;
                nArray = nArray11;
                nArray11[8] = 26;
                break;
            }
            default: {
                if (bl2) {
                    int[] nArray12 = new int[2];
                    nArray12[0] = 9;
                    nArray = nArray12;
                    nArray12[1] = 10;
                    break;
                }
                if (bl) {
                    int[] nArray13 = new int[2];
                    nArray13[0] = 10;
                    nArray = nArray13;
                    nArray13[1] = 11;
                    break;
                }
                if (string2.equals("minecraft:flint") || string2.equals("minecraft:cobblestone") && !string.equals("stone_sword")) {
                    int[] nArray14 = new int[3];
                    nArray14[0] = 2;
                    nArray14[1] = 20;
                    nArray = nArray14;
                    nArray14[2] = 11;
                    break;
                }
                int[] nArray15 = new int[8];
                nArray15[0] = 13;
                nArray15[1] = 14;
                nArray15[2] = 15;
                nArray15[3] = 16;
                nArray15[4] = 17;
                nArray15[5] = 12;
                nArray15[6] = 2;
                nArray = nArray15;
                nArray15[7] = 20;
            }
        }
        return nArray;
    }

    private static void assign(int[] nArray, boolean[] blArray, int n, int[] nArray2) {
        for (int n2 : nArray2) {
            if (blArray[n2]) continue;
            nArray[n] = n2;
            blArray[n2] = true;
            return;
        }
    }

    public static List<String> environment(String string, String string2) {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (SwordNativeMaterialRules.Piece piece : SwordCompatibilityRules.upgradeReferenceBudget(string2, List.of())) {
            if (piece.origin() != SwordNativeMaterialRules.Origin.THEME || arrayList.contains(piece.itemId()) || arrayList.size() >= 2) continue;
            arrayList.add(piece.itemId());
        }
        if (!arrayList.isEmpty()) {
            return List.copyOf(arrayList);
        }
        if (string2.contains("wooden_")) {
            return List.of("minecraft:grass", "minecraft:oak_leaves");
        }
        return switch (string) {
            case "bow", "crossbow" -> List.of("minecraft:slime_ball", "minecraft:grass");
            case "staff" -> List.of("irons_spellbooks:arcane_essence", "minecraft:amethyst_shard");
            case "trident" -> List.of("minecraft:prismarine_shard", "minecraft:kelp");
            default -> List.of("minecraft:cobblestone", "minecraft:string");
        };
    }
}

