/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 */
package net.farmingtales.ftboss.boss;

import java.util.List;
import java.util.Random;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class ManorBan {
    public static final List<String> BANNED_SPELLS = List.of("black_hole", "heartstop");
    private static final String[] LINES = new String[]{"\u96d5\u866b\u5c0f\u6280\u8fd8\u6562\u73ed\u95e8\u5f04\u65a7\uff1f", "\u5475\uff0c\u65c1\u95e8\u5de6\u9053"};
    private static final Random RNG = new Random();

    private ManorBan() {
    }

    public static void scold(ServerPlayer p, String what) {
        String line = LINES[RNG.nextInt(LINES.length)];
        p.m_5661_((Component)Component.m_237113_((String)("\u00a7c\u00a7l" + line)), true);
        p.m_213846_((Component)Component.m_237113_((String)("\u00a7c\u00a7l" + line)));
        p.m_9236_().m_6263_(null, p.m_20185_(), p.m_20186_(), p.m_20189_(), SoundEvents.f_11668_, SoundSource.HOSTILE, 0.8f, 0.6f);
        FTBossMod.LOGGER.info("[FTBoss][Manor] \u7981\u672f\u62e6\u622a: {} {} -> \u5df2\u53d6\u6d88", (Object)p.m_7755_().getString(), (Object)what);
    }
}

