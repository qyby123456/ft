/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public final class WeaponSkillLoadoutRules {
    public static final int MAX_SKILLS = 8;

    private WeaponSkillLoadoutRules() {
    }

    public static List<Integer> carried(int n, int[] nArray) {
        int n2 = Math.max(0, Math.min(8, n));
        TreeSet<Integer> treeSet = new TreeSet<Integer>();
        if (nArray == null) {
            for (int i = 0; i < n2; ++i) {
                treeSet.add(i);
            }
        } else {
            for (int n3 : nArray) {
                if (n3 < 0 || n3 >= n2) continue;
                treeSet.add(n3);
            }
        }
        if (treeSet.isEmpty() && n2 > 0) {
            treeSet.add(0);
        }
        return List.copyOf(treeSet);
    }

    public static List<Integer> toggle(int n, int[] nArray, int n2) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>(WeaponSkillLoadoutRules.carried(n, nArray));
        if (n2 < 0 || n2 >= Math.min(8, n)) {
            return List.copyOf(arrayList);
        }
        if (arrayList.contains(n2)) {
            if (arrayList.size() > 1) {
                arrayList.remove((Object)n2);
            }
        } else {
            arrayList.add(n2);
        }
        arrayList.sort(Integer::compareTo);
        return List.copyOf(arrayList);
    }

    public static int wheelIndex(int n, double d, double d2, double d3) {
        if (n <= 0 || n > 8 || d * d + d2 * d2 < d3 * d3) {
            return -1;
        }
        double d4 = Math.atan2(d, -d2);
        double d5 = Math.PI * 2;
        return (int)Math.floor((d4 + d5 + Math.PI / (double)n) % d5 / d5 * (double)n) % n;
    }
}

