/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Group
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeActionObservation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ServerLevel.class}, remap=false)
public abstract class NativeSpawnObservationMixin {
    @Inject(method={"m_7967_(Lnet/minecraft/world/entity/Entity;)Z"}, at={@At(value="RETURN")}, require=0, remap=false)
    @Group(name="native$spawn", min=1, max=1)
    private void nativeAddedSrg(Entity entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        NativeActionObservation.added(entity, (Boolean)callbackInfoReturnable.getReturnValue());
    }
}

