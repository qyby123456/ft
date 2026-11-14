package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * 2026-09-20 (v183) 按用户要求：文玄蓝「超能光束」不再造成伤害，改成**眩晕 + 清空玩家增益**。
 * 眩晕直接复用庄园本体那套硬定身({@code EasterBossEntity#stunPlayerAny} → 每 tick 拉回锚点)，
 * 而"清增益"是独立的一件事，放这里以后别的技能也能用。
 */
public final class PlayerBuffRules {
    private PlayerBuffRules() {}

    /** 清空目标的**增益**效果（等价于喝一桶奶，但不动减益），返回清掉的数量。 */
    public static int clearBuffs(LivingEntity target) {
        if (target == null) return 0;
        int cleared = 0;
        // getActiveEffects() 是活集合，必须先拷一份再删，否则 ConcurrentModificationException
        for (MobEffectInstance instance : new ArrayList<>(target.getActiveEffects())) {
            if (instance.getEffect().isBeneficial() && target.removeEffect(instance.getEffect())) cleared++;
        }
        return cleared;
    }
}
