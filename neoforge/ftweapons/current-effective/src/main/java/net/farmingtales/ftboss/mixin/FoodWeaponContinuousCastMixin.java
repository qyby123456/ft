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
@Mixin(
    targets = {"io.redspace.ironsspellbooks.capabilities.magic.MagicManager"},
    remap = false
)
public abstract class FoodWeaponContinuousCastMixin {
    @Redirect(
        method = {"lambda$tick$0(ZLnet/minecraft/world/entity/player/Player;)V"},
        at = @At(
            value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onServerCastTick(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"
        ),
        require = 1,
        expect = 1,
        allow = 1,
        remap = false
    )
    private void ftboss$castTick(AbstractSpell var1, Level var2, int var3, LivingEntity var4, MagicData var5) {
        try {
            NativeIronCastObservation.callback(
                var5,
                var4,
                "NATIVE_CONTINUOUS_TICK",
                false,
                () -> FoodWeaponSpellSession.callback(var5, var4, false, () -> var1.onServerCastTick(var2, var3, var4, var5))
            );
        } catch (Error | RuntimeException var7) {
            NativeIronExecution.cancelStored(var5);
            throw var7;
        }
    }

    @Redirect(
        method = {"lambda$tick$0(ZLnet/minecraft/world/entity/player/Player;)V"},
        at = @At(
            value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onServerCastComplete(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/magic/MagicData;Z)V"
        ),
        require = 2,
        expect = 2,
        allow = 2,
        remap = false
    )
    private void ftboss$castComplete(AbstractSpell var1, Level var2, int var3, LivingEntity var4, MagicData var5, boolean var6) {
        try {
            NativeIronCastObservation.callback(
                var5,
                var4,
                var6 ? "CANCELLED_COMPLETE" : "NATIVE_COMPLETE",
                true,
                () -> FoodWeaponSpellSession.callback(var5, var4, true, () -> var1.onServerCastComplete(var2, var3, var4, var5, var6))
            );
        } finally {
            NativeIronExecution.cancelStored(var5);
        }
    }
}
