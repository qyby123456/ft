/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.BengerPupEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class BleedLink {
    private static final long TTL = 100L;
    public static final float HEAL_RATIO = 0.5f;
    private static final Map<UUID, Rec> LINK = new ConcurrentHashMap<UUID, Rec>();

    private BleedLink() {
    }

    public static void link(Player player, LivingEntity source) {
        if (player == null || source == null) {
            return;
        }
        LivingEntity credit = source;
        if (source instanceof BengerPupEntity) {
            BengerPupEntity pup = (BengerPupEntity)source;
            BengerEntity boss = null;
            double best = 4096.0;
            for (BengerEntity b : pup.m_9236_().m_45976_(BengerEntity.class, pup.m_20191_().m_82400_(64.0))) {
                double d = b.m_20280_((Entity)pup);
                if (!(d < best)) continue;
                best = d;
                boss = b;
            }
            if (boss != null) {
                credit = boss;
            }
        }
        LINK.put(player.m_20148_(), new Rec(credit.m_20148_(), (long)player.f_19797_ + 100L));
    }

    public static LivingEntity creditFor(Player player) {
        if (player == null) {
            return null;
        }
        Rec rec = LINK.get(player.m_20148_());
        if (rec == null) {
            return null;
        }
        if ((long)player.f_19797_ > rec.expiry()) {
            LINK.remove(player.m_20148_());
            return null;
        }
        List e = player.m_9236_().m_45976_(LivingEntity.class, player.m_20191_().m_82400_(64.0));
        for (LivingEntity le : e) {
            if (!le.m_20148_().equals(rec.source()) || !le.m_6084_()) continue;
            return le;
        }
        return null;
    }

    public static void refund(Player player, float damage) {
        LivingEntity src = BleedLink.creditFor(player);
        if (src == null) {
            return;
        }
        float heal = damage * 0.5f;
        if (src.m_21223_() < src.m_21233_()) {
            src.m_5634_(heal);
        }
    }

    private record Rec(UUID source, long expiry) {
    }
}

