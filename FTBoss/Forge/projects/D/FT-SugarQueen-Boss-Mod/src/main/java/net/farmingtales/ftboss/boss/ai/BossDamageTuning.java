package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

/**
 * Boss 伤害相关的**共用工具**(只剩真实伤害来源; 全局倍率已烘焙进数据)。
 *
 * <p><b>2026-09-13 v77 重要变更 —— 运行时伤害钩子已删除。</b>
 *
 * <p>历史: v69 起这里挂着一个 {@code LivingHurtEvent} 钩子, 每次玩家受伤都要现场判断
 * "这一下是不是本模组 Boss 打的", 然后乘 全局 ×0.5(+ 彩蛋九人 ×4/3)。
 * 用户要求"把全局修改过的数据直接写进数据里, 减少逐次计算", 于是现在:
 *
 * <ul>
 *   <li>**彩蛋系**(0~10 变体 + 它的投射物/召唤物): 净倍率 {@code 0.5 × 4/3 = 2/3} 已折进
 *       {@code EasterBossEntity.BOSS_DAMAGE_MULT}(2.0 → 4/3) —— 所有 {@code × BOSS_DAMAGE_MULT}
 *       出来的数字就是玩家实际吃到的伤害;</li>
 *   <li>**非彩蛋系**(食物系 Boss / 召唤物 / 炮弹等): 相关数字已逐个乘 0.5(近战 {@code ATTACK_DAMAGE} 一起);</li>
 *   <li>个别"没有攻击者实体"的伤害(虚空/真实/火焰/粉碎轮)本来就不吃钩子, 数字保持原样;</li>
 *   <li>爆炸伤害当时也吃 0.5 —— 现在按"威力近似线性"把爆炸威力对半(已在各处注明)。</li>
 * </ul>
 *
 * <p>批量改写的脚本留在 {@code tools/bake_global_damage.cjs}(带断言, 改错会报出来)。
 * 想再整体调整"Boss 对玩家伤害"时: 彩蛋系改 {@code BOSS_DAMAGE_MULT} 一个数,
 * 非彩蛋系跑那个脚本的表(或直接改各自数字)。
 */
public final class BossDamageTuning {

    private BossDamageTuning() {}

    /* ==================== 真实伤害来源 ==================== */

    /** armamentarium 的"真实伤害"类型(无视护甲/抗性); 取不到就退回 magic */
    private static net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> TRUE_DMG_KEY = null;
    private static boolean TRUE_DMG_TRIED = false;

    /**
     * 共用的**真实伤害**来源(功底血怒的 2% 附伤用它)。
     *
     * <p>类型优先级沿用 {@code DefectOrbs} 光束那套: 从 armamentarium 的 bypasses_armor 名单里挑第一个存在的
     * (electrocution 最贴合); 没有该 mod 就退回 {@code magic()}(同样无视护甲, 但会被魔法抗性减免)。
     */
    public static DamageSource trueDamage(ServerLevel sl, Entity src) {
        if (!TRUE_DMG_TRIED) {
            TRUE_DMG_TRIED = true;
            try {
                var reg = sl.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE);
                for (String p : new String[]{"electrocution", "assassination", "hemorrhage", "frenzied_burn", "venom", "cacophony", "doom"}) {
                    var key = net.minecraft.resources.ResourceKey.create(
                            net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                            new net.minecraft.resources.ResourceLocation("armamentarium", p));
                    if (reg.containsKey(key)) {
                        TRUE_DMG_KEY = key;
                        FTBossMod.LOGGER.info("[FTBoss][伤害] 真实伤害类型 = armamentarium:{}", p);
                        break;
                    }
                }
            } catch (Throwable ignored) {
            }
            if (TRUE_DMG_KEY == null) FTBossMod.LOGGER.info("[FTBoss][伤害] 未找到 armamentarium 真实伤害类型 -> 退回魔法伤害");
        }
        if (TRUE_DMG_KEY == null) return sl.damageSources().magic();
        try {
            var holder = sl.registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(TRUE_DMG_KEY);
            return new DamageSource(holder, src, src);
        } catch (Throwable t) {
            return sl.damageSources().magic();
        }
    }
}
