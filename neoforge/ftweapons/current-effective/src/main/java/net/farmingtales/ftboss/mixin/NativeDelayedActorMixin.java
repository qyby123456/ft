package net.farmingtales.ftboss.mixin;

import java.util.UUID;
import net.farmingtales.ftboss.item.NativeActionObservation;
import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
    targets = {"com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity", "com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity"},
    remap = false
)
public abstract class NativeDelayedActorMixin {
    @Inject(
        method = {"setCaster(Lnet/minecraft/world/entity/LivingEntity;)V"},
        at = {@At("RETURN")},
        require = 0,
        remap = false
    )
    private void ftboss$afterCaster(LivingEntity var1, CallbackInfo var2) {
        if (var1 != null) {
            NativeActionObservation.remember((Entity)(Object)this, var1);
        }
    }

    @Inject(
        method = {"setCreatorEntityUUID(Ljava/util/UUID;)V"},
        at = {@At("RETURN")},
        require = 0,
        remap = false
    )
    private void ftboss$afterCreator(UUID var1, CallbackInfo var2) {
        if (var1 != null) {
            NativeActionObservation.remember((Entity)(Object)this, var1);
        }
    }

    @Redirect(
        method = {"createChain"},
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/L_Ender/cataclysm/entity/projectile/Tidal_Tentacle_Entity;setCreatorEntityUUID(Ljava/util/UUID;)V"
        ),
        require = 0,
        remap = false
    )
    private void ftboss$childActor(@Coerce Object var1, UUID var2) {
        NativeSpecialCaller.tentacleChild((Entity)var1, var2, (Entity)(Object)this);
    }
}
