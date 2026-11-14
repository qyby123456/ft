/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.RelativeMovement
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import java.util.Set;
import net.farmingtales.ftboss.boss.ai.DandanTeleportSeal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ServerPlayer.class}, remap=false)
public abstract class DandanPlayerTeleportMixin {
    @Inject(method={"teleportTo(DDD)V", "m_6021_(DDD)V"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void ftboss$local(double d, double d2, double d3, CallbackInfo callbackInfo) {
        if (DandanTeleportSeal.block((ServerPlayer)this)) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", "m_8999_(Lnet/minecraft/server/level/ServerLevel;DDDFF)V"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void ftboss$dimension(ServerLevel serverLevel, double d, double d2, double d3, float f, float f2, CallbackInfo callbackInfo) {
        if (DandanTeleportSeal.block((ServerPlayer)this)) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", "m_264318_(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void ftboss$relative(ServerLevel serverLevel, double d, double d2, double d3, Set<RelativeMovement> set, float f, float f2, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (DandanTeleportSeal.block((ServerPlayer)this)) {
            callbackInfoReturnable.setReturnValue((Object)false);
        }
    }
}

