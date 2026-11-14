package net.farmingtales.ftboss.mixin;

import java.util.UUID;
import net.farmingtales.ftboss.item.OwnerNativeMaterialEntityIdentity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
    targets = {"net.minecraft.world.entity.Entity"},
    remap = false
)
public abstract class OwnerNativeMaterialEntityIdentityMixin implements OwnerNativeMaterialEntityIdentity {
    @Shadow(
        remap = false
    )
    protected UUID uuid;

    @Override
    public final UUID ftboss$ownerMaterialEntityIdentity() {
        return this.uuid;
    }
}
