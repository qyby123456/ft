package net.farmingtales.ftboss.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(
   targets = {"net.minecraft.world.entity.projectile.Projectile"},
   priority = 1,
   remap = false
)
public abstract class NativeReadCutProjectileMixin {
}
