package net.farmingtales.ftboss.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.farmingtales.ftboss.integ.FoodWeaponSpellSession;
import net.farmingtales.ftboss.integ.NativeIronCastObservation;
import net.farmingtales.ftboss.integ.NativeIronExecution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
    targets = {"io.redspace.ironsspellbooks.api.magic.MagicData"},
    remap = false
)
public abstract class FoodWeaponMagicResetMixin {
    @Inject(
        method = {"resetCastingState()V"},
        at = {@At("HEAD")},
        remap = false
    )
    private void ftboss$reset(CallbackInfo var1) {
        NativeIronExecution.cancelStored((MagicData)(Object)this);
        NativeIronCastObservation.clear((MagicData)(Object)this);
        FoodWeaponSpellSession.clear((MagicData)(Object)this);
    }
}
