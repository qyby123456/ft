package net.farmingtales.ftboss.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.farmingtales.ftboss.entity.EasterBossEntity$PlayerReachMeleeGoal", remap = false)
public abstract class NativeManorBasicGoalMixin {
}
