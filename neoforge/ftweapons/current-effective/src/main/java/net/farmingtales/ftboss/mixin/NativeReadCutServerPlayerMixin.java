package net.farmingtales.ftboss.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(
   targets = {"net.minecraft.server.level.ServerPlayer"},
   priority = 1,
   remap = false
)
public abstract class NativeReadCutServerPlayerMixin {
}
