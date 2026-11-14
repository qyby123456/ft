package net.farmingtales.ftboss.item;

import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;

public final class NativeFoundationComponentText {
    private NativeFoundationComponentText() {
    }

    public static String serialize(Component component) {
        return serialize(component, NativeFoundationItemData.registries());
    }

    public static String serialize(Component component, HolderLookup.Provider registries) {
        return Component.Serializer.toJson(Objects.requireNonNull(component, "component"), Objects.requireNonNull(registries, "registries"));
    }
}
