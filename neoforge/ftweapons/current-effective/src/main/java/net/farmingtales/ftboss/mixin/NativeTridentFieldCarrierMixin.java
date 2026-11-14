package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeTridentFieldCarrier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {AbstractArrow.class},
    remap = false
)
public interface NativeTridentFieldCarrierMixin extends NativeTridentFieldCarrier {
    @Accessor(
        value = "pickupItemStack",
        remap = false
    )
    @Override
    ItemStack ftboss$originalTridentStack();
}

