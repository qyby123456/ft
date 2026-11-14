package net.farmingtales.ftboss.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;

public final class NativeMapMutationScope280 {
    private static final ThreadLocal<Object> CURRENT = new ThreadLocal<>();
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private NativeMapMutationScope280() { }

    private static boolean nativeCaller(String method, String descriptor) {
        return WALKER.walk(frames -> frames.skip(2).findFirst().map(frame -> {
            String name = frame.getMethodName();
            return frame.getDeclaringClass() == CompoundTag.class
                && (name.equals(method) || name.endsWith("$" + method))
                && frame.getMethodType().toMethodDescriptorString().equals(descriptor);
        }).orElse(false));
    }

    public static boolean suppressed(Object map) {
        return map != null && CURRENT.get() == map;
    }

    public static Object put(Map<Object, Object> map, Object key, Object value, Operation<Object> original) {
        if (!nativeCaller("ftboss$nativeMapPutScope280", "(Ljava/util/Map;Ljava/lang/Object;Ljava/lang/Object;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Ljava/lang/Object;")) return original.call(map, key, value);
        Object previous = CURRENT.get();
        CURRENT.set(map);
        try { return original.call(map, key, value); }
        finally { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); }
    }

    public static Object remove(Map<Object, Object> map, Object key, Operation<Object> original) {
        if (!nativeCaller("ftboss$nativeMapRemoveScope280", "(Ljava/util/Map;Ljava/lang/Object;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Ljava/lang/Object;")) return original.call(map, key);
        Object previous = CURRENT.get();
        CURRENT.set(map);
        try { return original.call(map, key); }
        finally { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); }
    }
}
