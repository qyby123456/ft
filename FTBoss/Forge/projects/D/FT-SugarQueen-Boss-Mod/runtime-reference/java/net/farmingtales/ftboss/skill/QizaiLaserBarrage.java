/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
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
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem;
import net.farmingtales.ftboss.util.QizaiLaserBarrageRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class QizaiLaserBarrage {
    private final EasterBossEntity boss;
    private final List<Beam> beams = new ArrayList<Beam>();
    private UUID preferredTarget;
    private Vec3 lastTarget = Vec3.f_82478_;
    private Vec3 center = Vec3.f_82478_;
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

    public QizaiLaserBarrage(EasterBossEntity easterBossEntity) {
        this.boss = easterBossEntity;
    }

    public boolean busy() {
        return this.busy;
    }

    public boolean begin(ServerLevel serverLevel, LivingEntity livingEntity) {
        LivingEntity livingEntity2;
        if (this.busy || serverLevel != this.boss.m_9236_() || !(livingEntity instanceof LivingEntity) || !this.combat(livingEntity2 = livingEntity)) {
            return false;
        }
        this.preferredTarget = livingEntity2.m_20148_();
        this.lastTarget = livingEntity2.m_20191_().m_82399_();
        this.center = this.boss.m_20182_();
        this.started = serverLevel.m_46467_();
        this.age = 0;
        this.nextBeam = 0;
        this.round = 0;
        this.roundStart = 0;
        this.beams.clear();
        this.activeTicks = Math.max(1, this.boss.getChallengeDifficulty().acceleratedTicks(120));
        this.warningTicks = Math.max(1, this.boss.getChallengeDifficulty().acceleratedTicks(14));
        this.fadeTicks = Math.max(1, this.boss.getChallengeDifficulty().acceleratedTicks(6));
        this.recoveryTicks = Math.max(0, this.boss.getChallengeDifficulty().acceleratedTicks(20));
        this.beamCount = QizaiLaserBarrageRules.roundBeamCount(this.round);
        this.busy = true;
        this.boss.m_21573_().m_26573_();
        this.boss.playCast(this.totalTicks(), 2);
        QizaiPrayerStaffItem.animate((LivingEntity)this.boss, true);
        this.sync();
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_11736_, SoundSource.HOSTILE, 1.3f, 1.45f);
        this.announceRound(serverLevel);
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} start: rounds={}, beams={}, active={}, warning={}, bossY={}", new Object[]{"\u4e07\u68f1\u661f\u72f1", 3, this.beamCount, this.activeTicks, this.warningTicks, this.boss.m_20186_()});
        return true;
    }

    public boolean tick(ServerLevel serverLevel) {
        if (!this.busy) {
            return false;
        }
        if (serverLevel != this.boss.m_9236_() || !this.boss.m_6084_() || this.boss.m_213877_()) {
            this.cancel();
            return false;
        }
        List list = this.targets(serverLevel);
        this.boss.m_21573_().m_26573_();
        if (!list.isEmpty()) {
            LivingEntity livingEntity = this.preferred(serverLevel, list);
            this.preferredTarget = livingEntity.m_20148_();
            this.lastTarget = livingEntity.m_20191_().m_82399_();
        }
        this.boss.m_21563_().m_24950_(this.lastTarget.f_82479_, this.lastTarget.f_82480_, this.lastTarget.f_82481_, 90.0f, 90.0f);
        ++this.age;
        boolean bl = false;
        if (this.round + 1 < 3 && this.age >= this.roundStart + this.roundTicks()) {
            ++this.round;
            this.roundStart += this.roundTicks();
            this.nextBeam = 0;
            this.beams.clear();
            this.beamCount = QizaiLaserBarrageRules.roundBeamCount(this.round);
            this.announceRound(serverLevel);
            bl = true;
        }
        while (this.nextBeam < this.beamCount && this.age >= this.spawnTick(this.nextBeam)) {
            this.spawn(serverLevel, list, this.nextBeam++);
            bl = true;
        }
        for (int i = 0; i < this.beams.size(); ++i) {
            Beam beam2 = this.beams.get(i);
            if (beam2.fired || this.age < beam2.fireTick) continue;
            this.fire(serverLevel, beam2, i);
            bl = true;
        }
        if (this.age >= this.totalTicks()) {
            FTBossMod.LOGGER.info("[FTBoss][Qizai] {} complete: spawned={}, targeted={}", new Object[]{"\u4e07\u68f1\u661f\u72f1", this.beams.size(), this.beams.stream().filter(beam -> beam.targeted).count()});
            this.cancel();
            return false;
        }
        if (bl) {
            this.sync();
        }
        return true;
    }

    private void spawn(ServerLevel serverLevel, List list, int n) {
        Vec3 vec3;
        boolean bl = QizaiLaserBarrageRules.targetsPlayer(n);
        Vec3 vec32 = this.randomOrigin(serverLevel);
        if (bl) {
            if (!list.isEmpty()) {
                LivingEntity livingEntity = this.preferred(serverLevel, list);
                this.preferredTarget = livingEntity.m_20148_();
                this.lastTarget = livingEntity.m_20191_().m_82399_();
                ManorLivingTargets.tell(livingEntity, (Component)Component.m_237113_((String)"\u00a7c\u3010\u4e07\u68f1\u661f\u72f1\u3011\u9501\u5b9a\u5149\u675f \u2014\u2014 \u79bb\u5f00\u7ea2\u767d\u9884\u8b66\u7ebf\uff01"), true);
            }
            vec3 = this.clip(serverLevel, vec32, this.lastTarget);
        } else {
            vec3 = this.randomDirection(serverLevel, vec32);
        }
        int n2 = this.spawnTick(n);
        Beam beam = new Beam(vec32, vec3, n2, n2 + this.warningTicks, bl);
        this.beams.add(beam);
        serverLevel.m_6263_(null, vec32.f_82479_, vec32.f_82480_, vec32.f_82481_, SoundEvents.f_144243_, SoundSource.HOSTILE, bl ? 0.9f : 0.45f, bl ? 1.85f : 1.25f + (float)(n % 5) * 0.08f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void fire(ServerLevel serverLevel, Beam beam, int n) {
        beam.fired = true;
        beam.end = this.clip(serverLevel, beam.origin, beam.end);
        int n2 = 0;
        AABB aABB = new AABB(beam.origin, beam.end).m_82400_(0.95);
        for (LivingEntity livingEntity : serverLevel.m_6443_(LivingEntity.class, aABB, this::combat)) {
            if (!QizaiLaserBarrageRules.intersects(livingEntity.m_20191_(), beam.origin, beam.end)) continue;
            int n3 = livingEntity.f_19802_;
            livingEntity.f_19802_ = 0;
            try {
                if (!livingEntity.m_6469_(serverLevel.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), 48.0f)) continue;
                ++n2;
            }
            finally {
                livingEntity.f_19802_ = Math.max(n3, livingEntity.f_19802_);
            }
        }
        int n4 = Math.min(42, Math.max(2, (int)Math.ceil(beam.origin.m_82554_(beam.end))));
        for (int i = 0; i <= n4; ++i) {
            Vec3 vec3 = beam.origin.m_165921_(beam.end, (double)i / (double)n4);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, 1, 0.0, 0.0, 0.0, 0.0);
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_175830_, beam.end.f_82479_, beam.end.f_82480_, beam.end.f_82481_, 8, 0.16, 0.16, 0.16, 0.04);
        serverLevel.m_6263_(null, beam.origin.f_82479_, beam.origin.f_82480_, beam.origin.f_82481_, SoundEvents.f_12001_, SoundSource.HOSTILE, beam.targeted ? 1.05f : 0.7f, 1.2f + (float)(n % 7) * 0.05f);
        FTBossMod.LOGGER.debug("[FTBoss][Qizai] {} beam {}/{}: targeted={}, hits={}", new Object[]{"\u4e07\u68f1\u661f\u72f1", n + 1, this.beamCount, beam.targeted, n2});
    }

    private Vec3 randomOrigin(ServerLevel serverLevel) {
        for (int i = 0; i < 12; ++i) {
            double d = this.boss.m_217043_().m_188500_() * Math.PI * 2.0;
            double d2 = Math.sqrt(this.boss.m_217043_().m_188500_()) * 24.0;
            double d3 = 3.0 + this.boss.m_217043_().m_188500_() * 7.0;
            Vec3 vec3 = this.center.m_82520_(Math.cos(d) * d2, d3, Math.sin(d) * d2);
            BlockPos blockPos = BlockPos.m_274446_((Position)vec3);
            AABB aABB = new AABB(vec3.f_82479_ - 0.28, vec3.f_82480_ - 0.28, vec3.f_82481_ - 0.28, vec3.f_82479_ + 0.28, vec3.f_82480_ + 0.28, vec3.f_82481_ + 0.28);
            if (!serverLevel.m_46805_(blockPos) || !serverLevel.m_6857_().m_61937_(blockPos) || !serverLevel.m_45772_(aABB)) continue;
            return vec3;
        }
        double d = this.boss.m_217043_().m_188500_() * Math.PI * 2.0;
        return this.center.m_82520_(Math.cos(d) * 24.0 * 0.5, 6.5, Math.sin(d) * 24.0 * 0.5);
    }

    private Vec3 randomDirection(ServerLevel serverLevel, Vec3 vec3) {
        Vec3 vec32 = vec3.m_82520_(0.0, -36.0, 0.0);
        for (int i = 0; i < 8; ++i) {
            Vec3 vec33;
            double d = this.boss.m_217043_().m_188500_() * Math.PI * 2.0;
            double d2 = -0.65 + this.boss.m_217043_().m_188500_() * 0.9;
            double d3 = Math.sqrt(Math.max(0.0, 1.0 - d2 * d2));
            Vec3 vec34 = new Vec3(Math.cos(d) * d3, d2, Math.sin(d) * d3);
            vec32 = vec33 = this.clip(serverLevel, vec3, vec3.m_82549_(vec34.m_82490_(36.0)));
            if (!(vec3.m_82557_(vec33) >= 64.0)) continue;
            return vec33;
        }
        return vec32;
    }

    private Vec3 clip(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32) {
        return serverLevel.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss)).m_82450_();
    }

    private List targets(ServerLevel serverLevel) {
        return ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss).stream().filter(this::combat).sorted(Comparator.comparingDouble(arg_0 -> ((EasterBossEntity)this.boss).m_20280_(arg_0))).toList();
    }

    private LivingEntity preferred(ServerLevel serverLevel, List list) {
        LivingEntity livingEntity;
        LivingEntity livingEntity2 = this.preferredTarget == null ? null : ManorLivingTargets.resolve((Level)serverLevel, this.preferredTarget);
        return livingEntity2 instanceof LivingEntity && list.contains(livingEntity = livingEntity2) ? livingEntity : (LivingEntity)list.get(this.boss.m_217043_().m_188503_(list.size()));
    }

    private boolean combat(LivingEntity livingEntity) {
        if (!ManorLivingTargets.valid((Entity)this.boss, livingEntity)) {
            return false;
        }
        return livingEntity != null && livingEntity.m_9236_() == this.boss.m_9236_() && livingEntity.m_6084_() && !livingEntity.m_213877_() && !ManorLivingTargets.creative(livingEntity) && !livingEntity.m_5833_() && !this.boss.m_7307_((Entity)livingEntity) && !livingEntity.m_7307_((Entity)this.boss) && livingEntity.m_20280_((Entity)this.boss) <= 1600.0;
    }

    private int totalTicks() {
        return QizaiLaserBarrageRules.castDuration(this.activeTicks, this.warningTicks, this.fadeTicks, this.recoveryTicks);
    }

    private int roundTicks() {
        return QizaiLaserBarrageRules.roundDuration(this.activeTicks, this.warningTicks, this.fadeTicks);
    }

    private int spawnTick(int n) {
        return this.roundStart + QizaiLaserBarrageRules.roundSpawnTick(n, this.activeTicks, this.beamCount);
    }

    private void announceRound(ServerLevel serverLevel) {
        this.announce(serverLevel, "\u00a7d\u3010\u4e07\u68f1\u661f\u72f1\u3011\u00a7e\u7b2c " + (this.round + 1) + "/3 \u8f6e \u00b7 " + this.beamCount + " \u675f\u6fc0\u5149 \u00a7c\u2014\u2014 \u8eb2\u5f00\u9501\u5b9a\u9884\u8b66\uff01");
    }

    private void sync() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("qizai", 10);
        compoundTag.m_128356_("start", this.started);
        compoundTag.m_128405_("duration", this.totalTicks());
        compoundTag.m_128405_("activeTicks", this.activeTicks);
        compoundTag.m_128405_("round", this.round);
        compoundTag.m_128405_("roundStart", this.roundStart);
        compoundTag.m_128405_("beamCount", this.beamCount);
        compoundTag.m_128405_("warningTicks", this.warningTicks);
        compoundTag.m_128405_("fadeTicks", this.fadeTicks);
        compoundTag.m_128379_("released", this.age >= this.roundStart + this.warningTicks + 1);
        QizaiLaserBarrage.putPoint(compoundTag, "center", this.center);
        ListTag listTag = new ListTag();
        for (Beam beam : this.beams) {
            CompoundTag compoundTag2 = new CompoundTag();
            QizaiLaserBarrage.putPoint(compoundTag2, "", beam.origin);
            QizaiLaserBarrage.putPoint(compoundTag2, "end", beam.end);
            compoundTag2.m_128405_("spawn", beam.spawnTick);
            compoundTag2.m_128405_("fire", beam.fireTick);
            compoundTag2.m_128379_("targeted", beam.targeted);
            compoundTag2.m_128379_("fired", beam.fired);
            listTag.add((Object)compoundTag2);
        }
        compoundTag.m_128365_("beams", (Tag)listTag);
        this.boss.setQizaiCue(compoundTag);
    }

    private static void putPoint(CompoundTag compoundTag, String string, Vec3 vec3) {
        compoundTag.m_128347_(string + "X", vec3.f_82479_);
        compoundTag.m_128347_(string + "Y", vec3.f_82480_);
        compoundTag.m_128347_(string + "Z", vec3.f_82481_);
    }

    private void announce(ServerLevel level, String text) {
        for (ServerPlayer player : level.m_6907_()) {
            if (!(player.m_20280_((Entity)this.boss) <= 1600.0)) continue;
            player.m_5661_((Component)Component.m_237113_((String)text), true);
        }
    }

    public void cancel() {
        if (!this.busy) {
            return;
        }
        this.busy = false;
        this.preferredTarget = null;
        this.lastTarget = Vec3.f_82478_;
        this.beams.clear();
        this.age = 0;
        this.nextBeam = 0;
        this.round = 0;
        this.roundStart = 0;
        QizaiPrayerStaffItem.animate((LivingEntity)this.boss, false);
        this.boss.setQizaiCue(new CompoundTag());
    }

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
}

