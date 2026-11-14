package net.farmingtales.ftboss.item;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class NativeSocketWeaponTokenData {
    private static final String KEY = "FTBossFoodSocketWeaponToken";

    private NativeSocketWeaponTokenData() {
    }

    private static void requireStack(ItemStack stack) {
        if (Objects.requireNonNull(stack, "stack").isEmpty()) {
            throw new IllegalArgumentException("Native socket token requires a nonempty physical stack");
        }
    }

    public static UUID ensure(ItemStack stack) {
        requireStack(stack);
        CompoundTag data = NativeFoundationItemData.customData(stack);
        if (data != null && data.hasUUID(KEY)) return data.getUUID(KEY);
        CompoundTag changed = data == null ? new CompoundTag() : data;
        UUID token = UUID.randomUUID();
        changed.putUUID(KEY, token);
        NativeFoundationItemData.setCustomData(stack, changed);
        return token;
    }

    public static CompoundTag entry(ItemStack stack) {
        requireStack(stack);
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            NativeFoundationItemData.setCustomData(stack, new CompoundTag());
            data = Objects.requireNonNull(stack.get(DataComponents.CUSTOM_DATA), "actual native socket custom data");
        }
        return data.getUnsafe();
    }

    public static void write(ItemStack stack, CompoundTag receiver, UUID token) {
        requireStack(stack);
        Objects.requireNonNull(token, "token");
        CustomData actual = stack.get(DataComponents.CUSTOM_DATA);
        if (actual == null || actual.getUnsafe() != Objects.requireNonNull(receiver, "actual receiver")) {
            throw new IllegalStateException("Native socket token receiver is not the actual current component root");
        }
        CompoundTag changed = receiver.copy();
        changed.putUUID(KEY, token);
        NativeFoundationItemData.setCustomData(stack, changed);
    }
}
