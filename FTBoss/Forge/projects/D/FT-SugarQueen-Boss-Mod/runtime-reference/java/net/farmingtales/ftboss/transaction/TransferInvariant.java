/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class TransferInvariant {
    private TransferInvariant() {
    }

    public static boolean valid(Contents contents, Contents contents2, int n, Set<Integer> set) {
        if (contents.cells().size() != contents2.cells().size() || contents.inventory().size() != contents2.inventory().size() || n < 0 || n >= contents.inventory().size() || !contents.inventory().get(n).equals(contents2.inventory().get(n))) {
            return false;
        }
        for (int i = 0; i < contents2.cells().size(); ++i) {
            Stack stack = contents2.cells().get(i);
            if (stack.count() <= 1 && (!set.contains(i) || stack.count() <= 0 || stack.equals(contents.cells().get(i)))) continue;
            return false;
        }
        for (int n2 : set) {
            if (n2 >= 0 && n2 < contents.cells().size()) continue;
            return false;
        }
        return TransferInvariant.amounts(contents).equals(TransferInvariant.amounts(contents2));
    }

    private static Map<String, Long> amounts(Contents contents) {
        HashMap<String, Long> hashMap = new HashMap<String, Long>();
        for (Stack stack : contents.cells()) {
            TransferInvariant.add(hashMap, stack);
        }
        for (Stack stack : contents.inventory()) {
            TransferInvariant.add(hashMap, stack);
        }
        for (Stack stack : contents.drops()) {
            TransferInvariant.add(hashMap, stack);
        }
        TransferInvariant.add(hashMap, contents.cursor());
        return hashMap;
    }

    private static void add(Map<String, Long> map, Stack stack) {
        if (stack.count() > 0) {
            map.merge(stack.fingerprint(), Long.valueOf(stack.count()), Long::sum);
        }
    }

    public record Contents(List<Stack> cells, List<Stack> inventory, Stack cursor, List<Stack> drops) {
        public Contents {
            list = List.copyOf(list);
            list2 = List.copyOf(list2);
            list3 = List.copyOf(list3);
            Objects.requireNonNull(stack);
        }
    }

    public record Stack(String fingerprint, int count) {
        public Stack {
            Objects.requireNonNull(string);
            if (n < 0 || n > 1024 || n == 0 && !string.isEmpty() || n > 0 && string.isEmpty()) {
                throw new IllegalArgumentException("stack");
            }
        }
    }
}

