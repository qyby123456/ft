package net.farmingtales.ftboss.skill;

import java.util.Arrays;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.QizaiSpellBridge;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * 2026-09-20 (v188) 按用户要求：**祁崽一次读条同时放 4 门法术**，并且**允许它抽庄园的辅助法术做防御**。
 *
 * <p>用户原话：「祁崽施法速度翻倍，法术半径翻倍，单次施法数量4个。移速加快，允许使用铁魔法防御」，
 * 并在选项里确认：单次施法 = 「一次读条同时放 4 门法术」；铁魔法防御 = 「允许它抽庄园的辅助法术」。
 *
 * <p>组成：**主招（星十字 / 玫瑰星陨）+ 另一门自己的招 + 2 门庄园辅助法术**（护盾 / 治疗那一类，
 * {@code ManorSpellRules.Kind.support()} 为真、且不属于它自己的变体）。四门共用同一条读条时间
 * （读条时长在 {@code ManorSignatureSpell} 里对祁崽再减半），所以是"一次读条甩四套"。
 *
 * <p>四门法术都通过 {@link QizaiSpellBridge} 走铁魔法（集成侧 {@code QizaiIronSpells} 提供实现）；
 * 铁魔法不可用时主招回退到纯代码版本（见 {@code QizaiMagic.release}），辅助则直接跳过。
 */
public final class QizaiMulticast {
    /** 一次读条同时放几门法术（用户指定 4） */
    public static final int SPELLS_PER_CAST = 6;
    /** 其中辅助（防御）类占几门 */
    public static final int SUPPORT_PER_CAST = 2;

    private QizaiMulticast() {}

    /** 可抽用的庄园辅助法术池：所有 damage == 0 的支援型法术，排除它自己变体的（那两门是攻击招）。 */
    public static List<ManorSpellRules.Kind> supportPool() {
        return Arrays.stream(ManorSpellRules.Kind.values())
                .filter(ManorSpellRules.Kind::support)
                .filter(kind -> kind.variant != EasterBossEntity.QIZAI_VARIANT)
                .toList();
    }

    /**
     * 释放一次「四门齐发」。
     *
     * @param kind     主招（必须是 {@link QizaiRules#constellation(int)} 里的那种）
     * @param marks    主招的地面标记（由调用方按既有流程算好）
     * @param aimPoint 瞄准点（用来给第二门招算标记）
     * @return false = 铁魔法这一侧没接上，调用方应当走原来的纯代码回退
     */
    public static boolean cast(ServerLevel level, EasterBossEntity boss, int kind, List<Vec3> marks, Vec3 aimPoint) {
        if (!QizaiRules.constellation(kind) || !QizaiSpellBridge.cast(level, boss, kind, marks)) return false;
        /* ★ v191: 日志改成把**真实放出去的法术 id** 列出来（以前只报"几门"，
           support 池里某门法术静默失效时从日志上看不出来 —— 排查"某个法术没效果"全靠这一行）。 */
        List<String> fired = new java.util.ArrayList<>();
        fired.add(QizaiRules.id(kind) + "(主)");
        int secondary = kind == QizaiRules.STAR_CROSS ? QizaiRules.ROSE_STARFALL : QizaiRules.STAR_CROSS;
        List<Vec3> secondaryMarks = QizaiConstellation.marks(level, boss, secondary, aimPoint);
        if (!secondaryMarks.isEmpty() && QizaiSpellBridge.cast(level, boss, secondary, secondaryMarks)) fired.add(QizaiRules.id(secondary));
        if (boss.castQizaiArsenal(aimPoint)) {
            fired.add("crystal_missiles");
            fired.add("prism_lasers");
        }
        List<ManorSpellRules.Kind> support = supportPool();
        if (!support.isEmpty()) {
            int seed = (int) Math.floorMod(level.getGameTime() / 20, support.size());
            for (int index = 0; index < SUPPORT_PER_CAST; index++) {
                ManorSpellRules.Kind pick = support.get((seed + index * 3) % support.size());
                if (QizaiSpellBridge.castSupport(level, boss, pick.id)) fired.add(pick.id);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Qizai] 一次读条放出 {} 门法术（目标 {} 门）: {}",
                fired.size(), SPELLS_PER_CAST, String.join(" + ", fired));
        return true;
    }
}
