/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FoodWeaponEchoQueue<Value> {
    private static final int MAX_PENDING_PER_OWNER = 2;
    private static final int MAX_TOTAL_PENDING = 4096;
    private final Map<Key, Value> pending = new HashMap<Key, Value>();

    public boolean canOffer(Key key) {
        if (key == null || key.owner() == null || key.weapon() == null || key.attack() == null || !"MAIN_HAND".equals(key.originalHand()) && !"OFF_HAND".equals(key.originalHand()) || this.pending.containsKey(key) || this.pending.size() >= 4096) {
            return false;
        }
        if (this.pending.keySet().stream().anyMatch(key2 -> key2.owner().equals(key.owner()) && key2.weapon().equals(key.weapon()) && key2.originalHand().equals(key.originalHand()))) {
            return false;
        }
        return this.pending.keySet().stream().filter(key2 -> key2.owner().equals(key.owner())).count() < 2L;
    }

    public boolean offer(Key key, Value Value2) {
        if (Value2 == null || !this.canOffer(key)) {
            return false;
        }
        this.pending.put(key, Value2);
        return true;
    }

    public List<Entry<Value>> entries(UUID uUID) {
        ArrayList arrayList = new ArrayList();
        this.pending.forEach((key, object) -> {
            if (key.owner().equals(uUID)) {
                arrayList.add(new Entry<Object>((Key)key, object));
            }
        });
        return List.copyOf(arrayList);
    }

    public void remove(Key key) {
        this.pending.remove(key);
    }

    public boolean remove(Key key, Value Value2) {
        if (Value2 == null || this.pending.get(key) != Value2) {
            return false;
        }
        this.pending.remove(key);
        return true;
    }

    public void clear(UUID uUID) {
        this.pending.keySet().removeIf(key -> key.owner().equals(uUID));
    }

    public void clear() {
        this.pending.clear();
    }

    public record Key(UUID owner, UUID weapon, String originalHand, UUID attack) {
    }

    public record Entry<Value>(Key key, Value value) {
    }
}

