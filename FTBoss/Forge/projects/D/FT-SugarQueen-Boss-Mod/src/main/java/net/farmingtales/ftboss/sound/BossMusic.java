package net.farmingtales.ftboss.sound;

import java.util.ArrayList;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class BossMusic {
    public static final String TAG_NO_BGM = "ftboss_no_bgm";
    private static final double HEAR_RANGE = 96;
    private static final double RETAIN_RANGE = 128;
    private static final int GRACE_TICKS = 100;
    private static final int REQUEST_TIMEOUT_TICKS = 80;
    private static final int MIN_ACT_TICKS = 200;
    private static final UUID SILENCE = new UUID(0, 0);
    private static final Map<UUID, Member> MEMBERS = new LinkedHashMap<>();
    private static final Map<UUID, Listener> LISTENERS = new HashMap<>();
    private static final List<ManorCue> MANOR = new ArrayList<>();
    private static MinecraftServer ownerServer;
    private static long revision;
    private static long lastUpdate = -10;

    private record Member(LivingEntity boss, long seen) {}
    private record Selection(UUID encounter, ResourceLocation dimension, List<ResourceLocation> tracks) {}

    private static final class Listener {
        LivingEntity boss;
        UUID encounter;
        Selection selection;
        long absentSince = -1;
        long actChanged;
        int phase;
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
            id = identity == null ? UUID.randomUUID() : identity;
            this.level = level;
            this.anchor = anchor;
            created = level.getServer().getTickCount();
            lastMember = created;
        }
    }

    private BossMusic() {}

    private static void useServer(MinecraftServer server) {
        if (ownerServer == server) return;
        reset();
        ownerServer = server;
    }

    private static void reset() {
        MEMBERS.clear();
        LISTENERS.clear();
        MANOR.clear();
        ownerServer = null;
        revision = 0;
        lastUpdate = -10;
    }

    public static void markNoBgm(Entity entity) {
        if (entity != null) entity.getPersistentData().putBoolean(TAG_NO_BGM, true);
    }

    public static boolean isNoBgm(Entity entity) {
        return entity != null && entity.getPersistentData().getBoolean(TAG_NO_BGM);
    }

    public static boolean isBgmBoss(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive() || entity.isRemoved() || isNoBgm(entity)) return false;
        if (entity instanceof EasterBossEntity easter) return !easter.isMini();
        var type = entity.getType();
        return type == FTEntities.TEA_TREE.get() || type == FTEntities.SUGAR_QUEEN.get()
                || type == FTEntities.MEAT_MOUNTAIN.get() || type == FTEntities.WINE_BARREL.get()
                || type == FTEntities.RAT_BOSS.get() || type == FTEntities.DEFECT_CORE.get()
                || type == FTEntities.COMPOST_BOSS.get() || type == FTEntities.APPLE_BOSS.get()
                || type == FTEntities.BENGER.get() || entity instanceof TideFishBossEntity;
    }

    public static void request(ServerLevel level, LivingEntity boss) {
        if (!FTSounds.MUSIC_ENABLED || level == null || !isBgmBoss(boss) || boss.level() != level
                || HiddenBossIntro.isActing(boss.getUUID())) return;
        useServer(level.getServer());
        MEMBERS.put(boss.getUUID(), new Member(boss, level.getServer().getTickCount()));
        if (boss instanceof EasterBossEntity) {
            UUID fight = ManorFight.fightId(boss);
            Vec3 anchor = fight == null ? null : ManorFight.anchorOf(fight);
            if (anchor != null) {
                ManorCue cue = cue(level, anchor, fight);
                cue.anchor = anchor;
                cue.fight = fight;
                cue.phase = 2;
                cue.lastMember = level.getServer().getTickCount();
            }
        }
    }

    public static void release(ServerLevel level, LivingEntity boss) {
        if (boss != null) MEMBERS.remove(boss.getUUID());
    }

    private static ManorCue cue(ServerLevel level, Vec3 anchor) {
        return cue(level, anchor, null);
    }

    private static ManorCue cue(ServerLevel level, Vec3 anchor, UUID identity) {
        useServer(level.getServer());
        return MANOR.stream().filter(cue -> cue.level == level && (identity != null && identity.equals(cue.fight)
                        || (identity == null || cue.fight == null) && cue.anchor.distanceToSqr(anchor) <= 32 * 32))
                .min(Comparator.comparingDouble(cue -> cue.anchor.distanceToSqr(anchor))).orElseGet(() -> {
                    ManorCue created = new ManorCue(level, anchor, identity);
                    MANOR.add(created);
                    return created;
                });
    }

    public static void beginManorPhase(ServerLevel level, Vec3 anchor) {
        if (FTSounds.MUSIC_ENABLED && level != null && anchor != null) cue(level, anchor);
    }

    public static void playManorCue(ServerLevel level, Vec3 anchor) {
        if (!FTSounds.MUSIC_ENABLED || level == null || anchor == null) return;
        ManorCue cue = cue(level, anchor);
        if (cue.phase == 0) cue.phase = 1;
    }

    public static void startManorBattle(ServerLevel level, Vec3 anchor) {
        if (!FTSounds.MUSIC_ENABLED || level == null || anchor == null) return;
        ManorCue cue = cue(level, anchor);
        cue.phase = 2;
        cue.lastMember = level.getServer().getTickCount();
    }

    public static void stopManorCue(String reason) {
        if (MANOR.removeIf(cue -> cue.phase < 2)) FTBossMod.LOGGER.info("[FTBoss][Music] manor cue stopped: {}", reason);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) sweep(event.getServer());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { reset(); }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { LISTENERS.remove(event.getEntity().getUUID()); }

    public static void sweep(MinecraftServer server) {
        if (server == null) return;
        useServer(server);
        long now = server.getTickCount();
        if (now >= lastUpdate && now - lastUpdate < 10) return;
        lastUpdate = now;
        update(server, now);
    }

    private static void update(MinecraftServer server, long now) {
        MEMBERS.values().removeIf(member -> !isBgmBoss(member.boss) || now - member.seen > REQUEST_TIMEOUT_TICKS);
        MANOR.removeIf(cue -> expired(cue, now));
        for (ServerPlayer player : server.getPlayerList().getPlayers()) updatePlayer(player, now);
    }

    private static boolean expired(ManorCue cue, long now) {
        if (cue.phase < 2) {
            return now - cue.created > GRACE_TICKS && !HiddenBossIntro.isActive() && !HiddenBossChallenge.awaiting();
        }
        for (Member member : MEMBERS.values()) {
            if (member.boss instanceof EasterBossEntity && member.boss.level() == cue.level
                    && member.boss.distanceToSqr(cue.anchor) <= RETAIN_RANGE * RETAIN_RANGE) {
                cue.lastMember = now;
                UUID fight = ManorFight.fightId(member.boss);
                if (fight != null) cue.fight = fight;
            }
        }
        return cue.fight != null ? !ManorFight.hasActive(cue.fight) : now - cue.lastMember > GRACE_TICKS;
    }

    private static boolean near(ServerPlayer player, LivingEntity boss, double range) {
        return isBgmBoss(boss) && boss.level() == player.level() && player.distanceToSqr(boss) <= range * range;
    }

    private static void updatePlayer(ServerPlayer player, long now) {
        Listener listener = LISTENERS.computeIfAbsent(player.getUUID(), unused -> new Listener());
        ResourceLocation dimension = player.level().dimension().location();
        if (!FTSounds.MUSIC_ENABLED || !player.isAlive() || player.isSpectator()
                || listener.selection != null && !listener.selection.dimension.equals(dimension)) {
            listener.boss = null;
            listener.absentSince = -1;
            send(player, listener, new Selection(SILENCE, dimension, List.of()));
            if (!FTSounds.MUSIC_ENABLED || !player.isAlive() || player.isSpectator()) return;
        }
        ManorCue manor = MANOR.stream().filter(cue -> cue.level == player.level()
                        && player.distanceToSqr(cue.anchor) <= (listener.selection != null
                        && listener.selection.encounter.equals(cue.id) ? RETAIN_RANGE * RETAIN_RANGE : HEAR_RANGE * HEAR_RANGE))
                .min(Comparator.comparingDouble(cue -> player.distanceToSqr(cue.anchor))).orElse(null);
        if (manor != null) {
            listener.boss = null;
            listener.absentSince = -1;
            List<ResourceLocation> tracks = manor.phase == 0 ? List.of() : manor.phase == 1
                    ? tracks(FTSounds.BOSS_MUSIC_MANOR_DUAL.get()) : manorBattle();
            send(player, listener, new Selection(manor.id, dimension, tracks));
            return;
        }
        LivingEntity boss = listener.boss;
        if (boss == null || !MEMBERS.containsKey(boss.getUUID()) || !near(player, boss, RETAIN_RANGE)) {
            boss = MEMBERS.values().stream().map(Member::boss).filter(candidate -> near(player, candidate, HEAR_RANGE))
                    .min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
        }
        if (boss != null) {
            boolean keepEncounter = listener.boss != null && listener.boss.getType() == boss.getType()
                    && listener.absentSince < 0 && (listener.boss == boss || listener.boss.isDeadOrDying());
            if (!keepEncounter) {
                UUID fight = ManorFight.fightId(boss);
                listener.encounter = fight == null ? boss.getUUID() : fight;
                listener.phase = 0;
                listener.actChanged = now;
            }
            listener.boss = boss;
            listener.absentSince = -1;
            send(player, listener, new Selection(listener.encounter, dimension, musicFor(boss, listener, now)));
        } else {
            if (listener.absentSince < 0) listener.absentSince = now;
            if (now - listener.absentSince < GRACE_TICKS) return;
            if (listener.boss != null && listener.boss.isDeadOrDying() && !(listener.boss instanceof EasterBossEntity)) {
                send(player, listener, new Selection(listener.encounter, dimension, tracks(FTSounds.BOSS_MUSIC_CURTAIN.get())));
                listener.boss = null;
            } else if (listener.selection == null || !listener.selection.tracks.equals(tracks(FTSounds.BOSS_MUSIC_CURTAIN.get()))) {
                send(player, listener, new Selection(SILENCE, dimension, List.of()));
                listener.boss = null;
            }
        }
    }

    private static List<ResourceLocation> manorBattle() {
        return tracks(FTSounds.BOSS_MUSIC_MANOR_GUIGNOL.get(), FTSounds.BOSS_MUSIC_MANOR_DUAL_BATTLE.get());
    }

    private static List<ResourceLocation> tracks(SoundEvent... events) {
        return java.util.Arrays.stream(events).map(SoundEvent::getLocation).toList();
    }

    private static List<ResourceLocation> musicFor(LivingEntity boss, Listener listener, long now) {
        if (boss instanceof TideFishBossEntity fish) {
            listener.phase = Math.max(listener.phase, fish.getPhase());
            return tracks(listener.phase >= 2 ? FTSounds.BOSS_MUSIC_TIDE_FISH_P2.get() : FTSounds.BOSS_MUSIC_TIDE_FISH.get());
        }
        if (boss instanceof CompostBossEntity compost) {
            listener.phase = Math.max(listener.phase, compost.getPhase());
            return tracks(listener.phase >= 2 ? FTSounds.BOSS_MUSIC_COMPOST_P2.get() : FTSounds.BOSS_MUSIC_COMPOST.get());
        }
        if (boss instanceof AppleBossEntity apple) {
            listener.phase = Math.max(listener.phase, apple.getPhase());
            return tracks(listener.phase >= 2 ? FTSounds.BOSS_MUSIC_APPLE_P2.get() : FTSounds.BOSS_MUSIC_APPLE.get());
        }
        if (boss instanceof EasterBossEntity) return manorBattle();
        var type = boss.getType();
        if (type == FTEntities.TEA_TREE.get()) return tracks(FTSounds.BOSS_MUSIC_SAYA.get());
        if (type == FTEntities.SUGAR_QUEEN.get()) return tracks(FTSounds.BOSS_MUSIC_GINGERBREAD.get());
        if (type == FTEntities.MEAT_MOUNTAIN.get()) return tracks(FTSounds.BOSS_MUSIC_BUTCHER.get());
        if (type == FTEntities.WINE_BARREL.get()) return tracks(FTSounds.BOSS_MUSIC_TAVERN.get());
        if (type == FTEntities.RAT_BOSS.get()) return tracks(FTSounds.BOSS_MUSIC_RAT.get());
        if (type == FTEntities.DEFECT_CORE.get()) return tracks(FTSounds.BOSS_MUSIC_DEFECT.get());
        if (type == FTEntities.BENGER.get()) return tracks(FTSounds.BOSS_MUSIC_BENGER.get());
        float ratio = boss.getHealth() / Math.max(1, boss.getMaxHealth());
        int desired = ratio <= 0.33F ? 2 : ratio <= 0.66F ? 1 : 0;
        if (desired > listener.phase && now - listener.actChanged >= MIN_ACT_TICKS) {
            listener.phase = desired;
            listener.actChanged = now;
        }
        return tracks(listener.phase == 0 ? FTSounds.BOSS_MUSIC_OVERTURE.get()
                : listener.phase == 1 ? FTSounds.BOSS_MUSIC_MASQUERADE.get() : FTSounds.BOSS_MUSIC_FINALE.get());
    }

    private static void send(ServerPlayer player, Listener listener, Selection selection) {
        if (selection.equals(listener.selection)) return;
        listener.selection = selection;
        var packet = new BossMusicPacket(++revision, selection.encounter, selection.dimension, selection.tracks);
        BossMusicNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        FTBossMod.LOGGER.info("[FTBoss][Music] state revision={} player={} encounter={} tracks={}",
                revision, player.getGameProfile().getName(), selection.encounter, selection.tracks);
    }

    public static boolean isPlaying() {
        return LISTENERS.values().stream().anyMatch(listener -> listener.selection != null && !listener.selection.tracks.isEmpty());
    }
}
