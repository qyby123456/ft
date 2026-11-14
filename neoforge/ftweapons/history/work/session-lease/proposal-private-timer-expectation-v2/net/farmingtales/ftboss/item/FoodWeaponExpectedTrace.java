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
        int kindValue = Arrays.binarySearch(value.keys, string);
        if (kindValue < 0) {
            return value;
        }
        String[] stringArray = new String[value.keys.length - 1];
        Value[] valueArray = new Value[stringArray.length];
        System.arraycopy(value.keys, 0, stringArray, 0, kindValue);
        System.arraycopy(value.children, 0, valueArray, 0, kindValue);
        System.arraycopy(value.keys, kindValue + 1, stringArray, kindValue, stringArray.length - kindValue);
        System.arraycopy(value.children, kindValue + 1, valueArray, kindValue, valueArray.length - kindValue);
        return FoodWeaponExpectedTrace.compound(stringArray, valueArray);
    }

    static Tag materialize(Value value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing immutable field image");
        }
        return switch (value.kind) {
            case 0 -> EndTag.INSTANCE;
            case 1 -> ByteTag.valueOf((byte)((byte)value.bits));
            case 2 -> ShortTag.valueOf((short)((short)value.bits));
            case 3 -> IntTag.valueOf((int)((int)value.bits));
            case 4 -> LongTag.valueOf((long)value.bits);
            case 5 -> FloatTag.valueOf((float)Float.intBitsToFloat((int)value.bits));
            case 6 -> DoubleTag.valueOf((double)Double.longBitsToDouble(value.bits));
            case 8 -> StringTag.valueOf((String)value.string);
            case 7 -> {
                byte[] bytes = new byte[value.elements.length];
                for (int byteIndex = 0; byteIndex < bytes.length; ++byteIndex) {
                    bytes[byteIndex] = (byte)value.elements[byteIndex];
                }
                yield new ByteArrayTag(bytes);
            }
            case 11 -> {
                int[] integers = new int[value.elements.length];
                for (int integerIndex = 0; integerIndex < integers.length; ++integerIndex) {
                    integers[integerIndex] = (int)value.elements[integerIndex];
                }
                yield new IntArrayTag(integers);
            }
            case 12 -> new LongArrayTag((long[])value.elements.clone());
            case 9 -> {
                if (value.children.length == 0 && value.bits != 0L) {
                    throw new IllegalArgumentException("Unsupported typed empty image list");
                }
                ListTag list = new ListTag();
                for (Value element : value.children) {
                    list.add(FoodWeaponExpectedTrace.materialize(element));
                }
                yield list;
            }
            case 10 -> {
                CompoundTag compound = new CompoundTag();
                for (int fieldIndex = 0; fieldIndex < value.keys.length; ++fieldIndex) {
                    compound.put(value.keys[fieldIndex], FoodWeaponExpectedTrace.materialize(value.children[fieldIndex]));
                }
                yield compound;
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

    static Value number(int kindValue, long longValue) {
        if (kindValue < 1 || kindValue > 6) {
            throw new IllegalArgumentException("Expected numeric kind");
        }
        long priorLong = switch (kindValue) {
            case 1 -> (byte)longValue;
            case 2 -> (short)longValue;
            case 3 -> (int)longValue;
            case 5 -> longValue & 0xFFFFFFFFL;
            default -> longValue;
        };
        return new Value(kindValue, priorLong, null, null, null, null);
    }

    static Value string(String string) {
        if (string == null || string.length() > 0x100000) {
            throw new IllegalArgumentException("Expected string");
        }
        return new Value(8, 0L, string, null, null, null);
    }

    static Value array(int kindValue, long[] lArray) {
        if (lArray == null || lArray.length > 0x100000 || kindValue != 7 && kindValue != 11 && kindValue != 12) {
            throw new IllegalArgumentException("Expected array");
        }
        long[] lArray2 = (long[])lArray.clone();
        for (int index = 0; index < lArray2.length; ++index) {
            if (kindValue == 7) {
                lArray2[index] = (byte)lArray2[index];
                continue;
            }
            if (kindValue != 11) continue;
            lArray2[index] = (int)lArray2[index];
        }
        return new Value(kindValue, 0L, null, null, null, lArray2);
    }

    static Value list(int kindValue, Value[] valueArray) {
        Value[] valueArray2;
        if (valueArray == null || valueArray.length > 65536 || kindValue < 0 || kindValue > 12) {
            throw new IllegalArgumentException("Expected list");
        }
        for (Value value : valueArray2 = (Value[])valueArray.clone()) {
            if (value != null && value.kind == kindValue) continue;
            throw new IllegalArgumentException("Expected homogeneous list");
        }
        return new Value(9, kindValue, null, null, valueArray2, null);
    }

    static Value compound(String[] stringArray, Value[] valueArray) {
        if (stringArray == null || valueArray == null || stringArray.length != valueArray.length || stringArray.length > 65536) {
            throw new IllegalArgumentException("Expected compound");
        }
        String[] stringArray2 = (String[])stringArray.clone();
        Value[] valueArray2 = (Value[])valueArray.clone();
        for (int index = 0; index < stringArray2.length; ++index) {
            if (stringArray2[index] == null || stringArray2[index].length() > 0x100000 || valueArray2[index] == null) {
                throw new IllegalArgumentException("Expected compound field");
            }
            if (index <= 0 || stringArray2[index - 1].compareTo(stringArray2[index]) < 0) continue;
            throw new IllegalArgumentException("Expected strictly sorted unique keys");
        }
        return new Value(10, 0L, null, stringArray2, valueArray2, null);
    }

    static Value field(Value value, String string) {
        if (value == null || value.kind != 10) {
            return null;
        }
        int kindValue = Arrays.binarySearch(value.keys, string);
        return kindValue < 0 ? null : value.children[kindValue];
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
        for (int index = 0; index < value.children.length; ++index) {
            if (FoodWeaponExpectedTrace.same(value.children[index], value2.children[index])) continue;
            return false;
        }
        return true;
    }

    static Value put(Value value, String string, Value value2) {
        if (value == null || value.kind != 10 || string == null || value2 == null) {
            throw new IllegalArgumentException("Expected compound replacement");
        }
        int kindValue = Arrays.binarySearch(value.keys, string);
        int secondValue = kindValue < 0 ? -kindValue - 1 : kindValue;
        String[] stringArray = new String[value.keys.length + (kindValue < 0 ? 1 : 0)];
        Value[] valueArray = new Value[stringArray.length];
        System.arraycopy(value.keys, 0, stringArray, 0, secondValue);
        System.arraycopy(value.children, 0, valueArray, 0, secondValue);
        stringArray[secondValue] = string;
        valueArray[secondValue] = value2;
        int thirdValue = value.keys.length - secondValue - (kindValue < 0 ? 0 : 1);
        System.arraycopy(value.keys, secondValue + (kindValue < 0 ? 0 : 1), stringArray, secondValue + 1, thirdValue);
        System.arraycopy(value.children, secondValue + (kindValue < 0 ? 0 : 1), valueArray, secondValue + 1, thirdValue);
        return FoodWeaponExpectedTrace.compound(stringArray, valueArray);
    }

    static Value compoundAt(Value value, String[] stringArray) {
        Value value2 = value;
        for (String string : stringArray) {
            value2 = FoodWeaponExpectedTrace.field(value2, string);
        }
        return value2 != null && value2.kind == 10 ? value2 : EMPTY;
    }

    private static Value replaceAt(Value value, String[] stringArray, int kindValue, Value value2) {
        if (kindValue == stringArray.length) {
            return value2;
        }
        Value value3 = value != null && value.kind == 10 ? value : EMPTY;
        return FoodWeaponExpectedTrace.put(value3, stringArray[kindValue], FoodWeaponExpectedTrace.replaceAt(FoodWeaponExpectedTrace.field(value3, stringArray[kindValue]), stringArray, kindValue + 1, value2));
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

        private Value(int kindValue, long longValue, String string, String[] stringArray, Value[] valueArray, long[] lArray) {
            this.kind = kindValue;
            this.bits = longValue;
            this.string = string;
            this.keys = stringArray;
            this.children = valueArray;
            this.elements = lArray;
            int secondValue = 1;
            int thirdValue = 1;
            if (valueArray != null) {
                for (Value value : valueArray) {
                    secondValue = Math.max(secondValue, value.depth + 1);
                    thirdValue = Math.addExact(thirdValue, value.nodes);
                }
            }
            if (secondValue > 65 || thirdValue > 65536) {
                throw new IllegalArgumentException("Expected field budget");
            }
            this.depth = secondValue;
            this.nodes = thirdValue;
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

        Operation operation(int kindValue) {
            return this.operations[kindValue];
        }
    }

    static final class Builder {
        private State expected;
        private final ArrayList<Operation> operations = new ArrayList<>();
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

        Operation nativeTimer(String profile,String key,Value argument) {
            this.writable();
            if (key == null || argument == null || profile != null && profile.length() > 1024) throw new IllegalArgumentException("Independent native timer intent required");
            State before = this.expected;
            Value weapon = before.weapon;
            Value id = field(weapon,"id"), count = field(weapon,"count"), components = field(weapon,"components");
            if (id == null || id.kind != STRING || count == null || count.kind != INT
                    || field(weapon,"tag") != null || field(weapon,"Count") != null || field(weapon,"ForgeCaps") != null
                    || components != null && components.kind != COMPOUND) throw new IllegalArgumentException("Actual native id/count/components expected domain required");
            ArrayList<Mutation> prefixes = new ArrayList<>();
            if (components == null) {
                Mutation prefix = new Mutation(Domain.WEAPON,weapon,new String[0],"components",EMPTY);
                prefixes.add(prefix); weapon = prefix.after;
            }
            Value custom = field(field(weapon,"components"),"minecraft:custom_data");
            if (custom == null || custom.kind != COMPOUND) {
                Mutation prefix = new Mutation(Domain.WEAPON,weapon,new String[]{"components"},"minecraft:custom_data",EMPTY);
                prefixes.add(prefix); weapon = prefix.after;
            }
            Value evolution = field(field(field(weapon,"components"),"minecraft:custom_data"),"FTBossFoodEvolution");
            if (evolution == null || evolution.kind != COMPOUND) {
                Mutation prefix = new Mutation(Domain.WEAPON,weapon,new String[]{"components","minecraft:custom_data"},"FTBossFoodEvolution",EMPTY);
                prefixes.add(prefix); weapon = prefix.after;
            }
            Value installed = field(compoundAt(weapon,new String[]{"components","minecraft:custom_data","FTBossFoodEvolution"}),"Profile");
            if (profile != null && (installed == null || installed.kind != STRING)) {
                Mutation prefix = new Mutation(Domain.WEAPON,weapon,new String[]{"components","minecraft:custom_data","FTBossFoodEvolution"},"Profile",string(profile));
                prefixes.add(prefix); weapon = prefix.after;
            }
            State entry = before.replace(Domain.WEAPON,weapon);
            Mutation write = new Mutation(Domain.WEAPON,weapon,new String[]{"components","minecraft:custom_data","FTBossFoodEvolution"},key,argument);
            State after = entry.replace(Domain.WEAPON,write.after);
            Operation operation = new Operation(this.operations.size(),Kind.TIMER,before,entry,after,profile,key,argument,prefixes.toArray(new Mutation[0]),write);
            this.operations.add(operation); this.expected = after; return operation;
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

        Operation rangedAcquireDetached431(Object root, Object buffer) {
            this.writable();
            Value prior = FoodWeaponExpectedTrace.field(this.expected.player, "FTBossRangedProcTimers");
            if (root == null || buffer == null || prior != null && prior.kind == 10
                || this.expected.rangedAcquired
                || this.expected.rangedPlayerIdentity != null && this.expected.rangedPlayerIdentity != root) {
                throw new IllegalArgumentException("Independent detached ranged allocation intent required");
            }
            Value empty = FoodWeaponExpectedTrace.emptyCompound();
            State after = new State(this.expected.weapon, this.expected.player, this.expected.carrier, empty, false, root, buffer, true);
            Operation operation = new Operation(this.operations.size(), Kind.RANGED_ACQUIRE, this.expected, this.expected, after, null,
                "FTBossRangedProcTimers", empty, new Mutation[0], null);
            this.operations.add(operation);
            this.expected = after;
            return operation;
        }

        Operation rangedLong(String string, long longValue) {
            Value value = FoodWeaponExpectedTrace.field(this.expected.rangedBuffer, string);
            if (value != null && (value.kind == 5 || value.kind == 6)) {
                throw new IllegalArgumentException("Independent installed-SDK numeric conversion intent required");
            }
            long priorLong = value == null || value.kind < 1 || value.kind > 4 ? 0L : value.bits;
            return this.rangedLong(string, longValue, priorLong);
        }

        Operation rangedLong(String string, long longValue, long priorLong) {
            this.writable();
            this.requireRangedAcquisition();
            if (string == null || !string.startsWith("Last")) {
                throw new IllegalArgumentException("Original ranged timer key");
            }
            Value value = FoodWeaponExpectedTrace.field(this.expected.rangedBuffer, string);
            if (value == null || value.kind < 1 || value.kind > 6) {
                if (priorLong != 0L) {
                    throw new IllegalArgumentException("Expected nonnumeric conversion is zero");
                }
            } else if (value.kind <= 4 && priorLong != value.bits) {
                throw new IllegalArgumentException("Expected integral conversion differs");
            }
            Value value2 = FoodWeaponExpectedTrace.number(4, Math.max(longValue, priorLong));
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
            boolean rootReplacement = mutation.path.length == 0 && "FTBossRangedProcTimers".equals(string);
            boolean nestedReplacement = mutation.path.length > 0 && "FTBossRangedProcTimers".equals(mutation.path[0]);
            State state = domain == Domain.PLAYER ? this.expected.replacePlayer(mutation.after, rootReplacement, nestedReplacement) : this.expected.replace(domain, mutation.after);
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

        private Operation(int kindValue, Kind kind, State state, State state2, State state3, String string, String string2, Value value, Mutation[] mutationArray, Mutation mutation) {
            this.ordinal = kindValue;
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

        Mutation prefix(int kindValue) {
            return this.prefixes[kindValue];
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

        private State(Value value, Value value2, Value value3, Value value4, boolean rootReplacement) {
            this(value, value2, value3, value4, rootReplacement, null, null, false);
        }

        private State(Value value, Value value2, Value value3, Value value4, boolean rootReplacement, Object object, Object object2, boolean nestedReplacement) {
            if (value == null || value2 == null || value3 == null || value.kind != 10 || value2.kind != 10 || value3.kind != 10) {
                throw new IllegalArgumentException("Expected three field domains");
            }
            this.weapon = value;
            this.player = value2;
            this.carrier = value3;
            this.rangedBuffer = value4;
            this.rangedAttached = rootReplacement;
            this.rangedPlayerIdentity = object;
            this.rangedBufferIdentity = object2;
            this.rangedAcquired = nestedReplacement;
        }

        Value image(Domain domain) {
            return switch (domain) {
                default -> throw new IncompatibleClassChangeError();
                case WEAPON -> this.weapon;
                case PLAYER -> this.player;
                case CARRIER -> this.carrier;
                case RANGED_BUFFER -> this.rangedBuffer;
            };
        }

        State replace(Domain domain, Value value) {
            if (!(domain != Domain.RANGED_BUFFER || this.rangedAcquired && this.rangedPlayerIdentity != null && this.rangedBufferIdentity != null)) {
                throw new IllegalArgumentException("Original ranged acquisition identities required before buffer mutation");
            }
            return switch (domain) {
                default -> throw new IncompatibleClassChangeError();
                case WEAPON -> new State(value, this.player, this.carrier, this.rangedBuffer, this.rangedAttached, this.rangedPlayerIdentity, this.rangedBufferIdentity, this.rangedAcquired);
                case PLAYER -> this.replacePlayer(value, true, false);
                case CARRIER -> new State(this.weapon, this.player, value, this.rangedBuffer, this.rangedAttached, this.rangedPlayerIdentity, this.rangedBufferIdentity, this.rangedAcquired);
                case RANGED_BUFFER -> new State(this.weapon, this.rangedAttached ? FoodWeaponExpectedTrace.put(this.player, "FTBossRangedProcTimers", value) : this.player, this.carrier, value, this.rangedAttached, this.rangedPlayerIdentity, this.rangedBufferIdentity, this.rangedAcquired);
            };
        }

        private State replacePlayer(Value value, boolean rootReplacement, boolean nestedReplacement) {
            boolean attachedReplacement = this.rangedAttached && !rootReplacement;
            Value value2 = attachedReplacement && nestedReplacement ? FoodWeaponExpectedTrace.compoundAt(value, new String[]{"FTBossRangedProcTimers"}) : this.rangedBuffer;
            return new State(this.weapon, value, this.carrier, value2, attachedReplacement, this.rangedPlayerIdentity, this.rangedBufferIdentity, this.rangedAcquired);
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

    static Tag timerExpectationForRead(Value value) {
        boolean caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == FoodWeaponSourceLedger.class
                && frame.getMethodName().equals("nativeTimerExpectationReturned")
                && frame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session;Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;)Z")).orElse(false));
        if (!caller) throw new IllegalStateException("Private timer expected image requires its genuine ledger caller");
        return materializeTimerExpectation(value);
    }

    private static Tag materializeTimerExpectation(Value value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing immutable field image");
        }
        return switch (value.kind) {
            case 0 -> EndTag.INSTANCE;
            case 1 -> ByteTag.valueOf((byte)((byte)value.bits));
            case 2 -> ShortTag.valueOf((short)((short)value.bits));
            case 3 -> IntTag.valueOf((int)((int)value.bits));
            case 4 -> LongTag.valueOf((long)value.bits);
            case 5 -> FloatTag.valueOf((float)Float.intBitsToFloat((int)value.bits));
            case 6 -> DoubleTag.valueOf((double)Double.longBitsToDouble(value.bits));
            case 8 -> StringTag.valueOf((String)value.string);
            case 7 -> {
                byte[] bytes = new byte[value.elements.length];
                for (int byteIndex = 0; byteIndex < bytes.length; ++byteIndex) {
                    bytes[byteIndex] = (byte)value.elements[byteIndex];
                }
                yield new ByteArrayTag(bytes);
            }
            case 11 -> {
                int[] integers = new int[value.elements.length];
                for (int integerIndex = 0; integerIndex < integers.length; ++integerIndex) {
                    integers[integerIndex] = (int)value.elements[integerIndex];
                }
                yield new IntArrayTag(integers);
            }
            case 12 -> new LongArrayTag((long[])value.elements.clone());
            case 9 -> {
                if (value.children.length == 0 && value.bits != 0L) {
                    throw new IllegalArgumentException("Unsupported typed empty image list");
                }
                ListTag list = new ListTag();
                for (Value element : value.children) {
                    list.add(materializeTimerExpectation(element));
                }
                yield list;
            }
            case 10 -> {
                java.util.LinkedHashMap<String, Tag> fields = new java.util.LinkedHashMap<>();
                for (int fieldIndex = 0; fieldIndex < value.keys.length; ++fieldIndex) {
                    fields.put(value.keys[fieldIndex], materializeTimerExpectation(value.children[fieldIndex]));
                }
                yield NativeExpectedFieldObservation.timerExpectationCompoundForRead(fields);
            }
            default -> throw new IllegalArgumentException("Unknown immutable field image kind");
        };
    }

}

