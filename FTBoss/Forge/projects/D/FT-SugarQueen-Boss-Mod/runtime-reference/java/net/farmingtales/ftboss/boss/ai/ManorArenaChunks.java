/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.TicketType
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.world.ForgeChunkManager
 */
package net.farmingtales.ftboss.boss.ai;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.ManorDamageRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;

public final class ManorArenaChunks {
    public static final TicketType<ChunkPos> ARENA_TICKET = TicketType.m_9462_((String)"ftboss_manor_arena", Comparator.comparingLong(ChunkPos::m_45588_));
    public static final int RADIUS_CHUNKS = 5;
    private static final Map<ServerLevel, Set<Long>> HELD = new HashMap<ServerLevel, Set<Long>>();
    private static final Map<ServerLevel, Set<Long>> CENTERS = new HashMap<ServerLevel, Set<Long>>();
    private static final Map<ServerLevel, Set<Long>> PENDING = new HashMap<ServerLevel, Set<Long>>();

    private ManorArenaChunks() {
    }

    private static UUID owner(ServerLevel serverLevel) {
        return UUID.nameUUIDFromBytes(("ftboss:manor-arena:" + String.valueOf(serverLevel.m_46472_().m_135782_())).getBytes(StandardCharsets.UTF_8));
    }

    public static void register() {
        ForgeChunkManager.setForcedChunkLoadingCallback((String)"ftboss", (serverLevel, ticketHelper) -> {
            UUID uUID = ManorArenaChunks.owner(serverLevel);
            if (ticketHelper.getEntityTickets().containsKey(uUID)) {
                ticketHelper.removeAllTickets(uUID);
            }
        });
    }

    private static long center(Vec3 vec3) {
        BlockPos blockPos = BlockPos.m_274561_((double)vec3.f_82479_, (double)vec3.f_82480_, (double)vec3.f_82481_);
        return ManorDamageRules.chunkKey(blockPos.m_123341_() >> 4, blockPos.m_123343_() >> 4);
    }

    public static void hold(ServerLevel serverLevel, Vec3 vec3) {
        if (serverLevel == null || vec3 == null) {
            return;
        }
        HashSet<Long> hashSet = new HashSet<Long>(CENTERS.getOrDefault(serverLevel, Set.of()));
        hashSet.add(ManorArenaChunks.center(vec3));
        ManorArenaChunks.sync(serverLevel, hashSet);
    }

    public static void beginFrame() {
        PENDING.clear();
    }

    public static void track(ServerLevel serverLevel2, Vec3 vec3) {
        if (serverLevel2 != null && vec3 != null) {
            PENDING.computeIfAbsent(serverLevel2, serverLevel -> new HashSet()).add(ManorArenaChunks.center(vec3));
        }
    }

    public static void flush() {
        for (Map.Entry<ServerLevel, Set<Long>> entry : PENDING.entrySet()) {
            ManorArenaChunks.sync(entry.getKey(), entry.getValue());
        }
        PENDING.clear();
    }

    private static void sync(ServerLevel serverLevel2, Set<Long> set) {
        ChunkPos chunkPos;
        if (set.equals(CENTERS.get(serverLevel2))) {
            return;
        }
        Set<Long> set2 = ManorDamageRules.arenaChunks(set);
        Set set3 = HELD.computeIfAbsent(serverLevel2, serverLevel -> new HashSet());
        UUID uUID = ManorArenaChunks.owner(serverLevel2);
        for (long l : set2) {
            if (set3.contains(l)) continue;
            chunkPos = new ChunkPos(l);
            ForgeChunkManager.forceChunk((ServerLevel)serverLevel2, (String)"ftboss", (UUID)uUID, (int)chunkPos.f_45578_, (int)chunkPos.f_45579_, (boolean)true, (boolean)true);
            set3.add(l);
        }
        for (long l : new HashSet(set3)) {
            if (set2.contains(l)) continue;
            chunkPos = new ChunkPos(l);
            ForgeChunkManager.forceChunk((ServerLevel)serverLevel2, (String)"ftboss", (UUID)uUID, (int)chunkPos.f_45578_, (int)chunkPos.f_45579_, (boolean)false, (boolean)true);
            set3.remove(l);
        }
        CENTERS.put(serverLevel2, new HashSet<Long>(set));
        FTBossMod.LOGGER.info("[FTBoss][Manor] Forced arena ticking: {} cores={} chunks={}", new Object[]{serverLevel2.m_46472_().m_135782_(), set.size(), set3.size()});
    }

    public static void release(ServerLevel serverLevel) {
        if (serverLevel == null) {
            return;
        }
        Set<Long> set = HELD.get(serverLevel);
        if (set != null) {
            UUID uUID = ManorArenaChunks.owner(serverLevel);
            for (long l : new HashSet<Long>(set)) {
                ChunkPos chunkPos = new ChunkPos(l);
                ForgeChunkManager.forceChunk((ServerLevel)serverLevel, (String)"ftboss", (UUID)uUID, (int)chunkPos.f_45578_, (int)chunkPos.f_45579_, (boolean)false, (boolean)true);
                set.remove(l);
            }
        }
        HELD.remove(serverLevel);
        CENTERS.remove(serverLevel);
        PENDING.remove(serverLevel);
    }

    public static void reset() {
        for (ServerLevel serverLevel : new HashSet<ServerLevel>(HELD.keySet())) {
            ManorArenaChunks.release(serverLevel);
        }
        PENDING.clear();
    }

    public static int heldCount(ResourceKey<Level> resourceKey) {
        return HELD.entrySet().stream().filter(entry -> ((ServerLevel)entry.getKey()).m_46472_().equals((Object)resourceKey)).mapToInt(entry -> ((Set)entry.getValue()).size()).sum();
    }

    public static boolean dropOutside(ServerLevel serverLevel, Vec3 vec3) {
        if (serverLevel == null || vec3 == null || CENTERS.getOrDefault(serverLevel, Set.of()).size() > 1) {
            return false;
        }
        int n = ManorArenaChunks.heldCount((ResourceKey<Level>)serverLevel.m_46472_());
        ManorArenaChunks.sync(serverLevel, Set.of(Long.valueOf(ManorArenaChunks.center(vec3))));
        return ManorArenaChunks.heldCount((ResourceKey<Level>)serverLevel.m_46472_()) < n;
    }
}

