package net.farmingtales.ftboss.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 【震颤】(2026-09-14 v133 按用户要求「下砸造成震颤效果，每十层震颤会眩晕玩家 1 秒」)。
 *
 * <p>来源：benger 的【连续下砸】每砸中一次给玩家叠 **1 层**（等级 = 层数 - 1，最多显示到 10 层）。
 * 叠到 **10 层**时立刻：眩晕 **1 秒**（缓慢 255 + 跳跃抑制 128 —— 玩家彻底动不了，比硬定身温和、
 * 也不需要服务端每 tick 拉锚点）并**清空自身**（层数归零，重新开始叠）。
 *
 * <p>做成状态效果而不是 Boss 内部计时表，理由和【虚空侵蚀】一样：玩家能在 HUD 上**看见**自己叠了几层、
 * 还差几层要被震晕。
 */
public class TremorEffect extends MobEffect {

    /** 触发眩晕的层数 */
    public static final int STUN_AT = 10;
    /** 最大层数（等级 = 层数 - 1） */
    public static final int MAX_STACKS = 10;
    /** 每层持续（10 秒不挨砸就自然清零） */
    public static final int TICKS = 10 * 20;
    /** 眩晕时长（tick） */
    public static final int STUN_TICKS = 20;

    public TremorEffect() {
        super(MobEffectCategory.HARMFUL, 0x8A6A2A);   // 土黄（震）
    }

    @Override
    public boolean isBeneficial() {
        return false;
    }

    /** 每 tick 检查一次（到 10 层要立刻结算） */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        if (amplifier + 1 < STUN_AT) return;
        /* 十层 → 眩晕 1 秒 + 清零 */
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, STUN_TICKS, 250, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffects.JUMP, STUN_TICKS, 128, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, STUN_TICKS + 20, 0, false, true, true));
        entity.setDeltaMovement(0.0, entity.getDeltaMovement().y, 0.0);
        if (entity instanceof Player p) {
            p.setSprinting(false);
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("§6【震颤】§7十层积聚 —— §c你被震得站不稳！"), true);
        }
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                    entity.getX(), entity.getY() + 1.6, entity.getZ(), 24, 0.4, 0.3, 0.4, 0.15);
            sl.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    net.minecraft.sounds.SoundEvents.ANVIL_LAND, net.minecraft.sounds.SoundSource.HOSTILE, 1.4f, 0.5f);
            net.farmingtales.ftboss.FTBossMod.LOGGER.info("[FTBoss][震颤] {} 叠满 {} 层 → 眩晕 {} 秒, 层数清零",
                    entity.getName().getString(), STUN_AT, STUN_TICKS / 20);
        }
        entity.removeEffect(this);
    }
}
