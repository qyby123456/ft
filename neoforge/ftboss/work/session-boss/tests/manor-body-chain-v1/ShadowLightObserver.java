package ftboss.bossprobe.mixins;
import ftboss.bossprobe.LightObservation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=net.farmingtales.ftboss.zheergenfx.LightNetwork.class,remap=false)
public abstract class ShadowLightObserver {
    @Inject(method="send",at=@At("HEAD"),require=1,remap=false)
    private static void observe(net.minecraft.server.level.ServerLevel level,net.farmingtales.ftboss.zheergenfx.LightPacket packet,CallbackInfo callback){LightObservation.shadow(packet);}
}
