package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.ItemContainerContents;

public final class NativeNestedItemGraph {
    private final List<Edge> edges = new ArrayList<>();

    private NativeNestedItemGraph(Context context, PatchedDataComponentMap components, HolderLookup.Provider registries, int depth) {
        components.stream().forEach(component -> {
            Object value = component.value();
            if (value instanceof ItemContainerContents || value instanceof BundleContents || value instanceof ChargedProjectiles) {
                if (!(value instanceof NativeNestedItemFields.Items fields)) {
                    throw new IllegalStateException("Actual native nested item component field Mixin is required");
                }
                List<ItemStack> actual = Objects.requireNonNull(fields.ftboss$nestedItems(), "actual nested item storage");
                if (actual.size() > 65536) throw new IllegalArgumentException("Native nested item storage budget");
                List<ItemStack> children = List.copyOf(actual);
                this.edges.add(new Edge(value, actual, children));
                for (ItemStack child : children) context.capture(child, registries, depth + 1);
            } else if (value instanceof FoodProperties food && food.usingConvertsTo().isPresent()) {
                ItemStack child = food.usingConvertsTo().orElseThrow();
                this.edges.add(new Edge(value, food.usingConvertsTo(), List.of(child)));
                context.capture(child, registries, depth + 1);
            }
        });
    }

    static NativeNestedItemGraph capture(Context context, PatchedDataComponentMap components, HolderLookup.Provider registries, int depth) {
        return new NativeNestedItemGraph(context, components, registries, depth);
    }

    boolean same(NativeNestedItemGraph current) {
        if (current == null || this.edges.size() != current.edges.size()) return false;
        for (int index = 0; index < this.edges.size(); index++) {
            Edge before = this.edges.get(index);
            Edge after = current.edges.get(index);
            if (before.value != after.value || before.storage != after.storage || before.children.size() != after.children.size()) return false;
            for (int slot = 0; slot < before.children.size(); slot++) {
                if (before.children.get(slot) != after.children.get(slot)) return false;
            }
        }
        return true;
    }

    void observeContainers(Consumer<Object> observer) {
        for (Edge edge : this.edges) {
            if (!(edge.value instanceof FoodProperties)) observer.accept(edge.value);
        }
    }

    private record Edge(Object value, Object storage, List<ItemStack> children) {
    }

    static final class Context {
        private final IdentityHashMap<ItemStack, NativeItemComponentObservation> observations = new IdentityHashMap<>();
        private final IdentityHashMap<ItemStack, Boolean> active = new IdentityHashMap<>();
        private int references;

        NativeItemComponentObservation capture(ItemStack stack, HolderLookup.Provider registries, int depth) {
            Objects.requireNonNull(stack, "actual nested stack");
            if (depth > 64 || ++this.references > 65536) throw new IllegalArgumentException("Native nested item graph budget exceeded");
            if (this.active.containsKey(stack)) throw new IllegalArgumentException("Cyclic native nested item component graph");
            NativeItemComponentObservation existing = this.observations.get(stack);
            if (existing != null) return existing;
            this.active.put(stack, Boolean.TRUE);
            try {
                NativeItemComponentObservation observed = new NativeItemComponentObservation(stack, registries, this, depth);
                this.observations.put(stack, observed);
                return observed;
            } finally {
                this.active.remove(stack);
            }
        }

        boolean same(Context current) {
            if (current == null || this.observations.size() != current.observations.size()) return false;
            for (Map.Entry<ItemStack, NativeItemComponentObservation> entry : this.observations.entrySet()) {
                if (!entry.getValue().sameLocal(current.observations.get(entry.getKey()))) return false;
            }
            return true;
        }

        boolean sameExcept(Context current, ItemStack root) {
            if (current == null || this.observations.size() != current.observations.size()
                || !this.observations.keySet().equals(current.observations.keySet())) return false;
            for (Map.Entry<ItemStack, NativeItemComponentObservation> entry : this.observations.entrySet()) {
                if (entry.getKey() != root && !entry.getValue().sameLocal(current.observations.get(entry.getKey()))) return false;
            }
            return true;
        }

        void observe(Consumer<NativeItemComponentObservation> observer) {
            this.observations.values().forEach(observer);
        }
    }
}
