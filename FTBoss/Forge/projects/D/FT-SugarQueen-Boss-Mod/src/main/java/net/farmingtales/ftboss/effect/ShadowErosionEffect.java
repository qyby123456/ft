package net.farmingtales.ftboss.effect;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.boss.ai.BossOutgoingDamageNerfs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 【虚空侵蚀】(2026-09-14 v118 按用户要求「允许虚空持续伤害叠加，可以做成debuff」)。
 *
 * <p>折耳根每次打中玩家就叠 **1 层**(上限 {@value #MAX_STACKS})、时长刷新为 {@value #TICKS} tick(10 秒);
 * 每秒(20t)按层数结算一次 **虚空伤害**({@code fellOutOfWorld}: 自带穿甲, 不吃护甲与魔法减免)——
 * 也就是"层数越高每秒掉得越多"；基础每层 4 点再吃折耳根的最终出伤倍率
 * ({@code BossOutgoingDamageNerfs.ZHEERGEN_MULTIPLIER}，2026-09-21 起 **0.25**),
 * 所以实际是 **1 点/层/秒、5 层 5 点/秒**（当天先由 4 减半到 2，用户要求再减半后为 1）。
 *
 * <p>之所以做成真正的状态效果(而不是 Boss 内部的一张计时表): 玩家能在 HUD 上**看见**自己中了几层、
 * 还有多久; 层数也天然跟着原版的"同名效果取更高等级 + 刷新时长"规则走。
 */
public class ShadowErosionEffect extends MobEffect {

    /** 每层每秒的虚空伤害 */
    public static final float DAMAGE_PER_STACK = 4.0F;
    /** 最大层数(等级 = 层数 - 1) */
    public static final int MAX_STACKS = 5;
    /** 持续时间(10 秒) */
    public static final int TICKS = 10 * 20;

    public ShadowErosionEffect() {
        super(MobEffectCategory.HARMFUL, 0x7A2EBE);   // 虚空紫
    }

    @Override
    public boolean isBeneficial() {
        return false;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>();
    }

    /** 每秒结算一次(和原版其它 DoT 一样用 duration 取模) */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        float dmg = BossOutgoingDamageNerfs.zheergenDamage(DAMAGE_PER_STACK * (amplifier + 1));
        entity.invulnerableTime = 0;
        entity.hurt(entity.damageSources().fellOutOfWorld(), net.farmingtales.ftboss.boss.ai.ManorCombatHalving.statusDamage(dmg));
    }

    /**
     * 当前层数（0 ~ {@link #MAX_STACKS}）；没中这个效果就是 0。
     *
     * <p>按 {@code amplifier + 1} 反推 —— 施加处（{@code EasterBossEntity.applyShadowVoid}）
     * 写的正是 {@code stacks - 1}，两边口径一致。
     */
    public static int stacksOf(LivingEntity entity) {
        if (entity == null) return 0;
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof ShadowErosionEffect) {
                return Math.min(MAX_STACKS, inst.getAmplifier() + 1);
            }
        }
        return 0;
    }

    /**
     * 是否已叠到**满层**。
     *
     * <p>★ 2026-10-10 按用户要求新增，作为折耳根被动「往日之影」的触发条件
     * （满层后他每一次技能都会留下残影，见 {@code EasterBossEntity.onManorFiveCastStart}）。
     */
    public static boolean atFullStacks(LivingEntity entity) {
        return stacksOf(entity) >= MAX_STACKS;
    }
}
