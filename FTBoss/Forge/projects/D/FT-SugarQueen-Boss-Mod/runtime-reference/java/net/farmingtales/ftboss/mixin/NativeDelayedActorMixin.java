/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
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
@Mixin(targets={"com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity", "com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity"}, remap=false)
public abstract class NativeDelayedActorMixin {
    @Inject(method={"setCaster(Lnet/minecraft/world/entity/LivingEntity;)V"}, at={@At(value="RETURN")}, require=0, remap=false)
    private void ftboss$afterCaster(LivingEntity livingEntity, CallbackInfo callbackInfo) {
        if (livingEntity != null) {
            NativeActionObservation.remember((Entity)this, livingEntity);
        }
    }

    @Inject(method={"setCreatorEntityUUID(Ljava/util/UUID;)V"}, at={@At(value="RETURN")}, require=0, remap=false)
    private void ftboss$afterCreator(UUID uUID, CallbackInfo callbackInfo) {
        if (uUID != null) {
            NativeActionObservation.remember((Entity)this, uUID);
        }
    }

    @Redirect(method={"createChain"}, at=@At(value="INVOKE", target="Lcom/github/L_Ender/cataclysm/entity/projectile/Tidal_Tentacle_Entity;setCreatorEntityUUID(Ljava/util/UUID;)V"), require=0, remap=false)
    private void ftboss$childActor(@Coerce Object object, UUID uUID) {
        NativeSpecialCaller.tentacleChild((Entity)object, uUID, (Entity)this);
    }
}

