package net.farmingtales.ftboss.boss.ai;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 庄园 boss 战的**场地区块常加载**（2026-09-24 按用户要求「庄园 boss 战的场地区块保持加载」）。
 *
 * <p><b>为什么需要：</b>{@code ManorFight} 的一整套逻辑都假设战场上那位 Boss 一直在世界里 ——
 * 换人、血条、召回边界、{@code UNLOAD_GRACE_TICKS}(30 秒) 的"卸载算不算击败"判定全靠它。
 * 而区块卸载时实体是 {@code setRemoved(UNLOADED_TO_CHUNK)}、{@code level.getEntity(uuid)} 返回 null：
 *
 * <ul>
 *   <li>玩家死亡、跑远、换维度、切旁观 —— 战场区块一卸载，Boss 就"消失"；
 *       超过 30 秒宽限期直接 {@code endFight("Boss 卸载后无法恢复")}，整场白打。</li>
 *   <li>女皇那类会召唤的 Boss，召唤物跟着主子一起冻结； {@code pumpAll} 里甚至专门写了
 *       "查不到就 {@code sl.getChunk(...)} 拉一下"的补丁（每 100 tick 一次）——
 *       说明这个坑在实战里出现过。</li>
 * </ul>
 *
 * <p><b>做法：</b>开战时按战场锚点（{@code Fight.anchor}）给周围 {@link #RADIUS_CHUNKS} 个区块
 * 挂一张**专用票据**；战场结束、整场清空、或者服务器换实例时撤票。要点：
 *
 * <ol>
 *   <li>用**自己的 {@link TicketType}**（{@code ftboss_manor_arena}），不动原版 {@code FORCED} ——
 *       这样既不会和玩家的 {@code /forceload}、传送门票据互相顶掉，也**不会写进存档**的
 *       {@code forcedchunks}（{@code ServerLevel.setChunkForced} 会持久化，我们不要那个语义：
 *       战斗结束就该恢复自然卸载）。</li>
 *   <li>{@link #RADIUS_CHUNKS} = 5 区块(80 格)：庄园的召回边界是 **76 格**
 *       （见 {@code ManorFight} 的 v94 说明），取 5 刚好把整个战场连边界一起盖住。</li>
 *   <li>{@link #hold} 是**幂等**的：内部记住"当前握着哪些区块"，只补新增、只撤不再需要的 ——
 *       所以每次换人/搬迁重复调用也不会重复发票据（票据重复添加会叠加计数、撤不干净）。</li>
 * </ol>
 */
public final class ManorArenaChunks {
    /**
     * 专用票据类型。**不要**用 {@code TicketType.FORCED} —— 那个类型会和原版 {@code /forceload}
     * 共用同一种票据，撤票时可能把玩家的强制加载一起撤掉。
     */
    public static final TicketType<ChunkPos> ARENA_TICKET =
            TicketType.create("ftboss_manor_arena", Comparator.comparingLong(ChunkPos::toLong));

    /** 战场半径：庄园召回边界 76 格 → 5 区块(80 格)刚好盖住。 */
    public static final int RADIUS_CHUNKS = 5;

    /** 每个维度当前**我们**握着的区块（key = {@link ChunkPos#toLong()}）。 */
    private static final Map<ResourceKey<Level>, Set<Long>> HELD = new ConcurrentHashMap<>();

    private ManorArenaChunks() {}

    /**
     * 把战场锚点周围 {@link #RADIUS_CHUNKS} 区块设为常加载（幂等；搬迁后调用会自动放掉旧场地）。
     *
     * @param level  战场所在维度
     * @param anchor 战场锚点（{@code ManorFight.Fight.anchor}）
     */
    public static void hold(ServerLevel level, Vec3 anchor) {
        if (level == null || anchor == null) return;
        try {
            int cx = BlockPos.containing(anchor.x, anchor.y, anchor.z).getX() >> 4;
            int cz = BlockPos.containing(anchor.x, anchor.y, anchor.z).getZ() >> 4;
            boolean changed = false;
            for (int dx = -RADIUS_CHUNKS; dx <= RADIUS_CHUNKS; dx++) {
                for (int dz = -RADIUS_CHUNKS; dz <= RADIUS_CHUNKS; dz++) {
                    if (add(level, ChunkPos.asLong(cx + dx, cz + dz))) changed = true;
                }
            }
            /* 战场搬迁过：旧场地那批不再需要，顺手放掉（同一维度只允许一场庄园战，
               见 ManorFight.start/adopt 里都会先 clear(dim)，所以这里不用担心误放别的战场）。 */
            if (dropOutside(level, anchor)) changed = true;
            if (changed) {
                FTBossMod.LOGGER.info("[FTBoss][Manor] 场地区块常加载: {} 中心=({},{}) 半径={} 区块 已握={}",
                        level.dimension().location(), cx, cz, RADIUS_CHUNKS, heldCount(level.dimension()));
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Manor] 场地区块常加载失败(已忽略, 不崩服): {}", t.toString());
        }
    }

    /** 放开本维度我们握着的全部区块（战斗结束/整场清空时调用）。 */
    public static void release(ServerLevel level) {
        if (level == null) return;
        try {
            Set<Long> held = HELD.remove(level.dimension());
            if (held == null || held.isEmpty()) return;
            for (long key : held) {
                ChunkPos pos = new ChunkPos(key);
                level.getChunkSource().removeRegionTicket(ARENA_TICKET, pos, RADIUS_CHUNKS, pos);
            }
            FTBossMod.LOGGER.info("[FTBoss][Manor] 场地区块常加载已解除: {} 释放 {} 区块",
                    level.dimension().location(), held.size());
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Manor] 解除场地区块常加载失败(已忽略): {}", t.toString());
        }
    }

    /**
     * 只清记账、不撤票 —— 只在**换服务器实例**时用（{@code ManorFight.initialize}）：
     * 那时旧的 {@code ServerLevel} 已经连同票据一起没了，再去撤票反而会碰到失效对象。
     */
    public static void reset() {
        HELD.clear();
    }

    /** 本维度我们握着多少区块（诊断/回归用）。 */
    public static int heldCount(ResourceKey<Level> dim) {
        Set<Long> held = HELD.get(dim);
        return held == null ? 0 : held.size();
    }

    private static boolean add(ServerLevel level, long key) {
        Set<Long> held = HELD.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet());
        if (!held.add(key)) return false;
        ChunkPos pos = new ChunkPos(key);
        level.getChunkSource().addRegionTicket(ARENA_TICKET, pos, RADIUS_CHUNKS, pos);
        return true;
    }

    /** 把新场地之外的区块放掉；有实际释放就返回 true（{@link #hold} 内部调用）。 */
    public static boolean dropOutside(ServerLevel level, Vec3 anchor) {
        if (level == null || anchor == null) return false;
        Set<Long> held = HELD.get(level.dimension());
        if (held == null) return false;
        int cx = BlockPos.containing(anchor.x, anchor.y, anchor.z).getX() >> 4;
        int cz = BlockPos.containing(anchor.x, anchor.y, anchor.z).getZ() >> 4;
        boolean dropped = false;
        try {
            for (var it = held.iterator(); it.hasNext(); ) {
                long key = it.next();
                ChunkPos pos = new ChunkPos(key);
                if (Math.abs(pos.x - cx) <= RADIUS_CHUNKS && Math.abs(pos.z - cz) <= RADIUS_CHUNKS) continue;
                level.getChunkSource().removeRegionTicket(ARENA_TICKET, pos, RADIUS_CHUNKS, pos);
                it.remove();
                dropped = true;
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Manor] 收缩场地区块常加载失败(已忽略): {}", t.toString());
        }
        return dropped;
    }
}
