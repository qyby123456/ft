/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.server.ServerStoppingEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightData;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ManorIntroPacket;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.ManorIntroTimeline;
import net.farmingtales.ftboss.util.PresentationClock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class HiddenBossIntro {
    public static final double DISTANCE = 15.0;
    public static final int DEFAULT_VARIANT = 9;
    public static final int INTRO_MIN_TICKS = 920;
    private static final double VIEW_RADIUS_SQUARED = 9216.0;
    private static final int GROUND_SEARCH_UP = 4;
    private static final int GROUND_SEARCH_DOWN = 32;
    private static final List<Act> ACTIVE = new ArrayList<Act>();
    private static int lastPump = -1;

    private HiddenBossIntro() {
    }

    public static void start(ServerLevel level, Vec3 anchor, Vec3 from, String ... lines) {
        HiddenBossIntro.start(level, anchor, from, 9, lines);
    }

    public static void start(ServerLevel level, Vec3 anchor, Vec3 from, int variant, String ... lines) {
        HiddenBossIntro.start(level, anchor, from, variant, null, lines);
    }

    public static void start(ServerLevel level, Vec3 anchor, Vec3 from, UUID summonerId, String ... lines) {
        HiddenBossIntro.start(level, anchor, from, 9, summonerId, lines);
    }

    public static void start(ServerLevel level, Vec3 anchor, Vec3 from, int variant, UUID summonerId, String ... lines) {
        HiddenBossIntro.start(level, anchor, from, variant, summonerId, ManorDifficulty.NORMAL, lines);
    }

    public static void start(ServerLevel level, Vec3 anchor, Vec3 from, int variant, UUID summonerId, ManorDifficulty difficulty, String ... lines) {
        if (level == null || HiddenBossIntro.isActive() || !SummonGate.isAllowedDim((Level)level) || ManorFight.hasActive(level)) {
            return;
        }
        Vec3 direction = from.m_82546_(anchor);
        if (direction.m_82556_() < 1.0E-4) {
            direction = new Vec3(0.0, 0.0, 1.0);
        }
        direction = direction.m_82541_();
        double posX = anchor.f_82479_ + direction.f_82479_ * 15.0;
        double posZ = anchor.f_82481_ + direction.f_82481_ * 15.0;
        EasterBossEntity boss = (EasterBossEntity)((EntityType)FTEntities.EASTER_BOSS.get()).m_20615_((Level)level);
        if (boss == null) {
            BossMusic.stopManorCue("\u5165\u573a\u5b9e\u4f53\u521b\u5efa\u5931\u8d25");
            return;
        }
        boss.setVariant(variant);
        boss.setChallengeDifficulty(difficulty);
        boss.setManorSummonerId(summonerId);
        Vec3 grounded = HiddenBossIntro.findGround(level, boss, new Vec3(posX, from.f_82480_, posZ));
        if (grounded == null) {
            grounded = HiddenBossIntro.findGround(level, boss, anchor);
        }
        if (grounded == null) {
            BossMusic.stopManorCue("\u9644\u8fd1\u6ca1\u6709\u53ef\u7ad9\u7acb\u7684\u6f14\u51fa\u5730\u9762");
            for (ServerPlayer player : level.m_6907_()) {
                if (!(player.m_20238_(anchor) <= 9216.0)) continue;
                player.m_213846_((Component)Component.m_237113_((String)"\u00a7c\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u9644\u8fd1\u6ca1\u6709\u53ef\u7ad9\u7acb\u7684\u6f14\u51fa\u5730\u9762\uff0c\u8bf7\u5728\u5e73\u5766\u5730\u9762\u91cd\u65b0\u89e6\u53d1\u3002"));
            }
            return;
        }
        posX = grounded.f_82479_;
        posZ = grounded.f_82481_;
        double posY = grounded.f_82480_;
        float yaw = (float)Math.toDegrees(Math.atan2(anchor.f_82481_ - posZ, anchor.f_82479_ - posX)) - 90.0f;
        boss.m_7678_(posX, posY, posZ, yaw, 0.0f);
        boss.m_20331_(true);
        boss.m_21557_(true);
        boss.setManorIntroHidden(variant == 9);
        EasterBossEntity easterBossEntity = boss;
        easterBossEntity.prepareManorEntryBuffs();
        if (!level.m_7967_((Entity)easterBossEntity)) {
            BossMusic.stopManorCue("\u5165\u573a\u5b9e\u4f53\u751f\u6210\u88ab\u53d6\u6d88");
            return;
        }
        ServerBossEvent bar = new ServerBossEvent((Component)Component.m_237113_((String)("\u00a7c\u00a7l\u751c\u82f9\u679c\u5e84\u56ed" + boss.getChallengeDifficulty().badge())), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        bar.m_142711_(0.0f);
        BossBarVisibility.visible(boss.getBossBar(), false);
        Act act = new Act(level, boss, bar, lines);
        ACTIVE.add(act);
        BossMusic.beginManorPhase(level, anchor);
        BossMusic.playManorCue(level, boss.m_20182_());
        HiddenBossIntro.syncViewers(level, act, 0L);
        level.m_6263_(null, posX, posY, posZ, SoundEvents.f_11736_, SoundSource.HOSTILE, 1.0f, 0.7f);
        FTBossMod.LOGGER.info("[FTBoss][ManorIntro] start id={} duration={}ms; client mesh, no particles/crystal entities", (Object)act.id, (Object)46000L);
    }

    private static Vec3 findGround(ServerLevel level, EasterBossEntity boss, Vec3 preferred) {
        BlockPos column = BlockPos.m_274446_((Position)preferred);
        int highest = Math.min(level.m_151558_() - 1, column.m_123342_() + 4);
        int lowest = Math.max(level.m_141937_(), column.m_123342_() - 32);
        Vec3 closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (int blockY = highest; blockY >= lowest; --blockY) {
            BlockState state;
            BlockPos floor = new BlockPos(column.m_123341_(), blockY, column.m_123343_());
            if (!level.m_46805_(floor) || !(state = level.m_8055_(floor)).m_60819_().m_76178_()) continue;
            for (AABB surface : state.m_60812_((BlockGetter)level, floor).m_83299_()) {
                AABB body;
                double feetY;
                double distance;
                double localX = preferred.f_82479_ - (double)floor.m_123341_();
                double localZ = preferred.f_82481_ - (double)floor.m_123343_();
                if (localX < surface.f_82288_ || localX > surface.f_82291_ || localZ < surface.f_82290_ || localZ > surface.f_82293_ || (distance = Math.abs((feetY = (double)blockY + surface.f_82292_) - preferred.f_82480_)) >= closestDistance || !level.m_45756_((Entity)boss, body = boss.m_20191_().m_82386_(preferred.f_82479_ - boss.m_20185_(), feetY - boss.m_20186_(), preferred.f_82481_ - boss.m_20189_())) || level.m_46855_(body)) continue;
                closest = new Vec3(preferred.f_82479_, feetY, preferred.f_82481_);
                closestDistance = distance;
            }
        }
        return closest;
    }

    private static void send(ServerPlayer player, Act act, long elapsedMillis, boolean active) {
        FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new ManorIntroPacket(act.id, act.dimension.m_135782_(), act.position, elapsedMillis, active));
    }

    private static void syncViewers(ServerLevel level, Act act, long elapsedMillis) {
        boolean sync = elapsedMillis >= act.nextSyncMillis;
        HashSet<UUID> nearby = new HashSet<UUID>();
        for (ServerPlayer player : level.m_6907_()) {
            if (player.m_20238_(act.position) > 9216.0) continue;
            nearby.add(player.m_20148_());
            BossBarVisibility.addPlayer(act.bar, player);
            if (!act.viewers.add(player.m_20148_()) && !sync) continue;
            HiddenBossIntro.send(player, act, elapsedMillis, true);
        }
        for (UUID viewer : new HashSet<UUID>(act.viewers)) {
            if (nearby.contains(viewer)) continue;
            ServerPlayer player = level.m_7654_().m_6846_().m_11259_(viewer);
            if (player != null) {
                act.bar.m_6539_(player);
                HiddenBossIntro.send(player, act, elapsedMillis, false);
            }
            act.viewers.remove(viewer);
        }
        for (ServerPlayer player : new ArrayList(act.bar.m_8324_())) {
            if (nearby.contains(player.m_20148_())) continue;
            act.bar.m_6539_(player);
        }
        if (sync) {
            act.nextSyncMillis = elapsedMillis + 2000L;
        }
    }

    private static void clear(MinecraftServer server, Act act, long elapsedMillis) {
        Entity entity;
        ServerLevel level = server.m_129880_(act.dimension);
        if (level != null && (entity = level.m_8791_(act.id)) instanceof EasterBossEntity) {
            EasterBossEntity boss = (EasterBossEntity)entity;
            boss.setManorIntroHidden(false);
        }
        act.bar.m_7706_();
        for (UUID viewer : act.viewers) {
            ServerPlayer player = server.m_6846_().m_11259_(viewer);
            if (player == null) continue;
            HiddenBossIntro.send(player, act, elapsedMillis, false);
        }
        act.viewers.clear();
    }

    public static void pumpAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || ACTIVE.isEmpty() || lastPump == server.m_129921_()) {
            return;
        }
        lastPump = server.m_129921_();
        for (Act act : new ArrayList<Act>(ACTIVE)) {
            EasterBossEntity boss;
            Entity entity;
            long elapsedMillis = Math.max(0L, PresentationClock.millis() - act.startMillis);
            ServerLevel level = server.m_129880_(act.dimension);
            if (level == null || !((entity = level.m_8791_(act.id)) instanceof EasterBossEntity) || !(boss = (EasterBossEntity)entity).m_6084_()) {
                HiddenBossIntro.clear(server, act, elapsedMillis);
                ACTIVE.remove(act);
                BossMusic.stopManorCue("\u5165\u573a\u5b9e\u4f53\u5df2\u79fb\u9664");
                continue;
            }
            act.position = boss.m_20182_();
            act.bar.m_142711_(ManorIntroTimeline.progress(elapsedMillis));
            if (elapsedMillis >= act.nextSyncMillis) {
                HiddenBossIntro.syncViewers(level, act, elapsedMillis);
            }
            while (act.sent < act.lines.length && elapsedMillis >= ManorIntroTimeline.dialogueMillis(act.sent, act.lines.length)) {
                String text;
                if ((text = act.lines[act.sent++]) == null || text.isEmpty()) continue;
                for (ServerPlayer player : level.m_6907_()) {
                    if (!act.viewers.contains(player.m_20148_())) continue;
                    player.m_213846_((Component)Component.m_237113_((String)text));
                }
            }
            if (elapsedMillis < 46000L) continue;
            HiddenBossIntro.clear(server, act, elapsedMillis);
            ACTIVE.remove(act);
            boss.prepareManorEntryBuffs();
            boss.m_20331_(false);
            boss.m_21557_(false);
            if (boss.getVariant() == 9) {
                EasterBossEntity.supportSparrow(level, boss);
                int first = EasterBossEntity.MANOR_VARIANTS[boss.m_217043_().m_188503_(EasterBossEntity.MANOR_COUNT)];
                ManorFight.start(level, boss.m_20182_(), first, boss.getManorSummonerId(), boss.getChallengeDifficulty());
                for (ServerPlayer player : level.m_6907_()) {
                    if (!(player.m_20280_((Entity)boss) <= 9216.0)) continue;
                    player.m_213846_((Component)Component.m_237113_((String)("\u00a7e\u3010\u9ebb\u96c0\u3011\u00a77\u7559\u5728\u573a\u5730\u652f\u63f4\uff01\u00a7c\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u00a77\u7b2c\u4e00\u4f4d\u4e0a\u573a\uff1a\u00a7c" + EasterBossEntity.NAMES[first])));
                }
            } else {
                BossBarVisibility.visible(boss.getBossBar(), false);
                ManorFight.adopt(level, boss);
            }
            level.m_5594_(null, boss.m_20183_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 1.0f, 0.8f);
            BossMusic.startManorBattle(level, boss.m_20182_());
            FTBossMod.LOGGER.info("[FTBoss][ManorIntro] complete id={} elapsed={}ms -> battle", (Object)act.id, (Object)elapsedMillis);
        }
    }

    public static void pump() {
        HiddenBossIntro.pumpAll();
    }

    public static boolean isActive() {
        return !ACTIVE.isEmpty();
    }

    public static void cancelAll() {
        for (Act act : new ArrayList<Act>(ACTIVE)) {
            HiddenBossIntro.cancel(act.id);
        }
    }

    public static boolean stopForHardcore(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        ServerLevel level = player.m_284548_();
        boolean stopped = false;
        for (Act act : new ArrayList<Act>(ACTIVE)) {
            Entity entity;
            if (!act.dimension.equals((Object)level.m_46472_()) || !((entity = level.m_8791_(act.id)) instanceof EasterBossEntity)) continue;
            EasterBossEntity boss = (EasterBossEntity)entity;
            if (!player.m_20148_().equals(boss.getManorSummonerId())) continue;
            ManorFightData.get(level.m_7654_()).retire(act.id);
            HiddenBossIntro.cancel(act.id);
            BossMusic.release(level, (LivingEntity)boss);
            boss.m_146870_();
            stopped = true;
        }
        return stopped;
    }

    static boolean stopForCreative(ServerLevel level) {
        boolean stopped = false;
        for (Act act : new ArrayList<Act>(ACTIVE)) {
            if (!act.dimension.equals((Object)level.m_46472_())) continue;
            ManorFightData.get(level.m_7654_()).retire(act.id);
            HiddenBossIntro.cancel(act.id);
            Entity entity = level.m_8791_(act.id);
            if (entity instanceof EasterBossEntity) {
                EasterBossEntity boss = (EasterBossEntity)entity;
                BossMusic.release(level, (LivingEntity)boss);
                boss.m_146870_();
            }
            stopped = true;
        }
        return stopped;
    }

    public static void cancel(UUID entityId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (Act act : new ArrayList<Act>(ACTIVE)) {
            if (!act.id.equals(entityId)) continue;
            HiddenBossIntro.clear(server, act, Math.max(0L, PresentationClock.millis() - act.startMillis));
            ACTIVE.remove(act);
            BossMusic.stopManorCue("\u6218\u573a\u53d6\u6d88");
        }
    }

    public static boolean isActing(UUID id) {
        for (Act act : ACTIVE) {
            if (!act.id.equals(id)) continue;
            return true;
        }
        return false;
    }

    private static final class Act {
        final ResourceKey<Level> dimension;
        final UUID id;
        final long startMillis = PresentationClock.millis();
        final ServerBossEvent bar;
        final String[] lines;
        final Set<UUID> viewers = new HashSet<UUID>();
        Vec3 position;
        long nextSyncMillis;
        int sent;

        Act(ServerLevel level, EasterBossEntity boss, ServerBossEvent bar, String[] lines) {
            this.dimension = level.m_46472_();
            this.id = boss.m_20148_();
            this.position = boss.m_20182_();
            this.bar = bar;
            this.lines = lines == null ? new String[]{} : (String[])lines.clone();
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss")
    public static final class Ticker {
        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                HiddenBossIntro.pumpAll();
            }
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            for (Act act : ACTIVE) {
                HiddenBossIntro.clear(event.getServer(), act, 0L);
                ServerLevel level = event.getServer().m_129880_(act.dimension);
                if (level == null || level.m_8791_(act.id) == null) continue;
                level.m_8791_(act.id).m_146870_();
            }
            ACTIVE.clear();
            lastPump = -1;
            BossMusic.stopManorCue("\u670d\u52a1\u5668\u5173\u95ed");
        }
    }
}

