package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QingyiCombatRules;

public final class QingyiTridentRecovery {
    private static final String PROGRESS_KEY = "ftbossQingyiTridentHealProgress";

    private QingyiTridentRecovery() {}

    public static void onSummoned(EasterBossEntity boss) {
        if (boss.getVariant() != EasterBossEntity.QINGYI_VARIANT || boss.isMini()
                || boss.level().isClientSide || !boss.isAlive()) return;
        var data = boss.getPersistentData();
        int progress = Math.floorMod(data.getInt(PROGRESS_KEY), QingyiCombatRules.TRIDENTS_PER_HEAL) + 1;
        data.putInt(PROGRESS_KEY, progress % QingyiCombatRules.TRIDENTS_PER_HEAL);
        if (progress == QingyiCombatRules.TRIDENTS_PER_HEAL) {
            boss.heal(QingyiCombatRules.TRIDENT_HEAL_AMOUNT);
        }
    }
}
