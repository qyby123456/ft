package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;

public final class ManorTridentDamageHooks {
    private ManorTridentDamageHooks() {}

    public static float damage(float amount, ItemStack stack, Entity owner) {
        String identifier = ManorWeaponEnhancements.id(stack);
        return owner instanceof ServerPlayer && identifier.startsWith("tridents_n_stuff:") && ManorWeaponBalanceRules.doubled(identifier)
                ? amount * ManorWeaponBalanceRules.DAMAGE_SCALE : amount;
    }

    public static LightningBolt lightning(LightningBolt bolt, ItemStack stack, Entity owner) {
        bolt.setDamage(damage(bolt.getDamage(), stack, owner));
        return bolt;
    }
}
