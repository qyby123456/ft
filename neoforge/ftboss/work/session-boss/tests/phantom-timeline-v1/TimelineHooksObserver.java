package ftboss.bossprobe.mixins;
import ftboss.bossprobe.TimelineObservation;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=net.farmingtales.ftboss.zheergenphantom.PhantomHooks.class,remap=false)
public abstract class TimelineHooksObserver {
    @Inject(method="capture",at=@At("HEAD"),require=1,remap=false)
    private static void captureHead(EasterBossEntity actor,CallbackInfo callback){TimelineObservation.captureHead(actor);}
    @Inject(method="capture",at=@At("RETURN"),require=1,remap=false)
    private static void captureReturn(EasterBossEntity actor,CallbackInfo callback){TimelineObservation.captureReturn(actor);}
    @Inject(method="tick",at=@At("HEAD"),require=1,remap=false)
    private static void tickHead(EasterBossEntity actor,CallbackInfo callback){TimelineObservation.tickHead(actor);}
    @Inject(method="tick",at=@At("RETURN"),require=1,remap=false)
    private static void tickReturn(EasterBossEntity actor,CallbackInfo callback){TimelineObservation.tickReturn(actor);}
}
