package net.farmingtales.ftboss.mixin;

import java.util.List;
import net.farmingtales.ftboss.integ.tridents.SanguineBondHooks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
    targets = {"me.fzzyhmstrs.tridents_n_stuff.item.TnsTridentItem"},
    remap = false
)
public abstract class SanguineBondTooltipMixin {
    @Inject(
        method = {"appendHoverText", "appendHoverText"},
        at = {@At("TAIL")},
        remap = false,
        require = 0
    )
    private void ftboss$appendSkills(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag var4, CallbackInfo var5) {
        if (SanguineBondHooks.isSanguine(stack)) {
            tooltip.add(Component.translatable("tooltip.ftboss.sanguine_bond.skill_one"));
            tooltip.add(Component.translatable("tooltip.ftboss.sanguine_bond.skill_two"));
            tooltip.add(Component.translatable("tooltip.ftboss.sanguine_bond.bleeding"));
        }
    }
}
