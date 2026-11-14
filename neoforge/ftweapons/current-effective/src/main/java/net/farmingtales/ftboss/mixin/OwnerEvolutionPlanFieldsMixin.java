package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.OwnerEvolutionPlanFields;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponFinalDamage$EvolutionProducer", "net.farmingtales.ftboss.item.FoodWeaponFinalDamage$MaterialProducer"},
    remap = false
)
public abstract class OwnerEvolutionPlanFieldsMixin implements OwnerEvolutionPlanFields {
    @Shadow(
        remap = false
    )
    @Final
    private FoodWeaponHitPlan plan;

    @Unique
    @Override
    public final FoodWeaponHitPlan ftboss$ownerOriginalPlan() {
        return this.plan;
    }
}
