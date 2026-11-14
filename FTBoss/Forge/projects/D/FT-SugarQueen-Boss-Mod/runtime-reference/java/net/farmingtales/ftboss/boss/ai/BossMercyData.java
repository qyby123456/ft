/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.saveddata.SavedData
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class BossMercyData
extends SavedData {
    private static final String DATA_NAME = "ftboss_mercy";
    private static final String K_PLAYERS = "players";
    private static final String K_UUID = "uuid";
    private static final String K_STREAKS = "streaks";
    public static final long STREAK_TIMEOUT = 12000L;
    private final Map<UUID, Map<String, long[]>> streaks = new HashMap<UUID, Map<String, long[]>>();

    public static BossMercyData get(ServerLevel anyLevel) {
        return (BossMercyData)anyLevel.m_7654_().m_129783_().m_8895_().m_164861_(BossMercyData::load, BossMercyData::new, DATA_NAME);
    }

    public int streak(UUID player, String bossType, long now) {
        Map<String, long[]> m = this.streaks.get(player);
        if (m == null) {
            return 0;
        }
        long[] v = m.get(bossType);
        if (v == null) {
            return 0;
        }
        if (now - v[1] > 12000L) {
            return 0;
        }
        return (int)v[0];
    }

    public void setStreak(UUID player, String bossType, int count, long now) {
        if (count <= 0) {
            Map<String, long[]> m = this.streaks.get(player);
            if (m != null) {
                m.remove(bossType);
                if (m.isEmpty()) {
                    this.streaks.remove(player);
                }
            }
        } else {
            this.streaks.computeIfAbsent(player, k -> new HashMap()).put(bossType, new long[]{count, now});
        }
        this.m_77762_();
    }

    public void clearAll(UUID player) {
        if (this.streaks.remove(player) != null) {
            this.m_77762_();
        }
    }

    public static BossMercyData load(CompoundTag tag) {
        BossMercyData d = new BossMercyData();
        ListTag list = tag.m_128437_(K_PLAYERS, 10);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag pt = list.m_128728_(i);
            try {
                UUID id = UUID.fromString(pt.m_128461_(K_UUID));
                CompoundTag st = pt.m_128469_(K_STREAKS);
                HashMap<String, long[]> m = new HashMap<String, long[]>();
                for (String key : st.m_128431_()) {
                    long tick;
                    long count;
                    int[] arr = st.m_128465_(key);
                    if (arr.length >= 2) {
                        count = arr[0];
                        tick = arr[1];
                    } else {
                        count = st.m_128451_(key);
                        tick = 0L;
                    }
                    if (count <= 0L) continue;
                    m.put(key, new long[]{count, tick});
                }
                if (m.isEmpty()) continue;
                d.streaks.put(id, m);
                continue;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return d;
    }

    public CompoundTag m_7176_(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Map<String, long[]>> e : this.streaks.entrySet()) {
            if (e.getValue().isEmpty()) continue;
            CompoundTag pt = new CompoundTag();
            pt.m_128359_(K_UUID, e.getKey().toString());
            CompoundTag st = new CompoundTag();
            for (Map.Entry<String, long[]> s : e.getValue().entrySet()) {
                st.m_128385_(s.getKey(), new int[]{(int)s.getValue()[0], (int)s.getValue()[1]});
            }
            pt.m_128365_(K_STREAKS, (Tag)st);
            list.add((Object)pt);
        }
        tag.m_128365_(K_PLAYERS, (Tag)list);
        return tag;
    }
}

