/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.Item
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;

public final class NativeExpectedFieldViews {
    private NativeExpectedFieldViews() {
    }

    public static interface Provider {
        public Object ftboss$rawDispatcher();

        public boolean ftboss$rawCapsValid();

        public boolean ftboss$rawCapsLazy();

        public boolean ftboss$rawCapsInitialized();

        public Object ftboss$rawCapsParentSupplier();

        public CompoundTag ftboss$rawCapsLazyData();
    }

    public static interface Actor {
        public CompoundTag ftboss$rawPersistentData();
    }

    public static interface Stack {
        public CompoundTag ftboss$rawStackTag();

        public CompoundTag ftboss$rawStackCaps();

        public int ftboss$rawStackCount();

        public Item ftboss$rawStackItem();
    }

    public static interface Longs {
        public long[] ftboss$rawLongs();
    }

    public static interface Ints {
        public int[] ftboss$rawInts();
    }

    public static interface Bytes {
        public byte[] ftboss$rawBytes();
    }

    public static interface TextValue {
        public String ftboss$rawText();
    }

    public static interface DoubleValue {
        public double ftboss$rawDouble();
    }

    public static interface FloatValue {
        public float ftboss$rawFloat();
    }

    public static interface LongValue {
        public long ftboss$rawLong();
    }

    public static interface IntValue {
        public int ftboss$rawInt();
    }

    public static interface ShortValue {
        public short ftboss$rawShort();
    }

    public static interface ByteValue {
        public byte ftboss$rawByte();
    }

    public static interface Sequence {
        public List<Tag> ftboss$rawListFields();

        public byte ftboss$rawListKind();
    }

    public static interface Compound {
        public Map<String, Tag> ftboss$rawCompoundFields();
    }
}

