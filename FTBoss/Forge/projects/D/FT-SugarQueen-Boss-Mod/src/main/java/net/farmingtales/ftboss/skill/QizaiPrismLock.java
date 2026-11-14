package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QizaiPrismRules;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Grounded multi-angle tracking, lock-on and beam attack for Qizai. */
public final class QizaiPrismLock {
    private static final class Beam {
        final Vec3 origin;
        Vec3 end;
        Beam(Vec3 origin, Vec3 end) { this.origin = origin; this.end = end; }
    }
    private final EasterBossEntity boss;
    private final List<Beam> beams = new ArrayList<>();
    private UUID targetId;
    private Vec3 lockedPoint = Vec3.ZERO;
    private long started;
    private int age;
    private int trackTicks;
    private int lockTicks;
    private int fireGap;
    private int recoveryTicks;
    private boolean locked;

    public QizaiPrismLock(EasterBossEntity boss) { this.boss = boss; }
    public boolean busy() { return targetId != null; }
    public boolean locked() { return busy() && locked; }

    public boolean begin(ServerLevel level, LivingEntity victim) {
        if (busy() || level != boss.level() || !(victim instanceof ServerPlayer player) || !combat(player)) return false;
        targetId = player.getUUID();
        lockedPoint = player.getBoundingBox().getCenter();
        started = level.getGameTime();
        age = 0;
        locked = false;
        trackTicks = boss.getChallengeDifficulty().acceleratedTicks(QizaiPrismRules.TRACK_TICKS);
        lockTicks = boss.getChallengeDifficulty().acceleratedTicks(QizaiPrismRules.LOCK_TICKS);
        fireGap = Math.max(1, boss.getChallengeDifficulty().acceleratedTicks(QizaiPrismRules.FIRE_GAP));
        recoveryTicks = boss.getChallengeDifficulty().acceleratedTicks(QizaiPrismRules.RECOVERY);
        beams.clear();
        for (int index = 0; index < QizaiPrismRules.BEAMS; index++) {
            beams.add(new Beam(randomAnchor(level, lockedPoint, index), lockedPoint));
        }
        boss.getNavigation().stop();
        boss.playCast(totalTicks(), 2);
        net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem.animate(boss, true);
        sync();
        level.playSound(null, boss.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.2F, 1.35F);
        announce(level, "§d【绯星锁裁】§7六面晶棱正在追踪 —— §e锁定提示后立刻横移！");
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} start: target={}, beams={}, track={}, lock={}, bossY={}",
                QizaiPrismRules.NAME, player.getName().getString(), beams.size(), trackTicks, lockTicks, boss.getY());
        return true;
    }

    public boolean tick(ServerLevel level) {
        if (!busy()) return false;
        if (level != boss.level() || !boss.isAlive() || boss.isRemoved() || boss.isNoAi()) { cancel(); return false; }
        boss.getNavigation().stop();
        age++;
        if (!locked) {
            Player target = level.getPlayerByUUID(targetId);
            if (target == null || !combat(target)) { cancel(); return false; }
            lockedPoint = target.getBoundingBox().getCenter();
            for (Beam beam : beams) beam.end = lockedPoint;
            boss.getLookControl().setLookAt(lockedPoint.x, lockedPoint.y, lockedPoint.z, 90.0F, 90.0F);
            if (age >= trackTicks) lock(level, target);
        }
        for (int index = 0; index < beams.size(); index++) {
            if (age == QizaiPrismRules.fireTick(index, trackTicks, lockTicks, fireGap)) fire(level, index);
        }
        if (age >= totalTicks()) {
            FTBossMod.LOGGER.info("[FTBoss][Qizai] {} complete: beams={}, locked={}", QizaiPrismRules.NAME, beams.size(), lockedPoint);
            cancel();
            return false;
        }
        sync();
        return true;
    }

    private void lock(ServerLevel level, Player target) {
        locked = true;
        lockedPoint = target.getBoundingBox().getCenter();
        for (Beam beam : beams) beam.end = clip(level, beam.origin, lockedPoint);
        sync();
        level.playSound(null, lockedPoint.x, lockedPoint.y, lockedPoint.z,
                SoundEvents.NOTE_BLOCK_PLING.get(), SoundSource.HOSTILE, 1.4F, 1.9F);
        target.displayClientMessage(Component.literal("§c【绯星锁裁】锁定完成 —— 横移躲开交叉火线！"), true);
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} locked: target={}, point={}",
                QizaiPrismRules.NAME, target.getName().getString(), lockedPoint);
    }

    private void fire(ServerLevel level, int index) {
        Beam beam = beams.get(index);
        Vec3 end = clip(level, beam.origin, beam.end);
        beam.end = end;
        int hits = 0;
        AABB corridor = new AABB(beam.origin, end).inflate(QizaiPrismRules.BEAM_RADIUS + 0.5);
        for (Player player : level.getEntitiesOfClass(Player.class, corridor, this::combat)) {
            if (!QizaiPrismRules.intersects(player.getBoundingBox(), beam.origin, end)) continue;
            int previousInvulnerability = player.invulnerableTime;
            player.invulnerableTime = 0;
            try {
                if (player.hurt(level.damageSources().indirectMagic(boss, boss), QizaiPrismRules.DAMAGE)) hits++;
            } finally {
                player.invulnerableTime = Math.max(previousInvulnerability, player.invulnerableTime);
            }
        }
        int steps = Math.min(48, Math.max(2, (int) Math.ceil(beam.origin.distanceTo(end) * 1.5)));
        for (int step = 0; step <= steps; step++) {
            Vec3 point = beam.origin.lerp(end, step / (double) steps);
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, end.x, end.y, end.z, 12, 0.18, 0.18, 0.18, 0.05);
        level.playSound(null, beam.origin.x, beam.origin.y, beam.origin.z,
                SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE, 1.0F, 1.25F + index * 0.07F);
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} beam {}/{}: hits={}, from={}, to={}",
                QizaiPrismRules.NAME, index + 1, beams.size(), hits, beam.origin, end);
    }

    private Vec3 randomAnchor(ServerLevel level, Vec3 target, int index) {
        double sector = index * Math.PI * 2.0 / QizaiPrismRules.BEAMS;
        for (int attempt = 0; attempt < QizaiPrismRules.ANCHOR_ATTEMPTS; attempt++) {
            double angle = sector + (boss.getRandom().nextDouble() - 0.5) * Math.PI / 2.4;
            double radius = QizaiPrismRules.MIN_ANCHOR_RADIUS + boss.getRandom().nextDouble()
                    * (QizaiPrismRules.MAX_ANCHOR_RADIUS - QizaiPrismRules.MIN_ANCHOR_RADIUS);
            double height = QizaiPrismRules.MIN_ANCHOR_HEIGHT + boss.getRandom().nextDouble()
                    * (QizaiPrismRules.MAX_ANCHOR_HEIGHT - QizaiPrismRules.MIN_ANCHOR_HEIGHT);
            Vec3 candidate = target.add(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            BlockPos block = BlockPos.containing(candidate);
            AABB space = new AABB(candidate.x - 0.3, candidate.y - 0.3, candidate.z - 0.3,
                    candidate.x + 0.3, candidate.y + 0.3, candidate.z + 0.3);
            if (level.hasChunkAt(block) && level.getWorldBorder().isWithinBounds(block) && level.noCollision(space)
                    && level.clip(new ClipContext(candidate, target, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, boss)).getType() == HitResult.Type.MISS) return candidate;
        }
        double angle = sector + (boss.getRandom().nextDouble() - 0.5) * 0.35;
        double radius = (QizaiPrismRules.MIN_ANCHOR_RADIUS + QizaiPrismRules.MAX_ANCHOR_RADIUS) * 0.5;
        double height = QizaiPrismRules.MIN_ANCHOR_HEIGHT
                + (QizaiPrismRules.MAX_ANCHOR_HEIGHT - QizaiPrismRules.MIN_ANCHOR_HEIGHT) * (index + 0.5) / QizaiPrismRules.BEAMS;
        return target.add(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private Vec3 clip(ServerLevel level, Vec3 origin, Vec3 end) {
        return level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss)).getLocation();
    }
    private boolean combat(Player player) {
        return player != null && player.level() == boss.level() && player.isAlive() && !player.isRemoved()
                && !player.isCreative() && !player.isSpectator() && !boss.isAlliedTo(player) && !player.isAlliedTo(boss)
                && player.distanceToSqr(boss) <= QizaiPrismRules.RANGE * QizaiPrismRules.RANGE;
    }
    private int totalTicks() { return QizaiPrismRules.duration(trackTicks, lockTicks, fireGap, recoveryTicks); }

    private void sync() {
        CompoundTag cue = new CompoundTag();
        cue.putInt("qizai", QizaiRules.PRISM_LOCK);
        cue.putLong("start", started);
        cue.putInt("duration", totalTicks());
        cue.putInt("trackTicks", trackTicks);
        cue.putInt("lockTicks", lockTicks);
        cue.putInt("fireGap", fireGap);
        cue.putInt("fadeTicks", QizaiPrismRules.BEAM_FADE);
        cue.putBoolean("locked", locked);
        cue.putBoolean("released", age >= trackTicks + lockTicks);
        putPoint(cue, "target", lockedPoint);
        ListTag list = new ListTag();
        for (Beam beam : beams) {
            CompoundTag entry = new CompoundTag();
            putPoint(entry, "", beam.origin);
            putPoint(entry, "end", beam.end);
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
        for (ServerPlayer player : level.players()) if (player.distanceToSqr(boss) <= QizaiPrismRules.RANGE * QizaiPrismRules.RANGE) {
            player.displayClientMessage(Component.literal(text), true);
        }
    }
    public void cancel() {
        if (!busy()) return;
        targetId = null;
        beams.clear();
        age = 0;
        locked = false;
        net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem.animate(boss, false);
        boss.setQizaiCue(new CompoundTag());
    }
}
