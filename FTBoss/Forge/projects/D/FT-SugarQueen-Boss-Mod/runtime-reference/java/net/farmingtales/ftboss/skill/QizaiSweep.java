/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.DoubleTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
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
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.skill.qizaicross.CompletedCue;
import net.farmingtales.ftboss.util.QizaiSweepRules;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class QizaiSweep {
    private static final int IDLE = 0;
    private static final int WINDUP = 1;
    private static final int SWEEP = 2;
    private static final int RAIN = 3;
    private static final int RECOVERY = 4;
    private final EasterBossEntity boss;
    private final Map<UUID, Integer> hitCooldown = new HashMap<UUID, Integer>();
    private final List<Star> volley = new ArrayList<Star>();
    private int phase;
    private int ticks;
    private double angle;
    private Vec3 anchor = Vec3.f_82478_;
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

    public QizaiSweep(EasterBossEntity easterBossEntity) {
        this.boss = easterBossEntity;
    }

    public boolean busy() {
        return this.phase != 0;
    }

    public boolean begin(ServerLevel serverLevel, LivingEntity livingEntity) {
        LivingEntity livingEntity2;
        if (this.busy() || !(livingEntity instanceof LivingEntity) || !this.combat(livingEntity2 = livingEntity) || serverLevel != this.boss.m_9236_()) {
            return false;
        }
        this.phase = 1;
        this.ticks = this.boss.getChallengeDifficulty().acceleratedTicks(30);
        this.windupStarted = serverLevel.m_46467_();
        this.windupDuration = this.ticks;
        this.anchor = this.boss.m_20182_();
        this.angle = Math.toDegrees(Math.atan2(livingEntity.m_20189_() - this.anchor.f_82481_, livingEntity.m_20185_() - this.anchor.f_82479_));
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
        this.sweepSpeed = Math.max(1, Math.round(100.0f / (float)this.sweepTicks));
        int n = this.ticks + 3 * (this.sweepTicks + 1 + 4 * this.rainGap) + 24;
        this.boss.m_21573_().m_26573_();
        this.boss.playCast(n, 0);
        QizaiPrayerStaffItem.animate((LivingEntity)this.boss, true);
        this.beams(serverLevel, false, 0, 0);
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 1.1f, 0.7f);
        this.announce(serverLevel, "\u00a7d\u3010\u7eef\u661f\u6307\u9488\u3011\u00a77\u7ea2\u675f\u8df3\u8fc7\uff0c\u84dd\u675f\u843d\u5730\u8eb2\uff1b\u8ddf\u968f\u7bad\u5934\u7ed5\u884c\u3002");
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} start: anchor={}, rounds={}, windup={}", new Object[]{"\u7eef\u661f\u6307\u9488", this.anchor, this.roundsLeft, this.ticks});
        return true;
    }

    public boolean tick(ServerLevel serverLevel) {
        if (this.phase == 0) {
            CompletedCue.sweep(serverLevel, this.boss, this.phase, this.windupStarted, this.windupDuration, this.anchor, this.wave, this.sweepTicks, this.ticks);
            return false;
        }
        if (serverLevel != this.boss.m_9236_() || !this.boss.m_6084_() || this.boss.m_213877_() || this.boss.m_21525_()) {
            this.cancel();
            CompletedCue.sweep(serverLevel, this.boss, this.phase, this.windupStarted, this.windupDuration, this.anchor, this.wave, this.sweepTicks, this.ticks);
            return false;
        }
        this.boss.m_21573_().m_26573_();
        this.hitCooldown.replaceAll((uUID, n) -> n - 1);
        this.hitCooldown.values().removeIf(n -> n <= 0);
        this.steerVolley(serverLevel);
        switch (this.phase) {
            case 1: {
                this.beams(serverLevel, false, 0, 0);
                if (--this.ticks > 0) break;
                this.startSweep(serverLevel);
                break;
            }
            case 2: {
                this.sweep(serverLevel);
                break;
            }
            case 3: {
                this.rain(serverLevel);
                break;
            }
            case 4: {
                if (--this.ticks > 0) break;
                FTBossMod.LOGGER.info("[FTBoss][Qizai] {} complete: {} rounds", (Object)"\u7eef\u661f\u6307\u9488", (Object)3);
                this.cancel();
                break;
            }
            default: {
                this.cancel();
            }
        }
        boolean bl = this.phase != 0;
        CompletedCue.sweep(serverLevel, this.boss, this.phase, this.windupStarted, this.windupDuration, this.anchor, this.wave, this.sweepTicks, this.ticks);
        return bl;
    }

    private int round() {
        return 3 - this.roundsLeft;
    }

    private void startSweep(ServerLevel serverLevel) {
        this.phase = 2;
        this.ticks = this.sweepTicks;
        this.elapsed = 0;
        this.volleyTimer = 0;
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_11736_, SoundSource.HOSTILE, 1.2f, 1.2f);
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} round={} arms={}", new Object[]{"\u7eef\u661f\u6307\u9488", this.round() + 1, QizaiSweepRules.arms(this.round())});
    }

    private void sweep(ServerLevel serverLevel) {
        this.beams(serverLevel, true, this.round(), ++this.elapsed * this.sweepSpeed);
        if (this.volleyTimer-- <= 0) {
            this.volleyTimer = this.volleyInterval - 1;
            this.fireVolley(serverLevel);
        }
        if (--this.ticks <= 0) {
            this.phase = 3;
            this.wave = 0;
            this.rainTimer = 0;
            this.windupStarted = serverLevel.m_46467_();
            this.windupDuration = 1 + 4 * this.rainGap;
            this.boss.setQizaiCue(new CompoundTag());
        }
    }

    private void beams(ServerLevel serverLevel, boolean bl, int n, int n2) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < QizaiSweepRules.arms(n); ++i) {
            double d = Math.toRadians(QizaiSweepRules.angle(this.angle, n2, n, i));
            Vec3 vec3 = this.anchor.m_82520_(0.0, QizaiSweepRules.height(n, i), 0.0);
            Vec3 vec32 = vec3.m_82520_(Math.cos(d) * 32.0, 0.0, Math.sin(d) * 32.0);
            vec32 = serverLevel.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss)).m_82450_();
            double d2 = vec3.m_82554_(vec32);
            LivingEntity livingEntity = null;
            if (bl) {
                for (LivingEntity livingEntity2 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
                    AABB aABB;
                    Optional optional;
                    if (!this.combat(livingEntity2) || livingEntity2.m_20238_(this.anchor) > 1600.0 || !(optional = (aABB = livingEntity2.m_20191_().m_82377_(1.3, 0.0, 1.3)).m_82390_(vec3) ? Optional.of(vec3) : aABB.m_82371_(vec3, vec32)).isPresent() || !(vec3.m_82554_((Vec3)optional.get()) <= d2)) continue;
                    d2 = vec3.m_82554_((Vec3)optional.get());
                    livingEntity = livingEntity2;
                }
            }
            listTag.add((Object)DoubleTag.m_128500_((double)d2));
            if (livingEntity == null || this.hitCooldown.getOrDefault(livingEntity.m_20148_(), 0) > 0) continue;
            this.hitCooldown.put(livingEntity.m_20148_(), 20);
            livingEntity.f_19802_ = 0;
            livingEntity.m_6469_(serverLevel.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), 48.0f);
            livingEntity.f_19864_ = true;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("qizai", 7);
        compoundTag.m_128356_("start", serverLevel.m_46467_());
        compoundTag.m_128405_("duration", 4);
        compoundTag.m_128379_("released", bl);
        compoundTag.m_128347_("angle", QizaiSweepRules.angle(this.angle, n2, n, 0));
        compoundTag.m_128347_("initialAngle", this.angle);
        compoundTag.m_128405_("elapsed", n2);
        compoundTag.m_128405_("round", n);
        compoundTag.m_128347_("anchorX", this.anchor.f_82479_);
        compoundTag.m_128347_("anchorY", this.anchor.f_82480_);
        compoundTag.m_128347_("anchorZ", this.anchor.f_82481_);
        compoundTag.m_128365_("lengths", (Tag)listTag);
        compoundTag.m_128356_("windupStart", this.windupStarted);
        compoundTag.m_128405_("windupDuration", this.windupDuration);
        this.boss.setQizaiCue(compoundTag);
    }

    private void fireVolley(ServerLevel serverLevel) {
        List<LivingEntity> list = ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss).stream().filter(this::combat).filter(livingEntity -> livingEntity.m_20238_(this.anchor) <= 4096.0).sorted(Comparator.comparingDouble(livingEntity -> livingEntity.m_20238_(this.anchor))).map(livingEntity -> livingEntity).toList();
        if (list.isEmpty()) {
            return;
        }
        for (int i = 0; i < 8; ++i) {
            double d = Math.toRadians((double)i * 360.0 / 8.0 + (double)(this.elapsed * this.sweepSpeed));
            Vec3 vec3 = new Vec3(Math.cos(d), 0.0, Math.sin(d));
            this.spawn(serverLevel, this.anchor.m_82549_(vec3.m_82490_(1.4)).m_82520_(0.0, 1.6, 0.0), vec3.m_82490_(0.55), list.get(i % list.size()).m_20148_());
        }
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_12417_, SoundSource.HOSTILE, 0.8f, 1.5f);
    }

    private void rain(ServerLevel serverLevel) {
        if (this.roundsLeft > 1) {
            this.beams(serverLevel, false, this.round() + 1, 0);
        }
        if (this.rainTimer-- > 0) {
            return;
        }
        this.rainTimer = this.rainGap - 1;
        double d = this.angle + (double)this.wave * 7.5;
        for (int i = 0; i < 24; ++i) {
            double d2 = Math.toRadians(d + (double)i * 360.0 / 24.0);
            this.spawn(serverLevel, this.anchor.m_82520_(0.0, 1.4, 0.0), new Vec3(Math.cos(d2), 0.0, Math.sin(d2)).m_82490_(0.7), null);
        }
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 1.0f, 1.2f);
        SpellVisuals.show(serverLevel, (LivingEntity)this.boss, "rose_starfall", SpellVisualRules.Phase.RELEASE, this.anchor);
        if (++this.wave >= 5) {
            if (--this.roundsLeft > 0) {
                this.startSweep(serverLevel);
            } else {
                this.phase = 4;
                this.ticks = 24;
                this.boss.setQizaiCue(new CompoundTag());
            }
        }
    }

    private void spawn(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32, UUID uUID) {
        QizaiStarEntity qizaiStarEntity = new QizaiStarEntity(serverLevel, this.boss, vec3);
        qizaiStarEntity.m_20256_(vec32);
        if (serverLevel.m_7967_((Entity)qizaiStarEntity)) {
            this.volley.add(new Star(qizaiStarEntity, uUID, 0));
        }
    }

    private void steerVolley(ServerLevel serverLevel) {
        ListIterator<Star> listIterator = this.volley.listIterator();
        while (listIterator.hasNext()) {
            LivingEntity livingEntity;
            int n;
            Star star = listIterator.next();
            int n2 = star.age() + 1;
            int n3 = n = star.target() == null ? 60 : 40;
            if (!star.entity().m_6084_() || star.entity().m_213877_() || n2 >= n) {
                star.entity().m_146870_();
                listIterator.remove();
                continue;
            }
            listIterator.set(new Star(star.entity(), star.target(), n2));
            if (star.target() == null || (livingEntity = ManorLivingTargets.resolve((Level)serverLevel, star.target())) == null || !this.combat(livingEntity)) continue;
            star.entity().m_20256_(QizaiSweepRules.turn(star.entity().m_20184_(), livingEntity.m_20191_().m_82399_().m_82546_(star.entity().m_20182_())));
        }
    }

    private void clearStars() {
        for (Star star : this.volley) {
            star.entity().m_146870_();
        }
        this.volley.clear();
    }

    public void cancel() {
        this.phase = 0;
        this.ticks = 0;
        this.roundsLeft = 0;
        this.clearStars();
        this.hitCooldown.clear();
        QizaiPrayerStaffItem.animate((LivingEntity)this.boss, false);
        this.boss.setQizaiCue(new CompoundTag());
    }

    private boolean combat(LivingEntity livingEntity) {
        if (!ManorLivingTargets.valid((Entity)this.boss, livingEntity)) {
            return false;
        }
        return livingEntity.m_6084_() && !livingEntity.m_213877_() && !ManorLivingTargets.creative(livingEntity) && !livingEntity.m_5833_() && livingEntity.m_9236_() == this.boss.m_9236_();
    }

    private void announce(ServerLevel level, String message) {
        for (Player player : level.m_6907_()) {
            if (!(player.m_20238_(this.anchor) <= 4096.0)) continue;
            player.m_5661_((Component)Component.m_237113_((String)message), true);
        }
    }

    private record Star(QizaiStarEntity entity, UUID target, int age) {
    }
}

