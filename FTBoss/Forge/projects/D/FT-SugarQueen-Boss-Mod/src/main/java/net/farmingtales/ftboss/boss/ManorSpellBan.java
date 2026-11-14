package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorSpellBan {

    private ManorSpellBan() {}

    /** 由 FTBossMod 在确认 irons_spellbooks 已加载后反射调用 */
    @SubscribeEvent
    public static void onPreCast(io.redspace.ironsspellbooks.api.events.SpellPreCastEvent e) {
        try {
            String id = e.getSpellId();
            if (id == null) return;
            String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
            if (!(e.getEntity() instanceof ServerPlayer p)) return;
            if (!ManorBan.BANNED_SPELLS.contains(path)
                    && !(ManorFight.isImpossibleFor(p) && ManorImpossibleRules.BANNED_BUFF_SPELLS.contains(path))) return;
            if (!ManorFight.isPlayerFighting(p) && !ManorFight.isImpossibleFor(p)) return;
            e.setCanceled(true);
            ManorBan.scold(p, "试图施放 " + id);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Manor] 禁术拦截异常: {}", t.toString());
        }
    }
}
