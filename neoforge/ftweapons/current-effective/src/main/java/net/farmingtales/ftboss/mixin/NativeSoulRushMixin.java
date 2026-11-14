package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(
    targets = {"com.github.L_Ender.cataclysm.items.Soul_Render", "com.github.L_Ender.cataclysm.event.ServerEventHandler"},
    remap = false
)
public abstract class NativeSoulRushMixin {
    @Redirect(
        method = {"releaseUsing"},
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/L_Ender/cataclysm/capabilities/RenderRushCapability$IRenderRushCapability;setRush(Z)V"
        ),
        require = 0,
        remap = false
    )
    private void ftboss$rememberRush(@Coerce Object var1, boolean var2) {
        NativeSpecialCaller.rush(var1, var2);
    }

    @Redirect(
        method = {"onLivingUpdateEvent"},
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/L_Ender/cataclysm/capabilities/RenderRushCapability$IRenderRushCapability;tick(Lnet/minecraft/world/entity/LivingEntity;)V"
        ),
        require = 0,
        remap = false
    )
    private void ftboss$exactRushTick(@Coerce Object var1, LivingEntity var2) {
        NativeSpecialCaller.rushTick(var1, var2);
    }
}
