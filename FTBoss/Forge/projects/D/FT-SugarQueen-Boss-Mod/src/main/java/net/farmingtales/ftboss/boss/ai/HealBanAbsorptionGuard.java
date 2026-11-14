package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.HealBanEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 禁疗优先于伤害吸收：移除已有吸收效果和黄心，并拦截禁疗期间的新吸收。
 * 2026-10-15：仅服务端执行，不改变禁疗时长、生命值、普通伤害与其他防御效果。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class HealBanAbsorptionGuard {
    private HealBanAbsorptionGuard() {}

    private static void clearAbsorption(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.ABSORPTION)) {
            entity.removeEffect(MobEffects.ABSORPTION);
        }
        if (entity.getAbsorptionAmount() > 0.0F) {
            entity.setAbsorptionAmount(0.0F);
        }
    }

    private static boolean isServerBanned(LivingEntity entity) {
        return !entity.level().isClientSide && HealBan.isBanned(entity);
    }

    private static void suppressAbsorption(LivingEntity entity) {
        if (isServerBanned(entity)) {
            clearAbsorption(entity);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide && event.getEffectInstance().getEffect() instanceof HealBanEffect) {
            clearAbsorption(entity);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect() == MobEffects.ABSORPTION && isServerBanned(event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        suppressAbsorption(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            suppressAbsorption(event.player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        suppressAbsorption(event.getEntity());
    }
}
