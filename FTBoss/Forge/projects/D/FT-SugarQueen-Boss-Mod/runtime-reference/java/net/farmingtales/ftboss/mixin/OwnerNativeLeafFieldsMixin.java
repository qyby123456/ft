/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerNativeLeafFields;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponHitPlan$TypedLeaf"}, remap=false)
public abstract class OwnerNativeLeafFieldsMixin
implements OwnerNativeLeafFields {
    @Shadow(remap=false)
    @Final
    private FoodWeaponSourceLedger.AttemptDomain attempt;

    @Override
    @Unique
    public final FoodWeaponSourceLedger.AttemptDomain ftboss$ownerNativeAttempt() {
        return this.attempt;
    }
}

