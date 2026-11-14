package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 【柠檬猫 · 继承来的 boss 技能】的伤害口径（2026-09-19 v159）。
 *
 * <p><b>用户要求</b>：「柠檬获取到 boss 技能后，修正原 boss 技能伤害到原本 boss 技能伤害 ×5」。
 * 追加澄清：「柠檬原技能伤害不变，只是修改获取的 boss 技能」——
 * 所以本类**只管继承来的那一套**（献祭召唤物后获得、直到柠檬猫死亡的技能），
 * 柠檬猫自己的猛火翻锅 / 借招 / 庄园法术 / 铁魔法一律不动。
 *
 * <p><b>为什么要单独一个类</b>：继承技能不是一处常数，而是散在 6 个落点，
 * 而且其中一部分是**和真身 Boss 共用同一份代码**的（见下），
 * 把倍率集中在这里才不会漏改、也不会误伤真身：
 * <ul>
 *   <li>{@code LemonInheritedAbilities} 里屠夫的近战、茶树的毒刺、酒桶王的
 *       {@code BarrelTactics} 三处硬编码伤害、魔法火球；</li>
 *   <li>{@link net.farmingtales.ftboss.entity.BarrelCannonballEntity} 的直击伤害与爆炸威力
 *       —— 它同时被**真的酒桶王**使用，所以只能按"这颗炮弹是不是继承召唤物"逐发判断；</li>
 *   <li>{@link net.farmingtales.ftboss.entity.EasterBossEntity} 召唤出的**助战 Boss**
 *       自己的技能 —— 它们走 {@code EasterBossEntity.manorSkillDamage}。</li>
 * </ul>
 *
 * <p><b>哪些不算"继承技能"（故意不乘）</b>：
 * <ul>
 *   <li>{@code TeaTactics.absorb()} 的吸血、{@code SayaBuds} 的护盾 —— 是回复/防御不是伤害；</li>
 *   <li>{@code EvokerFangs}（茶树树根）—— 原版把这个伤害**内联写死在私有方法里**
 *       （{@code dealDamageTo}，6.0F，1.20.1），本模组无法覆写；
 *       它单根只有 6 点，×5 也才 30，相对下表的几千点可以忽略，故按原样保留（已知差异）。</li>
 * </ul>
 *
 * <p><b>这个倍率会打到什么量级</b>（基数是改动前玩家实吃的值）：
 * <pre>
 *   屠夫近战   24~30        -> 120~150
 *   酒桶砸地    4.75        -> 23.75
 *   酒桶喷射    3.0         -> 15
 *   酒桶翻滚    2.9         -> 14.5
 *   茶树毒刺    2.5         -> 12.5
 *   魔法火球    11+4×tier    -> 55+20×tier
 *   炮弹直击    4.0         -> 20
 *   炮弹爆炸    威力 3.0     -> 15.0（爆炸伤害≈线性）
 * </pre>
 */
public final class LemonInheritedAbilityRules {

    /** 继承来的 boss 技能伤害倍率。 */
    public static final float MULTIPLIER = 5.0F;

    private LemonInheritedAbilityRules() {}

    /** 把继承技能的基础伤害放大到 {@link #MULTIPLIER} 倍。 */
    public static float damage(float baseDamage) {
        if (!Float.isFinite(baseDamage) || baseDamage <= 0.0F) return baseDamage;
        return baseDamage * MULTIPLIER;
    }

    /**
     * 这颗投射物是不是"柠檬猫继承来的东西"。
     *
     * <p>用 {@code ManorFightMembers.inherit(...)} 打的 {@code ftboss:lemon_inherited_until}
     * 标记来判定 —— 继承期间由柠檬猫生成的所有东西（炮弹/火球/小怪…）都带这个标记，
     * 而真身酒桶王射出的炮弹不带。
     */
    public static boolean inherited(Entity entity) {
        return entity != null && LemonSacrifice.isInheritedSummon(entity);
    }

    /**
     * 助战 Boss 的技能伤害：柠檬猫献祭继承期间，它召来的助战 Boss 本身也算"继承来的能力"，
     * 所以它们的 {@code manorSkillDamage} 也要 ×{@link #MULTIPLIER}。
     *
     * <p>只对**柠檬猫召来的**助战成立（{@link LemonSacrifice#isInheritedSummon}）；
     * 助战 Boss 在别的场合（例如自己本来就是场上 Boss）保持原伤害。
     */
    public static float allySkillDamage(LivingEntity attacker, float damage) {
        if (!(attacker instanceof EasterBossEntity)) return damage;
        return inherited(attacker) ? damage(damage) : damage;
    }
}
