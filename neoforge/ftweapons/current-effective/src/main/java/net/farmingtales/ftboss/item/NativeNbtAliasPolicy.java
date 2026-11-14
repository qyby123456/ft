package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NbtUtils;

public final class NativeNbtAliasPolicy {
    private NativeNbtAliasPolicy() {
    }

    public static boolean trustedRawFieldRead() {
        List<StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).filter(frame -> !frame.getMethodName().contains("ftboss$nativeRawAlias")).limit(2L).toList());
        if (frames.size() != 2 || !frames.get(0).getMethodName().startsWith("ftboss$raw")) return false;
        Class<?> observer = frames.get(1).getDeclaringClass().getNestHost();
        if (observer == OwnerTimerSemanticAuthority.class) return timerGraphRead(frames.get(0), frames.get(1));
        if (observer == OwnerNativeSemanticAuthority.class) {
            StackFrame caller = frames.get(1), accessor = frames.get(0);
            return caller.getDeclaringClass() == OwnerNativeSemanticAuthority.class && caller.getMethodName().equals("read")
                && caller.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$ReadContext;Ljava/lang/Object;Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$RawField;)Ljava/lang/Object;")
                && OwnerNativeSemanticAuthority.authenticatedRawMapRead(accessor.getDeclaringClass(), accessor.getMethodName(), accessor.getDescriptor());
        }
        return observer == NativeNbtGraphObservation.class || observer == NativeExpectedRawFields.class
            || exactLegacyObserver(observer, "OwnerFieldExpectedAuthority")
            || exactLegacyObserver(observer, "OwnerFullRepairGuard")
            || exactLegacyObserver(observer, "FoodWeaponSourceLedger")
            || exactLegacyObserver(observer, "OwnerRepairGenerationAuthority")
            || exactLegacyObserver(observer, "NativeExpectedFieldObservation");
    }

    private static boolean exactLegacyObserver(Class<?> observer, String name) {
        String qualified = "net.farmingtales.ftboss.item." + name;
        if (!observer.getName().equals(qualified)) return false;
        try {
            return observer == Class.forName(qualified, false, NativeNbtAliasPolicy.class.getClassLoader());
        } catch (ClassNotFoundException missing) {
            return false;
        }
    }


    public static boolean trustedTaskProgressArrayRead(Object receiver, Object array) {
        List<StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(5L).toList());
        if (!(receiver instanceof IntArrayTag) || !(receiver instanceof NativeTaskProgressArrayFields) || !(array instanceof int[])
            || frames.size() != 5 || frames.get(0).getDeclaringClass() != IntArrayTag.class
            || !frames.get(0).getMethodName().contains("ftboss$nativePublicAlias")
            || !frames.get(0).getDescriptor().equals("(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V")
            || frames.get(1).getDeclaringClass() != IntArrayTag.class || !frames.get(1).getMethodName().equals("getAsIntArray")
            || !frames.get(1).getDescriptor().equals("()[I")
            || frames.get(2).getDeclaringClass() != CompoundTag.class || !frames.get(2).getMethodName().equals("getIntArray")
            || !frames.get(2).getDescriptor().equals("(Ljava/lang/String;)[I")
            || frames.get(3).getDeclaringClass() != OwnerStatueTaskWrites.class || !frames.get(3).getMethodName().equals("progress")
            || !frames.get(3).getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/nbt/CompoundTag;)[I")
            || frames.get(4).getDeclaringClass() != FoodWeaponRepair.class
            || !java.util.Set.of("activeQuest", "view", "submit").contains(frames.get(4).getMethodName())) return false;
        try {
            return OwnerStatueTaskOrigin.boundedProgressAlias(receiver, array);
        } catch (Throwable rejected) {
            return false;
        }
    }

    private static void requireTaskArrayCaller(String method, String origin) {
        List<StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(2L).limit(2L).toList());
        if (frames.size() != 2 || frames.get(0).getDeclaringClass() != IntArrayTag.class
            || !frames.get(0).getMethodName().equals(method) || !frames.get(0).getDescriptor().equals("(Ljava/lang/Object;)V")
            || frames.get(1).getDeclaringClass() != OwnerStatueTaskOrigin.class || !frames.get(1).getMethodName().equals(origin)) {
            throw new IllegalStateException("Native task progress mutation caller denied");
        }
    }

    public static void requireTaskProgressArrayWrite(Object storage, Object permit) {
        requireTaskArrayCaller("ftboss$nativeTaskProgressArrayWrite", "beforeArrayWrite");
        if (!OwnerStatueTaskOrigin.permitsNativeProgressArrayWrite(storage, permit)) {
            throw new IllegalStateException("Native task progress write permit denied");
        }
    }

    public static void requireTaskProgressArrayRejection(Object storage, Object session) {
        requireTaskArrayCaller("ftboss$nativeTaskProgressArrayRejected", "burn");
        if (!OwnerStatueTaskOrigin.permitsNativeProgressArrayRejection(storage, session)) {
            throw new IllegalStateException("Native task progress rejection source denied");
        }
    }

    public static boolean trustedImmutableArrayRead() {
        List<StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).filter(frame -> !frame.getMethodName().contains("ftboss$nativePublicAlias")).limit(2L).toList());
        if (frames.size() != 2 || frames.get(0).getDeclaringClass() != IntArrayTag.class
            || !frames.get(0).getMethodName().equals("getAsIntArray") || !frames.get(0).getDescriptor().equals("()[I")) return false;
        StackFrame consumer = frames.get(1);
        return consumer.getDeclaringClass() == CompoundTag.class && consumer.getMethodName().equals("hasUUID")
                && consumer.getDescriptor().equals("(Ljava/lang/String;)Z")
            || consumer.getDeclaringClass() == NbtUtils.class && consumer.getMethodName().equals("loadUUID")
                && consumer.getDescriptor().equals("(Lnet/minecraft/nbt/Tag;)Ljava/util/UUID;");
    }
    public static boolean trustedFullRepairUuidRead() {
        List<StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).limit(3L).toList());
        if (frames.size() != 3 || frames.get(0).getDeclaringClass() != IntArrayTag.class
            || !frames.get(0).getMethodName().endsWith("$ftboss$fullRepairAlias")
            || !frames.get(0).getDescriptor().equals("(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V")
            || frames.get(1).getDeclaringClass() != IntArrayTag.class || !frames.get(1).getMethodName().equals("getAsIntArray")
            || !frames.get(1).getDescriptor().equals("()[I")) return false;
        StackFrame consumer = frames.get(2);
        return consumer.getDeclaringClass() == CompoundTag.class && consumer.getMethodName().equals("hasUUID")
                && consumer.getDescriptor().equals("(Ljava/lang/String;)Z")
            || consumer.getDeclaringClass() == NbtUtils.class && consumer.getMethodName().equals("loadUUID")
                && consumer.getDescriptor().equals("(Lnet/minecraft/nbt/Tag;)Ljava/util/UUID;");
    }
    private static boolean timerGraphRead(StackFrame accessor, StackFrame caller) {
        if (caller.getDeclaringClass() != OwnerTimerSemanticAuthority.class || !caller.getMethodName().equals("graph")
            || !caller.getDescriptor().equals("(Lnet/minecraft/nbt/Tag;Ljava/util/IdentityHashMap;I)V")) return false;
        Class<?> owner = accessor.getDeclaringClass();
        String name = accessor.getMethodName(), descriptor = accessor.getDescriptor();
        return owner == CompoundTag.class && name.equals("ftboss$rawCompoundFields") && descriptor.equals("()Ljava/util/Map;")
            || owner == net.minecraft.nbt.ListTag.class && name.equals("ftboss$rawListFields") && descriptor.equals("()Ljava/util/List;")
            || owner == net.minecraft.nbt.ByteArrayTag.class && name.equals("ftboss$rawBytes") && descriptor.equals("()[B")
            || owner == IntArrayTag.class && name.equals("ftboss$rawInts") && descriptor.equals("()[I")
            || owner == net.minecraft.nbt.LongArrayTag.class && name.equals("ftboss$rawLongs") && descriptor.equals("()[J");
    }
}
