package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import net.farmingtales.ftboss.effect.HealBanEffect;
import net.farmingtales.ftboss.effect.ShadowErosionEffect;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = "ftboss")
public final class ManorStatusImmunity {
    private ManorStatusImmunity() {}

    public static boolean blocks(LivingEntity entity, MobEffect effect) {
        return entity instanceof EasterBossEntity && (effect instanceof HealBanEffect || effect instanceof ShadowErosionEffect);
    }

    public static boolean allowed(boolean original, LivingEntity entity, MobEffectInstance effect) {
        return original && (effect == null || !blocks(entity, effect.getEffect().value()));
    }

    public static boolean protectRemoval(boolean original, MobEffectEvent.Remove event) {
        return original && !blocks(event.getEntity(), event.getEffect().value());
    }

    public static void purge(LivingEntity entity) {
        if (entity instanceof EasterBossEntity && !entity.level().isClientSide) {
            for (MobEffectInstance effect : new ArrayList<>(entity.getActiveEffects())) {
                if (blocks(entity, effect.getEffect().value())) entity.removeEffect(effect.getEffect());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onApplicable(MobEffectEvent.Applicable event) {
        MobEffectInstance effect = event.getEffectInstance();
        if (effect != null && blocks(event.getEntity(), effect.getEffect().value())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity living) purge(living);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) purge(living);
    }
}
