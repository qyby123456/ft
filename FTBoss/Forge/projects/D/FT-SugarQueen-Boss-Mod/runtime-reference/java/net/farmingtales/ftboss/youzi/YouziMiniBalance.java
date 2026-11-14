/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 */
package net.farmingtales.ftboss.youzi;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class YouziMiniBalance {
    private YouziMiniBalance() {
    }

    public static void restore(EasterBossEntity easterBossEntity) {
        if (!easterBossEntity.isMini() || easterBossEntity.getVariant() != 0) {
            return;
        }
        AttributeInstance attributeInstance = easterBossEntity.m_21051_(Attributes.f_22276_);
        if (attributeInstance != null) {
            attributeInstance.m_22100_(75.0);
        }
        easterBossEntity.m_21153_(Math.min(easterBossEntity.m_21223_(), 75.0f));
    }
}

