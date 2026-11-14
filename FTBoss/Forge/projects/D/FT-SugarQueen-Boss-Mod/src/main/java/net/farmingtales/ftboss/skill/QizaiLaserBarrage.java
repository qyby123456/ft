package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QizaiLaserBarrageRules;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Grounded field skill that continually locks random directions and guarantees a player lock every second. */
public final class QizaiLaserBarrage {
    private static final class Beam {
        final Vec3 origin;
        Vec3 end;
        final int spawnTick;
        final int fireTick;
        final boolean targeted;
        boolean fired;

        Beam(Vec3 origin, Vec3 end, int spawnTick, int fireTick, boolean targeted) {
            this.origin = origin;
            this.end = end;
            this.spawnTick = spawnTick;
            this.fireTick = fireTick;
            this.targeted = targeted;
        }
    }

    private final EasterBossEntity boss;
    private final List<Beam> beams = new ArrayList<>();
    private UUID preferredTarget;
    /** 最近一次有效玩家的碰撞箱中心；玩家暂时失效后，已承诺的锁定束继续打向这个冻结点。 */
    private Vec3 lastTarget = Vec3.ZERO;
    private Vec3 center = Vec3.ZERO;
    private long started;
    private int age;
    private int activeTicks;
    private int round;
    private int roundStart;
    private int warningTicks;
    private int fadeTicks;
    private int recoveryTicks;
    private int beamCount;
    private int nextBeam;
    private boolean busy;

    public QizaiLaserBarrage(EasterBossEntity boss) { this.boss = boss; }
    public boolean busy() { return busy; }

    public boolean begin(ServerLevel level, LivingEntity victim) {
        if (busy || level != boss.level() || !(victim instanceof ServerPlayer player) || !combat(player)) return false;
        preferredTarget = player.getUUID();
        lastTarget = player.getBoundingBox().getCenter();
        center = boss.position();
        started = level.getGameTime();
        age = 0;
        nextBeam = 0;
        round = 0;
        roundStart = 0;
        beams.clear();
        activeTicks = Math.max(1, boss.getChallengeDifficulty().acceleratedTicks(QizaiLaserBarrageRules.ACTIVE_TICKS));
        warningTicks = Math.max(1, boss.getChallengeDifficulty().acceleratedTicks(QizaiLaserBarrageRules.WARNING_TICKS));
        fadeTicks = Math.max(1, boss.getChallengeDifficulty().acceleratedTicks(QizaiLaserBarrageRules.BEAM_FADE));
        recoveryTicks = Math.max(0, boss.getChallengeDifficulty().acceleratedTicks(QizaiLaserBarrageRules.RECOVERY));
        beamCount = QizaiLaserBarrageRules.roundBeamCount(round);
        busy = true;
        boss.getNavigation().stop();
        boss.playCast(totalTicks(), 2);
        net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem.animate(boss, true);
        sync();
        level.playSound(null, boss.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.3F, 1.45F);
        announceRound(level);
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} start: rounds={}, beams={}, active={}, warning={}, bossY={}",
                QizaiLaserBarrageRules.NAME, QizaiLaserBarrageRules.ROUNDS, beamCount, activeTicks, warningTicks, boss.getY());
        return true;
    }

    public boolean tick(ServerLevel level) {
        if (!busy) return false;
        if (level != boss.level() || !boss.isAlive() || boss.isRemoved()) { cancel(); return false; }
        List<ServerPlayer> targets = targets(level);
        boss.getNavigation().stop();
        if (!targets.isEmpty()) {
            ServerPlayer focus = preferred(level, targets);
            preferredTarget = focus.getUUID();
            lastTarget = focus.getBoundingBox().getCenter();
        }
        boss.getLookControl().setLookAt(lastTarget.x, lastTarget.y, lastTarget.z, 90.0F, 90.0F);
        age++;
        boolean changed = false;
        if (round + 1 < QizaiLaserBarrageRules.ROUNDS && age >= roundStart + roundTicks()) {
            round++;
            roundStart += roundTicks();
            nextBeam = 0;
            beams.clear();
            beamCount = QizaiLaserBarrageRules.roundBeamCount(round);
            announceRound(level);
            changed = true;
        }
        while (nextBeam < beamCount && age >= spawnTick(nextBeam)) {
            spawn(level, targets, nextBeam++);
            changed = true;
        }
        for (int index = 0; index < beams.size(); index++) {
            Beam beam = beams.get(index);
            if (!beam.fired && age >= beam.fireTick) {
                fire(level, beam, index);
                changed = true;
            }
        }
        if (age >= totalTicks()) {
            FTBossMod.LOGGER.info("[FTBoss][Qizai] {} complete: spawned={}, targeted={}", QizaiLaserBarrageRules.NAME,
                    beams.size(), beams.stream().filter(beam -> beam.targeted).count());
            cancel();
            return false;
        }
        if (changed) sync();
        return true;
    }

    private void spawn(ServerLevel level, List<ServerPlayer> targets, int index) {
        boolean targeted = QizaiLaserBarrageRules.targetsPlayer(index);
        Vec3 origin = randomOrigin(level);
        Vec3 end;
        if (targeted) {
            if (!targets.isEmpty()) {
                ServerPlayer target = preferred(level, targets);
                preferredTarget = target.getUUID();
                lastTarget = target.getBoundingBox().getCenter();
                target.displayClientMessage(Component.literal("§c【万棱星狱】锁定光束 —— 离开红白预警线！"), true);
            }
            end = clip(level, origin, lastTarget);
        } else {
            end = randomDirection(level, origin);
        }
        int spawnTick = spawnTick(index);
        Beam beam = new Beam(origin, end, spawnTick, spawnTick + warningTicks, targeted);
        beams.add(beam);
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE,
                targeted ? 0.9F : 0.45F, targeted ? 1.85F : 1.25F + (index % 5) * 0.08F);
    }

    private void fire(ServerLevel level, Beam beam, int index) {
        beam.fired = true;
        beam.end = clip(level, beam.origin, beam.end);
        int hits = 0;
        AABB corridor = new AABB(beam.origin, beam.end).inflate(QizaiLaserBarrageRules.BEAM_RADIUS + 0.5);
        for (Player player : level.getEntitiesOfClass(Player.class, corridor, this::combat)) {
            if (!QizaiLaserBarrageRules.intersects(player.getBoundingBox(), beam.origin, beam.end)) continue;
            int previousInvulnerability = player.invulnerableTime;
            player.invulnerableTime = 0;
            try {
                if (player.hurt(level.damageSources().indirectMagic(boss, boss), QizaiLaserBarrageRules.DAMAGE)) hits++;
            } finally {
                player.invulnerableTime = Math.max(previousInvulnerability, player.invulnerableTime);
            }
        }
        int steps = Math.min(42, Math.max(2, (int) Math.ceil(beam.origin.distanceTo(beam.end))));
        for (int step = 0; step <= steps; step++) {
            Vec3 point = beam.origin.lerp(beam.end, step / (double) steps);
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, beam.end.x, beam.end.y, beam.end.z, 8, 0.16, 0.16, 0.16, 0.04);
        level.playSound(null, beam.origin.x, beam.origin.y, beam.origin.z, SoundEvents.GUARDIAN_ATTACK,
                SoundSource.HOSTILE, beam.targeted ? 1.05F : 0.7F, 1.2F + (index % 7) * 0.05F);
        FTBossMod.LOGGER.debug("[FTBoss][Qizai] {} beam {}/{}: targeted={}, hits={}",
                QizaiLaserBarrageRules.NAME, index + 1, beamCount, beam.targeted, hits);
    }

    private Vec3 randomOrigin(ServerLevel level) {
        for (int attempt = 0; attempt < QizaiLaserBarrageRules.ANCHOR_ATTEMPTS; attempt++) {
            double angle = boss.getRandom().nextDouble() * Math.PI * 2;
            double radius = Math.sqrt(boss.getRandom().nextDouble()) * QizaiLaserBarrageRules.FIELD_RADIUS;
            double height = QizaiLaserBarrageRules.MIN_ANCHOR_HEIGHT + boss.getRandom().nextDouble()
                    * (QizaiLaserBarrageRules.MAX_ANCHOR_HEIGHT - QizaiLaserBarrageRules.MIN_ANCHOR_HEIGHT);
            Vec3 candidate = center.add(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            BlockPos block = BlockPos.containing(candidate);
            AABB space = new AABB(candidate.x - 0.28, candidate.y - 0.28, candidate.z - 0.28,
                    candidate.x + 0.28, candidate.y + 0.28, candidate.z + 0.28);
            if (level.hasChunkAt(block) && level.getWorldBorder().isWithinBounds(block) && level.noCollision(space)) return candidate;
        }
        double angle = boss.getRandom().nextDouble() * Math.PI * 2;
        return center.add(Math.cos(angle) * QizaiLaserBarrageRules.FIELD_RADIUS * 0.5,
                (QizaiLaserBarrageRules.MIN_ANCHOR_HEIGHT + QizaiLaserBarrageRules.MAX_ANCHOR_HEIGHT) * 0.5,
                Math.sin(angle) * QizaiLaserBarrageRules.FIELD_RADIUS * 0.5);
    }

    private Vec3 randomDirection(ServerLevel level, Vec3 origin) {
        Vec3 fallback = origin.add(0, -QizaiLaserBarrageRules.BEAM_LENGTH, 0);
        for (int attempt = 0; attempt < QizaiLaserBarrageRules.DIRECTION_ATTEMPTS; attempt++) {
            double yaw = boss.getRandom().nextDouble() * Math.PI * 2;
            double vertical = -0.65 + boss.getRandom().nextDouble() * 0.9;
            double horizontal = Math.sqrt(Math.max(0, 1 - vertical * vertical));
            Vec3 direction = new Vec3(Math.cos(yaw) * horizontal, vertical, Math.sin(yaw) * horizontal);
            Vec3 end = clip(level, origin, origin.add(direction.scale(QizaiLaserBarrageRules.BEAM_LENGTH)));
            fallback = end;
            if (origin.distanceToSqr(end) >= 64) return end;
        }
        return fallback;
    }

    private Vec3 clip(ServerLevel level, Vec3 origin, Vec3 end) {
        return level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss)).getLocation();
    }
    private List<ServerPlayer> targets(ServerLevel level) {
        return level.players().stream().filter(this::combat)
                .sorted(Comparator.comparingDouble(boss::distanceToSqr)).toList();
    }
    private ServerPlayer preferred(ServerLevel level, List<ServerPlayer> targets) {
        Player preferred = preferredTarget == null ? null : level.getPlayerByUUID(preferredTarget);
        return preferred instanceof ServerPlayer serverPlayer && targets.contains(serverPlayer) ? serverPlayer
                : targets.get(boss.getRandom().nextInt(targets.size()));
    }
    private boolean combat(Player player) {
        return player != null && player.level() == boss.level() && player.isAlive() && !player.isRemoved()
                && !player.isCreative() && !player.isSpectator() && !boss.isAlliedTo(player) && !player.isAlliedTo(boss)
                && player.distanceToSqr(boss) <= QizaiLaserBarrageRules.RANGE * QizaiLaserBarrageRules.RANGE;
    }
    private int totalTicks() {
        return QizaiLaserBarrageRules.castDuration(activeTicks, warningTicks, fadeTicks, recoveryTicks);
    }
    private int roundTicks() {
        return QizaiLaserBarrageRules.roundDuration(activeTicks, warningTicks, fadeTicks);
    }
    private int spawnTick(int index) {
        return roundStart + QizaiLaserBarrageRules.roundSpawnTick(index, activeTicks, beamCount);
    }
    private void announceRound(ServerLevel level) {
        announce(level, "§d【万棱星狱】§e第 " + (round + 1) + "/" + QizaiLaserBarrageRules.ROUNDS
                + " 轮 · " + beamCount + " 束激光 §c—— 躲开锁定预警！");
    }

    private void sync() {
        CompoundTag cue = new CompoundTag();
        cue.putInt("qizai", QizaiRules.LASER_BARRAGE);
        cue.putLong("start", started);
        cue.putInt("duration", totalTicks());
        cue.putInt("activeTicks", activeTicks);
        cue.putInt("round", round);
        cue.putInt("roundStart", roundStart);
        cue.putInt("beamCount", beamCount);
        cue.putInt("warningTicks", warningTicks);
        cue.putInt("fadeTicks", fadeTicks);
        cue.putBoolean("released", age >= roundStart + warningTicks + 1);
        putPoint(cue, "center", center);
        ListTag list = new ListTag();
        for (Beam beam : beams) {
            CompoundTag entry = new CompoundTag();
            putPoint(entry, "", beam.origin);
            putPoint(entry, "end", beam.end);
            entry.putInt("spawn", beam.spawnTick);
            entry.putInt("fire", beam.fireTick);
            entry.putBoolean("targeted", beam.targeted);
            entry.putBoolean("fired", beam.fired);
            list.add(entry);
        }
        cue.put("beams", list);
        boss.setQizaiCue(cue);
    }
    private static void putPoint(CompoundTag tag, String prefix, Vec3 point) {
        tag.putDouble(prefix + "X", point.x);
        tag.putDouble(prefix + "Y", point.y);
        tag.putDouble(prefix + "Z", point.z);
    }
    private void announce(ServerLevel level, String text) {
        for (ServerPlayer player : level.players()) if (player.distanceToSqr(boss) <= QizaiLaserBarrageRules.RANGE * QizaiLaserBarrageRules.RANGE) {
            player.displayClientMessage(Component.literal(text), true);
        }
    }
    public void cancel() {
        if (!busy) return;
        busy = false;
        preferredTarget = null;
        lastTarget = Vec3.ZERO;
        beams.clear();
        age = 0;
        nextBeam = 0;
        round = 0;
        roundStart = 0;
        net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem.animate(boss, false);
        boss.setQizaiCue(new CompoundTag());
    }
}
