package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

final class NativeRepairTaskData {
    private static final String ROOT = "FTBossWeaponLifecycle";

    private NativeRepairTaskData() {
    }

    private static void requireCaller(boolean creating) {
        StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.filter(frame -> frame.getDeclaringClass() != NativeRepairTaskData.class).findFirst().orElseThrow());
        Set<String> methods = creating ? Set.of("advance", "start", "claim") : Set.of("revision", "activeQuest", "invalidateForProgression");
        if (caller.getDeclaringClass() != FoodWeaponRepair.class || !methods.contains(caller.getMethodName())) {
            throw new IllegalStateException("Native repair task storage access denied");
        }
    }

    static CompoundTag read(ItemStack stack) {
        requireCaller(false);
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = data == null ? null : data.getUnsafe();
        return root == null || !root.contains(ROOT, 10) ? null : root.getCompound(ROOT);
    }

    static int[] progressCopy(ItemStack stack, CompoundTag quest) {
        StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (!(caller.getDeclaringClass() == OwnerStatueTaskWrites.class && caller.getMethodName().equals("progress"))
            && !(caller.getDeclaringClass() == NativeFoodWeaponWear.class && caller.getMethodName().equals("taskLive"))) {
            throw new IllegalStateException("Original native repair progress read required");
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = data == null ? null : data.getUnsafe();
        if (root == null || root.getCompound(ROOT).getCompound("RepairQuest") != quest) throw new IllegalStateException("Detached native repair progress receiver");
        var observed = FoodWeaponExpectedTrace.field(NativeExpectedRawFields.observe(quest), "Progress");
        if (observed == null) return new int[0];
        var copy = FoodWeaponExpectedTrace.materialize(observed);
        return copy instanceof net.minecraft.nbt.IntArrayTag values ? values.getAsIntArray().clone() : new int[0];
    }

    static CompoundTag ensure(ItemStack stack) {
        requireCaller(true);
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || data == CustomData.EMPTY) {
            data = CustomData.of(new CompoundTag());
            stack.set(DataComponents.CUSTOM_DATA, data);
            if (stack.get(DataComponents.CUSTOM_DATA) != data) {
                throw new IllegalStateException("Native repair task component replaced during creation");
            }
        }
        CompoundTag root = data.getUnsafe();
        if (!root.contains(ROOT, 10)) {
            root.put(ROOT, new CompoundTag());
        }
        return root.getCompound(ROOT);
    }
}
