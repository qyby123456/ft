package net.farmingtales.ftboss.item;

import java.util.Objects;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;

public final class NativeStackComponents {
    private NativeStackComponents() {
    }

    public static Snapshot capture(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        if (stack.isEmpty() || !(stack.getComponents() instanceof PatchedDataComponentMap components)) {
            throw new IllegalArgumentException("A mutable non-empty stack is required");
        }
        ItemStack detached = NativeFoundationItemData.decode(NativeFoundationItemData.image(stack));
        if (detached.isEmpty() || detached.getItem() != stack.getItem() || detached.getCount() != stack.getCount()
            || components.stream().count() != detached.getComponents().stream().count()
            || !components.stream().allMatch(component -> Objects.equals(component.value(), detached.get(component.type())))) {
            throw new IllegalArgumentException("Component rollback copy differs from the original stack");
        }
        return new Snapshot(stack, components, detached.getComponentsPatch(), stack.getCount());
    }

    public static String fingerprint(ItemStack stack, HolderLookup.Provider registries) {
        return fingerprint(stack.saveOptional(Objects.requireNonNull(registries, "registries")));
    }

    public static String fingerprint(Tag var0) {
        try {
            MessageDigest var1 = MessageDigest.getInstance("SHA-256");
            canonical(var1, var0);
            return HexFormat.of().formatHex(var1.digest());
        } catch (NoSuchAlgorithmException var2) {
            throw new IllegalStateException(var2);
        }
    }

    private static void field(MessageDigest var0, String var1) {
        byte[] var2 = var1.getBytes(StandardCharsets.UTF_8);
        var0.update(ByteBuffer.allocate(4).putInt(var2.length).array());
        var0.update(var2);
    }

    private static void canonical(MessageDigest var0, Tag var1) {
        var0.update(var1.getId());
        if (var1 instanceof CompoundTag var2) {
            List<String> var4 = var2.getAllKeys().stream().sorted().toList();
            field(var0, Integer.toString(var4.size()));

            for (String var6 : var4) {
                field(var0, var6);
                canonical(var0, Objects.requireNonNull(var2.get(var6)));
            }
        } else if (var1 instanceof ListTag var3) {
            field(var0, Integer.toString(var3.size()));
            field(var0, Byte.toString(var3.getElementType()));

            for (Tag var8 : var3) {
                canonical(var0, var8);
            }
        } else {
            field(var0, var1.toString());
        }
    }

    public static final class Snapshot {
        private final ItemStack owner;
        private final PatchedDataComponentMap components;
        private final DataComponentPatch patch;
        private final int count;

        private Snapshot(ItemStack owner, PatchedDataComponentMap components, DataComponentPatch patch, int count) {
            this.owner = owner;
            this.components = components;
            this.patch = patch;
            this.count = count;
        }

        public void restore(ItemStack stack) {
            if (stack != this.owner) {
                throw new IllegalArgumentException("Component rollback belongs to another stack");
            }
            this.components.restorePatch(this.patch);
            stack.setCount(this.count);
        }
    }
}
