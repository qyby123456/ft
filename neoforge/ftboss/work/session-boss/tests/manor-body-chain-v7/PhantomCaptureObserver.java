package ftboss.bossprobe.mixins;
import ftboss.bossprobe.PhantomCaptureObservation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=net.farmingtales.ftboss.zheergenphantom.PhantomHooks.class,remap=false)
public abstract class PhantomCaptureObserver {
    @Inject(method="capture",at=@At("HEAD"),require=1,remap=false)
    private static void observe(net.farmingtales.ftboss.entity.EasterBossEntity actor,CallbackInfo callback){PhantomCaptureObservation.observe(actor);}
}
