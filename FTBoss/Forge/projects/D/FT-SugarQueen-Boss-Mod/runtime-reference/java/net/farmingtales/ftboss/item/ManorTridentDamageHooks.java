/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.ManorWeaponEnhancements;
import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;

public final class ManorTridentDamageHooks {
    private ManorTridentDamageHooks() {
    }

    public static float damage(float f, ItemStack itemStack, Entity entity) {
        String string = ManorWeaponEnhancements.id(itemStack);
        return entity instanceof ServerPlayer && string.startsWith("tridents_n_stuff:") && ManorWeaponBalanceRules.doubled(string) ? f * 2.0f : f;
    }

    public static LightningBolt lightning(LightningBolt lightningBolt, ItemStack itemStack, Entity entity) {
        lightningBolt.setDamage(ManorTridentDamageHooks.damage(lightningBolt.getDamage(), itemStack, entity));
        return lightningBolt;
    }
}

