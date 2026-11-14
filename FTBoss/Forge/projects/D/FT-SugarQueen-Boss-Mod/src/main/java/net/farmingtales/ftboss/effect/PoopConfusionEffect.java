package net.farmingtales.ftboss.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 【屎？！！】(2026-09-12 新增, 7 号 Boss 炫彩答辩超人专属):
 * 被答辩超人打中就叠一层(见 {@code BossHandler#onBossHitPlayer} 与 {@code PoopCurse})。
 *
 * <p>两条效果:
 * <ul>
 *   <li><b>扣法力</b>: 每秒(20t)扣一次 Iron's Spells 的法力, 层数越高扣得越多。这里用**反射**调用
 *       {@code MagicData}, 铁魔法没装时静默跳过, 不会因为缺类把游戏搞崩。</li>
 *   <li><b>方向键位混乱</b>: 在**客户端**实现({@code client/PoopConfusionClient}, 挂
 *       {@code MovementInputUpdateEvent}) —— 1 层 A/D 反向, 2 层前后左右全反, 3 层以上整体转 90°。</li>
 * </ul>
 */
public class PoopConfusionEffect extends MobEffect {

    /** 每层每秒扣的法力基数 */
    private static final float MANA_PER_STACK = 3.0F;

    public PoopConfusionEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B5A2B);   // 屎褐色
    }

    @Override
    public boolean isBeneficial() {
        return false;
    }

    /** 每秒结算一次 */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide || !(entity instanceof Player player)) return;
        float drain = MANA_PER_STACK + amplifier * 2.0F;      // 1 层 3/s …… 5 层 11/s
        try {
            Class<?> mdClass = Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData");
            Object md = mdClass.getMethod("getPlayerMagicData", LivingEntity.class).invoke(null, player);
            if (md == null) return;
            float mana = (Float) mdClass.getMethod("getMana").invoke(md);
            if (mana <= 0.0F) return;
            mdClass.getMethod("setMana", float.class).invoke(md, Math.max(0.0F, mana - drain));
        } catch (Throwable ignored) {
            // 铁魔法缺失 / 版本不匹配: 只保留方向键混乱
        }
    }
}
