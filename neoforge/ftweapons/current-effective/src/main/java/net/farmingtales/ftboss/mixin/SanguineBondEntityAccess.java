package net.farmingtales.ftboss.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(
    targets = {"me.fzzyhmstrs.tridents_n_stuff.entity.TnsTridentEntity"},
    remap = false
)
public interface SanguineBondEntityAccess {
    @Accessor("tridentStack")
    ItemStack ftboss$getTridentStack();
}
