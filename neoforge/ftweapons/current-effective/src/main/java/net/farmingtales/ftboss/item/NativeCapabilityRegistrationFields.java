package net.farmingtales.ftboss.item;

import java.util.Map;
import net.minecraft.world.item.Item;

public interface NativeCapabilityRegistrationFields {
    Map<Item, ?> ftboss$rawNativeProviders();
    long ftboss$nativeCapabilityVersion();
    void ftboss$nativeCapabilityRegistration();
}
