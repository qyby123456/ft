package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/** 食武多技能携带的纯规则；2026-10-12，限制失效阶段并保证至少携带一项。 */
public final class WeaponSkillLoadoutRules {
    public static final int MAX_SKILLS = 8;

    private WeaponSkillLoadoutRules() {}

    public static List<Integer> carried(int unlocked, int[] stored) {
        int count = Math.max(0, Math.min(MAX_SKILLS, unlocked));
        TreeSet<Integer> selected = new TreeSet<>();
        if (stored == null) {
            for (int index = 0; index < count; index++) selected.add(index);
        } else {
            for (int index : stored) if (index >= 0 && index < count) selected.add(index);
        }
        if (selected.isEmpty() && count > 0) selected.add(0);
        return List.copyOf(selected);
    }

    public static List<Integer> toggle(int unlocked, int[] stored, int index) {
        List<Integer> selected = new ArrayList<>(carried(unlocked, stored));
        if (index < 0 || index >= Math.min(MAX_SKILLS, unlocked)) return List.copyOf(selected);
        if (selected.contains(index)) {
            if (selected.size() > 1) selected.remove(Integer.valueOf(index));
        } else selected.add(index);
        selected.sort(Integer::compareTo);
        return List.copyOf(selected);
    }

    public static int wheelIndex(int count, double offsetX, double offsetY, double deadZone) {
        if (count <= 0 || count > MAX_SKILLS || offsetX * offsetX + offsetY * offsetY < deadZone * deadZone) return -1;
        double angle = Math.atan2(offsetX, -offsetY);
        double fullTurn = Math.PI * 2.0;
        return (int) Math.floor(((angle + fullTurn + Math.PI / count) % fullTurn) / fullTurn * count) % count;
    }
}
