package ftboss.bossprobe.mixins;
import ftboss.bossprobe.TimelineObservation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage.class,remap=false)
public abstract class TimelineDamageObserver {
    @Inject(method="hurtSkill",at=@At("HEAD"),require=1,remap=false)
    private static void head(net.minecraft.world.entity.LivingEntity victim,net.minecraft.world.entity.Entity owner,net.minecraft.world.damagesource.DamageSource source,float amount,CallbackInfoReturnable<Boolean> callback){TimelineObservation.damageHead(victim,owner,amount);}
    @Inject(method="hurtSkill",at=@At("RETURN"),require=1,remap=false)
    private static void tail(net.minecraft.world.entity.LivingEntity victim,net.minecraft.world.entity.Entity owner,net.minecraft.world.damagesource.DamageSource source,float amount,CallbackInfoReturnable<Boolean> callback){TimelineObservation.damageReturn(victim,owner);}
}
