package net.farmingtales.ftboss.mixin;

import java.util.List;
import net.farmingtales.ftboss.item.OwnerOriginalPublicationFields;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponFinalDamage$Publication"},
    remap = false
)
public abstract class OwnerOriginalPublicationFieldsMixin implements OwnerOriginalPublicationFields {
    @Shadow(
        remap = false
    )
    @Final
    private List<?> producers;

    @Unique
    @Override
    public final List<?> ftboss$ownerOriginalProducers() {
        return this.producers;
    }
}
