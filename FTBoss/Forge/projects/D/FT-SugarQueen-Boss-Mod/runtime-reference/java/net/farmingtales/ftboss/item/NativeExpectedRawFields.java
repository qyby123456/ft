/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ByteArrayTag
 *  net.minecraft.nbt.ByteTag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.DoubleTag
 *  net.minecraft.nbt.EndTag
 *  net.minecraft.nbt.FloatTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongArrayTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.ShortTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
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
    private final IdentityHashMap<Tag, Boolean> active = new IdentityHashMap();
    private int nodes;

    private NativeExpectedRawFields() {
    }

    static FoodWeaponExpectedTrace.Value observe(Tag tag) {
        return tag == null ? null : new NativeExpectedRawFields().value(tag, 0);
    }

    private FoodWeaponExpectedTrace.Value value(Tag tag, int n) {
        if (n > 64 || ++this.nodes > 65536 || this.active.put(tag, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("Raw field graph is cyclic or over budget");
        }
        try {
            Class<?> clazz = tag.getClass();
            if (clazz == CompoundTag.class) {
                Map<String, Tag> map = ((NativeExpectedFieldViews.Compound)tag).ftboss$rawCompoundFields();
                if (map == null || map.getClass() != HashMap.class || map.size() > 65536 - this.nodes) {
                    throw new IllegalArgumentException("Unsupported raw CompoundTag map implementation or size");
                }
                for (String stringArray2 : map.keySet()) {
                    if (stringArray2 != null) continue;
                    throw new IllegalArgumentException("Null raw tag key");
                }
                TreeMap<String, Tag> treeMap = new TreeMap<String, Tag>(map);
                String[] valueArray = (String[])treeMap.keySet().toArray(String[]::new);
                FoodWeaponExpectedTrace.Value[] i = new FoodWeaponExpectedTrace.Value[valueArray.length];
                for (int value = 0; value < valueArray.length; ++value) {
                    i[value] = this.value((Tag)treeMap.get(valueArray[value]), n + 1);
                }
                FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.compound(valueArray, i);
                return value;
            }
            if (clazz == ListTag.class) {
                NativeExpectedFieldViews.Sequence sequence = (NativeExpectedFieldViews.Sequence)tag;
                List<Tag> list = sequence.ftboss$rawListFields();
                if (list == null || list.getClass() != ArrayList.class || list.size() > 65536 - this.nodes) {
                    throw new IllegalArgumentException("Unsupported raw ListTag list implementation or size");
                }
                FoodWeaponExpectedTrace.Value[] lArray = new FoodWeaponExpectedTrace.Value[list.size()];
                for (int value = 0; value < lArray.length; ++value) {
                    lArray[value] = this.value(list.get(value), n + 1);
                }
                FoodWeaponExpectedTrace.Value i = FoodWeaponExpectedTrace.list(sequence.ftboss$rawListKind(), lArray);
                return i;
            }
            if (clazz == EndTag.class) {
                FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.end();
                return value;
            }
            if (clazz == ByteTag.class) {
                FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.number(1, ((NativeExpectedFieldViews.ByteValue)tag).ftboss$rawByte());
                return value;
            }
            if (clazz == ShortTag.class) {
                FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.number(2, ((NativeExpectedFieldViews.ShortValue)tag).ftboss$rawShort());
                return value;
            }
            if (clazz == IntTag.class) {
                FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.number(3, ((NativeExpectedFieldViews.IntValue)tag).ftboss$rawInt());
                return value;
            }
            if (clazz == LongTag.class) {
                FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.number(4, ((NativeExpectedFieldViews.LongValue)tag).ftboss$rawLong());
                return value;
            }
            if (clazz == FloatTag.class) {
                FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.number(5, Float.floatToRawIntBits(((NativeExpectedFieldViews.FloatValue)tag).ftboss$rawFloat()));
                return value;
            }
            if (clazz == DoubleTag.class) {
                FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.number(6, Double.doubleToRawLongBits(((NativeExpectedFieldViews.DoubleValue)tag).ftboss$rawDouble()));
                return value;
            }
            if (clazz == StringTag.class) {
                FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.string(((NativeExpectedFieldViews.TextValue)tag).ftboss$rawText());
                return value;
            }
            if (clazz == ByteArrayTag.class) {
                int n2;
                byte[] byArray = ((NativeExpectedFieldViews.Bytes)tag).ftboss$rawBytes();
                int n3 = n2 = byArray == null ? -1 : byArray.length;
                if (n2 < 0 || n2 > 0x100000) {
                    throw new IllegalArgumentException("Raw primitive array length");
                }
                long[] lArray = new long[n2];
                for (int value = 0; value < n2; ++value) {
                    lArray[value] = byArray[value];
                }
                FoodWeaponExpectedTrace.Value i = FoodWeaponExpectedTrace.array(7, lArray);
                return i;
            }
            if (clazz == IntArrayTag.class) {
                int n4;
                int[] nArray = ((NativeExpectedFieldViews.Ints)tag).ftboss$rawInts();
                int n5 = n4 = nArray == null ? -1 : nArray.length;
                if (n4 < 0 || n4 > 0x100000) {
                    throw new IllegalArgumentException("Raw primitive array length");
                }
                long[] value = new long[n4];
                for (int value2 = 0; value2 < n4; ++value2) {
                    value[value2] = nArray[value2];
                }
                FoodWeaponExpectedTrace.Value value3 = FoodWeaponExpectedTrace.array(11, value);
                return value3;
            }
            if (clazz == LongArrayTag.class) {
                int n6;
                long[] lArray = ((NativeExpectedFieldViews.Longs)tag).ftboss$rawLongs();
                int n7 = n6 = lArray == null ? -1 : lArray.length;
                if (n6 < 0 || n6 > 0x100000) {
                    throw new IllegalArgumentException("Raw primitive array length");
                }
                FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.array(12, lArray);
                return value;
            }
            throw new IllegalArgumentException("Unsupported exact raw Tag class");
        }
        finally {
            this.active.remove(tag);
        }
    }
}

