/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.integ.spells.youzifacing;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.youzi.facing.YouziFacing;
import net.minecraft.world.entity.LivingEntity;

public final class YouziCastFacing {
    private YouziCastFacing() {
    }

    public static void prepare(LivingEntity livingEntity, ManorSpellRules.Kind kind, MagicData magicData) {
        if (kind == null || !YouziFacing.face(livingEntity, kind.id)) {
            return;
        }
        if (kind == ManorSpellRules.Kind.EFFIGY_VOLLEY && magicData != null && magicData.getAdditionalCastData() instanceof BossSignatureSpell.Aim) {
            magicData.setAdditionalCastData((ICastData)new BossSignatureSpell.Aim(BossSpellEffects.aim(livingEntity)));
        }
    }
}

