package net.farmingtales.ftboss.boss.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 「连死怜悯」的计数存档。
 *
 * <p><b>为什么不用玩家 persistentData</b>: 玩家死亡重生时服务端会**新建一个 ServerPlayer 实例**,
 * 而 ForgeData(persistentData)挂在实体上 → 计数每次死亡都被清空, 表现就是"死了一次之后一直停在 1/5"。
 * 这里存到**世界存档**({@link SavedData}, 落在 overworld 的 data 目录), 死亡重生/退出重进/重启服务器都保留。
 *
 * <p><b>2026-09-11 二次修</b>: 计数原来**永不过期** —— 玩家打某 Boss 死了 3 次就把 3/5 留在存档里, 隔很久再被它打死一次
 * 就凑满 5 次把 Boss 顶掉(日志实测: 故障机器人离场 1 秒后, 屠夫的旧计数也到 5 一起离场)。
 * 现在每个计数**带时间戳**, 超过 {@link #STREAK_TIMEOUT} 没更新就视为过期, 从 0 重新数。
 *
 * <p>结构: {@code { players: [ { uuid, streaks: { "ftboss:rat_boss": [次数, 游戏刻] } } ] }}(旧存档只有次数, 会被当成过期)
 */
public class BossMercyData extends SavedData {

    private static final String DATA_NAME = "ftboss_mercy";
    private static final String K_PLAYERS = "players";
    private static final String K_UUID = "uuid";
    private static final String K_STREAKS = "streaks";

    /** 连死计数有效期(tick): 10 分钟没再死就过期重数 */
    public static final long STREAK_TIMEOUT = 20L * 60L * 10L;

    /** 玩家 UUID -> (Boss 类型 id -> [次数, 最后一次死亡的游戏刻]) */
    private final Map<UUID, Map<String, long[]>> streaks = new HashMap<>();

    public static BossMercyData get(ServerLevel anyLevel) {
        return anyLevel.getServer().overworld().getDataStorage()
                .computeIfAbsent(BossMercyData::load, BossMercyData::new, DATA_NAME);
    }

    /** 当前有效次数(过期则返回 0) */
    public int streak(UUID player, String bossType, long now) {
        Map<String, long[]> m = streaks.get(player);
        if (m == null) return 0;
        long[] v = m.get(bossType);
        if (v == null) return 0;
        if (now - v[1] > STREAK_TIMEOUT) return 0;      // 过期: 视为从 0 开始
        return (int) v[0];
    }

    /** 记录一次死亡(次数 + 当前时间) */
    public void setStreak(UUID player, String bossType, int count, long now) {
        if (count <= 0) {
            Map<String, long[]> m = streaks.get(player);
            if (m != null) {
                m.remove(bossType);
                if (m.isEmpty()) streaks.remove(player);
            }
        } else {
            streaks.computeIfAbsent(player, k -> new HashMap<>()).put(bossType, new long[]{count, now});
        }
        setDirty();
    }

    public void clearAll(UUID player) {
        if (streaks.remove(player) != null) setDirty();
    }

    // ================= 序列化 =================

    public static BossMercyData load(CompoundTag tag) {
        BossMercyData d = new BossMercyData();
        ListTag list = tag.getList(K_PLAYERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag pt = list.getCompound(i);
            try {
                UUID id = UUID.fromString(pt.getString(K_UUID));
                CompoundTag st = pt.getCompound(K_STREAKS);
                Map<String, long[]> m = new HashMap<>();
                for (String key : st.getAllKeys()) {
                    int[] arr = st.getIntArray(key);        // 新格式: [次数, 时间]
                    long count, tick;
                    if (arr.length >= 2) {
                        count = arr[0];
                        tick = arr[1];
                    } else {
                        count = st.getInt(key);             // 兼容旧存档: 只有次数, 没有时间 -> tick=0 => 视为过期
                        tick = 0L;
                    }
                    if (count > 0) m.put(key, new long[]{count, tick});
                }
                if (!m.isEmpty()) d.streaks.put(id, m);
            } catch (Throwable ignored) {
            }
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Map<String, long[]>> e : streaks.entrySet()) {
            if (e.getValue().isEmpty()) continue;
            CompoundTag pt = new CompoundTag();
            pt.putString(K_UUID, e.getKey().toString());
            CompoundTag st = new CompoundTag();
            for (Map.Entry<String, long[]> s : e.getValue().entrySet()) {
                st.putIntArray(s.getKey(), new int[]{(int) s.getValue()[0], (int) s.getValue()[1]});
            }
            pt.put(K_STREAKS, st);
            list.add(pt);
        }
        tag.put(K_PLAYERS, list);
        return tag;
    }
}
