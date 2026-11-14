/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
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
@Mixin(targets={"io.redspace.ironsspellbooks.api.magic.MagicData"}, remap=false)
public abstract class FoodWeaponMagicResetMixin {
    @Inject(method={"resetCastingState()V"}, at={@At(value="HEAD")}, remap=false)
    private void ftboss$reset(CallbackInfo callbackInfo) {
        NativeIronExecution.cancelStored((MagicData)this);
        NativeIronCastObservation.clear((MagicData)this);
        FoodWeaponSpellSession.clear((MagicData)this);
    }
}

