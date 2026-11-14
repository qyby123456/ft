/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.farmingtales.ftboss.integ.FoodWeaponSpellSession;
import net.farmingtales.ftboss.integ.NativeIronCastObservation;
import net.farmingtales.ftboss.integ.NativeIronExecution;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets={"io.redspace.ironsspellbooks.capabilities.magic.MagicManager"}, remap=false)
public abstract class FoodWeaponContinuousCastMixin {
    @Redirect(method={"lambda$tick$0(ZLnet/minecraft/world/entity/player/Player;)V"}, at=@At(value="INVOKE", target="Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onServerCastTick(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"), require=1, expect=1, allow=1, remap=false)
    private void ftboss$castTick(AbstractSpell abstractSpell, Level level, int n, LivingEntity livingEntity, MagicData magicData) {
        try {
            NativeIronCastObservation.callback(magicData, livingEntity, "NATIVE_CONTINUOUS_TICK", false, () -> FoodWeaponSpellSession.callback(magicData, livingEntity, false, () -> abstractSpell.onServerCastTick(level, n, livingEntity, magicData)));
        }
        catch (Error | RuntimeException throwable) {
            NativeIronExecution.cancelStored(magicData);
            throw throwable;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Redirect(method={"lambda$tick$0(ZLnet/minecraft/world/entity/player/Player;)V"}, at=@At(value="INVOKE", target="Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onServerCastComplete(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/magic/MagicData;Z)V"), require=2, expect=2, allow=2, remap=false)
    private void ftboss$castComplete(AbstractSpell abstractSpell, Level level, int n, LivingEntity livingEntity, MagicData magicData, boolean bl) {
        try {
            NativeIronCastObservation.callback(magicData, livingEntity, bl ? "CANCELLED_COMPLETE" : "NATIVE_COMPLETE", true, () -> FoodWeaponSpellSession.callback(magicData, livingEntity, true, () -> abstractSpell.onServerCastComplete(level, n, livingEntity, magicData, bl)));
        }
        finally {
            NativeIronExecution.cancelStored(magicData);
        }
    }
}

