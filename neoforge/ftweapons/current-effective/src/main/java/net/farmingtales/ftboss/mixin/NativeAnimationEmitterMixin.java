package net.farmingtales.ftboss.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = {"net.farmingtales.ftboss.skill.SpellVisuals", "net.farmingtales.ftboss.integ.spells.ManorSignatureSpell"}, remap = false)
public abstract class NativeAnimationEmitterMixin {
}
