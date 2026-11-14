package net.farmingtales.ftboss.item;

import java.util.TreeSet;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

final class NativeComponentCommitPlan {
    private final ItemStack source;
    private final String key;
    private final Thread thread;
    private final NativeItemComponentObservation physicalBefore;
    private final CompoundTag physicalRoot;
    private final Value physicalBeforeValue;
    private final Value publicBeforeValue;
    private final Value deltaBefore;
    private final Value deltaAfter;
    private final Value expectedAfter;

    private NativeComponentCommitPlan(ItemStack source, String key, CompoundTag before, CompoundTag after,
            NativeItemComponentObservation physicalBefore, CompoundTag physicalRoot) {
        this.source = source;
        this.key = key;
        this.thread = Thread.currentThread();
        this.physicalBefore = physicalBefore;
        this.physicalRoot = physicalRoot;
        this.physicalBeforeValue = NativeExpectedRawFields.observe(physicalRoot);
        this.publicBeforeValue = publicRoot(source);
        this.deltaBefore = NativeExpectedRawFields.observe(before);
        this.deltaAfter = NativeExpectedRawFields.observe(after);
        Value current = compound(FoodWeaponExpectedTrace.field(this.publicBeforeValue, key));
        this.expectedAfter = FoodWeaponExpectedTrace.put(this.publicBeforeValue, key, merge(current, this.deltaBefore, this.deltaAfter, before, after));
        if (!physicalBefore.matches(source)) {
            throw new IllegalStateException("Native component input changed while planning the original commit");
        }
    }

    private static void caller() {
        Class<?> actual = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames ->
            frames.filter(frame -> frame.getDeclaringClass() != NativeComponentCommitPlan.class).findFirst()
                .map(StackWalker.StackFrame::getDeclaringClass).orElse(null));
        if (actual != NativeExpectedFieldObservation.class) {
            throw new IllegalStateException("Native component transition plans require the actual observer caller");
        }
    }

    static NativeComponentCommitPlan prepare(ItemStack source, String key, CompoundTag before, CompoundTag after,
            NativeItemComponentObservation physicalBefore, CompoundTag physicalRoot) {
        caller();
        if (source == null || key == null || before == null || after == null || physicalBefore == null) {
            throw new IllegalArgumentException("Actual native commit inputs and complete physical BEFORE required");
        }
        return new NativeComponentCommitPlan(source, key, before, after, physicalBefore, physicalRoot);
    }

    private static Value publicRoot(ItemStack source) {
        CustomData data = source.get(DataComponents.CUSTOM_DATA);
        return data == null ? FoodWeaponExpectedTrace.emptyCompound() : NativeExpectedRawFields.observe(data.getUnsafe());
    }

    private static Value compound(Value value) {
        return value != null && value.kind == FoodWeaponExpectedTrace.COMPOUND ? value : FoodWeaponExpectedTrace.emptyCompound();
    }

    private static Value merge(Value current, Value before, Value after, CompoundTag beforeSource, CompoundTag afterSource) {
        TreeSet<String> keys = new TreeSet<>();
        for (String key : FoodWeaponExpectedTrace.compoundKeys(before)) keys.add(key);
        for (String key : FoodWeaponExpectedTrace.compoundKeys(after)) keys.add(key);
        Value result = current;
        for (String key : keys) {
            Value original = FoodWeaponExpectedTrace.field(before, key);
            Value changed = FoodWeaponExpectedTrace.field(after, key);
            net.minecraft.nbt.Tag originalTag = beforeSource == null ? null : beforeSource.get(key);
            net.minecraft.nbt.Tag changedTag = afterSource.get(key);
            if (java.util.Objects.equals(originalTag, changedTag)) continue;
            if (changed == null) {
                result = FoodWeaponExpectedTrace.remove(result, key);
            } else {
                Value existing = FoodWeaponExpectedTrace.field(result, key);
                if ((original == null || original.kind == FoodWeaponExpectedTrace.COMPOUND)
                        && changed.kind == FoodWeaponExpectedTrace.COMPOUND
                        && existing != null && existing.kind == FoodWeaponExpectedTrace.COMPOUND) {
                    result = FoodWeaponExpectedTrace.put(result, key, merge(existing, compound(original), changed, originalTag instanceof CompoundTag compound ? compound : null, (CompoundTag) changedTag));
                } else {
                    result = FoodWeaponExpectedTrace.put(result, key, changed);
                }
            }
        }
        return result;
    }

    boolean ready(ItemStack source, String key, CompoundTag before, CompoundTag after, CompoundTag physicalRoot) {
        caller();
        return this.thread == Thread.currentThread() && this.source == source && this.key == key
            && this.physicalRoot == physicalRoot && this.physicalBefore.matches(source)
            && FoodWeaponExpectedTrace.same(this.physicalBeforeValue, NativeExpectedRawFields.observe(physicalRoot))
            && FoodWeaponExpectedTrace.same(this.publicBeforeValue, publicRoot(source))
            && FoodWeaponExpectedTrace.same(this.deltaBefore, NativeExpectedRawFields.observe(before))
            && FoodWeaponExpectedTrace.same(this.deltaAfter, NativeExpectedRawFields.observe(after));
    }

    boolean matches(NativeItemComponentObservation returned, CompoundTag physicalRoot) {
        caller();
        return this.thread == Thread.currentThread() && returned != null && returned.matches(this.source)
            && returned.itemIdentity() == this.physicalBefore.itemIdentity() && returned.count() == this.physicalBefore.count()
            && FoodWeaponExpectedTrace.same(this.expectedAfter, NativeExpectedRawFields.observe(physicalRoot));
    }
}
