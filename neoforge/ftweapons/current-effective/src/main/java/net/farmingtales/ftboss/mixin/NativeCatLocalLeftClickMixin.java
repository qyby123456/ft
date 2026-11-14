package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(
    targets = {"com.github.L_Ender.cataclysm.event.ServerEventHandler"},
    remap = false
)
public abstract class NativeCatLocalLeftClickMixin {
    @Redirect(
        method = {"onPlayerInteract(Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$LeftClickEmpty;)V"},
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/L_Ender/cataclysm/items/ILeftClick;onLeftClick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"
        ),
        require = 0,
        remap = false
    )
    private boolean ftboss$actualLeftClick(@Coerce Object var1, ItemStack var2, LivingEntity var3) {
        return NativeSpecialCaller.leftClick(var1, var2, var3);
    }
}
