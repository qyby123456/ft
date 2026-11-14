/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package net.farmingtales.ftboss.sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossChallenge;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.sound.BossMusicNetwork;
import net.farmingtales.ftboss.sound.BossMusicPacket;
import net.farmingtales.ftboss.sound.FTSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="ftboss")
public final class BossMusic {
    public static final String TAG_NO_BGM = "ftboss_no_bgm";
    private static final double HEAR_RANGE = 96.0;
    private static final double RETAIN_RANGE = 128.0;
    private static final int GRACE_TICKS = 100;
    private static final int REQUEST_TIMEOUT_TICKS = 80;
    private static final int MIN_ACT_TICKS = 200;
    private static final UUID SILENCE = new UUID(0L, 0L);
    private static final Map<UUID, Member> MEMBERS = new LinkedHashMap<UUID, Member>();
    private static final Map<UUID, Listener> LISTENERS = new HashMap<UUID, Listener>();
    private static final List<ManorCue> MANOR = new ArrayList<ManorCue>();
    private static MinecraftServer ownerServer;
    private static long revision;
    private static long lastUpdate;

    private BossMusic() {
    }

    private static void useServer(MinecraftServer server) {
        if (ownerServer == server) {
            return;
        }
        BossMusic.reset();
        ownerServer = server;
    }

    private static void reset() {
        MEMBERS.clear();
        LISTENERS.clear();
        MANOR.clear();
        ownerServer = null;
        revision = 0L;
        lastUpdate = -10L;
    }

    public static void markNoBgm(Entity entity) {
        if (entity != null) {
            entity.getPersistentData().m_128379_(TAG_NO_BGM, true);
        }
    }

    public static boolean isNoBgm(Entity entity) {
        return entity != null && entity.getPersistentData().m_128471_(TAG_NO_BGM);
    }

    public static boolean isBgmBoss(Entity entity) {
        LivingEntity living;
        if (!(entity instanceof LivingEntity) || !(living = (LivingEntity)entity).m_6084_() || entity.m_213877_() || BossMusic.isNoBgm(entity)) {
            return false;
        }
        if (entity instanceof EasterBossEntity) {
            EasterBossEntity easter = (EasterBossEntity)entity;
            return !easter.isMini();
        }
        EntityType type = entity.m_6095_();
        return type == FTEntities.TEA_TREE.get() || type == FTEntities.SUGAR_QUEEN.get() || type == FTEntities.MEAT_MOUNTAIN.get() || type == FTEntities.WINE_BARREL.get() || type == FTEntities.RAT_BOSS.get() || type == FTEntities.DEFECT_CORE.get() || type == FTEntities.COMPOST_BOSS.get() || type == FTEntities.APPLE_BOSS.get() || type == FTEntities.BENGER.get() || entity instanceof TideFishBossEntity;
    }

    public static void request(ServerLevel level, LivingEntity boss) {
        if (level == null || !BossMusic.isBgmBoss((Entity)boss) || boss.m_9236_() != level || HiddenBossIntro.isActing(boss.m_20148_())) {
            return;
        }
        BossMusic.useServer(level.m_7654_());
        MEMBERS.put(boss.m_20148_(), new Member(boss, level.m_7654_().m_129921_()));
        if (boss instanceof EasterBossEntity) {
            Vec3 anchor;
            UUID fight = ManorFight.fightId((Entity)boss);
            Vec3 vec3 = anchor = fight == null ? null : ManorFight.anchorOf(fight);
            if (anchor != null) {
                ManorCue cue = BossMusic.cue(level, anchor, fight);
                cue.anchor = anchor;
                cue.fight = fight;
                cue.phase = 2;
                cue.lastMember = level.m_7654_().m_129921_();
            }
        }
    }

    public static void release(ServerLevel level, LivingEntity boss) {
        if (boss != null) {
            MEMBERS.remove(boss.m_20148_());
        }
    }

    private static ManorCue cue(ServerLevel level, Vec3 anchor) {
        return BossMusic.cue(level, anchor, null);
    }

    private static ManorCue cue(ServerLevel level, Vec3 anchor, UUID identity) {
        BossMusic.useServer(level.m_7654_());
        return MANOR.stream().filter(cue -> cue.level == level && (identity != null && identity.equals(cue.fight) || (identity == null || cue.fight == null) && cue.anchor.m_82557_(anchor) <= 1024.0)).min(Comparator.comparingDouble(cue -> cue.anchor.m_82557_(anchor))).orElseGet(() -> {
            ManorCue created = new ManorCue(level, anchor, identity);
            MANOR.add(created);
            return created;
        });
    }

    public static void beginManorPhase(ServerLevel level, Vec3 anchor) {
        if (level != null && anchor != null) {
            BossMusic.cue(level, anchor);
        }
    }

    public static void playManorCue(ServerLevel level, Vec3 anchor) {
        if (level == null || anchor == null) {
            return;
        }
        ManorCue cue = BossMusic.cue(level, anchor);
        if (cue.phase == 0) {
            cue.phase = 1;
        }
    }

    public static void startManorBattle(ServerLevel level, Vec3 anchor) {
        if (level == null || anchor == null) {
            return;
        }
        ManorCue cue = BossMusic.cue(level, anchor);
        cue.phase = 2;
        cue.lastMember = level.m_7654_().m_129921_();
    }

    public static void stopManorCue(String reason) {
        if (MANOR.removeIf(cue -> cue.phase < 2)) {
            FTBossMod.LOGGER.info("[FTBoss][Music] manor cue stopped: {}", (Object)reason);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BossMusic.sweep(event.getServer());
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        BossMusic.reset();
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        LISTENERS.remove(event.getEntity().m_20148_());
    }

    public static void sweep(MinecraftServer server) {
        if (server == null) {
            return;
        }
        BossMusic.useServer(server);
        long now = server.m_129921_();
        if (now >= lastUpdate && now - lastUpdate < 10L) {
            return;
        }
        lastUpdate = now;
        BossMusic.update(server, now);
    }

    private static void update(MinecraftServer server, long now) {
        MEMBERS.values().removeIf(member -> !BossMusic.isBgmBoss((Entity)member.boss) || now - member.seen > 80L);
        MANOR.removeIf(cue -> BossMusic.expired(cue, now));
        for (ServerPlayer player : server.m_6846_().m_11314_()) {
            BossMusic.updatePlayer(player, now);
        }
    }

    private static boolean expired(ManorCue cue, long now) {
        if (cue.phase < 2) {
            return now - cue.created > 100L && !HiddenBossIntro.isActive() && !HiddenBossChallenge.awaiting();
        }
        for (Member member : MEMBERS.values()) {
            if (!(member.boss instanceof EasterBossEntity) || member.boss.m_9236_() != cue.level || !(member.boss.m_20238_(cue.anchor) <= 16384.0)) continue;
            cue.lastMember = now;
            UUID fight = ManorFight.fightId((Entity)member.boss);
            if (fight == null) continue;
            cue.fight = fight;
        }
        return cue.fight != null ? !ManorFight.hasActive(cue.fight) : now - cue.lastMember > 100L;
    }

    private static boolean near(ServerPlayer player, LivingEntity boss, double range) {
        return BossMusic.isBgmBoss((Entity)boss) && boss.m_9236_() == player.m_9236_() && player.m_20280_((Entity)boss) <= range * range;
    }

    private static void updatePlayer(ServerPlayer player, long now) {
        LivingEntity boss;
        ResourceLocation dimension;
        Listener listener;
        block15: {
            block14: {
                ManorCue manor;
                listener = LISTENERS.computeIfAbsent(player.m_20148_(), unused -> new Listener());
                dimension = player.m_9236_().m_46472_().m_135782_();
                if (!player.m_6084_() || player.m_5833_() || listener.selection != null && !listener.selection.dimension.equals((Object)dimension)) {
                    listener.boss = null;
                    listener.absentSince = -1L;
                    BossMusic.send(player, listener, new Selection(SILENCE, dimension, List.of()));
                    if (!player.m_6084_() || player.m_5833_()) {
                        return;
                    }
                }
                if ((manor = (ManorCue)MANOR.stream().filter(cue -> {
                    if (cue.level != player.m_9236_()) return false;
                    double d = player.m_20238_(cue.anchor);
                    double d2 = listener.selection != null && listener.selection.encounter.equals(cue.id) ? 16384.0 : 9216.0;
                    if (!(d <= d2)) return false;
                    return true;
                }).min(Comparator.comparingDouble(cue -> player.m_20238_(cue.anchor))).orElse(null)) != null) {
                    listener.boss = null;
                    listener.absentSince = -1L;
                    List<ResourceLocation> tracks = manor.phase == 0 ? List.of() : (manor.phase == 1 ? BossMusic.tracks((SoundEvent)FTSounds.BOSS_MUSIC_MANOR_DUAL.get()) : BossMusic.manorBattle());
                    BossMusic.send(player, listener, new Selection(manor.id, dimension, tracks));
                    return;
                }
                boss = listener.boss;
                if (boss == null || !MEMBERS.containsKey(boss.m_20148_())) break block14;
                if (BossMusic.near(player, boss, 128.0)) break block15;
            }
            boss = MEMBERS.values().stream().map(Member::boss).filter(candidate -> BossMusic.near(player, candidate, 96.0)).min(Comparator.comparingDouble(arg_0 -> ((ServerPlayer)player).m_20280_(arg_0))).orElse(null);
        }
        if (boss != null) {
            boolean keepEncounter;
            boolean bl = keepEncounter = listener.boss != null && listener.boss.m_6095_() == boss.m_6095_() && listener.absentSince < 0L && (listener.boss == boss || listener.boss.m_21224_());
            if (!keepEncounter) {
                UUID fight = ManorFight.fightId((Entity)boss);
                listener.encounter = fight == null ? boss.m_20148_() : fight;
                listener.phase = 0;
                listener.actChanged = now;
            }
            listener.boss = boss;
            listener.absentSince = -1L;
            BossMusic.send(player, listener, new Selection(listener.encounter, dimension, BossMusic.musicFor(boss, listener, now)));
        } else {
            if (listener.absentSince < 0L) {
                listener.absentSince = now;
            }
            if (now - listener.absentSince < 100L) {
                return;
            }
            if (listener.boss != null && listener.boss.m_21224_() && !(listener.boss instanceof EasterBossEntity)) {
                BossMusic.send(player, listener, new Selection(listener.encounter, dimension, BossMusic.tracks((SoundEvent)FTSounds.BOSS_MUSIC_CURTAIN.get())));
                listener.boss = null;
            } else if (listener.selection == null || !listener.selection.tracks.equals(BossMusic.tracks((SoundEvent)FTSounds.BOSS_MUSIC_CURTAIN.get()))) {
                BossMusic.send(player, listener, new Selection(SILENCE, dimension, List.of()));
                listener.boss = null;
            }
        }
    }

    private static List<ResourceLocation> manorBattle() {
        return BossMusic.tracks((SoundEvent)FTSounds.BOSS_MUSIC_MANOR_GUIGNOL.get(), (SoundEvent)FTSounds.BOSS_MUSIC_MANOR_DUAL_BATTLE.get());
    }

    private static List<ResourceLocation> tracks(SoundEvent ... events) {
        return Arrays.stream(events).map(SoundEvent::m_11660_).toList();
    }

    private static List<ResourceLocation> musicFor(LivingEntity boss, Listener listener, long now) {
        int desired;
        if (boss instanceof TideFishBossEntity) {
            TideFishBossEntity fish = (TideFishBossEntity)boss;
            listener.phase = Math.max(listener.phase, fish.getPhase());
            return BossMusic.tracks(listener.phase >= 2 ? (SoundEvent)FTSounds.BOSS_MUSIC_TIDE_FISH_P2.get() : (SoundEvent)FTSounds.BOSS_MUSIC_TIDE_FISH.get());
        }
        if (boss instanceof CompostBossEntity) {
            CompostBossEntity compost = (CompostBossEntity)boss;
            listener.phase = Math.max(listener.phase, compost.getPhase());
            return BossMusic.tracks(listener.phase >= 2 ? (SoundEvent)FTSounds.BOSS_MUSIC_COMPOST_P2.get() : (SoundEvent)FTSounds.BOSS_MUSIC_COMPOST.get());
        }
        if (boss instanceof AppleBossEntity) {
            AppleBossEntity apple = (AppleBossEntity)boss;
            listener.phase = Math.max(listener.phase, apple.getPhase());
            return BossMusic.tracks(listener.phase >= 2 ? (SoundEvent)FTSounds.BOSS_MUSIC_APPLE_P2.get() : (SoundEvent)FTSounds.BOSS_MUSIC_APPLE.get());
        }
        if (boss instanceof EasterBossEntity) {
            return BossMusic.manorBattle();
        }
        EntityType type = boss.m_6095_();
        if (type == FTEntities.TEA_TREE.get()) {
            return BossMusic.tracks((SoundEvent)FTSounds.BOSS_MUSIC_SAYA.get());
        }
        if (type == FTEntities.SUGAR_QUEEN.get()) {
            return BossMusic.tracks((SoundEvent)FTSounds.BOSS_MUSIC_GINGERBREAD.get());
        }
        if (type == FTEntities.MEAT_MOUNTAIN.get()) {
            return BossMusic.tracks((SoundEvent)FTSounds.BOSS_MUSIC_BUTCHER.get());
        }
        if (type == FTEntities.WINE_BARREL.get()) {
            return BossMusic.tracks((SoundEvent)FTSounds.BOSS_MUSIC_TAVERN.get());
        }
        if (type == FTEntities.RAT_BOSS.get()) {
            return BossMusic.tracks((SoundEvent)FTSounds.BOSS_MUSIC_RAT.get());
        }
        if (type == FTEntities.DEFECT_CORE.get()) {
            return BossMusic.tracks((SoundEvent)FTSounds.BOSS_MUSIC_DEFECT.get());
        }
        if (type == FTEntities.BENGER.get()) {
            return BossMusic.tracks((SoundEvent)FTSounds.BOSS_MUSIC_BENGER.get());
        }
        float ratio = boss.m_21223_() / Math.max(1.0f, boss.m_21233_());
        int n = ratio <= 0.33f ? 2 : (desired = ratio <= 0.66f ? 1 : 0);
        if (desired > listener.phase && now - listener.actChanged >= 200L) {
            listener.phase = desired;
            listener.actChanged = now;
        }
        return BossMusic.tracks(listener.phase == 0 ? (SoundEvent)FTSounds.BOSS_MUSIC_OVERTURE.get() : (listener.phase == 1 ? (SoundEvent)FTSounds.BOSS_MUSIC_MASQUERADE.get() : (SoundEvent)FTSounds.BOSS_MUSIC_FINALE.get()));
    }

    private static void send(ServerPlayer player, Listener listener, Selection selection) {
        if (selection.equals(listener.selection)) {
            return;
        }
        listener.selection = selection;
        BossMusicPacket packet = new BossMusicPacket(++revision, selection.encounter, selection.dimension, selection.tracks);
        BossMusicNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)packet);
        FTBossMod.LOGGER.info("[FTBoss][Music] state revision={} player={} encounter={} tracks={}", new Object[]{revision, player.m_36316_().getName(), selection.encounter, selection.tracks});
    }

    public static boolean isPlaying() {
        return LISTENERS.values().stream().anyMatch(listener -> listener.selection != null && !listener.selection.tracks.isEmpty());
    }

    static {
        lastUpdate = -10L;
    }

    private record Member(LivingEntity boss, long seen) {
    }

    private static final class ManorCue {
        final UUID id;
        final ServerLevel level;
        Vec3 anchor;
        final long created;
        int phase;
        UUID fight;
        long lastMember;

        ManorCue(ServerLevel level, Vec3 anchor, UUID identity) {
            this.id = identity == null ? UUID.randomUUID() : identity;
            this.level = level;
            this.anchor = anchor;
            this.lastMember = this.created = (long)level.m_7654_().m_129921_();
        }
    }

    private static final class Listener {
        LivingEntity boss;
        UUID encounter;
        Selection selection;
        long absentSince = -1L;
        long actChanged;
        int phase;

        private Listener() {
        }
    }

    private record Selection(UUID encounter, ResourceLocation dimension, List<ResourceLocation> tracks) {
    }
}

