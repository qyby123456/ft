package net.farmingtales.ftboss.boss.ai;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.ManorDamageRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;

@EventBusSubscriber(modid = "ftboss", bus = Bus.MOD)
public final class ManorArenaChunks {
    public static final TicketType<ChunkPos> ARENA_TICKET = TicketType.create("ftboss_manor_arena", Comparator.comparingLong(ChunkPos::toLong));
    public static final int RADIUS_CHUNKS = 5;
    private static final Map<ServerLevel, Set<Long>> HELD = new HashMap<>();
    private static final Map<ServerLevel, Set<Long>> CENTERS = new HashMap<>();
    private static final Map<ServerLevel, Set<Long>> PENDING = new HashMap<>();

    private ManorArenaChunks() {
    }

    private static UUID owner(ServerLevel var0) {
        return UUID.nameUUIDFromBytes(("ftboss:manor-arena:" + var0.dimension().location()).getBytes(StandardCharsets.UTF_8));
    }

    private static final TicketController CONTROLLER = new TicketController(ResourceLocation.fromNamespaceAndPath("ftboss", "manor_arena"), (var0, var1) -> {
            UUID var2 = owner(var0);
            if (var1.getEntityTickets().containsKey(var2)) {
                var1.removeAllTickets(var2);
            }
        });

    @SubscribeEvent
    public static void register(RegisterTicketControllersEvent event) {
        event.register(CONTROLLER);
    }

    private static long center(Vec3 var0) {
        BlockPos var1 = BlockPos.containing(var0.x, var0.y, var0.z);
        return ManorDamageRules.chunkKey(var1.getX() >> 4, var1.getZ() >> 4);
    }

    public static void hold(ServerLevel var0, Vec3 var1) {
        if (var0 != null && var1 != null) {
            HashSet<Long> var2 = new HashSet<>(CENTERS.getOrDefault(var0, Set.of()));
            var2.add(center(var1));
            sync(var0, var2);
        }
    }

    public static void beginFrame() {
        PENDING.clear();
    }

    public static void track(ServerLevel var0, Vec3 var1) {
        if (var0 != null && var1 != null) {
            PENDING.computeIfAbsent(var0, var0x -> new HashSet<>()).add(center(var1));
        }
    }

    public static void flush() {
        for (Entry<ServerLevel, Set<Long>> var1 : PENDING.entrySet()) {
            sync((ServerLevel)var1.getKey(), (Set<Long>)var1.getValue());
        }

        PENDING.clear();
    }

    private static void sync(ServerLevel var0, Set<Long> var1) {
        if (!var1.equals(CENTERS.get(var0))) {
            Set<Long> var2 = ManorDamageRules.arenaChunks(var1);
            Set<Long> var3 = HELD.computeIfAbsent(var0, var0x -> new HashSet<>());
            UUID var4 = owner(var0);

            for (long var6 : var2) {
                if (!var3.contains(var6)) {
                    ChunkPos var8 = new ChunkPos(var6);
                    CONTROLLER.forceChunk(var0, var4, var8.x, var8.z, true, true);
                    var3.add(var6);
                }
            }

            for (long var10 : new HashSet<>(var3)) {
                if (!var2.contains(var10)) {
                    ChunkPos var11 = new ChunkPos(var10);
                    CONTROLLER.forceChunk(var0, var4, var11.x, var11.z, false, true);
                    var3.remove(var10);
                }
            }

            CENTERS.put(var0, new HashSet<>(var1));
            FTBossMod.LOGGER.info("[FTBoss][Manor] Forced arena ticking: {} cores={} chunks={}", var0.dimension().location(), var1.size(), var3.size());
        }
    }

    public static void release(ServerLevel var0) {
        if (var0 != null) {
            Set<Long> var1 = HELD.get(var0);
            if (var1 != null) {
                UUID var2 = owner(var0);

                for (long var4 : new HashSet<>(var1)) {
                    ChunkPos var6 = new ChunkPos(var4);
                    CONTROLLER.forceChunk(var0, var2, var6.x, var6.z, false, true);
                    var1.remove(var4);
                }
            }

            HELD.remove(var0);
            CENTERS.remove(var0);
            PENDING.remove(var0);
        }
    }

    public static void reset() {
        for (ServerLevel var1 : new HashSet<>(HELD.keySet())) {
            release(var1);
        }

        PENDING.clear();
    }

    public static int heldCount(ResourceKey<Level> var0) {
        return HELD.entrySet().stream().filter(var1 -> var1.getKey().dimension().equals(var0)).mapToInt(var0x -> var0x.getValue().size()).sum();
    }

    public static boolean dropOutside(ServerLevel var0, Vec3 var1) {
        if (var0 != null && var1 != null && CENTERS.getOrDefault(var0, Set.of()).size() <= 1) {
            int var2 = heldCount(var0.dimension());
            sync(var0, Set.of(center(var1)));
            return heldCount(var0.dimension()) < var2;
        } else {
            return false;
        }
    }
}
