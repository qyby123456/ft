/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.events.SpellPreCastEvent
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorBan;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorImpossibleRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorSpellBan {
    private ManorSpellBan() {
    }

    @SubscribeEvent
    public static void onPreCast(SpellPreCastEvent e) {
        try {
            String id = e.getSpellId();
            if (id == null) {
                return;
            }
            String path = id.contains(":") ? id.substring(id.indexOf(58) + 1) : id;
            Player player = e.getEntity();
            if (!(player instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer p = (ServerPlayer)player;
            if (!(ManorBan.BANNED_SPELLS.contains(path) || ManorFight.isImpossibleFor((Player)p) && ManorImpossibleRules.BANNED_BUFF_SPELLS.contains(path))) {
                return;
            }
            if (!ManorFight.isPlayerFighting((Player)p) && !ManorFight.isImpossibleFor((Player)p)) {
                return;
            }
            e.setCanceled(true);
            ManorBan.scold(p, "\u8bd5\u56fe\u65bd\u653e " + id);
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Manor] \u7981\u672f\u62e6\u622a\u5f02\u5e38: {}", (Object)t.toString());
        }
    }
}

