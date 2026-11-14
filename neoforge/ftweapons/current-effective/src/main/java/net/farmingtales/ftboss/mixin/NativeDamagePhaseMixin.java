package net.farmingtales.ftboss.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({LivingEntity.class, Player.class})
public abstract class NativeDamagePhaseMixin {
}
