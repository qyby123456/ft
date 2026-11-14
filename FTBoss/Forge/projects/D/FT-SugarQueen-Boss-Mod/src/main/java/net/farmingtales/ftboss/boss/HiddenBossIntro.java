package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ManorIntroPacket;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.ManorIntroTimeline;
import net.farmingtales.ftboss.util.PresentationClock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class HiddenBossIntro {
    public static final double DISTANCE = 15.0;
    public static final int DEFAULT_VARIANT = 9;
    public static final int INTRO_MIN_TICKS = ManorIntroTimeline.DURATION_TICKS;
    private static final double VIEW_RADIUS_SQUARED = 96.0 * 96.0;
    private static final int GROUND_SEARCH_UP = 4;
    private static final int GROUND_SEARCH_DOWN = 32;
    private static final List<Act> ACTIVE = new ArrayList<>();
    private static int lastPump = -1;

    private static final class Act {
        final ResourceKey<Level> dimension;
        final UUID id;
        final long startMillis = PresentationClock.millis();
        final ServerBossEvent bar;
        final String[] lines;
        final Set<UUID> viewers = new HashSet<>();
        final Set<UUID> buffPrepared = new HashSet<>();
        Vec3 position;
        long nextSyncMillis;
        int sent;

        Act(ServerLevel level, EasterBossEntity boss, ServerBossEvent bar, String[] lines) {
            dimension = level.dimension();
            id = boss.getUUID();
            position = boss.position();
            this.bar = bar;
            this.lines = lines == null ? new String[0] : lines.clone();
        }
    }

    private HiddenBossIntro() {}

    public static void start(ServerLevel level, Vec3 anchor, Vec3 from, String... lines) {
        start(level, anchor, from, DEFAULT_VARIANT, lines);
    }

    public static void start(ServerLevel level, Vec3 anchor, Vec3 from, int variant, String... lines) {
        start(level, anchor, from, variant, null, lines);
    }

    public static void start(ServerLevel level, Vec3 anchor, Vec3 from, UUID summonerId, String... lines) {
        start(level, anchor, from, DEFAULT_VARIANT, summonerId, lines);
    }

    public static void start(ServerLevel level, Vec3 anchor, Vec3 from, int variant, UUID summonerId, String... lines) {
        start(level, anchor, from, variant, summonerId, ManorDifficulty.NORMAL, lines);
    }

    public static void start(ServerLevel level, Vec3 anchor, Vec3 from, int variant, UUID summonerId,
                             ManorDifficulty difficulty, String... lines) {
        if (level == null || isActive() || !SummonGate.isAllowedDim(level) || ManorFight.hasActive(level)) return;
        Vec3 direction = from.subtract(anchor);
        if (direction.lengthSqr() < 1.0E-4) direction = new Vec3(0, 0, 1);
        direction = direction.normalize();
        double posX = anchor.x + direction.x * DISTANCE;
        double posZ = anchor.z + direction.z * DISTANCE;
        EasterBossEntity boss = FTEntities.EASTER_BOSS.get().create(level);
        if (boss == null) {
            BossMusic.stopManorCue("入场实体创建失败");
            return;
        }
        boss.setVariant(variant);
        boss.setChallengeDifficulty(difficulty);
        boss.setManorSummonerId(summonerId);
        Vec3 grounded = findGround(level, boss, new Vec3(posX, from.y, posZ));
        if (grounded == null) grounded = findGround(level, boss, anchor);
        if (grounded == null) {
            BossMusic.stopManorCue("附近没有可站立的演出地面");
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(anchor) <= VIEW_RADIUS_SQUARED) {
                    player.sendSystemMessage(Component.literal("§c【甜苹果庄园】附近没有可站立的演出地面，请在平坦地面重新触发。"));
                }
            }
            return;
        }
        posX = grounded.x;
        posZ = grounded.z;
        double posY = grounded.y;
        float yaw = (float) Math.toDegrees(Math.atan2(anchor.z - posZ, anchor.x - posX)) - 90.0F;
        boss.moveTo(posX, posY, posZ, yaw, 0.0F);
        boss.setInvulnerable(true);
        boss.setNoAi(true);
        boss.setManorIntroHidden(variant == EasterBossEntity.SPARROW_VARIANT);
        boss.prepareManorEntryBuffs();
        if (!level.addFreshEntity(boss)) {
            BossMusic.stopManorCue("入场实体生成被取消");
            return;
        }
        ServerBossEvent bar = new ServerBossEvent(Component.literal("§c§l甜苹果庄园"
                + boss.getChallengeDifficulty().badge()),
                BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        bar.setProgress(0.0F);
        boss.getBossBar().setVisible(false);
        Act act = new Act(level, boss, bar, lines);
        ACTIVE.add(act);
        BossMusic.beginManorPhase(level, anchor);
        BossMusic.playManorCue(level, boss.position());
        syncViewers(level, act, 0L);
        level.playSound(null, posX, posY, posZ, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.0F, 0.7F);
        FTBossMod.LOGGER.info("[FTBoss][ManorIntro] start id={} duration={}ms; client mesh, no particles/crystal entities",
                act.id, ManorIntroTimeline.DURATION_MILLIS);
    }

    private static Vec3 findGround(ServerLevel level, EasterBossEntity boss, Vec3 preferred) {
        BlockPos column = BlockPos.containing(preferred);
        int highest = Math.min(level.getMaxBuildHeight() - 1, column.getY() + GROUND_SEARCH_UP);
        int lowest = Math.max(level.getMinBuildHeight(), column.getY() - GROUND_SEARCH_DOWN);
        Vec3 closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (int blockY = highest; blockY >= lowest; blockY--) {
            BlockPos floor = new BlockPos(column.getX(), blockY, column.getZ());
            if (!level.hasChunkAt(floor)) continue;
            var state = level.getBlockState(floor);
            if (!state.getFluidState().isEmpty()) continue;
            for (AABB surface : state.getCollisionShape(level, floor).toAabbs()) {
                double localX = preferred.x - floor.getX();
                double localZ = preferred.z - floor.getZ();
                if (localX < surface.minX || localX > surface.maxX || localZ < surface.minZ || localZ > surface.maxZ) continue;
                double feetY = blockY + surface.maxY;
                double distance = Math.abs(feetY - preferred.y);
                if (distance >= closestDistance) continue;
                AABB body = boss.getBoundingBox().move(preferred.x - boss.getX(), feetY - boss.getY(), preferred.z - boss.getZ());
                if (!level.noCollision(boss, body) || level.containsAnyLiquid(body)) continue;
                closest = new Vec3(preferred.x, feetY, preferred.z);
                closestDistance = distance;
            }
        }
        return closest;
    }

    private static void send(ServerPlayer player, Act act, long elapsedMillis, boolean active) {
        FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ManorIntroPacket(act.id, act.dimension.location(), act.position, elapsedMillis, active));
    }

    private static void syncViewers(ServerLevel level, Act act, long elapsedMillis) {
        boolean sync = elapsedMillis >= act.nextSyncMillis;
        Set<UUID> nearby = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(act.position) > VIEW_RADIUS_SQUARED) continue;
            nearby.add(player.getUUID());
            net.farmingtales.ftboss.boss.ai.ManorIntroPlayerBuffs.begin(player, act.buffPrepared);
            act.bar.addPlayer(player);
            if (act.viewers.add(player.getUUID()) || sync) send(player, act, elapsedMillis, true);
        }
        for (UUID viewer : new HashSet<>(act.viewers)) {
            if (nearby.contains(viewer)) continue;
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(viewer);
            if (player != null) {
                act.bar.removePlayer(player);
                send(player, act, elapsedMillis, false);
            }
            act.viewers.remove(viewer);
        }
        for (ServerPlayer player : new ArrayList<>(act.bar.getPlayers())) {
            if (!nearby.contains(player.getUUID())) act.bar.removePlayer(player);
        }
        if (sync) act.nextSyncMillis = elapsedMillis + ManorIntroTimeline.SYNC_MILLIS;
    }

    private static void clear(MinecraftServer server, Act act, long elapsedMillis) {
        ServerLevel level = server.getLevel(act.dimension);
        if (level != null && level.getEntity(act.id) instanceof EasterBossEntity boss) {
            boss.setManorIntroHidden(false);
        }
        act.bar.removeAllPlayers();
        for (UUID viewer : act.viewers) {
            ServerPlayer player = server.getPlayerList().getPlayer(viewer);
            if (player != null) send(player, act, elapsedMillis, false);
        }
        act.viewers.clear();
    }

    public static void pumpAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || ACTIVE.isEmpty() || lastPump == server.getTickCount()) return;
        lastPump = server.getTickCount();
        for (Act act : new ArrayList<>(ACTIVE)) {
            long elapsedMillis = Math.max(0L, PresentationClock.millis() - act.startMillis);
            ServerLevel level = server.getLevel(act.dimension);
            if (level == null || !(level.getEntity(act.id) instanceof EasterBossEntity boss) || !boss.isAlive()) {
                clear(server, act, elapsedMillis);
                ACTIVE.remove(act);
                BossMusic.stopManorCue("入场实体已移除");
                continue;
            }
            act.position = boss.position();
            act.bar.setProgress(ManorIntroTimeline.progress(elapsedMillis));
            if (elapsedMillis >= act.nextSyncMillis) syncViewers(level, act, elapsedMillis);
            while (act.sent < act.lines.length
                    && elapsedMillis >= ManorIntroTimeline.dialogueMillis(act.sent, act.lines.length)) {
                String text = act.lines[act.sent++];
                if (text == null || text.isEmpty()) continue;
                for (ServerPlayer player : level.players()) {
                    if (act.viewers.contains(player.getUUID())) player.sendSystemMessage(Component.literal(text));
                }
            }
            if (elapsedMillis < ManorIntroTimeline.DURATION_MILLIS) continue;
            clear(server, act, elapsedMillis);
            ACTIVE.remove(act);
            boss.prepareManorEntryBuffs();
            boss.setInvulnerable(false);
            boss.setNoAi(false);
            if (boss.getVariant() == EasterBossEntity.SPARROW_VARIANT) {
                EasterBossEntity.supportSparrow(level, boss);
                int first = EasterBossEntity.MANOR_VARIANTS[boss.getRandom().nextInt(EasterBossEntity.MANOR_COUNT)];
                ManorFight.start(level, boss.position(), first, boss.getManorSummonerId(), boss.getChallengeDifficulty());
                for (ServerPlayer player : level.players()) {
                    if (player.distanceToSqr(boss) <= VIEW_RADIUS_SQUARED) {
                        player.sendSystemMessage(Component.literal("§e【麻雀】§7留在场地支援！§c【甜苹果庄园】§7第一位上场：§c"
                                + EasterBossEntity.NAMES[first]));
                    }
                }
            } else {
                boss.getBossBar().setVisible(false);
                ManorFight.adopt(level, boss);
            }
            level.playSound(null, boss.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.0F, 0.8F);
            BossMusic.startManorBattle(level, boss.position());
            FTBossMod.LOGGER.info("[FTBoss][ManorIntro] complete id={} elapsed={}ms -> battle", act.id, elapsedMillis);
        }
    }

    public static void pump() { pumpAll(); }

    public static boolean isActive() { return !ACTIVE.isEmpty(); }

    public static void cancelAll() {
        for (Act act : new ArrayList<>(ACTIVE)) cancel(act.id);
    }

    public static boolean stopForHardcore(ServerPlayer player) {
        if (player == null) return false;
        ServerLevel level = player.serverLevel();
        boolean stopped = false;
        for (Act act : new ArrayList<>(ACTIVE)) {
            if (!act.dimension.equals(level.dimension())) continue;
            if (!(level.getEntity(act.id) instanceof EasterBossEntity boss)
                    || !player.getUUID().equals(boss.getManorSummonerId())) continue;
            ManorFightData.get(level.getServer()).retire(act.id);
            cancel(act.id);
            BossMusic.release(level, boss);
            boss.discard();
            stopped = true;
        }
        return stopped;
    }

    static boolean stopForCreative(ServerLevel level) {
        boolean stopped = false;
        for (Act act : new ArrayList<>(ACTIVE)) {
            if (!act.dimension.equals(level.dimension())) continue;
            ManorFightData.get(level.getServer()).retire(act.id);
            cancel(act.id);
            if (level.getEntity(act.id) instanceof EasterBossEntity boss) {
                BossMusic.release(level, boss);
                boss.discard();
            }
            stopped = true;
        }
        return stopped;
    }

    public static void cancel(UUID entityId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        for (Act act : new ArrayList<>(ACTIVE)) {
            if (!act.id.equals(entityId)) continue;
            clear(server, act, Math.max(0L, PresentationClock.millis() - act.startMillis));
            ACTIVE.remove(act);
            BossMusic.stopManorCue("战场取消");
        }
    }

    public static boolean isActing(UUID id) {
        for (Act act : ACTIVE) if (act.id.equals(id)) return true;
        return false;
    }

    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
    public static final class Ticker {
        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) pumpAll();
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            for (Act act : ACTIVE) {
                clear(event.getServer(), act, 0L);
                ServerLevel level = event.getServer().getLevel(act.dimension);
                if (level != null && level.getEntity(act.id) != null) level.getEntity(act.id).discard();
            }
            ACTIVE.clear();
            lastPump = -1;
            BossMusic.stopManorCue("服务器关闭");
        }
    }
}
