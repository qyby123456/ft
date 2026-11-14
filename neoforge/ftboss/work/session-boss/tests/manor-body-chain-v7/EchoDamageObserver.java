package ftboss.bossprobe.mixins;
import ftboss.bossprobe.EchoDamageObservation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage.class,remap=false)
public abstract class EchoDamageObserver {
    @Inject(method="hurtSkill",at=@At("HEAD"),require=1,remap=false)
    private static void observe(net.minecraft.world.entity.LivingEntity victim,net.minecraft.world.entity.Entity owner,net.minecraft.world.damagesource.DamageSource source,float amount,CallbackInfoReturnable<Boolean> callback){EchoDamageObservation.observe(victim,owner,amount);}
}
