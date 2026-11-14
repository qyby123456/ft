package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

final class NativeExpectedRawFields {
    private final IdentityHashMap<Tag, Boolean> active = new IdentityHashMap<>();
    private int nodes;

    private NativeExpectedRawFields() {
    }

    static FoodWeaponExpectedTrace.Value observe(Tag value) {
        return value == null ? null : new NativeExpectedRawFields().value(value, 0);
    }

    static CompoundTag materialPersistent(net.minecraft.server.level.ServerPlayer player) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerMaterialProcAuthority.class)
            throw new SecurityException("Authentic material raw observer required");
        return ((NativeExpectedFieldViews.Actor) player).ftboss$rawPersistentData();
    }

    static Tag materialChild(CompoundTag parent, String key) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerMaterialProcAuthority.class)
            throw new SecurityException("Authentic material raw observer required");
        return ((NativeExpectedFieldViews.Compound) parent).ftboss$rawCompoundFields().get(key);
    }

    private FoodWeaponExpectedTrace.Value value(Tag value, int depth) {
        if (depth > 64 || ++this.nodes > 65536 || this.active.put(value, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("Raw field graph is cyclic or over budget");
        }
        try {
            Class<?> type = value.getClass();
            if (type == CompoundTag.class) {
                Map<String, Tag> fields = ((NativeExpectedFieldViews.Compound) value).ftboss$rawCompoundFields();
                if (fields == null || (fields.getClass() != HashMap.class && !NativeFreshFactoryTrust298.readableMap(value, fields)) || fields.size() > 65536 - this.nodes) {
                    throw new IllegalArgumentException("Unsupported raw CompoundTag map implementation or size");
                }
                for (String key : fields.keySet()) {
                    if (key != null) continue;
                    throw new IllegalArgumentException("Null raw tag key");
                }
                TreeMap<String, Tag> sorted = new TreeMap<>(fields);
                String[] keys = sorted.keySet().toArray(String[]::new);
                FoodWeaponExpectedTrace.Value[] children = new FoodWeaponExpectedTrace.Value[keys.length];
                for (int index = 0; index < keys.length; ++index) {
                    children[index] = this.value(sorted.get(keys[index]), depth + 1);
                }
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.compound(keys, children);
                return result;
            }
            if (type == ListTag.class) {
                NativeExpectedFieldViews.Sequence sequence = (NativeExpectedFieldViews.Sequence) value;
                List<Tag> fields = sequence.ftboss$rawListFields();
                if (fields == null || fields.getClass() != ArrayList.class || fields.size() > 65536 - this.nodes) {
                    throw new IllegalArgumentException("Unsupported raw ListTag list implementation or size");
                }
                FoodWeaponExpectedTrace.Value[] children = new FoodWeaponExpectedTrace.Value[fields.size()];
                for (int index = 0; index < children.length; ++index) {
                    children[index] = this.value(fields.get(index), depth + 1);
                }
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.list(sequence.ftboss$rawListKind(), children);
                return result;
            }
            if (type == EndTag.class) {
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.end();
                return result;
            }
            if (type == ByteTag.class) {
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.number(1, ((NativeExpectedFieldViews.ByteValue) value).ftboss$rawByte());
                return result;
            }
            if (type == ShortTag.class) {
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.number(2, ((NativeExpectedFieldViews.ShortValue) value).ftboss$rawShort());
                return result;
            }
            if (type == IntTag.class) {
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.number(3, ((NativeExpectedFieldViews.IntValue) value).ftboss$rawInt());
                return result;
            }
            if (type == LongTag.class) {
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.number(4, ((NativeExpectedFieldViews.LongValue) value).ftboss$rawLong());
                return result;
            }
            if (type == FloatTag.class) {
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.number(5, Float.floatToRawIntBits(((NativeExpectedFieldViews.FloatValue) value).ftboss$rawFloat()));
                return result;
            }
            if (type == DoubleTag.class) {
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.number(6, Double.doubleToRawLongBits(((NativeExpectedFieldViews.DoubleValue) value).ftboss$rawDouble()));
                return result;
            }
            if (type == StringTag.class) {
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.string(((NativeExpectedFieldViews.TextValue) value).ftboss$rawText());
                return result;
            }
            if (type == ByteArrayTag.class) {
                byte[] values = ((NativeExpectedFieldViews.Bytes) value).ftboss$rawBytes();
                int length = values == null ? -1 : values.length;
                if (length < 0 || length > 0x100000) {
                    throw new IllegalArgumentException("Raw primitive array length");
                }
                long[] elements = new long[length];
                for (int index = 0; index < length; ++index) {
                    elements[index] = values[index];
                }
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.array(7, elements);
                return result;
            }
            if (type == IntArrayTag.class) {
                int[] values = ((NativeExpectedFieldViews.Ints) value).ftboss$rawInts();
                int length = values == null ? -1 : values.length;
                if (length < 0 || length > 0x100000) {
                    throw new IllegalArgumentException("Raw primitive array length");
                }
                long[] elements = new long[length];
                for (int index = 0; index < length; ++index) {
                    elements[index] = values[index];
                }
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.array(11, elements);
                return result;
            }
            if (type == LongArrayTag.class) {
                long[] values = ((NativeExpectedFieldViews.Longs) value).ftboss$rawLongs();
                int length = values == null ? -1 : values.length;
                if (length < 0 || length > 0x100000) {
                    throw new IllegalArgumentException("Raw primitive array length");
                }
                FoodWeaponExpectedTrace.Value result = FoodWeaponExpectedTrace.array(12, values);
                return result;
            }
            throw new IllegalArgumentException("Unsupported exact raw Tag class");
        } finally {
            this.active.remove(value);
        }
    }
    static java.util.Set<String> detachedKeys(CompoundTag compound) {
        if (compound.getClass() == CompoundTag.class) {
            Map<String, Tag> fields = ((NativeExpectedFieldViews.Compound)compound).ftboss$rawCompoundFields();
            if (fields != null && (fields.getClass() == HashMap.class || NativeFreshFactoryTrust298.readableMap(compound, fields))) {
                return java.util.Set.copyOf(fields.keySet());
            }
        }
        return java.util.Set.copyOf(compound.getAllKeys());
    }
}
