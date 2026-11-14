package net.farmingtales.ftboss.item;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ThornCrownEffects {
    private static final String LAST_REFLECT_TICK = "ftboss_thorn_crown_last_reflect";

    private ThornCrownEffects() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        LivingEntity wearer = event.getEntity();
        if (wearer.level().isClientSide || event.getAmount() <= 0.0F
                || ThornCrownItem.equippedPieces(wearer) <= 0) return;
        if (event.getSource().is(DamageTypes.THORNS)) return;
        if (event.getSource().is(DamageTypeTags.IS_EXPLOSION) || event.getSource().is(DamageTypeTags.IS_PROJECTILE)) return;
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity attacker)) return;
        if (attacker == wearer || !attacker.isAlive() || wearer.isAlliedTo(attacker)) return;
        if (wearer instanceof Player player && attacker instanceof Player target && !player.canHarmPlayer(target)) return;
        long currentTick = wearer.level().getGameTime();
        var data = wearer.getPersistentData();
        if (data.contains(LAST_REFLECT_TICK)) {
            long elapsed = currentTick - data.getLong(LAST_REFLECT_TICK);
            if (elapsed >= 0 && elapsed < ThornCrownItem.REFLECT_COOLDOWN_TICKS) return;
        }
        data.putLong(LAST_REFLECT_TICK, currentTick);
        boolean doubled = SilverKeyItem.isEquipped(wearer);
        attacker.hurt(wearer.damageSources().thorns(wearer),
                ThornCrownItem.reflectedDamage(event.getAmount(), doubled));
    }
}
