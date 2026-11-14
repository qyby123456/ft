package net.farmingtales.ftboss.mixin;

import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.OwnerOriginalPlanFields;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(
    value = {FoodWeaponHitPlan.class},
    remap = false
)
public abstract class OwnerOriginalPlanFieldsMixin implements OwnerOriginalPlanFields {
    @Shadow(
        remap = false
    )
    @Final
    private List<?> typedLeaves;

    @Unique
    @Override
    public final List<?> ftboss$ownerOriginalLeaves() {
        return this.typedLeaves;
    }
}
