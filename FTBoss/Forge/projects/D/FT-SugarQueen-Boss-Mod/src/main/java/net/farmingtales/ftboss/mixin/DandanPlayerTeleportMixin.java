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

/** 在通用玩家传送入口拦截指令和模组主动位移，不改动客户端位置。 */
@Mixin(value = ServerPlayer.class, remap = false)
public abstract class DandanPlayerTeleportMixin {
    @Inject(method = {"teleportTo(DDD)V", "m_6021_(DDD)V"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void ftboss$local(double targetX, double targetY, double targetZ, CallbackInfo callback) {
        if (DandanTeleportSeal.block((ServerPlayer) (Object) this)) callback.cancel();
    }

    @Inject(method = {"teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V",
            "m_8999_(Lnet/minecraft/server/level/ServerLevel;DDDFF)V"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void ftboss$dimension(ServerLevel level, double targetX, double targetY, double targetZ,
                                  float yaw, float pitch, CallbackInfo callback) {
        if (DandanTeleportSeal.block((ServerPlayer) (Object) this)) callback.cancel();
    }

    @Inject(method = {"teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z",
            "m_264318_(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z"},
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ftboss$relative(ServerLevel level, double targetX, double targetY, double targetZ,
                                 Set<RelativeMovement> relative, float yaw, float pitch, CallbackInfoReturnable<Boolean> callback) {
        if (DandanTeleportSeal.block((ServerPlayer) (Object) this)) callback.setReturnValue(false);
    }
}
