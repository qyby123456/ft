package net.farmingtales.ftboss.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 【血怒】(2026-09-14 v119 按用户要求「血怒也是[做成 buff]」)。
 *
 * <p>功底每命中玩家一层(最多 {@code EasterBossEntity.BLOOD_RAGE_MAX = 5} 层),
 * 每层 +50% 攻击(满层 +250%)、满层再 +40% 移速 —— 攻击力那份仍然由
 * {@code AttributeModifier}({@code BLOOD_RAGE_MOD}) 提供, 这个效果只负责**把层数显示出来**:
 * 等级 = 层数 - 1, 每次叠层刷新时长。这样玩家一眼能看出"功底现在几层血怒"。
 *
 * <p>注意它是 **BENEFICIAL**: 庄园 Boss 的 2 秒自净化只清**负面**效果(v106 起),
 * 所以这个增益不会被它自己洗掉。
 */
public class BloodRageEffect extends MobEffect {

    /** 每次叠层后刷新的时长(40 秒; 持续命中会一直续) */
    public static final int TICKS = 40 * 20;

    public BloodRageEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC82832);   // 血红
    }

    @Override
    public boolean isBeneficial() {
        return true;
    }
}
