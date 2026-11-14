package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class NativeNbtGraphObservation {
    private final Tag root;
    private final Value fields;
    private final IdentityHashMap<Tag, Node> nodes = new IdentityHashMap<>();
    private final IdentityHashMap<Tag, Boolean> active = new IdentityHashMap<>();
    private int budget;

    private NativeNbtGraphObservation(Tag root) {
        this.root = root;
        visit(root, 0);
        this.fields = root == null ? null : NativeExpectedRawFields.observe(root);
        if (!unchangedNodes()) {
            throw new IllegalStateException("Native NBT graph changed during observation");
        }
    }

    public static NativeNbtGraphObservation capture(Tag root) {
        return new NativeNbtGraphObservation(root);
    }

    private void visit(Tag tag, int depth) {
        if (tag == null) {
            if (depth != 0) throw new IllegalArgumentException("Null native NBT graph child");
            return;
        }
        if (depth > 64 || ++this.budget > 65536) {
            throw new IllegalArgumentException("Native NBT graph budget exceeded");
        }
        if (this.active.containsKey(tag)) {
            throw new IllegalArgumentException("Cyclic native NBT graph");
        }
        if (this.nodes.containsKey(tag)) return;
        Class<?> type = tag.getClass();
        boolean immutable = type == ByteTag.class || type == ShortTag.class || type == IntTag.class
            || type == LongTag.class || type == FloatTag.class || type == DoubleTag.class
            || type == StringTag.class || type == EndTag.class;
        if (immutable) {
            this.nodes.put(tag, new Node(null, -1L, List.of(), List.of()));
            return;
        }
        if (!(tag instanceof NativeNbtMutationFields versioned)) {
            throw new IllegalStateException("Actual native NBT mutation Mixin is required");
        }
        if (versioned.ftboss$nativeNbtRawEscaped()) {
            throw new IllegalStateException("Previously escaped native NBT raw storage");
        }
        long version = versioned.ftboss$nativeNbtMutationVersion();
        if (version == Long.MAX_VALUE) {
            throw new IllegalStateException("Native NBT mutation version exhausted");
        }
        Object storage;
        List<String> keys = List.of();
        List<Tag> children = new ArrayList<>();
        if (type == CompoundTag.class && tag instanceof NativeExpectedFieldViews.Compound fields) {
            Map<String, Tag> actual = fields.ftboss$rawCompoundFields();
            if (actual == null || actual.size() > 65536 - this.budget) throw new IllegalArgumentException("Native NBT map budget");
            TreeMap<String, Tag> ordered = new TreeMap<>();
            for (Map.Entry<String, Tag> entry : actual.entrySet()) {
                if (entry.getKey() == null || entry.getKey().length() > 1048576 || entry.getValue() == null) {
                    throw new IllegalArgumentException("Invalid native NBT map entry");
                }
                ordered.put(entry.getKey(), entry.getValue());
            }
            storage = actual;
            keys = new ArrayList<>(ordered.keySet());
            children.addAll(ordered.values());
        } else if (type == ListTag.class && tag instanceof NativeExpectedFieldViews.Sequence fields) {
            List<Tag> actual = fields.ftboss$rawListFields();
            if (actual == null || actual.size() > 65536 - this.budget) throw new IllegalArgumentException("Native NBT list budget");
            storage = actual;
            children.addAll(actual);
        } else if (type == ByteArrayTag.class && tag instanceof NativeExpectedFieldViews.Bytes fields) {
            byte[] actual = fields.ftboss$rawBytes();
            if (actual == null || actual.length > 1048576) throw new IllegalArgumentException("Native NBT byte array budget");
            storage = actual;
        } else if (type == IntArrayTag.class && tag instanceof NativeExpectedFieldViews.Ints fields) {
            int[] actual = fields.ftboss$rawInts();
            if (actual == null || actual.length > 1048576) throw new IllegalArgumentException("Native NBT int array budget");
            storage = actual;
        } else if (type == LongArrayTag.class && tag instanceof NativeExpectedFieldViews.Longs fields) {
            long[] actual = fields.ftboss$rawLongs();
            if (actual == null || actual.length > 1048576) throw new IllegalArgumentException("Native NBT long array budget");
            storage = actual;
        } else {
            throw new IllegalArgumentException("Unsupported actual native NBT graph class");
        }
        this.nodes.put(tag, new Node(storage, version, keys, children));
        this.active.put(tag, Boolean.TRUE);
        try {
            for (Tag child : children) visit(child, depth + 1);
        } finally {
            this.active.remove(tag);
        }
    }

    private boolean unchangedNodes() {
        for (Map.Entry<Tag, Node> entry : this.nodes.entrySet()) {
            if (entry.getKey() instanceof NativeNbtMutationFields fields
                && (fields.ftboss$nativeNbtRawEscaped() || fields.ftboss$nativeNbtMutationVersion() != entry.getValue().version)) return false;
        }
        return true;
    }

    public boolean same(NativeNbtGraphObservation current) {
        if (current == null || this.root != current.root || this.nodes.size() != current.nodes.size()
            || !FoodWeaponExpectedTrace.same(this.fields, current.fields) || !unchangedNodes() || !current.unchangedNodes()) return false;
        for (Map.Entry<Tag, Node> entry : this.nodes.entrySet()) {
            Node before = entry.getValue();
            Node after = current.nodes.get(entry.getKey());
            if (after == null || before.storage != after.storage || before.version != after.version
                || !before.keys.equals(after.keys) || before.children.size() != after.children.size()) return false;
            for (int index = 0; index < before.children.size(); index++) {
                if (before.children.get(index) != after.children.get(index)) return false;
            }
        }
        return true;
    }

    public boolean matches(Tag root) {
        if (!unchangedNodes()) return false;
        return same(capture(root));
    }

    private record Node(Object storage, long version, List<String> keys, List<Tag> children) {
    }
}
