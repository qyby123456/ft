package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * 【屎？！！】的施放入口(2026-09-12 晚按用户要求改版)。
 *
 * <p><b>旧行为已废弃</b>: 原本"Boss 每命中一次叠一层(最高 5 层)、召唤物咬中刷新" —— 施放太频繁,
 * 玩家几乎一直在方向键混乱。
 *
 * <p><b>新行为</b>: Boss 按冷却**概率施放一次**(见 {@code CompostBossEntity} 的 curseCd 段),
 * 命中就固定 I 级、固定 8 秒, **不叠层、命中不再触发、召唤物不再触发**。
 */
public final class PoopCurse {

    /** 单次施放的持续时间(8 秒) */
    public static final int DURATION = 160;
    /** 单次施放的等级(0 = I 级, 客户端 = A/D 反向) */
    public static final int AMPLIFIER = 0;

    private PoopCurse() {}

    /** Boss 概率施放一次: 固定 I 级 + 固定时长, 不叠层 */
    public static void castOnce(ServerPlayer player, String source) {
        if (player == null || !player.isAlive()) return;
        player.addEffect(new MobEffectInstance(FTEffects.POOP_CONFUSION.get(), DURATION, AMPLIFIER,
                false, true, true));
        player.displayClientMessage(Component.literal("§6屎？！！ §7← " + source
                + " §8| §c方向键混乱 §7+ §b法力流失"), true);
        if (player.level() instanceof ServerLevel sl) {
            Fx.particles(sl, player.getX(), player.getY() + 1.0, player.getZ(), 18,
                    ParticleTypes.ITEM_SLIME, 0.6, 0.1);
            Fx.particles(sl, player.getX(), player.getY() + 1.2, player.getZ(), 6,
                    ParticleTypes.LARGE_SMOKE, 0.4, 0.04);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SLIME_ATTACK, SoundSource.HOSTILE, 1.0f, 0.55f);
        }
        FTBossMod.LOGGER.info("[FTBoss][答辩超人] 向 {} 施放「屎？！！」(单次 {}t)",
                player.getName().getString(), DURATION);
    }

    /** 当前层数(0 = 没中) */
    public static int stacksOf(ServerPlayer player) {
        MobEffectInstance inst = player.getEffect(FTEffects.POOP_CONFUSION.get());
        return inst == null ? 0 : inst.getAmplifier() + 1;
    }
}
