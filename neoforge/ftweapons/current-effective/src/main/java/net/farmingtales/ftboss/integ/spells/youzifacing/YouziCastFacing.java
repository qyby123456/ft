package net.farmingtales.ftboss.integ.spells.youzifacing;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell.Aim;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.farmingtales.ftboss.youzi.facing.YouziFacing;
import net.minecraft.world.entity.LivingEntity;

public final class YouziCastFacing {
    private YouziCastFacing() {
    }

    public static void prepare(LivingEntity var0, Kind var1, MagicData var2) {
        if (var1 != null && YouziFacing.face(var0, var1.id)) {
            if (var1 == Kind.EFFIGY_VOLLEY && var2 != null && var2.getAdditionalCastData() instanceof Aim) {
                var2.setAdditionalCastData(new Aim(BossSpellEffects.aim(var0)));
            }
        }
    }
}
