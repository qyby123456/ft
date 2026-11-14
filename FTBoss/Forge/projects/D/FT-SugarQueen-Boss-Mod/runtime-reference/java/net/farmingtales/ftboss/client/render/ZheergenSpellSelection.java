/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class ZheergenSpellSelection {
    private ZheergenSpellSelection() {
    }

    private static int priority(String string) {
        return string.equals("PULSE") ? 3 : (string.equals("RELEASE") ? 2 : (string.equals("CHARGE") ? 1 : 0));
    }

    public static List<Integer> choose(List<Candidate> list) {
        LinkedHashMap<String, Candidate> linkedHashMap = new LinkedHashMap<String, Candidate>();
        for (Candidate object : list) {
            Candidate candidate = (Candidate)linkedHashMap.get(object.key());
            if (candidate != null && ZheergenSpellSelection.priority(object.phase()) <= ZheergenSpellSelection.priority(candidate.phase()) && (ZheergenSpellSelection.priority(object.phase()) != ZheergenSpellSelection.priority(candidate.phase()) || object.refreshed() <= candidate.refreshed())) continue;
            linkedHashMap.put(object.key(), object);
        }
        ArrayList arrayList = new ArrayList();
        for (Candidate candidate : linkedHashMap.values()) {
            arrayList.add(candidate.index());
        }
        return arrayList;
    }

    public record Candidate(String key, String phase, long refreshed, int index) {
    }
}

