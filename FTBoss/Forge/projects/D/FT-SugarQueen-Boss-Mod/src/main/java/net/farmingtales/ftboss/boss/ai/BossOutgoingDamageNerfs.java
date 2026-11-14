package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 庄园 Boss 的**最终出伤统一调整**（削减 + 提高）。
 *
 * <p>放在 {@link EventPriority#LOW}，让庄园自适应、非庄园 Boss ×2、折耳根虚空附伤等
 * 既有修正先结算，再统一按“玩家当前实际看到的伤害”调整。投射物、庄园包装伤害、
 * Iron 召唤物与带主人标记的小怪都会向上追溯，避免只改到近战而漏掉技能。
 *
 * <p><b>2026-09-21 用户要求「折耳根的虚空伤害减半，虚空buff伤害减半，伤害减半」</b>：
 * 当时她已经是 ×0.5，用户确认要**在现有基础上再减半**（累计保留 1/4）。三条出伤
 * 都走这一个倍率，所以只改这一个常量就同时命中：
 * <ol>
 *   <li><b>普通出伤</b>：近战 / 技能 / 投戟 / 铁魔法 —— 本类的 {@code onHurt} 直接乘；</li>
 *   <li><b>虚空附伤</b>（连击转虚空那发穿甲伤害）：它是一次独立的 {@code hurt}，同样被本类乘到
 *       （{@code fellOutOfWorld} 没有攻击者实体，靠 {@code ManorMercy.withOwner} 归因）；</li>
 *   <li><b>虚空侵蚀 buff</b>：无序言伤害，走 {@link #zheergenDamage}，与这里同一个常量。</li>
 * </ol>
 *
 * <p><b>2026-09-21 同日用户要求「朵拉伤害提高1/2，雪黎伤害提高1/2」</b>：按本工程一贯口径
 * "提高 1/2" = ×(1 + 1/2) = **×1.5**，加在同一个 {@link #multiplier} 表里 —— 于是
 * 朵拉的狙击（{@code DoraSniperDamage} 的三段伤害**自带主人**）、她的枪械与近战、
 * 雪黎的碾压轮 / 挥击 / 冰霜、以及**雪黎的女仆**（靠 {@code ManorFightMembers.owner} /
 * {@code MinionBlessing} 追溯回本体）全都一起提高，不用逐个技能去改常量。
 */
public final class BossOutgoingDamageNerfs {
    /** 折耳根全部伤害**保留 1/4**（2026-09-21 用户要求再减半：0.5 → 0.25）。 */
    public static final float ZHEERGEN_MULTIPLIER = 0.25F;
    /** 屠夫全部伤害减少 3/4，即保留 1/4。 */
    public static final float BUTCHER_MULTIPLIER = 0.25F;
    /** 朵拉全部伤害**提高 1/2**（2026-09-21 用户要求）。 */
    public static final float DORA_MULTIPLIER = 1.5F;
    /** 雪黎全部伤害**提高 1/2**（2026-09-21 用户要求；含她的女仆）。 */
    public static final float XUELI_MULTIPLIER = 1.5F;

    private BossOutgoingDamageNerfs() {}

    /** Applies the same final modifier to Zheergen's source-less status-effect damage. */
    public static float zheergenDamage(float amount) {
        return amount * ZHEERGEN_MULTIPLIER;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHurt(LivingHurtEvent event) {
        if (event.isCanceled() || XueliCrushDamage.isActive(event.getEntity())) return;
        float amount = event.getAmount();
        if (!Float.isFinite(amount) || amount <= 0.0F) return;
        try {
            float multiplier = multiplier(source(event));
            if (multiplier != 1.0F) event.setAmount(amount * multiplier);
        } catch (Throwable ignored) {
            // 出伤削减出问题时宁可保留原伤害，也不能打断 Forge 伤害结算。
        }
    }

    private static Entity source(LivingHurtEvent event) {
        Entity source = event.getSource().getEntity();
        if (source == null) source = event.getSource().getDirectEntity();
        if (source == null) source = ManorMercy.damageOwner();
        return source;
    }

    private static float multiplier(Entity source) {
        Entity current = source;
        for (int depth = 0; current != null && depth < 8; depth++) {
            // 先检查当前实体，再找主人：柠檬猫召来的屠夫本体也必须吃屠夫削减。
            if (current instanceof EasterBossEntity boss
                    && boss.getVariant() == ShadowVoidSplit.SHADOW_VARIANT && !boss.isMini()) {
                return ZHEERGEN_MULTIPLIER;
            }
            if (current instanceof MeatMountainEntity) return BUTCHER_MULTIPLIER;
            /* 2026-09-21 用户要求「朵拉伤害提高1/2，雪黎伤害提高1/2」。
               放在同一张表里，是为了白拿下面那段"向上追溯主人"的遍历：
               雪黎的女仆 / 朵拉的召唤物打出来的伤害也算她们自己的伤害。
               ⚠ 这里**故意不排除迷你分身**（上面两条削减才排除）：雪黎的「自爆分身」是她自己的招式，
               那些爆炸伤害当然也该跟着提高 1/2。 */
            if (current instanceof EasterBossEntity manor) {
                if (manor.getVariant() == EasterBossEntity.DORA_VARIANT) return DORA_MULTIPLIER;
                if (manor.getVariant() == EasterBossEntity.XUELI_VARIANT) return XUELI_MULTIPLIER;
            }

            // A marked combat minion names its immediate boss. Check that before the Manor root-owner
            // marker, which intentionally skips intermediate owners (for example Butcher -> Lemon Cat).
            Entity owner = null;
            java.util.UUID marked = MinionBlessing.ownerId(current);
            if (marked != null && current.level() instanceof ServerLevel level) owner = level.getEntity(marked);
            if (owner == null || owner == current) owner = ManorFightMembers.owner(current);
            if (owner == null || owner == current) {
                try {
                    Object summoner = current.getClass().getMethod("getSummoner").invoke(current);
                    if (summoner instanceof Entity found && found != current) owner = found;
                } catch (Throwable ignored) {
                }
            }
            if (owner == null || owner == current) break;
            current = owner;
        }
        return 1.0F;
    }
}
