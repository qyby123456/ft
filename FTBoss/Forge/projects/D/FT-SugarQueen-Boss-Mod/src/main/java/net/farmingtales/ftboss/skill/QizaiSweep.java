package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.util.QizaiRules;
import net.farmingtales.ftboss.util.QizaiSweepRules;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

public final class QizaiSweep {
    private static final int IDLE = 0;
    private static final int WINDUP = 1;
    private static final int SWEEP = 2;
    private static final int RAIN = 3;
    private static final int RECOVERY = 4;
    private final EasterBossEntity boss;
    private final Map<UUID, Integer> hitCooldown = new HashMap<>();
    private final List<Star> volley = new ArrayList<>();
    private record Star(QizaiStarEntity entity, UUID target, int age) {}
    private int phase;
    private int ticks;
    private double angle;
    private Vec3 anchor = Vec3.ZERO;
    private int elapsed;
    private int volleyTimer;
    private int wave;
    private int rainTimer;
    private long windupStarted;
    private int windupDuration;
    private int roundsLeft;
    /**
     * ★ 2026-09-21 用户要求「终结难度下祁崽法术施法速度翻倍，cd减半」，并选择"补齐例外" ——
     * 本招的**扫射/星雨演出时长**原先也不吃难度，现在也过。
     *
     * <p>关键取舍：{@link QizaiSweepRules#angle} 是"每 tick 转固定角度"的公式（客户端渲染复用同一个函数），
     * 所以直接砍半时长会让光束只扫半圈 —— 那是"演出变弱"，不是"演出变快"。
     * 因此这里**保持总转角和星雨波数不变**，改成把每帧的 {@code elapsed} 乘一个压缩倍率
     * （{@link #sweepSpeed}，终结难度 = 2）：整场演出快一倍，轨迹与观感完全一致。
     */
    private int sweepTicks = QizaiSweepRules.SWEEP_TICKS;
    private int rainGap = QizaiSweepRules.RAIN_GAP;
    private int volleyInterval = QizaiSweepRules.VOLLEY_INTERVAL;
    private int sweepSpeed = 1;

    public QizaiSweep(EasterBossEntity boss) { this.boss = boss; }

    public boolean busy() { return phase != IDLE; }

    public boolean begin(ServerLevel level, LivingEntity target) {
        if (busy() || !(target instanceof Player player) || !combat(player) || level != boss.level()) return false;
        phase = WINDUP;
        ticks = boss.getChallengeDifficulty().acceleratedTicks(QizaiSweepRules.WINDUP);
        windupStarted = level.getGameTime();
        windupDuration = ticks;
        anchor = boss.position();
        angle = Math.toDegrees(Math.atan2(target.getZ() - anchor.z, target.getX() - anchor.x));
        elapsed = 0;
        volleyTimer = 0;
        wave = 0;
        rainTimer = 0;
        roundsLeft = QizaiRules.RELEASES;
        hitCooldown.clear();
        clearStars();
        /* ★ 2026-09-21: 扫射 / 星雨间隔 / 星矢轮次全部过难度；压缩倍率让"同样是转满一圈"只花一半时间。 */
        this.sweepTicks = Math.max(1, boss.getChallengeDifficulty().acceleratedTicks(QizaiSweepRules.SWEEP_TICKS));
        this.rainGap = Math.max(1, boss.getChallengeDifficulty().acceleratedTicks(QizaiSweepRules.RAIN_GAP));
        this.volleyInterval = Math.max(1, boss.getChallengeDifficulty().acceleratedTicks(QizaiSweepRules.VOLLEY_INTERVAL));
        this.sweepSpeed = Math.max(1, Math.round((float) QizaiSweepRules.SWEEP_TICKS / this.sweepTicks));
        int total = ticks + QizaiRules.RELEASES * (sweepTicks
                + 1 + (QizaiSweepRules.RAIN_WAVES - 1) * rainGap) + QizaiSweepRules.RECOVERY;
        boss.getNavigation().stop();
        boss.playCast(total, 0);
        net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem.animate(boss, true);
        beams(level, false, 0, 0);
        level.playSound(null, boss.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.1F, 0.7F);
        announce(level, "§d【绯星指针】§7红束跳过，蓝束落地躲；跟随箭头绕行。");
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} start: anchor={}, rounds={}, windup={}", QizaiSweepRules.NAME, anchor, roundsLeft, ticks);
        return true;
    }

    public boolean tick(ServerLevel level) {
        if (phase == IDLE) return false;
        if (level != boss.level() || !boss.isAlive() || boss.isRemoved() || boss.isNoAi()) { cancel(); return false; }
        boss.getNavigation().stop();
        hitCooldown.replaceAll((identifier, remaining) -> remaining - 1);
        hitCooldown.values().removeIf(remaining -> remaining <= 0);
        steerVolley(level);
        switch (phase) {
            case WINDUP -> {
                beams(level, false, 0, 0);
                if (--ticks <= 0) startSweep(level);
            }
            case SWEEP -> sweep(level);
            case RAIN -> rain(level);
            case RECOVERY -> {
                if (--ticks <= 0) {
                    FTBossMod.LOGGER.info("[FTBoss][Qizai] {} complete: {} rounds", QizaiSweepRules.NAME, QizaiRules.RELEASES);
                    cancel();
                }
            }
            default -> cancel();
        }
        return phase != IDLE;
    }

    private int round() { return QizaiRules.RELEASES - roundsLeft; }

    private void startSweep(ServerLevel level) {
        phase = SWEEP;
        ticks = sweepTicks;
        elapsed = 0;
        volleyTimer = 0;
        level.playSound(null, boss.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.2F, 1.2F);
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} round={} arms={}", QizaiSweepRules.NAME, round() + 1, QizaiSweepRules.arms(round()));
    }

    private void sweep(ServerLevel level) {
        /* elapsed 传"生效后的帧数"（× 压缩倍率）：命中判定与客户端画出来的束保持同一个角度。 */
        beams(level, true, round(), ++elapsed * sweepSpeed);
        if (volleyTimer-- <= 0) {
            volleyTimer = volleyInterval - 1;
            fireVolley(level);
        }
        if (--ticks <= 0) {
            phase = RAIN;
            wave = 0;
            rainTimer = 0;
            windupStarted = level.getGameTime();
            windupDuration = 1 + (QizaiSweepRules.RAIN_WAVES - 1) * rainGap;
            boss.setQizaiCue(new CompoundTag());
        }
    }

    private void beams(ServerLevel level, boolean firing, int round, int elapsed) {
        ListTag lengths = new ListTag();
        for (int arm = 0; arm < QizaiSweepRules.arms(round); arm++) {
            double heading = Math.toRadians(QizaiSweepRules.angle(angle, elapsed, round, arm));
            Vec3 origin = anchor.add(0, QizaiSweepRules.height(round, arm), 0);
            Vec3 end = origin.add(Math.cos(heading) * QizaiSweepRules.RADIUS, 0, Math.sin(heading) * QizaiSweepRules.RADIUS);
            end = level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss)).getLocation();
            double length = origin.distanceTo(end);
            Player first = null;
            if (firing) for (Player player : level.players()) {
                if (!combat(player) || player.distanceToSqr(anchor) > 40 * 40) continue;
                var box = player.getBoundingBox().inflate(QizaiSweepRules.BEAM_HALF_WIDTH, 0, QizaiSweepRules.BEAM_HALF_WIDTH);
                var hit = box.contains(origin) ? java.util.Optional.of(origin) : box.clip(origin, end);
                if (hit.isPresent() && origin.distanceTo(hit.get()) <= length) {
                    length = origin.distanceTo(hit.get());
                    first = player;
                }
            }
            lengths.add(DoubleTag.valueOf(length));
            if (first != null && hitCooldown.getOrDefault(first.getUUID(), 0) <= 0) {
                hitCooldown.put(first.getUUID(), QizaiSweepRules.BEAM_HIT_COOLDOWN);
                first.invulnerableTime = 0;
                first.hurt(level.damageSources().indirectMagic(boss, boss), QizaiSweepRules.BEAM_DAMAGE);
                first.hurtMarked = true;
            }
        }
        CompoundTag cue = new CompoundTag();
        cue.putInt("qizai", QizaiRules.SWEEP);
        cue.putLong("start", level.getGameTime());
        cue.putInt("duration", 4);
        cue.putBoolean("released", firing);
        cue.putDouble("angle", QizaiSweepRules.angle(angle, elapsed, round, 0));
        cue.putDouble("initialAngle", angle);
        cue.putInt("elapsed", elapsed);
        cue.putInt("round", round);
        cue.putDouble("anchorX", anchor.x);
        cue.putDouble("anchorY", anchor.y);
        cue.putDouble("anchorZ", anchor.z);
        cue.put("lengths", lengths);
        cue.putLong("windupStart", windupStarted);
        cue.putInt("windupDuration", windupDuration);
        boss.setQizaiCue(cue);
    }

    private void fireVolley(ServerLevel level) {
        List<Player> targets = level.players().stream().filter(this::combat)
                .filter(player -> player.distanceToSqr(anchor) <= QizaiSweepRules.RADIUS * QizaiSweepRules.RADIUS * 4)
                .sorted(java.util.Comparator.comparingDouble(player -> player.distanceToSqr(anchor))).map(player -> (Player) player).toList();
        if (targets.isEmpty()) return;
        for (int index = 0; index < QizaiSweepRules.VOLLEY_STARS; index++) {
            double heading = Math.toRadians(index * 360.0 / QizaiSweepRules.VOLLEY_STARS + elapsed * sweepSpeed);
            Vec3 direction = new Vec3(Math.cos(heading), 0, Math.sin(heading));
            spawn(level, anchor.add(direction.scale(1.4)).add(0, 1.6, 0), direction.scale(QizaiSweepRules.VOLLEY_SPEED),
                    targets.get(index % targets.size()).getUUID());
        }
        level.playSound(null, boss.blockPosition(), SoundEvents.SHULKER_SHOOT, SoundSource.HOSTILE, 0.8F, 1.5F);
    }

    private void rain(ServerLevel level) {
        if (roundsLeft > 1) beams(level, false, round() + 1, 0);
        if (rainTimer-- > 0) return;
        rainTimer = rainGap - 1;
        double offset = angle + wave * QizaiSweepRules.RAIN_ROTATION_PER_WAVE;
        for (int index = 0; index < QizaiSweepRules.RAIN_COUNT; index++) {
            double heading = Math.toRadians(offset + index * 360.0 / QizaiSweepRules.RAIN_COUNT);
            spawn(level, anchor.add(0, 1.4, 0), new Vec3(Math.cos(heading), 0, Math.sin(heading)).scale(QizaiSweepRules.RAIN_SPEED), null);
        }
        level.playSound(null, boss.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.0F, 1.2F);
        SpellVisuals.show(level, boss, QizaiSweepRules.VISUAL_RING_ID, Phase.RELEASE, anchor);
        if (++wave >= QizaiSweepRules.RAIN_WAVES) {
            if (--roundsLeft > 0) startSweep(level);
            else {
                phase = RECOVERY;
                ticks = QizaiSweepRules.RECOVERY;
                boss.setQizaiCue(new CompoundTag());
            }
        }
    }

    private void spawn(ServerLevel level, Vec3 origin, Vec3 velocity, UUID target) {
        var star = new QizaiStarEntity(level, boss, origin);
        star.setDeltaMovement(velocity);
        if (level.addFreshEntity(star)) volley.add(new Star(star, target, 0));
    }

    private void steerVolley(ServerLevel level) {
        for (var iterator = volley.listIterator(); iterator.hasNext();) {
            Star star = iterator.next();
            int age = star.age() + 1;
            int life = star.target() == null ? QizaiRules.STAR_LIFETIME : QizaiSweepRules.VOLLEY_LIFETIME;
            if (!star.entity().isAlive() || star.entity().isRemoved() || age >= life) {
                star.entity().discard();
                iterator.remove();
                continue;
            }
            iterator.set(new Star(star.entity(), star.target(), age));
            if (star.target() == null) continue;
            Player target = level.getPlayerByUUID(star.target());
            if (target != null && combat(target)) star.entity().setDeltaMovement(QizaiSweepRules.turn(star.entity().getDeltaMovement(),
                    target.getBoundingBox().getCenter().subtract(star.entity().position())));
        }
    }

    private void clearStars() {
        for (Star star : volley) star.entity().discard();
        volley.clear();
    }

    public void cancel() {
        phase = IDLE;
        ticks = 0;
        roundsLeft = 0;
        clearStars();
        hitCooldown.clear();
        net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem.animate(boss, false);
        boss.setQizaiCue(new CompoundTag());
    }

    private boolean combat(Player player) {
        return player.isAlive() && !player.isRemoved() && !player.isCreative() && !player.isSpectator() && player.level() == boss.level();
    }

    private void announce(ServerLevel level, String message) {
        for (Player player : level.players()) {
            if (player.distanceToSqr(anchor) <= 64 * 64) player.displayClientMessage(Component.literal(message), true);
        }
    }
}
