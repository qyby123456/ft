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
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.boss.ai.ManorPredictiveAttacks;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem;
import net.farmingtales.ftboss.skill.QizaiConstellation;
import net.farmingtales.ftboss.skill.QizaiLaserBarrage;
import net.farmingtales.ftboss.skill.QizaiMulticast;
import net.farmingtales.ftboss.skill.QizaiPrismLock;
import net.farmingtales.ftboss.skill.QizaiSkyNova;
import net.farmingtales.ftboss.skill.QizaiSweep;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class QizaiMagic {
    private final EasterBossEntity boss;
    private final QizaiSweep sweep;
    private final QizaiSkyNova skyNova;
    private final QizaiPrismLock prismLock;
    private final QizaiLaserBarrage laserBarrage;
    private final List<Vec3> marks = new ArrayList<Vec3>();
    private Vec3 aim = Vec3.f_82478_;
    private int kind;
    private int remaining;
    private int recovery;
    private int releasesLeft;
    private int repeatWait;
    private int cooldown = QizaiRules.cooldown(40);
    private int stepCooldown = QizaiRules.cooldown(80);
    private int sequence;

    public QizaiMagic(EasterBossEntity easterBossEntity) {
        this.boss = easterBossEntity;
        this.sweep = new QizaiSweep(easterBossEntity);
        this.skyNova = new QizaiSkyNova(easterBossEntity);
        this.prismLock = new QizaiPrismLock(easterBossEntity);
        this.laserBarrage = new QizaiLaserBarrage(easterBossEntity);
    }

    public boolean busy() {
        return this.kind != 0;
    }

    public boolean uninterruptible() {
        if (this.boss.getVariant() != 11 || !this.boss.m_6084_() || this.boss.m_213877_()) {
            return false;
        }
        return this.kind == 8 && this.skyNova.busy() || this.kind == 10 && this.laserBarrage.busy();
    }

    public boolean protectedCast() {
        if (this.boss.getVariant() != 11 || !this.boss.m_6084_() || this.boss.m_213877_()) {
            return false;
        }
        return this.kind == 8 && this.skyNova.busy();
    }

    public boolean skyCasting() {
        return this.kind == 8 && this.skyNova.busy();
    }

    private boolean eligible(boolean bl) {
        return !(this.boss.getVariant() != 11 || !this.boss.m_6084_() || this.boss.m_213877_() || !bl && this.boss.m_21525_() || this.boss.isMini() || this.boss.isGiant() || !bl && this.boss.m_20147_() || ManorFight.isFinaleMember((LivingEntity)this.boss));
    }

    private boolean target(LivingEntity livingEntity) {
        LivingEntity livingEntity2;
        if (!ManorLivingTargets.valid((Entity)this.boss, livingEntity)) {
            return false;
        }
        return livingEntity instanceof LivingEntity && (livingEntity2 = livingEntity).m_9236_() == this.boss.m_9236_() && livingEntity2.m_6084_() && !livingEntity2.m_213877_() && !ManorLivingTargets.creative(livingEntity2) && !livingEntity2.m_5833_() && this.boss.m_20280_((Entity)livingEntity2) <= 1024.0;
    }

    public void tick(ServerLevel serverLevel) {
        if (this.boss.getVariant() != 11) {
            return;
        }
        boolean bl = this.uninterruptible();
        if (!this.eligible(bl)) {
            this.forceCancel();
            return;
        }
        if (this.stepCooldown > 0) {
            --this.stepCooldown;
        }
        LivingEntity livingEntity2 = this.boss.m_5448_();
        if (this.busy()) {
            boolean bl2;
            boolean bl3 = bl2 = this.kind == 9 && this.prismLock.locked();
            if (!(bl || this.kind == 7 || bl2 || this.target(livingEntity2))) {
                this.cancel();
                return;
            }
            this.boss.m_21573_().m_26573_();
            this.boss.m_21563_().m_24946_(this.aim.f_82479_, this.aim.f_82480_, this.aim.f_82481_);
            if (this.kind == 8) {
                if (this.skyNova.tick(serverLevel)) {
                    return;
                }
                this.kind = 0;
                this.marks.clear();
                this.cooldown = this.boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.cooldown(64));
                return;
            }
            if (this.kind == 7) {
                if (this.sweep.tick(serverLevel)) {
                    return;
                }
                this.kind = 0;
                this.marks.clear();
                this.cooldown = this.boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.cooldown(64));
                return;
            }
            if (this.kind == 9) {
                if (this.prismLock.tick(serverLevel)) {
                    return;
                }
                this.kind = 0;
                this.marks.clear();
                this.cooldown = this.boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.cooldown(64));
                return;
            }
            if (this.kind == 10) {
                if (this.laserBarrage.tick(serverLevel)) {
                    return;
                }
                this.kind = 0;
                this.marks.clear();
                this.cooldown = this.boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.cooldown(64));
                return;
            }
            if (this.remaining == 0 && this.releasesLeft > 0) {
                if (--this.repeatWait <= 0) {
                    --this.releasesLeft;
                    this.release(serverLevel);
                    this.repeatWait = 12;
                    this.recovery = QizaiRules.recovery(this.kind);
                    this.cue(serverLevel, this.recovery, true);
                }
                return;
            }
            if (this.remaining > 0 && --this.remaining == 0) {
                this.releasesLeft = 2;
                this.repeatWait = 12;
                this.release(serverLevel);
                this.recovery = QizaiRules.recovery(this.kind);
                this.cue(serverLevel, this.recovery, true);
            } else if (this.remaining == 0 && --this.recovery <= 0) {
                this.kind = 0;
                this.marks.clear();
                this.boss.setQizaiCue(new CompoundTag());
                this.cooldown = this.boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.cooldown(32));
            }
            return;
        }
        if (this.cooldown > 0) {
            --this.cooldown;
        }
        if (!this.target(livingEntity2) || !this.boss.m_142582_((Entity)livingEntity2)) {
            return;
        }
        if (this.stepCooldown == 0) {
            LivingEntity livingEntity3 = ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss).stream().filter(livingEntity -> this.target((LivingEntity)livingEntity) && this.boss.m_20182_().m_82546_(livingEntity.m_20182_()).m_165925_() <= 25.0 && Math.abs(this.boss.m_20186_() - livingEntity.m_20186_()) <= 3.0 && this.boss.m_142582_((Entity)livingEntity)).min(Comparator.comparingDouble(arg_0 -> ((EasterBossEntity)this.boss).m_20280_(arg_0))).orElse(null);
            if (livingEntity3 != null) {
                this.stepCooldown = this.boss.getChallengeDifficulty().acceleratedTicks(20);
                if (this.boss.m_217043_().m_188501_() < 0.4f && this.step(serverLevel, livingEntity3)) {
                    return;
                }
            }
        }
        if (this.cooldown == 0 && this.begin(serverLevel, livingEntity2, ManorLivingTargets.airborneSpell(QizaiRules.next(this.sequence), this.boss.m_5448_()))) {
            ++this.sequence;
        }
    }

    public boolean begin(ServerLevel serverLevel, LivingEntity livingEntity, int n) {
        if (!(serverLevel == this.boss.m_9236_() && !this.busy() && this.eligible(false) && this.target(livingEntity) && this.boss.m_142582_((Entity)livingEntity) && QizaiRules.spell(n))) {
            return false;
        }
        this.marks.clear();
        this.releasesLeft = 0;
        this.repeatWait = 0;
        this.aim = livingEntity.m_146892_();
        if (n == 8) {
            if (!this.skyNova.begin(serverLevel, livingEntity)) {
                return false;
            }
            this.kind = 8;
            this.remaining = 0;
            this.recovery = 0;
            ManorPredictiveAttacks.extraSpell(serverLevel, this.boss, livingEntity, n);
            return true;
        }
        if (n == 7) {
            if (!this.sweep.begin(serverLevel, livingEntity)) {
                return false;
            }
            this.kind = 7;
            this.remaining = 0;
            this.recovery = 0;
            ManorPredictiveAttacks.extraSpell(serverLevel, this.boss, livingEntity, n);
            return true;
        }
        if (n == 9) {
            if (!this.prismLock.begin(serverLevel, livingEntity)) {
                return false;
            }
            this.kind = 9;
            this.remaining = 0;
            this.recovery = 0;
            ManorPredictiveAttacks.extraSpell(serverLevel, this.boss, livingEntity, n);
            return true;
        }
        if (n == 10) {
            if (!this.laserBarrage.begin(serverLevel, livingEntity)) {
                return false;
            }
            this.kind = 10;
            this.remaining = 0;
            this.recovery = 0;
            ManorPredictiveAttacks.extraSpell(serverLevel, this.boss, livingEntity, n);
            return true;
        }
        if (QizaiRules.constellation(n)) {
            this.marks.addAll(QizaiConstellation.marks(serverLevel, (LivingEntity)this.boss, n, livingEntity.m_20182_()));
            if (this.marks.isEmpty()) {
                return false;
            }
        } else if (n != 1) {
            Vec3 vec3 = this.floor(serverLevel, livingEntity.m_20182_());
            if (vec3 == null) {
                return false;
            }
            this.marks.add(vec3);
            if (n == 3) {
                Vec3 vec32 = livingEntity.m_20182_().m_82546_(this.boss.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_().m_82524_(1.5707964f);
                for (int n2 : new int[]{-1, 1}) {
                    Vec3 vec33 = this.floor(serverLevel, vec3.m_82549_(vec32.m_82490_((double)(n2 * 7))));
                    if (vec33 == null) continue;
                    this.marks.add(vec33);
                }
            }
        }
        this.kind = n;
        this.remaining = this.boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.windup(this.kind));
        this.recovery = 0;
        this.boss.m_21573_().m_26573_();
        this.boss.playCast(this.remaining + QizaiRules.repeatTicks() + QizaiRules.recovery(this.kind), this.kind == 3 ? 0 : (this.kind == 2 ? 3 : 2));
        this.cue(serverLevel, this.remaining, false);
        if (this.kind == 3) {
            QizaiPrayerStaffItem.animate((LivingEntity)this.boss, true);
        }
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 1.2f, this.kind == 3 ? 0.65f : 1.2f);
        ManorPredictiveAttacks.extraSpell(serverLevel, this.boss, livingEntity, n);
        return true;
    }

    private Vec3 floor(ServerLevel serverLevel, Vec3 vec3) {
        if (!serverLevel.m_46805_(BlockPos.m_274446_((Position)vec3))) {
            return null;
        }
        BlockHitResult blockHitResult = serverLevel.m_45547_(new ClipContext(vec3.m_82520_(0.0, 0.5, 0.0), vec3.m_82520_(0.0, -10.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss));
        return blockHitResult.m_6662_() == HitResult.Type.BLOCK ? blockHitResult.m_82450_().m_82520_(0.0, 0.035, 0.0) : null;
    }

    private void release(ServerLevel serverLevel) {
        if (QizaiRules.constellation(this.kind)) {
            if (!QizaiMulticast.cast(serverLevel, this.boss, this.kind, this.marks, this.aim)) {
                float f = this.kind == 5 ? 104.0f : 64.0f;
                QizaiConstellation.cast(serverLevel, (LivingEntity)this.boss, this.kind, this.marks, livingEntity -> {
                    if (this.target(livingEntity) && !this.boss.m_7307_((Entity)livingEntity) && !livingEntity.m_7307_((Entity)this.boss)) {
                        livingEntity.f_19802_ = 0;
                        livingEntity.m_6469_(serverLevel.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), f);
                    }
                });
            }
            return;
        }
        if (this.kind == 3) {
            QizaiPrayerStaffItem.animate((LivingEntity)this.boss, false);
        }
        if (this.kind == 1) {
            Vec3 vec3 = this.boss.m_146892_().m_82520_(0.0, -0.15, 0.0);
            Vec3 vec32 = this.aim.m_82546_(vec3).m_82541_();
            for (int i = -1; i <= 1; ++i) {
                QizaiStarEntity qizaiStarEntity = new QizaiStarEntity(serverLevel, this.boss, vec3);
                Vec3 vec33 = vec32.m_82524_((float)i * 0.19f).m_82490_(0.8);
                qizaiStarEntity.m_20256_(vec33);
                serverLevel.m_7967_((Entity)qizaiStarEntity);
            }
        } else {
            block1: for (LivingEntity livingEntity2 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
                if (!this.target(livingEntity2)) continue;
                for (Vec3 vec3 : this.marks) {
                    BlockHitResult blockHitResult;
                    Vec3 vec34 = livingEntity2.m_20182_().m_82546_(vec3);
                    if (!QizaiRules.inside(vec34.m_165925_(), vec34.f_82480_, this.kind) || (blockHitResult = serverLevel.m_45547_(new ClipContext(vec3.m_82520_(0.0, 1.5, 0.0), livingEntity2.m_146892_(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss))).m_6662_() != HitResult.Type.MISS) continue;
                    livingEntity2.f_19802_ = 0;
                    livingEntity2.m_6469_(serverLevel.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), this.kind == 3 ? 128.0f : 80.0f);
                    continue block1;
                }
            }
        }
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_276532_, SoundSource.HOSTILE, 1.3f, this.kind == 3 ? 0.6f : 1.4f);
    }

    private boolean step(ServerLevel serverLevel, LivingEntity livingEntity2) {
        Vec3 vec3 = this.boss.m_20182_();
        Vec3 vec32 = null;
        for (int i = 0; i < 16; ++i) {
            double d;
            double d2 = this.boss.m_217043_().m_188500_() * Math.PI * 2.0;
            double d3 = 4.0 + this.boss.m_217043_().m_188500_() * 4.0;
            Vec3 vec33 = ManorSafeTeleport.find(serverLevel, (Entity)this.boss, vec3.m_82520_(Math.cos(d2) * d3, 0.0, Math.sin(d2) * d3), 1);
            if (vec33 == null || (d = vec33.m_82546_(vec3).m_165925_()) < 16.0 || d > 64.0 || Math.abs(vec33.f_82480_ - vec3.f_82480_) > 3.0 || vec33.m_82557_(livingEntity2.m_20182_()) < 36.0 || ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss).stream().anyMatch(livingEntity -> this.target((LivingEntity)livingEntity) && vec33.m_82557_(livingEntity.m_20182_()) < 9.0) || serverLevel.m_45547_(new ClipContext(this.boss.m_146892_(), vec33.m_82520_(0.0, (double)this.boss.m_20192_(), 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss)).m_6662_() != HitResult.Type.MISS) continue;
            vec32 = vec33;
            break;
        }
        if (vec32 == null) {
            return false;
        }
        this.marks.clear();
        this.marks.add(this.boss.m_20182_());
        ManorSafeTeleport.move(serverLevel, (Entity)this.boss, vec32);
        this.kind = 4;
        this.remaining = 0;
        this.recovery = 12;
        this.aim = livingEntity2.m_146892_();
        this.stepCooldown = this.boss.getChallengeDifficulty().acceleratedTicks(80);
        this.cue(serverLevel, this.recovery, true);
        this.boss.playCast(this.recovery, 4);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, vec3.f_82479_, vec3.f_82480_ + 1.0, vec3.f_82481_, 16, 0.3, 0.6, 0.3, 0.04);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, vec32.f_82479_, vec32.f_82480_ + 1.0, vec32.f_82481_, 16, 0.3, 0.6, 0.3, 0.04);
        serverLevel.m_5594_(null, BlockPos.m_274446_((Position)vec3), SoundEvents.f_11852_, SoundSource.HOSTILE, 0.7f, 1.4f);
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 0.8f, 1.8f);
        return true;
    }

    private void cue(ServerLevel serverLevel, int n, boolean bl) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("qizai", this.kind);
        compoundTag.m_128356_("start", serverLevel.m_46467_());
        compoundTag.m_128405_("duration", n);
        compoundTag.m_128379_("released", bl);
        compoundTag.m_128405_("volley", bl && QizaiRules.spell(this.kind) ? 3 - this.releasesLeft : 0);
        compoundTag.m_128347_("radius", QizaiRules.radius(this.kind));
        ListTag listTag = new ListTag();
        for (Vec3 vec3 : this.marks) {
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.m_128347_("x", vec3.f_82479_);
            compoundTag2.m_128347_("y", vec3.f_82480_);
            compoundTag2.m_128347_("z", vec3.f_82481_);
            listTag.add((Object)compoundTag2);
        }
        compoundTag.m_128365_("marks", (Tag)listTag);
        this.boss.setQizaiCue(compoundTag);
    }

    public void cancel() {
        if (this.uninterruptible()) {
            return;
        }
        this.forceCancel();
    }

    public void forceCancel() {
        QizaiConstellation.cancel((LivingEntity)this.boss);
        this.sweep.cancel();
        this.skyNova.cancel();
        this.prismLock.cancel();
        this.laserBarrage.cancel();
        if (this.kind == 3) {
            QizaiPrayerStaffItem.animate((LivingEntity)this.boss, false);
        }
        if (this.busy()) {
            this.boss.setQizaiCue(new CompoundTag());
        }
        this.kind = 0;
        this.remaining = 0;
        this.recovery = 0;
        this.cooldown = QizaiRules.cooldown(40);
        this.releasesLeft = 0;
        this.repeatWait = 0;
        this.marks.clear();
    }
}

