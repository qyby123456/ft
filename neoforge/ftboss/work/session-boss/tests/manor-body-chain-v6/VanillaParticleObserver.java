package ftboss.bossprobe.mixins;
import ftboss.bossprobe.EntryFxObservation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=net.minecraft.server.level.ServerLevel.class,remap=false)
public abstract class VanillaParticleObserver {
    @Inject(method="sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I",at=@At("HEAD"),require=1,remap=false)
    private void observe(net.minecraft.core.particles.ParticleOptions type,double x,double y,double z,int count,double spreadX,double spreadY,double spreadZ,double speed,CallbackInfoReturnable<Integer> callback){EntryFxObservation.particle(type,x,y,z,count,spreadX,spreadY,spreadZ,speed);}
}
