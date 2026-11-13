package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.effect.HealBanEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "ftboss")
public final class HealBanAbsorptionGuard {
    private HealBanAbsorptionGuard() {}

    private static void clearAbsorption(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.ABSORPTION)) entity.removeEffect(MobEffects.ABSORPTION);
        if (entity.getAbsorptionAmount() > 0.0F) entity.setAbsorptionAmount(0.0F);
    }

    private static boolean isServerBanned(LivingEntity entity) {
        return !entity.level().isClientSide && HealBan.isBanned(entity);
    }

    private static void suppressAbsorption(LivingEntity entity) {
        if (isServerBanned(entity)) clearAbsorption(entity);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide && event.getEffectInstance().getEffect().value() instanceof HealBanEffect) clearAbsorption(entity);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect().equals(MobEffects.ABSORPTION) && isServerBanned(event.getEntity())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living) suppressAbsorption(living);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        suppressAbsorption(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        suppressAbsorption(event.getEntity());
    }
}
