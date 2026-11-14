package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 【连锁增益】(2026-09-12 v29 用户定案): 监听 {@code MobEffectEvent.Added} ——
 * 任何彩蛋 Boss **获得效果**时通知它自己(它再判断"是不是正面效果 + 冷却"),
 * 由 {@link EasterBossEntity#onBuffGainedHook} 落地: 额外 2 个随机增益 + 给玩家 1 个随机减益。
 *
 * <p>为什么不用 {@code addEffect} 覆写: 原版 {@code LivingEntity.addEffect} 是 **final**, 覆盖不了。
 */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class EasterBuffChain {

    private EasterBuffChain() {}

    @SubscribeEvent
    public static void onEffectAdded(net.minecraftforge.event.entity.living.MobEffectEvent.Added e) {
        if (e.getEntity() instanceof EasterBossEntity boss) {
            boss.onBuffGainedHook(e.getEffectInstance());
        }
    }
}
