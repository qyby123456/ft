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

@Mixin(
    value = {ServerPlayer.class},
    remap = false
)
public abstract class DandanPlayerTeleportMixin {
    @Inject(
        method = {"teleportTo(DDD)V", "teleportTo(DDD)V"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private void ftboss$local(double var1, double var3, double var5, CallbackInfo var7) {
        if (DandanTeleportSeal.block((ServerPlayer)(Object)this)) {
            var7.cancel();
        }
    }

    @Inject(
        method = {"teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private void ftboss$dimension(ServerLevel var1, double var2, double var4, double var6, float var8, float var9, CallbackInfo var10) {
        if (DandanTeleportSeal.block((ServerPlayer)(Object)this)) {
            var10.cancel();
        }
    }

    @Inject(
        method = {"teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private void ftboss$relative(
        ServerLevel var1, double var2, double var4, double var6, Set<RelativeMovement> var8, float var9, float var10, CallbackInfoReturnable<Boolean> var11
    ) {
        if (DandanTeleportSeal.block((ServerPlayer)(Object)this)) {
            var11.setReturnValue(false);
        }
    }
}
