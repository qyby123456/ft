package net.farmingtales.ftboss.boss;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局 Boss 血条登记表。
 * 统一负责"孤儿血条"治理, 覆盖所有容易漏清理的路径:
 *  - 玩家死亡重生(旧 ServerPlayer 实例残留) / 下线
 *  - 实体区块卸载、换维度、被 remove
 *  - 实体死亡但清理分支没跑到
 * 客户端只有在收到 REMOVE 包时才会销毁血条, 所以这里保证任何情况下都发 REMOVE。
 */
public final class BossBars {

    private static final Map<UUID, ServerBossEvent> BARS = new ConcurrentHashMap<>();

    private BossBars() {}

    /** 实体创建血条时登记(鼠群/茶树/酒桶王/屠夫/故障机器人/女皇本体…)。
     *  <p>v112 修复: **客户端一律不登记**。以前这个方法是"谁调都收", 而 5 只 Boss 的血条是在
     *  {@code Mob} 构造器里建的 —— {@code EntityType.create()} 在客户端收生成包时同样会跑构造器,
     *  于是单机(集成服共用静态表)下客户端对象会按同一个 UUID **覆盖掉服务端那一条**,
     *  之后 {@link #untrack} 的两参 {@code remove(uuid, bar)} 永远匹配不上 →
     *  "血条卡在天上"的兜底清理实质失效。 */
    public static void track(Entity owner, ServerBossEvent bar) {
        if (owner == null || bar == null) return;
        if (owner.level().isClientSide) return;          // v112: 客户端不持有血条登记表
        BARS.put(owner.getUUID(), bar);
    }

    /** 隐藏 + 从所有玩家摘除(会发 REMOVE 包) */
    public static void hide(ServerBossEvent bar) {
        bar.setVisible(false);
        bar.removeAllPlayers();
    }

    /** 注销某个实体的血条并立即清理 */
    public static void untrack(Entity owner, ServerBossEvent bar) {
        if (owner == null) return;
        if (owner.level().isClientSide) return;          // v112: 客户端没登记过, 也别去 hide
        BARS.remove(owner.getUUID(), bar);
        hide(bar);
    }

    /** 玩家重生(旧实例) / 下线: 从所有血条中摘除, 避免向失效实例发包导致 REMOVE 丢失 */
    public static void forgetPlayer(ServerPlayer p) {
        if (p == null) return;
        for (ServerBossEvent bar : BARS.values()) {
            try {
                bar.removePlayer(p);
            } catch (Throwable ignored) {
            }
        }
    }

    /** 遍历当前登记在册的 Boss 实体(供"按场上人数缩放"等功能使用) */
    public static void forEach(MinecraftServer server, java.util.function.Consumer<LivingEntity> fn) {
        if (server == null) return;
        for (UUID id : BARS.keySet()) {
            for (ServerLevel sl : server.getAllLevels()) {
                Entity e = sl.getEntity(id);
                if (e instanceof LivingEntity le) { fn.accept(le); break; }
            }
        }
    }

    /** 周期兜底清扫: 主人已不存在(卸载/移除)或已死亡 -> 隐藏 + REMOVE + 注销 */
    public static void sweep(MinecraftServer server) {
        if (server == null || BARS.isEmpty()) return;
        var it = BARS.entrySet().iterator();
        while (it.hasNext()) {
            var en = it.next();
            boolean alive = false;
            for (ServerLevel sl : server.getAllLevels()) {
                Entity e = sl.getEntity(en.getKey());
                if (e instanceof LivingEntity le && !le.isRemoved() && le.isAlive()) {
                    alive = true;
                    break;
                }
            }
            if (!alive) {
                hide(en.getValue());
                it.remove();
            }
        }
    }
}
