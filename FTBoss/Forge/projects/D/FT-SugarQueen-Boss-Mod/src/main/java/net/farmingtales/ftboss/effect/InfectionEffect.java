package net.farmingtales.ftboss.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 【感染】(2026-09-11 新增): 被【！?区区？！】每咬一口叠一层。
 * 效果本体不改属性 —— "玩家攻击敌人时受到 层数 点伤害" 由 {@code BossHandler} 里的事件处理,
 * 这样无论玩家用什么方式打敌人(近战/远程/法术)都能触发。
 */
public class InfectionEffect extends MobEffect {

    public InfectionEffect() {
        super(MobEffectCategory.HARMFUL, 0xB08A46);
    }

    @Override
    public boolean isBeneficial() {
        return false;
    }
}
