package net.farmingtales.ftboss.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrossbowItem.class)
public interface NativeCrossbowArrowInvoker {
    @Invoker("createProjectile")
    Projectile ftboss$v6$invokeArrow(Level level, LivingEntity actor, ItemStack weapon, ItemStack ammunition, boolean critical);
}
