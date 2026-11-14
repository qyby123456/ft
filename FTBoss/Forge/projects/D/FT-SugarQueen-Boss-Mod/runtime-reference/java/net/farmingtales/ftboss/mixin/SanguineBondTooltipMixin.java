/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
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
@Mixin(targets={"me.fzzyhmstrs.tridents_n_stuff.item.TnsTridentItem"}, remap=false)
public abstract class SanguineBondTooltipMixin {
    @Inject(method={"m_7373_", "appendHoverText"}, at={@At(value="TAIL")}, remap=false, require=0)
    private void ftboss$appendSkills(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo callback) {
        if (!SanguineBondHooks.isSanguine(stack)) {
            return;
        }
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.sanguine_bond.skill_one"));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.sanguine_bond.skill_two"));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.sanguine_bond.bleeding"));
    }
}

