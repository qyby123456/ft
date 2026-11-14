package net.farmingtales.ftboss.mixin;

import java.util.Map;
import net.farmingtales.ftboss.item.NativeItemCapabilityFields;
import net.farmingtales.ftboss.item.NativeItemCapabilityState;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemCapability.class, remap = false)
public abstract class NativeItemCapabilityFieldsMixin implements NativeItemCapabilityFields {
    @Shadow
    @Final
    private Map<Item, ?> providers;

    @Override
    public final boolean ftboss$nativeHasItemProviders(Item item) {
        NativeItemCapabilityState.requireObserver();
        return this.providers.containsKey(item);
    }
}
