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
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem;
import net.farmingtales.ftboss.util.QizaiPrismRules;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class QizaiPrismLock {
    private final EasterBossEntity boss;
    private final List<Beam> beams = new ArrayList<Beam>();
    private UUID targetId;
    private Vec3 lockedPoint = Vec3.f_82478_;
    private long started;
    private int age;
    private int trackTicks;
    private int lockTicks;
    private int fireGap;
    private int recoveryTicks;
    private boolean locked;

    public QizaiPrismLock(EasterBossEntity easterBossEntity) {
        this.boss = easterBossEntity;
    }

    public boolean busy() {
        return this.targetId != null;
    }

    public boolean locked() {
        return this.busy() && this.locked;
    }

    public boolean begin(ServerLevel serverLevel, LivingEntity livingEntity) {
        LivingEntity livingEntity2;
        if (this.busy() || serverLevel != this.boss.m_9236_() || !(livingEntity instanceof LivingEntity) || !this.combat(livingEntity2 = livingEntity)) {
            return false;
        }
        this.targetId = livingEntity2.m_20148_();
        this.lockedPoint = livingEntity2.m_20191_().m_82399_();
        this.started = serverLevel.m_46467_();
        this.age = 0;
        this.locked = false;
        this.trackTicks = this.boss.getChallengeDifficulty().acceleratedTicks(24);
        this.lockTicks = this.boss.getChallengeDifficulty().acceleratedTicks(12);
        this.fireGap = Math.max(1, this.boss.getChallengeDifficulty().acceleratedTicks(2));
        this.recoveryTicks = this.boss.getChallengeDifficulty().acceleratedTicks(20);
        this.beams.clear();
        for (int i = 0; i < 6; ++i) {
            this.beams.add(new Beam(this.randomAnchor(serverLevel, this.lockedPoint, i), this.lockedPoint));
        }
        this.boss.m_21573_().m_26573_();
        this.boss.playCast(this.totalTicks(), 2);
        QizaiPrayerStaffItem.animate((LivingEntity)this.boss, true);
        this.sync();
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 1.2f, 1.35f);
        this.announce(serverLevel, "\u00a7d\u3010\u7eef\u661f\u9501\u88c1\u3011\u00a77\u516d\u9762\u6676\u68f1\u6b63\u5728\u8ffd\u8e2a \u2014\u2014 \u00a7e\u9501\u5b9a\u63d0\u793a\u540e\u7acb\u523b\u6a2a\u79fb\uff01");
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} start: target={}, beams={}, track={}, lock={}, bossY={}", new Object[]{"\u7eef\u661f\u9501\u88c1", livingEntity2.m_7755_().getString(), this.beams.size(), this.trackTicks, this.lockTicks, this.boss.m_20186_()});
        return true;
    }

    public boolean tick(ServerLevel serverLevel) {
        if (!this.busy()) {
            return false;
        }
        if (serverLevel != this.boss.m_9236_() || !this.boss.m_6084_() || this.boss.m_213877_() || this.boss.m_21525_()) {
            this.cancel();
            return false;
        }
        this.boss.m_21573_().m_26573_();
        ++this.age;
        if (!this.locked) {
            LivingEntity livingEntity = ManorLivingTargets.resolve((Level)serverLevel, this.targetId);
            if (livingEntity == null || !this.combat(livingEntity)) {
                this.cancel();
                return false;
            }
            this.lockedPoint = livingEntity.m_20191_().m_82399_();
            for (Beam beam : this.beams) {
                beam.end = this.lockedPoint;
            }
            this.boss.m_21563_().m_24950_(this.lockedPoint.f_82479_, this.lockedPoint.f_82480_, this.lockedPoint.f_82481_, 90.0f, 90.0f);
            if (this.age >= this.trackTicks) {
                this.lock(serverLevel, livingEntity);
            }
        }
        for (int i = 0; i < this.beams.size(); ++i) {
            if (this.age != QizaiPrismRules.fireTick(i, this.trackTicks, this.lockTicks, this.fireGap)) continue;
            this.fire(serverLevel, i);
        }
        if (this.age >= this.totalTicks()) {
            FTBossMod.LOGGER.info("[FTBoss][Qizai] {} complete: beams={}, locked={}", new Object[]{"\u7eef\u661f\u9501\u88c1", this.beams.size(), this.lockedPoint});
            this.cancel();
            return false;
        }
        this.sync();
        return true;
    }

    private void lock(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.locked = true;
        this.lockedPoint = livingEntity.m_20191_().m_82399_();
        for (Beam beam : this.beams) {
            beam.end = this.clip(serverLevel, beam.origin, this.lockedPoint);
        }
        this.sync();
        serverLevel.m_6263_(null, this.lockedPoint.f_82479_, this.lockedPoint.f_82480_, this.lockedPoint.f_82481_, (SoundEvent)SoundEvents.f_12216_.get(), SoundSource.HOSTILE, 1.4f, 1.9f);
        ManorLivingTargets.tell(livingEntity, (Component)Component.m_237113_((String)"\u00a7c\u3010\u7eef\u661f\u9501\u88c1\u3011\u9501\u5b9a\u5b8c\u6210 \u2014\u2014 \u6a2a\u79fb\u8eb2\u5f00\u4ea4\u53c9\u706b\u7ebf\uff01"), true);
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} locked: target={}, point={}", new Object[]{"\u7eef\u661f\u9501\u88c1", livingEntity.m_7755_().getString(), this.lockedPoint});
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void fire(ServerLevel serverLevel, int n) {
        Vec3 vec3;
        Beam beam = this.beams.get(n);
        beam.end = vec3 = this.clip(serverLevel, beam.origin, beam.end);
        int n2 = 0;
        AABB aABB = new AABB(beam.origin, vec3).m_82400_(1.05);
        for (LivingEntity livingEntity : serverLevel.m_6443_(LivingEntity.class, aABB, this::combat)) {
            if (!QizaiPrismRules.intersects(livingEntity.m_20191_(), beam.origin, vec3)) continue;
            int n3 = livingEntity.f_19802_;
            livingEntity.f_19802_ = 0;
            try {
                if (!livingEntity.m_6469_(serverLevel.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), 72.0f)) continue;
                ++n2;
            }
            finally {
                livingEntity.f_19802_ = Math.max(n3, livingEntity.f_19802_);
            }
        }
        int n4 = Math.min(48, Math.max(2, (int)Math.ceil(beam.origin.m_82554_(vec3) * 1.5)));
        for (int i = 0; i <= n4; ++i) {
            Vec3 vec32 = beam.origin.m_165921_(vec3, (double)i / (double)n4);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, vec32.f_82479_, vec32.f_82480_, vec32.f_82481_, 1, 0.0, 0.0, 0.0, 0.0);
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_175830_, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, 12, 0.18, 0.18, 0.18, 0.05);
        serverLevel.m_6263_(null, beam.origin.f_82479_, beam.origin.f_82480_, beam.origin.f_82481_, SoundEvents.f_12001_, SoundSource.HOSTILE, 1.0f, 1.25f + (float)n * 0.07f);
        FTBossMod.LOGGER.info("[FTBoss][Qizai] {} beam {}/{}: hits={}, from={}, to={}", new Object[]{"\u7eef\u661f\u9501\u88c1", n + 1, this.beams.size(), n2, beam.origin, vec3});
    }

    private Vec3 randomAnchor(ServerLevel serverLevel, Vec3 vec3, int n) {
        double d = (double)n * Math.PI * 2.0 / 6.0;
        for (int i = 0; i < 12; ++i) {
            double d2 = d + (this.boss.m_217043_().m_188500_() - 0.5) * Math.PI / 2.4;
            double d3 = 7.0 + this.boss.m_217043_().m_188500_() * 7.0;
            double d4 = 3.0 + this.boss.m_217043_().m_188500_() * 6.0;
            Vec3 vec32 = vec3.m_82520_(Math.cos(d2) * d3, d4, Math.sin(d2) * d3);
            BlockPos blockPos = BlockPos.m_274446_((Position)vec32);
            AABB aABB = new AABB(vec32.f_82479_ - 0.3, vec32.f_82480_ - 0.3, vec32.f_82481_ - 0.3, vec32.f_82479_ + 0.3, vec32.f_82480_ + 0.3, vec32.f_82481_ + 0.3);
            if (!serverLevel.m_46805_(blockPos) || !serverLevel.m_6857_().m_61937_(blockPos) || !serverLevel.m_45772_(aABB) || serverLevel.m_45547_(new ClipContext(vec32, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss)).m_6662_() != HitResult.Type.MISS) continue;
            return vec32;
        }
        double d5 = d + (this.boss.m_217043_().m_188500_() - 0.5) * 0.35;
        double d6 = 10.5;
        double d7 = 3.0 + 6.0 * ((double)n + 0.5) / 6.0;
        return vec3.m_82520_(Math.cos(d5) * d6, d7, Math.sin(d5) * d6);
    }

    private Vec3 clip(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32) {
        return serverLevel.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss)).m_82450_();
    }

    private boolean combat(LivingEntity livingEntity) {
        if (!ManorLivingTargets.valid((Entity)this.boss, livingEntity)) {
            return false;
        }
        return livingEntity != null && livingEntity.m_9236_() == this.boss.m_9236_() && livingEntity.m_6084_() && !livingEntity.m_213877_() && !ManorLivingTargets.creative(livingEntity) && !livingEntity.m_5833_() && !this.boss.m_7307_((Entity)livingEntity) && !livingEntity.m_7307_((Entity)this.boss) && livingEntity.m_20280_((Entity)this.boss) <= 1600.0;
    }

    private int totalTicks() {
        return QizaiPrismRules.duration(this.trackTicks, this.lockTicks, this.fireGap, this.recoveryTicks);
    }

    private void sync() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("qizai", 9);
        compoundTag.m_128356_("start", this.started);
        compoundTag.m_128405_("duration", this.totalTicks());
        compoundTag.m_128405_("trackTicks", this.trackTicks);
        compoundTag.m_128405_("lockTicks", this.lockTicks);
        compoundTag.m_128405_("fireGap", this.fireGap);
        compoundTag.m_128405_("fadeTicks", 8);
        compoundTag.m_128379_("locked", this.locked);
        compoundTag.m_128379_("released", this.age >= this.trackTicks + this.lockTicks);
        QizaiPrismLock.putPoint(compoundTag, "target", this.lockedPoint);
        ListTag listTag = new ListTag();
        for (Beam beam : this.beams) {
            CompoundTag compoundTag2 = new CompoundTag();
            QizaiPrismLock.putPoint(compoundTag2, "", beam.origin);
            QizaiPrismLock.putPoint(compoundTag2, "end", beam.end);
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
        if (!this.busy()) {
            return;
        }
        this.targetId = null;
        this.beams.clear();
        this.age = 0;
        this.locked = false;
        QizaiPrayerStaffItem.animate((LivingEntity)this.boss, false);
        this.boss.setQizaiCue(new CompoundTag());
    }

    private static final class Beam {
        final Vec3 origin;
        Vec3 end;

        Beam(Vec3 origin, Vec3 end) {
            this.origin = origin;
            this.end = end;
        }
    }
}

