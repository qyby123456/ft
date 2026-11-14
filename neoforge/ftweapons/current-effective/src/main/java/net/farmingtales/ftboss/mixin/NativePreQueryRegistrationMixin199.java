package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativePreQueryCapabilityLifetime199;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RegisterCapabilitiesEvent.class, remap = false)
public abstract class NativePreQueryRegistrationMixin199 {
    @Redirect(
        method = "registerItem(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ItemLike;asItem()Lnet/minecraft/world/item/Item;"),
        require = 1,
        allow = 1,
        remap = false
    )
    private Item ftboss$nativeLifetimeItem(ItemLike actual,ItemCapability<?,?> capability,ICapabilityProvider<?,?,?> provider,ItemLike[] items) {
        Item item = actual.asItem();
        try {
            NativePreQueryCapabilityLifetime199.resolved(capability,item);
        } catch (Throwable observationFailure) {
        }
        return item;
    }
}
