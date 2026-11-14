package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

final class NativeComponentMergeBoundary391 {
    private final Thread thread = Thread.currentThread();
    private final ItemStack source;
    private final String key;
    private final NativeItemComponentObservation sourceImage;
    private final CompoundTag before;
    private final CompoundTag after;
    private final NativeNbtGraphObservation beforeImage;
    private final NativeNbtGraphObservation afterImage;
    private final IdentityHashMap<Object, Boolean> forbidden = new IdentityHashMap<>();
    private final List<Write> writes = new ArrayList<>();
    private final Value rootBefore;
    private CompoundTag buffer;
    private NativeNbtGraphObservation bufferImage;
    private Value expectedBuffer;
    private int cursor;
    private Write active;
    private CompoundTag activeOwner;
    private Tag previous;
    private int mapChildren;
    private boolean mapPending;
    private Tag mapArgument;
    private boolean bodyReturned;
    private CompoundTag publishedRoot;
    private NativeNbtGraphObservation publishedImage;
    private final IdentityHashMap<NativeNbtMutationFields, Long> versions = new IdentityHashMap<>();

    NativeComponentMergeBoundary391(ItemStack source, String key, CompoundTag before, CompoundTag after,
            NativeItemComponentObservation sourceImage, CompoundTag physicalRoot) {
        caller();
        this.source = Objects.requireNonNull(source);
        this.key = Objects.requireNonNull(key);
        this.before = Objects.requireNonNull(before);
        this.after = Objects.requireNonNull(after);
        this.sourceImage = Objects.requireNonNull(sourceImage);
        this.beforeImage = NativeNbtGraphObservation.capture(before);
        this.afterImage = NativeNbtGraphObservation.capture(after);
        this.rootBefore = physicalRoot == null ? FoodWeaponExpectedTrace.emptyCompound()
            : NativeExpectedRawFields.observe(physicalRoot);
        nodes(physicalRoot, forbidden);
        nodes(before, forbidden);
        nodes(after, forbidden);
        check(sourceImage.matches(source), "Component input changed during merge preparation");
    }

    private static void caller() {
        Class<?> actual = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames ->
            frames.filter(frame -> frame.getDeclaringClass() != NativeComponentMergeBoundary391.class
                && frame.getDeclaringClass() != Write.class).findFirst()
                .map(StackWalker.StackFrame::getDeclaringClass).orElse(null));
        check(actual == NativeExpectedFieldObservation.class, "Component merge boundary requires its exact observer caller");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private void live() {
        check(thread == Thread.currentThread() && sourceImage.matches(source)
            && beforeImage.matches(before) && afterImage.matches(after), "Component merge input, thread or original source changed");
    }

    void bind(CompoundTag current) {
        caller();
        live();
        check(buffer == null && current != null && active == null && !bodyReturned, "Repeated or null component merge callback");
        Value existing = FoodWeaponExpectedTrace.field(rootBefore, key);
        Value expected = existing != null && existing.kind == FoodWeaponExpectedTrace.COMPOUND
            ? existing : FoodWeaponExpectedTrace.emptyCompound();
        check(FoodWeaponExpectedTrace.same(expected, NativeExpectedRawFields.observe(current)), "Detached merge callback has a different BEFORE");
        disjoint(current, forbidden);
        buffer = current;
        expectedBuffer = expected;
        bufferImage = NativeNbtGraphObservation.capture(current);
        collect(new String[0], expected, before, after);
    }

    private void collect(String[] path, Value current, CompoundTag before, CompoundTag after) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        if (before != null) keys.addAll(List.of(NativeExpectedFieldObservation.nativeComponentMergeKeys391(before)));
        keys.addAll(List.of(NativeExpectedFieldObservation.nativeComponentMergeKeys391(after)));
        for (String entry : keys) {
            Tag original = before == null ? null : before.get(entry);
            Tag changed = after.get(entry);
            if (Objects.equals(original, changed)) continue;
            Value existing = FoodWeaponExpectedTrace.field(current, entry);
            if ((original == null || original instanceof CompoundTag) && changed instanceof CompoundTag changedCompound
                    && existing != null && existing.kind == FoodWeaponExpectedTrace.COMPOUND) {
                String[] nested = java.util.Arrays.copyOf(path, path.length + 1);
                nested[path.length] = entry;
                collect(nested, existing, original instanceof CompoundTag compound ? compound : null, changedCompound);
            } else {
                writes.add(new Write(path.clone(), entry, changed, changed == null ? null : NativeExpectedRawFields.observe(changed)));
            }
        }
    }

    void beforeWrite(CompoundTag owner, String key, Tag changed) {
        caller();
        live();
        check(buffer != null && !bodyReturned && active == null && publishedRoot == null
            && cursor < writes.size() && bufferImage.matches(buffer), "Component merge write is nested, stale or extra");
        Write expected = writes.get(cursor);
        CompoundTag target = buffer;
        for (String entry : expected.path) {
            check(target.get(entry) instanceof CompoundTag, "Component merge path was substituted");
            target = (CompoundTag) target.get(entry);
        }
        check(owner == target && Objects.equals(key, expected.key) && changed == expected.changed,
            "Component merge write differs from the exact planned owner, key or delta tag");
        versions.clear();
        IdentityHashMap<Object, Boolean> currentNodes = new IdentityHashMap<>();
        nodes(buffer, currentNodes);
        for (Object node : currentNodes.keySet()) {
            if (node instanceof NativeNbtMutationFields versioned) versions.put(versioned, versioned.ftboss$nativeNbtMutationVersion());
        }
        active = expected;
        activeOwner = owner;
        previous = owner.get(key);
        mapChildren = 0;
    }

    void beforeMap(CompoundTag owner, Object key, Object argument, boolean originalPublicationCaller) {
        caller();
        live();
        check(argument instanceof Tag && !mapPending, "Component merge map argument is not NBT or its entry is repeated");
        if (active != null) {
            check(owner == activeOwner && Objects.equals(key, active.key) && active.changed != null && mapChildren == 0
                && FoodWeaponExpectedTrace.same(active.value, NativeExpectedRawFields.observe((Tag) argument)),
                "Component merge map write is not the exact pending copied delta");
            IdentityHashMap<Object, Boolean> excluded = new IdentityHashMap<>(forbidden);
            nodes(buffer, excluded);
            disjoint((Tag) argument, excluded);
        } else {
            check(originalPublicationCaller && bodyReturned && publishedRoot == null && key == this.key && argument == buffer
                && bufferImage.matches(buffer), "Component root publication is foreign, repeated or premature");
            Value existing = FoodWeaponExpectedTrace.field(rootBefore, this.key);
            Value expectedRoot = existing != null && existing.kind == FoodWeaponExpectedTrace.COMPOUND
                ? FoodWeaponExpectedTrace.put(rootBefore, this.key, expectedBuffer) : rootBefore;
            check(FoodWeaponExpectedTrace.same(expectedRoot, NativeExpectedRawFields.observe(owner))
                && (existing == null || existing.kind != FoodWeaponExpectedTrace.COMPOUND || owner.get(this.key) == buffer),
                "Component root publication has a foreign detached parent");
            disjoint(owner, forbidden);
            publishedRoot = owner;
            previous = owner.get(this.key);
        }
        mapPending = true;
        mapArgument = (Tag) argument;
    }

    void afterMap(CompoundTag owner, Object key, Object argument, Object returned) {
        caller();
        live();
        check(mapPending && argument == mapArgument && returned == previous && owner.get((String) key) == argument,
            "Component map return or published argument differs");
        if (active != null) {
            check(owner == activeOwner && Objects.equals(key, active.key) && mapChildren == 0, "Component merge map return has a different owner");
            mapChildren++;
        } else {
            check(owner == publishedRoot && key == this.key && argument == buffer && publishedImage == null,
                "Component root publication returned outside its exact event");
            publishedImage = NativeNbtGraphObservation.capture(owner);
        }
        mapPending = false;
        mapArgument = null;
    }

    void afterWrite() {
        caller();
        live();
        check(active != null && !mapPending && mapChildren == (active.changed == null ? 0 : 1), "Component merge lacks its exact backing map operation");
        expectedBuffer = replace(expectedBuffer, active.path, 0, active.key, active.value);
        check(FoodWeaponExpectedTrace.same(expectedBuffer, NativeExpectedRawFields.observe(buffer)), "Component merge changed another field");
        for (Map.Entry<NativeNbtMutationFields, Long> entry : versions.entrySet()) {
            NativeNbtMutationFields node = entry.getKey();
            long expectedVersion = entry.getValue() + (node == activeOwner ? 1L : 0L);
            check(expectedVersion >= 0 && !node.ftboss$nativeNbtRawEscaped()
                && node.ftboss$nativeNbtMutationVersion() == expectedVersion, "Component merge changed or restored an unplanned mutable node");
        }
        bufferImage = NativeNbtGraphObservation.capture(buffer);
        cursor++;
        active = null;
        activeOwner = null;
        versions.clear();
    }

    private static Value replace(Value root, String[] path, int depth, String key, Value changed) {
        if (depth == path.length) return changed == null ? FoodWeaponExpectedTrace.remove(root, key) : FoodWeaponExpectedTrace.put(root, key, changed);
        String entry = path[depth];
        return FoodWeaponExpectedTrace.put(root, entry, replace(FoodWeaponExpectedTrace.field(root, entry), path, depth + 1, key, changed));
    }

    void bodyReturned() {
        caller();
        live();
        check(buffer != null && active == null && !mapPending && !bodyReturned && cursor == writes.size() && bufferImage.matches(buffer),
            "Component merge callback returned with missing writes or an altered buffer");
        bodyReturned = true;
    }

    CompoundTag assignmentRoot() {
        caller();
        live();
        check(installed(publishedRoot), "Component assignment lacks its completed original detached publication");
        return publishedRoot;
    }

    boolean installed(CompoundTag root) {
        caller();
        return thread == Thread.currentThread() && bodyReturned && active == null && !mapPending && cursor == writes.size()
            && root == publishedRoot && publishedImage != null && publishedImage.matches(root)
            && beforeImage.matches(before) && afterImage.matches(after);
    }

    private static void disjoint(Tag tag, IdentityHashMap<Object, Boolean> forbidden) {
        IdentityHashMap<Object, Boolean> actual = new IdentityHashMap<>();
        nodes(tag, actual);
        for (Object node : actual.keySet()) check(!forbidden.containsKey(node), "Component merge detached graph shares mutable input storage");
    }

    private static void nodes(Tag tag, IdentityHashMap<Object, Boolean> nodes) {
        NativeExpectedFieldObservation.nativeComponentMergeNodes391(tag, nodes);
    }

    private record Write(String[] path, String key, Tag changed, Value value) { }
}
