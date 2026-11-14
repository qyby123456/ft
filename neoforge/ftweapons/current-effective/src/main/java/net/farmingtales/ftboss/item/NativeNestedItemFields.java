package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class NativeNestedItemFields {
    private NativeNestedItemFields() {
    }

    public static void requireObserver(Class<?> target) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(2L).toList());
        if (frames.size() != 2 || frames.get(0).getDeclaringClass() != target
            || !frames.get(0).getMethodName().equals("ftboss$nestedItems")
            || frames.get(1).getDeclaringClass().getNestHost() != NativeNestedItemGraph.class) {
            throw new IllegalStateException("Native nested item physical storage observation denied");
        }
    }

    public interface Items {
        List<ItemStack> ftboss$nestedItems();
    }
}
