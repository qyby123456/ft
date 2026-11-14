/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 */
package net.farmingtales.ftboss.lemonmotion;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;

public final class LemonMotionServer {
    private static final ThreadLocal<Intent> INTENT = new ThreadLocal();

    private LemonMotionServer() {
    }

    public static void summon(EasterBossEntity easterBossEntity, int n, int n2) {
        LemonMotionServer.cast(easterBossEntity, n, n2, "summon");
    }

    public static void absorb(EasterBossEntity easterBossEntity, int n, int n2) {
        LemonMotionServer.cast(easterBossEntity, n, n2, "absorb");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void cast(EasterBossEntity easterBossEntity, int n, int n2, String string) {
        Intent intent = INTENT.get();
        INTENT.set(new Intent(easterBossEntity, string));
        try {
            easterBossEntity.playCast(n, n2);
        }
        finally {
            if (intent == null) {
                INTENT.remove();
            } else {
                INTENT.set(intent);
            }
        }
    }

    public static void stamp(EasterBossEntity easterBossEntity, CompoundTag compoundTag) {
        String string;
        if (easterBossEntity.getVariant() != 4) {
            return;
        }
        Intent intent = INTENT.get();
        String string2 = intent != null && intent.owner() == easterBossEntity ? intent.id() : (string = !compoundTag.m_128471_("cast") && compoundTag.m_128471_("impact") ? "strike" : "");
        if (!string.isEmpty()) {
            compoundTag.m_128359_("lemon_motion_v1", string);
            compoundTag.m_128405_("lemon_motion_schema", 1);
        }
    }

    private record Intent(EasterBossEntity owner, String id) {
    }
}

