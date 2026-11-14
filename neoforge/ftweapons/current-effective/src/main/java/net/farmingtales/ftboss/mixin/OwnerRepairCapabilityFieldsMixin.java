package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerRepairCapabilityFields;
import net.minecraft.world.item.ItemStack;
import net.farmingtales.ftboss.item.NativeItemCapabilityState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(
    value = {ItemStack.class},
    remap = false
)
public abstract class OwnerRepairCapabilityFieldsMixin implements OwnerRepairCapabilityFields {
    @Unique
    @Override
    public final boolean ftboss$ownerRepairInitializedNoProviders() {
        return NativeItemCapabilityState.hasNoProviders((ItemStack)(Object)this);
    }
}
