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
import java.util.Arrays;
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

final class FoodWeaponExpectedTrace {
    static final int END = 0;
    static final int BYTE = 1;
    static final int SHORT = 2;
    static final int INT = 3;
    static final int LONG = 4;
    static final int FLOAT = 5;
    static final int DOUBLE = 6;
    static final int BYTE_ARRAY = 7;
    static final int STRING = 8;
    static final int LIST = 9;
    static final int COMPOUND = 10;
    static final int INT_ARRAY = 11;
    static final int LONG_ARRAY = 12;
    static final int MAX_NODES = 65536;
    static final int MAX_DEPTH = 64;
    private static final Value EMPTY = new Value(10, 0L, null, new String[0], new Value[0], null);

    private FoodWeaponExpectedTrace() {
    }

    static String[] compoundKeys(Value value) {
        if (value == null || value.kind != 10) {
            throw new IllegalArgumentException("Expected compound keys");
        }
        return (String[])value.keys.clone();
    }

    static Value remove(Value value, String string) {
        if (value == null || value.kind != 10 || string == null) {
            throw new IllegalArgumentException("Expected compound removal");
        }
        int n = Arrays.binarySearch(value.keys, string);
        if (n < 0) {
            return value;
        }
        String[] stringArray = new String[value.keys.length - 1];
        Value[] valueArray = new Value[stringArray.length];
        System.arraycopy(value.keys, 0, stringArray, 0, n);
        System.arraycopy(value.children, 0, valueArray, 0, n);
        System.arraycopy(value.keys, n + 1, stringArray, n, stringArray.length - n);
        System.arraycopy(value.children, n + 1, valueArray, n, valueArray.length - n);
        return FoodWeaponExpectedTrace.compound(stringArray, valueArray);
    }

    static Tag materialize(Value value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing immutable field image");
        }
        return switch (value.kind) {
            case 0 -> EndTag.f_128534_;
            case 1 -> ByteTag.m_128266_((byte)((byte)value.bits));
            case 2 -> ShortTag.m_129258_((short)((short)value.bits));
            case 3 -> IntTag.m_128679_((int)((int)value.bits));
            case 4 -> LongTag.m_128882_((long)value.bits);
            case 5 -> FloatTag.m_128566_((float)Float.intBitsToFloat((int)value.bits));
            case 6 -> DoubleTag.m_128500_((double)Double.longBitsToDouble(value.bits));
            case 8 -> StringTag.m_129297_((String)value.string);
            case 7 -> {
                byte[] var1_1 = new byte[value.elements.length];
                for (int var2_5 = 0; var2_5 < var1_1.length; ++var2_5) {
                    var1_1[var2_5] = (byte)value.elements[var2_5];
                }
                yield new ByteArrayTag(var1_1);
            }
            case 11 -> {
                int[] var1_2 = new int[value.elements.length];
                for (int var2_6 = 0; var2_6 < var1_2.length; ++var2_6) {
                    var1_2[var2_6] = (int)value.elements[var2_6];
                }
                yield new IntArrayTag(var1_2);
            }
            case 12 -> new LongArrayTag((long[])value.elements.clone());
            case 9 -> {
                if (value.children.length == 0 && value.bits != 0L) {
                    throw new IllegalArgumentException("Unsupported typed empty image list");
                }
                ListTag var1_3 = new ListTag();
                for (Value var5_11 : value.children) {
                    var1_3.add((Object)FoodWeaponExpectedTrace.materialize(var5_11));
                }
                yield var1_3;
            }
            case 10 -> {
                CompoundTag var1_4 = new CompoundTag();
                for (int var2_8 = 0; var2_8 < value.keys.length; ++var2_8) {
                    var1_4.m_128365_(value.keys[var2_8], FoodWeaponExpectedTrace.materialize(value.children[var2_8]));
                }
                yield var1_4;
            }
            default -> throw new IllegalArgumentException("Unknown immutable field image kind");
        };
    }

    static Value emptyCompound() {
        return EMPTY;
    }

    static Value end() {
        return new Value(0, 0L, null, null, null, null);
    }

    static Value number(int n, long l) {
        if (n < 1 || n > 6) {
            throw new IllegalArgumentException("Expected numeric kind");
        }
        long l2 = switch (n) {
            case 1 -> (byte)l;
            case 2 -> (short)l;
            case 3 -> (int)l;
            case 5 -> l & 0xFFFFFFFFL;
            default -> l;
        };
        return new Value(n, l2, null, null, null, null);
    }

    static Value string(String string) {
        if (string == null || string.length() > 0x100000) {
            throw new IllegalArgumentException("Expected string");
        }
        return new Value(8, 0L, string, null, null, null);
    }

    static Value array(int n, long[] lArray) {
        if (lArray == null || lArray.length > 0x100000 || n != 7 && n != 11 && n != 12) {
            throw new IllegalArgumentException("Expected array");
        }
        long[] lArray2 = (long[])lArray.clone();
        for (int i = 0; i < lArray2.length; ++i) {
            if (n == 7) {
                lArray2[i] = (byte)lArray2[i];
                continue;
            }
            if (n != 11) continue;
            lArray2[i] = (int)lArray2[i];
        }
        return new Value(n, 0L, null, null, null, lArray2);
    }

    static Value list(int n, Value[] valueArray) {
        Value[] valueArray2;
        if (valueArray == null || valueArray.length > 65536 || n < 0 || n > 12) {
            throw new IllegalArgumentException("Expected list");
        }
        for (Value value : valueArray2 = (Value[])valueArray.clone()) {
            if (value != null && value.kind == n) continue;
            throw new IllegalArgumentException("Expected homogeneous list");
        }
        return new Value(9, n, null, null, valueArray2, null);
    }

    static Value compound(String[] stringArray, Value[] valueArray) {
        if (stringArray == null || valueArray == null || stringArray.length != valueArray.length || stringArray.length > 65536) {
            throw new IllegalArgumentException("Expected compound");
        }
        String[] stringArray2 = (String[])stringArray.clone();
        Value[] valueArray2 = (Value[])valueArray.clone();
        for (int i = 0; i < stringArray2.length; ++i) {
            if (stringArray2[i] == null || stringArray2[i].length() > 0x100000 || valueArray2[i] == null) {
                throw new IllegalArgumentException("Expected compound field");
            }
            if (i <= 0 || stringArray2[i - 1].compareTo(stringArray2[i]) < 0) continue;
            throw new IllegalArgumentException("Expected strictly sorted unique keys");
        }
        return new Value(10, 0L, null, stringArray2, valueArray2, null);
    }

    static Value field(Value value, String string) {
        if (value == null || value.kind != 10) {
            return null;
        }
        int n = Arrays.binarySearch(value.keys, string);
        return n < 0 ? null : value.children[n];
    }

    static boolean same(Value value, Value value2) {
        if (value == value2) {
            return true;
        }
        if (value == null || value2 == null || value.kind != value2.kind || value.bits != value2.bits) {
            return false;
        }
        if (value.string != null && !value.string.equals(value2.string)) {
            return false;
        }
        if (!Arrays.equals(value.keys, value2.keys) || !Arrays.equals(value.elements, value2.elements)) {
            return false;
        }
        if (value.children == null || value2.children == null) {
            return value.children == value2.children;
        }
        if (value.children.length != value2.children.length) {
            return false;
        }
        for (int i = 0; i < value.children.length; ++i) {
            if (FoodWeaponExpectedTrace.same(value.children[i], value2.children[i])) continue;
            return false;
        }
        return true;
    }

    static Value put(Value value, String string, Value value2) {
        if (value == null || value.kind != 10 || string == null || value2 == null) {
            throw new IllegalArgumentException("Expected compound replacement");
        }
        int n = Arrays.binarySearch(value.keys, string);
        int n2 = n < 0 ? -n - 1 : n;
        String[] stringArray = new String[value.keys.length + (n < 0 ? 1 : 0)];
        Value[] valueArray = new Value[stringArray.length];
        System.arraycopy(value.keys, 0, stringArray, 0, n2);
        System.arraycopy(value.children, 0, valueArray, 0, n2);
        stringArray[n2] = string;
        valueArray[n2] = value2;
        int n3 = value.keys.length - n2 - (n < 0 ? 0 : 1);
        System.arraycopy(value.keys, n2 + (n < 0 ? 0 : 1), stringArray, n2 + 1, n3);
        System.arraycopy(value.children, n2 + (n < 0 ? 0 : 1), valueArray, n2 + 1, n3);
        return FoodWeaponExpectedTrace.compound(stringArray, valueArray);
    }

    static Value compoundAt(Value value, String[] stringArray) {
        Value value2 = value;
        for (String string : stringArray) {
            value2 = FoodWeaponExpectedTrace.field(value2, string);
        }
        return value2 != null && value2.kind == 10 ? value2 : EMPTY;
    }

    private static Value replaceAt(Value value, String[] stringArray, int n, Value value2) {
        if (n == stringArray.length) {
            return value2;
        }
        Value value3 = value != null && value.kind == 10 ? value : EMPTY;
        return FoodWeaponExpectedTrace.put(value3, stringArray[n], FoodWeaponExpectedTrace.replaceAt(FoodWeaponExpectedTrace.field(value3, stringArray[n]), stringArray, n + 1, value2));
    }

    static final class Value {
        final int kind;
        final long bits;
        final String string;
        private final String[] keys;
        private final Value[] children;
        private final long[] elements;
        final int depth;
        final int nodes;

        private Value(int n, long l, String string, String[] stringArray, Value[] valueArray, long[] lArray) {
            this.kind = n;
            this.bits = l;
            this.string = string;
            this.keys = stringArray;
            this.children = valueArray;
            this.elements = lArray;
            int n2 = 1;
            int n3 = 1;
            if (valueArray != null) {
                for (Value value : valueArray) {
                    n2 = Math.max(n2, value.depth + 1);
                    n3 = Math.addExact(n3, value.nodes);
                }
            }
            if (n2 > 65 || n3 > 65536) {
                throw new IllegalArgumentException("Expected field budget");
            }
            this.depth = n2;
            this.nodes = n3;
        }
    }

    static final class Plan {
        private final Operation[] operations;
        final State expectedAfter;

        private Plan(Operation[] operationArray, State state) {
            this.operations = (Operation[])operationArray.clone();
            this.expectedAfter = state;
        }

        int size() {
            return this.operations.length;
        }

        State before() {
            return this.operations.length == 0 ? this.expectedAfter : this.operations[0].before;
        }

        Operation operation(int n) {
            return this.operations[n];
        }
    }

    static final class Builder {
        private State expected;
        private final ArrayList<Operation> operations = new ArrayList();
        private boolean sealed;

        Builder(State state) {
            if (state == null) {
                throw new IllegalArgumentException("Independent expected BEFORE required");
            }
            this.expected = state;
        }

        private void writable() {
            if (this.sealed || this.operations.size() >= 65536) {
                throw new IllegalStateException("Expected trace already sealed or full");
            }
        }

        Operation timer(String string, String string2, Value value) {
            Object object;
            Object object2;
            Object object3;
            this.writable();
            if (string2 == null || value == null || string != null && string.length() > 1024) {
                throw new IllegalArgumentException("Independent timer intent required");
            }
            State state = this.expected;
            ArrayList<Object> arrayList = new ArrayList<Object>();
            Value value2 = this.expected.weapon;
            Value value3 = FoodWeaponExpectedTrace.field(value2, "tag");
            if (value3 == null || value3.kind != 10) {
                object3 = new Mutation(Domain.WEAPON, value2, new String[0], "tag", EMPTY);
                arrayList.add(object3);
                value2 = ((Mutation)object3).after;
            }
            if ((object3 = FoodWeaponExpectedTrace.field(FoodWeaponExpectedTrace.field(value2, "tag"), "FTBossFoodEvolution")) == null || ((Value)object3).kind != 10) {
                object2 = new Mutation(Domain.WEAPON, value2, new String[]{"tag"}, "FTBossFoodEvolution", EMPTY);
                arrayList.add(object2);
                value2 = ((Mutation)object2).after;
            }
            object2 = FoodWeaponExpectedTrace.field(FoodWeaponExpectedTrace.compoundAt(value2, new String[]{"tag", "FTBossFoodEvolution"}), "Profile");
            if (string != null && (object2 == null || ((Value)object2).kind != 8)) {
                object = new Mutation(Domain.WEAPON, value2, new String[]{"tag", "FTBossFoodEvolution"}, "Profile", FoodWeaponExpectedTrace.string(string));
                arrayList.add(object);
                value2 = ((Mutation)object).after;
            }
            object = state.replace(Domain.WEAPON, value2);
            Mutation mutation = new Mutation(Domain.WEAPON, value2, new String[]{"tag", "FTBossFoodEvolution"}, string2, value);
            State state2 = ((State)object).replace(Domain.WEAPON, mutation.after);
            Operation operation = new Operation(this.operations.size(), Kind.TIMER, state, (State)object, state2, string, string2, value, (Mutation[])arrayList.toArray(Mutation[]::new), mutation);
            this.operations.add(operation);
            this.expected = state2;
            return operation;
        }

        private void requireRangedAcquisition() {
            if (!this.expected.rangedAcquired || this.expected.rangedPlayerIdentity == null || this.expected.rangedBufferIdentity == null) {
                throw new IllegalArgumentException("Unsupported ranged plan without independent original getter identities");
            }
        }

        Operation rangedAcquire(Object object, Object object2) {
            this.writable();
            Value value = FoodWeaponExpectedTrace.field(this.expected.player, "FTBossRangedProcTimers");
            if (object == null || object2 == null || value == null || value.kind != 10 || this.expected.rangedPlayerIdentity != null && this.expected.rangedPlayerIdentity != object) {
                throw new IllegalArgumentException("Unplanned original ranged allocation/root identity is unsupported off-cut");
            }
            State state = new State(this.expected.weapon, this.expected.player, this.expected.carrier, value, true, object, object2, true);
            Operation operation = new Operation(this.operations.size(), Kind.RANGED_ACQUIRE, this.expected, this.expected, state, null, "FTBossRangedProcTimers", value, new Mutation[0], null);
            this.operations.add(operation);
            this.expected = state;
            return operation;
        }

        Operation rangedLong(String string, long l) {
            Value value = FoodWeaponExpectedTrace.field(this.expected.rangedBuffer, string);
            if (value != null && (value.kind == 5 || value.kind == 6)) {
                throw new IllegalArgumentException("Independent installed-SDK numeric conversion intent required");
            }
            long l2 = value == null || value.kind < 1 || value.kind > 4 ? 0L : value.bits;
            return this.rangedLong(string, l, l2);
        }

        Operation rangedLong(String string, long l, long l2) {
            this.writable();
            this.requireRangedAcquisition();
            if (string == null || !string.startsWith("Last")) {
                throw new IllegalArgumentException("Original ranged timer key");
            }
            Value value = FoodWeaponExpectedTrace.field(this.expected.rangedBuffer, string);
            if (value == null || value.kind < 1 || value.kind > 6) {
                if (l2 != 0L) {
                    throw new IllegalArgumentException("Expected nonnumeric conversion is zero");
                }
            } else if (value.kind <= 4 && l2 != value.bits) {
                throw new IllegalArgumentException("Expected integral conversion differs");
            }
            Value value2 = FoodWeaponExpectedTrace.number(4, Math.max(l, l2));
            Mutation mutation = new Mutation(Domain.RANGED_BUFFER, this.expected.rangedBuffer, new String[0], string, value2);
            return this.append(Kind.RANGED_TIMER, Domain.RANGED_BUFFER, string, value2, mutation);
        }

        Operation rangedRoot() {
            this.writable();
            this.requireRangedAcquisition();
            Value value = this.expected.rangedBuffer;
            Mutation mutation = new Mutation(Domain.PLAYER, this.expected.player, new String[0], "FTBossRangedProcTimers", value);
            State state = new State(this.expected.weapon, mutation.after, this.expected.carrier, value, true, this.expected.rangedPlayerIdentity, this.expected.rangedBufferIdentity, true);
            Operation operation = new Operation(this.operations.size(), Kind.RANGED_ROOT, this.expected, this.expected, state, null, "FTBossRangedProcTimers", value, new Mutation[0], mutation);
            this.operations.add(operation);
            this.expected = state;
            return operation;
        }

        Operation nativePut(Domain domain, String[] stringArray, String string, Value value) {
            this.writable();
            if (domain == null || stringArray == null || string == null || value == null) {
                throw new IllegalArgumentException("Independent native mutation intent required");
            }
            if (domain == Domain.RANGED_BUFFER) {
                this.requireRangedAcquisition();
            }
            Value value2 = this.expected.image(domain);
            for (String string2 : stringArray) {
                value2 = FoodWeaponExpectedTrace.field(value2, string2);
            }
            if (value2 == null || value2.kind != 10) {
                throw new IllegalArgumentException("Native receiver must independently exist");
            }
            Mutation mutation = new Mutation(domain, this.expected.image(domain), stringArray, string, value);
            return this.append(Kind.NATIVE_PUT, domain, string, value, mutation);
        }

        Operation nativeCapture() {
            this.writable();
            Operation operation = new Operation(this.operations.size(), Kind.NATIVE_CAPTURE, this.expected, this.expected, this.expected, null, null, null, new Mutation[0], null);
            this.operations.add(operation);
            return operation;
        }

        private Operation append(Kind kind, Domain domain, String string, Value value, Mutation mutation) {
            boolean bl = mutation.path.length == 0 && "FTBossRangedProcTimers".equals(string);
            boolean bl2 = mutation.path.length > 0 && "FTBossRangedProcTimers".equals(mutation.path[0]);
            State state = domain == Domain.PLAYER ? this.expected.replacePlayer(mutation.after, bl, bl2) : this.expected.replace(domain, mutation.after);
            Operation operation = new Operation(this.operations.size(), kind, this.expected, this.expected, state, null, string, value, new Mutation[0], mutation);
            this.operations.add(operation);
            this.expected = state;
            return operation;
        }

        Plan seal() {
            this.writable();
            this.sealed = true;
            return new Plan((Operation[])this.operations.toArray(Operation[]::new), this.expected);
        }
    }

    static final class Operation {
        final int ordinal;
        final Kind kind;
        final State before;
        final State entry;
        final State after;
        final String profile;
        final String key;
        final Value argument;
        private final Mutation[] prefixes;
        final Mutation write;

        private Operation(int n, Kind kind, State state, State state2, State state3, String string, String string2, Value value, Mutation[] mutationArray, Mutation mutation) {
            this.ordinal = n;
            this.kind = kind;
            this.before = state;
            this.entry = state2;
            this.after = state3;
            this.profile = string;
            this.key = string2;
            this.argument = value;
            this.prefixes = (Mutation[])mutationArray.clone();
            this.write = mutation;
        }

        int prefixCount() {
            return this.prefixes.length;
        }

        Mutation prefix(int n) {
            return this.prefixes[n];
        }
    }

    static final class Mutation {
        final Domain domain;
        final Value before;
        final Value after;
        final Value value;
        final String key;
        private final String[] path;

        private Mutation(Domain domain, Value value, String[] stringArray, String string, Value value2) {
            this.domain = domain;
            this.before = value;
            this.path = (String[])stringArray.clone();
            this.key = string;
            this.value = value2;
            Value value3 = FoodWeaponExpectedTrace.compoundAt(value, this.path);
            this.after = FoodWeaponExpectedTrace.replaceAt(value, this.path, 0, FoodWeaponExpectedTrace.put(value3, string, value2));
        }

        boolean matches(Domain domain, String[] stringArray, String string, Value value) {
            return this.domain == domain && Arrays.equals(this.path, stringArray) && this.key.equals(string) && FoodWeaponExpectedTrace.same(this.value, value);
        }
    }

    static final class State {
        final Value weapon;
        final Value player;
        final Value carrier;
        final Value rangedBuffer;
        final boolean rangedAttached;
        final Object rangedPlayerIdentity;
        final Object rangedBufferIdentity;
        final boolean rangedAcquired;

        State(Value value, Value value2, Value value3) {
            this(value, value2, value3, FoodWeaponExpectedTrace.compoundAt(value2, new String[]{"FTBossRangedProcTimers"}), FoodWeaponExpectedTrace.field(value2, "FTBossRangedProcTimers") != null && FoodWeaponExpectedTrace.field((Value)value2, (String)"FTBossRangedProcTimers").kind == 10);
        }

        private State(Value value, Value value2, Value value3, Value value4, boolean bl) {
            this(value, value2, value3, value4, bl, null, null, false);
        }

        private State(Value value, Value value2, Value value3, Value value4, boolean bl, Object object, Object object2, boolean bl2) {
            if (value == null || value2 == null || value3 == null || value.kind != 10 || value2.kind != 10 || value3.kind != 10) {
                throw new IllegalArgumentException("Expected three field domains");
            }
            this.weapon = value;
            this.player = value2;
            this.carrier = value3;
            this.rangedBuffer = value4;
            this.rangedAttached = bl;
            this.rangedPlayerIdentity = object;
            this.rangedBufferIdentity = object2;
            this.rangedAcquired = bl2;
        }

        Value image(Domain domain) {
            return switch (domain) {
                default -> throw new IncompatibleClassChangeError();
                case Domain.WEAPON -> this.weapon;
                case Domain.PLAYER -> this.player;
                case Domain.CARRIER -> this.carrier;
                case Domain.RANGED_BUFFER -> this.rangedBuffer;
            };
        }

        State replace(Domain domain, Value value) {
            if (!(domain != Domain.RANGED_BUFFER || this.rangedAcquired && this.rangedPlayerIdentity != null && this.rangedBufferIdentity != null)) {
                throw new IllegalArgumentException("Original ranged acquisition identities required before buffer mutation");
            }
            return switch (domain) {
                default -> throw new IncompatibleClassChangeError();
                case Domain.WEAPON -> new State(value, this.player, this.carrier, this.rangedBuffer, this.rangedAttached, this.rangedPlayerIdentity, this.rangedBufferIdentity, this.rangedAcquired);
                case Domain.PLAYER -> this.replacePlayer(value, true, false);
                case Domain.CARRIER -> new State(this.weapon, this.player, value, this.rangedBuffer, this.rangedAttached, this.rangedPlayerIdentity, this.rangedBufferIdentity, this.rangedAcquired);
                case Domain.RANGED_BUFFER -> new State(this.weapon, this.rangedAttached ? FoodWeaponExpectedTrace.put(this.player, "FTBossRangedProcTimers", value) : this.player, this.carrier, value, this.rangedAttached, this.rangedPlayerIdentity, this.rangedBufferIdentity, this.rangedAcquired);
            };
        }

        private State replacePlayer(Value value, boolean bl, boolean bl2) {
            boolean bl3 = this.rangedAttached && !bl;
            Value value2 = bl3 && bl2 ? FoodWeaponExpectedTrace.compoundAt(value, new String[]{"FTBossRangedProcTimers"}) : this.rangedBuffer;
            return new State(this.weapon, value, this.carrier, value2, bl3, this.rangedPlayerIdentity, this.rangedBufferIdentity, this.rangedAcquired);
        }
    }

    static enum Kind {
        TIMER,
        RANGED_TIMER,
        RANGED_ROOT,
        NATIVE_PUT,
        NATIVE_CAPTURE,
        RANGED_ACQUIRE;

    }

    static enum Domain {
        WEAPON,
        PLAYER,
        CARRIER,
        RANGED_BUFFER;

    }
}

