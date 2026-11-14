/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.QizaiSpellBridge;
import net.farmingtales.ftboss.skill.QizaiConstellation;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class QizaiMulticast {
    public static final int SPELLS_PER_CAST = 6;
    public static final int SUPPORT_PER_CAST = 2;

    private QizaiMulticast() {
    }

    public static List<ManorSpellRules.Kind> supportPool() {
        return Arrays.stream(ManorSpellRules.Kind.values()).filter(ManorSpellRules.Kind::support).filter(kind -> kind.variant != 11).toList();
    }

    public static boolean cast(ServerLevel level, EasterBossEntity boss, int kind, List<Vec3> marks, Vec3 aimPoint) {
        List<ManorSpellRules.Kind> support;
        if (!QizaiRules.constellation(kind) || !QizaiSpellBridge.cast(level, boss, kind, marks)) {
            return false;
        }
        ArrayList<Object> fired = new ArrayList<Object>();
        fired.add(QizaiRules.id(kind) + "(\u4e3b)");
        int secondary = kind == 5 ? 6 : 5;
        List<Vec3> secondaryMarks = QizaiConstellation.marks(level, (LivingEntity)boss, secondary, aimPoint);
        if (!secondaryMarks.isEmpty() && QizaiSpellBridge.cast(level, boss, secondary, secondaryMarks)) {
            fired.add(QizaiRules.id(secondary));
        }
        if (boss.castQizaiArsenal(aimPoint)) {
            fired.add("crystal_missiles");
            fired.add("prism_lasers");
        }
        if (!(support = QizaiMulticast.supportPool()).isEmpty()) {
            int seed = Math.floorMod(level.m_46467_() / 20L, support.size());
            for (int index = 0; index < 2; ++index) {
                ManorSpellRules.Kind pick = support.get((seed + index * 3) % support.size());
                if (!QizaiSpellBridge.castSupport(level, boss, pick.id)) continue;
                fired.add(pick.id);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Qizai] \u4e00\u6b21\u8bfb\u6761\u653e\u51fa {} \u95e8\u6cd5\u672f\uff08\u76ee\u6807 {} \u95e8\uff09: {}", new Object[]{fired.size(), 6, String.join((CharSequence)" + ", fired)});
        return true;
    }
}

