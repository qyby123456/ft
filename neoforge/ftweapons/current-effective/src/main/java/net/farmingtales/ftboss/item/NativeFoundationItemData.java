package net.farmingtales.ftboss.item;

import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class NativeFoundationItemData {
    private NativeFoundationItemData() {
    }

    public static HolderLookup.Provider registries() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && server.isSameThread()) {
            return server.registryAccess();
        }
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return ClientContext.registries();
        }
        throw new IllegalStateException("No actual native main-thread world registry context");
    }

    public static CompoundTag image(ItemStack stack) {
        return image(stack, registries());
    }

    public static CompoundTag image(ItemStack stack, HolderLookup.Provider registries) {
        Tag encoded = NativeExpectedFieldObservation.imageForRead(Objects.requireNonNull(stack, "stack"), Objects.requireNonNull(registries, "registries"));
        if (!(encoded instanceof CompoundTag compound)) {
            throw new IllegalArgumentException("Invalid native foundation item image type");
        }
        return compound;
    }

    public static ItemStack decode(CompoundTag image) {
        return NativeRepairStackImage.decode(registries(), image);
    }

    public static CompoundTag customData(ItemStack stack) {
        CustomData data = Objects.requireNonNull(stack, "stack").get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    public static CompoundTag element(ItemStack stack, String key) {
        CompoundTag data = customData(stack);
        return data == null || !data.contains(key, 10) ? null : data.getCompound(key);
    }

    public static boolean contains(ItemStack stack, String key) {
        CompoundTag data = customData(stack);
        return data != null && data.contains(key);
    }

    public static void setCustomData(ItemStack stack, CompoundTag data) {
        Objects.requireNonNull(stack, "stack").set(DataComponents.CUSTOM_DATA, CustomData.of(Objects.requireNonNull(data, "data")));
    }

    public static void copyComponents(ItemStack target, ItemStack source) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(source, "source");
        if (target.isEmpty() || source.isEmpty() || target.getItem() != source.getItem()
            || !(target.getComponents() instanceof PatchedDataComponentMap components)) {
            throw new IllegalArgumentException("Native foundation component transfer requires the same real item type");
        }
        components.restorePatch(source.getComponentsPatch());
    }

    public static CompoundTag materialComponents(CompoundTag var0) {
        if (var0.size() > 16384) {
            throw new IllegalArgumentException("Material compound node budget exceeded");
        } else {
            CompoundTag var1 = new CompoundTag();

            for (String var3 : NativeExpectedRawFields.detachedKeys(var0)) {
                if (!var3.equals("minecraft:damage")) {
                    var1.put(var3, var0.get(var3));
                }
            }

            return var1;
        }
    }

    private static final class ClientContext {
        private static HolderLookup.Provider registries() {
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft != null && minecraft.isSameThread() && minecraft.level != null) {
                return minecraft.level.registryAccess();
            }
            throw new IllegalStateException("No actual native client main-thread world registry context");
        }
    }
}
