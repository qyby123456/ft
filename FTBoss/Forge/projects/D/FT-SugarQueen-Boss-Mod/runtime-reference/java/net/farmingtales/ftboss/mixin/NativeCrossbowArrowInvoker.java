/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package net.farmingtales.ftboss.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={CrossbowItem.class})
public interface NativeCrossbowArrowInvoker {
    @Invoker(value="getArrow")
    public static AbstractArrow ftboss$v6$invokeArrow(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2) {
        throw new AssertionError((Object)"Mixin invoker not applied");
    }
}

