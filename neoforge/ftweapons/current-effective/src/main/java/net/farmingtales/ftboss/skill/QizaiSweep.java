package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem;
import net.farmingtales.ftboss.skill.qizaicross.CompletedCue;
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
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class QizaiSweep {
    private static final int IDLE = 0;
    private static final int WINDUP = 1;
    private static final int SWEEP = 2;
    private static final int RAIN = 3;
    private static final int RECOVERY = 4;
    private final EasterBossEntity boss;
    private final Map<UUID, Integer> hitCooldown = new HashMap<>();
    private final List<QizaiSweep.Star> volley = new ArrayList<>();
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
    private int sweepTicks = 100;
    private int rainGap = 10;
    private int volleyInterval = 15;
    private int sweepSpeed = 1;

    public QizaiSweep(EasterBossEntity var1) {
        this.boss = var1;
    }

    public boolean busy() {
        return this.phase != 0;
    }

    public boolean begin(ServerLevel var1, LivingEntity var2) {
        if (!this.busy() && (Object)var2 instanceof LivingEntity var3 && this.combat(var3) && var1 == this.boss.level()) {
            this.phase = 1;
            this.ticks = this.boss.getChallengeDifficulty().acceleratedTicks(30);
            this.windupStarted = var1.getGameTime();
            this.windupDuration = this.ticks;
            this.anchor = this.boss.position();
            this.angle = Math.toDegrees(Math.atan2(var2.getZ() - this.anchor.z, var2.getX() - this.anchor.x));
            this.elapsed = 0;
            this.volleyTimer = 0;
            this.wave = 0;
            this.rainTimer = 0;
            this.roundsLeft = 3;
            this.hitCooldown.clear();
            this.clearStars();
            this.sweepTicks = Math.max(1, this.boss.getChallengeDifficulty().acceleratedTicks(100));
            this.rainGap = Math.max(1, this.boss.getChallengeDifficulty().acceleratedTicks(10));
            this.volleyInterval = Math.max(1, this.boss.getChallengeDifficulty().acceleratedTicks(15));
            this.sweepSpeed = Math.max(1, Math.round(100.0F / (float)this.sweepTicks));
            int var4 = this.ticks + 3 * (this.sweepTicks + 1 + 4 * this.rainGap) + 24;
            this.boss.getNavigation().stop();
            this.boss.playCast(var4, 0);
            QizaiPrayerStaffItem.animate(this.boss, true);
            this.beams(var1, false, 0, 0);
            var1.playSound(null, this.boss.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.1F, 0.7F);
            this.announce(var1, "§d【绯星指针】§7红束跳过，蓝束落地躲；跟随箭头绕行。");
            FTBossMod.LOGGER.info("[FTBoss][Qizai] {} start: anchor={}, rounds={}, windup={}", "绯星指针", this.anchor, this.roundsLeft, this.ticks);
            return true;
        }

        return false;
    }

    public boolean tick(ServerLevel var1) {
        if (this.phase == 0) {
            CompletedCue.sweep(var1, this.boss, this.phase, this.windupStarted, this.windupDuration, this.anchor, this.wave, this.sweepTicks, this.ticks);
            return false;
        } else if (var1 == this.boss.level() && this.boss.isAlive() && !this.boss.isRemoved() && !this.boss.isNoAi()) {
            this.boss.getNavigation().stop();
            this.hitCooldown.replaceAll((var0, var1x) -> var1x - 1);
            this.hitCooldown.values().removeIf(var0 -> var0 <= 0);
            this.steerVolley(var1);
            switch (this.phase) {
                case 1:
                    this.beams(var1, false, 0, 0);
                    if (--this.ticks <= 0) {
                        this.startSweep(var1);
                    }
                    break;
                case 2:
                    this.sweep(var1);
                    break;
                case 3:
                    this.rain(var1);
                    break;
                case 4:
                    if (--this.ticks <= 0) {
                        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} complete: {} rounds", "绯星指针", 3);
                        this.cancel();
                    }
                    break;
                default:
                    this.cancel();
            }

            boolean var10000 = this.phase != 0;
            CompletedCue.sweep(var1, this.boss, this.phase, this.windupStarted, this.windupDuration, this.anchor, this.wave, this.sweepTicks, this.ticks);
            return var10000;
        } else {
            this.cancel();
            CompletedCue.sweep(var1, this.boss, this.phase, this.windupStarted, this.windupDuration, this.anchor, this.wave, this.sweepTicks, this.ticks);
            return false;
        }
    }

    private int round() {
        return 3 - this.roundsLeft;
    }

    private void startSweep(ServerLevel var1) {
        this.phase = 2;
        this.ticks = this.sweepTicks;
        this.elapsed = 0;
        this.volleyTimer = 0;
        var1.playSound(null, this.boss.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.2F, 1.2F);
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} round={} arms={}", "绯星指针", this.round() + 1, QizaiSweepRules.arms(this.round()));
    }

    private void sweep(ServerLevel var1) {
        this.beams(var1, true, this.round(), ++this.elapsed * this.sweepSpeed);
        if (this.volleyTimer-- <= 0) {
            this.volleyTimer = this.volleyInterval - 1;
            this.fireVolley(var1);
        }

        if (--this.ticks <= 0) {
            this.phase = 3;
            this.wave = 0;
            this.rainTimer = 0;
            this.windupStarted = var1.getGameTime();
            this.windupDuration = 1 + 4 * this.rainGap;
            this.boss.setQizaiCue(new CompoundTag());
        }
    }

    private void beams(ServerLevel var1, boolean var2, int var3, int var4) {
        ListTag var5 = new ListTag();

        for (int var6 = 0; var6 < QizaiSweepRules.arms(var3); var6++) {
            double var7 = Math.toRadians(QizaiSweepRules.angle(this.angle, (double)var4, var3, var6));
            Vec3 var9 = this.anchor.add(0.0, QizaiSweepRules.height(var3, var6), 0.0);
            Vec3 var10 = var9.add(Math.cos(var7) * 32.0, 0.0, Math.sin(var7) * 32.0);
            var10 = var1.clip(new ClipContext(var9, var10, Block.COLLIDER, Fluid.NONE, this.boss)).getLocation();
            double var11 = var9.distanceTo(var10);
            LivingEntity var13 = null;
            if (var2) {
                for (LivingEntity var15 : ManorLivingTargets.nearby(var1, this.boss)) {
                    if (this.combat(var15) && !(var15.distanceToSqr(this.anchor) > 1600.0)) {
                        AABB var16 = var15.getBoundingBox().inflate(1.3, 0.0, 1.3);
                        Optional var17 = var16.contains(var9) ? Optional.of(var9) : var16.clip(var9, var10);
                        if (var17.isPresent() && var9.distanceTo((Vec3)var17.get()) <= var11) {
                            var11 = var9.distanceTo((Vec3)var17.get());
                            var13 = var15;
                        }
                    }
                }
            }

            var5.add(DoubleTag.valueOf(var11));
            if (var13 != null && this.hitCooldown.getOrDefault(var13.getUUID(), 0) <= 0) {
                this.hitCooldown.put(var13.getUUID(), 20);
                var13.invulnerableTime = 0;
                var13.hurt(var1.damageSources().indirectMagic(this.boss, this.boss), 48.0F);
                var13.hurtMarked = true;
            }
        }

        CompoundTag var18 = new CompoundTag();
        var18.putInt("qizai", 7);
        var18.putLong("start", var1.getGameTime());
        var18.putInt("duration", 4);
        var18.putBoolean("released", var2);
        var18.putDouble("angle", QizaiSweepRules.angle(this.angle, (double)var4, var3, 0));
        var18.putDouble("initialAngle", this.angle);
        var18.putInt("elapsed", var4);
        var18.putInt("round", var3);
        var18.putDouble("anchorX", this.anchor.x);
        var18.putDouble("anchorY", this.anchor.y);
        var18.putDouble("anchorZ", this.anchor.z);
        var18.put("lengths", var5);
        var18.putLong("windupStart", this.windupStarted);
        var18.putInt("windupDuration", this.windupDuration);
        this.boss.setQizaiCue(var18);
    }

    private void fireVolley(ServerLevel var1) {
        List var2 = ManorLivingTargets.nearby(var1, this.boss)
            .stream()
            .filter(this::combat)
            .filter(var1x -> var1x.distanceToSqr(this.anchor) <= 4096.0)
            .sorted(Comparator.comparingDouble(var1x -> var1x.distanceToSqr(this.anchor)))
            .map(var0 -> (LivingEntity)var0)
            .toList();
        if (!var2.isEmpty()) {
            for (int var3 = 0; var3 < 8; var3++) {
                double var4 = Math.toRadians((double)var3 * 360.0 / 8.0 + (double)(this.elapsed * this.sweepSpeed));
                Vec3 var6 = new Vec3(Math.cos(var4), 0.0, Math.sin(var4));
                this.spawn(var1, this.anchor.add(var6.scale(1.4)).add(0.0, 1.6, 0.0), var6.scale(0.55), ((LivingEntity)var2.get(var3 % var2.size())).getUUID());
            }

            var1.playSound(null, this.boss.blockPosition(), SoundEvents.SHULKER_SHOOT, SoundSource.HOSTILE, 0.8F, 1.5F);
        }
    }

    private void rain(ServerLevel var1) {
        if (this.roundsLeft > 1) {
            this.beams(var1, false, this.round() + 1, 0);
        }

        if (this.rainTimer-- <= 0) {
            this.rainTimer = this.rainGap - 1;
            double var2 = this.angle + (double)this.wave * 7.5;

            for (int var4 = 0; var4 < 24; var4++) {
                double var5 = Math.toRadians(var2 + (double)var4 * 360.0 / 24.0);
                this.spawn(var1, this.anchor.add(0.0, 1.4, 0.0), new Vec3(Math.cos(var5), 0.0, Math.sin(var5)).scale(0.7), null);
            }

            var1.playSound(null, this.boss.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.0F, 1.2F);
            SpellVisuals.show(var1, this.boss, "rose_starfall", Phase.RELEASE, this.anchor);
            if (++this.wave >= 5) {
                if (--this.roundsLeft > 0) {
                    this.startSweep(var1);
                } else {
                    this.phase = 4;
                    this.ticks = 24;
                    this.boss.setQizaiCue(new CompoundTag());
                }
            }
        }
    }

    private void spawn(ServerLevel var1, Vec3 var2, Vec3 var3, UUID var4) {
        QizaiStarEntity var5 = new QizaiStarEntity(var1, this.boss, var2);
        var5.setDeltaMovement(var3);
        if (var1.addFreshEntity(var5)) {
            this.volley.add(new QizaiSweep.Star(var5, var4, 0));
        }
    }

    private void steerVolley(ServerLevel var1) {
        ListIterator var2 = this.volley.listIterator();

        while (var2.hasNext()) {
            QizaiSweep.Star var3 = (QizaiSweep.Star)var2.next();
            int var4 = var3.age() + 1;
            int var5 = var3.target() == null ? 60 : 40;
            if (var3.entity().isAlive() && !var3.entity().isRemoved() && var4 < var5) {
                var2.set(new QizaiSweep.Star(var3.entity(), var3.target(), var4));
                if (var3.target() != null) {
                    LivingEntity var6 = ManorLivingTargets.resolve(var1, var3.target());
                    if (var6 != null && this.combat(var6)) {
                        var3.entity()
                            .setDeltaMovement(
                                QizaiSweepRules.turn(var3.entity().getDeltaMovement(), var6.getBoundingBox().getCenter().subtract(var3.entity().position()))
                            );
                    }
                }
            } else {
                var3.entity().discard();
                var2.remove();
            }
        }
    }

    private void clearStars() {
        for (QizaiSweep.Star var2 : this.volley) {
            var2.entity().discard();
        }

        this.volley.clear();
    }

    public void cancel() {
        this.phase = 0;
        this.ticks = 0;
        this.roundsLeft = 0;
        this.clearStars();
        this.hitCooldown.clear();
        QizaiPrayerStaffItem.animate(this.boss, false);
        this.boss.setQizaiCue(new CompoundTag());
    }

    private boolean combat(LivingEntity var1) {
        return !ManorLivingTargets.valid(this.boss, var1)
            ? false
            : var1.isAlive() && !var1.isRemoved() && !ManorLivingTargets.creative(var1) && !var1.isSpectator() && var1.level() == this.boss.level();
    }

    private void announce(ServerLevel level, String message) {
        for (Player player : level.players()) {
            if (player.distanceToSqr(this.anchor) <= 4096.0) {
                player.displayClientMessage(Component.literal(message), true);
            }
        }
    }

    private static record Star(QizaiStarEntity entity, UUID target, int age) {
    }
}
