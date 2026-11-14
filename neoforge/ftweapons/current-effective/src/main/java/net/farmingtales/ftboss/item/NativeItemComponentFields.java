package net.farmingtales.ftboss.item;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.lang.StackWalker.Option;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

public final class NativeItemComponentFields {
    private NativeItemComponentFields() {
    }

    public static void requireObserver(Class<?> target, String method) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(2L).toList());
        if (frames.size() != 2 || frames.get(0).getDeclaringClass() != target
            || !frames.get(0).getMethodName().equals(method)
            || frames.get(1).getDeclaringClass().getNestHost() != NativeItemComponentObservation.class) {
            throw new IllegalStateException("Native component physical field observation denied");
        }
    }

    public interface CustomData {
        CompoundTag ftboss$nativeCustomData();
    }

    public interface Stack {
        Item ftboss$nativeItem();
        int ftboss$nativeCount();
        long ftboss$nativeStackMutationVersion();
        PatchedDataComponentMap ftboss$nativeComponents();
    }

    public interface Storage {
        DataComponentMap ftboss$nativePrototype();
        Reference2ObjectMap<DataComponentType<?>, Optional<?>> ftboss$nativePatch();
        boolean ftboss$nativeCopyOnWrite();
        long ftboss$nativeComponentMutationVersion();
    }
}
