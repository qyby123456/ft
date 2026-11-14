/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob
 *  net.minecraft.world.entity.Entity
 */
package net.farmingtales.ftboss.client.sugarqueen;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.farmingtales.ftboss.client.sugarqueen.QueenPolicy;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.minecraft.world.entity.Entity;

public final class QueenBridge {
    private QueenBridge() {
    }

    public static float radiusScale(Entity entity, String string) {
        return QueenPolicy.validScale(SpellVisuals.radiusScale(entity, string));
    }

    public static float chargeTime(Entity entity, String string, float f) {
        if (!(entity instanceof AbstractSpellCastingMob)) {
            return QueenPolicy.chargePose(string, 0.18f);
        }
        AbstractSpellCastingMob abstractSpellCastingMob = (AbstractSpellCastingMob)entity;
        MagicData magicData = abstractSpellCastingMob.getMagicData();
        if (!magicData.isCasting() || !("ftboss:" + string).equals(magicData.getCastingSpellId())) {
            return QueenPolicy.chargePose(string, 0.18f);
        }
        return QueenPolicy.chargePose(string, QueenPolicy.chargeTime(magicData.getCastDuration(), magicData.getCastDurationRemaining(), f));
    }
}

