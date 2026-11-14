package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 【附加减益】(2026-09-14 v119)。
 *
 * <p>把"给玩家挂外来效果"这件事收在一处, 免得每个技能各写一份(以前雪黎飞刀里就有一份):
 * <ul>
 *   <li>{@link #bleed} —— **流血**: 先用传奇怪物的 {@code bleeding}, 缺 mod 回退三叉戟与玩意的
 *       {@code bleeding}, 再叠 {@code armamentarium:blood_loss}(和屠夫剁骨刀同源); 缺 mod 自动跳过。</li>
 *   <li>{@link #blueFire} —— **蓝火**: 本模组自己的效果 {@code ftboss:blue_fire}(每秒 20 点虚空伤害)。</li>
 * </ul>
 */
public final class ExtraDebuffs {

    private static MobEffect bleedEffect;
    private static MobEffect bloodLossEffect;

    private ExtraDebuffs() {}

    /**
     * 流血(单位: tick)。
     *
     * <p><b>v124 按用户反馈「朵拉的对玩家造成流血效果未生效」重写</b>: 以前是**借别的模组**的流血
     * ({@code legendary_monsters:bleeding} / {@code tridents_n_stuff:bleeding} / {@code armamentarium:blood_loss}),
     * 实测那两个 {@code bleeding} 虽然注册名存在, 但挂上去基本不掉血(它们的伤害是绑在自己武器的结算里的),
     * 等于挂了个图标。
     *
     * <p>现在主用**本模组自己的** {@link net.farmingtales.ftboss.effect.BleedEffect}(每秒按层数掉血,
     * 穿甲, 不依赖任何外部模组); 只有在本模组效果拿不到时(注册表还没就绪)才退回外来的。
     */
    public static void bleed(LivingEntity target, int ticks, int amp) {
        if (target == null || target.level().isClientSide) return;
        int a = Math.max(0, Math.min(4, amp));
        MobEffect own = FTEffects.BLEED.get();
        String used;
        if (own != null) {
            target.addEffect(new MobEffectInstance(own, ticks, a, false, true, true));
            used = "ftboss:bleed";
        } else {
            if (bleedEffect == null) {
                bleedEffect = find("legendary_monsters", "bleeding");
                if (bleedEffect == null) bleedEffect = find("tridents_n_stuff", "bleeding");
            }
            if (bloodLossEffect == null) bloodLossEffect = find("armamentarium", "blood_loss");
            if (bleedEffect != null) {
                target.addEffect(new MobEffectInstance(bleedEffect, ticks, a, false, true, true));
            }
            if (bloodLossEffect != null) {
                target.addEffect(new MobEffectInstance(bloodLossEffect, ticks, a, false, true, true));
            }
            used = "外来(" + (bleedEffect != null || bloodLossEffect != null) + ")";
        }
        logBleed(target, ticks, a, used);
    }

    /* v124 诊断: 证明流血真的挂上了(每个目标 3 秒最多一条) */
    private static final java.util.Map<java.util.UUID, Long> LAST_BLEED_LOG = new java.util.concurrent.ConcurrentHashMap<>();

    private static void logBleed(LivingEntity target, int ticks, int amp, String used) {
        if (!(target.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
        long now = sl.getGameTime();
        Long last = LAST_BLEED_LOG.get(target.getUUID());
        if (last != null && now - last < 60L) return;
        if (LAST_BLEED_LOG.size() > 64) {
            LAST_BLEED_LOG.entrySet().removeIf(e -> now - e.getValue() >= 60L);
        }
        LAST_BLEED_LOG.put(target.getUUID(), now);
        FTBossMod.LOGGER.info("[FTBoss][流血] {} 挂上流血 {} 秒 x{} 层 (来源 {}, 每秒 {} 点)",
                target.getName().getString(), ticks / 20, amp + 1, used,
                String.format("%.1f", net.farmingtales.ftboss.effect.BleedEffect.DAMAGE_PER_SECOND * (amp + 1)));
    }

    /** 蓝火(本模组效果: 每秒 20 点虚空伤害) */
    public static void blueFire(LivingEntity target, int ticks) {
        if (target == null || target.level().isClientSide) return;
        MobEffect eff = FTEffects.BLUE_FIRE.get();
        if (eff == null) return;
        target.addEffect(new MobEffectInstance(eff, ticks, 0, false, true, true));
    }

    private static MobEffect find(String ns, String path) {
        try {
            return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(ns, path));
        } catch (Throwable t) {
            return null;
        }
    }
}
