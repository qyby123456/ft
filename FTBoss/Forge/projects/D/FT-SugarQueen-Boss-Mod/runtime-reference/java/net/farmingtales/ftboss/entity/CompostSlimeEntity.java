/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.monster.Slime
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

public class CompostSlimeEntity
extends Slime {
    public static final int LIFE = 900;

    public CompostSlimeEntity(EntityType<? extends CompostSlimeEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 0;
        this.m_21530_();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.m_33035_();
    }

    public void m_8119_() {
        super.m_8119_();
        if (!this.m_9236_().f_46443_ && this.f_19797_ > 900) {
            this.m_146870_();
        }
    }

    public boolean m_6785_(double distance) {
        return false;
    }
}

