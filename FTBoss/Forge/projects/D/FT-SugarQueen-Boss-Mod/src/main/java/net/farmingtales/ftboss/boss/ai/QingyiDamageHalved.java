package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QingyiCombatRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 【青衣伤害减半】(2026-09-21 按用户要求「青衣的伤害减半，三叉戟召唤速度减半」)。
 *
 * <p><b>为什么挂在事件上而不是去改各技能的伤害常量</b>：青衣的出伤路径有六七条 ——
 * ① 原版近战（{@code doHurtTarget} 走 ATTACK_DAMAGE 结算）、② 庄园三招
 * （雷潮裂阵 140 / 苍雷领域 125 / 星戟天裁 180，经 {@code manorSkillDamage}）、
 * ③ 铁魔法闪电（回落值 64.8）、④ 激流冲刺（12 × BOSS_DAMAGE_MULT × QINGYI_DAMAGE_MULT）、
 * ⑤ 投戟两招、⑥ 三叉戟落雷（16 × QINGYI_DAMAGE_MULT）、
 * ⑦ 三叉戟本体命中补伤（层数 + ATTACK_DAMAGE×(QINGYI_DAMAGE_MULT−1)，以及「风暴追寻者」那记写死的 32）。
 * 其中 **① 与"原版三叉戟本体伤害"根本不走** {@code manorSkillDamage} —— 逐个改必漏，
 * 而且以后再添一招又会漏一次。这一处统一乘，与 {@link NonManorDamageX2}（非庄园 Boss 全局 ×2）
 * 是同一套写法，两者互不干扰（那份显式排除了 {@code EasterBossEntity}）。
 *
 * <p>三个细节（照抄 X2 那份的处理）：
 * <ol>
 *   <li>投射物/爆炸的 {@code source.getEntity()} 往往是**弹体**，要顺着 {@link Projectile#getOwner()}
 *       找回本体（坠戟 {@code QingyiTridentEntity} 继承原版 {@code ThrownTrident}，伤害来源的 entity 就是青衣）；</li>
 *   <li>只认青衣本体（{@code variant == 5}）；</li>
 *   <li>倍率出问题**绝不能让伤害结算崩掉**，整体兜底 try/catch。</li>
 * </ol>
 */
public final class QingyiDamageHalved {

    private QingyiDamageHalved() {}

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        try {
            Entity src = e.getSource().getEntity();
            if (src == null) src = e.getSource().getDirectEntity();
            if (src instanceof Projectile projectile && projectile.getOwner() != null) src = projectile.getOwner();
            if (!(src instanceof EasterBossEntity boss) || boss.getVariant() != EasterBossEntity.QINGYI_VARIANT) return;
            float amount = e.getAmount();
            if (!Float.isFinite(amount) || amount <= 0.0f) return;
            e.setAmount(amount * (float) QingyiCombatRules.OUTGOING_DAMAGE_MULT);
        } catch (Throwable ignored) {
            /* 兜底：倍率出问题绝不能让伤害结算崩掉 */
        }
    }
}
