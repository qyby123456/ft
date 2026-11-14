package net.farmingtales.ftboss.item;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.lang.StackWalker.Option;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class NativeItemComponentObservation {
    private final ItemStack stack;
    private final HolderLookup.Provider registries;
    private final Item item;
    private final int count;
    private final PatchedDataComponentMap components;
    private final DataComponentMap prototype;
    private final Object patch;
    private final boolean copyOnWrite;
    private final long mutationVersion;
    private final long stackMutationVersion;
    private final NativeNestedItemGraph.Context graph;
    private final NativeNestedItemGraph nested;
    private final NativePotionEffectGraph potions;
    private final Map<DataComponentType<?>, Object> values = new IdentityHashMap<>();
    private final Tag encodedValues;
    private final Tag encodedPatch;
    private final NativeNbtGraphObservation customData;
    private final Map<CustomData, NativeNbtGraphObservation> customDataGraphs = new IdentityHashMap<>();

    NativeItemComponentObservation(ItemStack stack, HolderLookup.Provider registries, NativeNestedItemGraph.Context graph, int depth) {
        this.graph = graph;
        this.stack = Objects.requireNonNull(stack, "stack");
        this.registries = Objects.requireNonNull(registries, "registries");
        if (!((Object)stack instanceof NativeItemComponentFields.Stack fields)) {
            throw new IllegalStateException("Actual native component stack field Mixin is required");
        }
        this.stackMutationVersion = fields.ftboss$nativeStackMutationVersion();
        if (this.stackMutationVersion == Long.MAX_VALUE) throw new IllegalStateException("Native stack mutation version exhausted");
        this.item = fields.ftboss$nativeItem();
        this.count = fields.ftboss$nativeCount();
        this.components = Objects.requireNonNull(fields.ftboss$nativeComponents(), "actual components");
        if (!((Object)this.components instanceof NativeItemComponentFields.Storage storage)) {
            throw new IllegalStateException("Actual native component storage field Mixin is required");
        }
        long enteredVersion = storage.ftboss$nativeComponentMutationVersion();
        this.customData = NativeNbtGraphObservation.capture(actualCustomData());
        this.components.stream().forEach(component -> {
            if (component.value() instanceof CustomData data) {
                this.customDataGraphs.computeIfAbsent(data, key -> NativeNbtGraphObservation.capture(actualCustomData(key)));
            }
        });
        this.nested = NativeNestedItemGraph.capture(graph, this.components, registries, depth);
        this.potions = NativePotionEffectGraph.capture(this.components);
        this.encodedValues = encodeValues().copy();
        this.encodedPatch = encodePatch().copy();
        this.prototype = storage.ftboss$nativePrototype();
        this.patch = storage.ftboss$nativePatch();
        this.copyOnWrite = storage.ftboss$nativeCopyOnWrite();
        this.components.stream().forEach(component -> this.values.put(component.type(), component.value()));
        this.mutationVersion = storage.ftboss$nativeComponentMutationVersion();
        if (this.stackMutationVersion != fields.ftboss$nativeStackMutationVersion()
            || enteredVersion != this.mutationVersion || fields.ftboss$nativeComponents() != this.components
            || fields.ftboss$nativeItem() != this.item || fields.ftboss$nativeCount() != this.count
            || !encodedSame(this.encodedValues, encodeValues()) || !encodedSame(this.encodedPatch, encodePatch())
            || storage.ftboss$nativeComponentMutationVersion() != this.mutationVersion
            || !this.customData.matches(actualCustomData()) || !customGraphsLive() || !this.potions.live()) {
            throw new IllegalStateException("Native component observation changed during capture");
        }
    }

    private static boolean encodedSame(Tag before, Tag after) {
        if (before == after) return true;
        if (before == null || after == null) return false;
        Class<?> type = before.getClass();
        if (type != after.getClass()) return before.equals(after);
        if (type == net.minecraft.nbt.FloatTag.class) {
            return Float.floatToRawIntBits(((NativeExpectedFieldViews.FloatValue) before).ftboss$rawFloat())
                == Float.floatToRawIntBits(((NativeExpectedFieldViews.FloatValue) after).ftboss$rawFloat());
        }
        if (type == net.minecraft.nbt.DoubleTag.class) {
            return Double.doubleToRawLongBits(((NativeExpectedFieldViews.DoubleValue) before).ftboss$rawDouble())
                == Double.doubleToRawLongBits(((NativeExpectedFieldViews.DoubleValue) after).ftboss$rawDouble());
        }
        if (type == net.minecraft.nbt.CompoundTag.class) {
            java.util.Map<String, Tag> left = ((NativeExpectedFieldViews.Compound) before).ftboss$rawCompoundFields();
            java.util.Map<String, Tag> right = ((NativeExpectedFieldViews.Compound) after).ftboss$rawCompoundFields();
            if (!left.keySet().equals(right.keySet())) return false;
            for (java.util.Map.Entry<String, Tag> entry : left.entrySet()) {
                if (!encodedSame(entry.getValue(), right.get(entry.getKey()))) return false;
            }
            return true;
        }
        if (type == net.minecraft.nbt.ListTag.class) {
            java.util.List<Tag> left = ((NativeExpectedFieldViews.Sequence) before).ftboss$rawListFields();
            java.util.List<Tag> right = ((NativeExpectedFieldViews.Sequence) after).ftboss$rawListFields();
            if (((net.minecraft.nbt.ListTag) before).getElementType() != ((net.minecraft.nbt.ListTag) after).getElementType()
                || left.size() != right.size()) return false;
            for (int index = 0; index < left.size(); index++) {
                if (!encodedSame(left.get(index), right.get(index))) return false;
            }
            return true;
        }
        return before.equals(after);
    }

    public static NativeItemComponentObservation capture(ItemStack stack) {
        return capture(stack, NativeFoundationItemData.registries());
    }

    public static NativeItemComponentObservation capture(ItemStack stack, HolderLookup.Provider registries) {
        return new NativeNestedItemGraph.Context().capture(stack, registries, 0);
    }

    private CompoundTag actualCustomData() {
        CustomData data = this.components.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : actualCustomData(data);
    }

    private CompoundTag actualCustomData(CustomData data) {
        if (!((Object)data instanceof NativeItemComponentFields.CustomData fields)) {
            throw new IllegalStateException("Actual native CustomData field Mixin is required");
        }
        return fields.ftboss$nativeCustomData();
    }

    private boolean customGraphsLive() {
        for (Map.Entry<CustomData, NativeNbtGraphObservation> entry : this.customDataGraphs.entrySet()) {
            if (!entry.getValue().matches(actualCustomData(entry.getKey()))) return false;
        }
        return true;
    }

    public Object semanticStorage(boolean customData) {
        boolean allowed = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.skip(1L).findFirst().map(frame -> frame.getDeclaringClass() == OwnerNativeSemanticAuthority.class
                && frame.getMethodName().equals("read")).orElse(false));
        if (!allowed) throw new IllegalStateException("Native semantic storage identity observation denied");
        if (!matches(this.stack)) throw new IllegalStateException("Native semantic physical observation is stale");
        return customData ? actualCustomData() : this.components;
    }

    public Item itemIdentity() {
        return this.item;
    }

    public int count() {
        return this.count;
    }

    Tag timerPatchSnapshot(ItemStack actual) {
        boolean allowed = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == NativeTimerTransitionPlan.class && frame.getMethodName().equals("nativeWeaponBefore")
                && frame.getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/NativeItemComponentObservation;Lnet/farmingtales/ftboss/item/FoodWeaponExpectedTrace$Value;)Lnet/farmingtales/ftboss/item/FoodWeaponExpectedTrace$Value;")).orElse(false));
        if (!allowed || actual != this.stack || !matches(actual)) throw new IllegalStateException("Native timer immutable patch observation denied or stale");
        return this.encodedPatch.copy();
    }

    public void observeRepairStorage(BiConsumer<Object, CompoundTag> observer) {
        boolean allowed = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.skip(1L).findFirst().map(frame -> frame.getDeclaringClass().getNestHost() == OwnerFullRepairGuard.class).orElse(false));
        if (!allowed) throw new IllegalStateException("Native repair storage observation denied");
        this.graph.observe(observed -> {
            observer.accept(observed.stack, null);
            observer.accept(observed.components, null);
            observed.nested.observeContainers(value -> observer.accept(value, null));
            observed.potions.observeNodes(value -> observer.accept(value, null));
            observed.customDataGraphs.forEach((data, captured) -> observer.accept(data, observed.actualCustomData(data)));
        });
    }

    private Tag encodeValues() {
        return NativeExpectedFieldObservation.componentValuesForRead(this.registries, this.components);
    }

    private Tag encodePatch() {
        return NativeExpectedFieldObservation.componentPatchForRead(this.registries, this.components.asPatch());
    }

    public boolean matches(ItemStack stack) {
        if (stack != this.stack) {
            return false;
        }
        if (!this.customData.matches(actualCustomData()) || !customGraphsLive() || !this.potions.live()) return false;
        return same(capture(stack, this.registries));
    }

    public boolean same(NativeItemComponentObservation current) {
        return current != null && this.stack == current.stack && this.graph.same(current.graph);
    }

    boolean sameLocal(NativeItemComponentObservation current) {
        if (current == null || this.stack != current.stack || !customGraphsLive() || !current.customGraphsLive()
            || !this.customDataGraphs.keySet().equals(current.customDataGraphs.keySet())) return false;
        for (Map.Entry<CustomData, NativeNbtGraphObservation> entry : this.customDataGraphs.entrySet()) {
            if (!entry.getValue().same(current.customDataGraphs.get(entry.getKey()))) return false;
        }
        if (this.item != current.item || this.count != current.count || this.components != current.components
            || this.stackMutationVersion != current.stackMutationVersion || !this.nested.same(current.nested) || !this.potions.same(current.potions)
            || this.prototype != current.prototype || this.patch != current.patch || this.copyOnWrite != current.copyOnWrite
            || this.mutationVersion != current.mutationVersion || !this.customData.same(current.customData)
            || !encodedSame(this.encodedValues, current.encodedValues)
            || !encodedSame(this.encodedPatch, current.encodedPatch) || !this.values.keySet().equals(current.values.keySet())) {
            return false;
        }
        for (Map.Entry<DataComponentType<?>, Object> entry : this.values.entrySet()) {
            if (entry.getValue() != current.values.get(entry.getKey())) {
                return false;
            }
        }
        return true;
    }

    CompoundTag expectedCustomData() {
        if (StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass() != NativeExpectedFieldObservation.class) {
            throw new IllegalStateException("Native expected custom data identity observation denied");
        }
        if (!matches(this.stack)) throw new IllegalStateException("Native expected custom data physical observation is stale");
        return actualCustomData();
    }
    Object nativeWearStorage(boolean customData) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class
            || !(caller.getMethodName().equals("nativeTransition") || caller.getMethodName().equals("nativeTransitionReturned")
                || caller.getMethodName().equals("nativeTridentPickupReady") && caller.getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;Ljava/lang/Object;)Z")
                || caller.getMethodName().equals("nativeTridentWriterCustody") && caller.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/OwnerFieldExpectedAuthority$Prepared;Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session;Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Z")
                || caller.getMethodName().equals("advance") && caller.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$Before;Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session;Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;)Z"))) {
            throw new IllegalStateException("Native wear storage identity observation denied");
        }
        if (!matches(this.stack)) throw new IllegalStateException("Native wear storage observation is stale");
        return customData ? actualCustomData() : this.components;
    }

    boolean nativeWearSame(NativeItemComponentObservation current, int writes, boolean customData) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class || !caller.getMethodName().equals("nativeTransitionReturned")) return false;
        if (current == null || writes < 0 || writes > 1 || this.stack != current.stack || this.item != current.item || this.count != current.count
            || this.count != 1 || this.components != current.components || this.prototype != current.prototype
            || this.stackMutationVersion != current.stackMutationVersion || this.mutationVersion > Long.MAX_VALUE - writes
            || this.mutationVersion + writes != current.mutationVersion || !customGraphsLive() || !current.customGraphsLive()
            || !this.nested.same(current.nested) || !this.graph.sameExcept(current.graph, this.stack) || !this.potions.same(current.potions)) return false;
        java.util.Set<DataComponentType<?>> types = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        types.addAll(this.values.keySet());
        types.addAll(current.values.keySet());
        for (DataComponentType<?> type : types) {
            if (type == DataComponents.DAMAGE || customData && type == DataComponents.CUSTOM_DATA) continue;
            Object beforeValue = this.values.get(type);
            if (beforeValue != current.values.get(type)) return false;
            if (beforeValue instanceof CustomData data && !this.customDataGraphs.get(data).same(current.customDataGraphs.get(data))) return false;
        }
        if (!(this.encodedPatch instanceof CompoundTag beforePatch) || !(current.encodedPatch instanceof CompoundTag afterPatch)) return false;
        CompoundTag before = beforePatch.copy();
        CompoundTag after = afterPatch.copy();
        for (String key : java.util.List.of("minecraft:damage", "minecraft:custom_data")) {
            before.remove(key);
            after.remove(key);
        }
        return encodedSame(before, after);
    }

    boolean nativeCrossbowChargeSame(NativeItemComponentObservation current, boolean firing) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class || !caller.getMethodName().equals("nativeTransitionReturned")) return false;
        if (current == null || this.stack != current.stack || this.item != current.item || this.count != 1 || current.count != 1
            || this.components != current.components || this.prototype != current.prototype || this.stackMutationVersion != current.stackMutationVersion
            || this.mutationVersion == Long.MAX_VALUE || this.mutationVersion + 1 != current.mutationVersion
            || !customGraphsLive() || !current.customGraphsLive() || !this.potions.same(current.potions)) return false;
        java.util.Set<DataComponentType<?>> types = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        types.addAll(this.values.keySet());
        types.addAll(current.values.keySet());
        for (DataComponentType<?> type : types) {
            Object beforeValue = this.values.get(type);
            if (type == DataComponents.CHARGED_PROJECTILES) {
                boolean originalMaterialAbsence = beforeValue == null && !firing && !this.values.containsKey(type)
                    && this.prototype.get(DataComponents.CHARGED_PROJECTILES) == null
                    && this.item.getClass() == net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem.class
                    && OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(this.stack);
                if ((!originalMaterialAbsence && (!(beforeValue instanceof net.minecraft.world.item.component.ChargedProjectiles charged)
                        || charged.isEmpty() == firing))
                    || !(current.values.get(type) instanceof net.minecraft.world.item.component.ChargedProjectiles afterCharged)
                    || afterCharged.isEmpty() != firing) return false;
                continue;
            }
            if (beforeValue != current.values.get(type)
                || beforeValue instanceof net.minecraft.world.item.component.ItemContainerContents
                || beforeValue instanceof net.minecraft.world.item.component.BundleContents
                || beforeValue instanceof net.minecraft.world.food.FoodProperties food && food.usingConvertsTo().isPresent()) return false;
            if (beforeValue instanceof CustomData data && !this.customDataGraphs.get(data).same(current.customDataGraphs.get(data))) return false;
        }
        if (!(this.encodedPatch instanceof CompoundTag beforePatch) || !(current.encodedPatch instanceof CompoundTag afterPatch)) return false;
        CompoundTag before = beforePatch.copy();
        CompoundTag after = afterPatch.copy();
        before.remove("minecraft:charged_projectiles");
        after.remove("minecraft:charged_projectiles");
        return encodedSame(before, after) && current.matches(this.stack);
    }

    boolean nativeMeleeDamageOverride(int damage) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class || !caller.getMethodName().equals("nativeMeleeItemReturned")) throw new IllegalStateException("Native melee prototype observation denied");
        Integer original = this.prototype.get(DataComponents.DAMAGE);
        return original == null || damage != original.intValue();
    }

    boolean nativeDamageOverride(int damage) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class || !caller.getMethodName().equals("nativeWearReturned")) throw new IllegalStateException("Native damage prototype observation denied");
        Integer original = this.prototype.get(DataComponents.DAMAGE);
        return original == null || damage != original.intValue();
    }
}
