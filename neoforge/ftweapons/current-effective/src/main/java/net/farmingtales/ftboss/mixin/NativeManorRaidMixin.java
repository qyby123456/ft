package net.farmingtales.ftboss.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = {"net.farmingtales.ftboss.entity.EasterBossEntity", "net.farmingtales.ftboss.boss.ai.BossScaling", "net.farmingtales.ftboss.boss.ai.ManorFiveSkills"}, remap = false)
public abstract class NativeManorRaidMixin {
}

