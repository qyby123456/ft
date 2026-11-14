package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import net.farmingtales.ftboss.effect.HealBanEffect;
import net.farmingtales.ftboss.effect.ShadowErosionEffect;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ManorStatusImmunity {
    private ManorStatusImmunity() {}

    public static boolean blocks(LivingEntity entity, MobEffect effect) {
        return entity instanceof EasterBossEntity && (effect instanceof HealBanEffect || effect instanceof ShadowErosionEffect);
    }

    public static boolean allowed(boolean original, LivingEntity entity, MobEffectInstance effect) {
        return original && (effect == null || !blocks(entity, effect.getEffect()));
    }

    public static boolean protectRemoval(boolean original, MobEffectEvent.Remove event) {
        return original && !blocks(event.getEntity(), event.getEffect());
    }

    public static void purge(LivingEntity entity) {
        if (!(entity instanceof EasterBossEntity) || entity.level().isClientSide) return;
        for (MobEffectInstance effect : new ArrayList<>(entity.getActiveEffects())) {
            if (blocks(entity, effect.getEffect())) entity.removeEffect(effect.getEffect());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onApplicable(MobEffectEvent.Applicable event) {
        MobEffectInstance effect = event.getEffectInstance();
        if (effect != null && blocks(event.getEntity(), effect.getEffect())) event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTick(LivingEvent.LivingTickEvent event) {
        purge(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) purge(living);
    }
}
