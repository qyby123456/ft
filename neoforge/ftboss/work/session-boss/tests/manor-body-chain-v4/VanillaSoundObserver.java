package ftboss.bossprobe.mixins;
import ftboss.bossprobe.EntryFxObservation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=net.minecraft.world.level.Level.class,remap=false)
public abstract class VanillaSoundObserver {
    @Inject(method="playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",at=@At("HEAD"),require=1,remap=false)
    private void observe(net.minecraft.world.entity.player.Player player,double x,double y,double z,net.minecraft.sounds.SoundEvent type,net.minecraft.sounds.SoundSource source,float volume,float pitch,CallbackInfo callback){EntryFxObservation.sound(type,x,y,z,source,volume,pitch);}
}
