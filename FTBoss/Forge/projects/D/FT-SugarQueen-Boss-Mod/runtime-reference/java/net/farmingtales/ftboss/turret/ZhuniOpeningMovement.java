/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.turret;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.phys.Vec3;

public final class ZhuniOpeningMovement {
    private static final double HORIZONTAL_RANGE = 1.2;
    private static final double HEIGHT = 0.85;
    private static final double BOB_RANGE = 0.16;

    private ZhuniOpeningMovement() {
    }

    public static Vec3 offset(long l) {
        return new Vec3(Math.sin((double)l * 0.045) * 1.2, 0.85 + Math.sin((double)l * 0.06) * 0.16, Math.cos((double)l * 0.033));
    }

    public static Vec3 preferred(EasterBossEntity easterBossEntity) {
        return easterBossEntity.m_20182_().m_82549_(ZhuniOpeningMovement.offset(easterBossEntity.m_9236_().m_46467_()));
    }
}

