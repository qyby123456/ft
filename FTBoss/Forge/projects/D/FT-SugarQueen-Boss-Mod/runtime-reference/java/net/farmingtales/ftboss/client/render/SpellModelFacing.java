/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class SpellModelFacing {
    private SpellModelFacing() {
    }

    public static Vec3 direction(SpellVisualPacket spellVisualPacket, Entity entity, float f) {
        if (entity != null && spellVisualPacket.phase() != SpellVisualRules.Phase.BEAM && spellVisualPacket.spell().followsCaster(spellVisualPacket.phase())) {
            return entity.m_20252_(f);
        }
        return spellVisualPacket.end().m_82546_(spellVisualPacket.start());
    }
}

