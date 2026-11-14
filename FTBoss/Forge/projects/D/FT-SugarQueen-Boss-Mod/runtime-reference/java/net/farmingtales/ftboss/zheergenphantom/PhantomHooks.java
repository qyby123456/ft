/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 */
package net.farmingtales.ftboss.zheergenphantom;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.zheergenphantom.PhantomAccess;
import net.farmingtales.ftboss.zheergenphantom.PhantomSnapshot;
import net.farmingtales.ftboss.zheergenphantom.PhantomTimeline;
import net.minecraft.server.level.ServerLevel;

public final class PhantomHooks {
    private static final Map<EasterBossEntity, State> ACTIVE = new WeakHashMap<EasterBossEntity, State>();

    private PhantomHooks() {
    }

    private static boolean enabled(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 7 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && easterBossEntity.m_6084_() && easterBossEntity.m_9236_() instanceof ServerLevel;
    }

    public static void capture(EasterBossEntity easterBossEntity) {
        PhantomSnapshot phantomSnapshot;
        EasterBossEntity easterBossEntity2;
        if (!PhantomHooks.enabled(easterBossEntity) || !((easterBossEntity2 = easterBossEntity) instanceof PhantomAccess)) {
            return;
        }
        PhantomAccess phantomAccess = easterBossEntity2;
        easterBossEntity2 = (ServerLevel)easterBossEntity.m_9236_();
        State state = ACTIVE.get(easterBossEntity);
        if (state == null || state.level() != easterBossEntity2) {
            state = new State((ServerLevel)easterBossEntity2, new PhantomTimeline());
            ACTIVE.put(easterBossEntity, state);
        }
        if ((phantomSnapshot = phantomAccess.ftboss$phantomSnapshot()).skill() == null) {
            return;
        }
        state.timeline().schedule(easterBossEntity2.m_46467_(), phantomSnapshot);
        phantomAccess.ftboss$phantomClear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void tick(EasterBossEntity easterBossEntity) {
        State state = ACTIVE.get(easterBossEntity);
        if (state == null) {
            return;
        }
        if (!PhantomHooks.enabled(easterBossEntity) || state.level() != easterBossEntity.m_9236_()) {
            ACTIVE.remove(easterBossEntity);
            return;
        }
        EasterBossEntity easterBossEntity2 = easterBossEntity;
        if (!(easterBossEntity2 instanceof PhantomAccess)) {
            ACTIVE.remove(easterBossEntity);
            return;
        }
        PhantomAccess phantomAccess = easterBossEntity2;
        for (PhantomSnapshot phantomSnapshot : state.timeline().poll(state.level().m_46467_())) {
            phantomAccess.ftboss$phantomRestore(phantomSnapshot);
            try {
                phantomAccess.ftboss$phantomReplay(state.level());
            }
            finally {
                phantomAccess.ftboss$phantomClear();
            }
        }
        if (state.timeline().size() == 0) {
            ACTIVE.remove(easterBossEntity);
        }
    }

    private record State(ServerLevel level, PhantomTimeline timeline) {
    }
}

